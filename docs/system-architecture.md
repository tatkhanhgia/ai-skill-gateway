# System Architecture - Java MCP Skill Gateway

**Scope:** Documents the layered architecture implemented in the Java 21 + Quarkus MCP skill gateway server.
**Last Updated:** 2026-02-22

---

## High-Level Overview
- **Runtime:** Java 21, Quarkus 3.20.2, Maven-managed build with Quarkus MCP server HTTP extensions.
- **Purpose:** Expose `/api/v1/skills` endpoints that support skill publication, listing, search, version resolution, dependency analysis, and yanking; serve MCP agents and external clients with secure, observable interfaces.
- **Data stores:** PostgreSQL with `vector` and `tsvector` columns for embeddings/search plus migrations `V1__create_skills_schema.sql` through `V3__create_search_vector_trigger.sql` keep schema and indexes aligned.
- **AI integration:** `EmbeddingService` posts to configurable `ai.embedding.url/model`, transforms responses into Postgres `vector` literals, and gracefully degrades when the provider is unreachable.

## API Layer
- **SkillResource** exposes REST endpoints (`publish`, `list`, `get`, `search`, `versions`, `resolve`, `yank`, `dependencies`).
- **DTOs:** `SkillManifest`, `SkillSummary`, `SkillDetail`, `PublishResponse`, `SearchRequest`, `SearchResult`, `VersionInfo`, `VersionResolution` ensure typed payloads.
- **Filters & Exception Mapping:** `ApiKeyFilter` enforces `X-API-Key` before controllers execute; `GlobalExceptionMapper` maps domain errors (`ConflictException`, `NotFoundException`, `ValidationException`) to consistent JSON responses with proper status codes.

## Service Layer
- **SkillService** handles validation (`ManifestValidator`), persistence of `Skill` + `SkillVersion`, embedding refresh, and yank operations within transactional boundaries.
- **SearchService** merges keyword (`SkillRepository.keywordSearch`), semantic (`EmbeddingService` + `SkillRepository.vectorSearch`), and popularity signals. Scores are normalized (`SearchScore.normalize`) and combined via weights configured in `search.weight.*` before sorting and trimming to `limit` (`search.default-limit`, `search.max-limit`).
- **VersionService** relies on `SemVerParser`, `SemVerConstraint`, and `SemVer` helpers to list and resolve versions; toggles `latest`/`yanked` flags as needed.
- **DependencyResolver** builds dependency graphs and guards against `CircularDependencyException` to prevent infinite traversal.

## Persistence Layer
- **Repositories:** `SkillRepository` and `SkillVersionRepository` extend Panache and expose helpers for keyword search, vector search, tag retrieval (`findTagsBySkillIds`), and latest-version bookkeeping.
- **Embeddings & Search Vectors:** PostgreSQL `vector` columns store embeddings, while `tsvector` and triggers keep full-text search data synchronized; `SkillVersion` persists metadata such as `releaseNotes`, `latest`, `yanked`, and dependency payloads (`JSON`).

## Data & AI Flow
1. **Publish:** `SkillService.publish()` validates the manifest, persists versions, refreshes embeddings via `EmbeddingService`, and persists vectors/metadata atomically.
2. **Search:** `SearchService` executes two repository queries (keyword + vector), normalizes scores, merges per-skill tags, respects filters (category/tags), and returns top-ranked `SearchResult` entries.
3. **Version resolution:** Queries route through `VersionService.resolve()` which parses constraints (e.g., `^1.2.3`, `~1.2.3`, ranges) and returns matching non-yanked versions.
4. **Dependency inspection:** `/dependencies/{version}` uses `DependencyResolver` to expand requirements while preventing cycles.

## Configuration & Runtime
- **Properties:** `AppConfig` and `@ConfigProperty` fields centralize settings (`search.weight.*`, `search.default-limit`, `search.max-limit`, `ai.embedding.url/model`, pagination defaults).
- **Resiliency:** Embedding failures are caught in `SearchService` and `SkillService`, allowing API responses even when external AI endpoints miss.
- **Security:** `ApiKeyFilter` rejects unauthorized calls with HTTP 401 before they reach services; domain errors are normalized afterward.

