# AI Skill Repository Design Patterns & Package Management Research

**Date:** 2026-02-21
**Focus:** Design patterns for skill discovery, versioning, metadata, and indexing
**Reference Systems:** npm, Hugging Face Hub, GitHub Marketplace
**Token Budget:** ≤150 lines, grammar sacrificed for concision

---

## 1. Skill Discovery & Metadata Structure

### npm Registry Model
- **Package Manifest:** `package.json` contains name, version, description, keywords, author, license, dependencies
- **Keyword-based Discovery:** Flat list of searchable keywords enables basic classification
- **Metadata Storage:** Centralized registry API stores package metadata separately from code
- **Version Resolution:** Dependency tree resolved at install time; `package-lock.json` captures snapshots

### Hugging Face Hub Model
- **Git-based Repository:** Single versioned repo contains model weights, configs, tokenizers, docs
- **Model Card (YAML + Markdown):** Structured metadata layer with fields: task, languages, modalities, license, datasets, base_model
- **Task-centric Discovery:** Models mapped to single `pipeline_tag` (classification, translation, etc.)
- **Multilayer Metadata:** Config files (config.json) define architecture; README provides human context
- **Rich Discovery:** Metadata supports filtering by task, language, modality; inference APIs standardized per task

### GitHub Marketplace Model
- **Action Manifest:** `action.yml/action.yaml` at repo root defines metadata, inputs, outputs
- **Category + Secondary Tagging:** Primary category required; optional secondary category for cross-cutting concerns
- **Version Tagging:** Git tags map to releases; version numbers communicate impact level
- **Registry Isolation:** Separate repo per action; marketplace indexes manifests without code duplication

---

## 2. Skill Definition Format

### Proposed Schema (inspired by all three models)
```yaml
skill:
  name: skill-name                    # Unique identifier
  version: 1.2.3                      # Semantic versioning
  title: "Human-readable title"       # Display name
  description: "Brief overview"       # 1-2 sentences
  category: primary_category          # Primary classification
  tags: [tag1, tag2]                  # Searchable keywords

metadata:
  author: "creator/org"
  license: MIT|Apache2|GPL            # SPDX identifier
  requires: ["python>=3.11", "ollama"] # Dependencies
  minVersion: "1.0.0"                 # Compatibility baseline

discovery:
  languages: [en, vi]                 # Supported languages
  capabilities: [analysis, generation] # What skill does
  inputTypes: [text, json]            # Input formats
  outputTypes: [text, structured]     # Output formats

repository:
  url: "https://..."                  # Source repo
  branch: main|develop                # Version branch mapping
  path: /src/skill_name               # Location in repo

documentation:
  readmeUrl: "/README.md"             # Setup & usage
  examplesUrl: "/examples"            # Working demos
  apiDocsUrl: "/docs/api.md"         # API specification
```

---

## 3. Version Management & SemVer Strategy

### Core Pattern: MAJOR.MINOR.PATCH[-prerelease][+build]
- **MAJOR:** Breaking API changes (incompatible with existing calls)
- **MINOR:** Backward-compatible features (new capabilities, non-breaking enhancements)
- **PATCH:** Bug fixes (internal implementation changes)
- **Prerelease:** `-alpha`, `-beta`, `-rc.1` for unstable releases (sorted lexically)
- **Build metadata:** `+build.123` ignored during precedence comparison

### Prerelease Risk Levels
| Version | Risk | Use Case |
|---------|------|----------|
| 0.y.z | Highest | WIP, breaking changes expected |
| X.0.0-alpha | High | Early testing by developers |
| X.0.0-beta | Medium | Community testing, API mostly stable |
| X.0.0-rc.N | Low | Final stability checks before release |
| X.Y.Z | Production | Stable, backward-compatible |

### Version Lifecycle
```
Development → Alpha → Beta → RC → Release → Maintenance
  (0.x.x)    (x.0.0-alpha) (beta) (rc)      (x.y.z)
```

---

## 4. PostgreSQL Schema Design for Metadata

### Core Tables
```sql
-- Skills catalog
CREATE TABLE skills (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) UNIQUE NOT NULL,
  category VARCHAR(100) NOT NULL,
  description TEXT,
  repository_url VARCHAR(512),
  author_id BIGINT REFERENCES users(id),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW(),
  search_vector tsvector  -- Full-text search column
);

-- Versions with metadata
CREATE TABLE skill_versions (
  id BIGSERIAL PRIMARY KEY,
  skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
  version_semver VARCHAR(50) NOT NULL,  -- "1.2.3-beta.1"
  is_prerelease BOOLEAN DEFAULT FALSE,
  is_latest BOOLEAN DEFAULT FALSE,
  requires TEXT,  -- JSON: ["python>=3.11", "ollama"]
  published_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(skill_id, version_semver)
);

-- Tags for discovery (many-to-many)
CREATE TABLE skill_tags (
  id BIGSERIAL PRIMARY KEY,
  skill_id BIGINT NOT NULL REFERENCES skills(id) ON DELETE CASCADE,
  tag_name VARCHAR(100) NOT NULL,
  UNIQUE(skill_id, tag_name)
);

-- Full-text search index
CREATE INDEX skills_search_idx ON skills USING GIN(search_vector);

-- Version constraints
CREATE INDEX version_lookup ON skill_versions(skill_id, version_semver);
```

