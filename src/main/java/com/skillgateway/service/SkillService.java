package com.skillgateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillgateway.model.Skill;
import com.skillgateway.model.SkillVersion;
import com.skillgateway.model.dto.PublishResponse;
import com.skillgateway.model.dto.SkillDetail;
import com.skillgateway.model.dto.SkillManifest;
import com.skillgateway.model.dto.SkillSummary;
import com.skillgateway.model.dto.SkillVersionInfo;
import com.skillgateway.repository.SkillRepository;
import com.skillgateway.repository.SkillVersionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SkillService {

    @Inject
    SkillRepository skillRepository;

    @Inject
    SkillVersionRepository skillVersionRepository;

    @Inject
    ManifestValidator manifestValidator;

    @Inject
    EmbeddingService embeddingService;

    @Inject
    ObjectMapper objectMapper;

    @Transactional
    public PublishResponse publish(SkillManifest manifest) {
        manifestValidator.validateOrThrow(manifest);

        Skill skill = skillRepository.findByName(manifest.name())
            .orElseGet(Skill::new);

        boolean isNew = skill.id == null;
        skill.name = manifest.name();
        skill.category = manifest.category().toLowerCase();
        skill.description = manifest.description();
        skill.author = manifest.author();
        skill.repositoryUrl = manifest.repositoryUrl();
        skill.tags.clear();
        if (manifest.tags() != null) {
            skill.tags.addAll(manifest.tags());
        }

        try {
            float[] emb = embeddingService.embed(manifest.name() + " " + manifest.description());
            skill.embedding = embeddingService.asPgVectorLiteral(emb);
        } catch (RuntimeException ignore) {
            skill.embedding = null;
        }

        if (isNew) {
            skillRepository.persist(skill);
        }

        Optional<SkillVersion> existingVersion = skillVersionRepository.findBySkillAndVersion(skill.id, manifest.version());
        if (existingVersion.isPresent()) {
            throw new ConflictException("version already exists for skill: " + manifest.name() + "@" + manifest.version());
        }

        skillVersionRepository.clearLatestBySkillId(skill.id);

        SkillVersion version = new SkillVersion();
        version.skill = skill;
        version.versionSemver = manifest.version();
        version.prerelease = manifest.version().contains("-");
        version.latest = true;
        version.yanked = false;
        version.releaseNotes = manifest.releaseNotes();
        version.requires = manifest.requires() == null ? "[]" : toJsonArray(manifest.requires());
        try {
            skillVersionRepository.persist(version);
            skillVersionRepository.flush();
        } catch (Exception e) {
            throw new ConflictException("version already exists for skill: " + manifest.name() + "@" + manifest.version());
        }

        return new PublishResponse(skill.id, version.id, skill.name, version.versionSemver);
    }

    public Optional<SkillDetail> findByName(String name) {
        Optional<Skill> found = skillRepository.findByName(name);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Skill skill = found.get();
        SkillVersion latest = skillVersionRepository.findLatestBySkillId(skill.id)
            .orElse(null);
        SkillVersionInfo latestInfo = latest == null
            ? null
            : new SkillVersionInfo(latest.id, latest.versionSemver, latest.prerelease, latest.yanked);

        return Optional.of(new SkillDetail(
            skill.id,
            skill.name,
            skill.category,
            skill.description,
            skill.author,
            skill.repositoryUrl,
            skill.downloadCount == null ? 0 : skill.downloadCount,
            latestInfo,
            skill.tags.stream().sorted().toList()
        ));
    }

    public List<SkillSummary> list(String category, List<String> tags, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(1, Math.min(size, 50));
        return skillRepository.listSkills(category, tags == null ? List.of() : tags, safePage, safeSize);
    }

    @Transactional
    public void yankVersion(String name, String version, String reason) {
        SkillVersion target = skillVersionRepository.findByNameAndVersion(name, version)
            .orElseThrow(() -> new NotFoundException("version not found: " + name + "@" + version));
        target.yanked = true;
        target.latest = false;
        target.yankReason = reason;

        List<SkillVersion> versions = skillVersionRepository.findBySkillName(name).stream()
            .filter(v -> !v.yanked)
            .toList();
        if (!versions.isEmpty()) {
            versions.get(0).latest = true;
        }
    }

    private String toJsonArray(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("invalid requires payload", e);
        }
    }
}
