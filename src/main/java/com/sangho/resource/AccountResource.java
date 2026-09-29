package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;

import java.util.Map;

public class AccountResource {

    private final HttpClient http;
    private final String path = "/account/";

    public AccountResource(HttpClient http) { this.http = http; }

    /**
     * Renvoie l'App associée à la clé secrète utilisée pour cette requête.
     * Équivalent à apps().retrieve(id) sans avoir besoin de connaître l'id à
     * l'avance — pratique comme "qui suis-je" / health-check.
     */
    public Map<String, Object> retrieve() {
        http.assertSecretKey("account.retrieve");
        return http.get(path, null, new TypeReference<Map<String, Object>>() {});
    }
}
