package com.sangho.exception;

import java.util.Map;
import java.util.Set;

public class SanghoException extends RuntimeException {

    private static final Set<String> KNOWN_TYPES = Set.of(
        "AUTHENTICATION_ERROR", "PERMISSION_ERROR", "NOT_FOUND_ERROR", "CONFLICT_ERROR",
        "VALIDATION_ERROR", "RATE_LIMIT_ERROR", "API_ERROR", "NETWORK_ERROR", "TIMEOUT_ERROR"
    );

    private final String code;
    private final int statusCode;
    private final Map<String, Object> raw;
    private final String type;

    protected SanghoException(String message, String code, int statusCode, Map<String, Object> raw, String type) {
        super(message);
        this.raw = raw == null ? Map.of() : raw;
        // Le code métier précis renvoyé par le backend (raw["code"]) prime
        // toujours sur le code par défaut de la sous-classe.
        this.code = (this.raw.get("code") instanceof String s) ? s : code;
        Object backendType = this.raw.get("type");
        this.type = (backendType instanceof String s && KNOWN_TYPES.contains(s.toUpperCase()))
            ? s.toUpperCase()
            : type;
        this.statusCode = statusCode;
    }

    public SanghoException(String message, String code, int statusCode, Map<String, Object> raw) {
        this(message, code, statusCode, raw, "API_ERROR");
    }

    public SanghoException(String message) {
        this(message, "api_error", 0, Map.of(), "API_ERROR");
    }

    public String getCode()             { return code; }
    public int getStatusCode()          { return statusCode; }
    public Map<String, Object> getRaw() { return raw; }
    public String getType()             { return type; }
}
