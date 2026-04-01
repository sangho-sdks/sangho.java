package com.sangho.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ListResponse<T>(
    int count,
    String next,
    String previous,
    List<T> results
) {}
