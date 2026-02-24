# Documentation Hub - AI Skill Gateway

**Quick Links to Start:**

1. **[project-overview-pdr.md](./project-overview-pdr.md)** - Project requirements and API reference ⭐ START HERE
2. **[system-architecture.md](./system-architecture.md)** - System architecture and data flows
3. **[code-standards.md](./code-standards.md)** - Code standards and engineering guidelines
4. **[codebase-summary.md](./codebase-summary.md)** - Codebase structure summary

---

## What's Included

### Project Documentation
- **project-overview-pdr.md** - Product Development Requirements, API reference, MCP tools
- **system-architecture.md** - Layered architecture, data flows, deployment
- **code-standards.md** - Java/Quarkus coding standards, API design, testing
- **codebase-summary.md** - Module structure and key files
- **project-roadmap.md** - Development phases and milestones

---

## Get Started in 5 Minutes

### Step 1: Understand the Project (2 min)
Read `project-overview-pdr.md` for functional requirements and API endpoints

### Step 2: Review Architecture (2 min)
Read `system-architecture.md` for system design and data flows

### Step 3: Check Code Standards (1 min)
Skim `code-standards.md` for development guidelines

---

## File Guide

| File | Purpose | When to Use |
|------|---------|-------------|
| **project-overview-pdr.md** | Requirements & API reference | Understanding features |
| **system-architecture.md** | Architecture & data flows | System design context |
| **code-standards.md** | Coding guidelines | Development |
| **codebase-summary.md** | Code structure | Navigating codebase |
| **project-roadmap.md** | Development timeline | Planning |

---

## For Different Roles

### Project Managers
→ Read: `project-overview-pdr.md`, `project-roadmap.md`

### Backend Developers
→ Read: `system-architecture.md`, `code-standards.md`
→ Reference: `codebase-summary.md`

### DevOps/Operations
→ Read: `system-architecture.md` (Deployment section)

### QA Engineers
→ Read: `project-overview-pdr.md` (API Reference)

---

## Technology Stack

- **Runtime:** Java 21, Quarkus 3.20.2
- **Database:** PostgreSQL with pgvector
- **AI Integration:** Ollama embedding service
- **Build:** Maven

---

## API Overview

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/skills/publish` | API Key | Publish skill |
| GET | `/api/v1/skills/{name}` | - | Get skill detail |
| GET | `/api/v1/skills` | - | List skills |
| POST | `/api/v1/skills/{name}/versions/{version}/yank` | API Key | Yank version |
| GET | `/api/v1/skills/search` | - | Hybrid search |
| GET | `/api/v1/skills/{name}/versions` | - | List versions |
| GET | `/api/v1/skills/{name}/resolve` | - | Resolve version |
| GET | `/api/v1/skills/{name}/dependencies/{version}` | - | Dependency tree |

---

## Key Features

- Skill publishing with manifest validation
- Hybrid search (keyword + semantic + popularity)
- Semantic version resolution
- Dependency tree analysis
- API key authentication
- PostgreSQL vector search

---

**Created:** 2026-02-21 | **Status:** Active Development
