# Phase 04: Search & Wizard Finder

## Context Links
- [Plan Overview](./plan.md)
- [Fuse.js docs](https://www.fusejs.io/)
- [Phase 03 - Graph](./phase-03-skill-graph.md) - search results highlight graph nodes

## Overview
- **Priority:** P2
- **Status:** pending
- **Effort:** 4h
- **Description:** Two discovery mechanisms: (A) fuzzy search bar with instant results via Fuse.js, (B) step-by-step wizard ("Help me find a skill") with 2-3 questions that narrows down to top recommendations.

## Key Insights
- Fuse.js config: search on `name`, `description`, `useCases`, `category` with weighted scoring
- Wizard is data-driven: decision tree stored as JSON config, not hardcoded conditionals
- Search results should highlight matching nodes on the graph (cross-component state)
- Combined UI: search bar is always visible; wizard is a modal/sheet triggered by "Help me find" button
- Keep wizard to max 3 steps to avoid user fatigue

## Requirements

### Functional

**Search Bar:**
- Fuzzy text search across skill name, description, use cases, tags
- Instant results as-you-type (debounced 150ms)
- Results shown in dropdown with skill name, description snippet, category badge
- Click result -> navigates to graph + selects node + opens detail panel
- Results also highlight matching nodes on graph (glow effect)
- Keyboard navigation: arrow keys + Enter to select
- Empty state: show popular skills or category shortcuts

**Wizard Flow:**
- Triggered by "Help me find a skill" button
- Step 1: "What do you want to do?" -> 6 options:
  - Build (new feature/project)
  - Fix (bugs, errors, issues)
  - Test (testing, QA)
  - Deploy (infrastructure, CI/CD)
  - Design (UI/UX, frontend)
  - Research (analyze, plan, brainstorm)
- Step 2: "What area?" -> Dynamic options based on Step 1:
  - Build -> Frontend, Backend, Full-stack, Mobile, AI/ML
  - Fix -> Code bug, CI/CD failure, Performance, Database
  - Test -> Unit/E2E, UI visual, Load/security
  - Deploy -> Cloud (Cloudflare/GCP), Docker/K8s, Vercel/Static
  - Design -> Web UI, Components, 3D/Immersive, Documents
  - Research -> Architecture, Libraries, Problem analysis
- Step 3: Show top 3-5 skill recommendations with confidence scores
- Click recommendation -> select on graph + open detail

### Non-functional
- Search results appear <100ms (client-side, no network)
- Wizard completes in max 3 clicks
- Accessible: ARIA roles on search, keyboard navigable wizard steps

## Architecture

### Fuse.js Configuration
```typescript
const fuseOptions: IFuseOptions<Skill> = {
  keys: [
    { name: 'name', weight: 3 },
    { name: 'description', weight: 2 },
    { name: 'useCases', weight: 1.5 },
    { name: 'category', weight: 1 },
    { name: 'argumentHint', weight: 0.5 },
    { name: 'summary', weight: 1.5 },
  ],
  threshold: 0.4,         // 0 = exact match, 1 = match anything
  includeScore: true,
  includeMatches: true,
  minMatchCharLength: 2,
};
```

### Wizard Decision Tree (JSON)
```typescript
interface WizardStep {
  id: string;
  question: string;
  options: WizardOption[];
}

interface WizardOption {
  label: string;
  icon: string;           // Lucide icon name
  description: string;
  nextStepId?: string;    // null = show results
  skillFilter: {
    categories?: string[];
    skillIds?: string[];
    keywords?: string[];
  };
}
```

### Component Tree
```
<FinderPage> OR <SearchOverlay> (accessible from any page)
  ├── <SkillSearchBar>
  │   ├── <CommandInput>        # shadcn Command component
  │   └── <SearchResults>       # dropdown list
  └── <SkillWizard>
      ├── <WizardStepCard>      # question + option grid
      └── <WizardResults>       # top recommendations
```

### Cross-component Communication
```
Search/Wizard selection
       │
       ▼
  useSkillSelection() context (shared state)
    ├─ selectedSkillId: string | null
    ├─ highlightedSkillIds: Set<string>
    └─ selectSkill(id) / highlightSkills(ids[])
       │
       ▼
  Graph reads → highlights nodes
  Detail panel reads → shows info
```

## Related Code Files

### Files to create
- `src/components/skill-finder/skill-search-bar.tsx` - main search input + results
- `src/components/skill-finder/search-results-dropdown.tsx` - results list
- `src/components/skill-finder/skill-wizard.tsx` - wizard container
- `src/components/skill-finder/wizard-step-card.tsx` - individual step UI
- `src/components/skill-finder/wizard-results.tsx` - recommendation cards
- `src/hooks/use-skill-search.ts` - Fuse.js search hook
- `src/hooks/use-skill-selection.tsx` - shared selection context/provider
- `src/data/wizard-decision-tree.json` - wizard configuration
- `src/lib/wizard-engine.ts` - wizard state machine logic

### Files to modify
- `src/app.tsx` - wrap with `SkillSelectionProvider`
- `src/pages/explorer-page.tsx` - consume selection context for highlighting
- `src/pages/finder-page.tsx` - integrate search + wizard
- `src/components/layout/app-header.tsx` - embed global search bar

## Implementation Steps

### 1. Create shared selection context (src/hooks/use-skill-selection.tsx)

```typescript
const SkillSelectionContext = createContext<{
  selectedSkillId: string | null;
  highlightedSkillIds: Set<string>;
  selectSkill: (id: string | null) => void;
  highlightSkills: (ids: string[]) => void;
  clearHighlights: () => void;
}>(...);

export function SkillSelectionProvider({ children }) { ... }
export function useSkillSelection() { return useContext(SkillSelectionContext); }
```

### 2. Create Fuse.js search hook (src/hooks/use-skill-search.ts)

```typescript
export function useSkillSearch(skills: Skill[]) {
  const fuse = useMemo(() => new Fuse(skills, fuseOptions), [skills]);
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<FuseResult<Skill>[]>([]);

  // Debounced search
  useEffect(() => {
    const timer = setTimeout(() => {
      if (query.length >= 2) {
        setResults(fuse.search(query).slice(0, 10));
      } else {
        setResults([]);
      }
    }, 150);
    return () => clearTimeout(timer);
  }, [query, fuse]);

  return { query, setQuery, results };
}
```

### 3. Build SkillSearchBar using shadcn Command

Use `<Command>` component (based on cmdk) for polished search experience:
- `<CommandInput>` for the search input
- `<CommandList>` for results
- `<CommandItem>` for each result with name, description, category badge
- `<CommandEmpty>` for no results state

### 4. Create wizard decision tree JSON

Encode all Step 1 -> Step 2 mappings with skill filters. Each terminal option maps to a set of `skillIds` or `categories` + `keywords` for scoring.

### 5. Create wizard engine (src/lib/wizard-engine.ts)

Simple state machine:
```typescript
export function computeWizardResults(
  skills: Skill[],
  selections: WizardOption[]
): ScoredSkill[] {
  // Aggregate skill filters from all selections
  // Score each skill based on filter matches
  // Return top 5 sorted by score
}
```

### 6. Build wizard UI components

- `SkillWizard`: manages step state, renders current step or results
- `WizardStepCard`: grid of option cards with icons, click advances
- `WizardResults`: ranked list with confidence bars, click selects skill

### 7. Integrate search bar in header

Global search bar in `app-header.tsx`, available on all pages. On result click:
1. Navigate to explorer page
2. Set `selectedSkillId`
3. Set `highlightedSkillIds` with the result
4. Graph zooms to selected node

### 8. Integrate highlighting in graph

In `SkillNode`, check if `id` is in `highlightedSkillIds`:
- Yes: add pulsing ring animation (Framer Motion)
- No (but highlights active): dim opacity to 0.3

## Todo List
- [ ] Create `SkillSelectionProvider` context with selection + highlighting state
- [ ] Wrap app in provider
- [ ] Create `useSkillSearch` hook with Fuse.js
- [ ] Create `SkillSearchBar` using shadcn Command component
- [ ] Create `SearchResultsDropdown` with keyboard navigation
- [ ] Create `wizard-decision-tree.json` with all step/option mappings
- [ ] Create `wizard-engine.ts` scoring logic
- [ ] Create `SkillWizard`, `WizardStepCard`, `WizardResults` components
- [ ] Integrate search bar in app header (global)
- [ ] Wire search result click -> graph navigation + selection
- [ ] Wire wizard result click -> graph navigation + selection
- [ ] Add node highlighting (glow/dim) in SkillNode based on context
- [ ] Test: search "debug" -> finds debug, fix skills
- [ ] Test: wizard Build->Frontend -> recommends frontend-development, ui-styling, react-best-practices
- [ ] Test: keyboard navigation in search results

## Success Criteria
- Typing "deploy" in search shows devops, docker, cloudflare-related skills
- Search results appear <100ms after typing stops
- Clicking search result navigates to graph + highlights node
- Wizard completes in 2-3 clicks and shows relevant recommendations
- Wizard "Build -> Frontend" recommends frontend-development as top result
- Highlighted nodes pulse on graph; non-matching nodes dim
- Search bar accessible from all pages via header
- Keyboard: Tab to search, arrows to navigate, Enter to select

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| Fuse.js threshold too loose/strict | Start at 0.4, tune after testing with real queries |
| Wizard recommendations feel random | Test all 6x5=30 paths manually, curate skill-to-filter mappings |
| Cross-page navigation (search -> graph) feels janky | Use React Router `navigate()` + `useEffect` to zoom after mount |
| cmdk/Command component styling conflicts with theme | shadcn's Command already styled; minor tweaks in CSS |

## Next Steps
- Phase 5: Detail panel shows full skill info on selection
