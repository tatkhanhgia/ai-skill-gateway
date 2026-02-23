# Component Architecture Diagram

## System Layout: Skill Gateway Frontend

```
┌─────────────────────────────────────────────────────────────────────┐
│                        AI SKILL GATEWAY                             │
│                        Admin Dashboard                              │
└─────────────────────────────────────────────────────────────────────┘

                            ┌─ HEADER ─┐
                            │ Logo     │
                            │ Profile  │
                            └──────────┘

┌───────────────────────────────────────────────────────────────────────┐
│                          MAIN CONTENT AREA                            │
├─────────────────────────────────────────┬─────────────────────────────┤
│                                         │                             │
│  SIDEBAR (LEFT)                         │  MAIN PANEL (RIGHT)         │
│  ┌─────────────────────────────────┐    │  ┌─────────────────────┐   │
│  │ Navigation Menu                 │    │  │ Hero / Action Bar   │   │
│  │ ├─ Home                         │    │  │ [+ New Skill]       │   │
│  │ ├─ Explore Skills               │    │  └─────────────────────┘   │
│  │ ├─ Published                    │    │                             │
│  │ └─ Settings                     │    │  ┌─────────────────────┐   │
│  └─────────────────────────────────┘    │  │ SEARCH BAR          │   │
│                                         │  │ [🔍 Search...] [⚙️] │   │
│                                         │  └─────────────────────┘   │
│                                         │                             │
│                                         │  ┌─ FILTERS (LEFT) ─┐      │
│                                         │  │ [Framework ▼]    │      │
│                                         │  │ [Language ▼]     │      │
│                                         │  │ [License ▼]      │      │
│                                         │  └──────────────────┘      │
│                                         │                             │
│                                         │  ┌─ RESULTS (GRID) ──┐     │
│                                         │  │ ┌──────┐┌──────┐  │     │
│                                         │  │ │ Card1││ Card2│  │     │
│                                         │  │ └──────┘└──────┘  │     │
│                                         │  │ ┌──────┐┌──────┐  │     │
│                                         │  │ │ Card3││ Card4│  │     │
│                                         │  │ └──────┘└──────┘  │     │
│                                         │  └────────────────────┘     │
│                                         │                             │
│                                         │  [← Prev] Page 1 [Next →]  │
│                                         │  [20 per page ▼]            │
└─────────────────────────────────────────┴─────────────────────────────┘
```

---

## Skill Discovery Page: Component Tree

```
SearchAndFilterLayout
├─ Header
│  ├─ SearchBar
│  │  ├─ SearchInput
│  │  ├─ AutocompleteDropdown
│  │  │  ├─ SuggestionItem (x5-8)
│  │  │  ├─ "Did you mean?"
│  │  │  └─ "Popular searches"
│  │  └─ ClearButton
│  └─ HelpIcon
│
├─ TwoColumnLayout
│  │
│  ├─ LeftSidebar
│  │  ├─ FilterGroup (framework)
│  │  │  ├─ SearchInput
│  │  │  ├─ CheckboxList
│  │  │  │  ├─ Checkbox + Label + Count (python)
│  │  │  │  ├─ Checkbox + Label + Count (js)
│  │  │  │  └─ Checkbox + Label + Count (go)
│  │  │  └─ "Show All" Button
│  │  │
│  │  ├─ FilterGroup (language)
│  │  │  ├─ CheckboxList
│  │  │  └─ "Show All" Button
│  │  │
│  │  ├─ FilterGroup (rating)
│  │  │  ├─ RangeSlider (0-5 stars)
│  │  │  └─ StaticText (current range)
│  │  │
│  │  └─ AppliedFiltersBar
│  │     ├─ FilterChip (remove icon) (x N)
│  │     └─ "Clear All" Button
│  │
│  └─ MainContent
│     │
│     ├─ ResultsHeader
│     │  ├─ ResultCount ("1,234 results")
│     │  ├─ SortDropdown (relevance/downloads/rating)
│     │  └─ LayoutToggle (grid/list view)
│     │
│     ├─ SkillGrid (3 columns responsive)
│     │  └─ SkillCard (compact) (x 20)
│     │     ├─ SkillIcon
│     │     ├─ SkillHeader (name + version)
│     │     ├─ Metadata (publisher, updated date)
│     │     ├─ Description (2-line truncate)
│     │     ├─ StatsRow
│     │     │  ├─ Rating (⭐ 4.8 · 1.2K)
│     │     │  ├─ Downloads (↓ 25.3K)
│     │     │  └─ Framework badge
│     │     ├─ TagsList (max 3 visible)
│     │     └─ ActionButtons
│     │        ├─ PreviewButton
│     │        └─ InstallButton
│     │
│     └─ PaginationBar
│        ├─ PrevButton
│        ├─ PageIndicator (Page 1 of 42)
│        ├─ NextButton
│        └─ ItemsPerPageSelect (20/50/100)
```

---

## Publish Skill Workflow: Form Flow

