# AI Skill Repository Research Summary

**Generated:** 2026-02-21
**Status:** Research Complete - Ready for Implementation Planning

---

## Executive Summary

Researched design patterns from **npm registry**, **Hugging Face Hub**, and **GitHub Marketplace** to establish best practices for AI skill repository architecture. Key findings distilled into actionable patterns for skill discovery, versioning, metadata management, and PostgreSQL schema design.

---

## Core Findings

### 1. Skill Discovery Works Best with Hybrid Approach
- **Keyword-based:** PostgreSQL full-text search (tsvector) handles recall
- **Semantic-based:** Vector DB (ChromaDB) handles relevance on descriptions
- **Tag-based:** Flat taxonomy avoids brittleness of hierarchical categories
- **Reranking:** Combine keyword (0.3) + semantic (0.5) + popularity (0.2) scores

**Pattern:** npm uses keywords, HF uses task/language tags, GitHub uses action.yml metadata. Best approach = all three combined.

### 2. Version Management Must Be Explicit
- **Standard:** SemVer (MAJOR.MINOR.PATCH) with prerelease/build metadata
- **Constraint Syntax:** `^1.2.3` (compatible major), `~1.2.3` (patch only), ranges
- **Prerelease Lifecycle:** alpha → beta → rc → release, with clear risk signals
- **Resolution:** DAG traversal to detect circular deps; fail fast on conflicts

**Pattern:** Version constraints resolved at "install" time (like npm). Lock files capture snapshot for reproducibility.

### 3. Skill Metadata Must Support Multiple Discovery Modes
- **Git-centric:** (like HF) Single repo is source of truth; version = branch/tag
- **Manifest-centric:** (like npm) Separate metadata file (`skill.yaml`) with schema validation
- **Category-first:** Primary category + unlimited tags for flexibility
- **Rich Documentation:** README, examples, schema, API docs linked in manifest

**Pattern:** skill.yaml includes name, version, category, tags, dependencies, capabilities, repository metadata.

### 4. PostgreSQL Schema Supports Large-Scale Registry
- **Core Tables:** `skills` (catalog), `skill_versions` (versions + dependencies), `skill_tags` (discovery)
- **Search:** tsvector index (GIN) for weighted full-text search; tag filtering via WHERE clause
- **Ranking:** ts_rank() function scores matches; BM25 extensions for advanced scoring
- **Constraints:** Unique constraints on (skill_id, version_semver); foreign keys for referential integrity

**Pattern:** Append-only versions; skill metadata updated via ON CONFLICT clause; search vector built from weighted fields.

### 5. Discovery Categories Should Be Flat & Exhaustive
**8 Primary Categories (inspired by npm, HF, GitHub):**
- Data Processing (ETL, transformation)
- Analytics (reporting, insights)
- AI/ML (models, training, inference)
- Code Generation (templates, scaffolding)
- DevOps (deployment, CI/CD)
- Security (encryption, auth)
- Testing (unit, integration, e2e)
- Documentation (generation, publishing)

**Tags:** Unlimited flat namespace (language, domain, complexity, status, integration).

---

## Recommended Architecture

```
┌─────────────────────────────────────┐
│    Search API (FastAPI)             │
│  GET /skills/search?query=X         │
│  GET /skills/resolve-version        │
│  POST /skills/publish               │
└──────────┬──────────────────────────┘
           │
     ┌─────┴─────────────────┐
     │                       │
┌────▼──────────────────┐  ┌─▼──────────────┐
│ PostgreSQL Registry   │  │  Vector DB     │
│ - skills             │  │  (ChromaDB)    │
│ - skill_versions     │  │  - embeddings  │
│ - skill_tags         │  │  - similarity  │
│ - tsvector index     │  │    search      │
└───────────────────────┘  └────────────────┘

↓ Data Flow for Search ↓
1. Parse query → tokenize
2. Keyword search: tsvector @@ query → skill_ids
3. Semantic search: embed query → cosine sim → skill_ids
4. Tag filter: WHERE tag IN (...)
5. Rerank: combine scores (keyword 0.3 + semantic 0.5 + popularity 0.2)
6. Return top-k
```

