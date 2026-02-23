# MCP Specification & Java Implementation Research Report

**Date:** 2026-02-21 | **Focus:** Protocol architecture, core concepts, Java SDK, security patterns

---

## 1. MCP Architecture Overview

### Client-Server Model
- **JSON-RPC 2.0** message format (stateful connections)
- **Host** (AI app) → **Client** (connector) → **Server** (capability provider)
- Single dedicated connection per server; remote servers serve multiple clients
- **Two transport mechanisms:**
  - **STDIO**: Local process communication (optimal for single-client)
  - **Streamable HTTP + Server-Sent Events**: Remote communication with standard auth

### Two-Layer Design
1. **Data Layer**: JSON-RPC protocol, lifecycle management, primitives
2. **Transport Layer**: Connection management, message framing, authentication

### Initialization Handshake
```json
Client → Initialize request with capabilities
Server → Response with protocol version, supported features, identity
Client → notifications/initialized notification
```
Protocol version negotiation prevents incompatible interactions; both sides declare supported features (tools, resources, prompts).

---

## 2. Core Concepts (Server-Side Primitives)

### Tools
- **Executable functions** for AI to invoke (file ops, API calls, database queries)
- Discovery via `tools/list` → execution via `tools/call`
- Each tool includes: `name`, `title`, `description`, `inputSchema` (JSON Schema)
- Responses: array of content objects (text, images, resources)
- Real-time updates: servers emit `tools/list_changed` notifications

### Resources
- **Context data sources** for AI (file contents, DB records, API responses)
- URI-template based access for flexible addressing
- Retrieved via `resources/list` → `resources/read`
- Supports subscription system for content updates

### Prompts
- **Reusable message templates** for structuring LLM interactions
- System prompts, few-shot examples, interaction workflows
- Discovery/retrieval via `prompts/list` → `prompts/get`
- Can include dynamic argument completion

### Sampling (Client-Side)
- Allows servers to request LLM completions via client
- Method: `sampling/complete` (model-agnostic, avoids embedding LLM SDK)
- Requires explicit user approval; server can't see actual prompts

---

## 3. Java MCP SDK

### Official SDK (`io.modelcontextprotocol.sdk:mcp`)
**Latest:** v0.12.1 | **Maintained by:** Anthropic + Spring AI collaboration

**Core Features:**
- Tool/resource/prompt discovery & execution
- Progress tracking, ping/keepalive, logging
- Roots management (filesystem boundaries)
- Completion autocompletion suggestions

### Transport Implementations (No External Frameworks Required)
- Default STDIO transport (Java process streams)
- Java HttpClient-based SSE & Streamable-HTTP (client)
- Servlet-based SSE & Streamable-HTTP (server)
- Optional: Spring WebFlux (reactive), Spring WebMVC (servlet)

### SDK Architecture (Layered Design)

```
┌─ McpClient/McpServer (sync/async)
├─ McpSession (DefaultMcpSession)
├─ Transport Layer (JSON-RPC serialization)
│  ├─ StdioTransport
│  ├─ StreamableHttpTransport
│  └─ ServerSentEventsTransport
└─ io.modelcontextprotocol.json (Jackson abstraction)
```

### Key Design Patterns
1. **Transport-Agnostic Server APIs**: Core logic independent of delivery mechanism (Servlet default)
2. **Reactive Streams + Blocking Facade**: Project Reactor internally; exposes Reactive Streams publicly
3. **Pluggable Authorization Hooks**: No built-in auth; integrates with Spring Security, MicroProfile JWT, custom
4. **Abstraction-Based JSON Binding**: Jackson default; pluggable alternatives

### Dependencies
```xml
<!-- Core (includes STDIO, SSE, Streamable-HTTP) -->
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp</artifactId>
</dependency>

<!-- Optional: Spring WebFlux (reactive, scalable) -->
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp-spring-webflux</artifactId>
</dependency>

<!-- Optional: Spring WebMVC (servlet-based streaming) -->
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp-spring-webmvc</artifactId>
</dependency>
```

Use BOM for managed versions: `io.modelcontextprotocol.sdk:mcp-bom:0.12.1`

---

## 4. MCP Server Design Patterns (Practical)

### Initialization Pattern
1. Receive transport connection
2. Negotiate protocol version + declare capabilities
3. Return server identity & supported features
4. Wait for client requests

### Tool Implementation Pattern
```
1. Implement tool method (accept typed args, return content objects)
2. Register tool via tools/list callback
3. Handle tools/call requests → validate inputSchema → execute → return results
4. Emit tools/list_changed when tools dynamically change
```

### Resource Pattern
```
1. Define URI templates (e.g., "file://{path}")
2. Implement resources/list (advertise available templates)
3. Handle resources/read requests (parse URI, validate, return content)
4. Support subscriptions for cache invalidation
```

