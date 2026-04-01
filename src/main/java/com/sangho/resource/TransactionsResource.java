package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.Transaction;
import com.sangho.model.ListResponse;

import java.util.Map;

public class TransactionsResource {

    private static final TypeReference<ListResponse<Transaction>> LIST_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public TransactionsResource(HttpClient http) { this.http = http; }

    public ListResponse<Transaction> list(Map<String, String> params) {
        http.assertSecretKey("transactions.list");
        return http.get("/transactions/", params, LIST_TYPE);
    }

    public Transaction retrieve(String id) {
        http.assertSecretKey("transactions.retrieve");
        return http.get("/transactions/" + id + "/", null, Transaction.class);
    }

    public Map<String, Object> options() {
        return http.options("/transactions/");
    }
}
