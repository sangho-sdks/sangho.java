package com.sangho.resource;

import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

public class CheckoutSessionsResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;
    private final String path = "/checkout-sessions/";

    public CheckoutSessionsResource(HttpClient http) { this.http = http; }

    public ListResponse<Map<String, Object>> list(Map<String, String> params) {
        http.assertSecretKey("checkoutSessions.list");
        return http.get(path, params, LIST_TYPE);
    }

    public Map<String, Object> retrieve(String id) {
        // Le backend autorise explicitement la clé publique sur cette action
        // (page de confirmation côté navigateur) — ne pas la bloquer ici.
        return http.get(path + id + "/", null, MAP_TYPE);
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        http.assertSecretKey("checkoutSessions.create");
        return http.post(path, payload, Map.class);
    }

    public Map<String, Object> update(String id, Map<String, Object> payload) {
        http.assertSecretKey("checkoutSessions.update");
        return http.patch(path + id + "/", payload, Map.class);
    }

    public void delete(String id) {
        http.assertSecretKey("checkoutSessions.delete");
        http.delete(path + id + "/");
    }

    public Map<String, Object> expire(String id) {
        http.assertSecretKey("checkoutSessions.expire");
        return http.post(path + id + "/expire/", Map.of(), Map.class);
    }

    public Map<String, Object> options() {
        return http.options(path);
    }
}
