# Phase 06: Prepare NPM Release Workflow

## Context Links

- [Plan Overview](./plan.md)
- [Phase 05 - Tests and Local Packaging](./phase-05-validate-tests-and-local-packaging.md)
- [NPM package publishing docs](https://docs.npmjs.com/packages-and-modules/contributing-packages-to-the-registry)

## Overview

- **Priority:** P2
- **Status:** completed
- **Effort:** 2h
- **Description:** Prepare safe npm publishing process, versioning policy, documentation, and release checklist.

## Key Insights

- First release should be conservative: `0.1.0`, manual publish, no automated token in repo.
- npm package name may need availability check.
- Release must audit payload every time.
- Project docs should explain explicit install command and no auto postinstall behavior.

## Requirements

### Functional

- Release checklist for npm publish.
- Versioning policy.
- README quick start and command docs.
- Changelog entry.
- Optional GitHub Actions publish workflow only if user approves token management.

### Non-functional

- No npm token committed.
- No automated publish without explicit release trigger.
- Reproducible build and pack steps.

## Architecture

```text
release flow
   │
   ├─ npm run build
   ├─ npm test
   ├─ npm pack --dry-run --json
   ├─ inspect forbidden files
   ├─ npm pack
   ├─ smoke test tarball
   └─ npm publish --access public
```

## Related Code Files

### Files to create later

- `npm-package/README.md`
- `npm-package/CHANGELOG.md` if package-local changelog is desired
- optional `.github/workflows/publish-gtk-skill.yml`

### Files to modify later

- `docs/project-changelog.md` if present/created later by docs workflow.
- Root `README.md` optional pointer to npm package.

## Implementation Steps

1. Confirm package name availability:
   ```bash
   npm view gtk-skill name
   ```
   If taken, choose scoped package or alternate name.
2. Define semantic versioning:
   - `0.x` while CLI/install manifest format can change.
   - Minor for new asset groups/commands.
   - Patch for bug fixes and asset updates.
3. Add README usage:
   ```bash
   npx gtk-skill@latest install --dry-run
   npx gtk-skill@latest install
   npx gtk-skill@latest doctor
   ```
4. Add safety docs:
   - no postinstall writes.
   - default skip conflicts.
   - use `--backup --overwrite` for controlled replacement.
5. Add release checklist script or docs.
6. Manual publish first:
   ```bash
   npm login
   npm publish --access public
   ```
7. Optional future CI publish:
   - GitHub Actions release tag trigger.
   - NPM token in GitHub secrets only.
   - Requires user approval before implementation.

## Todo List

- [x] Check package name availability
- [x] Define versioning policy
- [x] Write package README usage
- [x] Write safety docs
- [x] Add release checklist
- [x] Add changelog entry
- [x] Run build/test/pack validation
- [x] Smoke test packed tarball
- [ ] Publish manually after user approval
- [x] Decide if CI publish workflow is needed later

## Progress Notes

- Package name check: `npm view gtk-skill name` returned 404, so name appears available at validation time.
- Versioning policy documented: package starts at `0.1.0`; keep `0.x` while CLI/install manifest format can change.
- Package README documents quick start, commands, safety behavior, and release checklist.
- Build/test/pack validation completed: `npm test --prefix npm-package`, `npm pack`, packed tarball smoke test, payload audit.
- Publish dry-run completed: `npm publish --dry-run --access public` reported `+ gtk-skill@0.1.0`.
- Manual publish remains intentionally unexecuted until user approves and npm login is available.
- CI publish workflow deferred; manual first release avoids storing npm token in repo.

## Success Criteria

- Release process documented.
- First publish can be done manually and safely.
- No npm token stored in repo.
- Package README explains install/update/doctor clearly.
- `npm publish --dry-run` passes before real publish.

## Risk Assessment

| Risk | Mitigation |
|---|---|
| Package name unavailable | Use scoped package or alternate name |
| Accidental bad publish | Use `npm publish --dry-run`, tarball smoke test, manual approval |
| Token leak | Manual publish first; CI secrets only if approved |

## Security Considerations

- Never commit npm tokens.
- Verify package payload before publish.
- Do not enable automated publish without protected release trigger.

## Next Steps

- After plan approval, run cook command from overview.
