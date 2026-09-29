package com.sangho.exception;

import java.util.Map;

/** 403 — la ressource est réservée aux Apps « Partenaire Plateforme » (routes Connect). */
public class SanghoPlatformPartnerRequiredException extends SanghoException {
    public SanghoPlatformPartnerRequiredException(String message, Map<String, Object> raw) {
        super(message, "PERMISSION_ERROR", "platform_partner_required", 403, raw);
    }
}
