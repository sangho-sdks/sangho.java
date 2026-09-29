package com.sangho;

import com.sangho.exception.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws IOException { server.shutdown(); }

    private SanghoClient client(String apiKey, int maxRetries) {
        return SanghoClient.builder()
            .apiKey(apiKey)
            .baseUrl(server.url("/").toString())
            .maxRetries(maxRetries)
            .build();
    }

    @Test
    void testRateLimitReadsRetryAfterField() {
        server.enqueue(new MockResponse()
            .setResponseCode(429)
            .setBody("{\"message\":\"Rate limit\",\"retry_after\":30}")
            .addHeader("Content-Type", "application/json"));

        SanghoClient c = client("sk_test_abc123456789", 0);
        SanghoRateLimitException ex = assertThrows(SanghoRateLimitException.class,
            () -> c.customers().list(null));
        assertEquals(30, ex.getRetryAfter());
    }

    @Test
    void testPublicKeyErrorCaseInsensitive() {
        server.enqueue(new MockResponse()
            .setResponseCode(403)
            .setBody("{\"message\":\"nope\",\"code\":\"PUBLIC_KEY_NOT_ALLOWED\"}")
            .addHeader("Content-Type", "application/json"));

        SanghoClient c = client("sk_test_abc123456789", 0);
        assertThrows(SanghoPublicKeyException.class, () -> c.customers().retrieve("x"));
    }

    @Test
    void testErrorExposesType() {
        server.enqueue(new MockResponse()
            .setResponseCode(401)
            .setBody("{\"message\":\"bad key\"}")
            .addHeader("Content-Type", "application/json"));

        SanghoClient c = client("sk_test_abc123456789", 0);
        SanghoAuthException ex = assertThrows(SanghoAuthException.class,
            () -> c.customers().list(null));
        assertEquals("AUTHENTICATION_ERROR", ex.getType());
    }

    @Test
    void testRetryThenSucceeds() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"message\":\"err\"}"));
        server.enqueue(new MockResponse()
            .setResponseCode(200)
            .setBody("{\"id\":\"cust_1\",\"email\":\"a@b.com\"}")
            .addHeader("Content-Type", "application/json"));

        SanghoClient c = client("sk_test_abc123456789", 3);
        var customer = c.customers().retrieve("cust_1");
        assertEquals("cust_1", customer.id());
        assertEquals(2, server.getRequestCount());
    }

    @Test
    void testInvalidKeyPrefixRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            SanghoClient.builder().apiKey("pk_live_" + "x".repeat(20)).build());
    }

    @Test
    void testNonHttpsBaseUrlRejected() {
        assertThrows(IllegalArgumentException.class, () ->
            SanghoClient.builder()
                .apiKey("sk_test_" + "x".repeat(20))
                .baseUrl("http://api.sangho.ga/v1")
                .build());
    }

    @Test
    void testLocalhostHttpAllowed() {
        assertDoesNotThrow(() ->
            SanghoClient.builder()
                .apiKey("sk_test_" + "x".repeat(20))
                .baseUrl("http://localhost:8000/v1")
                .build());
    }
}
