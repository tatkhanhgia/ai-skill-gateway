# Admin Dashboard Tech Stack Research
**Quarkus Backend + Modern Frontend** | 2025-02-21

---

## Executive Summary

**Recommended Stack:** React + Tailwind + shadcn/ui + ECharts + React Hook Form + Zustand

Simple, battle-tested, excellent Quarkus integration via REST APIs. ~8KB form lib, zero boilerplate state management.

---

## Frontend Framework Analysis

| Framework | Pros | Cons | Use For |
|-----------|------|------|---------|
| **React** | Ecosystem, dev tools, Quarkus Quinoa support | Boilerplate | MCP dashboard ✓ |
| **Vue 3** | DX, lightweight, Pinia official state mgmt | Smaller ecosystem | Teams prefer elegance |
| **Svelte** | Compiler-based, small bundles, reactivity | Growing ecosystem | Performance-critical UIs |

**Choice: React** - Largest ecosystem, mature Quarkus integration patterns, widest talent pool.

---

## Form Validation Comparison

| Library | Size | Style | Best For |
|---------|------|-------|----------|
| **React Hook Form** | 8.6 KB | Uncontrolled hooks | ✓ Admin forms |
| **Formik** | ~13 KB | Higher-level API | Complex validation chains |
| **React Final Form** | Minimal | Flexible | Custom requirements |
| **VeeValidate** | Vue-only | Schema-based | Vue projects |

**Choice: React Hook Form** - Lightweight, zero deps, integrates with Zod/Yup schema validation, minimal re-renders.

Schema combo: **React Hook Form + Zod** (5KB schema validation)

---

## Data Visualization Stack

| Library | Chart Types | Performance | Best For |
|---------|------------|-------------|----------|
| **ECharts** | 30+, heatmaps, 3D, geo | Millions of points | Analytics dashboards ✓ |
| **Chart.js** | 6 basic types | Good on mobile | Simple charts |
| **Recharts** | React wrapper, 8 types | Decent | React integration ease |

**Choice: ECharts** - Professional analytics, handles large datasets, free vs Chart.js simplicity trade-off optimal for MCP.

React wrapper: **echarts-for-react** (thin layer)

---

## Component Library Strategy

**Stack:**
- **Tailwind CSS v4** - Utility-first, smallest footprint
- **shadcn/ui** - Headless components (buttons, forms, dialogs, tables)
- **Radix UI** - Unstyled accessibility primitives under shadcn
- **Headless UI** - Popover, dropdown, menu components

Why: Copy-paste components into codebase, fully customizable, zero lock-in, ~50KB total gzip vs 200KB+ pre-built.

---

## State Management

| Solution | Bundle | Learning | API Calls |
|----------|--------|----------|-----------|
| **Zustand** | 2.4 KB | 5 mins | Manual fetch |
| **Redux Toolkit** | ~50 KB | Moderate | RTK Query built-in |
| **Context API** | 0 KB | Easy | Props drilling risk |
| **Pinia** | ~8 KB | Simple | Actions + async |

**Choice: Zustand** - Minimal bundle, integrates with React Query for API caching/sync.

**Combo: Zustand + TanStack Query** (React Query) - Separate concerns: state (app logic) vs server sync (data fetching).

---

## Recommended Tech Stack

```
Frontend:
├── React 19 (framework)
├── Vite (build, <1s dev startup)
├── TypeScript (type safety)
├── Tailwind CSS v4 (styling)
├── shadcn/ui (components)
├── React Hook Form + Zod (forms)
├── ECharts + echarts-for-react (charts)
├── Zustand (app state)
├── TanStack Query (API caching)
├── Axios (HTTP client)
└── Vitest (testing)

Backend:
├── Quarkus (Java framework)
├── REST API (JSON endpoints)
├── OpenAPI/Swagger docs
└── CORS configured for dev/prod

Integration:
├── Quarkus Quinoa (SPA bundling)
├── CORS middleware
├── JWT or session auth
└── Environment-based API URLs
```

---

## Integration Pattern: Quarkus + React

```typescript
// api/client.ts (singleton)
import axios from 'axios';

const API_BASE = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const apiClient = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' }
});

// Add auth interceptor for JWT
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

**Quarkus REST endpoint:**
```java
@Path("/api/skills")
@Produces("application/json")
public class SkillController {
  @GET public List<Skill> list() { }
  @POST public Skill create(SkillRequest req) { }
  @PUT @Path("{id}") public Skill update(Long id, SkillRequest req) { }
  @DELETE @Path("{id}") public void delete(Long id) { }
}
```

---

## Bundle Size Targets

| Package | Gzip | Notes |
|---------|------|-------|
| React 19 | ~45 KB | Framework core |
| Tailwind CSS | ~18 KB | Production purged |
| shadcn/ui | ~25 KB | Per component copied |
| ECharts | ~55 KB | Modular imports |
| React Hook Form | 8.6 KB | Zero deps |
| Zustand | 2.4 KB | Minimal |
| TanStack Query | ~45 KB | API caching |
| **Total (gzip)** | **~200 KB** | Competitive |

---

## Dev Environment Setup

```bash
# Init React + Vite
npm create vite@latest admin -- --template react-ts
cd admin && npm install

# Core deps
npm install react-router-dom zustand @tanstack/react-query axios
npm install zod react-hook-form @hookform/resolvers
npm install echarts echarts-for-react
npm install -D tailwindcss postcss autoprefixer

# shadcn/ui (use CLI or manual)
npx shadcn-ui@latest init

# Environment
cp .env.example .env  # REACT_APP_API_URL=http://localhost:8080/api
```

---

## Quarkus Integration (Quinoa)

Add to `pom.xml`:
```xml
<dependency>
  <groupId>io.quarkiverse</groupId>
  <artifactId>quarkus-quinoa</artifactId>
  <version>3.2.0</version>
</dependency>
```

Config in `application.properties`:
```properties
quarkus.quinoa.build-dir=dist
quarkus.quinoa.ui-dir=src/main/webui
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:5173
```

Production: Quinoa bundles React into JAR, serves via Quarkus. Single artifact deployment.

---

## Key Decisions & Rationale

1. **React over Vue/Svelte:** Ecosystem size, Quarkus examples, Zustand/React Query maturity
2. **Zustand + React Query:** Separation of concerns (app state ≠ server state), no Redux boilerplate
3. **shadcn/ui over Material/Chakra:** Copy-paste flexibility, zero provider lock-in, Tailwind alignment
4. **ECharts over Chart.js:** Future-proof for complex analytics, handles large datasets
5. **React Hook Form:** Uncontrolled forms reduce re-renders, 8.6 KB footprint
6. **Vite build:** <1s HMR, faster dev experience than CRA

---

## Unresolved Questions

- [ ] Real-time features needed? (WebSocket vs polling)
- [ ] User auth: Session-based or JWT?
- [ ] Offline-first capability required?
- [ ] Dark mode support priority?
- [ ] Mobile-first or desktop-primary dashboard?
- [ ] Multi-language i18n scope?

---

## Next Steps

1. **Create React project structure** with routing, API client, Zustand stores
2. **Setup shadcn/ui** components for common patterns (tables, modals, forms)
3. **Integrate ECharts** for analytics widgets
4. **Configure Quarkus Quinoa** for bundled deployment
5. **Implement auth** (JWT or session middleware)
6. **Write E2E tests** (Cypress/Playwright)
7. **Deploy** single JAR with embedded frontend

---

**Report Generated:** 2025-02-21 | **Status:** Research Complete | **Ready for Implementation Planning**
