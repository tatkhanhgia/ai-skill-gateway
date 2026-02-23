package com.skillgateway.service;

import com.skillgateway.model.SkillVersion;
import com.skillgateway.model.dto.VersionInfo;
import com.skillgateway.model.dto.VersionResolution;
import com.skillgateway.repository.SkillVersionRepository;
import com.skillgateway.version.SemVer;
import com.skillgateway.version.SemVerConstraint;
import com.skillgateway.version.SemVerParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Comparator;
import java.util.List;

@ApplicationScoped
public class VersionService {

    @Inject
    SkillVersionRepository skillVersionRepository;

    public VersionResolution resolve(String skillName, String constraint) {
        SemVerConstraint parsed = SemVerConstraint.parse(constraint);
        List<SkillVersion> versions = skillVersionRepository.findBySkillName(skillName);
        List<SemVer> matched = versions.stream()
            .filter(v -> !v.yanked)
            .map(v -> SemVerParser.parse(v.versionSemver))
            .filter(parsed::matches)
            .sorted(Comparator.reverseOrder())
            .toList();

        if (matched.isEmpty()) {
            throw new NotFoundException("no matching version found for " + skillName + " with constraint " + constraint);
        }

        return new VersionResolution(
            skillName,
            constraint,
            matched.get(0).toString(),
            matched.stream().map(SemVer::toString).toList()
        );
    }

    public List<VersionInfo> listVersions(String skillName) {
        return skillVersionRepository.findBySkillName(skillName).stream()
            .sorted((a, b) -> SemVerParser.parse(b.versionSemver).compareTo(SemVerParser.parse(a.versionSemver)))
            .map(v -> new VersionInfo(
                v.id,
                v.versionSemver,
                v.prerelease,
                v.latest,
                v.yanked,
                v.publishedAt == null ? null : v.publishedAt.toString()
            ))
            .toList();
    }
}
