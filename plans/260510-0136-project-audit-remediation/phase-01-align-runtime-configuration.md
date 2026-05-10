# Phase 01: Align Runtime Configuration

## Context Links

- [Plan Overview](./plan.md)
- [Scout Report](./reports/audit-remediation-scout-report.md)
- [README](../../README.md)
- [Application Properties](../../src/main/resources/application.properties)
- [Docker Compose](../../docker-compose.yml)

## Overview

- **Date:** 2026-05-10
- **Description:** Make local dev, Docker, README, health checks, and API examples use one canonical HTTP port and consistent Java runtime story.
- **Priority:** P1
- **Implementation status:** pending
- **Review status:** pending

## Key Insights

- Quarkus is configured with `quarkus.http.port=7070`.
- Dockerfiles expose `8080`, compose maps `8080:8080`, README curls `8080`.
- `pom.xml` compiles with `maven.compiler.release=17`, while docs claim Java 21.

## Requirements

- Pick one canonical HTTP port.
- Align `application.properties`, Dockerfiles, `docker-compose.yml`, README, and docs.
- Align Java prerequisite wording with actual compiler target or upgrade compiler target intentionally.

## Architecture

Runtime config should have one source of truth:

```text
application.properties -> Docker expose/map -> README curl examples -> health check docs
```

## Related Code Files

- Modify: `src/main/resources/application.properties`
- Modify: `Dockerfile.jvm`
- Modify: `Dockerfile.native`
- Modify: `docker-compose.yml`
- Modify: `README.md`
- Modify: `docs/*.md` where Java/port claims appear

## Implementation Steps

1. Decide canonical port. Recommended: `8080` for Docker/README consistency and Quarkus default.
2. Update `quarkus.http.port` or Docker mappings accordingly.
3. Add explicit `AI_EMBEDDING_URL` for Docker server to reach `ollama` service if needed.
4. Align Java prerequisite: either document Java 17+ or change compiler release to 21 after verifying.
5. Run Maven tests and a Docker config sanity check.

## Todo List

- [ ] Choose canonical port
- [ ] Update app/Docker/README/docs port references
- [ ] Check Docker service-to-service Ollama URL
- [ ] Align Java version docs or compiler target
- [ ] Run `mvn test`

## Success Criteria

- README commands match actual server port.
- Docker container exposes a reachable app port.
- Java prerequisite matches `pom.xml`.
- No stale `localhost:8080` or `7070` conflicts remain except intentional notes.

## Risk Assessment

- Changing port can break existing user scripts. Mitigation: note change in docs and keep env override possible.
- Changing Java release to 21 can break local Java 17 users. Mitigation: prefer doc alignment unless Java 21 APIs are required.

## Security Considerations

- Do not hardcode production secrets while editing compose/env examples.
- Keep API key examples clearly marked as development only.

## Next Steps

- Phase 2: fix DB vector schema/search behavior.

## Unresolved Questions

- Canonical port: `8080` recommended, but needs user confirmation if existing deployments use `7070`.
