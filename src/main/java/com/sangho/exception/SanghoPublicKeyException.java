package com.sangho.exception;

import java.util.Map;

/** 403 — opération réservée aux clés secrètes, appelée avec une clé publique. */
public class SanghoPublicKeyException extends SanghoException {
    public SanghoPublicKeyException(String message, Map<String, Object> raw) {
        super(message, "PERMISSION_ERROR", "PUBLIC_KEY_NOT_ALLOWED", 403, raw);
    }

    public SanghoPublicKeyException(String message) {
        this(message, Map.of());
    }
}
