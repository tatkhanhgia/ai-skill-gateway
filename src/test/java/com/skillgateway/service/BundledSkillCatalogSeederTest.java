package com.skillgateway.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillgateway.model.dto.PublishResponse;
import com.skillgateway.model.dto.SkillManifest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BundledSkillCatalogSeederTest {

    @TempDir
    Path tempDir;

    @Test
    void seedsSkillMetadataFromAssetManifest() throws IOException {
        Path packageRoot = tempDir.resolve("npm-package");
        Path skill = packageRoot.resolve("assets/codex/skills/demo-skill/SKILL.md");
        Files.createDirectories(skill.getParent());
        Files.writeString(skill, """
            ---
            name: ignored-frontmatter-name
            description: Demo skill
            category: ai
            keywords: [demo, codex]
            metadata:
              version: "2.0.0"
            ---
            Body.
            """);
        Path manifest = packageRoot.resolve("assets-manifest.json");
        Files.writeString(manifest, """
            {
              "files": [
                {
                  "source": "assets/codex/skills/demo-skill/SKILL.md",
                  "target": ".codex/skills/demo-skill/SKILL.md",
                  "type": "codex-skill",
                  "sha256": "x",
                  "size": 1
                }
              ]
            }
            """);

        FakeSkillService service = new FakeSkillService();
        BundledSkillCatalogSeeder seeder = new BundledSkillCatalogSeeder();
        seeder.objectMapper = new ObjectMapper();
        seeder.skillService = service;
        seeder.manifestPath = manifest.toString();

        BundledSkillCatalogSeeder.SeedStats stats = seeder.seed();

        assertEquals(new BundledSkillCatalogSeeder.SeedStats(1, 0, 0), stats);
        SkillManifest seeded = service.published.get(0);
        assertEquals("demo-skill", seeded.name());
        assertEquals("2.0.0", seeded.version());
        assertEquals("ai", seeded.category());
        assertEquals(List.of("demo", "codex", "bundled"), seeded.tags());
    }

    private static final class FakeSkillService extends SkillService {
        private final List<SkillManifest> published = new ArrayList<>();

        @Override
        public Optional<PublishResponse> publishIfVersionMissing(SkillManifest manifest) {
            published.add(manifest);
            return Optional.of(new PublishResponse(1L, 1L, manifest.name(), manifest.version()));
        }
    }
}
