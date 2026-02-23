package com.skillgateway.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "skill_versions")
public class SkillVersion extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    public Skill skill;

    @Column(name = "version_semver", nullable = false)
    public String versionSemver;

    @Column(name = "is_prerelease", nullable = false)
    public boolean prerelease;

    @Column(name = "is_latest", nullable = false)
    public boolean latest;

    @Column(name = "is_yanked", nullable = false)
    public boolean yanked;

    @Column(name = "requires", columnDefinition = "jsonb")
    public String requires = "[]";

    @Column(name = "release_notes", columnDefinition = "TEXT")
    public String releaseNotes;

    @Column(name = "yank_reason")
    public String yankReason;

    @Column(name = "published_at", insertable = false, updatable = false)
    public LocalDateTime publishedAt;
}
