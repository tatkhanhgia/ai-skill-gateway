# Phase 04: Implement CLI Commands and UX

## Context Links

- [Plan Overview](./plan.md)
- [Phase 03 - Installer Core](./phase-03-implement-installer-core.md)

## Overview

- **Priority:** P1
- **Status:** completed
- **Effort:** 3h
- **Description:** Build user-facing CLI commands around installer core with concise output and safe defaults.

## Key Insights

- CLI must work well in both interactive and CI/non-interactive contexts.
- Default command should be predictable: install all curated assets, skip conflicts.
- Users need visibility before writes: `--dry-run` first-class.

## Requirements

### Functional

Commands:

```bash
gtk-skill install [options]
gtk-skill list [options]
gtk-skill doctor [options]
gtk-skill update [options]
gtk-skill version
```

Install options:

```bash
--cwd <path>
--dry-run
--claude
--opencode
--skills
--agents
--hooks
--rules
--scripts
--overwrite
--backup
--yes
--json
```

### Non-functional

- Exit code `0` on success/no-op.
- Exit code `1` on conflicts/errors unless dry-run.
- Machine-readable `--json` output.
- No noisy logs by default.

## Architecture

```text
bin/gtk-skill.js
   │
   ▼
src/cli.ts
   ├─ install command → installer core
   ├─ list command → manifest reader
   ├─ doctor command → installed state checker
   ├─ update command → installer core with installed state awareness
   └─ version command → package metadata
```

## Related Code Files

### Files to create later

- `npm-package/src/cli.ts`
- `npm-package/src/commands/install.ts`
- `npm-package/src/commands/list.ts`
- `npm-package/src/commands/doctor.ts`
- `npm-package/src/commands/update.ts`
- `npm-package/src/output/render-summary.ts`
- `npm-package/src/output/render-json.ts`

## Implementation Steps

1. Choose CLI parser: recommend `commander` for simple command definitions.
2. Implement `install` command:
   - Parse options.
   - Build installer request.
   - Render summary.
   - If conflicts and no overwrite: exit 1 with instruction.
3. Implement `list` command:
   - Show packaged asset groups and counts.
   - Support `--json`.
4. Implement `doctor` command:
   - Read `.gtk-skill/install-manifest.json`.
   - Compare installed files against current package manifest.
   - Report missing, changed, unchanged, unknown.
5. Implement `update` command:
   - Similar to install.
   - Uses prior install manifest to distinguish package-managed files vs user-created files.
   - Default: update only unchanged package-managed files, skip locally modified files.
6. Implement output renderer:
   ```text
   gtk-skill install
   Target: C:\project
   Created: 120
   Unchanged: 40
   Conflicts: 3
   Next: rerun with --backup --overwrite to replace conflicts safely
   ```
7. Implement `--json` output for automation.
8. Add help examples.

## Todo List

- [x] Choose CLI parser
- [x] Implement command registration
- [x] Implement install command
- [x] Implement list command
- [x] Implement doctor command
- [x] Implement update command
- [x] Implement version command
- [x] Implement text summary output
- [x] Implement JSON output
- [x] Define exit codes
- [x] Add help examples

## Progress Notes

- User-facing CLI commands implemented around installer core.
- Error handling tightened so failing command paths return exit code `1`.
- Safe default behavior kept: non-project dirs rejected without explicit override; no writes during dry-run.

## Success Criteria

- `gtk-skill --help` shows commands and examples.
- `gtk-skill install --dry-run` works from any project folder.
- `gtk-skill list` shows asset groups/counts.
- `gtk-skill doctor` detects missing/changed installed files.
- `gtk-skill update` does not overwrite local modifications by default.
- `--json` returns valid JSON.

## Risk Assessment

| Risk | Mitigation |
|---|---|
| CLI too complex | Keep first release commands small and explicit |
| Interactive prompts break CI | Require `--yes`; support non-interactive defaults |
| Ambiguous flags | Group docs and examples clearly |

## Security Considerations

- CLI should never run installed hooks/scripts automatically.
- Require explicit overwrite flags for destructive operations.
- JSON output must not include sensitive file contents.

## Next Steps

- Phase 5: tests and local packaging validation.
