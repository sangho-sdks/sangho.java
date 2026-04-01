package com.sangho.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Webhook(
    String id,
    String object,
    String url,
    List<String> events,
    String status,
    @JsonProperty("created_at") String createdAt
) {}
