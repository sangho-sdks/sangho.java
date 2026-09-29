package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.http.HttpClient;
import com.sangho.param.RequestOptions;

import java.util.LinkedHashMap;
import java.util.Map;

/** Comptes des vendeurs d'une plateforme et KYC hébergé par Sangho. */
public class ConnectAccountsResource {

    private static final String PATH = "/connect/accounts/";
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public ConnectAccountsResource(HttpClient http) { this.http = http; }

    /**
     * Crée (ou retrouve) le compte d'un vendeur — IDEMPOTENT par {@code external_id}. {@code claim_token} n'est
     * renvoyé QU'À la création : transmettez-le au vendeur sans le stocker ni le journaliser
     * ({@link #reissueClaimToken} en émet un nouveau).
     */
    public Map<String, Object> create(String externalId, String email, Map<String, Object> extra, RequestOptions options) {
        http.assertSecretKey("connect.accounts.create");
        Map<String, Object> body = new LinkedHashMap<>();
        if (extra != null) body.putAll(extra);
        body.put("external_id", externalId);
        body.put("email", email);
        return http.post(PATH, body, Map.class, options);
    }

    public Map<String, Object> create(String externalId, String email) {
        return create(externalId, email, null, null);
    }

    public Map<String, Object> retrieve(String id) {
        http.assertSecretKey("connect.accounts.retrieve");
        return http.get(PATH + id + "/", null, MAP_TYPE);
    }

    /** Liste les comptes de la plateforme ({@code object=list}, {@code data}, non paginée). */
    public Map<String, Object> list() {
        http.assertSecretKey("connect.accounts.list");
        return http.get(PATH, null, MAP_TYPE);
    }

    /** Réémet le jeton de réclamation d'un compte encore {@code pending_claim} ; l'ancien est invalidé. */
    public Map<String, Object> reissueClaimToken(String id) {
        http.assertSecretKey("connect.accounts.reissueClaimToken");
        return http.post(PATH + id + "/claim-token/", Map.of(), Map.class);
    }

    /**
     * Lance le KYC hébergé par Sangho ; redirigez le vendeur vers {@code session.url} (valable environ une heure).
     * Le compte doit avoir été réclamé (sinon {@code SanghoConflictException}, code {@code account_not_claimed}).
     */
    public Map<String, Object> createKycSession(String id, String returnUrl, String refreshUrl, RequestOptions options) {
        http.assertSecretKey("connect.accounts.createKycSession");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("return_url", returnUrl);
        if (refreshUrl != null && !refreshUrl.isEmpty()) body.put("refresh_url", refreshUrl);
        return http.post(PATH + id + "/kyc-session/", body, Map.class, options);
    }

    /** Soldes : available, held, frozen, reserve, negative, paid_out (chaînes décimales). */
    public Map<String, Object> balance(String id) {
        http.assertSecretKey("connect.accounts.balance");
        return http.get(PATH + id + "/balance/", null, MAP_TYPE);
    }

    /** Retrait vers Mobile Money / banque, limité au disponible POSITIF. Clé d'idempotence OBLIGATOIRE. */
    public Map<String, Object> createPayout(String id, Object amount, String destination, String idempotencyKey) {
        http.assertSecretKey("connect.accounts.createPayout");
        String key = ConnectPaymentsResource.requireKey("connect.accounts.createPayout", idempotencyKey);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("amount", amount);
        body.put("destination", destination);
        return http.post(PATH + id + "/payouts/", body, Map.class, key);
    }

    public Map<String, Object> listPayouts(String id) {
        http.assertSecretKey("connect.accounts.listPayouts");
        return http.get(PATH + id + "/payouts/", null, MAP_TYPE);
    }
}
