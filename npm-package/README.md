# gtk-skill

Install curated Claude and OpenCode skills into a project.

## Quick start

```bash
npx gtk-skill@latest install --dry-run
npx gtk-skill@latest install
npx gtk-skill@latest doctor
```

`gtk-skill` never writes during npm install. You must run an explicit command.

## Commands

```bash
gtk-skill install [--dry-run] [--cwd <path>] [--claude] [--opencode] [--skills] [--agents] [--hooks] [--rules] [--scripts] [--overwrite] [--backup] [--json]
gtk-skill update [options]
gtk-skill list [--json]
gtk-skill doctor [--cwd <path>] [--json]
gtk-skill version
```

## Safety

- Default install skips files that already exist and differ.
- Use `--overwrite` only when you want package files to replace local files.
- Use `--backup --overwrite` to save replaced files under `.gtk-skill/backups/`.
- Scripts and hooks are copied only; they are never executed by this CLI.

## Release checklist

1. `npm run build`
2. `npm test`
3. `npm pack --dry-run --json`
4. Inspect payload for forbidden files: `.env`, `.venv`, `node_modules`, session state, local settings.
5. `npm pack`
6. Smoke test the tarball in a temp project.
7. Publish manually with `npm publish --access public` after npm login.
