package com.sangho.param;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProductCreateParams implements RequestParams {
    private final String name;
    private final Integer price;
    private final String description;

    private ProductCreateParams(Builder b) {
        this.name = b.name;
        this.price = b.price;
        this.description = b.description;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        if (name != null) m.put("name", name);
        if (price != null) m.put("price", price.toString());
        if (description != null) m.put("description", description);
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private Integer price;
        private String description;

        public Builder name(String v) { this.name = v; return this; }
        public Builder price(Integer v) { this.price = v; return this; }
        public Builder description(String v) { this.description = v; return this; }

        public ProductCreateParams build() { return new ProductCreateParams(this); }
    }
}
