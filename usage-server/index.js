const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;
const DATA_DIR = path.join(__dirname, 'data');

if (!fs.existsSync(DATA_DIR)) fs.mkdirSync(DATA_DIR, { recursive: true });

app.use(express.json({ limit: '10mb' }));

// Simple auth via shared secret (optional)
const AUTH_TOKEN = process.env.AUTH_TOKEN || null;

function auth(req, res, next) {
    if (!AUTH_TOKEN) return next();
    const token = req.headers['authorization'];
    if (token !== `Bearer ${AUTH_TOKEN}`) {
        return res.status(401).json({ error: 'Unauthorized' });
    }
    next();
}

// Upload usage data
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

// List all uploads
app.get('/api/data', (req, res) => {
    const files = fs.readdirSync(DATA_DIR)
        .filter(f => f.endsWith('.json'))
        .sort()
        .reverse()
        .map(f => {
            const stat = fs.statSync(path.join(DATA_DIR, f));
            return { filename: f, size: stat.size, uploadedAt: stat.mtime.toISOString() };
        });
    res.json(files);
});

// Download specific data file
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

// Delete specific data file
app.delete('/api/data/:filename', auth, (req, res) => {
    const filename = path.basename(req.params.filename);
    const filepath = path.join(DATA_DIR, filename);
    if (!fs.existsSync(filepath)) {
        return res.status(404).json({ error: 'Not found' });
    }
    fs.unlinkSync(filepath);
    res.json({ status: 'deleted', filename });
});

// Simple web UI
app.get('/', (req, res) => {
    const files = fs.readdirSync(DATA_DIR)
        .filter(f => f.endsWith('.json'))
        .sort()
        .reverse();

    res.send(`<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>HabitMind Usage Server</title>
<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
       background: #0F0F12; color: #E2E2E7; padding: 2rem; }
h1 { color: #818CF8; margin-bottom: 0.5rem; }
p { color: #94949E; margin-bottom: 2rem; }
table { width: 100%; border-collapse: collapse; }
th, td { text-align: left; padding: 0.75rem 1rem; border-bottom: 1px solid #24242A; }
th { color: #818CF8; font-weight: 600; }
td { color: #E2E2E7; }
a { color: #818CF8; text-decoration: none; }
a:hover { text-decoration: underline; }
.empty { color: #64748B; text-align: center; padding: 3rem; }
.badge { background: #818CF8; color: #fff; border-radius: 999px; padding: 0.15rem 0.6rem; font-size: 0.75rem; }
</style>
</head>
<body>
<h1>&#9670; HabitMind Usage Server</h1>
<p>App usage data collected from your device.</p>
${files.length === 0 ? '<div class="empty">No data uploaded yet. Open the app and sync your usage data.</div>'
: `<table><thead><tr><th>File</th><th>Size</th><th>Uploaded</th><th></th></tr></thead><tbody>
${files.map(f => {
    const stat = fs.statSync(path.join(DATA_DIR, f));
    const sizeKB = (stat.size / 1024).toFixed(1);
    return `<tr><td><a href="/api/data/${f}">${f.replace(/\.json$/, '')}</a></td>
            <td><span class="badge">${sizeKB} KB</span></td>
            <td>${stat.mtime.toLocaleString()}</td>
            <td><a href="/api/data/${f}" download>Download</a></td></tr>`;
}).join('')}
</tbody></table>`}
</body></html>`);
});

app.listen(PORT, () => {
    console.log(`HabitMind Usage Server running on port ${PORT}`);
    if (AUTH_TOKEN) console.log(`Auth enabled, token: ${AUTH_TOKEN}`);
});
