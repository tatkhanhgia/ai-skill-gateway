package com.skillgateway.model.dto;

public record VersionInfo(
    Long id,
    String version,
    boolean prerelease,
    boolean latest,
    boolean yanked,
    String publishedAt
) {}
