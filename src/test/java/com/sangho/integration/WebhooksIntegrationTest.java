package com.sangho.integration;

import com.sangho.exception.SanghoException;
import com.sangho.exception.SanghoNotFoundException;
import com.sangho.model.Webhook;
import com.sangho.resource.WebhooksResource;
import com.sangho.param.WebhookCreateParams;
import org.junit.jupiter.api.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class WebhooksIntegrationTest extends IntegrationTestBase {

    private static Webhook sharedWebhook;

    @BeforeAll
    static void createWebhook() {
        sharedWebhook = client.webhooks().create(
            WebhookCreateParams.builder()
                .url("https://webhook.site/" + UUID.randomUUID())
                .build(),
            List.of("payment_intent.succeeded", "customer.created")
        );
    }

    @AfterAll
    static void deleteWebhook() {
        if (sharedWebhook != null) {
            try { client.webhooks().delete(sharedWebhook.id()); } catch (Exception ignored) {}
        }
    }

    @Test
    void testCreateWebhook() {
        Webhook wh = client.webhooks().create(
            WebhookCreateParams.builder()
                .url("https://webhook.site/" + UUID.randomUUID())
                .build(),
            List.of("payment_intent.succeeded")
        );
        assertNotNull(wh.id());
        client.webhooks().delete(wh.id());
    }

    @Test
    void testRetrieveWebhook() {
        Webhook retrieved = client.webhooks().retrieve(sharedWebhook.id());
        assertEquals(sharedWebhook.id(), retrieved.id());
    }

    @Test
    void testListWebhooks() {
        var result = client.webhooks().list(null);
        assertNotNull(result.data());
    }

    @Test
    void testRollSecret() {
        Webhook result = client.webhooks().rollSecret(sharedWebhook.id());
        assertEquals(sharedWebhook.id(), result.id());
    }

    @Test
    void testListDeliveries() {
        var result = client.webhooks().listDeliveries(sharedWebhook.id(), null);
        assertNotNull(result);
    }

    @Test
    void testRetrieveNonexistentThrowsNotFound() {
        assertThrows(SanghoNotFoundException.class,
            () -> client.webhooks().retrieve("wh_doesnotexist000"));
    }

    // ── Signature verification (offline) ─────────────────────────────────────

    @Test
    void testConstructEventValidSignature() throws Exception {
        String secret  = "whsec_test_integration_java";
        String payload = "{\"event\":\"payment_intent.succeeded\"}";
        long   ts      = Instant.now().getEpochSecond();

        byte[] signedPayload = (ts + "." + payload).getBytes(StandardCharsets.UTF_8);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        String sig    = HexFormat.of().formatHex(mac.doFinal(signedPayload));
        String header = "t=" + ts + ",v1=" + sig;

        Map<String, Object> event = WebhooksResource.constructEvent(
            payload.getBytes(StandardCharsets.UTF_8), header, secret, 300
        );
        assertEquals("payment_intent.succeeded", event.get("event"));
    }

    @Test
    void testConstructEventWrongSecretRaises() throws Exception {
        String payload = "{\"event\":\"test\"}";
        long   ts      = Instant.now().getEpochSecond();
        byte[] signed  = (ts + "." + payload).getBytes(StandardCharsets.UTF_8);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("correct".getBytes(), "HmacSHA256"));
        String sig    = HexFormat.of().formatHex(mac.doFinal(signed));
        String header = "t=" + ts + ",v1=" + sig;

        assertThrows(SanghoException.class, () ->
            WebhooksResource.constructEvent(payload.getBytes(), header, "wrong", 300));
    }

    @Test
    void testConstructEventStaleTimestampRaises() throws Exception {
        String secret  = "whsec_test";
        String payload = "{\"event\":\"test\"}";
        long   oldTs   = Instant.now().getEpochSecond() - 600;
        byte[] signed  = (oldTs + "." + payload).getBytes(StandardCharsets.UTF_8);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
        String sig    = HexFormat.of().formatHex(mac.doFinal(signed));
        String header = "t=" + oldTs + ",v1=" + sig;

        assertThrows(SanghoException.class, () ->
            WebhooksResource.constructEvent(payload.getBytes(), header, secret, 300));
    }
}
