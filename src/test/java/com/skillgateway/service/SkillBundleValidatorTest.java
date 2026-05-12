package com.skillgateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Test;

class SkillBundleValidatorTest {

    private final SkillBundleValidator validator = validator();

    @Test
    void acceptsValidBundle() throws IOException {
        try (ValidatedSkillBundle bundle = validator.validate(zip(Map.of(
            "SKILL.md", skillMd(),
            "references/usage.md", "Use it"
        )))) {
            assertEquals("skill-bundle", bundle.manifest().name());
            assertEquals("1.0.0", bundle.manifest().version());
            assertEquals(2, bundle.files().size());
            assertTrue(bundle.files().stream().anyMatch(file -> file.path().equals("SKILL.md") && file.role().equals("entrypoint")));
        }
    }

    @Test
    void rejectsMissingSkillMarkdown() {
        assertThrows(ValidationException.class, () -> validator.validate(zip(Map.of("README.md", "No entrypoint"))));
    }

    @Test
    void rejectsTraversalPath() {
        assertThrows(ValidationException.class, () -> validator.validate(zip(Map.of("../SKILL.md", skillMd()))));
    }

    @Test
    void rejectsForbiddenFiles() {
        assertThrows(ValidationException.class, () -> validator.validate(zip(Map.of(
            "SKILL.md", skillMd(),
            ".env", "SECRET=value"
        ))));
    }

    @Test
    void rejectsPathLongerThanStoredColumn() {
        String longPath = "a".repeat(513);
        assertThrows(ValidationException.class, () -> validator.validate(zip(Map.of(
            "SKILL.md", skillMd(),
            longPath, "too long"
        ))));
    }

    @Test
    void acceptsPathAtStoredColumnLimit() throws IOException {
        String maxPath = "a".repeat(512);
        try (ValidatedSkillBundle bundle = validator.validate(zip(Map.of(
            "SKILL.md", skillMd(),
            maxPath, "at limit"
        )))) {
            assertTrue(bundle.files().stream().anyMatch(file -> file.path().equals(maxPath)));
        }
    }

    @Test
    void rejectsNestedExtraSkillMarkdown() {
        assertThrows(ValidationException.class, () -> validator.validate(zip(Map.of(
            "SKILL.md", skillMd(),
            "nested/SKILL.md", skillMd()
        ))));
    }

    @Test
    void rejectsOversizedFile() {
        validator.maxFileBytes = 4;
        assertThrows(ValidationException.class, () -> validator.validate(zip(Map.of("SKILL.md", skillMd()))));
    }

    @Test
    void rejectsMultilineYamlArrays() {
        String skillMd = """
            ---
            name: skill-bundle
            version: 1.0.0
            description: Bundle helper
            category: utility
            tags:
              - bundle
            ---
            """;
        assertThrows(ValidationException.class, () -> validator.validate(zip(Map.of("SKILL.md", skillMd))));
    }

    @Test
    void rejectsYamlBlockScalars() {
        String skillMd = """
            ---
            name: skill-bundle
            version: 1.0.0
            description: |
              Bundle helper
            category: utility
            ---
            """;
        assertThrows(ValidationException.class, () -> validator.validate(zip(Map.of("SKILL.md", skillMd))));
    }

    private SkillBundleValidator validator() {
        SkillBundleValidator created = new SkillBundleValidator();
        created.maxBytes = 1024 * 1024;
        created.maxFileBytes = 1024 * 1024;
        created.maxFiles = 20;
        created.manifestValidator = new ManifestValidator();
        created.manifestParser = new SkillBundleManifestParser();
        return created;
    }

    private ByteArrayInputStream zip(Map<String, String> files) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(out)) {
            for (Map.Entry<String, String> file : files.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    private String skillMd() {
        return """
            ---
            name: skill-bundle
            version: 1.0.0
            description: Bundle helper
            category: utility
            tags: [bundle, test]
            ---

            Instructions.
            """;
    }
}
