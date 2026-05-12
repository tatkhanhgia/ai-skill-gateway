# Phase 04 - Extend Web UI Bundle Publish Workflow

## Context Links

- [Plan](./plan.md)
- Current publish view: `web-ui/src/features/publish/publish-view.tsx`
- API client: `web-ui/src/lib/gateway-api-client.ts`
- Types: `web-ui/src/lib/gateway-types.ts`

## Overview

Priority: P1. Status: Completed. Add a practical bundle publish UX: zip upload first, folder upload where browser support exists.

## Key Insights

- Zip upload is reliable across browsers.
- Directory upload uses non-standard browser attributes; keep it optional.
- Preview must show metadata and file risk before publish.

## Requirements

- Add publish mode: `skill-bundle`.
- Select zip file.
- Optional folder selection if compatible.
- Preview parsed `SKILL.md`, file count, total size, blocked file errors.
- Submit multipart request with API key.
- Preserve current form/json/skill-md modes.

## Architecture

```text
PublishView mode
  -> Bundle file picker
  -> client-side preview helper
  -> gatewayApiClient.publishBundle()
  -> PublishResponse display
```

## Related Code Files

Modify:
- `web-ui/src/features/publish/publish-view.tsx`
- `web-ui/src/lib/gateway-api-client.ts`
- `web-ui/src/lib/gateway-types.ts`
- `web-ui/src/lib/publish-payload-validator.ts` only if shared category defaults are needed after blocker plan

Create:
- `web-ui/src/lib/skill-bundle-preview.ts`
- Optional component under `web-ui/src/features/publish/`

## Implementation Steps

1. Add API client multipart method using `FormData`.
2. Add mode switch and zip input.
3. Client-side preview reads zip metadata if a small dependency is acceptable; otherwise show filename/size and rely on server validation.
4. For folder upload, build zip client-side only if dependency risk is acceptable; otherwise defer.
5. Display server validation errors clearly.
6. Keep API key memory-only.

## Todo List

- [x] Add client method.
- [x] Add UI mode.
- [x] Add preview/error states.
- [x] Run frontend build.

## Success Criteria

- Existing publish modes still work.
- Bundle mode can submit zip with API key.
- User sees clear validation failures.

## Risk Assessment

- Risk: adding zip dependency bloats UI. Mitigation: start with server-validated zip upload, add rich preview later only if needed.

## Security Considerations

- Do not persist selected file or API key.
- Do not execute or render arbitrary HTML from bundle.

## Next Steps

- Phase 05 aligns docs and installer expectations.

## Unresolved Questions

- Folder upload in browser is optional in first pass.
