const express = require('express');
const fs = require('fs');
const path = require('path');
const multer = require('multer');

const app = express();
const PORT = process.env.PORT || 3000;
const AUTH_TOKEN = process.env.AUTH_TOKEN || null;
const DATA_DIR = path.join(__dirname, 'data');
const SCREENSHOTS_DIR = path.join(__dirname, 'screenshots');

[DATA_DIR, SCREENSHOTS_DIR].forEach(d => {
    if (!fs.existsSync(d)) fs.mkdirSync(d, { recursive: true });
});

const upload = multer({
    dest: path.join(__dirname, 'tmp'),
    limits: { fileSize: 10 * 1024 * 1024 },
    fileFilter: (_req, file, cb) => {
        if (!file.mimetype.startsWith('image/')) return cb(new Error('Images only'));
        cb(null, true);
    }
});

if (!fs.existsSync(path.join(__dirname, 'tmp'))) {
    fs.mkdirSync(path.join(__dirname, 'tmp'));
}

app.use(express.json({ limit: '10mb' }));
app.use((_req, res, next) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET,POST,DELETE,OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type,Authorization');
    if (_req.method === 'OPTIONS') return res.sendStatus(204);
    next();
});

function auth(req, res, next) {
    if (!AUTH_TOKEN) return next();
    const token = req.headers['authorization'];
    if (token !== `Bearer ${AUTH_TOKEN}`) return res.status(401).json({ error: 'Unauthorized' });
    next();
}

// ── Usage Data Upload ──
app.post('/api/upload', auth, (req, res) => {
    const data = req.body;
    if (!data || !data.apps) return res.status(400).json({ error: 'Missing usage data payload' });
    const deviceId = data.deviceId || 'unknown';
    const timestamp = data.exportedAt || new Date().toISOString();
    const safeName = deviceId.replace(/[^a-zA-Z0-9_-]/g, '_');
    const filename = `${safeName}_${timestamp.replace(/[:.]/g, '-')}.json`;
    fs.writeFileSync(path.join(DATA_DIR, filename), JSON.stringify(data, null, 2));
    console.log(`Saved: ${filename} (${data.apps.length} apps)`);
    res.json({ status: 'ok', file: filename });
});

// ── Screenshot Upload ──
app.post('/api/upload/screenshot', auth, upload.single('screenshot'), (req, res) => {
    if (!req.file) return res.status(400).json({ error: 'No file uploaded' });
    const pkg = req.body.packageName || 'unknown';
    const ts = req.body.capturedAt || Date.now();
    const ext = path.extname(req.file.originalname) || '.jpg';
    const filename = `ss_${ts}_${pkg.replace(/[^a-zA-Z0-9]/g, '_')}${ext}`;
    const dest = path.join(SCREENSHOTS_DIR, filename);
    fs.renameSync(req.file.path, dest);
    console.log(`Screenshot saved: ${filename}`);
    res.json({ status: 'ok', filename });
});

// ── List Screenshots ──
app.get('/api/screenshots', (_req, res) => {
    const files = fs.readdirSync(SCREENSHOTS_DIR)
        .filter(f => /\.(jpg|jpeg|png)$/i.test(f))
        .sort().reverse()
        .map(f => {
            const stat = fs.statSync(path.join(SCREENSHOTS_DIR, f));
            return { filename: f, size: stat.size, uploadedAt: stat.mtime.toISOString() };
        });
    res.json(files);
});

// ── Serve Screenshot Image ──
app.get('/api/screenshots/:filename', (req, res) => {
    const filename = path.basename(req.params.filename);
    const filepath = path.join(SCREENSHOTS_DIR, filename);
    if (!fs.existsSync(filepath)) return res.status(404).json({ error: 'Not found' });
    res.sendFile(filepath);
});

// ── Data File Endpoints ──
app.get('/api/data', (_req, res) => {
    const files = fs.readdirSync(DATA_DIR)
        .filter(f => f.endsWith('.json'))
        .sort().reverse()
        .map(f => {
            const stat = fs.statSync(path.join(DATA_DIR, f));
            return { filename: f, size: stat.size, uploadedAt: stat.mtime.toISOString() };
        });
    res.json(files);
});

