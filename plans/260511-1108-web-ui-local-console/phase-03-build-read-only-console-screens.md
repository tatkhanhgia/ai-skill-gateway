# Build Read-Only Console Screens

## Context Links

- [Client Agent Integration Guide](../../docs/client-agent-integration-guide.md)
- [Phase 02](./phase-02-implement-api-client-and-app-state.md)

## Overview

Priority: P1.
Status: Completed.
Build dashboard, skill explorer, and skill detail workflows using public GET endpoints.

## Key Insights

- First viewport should be actual console, not marketing.
- Local operators need dense scan-friendly UI.
- Empty search result is valid, not an error.

## Requirements

- Dashboard calls health and embedding status.
- Explorer supports list/search/filter.
- Detail view shows metadata, versions, resolve, dependencies.
- Show loading, empty, success, and error states.
- Keep layout responsive without oversized hero/cards.

## Architecture

```text
AppShell
  Top bar: base URL, API key state
  Left/nav: Dashboard, Explorer, Publish
  Main: selected view
DashboardView
SkillExplorerView
SkillDetailPanel
```

## Related Code Files

- Create: `web-ui/src/app/app-shell.tsx`
- Create: `web-ui/src/features/dashboard/dashboard-view.tsx`
- Create: `web-ui/src/features/skills/skill-explorer-view.tsx`
- Create: `web-ui/src/features/skills/skill-detail-panel.tsx`
- Create: `web-ui/src/components/status-indicator.tsx`
- Create: `web-ui/src/components/json-response-viewer.tsx`

## Implementation Steps

1. Build app shell with compact navigation and config controls.
2. Build dashboard health/status cards or panels with raw JSON fallback.
3. Build search/list form with query/category/tags/page/size/limit controls.
4. Render results in table/list with stable row sizing and detail action.
5. Build detail panel with tabs/sections for metadata, versions, resolve, dependencies.
6. Add refresh actions and visible empty states.

## Todo List

- [x] App shell and config controls.
- [x] Dashboard view.
- [x] Skill explorer list/search.
- [x] Skill detail panel.
- [x] Resolve/dependencies read-only tools.

## Success Criteria

- User can inspect server and embedding status.
- User can search/list skills.
- User can inspect one skill and its versions.
- User can resolve a version and inspect dependencies.

## Risk Assessment

- Overly decorative UI slows operator workflow; keep dense and direct.
- Search result schema may omit fields; render optional fields safely.

## Security Considerations

- Read-only screens must not require or expose API key.
- Do not dump request headers in JSON viewer.

## Next Steps

- Add publish/import workflow.
