package com.skillgateway.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skillgateway.model.dto.SkillManifest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalSkillArtifactStorageTest {

    @TempDir
    Path tempDir;

    @Test
    void savesAndReadsArtifactUnderStorageRoot() throws Exception {
        Path source = tempDir.resolve("source.zip");
        byte[] bytes = "zip-bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(source, bytes);

        LocalSkillArtifactStorage storage = new LocalSkillArtifactStorage();
        storage.storageRoot = tempDir.resolve("storage").toString();
        String uri = storage.save(source, manifest(), "abc123");

        assertTrue(uri.endsWith("skill-bundle/1.0.0/abc123.zip"));
        assertArrayEquals(bytes, storage.open(uri).readAllBytes());
    }

    private SkillManifest manifest() {
        return new SkillManifest("skill-bundle", "1.0.0", "Bundle helper", "utility", List.of(), null, null, List.of(), null);
    }
}