## Observability & Operations
- **Logging:** Domain exceptions should include contextual identifiers (skill name, version) when logging is added.
- **Health:** Quarkus health endpoints (`/q/health`) surface readiness; `quarkus.flyway.migrate-at-start=true` keeps the database schema up to date during startup.
- **Deployment:** Standard JVM invocation (`mvn quarkus:dev`) for local development; `mvn package` or `mvn -Pnative package` for production/native builds defined in `pom.xml`.

## Testing & Verify
- **Java 21 compliance:** Project compiles and runs under Java 21 (`<maven.compiler.release>21</maven.compiler.release>`). Maven Surefire 3.5.2 executes JUnit 5 suites that include `SemVerParserTest`, `SemVerConstraintTest`, and `ManifestValidatorTest`.
- **Validation path:** Manifest validation, semantic version parsing, and constraint matching remain covered by deterministic unit tests until service-level tests are added (mock repositories or embedded Postgres).

## MCP Integration Flow

```text
┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ MCP Client  │────▶│  MCP Server     │────▶│  Tool Handlers  │
│  (Claude)   │◄────│  (HTTP/SSE)     │◄────│  (Quarkus)      │
└─────────────┘     └─────────────────┘     └────────┬────────┘
                                                     │
                         ┌───────────────────────────┼──────────┐
                         ▼                           ▼          ▼
                  ┌─────────────┐            ┌────────────┐ ┌──────────┐
                  │SkillToolHandler│         │SearchToolHandler│VersionToolHandler│
                  └─────────────┘            └────────────┘ └──────────┘
```

### Tool Registration
- Tools auto-discovered via `@Tool` annotation
- Quarkus MCP server exposes via `/mcp` endpoint
- JSON-RPC 2.0 message format

### Tool Categories
1. **Skill Management** - publishSkill, getSkill, listSkills, yankVersion
2. **Search** - searchSkills
3. **Versioning** - resolveVersion, listVersions

## Data Flow Examples

### Publish Flow
```text
Client → POST /api/v1/skills/publish
  → ApiKeyFilter (auth check)
  → SkillResource.publish()
  → SkillService.publish()
    → ManifestValidator.validate()
    → SkillRepository.persist() / merge()
    → SkillVersionRepository.persist()
    → EmbeddingService.embed() (network call; degrades gracefully on failure)
    → SkillRepository.updateEmbedding()
  ← PublishResponse
```

### Search Flow
```text
Client → GET /api/v1/skills/search?query=...
  → SkillResource.search()
  → SearchService.search()
    → SkillRepository.keywordSearch() → List<ScoredSkill>
    → EmbeddingService.embed() → float[]
    → SkillRepository.vectorSearch() → List<ScoredSkill>
    → Merge & normalize scores
    → Apply weights (keyword/semantic/popularity)
    → Sort & limit
  ← List<SearchResult>
```

### Version Resolution Flow
```text
Client → GET /api/v1/skills/{name}/resolve?constraint=^1.0.0
  → SkillResource.resolve()
  → VersionService.resolve()
    → SemVerParser.parseConstraint()
    → SkillVersionRepository.listBySkill()
    → Filter non-yanked, sort by SemVer
    → Find best match
  ← VersionResolution
```

## Deployment Architecture

### Local Development
```text
┌─────────────────────────────────────────┐
│           Docker Compose                │
│  ┌─────────┐    ┌─────────────────┐    │
│  │   DB    │◄───│  Quarkus (dev)  │    │
│  │PostgreSQL│    │   Port 8080     │    │
│  │+pgvector│    └─────────────────┘    │
│  └─────────┘                             │
└─────────────────────────────────────────┘
```

### Production Considerations
- External PostgreSQL with pgvector extension
- Embedding service (Ollama or external API)
- Load balancer in front of Quarkus instances
- Health checks at `/q/health`

## Maintenance Notes
- **Documentation sync:** `repomix` compaction regenerates `repomix-output.xml`; `docs/codebase-summary.md` reflects module changes after each run.
- **Doc validation:** Run `node .claude/scripts/validate-docs.cjs docs/` after edits to ensure formatting and link hygiene.
- **File size:** Keep documentation under 800 LOC per file; split topics into subdirectories if nearing the limit.
