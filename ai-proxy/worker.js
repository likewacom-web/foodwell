// FoodWell AI proxy — Cloudflare Worker
// Receives a food photo from the app, asks Gemini to identify it, and returns
// { name, candidates, confidence, kcal, protein, carbs, fat, fiber, sugar, sodium }.
// The Gemini API key stays here as a secret; it is never shipped inside the APK.
//
// Settings (Workers → Settings → Variables and Secrets):
//   GEMINI_API_KEY  (Secret, required)  key from https://aistudio.google.com/apikey
//   APP_TOKEN       (Secret, optional)  if set, the app URL must end with ?k=<APP_TOKEN>
//   GEMINI_MODEL    (Text,   optional)  override the model, e.g. gemini-2.5-flash
//   ADMIN_TOKEN     (Secret, for codes) password for the /admin page that makes activation codes
//   CODES           (KV namespace binding, for codes) where activation codes are stored

const DEFAULT_MODELS = ["gemini-flash-latest", "gemini-2.5-flash"];
const MAX_IMAGE_BYTES = 8 * 1024 * 1024;

const CORS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization",
  "Access-Control-Max-Age": "86400",
};

const PROMPT = `You are a nutrition assistant for a Thai food-logging app.
Look at the photo and identify the main dish or food item.
- "name": the most likely dish name in Thai (e.g. "ข้าวกะเพราไก่ไข่ดาว"). If it is not food, use "".
- "candidates": up to 4 alternative Thai names, most likely first (include "name" first).
- "confidence": 0 to 1.
- "is_food": false if the photo does not show food or drink.
- Estimate nutrition for the whole portion visible: kcal, protein (g), carbs (g), fat (g),
  fiber (g), sugar (g), sodium (mg). Round to whole numbers.
How to estimate (work through these steps before answering):
1. List every component on the plate or in the pack separately (rice, meat, egg, sauce, bread, drink …).
2. Judge each portion size from visible references: plate or bowl size (Thai plate ≈ 23 cm),
   spoon, hand, packaging. A Thai street-food plate of rice is about 150–200 g cooked rice.
3. Count hidden calories that Thai cooking usually adds: frying oil (stir-fry ≈ 1–2 tbsp),
   sugar in sauces and drinks, coconut milk in curries, mayonnaise and ketchup.
4. Use typical Thai street-food or 7-Eleven recipes rather than home-cooked or Western ones.
   If the item is packaged and a label is readable, use the label.
5. Sum the components; macros must roughly agree with kcal (4·protein + 4·carbs + 9·fat ≈ kcal).`;

const SCHEMA = {
  type: "OBJECT",
  properties: {
    is_food: { type: "BOOLEAN" },
    name: { type: "STRING" },
    candidates: { type: "ARRAY", items: { type: "STRING" } },
    confidence: { type: "NUMBER" },
    kcal: { type: "NUMBER" },
    protein: { type: "NUMBER" },
    carbs: { type: "NUMBER" },
    fat: { type: "NUMBER" },
    fiber: { type: "NUMBER" },
    sugar: { type: "NUMBER" },
    sodium: { type: "NUMBER" },
  },
  required: ["is_food", "name", "candidates", "confidence"],
};

const json = (body, status = 200) =>
  new Response(JSON.stringify(body), {
    status,
    headers: { ...CORS, "Content-Type": "application/json; charset=utf-8" },
  });

// Turns Gemini's error into a short Thai reason the app can show, so a broken key or setting is obvious.
function explain(status, detail) {
  let msg = "";
  try {
    msg = JSON.parse(detail)?.error?.message || "";
  } catch {}
  if (/API key not valid|API_KEY_INVALID/i.test(detail)) return "คีย์ Gemini ไม่ถูกต้อง · สร้างคีย์ใหม่แล้วใส่ใน GEMINI_API_KEY";
  if (/expired/i.test(msg)) return "คีย์ Gemini หมดอายุ · สร้างคีย์ใหม่แล้วใส่ใน GEMINI_API_KEY";
  if (/location is not supported/i.test(msg)) return "Gemini ไม่รองรับตำแหน่งของเซิร์ฟเวอร์ตอนนี้ · ลองใหม่อีกครั้ง";
  if (/image/i.test(msg)) return "Gemini อ่านรูปนี้ไม่ได้ · ลองถ่ายใหม่";
  return `Gemini ตอบกลับ ${status}${msg ? ": " + msg.slice(0, 120) : ""}`;
}

// ---- Activation codes ---------------------------------------------------------------------
// A code turns on the app's full version (no ads, unlimited AI) for its number of days (0 = for good).
// Each code binds to the first phone that redeems it; the same phone may redeem it again after a
// reinstall. Codes are made and listed on the /admin page.

const DAY = 86400000;
const ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no 0/O, 1/I; 32 letters so bytes map without bias

