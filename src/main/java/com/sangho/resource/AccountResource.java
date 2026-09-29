package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

/** {@code client.account().retrieve()} — l'application liée à la clé secrète utilisée (introspection « qui suis-je »). */
public class AccountResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public AccountResource(HttpClient http) { this.http = http; }

    public Map<String, Object> retrieve() {
        http.assertSecretKey("account.retrieve");
        return http.get("/account/", null, MAP_TYPE);
    }
}
