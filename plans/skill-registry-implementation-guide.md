# Skill Registry Implementation Guide

**Companion to:** `skill-repository-design-research.md`
**Purpose:** Translate research into concrete code patterns & API contracts

---

## Skill Definition Format (skill.yaml)

```yaml
# Minimal required fields
name: skill-data-analyzer
version: 1.0.0
description: Analyzes datasets for patterns and anomalies

# Optional but recommended
metadata:
  author: "team-data"
  license: "MIT"
  category: "Data Processing"
  tags:
    - data-analysis
    - statistics
    - experimental

# Dependencies (other skills or external packages)
requires:
  - skill: vector-embedding@^1.0.0
  - skill: text-parser@~2.1.0
  - python: ">=3.11"
  - external: ollama

# Discovery metadata
capabilities:
  input: [csv, json, text]
  output: [json, structured-report]
  domains: [analytics, statistics]

# Repository & versioning
repository:
  type: git
  url: "https://github.com/org/skill-repo"
  ref: main
  path: ./skills/data-analyzer

documentation:
  readme: "./README.md"
  examples: "./examples"
  schema: "./docs/io-schema.json"
```

---

## PostgreSQL Implementation Patterns

### Insert New Skill with Version
```python
def publish_skill(db, manifest: dict) -> dict:
    """Publish skill to registry; auto-update search vector."""
    with db.transaction():
        # Insert or update skill metadata
        skill = db.execute("""
            INSERT INTO skills (name, category, description, repository_url)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT(name) DO UPDATE SET
                updated_at = NOW(),
                description = EXCLUDED.description
            RETURNING id
        """, (manifest['name'], manifest['category'], manifest['description'], manifest['repository']['url']))
        skill_id = skill[0]['id']

        # Insert version record
        version = db.execute("""
            INSERT INTO skill_versions (skill_id, version_semver, is_prerelease, requires)
            VALUES (%s, %s, %s, %s)
            RETURNING id
        """, (skill_id, manifest['version'], is_prerelease_version(manifest['version']),
              json.dumps(manifest.get('requires', []))))

        # Insert tags
        for tag in manifest.get('tags', []):
            db.execute("INSERT INTO skill_tags (skill_id, tag_name) VALUES (%s, %s)",
                      (skill_id, tag))

        # Update full-text search vector
        db.execute("""
            UPDATE skills SET search_vector =
                setweight(to_tsvector('english', name), 'A') ||
                setweight(to_tsvector('english', description), 'B') ||
                (SELECT setweight(to_tsvector('english', array_to_string(array_agg(tag_name), ' ')), 'C')
                 FROM skill_tags WHERE skill_id = %s)
            WHERE id = %s
        """, (skill_id, skill_id))

        return {'id': skill_id, 'version_id': version[0]['id']}
```

### Search with Ranking
```python
def search_skills(db, query: str, category: str = None, limit: int = 20) -> list:
    """Hybrid search: keyword + tag filter; rank by relevance."""
    conditions = []
    params = []

    # Full-text search
    search_query = db.execute("""
        SELECT id FROM skills
        WHERE search_vector @@ plainto_tsquery('english', %s)
    """, (query,))
    search_ids = [r['id'] for r in search_query]

    # Category filter
    if category:
        conditions.append("category = %s")
        params.append(category)

    # Rank results by relevance
    results = db.execute("""
        SELECT id, name, description, category,
               ts_rank(search_vector, plainto_tsquery('english', %s)) as relevance
        FROM skills
        WHERE id = ANY(%s) AND {conditions}
        ORDER BY relevance DESC
        LIMIT %s
    """.format(conditions=" AND ".join(conditions) if conditions else "TRUE"),
        [query, search_ids] + params + [limit])

    return [dict(r) for r in results]
```

### Version Resolution
```python
def resolve_version(db, skill_name: str, constraint: str) -> dict:
    """Parse constraint (^1.2.3, ~1.2.3, >=1.0.0), return best match."""
    versions = db.execute("""
        SELECT version_semver FROM skill_versions
        WHERE skill_id = (SELECT id FROM skills WHERE name = %s)
        ORDER BY version_semver DESC
    """, (skill_name,))

    semver_list = [parse_semver(v['version_semver']) for v in versions]
    matching = [v for v in semver_list if matches_constraint(v, constraint)]

    if not matching:
        raise VersionConstraintError(f"No version of {skill_name} matches {constraint}")

    return matching[0]  # Highest matching version

def matches_constraint(version, constraint):
    """Check if semver matches constraint (^1.2.3, ~1.2.3, >=X <Y)."""
    if constraint == "latest":
        return not version.prerelease
    if constraint.startswith("^"):
        base = parse_semver(constraint[1:])
        return version.major == base.major and version >= base
    if constraint.startswith("~"):
        base = parse_semver(constraint[1:])
        return version.major == base.major and version.minor == base.minor and version >= base
    # Handle >=X <Y ranges...
    return parse_semver(constraint) == version
```

