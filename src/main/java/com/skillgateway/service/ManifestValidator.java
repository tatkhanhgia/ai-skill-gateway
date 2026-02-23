package com.skillgateway.service;

import com.skillgateway.model.dto.SkillManifest;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@ApplicationScoped
public class ManifestValidator {

    private static final Pattern SKILL_NAME = Pattern.compile("^[a-z0-9][a-z0-9-]{2,99}$");
    private static final Pattern TAG_NAME = Pattern.compile("^[a-z0-9][a-z0-9-]{0,49}$");
    private static final Pattern SEMVER = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:-[-\\w.]+)?(?:\\+[\\w.]+)?$");
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
        "ai", "backend", "frontend", "devops", "security", "data", "testing", "utility"
    );

    public void validateOrThrow(SkillManifest manifest) {
        List<String> errors = validate(manifest);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public List<String> validate(SkillManifest manifest) {
        List<String> errors = new ArrayList<>();
        if (manifest == null) {
            errors.add("manifest is required");
            return errors;
        }
        if (manifest.name() == null || !SKILL_NAME.matcher(manifest.name()).matches()) {
            errors.add("name must be lowercase-hyphenated and 3-100 chars");
        }
        if (manifest.version() == null || !SEMVER.matcher(manifest.version()).matches()) {
            errors.add("version must be valid semver");
        }
        if (manifest.category() == null || !ALLOWED_CATEGORIES.contains(manifest.category().toLowerCase())) {
            errors.add("category is invalid");
        }
        if (manifest.tags() != null) {
            if (manifest.tags().size() > 20) {
                errors.add("tags max is 20");
            }
            for (String tag : manifest.tags()) {
                if (tag == null || !TAG_NAME.matcher(tag).matches()) {
                    errors.add("invalid tag: " + tag);
                }
            }
        }
        if (manifest.description() == null || manifest.description().isBlank()) {
            errors.add("description is required");
        }
        return errors;
    }
}
