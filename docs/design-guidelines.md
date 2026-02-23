# Design Guidelines - AI Skill Repository Dashboard

**Version:** 1.0
**Last Updated:** 2026-02-21
**Status:** Active Design System

---

## Table of Contents

1. [Design Principles](#design-principles)
2. [Color System](#color-system)
3. [Typography](#typography)
4. [Spacing & Layout](#spacing--layout)
5. [Components](#components)
6. [Patterns](#patterns)
7. [Accessibility](#accessibility)
8. [Responsive Design](#responsive-design)

---

## Design Principles

### 1. **Clarity First**
- Information hierarchy is clear and scannable
- Navigation is intuitive and predictable
- Actions have obvious affordances

### 2. **Consistency**
- Uniform component usage across all pages
- Consistent spacing, colors, and typography
- Predictable interaction patterns

### 3. **Efficiency**
- Minimize cognitive load
- Reduce steps to accomplish tasks
- Smart defaults and pre-filled data

### 4. **Trustworthiness**
- Clear error messages and feedback
- Transparent data handling
- Professional visual presentation

### 5. **Accessibility-First**
- WCAG 2.1 AA minimum compliance
- Keyboard navigation support
- Screen reader friendly
- High contrast ratios

---

## Color System

### Primary Palette

| Token | Hex | RGB | Usage |
|-------|-----|-----|-------|
| **Primary Blue** | #3B82F6 | 59, 130, 246 | Primary actions, links, highlights |
| **Success Emerald** | #10B981 | 16, 185, 129 | Success states, positive actions |
| **Warning Amber** | #F59E0B | 245, 158, 11 | Warnings, alerts, caution |
| **Error Red** | #EF4444 | 239, 68, 68 | Errors, destructive actions |

### Neutral Palette

| Token | Hex | Usage |
|-------|-----|-------|
| **Text Primary** | #1F2937 | Main content, headers |
| **Text Secondary** | #6B7280 | Descriptions, secondary content |
| **Text Tertiary** | #9CA3AF | Disabled text, hints |
| **Background** | #FFFFFF | Main page background |
| **Surface** | #F9FAFB | Cards, panels, elevated elements |
| **Border** | #E5E7EB | Dividers, input borders |

### Semantic Colors

```
Success:     #10B981 (bg-emerald-500)
Warning:     #F59E0B (bg-amber-500)
Error:       #EF4444 (bg-red-500)
Info:        #3B82F6 (bg-blue-500)
Disabled:    #D1D5DB (text-gray-300)
```

### Opacity Levels

- **Hover**: Primary color at 90% opacity
- **Active**: Primary color at 100% opacity
- **Disabled**: 50% opacity
- **Ghost**: 20% opacity (ghost buttons)

---

## Typography

### Font Stack

```
Headers:     'Inter', -apple-system, BlinkMacSystemFont, sans-serif (Weight: 600)
Body:        'IBM Plex Sans', -apple-system, BlinkMacSystemFont, sans-serif (Weight: 400)
Code:        'IBM Plex Mono', 'Courier New', monospace (Weight: 400)
Vietnamese:  All fonts support Vietnamese diacritical marks (ă, â, đ, ê, ô, ơ, ư)
```

### Type Scale

| Level | Font Size | Line Height | Letter Spacing | Weight | Usage |
|-------|-----------|------------|-----------------|--------|-------|
| **H1** | 32px | 40px | -0.5px | 600 | Page titles |
| **H2** | 24px | 32px | -0.25px | 600 | Section headers |
| **H3** | 20px | 28px | 0px | 600 | Subsection headers |
| **H4** | 16px | 24px | 0px | 600 | Card titles |
| **Body Large** | 16px | 24px | 0px | 400 | Main body text |
| **Body** | 14px | 22px | 0px | 400 | Standard text |
| **Body Small** | 12px | 18px | 0px | 400 | Secondary text, labels |
| **Caption** | 11px | 16px | 0px | 400 | Hints, metadata |
| **Code** | 13px | 20px | 0px | 400 | Code blocks, monospace |

### Typography Hierarchy

**Headers (Inter 600)**
- Page titles: 32px / 40px line height
- Section headers: 24px / 32px line height
- Subsection headers: 20px / 28px line height
- Card titles: 16px / 24px line height

**Body Text (IBM Plex Sans 400)**
- Primary content: 16px / 24px line height
- Standard text: 14px / 22px line height
- Secondary text: 12px / 18px line height
- Captions: 11px / 16px line height

**Code (IBM Plex Mono 400)**
- Code blocks: 13px / 20px line height
- Inline code: 12px / 20px line height
- Command snippets: 13px / 20px line height

### Text Styles

```css
/* Headings */
.h1 { font-size: 32px; font-weight: 600; line-height: 40px; }
.h2 { font-size: 24px; font-weight: 600; line-height: 32px; }
.h3 { font-size: 20px; font-weight: 600; line-height: 28px; }
.h4 { font-size: 16px; font-weight: 600; line-height: 24px; }

/* Body */
.body-lg { font-size: 16px; font-weight: 400; line-height: 24px; }
.body { font-size: 14px; font-weight: 400; line-height: 22px; }
.body-sm { font-size: 12px; font-weight: 400; line-height: 18px; }

/* Code */
.code { font-family: 'IBM Plex Mono'; font-size: 13px; line-height: 20px; }
```

---

## Spacing & Layout

### Base Unit: 8px Grid

All spacing, margins, and padding follow an 8px grid system.

### Spacing Scale

| Token | Value | Usage |
|-------|-------|-------|
| **xs** | 4px | Tight spacing (badges, small gaps) |
| **sm** | 8px | Component internal padding |
| **md** | 12px | Related elements spacing |
| **lg** | 16px | Section spacing, standard margins |
| **xl** | 24px | Major section separation |
| **2xl** | 32px | Page sections |
| **3xl** | 48px | Page-level spacing |

### Layout Grid

**Desktop (1024px+)**
- 12-column grid
- 24px gutters
- Max-width: 1280px
- Padding: 32px sides

**Tablet (768px - 1023px)**
- 8-column grid
- 16px gutters
- Padding: 24px sides

**Mobile (320px - 767px)**
- 4-column grid
- 12px gutters
- Padding: 16px sides

### Container Sizes

```
Desktop:  max-width: 1280px
Tablet:   max-width: 768px
Mobile:   full-width with 16px padding
```

---

## Components

### Button States

**Primary Button**
```
Default:   bg-blue-500, text-white
Hover:     bg-blue-600 (shade darker)
Active:    bg-blue-700
Disabled:  bg-gray-300, text-gray-500, cursor-not-allowed
Focus:     ring-2 ring-blue-400 ring-offset-2
```

**Secondary Button**
```
Default:   bg-gray-100, text-gray-900, border-gray-200
Hover:     bg-gray-200
Active:    bg-gray-300
Disabled:  bg-gray-100, text-gray-400, cursor-not-allowed
Focus:     ring-2 ring-blue-400 ring-offset-2
```

**Danger Button**
```
Default:   bg-red-500, text-white
Hover:     bg-red-600
Active:    bg-red-700
Disabled:  bg-gray-300, text-gray-500, cursor-not-allowed
Focus:     ring-2 ring-red-400 ring-offset-2
```

**Button Sizes**
- Small: 32px height, 12px horizontal padding, 12px text
- Medium: 40px height, 16px horizontal padding, 14px text
- Large: 48px height, 20px horizontal padding, 16px text

### Input Fields

**Base Style**
```
Background:     white
Border:         1px solid #E5E7EB
Border-radius:  6px
Padding:        12px 16px
Font-size:      14px
Line-height:    22px
```

**States**
- Default: border-gray-200
- Hover: border-gray-300, bg-gray-50
- Focus: border-blue-500, ring-2 ring-blue-200
- Error: border-red-500, ring-2 ring-red-200
- Disabled: bg-gray-100, text-gray-400, cursor-not-allowed
- Loading: border-blue-500, animate-pulse

### Cards

**Base Style**
```
Background:     white
Border:         1px solid #E5E7EB
Border-radius:  8px
Padding:        20px
Box-shadow:     0 1px 3px rgba(0,0,0,0.1)
Hover:          shadow-md, border-gray-300
```

**Variants**
- Default: white bg, gray border
- Elevated: white bg, stronger shadow
- Filled: gray-50 bg, lighter border
- Outlined: transparent bg, colored border

### Badges

**Sizes**
- Small: 6px y-padding, 10px x-padding, 11px text
- Medium: 8px y-padding, 12px x-padding, 12px text

**Variants**
- Primary: blue bg, white text
- Success: emerald bg, white text
- Warning: amber bg, white text
- Error: red bg, white text
- Secondary: gray bg, gray-900 text

### Alerts

**Styles**
```
Background:     Variant color at 10% opacity
Border-left:    4px solid variant color
Padding:        16px
Border-radius:  6px
Icon:           variant color at 100%
```

**Types**
- Success: emerald
- Warning: amber
- Error: red
- Info: blue

### Modals

**Structure**
```
Header:         24px title, 8px subtitle
Content:        16px padding, max-height: 60vh
Footer:         16px padding, button alignment (right)
Overlay:        rgba(0,0,0,0.5)
```

**Sizes**
- Small: 400px width
- Medium: 600px width (default)
- Large: 800px width

### Navigation

**Sidebar Navigation**
```
Width:          256px (collapsed: 64px)
Item height:    44px
Item padding:   12px 16px
Active state:   bg-blue-50, text-blue-600, left-border blue
```

**Top Navigation**
```
Height:         64px
Padding:        0 32px
Item padding:   8px 16px
Alignment:      left-aligned logo, right-aligned actions
```

---

## Patterns

### Form Patterns

**Single Column Form**
- Max-width: 600px
- Field spacing: 20px vertical
- Helper text: 12px, gray-600
- Error text: 12px, red-600

**Multi-Column Form**
- 2 columns on desktop, 1 on mobile
- Column gap: 24px
- Field spacing: 20px vertical

**Input Groups**
```
Label:          block, 14px font-600, 8px margin-bottom
Input:          full-width, 40px height
Helper:         12px, gray-600, 8px margin-top
Error message:  12px, red-600, 8px margin-top
```

### Card Grid Patterns

**Skill Discovery Grid**
- Desktop: 3 columns (320px cards)
- Tablet: 2 columns (280px cards)
- Mobile: 1 column (full-width)
- Card spacing: 20px

**Admin Dashboard Grid**
- KPI cards: 4 columns on desktop, 2 on tablet, 1 on mobile
- Chart section: full-width below KPIs
- Card height: 120px for KPI

### Search and Filter Pattern

**Search Bar**
- Height: 44px
- Icon: 20px, left-aligned
- Debounce: 300ms
- Clear button: on input with text

**Filter Sidebar**
- Width: 240px
- Collapsible on tablet/mobile
- Category sections: expandable/collapsible
- Multi-select checkboxes

### Status Indicators

**Skill Status Badge**
- Active: emerald badge, "Active"
- Deprecated: amber badge, "Deprecated"
- Beta: blue badge, "Beta"
- Draft: gray badge, "Draft"

**Installation Status**
- Installed: emerald checkmark
- Available: blue download icon
- Updating: blue spinner
- Error: red alert icon

---

## Accessibility

### WCAG 2.1 AA Compliance

**Color Contrast Ratios**
- Normal text: minimum 4.5:1
- Large text (18px+): minimum 3:1
- UI components: minimum 3:1 for focus indicators

**Required Contrasts**
```
Text on primary:    White text on #3B82F6 (contrast: 3.6:1) - adjust to white for 4.5:1
Text on success:    White text on #10B981 (contrast: 5.2:1) ✓
Text on warning:    Dark text on #F59E0B (contrast: 5.8:1) ✓
Text on error:      White text on #EF4444 (contrast: 6.2:1) ✓
```

### Keyboard Navigation

- Tab order follows visual flow (top-to-bottom, left-to-right)
- Focus indicators visible (ring-2 ring-blue-400)
- Skip links for main content
- All interactive elements keyboard-accessible
- Escape key closes modals

### Screen Reader Support

- Semantic HTML (button, input, nav, main, etc.)
- ARIA labels for icon buttons
- ARIA-live regions for dynamic content
- Form labels properly associated
- Error messages linked to inputs

### Focus Management

- Initial focus on primary action in modals
- Trap focus within modals (Tab loops)
- Return focus after modal close
- Visible focus indicators (2px ring)

### Reduced Motion

```css
@media (prefers-reduced-motion: reduce) {
  * {
    animation: none !important;
    transition: none !important;
  }
}
```

---

## Responsive Design

### Breakpoints

```
Mobile:   320px - 767px
Tablet:   768px - 1023px
Desktop:  1024px+
Wide:     1280px+
```

### Mobile-First Approach

1. Design for 320px mobile first
2. Enhance for 768px tablet
3. Optimize for 1024px desktop
4. Polish for 1280px+ wide screens

### Responsive Patterns

**Navigation**
- Mobile: hamburger menu, bottom sheet
- Tablet: collapsed sidebar
- Desktop: full sidebar

**Grid Layout**
- Mobile: 1 column
- Tablet: 2-3 columns
- Desktop: 3-4 columns

**Input Fields**
- Mobile: full-width, 40px height, larger text (16px)
- Tablet: 50% width in 2-column form
- Desktop: flexible width, max 600px forms

**Typography**
- Mobile: reduced size (H1: 24px, Body: 14px)
- Tablet: medium size (H1: 28px, Body: 14px)
- Desktop: full size (H1: 32px, Body: 14px)

### Touch Targets

- Minimum 44x44px for interactive elements
- 8px spacing between targets
- Larger buttons on mobile (48px+)
- Forms with increased padding on mobile

---

## Implementation Notes

### Tailwind CSS Usage

```tailwind
/* Colors */
bg-blue-500 text-white
bg-emerald-500 text-white
bg-amber-500 text-gray-900
bg-red-500 text-white

/* Spacing */
p-4 (16px), p-6 (24px), p-8 (32px)
m-4 (16px), m-6 (24px), m-8 (32px)
gap-4 (16px), gap-6 (24px)

/* Typography */
font-inter font-semibold text-2xl
font-plex text-base
font-mono text-sm

/* Responsive */
md:grid-cols-2 lg:grid-cols-3
md:p-8 lg:p-10
```

### shadcn/ui Components

**Used Components:**
- Button
- Input
- Card
- Badge
- Alert
- Modal/Dialog
- Select/Dropdown
- Tabs
- Checkbox
- RadioGroup
- Textarea
- Skeleton

---

## Design Tokens (CSS Variables)

```css
:root {
  /* Colors */
  --color-primary: #3B82F6;
  --color-success: #10B981;
  --color-warning: #F59E0B;
  --color-error: #EF4444;
  --color-text: #1F2937;
  --color-text-secondary: #6B7280;
  --color-border: #E5E7EB;

  /* Spacing */
  --spacing-xs: 4px;
  --spacing-sm: 8px;
  --spacing-md: 12px;
  --spacing-lg: 16px;
  --spacing-xl: 24px;
  --spacing-2xl: 32px;
  --spacing-3xl: 48px;

  /* Border Radius */
  --radius-sm: 4px;
  --radius-md: 6px;
  --radius-lg: 8px;

  /* Shadows */
  --shadow-sm: 0 1px 2px rgba(0,0,0,0.05);
  --shadow-md: 0 4px 6px rgba(0,0,0,0.1);
  --shadow-lg: 0 10px 15px rgba(0,0,0,0.1);
}
```

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-02-21 | Initial design system for AI Skill Repository Dashboard |

---

*Last updated: 2026-02-21*
