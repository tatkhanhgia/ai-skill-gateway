---
title: "Web UI Local Console"
description: "Build a standalone local-first React console for AI Skill Gateway REST operations."
status: completed
priority: P1
effort: 16h
branch: master
tags: [feature, frontend, api, auth]
blockedBy: []
blocks: []
created: 2026-05-11
---

# Web UI Local Console

## Overview

Build `web-ui/` as standalone Vite React TypeScript app for local gateway operators.
UI calls documented REST endpoints, uses Tailwind/shadcn, and keeps API key in memory only.

## Cross-Plan Dependencies

| Relationship | Plan | Status | Notes |
|---|---|---:|---|
| Related | [Java MCP Skill Repository Server](../260221-0043-java-mcp-skill-repository-server/plan.md) | in-progress | Backend API source. Not blocking because endpoints exist. |
| Related | [ClaudeKit Explorer](../260327-0033-claudekit-explorer/plan.md) | pending | Similar React/shadcn UI ideas. Different scope and data source. |

## Phases

| Phase | Name | Status | Effort |
|---|---|---|---:|
| 1 | [Scaffold Frontend Workspace](./phase-01-scaffold-frontend-workspace.md) | Completed | 3h |
| 2 | [Implement API Client and App State](./phase-02-implement-api-client-and-app-state.md) | Completed | 3h |
| 3 | [Build Read-Only Console Screens](./phase-03-build-read-only-console-screens.md) | Completed | 4h |
| 4 | [Build Publish and Import Workflow](./phase-04-build-publish-and-import-workflow.md) | Completed | 3h |
| 5 | [Build Protected Version Actions](./phase-05-build-protected-version-actions.md) | Completed | 1.5h |
| 6 | [Validate Build, UX, and Docs](./phase-06-validate-build-ux-and-docs.md) | Completed | 1.5h |

## Dependencies

- Node 20+ for Vite frontend.
- Existing gateway endpoints from `docs/client-agent-integration-guide.md`.
- Backend CORS allowance for local frontend origin if browser blocks requests.
- Tailwind/shadcn and lucide icons.

## Success Criteria

- `web-ui/` builds with no TypeScript errors.
- Local UI can call health, embedding status, list, search, detail, versions, resolve, dependencies.
- User can paste JSON or `SKILL.md`, preview normalized payload, and publish with API key.
- User can yank version with confirm and reason.
- API key stays memory-only; no localStorage/sessionStorage persistence.

## Cook Handoff

After approval, run implementation with:

```powershell
ck:cook C:\Users\Admin\Documents\Project\NetBeansProjects\MyProject\ai-skill-gateway\plans\260511-1108-web-ui-local-console\plan.md
```

## Unresolved Questions

- None.
