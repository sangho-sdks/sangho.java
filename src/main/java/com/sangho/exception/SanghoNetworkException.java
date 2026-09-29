package com.sangho.exception;

import java.util.Map;

/**
 * Erreur réseau (aucune réponse du serveur). Catégorie propre au SDK — la
 * requête n'a jamais atteint le backend, donc pas de raw/code métier.
 */
public class SanghoNetworkException extends SanghoException {
    public SanghoNetworkException(String message) {
        super(message, "network_error", 0, Map.of(), "NETWORK_ERROR");
    }
}
