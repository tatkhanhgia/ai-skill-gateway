# Phase 04: Search & Discovery

## Context Links

- [Plan Overview](./plan.md)
- [Architecture Patterns - Search Flow](../ARCHITECTURE-PATTERNS.md)
- [Skill Repository Design - Discovery](../skill-repository-design-research.md)
- [Research Summary - Hybrid Search](../RESEARCH-SUMMARY.md)

## Overview

- **Priority:** P1
- **Status:** completed
- **Description:** Implement hybrid search combining PostgreSQL full-text search (tsvector) with pgvector semantic search. Includes query parsing, score combination, tag/category filtering, and reranking.

## Key Insights

- Hybrid approach: keyword (0.3) + semantic (0.5) + popularity (0.2) scoring
- tsvector handles recall (finding relevant docs); semantic handles precision (ranking by meaning)
- ts_rank() for FTS scoring; cosine similarity for vector scoring
- Tag filtering applied as WHERE clause before scoring (pre-filter, not post-filter)
- Embedding generation: call Ollama `nomic-embed-text` model via HTTP
- 8 fixed categories + unlimited flat tags for discovery taxonomy

## Requirements

### Functional
- **Keyword search**: Query parsed to tsquery, matched against tsvector, ranked by ts_rank
- **Semantic search**: Query embedded via Ollama, cosine similarity against skill embeddings
- **Hybrid search**: Combine keyword + semantic + popularity scores
- **Tag filter**: Filter results by tag list (AND/OR logic)
- **Category filter**: Single-category filter
- **MCP tool**: `search_skills` tool callable from AI clients

### Non-Functional
- Search response < 200ms for hybrid query (p95)
- Support up to 10K skills without degradation
- Graceful fallback: if embedding service unavailable, use keyword-only
- Paginated results (default 20, max 50)

## Architecture

```
search_skills(query, category?, tags?, limit?)
    |
    v
SearchService.search()
    |
    +-- 1. Parse query
    |
    +-- 2. Keyword Search (PostgreSQL)
    |     SELECT id, ts_rank(search_vector, query) AS kw_score
    |     FROM skills
    |     WHERE search_vector @@ plainto_tsquery(query)
    |
    +-- 3. Semantic Search (pgvector)
    |     SELECT id, 1 - (embedding <=> query_vec) AS sem_score
    |     FROM skills
    |     ORDER BY embedding <=> query_vec
    |     LIMIT 50
    |
    +-- 4. Merge & Score
    |     score = 0.3 * normalize(kw_score)
    |           + 0.5 * sem_score
    |           + 0.2 * normalize(download_count)
    |
    +-- 5. Filter (category, tags, yanked=false)
    |
    +-- 6. Return top-K sorted by score
```

## Related Code Files

### Create
- `src/main/java/com/skillgateway/service/SearchService.java`
- `src/main/java/com/skillgateway/service/EmbeddingService.java`
- `src/main/java/com/skillgateway/model/dto/SearchRequest.java` (Record)
- `src/main/java/com/skillgateway/model/dto/SearchResult.java` (Record)
- `src/main/java/com/skillgateway/mcp/tools/SearchToolHandler.java`

### Modify
- `src/main/java/com/skillgateway/repository/SkillRepository.java` (add search queries)
- `src/main/java/com/skillgateway/service/SkillService.java` (call embedding on publish)

## Implementation Steps

1. **EmbeddingService** - Call Ollama API for embeddings
   ```java
   @ApplicationScoped
   public class EmbeddingService {
     @ConfigProperty(name = "ai.embedding.model")
     String model; // nomic-embed-text

     @ConfigProperty(name = "ai.embedding.url")
     String ollamaUrl; // http://localhost:11434

     public float[] embed(String text) {
       // POST http://localhost:11434/api/embeddings
       // body: {"model": "nomic-embed-text", "prompt": text}
       // returns: {"embedding": [0.1, 0.2, ...]}
     }
   }
   ```