```
PublishWizard (Multi-Step Form)
│
├─ Step 1: Basic Information
│  ├─ Input: Skill Name
│  │  └─ Validation: 3-50 chars, alphanumeric + hyphen
│  ├─ Input: Description
│  │  └─ Validation: 10-500 chars
│  ├─ Input: Icon Upload
│  │  ├─ FileDropZone (drag & drop)
│  │  └─ Preview + Crop Tool
│  └─ Button: Next →
│
├─ Step 2: Metadata
│  ├─ MultiSelect: Framework
│  │  └─ Options: Python, JavaScript, Go, etc.
│  ├─ MultiSelect: Language
│  │  └─ Options: Python, JavaScript, TypeScript, etc.
│  ├─ Select: License
│  │  └─ Options: MIT, Apache 2.0, GPL v3, etc.
│  ├─ MultiSelect: Tags
│  │  └─ Autocomplete + Create New
│  └─ Buttons: ← Back | Next →
│
├─ Step 3: Version & Changelog
│  ├─ Input: Version (SemVer format)
│  │  ├─ Validation: X.Y.Z format
│  │  └─ AutoSuggestButton (analyze commits)
│  │     └─ Suggestion Tooltip: "Patch bump: v1.2.4"
│  ├─ Textarea: Changelog
│  │  └─ Markdown preview
│  └─ Buttons: ← Back | Next →
│
├─ Step 4: Preview & Review
│  ├─ LeftPanel: Form Summary
│  │  └─ Readonly display of all fields
│  ├─ RightPanel: Manifest Preview
│  │  ├─ JSON/YAML Toggle
│  │  ├─ CopyButton
│  │  └─ CodeBlock (syntax highlighted)
│  │     └─ Content: Generated manifest
│  └─ Buttons: ← Back | [Publish]
│
├─ Step 5: Success Screen
│  ├─ SuccessIcon (checkmark)
│  ├─ Message: "Skill published successfully!"
│  ├─ ShareableLink
│  │  └─ CopyButton
│  ├─ ActionButtons
│  │  ├─ ViewSkillButton
│  │  ├─ ShareButton (Twitter, LinkedIn)
│  │  └─ PublishAnotherButton
│  └─ DraftAutoSave (bottom): "Last saved 2 mins ago"
```

---

## Admin Dashboard: Widget Layout

```
AdminDashboard
│
├─ Header
│  ├─ Title: "Dashboard"
│  ├─ DateRange Picker (last 30 days)
│  └─ RefreshButton
│
├─ MetricsRow (4 columns)
│  ├─ KPICard: "Active Skills"
│  │  ├─ Title
│  │  ├─ LargeNumber: 1,247
│  │  ├─ Trend: "↑ 12% from last month" (green)
│  │  └─ MiniLineChart (30-day history)
│  │
│  ├─ KPICard: "Downloads (MTD)"
│  │  ├─ Title
│  │  ├─ LargeNumber: 125.3K
│  │  ├─ Trend: "↑ 8% vs last month" (green)
│  │  └─ MiniLineChart
│  │
│  ├─ KPICard: "Avg Rating"
│  │  ├─ Title
│  │  ├─ LargeNumber: 4.2★
│  │  ├─ Trend: "↑ 0.1 from last week" (green)
│  │  └─ MiniBarChart
│  │
│  └─ StatusCard: "API Health"
│     ├─ StatusBadge (green dot + "Operational")
│     ├─ Metric: "99.98% uptime"
│     ├─ Metric: "p99 latency: 245ms"
│     └─ IncidentAlert (if any)
│
├─ ChartsSection (2x2 grid)
│  ├─ LineChart: "Downloads Over Time"
│  │  ├─ XAxis: Days
│  │  ├─ YAxis: Download Count
│  │  └─ Legend + Tooltip
│  │
│  ├─ DonutChart: "Language Distribution"
│  │  ├─ Slices: Python, JS, Go, etc.
│  │  ├─ Legend (with color boxes)
│  │  └─ CenterText: "Total: 42 skills"
│  │
│  ├─ BarChart: "Error Rates by Endpoint"
│  │  ├─ Bars (red for errors, blue for success)
│  │  └─ Tooltip: error details
│  │
│  └─ DonutChart: "Framework Distribution"
│     └─ Similar to language dist.
│
├─ DataTables Section
│  │
│  ├─ Table: "Recent Skills Published"
│  │  ├─ Columns:
│  │  │  ├─ Skill Name (sortable)
│  │  │  ├─ Version (sortable)
│  │  │  ├─ Publisher
│  │  │  ├─ Published Date (sortable)
│  │  │  └─ Downloads (sortable)
│  │  ├─ Rows: 5 most recent
│  │  ├─ RowActions: [View] [Edit] [Manage]
│  │  └─ "View All" Link
│  │
│  └─ Table: "Top Performing Skills"
│     ├─ Columns: Skill, Rating, Downloads, Trend
│     ├─ Rows: Top 5 by downloads
│     └─ "View All" Link
│
└─ Footer
   └─ LastUpdated: "Last refreshed 2 mins ago"
```

---

## Skill Card: Detailed View

