# Update Docs and Runtime Examples

## Context Links

- [Plan](./plan.md)
- [README](../../README.md)
- [System Architecture](../../docs/system-architecture.md)
- [Code Standards](../../docs/code-standards.md)
- [Project Overview PDR](../../docs/project-overview-pdr.md)

## Overview

Priority: P2  
Status: Complete  
Document AI-first provider model and config examples after implementation passes tests.

## Key Insights

- README currently presents Ollama as the local AI engine.
- Architecture docs say embedding URL/model are configurable, but not provider-compatible.
- OpenAI-compatible examples must avoid committed secrets.

## Requirements

- Document `ollama` as default local-first provider.
- Document `openai-compatible` config shape.
- Include examples for local OpenAI-compatible servers without requiring cloud.
- Mention fixed vector dimension requirement.
- Update changelog/roadmap if present and required by docs policy.

## Architecture

```text
Docs
  -> README quick start keeps Ollama path
  -> Configuration section adds provider alternatives
  -> Architecture explains provider strategy
  -> Code standards note secret handling and network-free tests
```

## Related Code Files

- Modify: `README.md`
- Modify: `docs/system-architecture.md`
- Modify: `docs/code-standards.md`
- Modify: `docs/project-overview-pdr.md`
- Optional modify: `docs/project-changelog.md` if file exists
- Optional modify: `docs/development-roadmap.md` if file exists

## Implementation Steps

1. Add README config examples:
   - Ollama default
   - OpenAI-compatible local endpoint
   - Remote endpoint with env var for API key
2. State vector dimension must match DB schema/config.
3. Update architecture docs to show provider strategy.
4. Update code standards with API key handling.
5. Run docs validation if script exists:
   - `node .claude/scripts/validate-docs.cjs docs/`

## Todo List

- [x] Update README.
- [x] Update architecture docs.
- [x] Update code standards/PDR.
- [x] Update changelog/roadmap if present.
- [x] Run docs validation if available.

## Success Criteria

- New users understand Ollama is default, OpenAI-compatible is supported.
- No docs contain real API keys.
- Config examples match implemented property names.

## Risk Assessment

- Risk: docs overpromise providers. Mitigation: say "OpenAI-compatible `/v1/embeddings` shape", not every provider.
- Risk: dimension confusion. Mitigation: explicit warning near examples.

## Security Considerations

- Use placeholders for secrets.
- Recommend env vars, not committed property values.

## Next Steps

- Final verification and code review during implementation.

## Unresolved Questions

- None.
