# Documentation Hub - AI Skill Gateway

Start here for current project documentation. This hub lists only files that exist in `docs/`.

## Core Documents

1. [Project Overview PDR](./project-overview-pdr.md) - requirements, scope, acceptance criteria, risks.
2. [System Architecture](./system-architecture.md) - runtime architecture, service layers, provider strategy.
3. [Architecture Notes](./architecture.md) - concise architecture and data-flow summary.
4. [Code Standards](./code-standards.md) - engineering, security, testing, docs rules.
5. [Codebase Summary](./codebase-summary.md) - module map and current implementation summary.
6. [Client and Agent Integration Guide](./client-agent-integration-guide.md) - REST API usage for external clients and AI agents.

## Generated Visuals

- [Claude Kit Map](./claude-kit-map.html) - local HTML map of skills, agents, commands, and relationships.

## Operational Notes

- Run Java tests with `mvn test`.
- Run local web console with `npm install --prefix web-ui` then `npm run dev --prefix web-ui`.
- Default web console URL is `http://localhost:5173`; it calls Docker backend `http://localhost:18080` or Maven backend `http://localhost:8080`.
- Validate docs with `node .claude/scripts/validate-docs.cjs docs/`.
- Keep provider examples secret-free; use environment variables for API keys.
- The web console keeps protected endpoint API key in React memory only; it does not use `localStorage` or `sessionStorage`.
- Keep generated dependency/build outputs untracked: `target/`, `node_modules/`, `npm-package/dist/`, and `web-ui/dist/`.

## Current AI Integration

- Default embedding provider: `ollama`.
- Optional provider: `openai-compatible` for `/v1/embeddings` APIs.
- Status endpoint: `GET /api/v1/embedding/status`.
- Quarkus health endpoint: `GET /q/health`.
