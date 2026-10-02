// FoodWell AI proxy — Cloudflare Worker
// Receives a food photo from the app, asks Gemini to identify it, and returns
// { name, candidates, confidence, kcal, protein, carbs, fat, fiber, sugar, sodium }.
// The Gemini API key stays here as a secret; it is never shipped inside the APK.
//
// Settings (Workers → Settings → Variables and Secrets):
//   GEMINI_API_KEY  (Secret, required)  key from https://aistudio.google.com/apikey
//   APP_TOKEN       (Secret, optional)  if set, the app URL must end with ?k=<APP_TOKEN>
//   GEMINI_MODEL    (Text,   optional)  override the model, e.g. gemini-2.5-flash
//   HUAWEI_CLIENT_ID / HUAWEI_CLIENT_SECRET  (Secrets, optional) enable /huawei/* (Huawei Health Kit)
//   HUAWEI_SCOPES   (Text,   optional)  space-separated scopes, overrides the defaults below

const DEFAULT_MODELS = ["gemini-flash-latest", "gemini-2.5-flash"];
const MAX_IMAGE_BYTES = 8 * 1024 * 1024;

const CORS = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "POST, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type",
  "Access-Control-Max-Age": "86400",
};

const PROMPT = `You are a nutrition assistant for a Thai food-logging app.
Look at the photo and identify the main dish or food item.
- "name": the most likely dish name in Thai (e.g. "ข้าวกะเพราไก่ไข่ดาว"). If it is not food, use "".
- "candidates": up to 4 alternative Thai names, most likely first (include "name" first).
- "confidence": 0 to 1.
- Estimate nutrition for the whole portion visible: kcal, protein (g), carbs (g), fat (g),
  fiber (g), sugar (g), sodium (mg). These are rough estimates; round to whole numbers.
- "is_food": false if the photo does not show food or drink.`;

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

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: CORS });
    const path = new URL(request.url).pathname;
    if (path.startsWith("/huawei/")) return huawei(request, env, path);
    if (request.method !== "POST") return json({ error: "ใช้ POST เท่านั้น" }, 405);

    if (env.APP_TOKEN && new URL(request.url).searchParams.get("k") !== env.APP_TOKEN) {
      return json({ error: "token ไม่ถูกต้อง" }, 401);
    }
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
        return json({ error: `Gemini ตอบกลับ ${resp.status}`, detail: detail.slice(0, 300) }, 502);
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

// ---------------------------------------------------------------------------------------------
// Huawei Health Kit (read today's steps / calories / heart rate from the user's Huawei account)
//
//   GET  /huawei/login?k=APP_TOKEN  → redirects to Huawei ID sign-in
//   GET  /huawei/callback           → exchanges the code (needs the client secret, so it lives here)
//                                     and hands the refresh token back to the app (foodwell://huawei)
//   POST /huawei/today?k=APP_TOKEN  {refresh_token, day:"yyyyMMdd", timeZone:"+0700"}
//                                   → {steps, activeKcal, heart, refresh_token?}
//
// Health Kit field names are matched loosely (e.g. any numeric field containing "step"), because
// Huawei's daily summaries name them differently per data type; `debug` carries a raw snippet when
// a value can't be found so the mapping can be fixed here without rebuilding the app.

const HW_AUTH = "https://oauth-login.cloud.huawei.com/oauth2/v3/authorize";
const HW_TOKEN = "https://oauth-login.cloud.huawei.com/oauth2/v3/token";
const HW_API = "https://health-api.cloud.huawei.com/healthkit/v2";
const HW_SCOPES = [
  "openid",
  "https://www.huawei.com/healthkit/step.read",
  "https://www.huawei.com/healthkit/calories.read",
  "https://www.huawei.com/healthkit/heartrate.read",
];
const HW_TYPES = {
  steps: "com.huawei.continuous.steps.delta",
  activeKcal: "com.huawei.continuous.calories.burnt",
  heart: "com.huawei.instantaneous.heart_rate",
};

