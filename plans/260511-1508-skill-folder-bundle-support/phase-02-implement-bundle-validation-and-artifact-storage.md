# Phase 02 - Implement Bundle Validation and Artifact Storage

## Context Links

- [Plan](./plan.md)
- [Phase 01](./phase-01-define-bundle-contract-and-schema.md)
- Existing validator: `src/main/java/com/skillgateway/service/ManifestValidator.java`
- NPM exclusion rules: `npm-package/src/manifest/asset-rules.ts`

## Overview

Priority: P1. Status: Completed. Validate uploaded skill archive safely, build file manifest, and store canonical artifact.

## Key Insights

- Zip handling is the main security boundary.
- Validation should happen before any durable write.
- Storage should be interface-based but local-first.

## Requirements

- Accept zip input as stream/file.
- Require root `SKILL.md`.
- Parse frontmatter enough to map to `SkillManifest`.
- Reject unsafe paths and forbidden files.
- Hash every file and whole artifact.
- Store artifact under content-addressed local path.

## Architecture

```text
Upload stream
  -> temp workspace
  -> safe extraction + limits
  -> SKILL.md frontmatter parse
  -> file manifest build
  -> canonical zip
  -> SkillArtifactStorage.save()
```

## Related Code Files

Modify:
- `src/main/java/com/skillgateway/service/ManifestValidator.java`
- `src/main/resources/application.properties`

Create:
- `src/main/java/com/skillgateway/service/SkillBundleService.java`
- `src/main/java/com/skillgateway/service/SkillBundleValidator.java`
- `src/main/java/com/skillgateway/service/SkillArtifactStorage.java`
- `src/main/java/com/skillgateway/service/LocalSkillArtifactStorage.java`
- `src/main/java/com/skillgateway/model/dto/SkillBundleManifest.java`
- `src/main/java/com/skillgateway/model/dto/SkillBundleFileInfo.java`

## Implementation Steps

1. Implement path normalization: reject absolute paths, drive prefixes, `..`, empty paths, duplicate normalized paths.
2. Reject forbidden basenames/segments: `.env`, `.env.*`, `node_modules`, `.git`, `.venv`, logs, temp files.
3. Enforce max bytes, max files, and max individual file bytes while extracting.
4. Require exactly one root `SKILL.md`; parse YAML frontmatter with simple controlled parser or Jackson YAML if dependency justified.
5. Reuse `ManifestValidator` after converting frontmatter to `SkillManifest`.
6. Build canonical zip with deterministic ordering and compute sha256.
7. Store under configured root by skill name/version/sha.

## Todo List

- [x] Build validator.
- [x] Build storage adapter.
- [x] Add bundle manifest DTOs.
- [x] Add unit tests for unsafe archive cases.

## Success Criteria

- Malicious archive never writes outside temp/storage root.
- Missing/bad `SKILL.md` fails with validation error.
- Stored artifact hash matches manifest.

## Risk Assessment

- Risk: reading large archive into memory. Mitigation: stream and enforce limits during extraction.
- Risk: YAML parser complexity. Mitigation: support required frontmatter fields first; add dependency only if needed.

## Security Considerations

- Clean temp directories.
- Do not execute bundled scripts.
- Treat all bundle content as data.

## Next Steps

- Phase 03 wires service into API and persistence transaction.

## Unresolved Questions

- None.
