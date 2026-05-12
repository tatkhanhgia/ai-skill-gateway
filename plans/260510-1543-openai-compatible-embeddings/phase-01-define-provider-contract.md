# Define Provider Contract

## Context Links

- [Plan](./plan.md)
- [EmbeddingService](../../src/main/java/com/skillgateway/service/EmbeddingService.java)
- [Application Properties](../../src/main/resources/application.properties)
- [Code Standards](../../docs/code-standards.md)
- [System Architecture](../../docs/system-architecture.md)

## Overview

Priority: P1  
Status: Complete  
Define config and service boundaries before implementation.

## Key Insights

- Current service assumes Ollama request/response shape.
- `SkillService` and `SearchService` already catch embedding failures.
- `vector(768)` is fixed in migrations/entity; provider output must match.

## Requirements

- Add provider selector: `ai.embedding.provider=ollama|openai-compatible`.
- Keep existing defaults working.
- Add optional API key config for OpenAI-compatible providers.
- Add timeout and dimension validation config if not already reusable.
- Avoid leaking API keys in logs, errors, docs, or tests.

## Architecture

```text
SkillService/SearchService
  -> EmbeddingService facade
    -> EmbeddingProvider selected by config
      -> Ollama provider
      -> OpenAI-compatible provider
```

## Related Code Files

- Modify: `src/main/resources/application.properties`
- Modify: `src/main/java/com/skillgateway/service/EmbeddingService.java`
- Create: `src/main/java/com/skillgateway/service/embedding/EmbeddingProvider.java`
- Create: `src/main/java/com/skillgateway/service/embedding/EmbeddingProviderType.java`
- Create or modify config helper if existing `AppConfig` is suitable.

## Implementation Steps

1. Define provider enum values: `ollama`, `openai-compatible`.
2. Define `EmbeddingProvider` with `float[] embed(String text)`.
3. Add config fields:
   - `ai.embedding.provider=${AI_EMBEDDING_PROVIDER:ollama}`
   - `ai.embedding.url=${AI_EMBEDDING_URL:http://localhost:11434/api/embeddings}`
   - `ai.embedding.model=${AI_EMBEDDING_MODEL:nomic-embed-text}`
   - `ai.embedding.api-key=${AI_EMBEDDING_API_KEY:}`
   - `ai.embedding.dimension=${AI_EMBEDDING_DIMENSION:768}`
   - optional `ai.embedding.timeout-seconds=${AI_EMBEDDING_TIMEOUT_SECONDS:3}`
4. Decide config injection style: keep `@ConfigProperty` for smallest change, or use `@ConfigMapping` if file remains simple.
5. Specify dimension check behavior: if returned vector length != configured dimension, throw provider exception.

## Todo List

- [x] Define provider interface.
- [x] Add provider config keys.
- [x] Document default behavior in comments/properties.
- [x] Confirm no API key is logged.

## Success Criteria

- Existing Ollama config keeps working without env changes.
- OpenAI-compatible config path is explicit.
- Dimension mismatch behavior is planned before provider code.

## Risk Assessment

- Risk: over-abstraction for two providers. Mitigation: small interface, no plugin framework.
- Risk: config sprawl. Mitigation: only add provider, key, timeout, dimension.

## Security Considerations

- Treat `ai.embedding.api-key` as secret.
- Never include API key in exception messages.

## Next Steps

- Implement provider classes and selection logic.

## Unresolved Questions

- None.
