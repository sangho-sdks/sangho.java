package com.sangho.param;

import java.util.LinkedHashMap;
import java.util.Map;

public class InvoiceCreateParams implements RequestParams {
    private final String customer;
    private final Integer amount;
    private final String dueDate;

    private InvoiceCreateParams(Builder b) {
        this.customer = b.customer;
        this.amount = b.amount;
        this.dueDate = b.dueDate;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        if (customer != null) m.put("customer", customer);
        if (amount != null) m.put("amount", String.valueOf(amount));
        if (dueDate != null) m.put("due_date", dueDate);
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String customer;
        private Integer amount;
        private String dueDate;

        public Builder customer(String v) { this.customer = v; return this; }
        public Builder amount(Integer v) { this.amount = v; return this; }
        public Builder dueDate(String v) { this.dueDate = v; return this; }

        public InvoiceCreateParams build() { return new InvoiceCreateParams(this); }
    }
}
