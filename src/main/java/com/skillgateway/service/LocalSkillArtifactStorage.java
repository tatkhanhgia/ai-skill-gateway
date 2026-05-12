package com.skillgateway.service;

import com.skillgateway.model.dto.SkillManifest;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class LocalSkillArtifactStorage implements SkillArtifactStorage {

    @ConfigProperty(name = "skill.bundle.storage-root")
    String storageRoot;

    @Override
    public String save(Path artifact, SkillManifest manifest, String sha256) throws IOException {
        Path root = root();
        Path relative = Path.of(manifest.name(), manifest.version(), sha256 + ".zip");
        Path target = root.resolve(relative).normalize();
        if (!target.startsWith(root)) {
            throw new ValidationException("artifact path escapes storage root");
        }
        Files.createDirectories(target.getParent());
        Files.copy(artifact, target, StandardCopyOption.REPLACE_EXISTING);
        return relative.toString().replace('\\', '/');
    }

    @Override
    public InputStream open(String artifactUri) throws IOException {
        Path resolved = resolve(artifactUri);
        if (!Files.isRegularFile(resolved)) {
            throw new NotFoundException("bundle artifact not found");
        }
        return Files.newInputStream(resolved);
    }

    @Override
    public Path resolve(String artifactUri) {
        Path root = root();
        Path resolved = root.resolve(artifactUri).normalize();
        if (!resolved.startsWith(root)) {
            throw new ValidationException("artifact path escapes storage root");
        }
        return resolved;
    }

    @Override
    public void delete(String artifactUri) throws IOException {
        Files.deleteIfExists(resolve(artifactUri));
    }

    private Path root() {
        return Path.of(storageRoot).toAbsolutePath().normalize();
    }
}
