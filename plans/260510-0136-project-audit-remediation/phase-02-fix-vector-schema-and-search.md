# Phase 02: Fix Vector Schema and Search

## Context Links

- [Plan Overview](./plan.md)
- [Scout Report](./reports/audit-remediation-scout-report.md)
- [SkillRepository](../../src/main/java/com/skillgateway/repository/SkillRepository.java)
- [Skill Entity](../../src/main/java/com/skillgateway/model/Skill.java)
- [DB Migration V1](../../src/main/resources/db/migration/V1__create_skills_schema.sql)

## Overview

- **Date:** 2026-05-10
- **Description:** Make semantic search schema and query compatible with pgvector, and add coverage for DB-backed search behavior.
- **Priority:** P1
- **Implementation status:** pending
- **Review status:** pending

## Key Insights

- Migration creates `embedding TEXT`.
- Entity declares `columnDefinition = "vector(768)"`.
- Native query uses `s.embedding <=> CAST(:vec AS vector)`.
- Current tests do not exercise repository vector search with real PostgreSQL/pgvector.

## Requirements

- Store embeddings in a pgvector-compatible column.
- Keep publish/search graceful when embedding provider fails.
- Ensure migrations work for new DBs and, if needed, existing DBs.
- Add tests that would fail on the current TEXT/vector mismatch.

## Architecture

```text
EmbeddingService -> vector literal -> Skill.embedding column -> pgvector <=> query -> SearchService scoring
```

## Related Code Files

- Modify: `src/main/resources/db/migration/V1__create_skills_schema.sql`
- Modify/add: `src/main/resources/db/migration/V4__*.sql`
- Modify: `src/main/java/com/skillgateway/model/Skill.java`
- Modify: `src/main/java/com/skillgateway/repository/SkillRepository.java`
- Add/modify tests under `src/test/java`

## Implementation Steps

1. Choose migration strategy:
   - For unreleased DB: update V1 to `embedding vector(768)`.
   - For existing DB support: add V4 converting `TEXT` to `vector(768)` where possible.
2. Update V2 to create vector index if supported and useful.
3. Ensure repository query casts parameter to `vector(768)` consistently.
4. Validate embedding dimension before persisting/searching.
5. Add integration test with PostgreSQL/pgvector if test infra available; otherwise add repository/service tests around SQL generation limits and document remaining gap.
6. Run `mvn test`.

## Todo List

- [ ] Decide migration strategy
- [ ] Fix `embedding` schema type
- [ ] Add vector index or document why omitted
- [ ] Validate embedding dimension
- [ ] Add DB/search test coverage
- [ ] Run `mvn test`

## Success Criteria

- Fresh DB migration creates pgvector-compatible `skills.embedding`.
- Semantic search query works with persisted embeddings.
- Embedding provider failure still returns keyword-only search results.
- Test suite catches schema/query regressions.

## Risk Assessment

- Converting existing TEXT rows may fail if malformed. Mitigation: `NULLIF`/safe conversion or migration note.
- pgvector availability differs by DB image. Mitigation: keep `pgvector/pgvector:pg16` in compose and test docs.

## Security Considerations

- Keep native query parameterized.
- Do not log raw embedding payloads unnecessarily.

## Next Steps

- Phase 3: enforce/document API security contract.

## Unresolved Questions

- Is backward migration support required for existing local DB volumes?
