from pathlib import Path
import datetime
import html
import json
import re

root = Path.cwd()


def parse_frontmatter(text):
    data = {}
    if not text.startswith('---'):
        return data
    end = text.find('\n---', 3)
    if end == -1:
        return data
    for line in text[3:end].splitlines():
        if ':' in line:
            key, value = line.split(':', 1)
            data[key.strip()] = value.strip().strip('"\'')
    return data


def first_text_line(text):
    body = text.split('---', 2)[-1] if text.startswith('---') else text
    for line in body.splitlines():
        value = line.strip()
        if value and not value.startswith('#') and not value.startswith('---'):
            return re.sub(r'[`*_<>]', '', value)[:220]
    return ''


def rel(path):
    return str(path.relative_to(root)).replace('\\', '/')


skills = []
for path in sorted((root / '.claude/skills').glob('*/SKILL.md')):
    text = path.read_text(encoding='utf-8', errors='ignore')
    meta = parse_frontmatter(text)
    skills.append({
        'name': meta.get('name') or path.parent.name,
        'path': rel(path),
        'description': meta.get('description') or first_text_line(text),
        'refs': re.findall(r'`([^`]*(?:agent|skill|SlashCommand|/ck:|/[a-z][^`]*)[^`]*)`', text, flags=re.I)[:12],
    })

agents = []
for path in sorted((root / '.claude/agents').glob('**/*.md')):
    text = path.read_text(encoding='utf-8', errors='ignore')
    meta = parse_frontmatter(text)
    agents.append({
        'name': meta.get('name') or path.stem,
        'path': rel(path),
        'description': (meta.get('description') or first_text_line(text))[:500],
        'model': meta.get('model', 'default'),
        'tools': meta.get('tools', ''),
        'backticks': re.findall(r'`([^`]+)`', text)[:30],
    })

commands = []
for base in [root / '.opencode/commands', root / '.claude/command-archive']:
    if not base.exists():
        continue
    for path in sorted(base.glob('**/*.md')):
        if path.name == 'SKILL.md':
            continue
        text = path.read_text(encoding='utf-8', errors='ignore')
        meta = parse_frontmatter(text)
        commands.append({
            'name': '/' + path.relative_to(base).with_suffix('').as_posix().replace('/', ':'),
            'path': rel(path),
            'description': (meta.get('description') or first_text_line(text))[:260],
            'text': text[:5000],
        })

seen_paths = set()
commands = [cmd for cmd in commands if not (cmd['path'] in seen_paths or seen_paths.add(cmd['path']))]
edges = []

for command in commands:
    body = command['text'].lower()
    for skill in skills:
        if re.search(r'\b' + re.escape(skill['name'].lower()) + r'\b', body):
            edges.append({'from': command['name'], 'to': skill['name'], 'type': 'command→skill'})
    for ref in sorted(set(re.findall(r'/(?:ck:)?[a-zA-Z0-9:_-]+', command['text'])))[:10]:
        edges.append({'from': command['name'], 'to': ref, 'type': 'command→command'})

for agent in agents:
    body = (agent['description'] + ' ' + agent['tools'] + ' ' + ' '.join(agent['backticks'])).lower()
    for skill in skills:
        if skill['name'].lower() in body:
            edges.append({'from': agent['name'], 'to': skill['name'], 'type': 'agent→skill'})

for skill in skills:
    joined = ' '.join(skill['refs']).lower()
    for agent in agents:
        if agent['name'].lower() in joined:
            edges.append({'from': skill['name'], 'to': agent['name'], 'type': 'skill→agent'})

