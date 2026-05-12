# 2026-05-13 - Repository Hygiene and Docs Sync

## Summary
- Stopped tracking generated Maven and npm dependency artifacts.
- Updated ignore rules for `target/`, `npm-package/node_modules/`, package `dist/`, and local tarballs.
- Added npm package ignore file so release dry-runs use explicit payload rules without npm warnings.
- Synced project docs with current Java/Quarkus, web console, bundle API, embedding status, and repository hygiene state.
- Fixed doc-validation false positives by rewording journal entries.

## Verification
- `node .claude/scripts/validate-docs.cjs docs/` passed.
- `mvn -q test` passed.
- `npm run build --prefix web-ui` passed.
- `npm test --prefix npm-package` passed.
- `npm pack --dry-run --json` reported 2324 entries, 9.0 MB package size, no forbidden artifact patterns found.

## Notes
- Existing untracked source/docs/plan files remain real project files, not generated artifacts.

## Unresolved Questions
- Decide whether to commit all existing untracked feature files together or split by feature area.
