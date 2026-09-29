package com.sangho.exception;

import java.util.Map;

/** 401 — clé API invalide, expirée ou absente. */
public class SanghoAuthException extends SanghoException {
    public SanghoAuthException(String message, Map<String, Object> raw) {
        super(message, "AUTHENTICATION_ERROR", null, 401, raw);
    }
}
