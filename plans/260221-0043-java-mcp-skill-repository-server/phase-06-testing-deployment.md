# Phase 06: Testing & Deployment

## Context Links

- [Plan Overview](./plan.md)
- [MCP Deployment Research](../mcp-server-deployment-research.md)
- [MCP Research Summary](../research/mcp-research-summary.md)
- [Java Framework Research](../research-java-mcp-frameworks.md)

## Overview

- **Priority:** P1
- **Status:** in-progress
- **Description:** Comprehensive test suite (unit + integration + MCP protocol), API key auth, native compilation, Docker image, and deployment configuration.

## Key Insights

- Quarkus test framework supports `@QuarkusTest` with CDI injection and Testcontainers
- `@QuarkusIntegrationTest` for native binary testing
- DevServices auto-starts PostgreSQL container during tests
- MCP Inspector for manual protocol verification
- GraalVM native image < 50MB, startup < 100ms
- API key auth simplest secure approach for MVP (upgrade to OAuth 2.1 later)

## Progress

- Java 21 runtime now installed across local, CI, and test machines to keep native builds aligned with production
- `./mvnw verify` completed (unit + integration + MCP protocol suites) and passed on Java 21, verifying hybrid publish/search/resolve flows
- Testcontainers-based PostgreSQL started cleanly and pgvector indexes honored during runs

## Remaining Gaps

- API key filter still needs implementation and tests for write-path enforcement (publish/yank)
- Docker artifacts (JVM + native) require finalization along with runtime configuration doc updates
- GitHub Actions workflow needs wiring to the new tests and native-graal build steps
- Documentation (README, roadmap, changelog) still needs updates reflecting Java 21 requirement and test status

## Requirements

### Functional
- Unit tests: SemVer parser, constraint matching, score normalization
- Integration tests: full publish -> search -> resolve flow against real DB
- MCP protocol tests: tools/list, tools/call with valid/invalid payloads
- API key auth on write endpoints (publish, yank)
- Docker image for deployment
- Native compilation via GraalVM

### Non-Functional
- Test coverage > 80% on service + version packages
- All tests pass in CI/CD (GitHub Actions)
- Native image < 50MB
- Docker image < 100MB (distroless base)
- Health check at `/q/health` verifiable by orchestrator

## Architecture

```
Testing Pyramid
    /\
   /  \  E2E: MCP Inspector manual tests
  /    \
 / Int  \ Integration: Quarkus + Testcontainers + real PostgreSQL
/________\
   Unit    SemVer, constraints, validators, score normalization

Deployment
    Docker Image (Quarkus native)
    |-- GraalVM native binary
    |-- application.properties (externalized)
    |-- PostgreSQL connection via env vars
    |-- Exposed port: 8080
```

## Related Code Files

### Create
- `src/test/java/com/skillgateway/version/SemVerParserTest.java`
- `src/test/java/com/skillgateway/version/SemVerConstraintTest.java`
- `src/test/java/com/skillgateway/service/SkillServiceTest.java`
- `src/test/java/com/skillgateway/service/SearchServiceTest.java`
- `src/test/java/com/skillgateway/service/VersionServiceTest.java`
- `src/test/java/com/skillgateway/service/DependencyResolverTest.java`
- `src/test/java/com/skillgateway/mcp/McpProtocolTest.java`
- `src/test/java/com/skillgateway/api/SkillResourceTest.java`
- `src/main/java/com/skillgateway/config/ApiKeyFilter.java`
- `Dockerfile.jvm` (JVM mode)
- `Dockerfile.native` (native mode)
- `.github/workflows/ci.yml`

## Implementation Steps

1. **Unit Tests - SemVer**
   ```java
   @Test void parseValidVersion() {
     SemVer v = SemVerParser.parse("1.2.3-beta.1+build.42");
     assertEquals(1, v.major());
     assertEquals("beta.1", v.prerelease());
   }

   @Test void caretConstraintMatches() {
     var c = SemVerConstraint.parse("^1.2.3");
     assertTrue(c.matches(SemVerParser.parse("1.5.0")));
     assertFalse(c.matches(SemVerParser.parse("2.0.0")));
   }

   @Test void invalidVersionThrows() {
     assertThrows(InvalidVersionException.class,
       () -> SemVerParser.parse("not.a.version"));
   }
   ```

2. **Integration Tests - Publish + Search**
   ```java
   @QuarkusTest
   class SkillServiceTest {
     @Inject SkillService skillService;

     @Test void publishAndSearch() {
       var manifest = new SkillManifest("test-skill", "1.0.0",
         "A test skill for analytics", "Analytics", List.of("test"), ...);
       var result = skillService.publish(manifest);
       assertNotNull(result.id());

       var found = searchService.search(
         new SearchRequest("analytics test", null, null, 10));
       assertFalse(found.isEmpty());
       assertEquals("test-skill", found.get(0).name());
     }
   }
   ```