async function huawei(request, env, path) {
  const url = new URL(request.url);
  if (!env.HUAWEI_CLIENT_ID || !env.HUAWEI_CLIENT_SECRET) {
    return json({ error: "ยังไม่ได้ตั้งค่า HUAWEI_CLIENT_ID / HUAWEI_CLIENT_SECRET ใน Worker" }, 500);
  }
  const callback = `${url.origin}/huawei/callback`;

  if (path === "/huawei/login") {
    if (env.APP_TOKEN && url.searchParams.get("k") !== env.APP_TOKEN) return json({ error: "token ไม่ถูกต้อง" }, 401);
    const q = new URLSearchParams({
      response_type: "code",
      access_type: "offline",
      client_id: env.HUAWEI_CLIENT_ID,
      redirect_uri: callback,
      scope: (env.HUAWEI_SCOPES || HW_SCOPES.join(" ")),
      state: crypto.randomUUID(),
    });
    return Response.redirect(`${HW_AUTH}?${q}`, 302);
  }

  if (path === "/huawei/callback") {
    const code = url.searchParams.get("code");
    if (!code) return page(`Huawei ไม่อนุญาต: ${url.searchParams.get("error_description") || url.searchParams.get("error") || "ไม่มี code"}`);
    const tok = await hwToken(env, { grant_type: "authorization_code", code, redirect_uri: callback });
    if (!tok.refresh_token) return page(`แลก token ไม่สำเร็จ: ${tok.error_description || tok.error || JSON.stringify(tok).slice(0, 200)}`);
    const back = `foodwell://huawei?refresh_token=${encodeURIComponent(tok.refresh_token)}`;
    return new Response(
      `<!doctype html><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;text-align:center;padding:40px">
       <h2>เชื่อมต่อ Huawei Health แล้ว ✓</h2><p><a href="${back}" style="font-size:20px">กลับไปที่ FoodWell</a></p>
       <script>location.href=${JSON.stringify(back)}</script></body>`,
      { headers: { "Content-Type": "text/html; charset=utf-8" } }
    );
  }

  if (path === "/huawei/today" && request.method === "POST") {
    if (env.APP_TOKEN && url.searchParams.get("k") !== env.APP_TOKEN) return json({ error: "token ไม่ถูกต้อง" }, 401);
    let body;
    try { body = await request.json(); } catch { return json({ error: "ข้อมูลไม่ใช่ JSON" }, 400); }
    if (!body.refresh_token) return json({ error: "ไม่มี refresh_token · เชื่อมต่อ Huawei ใหม่" }, 400);
    const tok = await hwToken(env, { grant_type: "refresh_token", refresh_token: body.refresh_token });
    if (!tok.access_token) return json({ error: "สิทธิ์ Huawei หมดอายุ · กดเชื่อมต่อ Huawei ใหม่", detail: tok.error_description || tok.error }, 401);

    const day = /^\d{8}$/.test(body.day || "") ? body.day : null;
    const timeZone = /^[+-]\d{4}$/.test(body.timeZone || "") ? body.timeZone : "+0700";
    if (!day) return json({ error: "day ต้องเป็น yyyyMMdd" }, 400);

    const out = { syncedAt: new Date().toISOString(), deviceName: "Huawei Health", sources: {} };
    const debug = {};
    for (const [key, type] of Object.entries(HW_TYPES)) {
      const r = await fetch(`${HW_API}/sampleSet:dailyPolymerize`, {
        method: "POST",
        headers: { Authorization: `Bearer ${tok.access_token}`, "Content-Type": "application/json", "x-client-id": env.HUAWEI_CLIENT_ID },
        body: JSON.stringify({ dataTypes: [type], startDay: day, endDay: day, timeZone }),
      });
      const text = await r.text();
      if (!r.ok) { debug[key] = `HTTP ${r.status}: ${text.slice(0, 200)}`; continue; }
      let data; try { data = JSON.parse(text); } catch { debug[key] = text.slice(0, 200); continue; }
      const fields = collectFields(data);
      const pick = (...names) => { for (const n of names) for (const f of fields) if (f.name.toLowerCase().includes(n)) return f.value; return null; };
      let v = null;
      if (key === "steps") v = pick("step");
      if (key === "activeKcal") v = pick("calorie", "calories");
      if (key === "heart") v = pick("last", "avg", "bpm");
      if (v == null) { if (fields.length) debug[key] = fields.slice(0, 6); else debug[key] = text.slice(0, 200); continue; }
      out[key] = Math.round(v);
      out.sources[key] = ["com.huawei.health"];
    }
    if (tok.refresh_token && tok.refresh_token !== body.refresh_token) out.refresh_token = tok.refresh_token;
    if (Object.keys(debug).length) out.debug = debug;
    return json(out);
  }

  return json({ error: "ไม่พบเส้นทางนี้" }, 404);
}

async function hwToken(env, params) {
  const r = await fetch(HW_TOKEN, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({ ...params, client_id: env.HUAWEI_CLIENT_ID, client_secret: env.HUAWEI_CLIENT_SECRET }),
  });
  try { return await r.json(); } catch { return { error: `HTTP ${r.status}` }; }
}

/** Flattens group[].sampleSet[].samplePoints[].value[] into [{name, value}], summing duplicates. */
function collectFields(data) {
  const totals = new Map();
  const walk = (o) => {
    if (Array.isArray(o)) return o.forEach(walk);
    if (!o || typeof o !== "object") return;
    if (typeof o.fieldName === "string") {
      const v = [o.integerValue, o.longValue, o.floatValue, o.doubleValue].map(Number).find(Number.isFinite);
      if (v != null) totals.set(o.fieldName, (totals.get(o.fieldName) || 0) + v);
    }
    Object.values(o).forEach(walk);
  };
  walk(data);
  return [...totals].map(([name, value]) => ({ name, value }));
}

const page = (msg) =>
  new Response(`<!doctype html><meta name="viewport" content="width=device-width"><body style="font-family:sans-serif;padding:32px"><h3>FoodWell · Huawei</h3><p>${msg.replace(/</g, "&lt;")}</p></body>`,
    { status: 400, headers: { "Content-Type": "text/html; charset=utf-8" } });
