package com.sangho.param;

import java.util.LinkedHashMap;
import java.util.Map;

public class WebhookCreateParams implements RequestParams {
    private final String url;
    private final String description;

    private WebhookCreateParams(Builder b) {
        this.url = b.url;
        this.description = b.description;
    }

    public Map<String, String> toMap() {
        Map<String, String> m = new LinkedHashMap<>();
        if (url != null) m.put("url", url);
        if (description != null) m.put("description", description);
        return m;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String url;
        private String description;

        public Builder url(String v) { this.url = v; return this; }
        public Builder description(String v) { this.description = v; return this; }

        public WebhookCreateParams build() { return new WebhookCreateParams(this); }
    }
}