function newCode() {
  const s = Array.from(crypto.getRandomValues(new Uint8Array(8)), (b) => ALPHABET[b % 32]).join("");
  return `MEOW-${s.slice(0, 4)}-${s.slice(4)}`;
}

// "meow-abcd-efgh", "ABCDEFGH" and "MEOW ABCD EFGH" all mean the same code.
function codeKey(raw) {
  let s = String(raw || "").toUpperCase().replace(/[^A-Z0-9]/g, "");
  if (s.length === 8) s = "MEOW" + s;
  return s.length === 12 ? "code:" + s : "";
}

const pretty = (key) => key.slice(5, 9) + "-" + key.slice(9, 13) + "-" + key.slice(13);

async function redeem(request, env) {
  if (!env.CODES) return json({ error: "เซิร์ฟเวอร์ยังไม่ได้ตั้งค่าโค้ด (KV: CODES)" }, 500);
  let body;
  try {
    body = await request.json();
  } catch {
    return json({ error: "ข้อมูลที่ส่งมาไม่ใช่ JSON" }, 400);
  }
  const key = codeKey(body.code);
  const device = String(body.device || "");
  if (!key) return json({ error: "รูปแบบโค้ดไม่ถูกต้อง" }, 400);
  if (!/^[a-f0-9]{16,64}$/.test(device)) return json({ error: "ข้อมูลเครื่องไม่ถูกต้อง" }, 400);

  const rec = await env.CODES.get(key, "json");
  if (!rec) return json({ error: "ไม่พบโค้ดนี้ · ตรวจตัวอักษรอีกครั้ง" }, 404);
  if (rec.revoked) return json({ error: "โค้ดนี้ถูกยกเลิกแล้ว" }, 410);
  if (rec.device && rec.device !== device) return json({ error: "โค้ดนี้ถูกใช้กับเครื่องอื่นแล้ว" }, 409);
  if (rec.device === device) return json({ until: rec.until, forever: rec.days === 0, days: rec.days, again: true });

  const now = Date.now();
  if (rec.validUntil && now > rec.validUntil) return json({ error: "โค้ดนี้หมดอายุแล้ว (เลยวันที่ต้องใช้)" }, 410);
  rec.device = device;
  rec.redeemedAt = now;
  rec.until = rec.days === 0 ? 0 : now + rec.days * DAY;
  await env.CODES.put(key, JSON.stringify(rec));
  return json({ until: rec.until, forever: rec.days === 0, days: rec.days });
}

