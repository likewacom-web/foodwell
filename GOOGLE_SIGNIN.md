# ตั้งค่าเข้าสู่ระบบ Google + สำรองข้อมูลไป Google Drive

แอป FoodWell ใช้ Google Identity Services ขอสิทธิ์ 2 อย่าง:
- ชื่อ / อีเมล / รูปโปรไฟล์ (แสดงว่าเข้าสู่ระบบบัญชีไหน)
- `drive.appdata` — พื้นที่ซ่อนของแอปใน Google Drive ของคุณ (อ่าน/เขียนได้เฉพาะไฟล์ของ FoodWell เท่านั้น มองไม่เห็นไฟล์อื่นใน Drive)

ไม่ต้องใส่ Client ID หรือรหัสลับใด ๆ ในแอป Google จับคู่แอปจาก **ชื่อแพ็กเกจ + ลายเซ็น SHA-1** ที่ลงทะเบียนไว้ ฟรีทั้งหมด

## ข้อมูลที่ต้องใช้
| รายการ | ค่า |
|---|---|
| Package name | `com.foodwell.app` |
| SHA-1 | `2E:49:8E:46:81:5F:08:26:B2:8E:62:2E:88:AF:BA:F3:2E:F2:59:20` |

(SHA-1 มาจาก `FoodWell_v89_Android_UI_Polish/android/app/debug.keystore` ที่ใช้เซ็น APK ทุกครั้ง)

## ขั้นตอน (ทำครั้งเดียว)
1. เปิด https://console.cloud.google.com แล้วล็อกอินด้วย Gmail
2. สร้างโปรเจกต์ใหม่ ตั้งชื่อ `FoodWell`
3. เปิดใช้ Drive API: เมนู **APIs & Services → Library** → ค้นหา **Google Drive API** → กด **Enable**
4. ตั้งค่าหน้าขอสิทธิ์: เมนู **Google Auth Platform** (หรือ **OAuth consent screen**) → **Get started**
   - App name: `FoodWell`, User support email: Gmail ของคุณ
   - Audience: **External**
   - Contact email: Gmail ของคุณ → ยอมรับเงื่อนไข → **Create**
5. เพิ่มตัวเองเป็นผู้ทดสอบ: **Audience → Test users → Add users** → ใส่ Gmail ที่จะใช้ในแอป (เพิ่มได้หลายคน สูงสุด 100)
6. สร้าง Client สำหรับ Android: **Clients → Create client**
   - Application type: **Android**
   - Package name: `com.foodwell.app`
   - SHA-1: ค่าจากตารางด้านบน → **Create**
7. (แนะนำ) **Data access → Add or remove scopes** → เพิ่ม `.../auth/drive.appdata` → **Save**

รอ 5–10 นาทีให้ Google อัปเดต แล้วในแอป: **เพิ่มเติม → ☁️ สำรองข้อมูลไป Google Drive → เข้าสู่ระบบด้วย Google**

## การใช้งาน
- **☁️ สำรองตอนนี้** — อัปโหลดข้อมูล FoodWell ทั้งหมด (อาหาร น้ำ กิจกรรม แผนอาหาร การตั้งค่า) ทับไฟล์สำรองเดิม
- **⤓ กู้คืนจาก Drive** — แทนที่ข้อมูลในเครื่องด้วยข้อมูลจาก Drive (ใช้ตอนเปลี่ยนเครื่อง/ติดตั้งแอปใหม่)
- **สำรองอัตโนมัติ** — เมื่อเปิดแอปและสำรองครั้งล่าสุดเกิน ~1 วัน จะสำรองให้เองโดยไม่มีหน้าต่างเด้ง
- ดู/ลบข้อมูลที่แอปเก็บไว้ได้ที่ drive.google.com → ⚙️ Settings → **Manage apps** → FoodWell → **Delete hidden app data**

## ข้อความผิดพลาดที่อาจเจอ
- `ยังไม่ได้ตั้งค่า Google Cloud ให้แอปนี้` → ยังไม่ได้ทำข้อ 6 หรือ SHA-1/แพ็กเกจพิมพ์ไม่ตรง (หรือเพิ่งสร้าง รอ 5–10 นาที)
- `ยังไม่ได้เปิด Google Drive API` → ทำข้อ 3
- หน้าล็อกอินขึ้นว่า **access blocked / ยังไม่ได้รับการยืนยัน** → Gmail นั้นยังไม่ได้อยู่ใน Test users (ข้อ 5)
- ระหว่างอยู่ในโหมด Testing Google อาจให้กดยืนยันสิทธิ์ใหม่เป็นระยะ ถ้าสำรองอัตโนมัติไม่ทำงาน ให้กด **สำรองตอนนี้** หนึ่งครั้ง

## หมายเหตุ
- ถ้าเปลี่ยน keystore ที่ใช้เซ็น APK ต้องเพิ่ม SHA-1 ใหม่ในข้อ 6 ด้วย
- ดู SHA-1 ได้ด้วยคำสั่ง
  `keytool -list -v -keystore FoodWell_v89_Android_UI_Polish/android/app/debug.keystore -storepass android -alias androiddebugkey`
