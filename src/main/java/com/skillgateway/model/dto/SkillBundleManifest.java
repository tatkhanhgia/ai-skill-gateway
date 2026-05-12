package com.skillgateway.model.dto;

import java.util.List;

public record SkillBundleManifest(
    SkillManifest manifest,
    List<SkillBundleFileInfo> files,
    String bundleSha256,
    long bundleSize,
    int fileCount
) {}
