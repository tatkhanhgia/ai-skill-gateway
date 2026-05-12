---
title: "Skill Folder Bundle Support"
description: "Support full skill folders as versioned bundles with SKILL.md entrypoint, file manifest, artifact storage, API download, and web publish workflow."
status: completed
priority: P1
effort: 18h
branch: master
tags: [feature, backend, frontend, storage, skills]
blockedBy: [260511-1142-fix-review-findings-embedding-dimension-and-publish-category]
blocks: []
created: 2026-05-11
---

# Skill Folder Bundle Support

## Overview

Add first-class support for complete skill folders. Current metadata-only publish remains backward compatible. New bundle publish stores searchable metadata in PostgreSQL and immutable zip artifacts in local storage, with per-file checksums for install/download integrity.

## Cross-Plan Dependencies

| Relationship | Plan | Status | Notes |
|---|---|---:|---|
| Blocked by | [Fix Review Findings](../260511-1142-fix-review-findings-embedding-dimension-and-publish-category/plan.md) | completed | Required embedding/category fixes present; bundle implementation proceeded without overlap. |
| Related | [Java MCP Skill Repository Server](../260221-0043-java-mcp-skill-repository-server/plan.md) | in-progress | Core registry foundation. Not blocking. |
| Related | [NPM Skill Distribution CLI](../260501-2110-npm-skill-distribution-cli/plan.md) | completed | Reuse file manifest/checksum patterns. |
| Related | [Web UI Local Console](../260511-1108-web-ui-local-console/plan.md) | completed | UI extension point for bundle publish. |

## Key Decisions

- Treat `SKILL.md` as required bundle entrypoint.
- Store artifact outside main metadata rows; DB stores summary and file manifest.
- Use local filesystem storage first via interface; do not overbuild cloud storage now.
- Index metadata plus text entrypoint/references only; do not embed binary assets.
- Keep existing `/publish` JSON endpoint intact.

## Phases

| Phase | Name | Status | Effort |
|---|---|---|---:|
| 1 | [Define Bundle Contract and Schema](./phase-01-define-bundle-contract-and-schema.md) | Completed | 3h |
| 2 | [Implement Bundle Validation and Artifact Storage](./phase-02-implement-bundle-validation-and-artifact-storage.md) | Completed | 5h |
| 3 | [Add Bundle Publish and Download API](./phase-03-add-bundle-publish-and-download-api.md) | Completed | 4h |
| 4 | [Extend Web UI Bundle Publish Workflow](./phase-04-extend-web-ui-bundle-publish-workflow.md) | Completed | 3h |
| 5 | [Update Installer/Docs Integration Contracts](./phase-05-update-installer-docs-integration-contracts.md) | Completed | 1.5h |
| 6 | [Test and Verification Pass](./phase-06-test-and-verification-pass.md) | Completed | 1.5h |

## Success Criteria

- Metadata-only publish still passes existing tests.
- Bundle publish rejects missing root `SKILL.md`, traversal paths, forbidden files, oversized bundles, and invalid frontmatter.
- Bundle download returns exact stored artifact and checksum metadata.
- Web UI can publish zip/folder bundle with preview.
- `mvn test`, web UI build, and relevant npm tests pass.

## Cook Handoff

After blockers complete and implementation is approved, run:

```powershell
ck:cook C:\Users\Admin\Documents\Project\NetBeansProjects\MyProject\ai-skill-gateway\plans\260511-1508-skill-folder-bundle-support\plan.md
```

## Unresolved Questions

- Resolved: local filesystem storage used for first implementation; Postgres stores artifact metadata and file manifest only.
