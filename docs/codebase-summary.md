# Codebase Summary

*Generated from `repomix` compaction (see `repomix-output.xml`).*
*Snapshot captured after the Java MCP server implementation.*

## Overview
- **Tech stack:** Java 21, Quarkus, Jakarta REST annotations, PostgreSQL (with vector extension), and small HTTP clients for embedding providers.
- **Services:** Core features are implemented inside `com.skillgateway.*` packages covering API, business services, repositories, versioning helpers, and MCP tool handlers.
- **AI integrations:** Embedding calls are delegated to `EmbeddingService` which posts to `ai.embedding.url`/`model`; search weights and limits are externally configurable via `search.*` properties.

## Key Modules
| Layer | Description | Representative Files |
| --- | --- | --- |
| API | Exposes REST endpoints for publish/search/versioning/dependencies. | `SkillResource.java`, `GlobalExceptionMapper.java`, `ApiKeyFilter.java` |
| Services | Implements business logic: publishing, searching (keyword + semantic + popularity fusion), version resolution, embedding, dependency resolution. | `SkillService.java`, `SearchService.java`, `VersionService.java`, `EmbeddingService.java`, `DependencyResolver.java` |
| Persistence | Panache repositories backed by PostgreSQL; skill embeddings stored as `vector`, search index maintained via migrations. | `SkillRepository.java`, `SkillVersionRepository.java`, migration scripts under `resources/db/migration/` |
| DTOs & Models | Plain Java records/classes representing manifests, search payloads, responses, and version graphs. | `model/dto/*.java`, `model/Skill.java`, `model/SkillVersion.java` |
| MCP Tools | Helper handlers surface search, version and skill metadata to the MCP agent surface. | `mcp/tools/SkillToolHandler.java`, `SearchToolHandler.java`, `VersionToolHandler.java` |

## Data & Persistence
- PostgreSQL schema created/updated through `V1__create_skills_schema.sql`, `V2__create_search_indexes.sql`, `V3__create_search_vector_trigger.sql`.
- Embeddings stored as Postgres `vector` and semantic search uses `<=>` operator with `embedding IS NOT NULL` filtering.
- Full-text search enabled via `search_vector` materialized fields (`ts_rank`, `plainto_tsquery`).
- Version metadata persisted per skill with `latest`, `yanked`, timestamps, and JSON-serialized dependency lists.

## Configuration & Runtime
- Runtime values sourced from `application.properties` and overridden through Quarkus config mappings such as `search.weight.*`, `search.default-limit`, `ai.embedding.*`, and `search.*` via `AppConfig`.
- API key enforcement lives in `ApiKeyFilter` and globally mapped errors emitted via `GlobalExceptionMapper`.

## Build & Dependencies
- Maven handles build lifecycle (`pom.xml`).
- Documentation and design artifacts live in `/docs/`; future plans under `/plans/`.
- Repomix already packed the code (`repomix-output.xml`) for fast AI-assisted reviews.

## Developer Notes
1. **Publish workflow:** `SkillResource.publish()` validates, persists or updates `Skill`, persists new `SkillVersion`, and re-calculates embeddings (with graceful degradation).
2. **Search fusion:** `SearchService` blends keyword, semantic, and popularity signals with normalizing helpers referencing `ScoredSkill`.
3. **Version resolution:** `VersionService` relies on `SemVerParser`, `SemVerConstraint`, `SemVer` utilities for constraint evaluation and release listing.
4. **Exception handling:** Domain exceptions (`ConflictException`, `NotFoundException`, `ValidationException`) bubble through mapper to uniform HTTP responses.

## Testing Status
- `mvn test` (Quarkus + Maven Surefire Plugin) executes JUnit 5 suites located in `src/test/java`, ensuring compatibility with Java 21 and project configuration.
- Key unit tests currently cover version parsing/constraints (`SemVerParserTest`, `SemVerConstraintTest`) and manifest validation (`ManifestValidatorTest`), verifying deterministic ordering, constraint logic, and rejection of malformed manifests.
- Additional service-level tests can be added under Quarkus test profiles when mockable repositories or embedded Postgres capabilities are introduced.

*Last updated: 2026-02-22.*
