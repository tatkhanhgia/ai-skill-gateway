# Scaffold Frontend Workspace

## Context Links

- [Brainstorm Report](../reports/260511-1052-web-ui-local-console-brainstorm.md)
- [Client Agent Integration Guide](../../docs/client-agent-integration-guide.md)
- [Code Standards](../../docs/code-standards.md)
- [System Architecture](../../docs/system-architecture.md)

## Overview

Priority: P1.
Status: Completed.
Create isolated `web-ui/` frontend workspace with Vite, React, TypeScript, Tailwind, shadcn, and baseline commands.

## Key Insights

- Frontend must be separate from Quarkus lifecycle.
- Existing backend has no visible CORS config in `src/main/resources/application.properties`; local browser requests may fail until configured.
- Keep setup minimal. No Next.js. No auth library.

## Requirements

- Create `web-ui/` only for frontend source.
- Use Vite React TypeScript.
- Use Tailwind/shadcn UI primitives.
- Use lucide icons for toolbar/actions.
- Add scripts for dev, build, lint/typecheck if available.
- Add CORS backend config only if needed for local frontend origin.

## Architecture

```text
Browser on Vite dev server
  -> fetch REST API
AI Skill Gateway Quarkus server
  -> existing services/repositories
```

## Related Code Files

- Create: `web-ui/package.json`
- Create: `web-ui/src/**`
- Create: `web-ui/index.html`
- Create: `web-ui/vite.config.ts`
- Create: `web-ui/tailwind.config.*`
- Create: `web-ui/components.json`
- Modify if needed: `src/main/resources/application.properties`
- Modify if needed: `.gitignore`

## Implementation Steps

1. Scaffold `web-ui/` with Vite React TypeScript.
2. Install Tailwind, shadcn dependencies, lucide icons.
3. Configure Tailwind content paths and base styles.
4. Add shadcn component config and initial primitives.
5. Check browser CORS against `http://localhost:18080`; add Quarkus local CORS config only if required.
6. Keep generated files focused; avoid broad repo formatting churn.

## Todo List

- [x] Create Vite React TypeScript workspace.
- [x] Configure Tailwind/shadcn.
- [x] Add base layout shell.
- [x] Verify CORS need.
- [x] Update `.gitignore` for `web-ui` build artifacts if needed.

## Success Criteria

- `npm install` succeeds inside `web-ui/`.
- `npm run build` succeeds or clear follow-up exists.
- Empty app renders with Tailwind styles.
- CORS path known and documented in phase notes.

## Risk Assessment

- shadcn setup can generate many files; keep only needed components.
- CORS can block all browser calls despite API working by curl.

## Security Considerations

- Do not create `.env` with real API key.
- Do not hardcode `dev-api-key`.

## Next Steps

- Implement shared API client and memory-only app state.
