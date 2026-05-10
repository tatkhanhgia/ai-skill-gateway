---
title: "Audit Remediation Scout Report"
date: 2026-05-10
status: complete
---

# Audit Remediation Scout Report

## Scope

Plan remediation for project audit findings: runtime config mismatch, vector schema/search risk, API auth contract drift, MCP scope drift, npm package payload/prepack issues, stale docs.

## Evidence

- `mvn test` via Windows Maven + Java 17 passed: 8 tests, 0 failures.
- `npm test` in `npm-package/` passed: 10 tests, 0 failures.
- `npm run pack:dry-run` passed but produced large tarball: 2440 entries, ~9.2 MB packed, ~32.9 MB unpacked.
- Git worktree already dirty, mostly `.claude` and `.opencode`; do not revert unrelated changes.

## Key Findings

- Runtime port conflict: `application.properties` uses `7070`; Docker/README examples expose/use `8080`.
- DB schema conflict: migration creates `skills.embedding TEXT`; entity/query expect pgvector semantics.
- API auth mismatch: docs say all API requests require key; `ApiKeyFilter` protects only selected POST endpoints.
- MCP mismatch: docs present MCP gateway; Quarkus MCP extension is commented out and handlers live in `mcp-tools-backup/`.
- npm packaging includes test/coverage artifacts and traverses excluded dependency folders before filtering.
- Docs link to missing `PLAN.md` and `docs/api-reference.md`; Java version claims differ from `maven.compiler.release=17`.

## Recommendation

Fix in dependency order:

1. Align runtime port and environment docs.
2. Fix vector schema/search and add DB-backed tests.
3. Decide/enforce API auth contract.
4. Decide MCP scope: restore or document as deferred.
5. Tighten npm asset traversal and payload exclude rules.
6. Sync docs and run full verification.

## Unresolved Questions

- Should canonical runtime port be `7070` or `8080`?
- Should GET endpoints be public or require `X-API-Key`?
- Should MCP server be restored now, or explicitly deferred?
- Should project target Java 17 or Java 21?
