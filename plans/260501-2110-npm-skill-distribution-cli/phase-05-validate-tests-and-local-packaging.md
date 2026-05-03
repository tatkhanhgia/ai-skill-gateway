# Phase 05: Validate Tests and Local Packaging

## Context Links

- [Plan Overview](./plan.md)
- [Phase 03 - Installer Core](./phase-03-implement-installer-core.md)
- [Phase 04 - CLI Commands](./phase-04-implement-cli-commands-and-ux.md)

## Overview

- **Priority:** P1
- **Status:** completed
- **Effort:** 3h
- **Description:** Add automated tests and local npm package validation to prove install/update safety before publish.

## Key Insights

- Installer behavior is filesystem-heavy; tests need real temp directories, not mocks only.
- `npm pack` is the closest local validation for actual publish payload.
- Windows path behavior must be tested because primary user environment is Windows 11.

## Requirements

### Functional

- Unit tests for manifest validation, path safety, conflict policy, checksum compare.
- Integration tests using temp project directories.
- Local `npm pack` validation.
- Smoke test CLI via packed tarball.
- Package content audit to ensure excluded files are absent.

### Non-functional

- Tests run on Windows.
- No network required.
- Tests clean up temp dirs.
- Test output concise.

## Architecture

```text
node --test
├── unit
│   ├── manifest validation
│   ├── path traversal rejection
│   ├── conflict policy
│   └── checksum compare
└── integration
    ├── install dry-run
    ├── install into temp project
    ├── conflict skip default
    ├── backup overwrite
    ├── doctor detects changes
    └── npm pack payload audit
```

## Related Code Files

### Files to create later

- `npm-package/tests/manifest.test.ts`
- `npm-package/tests/installer.test.ts`
- `npm-package/tests/cli.test.ts`
- `npm-package/tests/package-payload.test.ts`
- `npm-package/tests/helpers/temp-project.ts`

### Files to modify later

- `npm-package/package.json`
- CI workflow if release automation is added later.

## Implementation Steps

1. Configure TypeScript test strategy:
   - Option A: compile then run `node --test dist-tests`.
   - Option B: use `tsx --test` if dependency acceptable.
   - Recommend Option A for fewer runtime dependencies.
2. Unit test manifest reader:
   - rejects absolute target path.
   - rejects `..` traversal.
   - accepts valid `.claude/...` and `.opencode/...` paths.
3. Unit test operation planner:
   - missing file => create.
   - same checksum => skip unchanged.
   - changed existing file => conflict by default.
   - changed + overwrite => overwrite.
   - changed + backup => backup path generated.
4. Integration test install:
   - create temp project.
   - run installer with sample fixture manifest/assets.
   - assert files copied.
   - assert install manifest written.
5. Integration test CLI:
   - run `node bin/gtk-skill.js install --dry-run --cwd <temp>`.
   - assert exit code and output.
6. Package validation:
   - run `npm pack --dry-run --json`.
   - assert required package files included.
   - assert forbidden patterns absent.
7. Smoke test tarball:
   - `npm pack`.
   - install tarball into temp project.
   - run `npx gtk-skill install --dry-run`.
8. Document manual validation checklist.

## Todo List

- [x] Define test runner approach
- [x] Add unit tests for manifest safety
- [x] Add unit tests for conflict policy
- [x] Add unit tests for checksum compare
- [x] Add integration test for dry-run
- [x] Add integration test for install
- [x] Add integration test for conflict skip
- [x] Add integration test for backup overwrite
- [x] Add doctor/update tests
- [x] Add npm pack payload audit
- [x] Add packed tarball smoke test

## Progress Notes

- `npm test --prefix npm-package` passed with 10 tests.
- `node npm-package\bin\gtk-skill.js install --dry-run --cwd npm-package` passed.
- `npm pack` dry-run forbidden-file audit passed with payload count 2440 files.
- Packed tarball smoke test passed: `npm pack` from `npm-package`, install `gtk-skill-0.1.0.tgz` into temp project, run packaged `bin/gtk-skill.js install --dry-run --cwd <temp>`.
- Real install validation passed in temp project with `--skills`: created 2230 files and wrote `.gtk-skill/install-manifest.json`.
- Conflict-skip validation passed: modifying an installed `SKILL.md` produced 1 conflict and non-zero exit without overwrite.
- Backup-overwrite validation passed: `--backup --overwrite` replaced the changed file and created `.gtk-skill/backups/`.

## Success Criteria

- Test suite passes locally on Windows.
- `npm pack --dry-run --json` contains expected files only.
- Packed tarball can be installed into temp project.
- CLI works from packed tarball.
- Forbidden files are absent from package payload.

## Risk Assessment

| Risk | Mitigation |
|---|---|
| Tests brittle due to real repo asset changes | Use small fixtures for logic, one payload audit for real assets |
| Tarball smoke test slow | Keep as pre-release script, not every unit test run |
| Windows file locks | Close handles, use temp dirs, retry cleanup only in tests |

## Security Considerations

- Tests must verify path traversal rejection.
- Tests must verify no `.env`, `.venv`, `node_modules`, or session files ship.
- Tests should not execute distributed hook/script assets.

## Next Steps

- Phase 6: prepare npm release workflow.
