package com.skillgateway.model.dto;

import java.util.List;

public record SkillSummary(
    Long id,
    String name,
    String category,
    String description,
    int downloadCount,
    List<String> tags
) {}
