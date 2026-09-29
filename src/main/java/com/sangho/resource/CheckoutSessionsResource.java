package com.sangho.resource;

import com.sangho.param.RequestOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

/** Sessions de paiement : list, retrieve, create, expire, delete, options. */
public class CheckoutSessionsResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public CheckoutSessionsResource(HttpClient http) { this.http = http; }

    private static final String PATH = "/checkout-sessions/";

    public ListResponse<Map<String, Object>> list(Map<String, String> params) {
        http.assertSecretKey("checkoutSessions.list");
        return http.get(PATH, params, LIST_TYPE);
    }

    /** Le backend autorise explicitement la clé publique sur cette action (page de confirmation côté navigateur). */
    public Map<String, Object> retrieve(String id) {
        return http.get(PATH + id + "/", null, MAP_TYPE);
    }

    /** {@code payload} accepte {@code idempotency_key} : rejeu sans doublon. */
    public Map<String, Object> create(Map<String, Object> payload) {
        return create(payload, null);
    }

    public Map<String, Object> create(Map<String, Object> payload, RequestOptions options) {
        http.assertSecretKey("checkoutSessions.create");
        return http.post(PATH, payload, Map.class, options);
    }

    public Map<String, Object> expire(String id) {
        http.assertSecretKey("checkoutSessions.expire");
        return http.post(PATH + id + "/expire/", Map.of(), Map.class);
    }

    public void delete(String id) {
        http.assertSecretKey("checkoutSessions.delete");
        http.delete(PATH + id + "/");
    }

    public Map<String, Object> options() {
        return http.options(PATH);
    }
}
