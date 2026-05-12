---
title: "OpenAI-Compatible Embeddings"
description: "Add OpenAI-compatible embedding provider support while keeping Ollama as the local-first default."
status: complete
priority: P1
effort: 10h
branch: master
tags: [feature, backend, ai, api, search]
blockedBy: []
blocks: []
created: 2026-05-10
---

# OpenAI-Compatible Embeddings

## Overview

Current embedding integration is Ollama-shaped: request uses `prompt`, response reads root `embedding`. Add provider strategy so the gateway supports both Ollama and OpenAI-compatible `/v1/embeddings` APIs. Keep AI-first behavior: semantic search primary, keyword fallback graceful.

## Cross-Plan Dependencies

| Relationship | Plan | Status | Notes |
|---|---|---:|---|
| Related | [Java MCP Skill Repository Server](../260221-0043-java-mcp-skill-repository-server/plan.md) | in-progress | Existing backend/search foundation. Not blocking. |
| Related | [Project Audit Remediation](../260510-0136-project-audit-remediation/plan.md) | complete | Vector schema/search already corrected. No blocker. |

## Phases

| Phase | Name | Status | Effort |
|---|---|---|---:|
| 1 | [Define Provider Contract](./phase-01-define-provider-contract.md) | Complete | 2h |
| 2 | [Implement Provider Strategy](./phase-02-implement-provider-strategy.md) | Complete | 3h |
| 3 | [Add Tests and Validation](./phase-03-add-tests-and-validation.md) | Complete | 3h |
| 4 | [Update Docs and Runtime Examples](./phase-04-update-docs-and-runtime-examples.md) | Complete | 2h |

## Key Decisions

- Default remains `ollama`; OpenAI-compatible enabled by config.
- No hard dependency on remote AI. Provider failures keep graceful degradation.
- Keep PostgreSQL vector dimension fixed for this plan. Reject/degrade mismatched provider dimensions instead of changing schema dynamically.
- Do not store API keys in docs, tests, or committed config.

## Dependencies

- Existing `EmbeddingService`, `SearchService`, `SkillService`, `SkillRepository`.
- Quarkus config via `@ConfigProperty` / config mapping.
- Network-free unit tests for request/response mapping.

## Success Criteria

- Ollama default behavior remains compatible.
- OpenAI-compatible provider sends `input`, optional bearer auth, and parses `data[0].embedding`.
- Missing/failed provider still falls back without breaking publish/search.
- Dimension mismatch is detected and reported through provider failure path.
- `mvn test` passes.

## Cook Handoff

Completed with `ck:cook` on 2026-05-10.

## Unresolved Questions

- None.
