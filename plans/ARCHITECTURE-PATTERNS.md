# Skill Registry Architecture Patterns

**Visual & Conceptual Reference**

---

## 1. Search Architecture (Data Flow)

```
CLIENT QUERY
    |
    v
┌─────────────────────────────────────┐
│ Query Parser & Tokenizer            │
│ "find data analysis skills"         │
└─────────────┬───────────────────────┘
              |
         ┌────┴─────────────────────┐
         |                          |
         v                          v
    ┌────────────┐        ┌─────────────────┐
    │ Keyword    │        │ Semantic        │
    │ Search     │        │ Search          │
    │            │        │                 │
    │ tsvector   │        │ Vector DB       │
    │ @@ query   │        │ cosine_sim(q,d) │
    └────────────┘        └─────────────────┘
         |                          |
         | [skill_ids +            | [skill_ids +
         |  ts_rank score]         |  similarity score]
         |                          |
         └────────────┬─────────────┘
                      |
                      v
        ┌─────────────────────────────┐
        │ Combine Scores              │
        │ score = 0.3*kw +            │
        │         0.5*sem +           │
        │         0.2*popularity      │
        └─────────────┬───────────────┘
                      |
                      v
        ┌─────────────────────────────┐
        │ Filter by Category + Tags    │
        │ WHERE category = ? AND       │
        │       tag IN (...)           │
        └─────────────┬───────────────┘
                      |
                      v
        ┌─────────────────────────────┐
        │ Rank & Return Top-K         │
        │ ORDER BY score DESC         │
        │ LIMIT 20                    │
        └─────────────────────────────┘
```

---

## 2. Version Resolution Flow

```
┌──────────────────────┐
│ User Request         │
│ skill@^1.0.0         │
└──────────┬───────────┘
           |
           v
┌──────────────────────────────────┐
│ Parse Constraint                 │
│ Extract: operator (^), base (1.0.0)
└──────────┬───────────────────────┘
           |
           v
┌──────────────────────────────────┐
│ Query Available Versions         │
│ SELECT version FROM versions     │
│ WHERE skill_id = ? AND ...       │
└──────────┬───────────────────────┘
           |
           v
┌──────────────────────────────────┐
│ Filter by Operator Rules         │
│ ^1.0.0 → >=1.0.0 AND <2.0.0     │
│ ~1.0.0 → >=1.0.0 AND <1.1.0     │
└──────────┬───────────────────────┘
           |
           v
┌──────────────────────────────────┐
│ Exclude Prerelease (unless asked)│
│ WHERE is_prerelease = false      │
└──────────┬───────────────────────┘
           |
           v
┌──────────────────────────────────┐
│ Check for Matches                │
│                                  │
│ ✓ Found: Return max version      │
│ ✗ None: Raise VersionError       │
└──────────────────────────────────┘
```

---

## 3. Publish Flow with Indexing

```
┌──────────────────────┐
│ User Publishes       │
│ skill.yaml           │
└──────────┬───────────┘
           |
           v
┌──────────────────────────────────┐
│ Validate Manifest                │
│ Pydantic schema check            │
│ ✗ Error → 400 Bad Request        │
└──────────┬───────────────────────┘
           |
           v
┌──────────────────────────────────┐
│ Extract Metadata                 │
│ name, version, category, tags    │
│ description, requires            │
└──────────┬───────────────────────┘
           |
           v
┌──────────────────────────────────┐
│ INSERT/UPDATE DB                 │
│ - skills table                   │
│ - skill_versions table           │
│ - skill_tags table               │
└──────────┬───────────────────────┘
           |
           v
┌──────────────────────────────────┐
│ Rebuild Search Vector            │
│ tsvector = weighted concat(      │
│   name (A-weight) ||             │
│   description (B-weight) ||      │
│   tags (C-weight)                │
│ )                                │
└──────────┬───────────────────────┘
           |
           v
┌──────────────────────────────────┐
│ Index in Vector DB               │
│ embed(description) → ChromaDB    │
│ store with metadata              │
└──────────┬───────────────────────┘
           |
           v
┌──────────────────────────────────┐
│ Return Response                  │
│ 201 Created                      │
│ { id, version_id, url }          │
└──────────────────────────────────┘
```

---

## 4. Schema Dependency Graph

```
┌─────────────────────────────┐
│ skills (catalog)            │
│ ─────────────────────────   │
│ id (PK)                     │
│ name (UNIQUE)               │
│ category                    │
│ description                 │
│ search_vector (tsvector)    │
│ updated_at                  │
└──────────┬──────────────────┘
           |
      ┌────┴────────────────┐
      |                     |
      v                     v
┌─────────────────┐  ┌──────────────────┐
│skill_versions   │  │ skill_tags       │
│─────────────────│  │──────────────────│
│id (PK)          │  │skill_id (FK)     │
│skill_id (FK)    │  │tag_name          │
│version_semver   │  │PK: (skill_id,tag)
│is_prerelease    │  └──────────────────┘
│requires (JSON)  │
│published_at     │
└─────────────────┘

Indexes:
- skills.search_vector (GIN) for full-text
- skill_versions(skill_id, version_semver)
- skill_tags(skill_id, tag_name)
```

---

## 5. Category + Tag Discovery Model

```
PRIMARY CATEGORIES (Hierarchical)
┌──────────────────┐
│ Data Processing  │ → {csv, etl, transform}
│ Analytics        │ → {statistics, reporting}
│ AI/ML            │ → {nlp, vision, training}
│ Code Generation  │ → {templates, scaffolding}
│ DevOps           │ → {deployment, ci-cd}
│ Security         │ → {encryption, auth}
│ Testing          │ → {unit, integration, e2e}
│ Documentation    │ → {generation, publishing}
└──────────────────┘

FLAT TAGS (Unlimited)
Language: {python, javascript, go, java, rust}
Domain: {nlp, vision, timeseries, graphs, llm}
Complexity: {beginner, intermediate, advanced}
Status: {stable, experimental, deprecated}
Integration: {slack, github, aws, docker}
Maturity: {alpha, beta, stable, unmaintained}
```

