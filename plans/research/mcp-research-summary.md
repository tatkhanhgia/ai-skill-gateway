# MCP Research Summary - Quick Reference

## What is MCP?
Open protocol for LLM applications to integrate external tools, data, and services. Uses JSON-RPC 2.0 over STDIO or Streamable HTTP transports.

## Architecture (3 Key Participants)
- **Host**: AI application (Claude Code, desktop apps)
- **Client**: Connection manager within host
- **Server**: Capability provider (local or remote)

## Core Concepts
| Concept | Purpose | Pattern |
|---------|---------|---------|
| **Tools** | Executable functions for AI | List → Discover → Call → Execute |
| **Resources** | Context data (files, DB, APIs) | List with URIs → Read → Stream |
| **Prompts** | Message templates for workflows | List → Get with args → Use |
| **Sampling** | Server requests LLM from client | Request → Approval → Complete |

## Request-Response Model
```
Client → JSON-RPC request (tools/call, resources/read, etc.)
Server → Response with content objects (text, images, binary)
```

## Java SDK (v0.12.1)
**Org:** `io.modelcontextprotocol.sdk`

### Included (No External Deps)
- STDIO transport (local)
- Streamable HTTP + SSE (remote)
- Servlet transport
- JSON binding (Jackson)

### Optional
- Spring WebFlux (reactive, high-throughput)
- Spring WebMVC (servlet-based)

### Architecture
```
McpClient/McpServer
    ↓
McpSession (DefaultMcpSession)
    ↓
Transport (STDIO, HTTP, Servlet)
    ↓
JSON-RPC Serialization
```

## Design Patterns (Java)
1. **Transport-Agnostic APIs**: Core logic independent of delivery
2. **Reactive + Blocking**: Project Reactor internally; Reactive Streams facade
3. **Pluggable Auth**: No built-in security; hooks for Spring Security, JWT, custom
4. **Pluggable JSON**: Jackson default; swap alternatives

## Security (Streamable HTTP)
- **OAuth 2.1 + PKCE** for remote servers
- **Bearer tokens** in Authorization header
- **Short-lived access tokens** (minutes-hours)
- **Refresh tokens** stored securely (OS keychain)
- **Per-request auth** via standard headers
- **User consent required** before tool execution
- **Token never in URL** (appears in logs)

## Transport Choice
| Transport | Best For | Auth | Connections |
|-----------|----------|------|-------------|
| STDIO | Local, dev, testing | Process-level | Single client |
| Streamable HTTP | Remote, production, cloud | OAuth 2.1 Bearer | Multiple clients |

## New in 2025-11-25
- Async Tasks (call-now, fetch-later with status tracking)
- Stateless mode (subset of protocol)
- Server-side agent loops
- Parallel tool execution

## Implementation Checklist
- [ ] Choose transport (STDIO or Streamable HTTP)
- [ ] Add Maven dependency: `io.modelcontextprotocol.sdk:mcp`
- [ ] Add BOM for managed versions: `mcp-bom:0.12.1`
- [ ] Implement McpServer with tool/resource handlers
- [ ] Plug in authorization hooks (Spring Security/custom)
- [ ] Handle initialization + capability negotiation
- [ ] Implement tool/resource discovery lists
- [ ] Support progress tracking for long operations
- [ ] Test with MCP Inspector tool
- [ ] Deploy with OAuth token validation (if remote)

## Unresolved Questions
- Specific Spring AI integration patterns (vs plain SDK)
- Caching strategies for large resource lists
- Rate limiting + quota enforcement patterns
- Multi-tenancy isolation strategies

---
**Sources:**
- [MCP Specification](https://modelcontextprotocol.io/specification/2025-11-25)
- [Java SDK Docs](https://modelcontextprotocol.io/sdk/java/mcp-overview)
- [GitHub: modelcontextprotocol/java-sdk](https://github.com/modelcontextprotocol/java-sdk)
