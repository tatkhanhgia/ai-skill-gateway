package com.skillgateway.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.skillgateway.model.dto.SkillManifest;
import java.util.List;
import org.junit.jupiter.api.Test;

class ManifestValidatorTest {

    private final ManifestValidator validator = new ManifestValidator();

    @Test
    void acceptsValidManifest() {
        SkillManifest manifest = new SkillManifest(
            "skill-analytics",
            "1.0.0",
            "Analytics helper",
            "data",
            List.of("analytics", "reporting"),
            "team",
            "https://example.com/repo",
            List.of("skill:base-utils@^1.0.0"),
            "initial"
        );
        assertDoesNotThrow(() -> validator.validateOrThrow(manifest));
    }

    @Test
    void rejectsInvalidManifest() {
        SkillManifest manifest = new SkillManifest(
            "Bad Name",
            "abc",
            "",
            "unknown",
            List.of("Invalid Tag"),
            "team",
            "https://example.com/repo",
            List.of(),
            ""
        );
        assertThrows(ValidationException.class, () -> validator.validateOrThrow(manifest));
    }
}
