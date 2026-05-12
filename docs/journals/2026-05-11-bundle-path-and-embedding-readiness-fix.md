# Bundle Path and Embedding Readiness Fix

## Summary
- Fixed two review findings in bundle validation and embedding readiness.
- `SkillBundleValidator` now rejects normalized bundle paths longer than 512 chars, matching `skill_version_files.path VARCHAR(512)`.
- `EmbeddingProviderHealthCheck` now reports failed readiness when embedding status has `configured=false`.

## Tests
- Added bundle path boundary coverage for 512 accepted and 513 rejected.
- Added embedding health coverage for valid and invalid readiness states.
- Passed `mvn -q "-Dtest=SkillBundleValidatorTest,EmbeddingProviderHealthCheckTest" test`.
- Passed `mvn -q -DskipTests compile`.
- Passed `mvn -q test`.

## Notes
- Tester and journal subagents failed before work due missing provider credentials.
- Code reviewer subagent completed, no issues found.

## Unresolved Questions
- None.
