package com.sangho.resource;

import com.sangho.http.HttpClient;

import java.util.Map;

public class SandboxResource {

    private final HttpClient http;

    public SandboxResource(HttpClient http) { this.http = http; }

    /**
     * Supprime toutes les données sandbox de l'app courante (customers,
     * products, payment_intents, transactions, refunds, invoices,
     * checkout_sessions, subscriptions, payment_methods, receipts).
     *
     * L'app elle-même, ses clés API et ses paramètres sont conservés.
     * Bloqué côté backend si la clé utilisée est une clé de production
     * (sk_prod_*).
     */
    public Map<String, Object> reset() {
        http.assertSecretKey("sandbox.reset");
        return http.post("/reset/", Map.of(), Map.class);
    }
}
