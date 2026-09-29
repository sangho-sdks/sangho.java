package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

/** Profil de sécurité de l'\''application : retrieve, update, addAllowedIps, removeAllowedIps, options. */
public class SecurityResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public SecurityResource(HttpClient http) { this.http = http; }

    private static final String PATH = "/security/";

    public Map<String, Object> retrieve() {
        http.assertSecretKey("security.retrieve");
        return http.get(PATH + "me/", null, MAP_TYPE);
    }

    public Map<String, Object> update(Map<String, Object> payload) {
        http.assertSecretKey("security.update");
        return http.patch(PATH + "update_me/", payload, Map.class);
    }

    /** Pas d'action dédiée côté backend : on relit le profil, on recompose la liste puis on la renvoie via {@link #update}. */
    @SuppressWarnings("unchecked")
    public Map<String, Object> addAllowedIps(java.util.List<String> ips) {
        http.assertSecretKey("security.addAllowedIps");
        java.util.LinkedHashSet<String> merged = new java.util.LinkedHashSet<>();
        Object current = retrieve().get("allowed_ips");
        if (current instanceof java.util.List<?> l) l.forEach(i -> merged.add(String.valueOf(i)));
        merged.addAll(ips);
        return update(Map.of("allowed_ips", new java.util.ArrayList<>(merged)));
    }

    public Map<String, Object> removeAllowedIps(java.util.List<String> ips) {
        http.assertSecretKey("security.removeAllowedIps");
        java.util.List<String> remaining = new java.util.ArrayList<>();
        Object current = retrieve().get("allowed_ips");
        if (current instanceof java.util.List<?> l) l.forEach(i -> { if (!ips.contains(String.valueOf(i))) remaining.add(String.valueOf(i)); });
        return update(Map.of("allowed_ips", remaining));
    }

    public Map<String, Object> options() {
        return http.options(PATH);
    }
}
