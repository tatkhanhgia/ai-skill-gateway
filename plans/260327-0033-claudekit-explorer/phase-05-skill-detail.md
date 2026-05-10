# Phase 05: Skill Detail Panel

## Context Links
- [Plan Overview](./plan.md)
- [Phase 03 - Graph](./phase-03-skill-graph.md) - node click triggers panel
- [Phase 04 - Search](./phase-04-search-wizard.md) - search result click triggers panel

## Overview
- **Priority:** P2
- **Status:** pending
- **Effort:** 3h
- **Description:** Slide-in detail panel that shows full skill information when a skill is selected from the graph or search. Includes description, invoke command, use cases, related skills, and metadata.

## Key Insights
- Use shadcn `Sheet` component for slide-in panel (right side, 400px wide)
- Render skill markdown body with a lightweight markdown renderer (react-markdown or custom)
- Related skills shown as clickable chips that navigate to that skill on the graph
- Panel should not block graph interaction (overlay with click-outside to close)
- Keeping it simple: no tabs, single scrollable panel

## Requirements

### Functional
- Slide-in panel from right side on skill selection
- Header: skill name + category badge + version badge
- Invoke command section: copyable code block (e.g., `/cook [task]`)
- Description: full description from frontmatter
- Summary: first paragraph extracted from body
- Use cases: bullet list from extracted `useCases` array
- Related skills: clickable badge chips -> click navigates to that skill
- Metadata: version, license, has references, has scripts indicators
- Close button + click-outside to dismiss
- URL updates with selected skill for deep-linking (`/explorer?skill=cook`)

### Non-functional
- Panel opens with smooth 300ms slide animation (Framer Motion)
- Panel scrollable for long content
- Accessible: focus trap when open, Escape to close
- Panel works on mobile as full-screen sheet (bottom sheet or full overlay)

## Architecture

### Component Tree
```
<SkillDetailPanel>
  ├── <Sheet>                       # shadcn Sheet (right side)
  │   ├── <SheetHeader>
  │   │   ├── skill name (h2)
  │   │   ├── <Badge> category
  │   │   └── <Badge> version
  │   ├── <SheetContent>
  │   │   ├── <InvokeCommandBlock>  # copyable code
  │   │   ├── <SkillDescription>    # description + summary
  │   │   ├── <SkillUseCases>       # bullet list
  │   │   ├── <RelatedSkillChips>   # clickable badges
  │   │   └── <SkillMetadata>       # version, license, flags
  │   └── <SheetFooter>
  │       └── "View SKILL.md" link (GitHub)
  └── (empty when no skill selected)
```

### Data Flow
```
useSkillSelection().selectedSkillId
       │
       ▼
  Look up skill from catalog by ID
       │
       ▼
  <SkillDetailPanel skill={skill} />
```

## Related Code Files

### Files to create
- `src/components/skill-detail/skill-detail-panel.tsx` - main panel wrapper
- `src/components/skill-detail/invoke-command-block.tsx` - copyable command display
- `src/components/skill-detail/skill-description-section.tsx` - description + summary
- `src/components/skill-detail/skill-use-cases-section.tsx` - use cases list
- `src/components/skill-detail/related-skill-chips.tsx` - related skill badges
- `src/components/skill-detail/skill-metadata-section.tsx` - version/license/flags
- `src/hooks/use-skill-detail.ts` - derives detail data from catalog + selection

### Files to modify
- `src/pages/explorer-page.tsx` - render detail panel alongside graph
- `src/app.tsx` - handle URL search params for deep-linking

### Dependencies to install
- None new (shadcn Sheet already installed in Phase 1)

## Implementation Steps

### 1. Create useSkillDetail hook (src/hooks/use-skill-detail.ts)

```typescript
export function useSkillDetail(catalog: SkillCatalog) {
  const { selectedSkillId, selectSkill } = useSkillSelection();

  const skill = useMemo(
    () => catalog.skills.find(s => s.id === selectedSkillId) ?? null,
    [catalog, selectedSkillId]
  );

  const relatedSkills = useMemo(() => {
    if (!skill) return [];
    return skill.relatedSkills
      .map(id => catalog.skills.find(s => s.id === id))
      .filter(Boolean);
  }, [skill, catalog]);

  const invokeCommand = useMemo(() => {
    if (!skill) return '';
    const hint = skill.argumentHint ? ` ${skill.argumentHint}` : '';
    return `/${skill.name.replace('ck:', '')}${hint}`;
  }, [skill]);

  return { skill, relatedSkills, invokeCommand, isOpen: !!skill, close: () => selectSkill(null) };
}
```

### 2. Create InvokeCommandBlock

