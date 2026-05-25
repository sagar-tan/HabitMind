
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
    try {
        allFiles = await api('/api/data');
        [document.getElementById('fileSelect'), document.getElementById('tlFileSelect')].forEach(sel => {
            sel.innerHTML = '<option value="">Latest upload</option>';
            allFiles.forEach(f => {
                const o=document.createElement('option'); o.value=f.filename;
                o.textContent=f.filename.replace(/.json$/,'').split('_').slice(1).join('_')||f.filename;
                sel.appendChild(o);
            });
        });
        if (allFiles.length > 0) {
            loadFile('');
            loadTimeline('');
        } else {
            document.getElementById('subtitle').textContent = 'No uploads yet';
            document.getElementById('stats').innerHTML = '';
            document.getElementById('tlStats').innerHTML = '';
            document.getElementById('appsContent').innerHTML = '<div class="empty">No usage uploads yet. Open the app, grant the permissions, and relaunch once so it syncs.</div>';
            document.getElementById('timelineContent').innerHTML = '<div class="empty">No timeline data yet.</div>';
        }
    } catch (e) {
        document.getElementById('subtitle').textContent = 'Failed to load server data';
        document.getElementById('appsContent').innerHTML = '<div class="empty">Error loading uploads.</div>';
        document.getElementById('timelineContent').innerHTML = '<div class="empty">Error loading uploads.</div>';
    }
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

function fmtUptime(sec) {
    const h=Math.floor(sec/3600); const m=Math.floor((sec%3600)/60); const s=sec%60;
    return (h?h+'h ':'')+(m?m+'m ':'')+s+'s';
}

async function loadScreenshots() {
    document.getElementById('screenshotsContent').innerHTML='<div class="loading">Loading...</div>';
    document.getElementById('ssStats').innerHTML='<div class="loading">Loading...</div>';
    try {
        const health=await api('/api/health');
        document.getElementById('ssStats').innerHTML=
            '<div class="stat"><div class="num">'+health.totalScreenshots+'</div><div class="lbl">Screenshots</div></div>'+
            '<div class="stat"><div class="num">'+health.totalUploads+'</div><div class="lbl">Usage uploads</div></div>'+
            '<div class="stat"><div class="num">'+(health.lastScreenshotTime?fmtShort(health.lastScreenshotTime):'-')+'</div><div class="lbl">Last screenshot</div></div>'+
            '<div class="stat"><div class="num">'+fmtUptime(health.uptimeSeconds)+'</div><div class="lbl">Server uptime</div></div>';
    } catch(e) { document.getElementById('ssStats').innerHTML='<div class="empty">Could not load stats</div>'; }

    try {
        const files=await api('/api/screenshots');
        if(files.length===0){document.getElementById('screenshotsContent').innerHTML='<div class="empty">No screenshots yet</div>';return;}
        let html='<div class="screenshots">';
        files.forEach(f=>{
            const ts=f.filename.replace(/^ss_/,'').split('_')[0];
            const d=ts?new Date(parseInt(ts)).toLocaleString():'';
            html+='<div class="ss-card" onclick="document.getElementById(\'modal\').style.display=\'flex\';document.getElementById(\'modalImg\').src=''+BASE+'/api/screenshots/'+f.filename+'\'"><img src="'+BASE+'/api/screenshots/'+f.filename+'" loading="lazy"><div class="ss-info">'+d+'</div></div>';
        });
        html+='</div>';
        document.getElementById('screenshotsContent').innerHTML=html;
    } catch(e) { document.getElementById('screenshotsContent').innerHTML='<div class="empty">Error: '+e.message+'</div>'; }
}

refreshFiles();
