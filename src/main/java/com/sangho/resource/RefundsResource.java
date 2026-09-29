package com.sangho.resource;

import com.sangho.param.RequestOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.Refund;
import com.sangho.model.ListResponse;
import com.sangho.param.RefundCreateParams;

import java.util.Map;

public class RefundsResource {

    private static final TypeReference<ListResponse<Refund>> LIST_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public RefundsResource(HttpClient http) { this.http = http; }

    public ListResponse<Refund> list(Map<String, String> params) {
        http.assertSecretKey("refunds.list");
        return http.get("/refunds/", params, LIST_TYPE);
    }

    public Refund retrieve(String id) {
        http.assertSecretKey("refunds.retrieve");
        return http.get("/refunds/" + id + "/", null, Refund.class);
    }

    public Refund create(RefundCreateParams params) {
        return create(params, null);
    }

    /** Comme {@link #create(RefundCreateParams)}, avec une clé d'idempotence (rejeu sans doublon). */
    public Refund create(RefundCreateParams params, RequestOptions options) {
        http.assertSecretKey("refunds.create");
        return http.post("/refunds/", params, Refund.class, options);
    }

    public Refund cancel(String id) {
        http.assertSecretKey("refunds.cancel");
        return http.post("/refunds/" + id + "/cancel/", Map.of(), Refund.class);
    }

    public Map<String, Object> options() {
        return http.options("/refunds/");
    }
}
