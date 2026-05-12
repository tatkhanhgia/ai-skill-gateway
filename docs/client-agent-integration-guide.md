# Client and Agent Integration Guide

This document is the single integration reference for external clients and AI agents calling AI Skill Gateway.

## Base URL

Default Docker URL:

```text
http://localhost:18080
```

Dev mode URL:

```text
http://localhost:8080
```

Use the Docker URL unless you are running `mvn quarkus:dev`.

## Authentication

Read-only endpoints do not require authentication.

Write/control endpoints require:

```http
X-API-Key: dev-api-key
```

Protected endpoints:

- `POST /api/v1/skills/publish`
- `POST /api/v1/skills/publish-bundle`
- `POST /api/v1/skills/{name}/versions/{version}/yank`

## Local Web Console

The standalone browser console lives in `web-ui/`.

```powershell
npm install --prefix web-ui
npm run dev --prefix web-ui
```

Open:

```text
http://localhost:5173
```

The console uses strict Vite port `5173` because the backend CORS allowlist targets that origin. Stop the process using `5173` before starting the console if Vite reports a port conflict.

The console defaults to Docker backend `http://localhost:18080` and can switch to Maven dev backend `http://localhost:8080`. API key value is stored in React memory only and is sent only as `X-API-Key` for protected publish/yank calls.

## Health and Configuration

Check server health:

```http
GET /q/health
```

Check active embedding provider:

```http
GET /api/v1/embedding/status
```

Expected healthy embedding status:

```json
{
  "provider": "openai-compatible",
  "url": "http://host.docker.internal:1234/v1/embeddings",
  "model": "text-embedding-nomic-embed-text-v1.5",
  "dimension": 768,
  "timeoutSeconds": 30,
  "configured": true,
  "message": "Embedding provider configuration is valid"
}
```

## Main Workflows

### 1. List Skills

```http
GET /api/v1/skills?page=0&size=20
```

Optional query params:

- `category`: filter by category
- `tags`: comma-separated tags
- `page`: zero-based page number
- `size`: page size

Example:

```powershell
Invoke-RestMethod "http://localhost:18080/api/v1/skills?page=0&size=20"
```

Response:

```json
[
  {
    "id": 1,
    "name": "skill-analytics",
    "category": "data",
    "description": "Analytics helper",
    "downloadCount": 0,
    "tags": ["analytics", "reporting"]
  }
]
```

### 2. Search Skills

```http
GET /api/v1/skills/search?query=log%20analysis&limit=10
```

Optional query params:

- `query`: search text
- `category`: filter by category
- `tags`: comma-separated tags
- `limit`: max results

Example:

```powershell
Invoke-RestMethod "http://localhost:18080/api/v1/skills/search?query=log%20analysis&limit=10"
```

Response:

```json
[
  {
    "id": 1,
    "name": "skill-analytics",
    "category": "data",
    "description": "Analytics helper",
    "downloadCount": 0,
    "score": 0.76,
    "tags": ["analytics", "reporting"]
  }
]
```

Search combines keyword, semantic embedding, and popularity scoring. If embedding is unavailable, search degrades to non-semantic results.

### 3. Get Skill Detail

```http
GET /api/v1/skills/{name}
```

Example:

```powershell
Invoke-RestMethod "http://localhost:18080/api/v1/skills/skill-analytics"
```

Response:

```json
{
  "id": 1,
  "name": "skill-analytics",
  "category": "data",
  "description": "Analytics helper",
  "author": "Team",
  "repositoryUrl": "https://example.com/repo",
  "downloadCount": 0,
  "latestVersion": {
    "version": "1.0.0"
  },
  "tags": ["analytics", "reporting"]
}
```

### 4. Publish Skill

```http
POST /api/v1/skills/publish
X-API-Key: dev-api-key
Content-Type: application/json
```

Request body:

```json
{
  "name": "skill-analytics",
  "version": "1.0.0",
  "description": "Analytics helper",
  "category": "data",
  "tags": ["analytics", "reporting"],
  "author": "Team",
  "repositoryUrl": "https://example.com/repo",
  "requires": [],
  "releaseNotes": "Initial release"
}
```

Required fields:

- `name`
- `version`
- `description`
- `category`

Optional fields:

- `tags`
- `author`
- `repositoryUrl`
- `requires`
- `releaseNotes`

PowerShell example:

```powershell
$body = @{
  name = "skill-analytics"
  version = "1.0.0"
  description = "Analytics helper"
  category = "data"
  tags = @("analytics", "reporting")
  author = "Team"
  repositoryUrl = "https://example.com/repo"
  requires = @()
  releaseNotes = "Initial release"
} | ConvertTo-Json -Depth 6

Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:18080/api/v1/skills/publish" `
  -Headers @{ "X-API-Key" = "dev-api-key" } `
  -ContentType "application/json" `
  -Body $body
