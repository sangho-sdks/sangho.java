package com.sangho.exception;
import java.util.Map;
/** 403 — Write operation called with a public key. */
public class SanghoPublicKeyException extends SanghoException {
    public SanghoPublicKeyException(String message, String code, int statusCode, Map<String, Object> raw) {
        super(message, code, statusCode, raw);
    }
    public SanghoPublicKeyException(String message) { super(message, "api_error", 0, Map.of()); }
}
