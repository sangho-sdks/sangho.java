package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

public class SecurityResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private final HttpClient http;
    private final String path = "/security/";

    public SecurityResource(HttpClient http) { this.http = http; }

    public ListResponse<Map<String, Object>> list(Map<String, String> params) {
        http.assertSecretKey("security.list");
        return http.get(path, params, LIST_TYPE);
    }

    public Map<String, Object> retrieve(String id) {
        http.assertSecretKey("security.retrieve");
        return http.get(path + id + "/", null, new TypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        http.assertSecretKey("security.create");
        return http.post(path, payload, Map.class);
    }

    public Map<String, Object> update(String id, Map<String, Object> payload) {
        http.assertSecretKey("security.update");
        return http.patch(path + id + "/", payload, Map.class);
    }

    public void delete(String id) {
        http.assertSecretKey("security.delete");
        http.delete(path + id + "/");
    }

    public Map<String, Object> options() {
        return http.options(path);
    }
}
