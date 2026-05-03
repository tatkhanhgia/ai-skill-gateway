# Phase 02: Design NPM Package Scaffold

## Context Links

- [Plan Overview](./plan.md)
- [Phase 01 - Asset Audit](./phase-01-audit-distributable-assets.md)

## Overview

- **Priority:** P1
- **Status:** completed
- **Effort:** 3h
- **Description:** Create package layout, build config, executable entrypoint, and npm metadata for `gtk-skill`.

## Key Insights

- CLI package should be explicit: `npx gtk-skill install`, not `postinstall` side effects.
- TypeScript gives maintainability; compiled JS ships to npm.
- Use package `files` field as publish allowlist.
- Root repo currently has no root `package.json`; decide whether to add one to this repo or create package subdirectory.

## Requirements

### Functional

- npm package named `gtk-skill` or final user-approved name.
- `bin` entry exposes `gtk-skill` command.
- Build TypeScript to `dist/`.
- Include curated `assets/` and manifest in package.
- Provide package scripts for build/test/pack/manifest.

### Non-functional

- Works on Windows, macOS, Linux.
- Node 20+ target.
- No dependency bloat.
- Clear source layout under package boundary.

## Architecture

Recommended layout: package subdirectory to avoid mixing Java/Quarkus root with npm package.

```text
npm-package/
├── package.json
├── tsconfig.json
├── README.md
├── bin/
│   └── gtk-skill.js
├── src/
│   ├── cli.ts
│   ├── commands/
│   ├── installer/
│   ├── manifest/
│   └── project/
├── assets/
│   ├── claude/
│   └── opencode/
└── tests/
```

Alternative: root package. Rejected for first release because repo is primarily Java/Quarkus and already has many tool assets.

## Related Code Files

### Files to create later

- `npm-package/package.json`
- `npm-package/tsconfig.json`
- `npm-package/bin/gtk-skill.js`
- `npm-package/src/cli.ts`
- `npm-package/README.md`
- `npm-package/.npmignore` if needed

### Files to modify later

- Root docs/changelog if project docs require update.

## Implementation Steps

1. Confirm final package name: default `gtk-skill`.
2. Create `npm-package/` folder.
3. Add `package.json` metadata:
   ```json
   {
     "name": "gtk-skill",
     "version": "0.1.0",
     "type": "module",
     "bin": { "gtk-skill": "./bin/gtk-skill.js" },
     "files": ["bin", "dist", "assets", "assets-manifest.json", "README.md"],
     "engines": { "node": ">=20" }
   }
   ```
4. Add minimal dependencies:
   - CLI parser: `commander` or `cac`.
   - Prompts: optional `@inquirer/prompts` only if interactive mode needed.
   - File ops: prefer Node built-ins.
   - Glob: `fast-glob` or built-in recursive traversal.
5. Add bin shim with shebang:
   ```js
   #!/usr/bin/env node
   import '../dist/cli.js';
   ```
6. Add build/test scripts:
   ```json
   {
     "scripts": {
       "build": "tsc",
       "build:manifest": "node dist/manifest/build-manifest.js",
       "test": "node --test",
       "pack:local": "npm pack"
     }
   }
   ```
7. Add package README quick start.

## Todo List

- [x] Confirm package name
- [x] Decide package subdirectory vs root package
- [x] Define `package.json` metadata
- [x] Define `bin` entrypoint
- [x] Define TypeScript build config
- [x] Define dependency list
- [x] Define package `files` allowlist
- [x] Define package README contents

## Progress Notes

- `npm-package/` scaffold created and isolated from Java/Quarkus root.
- Package metadata, CLI bin shim, README, TypeScript config, generated assets, and manifest are in place.
- Local packaging path validated by `npm pack` dry-run audit.

## Success Criteria

- Package structure is isolated and publishable.
- `npm pack --dry-run` will include only intended files.
- CLI binary can be invoked after local install.
- No npm lifecycle hook writes to consumer project.

## Risk Assessment

| Risk | Mitigation |
|---|---|
| Package name unavailable | Verify before release, fallback scoped package |
| Root npm package conflicts with Java repo | Use `npm-package/` subdirectory |
| Too many dependencies | Prefer Node built-ins, keep CLI parser only |

## Security Considerations

- Never use `postinstall` for project writes.
- Keep publish allowlist strict.
- Package metadata should not leak local paths.

## Next Steps

- Phase 3: implement installer core design.
