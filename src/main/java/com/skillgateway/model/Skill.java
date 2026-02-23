package com.skillgateway.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
public class Skill extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true)
    public String name;

    @Column(nullable = false)
    public String category;

    @Column(columnDefinition = "TEXT")
    public String description;

    public String author;

    @Column(name = "repository_url")
    public String repositoryUrl;

    @Column(name = "download_count", nullable = false)
    public Integer downloadCount = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    public LocalDateTime updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @jakarta.persistence.CollectionTable(
        name = "skill_tags",
        joinColumns = @jakarta.persistence.JoinColumn(name = "skill_id")
    )
    @Column(name = "tag_name")
    public Set<String> tags = new HashSet<>();

    @Column(name = "embedding", columnDefinition = "vector(768)")
    public String embedding;
}
