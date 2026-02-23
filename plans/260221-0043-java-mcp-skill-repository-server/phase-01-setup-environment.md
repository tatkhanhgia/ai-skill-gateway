# Phase 01: Setup Environment

## Context Links

- [Plan Overview](./plan.md)
- [Java Framework Research](../research-java-mcp-frameworks.md)
- [MCP Java SDK Docs](https://modelcontextprotocol.io/sdk/java/mcp-overview)
- [Quarkus MCP Extension](https://quarkus.io/blog/mcp-server/)

## Overview

- **Priority:** P0
- **Status:** completed
- **Description:** Bootstrap Quarkus project with MCP SDK, PostgreSQL, and dev tooling. Establish project structure, build pipeline, and local dev environment.

## Key Insights

- Quarkus MCP extension eliminates boilerplate for tool/resource registration
- Java 21 virtual threads improve concurrency without reactive complexity
- Panache simplifies JPA with active record or repository patterns
- MCP SDK BOM (`mcp-bom:0.12.1`) manages transitive dependency versions
- GraalVM native compilation requires build-time metadata (Quarkus handles this)

## Requirements

### Functional
- Quarkus project compiles and starts with MCP transport
- PostgreSQL connection pool operational (Agroal)
- Health check endpoint responds at `/q/health`
- MCP initialization handshake completes successfully

### Non-Functional
- JVM startup < 3s; native startup < 0.1s
- Dev mode with hot-reload (quarkus:dev)
- Docker Compose for local PostgreSQL + pgvector

## Architecture

```
ai-skill-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/skillgateway/
│   │   │   ├── config/              # App configuration beans
│   │   │   ├── model/               # JPA entities + DTOs (records)
│   │   │   ├── repository/          # Panache repositories
│   │   │   ├── service/             # Business logic
│   │   │   ├── mcp/                 # MCP tool/resource handlers
│   │   │   │   ├── tools/           # Tool implementations
│   │   │   │   └── resources/       # Resource handlers
│   │   │   └── api/                 # REST endpoints (admin/debug)
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/migration/        # Flyway SQL migrations
│   │       └── META-INF/
│   └── test/
│       └── java/com/skillgateway/
├── docker-compose.yml               # PostgreSQL + pgvector
├── pom.xml
└── README.md
```

## Related Code Files

### Create
- `pom.xml` - Maven build with Quarkus BOM + MCP SDK + PostgreSQL + Flyway
- `src/main/resources/application.properties` - Quarkus config
- `docker-compose.yml` - PostgreSQL 16 + pgvector extension
- `src/main/java/com/skillgateway/config/AppConfig.java` - Central config
- `.mvn/wrapper/` - Maven wrapper for reproducible builds

## Implementation Steps

1. **Generate Quarkus project**
   ```bash
   mvn io.quarkus.platform:quarkus-maven-plugin:3.x:create \
     -DprojectGroupId=com.skillgateway \
     -DprojectArtifactId=ai-skill-gateway \
     -Dextensions="rest-jackson,hibernate-orm-panache,jdbc-postgresql,flyway,smallrye-health,quarkus-mcp-server-sse"
   ```

2. **Configure pom.xml dependencies**
   - Add MCP SDK BOM: `io.modelcontextprotocol.sdk:mcp-bom:0.12.1`
   - Add MCP core: `io.modelcontextprotocol.sdk:mcp`
   - Add pgvector Java library: `com.pgvector:pgvector:0.1.6`
   - Pin Java version to 21

3. **Setup application.properties**
   ```properties
   quarkus.datasource.db-kind=postgresql
   quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/skillgateway
   quarkus.datasource.username=admin
   quarkus.datasource.password=${DB_PASSWORD:changeme}
   quarkus.hibernate-orm.database.generation=none
   quarkus.flyway.migrate-at-start=true
   ```

4. **Create docker-compose.yml**
   - PostgreSQL 16 with pgvector extension
   - Persistent volume for data
   - Port 5432 exposed

5. **Implement basic MCP server skeleton**
   - Register server identity and capabilities
   - Implement `tools/list` returning empty list (placeholder)
   - Verify handshake with MCP Inspector

6. **Verify dev loop**
   - `mvn quarkus:dev` starts with hot-reload
   - Health check at `/q/health` returns UP
   - PostgreSQL connection verified at startup

## Todo List

- [x] Generate Quarkus project scaffold
- [x] Configure pom.xml with all dependencies
- [x] Create docker-compose.yml for PostgreSQL + pgvector
- [x] Write application.properties
- [x] Create AppConfig bean
- [x] Implement MCP server skeleton (empty tools/list)
- [x] Verify MCP handshake with Inspector
- [x] Verify health check endpoint
- [x] Test hot-reload in dev mode
- [x] Document local setup in README

## Success Criteria

- `mvn quarkus:dev` starts without errors
- `/q/health` returns `{"status": "UP"}`
- MCP client can connect and receive capabilities response
- PostgreSQL connection established on startup
- Flyway runs with empty migration (baseline)

## Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Quarkus MCP extension version mismatch | Build fails | Pin exact versions; check Quarkus extension catalog |
| pgvector not in PostgreSQL Docker image | Vector search unavailable | Use `pgvector/pgvector:pg16` Docker image |
| Java 21 not installed on dev machine | Can't compile | Use SDKMAN or Maven toolchains |
| MCP SDK breaking changes | API incompatibility | Pin BOM version; check changelog before upgrade |

## Security Considerations

- DB password via environment variable, never in source
- Dev mode binds to localhost only
- `.env` file gitignored
- No default admin credentials

## Next Steps

- Phase 02: Database schema design with Flyway migrations
- Phase 03: Core API endpoints (publish, search, resolve)
