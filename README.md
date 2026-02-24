# 🚀 AI Skill Gateway

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
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
* Java 21+
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

**3. Chạy Server (JVM mode):**

```powershell
set AUTH_API_KEY=dev-api-key
mvn quarkus:dev
```

> **Health check:** Sau khi khởi động, kiểm tra trạng thái tại `http://localhost:8080/q/health`

### Sử dụng API (Usage Examples)

**1. Lấy danh sách skills:**

```powershell
curl "http://localhost:8080/api/v1/skills?page=0&size=20"
```

**2. Tìm kiếm skills (Semantic/Keyword):**

```powershell
curl "http://localhost:8080/api/v1/skills/search?query=log%20analysis&limit=10"
```

**3. Đăng tải skill mới (Yêu cầu API Key):**

```powershell
curl -X POST "http://localhost:8080/api/v1/skills/publish" ^
  -H "X-API-Key: dev-api-key" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"skill-analytics\",\"version\":\"1.0.0\",\"description\":\"Analytics helper\",\"category\":\"data\",\"tags\":[\"analytics\"]}"
```

## 📋 Tài Liệu (Documentation)

* [Implementation Plan](./PLAN.md) - Kế hoạch triển khai chi tiết
* [Architecture](./docs/architecture.md) - Kiến trúc hệ thống chuyên sâu
* [API Reference](./docs/api-reference.md) - Tài liệu đặc tả API

## 🛠️ Phát Triển (Development)

Xem file [PLAN.md](./PLAN.md) để theo dõi roadmap và các tính năng sắp ra mắt. Mọi đóng góp (Pull Requests) đều được chào đón!

## 📄 Giấy Phép (License)

Dự án này được phân phối dưới giấy phép **MIT License**. Xem file [LICENSE](./LICENSE) để biết chi tiết.
