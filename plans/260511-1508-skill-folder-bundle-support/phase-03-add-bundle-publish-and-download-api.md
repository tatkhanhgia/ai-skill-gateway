# Phase 03 - Add Bundle Publish and Download API

## Context Links

- [Plan](./plan.md)
- [Phase 02](./phase-02-implement-bundle-validation-and-artifact-storage.md)
- Current resource: `src/main/java/com/skillgateway/api/SkillResource.java`
- API client docs: `docs/client-agent-integration-guide.md`

## Overview

Priority: P1. Status: Completed. Expose bundle publish, file list, and download endpoints while keeping current API stable.

## Key Insights

- `SkillResource.java` is already 120 lines; adding multipart/download there risks file bloat.
- A dedicated `SkillBundleResource` keeps concerns clean.
- Protected publish auth should apply to bundle publish too.

## Requirements

- Add protected multipart publish endpoint.
- Add public or protected download endpoint according to existing GET-public contract.
- Add file manifest endpoint.
- Keep duplicate version behavior identical.
- Return clear JSON errors for validation failures.

## Architecture

```text
SkillBundleResource
  POST /api/v1/skills/publish-bundle
  GET  /api/v1/skills/{name}/versions/{version}/files
  GET  /api/v1/skills/{name}/versions/{version}/bundle
      -> SkillBundleService
      -> SkillService publish metadata
      -> repositories + storage
```

## Related Code Files

Modify:
- `src/main/java/com/skillgateway/config/ApiKeyFilter.java`
- `src/main/java/com/skillgateway/service/SkillService.java`
- `src/main/java/com/skillgateway/repository/SkillVersionRepository.java`
- `src/main/java/com/skillgateway/api/GlobalExceptionMapper.java` if error mapping lacks multipart validation support

Create:
- `src/main/java/com/skillgateway/api/SkillBundleResource.java`
- Request/response DTOs under `src/main/java/com/skillgateway/model/dto/`

## Implementation Steps

1. Add `publish-bundle` to API-key protected mutating endpoint list.
2. Implement multipart resource with Quarkus REST-supported upload binding.
3. Publish metadata and artifact in correct order; avoid orphan artifact on DB failure where feasible.
4. Persist file manifest rows after version exists.
5. Add file listing endpoint sorted by path.
6. Add bundle download endpoint with `Content-Disposition`, `Content-Length`, checksum headers if simple.
7. Keep old `/publish` untouched except shared service extraction if needed.

## Todo List

- [x] Add resource.
- [x] Wire auth filter.
- [x] Add repository/service methods.
- [ ] Add API tests.

## Success Criteria

- Existing JSON publish tests still pass.
- Bundle publish succeeds for valid zip.
- Duplicate version fails.
- Downloaded bundle hash equals stored hash.

## Risk Assessment

- Risk: multipart binding mismatch. Mitigation: spike with smallest Quarkus REST test before broad wiring.

## Security Considerations

- Bundle publish must require API key.
- Download should never expose local filesystem paths.

## Next Steps

- Phase 04 extends web UI.

## Unresolved Questions

- Should bundle download be public like other GET endpoints, or require API key? Default plan: public.
