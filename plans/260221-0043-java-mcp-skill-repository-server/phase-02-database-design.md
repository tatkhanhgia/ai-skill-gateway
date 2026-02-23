# Phase 02: Database Design

## Context Links

- [Plan Overview](./plan.md)
- [Skill Repository Design Research](../skill-repository-design-research.md)
- [Architecture Patterns - Schema](../ARCHITECTURE-PATTERNS.md)
- [Quick Reference - Schema](../QUICK-REFERENCE.md)

## Overview

- **Priority:** P0
- **Status:** completed
- **Description:** Design and implement PostgreSQL schema for skill registry. Includes skills catalog, versioning, tags, full-text search (tsvector), and vector embeddings (pgvector). All via Flyway migrations.

## Key Insights

- 3 core tables: `skills`, `skill_versions`, `skill_tags` (from research)
- tsvector with weighted fields (name A, description B, tags C) for FTS
- GIN index on tsvector column for fast search
- pgvector column on skills for semantic search embeddings
- Append-only versions; `is_latest` flag for quick latest lookup
- ON CONFLICT clause for idempotent publish operations
- Trigger-based search_vector rebuild on INSERT/UPDATE

## Requirements

### Functional
- Store skill metadata (name, category, description, author, repo URL)
- Store multiple versions per skill with SemVer strings
- Store tags as many-to-many relationship
- Full-text search via tsvector with weighted ranking
- Vector embeddings stored per skill for semantic search
- Yank (soft-delete) versions for security issues

### Non-Functional
- GIN index on search_vector for sub-100ms FTS queries
- HNSW index on embedding column for sub-50ms vector search
- Unique constraints prevent duplicate skill names and version combos
- Foreign key cascading on skill deletion

## Architecture

```
skills (catalog)
  |-- id (PK, BIGSERIAL)
  |-- name (UNIQUE, VARCHAR)
  |-- category (VARCHAR)
  |-- description (TEXT)
  |-- author (VARCHAR)
  |-- repository_url (VARCHAR)
  |-- download_count (INT, default 0)
  |-- search_vector (tsvector)           -- FTS
  |-- embedding (vector(768))            -- pgvector
  |-- created_at, updated_at (TIMESTAMP)
  |
  +--< skill_versions (1:N)
  |     |-- id (PK)
  |     |-- skill_id (FK -> skills)
  |     |-- version_semver (VARCHAR)
  |     |-- is_prerelease (BOOLEAN)
  |     |-- is_latest (BOOLEAN)
  |     |-- is_yanked (BOOLEAN)
  |     |-- requires (JSONB)
  |     |-- release_notes (TEXT)
  |     |-- published_at (TIMESTAMP)
  |     |-- UNIQUE(skill_id, version_semver)
  |
  +--< skill_tags (M:N flat)
        |-- skill_id (FK -> skills)
        |-- tag_name (VARCHAR)
        |-- PRIMARY KEY(skill_id, tag_name)
```

## Related Code Files

### Create
- `src/main/resources/db/migration/V1__create_skills_schema.sql`
- `src/main/resources/db/migration/V2__create_search_indexes.sql`
- `src/main/resources/db/migration/V3__create_search_vector_trigger.sql`
- `src/main/java/com/skillgateway/model/Skill.java` (JPA entity)
- `src/main/java/com/skillgateway/model/SkillVersion.java` (JPA entity)
- `src/main/java/com/skillgateway/model/SkillTag.java` (JPA entity)
- `src/main/java/com/skillgateway/repository/SkillRepository.java` (Panache)
- `src/main/java/com/skillgateway/repository/SkillVersionRepository.java`

## Implementation Steps

