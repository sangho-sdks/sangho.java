package com.sangho.exception;

import java.util.Map;

/**
 * Signature de webhook refusée. {@link #getReason()} : {@code malformed} (en-tête illisible, 400), {@code expired}
 * (horodatage hors tolérance, 400) ou {@code mismatch} (aucune signature ne correspond à un secret, 401).
 *
 * <p>Sous-classe de {@link SanghoException} : {@code getCode()} garde les valeurs historiques
 * ({@code invalid_signature}, {@code stale_event}).
 */
public class SanghoWebhookSignatureException extends SanghoException {

    public static final String MALFORMED = "malformed";
    public static final String EXPIRED = "expired";
    public static final String MISMATCH = "mismatch";

    private final String reason;

    public SanghoWebhookSignatureException(String reason, String message) {
        super(message, MISMATCH.equals(reason) ? "AUTHENTICATION_ERROR" : "VALIDATION_ERROR",
            EXPIRED.equals(reason) ? "stale_event" : "invalid_signature",
            MISMATCH.equals(reason) ? 401 : 400, Map.of());
        this.reason = reason;
    }

    public String getReason() { return reason; }
}
