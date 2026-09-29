package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.Webhook;
import com.sangho.model.ListResponse;
import com.sangho.exception.SanghoException;
import com.sangho.param.WebhookCreateParams;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

public class WebhooksResource {

    private static final TypeReference<ListResponse<Webhook>> LIST_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public WebhooksResource(HttpClient http) { this.http = http; }

    public ListResponse<Webhook> list(Map<String, String> params) {
        http.assertSecretKey("webhooks.list");
        return http.get("/webhooks/", params, LIST_TYPE);
    }

    public Webhook retrieve(String id) {
        http.assertSecretKey("webhooks.retrieve");
        return http.get("/webhooks/" + id + "/", null, Webhook.class);
    }

    public Webhook create(WebhookCreateParams params, java.util.List<String> events) {
        http.assertSecretKey("webhooks.create");
        Map<String, Object> body = new java.util.LinkedHashMap<>(params.toMap());
        body.put("events", events);
        return http.post("/webhooks/", body, Webhook.class);
    }

    public Webhook update(String id, Map<String, Object> payload) {
        http.assertSecretKey("webhooks.update");
        return http.patch("/webhooks/" + id + "/", payload, Webhook.class);
    }

    public void delete(String id) {
        http.assertSecretKey("webhooks.delete");
        http.delete("/webhooks/" + id + "/");
    }

    public Webhook rollSecret(String id) {
        http.assertSecretKey("webhooks.rollSecret");
        return http.post("/webhooks/" + id + "/roll-secret/", Map.of(), Webhook.class);
    }

    public Map<String, Object> sendTestEvent(String id, String eventType) {
        http.assertSecretKey("webhooks.sendTestEvent");
        return http.post("/webhooks/" + id + "/test/", Map.of("event_type", eventType), Map.class);
    }

    public Map<String, Object> retryDelivery(String id, String deliveryId) {
        http.assertSecretKey("webhooks.retryDelivery");
        return http.post("/webhooks/" + id + "/deliveries/" + deliveryId + "/retry/", Map.of(), Map.class);
    }

    public Webhook disable(String id) {
        http.assertSecretKey("webhooks.disable");
        return http.post("/webhooks/" + id + "/disable/", Map.of(), Webhook.class);
    }

    public Webhook enable(String id) {
        http.assertSecretKey("webhooks.enable");
        return http.post("/webhooks/" + id + "/enable/", Map.of(), Webhook.class);
    }

    public ListResponse<Map<String, Object>> listDeliveries(String id, Map<String, String> params) {
        http.assertSecretKey("webhooks.listDeliveries");
        TypeReference<ListResponse<Map<String, Object>>> ref = new TypeReference<>() {};
        return http.get("/webhooks/" + id + "/deliveries/", params, ref);
    }

    public Map<String, Object> retrieveDelivery(String id, String deliveryId) {
        http.assertSecretKey("webhooks.retrieveDelivery");
        return http.get("/webhooks/" + id + "/deliveries/" + deliveryId + "/", null, new TypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> options() {
        return http.options("/webhooks/");
    }

    /**
     * Verify HMAC-SHA256 signature and return the parsed event payload.
     *
     * @param payload         Raw request body bytes
     * @param signatureHeader Value of the {@code Sangho-Signature} header
     * @param secret          Webhook signing secret
     * @param toleranceSecs   Maximum age of the event in seconds (default 300)
     */
    public static Map<String, Object> constructEvent(
        byte[] payload,
        String signatureHeader,
        String secret,
        int toleranceSecs
    ) {
        Map<String, String> parts = new java.util.LinkedHashMap<>();
        for (String part : signatureHeader.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) parts.put(kv[0], kv[1]);
        }

        String timestamp = parts.get("t");
        String v1        = parts.get("v1");

        if (timestamp == null || v1 == null)
            throw new SanghoException("Invalid Sangho-Signature header.", "invalid_signature", 0, Map.of());

        long ts = Long.parseLong(timestamp);
        if (Math.abs(Instant.now().getEpochSecond() - ts) > toleranceSecs)
            throw new SanghoException("Webhook timestamp too old.", "stale_event", 0, Map.of());

        try {
            byte[] signedPayload = (timestamp + ".").getBytes(StandardCharsets.UTF_8);
            byte[] combined = new byte[signedPayload.length + payload.length];
            System.arraycopy(signedPayload, 0, combined, 0, signedPayload.length);
            System.arraycopy(payload, 0, combined, signedPayload.length, payload.length);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(mac.doFinal(combined));

            if (!java.security.MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), v1.getBytes(StandardCharsets.UTF_8)))
                throw new SanghoException("Webhook signature mismatch.", "invalid_signature", 0, Map.of());

            return new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (SanghoException e) {
            throw e;
        } catch (Exception e) {
            throw new SanghoException("Webhook verification failed: " + e.getMessage());
        }
    }
}