3. **MCP Protocol Tests**
   ```java
   @QuarkusTest
   class McpProtocolTest {
     @Test void toolsListReturnsRegisteredTools() {
       // POST /mcp with JSON-RPC tools/list request
       // Verify response contains publishSkill, searchSkills, etc.
     }

     @Test void toolsCallSearchReturnsResults() {
       // POST /mcp with tools/call "searchSkills"
       // Verify response format
     }
   }
   ```

4. **API Key Auth Filter**
   ```java
   @Provider
   @Priority(Priorities.AUTHENTICATION)
   public class ApiKeyFilter implements ContainerRequestFilter {
     @ConfigProperty(name = "auth.api-key")
     String validApiKey;

     @Override
     public void filter(ContainerRequestContext ctx) {
       // Skip for read-only endpoints (GET, tools/list, search)
       if (isReadOnly(ctx)) return;

       String apiKey = ctx.getHeaderString("X-API-Key");
       if (!validApiKey.equals(apiKey)) {
         ctx.abortWith(Response.status(401).build());
       }
     }
   }
   ```

5. **Dockerfile.native**
   ```dockerfile
   FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21 AS build
   COPY --chown=quarkus:quarkus . /code
   WORKDIR /code
   RUN ./mvnw package -Pnative -DskipTests

   FROM quay.io/quarkus/quarkus-micro-image:2.0
   COPY --from=build /code/target/*-runner /application
   EXPOSE 8080
   CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
   ```

6. **GitHub Actions CI**
   ```yaml
   name: CI
   on: [push, pull_request]
   jobs:
     test:
       runs-on: ubuntu-latest
       services:
         postgres:
           image: pgvector/pgvector:pg16
           env:
             POSTGRES_DB: skillgateway
             POSTGRES_USER: admin
             POSTGRES_PASSWORD: testpass
           ports: ["5432:5432"]
       steps:
         - uses: actions/checkout@v4
         - uses: actions/setup-java@v4
           with: { distribution: temurin, java-version: 21 }
         - run: ./mvnw verify
   ```

7. **Docker Compose update** - Add server service alongside PostgreSQL
   ```yaml
   services:
     db:
       image: pgvector/pgvector:pg16
       ...
     server:
       build:
         context: .
         dockerfile: Dockerfile.jvm
       depends_on: [db]
       ports: ["8080:8080"]
       environment:
         QUARKUS_DATASOURCE_JDBC_URL: jdbc:postgresql://db:5432/skillgateway
         AUTH_API_KEY: ${API_KEY}
   ```

## Todo List

- [x] Write SemVerParser unit tests (valid, invalid, edge cases)
- [x] Write SemVerConstraint unit tests (all operators)
- [x] Write ManifestValidator unit tests
- [x] Write SkillService integration tests (publish, get, list)
- [x] Write SearchService integration tests (keyword, semantic, hybrid)
- [x] Write VersionService integration tests (resolve, list versions)
- [x] Write DependencyResolver tests (happy path, circular)
- [x] Write MCP protocol tests (tools/list, tools/call)
- [x] Write REST API tests (SkillResource)
- [ ] Implement ApiKeyFilter for write protection
- [ ] Create Dockerfile.jvm
- [ ] Create Dockerfile.native
- [ ] Create GitHub Actions CI workflow
- [ ] Update docker-compose.yml with server service
- [ ] Test native compilation locally
- [ ] Verify health check in Docker
- [ ] Document API key setup in README

## Success Criteria

- All unit tests pass (SemVer, constraints, validation)
- All integration tests pass against Testcontainers PostgreSQL
- MCP protocol tests verify tools/list and tools/call
- API key required for publish/yank; rejected without key
- Read endpoints (search, get, list) accessible without auth
- Native binary < 50MB; starts in < 100ms
- Docker image builds and runs successfully
- GitHub Actions CI green on push

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| GraalVM reflection issues | Native build fails | Use Quarkus `@RegisterForReflection`; test native regularly |
| Testcontainers Docker requirement | CI needs Docker | GitHub Actions supports services; local devs need Docker |
| pgvector not in Testcontainers image | Vector tests fail | Use `pgvector/pgvector:pg16` image in test config |
| API key in environment | Leaked in logs | Never log auth headers; use Quarkus secret config |
| Flaky integration tests | CI failures | Use `@TestTransaction` for isolation; deterministic test data |

## Security Considerations

- API key stored in environment variable, never in source
- HTTPS termination at load balancer / reverse proxy level
- Health endpoint does not expose sensitive info
- Docker image runs as non-root user (Quarkus micro image default)
- Native binary has smaller attack surface (no JVM)
- Future: upgrade to OAuth 2.1 with PKCE for production multi-tenant

## Next Steps

- After Phase 06 completion, system is MVP-ready
- Future enhancements:
  - OAuth 2.1 authentication
  - Rate limiting (Quarkus rate-limit extension)
  - Monitoring (Micrometer + Prometheus)
  - Web UI for skill browsing
  - Webhook notifications on publish
  - Multi-region registry federation
