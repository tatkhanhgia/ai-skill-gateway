package com.skillgateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillgateway.model.dto.SkillManifest;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class BundledSkillCatalogSeeder {

    private static final Logger LOG = Logger.getLogger(BundledSkillCatalogSeeder.class);
    private static final Pattern TAG_NAME = Pattern.compile("^[a-z0-9][a-z0-9-]{0,49}$");
    private static final Pattern SEMVER = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:-[-\\w.]+)?(?:\\+[\\w.]+)?$");
    private static final Set<String> CATEGORIES = Set.of("ai", "backend", "frontend", "devops", "security", "data", "testing", "utility");

    @ConfigProperty(name = "skill.seed.enabled")
    boolean enabled;

    @ConfigProperty(name = "skill.seed.assets-manifest")
    String manifestPath;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SkillService skillService;

    void seedOnStartup(@Observes StartupEvent ignored) {
        if (!enabled) {
            LOG.debug("Bundled skill catalog seed disabled");
            return;
        }
        try {
            SeedStats stats = seed();
            LOG.infof("Bundled skill catalog seed complete: %d created, %d existing, %d skipped",
                stats.created(), stats.existing(), stats.skipped());
        } catch (IOException e) {
            LOG.warnf("Bundled skill catalog seed skipped: IOException: %s", e.getMessage());
        } catch (RuntimeException e) {
            LOG.warnf("Bundled skill catalog seed skipped: %s", EmbeddingErrorSanitizer.summarize(e));
        }
    }

    SeedStats seed() throws IOException {
        Path manifest = Path.of(manifestPath);
        if (!Files.isRegularFile(manifest)) {
            LOG.warnf("Bundled asset manifest not found: %s", manifest.toAbsolutePath());
            return new SeedStats(0, 0, 0);
        }

        List<BundledSkillFile> skillFiles = skillFiles(manifest);
        int created = 0;
        int existing = 0;
        int skipped = 0;
        for (BundledSkillFile file : skillFiles) {
            Optional<SkillManifest> parsed = parseSkillManifest(file);
            if (parsed.isEmpty()) {
                skipped++;
                continue;
            }
            if (skillService.publishIfVersionMissing(parsed.get()).isPresent()) {
                created++;
            } else {
                existing++;
            }
        }
        return new SeedStats(created, existing, skipped);
    }

    private List<BundledSkillFile> skillFiles(Path manifestPath) throws IOException {
        JsonNode root = objectMapper.readTree(Files.readString(manifestPath, StandardCharsets.UTF_8));
        JsonNode files = root.path("files");
        if (!files.isArray()) {
            throw new ValidationException("assets manifest files must be an array");
        }

        List<BundledSkillFile> out = new ArrayList<>();
        Path packageRoot = manifestPath.toAbsolutePath().getParent();
        for (JsonNode file : files) {
            String source = file.path("source").asText("");
            String target = file.path("target").asText("");
            if (!isSkillMarkdown(target)) {
                continue;
            }
            out.add(new BundledSkillFile(skillName(target), packageRoot.resolve(source)));
        }
        return out;
    }

    private Optional<SkillManifest> parseSkillManifest(BundledSkillFile file) throws IOException {
        if (!Files.isRegularFile(file.path())) {
            LOG.warnf("Bundled skill file missing: %s", file.path());
            return Optional.empty();
        }
        Map<String, String> frontmatter = frontmatter(Files.readString(file.path(), StandardCharsets.UTF_8));
        String name = file.name();
        String description = firstNonBlank(frontmatter.get("description"), "Bundled " + name + " skill.");
        String category = normalizeCategory(frontmatter.get("category"));
        String version = normalizeVersion(firstNonBlank(frontmatter.get("version"), "1.0.0"));
        return Optional.of(new SkillManifest(
            name,
            version,
            description,
            category,
            tags(frontmatter),
            firstNonBlank(frontmatter.get("author"), null),
            firstNonBlank(frontmatter.get("repositoryUrl"), frontmatter.get("repository")),
            List.of(),
            "Seeded from bundled package assets"
        ));
    }

    private Map<String, String> frontmatter(String markdown) {
        Map<String, String> values = new LinkedHashMap<>();
        if (!markdown.startsWith("---")) {
            return values;
        }
        int end = markdown.indexOf("\n---", 3);
        if (end < 0) {
            return values;
        }
        for (String raw : markdown.substring(3, end).split("\\R")) {
            String line = raw.trim();
            int separator = line.indexOf(':');
            if (separator <= 0 || line.startsWith("- ")) {
                continue;
            }
            String key = line.substring(0, separator).trim();
            String value = line.substring(separator + 1).trim();
            if (!value.equals("|") && !value.equals(">")) {
                values.putIfAbsent(key, stripQuotes(value));
            }
        }
        return values;
    }

    private List<String> tags(Map<String, String> frontmatter) {
        String raw = firstNonBlank(frontmatter.get("tags"), frontmatter.get("keywords"));
        if (raw == null) {
            return List.of("bundled");
        }
        String normalized = raw.replace("[", "").replace("]", "");
        List<String> tags = new ArrayList<>();
        for (String item : normalized.split(",")) {
            String tag = stripQuotes(item.trim()).toLowerCase(Locale.ROOT).replace('_', '-');
            if (TAG_NAME.matcher(tag).matches() && !tags.contains(tag)) {
                tags.add(tag);
            }
            if (tags.size() == 19) {
                break;
            }
        }
        tags.add("bundled");
        return tags;
    }

    private boolean isSkillMarkdown(String target) {
        return (target.startsWith(".claude/skills/") || target.startsWith(".codex/skills/") || target.startsWith(".opencode/skills/"))
            && target.split("/").length == 4
            && target.endsWith("/SKILL.md");
    }

    private String skillName(String target) {
        return target.split("/")[2];
    }

    private String normalizeCategory(String category) {
        String normalized = category == null ? "" : category.toLowerCase(Locale.ROOT).trim();
        return CATEGORIES.contains(normalized) ? normalized : "utility";
    }

    private String normalizeVersion(String version) {
        return SEMVER.matcher(version).matches() ? version : "1.0.0";
    }

    private String firstNonBlank(String first, String fallback) {
        return first == null || first.isBlank() ? fallback : first.trim();
    }

    private String stripQuotes(String value) {
        return value.replaceAll("^['\"]|['\"]$", "");
    }

    record SeedStats(int created, int existing, int skipped) {}

    private record BundledSkillFile(String name, Path path) {}
}
