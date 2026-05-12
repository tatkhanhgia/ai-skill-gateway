---
title: "Skill Folder Bundle Support Brainstorm Summary"
type: report
status: approved
created: 2026-05-11
---

# Skill Folder Bundle Support Brainstorm Summary

## Summary

Current gateway stores searchable skill metadata well, but real agent skills are folders. A valid skill has `SKILL.md` as entrypoint, plus optional resources such as `scripts/`, `references/`, `assets/`, `examples/`, and license files.

## Findings

- Official Claude Code docs describe skills as directories with `SKILL.md` plus optional bundled files.
- Claude API skill upload accepts one or more files or zip, with `SKILL.md` top-level and common root.
- Local bundled spec in `npm-package/assets/claude/skills/agent_skills_spec.md` matches this: skill folder required, `SKILL.md` required.
- Backend currently stores only `SkillManifest` metadata. No bundle artifact or per-file manifest.
- NPM package already has useful file inventory concepts: source, target, type, sha256, size.

## Evaluated Approaches

| Approach | Pros | Cons | Decision |
|---|---|---|---|
| DB-only metadata + artifact bundle | Search remains fast, artifact immutable, install/download possible | Needs storage adapter and validation | Choose |
| Store every file body in DB | Simple transaction story | DB bloat, poor download/streaming, awkward binary handling | Reject |
| Store repository URL only | Fastest | Not offline, not immutable, weak registry semantics | Reject |

## Recommended Solution

Keep metadata/search in PostgreSQL. Store versioned skill bundle as canonical zip artifact through a storage adapter. Persist bundle summary and file manifest for validation, listing, and download.

## Risks

- Zip path traversal and symlink handling are security-critical.
- Large files can cause memory pressure if upload/extract is not streamed or limited.
- Multipart support may need Quarkus-specific request binding.
- Frontend directory upload support varies by browser; zip upload is reliable fallback.

## References

- Claude Code Skills: https://docs.claude.com/en/docs/claude-code/skills
- Claude API Skills Guide: https://docs.claude.com/en/api/skills-guide
- Local spec: `npm-package/assets/claude/skills/agent_skills_spec.md`

## Unresolved Questions

- Initial artifact storage default: local filesystem assumed. Confirm before implementation if Postgres bytea is required instead.
