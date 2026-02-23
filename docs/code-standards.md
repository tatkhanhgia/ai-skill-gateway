# Code Standards & Engineering Guidelines

**Scope:** Aligns implementation, configuration, and documentation practices for the Java 21 / Quarkus MCP skill gateway.
**Last Updated:** 2026-02-22

---

## Repository Layout & Module Responsibilities
- **Entry point:** `src/main/java/com/skillgateway/api/SkillResource.java` exposes `/api/v1/skills` REST endpoints (publish, list, detail, search, versions, resolve, dependencies, yank).
- **Services:** `SkillService`, `SearchService`, `VersionService`, `EmbeddingService`, and `DependencyResolver` each encapsulate a single concern (domain validation, embedding, search fusion, semantic versioning, dependency traversal).
- **Persistence:** Panache repositories (`SkillRepository`, `SkillVersionRepository`) live under `repository/` and handle keyword, vector, and tag queries backed by PostgreSQL migrations in `resources/db/migration/V1..V3__*.sql`.
- **DTOs & Models:** `model/` and `model/dto/` define manifest, summary, version, and dependency payloads; maintain immutable data structures (records or final classes) to keep serialization predictable.

## API Design & DTO Conventions
- Prefer explicit HTTP methods: `POST /publish`, `GET /search`, `GET /{name}`, `POST /{name}/versions/{version}/yank`, `GET /{name}/resolve`, `GET /{name}/dependencies/{version}`.
- Each method accepts or returns a strongly typed DTO (e.g., `SkillManifest`, `SearchRequest`, `SearchResult`, `PublishResponse`, `VersionInfo`, `VersionResolution`).
- Query parameters (category, tags, pagination) are normalized in `SkillResource` before reaching services (trim, filter blank entries, apply defaults).
- Responses consistently use JSON encoded by Quarkus REST-Jackson; avoid exposing entity internals directly.

## Error Handling & Validation
- Validation logic lives in `ManifestValidator` and service guards; throw domain exceptions (`ValidationException`, `ConflictException`, `NotFoundException`) when invariants fail.
- `GlobalExceptionMapper` centralizes exception-to-HTTP translations so controllers remain lean and service errors return structured JSON payloads.
- Logging should accompany exception throws (not shown in current code) once integrated to support observability; always include contextual identifiers such as skill name or version when available.

## Configuration & Runtime Properties
- Configuration is centralized via `AppConfig` and Quarkus `@ConfigProperty` beans (e.g., `search.weight.keyword`, `search.default-limit`, `ai.embedding.*`).
- Default pagination and search limits are enforced (`search.default-limit`, `search.max-limit`) before repository calls.
- Embedding payloads rely on `ai.embedding.url/model`; resiliency is achieved by catching runtime failures and continuing without semantic scores.

## Security, Observability & Deployment
- Every request passes through `ApiKeyFilter`, which checks `X-Api-Key` before entering resources; unauthorized calls abort with HTTP 401.
- Domain errors are normalized via `GlobalExceptionMapper` so monitoring systems can rely on `status`, `error`, and `message` fields (.e.g., `NotFoundException` → 404, `ConflictException` → 409).
- Quarkus health endpoints (`/q/health`) expose liveness/readiness for load balancers; use `quarkus.flyway.migrate-at-start=true` to keep schema current during startup.
- Standard runtime commands: `mvn quarkus:dev` for local JVM, `mvn package` for production, optional `mvn -Pnative package` for native images defined in `pom.xml` profile.

## Search & Embedding Conventions
- `SearchService` fuses keyword results (`SkillRepository.keywordSearch`), vector search results (`SkillRepository.vectorSearch`), and popularity (download counts). Scores are normalized per source (`normalize()` helper) before applying weights (`search.weight.keyword`, `.semantic`, `.popularity`).
- Embedding retrieval occurs through `EmbeddingService.embed()` followed by `asPgVectorLiteral()` to form Postgres vector literals, so SQL queries remain safe.
- List inputs (tags, categories) are compared case-insensitively; use `List.of()` for immutable defaults when filters are absent.

## Versioning & Dependency Management
- Semantic version helpers (`SemVer`, `SemVerParser`, `SemVerConstraint`) parse constraints (`^1.2.3`, `~1.2.3`, `>=1.2.3 <2.0.0`) and inform `VersionService.resolve()`.
- Dependency trees traverse `DependencyResolver.resolve(name, version)` while guarding against `CircularDependencyException`; cache or memoization can be added if traversal becomes a bottleneck.
- Version metadata (`SkillVersion.latest`, `.yanked`, `.releaseNotes`) is stored separately from `Skill`; updates flip the previous `latest` flag to keep reporting accurate.

## Testing Standards (Java 21)
- Project targets Java 21 (`<maven.compiler.release>21</maven.compiler.release>`); tests run under Quarkus 3.20.2 using Maven Surefire 3.5.2.
- `mvn test` executes JUnit 5 suites in `src/test/java`, currently covering: `SemVerParserTest`, `SemVerConstraintTest`, `ManifestValidatorTest`.
- Tests must remain deterministic (no reliance on randomized data or network) and guard purely in-memory logic until repository mock support is added.

## Documentation & Maintenance
- Keep `/docs` aligned with code: architecture, code standards, PDR, design docs, and wireframes.
- Run `node .claude/scripts/validate-docs.cjs docs/` after each documentation change to confirm linting rules and link integrity.
- Update `docs/codebase-summary.md` after running `repomix` to reflect any new modules or dependencies.

---

**Practice Notes:**
- Favor composition over deep inheritance when splitting services.
- Keep code files under 200 lines when practical; split complex helpers into dedicated packages (e.g., `version`, `service`).
- Avoid mutable shared state; rely on Quarkus CDI for stateless beans.
