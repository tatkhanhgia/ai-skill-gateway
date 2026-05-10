# Phase 04: Resolve MCP Scope Drift

## Context Links

- [Plan Overview](./plan.md)
- [Scout Report](./reports/audit-remediation-scout-report.md)
- [pom.xml](../../pom.xml)
- [MCP Backup Handlers](../../mcp-tools-backup)
- [System Architecture](../../docs/system-architecture.md)

## Overview

- **Date:** 2026-05-10
- **Description:** Decide whether MCP server support is in current release; restore it or remove/defer claims.
- **Priority:** P2
- **Implementation status:** pending
- **Review status:** pending

## Key Insights

- Quarkus MCP server dependency is commented out due to compatibility.
- MCP handler files exist in `mcp-tools-backup/`, not compiled source.
- Docs call this a Java MCP skill gateway.

## Requirements

- Pick one path:
  - Restore MCP server and handlers.
  - Defer MCP support and update docs/product scope.
- If restored, compile and test handler registration.
- If deferred, remove misleading docs and keep REST gateway as current scope.

## Architecture

Restore path:

```text
MCP HTTP endpoint -> Tool handlers -> Skill/Search/Version services
```

Defer path:

```text
REST API only now -> MCP integration noted as roadmap
```

## Related Code Files

- Modify: `pom.xml`
- Move/modify: `mcp-tools-backup/*.java` if restoring
- Modify: `docs/*.md`
- Modify: `README.md`

## Implementation Steps

1. Check current Quarkus/MCP dependency compatibility.
2. Decide restore vs defer based on time and risk.
3. Restore path:
   - Re-enable compatible dependency.
   - Move handlers under `src/main/java/com/skillgateway/mcp/tools/`.
   - Wire service injection and tests/smoke check.
4. Defer path:
   - Keep backup files or archive with clear note.
   - Update docs to say REST gateway now, MCP planned.
5. Run `mvn test`.

## Todo List

- [ ] Decide restore or defer
- [ ] Update dependency/docs accordingly
- [ ] Compile with chosen scope
- [ ] Add smoke test or explicit roadmap note
- [ ] Run `mvn test`

## Success Criteria

- No docs claim compiled MCP tools if not present.
- If MCP restored, app compiles and exposes expected MCP endpoint/tool metadata.
- `mcp-tools-backup` status is intentional.

## Risk Assessment

- Restoring beta MCP extension may cause dependency conflicts. Mitigation: defer if compatibility cost is high.
- Deferring MCP may reduce product promise. Mitigation: clear roadmap item.

## Security Considerations

- MCP endpoint must share API key policy from Phase 3.
- Tool handlers should not expose sensitive local file paths or config values.

## Next Steps

- Phase 5: npm package payload cleanup.

## Unresolved Questions

- Is MCP required for next release, or acceptable as roadmap?
