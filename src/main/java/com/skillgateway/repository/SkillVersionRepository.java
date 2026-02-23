package com.skillgateway.repository;

import com.skillgateway.model.SkillVersion;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class SkillVersionRepository implements PanacheRepository<SkillVersion> {

    public Optional<SkillVersion> findBySkillAndVersion(Long skillId, String version) {
        return find("skill.id = ?1 and versionSemver = ?2", skillId, version).firstResultOptional();
    }

    public List<SkillVersion> findBySkillName(String name) {
        return find("skill.name = ?1 order by publishedAt desc", name).list();
    }

    public Optional<SkillVersion> findLatestBySkillId(Long skillId) {
        return find("skill.id = ?1 and latest = true", skillId).firstResultOptional();
    }

    public void clearLatestBySkillId(Long skillId) {
        update("latest = false where skill.id = ?1", skillId);
    }

    public Optional<SkillVersion> findByNameAndVersion(String name, String version) {
        return find("skill.name = ?1 and versionSemver = ?2", name, version).firstResultOptional();
    }
}
