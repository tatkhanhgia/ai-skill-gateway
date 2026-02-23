# AI Skill Gateway - Implementation Plan

## 📋 Tổng quan Dự án

**Tên dự án:** AI Skill Gateway  
**Mục đích:** Gateway thông minh chạy local trên Windows, hỗ trợ tìm kiếm và đề xuất skill dựa trên AI với khả năng kết nối đa dạng nguồn lưu trữ.

---

## 🏗️ Kiến trúc Tổng thể

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                                       │
│  (CLI / Web UI / API Consumers)                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         AI GATEWAY (Windows)                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ API Server  │  │   AI Core   │  │   Memory    │  │   Skill Registry    │ │
│  │  (FastAPI)  │  │  (Ollama)   │  │   Manager   │  │                     │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              ▼                     ▼                     ▼
    ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
    │   Vector DB     │   │   Skill Stores  │   │   Memory DB     │
    │   (ChromaDB)    │   │   (Adapters)    │   │   (ChromaDB)    │
    │                 │   │                 │   │                 │
    │ • Skill Index   │   │ • Local Dir     │   │ • Chat History  │
    │ • Embeddings    │   │ • PostgreSQL    │   │ • Context       │
    │ • Metadata      │   │ • Google Drive  │   │ • Preferences   │
    └─────────────────┘   └─────────────────┘   └─────────────────┘
