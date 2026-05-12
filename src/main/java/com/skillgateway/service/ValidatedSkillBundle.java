package com.skillgateway.service;

import com.skillgateway.model.dto.SkillBundleFileInfo;
import com.skillgateway.model.dto.SkillBundleManifest;
import com.skillgateway.model.dto.SkillManifest;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.List;

public record ValidatedSkillBundle(
    SkillManifest manifest,
    List<SkillBundleFileInfo> files,
    Path artifactPath,
    String bundleSha256,
    long bundleSize
) implements AutoCloseable {
    public SkillBundleManifest toManifest() {
        return new SkillBundleManifest(manifest, files, bundleSha256, bundleSize, files.size());
    }

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(artifactPath);
    }
}
