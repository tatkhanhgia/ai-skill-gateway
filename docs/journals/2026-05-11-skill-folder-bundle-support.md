# 2026-05-11 - Skill Folder Bundle Support

Implemented first-pass full skill folder bundle support.

## Changes

- Added additive bundle schema: version artifact fields and `skill_version_files`.
- Added zip validation with root `SKILL.md`, path safety, forbidden-file checks, limits, per-file SHA-256, deterministic canonical zip output, and explicit temp cleanup.
- Added local artifact storage with path-root enforcement and cleanup support.
- Added `POST /api/v1/skills/publish-bundle`, file manifest listing, and bundle download with checksum headers.
- Added web console bundle publish mode using multipart `FormData`.
- Updated client integration docs, architecture, code standards, README, and plan statuses.

## Verification

- `mvn test` passed: 40 tests, 0 failures.
- `npm run build --prefix web-ui` passed.
- `node .claude/scripts/validate-docs.cjs docs/` passed.

## Review Notes

Code review found upload-size, artifact consistency, parser, and temp-file risks. Fixed with HTTP body limit, `FileUpload.size()` guard, DB publish before storage copy, storage delete compensation, closeable validated bundles, and explicit rejection of unsupported multiline YAML arrays/block scalars.

## Unresolved Questions

- Add Quarkus resource integration tests for multipart publish/list/download when test DB setup is available.
