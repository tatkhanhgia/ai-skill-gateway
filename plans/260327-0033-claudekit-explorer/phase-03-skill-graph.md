# Phase 03: Interactive Skill Graph

## Context Links
- [Plan Overview](./plan.md)
- [@xyflow/react docs](https://reactflow.dev/learn)
- [Phase 02 - Data Pipeline](./phase-02-data-pipeline.md)

## Overview
- **Priority:** P1
- **Status:** pending
- **Effort:** 5h
- **Description:** Build the core interactive graph visualization using @xyflow/react (React Flow v12). 67 skill nodes, color-coded by category, with relationship edges. Click to select, zoom/pan/drag, category filters.

## Key Insights
- React Flow v12 handles 60-100 nodes well if nodes are lightweight (no heavy re-renders)
- Use `useNodesState` / `useEdgesState` hooks for state management
- Custom node components for branded look
- `dagre` or `elkjs` for automatic layout (avoid manual positioning of 67 nodes)
- Category-based grouping via layout algorithm subgraph clusters
- Performance: use `nodeTypes` object outside component to prevent re-registration

## Requirements

### Functional
- Render all 67 skills as interactive nodes on a canvas
- Nodes color-coded by category (10 categories, 10 pastel colors)
- Edges between nodes for relationships (auto-detected + manual)
- Click node -> dispatch selected skill event (consumed by detail panel, Phase 5)
- Category filter toggles (show/hide groups)
- Zoom to fit on initial load
- Mini-map in bottom-right corner
- Search highlighting: when user searches (Phase 4), matching nodes glow/pulse
- Zoom-to-node when selected from search results

### Non-functional
- Initial render <2s for 67 nodes
- Smooth 60fps pan/zoom
- Responsive: hide graph below 768px, show list fallback
- Accessible: keyboard navigation for nodes

## Architecture

### Component Tree
```
<ExplorerPage>
  ├── <GraphControls>          # zoom buttons, fit view, minimap toggle
  ├── <CategoryFilterBar>      # toggle pills per category
  ├── <SkillGraph>             # main React Flow canvas
  │   ├── <SkillNode>          # custom node component (x67)
  │   └── <SkillEdge>          # custom edge component (animated)
  └── <MobileSkillList>        # list fallback for <768px
```

### State Flow
```
skills-catalog.json
       │
       ▼
  useSkillGraph() hook
    ├─ converts skills[] -> Node[]
    ├─ converts relationships[] -> Edge[]
    ├─ applies dagre layout
    ├─ handles category filter state
    └─ exposes: nodes, edges, onNodeClick, toggleCategory, fitView
       │
       ▼
  <ReactFlow nodes={nodes} edges={edges} ... />
```

## Related Code Files

### Files to create
- `src/components/skill-graph/skill-graph.tsx` - main graph wrapper
- `src/components/skill-graph/skill-node.tsx` - custom node component
- `src/components/skill-graph/skill-edge.tsx` - custom animated edge
- `src/components/skill-graph/graph-controls.tsx` - zoom/fit/minimap buttons
- `src/components/skill-graph/category-filter-bar.tsx` - category toggle pills
- `src/components/skill-graph/mobile-skill-list.tsx` - mobile fallback list
- `src/hooks/use-skill-graph.ts` - graph data transformation hook
- `src/hooks/use-graph-layout.ts` - dagre layout computation
- `src/lib/graph-layout.ts` - layout algorithm wrapper
- `src/lib/category-colors.ts` - category -> color mapping

### Files to modify
- `src/pages/explorer-page.tsx` - integrate graph components

### Dependencies to install
- `@dagrejs/dagre` - automatic node layout

## Implementation Steps

### 1. Install layout dependency
```bash
npm install @dagrejs/dagre
```

### 2. Create category color mapping (src/lib/category-colors.ts)

```typescript
export const CATEGORY_COLORS: Record<string, { bg: string; border: string; text: string }> = {
  'utilities':      { bg: '#DBEAFE', border: '#93C5FD', text: '#1E40AF' },
  'dev-tools':      { bg: '#E2E8F0', border: '#94A3B8', text: '#334155' },
  'other':          { bg: '#F3F4F6', border: '#D1D5DB', text: '#374151' },
  'frontend':       { bg: '#EDE9FE', border: '#C4B5FD', text: '#5B21B6' },
  'multimedia':     { bg: '#FCE7F3', border: '#F9A8D4', text: '#9D174D' },
  'frameworks':     { bg: '#FFF7ED', border: '#FDBA74', text: '#9A3412' },
  'backend':        { bg: '#DCFCE7', border: '#86EFAC', text: '#166534' },
  'ai-ml':          { bg: '#CFFAFE', border: '#67E8F9', text: '#155E75' },
  'infrastructure': { bg: '#FEF3C7', border: '#FCD34D', text: '#92400E' },
  'database':       { bg: '#D1FAE5', border: '#6EE7B7', text: '#065F46' },
};

export const CATEGORY_LABELS: Record<string, string> = {
  'utilities': 'Utilities',
  'dev-tools': 'Dev Tools',
  'other': 'Other',
  'frontend': 'Frontend',
  'multimedia': 'Multimedia',
  'frameworks': 'Frameworks',
  'backend': 'Backend',
  'ai-ml': 'AI / ML',
  'infrastructure': 'Infrastructure',
  'database': 'Database',
};
```

### 3. Create graph layout utility (src/lib/graph-layout.ts)

Use dagre to compute positions. Group by category for visual clustering:

```typescript
import Dagre from '@dagrejs/dagre';
import type { Node, Edge } from '@xyflow/react';

export function computeLayout(nodes: Node[], edges: Edge[]): Node[] {
  const g = new Dagre.graphlib.Graph().setDefaultEdgeLabel(() => ({}));

  g.setGraph({
    rankdir: 'TB',       // top to bottom
    nodesep: 80,         // horizontal spacing
    ranksep: 100,        // vertical spacing
    marginx: 40,
    marginy: 40,
  });

  nodes.forEach(node => g.setNode(node.id, { width: 200, height: 80 }));
  edges.forEach(edge => g.setEdge(edge.source, edge.target));

  Dagre.layout(g);

  return nodes.map(node => {
    const pos = g.node(node.id);
    return { ...node, position: { x: pos.x - 100, y: pos.y - 40 } };
  });
}
```

### 4. Create custom SkillNode component (src/components/skill-graph/skill-node.tsx)

```typescript
// Lightweight custom node
// - Rounded card with category color border
// - Skill name (bold) + short description (truncated)
// - Category badge
// - Handle on top (target) and bottom (source)
// - Hover: slight scale + glow
// - Selected: ring highlight
// Uses memo() to prevent unnecessary re-renders
```

Key design:
- Width: 200px, Height: auto (min 70px)
- Border-left: 4px solid category color
- Background: white with slight category tint
- Font: name 14px semibold, description 11px regular (2 lines max)

### 5. Create custom SkillEdge (src/components/skill-graph/skill-edge.tsx)

- Use `BezierEdge` as base
- Subtle gray color, 1px stroke
- Animated dash pattern on hover
- Different styles per relationship type:
  - `depends-on`: solid, slightly thicker
  - `references`: dashed
  - `related`: dotted

### 6. Create useSkillGraph hook (src/hooks/use-skill-graph.ts)

```typescript
export function useSkillGraph(catalog: SkillCatalog) {
  const [hiddenCategories, setHiddenCategories] = useState<Set<string>>(new Set());
  const [selectedSkillId, setSelectedSkillId] = useState<string | null>(null);
  const [highlightedSkillIds, setHighlightedSkillIds] = useState<Set<string>>(new Set());

  // Convert catalog to React Flow nodes/edges
  const allNodes = useMemo(() => /* map skills to Node[] */, [catalog]);
  const allEdges = useMemo(() => /* map relationships to Edge[] */, [catalog]);

  // Apply category filter
  const visibleNodes = useMemo(() =>
    allNodes.filter(n => !hiddenCategories.has(n.data.category)),
    [allNodes, hiddenCategories]
  );

  // Apply layout
  const layoutNodes = useMemo(() =>
    computeLayout(visibleNodes, visibleEdges),
    [visibleNodes, visibleEdges]
  );

  return {
    nodes: layoutNodes,
    edges: visibleEdges,
    selectedSkillId,
    onNodeClick: (_, node) => setSelectedSkillId(node.id),
    toggleCategory: (cat) => /* toggle set */,
    hiddenCategories,
    setHighlightedSkillIds,
  };
}
```

### 7. Create CategoryFilterBar (src/components/skill-graph/category-filter-bar.tsx)

- Horizontal scrollable row of pills/badges
- Each pill: category label + count + colored dot
- Click toggles visibility
- Active state: filled; inactive: outline/dimmed

### 8. Create GraphControls (src/components/skill-graph/graph-controls.tsx)

- Zoom in / Zoom out / Fit view buttons (use React Flow's `useReactFlow()` hook)
- Minimap toggle
- Reset layout button

### 9. Create MobileSkillList (src/components/skill-graph/mobile-skill-list.tsx)

- Shown below 768px via `useMediaQuery` or CSS `hidden md:block`
- Grouped by category with collapsible sections
- Each item: skill name, description snippet, category badge
- Click -> same selection behavior as graph node click

### 10. Assemble in ExplorerPage

```tsx
export default function ExplorerPage() {
  const catalog = useSkillCatalog(); // loads JSON
  const graph = useSkillGraph(catalog);

  return (
    <div className="h-full flex flex-col">
      <CategoryFilterBar ... />
      {/* Desktop: graph */}
      <div className="hidden md:flex flex-1">
        <SkillGraph nodes={graph.nodes} edges={graph.edges} ... />
      </div>
      {/* Mobile: list */}
      <div className="md:hidden flex-1">
        <MobileSkillList skills={catalog.skills} ... />
      </div>
    </div>
  );
}
```

## Todo List
- [ ] Install `@dagrejs/dagre`
- [ ] Create `category-colors.ts` with 10 category color definitions
- [ ] Create `graph-layout.ts` with dagre layout computation
- [ ] Create `SkillNode` custom node component with category styling
- [ ] Create `SkillEdge` custom edge component with relationship type styling
- [ ] Create `useSkillGraph` hook with filtering, selection, highlighting
- [ ] Create `CategoryFilterBar` with toggle pills
- [ ] Create `GraphControls` with zoom/fit/minimap
- [ ] Create `MobileSkillList` fallback for <768px
- [ ] Assemble all in `ExplorerPage`
- [ ] Test with full 67-node dataset
- [ ] Verify layout doesn't overlap nodes
- [ ] Verify category filter hides/shows node groups
- [ ] Verify node click dispatches selection
- [ ] Performance test: initial render <2s

## Success Criteria
- 67 nodes visible on canvas with readable labels
- Nodes colored correctly by category
- Edges visible between related skills
- Category filter toggles work (hide/show)
- Click node -> `selectedSkillId` updates
- Zoom/pan/drag smooth at 60fps
- Minimap functional
- Mobile (<768px) shows list view instead of graph
- No console errors or React Flow warnings

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| 67 nodes overlapping with dagre | Tune `nodesep`/`ranksep`, test different `rankdir` values |
| Performance with many edges | Limit edge rendering to visible viewport (React Flow does this natively) |
| Layout recalc on filter change causes jank | Use `useMemo` for layout, debounce filter changes |
| dagre layout ugly for disconnected clusters | Add invisible edges between category leaders, or use force-directed alternative |

## Next Steps
- Phase 4: Search bar + Wizard that highlights nodes on graph
- Phase 5: Detail panel opens on node click
