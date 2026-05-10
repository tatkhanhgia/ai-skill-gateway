# Phase 01: Project Setup & Scaffold

## Context Links
- [Brainstorm Report](../reports/brainstorm-260327-0012-claudekit-explorer-web.md)
- [Plan Overview](./plan.md)

## Overview
- **Priority:** P1 (blocking all other phases)
- **Status:** pending
- **Effort:** 2h
- **Description:** Create `claudekit-explorer` repo, scaffold React 19 + Vite 6 + TailwindCSS 4 + shadcn/ui + TypeScript project.

## Key Insights
- TailwindCSS 4 uses CSS-based config (`@theme` in CSS), no longer `tailwind.config.ts`
- shadcn/ui v2+ uses `components.json` for project config and `npx shadcn@latest init`
- Vite 6 requires `@vitejs/plugin-react` v5+
- React Router 7 uses file-based or declarative routing

## Requirements

### Functional
- New GitHub repo `claudekit-explorer` initialized
- React 19 + Vite 6 + TypeScript 5.7+ scaffold
- TailwindCSS 4 integrated with custom theme (pastel palette)
- shadcn/ui initialized with required components
- React Router 7 with 3 routes: Home, Explorer (graph), Finder (wizard)
- Framer Motion 11 installed
- ESLint + Prettier configured
- Basic layout shell (header, main, sidebar placeholder)

### Non-functional
- Dev server starts <3s
- TypeScript strict mode
- Path aliases configured (`@/` for `src/`)

## Architecture

```
claudekit-explorer/
├── src/
│   ├── components/
│   │   ├── layout/
│   │   │   ├── app-header.tsx
│   │   │   ├── app-layout.tsx
│   │   │   └── app-sidebar.tsx
│   │   └── ui/                    # shadcn/ui components
│   ├── data/                      # Generated catalog JSON (Phase 2)
│   ├── hooks/
│   ├── lib/
│   │   └── utils.ts               # cn() utility from shadcn
│   ├── pages/
│   │   ├── home-page.tsx
│   │   ├── explorer-page.tsx
│   │   └── finder-page.tsx
│   ├── types/
│   │   └── skill.ts               # Skill TypeScript interfaces
│   ├── app.tsx
│   ├── main.tsx
│   └── index.css                  # TailwindCSS 4 + theme
├── scripts/
│   └── build-catalog.ts           # Phase 2
├── public/
├── index.html
├── vite.config.ts
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
├── package.json
├── .gitignore
└── README.md
```

## Implementation Steps

### 1. Create repo & initialize project
```bash
mkdir claudekit-explorer && cd claudekit-explorer
npm create vite@latest . -- --template react-ts
git init
```

### 2. Install core dependencies
```bash
# Core
npm install react-router@7 @xyflow/react framer-motion fuse.js lucide-react

# Dev
npm install -D tailwindcss@4 @tailwindcss/vite @types/node
```

### 3. Configure Vite (vite.config.ts)
```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import path from 'node:path';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: { '@': path.resolve(__dirname, './src') },
  },
});
```

### 4. Setup TailwindCSS 4 (src/index.css)
```css
@import "tailwindcss";

@theme {
  --color-primary: oklch(0.55 0.15 260);
  --color-cat-utilities: oklch(0.75 0.1 240);
  --color-cat-dev-tools: oklch(0.7 0.05 250);
  --color-cat-other: oklch(0.75 0.03 260);
  --color-cat-frontend: oklch(0.75 0.12 290);
  --color-cat-multimedia: oklch(0.78 0.1 350);
  --color-cat-frameworks: oklch(0.78 0.1 60);
  --color-cat-backend: oklch(0.75 0.1 150);
  --color-cat-ai-ml: oklch(0.75 0.1 195);
  --color-cat-infrastructure: oklch(0.78 0.1 80);
  --color-cat-database: oklch(0.75 0.1 160);
  --font-family-sans: 'Inter', 'system-ui', sans-serif;
}
```

### 5. Initialize shadcn/ui
```bash
npx shadcn@latest init
# Select: New York style, Zinc base color, CSS variables
```

Install initial components:
```bash
npx shadcn@latest add button card input badge sheet scroll-area separator command dialog
```

### 6. Define TypeScript types (src/types/skill.ts)
```typescript
export interface Skill {
  id: string;              // folder name, e.g. "cook"
  name: string;            // from frontmatter, e.g. "ck:cook"
  description: string;     // from frontmatter
  version?: string;
  license?: string;
  argumentHint?: string;
  category: SkillCategory;
  hasReferences: boolean;
  hasScripts: boolean;
  bodyMarkdown: string;    // full markdown body after frontmatter
  useCases: string[];      // extracted from "When to Use" section
  relatedSkills: string[]; // extracted ck:* references
}

export type SkillCategory =
  | 'utilities'
  | 'dev-tools'
  | 'other'
  | 'frontend'
  | 'multimedia'
  | 'frameworks'
  | 'backend'
  | 'ai-ml'
  | 'infrastructure'
  | 'database';

export interface SkillRelationship {
  source: string;  // skill id
  target: string;  // skill id
  type: 'references' | 'depends-on' | 'related';
}

export interface SkillCatalog {
  skills: Skill[];
  relationships: SkillRelationship[];
  categories: Record<SkillCategory, { count: number; color: string }>;
  generatedAt: string;
}
```

### 7. Setup React Router with layout
- Create `app-layout.tsx` with header + `<Outlet />`
- Create 3 placeholder pages
- Configure routes in `app.tsx`

### 8. Add Inter font
```html
<!-- index.html -->
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
```

### 9. Verify dev server
```bash
npm run dev
# Confirm: compiles, routes work, TailwindCSS classes apply
```

## Todo List
- [ ] Create GitHub repo `claudekit-explorer`
- [ ] Scaffold with Vite 6 React-TS template
- [ ] Install all dependencies (react-router, @xyflow/react, framer-motion, fuse.js, lucide-react)
- [ ] Configure Vite with path aliases + TailwindCSS plugin
- [ ] Setup TailwindCSS 4 with custom category color theme
- [ ] Initialize shadcn/ui, add base components
- [ ] Create TypeScript type definitions for Skill, SkillCatalog
- [ ] Create layout shell (header, main content area)
- [ ] Setup React Router 7 with 3 routes
- [ ] Add Inter font
- [ ] Verify dev server starts and all routes render
- [ ] Commit initial scaffold

## Success Criteria
- `npm run dev` starts without errors
- All 3 routes accessible and render placeholder content
- TailwindCSS custom theme colors work
- shadcn/ui Button renders correctly
- TypeScript strict mode passes
- Path alias `@/` resolves correctly

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| TailwindCSS 4 breaking changes from v3 | Use `@tailwindcss/vite` plugin, CSS-based config only |
| shadcn/ui compatibility with TW4 | shadcn v2 supports TW4 natively |
| React 19 + React Router 7 issues | Both stable, well-documented |

## Next Steps
- Phase 2: Build data pipeline (SKILL.md parser -> JSON catalog)
