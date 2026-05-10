---
title: "ClaudeKit Explorer - Interactive Skill Browser"
description: "React SPA to discover, search, and visualize 67 ClaudeKit skills via interactive graph and wizard"
status: pending
priority: P1
effort: 20h
branch: kai/feat/claudekit-explorer
tags: [react, vite, tailwindcss, react-flow, skills, visualization]
created: 2026-03-27
---

# ClaudeKit Explorer - Implementation Plan

## Overview

Standalone React SPA (`claudekit-explorer` repo) for visually exploring 67 ClaudeKit skills across 10 categories. Features interactive node graph (@xyflow/react), fuzzy search (Fuse.js), step-by-step wizard, and slide-in detail panels.

**Data source:** Static JSON generated at build time from `.claude/skills/*/SKILL.md` files.
**Deploy target:** Vercel (free tier).

## Tech Stack

| Layer | Library | Version |
|-------|---------|---------|
| Framework | React | 19 |
| Build | Vite | 6 |
| Styling | TailwindCSS | 4 |
| UI Components | shadcn/ui | latest |
| Graph | @xyflow/react | 12 |
| Animation | Framer Motion | 11 |
| Search | Fuse.js | 7 |
| Router | React Router | 7 |
| Icons | Lucide React | latest |
| Language | TypeScript | 5.7+ |

## Skill Categories (10 categories, 67 skills)

| Category | Count | Color (pastel) |
|----------|-------|-----------------|
| utilities | 18 | Blue |
| dev-tools | 13 | Slate |
| other | 12 | Gray |
| frontend | 7 | Violet |
| multimedia | 6 | Pink |
| frameworks | 3 | Orange |
| backend | 3 | Green |
| ai-ml | 3 | Cyan |
| infrastructure | 1 | Amber |
| database | 1 | Emerald |

## Phases

| # | Phase | Effort | Status | File |
|---|-------|--------|--------|------|
| 1 | Project Setup & Scaffold | 2h | pending | [phase-01-project-setup.md](phase-01-project-setup.md) |
| 2 | Data Pipeline (SKILL.md -> JSON) | 3h | pending | [phase-02-data-pipeline.md](phase-02-data-pipeline.md) |
| 3 | Skill Graph Component | 5h | pending | [phase-03-skill-graph.md](phase-03-skill-graph.md) |
| 4 | Search & Wizard Finder | 4h | pending | [phase-04-search-wizard.md](phase-04-search-wizard.md) |
| 5 | Skill Detail Panel | 3h | pending | [phase-05-skill-detail.md](phase-05-skill-detail.md) |
| 6 | UI Polish & Animations | 2h | pending | [phase-06-ui-polish.md](phase-06-ui-polish.md) |
| 7 | Deploy to Vercel | 1h | pending | [phase-07-deploy.md](phase-07-deploy.md) |

## Key Dependencies

- Phase 2 unblocks all other phases (data must exist first)
- Phase 3, 4, 5 can partially parallelize after Phase 2
- Phase 6 depends on 3+4+5 being functional
- Phase 7 depends on all prior phases

## Key Decisions

1. **Separate repo** - NOT inside ai-skill-gateway. Clean CI/CD, independent deploy.
2. **Static JSON at build time** - No runtime API dependency. Copy SKILL.md files or point build script at sibling directory.
3. **Relationship mapping** - Hybrid: auto-extracted from `ck:*` references in SKILL.md + manual `skill-relationships.json` for implicit connections.
4. **Mobile strategy** - Graph view on desktop (>768px), list/card view fallback on mobile.
5. **Category colors** - Pastel palette matching TailwindCSS color scale, consistent across graph nodes and UI.