2. **SearchService** - Hybrid search orchestration
   ```java
   @ApplicationScoped
   public class SearchService {
     @Inject SkillRepository skillRepo;
     @Inject EmbeddingService embeddingService;

     public List<SearchResult> search(SearchRequest request) {
       // 1. Keyword search via native query
       List<ScoredSkill> keywordResults = skillRepo.keywordSearch(
         request.query(), 50);

       // 2. Semantic search (if embedding available)
       List<ScoredSkill> semanticResults = List.of();
       try {
         float[] queryVec = embeddingService.embed(request.query());
         semanticResults = skillRepo.vectorSearch(queryVec, 50);
       } catch (Exception e) { /* fallback to keyword-only */ }

       // 3. Merge scores
       // 4. Apply category/tag filters
       // 5. Sort by combined score, return top-K
     }
   }
   ```

3. **Native SQL queries** in SkillRepository
   ```java
   @RegisterForReflection
   public class SkillRepository implements PanacheRepository<Skill> {

     public List<ScoredSkill> keywordSearch(String query, int limit) {
       return getEntityManager().createNativeQuery("""
         SELECT s.id, s.name, s.description, s.category, s.download_count,
                ts_rank(s.search_vector, plainto_tsquery('english', :query)) as score
         FROM skills s
         WHERE s.search_vector @@ plainto_tsquery('english', :query)
         ORDER BY score DESC LIMIT :limit
         """)
         .setParameter("query", query)
         .setParameter("limit", limit)
         .getResultList();
     }

     public List<ScoredSkill> vectorSearch(float[] embedding, int limit) {
       return getEntityManager().createNativeQuery("""
         SELECT s.id, s.name, s.description, s.category, s.download_count,
                1 - (s.embedding <=> CAST(:vec AS vector)) as score
         FROM skills s
         WHERE s.embedding IS NOT NULL
         ORDER BY s.embedding <=> CAST(:vec AS vector)
         LIMIT :limit
         """)
         .setParameter("vec", Arrays.toString(embedding))
         .setParameter("limit", limit)
         .getResultList();
     }
   }
   ```

4. **Update publish flow** - Generate and store embedding on publish
   ```java
   // In SkillService.publish():
   float[] embedding = embeddingService.embed(
     manifest.name() + " " + manifest.description());
   skill.setEmbedding(embedding);
   ```

5. **MCP Search Tool**
   ```java
   @Tool(description = "Search skills by query with hybrid keyword + semantic matching")
   public List<SearchResult> searchSkills(
     @ToolArg(description = "Search query") String query,
     @ToolArg(description = "Category filter (optional)") String category,
     @ToolArg(description = "Comma-separated tag filter (optional)") String tags,
     @ToolArg(description = "Max results (default 20)") int limit
   ) { ... }
   ```

6. **Score normalization** - Min-max normalize keyword scores to [0,1] before combining

## Todo List

- [x] Implement EmbeddingService (Ollama HTTP client)
- [x] Implement SearchService with hybrid logic
- [x] Add keywordSearch native query to SkillRepository
- [x] Add vectorSearch native query to SkillRepository
- [x] Implement score normalization + combination
- [x] Add tag/category filtering to search
- [x] Update publish flow to generate embeddings
- [x] Create SearchToolHandler MCP tool
- [x] Add REST endpoint for search (GET /api/v1/skills/search)
- [x] Test with sample skills: verify ranking quality
- [x] Test fallback when Ollama is unavailable
- [x] Benchmark search latency with 100+ skills

## Success Criteria

- Keyword search returns results matching tsvector query
- Semantic search returns conceptually similar skills (not just keyword match)
- Hybrid scores rank relevant skills higher than keyword-only
- Category/tag filters narrow results correctly
- Search completes < 200ms with 100 skills
- Graceful degradation when embedding service offline

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Ollama not running | No semantic search | Fallback to keyword-only; log warning |
| Embedding dimension mismatch | Vector query fails | Validate embedding length before storage |
| Score normalization edge cases | Zero-division | Handle empty result sets; min=max case |
| pgvector query syntax | SQL errors | Test native queries in psql first |
| Large embedding generation time | Slow publish | Async embedding; publish returns before embedding complete |

## Security Considerations

- Search queries sanitized via parameterized SQL (no injection)
- Rate limit search endpoint (prevent resource exhaustion)
- Embedding service internal-only (localhost)
- Query logging excludes sensitive content

## Next Steps

- Phase 05: Version resolution with SemVer constraint parsing
- Phase 06: Testing and deployment pipeline
