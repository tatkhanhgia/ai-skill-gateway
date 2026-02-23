# Phase 03: Core API Endpoints

## Context Links

- [Plan Overview](./plan.md)
- [Implementation Guide](../skill-registry-implementation-guide.md)
- [MCP Spec Research](../research/mcp-specification-research-report.md)
- [Architecture Patterns - API Layer](../ARCHITECTURE-PATTERNS.md)

## Overview

- **Priority:** P0
- **Status:** completed
- **Description:** Implement MCP tool handlers and REST endpoints for skill CRUD: publish, get, list, yank. Includes skill.yaml manifest validation, service layer, and DTO records.

## Key Insights

- MCP tools exposed via Quarkus MCP extension annotations (`@Tool`, `@ToolArg`)
- REST endpoints serve admin/debug purposes; MCP tools are primary interface
- Java Records ideal for DTOs (immutable, compact, Jackson-friendly)
- Publish flow: validate manifest -> upsert skill -> insert version -> update tags -> rebuild search vector -> index embedding
- Panache active record or repository pattern simplifies DB access

## Requirements

### Functional
- **Publish skill**: Accept skill.yaml manifest, validate, store in DB, return skill_id + version_id
- **Get skill**: Return full metadata + latest version by name or ID
- **List skills**: Paginated listing with category/tag filters
- **Yank version**: Mark version as yanked (soft-delete for security/bugs)
- **MCP tools/list**: Advertise available tools to MCP clients

### Non-Functional
- Publish idempotent (ON CONFLICT updates existing)
- Input validation with clear error messages
- JSON-RPC error codes for MCP errors
- Response time < 100ms for single-skill lookups

## Architecture

```
MCP Client
    | tools/call "publish_skill"
    v
McpToolHandlers (Quarkus MCP annotations)
    |-- publishSkill(manifest) -> SkillService.publish()
    |-- getSkill(name) -> SkillService.findByName()
    |-- listSkills(category, tags, page) -> SkillService.list()
    |-- yankVersion(name, version) -> VersionService.yank()
    |
    v
SkillService
    |-- validate(SkillManifest)
    |-- publish(SkillManifest) -> Skill + SkillVersion
    |-- findByName(name) -> SkillDetail
    |-- list(filters, pagination) -> Page<SkillSummary>
    |
    v
SkillRepository / SkillVersionRepository (Panache)
    |
    v
PostgreSQL
```

## Related Code Files

### Create
- `src/main/java/com/skillgateway/model/dto/SkillManifest.java` (Record)
- `src/main/java/com/skillgateway/model/dto/SkillDetail.java` (Record)
- `src/main/java/com/skillgateway/model/dto/SkillSummary.java` (Record)
- `src/main/java/com/skillgateway/model/dto/PublishResponse.java` (Record)
- `src/main/java/com/skillgateway/service/SkillService.java`
- `src/main/java/com/skillgateway/service/ManifestValidator.java`
- `src/main/java/com/skillgateway/mcp/tools/SkillToolHandler.java`
- `src/main/java/com/skillgateway/api/SkillResource.java` (REST, admin)

### Modify
- `src/main/java/com/skillgateway/repository/SkillRepository.java` (add finders)

## Implementation Steps

1. **Define DTO Records**
   ```java
   // SkillManifest.java - input for publish
   public record SkillManifest(
     String name,
     String version,
     String description,
     String category,
     List<String> tags,
     String author,
     String repositoryUrl,
     List<String> requires,
     String releaseNotes
   ) {}

   // SkillDetail.java - full output
   public record SkillDetail(
     Long id, String name, String category, String description,
     String author, String repositoryUrl, int downloadCount,
     SkillVersionInfo latestVersion, List<String> tags
   ) {}
   ```

2. **Implement ManifestValidator**
   - Validate name (lowercase, hyphenated, 3-100 chars)
   - Validate version (valid SemVer string)
   - Validate category (one of 8 allowed categories)
   - Validate tags (max 20, lowercase, hyphenated)
   - Return list of validation errors or throw

3. **Implement SkillService**
   ```java
   @ApplicationScoped
   public class SkillService {
     @Inject SkillRepository skillRepo;
     @Inject SkillVersionRepository versionRepo;

     @Transactional
     public PublishResponse publish(SkillManifest manifest) {
       // 1. Validate
       // 2. Upsert skill (ON CONFLICT name)
       // 3. Insert version (fail if duplicate)
       // 4. Sync tags (delete old, insert new)
       // 5. Return response
     }

     public Optional<SkillDetail> findByName(String name) { ... }
     public Page<SkillSummary> list(String category, List<String> tags, int page, int size) { ... }
   }
   ```

4. **Implement MCP Tool Handlers**
   ```java
   @ApplicationScoped
   public class SkillToolHandler {
     @Inject SkillService skillService;

     @Tool(description = "Publish a new skill or version to the registry")
     public PublishResponse publishSkill(
       @ToolArg(description = "Skill manifest JSON") String manifestJson
     ) { ... }

     @Tool(description = "Get skill details by name")
     public SkillDetail getSkill(
       @ToolArg(description = "Skill name") String name
     ) { ... }

     @Tool(description = "List skills with optional filters")
     public List<SkillSummary> listSkills(
       @ToolArg(description = "Category filter") String category,
       @ToolArg(description = "Comma-separated tags") String tags,
       @ToolArg(description = "Page number") int page
     ) { ... }
   }
   ```

5. **Implement REST Resource** (admin/debug)
   ```java
   @Path("/api/v1/skills")
   public class SkillResource {
     @POST @Path("/publish") public Response publish(SkillManifest manifest);
     @GET @Path("/{name}") public SkillDetail get(@PathParam("name") String name);
     @GET public List<SkillSummary> list(@QueryParam params);
     @POST @Path("/{name}/versions/{version}/yank") public Response yank(...);
   }
   ```

6. **Error handling** - Global exception mapper for validation errors, not-found, conflicts

## Todo List

- [ ] Create DTO records (SkillManifest, SkillDetail, SkillSummary, PublishResponse)
- [ ] Implement ManifestValidator
- [ ] Implement SkillService.publish()
- [ ] Implement SkillService.findByName()
- [ ] Implement SkillService.list() with pagination
- [ ] Implement yank logic in VersionService
- [ ] Create SkillToolHandler with @Tool annotations
- [ ] Create SkillResource REST controller
- [ ] Create global exception mapper
- [ ] Test publish via MCP Inspector
- [ ] Test CRUD via REST client (curl/httpie)

## Success Criteria

- `tools/list` returns registered tools (publishSkill, getSkill, listSkills)
- Publish a skill manifest -> returns id + version_id
- Get skill by name -> returns full detail with latest version
- List skills with pagination and category filter works
- Yank marks version; yanked versions excluded from resolve
- Duplicate publish (same name+version) returns conflict error
- Invalid manifest returns 400 with field-level errors

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Quarkus MCP annotation API changes | Tool registration breaks | Pin Quarkus MCP extension version |
| Large manifest payload | Memory/timeout | Set max request size (1MB); validate early |
| Tag sync race condition | Inconsistent tags | Transactional boundary covers full publish |
| JSON serialization of Records | Incompatible output | Test Jackson serialization of all DTOs |

## Security Considerations

- Publish endpoint requires API key (Phase 06 adds auth)
- Input sanitization on all string fields (prevent XSS/injection)
- Rate limit publish endpoint (prevent abuse)
- Manifest name validation prevents path traversal characters

## Next Steps

- Phase 04: Add search endpoints (hybrid FTS + semantic)
- Phase 05: Version resolution with constraint parsing
