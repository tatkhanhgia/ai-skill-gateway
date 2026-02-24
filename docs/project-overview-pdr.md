# Project Overview & Product Development Requirements

**Project:** Java MCP Skill Gateway Server
**Version:** 1.0
**Status:** Active
**Last Updated:** 2026-02-22

## Purpose & Scope
Deliver a self-hosted Java-based MCP skill gateway that exposes RESTful skill catalog management, search, version resolution, and dependency graph services for MCP agents and external clients. The implementation replaces the initial FastAPI prototype with a Quarkus-based server, emphasizes vector search fusion, and keeps AI-related computation local by invoking configurable embedding providers.

### Scope
- Skill publish/list/search endpoints + version metadata management
- Weighted hybrid search combining keyword, semantic, and popularity signals
- Version constraint resolution and dependency tree exploration
- API key gating plus centralized exception handling
- Embedding service integration and PostgreSQL vector support

## Functional Requirements
1. **Publish Skill** – Accept manifests (`SkillManifest`) via `POST /api/v1/skills/publish`, validate payloads (`ManifestValidator`), persist `Skill` + `SkillVersion`, refresh embeddings, and return `PublishResponse`. *Acceptance:* Saves new versions atomically, rejects duplicate version names, and regenerates PostgreSQL vector when embedding endpoint is reachable.
2. **Retrieve Skill Detail** – `GET /api/v1/skills/{name}` returns `SkillDetail` including latest version metadata or 404 via `NotFoundException`. *Acceptance:* Non-existent names return JSON error from `GlobalExceptionMapper` and existing skills include sorted tags.
3. **List Skills with Filters** – `GET /api/v1/skills` accepts `category`, `tags`, pagination, and returns `SkillSummary`. *Acceptance:* Tags split by comma, pagination defaults to page=0/size=20, and invalid page/size sanitized.
4. **Search Skills** – `GET /api/v1/skills/search` merges keyword search (`SkillRepository.keywordSearch`), semantic search (`EmbeddingService` + `vectorSearch`), and popularity normalization (`SearchService`). *Acceptance:* Respect `search.default-limit`/`max-limit`, skip failed embeddings gracefully, and sort by fused score.
5. **Version Management** – Provide `GET /api/v1/skills/{name}/versions`, `GET /resolve`, `POST /versions/{version}/yank`, and dependency tree endpoint. *Acceptance:* Versions sorted by semantic ordering, yanked flag toggled with reason, dependency resolved via `DependencyResolver`.
6. **Security** – Every request must pass `ApiKeyFilter`. *Acceptance:* Requests without valid `X-Api-Key` return 401 before service layer or mapper responses.

## Non-functional Requirements
- **Reliability:** >=99% uptime for REST endpoints, DB migrations protected by Quarkus-managed transactions.
- **Performance:** Search requests must finish within 300ms for cached keyword results and 500ms when semantic embeddings succeed.
- **Scalability:** Designed to run on single node with vector-enabled PostgreSQL, but modularized services allow future horizontal scaling.
- **Maintainability:** Business logic split into services/repositories, supporting future unit tests with Mockito or Panache mocks.
- **Observability:** Exceptions funnel through `GlobalExceptionMapper`; logs should include skill/category names and reason codes.

## Technical Constraints & Dependencies
- **Runtime stack:** Java 21 + Quarkus (Jakarta REST annotations) with Panache/h2-style JPA patterns.
- **Database:** PostgreSQL with `vector` column for embeddings (`skills.embedding`), `search_vector` column for full-text search, and migrations (`V1`, `V2`, `V3`).
- **AI integration:** `EmbeddingService` calls `ai.embedding.url` and `model` from config, and `ConfigProperty` controls search weights and limits (`search.weight.*`, `search.default-limit`, `search.max-limit`). External embedding failure must not abort publish/search.
- **Security:** API key enforced via `ApiKeyFilter`; domain exceptions mapped by `GlobalExceptionMapper`.
- **Dependency resolution:** `SemVer`, `SemVerParser`, `SemVerConstraint` plus `DependencyResolver` and `CircularDependencyException` ensure consistent version graphs.

