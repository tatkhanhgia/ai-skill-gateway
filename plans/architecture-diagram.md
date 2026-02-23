# MCP Admin Dashboard - Architecture Diagram

---

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    ADMIN DASHBOARD (React)                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Pages: Dashboard | Skills | Settings | Analytics     │  │
│  │ Built with shadcn/ui components                       │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │         State Management (Zustand)                    │  │
│  │  • Auth Store (user, token, perms)                    │  │
│  │  • UI Store (sidebar, theme, modals)                  │  │
│  │  • App Store (filters, page state)                    │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │    API Layer (TanStack Query + Axios)                 │  │
│  │  • Auto caching & staleTime mgmt                      │  │
│  │  • Background sync (polling/refetch)                  │  │
│  │  • Optimistic updates                                 │  │
│  └──────────────────────────────────────────────────────┘  │
│                           │                                   │
│                           │ HTTP (REST JSON)                 │
│                           ▼                                   │
└─────────────────────────────────────────────────────────────┘
                              ▲
                              │
         ┌────────────────────┴────────────────────┐
         │                                         │
         │                                         │
┌────────┴──────────────────────────┐   ┌────────┴──────────────┐
│   Quarkus Backend (Java)           │   │  PostgreSQL / Cache  │
│  ┌──────────────────────────────┐ │   │                      │
│  │ REST Endpoints (/api/v1/...) │ │   │ • Skills Data        │
│  │  • GET /skills               │ │   │ • User Sessions      │
│  │  • POST /skills              │ │   │ • Analytics          │
│  │  • PUT /skills/{id}          │ │   │ • Vector Index       │
│  │  • DELETE /skills/{id}       │ │   │                      │
│  │  • GET /analytics            │ │   │                      │
│  └──────────────────────────────┘ │   │                      │
│                                    │   │                      │
│  ┌──────────────────────────────┐ │   │                      │
│  │ Business Logic Layers        │ │   │                      │
│  │  • Service (business rules)  │ │   │                      │
│  │  • Repository (data access)  │ │   │                      │
│  │  • Entity (JPA models)       │ │   │                      │
│  └──────────────────────────────┘ │   │                      │
│                                    │   │                      │
│  ┌──────────────────────────────┐ │   │                      │
│  │ Middleware                   │ │   │                      │
│  │  • CORS filter               │ │   │                      │
│  │  • Auth filter (JWT)         │ │   │                      │
│  │  • Error handler             │ │   │                      │
│  └──────────────────────────────┘ │   │                      │
└─────────────────────────────────────┘   └─────────────────────┘
```

---

## Data Flow: Create Skill

```
User fills form (React Hook Form)
        ↓
Form validates (Zod schema)
        ↓
User clicks Submit
        ↓
Zustand store updates optimistically
        ↓
TanStack Query sends POST /api/v1/skills (Axios)
        ↓
────────────────────────────────────────────────────
        ↓
Quarkus @POST /skills receives request
        ↓
Auth filter validates JWT
        ↓
SkillController.create() called
        ↓
SkillService validates business rules
        ↓
SkillRepository.save() persists to DB
        ↓
Response: 201 + Skill JSON
        ↓
────────────────────────────────────────────────────
        ↓
TanStack Query processes response
        ↓
Cache invalidation (queryClient.invalidateQueries)
        ↓
UI re-fetches /api/v1/skills
        ↓
Table re-renders with new skill
        ↓
Success toast notification
```

---

## Component Hierarchy

```
<App>
  ├─ <Layout>
  │  ├─ <Sidebar>
  │  │  └─ Navigation links (useNavigate)
  │  ├─ <TopBar>
  │  │  └─ User profile, logout
  │  └─ <Outlet>  (React Router)
  │     ├─ <DashboardPage>
  │     │  ├─ <StatCard> x4 (Zustand stats)
  │     │  ├─ <SkillsChart> (ECharts)
  │     │  └─ <RecentActivity> (TanStack Query)
  │     ├─ <SkillsPage>
  │     │  ├─ <SkillsTable> (TanStack Query data)
  │     │  ├─ <SkillModal> (React Hook Form)
  │     │  └─ <DeleteDialog> (shadcn/ui Dialog)
  │     ├─ <SettingsPage>
  │     │  └─ <SettingsForm> (Zod validation)
  │     └─ <AnalyticsPage>
  │        ├─ <BarChart>
  │        ├─ <HeatmapChart>
  │        └─ <TimeSeriesChart>
  └─ <ReactQueryDevtools>  (Debug, dev only)
