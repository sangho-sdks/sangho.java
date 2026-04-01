package com.sangho.exception;

import java.util.Map;

public class SanghoException extends RuntimeException {
    private final String code;
    private final int statusCode;
    private final Map<String, Object> raw;

    public SanghoException(String message, String code, int statusCode, Map<String, Object> raw) {
        super(message);
        this.code       = code;
        this.statusCode = statusCode;
        this.raw        = raw;
    }

    public SanghoException(String message) {
        this(message, "api_error", 0, Map.of());
    }

    public String getCode()            { return code; }
    public int getStatusCode()         { return statusCode; }
    public Map<String, Object> getRaw(){ return raw; }
}
