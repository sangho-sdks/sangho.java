package com.sangho.param;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SubscriptionCreateParams {
    private final String customer;
    private final String plan;

    private SubscriptionCreateParams(Builder b) {
        this.customer = b.customer;
        this.plan = b.plan;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        if (customer != null) m.put("customer", customer);
        if (plan != null) m.put("plan", plan);
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String customer;
        private String plan;

        public Builder customer(String v) { this.customer = v; return this; }
        public Builder plan(String v) { this.plan = v; return this; }

        public SubscriptionCreateParams build() { return new SubscriptionCreateParams(this); }
    }
}
