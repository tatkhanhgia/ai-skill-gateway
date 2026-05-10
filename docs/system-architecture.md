# System Architecture - Java MCP Skill Gateway

**Scope:** Documents the layered architecture implemented in the Java 17 + Quarkus REST skill gateway server and the `gtk-skill` npm packaging/distribution workflow.
**Last Updated:** 2026-05-01

---

## High-Level Overview
- **Server runtime:** Java 17, Quarkus 3.20.2, Maven-managed build. MCP HTTP extension remains deferred due version compatibility.
- **Package runtime:** `gtk-skill` is a Node 20+ TypeScript CLI built with `tsc` and published from `npm-package/`.
- **Purpose:** Expose `/api/v1/skills` endpoints for gateway operations and ship curated `.claude/` / `.opencode/` assets that can be installed into external projects with explicit CLI commands.
- **Data stores:** PostgreSQL with `vector` and `tsvector` columns for embeddings/search; local package install metadata stored under `.gtk-skill/install-manifest.json` in consumer projects.
- **AI integration:** `EmbeddingService` posts to configurable `ai.embedding.url/model`, transforms responses into Postgres `vector` literals, and gracefully degrades when the provider is unreachable.
- **Packaging integration:** `npm-package/assets-manifest.json` describes every shipped asset with source, target, type, checksum, and size.

## API Layer
- **SkillResource** exposes REST endpoints (`publish`, `list`, `get`, `search`, `versions`, `resolve`, `yank`, `dependencies`).
- **DTOs:** The API layer uses typed request/response DTOs documented in the codebase docs where they are verified.
- **Filters & Exception Mapping:** `ApiKeyFilter` enforces `X-Api-Key` for mutating endpoints; `GlobalExceptionMapper` maps domain errors to consistent JSON responses with proper status codes.

## npm Package Layer
- **CLI entrypoint:** `npm-package/src/cli.ts` registers `install`, `list`, `doctor`, `update`, and `version` commands using `commander`.
- **Command adapters:** `src/commands/install-command.ts`, `update-command.ts`, `list-command.ts`, and `doctor-command.ts` translate CLI flags into installer/manifest operations.
- **Manifest subsystem:** `assets-manifest.json` plus `src/manifest/` provide the package inventory, checksums, target paths, and validation rules for safe installs.
- **Installer subsystem:** `src/installer/install-assets.ts`, `plan-operations.ts`, `execute-operations.ts`, and `install-state.ts` enforce project-root detection, conflict policies, backup handling, and `.gtk-skill/install-manifest.json` persistence.
- **Verification:** Package tests cover manifest safety and installer behavior; `doctor` re-hashes installed files to detect missing or changed assets.

## Package Install Flow
1. `gtk-skill install` or `gtk-skill update` resolves the target root with `detectProjectRoot()`.
2. If the directory has no common project marker and the caller did not pass `--allow-non-project-dir`, install aborts with an error before any writes.
3. The installer reads `assets-manifest.json`, filters files by requested groups, plans file operations, and marks conflicts by default.
4. With `--overwrite` or `--backup --overwrite`, the installer replaces changed files and optionally writes backups under `.gtk-skill/backups/<timestamp>/`.
5. After a successful non-dry-run install, `writeInstallState()` records target paths and checksums in `.gtk-skill/install-manifest.json` for later `doctor` checks.
6. `doctor` reads that install state, compares current file hashes, and exits non-zero when files are missing or changed.

## Packaging Safety Boundaries
- npm install alone performs no writes into consumer projects; users must run an explicit CLI command.
- Target validation rejects absolute paths and traversal before assets can be installed.
- Runtime logs and similar local artifacts are excluded from package payload generation.
- The CLI copies scripts and hooks as files only; it does not execute packaged hooks or scripts during install/update.
- Release validation relies on `npm pack --dry-run --json` plus forbidden-file audit before publication.

## Service Layer
- **SkillService** handles validation (`ManifestValidator`), persistence of `Skill` + `SkillVersion`, embedding refresh, and yank operations within transactional boundaries.
- **SearchService** merges keyword (`SkillRepository.keywordSearch`), semantic (`EmbeddingService` + `SkillRepository.vectorSearch`), and popularity signals. Scores are normalized and combined via weights configured in `search.weight.*` before sorting and trimming to configured limits.
- **VersionService** relies on semantic-version helpers to list and resolve versions; toggles `latest`/`yanked` flags as needed.
- **DependencyResolver** builds dependency graphs and guards against `CircularDependencyException` to prevent infinite traversal.

## Persistence Layer
- **Repositories:** `SkillRepository` and `SkillVersionRepository` extend Panache and expose helpers for keyword search, vector search, tag retrieval, and latest-version bookkeeping.
- **Embeddings & Search Vectors:** PostgreSQL `vector` columns store embeddings, while `tsvector` and triggers keep full-text search data synchronized; `SkillVersion` persists metadata such as `releaseNotes`, `latest`, `yanked`, and dependency payloads.

## Data & AI Flow
1. **Publish:** `SkillService.publish()` validates the manifest, persists versions, refreshes embeddings via `EmbeddingService`, and persists vectors/metadata atomically.
2. **Search:** `SearchService` executes keyword and vector queries, normalizes scores, merges per-skill tags, respects filters, and returns ranked results.
3. **Version resolution:** Queries route through `VersionService.resolve()` which parses constraints and returns matching non-yanked versions.
4. **Dependency inspection:** `/dependencies/{version}` uses `DependencyResolver` to expand requirements while preventing cycles.

## Configuration & Runtime
- **Properties:** Quarkus config fields centralize settings such as `search.weight.*`, `search.default-limit`, `search.max-limit`, `ai.embedding.url/model`, and pagination defaults.
- **Resiliency:** Embedding failures are caught in `SearchService` and `SkillService`, allowing API responses even when external AI endpoints miss.
- **Security:** `ApiKeyFilter` rejects unauthorized mutating calls with HTTP 401 before they reach services; public GET endpoints support catalog discovery.

## Observability & Operations
- **Logging:** Domain exceptions should include contextual identifiers such as skill name and version when logging is added.
- **Health:** Quarkus health endpoints (`/q/health`) surface readiness; `quarkus.flyway.migrate-at-start=true` keeps the database schema up to date during startup.
- **Deployment:** Standard JVM invocation (`mvn quarkus:dev`) for local development; `mvn package` or `mvn -Pnative package` for production/native builds defined in `pom.xml`.

## Testing & Verification
- **Java 17 compliance:** Project compiles and runs under Java 17. Maven Surefire 3.5.2 executes JUnit 5 suites that include `SemVerParserTest`, `SemVerConstraintTest`, and `ManifestValidatorTest`.
- **npm package verification:** `npm test --prefix npm-package` builds the TypeScript CLI and runs package tests; per task context, CLI dry-run and forbidden-file pack audit also passed.

## Maintenance Notes
- **Documentation sync:** `repomix` compaction regenerates `repomix-output.xml`; `docs/codebase-summary.md` reflects module changes after each run.
- **Doc validation:** Run `node .claude/scripts/validate-docs.cjs docs/` after edits to ensure formatting and link hygiene.
- **File size:** Keep documentation under 800 LOC per file; split topics into subdirectories if nearing the limit.
