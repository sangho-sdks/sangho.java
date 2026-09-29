package com.sangho;

import com.sangho.exception.SanghoNetworkException;
import com.sangho.exception.SanghoTimeoutException;
import com.sangho.param.CustomerCreateParams;
import com.sangho.param.PaymentIntentCreateParams;
import com.sangho.param.RequestOptions;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Idempotence : clé fournie par l'appelant, aucun rejeu d'un POST sans clé après une erreur réseau. */
class IdempotencyTest {

    private MockWebServer server;
    private SanghoClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = SanghoClient.builder().apiKey("sk_test_abc123456789").baseUrl(server.url("/").toString())
            .maxRetries(1).build();
    }

    @AfterEach
    void tearDown() throws IOException { server.shutdown(); }

    private void ok(String body) {
        server.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));
    }

    private void drop() {
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));
    }

    private static CustomerCreateParams customer() {
        return CustomerCreateParams.builder().email("a@b.ga").name("A").build();
    }

    @Test
    void forwardsTheCallerKeyForTypedParams() throws Exception {
        ok("{\"id\":\"c\"}");
        client.customers().create(customer(), RequestOptions.idempotencyKey("cust-1"));
        assertEquals("cust-1", server.takeRequest().getHeader("Idempotency-Key"));
    }

    @Test
    void mapBodiesAcceptIdempotencyKeyWithoutSendingItInTheBody() throws Exception {
        ok("{\"id\":\"cs\"}");
        client.checkoutSessions().create(Map.of("success_url", "https://x/ok", "idempotency_key", "cs-1"));
        RecordedRequest req = server.takeRequest();
        assertEquals("cs-1", req.getHeader("Idempotency-Key"));
        assertFalse(req.getBody().readUtf8().contains("idempotency_key"));
    }

    @Test
    void generatesAKeyWhenTheCallerGivesNone() throws Exception {
        ok("{\"id\":\"c\"}");
        client.customers().create(customer());
        String key = server.takeRequest().getHeader("Idempotency-Key");
        assertNotNull(key);
        assertTrue(key.length() > 20);
    }

    @Test
    void doesNotReplayAPostWithoutKeyAfterANetworkError() {
        drop();
        ok("{\"id\":\"c\"}");
        assertThrows(SanghoNetworkException.class, () -> client.customers().create(customer()));
        assertEquals(1, server.getRequestCount());
    }

    @Test
    void replaysAPostWithAKeyAfterANetworkError() throws Exception {
        drop();
        ok("{\"id\":\"c\"}");
        assertNotNull(client.customers().create(customer(), RequestOptions.idempotencyKey("cust-2")));
        assertEquals(2, server.getRequestCount());
        server.takeRequest();  // la connexion coupée n'enregistre aucun en-tête
        assertEquals("cust-2", server.takeRequest().getHeader("Idempotency-Key"));
    }

    @Test
    void paymentIntentSendsCurrencyAndCustomerEmail() throws Exception {
        ok("{\"id\":\"pi\"}");
        client.paymentIntents().create(PaymentIntentCreateParams.builder().amount(5000).customerEmail("a@b.ga").build());
        String body = server.takeRequest().getBody().readUtf8();
        assertTrue(body.contains("\"currency\":\"XAF\"") && body.contains("\"customer_email\":\"a@b.ga\""));
    }

    @SuppressWarnings("unused")
    private static Class<?>[] unused() { return new Class<?>[] {SanghoTimeoutException.class}; }
}
