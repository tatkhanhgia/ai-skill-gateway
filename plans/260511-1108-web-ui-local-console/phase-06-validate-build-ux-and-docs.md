# Validate Build, UX, and Docs

## Context Links

- [Client Agent Integration Guide](../../docs/client-agent-integration-guide.md)
- [Docs README](../../docs/README.md)
- [Plan Overview](./plan.md)

## Overview

Priority: P1.
Status: Completed.
Run frontend/backend validation, browser smoke test, and update docs for local UI usage.

## Key Insights

- User must be able to run UI without reading source.
- API key memory-only behavior needs explicit doc note.
- Browser smoke test should catch CORS, blank screen, and basic layout issues.

## Requirements

- Run frontend build/typecheck.
- Run backend compile/tests if backend config changed.
- Run docs validation after docs changes.
- Start local dev server and smoke test in browser.
- Verify API key is not stored in local/session storage.

## Architecture

```text
Verification
  npm build/typecheck
  optional mvn test if backend changed
  browser smoke test
  docs validate
```

## Related Code Files

- Modify: `docs/README.md`
- Modify or create: `docs/client-agent-integration-guide.md` section for Web UI if useful
- Modify: `README.md` if run command should be discoverable from root
- No source deletion expected

## Implementation Steps

1. Run `npm run build` in `web-ui/`.
2. Run lint/typecheck script if configured.
3. If CORS/backend config changed, run `mvn test`.
4. Start `web-ui` dev server and verify desktop/mobile viewports.
5. Confirm health/status calls work against running gateway or show clean connection errors if gateway down.
6. Inspect browser storage for no API key persistence.
7. Update docs with local UI run steps and security note.
8. Run `node .claude/scripts/validate-docs.cjs docs/`.

## Todo List

- [x] Frontend build passes.
- [x] Backend tests pass if backend touched.
- [x] Browser smoke test passes.
- [x] API key storage check passes.
- [x] Docs updated and validated.

## Success Criteria

- UI starts locally and renders nonblank.
- Main workflows have visible success/error states.
- Docs tell user how to run backend + `web-ui`.
- No syntax/type errors.

## Risk Assessment

- Backend may not be running during UI validation; UI should still render and show connection error.
- shadcn generated code can bloat; review file count and keep only used components.

## Security Considerations

- Validate no API key in source, docs, localStorage, sessionStorage, URL, console logs.

## Next Steps

- Move implementation status to completed after test/review cycle.
