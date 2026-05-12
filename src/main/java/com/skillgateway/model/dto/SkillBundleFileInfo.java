package com.skillgateway.model.dto;

public record SkillBundleFileInfo(
    String path,
    String sha256,
    long size,
    String mediaType,
    String role
) {}
