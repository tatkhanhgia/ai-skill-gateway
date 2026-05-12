package com.skillgateway.repository;

import com.skillgateway.model.SkillVersionFile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class SkillVersionFileRepository implements PanacheRepository<SkillVersionFile> {

    public List<SkillVersionFile> findByVersionId(Long versionId) {
        return find("version.id = ?1 order by path", versionId).list();
    }
}