```

---

## State Management Separation

```
Zustand (App State)
├─ Auth store
│  ├─ user: User | null
│  ├─ token: string
│  ├─ isAuthenticated: boolean
│  └─ actions: { login(), logout(), setUser() }
├─ UI store
│  ├─ sidebarOpen: boolean
│  ├─ theme: 'light' | 'dark'
│  ├─ modalOpen: { [key: string]: boolean }
│  └─ actions: { toggleSidebar(), setTheme() }
└─ App store
   ├─ filters: { search: string, status: string }
   ├─ sortBy: string
   └─ actions: { setSearch(), setSortBy() }

TanStack Query (Server State)
├─ queryKey: ['skills']
│  └─ data: Skill[]
├─ queryKey: ['skills', id]
│  └─ data: Skill
├─ queryKey: ['analytics', period]
│  └─ data: Analytics
└─ Automatic: loading, error, staleTime, refetchOnWindowFocus
```

---

## API Contract (OpenAPI)

```yaml
/api/v1/skills:
  GET:
    summary: List all skills
    parameters:
      - name: search
        in: query
        schema:
          type: string
      - name: limit
        in: query
        schema:
          type: integer
          default: 20
    responses:
      200:
        content:
          application/json:
            schema:
              type: array
              items:
                $ref: '#/components/schemas/Skill'

  POST:
    summary: Create skill
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/SkillRequest'
    responses:
      201:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Skill'

/api/v1/skills/{id}:
  GET:
    summary: Get skill by ID
    parameters:
      - name: id
        in: path
        required: true
        schema:
          type: integer
    responses:
      200:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Skill'
      404:
        description: Skill not found

  PUT:
    summary: Update skill
    parameters:
      - name: id
        in: path
        required: true
        schema:
          type: integer
    requestBody:
      required: true
      content:
        application/json:
          schema:
            $ref: '#/components/schemas/SkillRequest'
    responses:
      200:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Skill'

  DELETE:
    summary: Delete skill
    parameters:
      - name: id
        in: path
        required: true
        schema:
          type: integer
    responses:
      204:
        description: Deleted successfully
```

---

## Deployment Topology

```
Development:
┌─────────────────┐      ┌─────────────────┐
│  React Vite     │      │  Quarkus Dev    │
│  :5173          │      │  :8080/api      │
│  HMR enabled    │◄────►│  Hot reload     │
│  localhost      │      │  localhost      │
└─────────────────┘      └─────────────────┘
       │                         │
       └────────────┬────────────┘
                    │
            PostgreSQL/Docker
              localhost:5432

Production:
┌──────────────────────────────────────┐
│  Single JAR (Quarkus + React dist)   │
│  ├─ /                  (React SPA)    │
│  ├─ /api/v1/*          (Quarkus APIs) │
│  └─ Static assets gzipped             │
│                                       │
│  Served on port 8080                 │
│  Environment: prod                   │
│  Logs: JSON structured                │
└──────────────────────────────────────┘
       │
       │ HTTPS
       ▼
    Users (nginx reverse proxy)
```

---

## Form Validation Flow

```
User Input (text field)
        ↓
React Hook Form captures onChange
        ↓
Zod schema validates async (email, unique name, etc.)
        ↓
Errors stored in formState.errors
        ↓
Error message rendered below input
        ↓
Submit button disabled if form invalid
        ↓
User fixes error
        ↓
Re-validates on blur or change
        ↓
Form valid → Submit enabled
```

---

## Chart Data Flow (ECharts)

```
Dashboard mounts
        ↓
TanStack Query: useQuery(['analytics', period])
        ↓
Axios: GET /api/v1/analytics?period=7d
        ↓
Quarkus Service aggregates metrics
        ↓
Response: { labels: [], datasets: [] }
        ↓
EChartsReact renders with option config
        ↓
User changes period filter (Zustand update)
        ↓
Query refetches automatically (staleTime expired)
        ↓
Chart animates new data
```

---

**Generated:** 2025-02-21 | **Format:** Architecture Reference | **Status:** Complete
