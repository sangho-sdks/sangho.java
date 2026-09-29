package com.sangho.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Page de résultats : la liste est dans {@code data} (et non {@code results}), comme dans l'API. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ListResponse<T>(
    int count,
    String next,
    String previous,
    List<T> data
) {}
