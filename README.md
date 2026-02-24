# AI Skill Gateway

> Java MCP Skill Gateway Server - Self-hosted skill registry with hybrid search and semantic versioning

## ✨ Features

- 🔍 **Hybrid Search** - Combines keyword, semantic, and popularity signals
- 🤖 **MCP Integration** - Exposes tools via Model Context Protocol
- 📦 **Skill Registry** - Publish, version, and manage AI skills
- 🧠 **Vector Search** - PostgreSQL pgvector for semantic similarity
- ⚡ **RESTful API** - Jakarta REST with Quarkus
- 🔐 **API Key Auth** - Secure endpoints with X-API-Key

## 🏗️ Architecture

```
┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Client    │────▶│  SkillResource  │────▶│  SkillService   │
└─────────────┘     └────────┬────────┘     └────────┬────────┘
                             │                       │
                             ├───────────────────────┼──────────┐
                             ▼                       ▼          ▼
                  ┌─────────────┐            ┌────────────┐ ┌──────────┐
                  │SkillRepository│          │EmbeddingService│SearchService│
                  └──────┬──────┘            └────────────┘ └──────────┘
                         │
                         ▼
                  ┌─────────────┐
                  │  PostgreSQL │ (vector + tsvector)
                  └─────────────┘
```

## 🚀 Quick Start

### Requirements

- Java 21+
- Maven 3.9+
- Docker Desktop
- PostgreSQL 15+ with pgvector

### Setup

```bash
# Start infrastructure
docker compose up -d db

# Run server
export AUTH_API_KEY=dev-api-key
mvn quarkus:dev
```

Health check: `http://localhost:8080/q/health`

### Usage

```bash
# List skills
curl "http://localhost:8080/api/v1/skills"

# Search skills
curl "http://localhost:8080/api/v1/skills/search?query=log%20analysis&limit=10"

# Publish skill (requires API key)
curl -X POST "http://localhost:8080/api/v1/skills/publish" \
  -H "X-API-Key: dev-api-key" \
  -H "Content-Type: application/json" \
  -d '{"name":"skill-analytics","version":"1.0.0","description":"Analytics helper","category":"data","tags":["analytics"]}'
```

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [Project Overview & PDR](./docs/project-overview-pdr.md) | Requirements & specifications |
| [System Architecture](./docs/system-architecture.md) | Technical architecture |
| [Code Standards](./docs/code-standards.md) | Engineering guidelines |
| [Codebase Summary](./docs/codebase-summary.md) | Module overview |
| [Project Roadmap](./docs/project-roadmap.md) | Development phases |

## 🛠️ Development

See [Project Roadmap](./docs/project-roadmap.md) for detailed timeline.

## 📄 License

MIT
