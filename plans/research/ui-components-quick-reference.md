# UI Components Quick Reference Guide

## Search Bar Pattern
```jsx
<div className="sticky top-0 bg-white shadow-sm">
  <input
    type="search"
    placeholder="Search 5,000+ skills..."
    value={query}
    onChange={(e) => setQuery(e.target.value)}
    className="w-full px-4 py-3 text-16 border-b border-gray-200"
  />
  {/* Autocomplete dropdown with 5-8 suggestions + "Did you mean" */}
</div>
```

## Sidebar Filters
```jsx
<aside className="w-64 bg-gray-50 border-r">
  <h2 className="text-14 font-600 text-gray-900 px-4 py-3">Filters</h2>

  {/* Expandable Filter Section */}
  <FilterGroup title="Language" defaultOpen>
    <Checkbox label="Python (4.2K)" count={4200} />
    <Checkbox label="JavaScript (3.1K)" count={3100} />
    <SearchInput placeholder="Search languages..." />
    <Button variant="ghost">Show All (12)</Button>
  </FilterGroup>

  {/* Range Slider */}
  <FilterGroup title="Rating">
    <RangeSlider min={0} max={5} step={0.5} />
    <span className="text-12 text-gray-500">4.0 - 5.0 stars</span>
  </FilterGroup>

  {/* Applied Filters with Clear */}
  <div className="px-4 py-3 border-t">
    <span className="text-12 font-600">Applied Filters</span>
    <div className="flex flex-wrap gap-2 mt-2">
      <Chip label="Python" onRemove={() => removeFilter('language', 'python')} />
      <Chip label="Rating 4+" onRemove={() => removeFilter('rating')} />
    </div>
  </div>
</aside>
```

## Skill Card (Compact)
```jsx
<article className="border border-gray-200 rounded-8 p-4 hover:shadow-elevation-1">
  <div className="flex gap-3">
    <img src={skillIcon} alt="" className="w-12 h-12 rounded-6" />
    <div className="flex-1">
      <h3 className="text-16 font-600 text-gray-900">
        {skillName} <span className="text-12 text-gray-500">v{version}</span>
      </h3>
      <p className="text-12 text-gray-600">By {publisher} · {lastUpdated}</p>
      <p className="text-14 text-gray-700 line-clamp-2 mt-2">{description}</p>

      {/* Stats Row */}
      <div className="flex gap-4 mt-3 text-12 text-gray-600">
        <span>⭐ {rating} ({reviewCount})</span>
        <span>↓ {downloadCount}</span>
        <span>{framework}</span>
      </div>

      {/* Tags */}
      <div className="flex gap-2 mt-3 flex-wrap">
        {tags.map(tag => <Badge key={tag}>{tag}</Badge>)}
      </div>

      {/* Actions */}
      <div className="flex gap-2 mt-4">
        <Button variant="secondary" size="sm">Preview</Button>
        <Button variant="primary" size="sm">Install</Button>
      </div>
    </div>
  </div>
</article>
```

## Publish Form with Validation
```jsx
<form className="max-w-2xl">
  <div className="grid grid-cols-2 gap-4">
    {/* Field with Validation */}
    <div className="col-span-1">
      <label className="text-14 font-600 text-gray-900">Skill Name</label>
      <input
        type="text"
        value={formData.name}
        onChange={handleChange}
        className={`w-full px-3 py-2 border rounded-6 text-14 ${
          errors.name ? 'border-red-500 bg-red-50' : 'border-gray-200'
        }`}
      />
      {errors.name && <p className="text-12 text-red-600 mt-1">{errors.name}</p>}
      <p className="text-12 text-gray-500 mt-1">3-50 chars, alphanumeric + hyphen</p>
    </div>

    {/* Version with Auto-Suggest */}
    <div className="col-span-1">
      <label className="text-14 font-600 text-gray-900">Version</label>
      <div className="flex gap-2">
        <input
          type="text"
          value={formData.version}
          className="flex-1 px-3 py-2 border border-gray-200 rounded-6 text-14 font-mono"
        />
        <Tooltip title="Suggested: v1.2.4 (patch bump)">
          <Button variant="secondary" size="sm">Auto</Button>
        </Tooltip>
      </div>
    </div>
  </div>

  {/* Manifest Preview */}
  <div className="grid grid-cols-2 gap-4 mt-6">
    <div className="col-span-1">
      <h3 className="text-14 font-600 text-gray-900 mb-2">Form</h3>
      {/* Form fields */}
    </div>
    <div className="col-span-1 bg-gray-50 rounded-8 p-4">
      <h3 className="text-14 font-600 text-gray-900 mb-2">Manifest Preview</h3>
      <pre className="text-12 font-mono text-gray-700 overflow-auto">
        {JSON.stringify(generateManifest(formData), null, 2)}
      </pre>
    </div>
  </div>

  {/* Submit */}
  <Button variant="primary" disabled={!isFormValid} className="mt-6">
    Publish v{formData.version}
  </Button>
</form>
```

