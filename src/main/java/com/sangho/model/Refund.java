package com.sangho.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Refund(
    String id,
    String object,
    int amount,
    String status,
    String reason,
    String transaction,
    @JsonProperty("created_at") String createdAt
) {}
