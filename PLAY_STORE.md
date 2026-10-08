# ลง Google Play — ขั้นตอนและข้อมูลที่ต้องกรอก

ไฟล์ที่เตรียมไว้แล้ว:

| อะไร | ไฟล์ |
|---|---|
| ไอคอน 512×512 | `store/icon-512.png` |
| ภาพหน้าปก (Feature graphic) 1024×500 | `store/feature-graphic-th.png`, `store/feature-graphic-en.png` |
| ภาพหน้าจอมือถือ 1080×1920 (6 ภาพ/ภาษา) | `store/screenshot-th-*.png`, `store/screenshot-en-*.png` |
| นโยบายความเป็นส่วนตัว (ไทย + อังกฤษ) | `docs/privacy-policy.html` (ในแอป: ตั้งค่า → เกี่ยวกับ) |
| Build ไฟล์ .aab ที่เซ็นแล้ว | GitHub Actions → **Release FoodWell (Google Play)** |

---

## 1. บัญชีนักพัฒนา
- สมัคร Google Play Console (ค่าสมัครครั้งเดียว 25 USD) และยืนยันตัวตน
- ⚠️ **บัญชีส่วนตัวที่สมัครใหม่** ต้องทดสอบแบบ **Closed testing กับผู้ทดสอบอย่างน้อย 12 คน ต่อเนื่อง 14 วัน** ก่อนจึงจะเปิด Production ได้ — เริ่มชวนเพื่อนไว้ได้เลย

## 2. สร้างกุญแจอัปโหลด (ทำครั้งเดียว เก็บให้ดี)
บนคอมพิวเตอร์ที่มี Java:
```bash
keytool -genkeypair -v -keystore foodwell-upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 foodwell-upload.jks > foodwell-upload.b64     # macOS: base64 -i foodwell-upload.jks -o foodwell-upload.b64
```
- **เก็บไฟล์ `.jks` และรหัสผ่านไว้ในที่ปลอดภัย 2 ที่** (ห้ามใส่ใน repo) — ถ้าหาย ต้องติดต่อ Google เพื่อรีเซ็ตกุญแจ
- ใน Play Console ให้เปิด **Play App Signing** (ค่าเริ่มต้น) Google จะถือกุญแจจริง ส่วนนี้เป็นแค่ “กุญแจอัปโหลด”

## 3. ใส่ Secrets ใน GitHub
Repo → Settings → Secrets and variables → Actions → **New repository secret**

| ชื่อ | ค่า |
|---|---|
| `UPLOAD_KEYSTORE_BASE64` | เนื้อหาไฟล์ `foodwell-upload.b64` |
| `UPLOAD_KEYSTORE_PASSWORD` | รหัสผ่าน keystore |
| `UPLOAD_KEY_ALIAS` | `upload` |
| `UPLOAD_KEY_PASSWORD` | รหัสผ่านกุญแจ (ถ้าใช้รหัสเดียวกับ keystore ไม่ต้องใส่) |
| `ADMOB_APP_ID`, `ADMOB_BANNER_ID`, `ADMOB_REWARDED_ID` | (เฉพาะถ้าจะเปิดโฆษณา — ดู `MONETIZATION.md`) |

## 4. Build ไฟล์ .aab
GitHub → Actions → **Release FoodWell (Google Play)** → **Run workflow** (เลือก ads ถ้าต้องการ) → รอ ~3 นาที → ดาวน์โหลด artifact **FoodWell-release-aab** (แตก zip ได้ `app-release.aab`)
- versionCode เพิ่มเองทุกครั้งที่ build (100 + เลขรอบ) จึงอัปโหลดทับเวอร์ชันเดิมได้เสมอ

## 5. สร้างแอปใน Play Console
- ชื่อแอป: **FoodWell** (เปลี่ยนได้ภายหลัง) · ภาษาเริ่มต้น: ไทย · แอป · ฟรี
- Testing → **Internal testing** → อัปโหลด `app-release.aab` → เพิ่มอีเมลผู้ทดสอบ (ทดสอบปุ่มซื้อปิดโฆษณาได้ที่นี่)
- จากนั้น **Closed testing** (12 คน 14 วัน — ดูข้อ 1) → **Production**

## 6. นโยบายความเป็นส่วนตัวต้องมี URL สาธารณะ
เลือกอย่างใดอย่างหนึ่ง:
- **GitHub Pages:** Repo → Settings → Pages → Source: `main` / โฟลเดอร์ `/docs` → URL จะเป็น `https://<user>.github.io/<repo>/privacy-policy.html` (repo ต้องเป็น public หรือใช้แผนที่รองรับ Pages)
- **Google Sites** (ฟรี): สร้างหน้าใหม่ แล้วคัดลอกเนื้อหาจาก `docs/privacy-policy.html`

