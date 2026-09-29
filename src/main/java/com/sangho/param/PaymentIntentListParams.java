package com.sangho.param;

import java.util.LinkedHashMap;
import java.util.Map;

public class PaymentIntentListParams implements RequestParams {
    private final String status;
    private final String customer;
    private final String ordering;
    private final Integer page;
    private final Integer pageSize;

    private PaymentIntentListParams(Builder b) {
        this.status = b.status;
        this.customer = b.customer;
        this.ordering = b.ordering;
        this.page = b.page;
        this.pageSize = b.pageSize;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        if (status != null) m.put("status", status);
        if (customer != null) m.put("customer", customer);
        if (ordering != null) m.put("ordering", ordering);
        if (page != null) m.put("page", page.toString());
        if (pageSize != null) m.put("page_size", pageSize.toString());
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String status;
        private String customer;
        private String ordering;
        private Integer page;
        private Integer pageSize;

        public Builder status(String v) { this.status = v; return this; }
        public Builder customer(String v) { this.customer = v; return this; }
        public Builder ordering(String v) { this.ordering = v; return this; }
        public Builder page(Integer v) { this.page = v; return this; }
        public Builder pageSize(Integer v) { this.pageSize = v; return this; }

        public PaymentIntentListParams build() { return new PaymentIntentListParams(this); }
    }
}
