# Project Roadmap - Java MCP Skill Gateway

**Status:** Active Development
**Last Updated:** 2026-02-24
**Version:** 1.0.0-SNAPSHOT

---

## Phase Overview

| Phase | Status | Progress | Target |
|-------|--------|----------|--------|
| Phase 1: Core Infrastructure | Complete | 100% | 2026-02-20 |
| Phase 2: Skill Management | Complete | 100% | 2026-02-22 |
| Phase 3: Search & Discovery | Complete | 100% | 2026-02-24 |
| Phase 4: Version & Dependency | In Progress | 80% | 2026-02-28 |
| Phase 5: MCP Integration | Complete | 100% | 2026-02-24 |
| Phase 6: Production Hardening | Pending | 0% | 2026-03-15 |

---

## Phase 1: Core Infrastructure (Complete)

**Deliverables:**
- Quarkus 3.20.2 project scaffold with Java 21
- PostgreSQL with pgvector extension
- Flyway migrations (V1-V3)
- Health endpoints (`/q/health`)
- API key authentication filter
- Global exception handling

**Key Files:**
- `pom.xml` - Maven build configuration
- `application.properties` - Runtime configuration
- `ApiKeyFilter.java` - Security filter
- `GlobalExceptionMapper.java` - Error handling

---

## Phase 2: Skill Management (Complete)

**Deliverables:**
- Skill entity and repository (Panache)
- SkillVersion entity with metadata
- Tag management
- CRUD REST endpoints
- Manifest validation

**API Endpoints:**
- `POST /api/v1/skills/publish` - Publish skill
- `GET /api/v1/skills/{name}` - Get skill detail
- `GET /api/v1/skills` - List skills with filters

**Key Files:**
- `Skill.java`, `SkillVersion.java` - Entities
- `SkillRepository.java`, `SkillVersionRepository.java` - Persistence
- `SkillService.java` - Business logic
- `ManifestValidator.java` - Validation

---

## Phase 3: Search & Discovery (Complete)

**Deliverables:**
- Hybrid search (keyword + semantic + popularity)
- Embedding service integration
- Full-text search with tsvector
- Vector similarity search
- Configurable search weights

**API Endpoints:**
- `GET /api/v1/skills/search` - Search skills

**Key Files:**
- `SearchService.java` - Search orchestration
- `EmbeddingService.java` - AI embedding calls
- `SkillRepository.java` - Search queries
- `V2__create_search_indexes.sql` - Search indexes
- `V3__create_search_vector_trigger.sql` - FTS trigger

**Search Configuration:**
```properties
search.weight.keyword=0.3
search.weight.semantic=0.5
search.weight.popularity=0.2
search.default-limit=20
search.max-limit=50
```

---

## Phase 4: Version & Dependency (In Progress)

**Deliverables:**
- Semantic versioning (SemVer)
- Version constraint resolution
- Dependency tree traversal
- Yank/unyank versions
- Circular dependency detection

**API Endpoints:**
- `GET /api/v1/skills/{name}/versions` - List versions
- `GET /api/v1/skills/{name}/resolve` - Resolve version constraint
- `POST /api/v1/skills/{name}/versions/{version}/yank` - Yank version
- `GET /api/v1/skills/{name}/dependencies/{version}` - Get dependency tree

**Key Files:**
- `SemVer.java`, `SemVerParser.java`, `SemVerConstraint.java` - Version handling
- `VersionService.java` - Version operations
- `DependencyResolver.java` - Dependency resolution
- `VersionToolHandler.java` - MCP tools

**Progress:** 80% - Core logic complete, needs integration tests

---

## Phase 5: MCP Integration (Complete)

**Deliverables:**
- MCP server HTTP transport
- Tool handlers for skills, search, versions
- JSON-RPC message handling

**MCP Tools:**
- `publishSkill` - Publish via MCP
- `getSkill` - Retrieve skill details
- `listSkills` - List with filters
- `searchSkills` - Hybrid search
- `resolveVersion` - Version resolution
- `listVersions` - Version listing
- `yankVersion` - Version yanking

**Key Files:**
- `SkillToolHandler.java`
- `SearchToolHandler.java`
- `VersionToolHandler.java`

---

## Phase 6: Production Hardening (Pending)

**Planned Deliverables:**
- Rate limiting
- Caching layer (Redis)
- Metrics and monitoring (Micrometer)
- Structured logging
- API documentation (OpenAPI)
- Integration tests
- Performance benchmarks
- Docker multi-stage builds
- Kubernetes manifests

**Target:** 2026-03-15

---

## Success Metrics

| Metric | Current | Target |
|--------|---------|--------|
| Code Coverage | ~40% | >=80% |
| Search Latency (p95) | ~300ms | <500ms |
| API Uptime | N/A | >=99% |
| MCP Tool Coverage | 7 tools | 10 tools |

---

## Dependencies

- Java 21+
- Maven 3.9+
- PostgreSQL 15+ with pgvector
- Ollama (for embeddings)
- Docker (for local development)

---

## Notes

- All dates are tentative and subject to change
- Priorities may shift based on user feedback
- Weekly reviews scheduled to track progress
