package com.sangho.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sangho.exception.SanghoValidationException;
import com.sangho.http.HttpClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Instructions sur un paiement avec répartition ({@code cpay_…}). La plateforme DÉCIDE, Sangho EXÉCUTE : libérer,
 * rembourser, geler. Création : {@code checkoutSessions().create(...)} avec un objet {@code connect}.
 */
public class ConnectPaymentsResource {

    private static final String PATH = "/connect/payments/";
    private static final List<String> REFUND_SCOPES = List.of("product", "full", "amount");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};
    private final HttpClient http;

    public ConnectPaymentsResource(HttpClient http) { this.http = http; }

    /**
     * Les écritures d'argent exigent une clé d'idempotence STABLE (ex. {@code release-<commande>}) : un rejeu ne doit
     * jamais dupliquer l'opération.
     */
    static String requireKey(String method, String key) {
        if (key == null || key.isEmpty()) {
            throw new SanghoValidationException(
                "'" + method + "' exige une clé d'idempotence stable, ex : 'release-<order_id>'.", Map.of());
        }
        return key;
    }

    public Map<String, Object> retrieve(String id) {
        http.assertSecretKey("connect.payments.retrieve");
        return http.get(PATH + id + "/", null, MAP_TYPE);
    }

    /** Libère les fonds bloqués (ou gelés) au vendeur, commission retenue. Événement {@code funds.released}. */
    public Map<String, Object> release(String id, String idempotencyKey) {
        http.assertSecretKey("connect.payments.release");
        String key = requireKey("connect.payments.release", idempotencyKey);
        return http.post(PATH + id + "/release/", Map.of(), Map.class, key);
    }

    /**
     * Rembourse le client. {@code scope} : {@code product} (livraison conservée), {@code full} (produit + livraison)
     * ou {@code amount} (montant libre, {@code amount} requis).
     */
    public Map<String, Object> refund(String id, String scope, String idempotencyKey, Object amount, String reason) {
        http.assertSecretKey("connect.payments.refund");
        String key = requireKey("connect.payments.refund", idempotencyKey);
        if (!REFUND_SCOPES.contains(scope)) {
            throw new SanghoValidationException("scope doit valoir \"product\", \"full\" ou \"amount\".", Map.of());
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("scope", scope);
        if (amount != null) body.put("amount", amount);
        if (reason != null && !reason.isEmpty()) body.put("reason", reason);
        return http.post(PATH + id + "/refund/", body, Map.class, key);
    }

    /** Bloqué → gelé (litige) ; événement {@code funds.frozen}. */
    public Map<String, Object> freeze(String id, String idempotencyKey) {
        http.assertSecretKey("connect.payments.freeze");
        return http.post(PATH + id + "/freeze/", Map.of(), Map.class, idempotencyKey);
    }

    /** Gelé → bloqué ; événement {@code funds.unfrozen}. */
    public Map<String, Object> unfreeze(String id, String idempotencyKey) {
        http.assertSecretKey("connect.payments.unfreeze");
        return http.post(PATH + id + "/unfreeze/", Map.of(), Map.class, idempotencyKey);
    }

    /** Sandbox uniquement : simule l'encaissement du paiement. */
    public Map<String, Object> simulatePayment(String id) {
        http.assertSecretKey("connect.payments.simulatePayment");
        return http.post(PATH + id + "/simulate-payment/", Map.of(), Map.class);
    }
}
