# Code Standards & Engineering Guidelines

**Scope:** Aligns implementation, configuration, and documentation practices for the Java 17 / Quarkus REST skill gateway and the `gtk-skill` npm distribution package.
**Last Updated:** 2026-05-17

---

## Repository Layout & Module Responsibilities
- **Server entry point:** `src/main/java/com/skillgateway/api/SkillResource.java` exposes `/api/v1/skills` REST endpoints.
- **Server services:** `SkillService`, `SearchService`, `VersionService`, `EmbeddingService`, and `DependencyResolver` each own one concern.
- **Persistence:** Panache repositories (`SkillRepository`, `SkillVersionRepository`) live under `repository/` and are backed by PostgreSQL migrations in `resources/db/migration/`.
- **DTOs & Models:** `model/` and `model/dto/` define manifest, summary, version, and dependency payloads.
- **npm package:** `npm-package/` contains the distributable `gtk-skill` CLI. `src/commands/` owns CLI registration, `src/installer/` owns planning/execution/install-state, `src/manifest/` owns inventory and validation, and `tests/` verifies installer and manifest safety.
- **Published assets:** `npm-package/assets/` and `npm-package/assets-manifest.json` are generated artifacts representing the curated `.claude/`, `.codex/`, and `.opencode/` payload shipped in the npm tarball.

## API Design & DTO Conventions
- Prefer explicit HTTP methods and resource-oriented paths.
- Each endpoint accepts or returns typed DTOs such as `SkillManifest`, `SearchRequest`, `SearchResult`, `PublishResponse`, `VersionInfo`, and `VersionResolution`.
- Normalize query parameters in `SkillResource` before reaching services.
- Keep JSON responses DTO-based; do not expose persistence entities directly.

## Error Handling & Validation
- Validation logic lives in `ManifestValidator` and service guards.
- Bundle upload validation lives in `SkillBundleValidator`; it must reject traversal, absolute paths, forbidden local files, duplicate paths, and configured size/count limit violations before persistence.
- Throw domain exceptions (`ValidationException`, `ConflictException`, `NotFoundException`) when invariants fail.
- `GlobalExceptionMapper` centralizes exception-to-HTTP translation.
- When logging is added around failures, include contextual identifiers such as skill name and version.

## Configuration & Runtime Properties
- Server configuration is centralized via `AppConfig` and Quarkus `@ConfigProperty` beans.
- Enforce default pagination and search limits before repository calls.
- Embedding requests rely on `ai.embedding.provider`, `ai.embedding.url`, `ai.embedding.model`, `ai.embedding.dimension`, and `ai.embedding.timeout-seconds`; failures must degrade gracefully.
- Treat `ai.embedding.api-key` as secret config. Do not log it, include it in exception text, commit it, or use real values in tests/docs.
- Treat bundle content as untrusted data. Store scripts and hooks but do not execute them during publish, validation, or download.
- Embedding status responses and health data must not expose API keys, authorization headers, request bodies, response bodies, or URL query strings.
- Package runtime is defined by `npm-package/package.json`, including Node `>=20`, explicit `bin`, and `prepack` build/asset-generation scripts.

## npm Package & CLI Conventions
- Keep the published command surface aligned with `npm-package/src/cli.ts`: `install`, `list`, `doctor`, `update`, and `version`.
- `install` and `update` must stay non-destructive by default. Only explicit `--overwrite` or `--backup --overwrite` may replace changed user files.
- Preserve the project-root guard implemented by `detectProjectRoot()`; installs into directories without `.git`, `package.json`, `pom.xml`, `pyproject.toml`, `Cargo.toml`, or `go.mod` require `--allow-non-project-dir`.
- Record installed file metadata in `.gtk-skill/install-manifest.json`; `doctor` depends on that manifest to report missing and changed files.
- Treat `assets-manifest.json` as generated data. Regenerate it via package scripts instead of editing checksums, sizes, or targets by hand.
- Never rely on npm lifecycle side effects for installation. Users must run an explicit CLI command after package install.

## Packaging Safety Rules
- Manifest validation must reject traversal or absolute targets before files can be installed.
- Exclude runtime logs and local-only artifacts from packaged assets.
- Validate release payloads with `npm pack --dry-run --json` before publishing; forbidden files include `.env`, `.venv`, `node_modules`, session state, and local settings.
- Scripts and hooks copied by `gtk-skill` are data files only; the CLI must not execute them during install or update.

## Security, Observability & Deployment
- Mutating API requests pass through `ApiKeyFilter`, which checks `X-Api-Key` before entering resources. Public GET endpoints support catalog discovery.
- Domain errors are normalized via `GlobalExceptionMapper` so monitoring can rely on consistent error fields.
- Quarkus health endpoints (`/q/health`) expose liveness/readiness.
- Standard server runtime commands remain `mvn quarkus:dev`, `mvn package`, and optional `mvn -Pnative package`.

## Search, Versioning & Dependency Conventions
- `SearchService` fuses keyword, vector, and popularity signals with configurable weights.
- `EmbeddingService.embed()` and `asPgVectorLiteral()` keep vector search integration consistent.
- Provider implementations must keep request/response mapping network-free testable through a fake transport or parser helper.
- Semantic version helpers (`SemVer`, `SemVerParser`, `SemVerConstraint`) drive `VersionService.resolve()`.
- `DependencyResolver.resolve(name, version)` must continue guarding against `CircularDependencyException`.

## Testing Standards
- Server tests run under Java 17 / Quarkus / Maven Surefire and remain deterministic.
- Current Java coverage includes `SemVerParserTest`, `SemVerConstraintTest`, and `ManifestValidatorTest`.
- Embedding provider tests must not call external services; assert request body/header mapping and parse canonical provider responses locally.
- `npm test --prefix npm-package` builds the TypeScript CLI and runs Node test suites from `dist/tests/*.test.js`.
- Package tests must cover non-project-directory guards, conflict planning, install-state generation, and manifest safety checks.
- Keep tests network-free; use local temp directories and fixtures instead of external services or registries.

## Documentation & Maintenance
- Keep `/docs` aligned with both the Java server and the `gtk-skill` package.
- Run `repomix` after substantial package/module changes so `repomix-output.xml` and `docs/codebase-summary.md` reflect current structure.
- Run `node .claude/scripts/validate-docs.cjs docs/` after documentation changes.
- Keep doc examples conservative: only document flags and commands verified in `npm-package/src/commands/` and `npm-package/README.md`.

---

**Practice Notes:**
- Favor composition over deep inheritance when splitting services.
- Keep code files under 200 lines when practical; split complex helpers into dedicated packages.
- Avoid mutable shared state; rely on Quarkus CDI for stateless beans.
