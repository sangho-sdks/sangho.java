package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

/** Moyens de paiement : list, retrieve, attach, detach, setDefault, options. Créés par le paiement lui-même : l'\''API n'\''expose ni création ni modification directe. */
public class PaymentMethodsResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public PaymentMethodsResource(HttpClient http) { this.http = http; }

    private static final String PATH = "/payment-methods/";

    public ListResponse<Map<String, Object>> list(Map<String, String> params) {
        http.assertSecretKey("paymentMethods.list");
        return http.get(PATH, params, LIST_TYPE);
    }

    public Map<String, Object> retrieve(String id) {
        http.assertSecretKey("paymentMethods.retrieve");
        return http.get(PATH + id + "/", null, MAP_TYPE);
    }

    public Map<String, Object> setDefault(String id) {
        http.assertSecretKey("paymentMethods.setDefault");
        return http.post(PATH + id + "/set-default/", Map.of(), Map.class);
    }

    public Map<String, Object> attach(String id, String customerId) {
        http.assertSecretKey("paymentMethods.attach");
        return http.post(PATH + id + "/attach/", Map.of("customer", customerId), Map.class);
    }

    public Map<String, Object> detach(String id) {
        http.assertSecretKey("paymentMethods.detach");
        return http.post(PATH + id + "/detach/", Map.of(), Map.class);
    }

    public Map<String, Object> options() {
        return http.options(PATH);
    }
}
