# AI Skill Gateway - Architecture

## Overview

The current implementation is a Java 17 Quarkus application that replaces the earlier FastAPI prototype. The server exposes a RESTful skill gateway, orchestrates search/embedding pipelines, and persists data in PostgreSQL with vector support. All configuration - search weights, embedding endpoint, API key, limits - lives in `application.properties` and is consumed through Quarkus `@ConfigProperty` and `@ConfigMapping` beans.

## Layered Composition

### API Layer

- **SkillResource** (`/api/v1/skills`) implements publish, list, detail, search, version resolution, dependency, and yank workflows. Endpoints map cleanly to DTOs such as `SkillManifest`, `SkillSummary`, `SearchRequest`, and `VersionResolution`.
- **EmbeddingStatusResource** (`/api/v1/embedding/status`) reports sanitized embedding provider configuration and validity.
- **ApiKeyFilter** enforces `X-Api-Key` for mutating endpoints. Public read endpoints remain available for catalog discovery.
- **GlobalExceptionMapper** normalizes `NotFoundException`, `ConflictException`, `ValidationException`, and other domain errors into consistent JSON responses.

### Service Layer

- **SkillService** drives publish and yank flows, validating manifests (`ManifestValidator`), refreshing embeddings via `EmbeddingService`, and serializing the `requires` list for storage.
- **SearchService** fuses keyword matches (`SkillRepository.keywordSearch`), semantic vectors (`EmbeddingService` + `vectorSearch`), and download popularity metrics into a single score using configurable weights (`search.weight.*`). Results respect pagination thresholds (`search.default-limit`, `search.max-limit`).
- **VersionService** sorts semantic versions using `SemVerParser`, applies `SemVerConstraint`, and publishes resolution history. It powers `/versions`, `/resolve`, and ensures only non-yanked releases are returned.
- **DependencyResolver** builds dependency trees and guards against `CircularDependencyException`.

### Persistence Layer

- **SkillRepository** and **SkillVersionRepository** extend Panache, providing convenience methods for find-by-name, keyword search, vector search, and latest-version bookkeeping.
- Embeddings and search vectors live in PostgreSQL columns (`vector`, `tsvector`). Migrations `V1__create_skills_schema.sql`, `V2__create_search_indexes.sql`, `V3__create_search_vector_trigger.sql` keep indexes in sync.
- `SkillVersion` tracks `latest`, `yanked`, release notes, and dependency payloads (JSON array) so the service layer can make informed decisions.

### Data & AI Integration

- **EmbeddingService** selects `ollama` or `openai-compatible` with `ai.embedding.provider`, converts provider responses to `float[]`, validates the configured dimension, and renders Postgres-compatible literals with `asPgVectorLiteral`. All provider failures degrade gracefully (publish/search continue if embedding endpoint is unreachable).
- **Search pipeline**: keyword rows return `ScoredSkill` entries with `ts_rank`. Semantic search clones the query embedding and requests the vector similarity operator `<=>`. SearchService normalizes scores, merges per-id data via `SearchScore`, then applies weights for ranking.

### Version & Dependency Flow

1. Publish sets the new version as `latest`, clears previous latest flags, and persists dependency requirements.
2. `VersionService.resolve()` parses constraints, filters out yanked entries, sorts via `SemVer`, and exposes all matches for auditing.
3. `/dependencies/{version}` routes to `DependencyResolver`, which traverses linked requirements while avoiding cycles.

### Security & Observability

- API key policy protects mutating endpoints before they reach the service layer; exception mapper normalizes invalid inputs.
- Domain exceptions contain human-readable messages that flow through `GlobalExceptionMapper` into structured HTTP errors.
- Logging and monitoring can hook into Quarkus health endpoints (`/q/health`), embedding status (`/api/v1/embedding/status`), and standard logs emitted by services.

### Deployment & Operations

- Quarkus handles database migrations (`quarkus.flyway.migrate-at-start=true`), preventing schema drift.
- `AppConfig` maps `search.*` settings to typed beans, centralizing configuration for weights and limits.
- Embedding and search services avoid blocking the API when external AI endpoints fail.
- PostgreSQL `vector` support requires an extension; the SQL migrations prepare the columns and triggers needed for efficient search.
- MCP HTTP tooling is deferred until Quarkus MCP dependency compatibility is restored. Current runtime scope is REST API plus npm package distribution.

*See `docs/project-overview-pdr.md` for requirements coverage and `docs/codebase-summary.md` for module references.*