async function admin(request, env, path) {
  if (request.method === "GET") return new Response(ADMIN_HTML, { headers: { "Content-Type": "text/html; charset=utf-8" } });
  if (!env.ADMIN_TOKEN) return json({ error: "ยังไม่ได้ตั้ง ADMIN_TOKEN ใน Worker" }, 500);
  if (request.headers.get("Authorization") !== "Bearer " + env.ADMIN_TOKEN) return json({ error: "รหัสผู้ดูแลไม่ถูกต้อง" }, 401);
  if (!env.CODES) return json({ error: "ยังไม่ได้ผูก KV ชื่อ CODES กับ Worker" }, 500);
  let body = {};
  try {
    body = await request.json();
  } catch {}

  if (path === "/admin/create") {
    const days = Math.max(0, Math.min(3650, Math.floor(Number(body.days) || 0)));
    const count = Math.max(1, Math.min(100, Math.floor(Number(body.count) || 1)));
    const validDays = Math.max(0, Math.floor(Number(body.validDays) || 0));
    const note = String(body.note || "").slice(0, 80);
    const now = Date.now();
    const codes = [];
    while (codes.length < count) {
      const code = newCode();
      const key = codeKey(code);
      if (await env.CODES.get(key)) continue;
      await env.CODES.put(key, JSON.stringify({ days, note, created: now, validUntil: validDays ? now + validDays * DAY : 0 }));
      codes.push(code);
    }
    return json({ codes, days, validDays, note });
  }

  if (path === "/admin/list") {
    const out = [];
    let cursor;
    do {
      const page = await env.CODES.list({ prefix: "code:", cursor });
      for (const k of page.keys) {
        const rec = (await env.CODES.get(k.name, "json")) || {};
        out.push({ code: pretty(k.name), ...rec, device: rec.device ? rec.device.slice(0, 6) + "…" : "" });
      }
      cursor = page.list_complete ? null : page.cursor;
    } while (cursor && out.length < 500);
    out.sort((a, b) => (b.created || 0) - (a.created || 0));
    return json({ codes: out });
  }

  if (path === "/admin/revoke") {
    const key = codeKey(body.code);
    const rec = key && (await env.CODES.get(key, "json"));
    if (!rec) return json({ error: "ไม่พบโค้ดนี้" }, 404);
    rec.revoked = true;
    await env.CODES.put(key, JSON.stringify(rec));
    return json({ ok: true });
  }
  return json({ error: "ไม่รู้จักคำสั่งนี้" }, 404);
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: CORS });
    const path = new URL(request.url).pathname.replace(/\/+$/, "");
    if (path === "/admin" || path.startsWith("/admin/")) return admin(request, env, path);
    if (request.method !== "POST") return json({ error: "ใช้ POST เท่านั้น" }, 405);

    if (env.APP_TOKEN && new URL(request.url).searchParams.get("k") !== env.APP_TOKEN) {
      return json({ error: "token ไม่ถูกต้อง" }, 401);
    }
    if (path === "/redeem") return redeem(request, env);
    if (!env.GEMINI_API_KEY) return json({ error: "ยังไม่ได้ตั้งค่า GEMINI_API_KEY" }, 500);

    let image;
    try {
      ({ image } = await request.json());
    } catch {
      return json({ error: "ข้อมูลที่ส่งมาไม่ใช่ JSON" }, 400);
    }
    const m = /^data:(image\/[a-z0-9.+-]+);base64,(.+)$/i.exec(String(image || ""));
    if (!m) return json({ error: "ต้องส่ง image เป็น data:image/...;base64" }, 400);
    const [, mimeType, data] = m;
    if (data.length * 0.75 > MAX_IMAGE_BYTES) return json({ error: "รูปใหญ่เกินไป" }, 413);

    const body = {
      contents: [{ parts: [{ inlineData: { mimeType, data } }, { text: PROMPT }] }],
      generationConfig: { responseMimeType: "application/json", responseSchema: SCHEMA, temperature: 0.2 },
    };

    const models = env.GEMINI_MODEL ? [env.GEMINI_MODEL] : DEFAULT_MODELS;
    let lastError = "";
    for (const model of models) {
      const resp = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json", "x-goog-api-key": env.GEMINI_API_KEY },
          body: JSON.stringify(body),
        }
      );
      if (resp.status === 404) {
        // Model name retired or renamed — try the next one.
        lastError = `ไม่พบโมเดล ${model}`;
        continue;
      }
      if (resp.status === 429) return json({ error: "ใช้เกินโควตาฟรีของ Gemini แล้ว ลองใหม่ภายหลัง" }, 429);
      if (!resp.ok) {
        const detail = await resp.text();
        console.log("gemini", model, resp.status, detail.slice(0, 500));
        return json({ error: explain(resp.status, detail), detail: detail.slice(0, 300) }, 502);
      }

      const out = await resp.json();
      const text = out?.candidates?.[0]?.content?.parts?.map((p) => p.text || "").join("") || "";
      let result;
      try {
        result = JSON.parse(text);
      } catch {
        return json({ error: "อ่านผลจาก Gemini ไม่ได้" }, 502);
      }
      if (!result.is_food || !result.name) return json({ error: "ไม่พบอาหารในรูปนี้" }, 422);

      const num = (v) => (Number.isFinite(Number(v)) && Number(v) >= 0 ? Math.round(Number(v)) : null);
      return json({
        name: String(result.name),
        candidates: (Array.isArray(result.candidates) ? result.candidates : []).map(String).slice(0, 4),
        confidence: Number.isFinite(result.confidence) ? Math.max(0, Math.min(1, result.confidence)) : null,
        kcal: num(result.kcal),
        protein: num(result.protein),
        carbs: num(result.carbs),
        fat: num(result.fat),
        fiber: num(result.fiber),
        sugar: num(result.sugar),
        sodium: num(result.sodium),
        model,
      });
    }
    return json({ error: lastError || "ไม่มีโมเดลที่ใช้ได้ · ตั้ง GEMINI_MODEL ใหม่" }, 502);
  },
};


