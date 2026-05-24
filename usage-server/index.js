const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;
const AUTH_TOKEN = process.env.AUTH_TOKEN || null;
const DATA_DIR = path.join(__dirname, 'data');

if (!fs.existsSync(DATA_DIR)) fs.mkdirSync(DATA_DIR, { recursive: true });

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
    if (token !== `Bearer ${AUTH_TOKEN}`) {
        return res.status(401).json({ error: 'Unauthorized' });
    }
    next();
}

app.post('/api/upload', auth, (req, res) => {
    const data = req.body;
    if (!data || !data.apps) {
        return res.status(400).json({ error: 'Missing usage data payload' });
    }
    const deviceId = data.deviceId || 'unknown';
    const timestamp = data.exportedAt || new Date().toISOString();
    const safeName = deviceId.replace(/[^a-zA-Z0-9_-]/g, '_');
    const filename = `${safeName}_${timestamp.replace(/[:.]/g, '-')}.json`;
    const filepath = path.join(DATA_DIR, filename);
    fs.writeFileSync(filepath, JSON.stringify(data, null, 2));
    console.log(`Saved: ${filename} (${data.apps.length} apps)`);
    res.json({ status: 'ok', file: filename });
});

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
    try {
        const raw = fs.readFileSync(filepath, 'utf-8');
        res.json(JSON.parse(raw));
    } catch {
        res.status(500).json({ error: 'Corrupt file' });
    }
});

app.delete('/api/data/:filename', auth, (req, res) => {
    const filename = path.basename(req.params.filename);
    const filepath = path.join(DATA_DIR, filename);
    if (!fs.existsSync(filepath)) return res.status(404).json({ error: 'Not found' });
    fs.unlinkSync(filepath);
    res.json({ status: 'deleted', filename });
});

