# 2026-05-11 - Bundle Publish IO Rollback Fix

Fixed bundle publish consistency when artifact storage fails with `IOException`.

## Root Cause

`SkillBundleService.publish()` persisted skill/version metadata before calling artifact storage. Storage failures throw checked `IOException`, and Jakarta transactions do not roll back checked exceptions by default.

## Change

- Added `@Transactional(rollbackOn = IOException.class)` to bundle publish.
- Added a regression test that asserts the rollback contract stays configured.

## Verification

- `mvn test` passed: 41 tests, 0 failures.
- `mvn -q -DskipTests compile` passed.

## Unresolved Questions

- None.
