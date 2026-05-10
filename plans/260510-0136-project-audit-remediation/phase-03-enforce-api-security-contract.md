# Phase 03: Enforce API Security Contract

## Context Links

- [Plan Overview](./plan.md)
- [Scout Report](./reports/audit-remediation-scout-report.md)
- [ApiKeyFilter](../../src/main/java/com/skillgateway/config/ApiKeyFilter.java)
- [PDR](../../docs/project-overview-pdr.md)

## Overview

- **Date:** 2026-05-10
- **Description:** Make API key behavior explicit and covered by tests.
- **Priority:** P1
- **Implementation status:** pending
- **Review status:** pending

## Key Insights

- Docs say every API request passes API key enforcement.
- Code only protects selected POST endpoints.
- Public GET catalog/search can be valid product behavior, but docs/tests must say so.

## Requirements

- Decide API auth policy.
- Enforce selected policy in `ApiKeyFilter`.
- Add tests for protected and public endpoints.
- Update docs and curl examples.

## Architecture

```text
ContainerRequestFilter -> auth policy -> resource method or 401
```

## Related Code Files

- Modify: `src/main/java/com/skillgateway/config/ApiKeyFilter.java`
- Add/modify: tests under `src/test/java/com/skillgateway`
- Modify: `README.md`
- Modify: `docs/project-overview-pdr.md`
- Modify: `docs/code-standards.md`
- Modify: `docs/system-architecture.md`

## Implementation Steps

1. Choose policy:
   - Option A: protect all `/api/v1/**` endpoints.
   - Option B: keep GET public, protect write/MCP endpoints.
2. Implement policy with path matching that is simple and testable.
3. Normalize header naming in docs: `X-API-Key`.
4. Add tests for missing, wrong, and valid key.
5. Run `mvn test`.

## Todo List

- [ ] Choose auth policy
- [ ] Update `ApiKeyFilter`
- [ ] Add auth tests
- [ ] Update README/docs examples
- [ ] Run `mvn test`

## Success Criteria

- Code and docs describe same auth model.
- Missing/wrong key behavior is tested.
- No protected write endpoint bypass remains.

## Risk Assessment

- Protecting all GET endpoints may break clients relying on public catalog. Mitigation: document and version behavior.
- Keeping GET public may conflict with privacy-first positioning. Mitigation: docs must be explicit.

## Security Considerations

- Avoid default production API key.
- Prefer requiring `AUTH_API_KEY` outside dev/test if feasible.
- Use constant-time comparison only if threat model warrants it; API key is simple shared secret for local use.

## Next Steps

- Phase 4: resolve MCP scope drift.

## Unresolved Questions

- Should search/list/detail GET endpoints be public?