app.get('/', (_req, res) => {
    res.send(`<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>HabitMind Usage</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#0F0F12;color:#E2E2E7;padding:1.5rem;max-width:800px;margin:0 auto}
h1{color:#818CF8;font-size:1.5rem;margin-bottom:0.25rem}
.sub{color:#64748B;font-size:0.85rem;margin-bottom:1.5rem}

.stats{display:grid;grid-template-columns:repeat(auto-fit,minmax(140px,1fr));gap:0.75rem;margin-bottom:1.5rem}
.stat{background:#16161A;border:1px solid #24242A;border-radius:12px;padding:1rem}
.stat .num{font-size:1.4rem;color:#E2E2E7;font-weight:600}
.stat .lbl{font-size:0.75rem;color:#64748B;margin-top:0.2rem}

.controls{display:flex;gap:0.5rem;margin-bottom:1rem;flex-wrap:wrap}
select{background:#1E1E24;color:#E2E2E7;border:1px solid #24242A;border-radius:8px;padding:0.5rem 0.75rem;font-size:0.85rem;flex:1;min-width:0}
.btn{padding:0.5rem 1rem;border-radius:8px;border:1px solid #818CF8;background:transparent;color:#818CF8;cursor:pointer;font-size:0.85rem}
.btn:hover{background:#818CF8;color:#0F0F12}

table{width:100%;border-collapse:collapse}
th,td{text-align:left;padding:0.5rem 0.75rem;border-bottom:1px solid #24242A}
th{color:#818CF8;font-size:0.7rem;text-transform:uppercase;letter-spacing:0.05em;font-weight:600}
td{font-size:0.85rem}
td.name{color:#E2E2E7}
td.time{color:#94949E;text-align:right;white-space:nowrap}
td.bar{width:40%}
.bar-fill{height:6px;border-radius:99px;background:#818CF8;min-width:2px;transition:width 0.3s}
.empty{color:#64748B;text-align:center;padding:3rem;font-size:0.9rem}
.loading{text-align:center;padding:2rem;color:#64748B}
.error{color:#F87171;text-align:center;padding:1rem}
</style>
</head>
<body>

<h1>&#9670; HabitMind Usage</h1>
<p class="sub" id="subtitle">Loading...</p>

<div class="stats" id="stats"></div>

<div class="controls">
    <select id="fileSelect" onchange="loadFile(this.value)">
        <option value="">Select upload...</option>
    </select>
    <button class="btn" onclick="refresh()">Refresh</button>
</div>

<div id="content"><div class="loading">Loading data...</div></div>

<script>
const BASE = '';
async function api(url) {
    const r = await fetch(BASE + url);
    if (!r.ok) throw new Error(await r.text());
    return r.json();
}

function fmt(m) {
    const h = Math.floor(m / 60);
    const min = m % 60;
    return (h ? h + 'h ' : '') + min + 'm';
}

function fmtDate(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'});
}

let allFiles = [];

async function refresh() {
    document.getElementById('content').innerHTML = '<div class="loading">Loading...</div>';
    document.getElementById('stats').innerHTML = '';
    try {
        allFiles = await api('/api/data');
        const sel = document.getElementById('fileSelect');
        sel.innerHTML = '<option value="">Select upload...</option>';
        allFiles.forEach(f => {
            const opt = document.createElement('option');
            opt.value = f.filename;
            const label = f.filename.replace(/\.json$/,'').split('_').slice(1).join('_') || f.filename;
            opt.textContent = label + ' (' + (f.size/1024).toFixed(1) + ' KB)';
            sel.appendChild(opt);
        });
        if (allFiles.length > 0) {
            sel.value = allFiles[0].filename;
            loadFile(allFiles[0].filename);
        } else {
            document.getElementById('content').innerHTML = '<div class="empty">No data yet. Upload from the HabitMind app.</div>';
            document.getElementById('subtitle').textContent = 'No uploads';
        }
    } catch(e) {
        document.getElementById('content').innerHTML = '<div class="error">Failed to fetch: ' + e.message + '</div>';
    }
}

async function loadFile(filename) {
    if (!filename) {
        document.getElementById('content').innerHTML = '<div class="empty">Select a file above</div>';
        document.getElementById('stats').innerHTML = '';
        document.getElementById('subtitle').textContent = 'Select a file';
        return;
    }
    document.getElementById('content').innerHTML = '<div class="loading">Loading...</div>';
    try {
        const data = await api('/api/data/' + encodeURIComponent(filename));
        render(data);
    } catch(e) {
        document.getElementById('content').innerHTML = '<div class="error">Error: ' + e.message + '</div>';
    }
}

function render(data) {
    const apps = data.apps || [];
    const totalMin = apps.reduce((s, a) => s + (a.totalTimeInForegroundMinutes || 0), 0);
    const maxMin = apps.length > 0 ? apps[0].totalTimeInForegroundMinutes || 1 : 1;

    document.getElementById('subtitle').textContent = fmtDate(data.exportedAt) + ' \u00b7 ' + data.interval;

    document.getElementById('stats').innerHTML =
        '<div class="stat"><div class="num">' + apps.length + '</div><div class="lbl">Apps tracked</div></div>' +
        '<div class="stat"><div class="num">' + fmt(totalMin) + '</div><div class="lbl">Total screen time</div></div>' +
        '<div class="stat"><div class="num">' + fmt(Math.round(totalMin / (apps.length || 1))) + '</div><div class="lbl">Avg per app</div></div>' +
        '<div class="stat"><div class="num">' + (apps.length > 0 ? apps[0].appName : '-') + '</div><div class="lbl">Most used</div></div>';

    if (apps.length === 0) {
        document.getElementById('content').innerHTML = '<div class="empty">No app usage data in this upload</div>';
        return;
    }

    const maxWidth = totalMin > 0 ? 100 : 1;
    let rows = '';
    apps.forEach((a, i) => {
        const m = a.totalTimeInForegroundMinutes || 0;
        const pct = totalMin > 0 ? (m / maxMin) * 100 : 0;
        const name = a.appName || a.packageName;
        const timeStr = fmt(m);
        rows += '<tr>' +
            '<td style="color:#64748B;width:2rem">' + (i + 1) + '</td>' +
            '<td class="name">' + name + '</td>' +
            '<td class="bar"><div class="bar-fill" style="width:' + pct + '%"></div></td>' +
            '<td class="time">' + timeStr + '</td>' +
            '</tr>';
    });

    document.getElementById('content').innerHTML =
        '<table><thead><tr><th>#</th><th>App</th><th></th><th>Time</th></tr></thead><tbody>' + rows + '</tbody></table>';
}

refresh();
</script>

</body>
</html>`);
});

app.listen(PORT, () => {
    console.log(`HabitMind Usage Server running on port ${PORT}`);
    if (AUTH_TOKEN) console.log(`Auth enabled, set AUTH_TOKEN env var on server`);
});
