package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

/** {@code client.sandbox().reset()} — purge les données de test de l'\''application. Refusé côté backend avec une clé de production (sk_prod_*). */
public class SandboxResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public SandboxResource(HttpClient http) { this.http = http; }

    public Map<String, Object> reset() {
        http.assertSecretKey("sandbox.reset");
        return http.post("/reset/", Map.of(), Map.class);
    }
}