---

## Implementation Phases

### Phase 1: Foundation (Weeks 1-2)
- [ ] PostgreSQL schema: skills, skill_versions, skill_tags tables
- [ ] SemVer parser + version constraint resolver
- [ ] skill.yaml manifest format + validation
- [ ] Basic full-text search (tsvector)
- [ ] API: POST /publish, GET /search (keyword + tag filter)

### Phase 2: Intelligence (Weeks 3-4)
- [ ] Vector DB integration (embed skill descriptions)
- [ ] Semantic search endpoint
- [ ] Hybrid search ranking (keyword + semantic + popularity)
- [ ] Prerelease lifecycle (alpha, beta, rc, stable)
- [ ] Dependency resolution (DAG + constraint satisfaction)

### Phase 3: Production (Weeks 5-6)
- [ ] Advanced constraint solving (complex version ranges)
- [ ] Conflict detection + resolution suggestions
- [ ] Analytics (usage stats, quality scores)
- [ ] Multi-language full-text search
- [ ] Migration from file-based to DB-based registry

---

## Key Design Decisions

| Decision | Rationale | Trade-off |
|----------|-----------|-----------|
| PostgreSQL tsvector | Native FTS; no extra infra | Must maintain search_vector via trigger/update |
| SemVer MAJOR.MINOR.PATCH | Industry standard; clear signals | Complexity in constraint parsing |
| Flat tag taxonomy | Flexible; avoids brittleness | Requires governance to prevent tag explosion |
| Skill manifest in YAML | Git-friendly; human-readable | Schema evolution requires migration scripts |
| Hybrid search (keyword + semantic) | Best of both worlds | Must maintain two indexes; more complex ranking |
| Git repo = source of truth | Immutable history; branching | Requires version ↔ branch/tag mapping |

---

## API Specification (High-level)

```
POST /api/v1/skills/publish
  Input: skill.yaml manifest
  Output: skill_id, version_id, published_at

GET /api/v1/skills/search
  Query Params: query, category, tags, limit
  Output: [{ id, name, version, description, rating, downloads, relevance_score }]

GET /api/v1/skills/{name}/resolve-version
  Query Params: constraint (e.g., ^1.0.0, ~1.2.3)
  Output: { resolved_version, available_versions, requires, prerelease }

GET /api/v1/skills/{name}/versions
  Output: [ { version_semver, published_at, is_prerelease, downloads } ]

POST /api/v1/skills/{name}/versions/{version}/yank
  Output: { yanked_at, reason }  (for security/bugs)
```

---

## Reference Materials

**Main Research Document:**
- `/plans/skill-repository-design-research.md` (150 lines, all patterns + schema)

**Implementation Guide:**
- `/plans/skill-registry-implementation-guide.md` (code patterns, migrations)

**External References:**
- [npm SemVer](https://semver.org/)
- [Hugging Face Model Card Spec](https://huggingface.co/docs/hub/en/model-cards)
- [GitHub Action Metadata](https://docs.github.com/en/actions/creating-actions/metadata-syntax-for-github-actions)
- [PostgreSQL Full-Text Search](https://www.postgresql.org/docs/current/textsearch.html)

---

## Next Steps

1. **Planner Agent:** Create detailed implementation plan with task breakdown
2. **Developer Agent:** Implement Phase 1 (PostgreSQL + SemVer + basic search)
3. **Tester Agent:** Write tests for version resolution + search ranking
4. **Code Reviewer:** Ensure schema design aligns with patterns
5. **Docs Manager:** Update `/docs` with registry architecture & API docs

---

## Unresolved Questions

1. **Prerelease Selection Policy:** Auto-select for non-prod? Require explicit opt-in?
2. **Transitive Dependency Updates:** How to handle breaking changes in deps of deps?
3. **Deprecation Policy:** Timeline and replacement guidance for EOL skills?
4. **Skill Provenance:** Track code origin/author verification for security?
5. **Multi-region Registry:** Should this support federated skill repositories?

---

*Research conducted on 2026-02-21. Document ready for architectural review and implementation planning phase.*
