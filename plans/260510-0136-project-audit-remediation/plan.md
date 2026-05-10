---
title: "Project Audit Remediation"
description: "Dependency-ordered remediation plan for runtime, search, auth, MCP scope, packaging, and docs drift."
status: complete
priority: P1
effort: 15h
branch: master
tags: [audit, backend, database, security, npm, docs]
created: 2026-05-10
---

# Project Audit Remediation

Conservative defaults for this remediation:
- Canonical runtime port: `8080`.
- Auth contract: public `GET`; API key only for mutating endpoints.
- MCP: document as deferred unless active server code is restored in app sources.
- Java target: follow `pom.xml` compiler release `17`.

## TODOs

- [x] 1. Align runtime config first.
Files: `src/main/resources/application.properties`, `docker-compose.yml`, `Dockerfile.jvm`, `Dockerfile.native`, `README.md`, `config/config.yaml`.
Done when: app, Docker, and docs all use `8080` and health example stays `/q/health`.

- [x] 2. Fix vector schema + search path before more API/docs edits.
Files: `src/main/resources/db/migration/V1__create_skills_schema.sql`, new follow-up migration under `src/main/resources/db/migration/`, `src/main/java/com/skillgateway/model/Skill.java`, `src/main/java/com/skillgateway/repository/SkillRepository.java`, `src/main/java/com/skillgateway/service/SearchService.java`, `src/main/java/com/skillgateway/service/EmbeddingService.java`.
Done when: DB schema and entity agree on `vector(768)`, semantic query path is real, fallback behavior remains graceful.

- [x] 3. Lock API auth contract after search/schema are stable.
Files: `src/main/java/com/skillgateway/config/ApiKeyFilter.java`, `src/main/java/com/skillgateway/api/SkillResource.java`, relevant REST tests under `src/test/java/`.
Done when: only mutating endpoints require `X-API-Key`; public `GET` behavior is explicit and tested.

- [x] 4. Resolve MCP scope drift with defer-by-default docs update.
Files: `pom.xml`, `README.md`, `docs/system-architecture.md`, `docs/code-standards.md`; only touch `mcp-tools-backup/` if app-side MCP is intentionally reactivated.
Done when: docs stop claiming active MCP server support unless real app wiring exists.

- [x] 5. Tighten npm package payload after app contract is settled.
Files: `npm-package/package.json`, `npm-package/src/manifest/walk-files.ts`, `npm-package/src/manifest/asset-rules.ts`, `npm-package/src/manifest/copy-assets.ts`, `npm-package/tests/filter-assets.test.ts`, `npm-package/tests/manifest.test.ts`.
Done when: traversal skips excluded dirs early and pack payload omits tests, coverage, local runtime artifacts, and dependency folders.

- [x] 6. Sync docs and run final verification last.
Files: `README.md`, `docs/system-architecture.md`, `docs/code-standards.md`, optional `docs/codebase-summary.md` if wording changed materially.
Checks: `mvn test`, `npm test --prefix npm-package`, `npm run pack:dry-run --prefix npm-package`.

## Dependency Order

1. Runtime first, so env examples and verification target one port.
2. Schema/search second, because auth/docs should describe real behavior.
3. Auth third, because it depends on final public vs mutating surface.
4. MCP fourth, because this cycle assumes defer unless real app activation happens.
5. npm package fifth; isolated to `npm-package/`, can overlap late backend work if ownership is clean.
6. Docs + verification last.

## Unresolved Questions

- None. Plan uses conservative defaults above.