manual_edges = [
    ('cook', 'planner', 'skill→agent'), ('fix', 'debugger', 'skill→agent'),
    ('test', 'tester', 'skill→agent'), ('code-review', 'code-reviewer', 'skill→agent'),
    ('docs', 'docs-manager', 'skill→agent'), ('project-management', 'project-manager', 'skill→agent'),
    ('research', 'researcher', 'skill→agent'), ('planning', 'planner', 'skill→agent'),
    ('scout', 'Explore', 'skill→agent'), ('mcp-management', 'mcp-manager', 'skill→agent'),
    ('frontend-design', 'ui-ux-designer', 'skill→agent'), ('git', 'git-manager', 'skill→agent'),
    ('/plan', 'planning', 'command→skill'), ('/test', 'test', 'command→skill'),
    ('/preview', 'preview', 'command→skill'), ('/watzup', 'watzup', 'command→skill'),
    ('/ask', 'ask', 'command→skill'), ('/use-mcp', 'use-mcp', 'command→skill'),
    ('/worktree', 'worktree', 'command→skill'), ('/review:codebase', 'code-review', 'command→skill'),
    ('/docs:update', 'docs', 'command→skill'), ('/kanban', 'kanban', 'command→skill'),
]
for source, target, edge_type in manual_edges:
    edges.append({'from': source, 'to': target, 'type': edge_type})

unique_edges = []
seen_edges = set()
for edge in edges:
    key = (edge['from'], edge['to'], edge['type'])
    if key not in seen_edges:
        seen_edges.add(key)
        unique_edges.append(edge)
edges = unique_edges

data = {
    'generated': datetime.datetime.now().isoformat(timespec='seconds'),
    'counts': {'skills': len(skills), 'agents': len(agents), 'commands': len(commands), 'edges': len(edges)},
    'skills': skills,
    'agents': agents,
    'commands': [{key: value for key, value in cmd.items() if key != 'text'} for cmd in commands],
    'edges': edges,
}

css = r'''
:root{--bg:#09111f;--panel:#101a2d;--panel2:#14223b;--border:#263a61;--text:#eef4ff;--muted:#9fb0ce;--blue:#82aaff;--green:#34d399;--purple:#c084fc;--orange:#fbbf24;--red:#fb7185}*{box-sizing:border-box}body{margin:0;background:linear-gradient(180deg,#081020,#0b1220 40%,#080d18);color:var(--text);font-family:Inter,Segoe UI,Arial,sans-serif;line-height:1.5}header{padding:36px 40px 26px;background:radial-gradient(circle at 20% 0,#3758ff66,transparent 34%),radial-gradient(circle at 80% 20%,#22c55e22,transparent 30%),#0b1220;border-bottom:1px solid var(--border)}h1{margin:0;font-size:clamp(30px,5vw,52px);letter-spacing:-.04em}h2{margin:0 0 14px;font-size:24px}h3{margin:16px 0 8px}.sub{max-width:1100px;color:var(--muted)}main{padding:24px 40px 44px}.stats{display:grid;grid-template-columns:repeat(4,minmax(120px,1fr));gap:14px}.card{background:linear-gradient(180deg,var(--panel),#0d1728);border:1px solid var(--border);border-radius:18px;padding:18px;box-shadow:0 18px 45px #0006}.num{font-size:36px;font-weight:800;color:var(--blue)}.section{margin-top:22px}.quick{display:grid;grid-template-columns:repeat(3,1fr);gap:14px}.code{background:#060b14;border:1px solid #263957;border-radius:14px;color:#dce8ff;margin-top:12px;padding:14px;white-space:pre-wrap;overflow:auto}.tabs{display:flex;gap:8px;flex-wrap:wrap;margin-bottom:12px}.tab{border:1px solid var(--border);background:#0b1424;color:var(--text);border-radius:999px;padding:9px 14px;cursor:pointer}.tab.active{background:#1d4ed8;border-color:#60a5fa}.workbench{display:grid;grid-template-columns:minmax(280px,420px) 1fr;gap:18px}.search{width:100%;padding:12px 14px;border:1px solid var(--border);background:#07101e;color:var(--text);border-radius:12px;margin-bottom:12px}.list{height:720px;overflow:auto;padding-right:4px}.item{display:block;width:100%;text-align:left;color:var(--text);background:#0b1424;border:1px solid #203354;border-radius:14px;margin:8px 0;padding:12px;cursor:pointer}.item:hover,.item.active{border-color:#7aa2ff;background:#132442}.item b{display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.muted{color:var(--muted);font-size:13px}.path{font-family:Consolas,monospace;color:#b8c7e6;word-break:break-all}.graph-shell{min-height:760px}.graph-toolbar{display:flex;gap:10px;align-items:center;justify-content:space-between;flex-wrap:wrap}.legend{display:flex;gap:12px;flex-wrap:wrap;color:var(--muted);font-size:13px}.dot{display:inline-block;width:10px;height:10px;border-radius:50%;margin-right:5px}.graph-wrap{height:610px;overflow:hidden;border:1px solid #22375c;border-radius:16px;background:radial-gradient(circle at center,#12213d,#07101e 72%);margin-top:12px}svg{width:100%;height:100%;display:block;touch-action:none;cursor:grab}svg.dragging{cursor:grabbing}.zoom-controls{display:flex;gap:6px;align-items:center;flex-wrap:wrap}.zoom-value{min-width:48px;text-align:center;color:var(--muted);font-size:13px}.edge{stroke:#6b83b4;stroke-width:1.1;opacity:.42}.node circle{stroke:#dbeafe55;stroke-width:1.2}.node text{fill:#eef4ff;font-size:11px;text-anchor:middle;paint-order:stroke;stroke:#07101e;stroke-width:3px;stroke-linejoin:round}.node{cursor:pointer}.detail{min-height:130px}.pill{display:inline-block;margin:3px;padding:5px 10px;border-radius:999px;background:#1d2c4a;color:#dbeafe;font-size:12px}.table-wrap{max-height:680px;overflow:auto;border:1px solid var(--border);border-radius:14px}table{width:100%;border-collapse:collapse;min-width:780px}th,td{border-bottom:1px solid #203354;padding:10px;text-align:left;vertical-align:top}th{position:sticky;top:0;background:#111d32;color:#fff}code{font-family:Consolas,monospace;color:#bfdbfe}.sr{position:absolute;left:-9999px}@media(max-width:1100px){main,header{padding-left:18px;padding-right:18px}.stats,.quick,.workbench{grid-template-columns:1fr}.list{height:420px}.graph-wrap{height:720px}.stats{grid-template-columns:repeat(2,1fr)}}
'''

