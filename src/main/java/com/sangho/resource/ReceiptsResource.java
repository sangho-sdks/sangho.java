package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

/** Reçus : list, retrieve, getPdfUrl, options (lecture seule). */
public class ReceiptsResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public ReceiptsResource(HttpClient http) { this.http = http; }

    private static final String PATH = "/receipts/";

    public ListResponse<Map<String, Object>> list(Map<String, String> params) {
        http.assertSecretKey("receipts.list");
        return http.get(PATH, params, LIST_TYPE);
    }

    public Map<String, Object> retrieve(String id) {
        http.assertSecretKey("receipts.retrieve");
        return http.get(PATH + id + "/", null, MAP_TYPE);
    }

    /** URL signée et expirante du PDF : {@code url}, {@code expires_at}. */
    public Map<String, Object> getPdfUrl(String id) {
        http.assertSecretKey("receipts.getPdfUrl");
        return http.get(PATH + id + "/pdf/", null, MAP_TYPE);
    }

    public Map<String, Object> options() {
        return http.options(PATH);
    }
}
