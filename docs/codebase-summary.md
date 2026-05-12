# Codebase Summary

*Generated from `repomix` compaction (see `repomix-output.xml`).*
*Snapshot captured after the Java REST gateway, web console, bundle API, and `gtk-skill` npm package implementation.*

## Overview
- **Tech stack:** Java 17, Quarkus, Jakarta REST annotations, PostgreSQL with vector support, plus a Node 20+ TypeScript CLI in `npm-package/`.
- **Gateway services:** Core server features live in `com.skillgateway.*` packages covering API, business services, repositories, and versioning helpers. MCP HTTP tooling is deferred.
- **Package distribution:** `gtk-skill` packages curated `.claude/` and `.opencode/` assets, ships a manifest-driven installer, and persists install state for later integrity checks.
- **AI integrations:** Embedding calls are delegated to `EmbeddingService`, which selects Ollama or OpenAI-compatible providers through `ai.embedding.*`; search weights and limits are externally configurable via `search.*` properties.

## Key Modules
| Layer | Description | Representative Files |
| --- | --- | --- |
| API | Exposes REST endpoints for publish/search/versioning/dependencies, bundle publish/download, and embedding status. | `SkillResource.java`, `SkillBundleResource.java`, `EmbeddingStatusResource.java`, `GlobalExceptionMapper.java`, `ApiKeyFilter.java` |
| Services | Implements publishing, bundle validation/storage, search fusion, version resolution, embedding, and dependency resolution. | `SkillService.java`, `SkillBundleService.java`, `SkillBundleValidator.java`, `SearchService.java`, `VersionService.java`, `EmbeddingService.java`, `DependencyResolver.java` |
| Persistence | Panache repositories backed by PostgreSQL; embeddings stored as `vector`, search indexes and bundle manifests managed by migrations. | `SkillRepository.java`, `SkillVersionRepository.java`, `SkillVersionFileRepository.java`, `resources/db/migration/*` |
| DTOs & Models | Java records/classes representing manifests, search payloads, responses, and version graphs. | `model/dto/*.java`, `model/Skill.java`, `model/SkillVersion.java` |
| MCP Tools | Deferred handlers are retained outside active source until dependency compatibility is restored. | `mcp-tools-backup/*.java` |
| npm CLI | Command registration, installer planning/execution, manifest generation, and package safety checks. | `npm-package/src/cli.ts`, `npm-package/src/commands/*.ts`, `npm-package/src/installer/*.ts`, `npm-package/src/manifest/*.ts` |

## Data & Persistence
- PostgreSQL schema is created and updated through migrations `V1` to `V5`, covering base skill tables, search indexes/triggers, pgvector conversion, and bundle artifact/file metadata.
- Embeddings are stored as Postgres `vector`; semantic search uses the `<=>` operator with `embedding IS NOT NULL` filtering.
- Full-text search uses `search_vector` materialized fields with `ts_rank` and `plainto_tsquery`.
- Version metadata persists per skill with `latest`, `yanked`, timestamps, and JSON-serialized dependency lists.
- Bundle metadata persists artifact URI, SHA-256, package format, entrypoint path, file count, and per-file checksums for published zip bundles.
- Consumer-project package install metadata is stored in `.gtk-skill/install-manifest.json` and backup replacements live under `.gtk-skill/backups/` when backup mode is used.

## npm Package Structure
- `npm-package/package.json` defines package name `gtk-skill`, Node engine `>=20`, published files, and `prepack` scripts.
- `npm-package/src/commands/` maps CLI flags to installer or manifest actions.
- `npm-package/src/installer/` plans create/conflict/overwrite/backup operations and writes install state after successful non-dry-run installs.
- `npm-package/src/project/detect-root.ts` treats installs into directories without common project markers as unsafe unless explicitly overridden.
- `npm-package/assets-manifest.json` records shipped asset source paths, target paths, types, checksums, and sizes.
- `npm-package/tests/` covers installer safety and manifest validation; package README documents command usage and release checks.

## Configuration & Runtime
- Server runtime values come from `application.properties` and Quarkus config mappings such as `search.weight.*`, `search.default-limit`, and `ai.embedding.*`.
- API key enforcement lives in `ApiKeyFilter`; globally mapped errors are emitted via `GlobalExceptionMapper`.
- Package runtime behavior is local-file based; `gtk-skill` does not execute packaged hooks or scripts during install/update.

## Build & Verification
- Maven handles the Java build lifecycle via `pom.xml`.
- `npm-package/` uses `tsc`, package scripts, and Node's built-in test runner.
- Verified package checks for this change set, per task context: `npm test --prefix npm-package`, CLI dry-run, and `npm pack` forbidden-file audit.
- Build outputs and dependency directories are ignored and should not be committed: `target/`, `node_modules/`, `npm-package/dist/`, and `web-ui/dist/`.
- `repomix` packed the repository into `repomix-output.xml` for codebase-wide review.

## Developer Notes
1. **Publish workflow:** `SkillResource.publish()` validates, persists or updates `Skill`, persists new `SkillVersion`, and recalculates embeddings with graceful degradation.
2. **Search fusion:** `SearchService` blends keyword, semantic, and popularity signals before ranking.
3. **Version resolution:** `VersionService` relies on `SemVerParser`, `SemVerConstraint`, and `SemVer` utilities for constraint evaluation and release listing.
4. **Package install safety:** `installAssets()` aborts before writes when the target lacks a common project marker and no override flag is supplied.
5. **Integrity checks:** `doctor` re-hashes installed files using manifest state to detect drift after installation.

## Testing Status
- `mvn test` executes JUnit 5 suites in `src/test/java`, ensuring Java 17 compatibility and core semantic-version / manifest validation coverage.
- `npm test --prefix npm-package` builds the TypeScript CLI and runs deterministic Node test suites located in `dist/tests/*.test.js`.
- Additional package smoke tests can expand around tarball installs when release automation is added.

*Last updated: 2026-05-13.*
