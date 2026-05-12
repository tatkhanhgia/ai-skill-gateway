---
title: "Fix Review Comments: Bundle Path Limit and Embedding Readiness"
description: "Plan two narrow fixes for bundle path length validation and embedding readiness health semantics."
status: pending
priority: P2
effort: 2h
branch: master
tags: [plan, review, quarkus, validation, healthcheck]
created: 2026-05-11
---

# Fix Review Comments

## Scope

Implement 2 review fixes only. No schema change. Use [V5__add_skill_bundle_artifacts.sql](../../src/main/resources/db/migration/V5__add_skill_bundle_artifacts.sql) as source of truth for `skill_version_files.path VARCHAR(512)`.

## Data Flow

- Bundle upload path: zip entry name -> `SkillBundleValidator.normalizePath()` -> validated manifest file list -> DB insert into `skill_version_files.path`.
- Readiness path: `EmbeddingProviderHealthCheck.call()` -> `EmbeddingService.status()` -> `configured()` flag -> readiness `UP` or `DOWN` + sanitized diagnostic data.

## Plan

| Phase | Files | Change | Dependencies | Risk | Rollback |
|---|---|---|---|---|---|
| 1 | [SkillBundleValidator.java](../../src/main/java/com/skillgateway/service/SkillBundleValidator.java), [SkillBundleValidatorTest.java](../../src/test/java/com/skillgateway/service/SkillBundleValidatorTest.java) | Add validator guard rejecting normalized paths longer than 512 chars before manifest file metadata is built. Add positive boundary test at 512 and negative test at 513+. | Uses V5 column limit only. No blocker from phase 2. | Medium: off-by-one or checking raw instead of normalized path. Mitigation: test exact boundary after slash normalization. | Revert validator branch and new tests only. No data migration needed. |
| 2 | [EmbeddingProviderHealthCheck.java](../../src/main/java/com/skillgateway/config/EmbeddingProviderHealthCheck.java), [EmbeddingProviderHealthCheckTest.java](../../src/test/java/com/skillgateway/config/EmbeddingProviderHealthCheckTest.java) | Change readiness to `DOWN` when `status.configured()` is `false`, preserve current non-secret response data, update test expectation and add `UP` case if absent. | Independent of phase 1. | Low: accidentally dropping existing response fields. Mitigation: assert status plus key data fields remain present. | Revert health check condition and tests only. |
| 3 | Same test files | Run targeted tests, then compile/full test pass if fast enough. | After phases 1-2. | Low: hidden coupling outside targeted tests. Mitigation: follow targeted run with broader Maven verification. | Revert only if verification shows regression tied to these edits. |

## Backward Compatibility

- DB schema unchanged; validation now fails earlier instead of letting oversized paths reach persistence.
- Health endpoint contract stays same except readiness status now matches misconfigured embedding provider state.

## Test Matrix

- Unit: reject normalized file path length `> 512`; accept exactly `512`; preserve existing traversal/forbidden path behavior.
- Unit: readiness `DOWN` when `configured=false`; readiness `UP` when `configured=true`; response data still excludes secrets.
- Integration smoke: `mvn test` for repo-wide regressions if targeted tests pass.

## Verification Commands

```powershell
mvn -Dtest=SkillBundleValidatorTest,EmbeddingProviderHealthCheckTest test
mvn -DskipTests compile
mvn test
```

## Success Criteria

- Any normalized bundle path longer than 512 chars throws `ValidationException`.
- A normalized path of exactly 512 chars remains valid if other rules pass.
- Readiness health returns `DOWN` when embedding config is invalid or incomplete and `configured()` is `false`.
- Existing sanitized health response fields remain present.

## Unresolved Questions

- None.