1. **V1 Migration: Core tables**
   ```sql
   CREATE EXTENSION IF NOT EXISTS vector;

   CREATE TABLE skills (
     id BIGSERIAL PRIMARY KEY,
     name VARCHAR(255) UNIQUE NOT NULL,
     category VARCHAR(100) NOT NULL,
     description TEXT,
     author VARCHAR(255),
     repository_url VARCHAR(512),
     download_count INTEGER DEFAULT 0,
     search_vector tsvector,
     embedding vector(768),
     created_at TIMESTAMP DEFAULT NOW(),
     updated_at TIMESTAMP DEFAULT NOW()
   );

   CREATE TABLE skill_versions (
     id BIGSERIAL PRIMARY KEY,
     skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
     version_semver VARCHAR(50) NOT NULL,
     is_prerelease BOOLEAN DEFAULT FALSE,
     is_latest BOOLEAN DEFAULT FALSE,
     is_yanked BOOLEAN DEFAULT FALSE,
     requires JSONB DEFAULT '[]',
     release_notes TEXT,
     published_at TIMESTAMP DEFAULT NOW(),
     UNIQUE(skill_id, version_semver)
   );

   CREATE TABLE skill_tags (
     skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
     tag_name VARCHAR(100) NOT NULL,
     PRIMARY KEY(skill_id, tag_name)
   );
   ```

2. **V2 Migration: Indexes**
   ```sql
   CREATE INDEX idx_skills_search_vector ON skills USING GIN(search_vector);
   CREATE INDEX idx_skills_embedding ON skills USING hnsw(embedding vector_cosine_ops);
   CREATE INDEX idx_skills_category ON skills(category);
   CREATE INDEX idx_skill_versions_lookup ON skill_versions(skill_id, is_latest);
   CREATE INDEX idx_skill_tags_tag ON skill_tags(tag_name);
   ```

3. **V3 Migration: Auto-update search_vector trigger**
   ```sql
   CREATE OR REPLACE FUNCTION update_search_vector() RETURNS trigger AS $$
   BEGIN
     NEW.search_vector :=
       setweight(to_tsvector('english', COALESCE(NEW.name, '')), 'A') ||
       setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B');
     NEW.updated_at := NOW();
     RETURN NEW;
   END;
   $$ LANGUAGE plpgsql;

   CREATE TRIGGER trg_skills_search_vector
     BEFORE INSERT OR UPDATE ON skills
     FOR EACH ROW EXECUTE FUNCTION update_search_vector();
   ```

4. **JPA Entities** - Map tables with Panache; use `@Formula` for computed fields
5. **Panache Repositories** - SkillRepository with custom finder methods
6. **Seed data** - Optional V4 migration with sample categories

## Todo List

- [ ] Write V1 migration (core tables)
- [ ] Write V2 migration (indexes)
- [ ] Write V3 migration (search vector trigger)
- [ ] Create Skill JPA entity
- [ ] Create SkillVersion JPA entity
- [ ] Create SkillTag JPA entity
- [ ] Create SkillRepository (Panache)
- [ ] Create SkillVersionRepository
- [ ] Verify Flyway runs all migrations on startup
- [ ] Test tsvector search via psql
- [ ] Test pgvector storage via psql

## Success Criteria

- All 3 migrations run cleanly on fresh database
- `SELECT * FROM skills WHERE search_vector @@ to_tsquery('analysis')` returns results
- pgvector embedding column accepts 768-dim vectors
- HNSW index operational for cosine similarity queries
- JPA entities map correctly; Panache CRUD operations work
- Trigger auto-updates search_vector on INSERT/UPDATE

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| pgvector extension not available | No semantic search | Use pgvector Docker image; fallback to keyword-only |
| HNSW index slow on large datasets | Search latency | Tune `m` and `ef_construction` params; start with ivfflat |
| tsvector language mismatch | Poor search quality | Allow configurable dictionary; default 'english' |
| JSONB requires column queries | Complex filtering | Keep JSONB for flexible deps; index if needed later |

## Security Considerations

- No PII stored in skills tables
- DB user has minimum required privileges (CRUD on schema tables only)
- Migration scripts reviewed for SQL injection (parameterized in app code)
- Embedding vectors not sensitive but bulk export should be rate-limited

## Next Steps

- Phase 03: Implement CRUD service layer and MCP tool handlers
- Phase 04: Wire up search (FTS + semantic) in SearchService