json_for_script = json.dumps(data, ensure_ascii=False).replace('</', '<\\/')

js = r'''
const DATA = __DATA__;
let currentKind = 'skill';
let currentQuery = '';
let activeName = '';
let graphState = {scale: 1, x: 0, y: 0, dragging: false, startX: 0, startY: 0, originX: 0, originY: 0};
const kinds = {skill: DATA.skills, agent: DATA.agents, command: DATA.commands};
const colors = {skill:'#34d399', agent:'#c084fc', command:'#fbbf24', other:'#94a3b8'};
function esc(value){return String(value||'').replace(/[&<>\"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','\"':'&quot;',"'":'&#39;'}[m]));}
function byName(kind,name){return (kinds[kind]||[]).find(item => item.name === name) || null;}
function edgeList(name){return DATA.edges.filter(edge => edge.from === name || edge.to === name);}
function setTab(kind){currentKind = kind; document.querySelectorAll('.tab').forEach(btn => btn.classList.toggle('active', btn.dataset.kind === kind)); renderList();}
function renderStats(){document.getElementById('stats').innerHTML = Object.entries(DATA.counts).map(([key,value]) => `<div class=\"card\"><div class=\"num\">${value}</div><div>${esc(key)}</div></div>`).join('');}
function renderList(){const list = document.getElementById('assetList'); const items = kinds[currentKind].filter(item => `${item.name} ${item.description} ${item.path}`.toLowerCase().includes(currentQuery)); document.getElementById('listTitle').textContent = `${currentKind}s (${items.length})`; list.innerHTML = items.map(item => `<button class=\"item ${item.name===activeName?'active':''}\" data-name=\"${esc(item.name)}\"><b>${esc(item.name)}</b><span class=\"muted\">${esc(item.description).slice(0,150)}</span><div class=\"path\">${esc(item.path)}</div></button>`).join(''); list.querySelectorAll('.item').forEach(btn => btn.addEventListener('click', () => showDetail(currentKind, btn.dataset.name)));}
function showDetail(kind,name){const item = byName(kind,name) || {name, description:'External or inferred node', path:''}; activeName = name; currentKind = kind in kinds ? kind : currentKind; const edges = edgeList(name); document.getElementById('detail').innerHTML = `<h2>${esc(item.name)}</h2><p>${esc(item.description)}</p><div class=\"path\">${esc(item.path)}</div>${item.model?`<p><b>Model:</b> ${esc(item.model)}</p>`:''}${item.tools?`<p><b>Tools:</b> ${esc(item.tools)}</p>`:''}<h3>Relationships (${edges.length})</h3><div>${edges.map(edge => `<span class=\"pill\">${esc(edge.from)} ${esc(edge.type)} ${esc(edge.to)}</span>`).join('') || '<span class=\"muted\">No direct relationship detected</span>'}</div>`; renderList(); highlightGraph(name);}
function graphNodes(){const nodes = []; DATA.commands.slice(0,28).forEach(item => nodes.push({id:item.name,type:'command'})); DATA.skills.slice(0,58).forEach(item => nodes.push({id:item.name,type:'skill'})); DATA.agents.forEach(item => nodes.push({id:item.name,type:'agent'})); return nodes;}
function applyGraphTransform(){const viewport = document.getElementById('graphViewport'); if(!viewport) return; viewport.setAttribute('transform', `translate(${graphState.x} ${graphState.y}) scale(${graphState.scale})`); document.getElementById('zoomValue').textContent = `${Math.round(graphState.scale*100)}%`;}
function zoomGraph(factor, clientX, clientY){const svg = document.getElementById('graph'); const rect = svg.getBoundingClientRect(); const viewBox = svg.viewBox.baseVal; const px = ((clientX ?? (rect.left+rect.width/2)) - rect.left) * viewBox.width / rect.width; const py = ((clientY ?? (rect.top+rect.height/2)) - rect.top) * viewBox.height / rect.height; const next = Math.max(0.35, Math.min(4, graphState.scale * factor)); const ratio = next / graphState.scale; graphState.x = px - (px - graphState.x) * ratio; graphState.y = py - (py - graphState.y) * ratio; graphState.scale = next; applyGraphTransform();}
function resetGraphZoom(){graphState = {scale:1, x:0, y:0, dragging:false, startX:0, startY:0, originX:0, originY:0}; applyGraphTransform();}
function setupGraphPanZoom(){const svg = document.getElementById('graph'); svg.addEventListener('wheel', event => {event.preventDefault(); zoomGraph(event.deltaY < 0 ? 1.12 : 0.88, event.clientX, event.clientY);}, {passive:false}); svg.addEventListener('pointerdown', event => {if(event.target.closest('.node')) return; graphState.dragging = true; graphState.startX = event.clientX; graphState.startY = event.clientY; graphState.originX = graphState.x; graphState.originY = graphState.y; svg.setPointerCapture(event.pointerId); svg.classList.add('dragging');}); svg.addEventListener('pointermove', event => {if(!graphState.dragging) return; const rect = svg.getBoundingClientRect(); const viewBox = svg.viewBox.baseVal; graphState.x = graphState.originX + (event.clientX - graphState.startX) * viewBox.width / rect.width; graphState.y = graphState.originY + (event.clientY - graphState.startY) * viewBox.height / rect.height; applyGraphTransform();}); svg.addEventListener('pointerup', event => {graphState.dragging = false; svg.classList.remove('dragging'); try{svg.releasePointerCapture(event.pointerId)}catch(_){}}); svg.addEventListener('pointerleave', () => {graphState.dragging = false; svg.classList.remove('dragging');}); document.getElementById('zoomIn').addEventListener('click', () => zoomGraph(1.2)); document.getElementById('zoomOut').addEventListener('click', () => zoomGraph(0.8)); document.getElementById('zoomReset').addEventListener('click', resetGraphZoom);}
function drawGraph(){const svg = document.getElementById('graph'); const width = 1180, height = 610, cx = width/2, cy = height/2; svg.setAttribute('viewBox', `0 0 ${width} ${height}`); svg.innerHTML = ''; const viewport = document.createElementNS('http://www.w3.org/2000/svg','g'); viewport.setAttribute('id','graphViewport'); svg.appendChild(viewport); const nodes = graphNodes(); const positions = {}; const groupConfig = {command:{r:275,y:0,start:-.5}, skill:{r:190,y:0,start:.8}, agent:{r:95,y:0,start:2.3}}; ['command','skill','agent'].forEach(type => {const group = nodes.filter(node => node.type === type); group.forEach((node,index) => {const angle = groupConfig[type].start + Math.PI*2*index/group.length; positions[node.id] = {x:cx + Math.cos(angle)*groupConfig[type].r, y:cy + Math.sin(angle)*groupConfig[type].r, type};});}); const edgeGroup = document.createElementNS('http://www.w3.org/2000/svg','g'); viewport.appendChild(edgeGroup); DATA.edges.filter(edge => positions[edge.from] && positions[edge.to]).slice(0,190).forEach(edge => {const a = positions[edge.from], b = positions[edge.to]; const line = document.createElementNS('http://www.w3.org/2000/svg','line'); line.setAttribute('x1', a.x); line.setAttribute('y1', a.y); line.setAttribute('x2', b.x); line.setAttribute('y2', b.y); line.setAttribute('class', 'edge'); line.dataset.from = edge.from; line.dataset.to = edge.to; edgeGroup.appendChild(line);}); const nodeGroup = document.createElementNS('http://www.w3.org/2000/svg','g'); viewport.appendChild(nodeGroup); nodes.forEach(node => {const pos = positions[node.id]; const g = document.createElementNS('http://www.w3.org/2000/svg','g'); g.setAttribute('class', `node ${node.type}`); g.dataset.name = node.id; g.dataset.kind = node.type; const circle = document.createElementNS('http://www.w3.org/2000/svg','circle'); circle.setAttribute('cx', pos.x); circle.setAttribute('cy', pos.y); circle.setAttribute('r', node.type === 'agent' ? 18 : 14); circle.setAttribute('fill', colors[node.type]); const text = document.createElementNS('http://www.w3.org/2000/svg','text'); text.setAttribute('x', pos.x); text.setAttribute('y', pos.y + 31); text.textContent = node.id.length > 22 ? node.id.slice(0,21)+'…' : node.id; g.appendChild(circle); g.appendChild(text); g.addEventListener('click', () => showDetail(node.type, node.id)); nodeGroup.appendChild(g);}); applyGraphTransform();}
function highlightGraph(name){document.querySelectorAll('#graph .node').forEach(node => {node.style.opacity = !name || node.dataset.name === name || edgeList(name).some(edge => edge.from === node.dataset.name || edge.to === node.dataset.name) ? '1' : '.25';}); document.querySelectorAll('#graph .edge').forEach(edge => {edge.style.opacity = !name || edge.dataset.from === name || edge.dataset.to === name ? '.9' : '.12'; edge.style.strokeWidth = edge.dataset.from === name || edge.dataset.to === name ? '2.2' : '1.1';});}
function clearHighlight(){activeName=''; highlightGraph(''); renderList();}
document.addEventListener('DOMContentLoaded', () => {renderStats(); document.querySelectorAll('.tab').forEach(btn => btn.addEventListener('click', () => setTab(btn.dataset.kind))); document.getElementById('search').addEventListener('input', event => {currentQuery = event.target.value.toLowerCase(); renderList();}); document.getElementById('clearGraph').addEventListener('click', clearHighlight); renderList(); drawGraph(); setupGraphPanZoom(); showDetail('skill', DATA.skills[0].name);});
'''.replace('__DATA__', json_for_script)

