# Phase 06 - Test and Verification Pass

## Context Links

- [Plan](./plan.md)
- Existing tests: `src/test/java/com/skillgateway/service/`
- Web UI package: `web-ui/package.json`
- NPM package tests: `npm-package/tests/`

## Overview

Priority: P1. Status: Completed. Prove bundle support works and does not regress existing metadata publish/search flows.

## Requirements

- Backend unit tests for validator/storage.
- Backend API tests for publish/list/download.
- Existing service tests still pass.
- Frontend build passes.
- NPM tests run if docs/types touch installer package.

## Test Matrix

| Area | Cases |
|---|---|
| Validator | valid bundle, missing `SKILL.md`, bad frontmatter, traversal, duplicate paths, forbidden files, max size |
| Storage | save, read, hash match, missing artifact |
| API | auth required for publish, duplicate version, file list, bundle download |
| Regression | JSON publish, search/list/detail, versions |
| UI | TypeScript build, API client compile |

## Related Code Files

Modify/create tests:
- `src/test/java/com/skillgateway/service/SkillBundleValidatorTest.java`
- `src/test/java/com/skillgateway/service/SkillBundleServiceTest.java`
- API/resource tests if current test setup supports REST-assured endpoints
- `web-ui` tests only if existing test harness exists

## Implementation Steps

1. Add small test bundle builders in test code.
2. Cover security validation first.
3. Cover happy path publish and download.
4. Run `mvn test`.
5. Run `npm run build --prefix web-ui`.
6. Run `npm test --prefix npm-package` if npm package files changed.
7. Update plan status/checklists after implementation.

## Todo List

- [x] Backend validation tests.
- [x] Backend API/storage tests.
- [x] Frontend build.
- [x] Optional npm tests not run; npm package files unchanged.
- [x] Final docs validation.

## Success Criteria

- All relevant commands pass.
- No failing tests ignored.
- Test output confirms old and new publish paths.

## Risk Assessment

- Risk: integration tests need database setup. Mitigation: keep core validation/storage tests independent, add repository tests only where existing Quarkus test infra supports it.

## Security Considerations

- Security rejection cases are required, not optional.

## Next Steps

- Ready for code review after tests pass.

## Unresolved Questions

- None.
