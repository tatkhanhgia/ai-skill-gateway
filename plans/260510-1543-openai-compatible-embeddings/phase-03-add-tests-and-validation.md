# Add Tests and Validation

## Context Links

- [Plan](./plan.md)
- [Implement Provider Strategy](./phase-02-implement-provider-strategy.md)
- [Testing Standards](../../docs/code-standards.md)
- [POM](../../pom.xml)

## Overview

Priority: P1  
Status: Complete  
Add deterministic tests without real network calls.

## Key Insights

- Current test suite is mostly pure unit tests.
- Code standards require network-free tests.
- Provider request/response mapping is the main risk.

## Requirements

- Test Ollama response parsing.
- Test OpenAI-compatible response parsing.
- Test OpenAI-compatible request body uses `input`, not `prompt`.
- Test bearer header behavior without exposing secret.
- Test dimension mismatch fails.
- Test invalid/empty embedding fails.

## Architecture

```text
Provider unit tests
  -> fake HTTP sender or parser/request builder helpers
  -> no external server
  -> assert parsed float[] and errors
```

## Related Code Files

- Create: `src/test/java/com/skillgateway/service/embedding/OllamaEmbeddingProviderTest.java`
- Create: `src/test/java/com/skillgateway/service/embedding/OpenAiCompatibleEmbeddingProviderTest.java`
- Optional create: `src/test/java/com/skillgateway/service/EmbeddingServiceTest.java`
- Modify only if needed: provider classes to allow fake transport injection.

## Implementation Steps

1. Prefer pure parser/request-builder tests over HTTP server tests.
2. If HTTP behavior must be tested, inject a tiny transport abstraction into providers.
3. Cover canonical payloads:
   - Ollama: `{ "embedding": [0.1, 0.2] }`
   - OpenAI-compatible: `{ "data": [{ "embedding": [0.1, 0.2] }] }`
4. Cover malformed payloads:
   - missing `embedding`
   - empty `data`
   - dimension mismatch
5. Add compile/test command to verification checklist.

## Todo List

- [x] Add Ollama parser tests.
- [x] Add OpenAI-compatible parser tests.
- [x] Add request body/header tests.
- [x] Add dimension validation tests.
- [x] Run `mvn test`.

## Success Criteria

- `mvn test` passes.
- Tests do not require Ollama, OpenAI, API key, or internet.
- Failure cases prove graceful-degradation path can trigger.

## Risk Assessment

- Risk: tests require implementation seams not present today. Mitigation: add small helper methods or transport abstraction, not heavy mocking framework.
- Risk: provider code grows over 200 lines. Mitigation: split parser/helper if needed.

## Security Considerations

- Use fake API key values only.
- Assert key is not included in exception messages.

## Next Steps

- Update docs and examples after behavior is verified.

## Unresolved Questions

- None.
