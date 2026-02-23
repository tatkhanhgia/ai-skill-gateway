package com.skillgateway.model.dto;

public record SkillVersionInfo(
    Long id,
    String version,
    boolean prerelease,
    boolean yanked
) {}
