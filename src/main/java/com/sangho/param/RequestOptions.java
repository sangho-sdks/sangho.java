package com.sangho.param;

/**
 * Options d'une requête d'écriture. {@code idempotencyKey} rend l'appel rejouable sans doublon (même clé + même corps =
 * même réponse) et active le nouvel essai après un délai dépassé / une erreur réseau ; sans clé, un POST n'est pas
 * rejoué dans ce cas (le serveur a pu le traiter).
 */
public final class RequestOptions {

    private final String idempotencyKey;

    private RequestOptions(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public static RequestOptions idempotencyKey(String key) { return new RequestOptions(key); }

    public static RequestOptions none() { return new RequestOptions(null); }

    public String getIdempotencyKey() { return idempotencyKey; }
}