⚠️ ก่อนเผยแพร่ แก้ **`[อีเมลติดต่อ / CONTACT EMAIL]`** ในไฟล์ `docs/privacy-policy.html` และ `assets/privacy.html` เป็นอีเมลที่ใช้ติดต่อผู้ใช้

---

## 7. ข้อความหน้าร้าน (Store listing)

### ภาษาไทย
**ชื่อ (≤30):** FoodWell: บันทึกอาหาร แคลอรี่

**คำอธิบายสั้น (≤80):** บันทึกอาหารไทย นับแคลอรี่ ดื่มน้ำ ออกกำลังกาย และวางแผนสุขภาพแบบใจดีกับตัวเอง

**คำอธิบายเต็ม:**
```
FoodWell ช่วยให้ดูแลการกินและสุขภาพได้ง่าย ๆ ทุกวัน แบบไม่กดดันตัวเอง

🍱 บันทึกอาหารง่าย
• ฐานข้อมูลอาหารไทยกว่า 240 เมนู พิมพ์ชื่อแล้วเติมค่าโภชนาการให้
• สแกนบาร์โค้ดสินค้า (Open Food Facts) ใส่ปริมาณที่กินจริงได้
• ถ่ายรูปอาหาร และวิเคราะห์ด้วย AI
• เมนูทำเอง คำนวณจากวัตถุดิบ · กินเหมือนเดิมได้ในแตะเดียว

🎯 เป้าหมายเฉพาะคุณ
• คำนวณ BMI (เกณฑ์คนเอเชีย) แคลอรี่ โปรตีน คาร์บ ไขมัน ที่ควรได้ต่อวัน
• แผนลดน้ำหนัก กำหนดเป้าและจังหวะเอง
• IF (อดอาหารเป็นช่วง) พร้อมตัวนับเวลา

🥗 เมนูสุขภาพ
• 39 สูตรอาหารไทยสุขภาพ แนะนำตามแคลอรี่ที่เหลือในวันนั้น
• วางแผนเมนูทั้งสัปดาห์ และสร้างรายการซื้อของอัตโนมัติ

🏋️ แผนออกกำลังกาย
• จัดตารางตามเป้าหมาย ระดับ และเวลาที่มี ทำที่บ้าน ยิม หรือกลางแจ้ง
• โหมดนำเล่น มีตัวจับเวลาและท่าตัวอย่างขยับได้ 42 ท่า

💧 น้ำและการเคลื่อนไหว
• เป้าดื่มน้ำตามน้ำหนักตัว · ประเมินแคลอรี่จากกิจกรรม 40+ ประเภท

📊 เข้าใจตัวเองมากขึ้น
• สรุปรายสัปดาห์ ข้อสังเกตอัตโนมัติ (เช่น น้ำตาลส่วนใหญ่มาจากเครื่องดื่ม)
• รายงานสุขภาพ PDF ให้แพทย์หรือนักโภชนาการดูได้

✨ และอีกมากมาย
• ถ้วยรางวัลและการแชร์ความสำเร็จ · วิดเจ็ตหน้าจอ · การแจ้งเตือน
• ธีมสว่าง/มืด เลือกสีได้ · ภาษาไทยและอังกฤษ
• ข้อมูลอยู่ในเครื่อง ซิงก์หลายเครื่องด้วยบัญชี Google ได้ (ไม่บังคับ)

ค่าโภชนาการเป็นค่าประมาณ ไม่ใช่คำแนะนำทางการแพทย์
```

### English
**Title (≤30):** FoodWell: Thai Food & Calories

**Short description (≤80):** Log Thai food, count calories, drink water, work out — kindly, every day.

**Full description:**
```
FoodWell makes it easy to look after what you eat and how you feel — without the pressure.

🍱 Easy food logging
• 240+ Thai dishes: type a name and the nutrition fills in
• Barcode scanning (Open Food Facts) with the amount you actually ate
• Food photos with AI analysis
• Home recipes calculated from ingredients · repeat a meal in one tap

🎯 Targets made for you
• BMI (Asian cut-offs) and daily calories, protein, carbs and fat
• Weight-loss plan with your own target and pace
• Intermittent fasting timer

🥗 Healthy recipes
• 39 healthy Thai recipes suggested from what's left for today
• Weekly meal plan and an automatic shopping list

🏋️ Workout plans
• A weekly schedule for your goal, level and time — at home, gym or outdoors
• Guided timer with 42 animated exercises

💧 Water & movement
• Water goal from your weight · calories burned for 40+ activities

📊 Understand yourself
• Weekly summary and automatic insights (e.g. most of your sugar comes from drinks)
• PDF health report to share with your doctor or dietitian

✨ And more
• Trophies and shareable cards · home-screen widget · reminders
• Light/dark themes with accent colours · Thai and English
• Data stays on your phone; optional sync across devices with your Google account

Nutrition values are estimates, not medical advice.
```

