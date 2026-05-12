package com.skillgateway.model.dto;

import java.util.List;

public record SkillBundlePublishResponse(
    Long skillId,
    Long versionId,
    String name,
    String version,
    String bundleSha256,
    long bundleSize,
    int fileCount,
    List<SkillBundleFileInfo> files
) {}
