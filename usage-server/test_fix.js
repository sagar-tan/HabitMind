// Simulate what the template literal produces
// The template source line from index.js line 343:
// src=\\''
// In template literal: \\ -> \, '' -> ''
// Output: \''

const tmpl = `src=\\''+BASE+`;
console.log("Template eval result:", JSON.stringify(tmpl));
console.log("Chars:", Array.from(tmpl).map(c => c + "(" + c.charCodeAt(0) + ")"));

// Now test in a full JS statement context
const context = `let html = ''; const BASE = 'http://x.com'; const d = ''; const f = { filename: 'test.jpg' }; `;

// The FULL line from the output (simulating template eval)
const statement = `html+='<div class="ss-card" onclick="document.getElementById(\\'modal\\').style.display=\\'flex\\';document.getElementById(\\'modalImg\\').src=\\''+BASE+'/api/screenshots/'+f.filename+'\\'"><img src="'+BASE+'/api/screenshots/'+f.filename+'" loading="lazy"><div class="ss-info">'+d+'</div></div>';`;

console.log("\nTesting full statement...");
console.log("Statement:", statement.substring(0, 120) + "...");
try {
    eval(context + statement);
    console.log("PARSE OK");
    // Show what html was built
    const html = eval(context + statement.replace('html+=', 'html+=') + '; html');
    console.log("HTML built:", html);
} catch(e) {
    console.log("ERROR:", e.message);
}
