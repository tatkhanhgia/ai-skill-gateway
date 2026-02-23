package com.skillgateway.repository;

import com.skillgateway.model.Skill;
import com.skillgateway.model.dto.SkillSummary;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class SkillRepository implements io.quarkus.hibernate.orm.panache.PanacheRepository<Skill> {

    public Optional<Skill> findByName(String name) {
        return find("name", name).firstResultOptional();
    }

    public List<SkillSummary> listSkills(String category, List<String> tags, int page, int size) {
        List<Skill> skills;
        if (category == null || category.isBlank()) {
            skills = findAll().page(page, size).list();
        } else {
            skills = find("category", category).page(page, size).list();
        }

        List<SkillSummary> out = new ArrayList<>();
        for (Skill skill : skills) {
            if (tags != null && !tags.isEmpty() && !skill.tags.containsAll(tags)) {
                continue;
            }
            out.add(new SkillSummary(
                skill.id,
                skill.name,
                skill.category,
                skill.description,
                skill.downloadCount == null ? 0 : skill.downloadCount,
                skill.tags.stream().sorted().toList()
            ));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public List<ScoredSkill> keywordSearch(String query, int limit) {
        EntityManager em = getEntityManager();
        List<Object[]> rows = em.createNativeQuery("""
            SELECT s.id, s.name, s.description, s.category, s.download_count,
                   ts_rank(s.search_vector, plainto_tsquery('english', :query)) as score
            FROM skills s
            WHERE s.search_vector @@ plainto_tsquery('english', :query)
            ORDER BY score DESC
            LIMIT :limit
            """)
            .setParameter("query", query)
            .setParameter("limit", limit)
            .getResultList();

        List<ScoredSkill> out = new ArrayList<>();
        for (Object[] row : rows) {
            out.add(ScoredSkill.fromRow(row));
        }
        return out;
    }

    public Map<Long, List<String>> findTagsBySkillIds(List<Long> skillIds) {
        if (skillIds == null || skillIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = getEntityManager().createNativeQuery("""
            SELECT st.skill_id, st.tag_name
            FROM skill_tags st
            WHERE st.skill_id IN (:ids)
            ORDER BY st.tag_name ASC
            """)
            .setParameter("ids", skillIds)
            .getResultList();

        Map<Long, List<String>> tagsById = new HashMap<>();
        for (Long id : skillIds) {
            tagsById.put(id, new ArrayList<>());
        }
        for (Object[] row : rows) {
            Long skillId = ((Number) row[0]).longValue();
            String tag = (String) row[1];
            tagsById.computeIfAbsent(skillId, ignored -> new ArrayList<>()).add(tag);
        }
        return tagsById;
    }

    @SuppressWarnings("unchecked")
    public List<ScoredSkill> vectorSearch(String vectorLiteral, int limit) {
        EntityManager em = getEntityManager();
        List<Object[]> rows = em.createNativeQuery("""
            SELECT s.id, s.name, s.description, s.category, s.download_count,
                   1 - (s.embedding <=> CAST(:vec AS vector)) as score
            FROM skills s
            WHERE s.embedding IS NOT NULL
            ORDER BY s.embedding <=> CAST(:vec AS vector)
            LIMIT :limit
            """)
            .setParameter("vec", vectorLiteral)
            .setParameter("limit", limit)
            .getResultList();

        List<ScoredSkill> out = new ArrayList<>();
        for (Object[] row : rows) {
            out.add(ScoredSkill.fromRow(row));
        }
        return out;
    }
}
