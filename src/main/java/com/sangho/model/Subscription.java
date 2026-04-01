package com.sangho.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Subscription(
    String id,
    String object,
    String customer,
    String plan,
    String status,
    @JsonProperty("current_period_start") String currentPeriodStart,
    @JsonProperty("current_period_end") String currentPeriodEnd,
    @JsonProperty("created_at") String createdAt
) {}
