---
title: "Skill Folder Bundle Support Plan"
created: 2026-05-11
type: journal
---

# Skill Folder Bundle Support Plan

## Context

User approved brainstorm direction: support complete skill folders, not only single `SKILL.md` metadata.

## What Happened

- Researched official Claude skill structure and local bundled skill spec.
- Confirmed current backend persists metadata only.
- Created implementation plan at `plans/260511-1508-skill-folder-bundle-support/`.
- Added brainstorm report and six phase files.
- Marked new plan blocked by pending publish/default fix plan to avoid frontend publish-flow overlap.

## Decisions

- Metadata remains in PostgreSQL.
- Bundle artifact stored separately as canonical zip through local-first storage adapter.
- `SKILL.md` required as entrypoint.
- Existing JSON publish remains backward compatible.

## Next

- Complete blocker plan `260511-1142-fix-review-findings-embedding-dimension-and-publish-category`.
- Then implement bundle support via `ck:cook` handoff in new plan.

## Unresolved Questions

- Confirm whether first artifact storage must be local filesystem or Postgres bytea.