```
SkillDetailPage
│
├─ Header Section
│  ├─ SkillIcon (large, 80x80)
│  ├─ SkillMeta
│  │  ├─ Title: "Skill Name"
│  │  ├─ Version Badge: "v1.2.3"
│  │  ├─ PublisherLink: "By @username"
│  │  ├─ PublishedDate: "Published 2 months ago"
│  │  ├─ UpdatedDate: "Last updated 5 days ago"
│  │  │
│  │  ├─ StatsRow
│  │  │  ├─ Rating: "⭐ 4.8 (1.2K reviews)"
│  │  │  ├─ Downloads: "↓ 25.3K total"
│  │  │  ├─ Framework: "Python, JS"
│  │  │  └─ License: "MIT"
│  │  │
│  │  └─ ActionButtons
│  │     ├─ PrimaryButton: "Install"
│  │     ├─ SecondaryButton: "Share"
│  │     ├─ IconButton: "Star" (heart icon)
│  │     └─ IconButton: "More Options" (...)
│  │
│  └─ TagsList
│     └─ Tag (x5-10): #automation, #python, #ml, etc.
│
├─ TwoColumnLayout
│  │
│  ├─ MainColumn
│  │  ├─ Section: Description
│  │  │  └─ Markdown (with code syntax highlight)
│  │  │
│  │  ├─ Section: Installation
│  │  │  ├─ Tabs: npm | pip | yarn | direct
│  │  │  ├─ CodeBlock (copy button)
│  │  │  └─ Content: Installation command
│  │  │
│  │  ├─ Section: Changelog
│  │  │  ├─ VersionTimeline
│  │  │  │  ├─ TimelineItem: v1.2.3 (5 days ago)
│  │  │  │  │  └─ ChangeList: fixed bug X, added feature Y
│  │  │  │  ├─ TimelineItem: v1.2.2 (2 weeks ago)
│  │  │  │  └─ TimelineItem: v1.2.1 (1 month ago)
│  │  │  └─ "View Full History" Link
│  │  │
│  │  ├─ Section: Reviews
│  │  │  ├─ ReviewItem (x3) [Latest]
│  │  │  │  ├─ AuthorName
│  │  │  │  ├─ Rating (⭐⭐⭐⭐⭐)
│  │  │  │  ├─ ReviewText
│  │  │  │  └─ "Helpful?" [Yes] [No]
│  │  │  └─ "View All Reviews" Link
│  │  │
│  │  └─ Section: Related Skills
│  │     └─ SkillCardCompact (x3) [Horizontal scroll]
│  │
│  └─ SidebarColumn
│     ├─ Card: Metadata
│     │  ├─ Framework: Python
│     │  ├─ Language: Python, English
│     │  ├─ License: MIT
│     │  ├─ Repository: [github link]
│     │  ├─ Maintainer: @username
│     │  └─ Verified Badge (if applicable)
│     │
│     ├─ Card: Dependencies
│     │  ├─ Dependency (x5)
│     │  │  └─ Name + Version requirement
│     │  └─ "View All" Link
│     │
│     └─ Card: Community Stats
│        ├─ Stars: 1,234
│        ├─ Forks: 245
│        ├─ Issues: 12
│        └─ Contributors: 8
```

---

## Design System Tokens

```
SPACING SCALE
├─ xs: 4px (1 unit)
├─ sm: 8px (2 units)
├─ md: 12px (3 units)
├─ lg: 16px (4 units)
├─ xl: 24px (6 units)
├─ 2xl: 32px (8 units)
└─ 3xl: 48px (12 units)

COLOR SEMANTIC
├─ success: #10B981 (Emerald)
├─ warning: #F59E0B (Amber)
├─ error: #EF4444 (Red)
├─ info: #3B82F6 (Blue)
├─ primary: #3B82F6 (Blue, primary action)
└─ secondary: #8B5CF6 (Purple, secondary action)

TYPOGRAPHY SCALE
├─ h1: 32px, 600 weight (Inter), line-height 1.2
├─ h2: 24px, 600 weight (Inter), line-height 1.3
├─ h3: 20px, 600 weight (Inter), line-height 1.4
├─ body: 16px, 400 weight (IBM Plex Sans), line-height 1.6
├─ small: 14px, 400 weight (IBM Plex Sans), line-height 1.5
├─ micro: 12px, 400 weight (IBM Plex Sans), line-height 1.4
└─ code: 13px, monospace (IBM Plex Mono), line-height 1.5

ELEVATION (SHADOW)
├─ elevation-1: 0 1px 3px rgba(0,0,0,0.1)
├─ elevation-2: 0 4px 6px rgba(0,0,0,0.1)
└─ elevation-3: 0 10px 15px rgba(0,0,0,0.1)

BORDER RADIUS
├─ xs: 4px (inputs, small elements)
├─ sm: 6px (buttons, badges)
├─ md: 8px (cards, modals)
└─ lg: 12px (large cards, containers)

INTERACTION
├─ debounce: 300ms (search input)
├─ transition: 200ms ease-in-out (smooth)
└─ focus: 2px blue outline
```

---

**Note**: All measurements use 8px base unit for consistency across responsive breakpoints.