**หมวดหมู่:** Health & Fitness · **แท็ก:** Calorie counter, Diet & nutrition, Fitness

---

## 8. แบบฟอร์ม Data safety (คำตอบตามที่แอปทำจริง)

**Does your app collect or share any of the required user data types?** → **Yes**
**Is all of the user data collected by your app encrypted in transit?** → **Yes**
**Do you provide a way for users to request that their data is deleted?** → **Yes** (ในแอป: ตั้งค่า → ข้อมูลและซิงก์ → ลบบัญชีและข้อมูลบนคลาวด์ · และทางอีเมลตามนโยบาย)

| ประเภทข้อมูล | เก็บ (Collected) | แชร์ (Shared) | จำเป็น? | วัตถุประสงค์ |
|---|---|---|---|---|
| Personal info → Name, Email address, User IDs | ✅ (เมื่อเข้าสู่ระบบ Google) | ❌ | Optional | App functionality, Account management |
| Personal info → Name (ชื่อที่ตั้งให้เพื่อนเห็นในห้องชาเลนจ์) + App activity (จำนวนวันที่ทำชาเลนจ์สำเร็จ) | ✅ (เมื่อเข้าร่วมห้องชาเลนจ์) | ❌ (ผู้ใช้เลือกแสดงให้เพื่อนในห้องเอง ไม่ใช่บุคคลที่สาม) | Optional | App functionality |
| Health and fitness → Health info, Fitness info | ✅ (เมื่อเปิดซิงก์) | ❌ | Optional | App functionality |
| Photos and videos → Photos | ✅ (รูปอาหาร เมื่อเปิดซิงก์ / ส่งวิเคราะห์ AI) | ❌ | Optional | App functionality |
| App activity → Other user-generated content (บันทึกอาหาร โน้ต แผน) | ✅ (เมื่อเปิดซิงก์) | ❌ | Optional | App functionality |
| Device or other IDs (Advertising ID) | ✅ **เฉพาะ build ที่เปิดโฆษณา** | ✅ (Google AdMob) | Required* | Advertising or marketing |
| Purchase history | ✅ (สถานะซื้อปิดโฆษณา ผ่าน Google Play) | ❌ | Optional | App functionality |

\* ถ้า build แบบ **ไม่เปิดโฆษณา** ให้ตัดแถว Device IDs ออก

หมายเหตุ: ข้อมูลที่อยู่ในเครื่องอย่างเดียวและไม่ถูกส่งออก **ไม่นับเป็น “collected”** ตามนิยามของ Google · การส่งรูปไปวิเคราะห์ AI และบาร์โค้ดไป Open Food Facts เกิดเมื่อผู้ใช้กดเองเท่านั้น

## 9. แบบฟอร์มอื่น ๆ ใน App content
- **Privacy policy:** URL จากข้อ 6
- **Ads:** ตอบ Yes ถ้า build เปิดโฆษณา
- **App access:** ทุกฟีเจอร์ใช้ได้โดยไม่ต้องล็อกอิน (ล็อกอินเฉพาะซิงก์)
- **Content rating (IARC):** หมวด Health/Reference — ไม่มีความรุนแรง ไม่มีเนื้อหาผู้ใหญ่ · มีการซื้อในแอป · ผู้ใช้ไม่ได้แชทกัน (ห้องชาเลนจ์แสดงเฉพาะชื่อและคะแนน) → ตอบเรื่อง "users can interact" ว่า **ไม่มีการแชท** แต่มีการแชร์ชื่อ/คะแนนกับคนที่มีรหัส
- **Target audience:** 18+ (หรือ 13+) — **อย่าเลือกกลุ่มเด็ก** (จะติดนโยบาย Families)
- **Health apps declaration:** เลือก *Nutrition and weight management*, *Activity and fitness* · ไม่ใช่อุปกรณ์การแพทย์
- **Account deletion:** ใส่ URL นโยบาย (หัวข้อ 7 “การลบข้อมูล”) และบอกว่าลบได้ในแอป
- **Government / Financial / News:** No

## 10. ก่อนกด Publish
- [ ] แก้อีเมลติดต่อในนโยบาย แล้วเผยแพร่ URL
- [ ] ถ้าใช้ซิงก์: เพิ่ม **SHA-1 ของ App signing key** (Play Console → Setup → App signing) ลงใน Firebase → Project settings → Android app แล้วดาวน์โหลด `google-services.json` ใหม่ใส่ `app/` — ไม่งั้นล็อกอิน Google บนเวอร์ชันจาก Play จะไม่ทำงาน
- [ ] ถ้าเปิดโฆษณา: สร้าง AdMob app + ad units, ใส่ `app-ads.txt` บนเว็บไซต์ผู้พัฒนา, สร้างสินค้า `remove_ads` (ดู `MONETIZATION.md`)
- [ ] ทดสอบ Internal testing บนมือถือจริงอย่างน้อย 1 เครื่อง
