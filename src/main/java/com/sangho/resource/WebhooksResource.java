package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.Webhook;
import com.sangho.model.ListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangho.exception.SanghoException;
import com.sangho.exception.SanghoWebhookSignatureException;
import com.sangho.param.WebhookCreateParams;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

public class WebhooksResource {

    public static final int DEFAULT_TOLERANCE_SECS = 300;

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
        return create(params, events, null);
    }

    public Webhook create(WebhookCreateParams params, java.util.List<String> events, com.sangho.param.RequestOptions options) {
        http.assertSecretKey("webhooks.create");
        Map<String, Object> body = new java.util.LinkedHashMap<>(params.toMap());
        body.put("events", events);
        return http.post("/webhooks/", body, Webhook.class, options);
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
     * Vérifie la signature HMAC-SHA256 et retourne l'événement décodé.
     *
     * <p>En-tête {@code Sangho-Signature: t=<ts>,v1=<hex>[,v1=<hex>…]}, message signé {@code "<ts>.<corps brut>"}.
     * Plusieurs {@code v1} (et une liste de secrets) sont acceptés pour la rotation ; comparaison à temps constant.
     * Passez le corps BRUT reçu.
     *
     * @param payload         corps brut de la requête
     * @param signatureHeader valeur de l'en-tête {@code Sangho-Signature}
     * @param secret          secret du webhook
     * @param toleranceSecs   écart maximal d'horodatage en secondes (défaut 300)
     * @throws SanghoWebhookSignatureException {@code reason} : malformed / expired / mismatch
     */
    public static Map<String, Object> constructEvent(byte[] payload, String signatureHeader, String secret, int toleranceSecs) {
        return constructEvent(payload, signatureHeader, List.of(secret), toleranceSecs);
    }

    public static Map<String, Object> constructEvent(byte[] payload, String signatureHeader, String secret) {
        return constructEvent(payload, signatureHeader, List.of(secret), DEFAULT_TOLERANCE_SECS);
    }

    public static Map<String, Object> constructEvent(String payload, String signatureHeader, String secret) {
        return constructEvent(payload.getBytes(StandardCharsets.UTF_8), signatureHeader, List.of(secret), DEFAULT_TOLERANCE_SECS);
    }

    /** Variante à plusieurs secrets (rotation d'un secret sans interruption). */
    public static Map<String, Object> constructEvent(byte[] payload, String signatureHeader, List<String> secrets, int toleranceSecs) {
        long timestamp = 0;
        List<String> signatures = new ArrayList<>();
        boolean hasTimestamp = false;
        for (String part : signatureHeader == null ? new String[0] : signatureHeader.split(",")) {
            int idx = part.indexOf('=');
            if (idx < 0) continue;
            String key = part.substring(0, idx).trim();
            String value = part.substring(idx + 1).trim();
            if (key.equals("t")) {
                if (!value.matches("\\d+")) throw malformed();
                timestamp = Long.parseLong(value);
                hasTimestamp = true;
            } else if (key.equals("v1") && !value.isEmpty()) {
                signatures.add(value);
            }
        }
        if (!hasTimestamp || signatures.isEmpty()) throw malformed();

        if (Math.abs(Instant.now().getEpochSecond() - timestamp) > toleranceSecs) {
            throw new SanghoWebhookSignatureException(SanghoWebhookSignatureException.EXPIRED, "Webhook timestamp too old.");
        }

        boolean matched = false;
        for (String secret : secrets) {
            if (secret == null || secret.isEmpty()) continue;
            String expected = hmacHex(secret, timestamp, payload);
            // Pas de court-circuit : le temps ne dépend pas du v1 qui correspond.
            for (String received : signatures) {
                if (MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), received.getBytes(StandardCharsets.UTF_8))) {
                    matched = true;
                }
            }
        }
        if (!matched) {
            throw new SanghoWebhookSignatureException(SanghoWebhookSignatureException.MISMATCH, "Webhook signature mismatch.");
        }

        try {
            return new ObjectMapper().readValue(payload, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new SanghoException("Webhook body is not valid JSON.", "invalid_payload", 400, Map.of());
        }
    }

    /** Génère un en-tête {@code Sangho-Signature} valide pour tester votre endpoint webhook. */
    public static String generateTestHeader(String payload, String secret, Long timestamp) {
        long ts = timestamp != null ? timestamp : Instant.now().getEpochSecond();
        return "t=" + ts + ",v1=" + hmacHex(secret, ts, payload.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateTestHeader(String payload, String secret) {
        return generateTestHeader(payload, secret, null);
    }

    private static SanghoWebhookSignatureException malformed() {
        return new SanghoWebhookSignatureException(SanghoWebhookSignatureException.MALFORMED, "Invalid Sangho-Signature header.");
    }

    private static String hmacHex(String secret, long timestamp, byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(mac.doFinal(payload));
        } catch (GeneralSecurityException e) {
            throw new SanghoException("Webhook verification failed: " + e.getMessage());
        }
    }
}