- Display invoke command in styled code block
- Copy-to-clipboard button (use `navigator.clipboard.writeText`)
- Subtle animation on copy (checkmark icon swap)
- Example display: `/cook [task|plan-path] [--interactive|--fast|--parallel|--auto|--no-test]`

### 3. Create SkillDescriptionSection

- Render `skill.description` as primary text
- Render `skill.summary` below as secondary/muted text
- No markdown rendering needed here (descriptions are plain text)

### 4. Create SkillUseCasesSection

- Render `skill.useCases[]` as bullet list
- Each item: Lucide `CheckCircle` icon + text
- If empty, show "General-purpose skill" fallback

### 5. Create RelatedSkillChips

- Horizontal wrap of badge chips
- Each chip: skill name + category color dot
- Click chip -> `selectSkill(relatedId)` (navigates within panel)
- If no related skills, hide section

### 6. Create SkillMetadataSection

- Grid of small metadata items:
  - Version: `v2.1.1` or "N/A"
  - License: `MIT` or "N/A"
  - References: checkmark/x icon
  - Scripts: checkmark/x icon
  - Category: full label
- Compact 2-column grid layout

### 7. Assemble SkillDetailPanel

```tsx
export function SkillDetailPanel({ catalog }: Props) {
  const { skill, relatedSkills, invokeCommand, isOpen, close } = useSkillDetail(catalog);

  return (
    <Sheet open={isOpen} onOpenChange={(open) => !open && close()}>
      <SheetContent side="right" className="w-[400px] sm:w-[450px]">
        {skill && (
          <>
            <SheetHeader>
              <SheetTitle>{skill.name}</SheetTitle>
              <div className="flex gap-2">
                <Badge style={categoryStyle}>{skill.category}</Badge>
                {skill.version && <Badge variant="outline">v{skill.version}</Badge>}
              </div>
            </SheetHeader>
            <ScrollArea className="flex-1">
              <InvokeCommandBlock command={invokeCommand} />
              <Separator />
              <SkillDescriptionSection skill={skill} />
              <Separator />
              <SkillUseCasesSection useCases={skill.useCases} />
              <Separator />
              <RelatedSkillChips skills={relatedSkills} onSelect={selectSkill} />
              <Separator />
              <SkillMetadataSection skill={skill} />
            </ScrollArea>
          </>
        )}
      </SheetContent>
    </Sheet>
  );
}
```

### 8. Add URL deep-linking

In `ExplorerPage`, sync `selectedSkillId` with URL search params:

```typescript
const [searchParams, setSearchParams] = useSearchParams();

useEffect(() => {
  const skillParam = searchParams.get('skill');
  if (skillParam && skillParam !== selectedSkillId) {
    selectSkill(skillParam);
  }
}, [searchParams]);

useEffect(() => {
  if (selectedSkillId) {
    setSearchParams({ skill: selectedSkillId }, { replace: true });
  } else {
    setSearchParams({}, { replace: true });
  }
}, [selectedSkillId]);
```

### 9. Integrate in ExplorerPage

Add `<SkillDetailPanel catalog={catalog} />` as sibling to `<SkillGraph>`.

## Todo List
- [ ] Create `useSkillDetail` hook with skill lookup + related skills + invoke command
- [ ] Create `InvokeCommandBlock` with copy-to-clipboard
- [ ] Create `SkillDescriptionSection`
- [ ] Create `SkillUseCasesSection` with bullet list
- [ ] Create `RelatedSkillChips` with clickable navigation
- [ ] Create `SkillMetadataSection` with version/license/flags grid
- [ ] Assemble `SkillDetailPanel` with shadcn Sheet
- [ ] Add URL search param deep-linking (`?skill=cook`)
- [ ] Integrate panel in ExplorerPage
- [ ] Test: click graph node -> panel slides in with correct data
- [ ] Test: click related skill chip -> panel updates to new skill
- [ ] Test: copy invoke command -> clipboard contains correct text
- [ ] Test: deep link `/explorer?skill=debug` -> opens with debug panel
- [ ] Test: mobile -> panel renders as full overlay

## Success Criteria
- Panel slides in from right on node/search selection
- All 5 sections display correct data for any skill
- Copy command button works
- Related skill chips navigate to other skills
- URL updates with `?skill=<id>` on selection
- Direct URL `/explorer?skill=cook` opens with panel pre-loaded
- Panel closes on X button, Escape key, or click outside
- Mobile: panel renders as full-width overlay
- Smooth 300ms slide animation

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| Sheet blocks graph interaction on mobile | Use `modal={false}` on desktop, `modal={true}` on mobile |
| Long descriptions overflow | ScrollArea handles overflow natively |
| Deep-link skill not found | Show "Skill not found" message, clear param |
| Copy-to-clipboard fails in some browsers | Fallback: select text prompt |

## Next Steps
- Phase 6: Polish all components with animations and visual refinement