**Discovery Path:**
```
1. User selects category: "Data Processing"
2. System shows available tags: csv, etl, transform, ...
3. User filters by tags: "transform" AND "advanced"
4. Query: WHERE category='Data Processing' AND tag_name IN ('transform', 'advanced')
5. Results ranked by keyword + semantic score
```

---

## 6. Prerelease Lifecycle

```
Development Branch (0.x.x)
    |
    v
Alpha Release (1.0.0-alpha.1, alpha.2, ...)
    │  [For developers & early adopters]
    │  [Frequent breaking changes expected]
    |
    v
Beta Release (1.0.0-beta.1, beta.2, ...)
    │  [For community testing]
    │  [API mostly stable; fixes & refinements]
    |
    v
Release Candidate (1.0.0-rc.1, rc.2, ...)
    │  [Final stability checks]
    │  [Only critical bug fixes]
    |
    v
Production Release (1.0.0)
    │  [Stable; semantic versioning rules apply]
    │  [Backward compatibility guaranteed until next major]
    |
    v
Maintenance (1.1.0, 1.2.0, 2.0.0, ...)
    │  [Patches, minor features, major rewrites]
    |
    v
Deprecated/EOL (yanked)
    │  [Security issues; marked as unsafe]
    │  [Warn users; recommend upgrade path]
    |
    v
Removed

Semantics:
- 0.y.z: Any breaking change OK (development)
- x.0.0-alpha: Breaking changes OK (pre-alpha testing)
- x.0.0-beta: API mostly stable (community testing)
- x.0.0-rc: Only critical fixes (release checks)
- x.y.z: No breaking changes (production)
- Yanked: Mark unsafe; users must manually update
```

---

## 7. Dependency Resolution (DAG)

```
INPUT: skill-A@^1.0.0
        ├─ skill-B@~2.1.0
        └─ skill-C@>=1.0.0 <2.0.0

AVAILABLE VERSIONS:
skill-A: [1.0.0, 1.1.0, 1.2.0, 2.0.0]
skill-B: [2.1.0, 2.2.0, 3.0.0]
skill-C: [1.0.0, 1.5.0, 2.0.0]

RESOLUTION STEPS:
1. A@^1.0.0 → select A@1.2.0 (max in range)
2. A@1.2.0 requires B@~2.1.0
3. B@~2.1.0 → select B@2.2.0
4. B@2.2.0 requires C@>=1.0.0 <2.0.0
5. C@>=1.0.0 <2.0.0 → select C@1.5.0
6. Check for circular deps: A→B→C (OK)
7. Return: {A@1.2.0, B@2.2.0, C@1.5.0}

CONFLICTS:
If A@1.2.0 requires D@^3.0.0 AND
   B@2.2.0 requires D@^2.0.0
   → No intersection in D versions
   → Return error: "Conflicting versions of D"
```

---

## 8. API Layer Stack

```
┌─────────────────────────────────────────┐
│ FastAPI Application                     │
└──────────┬────────────────────────────────┘
           |
     ┌─────┴──────────────┐
     |                    |
┌────▼──────┐   ┌────────▼────────┐
│ Routes    │   │ Middleware      │
│           │   │                 │
│ POST /pub-│   │ - Auth         │
│   lish    │   │ - Logging      │
│ GET /sear-│   │ - Error handle │
│   ch      │   │ - CORS         │
│ GET /reso-│   │                 │
│   lve     │   └────────────────┘
└────┬──────┘
     |
┌────▼─────────────────────────────┐
│ Service Layer                     │
│ - SkillRepository                │
│ - VersionResolver                │
│ - SearchEngine                   │
│ - Indexer                        │
└────┬─────────────────────────────┘
     |
     ├──────────────┬──────────────┐
     |              |              |
┌────▼────┐    ┌────▼────┐    ┌───▼──────┐
│PostgreSQL│    │ChromaDB │    │ File I/O │
│ Registry │    │Embeddings   │(manifest)│
└──────────┘    └─────────┘    └──────────┘
```

---

## Comparison Matrix: npm vs HF vs GitHub

| Aspect | npm | Hugging Face | GitHub |
|--------|-----|--------------|--------|
| **Versioning** | SemVer (required) | Git tags (flexible) | Git tags (flexible) |
| **Manifest** | package.json | model card (YAML) | action.yml (YAML) |
| **Repository** | Centralized registry | Distributed (git repos) | Distributed (git repos) |
| **Search** | Keywords | Task/language/tags | Action marketplace |
| **Metadata** | JSON | Markdown + YAML | YAML |
| **Dependency Mgmt** | Lock files (package-lock.json) | Git refs | Not applicable |
| **Registry Access** | API only | API + Git | API only |
| **Scale** | Millions (JavaScript) | Hundreds of thousands | Millions (Actions) |

**Recommendation for Skill Registry:**
- Adopt npm's SemVer + constraint resolution (battle-tested)
- Adopt HF's metadata richness + documentation support
- Adopt GitHub's manifest simplicity + git integration
- Add semantic search (none of the three do this well)
- Use PostgreSQL for scale + search capabilities (npm uses Node, HF uses Hugging Face infra, GitHub uses GitHub infra)

---

*Architecture patterns synthesized from production systems. Use as reference during implementation.*