```

---

## 📁 Cấu trúc Thư mục

```
ai-skill-gateway/
├── docs/                       # Tài liệu chi tiết
│   ├── architecture.md         # Kiến trúc hệ thống
│   ├── api-reference.md        # API documentation
│   └── storage-adapters.md     # Hướng dẫn thêm storage mới
│
├── src/                        # Source code chính
│   ├── core/                   # Core components
│   │   ├── __init__.py
│   │   ├── gateway.py          # Main gateway class
│   │   ├── config.py           # Configuration management
│   │   └── exceptions.py       # Custom exceptions
│   │
│   ├── ai/                     # AI Layer
│   │   ├── __init__.py
│   │   ├── llm.py              # Local LLM wrapper (Ollama)
│   │   ├── embedding.py        # Embedding model
│   │   ├── intent.py           # Intent analyzer
│   │   └── reranker.py         # Semantic reranker
│   │
│   ├── storage/                # Storage Adapters
│   │   ├── __init__.py
│   │   ├── base.py             # Abstract base class
│   │   ├── local.py            # Local Directory adapter
│   │   ├── postgres.py         # PostgreSQL adapter
│   │   ├── googledrive.py      # Google Drive adapter
│   │   └── registry.py         # Storage registry
│   │
│   ├── vector_db/              # Vector Database
│   │   ├── __init__.py
│   │   ├── chroma.py           # ChromaDB wrapper
│   │   ├── embeddings.py       # Embedding pipeline
│   │   └── memory.py           # Long-term memory
│   │
│   ├── api/                    # API Layer
│   │   ├── __init__.py
│   │   ├── server.py           # FastAPI server
│   │   ├── routes.py           # API endpoints
│   │   └── models.py           # Pydantic models
│   │
│   ├── skill/                  # Skill Management
│   │   ├── __init__.py
│   │   ├── models.py           # Skill data models
│   │   ├── indexer.py          # Skill indexer
│   │   ├── searcher.py         # Skill searcher
│   │   └── resolver.py         # Dependency resolver
│   │
│   └── utils/                  # Utilities
│       ├── __init__.py
│       ├── logger.py
│       └── helpers.py
│
├── tests/                      # Unit & Integration tests
│   ├── unit/
│   └── integration/
│
├── config/                     # Configuration files
│   ├── config.yaml             # Main config
│   ├── storage.yaml            # Storage connections
│   └── logging.yaml            # Logging config
│
├── scripts/                    # Helper scripts
│   ├── setup.ps1               # Windows setup script
│   ├── install-ollama.ps1      # Install Ollama
│   └── sync-skills.ps1         # Manual sync script
│
├── data/                       # Local data (gitignored)
│   ├── vector_db/              # ChromaDB data
│   ├── cache/                  # Cache files
│   └── logs/                   # Log files
│
├── requirements.txt            # Python dependencies
├── requirements-dev.txt        # Dev dependencies
├── main.py                     # Entry point
├── .env.example                # Environment template
├── .gitignore
└── README.md                   # Project overview
```

---

## 🚀 Roadmap Triển khai

### Phase 1: Foundation (Tuần 1-2)
**Mục tiêu:** Có một gateway chạy được với Local Directory

| Task | Chi tiết | Ưu tiên |
|------|----------|---------|
| 1.1 | Setup project structure, logging, config | P0 |
| 1.2 | Implement Local Directory storage adapter | P0 |
| 1.3 | Integrate Ollama LLM (llama3.1:8b) | P0 |
| 1.4 | Setup ChromaDB for vector storage | P0 |
| 1.5 | Build FastAPI server với endpoints cơ bản | P0 |
| 1.6 | Skill indexer: đọc và parse skill từ local | P0 |
| 1.7 | Basic search: keyword + semantic | P0 |

**Deliverable:** `POST /search` endpoint hoạt động với local storage

---

### Phase 2: Intelligence (Tuần 3-4)
**Mục tiêu:** Tăng cường AI capabilities và memory

| Task | Chi tiết | Ưu tiên |
|------|----------|---------|
| 2.1 | Intent analyzer: phân loại query type | P1 |
| 2.2 | Embedding pipeline với nomic-embed-text | P1 |
| 2.3 | Semantic search với vector similarity | P1 |
| 2.4 | Reranker: cross-encoder cho top-k results | P1 |
| 2.5 | Long-term memory: lưu trữ conversations | P1 |
| 2.6 | Context-aware response builder | P1 |

**Deliverable:** AI có thể hiểu ngữ cảnh và trả về kết quả chính xác

---

### Phase 3: Multi-Storage (Tuần 5-6)
**Mục tiêu:** Hỗ trợ nhiều nguồn lưu trữ

| Task | Chi tiết | Ưu tiên |
|------|----------|---------|
| 3.1 | Abstract storage interface (base.py) | P1 |
| 3.2 | PostgreSQL adapter | P1 |
| 3.3 | Google Drive adapter | P2 |
| 3.4 | Sync scheduler: đồng bộ metadata định kỳ | P2 |
| 3.5 | Storage health checker | P2 |
| 3.6 | Unified search across all storages | P1 |

**Deliverable:** Query trả về skills từ tất cả storages đã cấu hình

---

### Phase 4: Advanced Features (Tuần 7-8)
**Mục tiêu:** Production-ready với advanced capabilities

| Task | Chi tiết | Ưu tiên |
|------|----------|---------|
| 4.1 | Skill versioning support | P2 |
| 4.2 | Dependency resolver | P2 |
| 4.3 | Web UI cho management | P3 |
| 4.4 | Authentication & API keys | P2 |
| 4.5 | Rate limiting | P2 |
| 4.6 | Monitoring & metrics | P3 |
| 4.7 | Windows service wrapper | P2 |

**Deliverable:** Production-ready Windows application

---

## 🔧 Công nghệ Stack

### Core
| Component | Technology | Lý do chọn |
|-----------|------------|------------|
| Language | Python 3.11+ | Ecosystem AI mạnh, dễ maintain |
| API Framework | FastAPI | Async, auto-docs, hiệu suất cao |
| AI Runtime | Ollama | Dễ setup, quản lý model tiện |
| LLM Model | llama3.1:8b | Balance giữa hiệu năng và chất lượng |
| Embedding | nomic-embed-text | Local, nhẹ, đa ngôn ngữ tốt |

### Database & Storage
| Component | Technology | Lý do chọn |
|-----------|------------|------------|
| Vector DB | ChromaDB | Embed-friendly, không cần external server |
| Cache | SQLite/DuckDB | Lightweight, embedded |
| Config | YAML + Pydantic | Human-readable, validated |

### Windows Integration
| Component | Technology | Lý do chọn |
|-----------|------------|------------|
| Packaging | PyInstaller | Build .exe dễ dàng |
| Service | NSSM / WinSW | Chạy như Windows service |
| Installer | Inno Setup / MSI | Professional installation |

---

## 📊 Data Models

### Skill Model
```yaml
Skill:
  id: "uuid"
  name: "string"           # Tên skill
  version: "semver"        # Phiên bản
  description: "text"      # Mô tả chi tiết
  author: "string"
  tags: ["string"]         # Tags phân loại
  
  # Technical metadata
  language: "python|js|..."
  platform: ["windows", "linux", "mac"]
  dependencies: ["skill_id:version"]
  
  # Storage info
  source: "local|postgres|gdrive"
  location: "path/to/skill"
  checksum: "sha256"
  
  # AI-related
  embedding: "vector[768]"  # Cached embedding
  usage_count: "int"        # Độ phổ biến
  rating: "float"           # Đánh giá
  
  created_at: "datetime"
  updated_at: "datetime"
```

### Query Model
```yaml
Query:
  id: "uuid"
  client_id: "string"
  query_text: "text"
  
  # Context
  session_id: "uuid"
  history: ["previous_queries"]
  preferences: {"language": "vi", "platform": "windows"}
  
  # Filters
  filters:
    tags: ["string"]
    source: ["local", "postgres"]
    min_rating: "float"
  
  timestamp: "datetime"
```

### Response Model
```yaml
Response:
  query_id: "uuid"
  
  # Results
  skills: [
    {
      skill: "Skill",
      relevance_score: "float",
      match_reason: "text"    # Giải thích tại sao match
    }
  ]
  
  # AI-generated
  summary: "text"            # Tóm tắt kết quả
  recommendations: ["text"]  # Gợi ý bổ sung
  usage_guide: "text"        # Hướng dẫn sử dụng
  
  # Metadata
  processing_time: "float"   # ms
  sources_searched: ["string"]
  cached: "bool"
