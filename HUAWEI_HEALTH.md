# เชื่อมนาฬิกา Huawei กับ FoodWell (Huawei Health Kit)

FoodWell อ่าน **ก้าว · แคลอรี่จากกิจกรรม · ชีพจร** ของวันนี้จากบัญชี Huawei ผ่าน Huawei Health Kit
(ช่องทางเดียวกับที่แอป Health Sync ใช้) โดยมี FoodWell Worker บน Cloudflare เป็นตัวกลาง
เพราะการขอ token ต้องใช้ Client Secret ซึ่งห้ามใส่ไว้ในแอป

> ⚠️ Huawei ต้อง **ตรวจและอนุมัติ** การใช้ Health Kit ก่อน (หลายวัน–หลายสัปดาห์) และอาจไม่อนุมัติ
> ระหว่างรอ ใช้แอป Health Sync ไปก่อนได้ · ขั้นตอน/ชื่อเมนูของ Huawei อาจเปลี่ยน ให้ยึดตามหน้าเว็บจริง

## สิ่งที่ต้องมีก่อน
- FoodWell Worker ที่ deploy แล้ว (ดู `ai-proxy/README.md`) — ใช้ตัวเดียวกับ AI วิเคราะห์รูป
- ตั้ง **AI Endpoint** ในแอปแล้ว (หน้า เพิ่มเติม) เช่น `https://foodwell-ai.xxx.workers.dev/?k=รหัส`

## 1. สมัคร Huawei Developer
1. https://developer.huawei.com → **Sign up / Log in** ด้วย Huawei ID (บัญชีเดียวกับที่ใช้กับนาฬิกาก็ได้)
2. ทำ **Identity verification** แบบบุคคล (Individual)

## 2. สร้างแอปใน AppGallery Connect
1. **AppGallery Connect → My projects → Add project** ตั้งชื่อ `FoodWell`
2. ในโปรเจกต์ **Add app** → Platform **Android**, Package name `com.foodwell.app`
3. ที่ **Project settings → General information** จด **Client ID** และ **Client secret** (ของแอป/OAuth 2.0 client)
4. ตั้ง **Redirect URL / Callback address** ของ OAuth เป็น
   `https://<ชื่อ worker>.workers.dev/huawei/callback`

## 3. ขอสิทธิ์ Health Kit
1. Huawei Developers Console → **Health Kit** → **Apply for Health Kit**
2. เลือกแอป FoodWell แล้วขอสิทธิ์ **อ่าน (Read)**: Steps, Calories, Heart rate
3. กรอกเหตุผลการใช้ข้อมูล (เช่น "แสดงก้าว/แคลอรี่/ชีพจรของผู้ใช้เองในแอปบันทึกอาหารส่วนตัว") และลิงก์นโยบายความเป็นส่วนตัว
4. ส่งคำขอ แล้วรอการอนุมัติ

## 4. ใส่ค่าใน Cloudflare Worker
1. อัปเดตโค้ด Worker เป็น [`ai-proxy/worker.js`](ai-proxy/worker.js) ล่าสุด → **Deploy**
2. **Settings → Variables and Secrets** เพิ่ม Secret:
   - `HUAWEI_CLIENT_ID` = Client ID จากข้อ 2
   - `HUAWEI_CLIENT_SECRET` = Client secret จากข้อ 2
3. กด **Deploy** อีกครั้ง

## 5. เชื่อมในแอป
แอป FoodWell → **⌚ Smart Watch** → การ์ด **Huawei Health** → **เชื่อมต่อ Huawei**
→ ล็อกอิน Huawei ID → อนุญาต → ระบบจะพากลับมาที่แอปและซิงก์ให้
เมื่อเชื่อมแล้ว ค่า ก้าว / kcal กิจกรรม / ชีพจร จะมาจาก Huawei แทนตัวนับในมือถือ

## ถ้าเจอปัญหา
- `ยังไม่ได้ตั้งค่า HUAWEI_CLIENT_ID…` → ทำข้อ 4
- หน้า Huawei ขึ้น error เรื่อง redirect URI → ตรวจ Redirect URL ข้อ 2.4 ให้ตรงทุกตัวอักษร
- `สิทธิ์ Huawei หมดอายุ` → กด **เชื่อมต่อ Huawei** ใหม่
- การ์ดขึ้น "ค่าบางอย่างยังอ่านจาก Huawei ไม่ได้ …" → ส่งข้อความนั้นให้ผู้พัฒนา
  (ชื่อฟิลด์ของ Huawei แก้ได้ใน worker.js โดยไม่ต้อง build แอปใหม่)
- ในแอป Huawei Health อาจต้องอนุญาตการแชร์ข้อมูลด้วย: **ฉัน → ความเป็นส่วนตัว → การแชร์ข้อมูลและการอนุญาต**
