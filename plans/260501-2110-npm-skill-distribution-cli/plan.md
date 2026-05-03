---
title: "NPM Skill Distribution CLI"
description: "Create an npm package and CLI installer that distributes this repo's Claude/OpenCode skills, agents, hooks, rules, and scripts into target projects."
status: completed
priority: P1
effort: 18h
branch: master
tags: [feature, npm, cli, distribution, skills]
blockedBy: []
blocks: []
created: 2026-05-01
---

# NPM Skill Distribution CLI

## Overview

Build `gtk-skill` as an npm-distributed CLI. Users run `npx gtk-skill@latest install` or `npm install -D gtk-skill && npx gtk-skill install` to copy curated `.claude` and `.opencode` assets from this repo into a target project.

This plan complements existing plans:
- `260221-0043-java-mcp-skill-repository-server`: registry backend, not npm installer.
- `260327-0033-claudekit-explorer`: catalog UI, not distribution installer.

## Scope

In scope:
- Package distributable assets from `.claude/skills`, `.claude/agents`, `.claude/hooks`, `.claude/rules`, `.claude/scripts`, `.opencode/skills`, `.opencode/agents`.
- CLI commands for install/list/update/doctor.
- Safe file copy with dry-run, overwrite strategy, backups, and manifest tracking.
- npm publish-ready package.

Out of scope for first release:
- Hosted registry server integration.
- Postinstall auto-writing into consumer projects.
- Remote downloads from GitHub at install time.
- GUI/catalog explorer.

## Cross-Plan Dependencies

| Relationship | Plan | Status | Notes |
|---|---|---:|---|
| Related | [Java MCP Skill Repository Server](../260221-0043-java-mcp-skill-repository-server/plan.md) | in-progress | Can become future remote registry source. Not blocking. |
| Related | [ClaudeKit Explorer](../260327-0033-claudekit-explorer/plan.md) | pending | Can reuse asset catalog ideas. Not blocking. |

## Recommended UX

Prefer explicit command over npm lifecycle side effect:

```bash
npx gtk-skill@latest install
# or
npm install -D gtk-skill
npx gtk-skill install
```

Avoid `postinstall` writing files into user projects. Security/trust issue and npm ecosystem anti-pattern.

## Phases

| Phase | Name | Status | Effort |
|---|---|---|---:|
| 1 | [Audit Distributable Assets](./phase-01-audit-distributable-assets.md) | completed | 3h |
| 2 | [Design NPM Package Scaffold](./phase-02-design-npm-package-scaffold.md) | completed | 3h |
| 3 | [Implement Installer Core](./phase-03-implement-installer-core.md) | completed | 4h |
| 4 | [Implement CLI Commands and UX](./phase-04-implement-cli-commands-and-ux.md) | completed | 3h |
| 5 | [Validate Tests and Local Packaging](./phase-05-validate-tests-and-local-packaging.md) | completed | 3h |
| 6 | [Prepare NPM Release Workflow](./phase-06-prepare-npm-release-workflow.md) | completed | 2h |

## Key Architecture

```text
gtk-skill npm package
├── bin/gtk-skill.js              # executable entrypoint
├── dist/                         # compiled TypeScript output
├── assets/                       # curated source-of-truth payload
│   ├── claude/{skills,agents,hooks,rules,scripts}/
│   └── opencode/{skills,agents}/
├── manifest.json                 # asset inventory + checksums + package version
└── src/
    ├── cli.ts
    ├── commands/{install,list,update,doctor}.ts
    ├── installer/{copy-assets,backup,conflict-policy}.ts
    ├── manifest/{build-manifest,read-manifest}.ts
    └── project/{detect-root,target-paths}.ts
```

## Acceptance Criteria

- [x] `npm pack` produces tarball containing required assets only.
- [x] `npx gtk-skill install --dry-run` lists intended writes without changing files.
- [x] `npx gtk-skill install` copies selected assets into a sample project.
- [x] Existing user files are not overwritten unless explicit `--overwrite`.
- [x] Backup mode works before overwrites.
- [x] `gtk-skill doctor` reports installed version, missing files, changed files.
- [x] Windows paths supported.
- [x] No secrets, `.venv`, `node_modules`, caches, or local session artifacts included.

## Progress Update

Completed now:
- `npm-package/` scaffold exists with package metadata, bin entry, TypeScript source, README, tests, generated assets, generated manifest.
- Installer/list/doctor/update/version flows implemented.
- Guard added: install/update fail before writes in non-project dirs unless `--allow-non-project-dir`.
- Guard added: command exits with code `1` on errors.
- Payload audit tightened: `.logs` and `.jsonl` excluded.

Validated now:
- `npm test --prefix npm-package` passed: 10 tests.
- `node npm-package\bin\gtk-skill.js install --dry-run --cwd npm-package` passed.
- `npm pack` dry-run forbidden-file audit passed; payload reported 2440 files.
- Packed tarball smoke test passed in temp project.
- Real install, conflict-skip, and `--backup --overwrite` validations passed in temp project.
- `npm view gtk-skill name` returned 404, so package name appears available at validation time.
- `npm publish --dry-run --access public` passed and reported `+ gtk-skill@0.1.0`.

Remaining:
- Real npm publish requires user approval and npm login; no token or CI publish workflow added.

## Cook Command

After review, implement with:

```text
/ck:cook C:\Users\Admin\Documents\Project\NetBeansProjects\MyProject\ai-skill-gateway\plans\260501-2110-npm-skill-distribution-cli\plan.md
```
