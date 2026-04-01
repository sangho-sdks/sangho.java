package com.sangho.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Invoice(
    String id,
    String object,
    String customer,
    int amount,
    String status,
    String currency,
    @JsonProperty("due_date") String dueDate,
    Map<String, String> metadata,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("updated_at") String updatedAt
) {}
