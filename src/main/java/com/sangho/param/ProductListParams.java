package com.sangho.param;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProductListParams {
    private final String search;
    private final String status;
    private final String ordering;
    private final Integer page;
    private final Integer pageSize;

    private ProductListParams(Builder b) {
        this.search = b.search;
        this.status = b.status;
        this.ordering = b.ordering;
        this.page = b.page;
        this.pageSize = b.pageSize;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        if (search != null) m.put("search", search);
        if (status != null) m.put("status", status);
        if (ordering != null) m.put("ordering", ordering);
        if (page != null) m.put("page", page.toString());
        if (pageSize != null) m.put("page_size", pageSize.toString());
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String search;
        private String status;
        private String ordering;
        private Integer page;
        private Integer pageSize;

        public Builder search(String v) { this.search = v; return this; }
        public Builder status(String v) { this.status = v; return this; }
        public Builder ordering(String v) { this.ordering = v; return this; }
        public Builder page(Integer v) { this.page = v; return this; }
        public Builder pageSize(Integer v) { this.pageSize = v; return this; }

        public ProductListParams build() { return new ProductListParams(this); }
    }
}