### Search & Indexing Strategy
- **Full-Text Search:** PostgreSQL `tsvector` with stemming (English by default) + stop-word removal
- **Weighted Fields:** Title (A-weight) > description (B-weight) > tags (C-weight)
- **Tag-based Filtering:** `WHERE EXISTS (SELECT 1 FROM skill_tags WHERE tag_name = ?) AND category = ?`
- **Relevance Ranking:** ts_rank() function scores matches; BM25 extensions available via pg_textsearch

---

## 5. Skill Categories & Tagging Schema

### Primary Categories (Exhaustive)
```
- Data Processing (ETL, transformation, parsing)
- Analytics (statistics, reporting, insights)
- AI/ML (models, training, inference)
- Code Generation (templates, scaffolding)
- DevOps (deployment, monitoring, CI/CD)
- Security (encryption, authentication, audit)
- Testing (unit, integration, e2e tests)
- Documentation (generation, publishing)
```

### Tag Taxonomy (Flat, unlimited)
- **Language:** python, javascript, go, java, rust
- **Domain:** nlp, vision, timeseries, graphs
- **Complexity:** beginner, intermediate, advanced
- **Status:** stable, experimental, deprecated
- **Integration:** slack, github, aws, docker

### Discovery Algorithms
1. **Keyword Search:** Query → tokenize → stem → tsvector match (PostgreSQL)
2. **Semantic Search:** Embed query + skill descriptions → cosine similarity (vector DB)
3. **Tag Filtering:** WHERE tag IN (user_tags) AND category = X (SQL filter)
4. **Reranking:** Combine keyword score (0.3) + semantic score (0.5) + popularity (0.2)

---

## 6. Version Resolution & Dependency Management

### Version Constraint Resolution
```
"^1.2.3"  → >=1.2.3 <2.0.0  (compatible, same major)
"~1.2.3"  → >=1.2.3 <1.3.0  (conservative, patch only)
">=1.0.0 <2.0.0"  → explicit range
"1.2.3-beta" → exact prerelease version
"latest" → resolve to latest stable (non-prerelease)
```

### Dependency Graph Resolution
- **DAG Traversal:** DFS to detect circular deps; fail fast on conflicts
- **Constraint Satisfaction:** Find version set satisfying all constraints
- **Lock Files:** `skill-lock.json` records resolved tree for reproducibility
- **Fallback Strategy:** If no compatible version, report conflict to user

---

## 7. Implementation Priorities

### Phase 1 (MVP)
- PostgreSQL schema (skills, versions, tags tables)
- SemVer parser + version resolution logic
- Full-text search via tsvector (basic keyword + tag filtering)
- Skill manifest format (YAML) with basic validation

### Phase 2
- Vector DB integration (semantic search on descriptions)
- Reranking pipeline (keyword + semantic + popularity)
- Prerelease lifecycle management + deprecation warnings
- Category taxonomy enforcement

### Phase 3
- Advanced constraint solving (complex version ranges)
- Dependency conflict detection + resolution suggestions
- Analytics (usage stats, download trends, quality scores)
- Multi-language full-text search configs

---

## Key Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| PostgreSQL tsvector for search | Native FTS; no extra infra; GIN indexes scale |
| SemVer + MAJOR.MINOR.PATCH | Industry standard; clear compatibility signaling |
| Skill manifest in YAML | Human-readable; git-friendly; GitHub/HF precedent |
| Hybrid search (keyword + semantic) | Keyword = recall; semantic = relevance; combined = best UX |
| Tag-based categories | Flat namespace; flexible; avoids taxonomy brittleness |
| Git repo as source of truth | Immutable history; branching for versions; standard tooling |

---

## Unresolved Questions

1. Should prerelease versions be automatically selected for non-prod environments, or require explicit opt-in?
2. How to handle skill dependency updates when transitive deps have breaking changes?
3. What deprecation policy (timeline, replacement guidance) for EOL skills?
4. Should skill "provenance" (code origin, author verification) be tracked for security?

---

## Sources

- [About semantic versioning | npm Docs](https://docs.npmjs.com/about-semantic-versioning/)
- [Semantic Versioning 2.0.0](https://semver.org/)
- [Hugging Face Hub documentation](https://huggingface.co/docs/hub/en/index)
- [Model Cards - Hugging Face](https://huggingface.co/docs/hub/en/model-cards)
- [Publishing actions in GitHub Marketplace](https://docs.github.com/en/actions/sharing-automations/creating-actions/publishing-actions-in-github-marketplace)
- [PostgreSQL: Full Text Search](https://www.postgresql.org/docs/current/textsearch.html)
- [Postgres Full-Text Search: A Search Engine in a Database](https://www.crunchydata.com/blog/postgres-full-text-search-a-search-engine-in-a-database)
