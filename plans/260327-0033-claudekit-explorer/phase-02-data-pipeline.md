# Phase 02: Data Pipeline (SKILL.md -> JSON Catalog)

## Context Links
- [Plan Overview](./plan.md)
- [Skill Spec](../../.claude/skills/agent_skills_spec.md)
- [Existing YAML catalog](../../.claude/skills/ck-help/scripts/skills_data.yaml) - reference for category mapping

## Overview
- **Priority:** P1 (unblocks Phase 3-7)
- **Status:** pending
- **Effort:** 3h
- **Description:** Build a Node.js script that parses all 67 SKILL.md files, extracts frontmatter + body content, detects skill-to-skill references, and outputs `skills-catalog.json` into `src/data/`.

## Key Insights
- SKILL.md files have YAML frontmatter with `name`, `description` (required) + `version`, `license`, `argument-hint`, `languages` (optional)
- Frontmatter field `name` uses `ck:` prefix (e.g., `ck:cook`), but some don't (e.g., `document-skills/docx`)
- Skills reference other skills via `ck:<name>` pattern in markdown body
- Existing `skills_data.yaml` already has category assignments -- reuse this mapping
- Some skills have `has_references` and `has_scripts` flags -- extract from directory contents
- Description field uses various YAML styles: quoted strings, unquoted, multiline (`>-`)

## Requirements

### Functional
- Parse all `.claude/skills/*/SKILL.md` files (including nested like `document-skills/docx`)
- Extract YAML frontmatter fields
- Extract markdown body (everything after second `---`)
- Extract "When to Use" / "Use cases" section from body
- Detect `ck:<name>` references in body -> build relationship edges
- Merge category data from existing `skills_data.yaml` or embed as hardcoded map
- Output single `skills-catalog.json` with full catalog structure
- Support manual relationship overrides via `skill-relationships-manual.json`

### Non-functional
- Script runs <5s for 67 skills
- Idempotent (same input -> same output)
- Clear error messages for malformed SKILL.md files
- Runnable as `npm run build:catalog` and as Vite prebuild step

## Architecture

```
Build-time data flow:

  .claude/skills/*/SKILL.md
         │
         ▼
  scripts/build-catalog.ts
    ├─ parse YAML frontmatter (gray-matter)
    ├─ extract body markdown
    ├─ regex scan for ck:* references
    ├─ merge category map
    ├─ merge manual relationships
    └─ validate & dedupe
         │
         ▼
  src/data/skills-catalog.json   (imported by app)
```

### Output Schema (skills-catalog.json)
```json
{
  "skills": [
    {
      "id": "cook",
      "name": "ck:cook",
      "description": "ALWAYS activate this skill...",
      "version": "2.1.1",
      "license": null,
      "argumentHint": "[task|plan-path] [--interactive|...]",
      "category": "utilities",
      "hasReferences": true,
      "hasScripts": false,
      "useCases": ["Feature implementation", "Plan execution"],
      "relatedSkills": ["scout", "plan"],
      "summary": "End-to-end implementation with automatic workflow detection."
    }
  ],
  "relationships": [
    { "source": "cook", "target": "scout", "type": "references" },
    { "source": "fix", "target": "debug", "type": "depends-on" }
  ],
  "categories": {
    "utilities": { "count": 18, "label": "Utilities", "color": "#93C5FD" },
    "dev-tools": { "count": 13, "label": "Dev Tools", "color": "#94A3B8" }
  },
  "meta": {
    "totalSkills": 67,
    "generatedAt": "2026-03-27T00:33:00Z",
    "sourceDir": ".claude/skills"
  }
}
```

## Related Code Files

### Files to create
- `scripts/build-catalog.ts` - main parser script
- `scripts/category-map.ts` - hardcoded category assignment (from skills_data.yaml)
- `src/data/skill-relationships-manual.json` - manual relationship overrides
- `src/data/skills-catalog.json` - generated output (gitignored? or committed)

### Files to modify
- `package.json` - add `build:catalog` script, add `gray-matter` + `tsx` dev deps
- `vite.config.ts` - optional: add prebuild plugin

## Implementation Steps

### 1. Install build-time dependencies
```bash
npm install -D gray-matter tsx glob yaml
```

### 2. Create category map (scripts/category-map.ts)

Hardcode the category assignments from the existing `skills_data.yaml`:

