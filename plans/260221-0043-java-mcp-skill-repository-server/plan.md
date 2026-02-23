---
title: "Java MCP Skill Repository Server"
description: "REST API MCP server for AI skill discovery, versioning, and management using Java + PostgreSQL + Quarkus"
status: in-progress
priority: P1
effort: 40h
branch: master
tags: [java, mcp, quarkus, postgresql, skill-repository, rest-api]
created: 2026-02-21
---

# Java MCP Skill Repository Server - Implementation Plan

## Summary

Build a Java-based MCP server exposing REST API for AI skill discovery, publishing, versioning, and dependency resolution. Combines PostgreSQL full-text search with semantic vector search for hybrid skill retrieval.

## Tech Stack Decision

| Component | Choice | Rationale |
|-----------|--------|-----------|
| Framework | **Quarkus** | Official MCP extension, fastest native startup (0.049s), lowest memory |
| Database | **PostgreSQL** | Native FTS (tsvector), pgvector for embeddings, battle-tested |
| ORM | **Hibernate + Panache** | Quarkus-native, simplified CRUD, built-in connection pooling |
| Build | **Maven** | Standard, BOM support for MCP SDK versioning |
| Transport | **Streamable HTTP + SSE** | Multi-client, OAuth 2.1 ready, production-grade |
| JSON | **Jackson** | Framework default, Records support, best perf |

## Architecture

```
Client (Claude, AI Apps)
    |  JSON-RPC 2.0
    v
Quarkus MCP Server (Streamable HTTP + SSE)
    |
    +-- Tool Handlers (search, publish, resolve, versions)
    +-- Resource Handlers (skill manifests, categories)
    +-- Auth Layer (OAuth 2.1 / API Key)
    |
    +-- Service Layer
    |   +-- SkillService (CRUD, publish)
    |   +-- SearchService (hybrid: keyword + semantic)
    |   +-- VersionService (SemVer resolve, constraints)
    |
    +-- Data Layer
        +-- PostgreSQL (skills, versions, tags, tsvector FTS)
        +-- pgvector (skill description embeddings)
```

## Phases

| Phase | Title | Effort | Status |
|-------|-------|--------|--------|
| 01 | [Setup Environment](./phase-01-setup-environment.md) | 4h | completed |
| 02 | [Database Design](./phase-02-database-design.md) | 6h | completed |
| 03 | [Core API Endpoints](./phase-03-core-api-endpoints.md) | 10h | completed |
| 04 | [Search & Discovery](./phase-04-search-discovery.md) | 8h | completed |
| 05 | [Version Management](./phase-05-version-management.md) | 6h | completed |
| 06 | [Testing & Deployment](./phase-06-testing-deployment.md) | 6h | in-progress |

## Key Dependencies

- Java 21+ (virtual threads, records)
- Quarkus 3.x + MCP extension
- PostgreSQL 16+ (tsvector, pgvector)
- MCP Java SDK v0.12.1+ (`io.modelcontextprotocol.sdk:mcp`)
- Ollama (local embeddings via nomic-embed-text)

## Research References

- [MCP Spec Research](../research/mcp-specification-research-report.md)
- [Java Framework Research](../research-java-mcp-frameworks.md)
- [Skill Repository Design](../skill-repository-design-research.md)
- [Architecture Patterns](../ARCHITECTURE-PATTERNS.md)
- [Implementation Guide](../skill-registry-implementation-guide.md)
- [Deployment Research](../mcp-server-deployment-research.md)

## Success Criteria

- MCP-compliant server discoverable via `tools/list`
- Hybrid search returns relevant skills within 200ms (keyword + semantic)
- SemVer constraint resolution handles ^, ~, ranges, prerelease
- Publish workflow validates skill.yaml manifest and indexes
- PostgreSQL FTS + pgvector dual-index approach operational
- API key auth protecting write endpoints
- Native compilation via GraalVM produces <50MB binary
