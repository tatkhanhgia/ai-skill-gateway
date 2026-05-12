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

@Entity
@Table(name = "skill_version_files")
public class SkillVersionFile extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    public SkillVersion version;

    @Column(name = "path", nullable = false)
    public String path;

    @Column(name = "sha256", nullable = false)
    public String sha256;

    @Column(name = "size_bytes", nullable = false)
    public long size;

    @Column(name = "media_type")
    public String mediaType;

    @Column(name = "role", nullable = false)
    public String role;
}
