# Implement Provider Strategy

## Context Links

- [Plan](./plan.md)
- [Define Provider Contract](./phase-01-define-provider-contract.md)
- [EmbeddingService](../../src/main/java/com/skillgateway/service/EmbeddingService.java)
- [SearchService](../../src/main/java/com/skillgateway/service/SearchService.java)
- [SkillService](../../src/main/java/com/skillgateway/service/SkillService.java)

## Overview

Priority: P1  
Status: Complete  
Replace Ollama-only request parsing with provider-specific implementations.

## Key Insights

- Call sites should not know provider details.
- Keep `EmbeddingService.asPgVectorLiteral()` stable for repository usage.
- Existing graceful degradation should remain in `SkillService` and `SearchService`.

## Requirements

- Ollama provider request:
  - POST configured URL
  - JSON: `model`, `prompt`
  - parse root `embedding`
- OpenAI-compatible provider request:
  - POST configured URL, usually `/v1/embeddings`
  - JSON: `model`, `input`
  - add `Authorization: Bearer <key>` only when key is non-blank
  - parse `data[0].embedding`
- Validate non-empty embedding and expected dimension.

## Architecture

```text
EmbeddingService.embed(text)
  -> selectedProvider.embed(text)
  -> provider builds HTTP request
  -> provider parses provider response
  -> EmbeddingService validates vector literal when used by callers
```

## Related Code Files

- Modify: `src/main/java/com/skillgateway/service/EmbeddingService.java`
- Create: `src/main/java/com/skillgateway/service/embedding/OllamaEmbeddingProvider.java`
- Create: `src/main/java/com/skillgateway/service/embedding/OpenAiCompatibleEmbeddingProvider.java`
- Create: `src/main/java/com/skillgateway/service/embedding/EmbeddingProviderException.java`
- Optional create: `src/main/java/com/skillgateway/service/embedding/EmbeddingJsonParser.java`

## Implementation Steps

1. Keep `EmbeddingService` as facade used by existing services.
2. Move current Ollama body/response logic into `OllamaEmbeddingProvider`.
3. Add OpenAI-compatible provider with correct body/header/parser.
4. Share HTTP timeout and JSON mapper without global mutable state.
5. Add dimension validation in a common helper after parsing.
6. Throw runtime provider exception on HTTP >= 400, invalid JSON, empty vector, or dimension mismatch.
7. Keep `InterruptedException` behavior: restore interrupt flag.
8. Preserve fallback behavior by letting existing callers catch runtime exceptions.

## Todo List

- [x] Extract Ollama provider.
- [x] Add OpenAI-compatible provider.
- [x] Add provider selection.
- [x] Add common vector validation.
- [x] Keep `asPgVectorLiteral()` unchanged unless tests expose issue.

## Success Criteria

- Existing services compile with no call-site API change.
- `AI_EMBEDDING_PROVIDER=openai-compatible` changes request/response mapping.
- Provider errors are contained and do not abort public search/publish flows.

## Risk Assessment

- Risk: CDI injection complexity. Mitigation: manual provider selection inside facade is acceptable for two providers.
- Risk: remote providers return nested/errors in varied shapes. Mitigation: parse OpenAI-compatible canonical shape only.

## Security Considerations

- Add bearer auth header only for OpenAI-compatible and only when configured.
- Do not print request body if it may contain private skill descriptions.

## Next Steps

- Add tests for both provider body/response behavior.

## Unresolved Questions

- None.
