package com.sangho.resource;

import com.sangho.param.RequestOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.model.ListResponse;

import java.util.Map;

/** Liens de paiement : list, retrieve, create, update, delete (archive), archive, restore, options. */
public class PaymentLinksResource {

    private static final TypeReference<ListResponse<Map<String, Object>>> LIST_TYPE = new TypeReference<>() {};
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public PaymentLinksResource(HttpClient http) { this.http = http; }

    private static final String PATH = "/payment-links/";

    public ListResponse<Map<String, Object>> list(Map<String, String> params) {
        http.assertSecretKey("paymentLinks.list");
        return http.get(PATH, params, LIST_TYPE);
    }

    public Map<String, Object> retrieve(String id) {
        http.assertSecretKey("paymentLinks.retrieve");
        return http.get(PATH + id + "/", null, MAP_TYPE);
    }

    /** {@code payload} accepte {@code idempotency_key} : rejeu sans doublon. */
    public Map<String, Object> create(Map<String, Object> payload) {
        return create(payload, null);
    }

    public Map<String, Object> create(Map<String, Object> payload, RequestOptions options) {
        http.assertSecretKey("paymentLinks.create");
        return http.post(PATH, payload, Map.class, options);
    }

    public Map<String, Object> update(String id, Map<String, Object> payload) {
        http.assertSecretKey("paymentLinks.update");
        return http.patch(PATH + id + "/", payload, Map.class);
    }

    /** Pas de suppression physique côté API : {@code DELETE} archive le lien. */
    public void delete(String id) {
        http.assertSecretKey("paymentLinks.delete");
        http.delete(PATH + id + "/");
    }

    public Map<String, Object> archive(String id) {
        http.assertSecretKey("paymentLinks.archive");
        return http.post(PATH + id + "/archive/", Map.of(), Map.class);
    }

    public Map<String, Object> restore(String id) {
        http.assertSecretKey("paymentLinks.restore");
        return http.post(PATH + id + "/restore/", Map.of(), Map.class);
    }

    public Map<String, Object> options() {
        return http.options(PATH);
    }
}
