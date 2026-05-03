# Project Overview & Product Development Requirements

**Project:** AI Skill Gateway
**Version:** 1.1
**Status:** Active
**Last Updated:** 2026-05-01

## Purpose & Scope
Deliver a self-hosted MCP skill gateway plus a distributable `gtk-skill` npm package. The Java server exposes RESTful skill catalog management, search, version resolution, and dependency graph services for MCP agents and external clients. The npm package ships curated `.claude/` and `.opencode/` assets that can be installed into external projects with explicit, safety-first CLI commands.

### Scope
- Skill publish/list/search endpoints + version metadata management
- Weighted hybrid search combining keyword, semantic, and popularity signals
- Version constraint resolution and dependency tree exploration
- API key gating plus centralized exception handling
- Embedding service integration and PostgreSQL vector support
- `gtk-skill` packaging, manifest generation, installation, update, inventory, and integrity checks for curated assets

## Functional Requirements
1. **Publish Skill** – Accept manifests (`SkillManifest`) via `POST /api/v1/skills/publish`, validate payloads (`ManifestValidator`), persist `Skill` + `SkillVersion`, refresh embeddings, and return `PublishResponse`. Acceptance: saves new versions atomically, rejects duplicate version names, and regenerates PostgreSQL vectors when the embedding endpoint is reachable.
2. **Retrieve Skill Detail** – `GET /api/v1/skills/{name}` returns `SkillDetail` including latest version metadata or 404 via `NotFoundException`. Acceptance: non-existent names return JSON errors from `GlobalExceptionMapper`; existing skills include sorted tags.
3. **List Skills with Filters** – `GET /api/v1/skills` accepts `category`, `tags`, pagination, and returns `SkillSummary`. Acceptance: tags split by comma, pagination defaults to `page=0` / `size=20`, invalid page/size sanitized.
4. **Search Skills** – `GET /api/v1/skills/search` merges keyword search (`SkillRepository.keywordSearch`), semantic search (`EmbeddingService` + `vectorSearch`), and popularity normalization (`SearchService`). Acceptance: respects `search.default-limit` / `search.max-limit`, skips failed embeddings gracefully, sorts by fused score.
5. **Version Management** – Provide `GET /api/v1/skills/{name}/versions`, `GET /resolve`, `POST /versions/{version}/yank`, and dependency tree endpoint. Acceptance: versions sorted by semantic ordering, yanked flag toggled with reason, dependencies resolved via `DependencyResolver`.
6. **Packaged Asset Install** – `gtk-skill install` copies curated assets into a target project only when the user runs the command explicitly. Acceptance: npm install performs no writes, installs fail in directories without common project markers unless `--allow-non-project-dir` is passed, and `--dry-run` reports planned operations without changing files.
7. **Safe Conflict Handling** – `gtk-skill install` and `gtk-skill update` preserve local changes by default. Acceptance: default policy reports conflicts instead of overwriting, `--overwrite` replaces changed files, and `--backup --overwrite` stores replaced files under `.gtk-skill/backups/<timestamp>/`.
8. **Asset Inventory & Integrity Checks** – `gtk-skill list` and `gtk-skill doctor` expose package contents and post-install state. Acceptance: `list` reports manifest-backed counts by asset type, `doctor` compares installed hashes against `.gtk-skill/install-manifest.json`, and missing or changed files produce a failing exit code.
9. **Package Safety** – Published npm tarballs exclude forbidden runtime artifacts. Acceptance: manifest validation rejects absolute/traversal targets, runtime logs are excluded, and pack validation confirms files such as `.env`, `.venv`, `node_modules`, session state, and local settings are not shipped.
10. **Security** – Every API request passes `ApiKeyFilter`. Acceptance: requests without valid `X-Api-Key` return 401 before service layer or mapper responses.

## Verified npm Package Snapshot
- `npm-package/package.json` defines package name `gtk-skill`, Node engine `>=20`, explicit `bin` mapping, and a `prepack` workflow that builds TypeScript then regenerates copied assets and `assets-manifest.json`.
- `npm-package/src/cli.ts` registers `install`, `list`, `doctor`, `update`, and `version` commands.
- `npm-package/src/installer/install-assets.ts` blocks installs into directories without common project markers unless `allowNonProjectDir` is enabled.
- `npm-package/src/installer/install-state.ts` writes `.gtk-skill/install-manifest.json` for later `doctor` checks.
- `npm-package/tests/installer.test.ts` and `npm-package/tests/manifest.test.ts` cover installer safety and manifest validation.
- Per task context, `npm test --prefix npm-package`, CLI dry-run, and `npm pack` forbidden-file audit already passed for this change set.

