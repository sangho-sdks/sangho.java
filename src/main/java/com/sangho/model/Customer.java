package com.sangho.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Customer(
    String id,
    String object,
    String app,
    String email,
    String name,
    String phone,
    String status,
    @JsonProperty("is_blacklisted") boolean isBlacklisted,
    @JsonProperty("transactions_count") int transactionsCount,
    @JsonProperty("total_spent") int totalSpent,
    Map<String, String> metadata,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("updated_at") String updatedAt
) {}
