# MCP Admin Dashboard - Tech Stack Quick Reference

**Recommended:** React 19 + Vite + Tailwind + shadcn/ui + ECharts + React Hook Form + Zustand + TanStack Query

---

## Why This Stack?

| Aspect | Choice | Reason |
|--------|--------|--------|
| **Frontend** | React | Quarkus integration proven, largest ecosystem |
| **Build Tool** | Vite | <1s HMR, npm native ESM, faster than webpack |
| **Styling** | Tailwind CSS v4 | Utility-first, ~18KB gzip, zero runtime |
| **Components** | shadcn/ui | Copy-paste, headless, Radix primitives, customizable |
| **Forms** | React Hook Form + Zod | 8.6 KB, uncontrolled, type-safe schemas |
| **Charts** | ECharts | Professional dashboards, millions of data points, 30+ chart types |
| **State** | Zustand | 2.4 KB, minimal boilerplate, integrates TanStack Query |
| **API Cache** | TanStack Query | Built-in caching, sync, background refresh |
| **HTTP** | Axios | Standard practice, interceptors for auth |
| **Auth** | JWT + localStorage | Simple token-based, works with Quarkus filters |
| **Testing** | Vitest | Vue ecosystem but works React, Vite native |

---

## Bundle Size (Production Gzip)

```
React 19              45 KB
Tailwind CSS          18 KB
shadcn/ui (partial)   25 KB
ECharts               55 KB
React Hook Form        8.6 KB
Zustand               2.4 KB
TanStack Query        45 KB
─────────────────────────
TOTAL                ~200 KB
```

Competitive with alternative stacks. Single bundle split strategy reduces per-page loads.

---

## One-Liner Recipes

**Form with Validation:**
```typescript
const { register, handleSubmit, formState: { errors } } = useForm({
  resolver: zodResolver(schema)
});
```

**API Call with Caching:**
```typescript
const { data, isLoading } = useQuery({
  queryKey: ['skills'],
  queryFn: () => apiClient.get('/skills').then(r => r.data)
});
```

**Global State:**
```typescript
const store = create((set) => ({
  count: 0,
  inc: () => set(s => ({ count: s.count + 1 }))
}));
```

**Chart:**
```typescript
<EChartsReact option={{
  xAxis: { type: 'category', data: ['Mon', 'Tue'] },
  yAxis: { type: 'value' },
  series: [{ data: [120, 200], type: 'line' }]
}}/>
```

---

## Quarkus Integration

**1. Add Quinoa dependency** (pom.xml)
```xml
<dependency>
  <groupId>io.quarkiverse</groupId>
  <artifactId>quarkus-quinoa</artifactId>
  <version>3.2.0</version>
</dependency>
```

**2. Config** (application.properties)
```properties
quarkus.quinoa.build-dir=dist
quarkus.quinoa.ui-dir=src/main/webui
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:5173
```

**3. Build** - Quarkus embeds React dist in JAR. Single deployment.

**4. Serve** - Frontend at `GET /`, API at `GET /api/*`

---

## 30-Min Quickstart

```bash
# 1. React + Vite
npm create vite@latest admin -- --template react-ts && cd admin

# 2. Install core
npm install react-router-dom zustand @tanstack/react-query axios zod react-hook-form echarts echarts-for-react
npm install -D tailwindcss postcss autoprefixer

# 3. Tailwind init
npx tailwindcss init -p

# 4. Copy shadcn/ui config OR manual setup
mkdir -p src/components/ui

# 5. Start dev
npm run dev

# 6. Backend: curl -X POST http://localhost:8080/api/skills
```

---

## Alternatives Considered

| Stack | Trade-off |
|-------|-----------|
| **Vue 3 + Pinia + ECharts** | Smaller community, but DX excellent |
| **Svelte + Stores** | Best performance, compiler magic, immature ecosystem |
| **Angular + RxJS** | Enterprise-heavy, overkill for simple admin |
| **Solid.js** | Performance-optimal, tiny ecosystem, new paradigm |

**Verdict:** React wins on pragmatism for team scaling and available examples.

---

## Security Checklist

- [ ] CORS whitelist Quarkus origin in production
- [ ] JWT stored in httpOnly cookie (not localStorage) for auth
- [ ] API versioning (/api/v1) for backward compatibility
- [ ] Environment-based API URL (dev/prod/staging)
- [ ] Input validation both client (Zod) and server (Quarkus validators)
- [ ] HTTPS in production, CSP headers
- [ ] Sanitize user inputs before rendering

---

## File Structure Template

```
src/
├── main.tsx
├── api/
│   └── client.ts              (axios instance)
├── components/
│   ├── Layout.tsx
│   ├── Sidebar.tsx
│   └── ui/                    (shadcn/ui copied)
├── pages/
│   ├── Dashboard.tsx
│   ├── Skills.tsx
│   └── Settings.tsx
├── hooks/
│   ├── useSkills.ts           (TanStack Query)
│   └── useAuthStore.ts        (Zustand)
├── stores/
│   ├── auth.ts
│   └── ui.ts
├── types/
│   └── api.ts                 (TypeScript interfaces)
├── schemas/
│   └── forms.ts               (Zod schemas)
└── styles/
    └── globals.css            (Tailwind)
```

---

## Performance Tips

1. **Code splitting:** React Router v6 lazy loading per route
2. **Image optimization:** next/image equivalent or modern IMG tag
3. **ECharts modular:** Import specific chart modules, not full library
4. **TanStack Query staleTime:** 5 min default, adjust per domain
5. **Tailwind purging:** Production build removes unused CSS automatically

---

**Status:** ✅ Ready for Implementation | **Confidence:** High | **Risk:** Low (proven stack)
