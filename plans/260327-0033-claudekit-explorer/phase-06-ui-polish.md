# Phase 06: UI Polish & Animations

## Context Links
- [Plan Overview](./plan.md)
- [Brainstorm - UI/UX Direction](../reports/brainstorm-260327-0012-claudekit-explorer-web.md)

## Overview
- **Priority:** P3
- **Status:** pending
- **Effort:** 2h
- **Description:** Apply visual polish, Framer Motion animations, gradient theme, typography refinements, and responsive tweaks across all components.

## Key Insights
- Framer Motion 11 supports layout animations natively -- use for list reordering
- Keep animations subtle: 200-300ms duration, ease-out curves
- Gradient backgrounds: CSS `linear-gradient` on body/hero, not heavy canvas
- shadcn/ui components already handle dark mode via CSS variables if needed later
- Focus on Phase 1 = light theme only, dark mode is Phase 2 scope

## Requirements

### Functional
- Landing page (Home) with hero section: title, subtitle, CTA buttons
- Smooth page transitions between routes
- Node hover/selection animations on graph
- Search result entrance animations
- Wizard step transitions (slide/fade between steps)
- Loading skeleton states for initial catalog load
- Category filter bar scroll indicators (gradient fade on edges)
- Stats bar on home: total skills, categories, relationships count

### Non-functional
- All animations <300ms
- No layout shift (CLS) from animations
- Lighthouse Performance score >90
- Lighthouse Accessibility score >90

## Architecture

### Animation Inventory

| Component | Animation | Duration | Easing |
|-----------|-----------|----------|--------|
| Page transition | Fade + slight Y translate | 200ms | ease-out |
| Graph node hover | Scale 1.05 + shadow increase | 150ms | ease-out |
| Graph node select | Ring pulse | 600ms | ease-in-out (loop) |
| Search results | Stagger fade-in | 50ms/item | ease-out |
| Detail panel slide | translateX(100% -> 0) | 300ms | spring |
| Wizard step | Fade + slide-left | 250ms | ease-out |
| Category filter toggle | Scale 0.95 -> 1 | 150ms | ease-out |
| Copy button feedback | Icon swap (copy -> check) | 200ms | ease-out |
| Skeleton loading | Shimmer pulse | 1.5s | ease-in-out (loop) |

## Implementation Steps

### 1. Home page hero section

```tsx
<motion.div
  initial={{ opacity: 0, y: 20 }}
  animate={{ opacity: 1, y: 0 }}
  transition={{ duration: 0.5 }}
>
  <h1>ClaudeKit Explorer</h1>
  <p>Discover 67 skills across 10 categories</p>
  <div className="flex gap-3">
    <Button asChild><Link to="/explorer">Explore Graph</Link></Button>
    <Button variant="outline" asChild><Link to="/finder">Find a Skill</Link></Button>
  </div>
</motion.div>
```

Background: subtle gradient `bg-gradient-to-br from-blue-50 via-white to-violet-50`.

### 2. Stats bar on home page

Three stat cards with animated counters:
- "67 Skills" with package icon
- "10 Categories" with grid icon
- "40+ Connections" with git-branch icon

Use Framer Motion `useInView` + count-up animation.

### 3. Page transition wrapper

Create `<PageTransition>` wrapper using `AnimatePresence`:

```tsx
export function PageTransition({ children }: { children: ReactNode }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -8 }}
      transition={{ duration: 0.2 }}
    >
      {children}
    </motion.div>
  );
}
```

### 4. Graph node animations

In `SkillNode`, wrap content with `motion.div`:
- Hover: `whileHover={{ scale: 1.05 }}`
- Selected: animated ring via CSS `@keyframes pulse-ring`
- Highlighted (from search): `animate={{ boxShadow: '0 0 12px ...' }}`
- Dimmed: `animate={{ opacity: 0.3 }}`

### 5. Search results stagger animation

```tsx
<motion.ul>
  {results.map((result, i) => (
    <motion.li
      key={result.item.id}
      initial={{ opacity: 0, x: -10 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: i * 0.05 }}
    />
  ))}
</motion.ul>
```

### 6. Wizard step transitions

Use `AnimatePresence mode="wait"` to swap steps:

```tsx
<AnimatePresence mode="wait">
  <motion.div
    key={currentStep.id}
    initial={{ opacity: 0, x: 30 }}
    animate={{ opacity: 1, x: 0 }}
    exit={{ opacity: 0, x: -30 }}
    transition={{ duration: 0.25 }}
  >
    <WizardStepCard step={currentStep} />
  </motion.div>
</AnimatePresence>
```

### 7. Loading skeletons

Create `<SkillGraphSkeleton>` and `<SkillListSkeleton>`:
- Use shadcn `Skeleton` component
- Graph skeleton: random positioned gray rectangles
- List skeleton: repeated card outlines with shimmer

### 8. Typography and spacing refinements

- H1: 36px / 600 weight / tracking-tight
- H2: 24px / 600 weight
- Body: 14px / 400 weight / leading-relaxed
- Mono (commands): `font-mono text-sm`
- Consistent padding: `p-4` for cards, `p-6` for page sections, `gap-4` between elements

### 9. Responsive breakpoint verification

| Breakpoint | Layout |
|------------|--------|
| <640px (sm) | Single column, full-width cards, no graph |
| 640-768px (md) | Two columns possible, graph hidden |
| 768-1024px (lg) | Graph visible, detail panel overlays |
| >1024px (xl) | Full layout, graph + side panel |

### 10. Favicon and meta tags

- Add SVG favicon (simple hexagon with "CK" letters)
- `<title>ClaudeKit Explorer</title>`
- `<meta name="description" content="...">`
- OG image for social sharing (Phase 2, skip for now)

## Todo List
- [ ] Build home page hero with gradient background + CTA buttons
- [ ] Add stats bar with animated counters
- [ ] Create `PageTransition` wrapper with Framer Motion
- [ ] Add graph node hover/select/highlight animations
- [ ] Add search results stagger fade-in
- [ ] Add wizard step slide transitions
- [ ] Create loading skeleton components
- [ ] Refine typography scale (h1-h6, body, mono)
- [ ] Add category filter toggle animation
- [ ] Add copy button icon swap animation
- [ ] Verify responsive layouts at all breakpoints
- [ ] Add favicon + meta tags
- [ ] Run Lighthouse audit, fix issues
- [ ] Verify no layout shift (CLS = 0)

## Success Criteria
- Home page looks professional with gradient hero + stats
- All animations feel smooth and subtle (not distracting)
- Page transitions work between all 3 routes
- Graph nodes respond to hover/select/highlight with visual feedback
- No janky layout shifts during animations
- Lighthouse Performance >90
- Lighthouse Accessibility >90
- Responsive: usable from 375px to 1920px

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| Too many animations feel overwhelming | Use `prefers-reduced-motion` media query to disable all |
| Framer Motion bundle size | Tree-shakeable; only import used components |
| Gradient renders differently across browsers | Use standard CSS gradients, test Chrome/Firefox/Safari |

## Next Steps
- Phase 7: Deploy to Vercel