### Progress Tracking Pattern
- Emit progress notifications for long-running operations
- Format: `{ progressToken, progress, total }`
- Allows clients to show progress bars or cancel operations

### Long-Running Operations (Experimental Tasks)
- Return task handle instead of immediate result
- Clients poll status via `tasks/status` + fetch results with `tasks/result`
- Enables deferred execution, batch processing, multi-step workflows

---

## 5. Authentication & Security

### Transport Security (Streamable HTTP)

**OAuth 2.1 with PKCE** (recommended for remote servers):
- Authorization Code grant with Proof Key for Code Exchange
- **Token Strategy:**
  - Short-lived access tokens (minutes-hours) → fast expiration limits breach impact
  - Refresh tokens for session maintenance (store securely in OS keychain, NOT plaintext)
  - Never pass tokens in URL query strings (appear in logs)
  - Use Authorization: Bearer header exclusively

**Request Pattern:**
```
POST /mcp HTTP/1.1
Authorization: Bearer <access_token>
Content-Type: application/json

{ "jsonrpc": "2.0", "method": "tools/list", ... }
```

### Server-Side Security

1. **Authorization Hooks** (SDK provides pluggable pattern):
   - Intercept requests before tool execution
   - Validate bearer token against token provider
   - Check user permissions/scopes
   - Integrate with Spring Security, MicroProfile JWT, or custom solutions

2. **User Consent Model:**
   - Explicit approval required for sensitive operations
   - UIs must clearly show what tools do before invocation
   - Tool descriptions treated as untrusted (from server)
   - Sampling requests require user approval; limit server prompt visibility

3. **Data Protection:**
   - Get explicit consent before exposing user data to servers
   - Don't transmit resources elsewhere without user consent
   - Protect resources with appropriate access controls

4. **Tool Safety:**
   - Tools = arbitrary code execution; treat with caution
   - Hosts must obtain user consent before any tool invocation
   - Limit available tools based on user permissions

### Infrastructure Advantages (Streamable HTTP)
- Standard Authorization headers inspectable by WAFs, load balancers, auth providers
- Per-request authentication (not connection-level)
- Compatible with standard OAuth providers (Auth0, Okta, etc.)

---

## 6. 2025-11-25 Specification Updates

### Async Tasks (Experimental)
- Any request can return task handle instead of immediate result
- Enables call-now, fetch-later patterns
- Useful for: expensive computations, workflow automation, batch processing

### Stateless Mode
- Subset of MCP can run stateless via Streamable HTTP
- Complements traditional stateful STDIO connections

### Agent Capabilities
- Server-side agent loops (multi-step reasoning)
- Parallel tool execution
- Enhanced agentic workflows

### HTTP Transport Preference
- Streaming via Server-Sent Events + standard HTTP
- Simpler infrastructure (no long-lived connections)
- Better for cloud deployment scenarios

---

## 7. Implementation Considerations

### Choosing Transport
- **STDIO**: Local, single-client, development, testing
- **Streamable HTTP + SSE**: Remote, multi-client, production, cloud-native

### SDK Choice (Java)
- **Reactive (WebFlux)**: High-throughput, complex data pipelines
- **Servlet/WebMVC**: Traditional sync/blocking, simpler code
- **Core Transport**: Lightweight, no framework dependency

### Error Handling
- Return JSON-RPC errors with appropriate codes
- Propagate error context to client for logging/UI
- Use progress notifications for cancellation support

### Performance Patterns
- Cache tool/resource listings if static
- Lazy-load resources (don't pre-fetch all data)
- Stream large responses
- Use progress tracking for user feedback

---

## References

1. [MCP Specification 2025-11-25](https://modelcontextprotocol.io/specification/2025-11-25)
2. [MCP Architecture Overview](https://modelcontextprotocol.io/docs/learn/architecture)
3. [MCP Java SDK Overview](https://modelcontextprotocol.io/sdk/java/mcp-overview)
4. [Official Java SDK GitHub](https://github.com/modelcontextprotocol/java-sdk)
5. [Authorization in MCP](https://modelcontextprotocol.io/docs/tutorials/security/authorization)
6. [Streamable HTTP Security](https://auth0.com/blog/mcp-streamable-http/)
7. [OAuth 2.1 + Streamable HTTP](https://news.lavx.hu/article/authentication-and-authorization-in-model-context-protocol-oauth-2-1-and-the-streamable-http-transport)

---

**Summary:** MCP provides standardized JSON-RPC protocol for AI-context integration. Java SDK offers transport-agnostic server APIs with pluggable auth. Key strengths: Reactive design, OAuth 2.1 support, streaming HTTP for remote deployment. Production implementations should prioritize short-lived tokens, explicit user consent, and server-side authorization hooks.
