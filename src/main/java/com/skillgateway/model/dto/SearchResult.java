package com.skillgateway.model.dto;

import java.util.List;

public record SearchResult(
    Long id,
    String name,
    String category,
    String description,
    int downloadCount,
    double score,
    List<String> tags
) {}
