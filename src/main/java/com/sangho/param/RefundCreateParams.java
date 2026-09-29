package com.sangho.param;

import java.util.LinkedHashMap;
import java.util.Map;

public class RefundCreateParams implements RequestParams {
    private final String transaction;
    private final Integer amount;
    private final String reason;

    private RefundCreateParams(Builder b) {
        this.transaction = b.transaction;
        this.amount = b.amount;
        this.reason = b.reason;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        if (transaction != null) m.put("transaction", transaction);
        if (amount != null) m.put("amount", amount.toString());
        if (reason != null) m.put("reason", reason);
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String transaction;
        private Integer amount;
        private String reason;

        public Builder transaction(String v) { this.transaction = v; return this; }
        public Builder amount(Integer v) { this.amount = v; return this; }
        public Builder reason(String v) { this.reason = v; return this; }

        public RefundCreateParams build() { return new RefundCreateParams(this); }
    }
}
