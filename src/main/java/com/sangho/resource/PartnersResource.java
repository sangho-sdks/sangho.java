package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

/** Partenaires : ressource en lecture seule (list, retrieve, options). */
public class PartnersResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public PartnersResource(HttpClient http) { this.http = http; }

    private static final String PATH = "/partners/";

    public ListResponse<Map<String, Object>> list(Map<String, String> params) {
        http.assertSecretKey("partners.list");
        return http.get(PATH, params, LIST_TYPE);
    }

    public Map<String, Object> retrieve(String id) {
        http.assertSecretKey("partners.retrieve");
        return http.get(PATH + id + "/", null, MAP_TYPE);
    }

    public Map<String, Object> options() {
        return http.options(PATH);
    }
}
