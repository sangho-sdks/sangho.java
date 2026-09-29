package com.sangho;

import com.sangho.exception.*;
import com.sangho.http.HttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Erreurs typées et retry, en miroir du SDK JS (core/http.ts). */
class HttpClientTest {

    private MockWebServer server;
    private final List<Long> sleeps = new ArrayList<>();

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException { server.shutdown(); }

    private HttpClient http(int maxRetries, Duration timeout) {
        return new HttpClient("sk_test_abc123456789", server.url("/").toString(), timeout, maxRetries, sleeps::add);
    }

    private HttpClient http(int maxRetries) { return http(maxRetries, Duration.ofSeconds(5)); }

    private void respond(int status, String body) {
        server.enqueue(new MockResponse().setResponseCode(status).setBody(body).addHeader("Content-Type", "application/json"));
    }

    @Test
    void acceptsProductionKeysAndRejectsTheOldLivePrefix() {
        assertDoesNotThrow(() -> new HttpClient("sk_prod_abc123456789xyz", "https://api.sangho.ga/v1", Duration.ofSeconds(5)));
        assertThrows(IllegalArgumentException.class,
            () -> new HttpClient("sk_live_abc123456789xyz", "https://api.sangho.ga/v1", Duration.ofSeconds(5)));
    }

    @Test
    void refusesANonHttpsBaseUrlExceptLocalhost() {
        assertThrows(IllegalArgumentException.class,
            () -> new HttpClient("sk_test_abc123456789", "http://api.sangho.ga/v1", Duration.ofSeconds(5)));
        assertDoesNotThrow(() -> new HttpClient("sk_test_abc123456789", "http://localhost:8000/v1", Duration.ofSeconds(5)));
    }

    @Test
    void exposesTypeCodeAndRequestIdFromTheBackend() {
        respond(422, "{\"type\":\"VALIDATION_ERROR\",\"code\":\"AMOUNT_TOO_SMALL\",\"message\":\"Trop petit\",\"param\":\"amount\","
            + "\"errors\":{\"amount\":[\"min 100\"]},\"request_id\":\"req_1\",\"doc_url\":\"https://docs.sangho.ga/errors#AMOUNT_TOO_SMALL\"}");
        SanghoValidationException e = assertThrows(SanghoValidationException.class, () -> http(0).get("/x/", null, Map.class));
        assertEquals("VALIDATION_ERROR", e.getType());
        assertEquals("AMOUNT_TOO_SMALL", e.getCode());
        assertEquals("amount", e.getParam());
        assertEquals("req_1", e.getRequestId());
        assertEquals("amount: min 100", e.getMessage());
        assertEquals(422, e.getStatusCode());
        assertTrue(e.getFieldErrors().containsKey("amount"));
    }

    @Test
    void codeFallsBackToTheTypeWhenTheBackendSendsNone() {
        respond(404, "{\"message\":\"Introuvable\"}");
        SanghoNotFoundException e = assertThrows(SanghoNotFoundException.class, () -> http(0).get("/x/", null, Map.class));
        assertEquals("NOT_FOUND_ERROR", e.getType());
        assertEquals("NOT_FOUND_ERROR", e.getCode());
    }

    @Test
    void distinguishesPublicKeyFromPermissionCaseInsensitively() {
        respond(403, "{\"code\":\"PUBLIC_KEY_NOT_ALLOWED\",\"message\":\"no\"}");
        assertThrows(SanghoPublicKeyException.class, () -> http(0).get("/x/", null, Map.class));
        respond(403, "{\"code\":\"PERMISSION_DENIED\",\"message\":\"no\"}");
        assertThrows(SanghoPermissionException.class, () -> http(0).get("/x/", null, Map.class));
    }

    @Test
    void mapsAuthAndConflict() {
        respond(401, "{\"message\":\"x\"}");
        assertThrows(SanghoAuthException.class, () -> http(0).get("/x/", null, Map.class));
        respond(409, "{\"code\":\"IDEMPOTENCY_CONFLICT\"}");
        SanghoIdempotencyException e = assertThrows(SanghoIdempotencyException.class, () -> http(0).get("/x/", null, Map.class));
        assertEquals("CONFLICT_ERROR", e.getType());
    }

    @Test
    void aNonJsonErrorBodyDoesNotCrash() {
        respond(502, "<html>Bad gateway</html>");
        SanghoException e = assertThrows(SanghoException.class, () -> http(0).get("/x/", null, Map.class));
        assertEquals(502, e.getStatusCode());
    }

    @Test
    void retriesA503WithExponentialBackoffThenSucceeds() {
        respond(503, "{}");
        respond(200, "{\"id\":\"ok\"}");
        Map<?, ?> result = http(3).get("/x/", null, Map.class);
        assertEquals("ok", result.get("id"));
        assertEquals(List.of(500L), sleeps);
    }

    @Test
    void honoursRetryAfterAndNotRetryLater() {
        respond(429, "{\"retry_after\":7}");
        respond(200, "{}");
        http(3).get("/x/", null, Map.class);
        assertEquals(List.of(7000L), sleeps);
    }

    @Test
    void readsTheRetryAfterHeaderWhenTheBodyHasNone() {
        server.enqueue(new MockResponse().setResponseCode(429).setBody("{}").addHeader("Retry-After", "3"));
        SanghoRateLimitException e = assertThrows(SanghoRateLimitException.class, () -> http(0).get("/x/", null, Map.class));
        assertEquals(3, e.getRetryAfter());
        assertEquals("RATE_LIMIT_ERROR", e.getType());
    }

    @Test
    void givesUpAfterMaxRetries() {
        respond(500, "{\"message\":\"boom\"}");
        respond(500, "{\"message\":\"boom\"}");
        respond(500, "{\"message\":\"boom\"}");
        SanghoException e = assertThrows(SanghoException.class, () -> http(2).get("/x/", null, Map.class));
        assertEquals(500, e.getStatusCode());
        assertEquals(2, sleeps.size());
    }

    @Test
    void neverRetriesAPermanent4xx() {
        respond(404, "{\"message\":\"nope\"}");
        assertThrows(SanghoNotFoundException.class, () -> http(3).get("/x/", null, Map.class));
        assertEquals(1, server.getRequestCount());
        assertTrue(sleeps.isEmpty());
    }

    @Test
    void convertsAConnectionFailureToANetworkException() throws IOException {
        server.shutdown();
        SanghoNetworkException e = assertThrows(SanghoNetworkException.class, () -> http(0).get("/x/", null, Map.class));
        assertEquals("NETWORK_ERROR", e.getCode());
    }

    @Test
    void convertsATimeoutToATimeoutException() {
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));
        SanghoTimeoutException e = assertThrows(SanghoTimeoutException.class,
            () -> http(0, Duration.ofMillis(200)).get("/x/", null, Map.class));
        assertEquals("TIMEOUT_ERROR", e.getType());
    }

    @Test
    void sendsAnIdempotencyKeyOnEveryPost() throws InterruptedException {
        respond(201, "{}");
        http(0).post("/x/", Map.of("a", 1), Map.class);
        String key = server.takeRequest().getHeader("Idempotency-Key");
        assertNotNull(key);
        assertEquals(36, key.length());
    }
}
