# ตั้งค่าเข้าสู่ระบบ Gmail + ซิงก์ข้อมูลหลายเครื่อง (Firebase ฟรี)

FoodWell ใช้ Firebase (แพ็กเกจ Spark ฟรี ไม่ต้องผูกบัตร):
- **Authentication** — เข้าสู่ระบบด้วย Google (Gmail)
- **Cloud Firestore** — เก็บข้อมูลของแต่ละบัญชีที่ `users/{uid}/...` อ่าน/เขียนได้เฉพาะเจ้าของบัญชี

โควตาฟรีของ Firestore (ประมาณ 50,000 อ่าน / 20,000 เขียน ต่อวัน, พื้นที่ 1 GB) เหลือเฟือสำหรับใช้ส่วนตัว/ครอบครัว
(เงื่อนไขของ Google อาจเปลี่ยนได้)

## ข้อมูลที่ต้องใช้
| รายการ | ค่า |
|---|---|
| Android package name | `com.foodwell.app` |
| SHA-1 | `2E:49:8E:46:81:5F:08:26:B2:8E:62:2E:88:AF:BA:F3:2E:F2:59:20` |

## ขั้นตอน (ทำครั้งเดียว)
1. เปิด https://console.firebase.google.com → **Create a project** → ตั้งชื่อ `FoodWell` (ปิด Google Analytics ได้)
2. ในหน้าโปรเจกต์ กดไอคอน **Android** เพื่อเพิ่มแอป
   - Android package name: `com.foodwell.app`
   - Debug signing certificate SHA-1: ค่าจากตารางด้านบน
   - กด **Register app** → **Download google-services.json**
3. เปิดการเข้าสู่ระบบด้วย Google: **Build → Authentication → Get started → Sign-in method → Google → Enable** → เลือก support email → **Save**
4. สร้างฐานข้อมูล: **Build → Firestore Database → Create database** → เลือก location (เช่น `asia-southeast1` สิงคโปร์) → **Start in production mode**
5. ตั้งกฎความปลอดภัย: แท็บ **Rules** วางแทนของเดิม แล้วกด **Publish**
   ```
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /users/{uid}/{document=**} {
         allow read, write: if request.auth != null && request.auth.uid == uid;
       }
     }
   }
   ```
6. เอาไฟล์ `google-services.json` ใส่ใน repo ที่ `FoodWell_v89_Android_UI_Polish/android/app/google-services.json`
   - บน GitHub: เปิดโฟลเดอร์นั้น → **Add file → Upload files** → เลือกไฟล์ → **Commit**
   - หรือส่งไฟล์ให้ Claude ใส่ให้
   - ไฟล์นี้ไม่ใช่รหัสลับ (Firebase ออกแบบให้ใส่ในแอปได้) ความปลอดภัยของข้อมูลมาจากกฎในข้อ 5
7. GitHub Actions จะ build APK ใหม่ให้อัตโนมัติ → ติดตั้งทับได้เลย

## การใช้งานในแอป
หน้า **เพิ่มเติม → ☁️ ซิงก์ข้อมูลหลายเครื่อง → เข้าสู่ระบบด้วย Google**
- ข้อมูลทั้งหมด (อาหาร น้ำ กิจกรรม แผนอาหาร การตั้งค่า) ซิงก์อัตโนมัติไปทุกเครื่องที่ใช้บัญชีเดียวกัน
- แก้ที่เครื่องไหน อีกเครื่องจะอัปเดตเองภายในไม่กี่วินาที (หน้าแอปจะรีเฟรชเองเมื่อไม่ได้พิมพ์อยู่)
- ออฟไลน์ได้ — จะส่งข้อมูลเมื่อกลับมาออนไลน์
- เครื่องที่เข้าสู่ระบบทีหลังจะใช้ข้อมูลล่าสุดจากคลาวด์ (ถ้ามีข้อมูลเก่าในเครื่องนั้นที่อยากเก็บ ให้กด **💾 ส่งออก** ไว้ก่อน)
- ถ้าแก้ข้อมูลเดียวกันพร้อมกันสองเครื่อง ระบบใช้ข้อมูลที่บันทึกทีหลังสุด

## ข้อความผิดพลาดที่อาจเจอ
- `ยังไม่ได้เชื่อม Firebase` → APK นี้ build ก่อนเพิ่ม google-services.json (ข้อ 6)
- `ตั้งค่า Firebase ยังไม่ครบ (SHA-1 / เปิด Google sign-in)` → ตรวจข้อ 2 (SHA-1) และข้อ 3 แล้วดาวน์โหลด google-services.json ใหม่
- `Firestore ปฏิเสธการเข้าถึง` → ตรวจกฎในข้อ 5
- `ยังไม่ได้สร้าง Firestore Database` → ทำข้อ 4
- `ไม่พบบัญชี Google ในเครื่อง` → เพิ่มบัญชี Google ในการตั้งค่ามือถือก่อน
