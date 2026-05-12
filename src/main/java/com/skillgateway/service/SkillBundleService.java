package com.skillgateway.service;

import com.skillgateway.model.SkillVersion;
import com.skillgateway.model.SkillVersionFile;
import com.skillgateway.model.dto.PublishResponse;
import com.skillgateway.model.dto.SkillBundleFileInfo;
import com.skillgateway.model.dto.SkillBundlePublishResponse;
import com.skillgateway.repository.SkillVersionFileRepository;
import com.skillgateway.repository.SkillVersionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@ApplicationScoped
public class SkillBundleService {

    @Inject
    SkillBundleValidator validator;

    @Inject
    SkillArtifactStorage artifactStorage;

    @Inject
    SkillService skillService;

    @Inject
    SkillVersionRepository skillVersionRepository;

    @Inject
    SkillVersionFileRepository fileRepository;

    @Transactional(rollbackOn = IOException.class)
    public SkillBundlePublishResponse publish(InputStream input) throws IOException {
        try (ValidatedSkillBundle bundle = validator.validate(input)) {
            String artifactUri = null;
            PublishResponse published = skillService.publish(bundle.manifest());
            try {
                artifactUri = artifactStorage.save(bundle.artifactPath(), bundle.manifest(), bundle.bundleSha256());
                SkillVersion version = skillVersionRepository.findBySkillAndVersion(published.skillId(), published.version())
                    .orElseThrow(() -> new IllegalStateException("published version missing"));
                version.packageFormat = "zip";
                version.entrypointPath = "SKILL.md";
                version.bundleSha256 = bundle.bundleSha256();
                version.bundleSize = bundle.bundleSize();
                version.fileCount = bundle.files().size();
                version.artifactUri = artifactUri;
                persistFiles(version, bundle.files());
                return new SkillBundlePublishResponse(
                    published.skillId(),
                    published.versionId(),
                    published.name(),
                    published.version(),
                    bundle.bundleSha256(),
                    bundle.bundleSize(),
                    bundle.files().size(),
                    bundle.files()
                );
            } catch (RuntimeException | IOException e) {
                if (artifactUri != null) {
                    artifactStorage.delete(artifactUri);
                }
                throw e;
            }
        }
    }

    public List<SkillBundleFileInfo> listFiles(String name, String version) {
        SkillVersion found = findBundleVersion(name, version);
        return fileRepository.findByVersionId(found.id).stream()
            .map(file -> new SkillBundleFileInfo(file.path, file.sha256, file.size, file.mediaType, file.role))
            .toList();
    }

    public SkillVersion findBundleVersion(String name, String version) {
        SkillVersion found = skillVersionRepository.findByNameAndVersion(name, version)
            .orElseThrow(() -> new NotFoundException("version not found: " + name + "@" + version));
        if (found.artifactUri == null || found.artifactUri.isBlank()) {
            throw new NotFoundException("bundle artifact not found for: " + name + "@" + version);
        }
        return found;
    }

    private void persistFiles(SkillVersion version, List<SkillBundleFileInfo> files) {
        for (SkillBundleFileInfo info : files) {
            SkillVersionFile file = new SkillVersionFile();
            file.version = version;
            file.path = info.path();
            file.sha256 = info.sha256();
            file.size = info.size();
            file.mediaType = info.mediaType();
            file.role = info.role();
            fileRepository.persist(file);
        }
    }
}