```typescript
export const CATEGORY_MAP: Record<string, string> = {
  'agent-browser': 'other',
  'ai-artist': 'ai-ml',
  'ai-multimodal': 'ai-ml',
  'ask': 'utilities',
  'backend-development': 'backend',
  'better-auth': 'backend',
  'bootstrap': 'utilities',
  'brainstorm': 'utilities',
  'chrome-devtools': 'multimedia',
  'ck-help': 'dev-tools',
  'code-review': 'utilities',
  'coding-level': 'utilities',
  'context-engineering': 'other',
  'cook': 'utilities',
  'copywriting': 'other',
  'databases': 'database',
  'debug': 'utilities',
  'devops': 'infrastructure',
  'docs': 'utilities',
  'docs-seeker': 'dev-tools',
  'document-skills/docx': 'multimedia',
  'document-skills/pdf': 'multimedia',
  'document-skills/pptx': 'multimedia',
  'document-skills/xlsx': 'multimedia',
  'find-skills': 'dev-tools',
  'fix': 'utilities',
  'frontend-design': 'frontend',
  'frontend-development': 'frontend',
  'git': 'dev-tools',
  'gkg': 'other',
  'google-adk-python': 'ai-ml',
  'journal': 'utilities',
  'kanban': 'dev-tools',
  'markdown-novel-viewer': 'other',
  'mcp-builder': 'frontend',
  'mcp-management': 'dev-tools',
  'media-processing': 'multimedia',
  'mermaidjs-v11': 'other',
  'mintlify': 'other',
  'mobile-development': 'frameworks',
  'payment-integration': 'backend',
  'plan': 'utilities',
  'plans-kanban': 'dev-tools',
  'planning': 'utilities',
  'preview': 'utilities',
  'problem-solving': 'utilities',
  'project-management': 'utilities',
  'react-best-practices': 'other',
  'remotion': 'other',
  'repomix': 'dev-tools',
  'research': 'utilities',
  'scout': 'dev-tools',
  'sequential-thinking': 'utilities',
  'shader': 'other',
  'shopify': 'frameworks',
  'skill-creator': 'dev-tools',
  'tanstack': 'other',
  'team': 'dev-tools',
  'template-skill': 'dev-tools',
  'test': 'utilities',
  'threejs': 'frontend',
  'ui-styling': 'frontend',
  'ui-ux-pro-max': 'frontend',
  'use-mcp': 'dev-tools',
  'watzup': 'utilities',
  'web-design-guidelines': 'frontend',
  'web-frameworks': 'frameworks',
  'web-testing': 'other',
  'worktree': 'dev-tools',
};
```

### 3. Create build-catalog.ts

Core logic:

```typescript
// Pseudocode outline
import matter from 'gray-matter';
import { globSync } from 'glob';
import { readFileSync, writeFileSync } from 'fs';
import { CATEGORY_MAP } from './category-map';

const SKILLS_DIR = process.env.SKILLS_DIR || '../ai-skill-gateway/.claude/skills';
const OUTPUT = './src/data/skills-catalog.json';

// 1. Glob all SKILL.md files
const files = globSync(`${SKILLS_DIR}/**/SKILL.md`);

// 2. Parse each file
const skills = files.map(file => {
  const raw = readFileSync(file, 'utf-8');
  const { data: frontmatter, content: body } = matter(raw);

  // Extract skill ID from path (folder name relative to skills dir)
  const id = extractId(file);

  // Extract ck:* references from body
  const refs = [...body.matchAll(/ck:([a-z][\w-]*)/g)]
    .map(m => m[1])
    .filter(ref => ref !== id);  // exclude self-references

  // Extract first paragraph after H1 as summary
  const summary = extractSummary(body);

  // Extract use cases from "When to Use" section
  const useCases = extractUseCases(body);

  return {
    id,
    name: frontmatter.name || `ck:${id}`,
    description: frontmatter.description || '',
    version: frontmatter.version || null,
    license: frontmatter.license || null,
    argumentHint: frontmatter['argument-hint'] || null,
    category: CATEGORY_MAP[id] || 'other',
    hasReferences: checkDirExists(file, 'references'),
    hasScripts: checkDirExists(file, 'scripts'),
    useCases,
    relatedSkills: [...new Set(refs)],
    summary,
  };
});

// 3. Build relationships from cross-references
const relationships = skills.flatMap(s =>
  s.relatedSkills.map(target => ({
    source: s.id,
    target,
    type: 'references' as const,
  }))
);

// 4. Merge manual relationships
const manual = loadManualRelationships();
const allRelationships = dedupeRelationships([...relationships, ...manual]);

// 5. Build category stats
const categories = buildCategoryStats(skills);

// 6. Write output
writeFileSync(OUTPUT, JSON.stringify({ skills, relationships: allRelationships, categories, meta: {...} }, null, 2));
```

