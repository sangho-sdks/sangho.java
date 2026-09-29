package com.sangho.param;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CustomerUpdateParams {
    private final String email;
    private final String name;
    private final String phone;

    private CustomerUpdateParams(Builder b) {
        this.email = b.email;
        this.name = b.name;
        this.phone = b.phone;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        if (email != null) m.put("email", email);
        if (name != null) m.put("name", name);
        if (phone != null) m.put("phone", phone);
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String email;
        private String name;
        private String phone;

        public Builder email(String v) { this.email = v; return this; }
        public Builder name(String v) { this.name = v; return this; }
        public Builder phone(String v) { this.phone = v; return this; }

        public CustomerUpdateParams build() { return new CustomerUpdateParams(this); }
    }
}
