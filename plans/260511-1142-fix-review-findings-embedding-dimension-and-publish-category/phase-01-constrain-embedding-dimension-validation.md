# Constrain Embedding Dimension Validation

## Context Links

- [README](../../README.md)
- [application.properties](../../src/main/resources/application.properties)
- [EmbeddingService.java](../../src/main/java/com/skillgateway/service/EmbeddingService.java)
- [Skill.java](../../src/main/java/com/skillgateway/model/Skill.java)
- [V1__create_skills_schema.sql](../../src/main/resources/db/migration/V1__create_skills_schema.sql)
- [V4__convert_embedding_to_pgvector.sql](../../src/main/resources/db/migration/V4__convert_embedding_to_pgvector.sql)
- [EmbeddingServiceTest.java](../../src/test/java/com/skillgateway/service/EmbeddingServiceTest.java)

## Overview

Priority: P1.
Status: Pending.
Constrain backend config validation to `768` until schema and persistence are made dynamic.

## Key Insights

- Current DB contract is hard-coded in 3 places: Flyway migrations, JPA entity, and runtime expectation from providers.
- Current `EmbeddingService.status()` only rejects non-positive values, so `1536` can look valid while persistence remains `vector(768)`.
- Minimal fix is validation, not dynamic schema support.

## Requirements

- Functional:
  - Reject any embedding dimension other than `768`.
  - Keep default config value `768`.
  - Add regression tests for accepted `768` and rejected non-`768`.
- Non-functional:
  - No schema migration.
  - No new config surface.
  - Error message must explain why value is rejected.

## Architecture

```text
AI_EMBEDDING_DIMENSION
  -> application.properties default/fallback
  -> EmbeddingService.status()
     -> valid only when dimension == 768
  -> provider init / DB write path remains unchanged
```

Failure mode:
- Input: `AI_EMBEDDING_DIMENSION=1536`
- Before fix: config appears valid, later runtime mismatch with `vector(768)`
- After fix: status/config validation fails early with explicit message

## Related Code Files

- Modify: `src/main/java/com/skillgateway/service/EmbeddingService.java`
- Modify: `src/test/java/com/skillgateway/service/EmbeddingServiceTest.java`
- Optional doc/config note only if already used by code comments:
  - `src/main/resources/application.properties`

## Implementation Steps

1. Add a single source constant for fixed embedding dimension in `EmbeddingService` if it keeps code simple.
2. Replace `dimension <= 0` validation with fixed-dimension validation, message naming `768` explicitly.
3. Keep provider selection path unchanged to minimize risk.
4. Extend `EmbeddingServiceTest`:
   - valid config at `768`
   - invalid config at non-`768`
5. Do not touch `Skill` entity or Flyway migrations in this fix.

## Test Matrix

- Unit:
  - `status()` returns configured for valid `768`.
  - `status()` returns not configured for `767` or `1536`.
  - Existing URL/model/provider validation tests still pass.
- Integration:
  - Covered indirectly by `mvn test`; no new DB integration needed for this minimal fix.
- E2E:
  - Out of scope; backend status endpoint behavior is sufficient for this change.

## Risk Assessment

- High likelihood, high impact if skipped: runtime embedding write/search mismatch against `vector(768)`.
  Mitigation: fail fast in config validation with explicit message.
- Low likelihood, medium impact: hidden callers bypass `status()` and call provider path directly.
  Mitigation: keep plan note to check whether startup or publish paths rely on `status()` only; if not, mirror guard in provider selection during implementation review.

## Security Considerations

- No secret handling change.
- Validation message must not echo raw secrets or URLs.

## Rollback

- Revert `EmbeddingService` validation and its tests.
- No DB/data rollback needed.

## Success Criteria

- Any dimension other than `768` is rejected before a misleading "configured" status is reported.
- Tests prove fixed-dimension contract.

## Next Steps

- Run Phase 3 verification after frontend default fix lands.

## Unresolved Questions

- Confirm during implementation whether `selectedProvider()` also needs the same guard for defense in depth.
