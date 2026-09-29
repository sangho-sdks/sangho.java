package com.sangho.param;

import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentIntentCreateParams implements RequestParams {
    private final Integer amount;
    private final String customer;
    private final String currency;
    private final String customerEmail;
    private final String paymentMethod;
    private final String description;

    private PaymentIntentCreateParams(Builder b) {
        this.amount = b.amount;
        this.customer = b.customer;
        this.currency = b.currency;
        this.customerEmail = b.customerEmail;
        this.paymentMethod = b.paymentMethod;
        this.description = b.description;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        if (amount != null) m.put("amount", amount.toString());
        m.put("currency", currency);
        if (customer != null) m.put("customer", customer);
        if (customerEmail != null) m.put("customer_email", customerEmail);
        if (paymentMethod != null) m.put("payment_method", paymentMethod);
        if (description != null) m.put("description", description);
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Integer amount;
        private String customer;
        /** Code ISO 4217, exigé par le backend (défaut XAF). */
        private String currency = "XAF";
        private String customerEmail;
        private String paymentMethod;
        private String description;

        public Builder amount(Integer v) { this.amount = v; return this; }
        public Builder customer(String v) { this.customer = v; return this; }
        public Builder currency(String v) { this.currency = v; return this; }
        /** E-mail de l'acheteur (le backend lit {@code customer_email}). */
        public Builder customerEmail(String v) { this.customerEmail = v; return this; }
        public Builder paymentMethod(String v) { this.paymentMethod = v; return this; }
        public Builder description(String v) { this.description = v; return this; }

        public PaymentIntentCreateParams build() { return new PaymentIntentCreateParams(this); }
    }
}
