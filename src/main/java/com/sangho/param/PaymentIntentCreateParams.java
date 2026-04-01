package com.sangho.param;

import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentIntentCreateParams {
    private final Integer amount;
    private final String customer;
    private final String paymentMethod;
    private final String description;

    private PaymentIntentCreateParams(Builder b) {
        this.amount = b.amount;
        this.customer = b.customer;
        this.paymentMethod = b.paymentMethod;
        this.description = b.description;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        if (amount != null) m.put("amount", amount.toString());
        if (customer != null) m.put("customer", customer);
        if (paymentMethod != null) m.put("payment_method", paymentMethod);
        if (description != null) m.put("description", description);
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer amount;
        private String customer;
        private String paymentMethod;
        private String description;

        public Builder amount(Integer v) { this.amount = v; return this; }
        public Builder customer(String v) { this.customer = v; return this; }
        public Builder paymentMethod(String v) { this.paymentMethod = v; return this; }
        public Builder description(String v) { this.description = v; return this; }

        public PaymentIntentCreateParams build() { return new PaymentIntentCreateParams(this); }
    }
}
