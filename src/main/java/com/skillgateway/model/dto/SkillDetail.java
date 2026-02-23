package com.skillgateway.model.dto;

import java.util.List;

public record SkillDetail(
    Long id,
    String name,
    String category,
    String description,
    String author,
    String repositoryUrl,
    int downloadCount,
    SkillVersionInfo latestVersion,
    List<String> tags
) {}