```

Response:

```json
{
  "skillId": 1,
  "versionId": 1,
  "name": "skill-analytics",
  "version": "1.0.0"
}
```

### 5. Publish Skill Bundle

Skill bundles are zip archives with `SKILL.md` at the archive root. The gateway validates paths, forbidden files, size limits, frontmatter metadata, and file checksums before storing the artifact.

Required root layout:

```text
SKILL.md
references/...
scripts/...
assets/...
```

Rejected content includes absolute paths, `..` traversal, duplicate normalized paths, `.env` files, `.git`, `node_modules`, `.venv`, log files, and temp files. Bundled scripts are stored as data only; the gateway never executes them.

```http
POST /api/v1/skills/publish-bundle
X-API-Key: dev-api-key
Content-Type: multipart/form-data
```

PowerShell example:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:18080/api/v1/skills/publish-bundle" `
  -Headers @{ "X-API-Key" = "dev-api-key" } `
  -Form @{ bundle = Get-Item ".\skill-analytics.zip" }
```

List bundle files:

```http
GET /api/v1/skills/{name}/versions/{version}/files
```

Download bundle artifact:

```http
GET /api/v1/skills/{name}/versions/{version}/bundle
```

The download response is `application/zip` and includes `X-Bundle-Sha256` and `X-Bundle-Size` headers.

### 6. List Versions

```http
GET /api/v1/skills/{name}/versions
```

Example:

```powershell
Invoke-RestMethod "http://localhost:18080/api/v1/skills/skill-analytics/versions"
```

Response:

```json
[
  {
    "id": 1,
    "version": "1.0.0",
    "prerelease": false,
    "latest": true,
    "yanked": false,
    "publishedAt": "2026-05-11T02:00:00"
  }
]
```

### 7. Resolve Version

```http
GET /api/v1/skills/{name}/resolve?constraint=latest
```

Examples:

```powershell
Invoke-RestMethod "http://localhost:18080/api/v1/skills/skill-analytics/resolve?constraint=latest"
Invoke-RestMethod "http://localhost:18080/api/v1/skills/skill-analytics/resolve?constraint=>=1.0.0"
```

Response:

```json
{
  "skillName": "skill-analytics",
  "constraint": "latest",
  "selectedVersion": "1.0.0",
  "candidates": ["1.0.0"]
}
```

### 8. Resolve Dependencies

```http
GET /api/v1/skills/{name}/dependencies/{version}
```

Example:

```powershell
Invoke-RestMethod "http://localhost:18080/api/v1/skills/skill-analytics/dependencies/1.0.0"
```

Response:

```json
{
  "name": "skill-analytics",
  "version": "1.0.0",
  "dependencies": []
}
```

### 9. Yank Version

```http
POST /api/v1/skills/{name}/versions/{version}/yank?reason=deprecated
X-API-Key: dev-api-key
```

Example:

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:18080/api/v1/skills/skill-analytics/versions/1.0.0/yank?reason=deprecated" `
  -Headers @{ "X-API-Key" = "dev-api-key" }
```

Success response is HTTP 200 with an empty body.

## Error Shape

Errors return JSON:

```json
{
  "error": "validation_error",
  "details": ["name is required"]
}
```

Common statuses:

- `400`: invalid request or validation error
- `401`: missing or invalid `X-API-Key`
- `404`: skill/version not found
- `409`: duplicate skill version or conflict
- `500`: server/configuration error

## Agent Usage Rules

For AI agents integrating with this gateway:

1. Call `GET /q/health` before write workflows.
2. Call `GET /api/v1/embedding/status` before semantic-heavy search or bulk publish.
3. Prefer `GET /api/v1/skills/search` to discover relevant skills.
4. Use `GET /api/v1/skills/{name}` after search to fetch full detail.
5. Use `GET /api/v1/skills/{name}/resolve?constraint=latest` before installation or dependency resolution.
6. Use bundle file manifests and `X-Bundle-Sha256` to verify downloaded artifacts.
7. Never call protected endpoints without `X-API-Key`.
8. Do not log real API keys.
9. Treat empty search results as valid; retry only after changing query, category, tags, or limit.

## Current Local LM Studio Configuration

The current local tested configuration is:

```powershell
$env:AI_EMBEDDING_PROVIDER="openai-compatible"
$env:AI_EMBEDDING_URL="http://host.docker.internal:1234/v1/embeddings"
$env:AI_EMBEDDING_MODEL="text-embedding-nomic-embed-text-v1.5"
$env:AI_EMBEDDING_API_KEY="lm-studio"
$env:AI_EMBEDDING_DIMENSION="768"
$env:AI_EMBEDDING_TIMEOUT_SECONDS="30"
docker compose up -d --build --force-recreate server
```

`host.docker.internal` is required when the gateway runs in Docker and LM Studio runs on the Windows host.
