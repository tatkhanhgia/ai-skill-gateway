# Phase 06: Sync Docs and Release Checks

## Context Links

- [Plan Overview](./plan.md)
- [Scout Report](./reports/audit-remediation-scout-report.md)
- [README](../../README.md)
- [Docs](../../docs)
- [Package README](../../npm-package/README.md)

## Overview

- **Date:** 2026-05-10
- **Description:** Update documentation and run final verification after code fixes.
- **Priority:** P1
- **Implementation status:** pending
- **Review status:** pending

## Key Insights

- README links to missing `PLAN.md` and `docs/api-reference.md`.
- Docs claim Java 21/MCP/security/vector behavior that may not match final implementation.
- Documentation management rules require roadmap/changelog/architecture/code standards updates after significant fixes.

## Requirements

- Remove or create missing doc targets.
- Update architecture/PDR/code standards to match final decisions.
- Add changelog/roadmap entries if those docs exist or create them per project standards.
- Run full verification before marking plan complete.

## Architecture

Docs should describe implemented behavior:

```text
README quick start -> API docs -> architecture/PDR -> code standards -> changelog/roadmap
```

## Related Code Files

- Modify: `README.md`
- Modify: `docs/project-overview-pdr.md`
- Modify: `docs/codebase-summary.md`
- Modify: `docs/system-architecture.md`
- Modify: `docs/code-standards.md`
- Create/modify: `docs/api-reference.md` if kept
- Create/modify: `docs/development-roadmap.md`
- Create/modify: `docs/project-changelog.md`

## Implementation Steps

1. Update README quick start, ports, Java version, API key examples, and docs links.
2. Update docs to reflect final MCP and auth scope.
3. Add API reference or remove stale link.
4. Update roadmap/changelog with audit remediation summary.
5. Run:
   - `mvn test`
   - `npm test` in `npm-package/`
   - `npm run pack:dry-run` in `npm-package/`
   - docs validation if available: `node .claude/scripts/validate-docs.cjs docs/`
6. Record final evidence in plan progress notes.

## Todo List

- [ ] Update README
- [ ] Update architecture/PDR/code standards
- [ ] Fix missing docs links
- [ ] Update roadmap/changelog
- [ ] Run Java tests
- [ ] Run npm tests
- [ ] Run pack dry-run audit
- [ ] Run docs validation if available

## Success Criteria

- Docs no longer contradict code.
- Missing README doc links resolved.
- Verification commands pass.
- Plan can be marked complete with evidence.

## Risk Assessment

- Docs may drift if final phase decisions change. Mitigation: update docs after code, not before.
- Validation script may flag legacy docs unrelated to this task. Mitigation: fix relevant issues, report unrelated ones.

## Security Considerations

- Do not document real secrets.
- Ensure examples use placeholders and dev-only warnings where needed.

## Next Steps

- Request final code review.
- Ask user whether to commit changes.

## Unresolved Questions

- Should `docs/api-reference.md` be created now or should README point to existing docs only?
