# Phase 01 - Define Bundle Contract and Schema

## Context Links

- [Plan](./plan.md)
- [Brainstorm Summary](./reports/brainstorm-summary.md)
- Backend DTO: `src/main/java/com/skillgateway/model/dto/SkillManifest.java`
- Current schema: `src/main/resources/db/migration/V1__create_skills_schema.sql`
- NPM manifest pattern: `npm-package/src/types.ts`

## Overview

Priority: P1. Status: Completed. Define minimal durable data contract for skill bundles without breaking existing metadata-only publish.

## Key Insights

- Skill folder identity comes from `SKILL.md` frontmatter.
- Version artifact must be immutable once published.
- Search and install concerns should be separate.

## Requirements

- Add a bundle metadata model for a skill version.
- Add per-file manifest fields: path, sha256, size, media type, role.
- Add config for local artifact root and upload limits.
- Preserve current `SkillManifest` endpoint.

## Architecture

```text
SkillManifest metadata
  -> skills + skill_versions
Bundle artifact summary
  -> skill_versions columns
Per-file manifest
  -> skill_version_files rows
Artifact bytes
  -> local filesystem via storage adapter
```

## Related Code Files

Modify:
- `src/main/java/com/skillgateway/model/SkillVersion.java`
- `src/main/resources/application.properties`
- `docs/system-architecture.md`
- `docs/code-standards.md`

Create:
- `src/main/resources/db/migration/V5__add_skill_bundle_artifacts.sql`
- `src/main/java/com/skillgateway/model/SkillVersionFile.java`
- `src/main/java/com/skillgateway/repository/SkillVersionFileRepository.java`
- DTOs under `src/main/java/com/skillgateway/model/dto/`

## Implementation Steps

1. Define artifact fields on version: `packageFormat`, `entrypointPath`, `bundleSha256`, `bundleSize`, `fileCount`, `artifactUri`.
2. Define `skill_version_files` table keyed to `skill_versions(id)`.
3. Add indexes for version lookup and file path lookup.
4. Add upload limits to config with conservative defaults.
5. Document schema contract.

## Todo List

- [x] Draft migration.
- [x] Add entity/repository/DTO contract.
- [x] Add config keys.
- [x] Update architecture docs.

## Success Criteria

- Migration is additive and rollback-safe.
- Existing rows remain valid with null bundle fields.
- No current API response breaks.

## Risk Assessment

- Risk: schema overdesign. Mitigation: only store summary and file manifest fields needed by API/install.

## Security Considerations

- Do not store secrets or raw file content in searchable fields.
- File paths must be normalized before persistence.

## Next Steps

- Phase 02 uses this schema to validate and store artifacts.

## Unresolved Questions

- None.
