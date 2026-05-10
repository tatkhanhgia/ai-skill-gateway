# Brainstorm: ClaudeKit Explorer Web

**Date:** 2026-03-27
**Status:** Agreed
**Participants:** User + AI Brainstormer

---

## Problem Statement

69 ClaudeKit skills exist in `.claude/skills/` but no visual way to:
- Discover what skills are available
- Understand relationships between skills
- Find the right skill for a specific task
- Learn how to use each skill

Current discovery = reading SKILL.md files manually. Not scalable.

## Decisions Made

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Separate repo | Yes - `claudekit-explorer` | Clean separation from Java backend, independent CI/CD |
| Target users | Local first → Public | Start dev locally, deploy Vercel for community |
| Tech stack | React 19 + Vite 6 | Best ecosystem for interactive SPA |
| Styling | TailwindCSS 4 + shadcn/ui | Professional, light/gradient theme |
| Graph viz | @xyflow/react (React Flow v12) | Best interactive node graph library |
| Animation | Framer Motion | Smooth, gentle transitions |
| Search | Fuse.js (client-side) | Fuzzy search on static JSON, no backend needed |
| Data source | Hybrid (static JSON + optional API) | Static catalog bundled at build, API for semantic search |
| Deploy | Vercel | Free tier, auto-deploy from GitHub |
| Q&A flow | Search + Wizard combined | Quick search for experts, wizard for newcomers |

## Architecture

### Data Pipeline

```
Build time:
  .claude/skills/*/SKILL.md → parse script → skills-catalog.json → bundled in app

Runtime:
  skills-catalog.json → React state → Graph + Search + Wizard
  (Optional) Gateway API → Semantic search when backend available
```

### Project Structure

```
claudekit-explorer/
├── src/
│   ├── components/
│   │   ├── skill-graph/          # React Flow interactive graph
│   │   ├── skill-finder/         # Wizard + Search combo
│   │   ├── skill-detail/         # Slide-in detail panel
│   │   └── ui/                   # shadcn/ui components
│   ├── data/
│   │   ├── skills-catalog.json   # Generated from SKILL.md
│   │   └── skill-relationships.json  # Graph edges mapping
│   ├── hooks/                    # Custom React hooks
│   ├── lib/                      # Utilities, search config
│   └── pages/
│       ├── home.tsx              # Landing + search
│       ├── explorer.tsx          # Graph view
│       └── finder.tsx            # Wizard Q&A
├── scripts/
│   └── build-catalog.ts          # SKILL.md parser → JSON
├── public/
├── tailwind.config.ts
├── vite.config.ts
└── package.json
```

### Tech Stack

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

## Features (Phase 1)

### A. Skill Catalog + Interactive Graph
- Each skill = 1 node, color-coded by category
- Edges show relationships (references, workflow dependencies)
- Click node → slide-in detail panel
- Zoom/pan/drag freely
- Category toggle to show/hide groups
- Mobile fallback: list view

### B. Skill Finder (Search + Wizard)
- **Search bar**: Fuzzy search on name/description/tags via Fuse.js
- **Wizard flow** ("Help me find"):
  - Step 1: "What do you want to do?" (Build, Fix, Test, Deploy, Design, Research)
  - Step 2: "Project type?" (Frontend, Backend, Full-stack, Mobile, DevOps)
  - Step 3: Top 3-5 matching skills with confidence scores
- Results highlight on graph

### C. Skill Detail Panel
- Description, category, tags
- Invoke command (e.g., `/ck:cook`, `/ck:fix`)
- Use cases and examples
- Related skills (linked in graph)

## UI/UX Direction

- **Theme**: Light background, subtle gradient (blue → purple or blue → mint)
- **Typography**: Inter/Geist font family, clean hierarchy
- **Cards**: Rounded corners (12-16px), subtle shadows, light glassmorphism
- **Colors**: Pastel palette per category, primary accent blue/indigo
- **Animations**: Framer Motion ~300ms, fade-in, gentle scale, smooth slide
- **Graph nodes**: Soft colors, hover glow, smooth edge curves
- **Responsive**: Desktop-first, mobile-friendly (graph → list fallback)

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| 69 nodes cluttered | High | Category grouping, toggle visibility, zoom levels |
| Skill relationships not explicit in SKILL.md | Medium | Parse references + manual relationship mapping JSON |
| Wizard decision tree maintenance | Medium | Start simple 2-3 steps, data-driven (JSON config) |
| Graph unusable on mobile | Medium | Responsive: list view fallback on <768px |
| SKILL.md format inconsistency | Low | Robust parser with fallback defaults |

## Success Metrics

- [ ] All 69 skills visible and searchable
- [ ] Graph renders <2s on desktop
- [ ] Wizard recommends relevant skill in 3 steps or less
- [ ] Lighthouse performance >90
- [ ] Mobile-friendly (responsive down to 375px)
- [ ] Deploy to Vercel successfully

## Next Steps

1. Create `claudekit-explorer` repo
2. Scaffold React + Vite + TailwindCSS + shadcn/ui
3. Build SKILL.md parser script
4. Implement skill graph with @xyflow/react
5. Implement search + wizard finder
6. Polish UI/UX with animations
7. Deploy to Vercel
