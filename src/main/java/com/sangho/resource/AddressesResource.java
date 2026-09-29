package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

/** Adresses : list, retrieve, create, update, delete, options. */
public class AddressesResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public AddressesResource(HttpClient http) { this.http = http; }

    private static final String PATH = "/addresses/";

    public ListResponse<Map<String, Object>> list(Map<String, String> params) {
        http.assertSecretKey("addresses.list");
        return http.get(PATH, params, LIST_TYPE);
    }

    public Map<String, Object> retrieve(String id) {
        http.assertSecretKey("addresses.retrieve");
        return http.get(PATH + id + "/", null, MAP_TYPE);
    }

    public Map<String, Object> create(Map<String, Object> payload) {
        http.assertSecretKey("addresses.create");
        return http.post(PATH, payload, Map.class);
    }

    public Map<String, Object> update(String id, Map<String, Object> payload) {
        http.assertSecretKey("addresses.update");
        return http.patch(PATH + id + "/", payload, Map.class);
    }

    public void delete(String id) {
        http.assertSecretKey("addresses.delete");
        http.delete(PATH + id + "/");
    }

    public Map<String, Object> options() {
        return http.options(PATH);
    }
}
