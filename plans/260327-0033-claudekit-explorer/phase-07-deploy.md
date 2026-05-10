# Phase 07: Deploy to Vercel

## Context Links
- [Plan Overview](./plan.md)
- [Vercel Vite docs](https://vercel.com/docs/frameworks/vite)

## Overview
- **Priority:** P3
- **Status:** pending
- **Effort:** 1h
- **Description:** Deploy the built SPA to Vercel. Configure SPA routing, build command, environment variables, and custom domain (optional).

## Key Insights
- Vite SPA on Vercel needs `vercel.json` rewrite rules for client-side routing
- `skills-catalog.json` is committed to repo, so no build-time data generation needed on Vercel
- Free tier sufficient: static SPA, no serverless functions needed
- GitHub integration: auto-deploy on push to main

## Requirements

### Functional
- App builds successfully on Vercel
- All routes work (no 404 on direct URL access)
- Deep links work (e.g., `/explorer?skill=cook`)
- Performance: First Contentful Paint <1.5s

### Non-functional
- Auto-deploy from GitHub main branch
- Preview deploys for PRs
- Custom domain (optional, can configure later)
- HTTPS enforced

## Implementation Steps

### 1. Create vercel.json

```json
{
  "rewrites": [
    { "source": "/(.*)", "destination": "/index.html" }
  ],
  "headers": [
    {
      "source": "/assets/(.*)",
      "headers": [
        { "key": "Cache-Control", "value": "public, max-age=31536000, immutable" }
      ]
    }
  ]
}
```

### 2. Verify build command

Ensure `package.json` has correct build:
```json
{
  "scripts": {
    "build": "tsc -b && vite build",
    "preview": "vite preview"
  }
}
```

Vercel auto-detects Vite and uses `npm run build` with `dist` as output.

### 3. Connect GitHub repo to Vercel

```bash
# Install Vercel CLI (optional, can use dashboard)
npm i -g vercel

# Link project
vercel link

# Deploy
vercel --prod
```

Or: connect via Vercel dashboard -> Import Git Repository -> select `claudekit-explorer`.

### 4. Configure Vercel project settings

- **Framework Preset:** Vite
- **Build Command:** `npm run build`
- **Output Directory:** `dist`
- **Install Command:** `npm install`
- **Node.js Version:** 20.x

### 5. Test deployment

- [ ] Visit deployed URL
- [ ] Test direct navigation to `/explorer`
- [ ] Test direct navigation to `/finder`
- [ ] Test deep link `/explorer?skill=cook`
- [ ] Test on mobile device
- [ ] Run Lighthouse on deployed URL

### 6. (Optional) Custom domain

If desired:
- Add domain in Vercel dashboard
- Configure DNS CNAME record
- Vercel handles SSL automatically

### 7. Add deployment badge to README

```markdown
[![Deploy with Vercel](https://vercel.com/button)](https://vercel.com/new/clone?repository-url=...)
```

## Todo List
- [ ] Create `vercel.json` with SPA rewrites + cache headers
- [ ] Verify `npm run build` succeeds locally with no errors
- [ ] Push to GitHub
- [ ] Connect repo to Vercel (dashboard or CLI)
- [ ] Configure project settings (framework, build, output)
- [ ] Deploy to production
- [ ] Verify all routes work on deployed URL
- [ ] Verify deep links work
- [ ] Run Lighthouse on deployed URL (target >90)
- [ ] Add deploy badge to README

## Success Criteria
- App accessible at Vercel URL
- All 3 routes work via direct URL access
- Deep link `/explorer?skill=debug` loads correctly
- Auto-deploy triggers on push to main
- PR preview deploys work
- Lighthouse Performance >90 on deployed version
- No console errors in production

## Risk Assessment
| Risk | Mitigation |
|------|------------|
| Client-side routing 404s | `vercel.json` rewrites handle this |
| Large bundle size | Vite tree-shakes; check bundle with `vite-bundle-analyzer` if >500KB |
| Vercel free tier limits | Static SPA well within limits (100GB bandwidth) |

## Security Considerations
- No secrets or API keys in frontend code
- No server-side functions needed
- HTTPS enforced by Vercel
- No user data collected or stored

## Next Steps (Phase 2 - Future)
- Dark mode toggle
- Optional Gateway API integration for semantic search
- Skill comparison view (side-by-side)
- Skill usage analytics (anonymous)
- OG image generation for social sharing
- PWA support for offline access
