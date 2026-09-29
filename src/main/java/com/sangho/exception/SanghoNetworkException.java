package com.sangho.exception;

import java.util.Map;

/** Aucune réponse du serveur (DNS, connexion refusée…). Catégorie propre au SDK : la requête n'a jamais abouti. */
public class SanghoNetworkException extends SanghoException {
    public SanghoNetworkException(String message) {
        super(message != null ? message : "Network error. Please check your connection.", "NETWORK_ERROR", null, 0, Map.of());
    }
}
