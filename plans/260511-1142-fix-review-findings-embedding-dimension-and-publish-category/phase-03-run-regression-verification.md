# Run Regression Verification

## Context Links

- [pom.xml](../../pom.xml)
- [web-ui/package.json](../../web-ui/package.json)
- [EmbeddingServiceTest.java](../../src/test/java/com/skillgateway/service/EmbeddingServiceTest.java)
- [ManifestValidatorTest.java](../../src/test/java/com/skillgateway/service/ManifestValidatorTest.java)

## Overview

Priority: P1.
Status: Pending.
Verify both fixes with smallest reliable regression gate set.

## Requirements

- Run backend Maven tests.
- Run web UI build.
- Confirm no new schema/migration work was introduced.

## Implementation Steps

1. Run `mvn test`.
2. Run `npm run build --prefix web-ui`.
3. If a failure appears:
   - classify as pre-existing vs introduced
   - fix only regressions introduced by this plan
4. Record final pass/fail state in plan and changelog/docs only if implementation occurs.

## Test Matrix

- Backend unit/integration:
  - `EmbeddingServiceTest`
  - `ManifestValidatorTest`
  - full `mvn test` suite
- Frontend compile:
  - `npm run build --prefix web-ui`
- Manual smoke if time:
  - publish view preview shows valid default category
  - embedding status reports invalid for non-`768`

## Risk Assessment

- Medium likelihood, low impact: no dedicated UI unit harness means category fallback is validated mainly by build/manual smoke.
  Mitigation: keep change tiny; add a low-cost unit test only if harness is trivial.
- Low likelihood, medium impact: unrelated existing test failures block acceptance.
  Mitigation: distinguish baseline failures from regressions before widening scope.

## Rollback

- No product rollback.
- If verification uncovers broader issues, stop and keep scope limited to these 2 fixes.

## Success Criteria

- `mvn test` passes.
- `npm run build --prefix web-ui` passes.
- No schema/entity drift introduced.

## Unresolved Questions

- None.
