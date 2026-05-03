# Phase 03: Implement Installer Core

## Context Links

- [Plan Overview](./plan.md)
- [Phase 01 - Asset Audit](./phase-01-audit-distributable-assets.md)
- [Phase 02 - Package Scaffold](./phase-02-design-npm-package-scaffold.md)

## Overview

- **Priority:** P1
- **Status:** completed
- **Effort:** 4h
- **Description:** Build safe copy/update engine that installs packaged assets into a target project without accidental overwrites.

## Key Insights

- Installer is the core trust boundary.
- Default behavior must protect user files.
- Manifest tracking enables update/doctor behavior.
- Must handle Windows path separators and read-only files gracefully.

## Requirements

### Functional

- Detect target project root.
- Support explicit `--cwd <path>`.
- Copy selected asset groups.
- Support `--dry-run`.
- Support conflict policies:
  - `skip` default
  - `overwrite`
  - `backup-and-overwrite`
- Write install state file, e.g. `.gtk-skill/install-manifest.json`.
- Preserve directory structure under `.claude/` and `.opencode/`.

### Non-functional

- Idempotent repeated install.
- Clear summary of created/skipped/updated/conflicted files.
- No partial destructive update without backup.
- Atomic-ish writes: write temp file then rename where practical.

## Architecture

```text
command options
   │
   ▼
resolve target project
   │
   ▼
read package manifest
   │
   ▼
filter asset groups
   │
   ▼
plan file operations
   ├─ create
   ├─ skip unchanged
   ├─ conflict
   └─ overwrite/backup
   │
   ▼
execute or dry-run
   │
   ▼
write .gtk-skill/install-manifest.json
```

## Related Code Files

### Files to create later

- `npm-package/src/project/detect-root.ts`
- `npm-package/src/project/target-paths.ts`
- `npm-package/src/installer/copy-assets.ts`
- `npm-package/src/installer/conflict-policy.ts`
- `npm-package/src/installer/backup.ts`
- `npm-package/src/installer/install-state.ts`
- `npm-package/src/manifest/read-manifest.ts`

## Implementation Steps

1. Implement project root detection:
   - Use `--cwd` if provided.
   - Else use `process.cwd()`.
   - Detect existing project by common markers: `.git`, `package.json`, `pom.xml`, `pyproject.toml`, etc.
   - If no marker, allow install but warn.
2. Implement asset group filtering:
   - `--all` default.
   - `--claude`, `--opencode`.
   - `--skills`, `--agents`, `--hooks`, `--rules`, `--scripts`.
3. Implement operation planner:
   ```ts
   type PlannedOperation =
     | { action: 'create'; source: string; target: string }
     | { action: 'skip-unchanged'; target: string }
     | { action: 'conflict'; target: string; reason: string }
     | { action: 'overwrite'; source: string; target: string; backup?: string };
   ```
4. Implement checksum compare:
   - If target missing: create.
   - If target checksum equals package checksum: skip unchanged.
   - If target exists and differs: conflict unless overwrite mode enabled.
5. Implement backup mode:
   - Backup path: `.gtk-skill/backups/{timestamp}/relative/path`.
   - Copy existing target before overwrite.
6. Implement execution:
   - Ensure parent dirs exist.
   - Copy file bytes.
   - Preserve executable bit where platform supports it.
7. Write install manifest:
   ```json
   {
     "packageName": "gtk-skill",
     "packageVersion": "0.1.0",
     "installedAt": "...",
     "files": [{ "target": ".claude/skills/cook/SKILL.md", "sha256": "..." }]
   }
   ```
8. Return structured result for CLI rendering.

## Todo List

- [x] Implement project root detection design
- [x] Implement asset group filtering
- [x] Implement file operation planner
- [x] Implement checksum compare
- [x] Implement conflict policy
- [x] Implement backup-and-overwrite
- [x] Implement dry-run path
- [x] Implement actual copy path
- [x] Implement install manifest write
- [x] Implement structured installer result

## Progress Notes

- Installer core implemented with manifest-driven copy/update planning and install state tracking.
- Non-project dir guard added: install/update fail before writes unless `--allow-non-project-dir`.
- Error path returns exit code `1`.
- Dry-run validated via `node npm-package\bin\gtk-skill.js install --dry-run --cwd npm-package`.

## Success Criteria

- Dry-run lists planned operations and changes no files.
- Default install never overwrites changed user files.
- Backup mode preserves old files before overwrite.
- Re-running install is idempotent.
- Install manifest records package version and checksums.

## Risk Assessment

| Risk | Mitigation |
|---|---|
| User files overwritten | Default skip, require explicit overwrite flag |
| Partial install leaves inconsistent state | Summary shows failures, manifest writes only successful files |
| Windows path bugs | Use `path.join`, `path.relative`, no hardcoded separators |

## Security Considerations

- Prevent path traversal: reject manifest entries with absolute paths or `..` segments.
- Do not execute installed scripts during install.
- Keep install state inside target project `.gtk-skill/`.

## Next Steps

- Phase 4: expose installer via CLI commands.
