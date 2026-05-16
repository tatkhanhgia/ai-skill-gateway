# 🚀 AI Skill Gateway

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Quarkus](https://img.shields.io/badge/Quarkus-4695EB?style=for-the-badge&logo=quarkus&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Ollama](https://img.shields.io/badge/Ollama-000000?style=for-the-badge&logo=ollama&logoColor=white)

> **Gateway thông minh chạy local trên Windows - Tìm kiếm và đề xuất skill dựa trên AI**

## ✨ Tính năng Nổi Bật (Key Features)

- 🔍 **Tìm kiếm thông minh:** Kết hợp keyword search và semantic search (tìm kiếm theo ngữ nghĩa).
- 🤖 **AI Local Privacy-First:** Chạy hoàn toàn local với mạng nơ-ron cục bộ thông qua [Ollama](https://ollama.ai/), đảm bảo tuyệt đối an toàn dữ liệu.
- 📦 **Đa nguồn lưu trữ (Multi-Storage):** Hỗ trợ linh hoạt Local Directory, PostgreSQL và Google Drive.
- 🧠 **Memory lâu dài:** Tích hợp Vector Database để lưu trữ ngữ cảnh và lịch sử truy vấn.
- ⚡ **API RESTful:** Thiết kế chuẩn mực, dễ dàng tích hợp với mọi loại client.

## 🏗️ Kiến trúc Hệ Thống (Architecture)

Mô hình xử lý luồng dữ liệu của hệ thống:

```
[ Client / App ] 
       │
       ▼ (REST API)
[ 🧠 AI Skill Gateway (Quarkus) ]
       │
       ├─► 🤖 Local AI Engine (Ollama) -> Semantic Processing
       ├─► 📊 Vector DB -> Context & Memory Storage
       └─► 💾 Multi-Storage (Postgres / Drive / Local) -> Data Persistence
```

## 🚀 Hướng Dẫn Khởi Chạy (Quick Start)

### Yêu cầu hệ thống (Prerequisites)

* Windows 10/11
* Java 17+
* Maven 3.9+
* Docker Desktop
* [Ollama](https://ollama.ai/) đã cài đặt và cấu hình sẵn các model cần thiết.

### Cài đặt (Installation)

**1. Clone repository:**

```powershell
git clone https://github.com/your-username/ai-skill-gateway.git
cd ai-skill-gateway
```

**2. Khởi động Infrastructure (Database & AI Engine):**

```powershell
docker compose up -d db ollama
```

> **Ports mặc định qua Docker Compose:** Server `18080`, PostgreSQL `15432`, Ollama `11435`.
> Có thể override bằng biến môi trường `SERVER_PORT`, `DB_PORT`, `OLLAMA_PORT`.

**3. Chạy Server (JVM mode):**

```powershell
set AUTH_API_KEY=dev-api-key
mvn quarkus:dev
```

> **Health check:** Khi chạy bằng Maven: `http://localhost:8080/q/health`.
> Khi chạy bằng Docker Compose: `http://localhost:18080/q/health`.
> **Embedding status:** `http://localhost:8080/api/v1/embedding/status` trả về cấu hình provider đã lọc secret.

### Embedding Provider Configuration

Ollama remains the default local-first provider:

```powershell
set AI_EMBEDDING_PROVIDER=ollama
set AI_EMBEDDING_URL=http://localhost:11434/api/embeddings
set AI_EMBEDDING_MODEL=nomic-embed-text
set AI_EMBEDDING_DIMENSION=768
```

OpenAI-compatible `/v1/embeddings` providers are also supported:

```powershell
set AI_EMBEDDING_PROVIDER=openai-compatible
set AI_EMBEDDING_URL=http://localhost:11435/v1/embeddings
set AI_EMBEDDING_MODEL=text-embedding-model
set AI_EMBEDDING_API_KEY=
set AI_EMBEDDING_DIMENSION=768
```

For remote providers, pass the API key through `AI_EMBEDDING_API_KEY`; do not commit it to config files. The returned embedding dimension must match `AI_EMBEDDING_DIMENSION` and the PostgreSQL vector schema.

Check active embedding configuration:

```powershell
curl "http://localhost:8080/api/v1/embedding/status"
```

### Bundled Skill Catalog Seed

At startup, the server imports bundled skill metadata from `npm-package/assets-manifest.json` into PostgreSQL so `/api/v1/skills` has initial catalog data. Bundled assets include the project-local Claude, Codex, and OpenCode skill catalogs, including copied Codex session skills under `.codex/skills`. This seed is idempotent and skips versions already present.

```powershell
set SKILL_SEED_ENABLED=true
set SKILL_SEED_ASSETS_MANIFEST=npm-package/assets-manifest.json
```

The seed populates searchable catalog metadata. Bundle artifact download endpoints are populated by publishing zip bundles through the Skill Bundle API.

### Sử dụng API (Usage Examples)

**1. Lấy danh sách skills:**

```powershell
curl "http://localhost:18080/api/v1/skills?page=0&size=20"
```

**2. Tìm kiếm skills (Semantic/Keyword):**

```powershell
curl "http://localhost:18080/api/v1/skills/search?query=log%20analysis&limit=10"
```

### Local Web Console

Run the standalone React console from `web-ui/`:

```powershell
npm install --prefix web-ui
npm run dev --prefix web-ui
```

Open `http://localhost:5173`. The console can target Docker `http://localhost:18080` or Maven dev `http://localhost:8080`. API key input is memory-only for protected publish/yank actions.
The Vite dev server uses strict port `5173`; free that port before starting the console.

**3. Đăng tải skill mới (Yêu cầu API Key):**

```powershell
curl -X POST "http://localhost:18080/api/v1/skills/publish" ^
  -H "X-API-Key: dev-api-key" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"skill-analytics\",\"version\":\"1.0.0\",\"description\":\"Analytics helper\",\"category\":\"data\",\"tags\":[\"analytics\"]}"
```

## 📋 Tài Liệu (Documentation)

* [Project Requirements](./docs/project-overview-pdr.md) - Yêu cầu và phạm vi dự án
* [Architecture](./docs/architecture.md) - Kiến trúc hệ thống chuyên sâu
* [Codebase Summary](./docs/codebase-summary.md) - Tổng quan module hiện tại

## 🛠️ Phát Triển (Development)

Xem file [Project Requirements](./docs/project-overview-pdr.md) để theo dõi phạm vi và các tiêu chí chấp nhận. Mọi đóng góp (Pull Requests) đều được chào đón!

## 📄 Giấy Phép (License)

Dự án này được phân phối dưới giấy phép **MIT License**. Xem file [LICENSE](./LICENSE) để biết chi tiết.

## Skill Bundle API

Bundle zip files must contain `SKILL.md` at archive root. The server stores metadata in PostgreSQL and zip artifacts in local storage.

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:18080/api/v1/skills/publish-bundle" `
  -Headers @{ "X-API-Key" = "dev-api-key" } `
  -Form @{ bundle = Get-Item ".\skill-analytics.zip" }
```

List bundle files:

```powershell
Invoke-RestMethod "http://localhost:18080/api/v1/skills/skill-analytics/versions/1.0.0/files"
```

Download bundle:

```powershell
Invoke-WebRequest `
  -Uri "http://localhost:18080/api/v1/skills/skill-analytics/versions/1.0.0/bundle" `
  -OutFile ".\skill-analytics-1.0.0.zip"
```