## Dashboard Metrics Row
```jsx
<div className="grid grid-cols-4 gap-4">
  {/* KPI Card */}
  <article className="bg-white border border-gray-200 rounded-8 p-4">
    <p className="text-12 font-600 text-gray-600 uppercase tracking-wide">Active Skills</p>
    <h2 className="text-32 font-600 text-gray-900 mt-2">1,247</h2>
    <p className="text-12 text-emerald-600 mt-1">↑ 12% from last month</p>
    <div className="mt-4 h-16">
      <LineChart data={historicalData} />
    </div>
  </article>

  {/* Status Badge */}
  <article className="bg-white border border-gray-200 rounded-8 p-4 flex flex-col justify-between">
    <div>
      <p className="text-12 font-600 text-gray-600 uppercase">API Health</p>
      <div className="flex items-center gap-2 mt-3">
        <div className="w-2 h-2 rounded-full bg-emerald-500"></div>
        <span className="text-14 font-600 text-gray-900">Operational</span>
      </div>
      <p className="text-12 text-gray-500 mt-1">99.98% uptime</p>
    </div>
  </article>
</div>
```

## Search Results Grid
```jsx
<div className="grid grid-cols-3 gap-4">
  {results.map(skill => <SkillCard key={skill.id} skill={skill} />)}
</div>

{/* Pagination */}
<div className="flex justify-center items-center gap-2 mt-8">
  <Button variant="secondary" disabled={page === 1}>← Previous</Button>
  <span className="text-14 text-gray-700">Page {page} of {totalPages}</span>
  <Button variant="secondary" disabled={page === totalPages}>Next →</Button>
  <select className="ml-4 px-3 py-2 border border-gray-200 rounded-6 text-14">
    <option>20 per page</option>
    <option>50 per page</option>
    <option>100 per page</option>
  </select>
</div>
```

## Color Token Utilities
```css
/* Semantic Colors */
.text-success { color: #10B981; }
.text-warning { color: #F59E0B; }
.text-error { color: #EF4444; }
.text-info { color: #3B82F6; }

.bg-success { background-color: #ECFDF5; } /* light variant */
.bg-warning { background-color: #FFFBEB; }
.bg-error { background-color: #FEF2F2; }

/* Neutral Scale */
.text-gray-50, .text-gray-100, ... .text-gray-900

/* Elevation Shadows */
.shadow-elevation-1 { box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.shadow-elevation-2 { box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
```

## Accessibility Checklist
- [ ] All inputs have labels (for attribute linked)
- [ ] Color not sole indicator (use icons + text)
- [ ] Contrast ratio ≥ 4.5:1 for text
- [ ] Keyboard navigation: Tab, Arrow keys, Enter/Space
- [ ] ARIA labels for icons, status badges
- [ ] Focus visible (outline or ring)
- [ ] Error messages linked to inputs (aria-describedby)
- [ ] Dark mode: test all colors for contrast

---

**Font Stack**
```
headers: font-family: 'Inter', -apple-system, sans-serif;
body: font-family: 'IBM Plex Sans', -apple-system, sans-serif;
code: font-family: 'IBM Plex Mono', monospace;
```

**Spacing Grid**: 4px base (1 unit), use multiples: 4, 8, 12, 16, 24, 32, 48px

**Breakpoints**: sm:640px, md:768px, lg:1024px, xl:1280px (Tailwind default)