## Implementation Guidance & Architectural Decisions
- **Layering:** Keep controller (`SkillResource`) thin, let service layer handle validation, embedding, scoring, and repository delegation.
- **Search Fusion:** Normalize keyword scores and popularity before weighting; store combined scores temporarily inside `SearchScore` helper.
- **Transaction boundaries:** `@Transactional` on publish/yank ensures atomic writes.
- **Embeddings:** Guard network calls with try/catch; degrade to null vector if provider fails (per current implementation). Keep request body as JSON prompt.
- **Exceptions:** Throw domain-specific exceptions (`ConflictException`, `ValidationException`) and rely on `GlobalExceptionMapper` to emit structured HTTP responses.

## Success Metrics & Acceptance Criteria
| Metric | Target | Verification |
| --- | --- | --- |
| API key enforcement coverage | 100% of protected endpoints | Security tests and manual curl with/without key |
| Search latency | <500ms for successful semantic path | Benchmark search endpoint with sample text |
| Functional test coverage | >=80% for service layer | Unit tests for SkillService, SearchService, VersionService |
| Documentation synchronization | Docs updated for architecture, standards, PDR | Repo review and doc validation script |

## Configuration Reference

### Required Environment Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `AUTH_API_KEY` | API key for protected endpoints | - |
| `DB_USERNAME` | PostgreSQL username | admin |
| `DB_PASSWORD` | PostgreSQL password | changeme |
| `DB_JDBC_URL` | JDBC connection URL | jdbc:postgresql://localhost:5432/skillgateway |

### Optional Configuration
| Property | Description | Default |
|----------|-------------|---------|
| `AI_EMBEDDING_URL` | Embedding service endpoint | http://localhost:11434/api/embeddings |
| `AI_EMBEDDING_MODEL` | Embedding model name | nomic-embed-text |
| `search.weight.keyword` | Keyword search weight | 0.3 |
| `search.weight.semantic` | Semantic search weight | 0.5 |
| `search.weight.popularity` | Popularity weight | 0.2 |
| `search.default-limit` | Default result limit | 20 |
| `search.max-limit` | Maximum result limit | 50 |

## Testing Status (Java 21)
- `mvn test` (Quarkus + Maven Surefire 3.5.2) executes JUnit 5 suites in `src/test/java` and proves compatibility with Java 21.
- Core unit tests include `SemVerParserTest`, `SemVerConstraintTest`, and `ManifestValidatorTest`, verifying semantic version parsing rules, constraint matching, and manifest validation without hitting persistence layers.
- Service-level regression suites can be expanded later under Quarkus test profiles or with embedded PostgreSQL; document updates will capture any new coverage.

## Risks & Mitigations
- **Embedding provider outages** – Already handled by try/catch; add retry/backoff if SLO demands.
- **Semantic search drift** – Weight adjustments tracked via `search.weight.*`; default limit ensures controlled result sets.
- **Circular dependencies** – `DependencyResolver` throws `CircularDependencyException`; include integration test covering loops.

## API Reference

### Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/skills/publish` | API Key | Publish new skill/version |
| GET | `/api/v1/skills/{name}` | - | Get skill detail |
| GET | `/api/v1/skills` | - | List skills (filter by category/tags) |
| POST | `/api/v1/skills/{name}/versions/{version}/yank` | API Key | Yank a version |
| GET | `/api/v1/skills/search` | - | Hybrid search |
| GET | `/api/v1/skills/{name}/versions` | - | List versions |
| GET | `/api/v1/skills/{name}/resolve` | - | Resolve version constraint |
| GET | `/api/v1/skills/{name}/dependencies/{version}` | - | Get dependency tree |

### MCP Tools

| Tool | Description |
|------|-------------|
| `publishSkill` | Publish via MCP |
| `getSkill` | Get skill details |
| `listSkills` | List with filters |
| `searchSkills` | Hybrid search |
| `resolveVersion` | Resolve constraint |
| `listVersions` | List versions |
| `yankVersion` | Yank version |

## Version History
- **1.0 (2026-02-22):** Initial Java MCP server PDR published alongside docs.
- **1.1 (2026-02-24):** Added API reference, MCP tools documentation
