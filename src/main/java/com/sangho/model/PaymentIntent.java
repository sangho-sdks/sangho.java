package com.sangho.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentIntent(
    String id,
    String object,
    int amount,
    String currency,
    String status,
    String customer,
    @JsonProperty("payment_method") String paymentMethod,
    String description,
    Map<String, String> metadata,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("updated_at") String updatedAt
) {}
