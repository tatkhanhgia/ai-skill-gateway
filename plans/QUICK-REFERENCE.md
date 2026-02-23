# Skill Repository Quick Reference

**Use this for:** Implementation decisions, API design, schema questions
**Full details in:** skill-repository-design-research.md

---

## SemVer Quick Rules

| Pattern | Meaning | Example |
|---------|---------|---------|
| MAJOR.MINOR.PATCH | Standard release | 1.2.3 |
| ^1.2.3 | Compatible (same major) | Matches 1.3.0, 1.5.2, not 2.0.0 |
| ~1.2.3 | Conservative (patch only) | Matches 1.2.5, not 1.3.0 |
| >=1.0.0 <2.0.0 | Explicit range | Matches 1.0.0 to 1.9.9 |
| 1.0.0-alpha | Prerelease (highest risk) | For early developer testing |
| 1.0.0-beta | Beta (medium risk) | For community testing |
| 1.0.0-rc.1 | Release candidate | Final checks before 1.0.0 |
| latest | Highest stable | Non-prerelease only |

**Risk Levels:** 0.x.x (highest) > alpha > beta > rc > release (lowest)

---

## skill.yaml Minimal Template

```yaml
name: my-skill
version: 1.0.0
description: What this skill does
category: Data Processing
tags:
  - analysis
  - starter

requires:
  - python: ">=3.11"
  - skill: vector-embedding@^1.0.0

repository:
  url: "https://github.com/org/repo"
  ref: main
```

---

## PostgreSQL Schema (3 Core Tables)

```sql
CREATE TABLE skills (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  category VARCHAR(100) NOT NULL,
  description TEXT,
  search_vector tsvector  -- Full-text index
);

CREATE TABLE skill_versions (
  id BIGSERIAL PRIMARY KEY,
  skill_id BIGINT REFERENCES skills(id),
  version_semver VARCHAR(50) UNIQUE NOT NULL,
  is_prerelease BOOLEAN,
  requires TEXT  -- JSON array
);

CREATE TABLE skill_tags (
  skill_id BIGINT REFERENCES skills(id),
  tag_name VARCHAR(100) NOT NULL,
  PRIMARY KEY (skill_id, tag_name)
);

CREATE INDEX skills_search_idx ON skills USING GIN(search_vector);
```

---

## Search Strategy

```
1. Full-text keyword search on tsvector (PostgreSQL)
   → Returns relevant skill_ids with ranking score

2. Tag + category filter
   → WHERE category = ? AND tag_name IN (...)

3. Semantic search on descriptions (Vector DB)
   → Embed query → cosine similarity

4. Rerank results
   → Score = keyword(0.3) + semantic(0.5) + popularity(0.2)

5. Return top-k
```

---

## Discovery Categories (8 Primary)

```
Data Processing    → ETL, transformation, parsing
Analytics          → Statistics, reporting, insights
AI/ML              → Models, training, inference
Code Generation    → Templates, scaffolding
DevOps             → Deployment, monitoring, CI/CD
Security           → Encryption, auth, audit
Testing            → Unit, integration, e2e
Documentation      → Generation, publishing
```

---

## Version Resolution Pseudocode

```python
def resolve_version(skill_name, constraint):
  versions = db.query("SELECT version_semver FROM skill_versions WHERE skill_id = ?")

  if constraint == "latest":
    return max([v for v in versions if not v.is_prerelease])

  if constraint.startswith("^"):
    base = parse_semver(constraint[1:])
    matches = [v for v in versions if v.major == base.major and v >= base]
  elif constraint.startswith("~"):
    base = parse_semver(constraint[1:])
    matches = [v for v in versions if v.major == base.major and v.minor == base.minor and v >= base]
  else:
    matches = [v for v in versions if v == constraint]

  if not matches:
    raise VersionConstraintError(f"No match for {skill_name}@{constraint}")

  return max(matches)
```

---

## API Endpoints (MVP)

```
POST   /api/v1/skills/publish
       → Input: skill.yaml manifest
       → Output: { id, version_id, published_at }

GET    /api/v1/skills/search?query=X&category=Y&tags=Z
       → Output: [{ id, name, version, description, relevance_score }]

GET    /api/v1/skills/{name}/resolve-version?constraint=^1.0.0
       → Output: { resolved_version, requires }

GET    /api/v1/skills/{name}/versions
       → Output: [{ version_semver, published_at, is_prerelease }]
```

---

## Implementation Checklist

### Phase 1 (Foundation)
- [ ] PostgreSQL schema + migrations
- [ ] SemVer parser (parse_semver function)
- [ ] Version resolver (matches_constraint logic)
- [ ] skill.yaml validator (Pydantic schema)
- [ ] Publish API endpoint
- [ ] Keyword search (tsvector)
- [ ] Tag filter queries

### Phase 2 (Intelligence)
- [ ] Vector DB (ChromaDB) integration
- [ ] Semantic search endpoint
- [ ] Hybrid reranking (keyword + semantic + popularity)
- [ ] Prerelease warnings/flags
- [ ] Dependency resolution (DAG traversal)

### Phase 3 (Production)
- [ ] Advanced constraint solver
- [ ] Conflict detection
- [ ] Analytics + quality metrics
- [ ] Multi-language FTS
- [ ] Migration scripts (file → DB)

---

## Common Pitfalls to Avoid

| Pitfall | Solution |
|---------|----------|
| No version locking | Use skill-lock.json (like package-lock.json) |
| Ambiguous constraint | Fail fast if resolution has no unique best match |
| Tag explosion | Enforce governance; curate allowed tags |
| Prerelease auto-selection | Require explicit opt-in for non-prod |
| Search vector staleness | Rebuild via trigger on INSERT/UPDATE |
| Circular dependencies | Detect in DAG traversal; return error with cycle path |

---

## File Structure (Go-forward)

```
src/registry/
  ├── models.py            # SkillManifest, Version, etc
  ├── db_schema.py         # SQLAlchemy ORM + migrations
  ├── repository.py        # Publish, search, resolve CRUD
  ├── version_resolver.py  # SemVer parsing + constraint logic
  ├── search_engine.py     # tsvector + semantic search
  └── indexer.py           # Vector DB integration

src/api/
  ├── routes.py            # FastAPI endpoints
  └── dependencies.py      # DB session, auth, etc

tests/
  ├── test_version_resolver.py
  ├── test_search.py
  └── test_api.py
```

---

## Decision Matrix for Adoption

| Feature | npm | HF Hub | GitHub | Adopt? |
|---------|-----|-------|--------|--------|
| SemVer | Yes | No (git tags) | Yes | **Yes** |
| Manifest file | package.json | model card | action.yml | **Yes** |
| Git-centric | No (npm registry) | **Yes** | **Yes** | Partial |
| Full-text search | Yes (npm.com UI) | Yes (Hugging Face Hub) | Yes (GitHub) | **Yes** |
| Semantic search | No | No (embeddings are internal) | No | **Yes** (add) |
| Tags/Categories | Keywords | task/language/modality | action categories | **Yes** (flat) |
| Version constraints | ^, ~, ranges | git refs | not applicable | **Yes** |

**Conclusion:** Adopt best practices from all three; npm versioning is gold standard, HF metadata is comprehensive, GitHub manifest is lightweight.

---

*Last Updated: 2026-02-21*
