# Phase 05: Tighten NPM Package Payload

## Context Links

- [Plan Overview](./plan.md)
- [Scout Report](./reports/audit-remediation-scout-report.md)
- [Package JSON](../../npm-package/package.json)
- [Asset Rules](../../npm-package/src/manifest/asset-rules.ts)
- [Walk Files](../../npm-package/src/manifest/walk-files.ts)

## Overview

- **Date:** 2026-05-10
- **Description:** Make `gtk-skill` package generation faster, smaller, and stricter.
- **Priority:** P1
- **Implementation status:** pending
- **Review status:** pending

## Key Insights

- `copy-assets` calls `walkFiles()` before excluding `.venv`/`node_modules`.
- Dry-run package currently includes test/coverage artifacts.
- `.npmignore` excludes some runtime paths, but generated `assets/` already contains artifacts before packing.

## Requirements

- Prune excluded directories during traversal, not after full recursion.
- Exclude test, coverage, cache, and generated local artifacts from assets unless intentionally shipped.
- Keep required runtime skill scripts/references intact.
- Add tests for traversal exclusion and payload audit.

## Architecture

```text
assetRoots -> filtered recursive walker -> assets/ -> manifest -> npm pack audit
```

## Related Code Files

- Modify: `npm-package/src/manifest/walk-files.ts`
- Modify: `npm-package/src/manifest/copy-assets.ts`
- Modify: `npm-package/src/manifest/build-manifest.ts`
- Modify: `npm-package/src/manifest/asset-rules.ts`
- Add/modify: `npm-package/tests/*.test.ts`
- Modify: `npm-package/.npmignore`

## Implementation Steps

1. Extend walker to accept an exclusion predicate and skip directories early.
2. Add exclude rules for:
   - `__tests__`
   - `tests`
   - `*.test.*`
   - `.coverage`
   - `coverage-*`
   - temporary/generated local artifacts
3. Audit whether skill reference test fixtures are needed at runtime; keep only if required.
4. Add tests for excluded traversal and manifest entries.
5. Run `npm test`.
6. Run `npm run pack:dry-run` and inspect forbidden file patterns.

## Todo List

- [ ] Add early-prune walker
- [ ] Tighten exclude rules
- [ ] Add package payload tests
- [ ] Regenerate assets/manifest
- [ ] Run `npm test`
- [ ] Run `npm run pack:dry-run`

## Success Criteria

- Pack payload has no `.coverage`, test suites, `node_modules`, `.venv`, `.env`, session state, or local settings.
- `prepare:assets` completes without traversing excluded dependency trees.
- `npm test` and pack dry-run pass.

## Risk Assessment

- Over-filtering may remove useful skill test fixtures or examples. Mitigation: review manifest diff by asset type.
- Smaller package may alter install expectations. Mitigation: document what is runtime payload vs development-only.

## Security Considerations

- Keep default-deny posture for dotfiles and local state.
- Avoid publishing generated logs, coverage DBs, or credentials.

## Next Steps

- Phase 6: sync docs and release checks.

## Unresolved Questions

- Should package include skill tests for users, or only runtime docs/scripts?
