# Align Publish Category Defaults

## Context Links

- [ManifestValidator.java](../../src/main/java/com/skillgateway/service/ManifestValidator.java)
- [ManifestValidatorTest.java](../../src/test/java/com/skillgateway/service/ManifestValidatorTest.java)
- [publish-payload-normalizer.ts](../../web-ui/src/lib/publish-payload-normalizer.ts)
- [publish-view.tsx](../../web-ui/src/features/publish/publish-view.tsx)
- [web-ui/package.json](../../web-ui/package.json)

## Overview

Priority: P1.
Status: Pending.
Replace default publish category `general` with a category accepted by backend validation.

## Key Insights

- Backend already owns source-of-truth allowed categories.
- UI currently injects invalid data in 2 places: form default state and payload normalizer fallback.
- Minimal safe fix is to choose one valid default category and keep free-text editing behavior unchanged.

## Requirements

- Functional:
  - Form mode default category must be backend-valid.
  - JSON/markdown normalization fallback category must be backend-valid.
  - Default publish flow must succeed without manual category edit.
- Non-functional:
  - No backend category expansion.
  - No new category picker component unless needed later.

## Architecture

```text
Form default / JSON fallback / SKILL.md fallback
  -> normalizePublishPayload()
  -> payload preview
  -> publish request
  -> ManifestValidator allowed-categories check
```

Failure mode:
- Input missing category
- Before fix: UI injects `general` -> backend rejects
- After fix: UI injects allowed default -> backend accepts

## Related Code Files

- Modify: `web-ui/src/lib/publish-payload-normalizer.ts`
- Modify: `web-ui/src/features/publish/publish-view.tsx`
- Optional if implementation chooses cheap unit coverage:
  - add/modify web UI test file for normalizer if test harness already exists
- Optional backend regression:
  - `src/test/java/com/skillgateway/service/ManifestValidatorTest.java` to keep allowed category expectation explicit

## Implementation Steps

1. Pick one existing allowed category as default; prefer `utility` or `ai`, but keep same value in all UI entry paths.
2. Replace `general` in `publish-payload-normalizer.ts`.
3. Replace `general` in `publish-view.tsx` form seed state.
4. If low-cost, add/extend a test asserting chosen category is accepted by `ManifestValidator` and/or normalizer fallback emits that value.
5. Do not add new backend categories in this fix.

## Test Matrix

- Unit:
  - `ManifestValidator` accepts chosen default category.
  - If web UI test harness exists later, `normalizePublishPayload({})` uses chosen default.
- Integration:
  - `npm run build --prefix web-ui` ensures TS compile path stays valid.
- E2E:
  - Manual smoke acceptable: open publish view, leave category untouched, preview shows allowed category.

## Risk Assessment

- High likelihood, medium impact if skipped: default publish path fails for first-time UI users.
  Mitigation: replace both default injection points in same change.
- Low likelihood, low impact: future backend category list changes drift again.
  Mitigation: document follow-up option to centralize allowed categories later; do not expand scope now.

## Security Considerations

- No auth/API key behavior change.
- No user data persistence change.

## Rollback

- Revert two UI default changes and any added tests.

## Success Criteria

- Default publish payload category is backend-valid in form, JSON, and markdown-assisted flows.
- Build passes with no TS errors.

## Next Steps

- Run Phase 3 verification.

## Unresolved Questions

- Choose final default category during implementation; keep it consistent across all UI paths.
