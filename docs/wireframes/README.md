# AI Skill Repository Dashboard - Wireframes

**Version:** 1.0
**Created:** 2026-02-21
**Design System:** See `/docs/design-guidelines.md`

---

## Overview

This directory contains HTML wireframes for the AI Skill Repository Dashboard. All wireframes are production-ready and follow the design system specifications in `design-guidelines.md`.

## Wireframes

### 1. Skill Discovery Page
**File:** `01-skill-discovery-page.html`

**Purpose:** Main entry point for users to discover and browse available skills.

**Key Features:**
- Search bar with 300ms debounce
- Filter sidebar (category, status, tags)
- 3-column skill card grid (responsive: 2 on tablet, 1 on mobile)
- Skill cards with:
  - Icon, title, subtitle, badge
  - Description with line clamp
  - Metadata (rating, version, size)
  - Tags display
  - Preview + Install buttons
- Pagination controls
- Status badges (Active, Beta, Deprecated)

**Responsive Breakpoints:**
- Desktop (1024px+): 3 columns, full sidebar
- Tablet (768px-1023px): 2 columns, sidebar hidden
- Mobile (320px-767px): 1 column, stacked layout

**Interaction Patterns:**
- Search with real-time filtering
- Checkbox filtering
- Card hover effects with shadow
- Click card to view details

---

### 2. Skill Detail Page
**File:** `02-skill-detail-page.html`

**Purpose:** Deep-dive page showing complete skill information, documentation, and installation instructions.

**Key Features:**
- Breadcrumb navigation
- Header with icon, title, metadata, status badges
- Tab interface (Overview, Documentation, Versions, Reviews)
- 2-column layout (main content + sidebar)
- Main content sections:
  - Description
  - Key Features (bulleted list)
  - Installation (NPM, Python, Docker)
  - Quick Start (code examples)
  - Use Cases
  - Requirements
  - Support
- Right sidebar with:
  - Info card (version, published, updated, downloads, license)
  - Tags card
  - Author card with profile link
  - Recent Versions card with quick links
- Code blocks with dark theme
- Helper text and descriptions

**Responsive Breakpoints:**
- Desktop: 2-column layout
- Tablet/Mobile: Stacked single column
- Sidebar becomes full-width below content

