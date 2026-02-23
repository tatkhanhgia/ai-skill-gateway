package com.skillgateway.model.dto;

import java.util.List;

public record SkillManifest(
    String name,
    String version,
    String description,
    String category,
    List<String> tags,
    String author,
    String repositoryUrl,
    List<String> requires,
    String releaseNotes
) {}
