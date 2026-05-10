# Planner Report: ClaudeKit Explorer

**Date:** 2026-03-27
**Plan:** `plans/260327-0033-claudekit-explorer/`
**Status:** Complete - ready for implementation

## Summary

Created 7-phase implementation plan for `claudekit-explorer`, a standalone React SPA to visually explore 67 ClaudeKit skills. Separate GitHub repo, not embedded in ai-skill-gateway.

## Research Findings

- **67 skills** across **10 categories** already catalogued in `skills_data.yaml` (reusable for category mapping)
- SKILL.md frontmatter: `name` (required), `description` (required), `version`, `license`, `argument-hint` (optional); uses YAML with varied styles (quoted, unquoted, multiline `>-`)
- Skills cross-reference each other via `ck:<name>` patterns in markdown body -- extractable via regex for auto-relationship edges
- Nested skills exist (e.g., `document-skills/docx`) -- parser must handle subdirectories
- Existing `skills_data.yaml` has category assignments + `has_references`/`has_scripts` flags -- can bootstrap the catalog

## Plan Overview

| Phase | Description | Effort | Dependency |
|-------|-------------|--------|------------|
| 1. Project Setup | React 19 + Vite 6 + TailwindCSS 4 + shadcn/ui scaffold | 2h | None |
| 2. Data Pipeline | SKILL.md parser -> `skills-catalog.json` (gray-matter + regex) | 3h | Phase 1 |
| 3. Skill Graph | @xyflow/react v12 with dagre layout, 67 nodes, category colors | 5h | Phase 2 |
| 4. Search & Wizard | Fuse.js fuzzy search + 3-step wizard with scoring | 4h | Phase 2 |
| 5. Skill Detail | Slide-in panel (shadcn Sheet) with invoke command, use cases, related skills | 3h | Phase 3/4 |
| 6. UI Polish | Framer Motion animations, gradient theme, responsive tweaks | 2h | Phase 3-5 |
| 7. Deploy | Vercel with SPA routing, auto-deploy from GitHub | 1h | Phase 6 |

**Total: ~20h**

## Key Architecture Decisions

1. **Commit `skills-catalog.json`** to repo (Vercel can't access ai-skill-gateway at build time). Regenerate via `npm run build:catalog` when skills change.
2. **Dagre layout** for automatic node positioning (avoids manual coordinate maintenance for 67 nodes).
3. **Shared selection context** (`useSkillSelection`) bridges graph, search, wizard, and detail panel.
4. **URL deep-linking** (`/explorer?skill=cook`) for shareability.
5. **Mobile fallback**: list/card view replaces graph below 768px.

## File Inventory (40+ files planned)

Key deliverables per phase:
- **Phase 2:** `scripts/build-catalog.ts`, `scripts/category-map.ts`, `src/data/skills-catalog.json`
- **Phase 3:** `src/components/skill-graph/*` (6 files), `src/hooks/use-skill-graph.ts`, `src/lib/graph-layout.ts`
- **Phase 4:** `src/components/skill-finder/*` (5 files), `src/hooks/use-skill-search.ts`, `src/data/wizard-decision-tree.json`
- **Phase 5:** `src/components/skill-detail/*` (6 files), `src/hooks/use-skill-detail.ts`

## Unresolved Questions

1. **Category refinement**: The "other" category has 12 skills (largest grab-bag). Worth splitting into sub-categories (e.g., "content", "visualization")? Deferred to after Phase 1 user feedback.
2. **Template-skill inclusion**: Should `template-skill` appear in the explorer or be filtered as internal? Recommend including it.
3. **Planning skill duplication**: Both `plan` and `planning` skills exist in the glob. Need to verify if `planning` is a duplicate or distinct. Parser should handle gracefully.
4. **Graph layout algorithm**: Dagre works well for DAGs but skill relationships form a general graph with clusters. May need to evaluate force-directed (d3-force) if dagre produces poor layouts. Test after Phase 3.
