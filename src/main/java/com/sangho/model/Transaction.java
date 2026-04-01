package com.sangho.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Transaction(
    String id,
    String object,
    int amount,
    String currency,
    String status,
    String customer,
    String type,
    @JsonProperty("payment_method") String paymentMethod,
    @JsonProperty("created_at") String createdAt
) {}
