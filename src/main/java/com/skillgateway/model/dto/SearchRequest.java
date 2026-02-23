package com.skillgateway.model.dto;

import java.util.List;

public record SearchRequest(
    String query,
    String category,
    List<String> tags,
    Integer limit
) {}
