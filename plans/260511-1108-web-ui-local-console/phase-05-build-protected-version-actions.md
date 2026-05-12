# Build Protected Version Actions

## Context Links

- [Client Agent Integration Guide](../../docs/client-agent-integration-guide.md)
- [Phase 03](./phase-03-build-read-only-console-screens.md)
- [Phase 04](./phase-04-build-publish-and-import-workflow.md)

## Overview

Priority: P2.
Status: Completed.
Add protected version controls, focused on yank version with explicit confirmation and reason input.

## Key Insights

- Yank is a control action and should not be one-click.
- Missing/invalid API key returns 401; client should preflight missing key.

## Requirements

- Add yank action from version list/detail.
- Require confirm modal.
- Require or strongly prompt for reason.
- Show success even when response body is empty.
- Refresh version list after success.

## Architecture

```text
SkillDetailPanel
  VersionList
  YankVersionDialog
GatewayApiClient.yankVersion()
```

## Related Code Files

- Modify: `web-ui/src/features/skills/skill-detail-panel.tsx`
- Create: `web-ui/src/features/skills/yank-version-dialog.tsx`
- Modify: `web-ui/src/lib/gateway-api-client.ts`

## Implementation Steps

1. Add action button on each non-yanked version row.
2. Open confirm dialog with skill name, version, and reason input.
3. Block submit when API key missing.
4. Call yank endpoint with URL-encoded reason.
5. Treat HTTP 200 empty body as success.
6. Refresh detail/versions after success.

## Todo List

- [x] Add yank dialog.
- [x] Wire protected endpoint.
- [x] Refresh state after success.
- [x] Render 401/404/409 errors clearly.

## Success Criteria

- User can yank a version after confirm.
- User sees clear missing API key state.
- Empty success body does not break UI.

## Risk Assessment

- Accidental yanks harm registry state; confirmation text must be explicit.

## Security Considerations

- Do not display API key in dialog or errors.
- URL-encode reason.

## Next Steps

- Validate build, UX, docs.
