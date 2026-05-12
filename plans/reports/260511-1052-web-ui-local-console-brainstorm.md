---
type: brainstorm-report
date: 2026-05-11
topic: web-ui-local-console
status: agreed
---

# Web UI Local Console Brainstorm

## Summary

Build a local-first standalone frontend for AI Skill Gateway.
Frontend calls existing REST API at `http://localhost:18080` by default.
Public/multi-user mode deferred.

## Problem

Current integration guide is complete for clients and agents, but humans need a fast UI to inspect gateway status, search skills, publish manifests, resolve versions, inspect dependencies, and yank versions.

## Requirements

- Run local first.
- Separate frontend project for easier management.
- Use Tailwind/shadcn for UI.
- API key held in memory only.
- Support basic publish form.
- Support paste/import manifest JSON.
- Support paste/import raw `SKILL.md` and parse frontmatter when possible.
- Call documented endpoints from `docs/client-agent-integration-guide.md`.

## Evaluated Approaches

| Approach | Pros | Cons | Decision |
|---|---|---|---|
| Static UI served by Quarkus | Same origin, less CORS work | Couples UI lifecycle to backend | Rejected for now |
| Standalone Vite React UI | Fast, separate lifecycle, easy public later | May need CORS config | Selected |
| Next.js app | Strong future app structure | Overkill for local console | Rejected for MVP |

## Recommended Solution

Create `web-ui/` as standalone Vite React TypeScript app.
Use Tailwind/shadcn for practical console UI.
Keep API client small and explicit.
Default base URL: `http://localhost:18080`.
Allow runtime base URL switch to `http://localhost:8080`.
Keep `X-API-Key` in React memory state only, never hardcode, never persist by default.

## MVP Screens

1. Dashboard
   - Gateway health: `GET /q/health`
   - Embedding status: `GET /api/v1/embedding/status`
   - Base URL selector/input
   - API key presence indicator

2. Skill Explorer
   - List skills: `GET /api/v1/skills`
   - Search: `GET /api/v1/skills/search`
   - Filters: query, category, tags, page, size, limit

3. Skill Detail
   - Detail: `GET /api/v1/skills/{name}`
   - Versions: `GET /api/v1/skills/{name}/versions`
   - Resolve: `GET /api/v1/skills/{name}/resolve?constraint=...`
   - Dependencies: `GET /api/v1/skills/{name}/dependencies/{version}`

4. Publish Skill
   - Form mode for common fields.
   - JSON mode for raw manifest.
   - `SKILL.md` paste/import mode.
   - Preview normalized publish payload.
   - Submit: `POST /api/v1/skills/publish`.

5. Version Actions
   - Yank version with reason and confirm.
   - Submit: `POST /api/v1/skills/{name}/versions/{version}/yank?reason=...`.

## UX Principles

- Console-first, not landing page.
- Dense but readable layout.
- Show real API responses and normalized errors.
- Make protected actions visibly require API key.
- Confirmation for destructive/control actions.

## Risks

- Browser CORS may require backend config if not already enabled.
- Parsing arbitrary `SKILL.md` frontmatter can be imperfect; preview payload before submit is required.
- Public deployment later requires real auth, RBAC, audit log, secret handling, and stricter CORS.

## Success Metrics

- User can verify gateway health and embedding status from UI.
- User can search/list/detail skills from UI.
- User can paste `SKILL.md`, preview payload, and publish a skill.
- User can resolve dependencies and yank a version with API key.
- API key never written to source, docs examples, localStorage, or sessionStorage.

## Next Steps

- Create implementation plan for `web-ui/`.
- Include backend CORS check in plan.
- Implement frontend MVP after plan approval.
- Run build/typecheck and browser smoke test.

## Unresolved Questions

- None.
