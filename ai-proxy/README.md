# FoodWell AI proxy (ฟรี: Gemini + Cloudflare Workers)

เซิร์ฟเวอร์ตัวกลางสำหรับปุ่ม **✨ วิเคราะห์รูป** ในหน้าเพิ่มอาหาร
แอปส่งรูปมาที่นี่ → ส่งต่อให้ Gemini → ตอบกลับชื่ออาหาร (ภาษาไทย) และค่าโภชนาการโดยประมาณ
API key เก็บไว้ที่ Cloudflare เท่านั้น ไม่ได้อยู่ในแอป

ทั้งสองบริการมีแพ็กเกจฟรี ไม่ต้องผูกบัตร (เงื่อนไขของผู้ให้บริการอาจเปลี่ยนได้)
หมายเหตุ: ในแพ็กเกจฟรี Google อาจนำข้อมูลที่ส่งไปใช้ปรับปรุงบริการ

## 1. ขอ Gemini API key
1. เปิด https://aistudio.google.com/apikey แล้วล็อกอินด้วยบัญชี Google
2. กด **Create API key** แล้วคัดลอก key เก็บไว้

## 2. สร้าง Worker บน Cloudflare
1. สมัคร/ล็อกอินที่ https://dash.cloudflare.com
2. ไปที่ **Workers & Pages** → **Create** → เลือกเริ่มจาก **Hello World**
3. ตั้งชื่อ เช่น `foodwell-ai` แล้วกด **Deploy**
4. กด **Edit code** ลบโค้ดเดิมทั้งหมด แล้ววางเนื้อหาไฟล์ [`worker.js`](./worker.js) แทน → กด **Deploy**

## 3. ใส่ key (ตั้งเป็น Secret)
ใน Worker ที่สร้าง → **Settings** → **Variables and Secrets** → **Add**

| ชื่อ | ประเภท | ค่า |
|---|---|---|
| `GEMINI_API_KEY` | Secret | key จากข้อ 1 |
| `APP_TOKEN` | Secret | ตั้งรหัสเองยาว ๆ เช่น `fw-8k2m9q` (กันคนอื่นแอบใช้โควตา) |
| `GEMINI_MODEL` | Text (ไม่ใส่ก็ได้) | ระบุรุ่นเอง ถ้ารุ่นเริ่มต้นใช้ไม่ได้ เช่น `gemini-2.5-flash` |

กด **Deploy** อีกครั้งหลังเพิ่มค่า

## 4. ตั้งค่าในแอป
1. คัดลอก URL ของ Worker เช่น `https://foodwell-ai.yourname.workers.dev`
2. ในแอป FoodWell → **เพิ่มเติม** → **🤖 AI Food Recognition** → ช่อง **AI Endpoint** ใส่
   `https://foodwell-ai.yourname.workers.dev/?k=รหัส-APP_TOKEN`
3. กด **บันทึก Endpoint**
4. ทดลอง: หน้า **อาหาร** → ถ่าย/เลือกรูป → **✨ วิเคราะห์รูป**

## ข้อความผิดพลาดที่อาจเจอ
- `token ไม่ถูกต้อง` → `?k=` ในแอปไม่ตรงกับ `APP_TOKEN`
- `ยังไม่ได้ตั้งค่า GEMINI_API_KEY` → ยังไม่ได้เพิ่ม Secret หรือยังไม่กด Deploy
- `ใช้เกินโควตาฟรีของ Gemini แล้ว` → รอให้โควตารีเซ็ต (ไม่มีการเก็บเงิน)
- `ไม่พบโมเดล ...` → ตั้ง `GEMINI_MODEL` เป็นรุ่นที่ยังเปิดใช้ (ดูรายชื่อใน AI Studio)

## รูปแบบ API
`POST` JSON `{ "image": "data:image/jpeg;base64,..." }`
ตอบกลับ `{ "name", "candidates", "confidence", "kcal", "protein", "carbs", "fat", "fiber", "sugar", "sodium", "model" }`

## Huawei Health (ไม่บังคับ)
Worker เดียวกันนี้มีเส้นทาง `/huawei/*` สำหรับดึงข้อมูลนาฬิกา Huawei ผ่าน Huawei Health Kit
ต้องเพิ่ม Secret `HUAWEI_CLIENT_ID` และ `HUAWEI_CLIENT_SECRET` · ขั้นตอนเต็มดูที่ [HUAWEI_HEALTH.md](../HUAWEI_HEALTH.md)