// The admin page holds no secrets: it asks for ADMIN_TOKEN and keeps it only in this browser.
const ADMIN_HTML = `<!doctype html><html lang="th"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1"><title>MeowFit · โค้ดเปิดใช้งาน</title>
<style>
 :root{--bg:#f6f7f9;--card:#fff;--text:#22242a;--muted:#62666f;--line:#e4e6eb;--accent:#c2477a}
 @media (prefers-color-scheme:dark){:root{--bg:#15161a;--card:#1e2026;--text:#eceef2;--muted:#a3a7b0;--line:#30333b;--accent:#f0a3c1}}
 body{margin:0;background:var(--bg);color:var(--text);font:15px/1.6 system-ui,sans-serif}
 main{max-width:720px;margin:0 auto;padding:16px}
 .card{background:var(--card);border:1px solid var(--line);border-radius:16px;padding:14px;margin:12px 0}
 label{display:block;font-size:13px;color:var(--muted);margin-top:8px}
 input,select{width:100%;box-sizing:border-box;padding:10px;border-radius:10px;border:1px solid var(--line);background:var(--bg);color:var(--text);font:inherit}
 button{margin-top:12px;padding:11px 16px;border:0;border-radius:12px;background:var(--accent);color:#fff;font:700 15px system-ui;cursor:pointer}
 button.ghost{background:none;color:var(--accent);border:1px solid var(--line)}
 .row{display:grid;grid-template-columns:1fr 1fr;gap:10px}
 pre{white-space:pre-wrap;font:600 15px/1.8 ui-monospace,monospace;margin:8px 0 0}
 table{width:100%;border-collapse:collapse;font-size:13px}td,th{padding:6px 4px;border-bottom:1px solid var(--line);text-align:left}
 .msg{color:var(--accent);font-weight:700;margin-top:8px}
</style></head><body><main>
<h2>🎟️ MeowFit · โค้ดเปิดใช้งานตัวเต็ม</h2>
<div class="card"><label>รหัสผู้ดูแล (ADMIN_TOKEN)</label><input id="tok" type="password" autocomplete="off"></div>
<div class="card"><b>สร้างโค้ดใหม่</b>
 <div class="row"><div><label>ใช้ได้นาน</label><select id="days">
  <option value="7">7 วัน</option><option value="30" selected>30 วัน</option><option value="90">90 วัน</option>
  <option value="180">180 วัน</option><option value="365">1 ปี</option><option value="0">ถาวร</option></select></div>
 <div><label>จำนวนโค้ด</label><input id="count" type="number" min="1" max="100" value="1"></div></div>
 <label>ต้องใช้ภายใน (วัน · 0 = ไม่จำกัด)</label><input id="valid" type="number" min="0" value="0">
 <label>บันทึกช่วยจำ เช่น ชื่อผู้รับ</label><input id="note" maxlength="80">
 <button id="create">สร้างโค้ด</button>
 <div id="out"></div></div>
<div class="card"><b>โค้ดทั้งหมด</b><br><button class="ghost" id="list">โหลดรายการ</button><div id="table"></div></div>
<script>
const $=id=>document.getElementById(id);
try{$("tok").value=localStorage.getItem("meowAdmin")||""}catch(e){}
$("tok").onchange=()=>{try{localStorage.setItem("meowAdmin",$("tok").value)}catch(e){}};
async function call(path,body){
 const r=await fetch(path,{method:"POST",headers:{"Content-Type":"application/json",Authorization:"Bearer "+$("tok").value},body:JSON.stringify(body||{})});
 const j=await r.json().catch(()=>({error:"HTTP "+r.status}));if(!r.ok)throw new Error(j.error||r.status);return j}
const esc=t=>String(t??"").replace(/[&<>"]/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;"}[c]));
const d=t=>t?new Date(t).toLocaleDateString("th-TH",{day:"numeric",month:"short",year:"2-digit"}):"";
$("create").onclick=async()=>{$("out").innerHTML="";try{
 const j=await call("/admin/create",{days:$("days").value,count:$("count").value,validDays:$("valid").value,note:$("note").value});
 const text=j.codes.join("\\n");
 $("out").innerHTML='<pre>'+esc(text)+'</pre><button class="ghost" id="copy">คัดลอกทั้งหมด</button>';
 $("copy").onclick=()=>navigator.clipboard.writeText(text).then(()=>$("copy").textContent="คัดลอกแล้ว ✓");
}catch(e){$("out").innerHTML='<div class="msg">'+esc(e.message)+'</div>'}};
$("list").onclick=async()=>{$("table").innerHTML="กำลังโหลด…";try{
 const j=await call("/admin/list");
 $("table").innerHTML='<table><tr><th>โค้ด</th><th>นาน</th><th>สถานะ</th><th>บันทึก</th><th></th></tr>'+j.codes.map(c=>
  '<tr><td><code>'+esc(c.code)+'</code></td><td>'+(c.days?c.days+" วัน":"ถาวร")+'</td><td>'
  +(c.revoked?"ยกเลิกแล้ว":c.device?"ใช้แล้ว "+d(c.redeemedAt)+(c.until?" · ถึง "+d(c.until):""):(c.validUntil&&Date.now()>c.validUntil?"หมดอายุ":"ยังไม่ใช้"))
  +'</td><td>'+esc(c.note)+'</td><td>'+(c.revoked?"":'<button class="ghost" data-r="'+esc(c.code)+'">ยกเลิก</button>')+'</td></tr>').join("")+'</table>';
 $("table").querySelectorAll("[data-r]").forEach(b=>b.onclick=async()=>{if(!confirm("ยกเลิกโค้ด "+b.dataset.r+"?"))return;
  try{await call("/admin/revoke",{code:b.dataset.r});$("list").click()}catch(e){alert(e.message)}});
}catch(e){$("table").innerHTML='<div class="msg">'+esc(e.message)+'</div>'}};
</script></main></body></html>`;
