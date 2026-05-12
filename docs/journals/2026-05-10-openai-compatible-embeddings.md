# 2026-05-10 - OpenAI-Compatible Embeddings

Implemented provider strategy behind `EmbeddingService` while keeping Ollama default. Added OpenAI-compatible `/v1/embeddings` support with optional bearer auth, canonical `input` payload, `data[0].embedding` parsing, timeout config, and fixed-dimension validation.

Key decisions:
- Keep call sites stable in `SkillService` and `SearchService`.
- Preserve existing skill vectors when republish embedding refresh fails.
- Reject empty, dimension-mismatched, non-numeric, and non-finite provider vectors.
- Log sanitized degradation warnings without API keys or request/response bodies.
- Add sanitized embedding provider status endpoint and readiness health check.
- Harden status/health against URL query, userinfo, and invalid URL secret leaks.
- Redact embedding degradation log summaries so malformed provider URL messages cannot leak credentials.
- Reject non-HTTP, relative, and empty embedding URLs in status/readiness validation.
- Reject blank embedding model in status/readiness validation so health output cannot report an unusable provider config as valid.
- Make docs validator recursive and portable without shell `grep`.

Verification:
- `mvn test` passed: 28 tests, 0 failures.
- Docs validator passed clean: 123 code references validated, 6 internal links working.
- Code review found no critical/high issues after fixes.
- Follow-up verification for blank model validation passed `mvn -q -Dtest=EmbeddingServiceTest test` and `mvn -q test`.

Unresolved questions:
- None.
