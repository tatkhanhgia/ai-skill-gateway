# Phase 01: Audit Distributable Assets

## Context Links

- [Plan Overview](./plan.md)
- [Development Rules](../../.claude/rules/development-rules.md)
- [Project README](../../README.md)

## Overview

- **Priority:** P1
- **Status:** completed
- **Effort:** 3h
- **Description:** Inventory all candidate `.claude` and `.opencode` assets, classify what is safe to publish, and define package include/exclude rules.

## Key Insights

- Repo contains generated/session/local files that must not ship.
- `.claude/skills/.venv`, `node_modules`, caches, `.env`, session-state, metadata, and local settings are high-risk.
- Distribution should copy curated runtime assets, not whole `.claude` blindly.
- Manifest must be deterministic so `doctor` and `update` can compare installed files.

## Requirements

### Functional

- Identify publishable asset roots:
  - `.claude/skills/`
  - `.claude/agents/`
  - `.claude/hooks/`
  - `.claude/rules/`
  - `.claude/scripts/`
  - `.opencode/skills/`
  - `.opencode/agents/`
- Identify excluded files/directories.
- Produce asset inventory design with path, size, checksum, target path, asset type.
- Define mapping from source package path to target project path.

### Non-functional

- No secrets or local state in npm package.
- Deterministic ordering and checksums.
- Windows-compatible path handling.

## Architecture

```text
repo asset roots
   │
   ▼
asset audit script/design
   ├─ include rules
   ├─ exclude rules
   ├─ checksum per file
   └─ generated package manifest
       ▼
assets/ + manifest.json in npm package
```

## Related Code Files

### Files to create later

- `src/manifest/build-manifest.ts`
- `src/manifest/read-manifest.ts`
- `manifest.json` or generated `assets-manifest.json`

### Files to modify later

- `package.json`
- `.npmignore` or package `files` field

## Implementation Steps

1. List all candidate files under approved asset roots.
2. Define exclude globs:
   - `**/node_modules/**`
   - `**/.venv/**`
   - `**/.env*`
   - `**/.git/**`
   - `**/__pycache__/**`
   - `**/.pytest_cache/**`
   - `**/coverage/**`
   - `.claude/session-state/**`
   - `.claude/metadata.json`
   - `.claude/settings.local.json`
   - transient logs, temp files, local credentials.
3. Define include strategy: package assets copied into `assets/claude/...` and `assets/opencode/...`.
4. Define manifest schema:
   ```json
   {
     "packageVersion": "0.1.0",
     "generatedAt": "2026-05-01T00:00:00Z",
     "files": [
       {
         "source": "assets/claude/skills/cook/SKILL.md",
         "target": ".claude/skills/cook/SKILL.md",
         "type": "claude-skill",
         "sha256": "...",
         "size": 1234
       }
     ]
   }
   ```
5. Decide if `.claude/settings.json` should be distributed. Default: no, only offer optional merge later.
6. Document first-release asset set and excluded set.

## Todo List

- [x] Audit `.claude` asset roots
- [x] Audit `.opencode` asset roots
- [x] Define include globs
- [x] Define exclude globs
- [x] Define manifest schema
- [x] Decide target path mapping
- [x] Decide whether settings are excluded or merged optionally
- [x] Verify no secrets/local state included

## Progress Notes

- Curated asset roots shipped under generated `assets/` and manifest.
- Excludes enforced for `.venv`, `node_modules`, `.env*`, session/local metadata, `.logs`, and `.jsonl` payloads.
- Audit outcome proven by `npm pack` dry-run forbidden-file audit.

## Success Criteria

- Complete asset inclusion/exclusion rules exist.
- Manifest schema can represent every shipped file.
- npm package will not include `.venv`, `node_modules`, `.env`, caches, session state, or local metadata.
- Target paths are clear for Claude and OpenCode assets.

## Risk Assessment

| Risk | Mitigation |
|---|---|
| Accidentally publish secrets/local files | Default-deny sensitive globs, explicit package files list, CI validation |
| Package too large | Exclude dependencies/caches, keep only source/runtime assets |
| Broken skill scripts after copy | Include needed script source and docs; do not include local venv |

## Security Considerations

- Treat all dotfiles as sensitive unless explicitly allowlisted.
- Do not ship credentials, local settings, session logs, or metadata.
- Checksums help detect tampering or local changes.

## Next Steps

- Phase 2: design npm package scaffold.
