// FoodWell AI proxy — Cloudflare Worker
// Receives a food photo from the app, asks Gemini to identify it, and returns
// { name, candidates, confidence, kcal, protein, carbs, fat, fiber, sugar, sodium }.
// The Gemini API key stays here as a secret; it is never shipped inside the APK.
//
// Settings (Workers → Settings → Variables and Secrets):
//   GEMINI_API_KEY  (Secret, required)  key from https://aistudio.google.com/apikey
//   APP_TOKEN       (Secret, optional)  if set, the app URL must end with ?k=<APP_TOKEN>
//   GEMINI_MODEL    (Text,   optional)  override the model, e.g. gemini-2.5-flash

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

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: CORS });
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