app.get('/api/data/:filename', (req, res) => {
    const filename = path.basename(req.params.filename);
    const filepath = path.join(DATA_DIR, filename);
    if (!fs.existsSync(filepath)) return res.status(404).json({ error: 'Not found' });
    try { res.json(JSON.parse(fs.readFileSync(filepath, 'utf-8'))); }
    catch { res.status(500).json({ error: 'Corrupt file' }); }
});

app.delete('/api/data/:filename', auth, (req, res) => {
    const filename = path.basename(req.params.filename);
    const filepath = path.join(DATA_DIR, filename);
    if (!fs.existsSync(filepath)) return res.status(404).json({ error: 'Not found' });
    fs.unlinkSync(filepath);
    res.json({ status: 'deleted', filename });
});

// ── Dashboard ──
app.get('/', (_req, res) => {
    res.send(`<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>HabitMind Usage</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#0F0F12;color:#E2E2E7;padding:1.5rem;max-width:960px;margin:0 auto}
h1{color:#818CF8;font-size:1.5rem;margin-bottom:0.25rem}
.sub{color:#64748B;font-size:0.85rem;margin-bottom:1.5rem}
.tabs{display:flex;gap:0.5rem;margin-bottom:1.5rem}
.tab{padding:0.5rem 1.2rem;border-radius:8px;background:#1E1E24;border:1px solid #24242A;color:#94949E;cursor:pointer;font-size:0.85rem}
.tab.active{background:#818CF8;color:#0F0F12;border-color:#818CF8}
.tab:hover:not(.active){background:#24242A}
.panel{display:none}
.panel.active{display:block}
.stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:0.75rem;margin-bottom:1.5rem}
.stat{background:#16161A;border:1px solid #24242A;border-radius:12px;padding:1rem}
.stat .num{font-size:1.4rem;color:#E2E2E7;font-weight:600}
.stat .lbl{font-size:0.75rem;color:#64748B;margin-top:0.2rem}
select{background:#1E1E24;color:#E2E2E7;border:1px solid #24242A;border-radius:8px;padding:0.5rem 0.75rem;font-size:0.85rem;width:100%;margin-bottom:1rem}
table{width:100%;border-collapse:collapse}
th,td{text-align:left;padding:0.5rem 0.75rem;border-bottom:1px solid #24242A}
th{color:#818CF8;font-size:0.7rem;text-transform:uppercase;letter-spacing:0.05em;font-weight:600}
td{font-size:0.85rem;color:#E2E2E7}
td.time{color:#94949E;text-align:right;white-space:nowrap}
td.bar{width:40%}
.bar-fill{height:6px;border-radius:99px;background:#818CF8;min-width:2px;transition:width 0.3s}
.empty{color:#64748B;text-align:center;padding:3rem;font-size:0.9rem}
.screenshots{display:grid;grid-template-columns:repeat(auto-fill,minmax(200px,1fr));gap:0.75rem}
.ss-card{background:#16161A;border:1px solid #24242A;border-radius:12px;overflow:hidden;cursor:pointer}
.ss-card img{width:100%;height:auto;display:block}
.ss-card .ss-info{padding:0.5rem;font-size:0.75rem;color:#94949E}
.modal{position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,0.8);display:flex;align-items:center;justify-content:center;z-index:100;cursor:pointer}
.modal img{max-width:90%;max-height:90%;border-radius:8px}
.timeline{position:relative;padding-left:1.5rem}
.timeline::before{content:'';position:absolute;left:6px;top:0;bottom:0;width:2px;background:#24242A}
.tl-event{position:relative;padding:0.5rem 0 0.5rem 1rem;border-bottom:1px solid #24242A}
.tl-event::before{content:'';position:absolute;left:-1.15rem;top:0.9rem;width:10px;height:10px;border-radius:50%;background:#818CF8}
.tl-event .tl-time{font-size:0.7rem;color:#64748B}
.tl-event .tl-app{font-size:0.85rem;color:#E2E2E7}
.loading{text-align:center;padding:2rem;color:#64748B}
</style>
</head>
<body>

<h1>&#9670; HabitMind Usage</h1>
<p class="sub" id="subtitle">Tracking your digital life</p>

<div class="tabs">
    <div class="tab active" onclick="switchTab('apps')">Apps</div>
    <div class="tab" onclick="switchTab('timeline')">Timeline</div>
    <div class="tab" onclick="switchTab('screenshots')">Screenshots</div>
</div>

<div id="panel-apps" class="panel active">
    <div class="stats" id="stats"></div>
    <select id="fileSelect" onchange="loadFile(this.value)"></select>
    <div id="appsContent"><div class="loading">Loading...</div></div>
</div>

<div id="panel-timeline" class="panel">
    <div class="stats" id="tlStats"></div>
    <select id="tlFileSelect" onchange="loadTimeline(this.value)"></select>
    <div id="timelineContent"><div class="loading">Loading...</div></div>
</div>

<div id="panel-screenshots" class="panel">
    <div id="screenshotsContent"><div class="loading">Loading...</div></div>
</div>

<div id="modal" class="modal" style="display:none" onclick="this.style.display='none'">
    <img id="modalImg" src="">
</div>

<script>
const BASE = '';
let allFiles = [];

async function api(url) { const r=await fetch(BASE+url); if(!r.ok)throw new Error(await r.text()); return r.json(); }

function fmt(m) { const h=Math.floor(m/60); const min=m%60; return (h?h+'h ':'')+min+'m'; }

function fmtTime(iso) { if(!iso)return ''; const d=new Date(iso); return d.toLocaleDateString()+' '+d.toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'}); }

function fmtShort(iso) { if(!iso)return ''; return new Date(iso).toLocaleTimeString([],{hour:'2-digit',minute:'2-digit'}); }

function switchTab(name) {
    document.querySelectorAll('.tab').forEach(t=>t.classList.remove('active'));
    document.querySelectorAll('.panel').forEach(p=>p.classList.remove('active'));
    document.querySelector('.tab[onclick*="'+name+'"]').classList.add('active');
    document.getElementById('panel-'+name).classList.add('active');
    if (name === 'screenshots') loadScreenshots();
}

async function refreshFiles() {
    allFiles = await api('/api/data');
    [document.getElementById('fileSelect'), document.getElementById('tlFileSelect')].forEach(sel => {
        sel.innerHTML = '<option value="">Latest upload</option>';
        allFiles.forEach(f => {
            const o=document.createElement('option'); o.value=f.filename;
            o.textContent=f.filename.replace(/\.json$/,'').split('_').slice(1).join('_')||f.filename;
            sel.appendChild(o);
        });
    });
    if (allFiles.length > 0) { loadFile(''); loadTimeline(''); }
    else { document.getElementById('appsContent').innerHTML='<div class="empty">No data yet</div>'; }
}

async function loadFile(filename) {
    if (!filename && allFiles.length>0) filename=allFiles[0].filename;
    if (!filename) { document.getElementById('appsContent').innerHTML='<div class="empty">Select a file</div>'; return; }
    document.getElementById('appsContent').innerHTML='<div class="loading">Loading...</div>';
    try {
        const data=await api('/api/data/'+encodeURIComponent(filename));
        renderApps(data);
    } catch(e) { document.getElementById('appsContent').innerHTML='<div class="empty">Error: '+e.message+'</div>'; }
}

function renderApps(data) {
    const apps=data.apps||[]; const totalMin=apps.reduce((s,a)=>s+(a.totalTimeInForegroundMinutes||0),0);
    const maxMin=apps.length>0?Math.max(...apps.map(a=>a.totalTimeInForegroundMinutes||1)):1;
    document.getElementById('subtitle').textContent='Apps tracked — '+(data.exportedAt?fmtTime(data.exportedAt):'');
    document.getElementById('stats').innerHTML=
        '<div class="stat"><div class="num">'+apps.length+'</div><div class="lbl">Apps tracked</div></div>'+
        '<div class="stat"><div class="num">'+fmt(totalMin)+'</div><div class="lbl">Total screen time</div></div>'+
        '<div class="stat"><div class="num">'+fmt(Math.round(totalMin/(apps.length||1)))+'</div><div class="lbl">Avg per app</div></div>'+
        '<div class="stat"><div class="num">'+(apps.length>0?apps[0].appName:'-')+'</div><div class="lbl">Most used</div></div>';
    if (apps.length===0) { document.getElementById('appsContent').innerHTML='<div class="empty">No data</div>'; return; }
    let rows='';
    apps.forEach((a,i)=>{
        const m=a.totalTimeInForegroundMinutes||0;
        const pct=totalMin>0?(m/maxMin)*100:0;
        rows+='<tr><td style="color:#64748B;width:2rem">'+(i+1)+'</td><td>'+(a.appName||a.packageName)+'</td><td class="bar"><div class="bar-fill" style="width:'+pct+'%"></div></td><td class="time">'+fmt(m)+'</td></tr>';
    });
    document.getElementById('appsContent').innerHTML='<table><thead><tr><th>#</th><th>App</th><th></th><th>Time</th></tr></thead><tbody>'+rows+'</tbody></table>';
}

async function loadTimeline(filename) {
    if (!filename && allFiles.length>0) filename=allFiles[0].filename;
    if (!filename) { document.getElementById('timelineContent').innerHTML='<div class="empty">No data</div>'; return; }
    document.getElementById('timelineContent').innerHTML='<div class="loading">Loading...</div>';
    try {
        const data=await api('/api/data/'+encodeURIComponent(filename));
        renderTimeline(data);
    } catch(e) { document.getElementById('timelineContent').innerHTML='<div class="empty">Error: '+e.message+'</div>'; }
}

function renderTimeline(data) {
    const sessions=data.sessions||[];
    document.getElementById('tlStats').innerHTML='<div class="stat"><div class="num">'+sessions.length+'</div><div class="lbl">App switches today</div></div>';
    if(sessions.length===0){document.getElementById('timelineContent').innerHTML='<div class="empty">No session data. Enable the Accessibility Service from Settings.</div>';return;}
    let html='<div class="timeline">';
    sessions.slice(-50).forEach(s=>{
        const t=s.closedAt||Date.now(); const dur=Math.round((t-s.openedAt)/60000);
        html+='<div class="tl-event"><div class="tl-time">'+fmtShort(new Date(s.openedAt).toISOString())+' — '+dur+'m</div><div class="tl-app">'+(s.appName||s.packageName)+'</div></div>';
    });
    html+='</div>';
    document.getElementById('timelineContent').innerHTML=html;
}

async function loadScreenshots() {
    document.getElementById('screenshotsContent').innerHTML='<div class="loading">Loading...</div>';
    try {
        const files=await api('/api/screenshots');
        if(files.length===0){document.getElementById('screenshotsContent').innerHTML='<div class="empty">No screenshots yet</div>';return;}
        let html='<div class="screenshots">';
        files.forEach(f=>{
            const ts=f.filename.replace(/^ss_/,'').split('_')[0];
            const d=ts?new Date(parseInt(ts)).toLocaleString():'';
            html+='<div class="ss-card" onclick="document.getElementById(\'modal\').style.display=\'flex\';document.getElementById(\'modalImg\').src=\''+BASE+'/api/screenshots/'+f.filename+'\'"><img src="'+BASE+'/api/screenshots/'+f.filename+'" loading="lazy"><div class="ss-info">'+d+'</div></div>';
        });
        html+='</div>';
        document.getElementById('screenshotsContent').innerHTML=html;
    } catch(e) { document.getElementById('screenshotsContent').innerHTML='<div class="empty">Error: '+e.message+'</div>'; }
}

refreshFiles();
</script>
</body></html>`);
});

app.listen(PORT, () => {
    console.log(`HabitMind Usage Server running on port ${PORT}`);
    if (AUTH_TOKEN) console.log(`Auth enabled, set AUTH_TOKEN env var on server`);
});