**Code Blocks:**
- Dark background (#1F2937)
- Light text (#E5E7EB)
- Monospace font (IBM Plex Mono)
- Copy button (future enhancement)

---

### 3. Publish Workflow
**File:** `03-publish-workflow.html`

**Purpose:** 5-step wizard for publishing new skills to the repository.

**Key Features:**
- Visual stepper with 5 steps:
  1. Basic Info (completed)
  2. Details (completed)
  3. Setup (active) - runtime, dependencies, permissions
  4. Files (pending)
  5. Review (pending)
- Step completion states (completed = green, active = blue, pending = gray)
- Step 3 form content:
  - Runtime Environment selector
  - Min/Max version inputs
  - Dependencies textarea (format depends on runtime)
  - Peer Dependencies
  - System Requirements (memory, CPU, storage)
  - Network & Permissions checkboxes
  - Environment Variables textarea
- Form validation indicators
- Previous/Next navigation
- Submit button at form end

**Form Features:**
- Required field indicators (*)
- Helper text for guidance
- Conditional fields based on runtime
- Input validation feedback
- Textarea with multiline examples

**Responsive Behavior:**
- Single column on mobile
- Form row (2-col) becomes single column on small screens
- Full-width buttons on mobile
- Reduced padding on mobile

**Success State:**
- Completion screen with checkmark
- Option to view published skill
- Option to publish another skill

---

### 4. Admin Dashboard
**File:** `04-admin-dashboard.html`

**Purpose:** Administrative dashboard for monitoring skills, users, and system metrics.

**Key Features:**
- Fixed left sidebar (256px) with navigation:
  - Dashboard (active)
  - Skills Management
  - Users
  - Settings
  - Analytics
  - Security
- Main content area with:
  - Page header with title and description
  - Date range quick filters (7d, 30d, 90d, 1y)
  - Export button
  - 4 KPI cards:
    - Total Skills (45 new)
    - Total Downloads (487K, +12%)
    - Active Users (8,234, +5%)
    - Avg Rating (4.7, -0.1)
  - KPI card components show value, metadata, change indicator, icon
  - 2-column chart grid:
    - Downloads Trend (line chart placeholder)
    - Skills by Category (pie chart placeholder)
  - Chart legends with color indicators
  - "Recent Skills" data table with:
    - Name, Category, Version, Status, Downloads, Rating, Actions
    - Status badges (Active, Beta, Pending Review)
    - Edit/View action buttons
    - Pagination (showing 1-5 of 245)

**Responsive Breakpoints:**
- Desktop: 2-column chart grid, 4-column KPI
- Tablet (< 1200px): 1-column charts, 2-column KPI
- Mobile: Sidebar hidden, single column layout, stacked buttons

**Interactive Elements:**
- Date range buttons filter data
- Export button for reporting
- Row hover effects
- Action button states
- Pagination controls

---

### 5. Version Management
**File:** `05-version-management.html`

**Purpose:** Compare versions, view changelog, and manage skill versions.

**Key Features:**
- Breadcrumb navigation
- Page header with download all button
- 2-column layout:
  - Left sidebar: Version list (320px)
  - Right main: Comparison and changelog
- Version sidebar:
  - List of versions with:
    - Version number
    - Release date
    - "Current" badge for active version
    - Clickable selection
  - Active version highlighting
  - Scrollable with max-height
- Comparison section:
  - Dual version headers (current vs previous)
  - Compare-with selector dropdown
  - Stats grid (Lines Changed, Files Modified, Changes)
  - Comparison table:
    - Attribute, Current value, Previous value
    - Visual diff highlighting (changed = amber, added = green, removed = red)
- Changelog section:
  - Detailed entries for each change
  - Badge types: Feature (blue), Bug Fix (green), Breaking (red), Security (red)
  - Entry structure:
    - Badge + Title + Description
    - Optional bulleted details
    - Release date
  - Sorted chronologically (newest first)

**Color Coding:**
- Changed values: #FEF3C7 (amber background)
- Added values: #DCFCE7 (emerald/green background)
- Removed values: #FEE2E2 (red background)
- Feature: #DBEAFE (blue badge)
- Bug Fix: #DCFCE7 (green badge)
- Breaking: #FEE2E2 (red badge)
- Security: #FECACA (dark red badge)

**Responsive Behavior:**
- Tablet: Sidebar becomes single column, full-width main content
- Mobile: Stacked layout, reduced padding, smaller font sizes
- Table responsive: Adjusts padding and font size on mobile

---

## Design System Integration

All wireframes implement the design system from `design-guidelines.md`:

### Colors
- Primary Blue: #3B82F6
- Success Emerald: #10B981
- Warning Amber: #F59E0B
- Error Red: #EF4444
- Text Primary: #1F2937
- Text Secondary: #6B7280
- Background: #F9FAFB
- Border: #E5E7EB

### Typography
- Headers: Inter 600
- Body: IBM Plex Sans 400
- Code: IBM Plex Mono 400
- Vietnamese language support built-in

### Spacing (8px Grid)
- xs: 4px
- sm: 8px
- md: 12px
- lg: 16px
- xl: 24px
- 2xl: 32px
- 3xl: 48px

### Components
- Buttons (primary, secondary, danger)
- Input fields
- Cards with hover effects
- Badges (status indicators)
- Alerts
- Tables with pagination
- Modals/Forms
- Navigation (sidebar, tabs)
- Breadcrumbs

---

## Accessibility Features

All wireframes include:
- WCAG 2.1 AA color contrast compliance
- Semantic HTML structure
- Focus indicators (visible ring outlines)
- Keyboard navigation support
- Skip links for main content
- Screen reader friendly labels
- ARIA attributes where needed
- Clear visual hierarchy
- Status indicators for form states

---

## Implementation Notes

### HTML Structure
- Semantic HTML5 (nav, main, article, section)
- Proper heading hierarchy (h1-h6)
- Form labels associated with inputs
- List semantics for navigation

### CSS Approach
- Mobile-first responsive design
- CSS Grid and Flexbox for layouts
- CSS variables for theming
- Smooth transitions and animations
- Respects `prefers-reduced-motion`
- No CSS frameworks required (pure CSS)

### Google Fonts
All wireframes use:
- **Inter:** Headers (font-weight: 600)
- **IBM Plex Sans:** Body text (font-weight: 400)
- **IBM Plex Mono:** Code blocks (font-weight: 400)
- All fonts have **full Vietnamese character support**

### Responsive Strategy
1. Mobile-first design (320px base)
2. Tablet optimizations (768px breakpoint)
3. Desktop enhancements (1024px+ breakpoint)
4. Wide screen polish (1280px+)

### Touch Targets
- Minimum 44x44px on mobile
- 8px spacing between interactive elements
- Larger buttons on mobile (48px+)

---

## Next Steps

1. **Convert to React Components**
   - Break HTML into reusable React components
   - Add state management for forms
   - Implement client-side routing
   - Add API integration

2. **Add Real Data**
   - Connect to API endpoints
   - Implement search and filtering
   - Add pagination
   - Real-time updates

3. **Enhance Interactions**
   - Add form validation
   - Implement animations
   - Add loading states
   - Error handling

4. **Optimization**
   - Image optimization
   - Code splitting
   - Performance metrics
   - SEO optimization

---

## File Structure

```
wireframes/
├── README.md                          # This file
├── 01-skill-discovery-page.html       # Discovery page
├── 02-skill-detail-page.html          # Detail page
├── 03-publish-workflow.html           # Publish wizard
├── 04-admin-dashboard.html            # Admin dashboard
└── 05-version-management.html         # Version comparison
```

---

## Testing Checklist

- [ ] All pages render correctly on Chrome, Firefox, Safari, Edge
- [ ] Responsive behavior tested at 320px, 768px, 1024px, 1280px
- [ ] Keyboard navigation works (Tab, Enter, Escape)
- [ ] Focus indicators visible on all interactive elements
- [ ] Color contrast passes WCAG 2.1 AA
- [ ] Touch targets minimum 44x44px on mobile
- [ ] Forms are fully accessible
- [ ] No horizontal scrolling on mobile
- [ ] Text scales properly (mobile text at least 16px)
- [ ] Images are optimized
- [ ] Loading states indicated
- [ ] Error messages clear and helpful
- [ ] Vietnamese text renders correctly

---

## Browser Support

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+
- Mobile browsers (iOS Safari, Chrome Mobile)

---

## Notes

- All wireframes are HTML-only (no JavaScript required for basic functionality)
- Design annotations included in corner of each page
- Placeholder content uses realistic data
- Date formats use ISO 8601 (YYYY-MM-DD)
- All currency uses USD (can be localized)
- Code examples include Node.js, Python, and Docker

---

*Last updated: 2026-02-21*
