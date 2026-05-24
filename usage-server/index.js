const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;
const AUTH_TOKEN = process.env.AUTH_TOKEN || null;
const DATA_DIR = path.join(__dirname, 'data');
const MAX_SUMMARY_APPS = 15;

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
    if (!fs.existsSync(filepath)) {
        return res.status(404).json({ error: 'Not found' });
    }
    res.setHeader('Content-Type', 'application/json');
    res.setHeader('Content-Disposition', `attachment; filename="${filename}"`);
    res.sendFile(filepath);
});

app.delete('/api/data/:filename', auth, (req, res) => {
    const filename = path.basename(req.params.filename);
    const filepath = path.join(DATA_DIR, filename);
    if (!fs.existsSync(filepath)) return res.status(404).json({ error: 'Not found' });
    fs.unlinkSync(filepath);
    res.json({ status: 'deleted', filename });
});

// Latest summary — top apps from the most recent upload
app.get('/api/summary', (_req, res) => {
    const files = fs.readdirSync(DATA_DIR)
        .filter(f => f.endsWith('.json'))
        .sort().reverse();
    if (files.length === 0) return res.json({ exists: false });

    const raw = fs.readFileSync(path.join(DATA_DIR, files[0]), 'utf-8');
    try {
        const data = JSON.parse(raw);
        const top = (data.apps || []).slice(0, MAX_SUMMARY_APPS);
        res.json({
            exists: true,
            exportedAt: data.exportedAt,
            totalApps: (data.apps || []).length,
            totalMinutes: top.reduce((s, a) => s + (a.totalTimeInForegroundMinutes || 0), 0),
            topApps: top.map(a => ({
                name: a.appName,
                minutes: a.totalTimeInForegroundMinutes || 0
            }))
        });
    } catch { res.json({ exists: false }); }
});

app.get('/', (_req, res) => {
    const files = fs.readdirSync(DATA_DIR)
        .filter(f => f.endsWith('.json'))
        .sort().reverse();

    const summaryRows = (() => {
        if (files.length === 0) return '';
        const raw = fs.readFileSync(path.join(DATA_DIR, files[0]), 'utf-8');
        try {
            const data = JSON.parse(raw);
            const top = (data.apps || []).slice(0, MAX_SUMMARY_APPS);
            return `<div style="color:#94949E;margin-bottom:1.5rem;font-size:0.9rem">
                Latest upload: <strong style="color:#E2E2E7">${top.length} apps</strong> —
                <strong style="color:#E2E2E7">${top.reduce((s,a)=>s+(a.totalTimeInForegroundMinutes||0),0)} min</strong> total screen time
                &middot; ${data.exportedAt ? new Date(data.exportedAt).toLocaleString() : ''}
            </div>
            <table><thead><tr><th>#</th><th>App</th><th>Time</th></tr></thead><tbody>
            ${top.map((a,i) => {
                const h = Math.floor((a.totalTimeInForegroundMinutes||0)/60);
                const m = (a.totalTimeInForegroundMinutes||0)%60;
                return `<tr><td style="color:#64748B">${i+1}</td>
                    <td>${a.appName}</td>
                    <td>${h > 0 ? h+'h ' : ''}${m}m</td></tr>`;
            }).join('')}
            </tbody></table>`;
        } catch { return ''; }
    })();

    res.send(`<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>HabitMind Usage Server</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#0F0F12;color:#E2E2E7;padding:2rem}
h1{color:#818CF8;margin-bottom:0.25rem}
.sub{color:#64748B;margin-bottom:1rem;font-size:0.85rem}
a{color:#818CF8;text-decoration:none}
a:hover{text-decoration:underline}
table{width:100%;border-collapse:collapse;margin-bottom:2rem}
th,td{text-align:left;padding:0.6rem 0.75rem;border-bottom:1px solid #24242A}
th{color:#818CF8;font-weight:600;font-size:0.8rem;text-transform:uppercase;letter-spacing:0.05em}
td{color:#E2E2E7}
.empty{color:#64748B;text-align:center;padding:3rem}
.badge{background:#818CF8;color:#fff;border-radius:999px;padding:0.15rem 0.6rem;font-size:0.75rem}
.nav{display:flex;gap:0.5rem;margin-bottom:2rem}
.nav a{padding:0.4rem 1rem;border-radius:8px;background:#1E1E24;font-size:0.85rem;border:1px solid #24242A}
.nav a:hover{background:#24242A}
.file-list td:last-child{text-align:right}
</style>
</head>
<body>
<h1>&#9670; HabitMind Usage</h1>
<p class="sub">Your personal app usage tracking server</p>

<div class="nav">
    <a href="/">Dashboard</a>
    <a href="/files">All Files</a>
</div>

${summaryRows || '<div class="empty">No data uploaded yet. Open the app &#8594; Settings &#8594; Usage Data Sync &#8594; Upload Now.</div>'}
</body></html>`);
});

app.get('/files', (_req, res) => {
    const files = fs.readdirSync(DATA_DIR)
        .filter(f => f.endsWith('.json'))
        .sort().reverse();

    const rows = files.length === 0
        ? '<div class="empty">No files</div>'
        : `<table><thead><tr><th>File</th><th>Size</th><th>Uploaded</th><th></th></tr></thead><tbody>
${files.map(f => {
    const stat = fs.statSync(path.join(DATA_DIR, f));
    const sizeKB = (stat.size / 1024).toFixed(1);
    return `<tr><td><a href="/api/data/${f}">${f.replace(/\.json$/, '').slice(0, 50)}</a></td>
            <td><span class="badge">${sizeKB} KB</span></td>
            <td style="font-size:0.85rem;color:#94949E">${stat.mtime.toLocaleString()}</td>
            <td><a href="/api/data/${f}" download>Download</a></td></tr>`;
}).join('')}
</tbody></table>`;

    res.send(`<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>Files — HabitMind Usage</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;background:#0F0F12;color:#E2E2E7;padding:2rem}
h2{color:#818CF8;margin-bottom:1rem}
a{color:#818CF8;text-decoration:none}
a:hover{text-decoration:underline}
table{width:100%;border-collapse:collapse}
th,td{text-align:left;padding:0.6rem 0.75rem;border-bottom:1px solid #24242A}
th{color:#818CF8;font-weight:600;font-size:0.8rem;text-transform:uppercase;letter-spacing:0.05em}
td{color:#E2E2E7}
.empty{color:#64748B;text-align:center;padding:3rem}
.badge{background:#818CF8;color:#fff;border-radius:999px;padding:0.15rem 0.6rem;font-size:0.75rem}
.nav{margin-bottom:2rem}
.nav a{padding:0.4rem 1rem;border-radius:8px;background:#1E1E24;font-size:0.85rem;border:1px solid #24242A}
</style>
</head>
<body>
<div class="nav"><a href="/">&#8592; Dashboard</a></div>
<h2>All Uploaded Files</h2>
${rows}
</body></html>`);
});

app.listen(PORT, () => {
    console.log(`HabitMind Usage Server running on port ${PORT}`);
    if (AUTH_TOKEN) console.log(`Auth enabled, set AUTH_TOKEN env var on server`);
});
