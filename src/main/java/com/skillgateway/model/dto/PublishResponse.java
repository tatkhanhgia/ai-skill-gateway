package com.skillgateway.model.dto;

public record PublishResponse(
    Long skillId,
    Long versionId,
    String name,
    String version
) {}
