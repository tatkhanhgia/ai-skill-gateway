package com.skillgateway.service;

import com.skillgateway.model.dto.SkillManifest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface SkillArtifactStorage {
    String save(Path artifact, SkillManifest manifest, String sha256) throws IOException;

    InputStream open(String artifactUri) throws IOException;

    Path resolve(String artifactUri);

    void delete(String artifactUri) throws IOException;
}
