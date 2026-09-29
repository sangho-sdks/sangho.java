package com.sangho.resource;

import com.sangho.http.HttpClient;

/**
 * Marketplace / paiement avec répartition : {@code accounts()} et {@code payments()}. Clé secrète uniquement.
 *
 * <p>Réservé aux Apps ayant le statut <b>Partenaire Plateforme</b> (approuvé par Sangho) : une App marchande
 * ordinaire reçoit un 403 {@code SanghoPlatformPartnerRequiredException} sur n'importe quel appel.
 */
public class ConnectResource {

    private final ConnectAccountsResource accounts;
    private final ConnectPaymentsResource payments;

    public ConnectResource(HttpClient http) {
        this.accounts = new ConnectAccountsResource(http);
        this.payments = new ConnectPaymentsResource(http);
    }

    public ConnectAccountsResource accounts() { return accounts; }
    public ConnectPaymentsResource payments() { return payments; }
}
