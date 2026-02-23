# MCP Server Deployment & Access Research

## 1. Transport Mechanisms

### STDIO (Standard Input/Output)
- **Simplest & most universal** - client spawns server as child process
- Communication via STDIN/STDOUT streams
- **Best for:** local processes, CLI integrations, development
- No network overhead, zero configuration
- Works across platform boundaries (Windows, Linux, macOS)

### Streamable HTTP
- Single HTTP endpoint supporting POST (client→server) & GET (optional SSE for streaming)
- Server-Sent Events (SSE) enables multi-message responses
- **Best for:** remote servers, scalability, cloud deployments
- Supports standard HTTP auth: bearer tokens, API keys, custom headers
- URL pattern: `https://example.com/mcp`

### WebSocket Transport (Emerging)
- Not yet standardized in core spec, proposed SEP-1288
- Persistent full-duplex bidirectional communication
- Low latency, session persistence across network interruptions
- Single connection management vs multiple HTTP requests
- **Future candidate** for stateful, real-time scenarios

---

## 2. Discovery Mechanisms

### Dynamic Tool Discovery
- **Zero manual setup** - omit server config details from client
- Client calls `tools/list` endpoint at runtime
- Server advertises available capabilities dynamically
- **Benefits:** future-proof (servers evolve without client updates), runtime adaptability

### Configuration-Based Discovery
- Client-side mcpServers object in settings file
- Top-level mcp object for global settings
- Example: VS Code MCP configuration, Claude Desktop config
- Explicit server registration with connection parameters

### Hub Server Pattern
- Central registry managing multiple MCP servers
- Config-based server initialization
- Health monitoring & automatic reconnection
- Example: mcp-hub project, Copilot Studio agent connectors

---

## 3. Configuration & Connection Management

### Client-Server Model
- One MCP **host** (e.g., Claude Desktop) connects to multiple MCP **servers**
- Each server gets dedicated **MCP client** with persistent connection
- JSON-RPC 2.0 protocol over bidirectional transport
- Stateful session protocol focused on context exchange

### Connection Setup Flow
1. Client discovers server (config or dynamic)
2. Establishes transport connection (STDIO/HTTP/WebSocket)
3. Sends initialization request
4. Server responds with capabilities
5. Client calls tools/resources/prompts as needed

### Multi-Turn Session Management
- Conversation context persists across multiple requests
- **Critical for scalability:** if requests scatter across multiple servers, context is lost
- Requires session affinity or distributed state (Redis)

---

## 4. Security Framework

### OAuth 2.1 Standard (March 2025)
- **Mandatory framework** adopted by MCP spec
- All clients required to use **PKCE (Proof Key for Code Exchange)**
- PKCE prevents token interception & code injection attacks
- Eliminates vulnerabilities in naive OAuth implementations

### API Key Management
- Store keys in platform-native secrets managers or environment variables
- **Never hardcode** credentials
- Server reads at runtime during initialization
- Recommended: environment-based for local/dev, vault for production

### Transport Security
- **HTTPS mandatory** (OAuth requirement)
- Test over HTTPS during dev for production parity
- Recommended: mTLS (mutual TLS) or DPoP (Demonstration of Proof-of-Possession)
- Both agent & server prove identity cryptographically

### Authentication Patterns
- HTTP header: `Authorization: Bearer <token>` or `Authorization: Bearer <api-key>`
- Server validates credential before processing tool calls
- **Phantom token pattern:** API gateway converts opaque token → JWT
- MCP server validates JWT, checks audience claim

### Enterprise Approaches
- External OAuth provider (offload complexity)
- MCP server acts as resource server only
- Separate authorization server handles login/consent/token issuance

---

## 5. Scalability for Multi-User

### Transport Choice Matters
- **STDIO:** single-process only, not scalable
- **Streamable HTTP:** horizontal scaling candidate
- Load balancer can distribute across multiple server instances

### Session Affinity Requirement
- User conversation = persistent context across multiple requests
- **Must route all requests from same user to same server instance**
- Strategies: IP hash, cookie-based sticky sessions, explicit session ID
- Breaking affinity = lost context = broken user experience

### Distributed State (Redis Pattern)
- Multiple server instances share state via Redis cache
- Enables stateless server design
- Session data synced across all nodes
- Allows session migration if server fails

### Horizontal Scaling Architecture
```
Load Balancer
    ├─→ MCP Server Instance 1 ──┐
    ├─→ MCP Server Instance 2 ──┼─→ Redis (shared state)
    └─→ MCP Server Instance N ──┘
```

### Auto-Scaling
- Adjust resource allocation based on traffic demand
- Crucial for unpredictable load spikes
- Kubernetes recommended for cloud deployments
- Health checks + container orchestration

### Gateway Pattern (2025+ Best Practice)
- **MCP Gateway** acts like API Gateway for AI tools
- Centralized tool access pipeline
- Single point for: auth, rate limiting, logging, monitoring
- More scalable & secure than direct server access
- Decouples auth from individual MCP servers

---

## 6. Production Deployment Patterns

### Layered Architecture
```
Client Layer (Claude Desktop, Custom AI Clients)
    ↓
API Gateway Layer (Load Balancer, Auth, Rate Limiting)
    ↓
MCP Server Pool (Health Checks, Auto-Scaling)
    ↓
Backend Integration (DB, APIs, File Systems, Message Queues)
```

### Key Considerations
- **No built-in** load balancing or HA without tooling
- Unlike REST APIs, MCP maintains persistent connections
- Multi-turn conversations last minutes, not milliseconds
- Load balancing strategy crucial for production

### Containerization
- Package MCP servers as Docker containers
- Define resource limits & requests (CPU, memory)
- Health checks for liveness/readiness probes
- Managed in Kubernetes or similar orchestration

### Recent Innovations (November 2025 Spec)
- **Tasks Primitive:** async long-running operations
- Server creates task → returns handle → publishes progress → delivers results
- Beyond simple request-response model

---

## Summary Table

| Aspect | STDIO | Streamable HTTP | WebSocket |
|--------|-------|-----------------|-----------|
| **Setup** | Simple | Moderate | Complex |
| **Scalability** | None | Excellent | Good |
| **Deployment** | Local | Remote/Cloud | Remote |
| **Session State** | Process-bound | Distributed cache | Persistent |
| **Auth** | Environment | OAuth 2.1, API keys | OAuth 2.1, tokens |
| **Latency** | Lowest | Medium | Low |

---

## Unresolved Questions

1. WebSocket standardization timeline - when will SEP-1288 be merged?
2. Performance benchmarks: Streamable HTTP vs WebSocket at scale
3. Best practices for session affinity with geographically distributed load balancers
4. Redis vs other distributed state solutions for MCP
5. Task primitive usage patterns in production (November 2025 feature)