## Non-functional Requirements
- **Reliability:** >=99% uptime for REST endpoints; DB migrations protected by Quarkus-managed transactions.
- **Performance:** Search requests finish within 300ms for cached keyword results and 500ms when semantic embeddings succeed.
- **Scalability:** Designed for a single node with vector-enabled PostgreSQL, but services remain modular for later horizontal scaling.
- **Maintainability:** Business logic split into services/repositories; npm package logic split into manifest, installer, command, and output modules.
- **Observability:** Exceptions funnel through `GlobalExceptionMapper`; logs should include skill/category names and reason codes.
- **CLI safety:** File installation stays explicit, deterministic, and auditable through manifest-based metadata under `.gtk-skill/`.
- **Compatibility:** `gtk-skill` targets Node.js 20+ and must not execute packaged scripts or hooks during install/update operations.

## Technical Constraints & Dependencies
- **Server runtime:** Java 21 + Quarkus with Jakarta REST and Panache patterns.
- **Database:** PostgreSQL with `vector` column for embeddings and `search_vector` for full-text search; migrations `V1` to `V3` manage schema/indexes.
- **AI integration:** `EmbeddingService` calls `ai.embedding.url` and `model`; `search.weight.*`, `search.default-limit`, and `search.max-limit` come from config. External embedding failure must not abort publish/search.
- **Security:** API key enforced via `ApiKeyFilter`; domain exceptions mapped by `GlobalExceptionMapper`.
- **Dependency resolution:** `SemVer`, `SemVerParser`, `SemVerConstraint`, `DependencyResolver`, and `CircularDependencyException` enforce version graph consistency.
- **CLI runtime:** `gtk-skill` is a TypeScript/Node package using `commander`; published payload is constrained by the `files` field in `npm-package/package.json` and manifest validation in `npm-package/src/manifest/`.
- **Install state:** Local installation metadata is written to `.gtk-skill/install-manifest.json` and used by `doctor` to detect missing or changed files.

## Implementation Guidance & Architectural Decisions
- **Layering:** Keep `SkillResource` thin; let services handle validation, embeddings, scoring, and repository delegation.
- **Search Fusion:** Normalize keyword scores and popularity before weighting; keep combined scores in helper objects.
- **Transaction boundaries:** `@Transactional` on publish/yank ensures atomic writes.
- **Embeddings:** Guard network calls with try/catch; degrade to null vectors if provider fails.
- **Exceptions:** Throw domain-specific exceptions and rely on `GlobalExceptionMapper` for structured HTTP responses.
- **CLI packaging:** Generate installable assets into `npm-package/assets/`, then derive `assets-manifest.json` from shipped files rather than handwritten metadata.
- **Conflict policy:** Keep default install/update behavior non-destructive; require explicit `--overwrite` or `--backup --overwrite` to replace changed files.
- **Project detection:** Treat installs into arbitrary folders as unsafe by default; require a project marker (`.git`, `package.json`, `pom.xml`, `pyproject.toml`, `Cargo.toml`, or `go.mod`) or an explicit override flag.

## Success Metrics & Acceptance Criteria
| Metric | Target | Verification |
| --- | --- | --- |
| API key enforcement coverage | 100% of protected endpoints | Security tests and manual curl with/without key |
| Search latency | <500ms for successful semantic path | Benchmark search endpoint with sample text |
| Functional test coverage | >=80% for service layer | Unit tests for `SkillService`, `SearchService`, `VersionService` |
| npm package command coverage | Core install/list/doctor/update safety paths covered | `npm test --prefix npm-package` |
| Package payload safety | 0 forbidden files in tarball | `npm pack --dry-run --json` audit |
| Documentation synchronization | Docs updated for architecture, standards, PDR | Repo review and doc validation script |

## Testing Status
- `mvn test` executes JUnit 5 suites in `src/test/java`, proving Java 21 compatibility for the server.
- Core Java tests include `SemVerParserTest`, `SemVerConstraintTest`, and `ManifestValidatorTest`.
- `npm test --prefix npm-package` builds the TypeScript CLI and runs Node test suites from `dist/tests/*.test.js`, covering manifest validation and installer behavior.
- Per task context, additional release checks passed: CLI dry-run and `npm pack` forbidden-file audit.

## Risks & Mitigations
- **Embedding provider outages** – Already handled by try/catch; add retry/backoff if SLO demands.
- **Semantic search drift** – Weight adjustments tracked via `search.weight.*`; default limits bound result sets.
- **Circular dependencies** – `DependencyResolver` throws `CircularDependencyException`; keep integration coverage for loops.
- **Asset drift between repo and package** – Mitigate via `prepare:assets`, manifest regeneration, and prepack checks.
- **Unsafe overwrites in consumer projects** – Mitigate via default conflict reporting, backup mode, and explicit overwrite flags.

## Version History
- **1.1 (2026-05-01):** Added verified requirements for the `gtk-skill` npm package, install safety model, manifest-based integrity checks, and release validation workflow.
- **1.0 (2026-02-22):** Initial Java MCP server PDR published.