### 4. Key extraction functions

**extractSummary(body)**: Get first non-empty paragraph after the first H1 heading.

**extractUseCases(body)**: Look for sections titled "When to Use", "Use Cases", "When to Apply". Parse bullet points under those headings.

**extractId(filepath)**: Parse relative path from skills dir. Handle nested skills like `document-skills/docx` -> use folder name after skills dir.

### 5. Create manual relationships file

```json
[
  { "source": "fix", "target": "debug", "type": "depends-on" },
  { "source": "cook", "target": "plan", "type": "depends-on" },
  { "source": "cook", "target": "test", "type": "depends-on" },
  { "source": "cook", "target": "code-review", "type": "depends-on" },
  { "source": "bootstrap", "target": "cook", "type": "related" },
  { "source": "frontend-development", "target": "ui-styling", "type": "related" },
  { "source": "frontend-development", "target": "react-best-practices", "type": "related" },
  { "source": "frontend-design", "target": "ui-ux-pro-max", "type": "related" },
  { "source": "backend-development", "target": "databases", "type": "related" },
  { "source": "backend-development", "target": "devops", "type": "related" },
  { "source": "web-frameworks", "target": "frontend-development", "type": "related" },
  { "source": "mcp-builder", "target": "mcp-management", "type": "related" },
  { "source": "chrome-devtools", "target": "agent-browser", "type": "related" },
  { "source": "ai-artist", "target": "ai-multimodal", "type": "related" },
  { "source": "plan", "target": "project-management", "type": "related" },
  { "source": "kanban", "target": "plans-kanban", "type": "related" },
  { "source": "sequential-thinking", "target": "problem-solving", "type": "related" },
  { "source": "docs", "target": "docs-seeker", "type": "related" },
  { "source": "web-testing", "target": "test", "type": "related" },
  { "source": "threejs", "target": "shader", "type": "related" }
]
```

### 6. Add npm scripts
```json
{
  "scripts": {
    "build:catalog": "tsx scripts/build-catalog.ts",
    "prebuild": "npm run build:catalog",
    "predev": "npm run build:catalog"
  }
}
```

### 7. Configure SKILLS_DIR environment variable

For local dev, point at sibling ai-skill-gateway repo:
```bash
# .env.local
SKILLS_DIR=../ai-skill-gateway/.claude/skills
```

For CI/Vercel, provide the path or commit the generated JSON.

**Decision: Commit `skills-catalog.json` to repo.** Reason: Vercel build won't have access to ai-skill-gateway repo. Regenerate and recommit when skills change.

### 8. Validate output
```bash
npm run build:catalog
# Verify: src/data/skills-catalog.json exists
# Verify: 67 skills parsed
# Verify: relationships array populated
# Verify: all 10 categories present
```

## Todo List
- [ ] Install gray-matter, tsx, glob dev dependencies
- [ ] Create `scripts/category-map.ts` with all 67 skill category assignments
- [ ] Create `scripts/build-catalog.ts` with YAML parsing + body extraction
- [ ] Implement `extractSummary()` - first paragraph after H1
- [ ] Implement `extractUseCases()` - bullet points from "When to Use" section
- [ ] Implement `ck:*` reference scanning for auto-relationships
- [ ] Create `src/data/skill-relationships-manual.json` with curated edges
- [ ] Add merge logic for manual + auto relationships with deduplication
- [ ] Add `build:catalog`, `prebuild`, `predev` npm scripts
- [ ] Run parser against all 67 SKILL.md files, fix edge cases
- [ ] Validate JSON output schema matches TypeScript types
- [ ] Commit generated `skills-catalog.json`

## Success Criteria
- `npm run build:catalog` completes without errors
- Output JSON contains exactly 67 skills (matches glob count)
- Every skill has: id, name, description, category, summary
- Relationships array has 20+ auto-detected edges + 20 manual edges
- All 10 categories present with correct counts
- JSON validates against the `SkillCatalog` TypeScript interface
- Edge cases handled: multiline YAML descriptions, nested skill paths, missing optional fields

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| YAML frontmatter parsing fails on some files | gray-matter handles edge cases well; add try/catch per file with error log |
| Nested skill paths (document-skills/docx) | Normalize path extraction to handle any nesting depth |
| ck:* false positives in body text | Filter: only match known skill IDs from parsed list |
| Category map goes stale | Single source file; easy to update when skills change |

## Security Considerations
- Script reads files from local filesystem only
- No network calls
- Output is static JSON, no executable code

## Next Steps
- Phase 3: Use `skills-catalog.json` to render interactive graph