skill_rows = ''.join(f'<tr><td>{html.escape(item["name"])}</td><td>{html.escape(item["description"][:220])}</td><td><code>{html.escape(item["path"])}</code></td></tr>' for item in skills)
agent_rows = ''.join(f'<tr><td>{html.escape(item["name"])}</td><td>{html.escape(item["description"][:220])}</td><td><code>{html.escape(item["path"])}</code></td></tr>' for item in agents)
command_rows = ''.join(f'<tr><td>{html.escape(item["name"])}</td><td>{html.escape(item["description"][:220])}</td><td><code>{html.escape(item["path"])}</code></td></tr>' for item in data['commands'])

html_doc = f'''<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Claude Kit Map</title>
  <style>{css}</style>
</head>
<body>
  <header>
    <h1>Claude Kit — Agents, Skills, Commands</h1>
    <p class="sub">Bản đồ local từ repo này. Generated: {html.escape(data['generated'])}. Dùng để hiểu thứ gì gọi thứ gì, khi nào dùng, workflow tổng thể.</p>
  </header>
  <main>
    <section id="stats" class="stats" aria-label="Counts"></section>

    <section class="section card">
      <h2>1. Cách dùng nhanh</h2>
      <div class="quick">
        <div><h3>Skill</h3><p>Playbook được kích hoạt bằng slash command hoặc Skill tool. Nạp quy trình, rules, scripts, references.</p></div>
        <div><h3>Agent</h3><p>Subagent chuyên trách, spawn qua Agent/Task tool. Dùng khi cần tách context hoặc chuyên môn sâu.</p></div>
        <div><h3>Command</h3><p>Shortcut slash command. Active/mirror: <code>.opencode/commands</code>. Archive: <code>.claude/command-archive</code>.</p></div>
      </div>
      <div class="code">Luồng chuẩn: User → slash command/skill → skill workflow → spawn agent nếu cần → agent dùng tools/scripts → reports/plans/docs.\nImplementation flow: /cook hoặc /planning → planner/researcher → fullstack-developer → code-simplifier → tester → code-reviewer → docs-manager.</div>
    </section>

    <section class="section workbench">
      <aside class="card">
        <h2>2. Catalog</h2>
        <input id="search" class="search" placeholder="Tìm skill, agent, command...">
        <div class="tabs" role="tablist">
          <button class="tab active" data-kind="skill">Skills</button>
          <button class="tab" data-kind="agent">Agents</button>
          <button class="tab" data-kind="command">Commands</button>
        </div>
        <h3 id="listTitle">skills</h3>
        <div id="assetList" class="list"></div>
      </aside>

      <section>
        <div class="card graph-shell">
          <div class="graph-toolbar">
            <div>
              <h2>3. Relationship graph</h2>
              <div class="legend">
                <span><i class="dot" style="background:#34d399"></i>Skill</span>
                <span><i class="dot" style="background:#c084fc"></i>Agent</span>
                <span><i class="dot" style="background:#fbbf24"></i>Command</span>
              </div>
            </div>
            <div class="zoom-controls"><button id="zoomOut" class="tab" type="button">−</button><span id="zoomValue" class="zoom-value">100%</span><button id="zoomIn" class="tab" type="button">+</button><button id="zoomReset" class="tab" type="button">Reset zoom</button><button id="clearGraph" class="tab" type="button">Clear highlight</button></div>
          </div>
          <div class="graph-wrap"><svg id="graph" role="img" aria-label="Claude Kit relationship graph"></svg></div>
        </div>
        <div id="detail" class="section card detail"></div>
      </section>
    </section>

    <section class="section card">
      <h2>4. Workflow model</h2>
      <div class="code">Planning: planning / ck-plan skills → planner agent → researcher/Explore agents → plan files in plans/.\nImplementation: cook/backend/frontend/etc. skills → fullstack-developer or specialist agents → code files.\nValidation: test skill → tester agent → test reports; code-review skill → code-reviewer / code-review-expert.\nOperations: git skill → git-manager/git-expert; deploy/devops → infra agents; mcp-management/use-mcp → mcp-manager.\nDesign/UI: design/frontend-design/ui-styling/stitch/preview → ui-ux-designer + browser/media skills.\nDebugging/security: fix/ck-debug/security-scan/ck-security → debugger/security audit flow.</div>
    </section>

    <section class="section card">
      <h2>5. Inventory table</h2>
      <h3>Skills</h3><div class="table-wrap"><table><tr><th>Name</th><th>Use</th><th>Path</th></tr>{skill_rows}</table></div>
      <h3>Agents</h3><div class="table-wrap"><table><tr><th>Name</th><th>Use</th><th>Path</th></tr>{agent_rows}</table></div>
      <h3>Commands</h3><div class="table-wrap"><table><tr><th>Name</th><th>Use</th><th>Path</th></tr>{command_rows}</table></div>
    </section>
  </main>
  <script>{js}</script>
</body>
</html>
'''

out = root / 'docs/claude-kit-map.html'
out.write_text(html_doc, encoding='utf-8')
(root / 'plans/reports/claude-kit-map-data.json').write_text(json.dumps(data, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({'output': str(out), 'data': str(root / 'plans/reports/claude-kit-map-data.json'), 'counts': data['counts']}, ensure_ascii=False, indent=2))
