---
title: "Fix Review Findings: Embedding Dimension and Publish Category"
description: "Minimal plan to align embedding config validation with pgvector(768) and stop web UI default publish failures."
status: pending
priority: P1
effort: 3h
branch: master
tags: [fix, backend, frontend, validation]
blockedBy: []
blocks: [260511-1508-skill-folder-bundle-support]
created: 2026-05-11
---

# Fix Review Findings: Embedding Dimension and Publish Category

## Overview

Apply smallest safe fix for 2 regressions:
- Backend: reject `AI_EMBEDDING_DIMENSION` values other than `768` because DB schema, migration, and entity remain fixed at `vector(768)`.
- Web UI: replace default publish category `general` with a backend-accepted category so default publish flow succeeds.

## Phases

| Phase | Name | Status | Effort |
|---|---|---|---:|
| 1 | [Constrain Embedding Dimension Validation](./phase-01-constrain-embedding-dimension-validation.md) | Pending | 1h |
| 2 | [Align Publish Category Defaults](./phase-02-align-publish-category-defaults.md) | Pending | 0.5h |
| 3 | [Run Regression Verification](./phase-03-run-regression-verification.md) | Pending | 1.5h |

## Dependency Graph

- Phase 1 has no blockers.
- Phase 2 has no blockers.
- Phase 3 depends on Phases 1 and 2.
- No parallel phase should edit same file:
  - Phase 1 owns backend config/service/tests.
  - Phase 2 owns web UI publish files.
  - Phase 3 owns no product files; verification only.

## Data Flows

- Embedding config flow:
  `AI_EMBEDDING_DIMENSION` env var -> Quarkus config `ai.embedding.dimension` -> `EmbeddingService.status()/provider init` -> embedding vector write path -> PostgreSQL `skills.embedding vector(768)`.
- Publish category flow:
  publish form/JSON normalization defaults -> publish payload preview -> client `POST /api/v1/skills/publish` -> `ManifestValidator` allowed-category check.

## Backward Compatibility

- Keep schema static at `768`; no migration, no entity change, no API contract change.
- Existing clients using `768` continue unchanged.
- Existing UI/manual publish payloads with valid categories continue unchanged.
- Invalid env values and default `general` stop earlier with explicit validation behavior instead of runtime/server-side failure.

## Success Criteria

- Backend marks non-`768` embedding dimension config invalid before provider usage or DB write mismatch.
- UI default publish payload uses an allowed category.
- Regression tests cover the new dimension guard and category acceptance path where feasible.
- `mvn test` passes.
- `npm run build --prefix web-ui` passes.

## Rollback

- Backend rollback: revert validation/test changes only; no schema rollback needed.
- Frontend rollback: revert default category change only.
- Verification rollback: none.

## Unresolved Questions

- None.