```

---

## 🔌 API Endpoints

### Core Endpoints

#### 1. Search Skills
```http
POST /api/v1/skills/search
Content-Type: application/json

{
  "query": "Tôi cần phân tích log hệ thống Windows",
  "filters": {
    "platform": ["windows"],
    "language": "python"
  },
  "limit": 5,
  "session_id": "uuid"
}
```

#### 2. Get Skill Detail
```http
GET /api/v1/skills/{skill_id}
```

#### 3. List Storages
```http
GET /api/v1/storages
```

#### 4. Sync Storage
```http
POST /api/v1/storages/{storage_id}/sync
```

#### 5. Chat (Conversational)
```http
POST /api/v1/chat
Content-Type: application/json

{
  "message": "Tôi cần làm việc A",
  "session_id": "uuid",
  "context": {}
}
```

### Admin Endpoints

#### 6. Health Check
```http
GET /health
```

#### 7. Stats
```http
GET /api/v1/admin/stats
```

#### 8. Reload Config
```http
POST /api/v1/admin/reload
```

---

## 🔍 Query Processing Pipeline

```
1. Receive Query
        │
        ▼
2. Intent Analysis (AI Local)
   ├── Query type: search | chat | info
   ├── Keywords extraction
   └── Context requirements
        │
        ▼
3. Memory Check (Vector DB)
   ├── Similar past queries?
   └── Cached results?
        │
        ▼
4. Storage Query
   ├── Broadcast to all storages
   ├── Collect candidates
   └── Metadata filtering
        │
        ▼
5. Semantic Retrieval (Vector DB)
   ├── Embed query
   ├── Similarity search
   └── Retrieve skill embeddings
        │
        ▼
6. Reranking
   ├── Cross-encoder scoring
   ├── Filter by threshold
   └── Select top-k
        │
        ▼
7. Response Generation (AI Local)
   ├── Analyze matches
   ├── Generate summary
   └── Build usage guide
        │
        ▼
8. Memory Update
   └── Store interaction
        │
        ▼
9. Return Response
```

---

## ⚙️ Configuration

### config.yaml
```yaml
app:
  name: "AI Skill Gateway"
  version: "1.0.0"
  host: "127.0.0.1"
  port: 8080
  debug: false

ai:
  llm:
    provider: "ollama"
    model: "llama3.1:8b"
    temperature: 0.7
    max_tokens: 2048
  
  embedding:
    model: "nomic-embed-text"
    dimension: 768
  
  reranker:
    enabled: true
    model: "cross-encoder/ms-marco-MiniLM-L-6-v2"
    top_k: 5

vector_db:
  provider: "chromadb"
  path: "./data/vector_db"
  collection_name: "skills"

storage:
  adapters:
    - name: "local"
      type: "local"
      enabled: true
      config:
        base_path: "C:/Skills"
        recursive: true
    
    - name: "postgres"
      type: "postgresql"
      enabled: false
      config:
        host: "localhost"
        port: 5432
        database: "skills"
        user: "admin"
        password: "${POSTGRES_PASSWORD}"
    
    - name: "gdrive"
      type: "googledrive"
      enabled: false
      config:
        credentials_path: "./config/gdrive-credentials.json"
        folder_id: "..."

memory:
  enabled: true
  max_history: 10
  similarity_threshold: 0.85

logging:
  level: "INFO"
  file: "./data/logs/gateway.log"
  rotation: "1 day"
```

---

## 🧪 Testing Strategy

### Unit Tests
- Storage adapters (mock external calls)
- AI wrappers (mock LLM responses)
- Data models validation
- Utility functions

### Integration Tests
- End-to-end query flow
- Multi-storage search
- Vector DB operations
- API endpoints

### Performance Tests
- Concurrent query handling
- Large skill set (10K+ skills)
- Memory usage over time

---

## 📦 Deployment

### Development
```bash
# Setup
python -m venv venv
venv\Scripts\activate
pip install -r requirements-dev.txt

# Run
python main.py
```

### Production (Windows)
```powershell
# Build executable
pyinstaller --onefile --windowed main.py

# Install as service
.\scripts\install-service.ps1

# Start
net start ai-skill-gateway
```

---

## 📚 Tài liệu Tham khảo

1. [Ollama Documentation](https://ollama.ai/docs)
2. [ChromaDB Docs](https://docs.trychroma.com/)
3. [FastAPI Docs](https://fastapi.tiangolo.com/)
4. [Sentence Transformers](https://www.sbert.net/)

---

## 📝 Notes

- Luôn giữ AI Local để đảm bảo privacy
- Vector DB nên có backup định kỳ
- Cân nhắc dùng SQLite cho metadata cache nếu scale nhỏ
- Theo dõi VRAM usage khi chạy LLM local

---

*Created: 2026-02-19*  
*Last Updated: 2026-02-19*  
*Version: 1.0*
