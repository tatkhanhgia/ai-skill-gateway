# Build Publish and Import Workflow

## Context Links

- [Client Agent Integration Guide](../../docs/client-agent-integration-guide.md)
- [Brainstorm Report](../reports/260511-1052-web-ui-local-console-brainstorm.md)
- [Phase 02](./phase-02-implement-api-client-and-app-state.md)

## Overview

Priority: P1.
Status: Completed.
Build publish screen with form mode, raw JSON mode, and raw `SKILL.md` paste/import mode.

## Key Insights

- Preview payload is mandatory because `SKILL.md` parsing is inherently imperfect.
- Backend required fields: `name`, `version`, `description`, `category`.
- Optional fields: `tags`, `author`, `repositoryUrl`, `requires`, `releaseNotes`.

## Requirements

- Basic form for common publish fields.
- JSON editor/textarea for raw manifest.
- `SKILL.md` paste/import that parses YAML frontmatter when possible.
- Normalize parsed data into publish payload.
- Validate required fields before submit.
- Show response and server validation errors.

## Architecture

```text
PublishView
  FormMode
  JsonMode
  SkillMdMode
  PayloadPreview
  PublishSubmit
parseSkillMarkdown()
normalizePublishPayload()
validatePublishPayload()
```

## Related Code Files

- Create: `web-ui/src/features/publish/publish-view.tsx`
- Create: `web-ui/src/features/publish/publish-payload-preview.tsx`
- Create: `web-ui/src/lib/skill-markdown-parser.ts`
- Create: `web-ui/src/lib/publish-payload-normalizer.ts`
- Create: `web-ui/src/lib/publish-payload-validator.ts`

## Implementation Steps

1. Build publish view with mode tabs.
2. Implement form state for supported manifest fields.
3. Implement JSON parse path with clear parse errors.
4. Implement simple frontmatter parser for `SKILL.md`; map `name`, `description`, metadata where available.
5. Add payload preview with editable final JSON if useful and simple.
6. Validate required fields before protected API call.
7. Submit via `POST /api/v1/skills/publish` with memory API key.

## Todo List

- [x] Build publish screen.
- [x] Add JSON manifest parser.
- [x] Add `SKILL.md` frontmatter parser.
- [x] Add payload normalizer/validator.
- [x] Add protected publish submit.

## Success Criteria

- User can publish from form input.
- User can paste JSON and publish.
- User can paste `SKILL.md`, preview payload, fix fields, publish.
- Server errors render clearly.

## Risk Assessment

- Full Markdown parsing is unnecessary; frontmatter + preview is enough for MVP.
- Mapping arbitrary skill metadata may be lossy; user can correct before submit.

## Security Considerations

- API key only in request header.
- Do not persist pasted skill content unless user explicitly exports later. Out of MVP.

## Next Steps

- Add yank protected action and confirmation UI.
