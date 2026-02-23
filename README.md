# AI Skill Gateway

> 🚀 Gateway thông minh chạy local trên Windows - Tìm kiếm và đề xuất skill dựa trên AI

## ✨ Tính năng

- 🔍 **Tìm kiếm thông minh** - Kết hợp keyword search và semantic search
- 🤖 **AI Local** - Chạy hoàn toàn local với Ollama, đảm bảo privacy
- 📦 **Đa nguồn lưu trữ** - Local Directory, PostgreSQL, Google Drive
- 🧠 **Memory lâu dài** - Vector DB lưu trữ ngữ cảnh và lịch sử
- ⚡ **API RESTful** - Dễ dàng tích hợp với mọi client

## 🏗️ Kiến trúc

```
Client → AI Gateway → [Local AI + Vector DB + Multi Storage]
```

## 🚀 Quick Start

### Yêu cầu

- Windows 10/11
- Java 21+
- Maven 3.9+
- Docker Desktop
- [Ollama](https://ollama.ai/) đã cài đặt

### Cài đặt

```powershell
# Clone
cd ai-skill-gateway

# Start infra
docker compose up -d db ollama

# Chạy server (JVM)
set AUTH_API_KEY=dev-api-key
mvn quarkus:dev
```

Health check: `http://localhost:8080/q/health`

### Sử dụng

```powershell
# List skills
curl "http://localhost:8080/api/v1/skills?page=0&size=20"

# Search skills
curl "http://localhost:8080/api/v1/skills/search?query=log%20analysis&limit=10"

# Publish skill (requires API key)
curl -X POST "http://localhost:8080/api/v1/skills/publish" ^
  -H "X-API-Key: dev-api-key" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"skill-analytics\",\"version\":\"1.0.0\",\"description\":\"Analytics helper\",\"category\":\"data\",\"tags\":[\"analytics\"]}"
```

## 📋 Documentation

- [Implementation Plan](./PLAN.md) - Kế hoạch triển khai chi tiết
- [Architecture](./docs/architecture.md) - Kiến trúc hệ thống
- [API Reference](./docs/api-reference.md) - Tài liệu API

## 🛠️ Development

Xem [PLAN.md](./PLAN.md) để biết roadmap chi tiết.

## 📄 License

MIT
