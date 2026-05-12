package com.skillgateway.service;

import com.skillgateway.model.dto.SkillManifest;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SkillBundleManifestParser {

    public SkillManifest parse(String skillMarkdown) {
        Map<String, Object> values = parseFlatYaml(extractFrontmatter(skillMarkdown));
        return new SkillManifest(
            asString(values.get("name")),
            asString(values.get("version")),
            asString(values.get("description")),
            asString(values.get("category")),
            asStringList(values.get("tags")),
            asString(values.get("author")),
            firstString(values, "repositoryUrl", "repository"),
            asStringList(values.get("requires")),
            firstString(values, "releaseNotes", "release_notes")
        );
    }

    private String extractFrontmatter(String markdown) {
        if (!markdown.startsWith("---")) {
            throw new ValidationException("SKILL.md frontmatter is required");
        }
        int end = markdown.indexOf("\n---", 3);
        if (end < 0) {
            throw new ValidationException("SKILL.md frontmatter is not closed");
        }
        return markdown.substring(3, end);
    }

    private Map<String, Object> parseFlatYaml(String frontmatter) {
        Map<String, Object> values = new HashMap<>();
        for (String rawLine : frontmatter.split("\\R")) {
            String line = rawLine.trim();
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("- ")) {
                throw new ValidationException("SKILL.md frontmatter does not support multiline arrays");
            }
            int separator = line.indexOf(':');
            if (separator > 0) {
                values.put(line.substring(0, separator).trim(), parseScalar(line.substring(separator + 1).trim()));
            }
        }
        return values;
    }

    private Object parseScalar(String value) {
        String unquoted = value.replaceAll("^['\"]|['\"]$", "");
        if ("|".equals(unquoted) || ">".equals(unquoted)) {
            throw new ValidationException("SKILL.md frontmatter does not support block scalars");
        }
        if (unquoted.startsWith("[") && unquoted.endsWith("]")) {
            return List.of(unquoted.substring(1, unquoted.length() - 1).split(",")).stream()
                .map(item -> item.trim().replaceAll("^['\"]|['\"]$", ""))
                .filter(item -> !item.isBlank())
                .toList();
        }
        return unquoted;
    }

    private List<String> asStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).filter(item -> !item.isBlank()).toList();
        }
        if (value instanceof String text && !text.isBlank()) {
            return List.of(text.split(",")).stream().map(String::trim).filter(item -> !item.isBlank()).toList();
        }
        return List.of();
    }

    private String firstString(Map<String, Object> values, String first, String second) {
        String found = asString(values.get(first));
        return found == null ? asString(values.get(second)) : found;
    }

    private String asString(Object value) {
        return value instanceof String text && !text.isBlank() ? text.trim() : null;
    }
}
