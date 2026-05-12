# Implement API Client and App State

## Context Links

- [Client Agent Integration Guide](../../docs/client-agent-integration-guide.md)
- [Phase 01](./phase-01-scaffold-frontend-workspace.md)

## Overview

Priority: P1.
Status: Completed.
Create typed gateway API client, runtime base URL state, memory-only API key state, and normalized error handling.

## Key Insights

- Read endpoints do not require auth.
- Protected endpoints require `X-API-Key`.
- API key must never persist to localStorage/sessionStorage.
- Error shape is stable: `{ error, details }`.

## Requirements

- Default base URL: `http://localhost:18080`.
- Allow switch to `http://localhost:8080`.
- Keep API key in React state/context only.
- Attach `X-API-Key` only for protected calls.
- Normalize HTTP/network errors for UI display.
- Define DTO types from integration guide.

## Architecture

```text
AppConfigContext
  baseUrl
  apiKey memory value
GatewayApiClient
  health/status/read calls
  publish/yank protected calls
UI hooks/components
  consume client results
```

## Related Code Files

- Create: `web-ui/src/lib/gateway-api-client.ts`
- Create: `web-ui/src/lib/gateway-types.ts`
- Create: `web-ui/src/lib/api-errors.ts`
- Create: `web-ui/src/providers/app-config-provider.tsx`
- Create: `web-ui/src/hooks/use-gateway-request.ts`

## Implementation Steps

1. Define DTO interfaces for skills, search results, details, versions, resolution, dependencies, publish response, embedding status, and API errors.
2. Implement `requestJson` helper with query params and method/body support.
3. Implement gateway client methods for all documented endpoints.
4. Add protected-call guard that errors clearly when API key is missing.
5. Add app config provider with base URL and API key setter.
6. Avoid storage APIs for API key. Optional base URL persistence only if not mixed with secrets; default simpler: memory only.

## Todo List

- [x] Define DTOs.
- [x] Implement fetch wrapper.
- [x] Implement endpoint methods.
- [x] Add memory-only config provider.
- [x] Add consistent error normalization.

## Success Criteria

- API client compiles.
- Missing API key blocks publish/yank before request.
- Error messages show HTTP status and server details.
- No `localStorage` or `sessionStorage` usage for API key.

## Risk Assessment

- DTO drift from backend can break UI; keep response rendering defensive.
- Health endpoint response shape may differ from documented skill DTOs; render raw JSON fallback.

## Security Considerations

- Never log API key.
- Never include API key in URL params.
- Clear API key from memory when user clicks disconnect/clear.

## Next Steps

- Build read-only screens using the client.