---

## API Contract Examples

### GET /api/v1/skills/search
```json
Request:
{
  "query": "data analysis",
  "category": "Data Processing",
  "tags": ["statistics"],
  "limit": 10
}

Response: 200 OK
{
  "results": [
    {
      "id": "skill-123",
      "name": "skill-data-analyzer",
      "version": "1.0.0",
      "description": "Analyzes datasets...",
      "category": "Data Processing",
      "tags": ["data-analysis", "statistics"],
      "rating": 4.5,
      "downloads": 1250,
      "relevance_score": 0.92
    }
  ],
  "total": 45,
  "page": 1
}
```

### POST /api/v1/skills/publish
```json
Request:
{
  "manifest": {
    "name": "skill-data-analyzer",
    "version": "1.0.0",
    "description": "...",
    "category": "Data Processing",
    "tags": ["data-analysis"],
    "requires": ["python>=3.11"],
    "repository": {...}
  }
}

Response: 201 Created
{
  "id": "skill-123",
  "name": "skill-data-analyzer",
  "version": "1.0.0",
  "published_at": "2026-02-21T10:30:00Z",
  "url": "/api/v1/skills/skill-data-analyzer/versions/1.0.0"
}
```

### GET /api/v1/skills/{name}/resolve-version
```json
Request:
{
  "constraint": "^1.0.0"
}

Response: 200 OK
{
  "name": "skill-data-analyzer",
  "requested_constraint": "^1.0.0",
  "resolved_version": "1.2.3",
  "available_versions": ["1.2.3", "1.2.2", "1.2.1", "1.0.0"],
  "requires": ["python>=3.11"],
  "prerelease": false
}
```

---

## Code Organization (Modules)

```
src/
├── registry/
│   ├── __init__.py
│   ├── models.py              # Pydantic models (SkillManifest, Version, etc)
│   ├── db_schema.py           # SQLAlchemy tables + migrations
│   ├── repository.py          # CRUD ops: publish, search, resolve
│   ├── version_resolver.py    # SemVer parsing + constraint matching
│   ├── search_engine.py       # Full-text + semantic search
│   └── indexer.py             # Vector DB integration
├── api/
│   ├── routes.py              # FastAPI endpoints
│   └── dependencies.py        # Dependency injection
└── tests/
    ├── test_version_resolver.py
    ├── test_search.py
    └── test_api.py
```

---

## Testing Strategy

```python
# test_version_resolver.py
class TestVersionResolution:
    def test_caret_constraint(self):
        assert resolve("skill@^1.2.3", versions=["1.3.0", "1.2.3", "2.0.0"]) == "1.3.0"

    def test_tilde_constraint(self):
        assert resolve("skill@~1.2.3", versions=["1.2.5", "1.3.0", "2.0.0"]) == "1.2.5"

    def test_prerelease_excluded_by_default(self):
        assert resolve("skill@>=1.0.0", versions=["1.0.0", "1.0.1-alpha"]) == "1.0.0"

    def test_prerelease_explicit_opt_in(self):
        assert resolve("skill@1.0.1-alpha", versions=["1.0.0", "1.0.1-alpha"]) == "1.0.1-alpha"

# test_search.py
class TestSearchRanking:
    def test_keyword_match_in_name_ranks_higher(self):
        results = search("analyzer", skills=[
            {"name": "data-analyzer", "description": "..."},
            {"name": "processor", "description": "analyzer tool"}
        ])
        assert results[0]['name'] == "data-analyzer"  # name match > desc match
```

---

## Migration Path

### From File-based to DB-based Registry
1. Scan existing skill YAML files → parse manifests
2. INSERT into `skills`, `skill_versions`, `skill_tags` tables
3. Build full-text search vectors via trigger
4. Test search results match old file-based lookups
5. Redirect API calls from filesystem to PostgreSQL

### Backward Compatibility
- Accept both `skill.yaml` in repo and registry query
- Version resolution respects `skill-lock.json` from existing setups
- API returns results compatible with old consumers
