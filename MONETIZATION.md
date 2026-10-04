# โฆษณา + ซื้อครั้งเดียวเพื่อปิดโฆษณา

แอปมีระบบนี้ติดตั้งไว้แล้ว แต่ **ปิดโฆษณาไว้ก่อน** และใช้ ID ทดสอบของ Google จนกว่าจะลง Google Play

| ส่วน | ไฟล์ |
|---|---|
| แบนเนอร์ AdMob + หน้าขอความยินยอม (UMP) + Google Play Billing | `android/app/src/main/java/com/foodwell/app/Monetization.kt` |
| การ์ด "🚫 ปิดโฆษณาถาวร" ในหน้า **เพิ่มเติม** | `assets/index.html` (บล็อก v98, `window.FoodWellPro`) |
| เปิด/ปิดโฆษณา และ ID | `android/app/build.gradle` (`-Pads`, `-PadmobAppId`, `-PadmobBannerId`) |

## สิ่งที่ต้องทำตอนจะลง Google Play

1. **Google Play Console** → สร้างแอป `com.foodwell.app`
   → **Monetize → Products → In-app products** → สร้างสินค้า ID **`remove_ads`** (ต้องตรงตัว)
   ตั้งราคา (เช่น ฿79) แล้วกด **Activate**
   *(ต้องอัปโหลดแอปที่มี Billing อย่างน้อย 1 ครั้งก่อน ถึงจะสร้างสินค้าได้)*
2. **AdMob** → Apps → Add app → เลือกแอปจาก Play → ได้ **App ID** (`ca-app-pub-xxx~yyy`)
   → Ad units → **Banner** → ได้ **Ad unit ID** (`ca-app-pub-xxx/zzz`)
   → Privacy & messaging → สร้างข้อความ **GDPR** (หน้ายินยอมสำหรับผู้ใช้ยุโรป)
3. Build แบบเปิดโฆษณา:
   ```
   gradle assembleRelease -Pads=true -PadmobAppId=ca-app-pub-xxx~yyy -PadmobBannerId=ca-app-pub-xxx/zzz
   ```
4. ใส่ไฟล์ `app-ads.txt` ที่เว็บไซต์ผู้พัฒนาที่กรอกใน Play Console (AdMob ให้เนื้อหาไฟล์มา)
5. Play Console → **Data safety**: แจ้งว่ามีโฆษณา (Advertising ID) และการซื้อในแอป

## ทดสอบการซื้อ
- ใส่ Gmail ของตัวเองใน Play Console → **Settings → License testing** จะซื้อได้โดยไม่เสียเงินจริง
- การซื้อใช้ได้เฉพาะแอปที่ติดตั้งจาก Play (internal testing ก็ได้) — APK ที่โหลดจาก GitHub จะขึ้นว่า "ซื้อได้เมื่อติดตั้งแอปจาก Google Play"
- **ห้ามกดโฆษณาจริงของตัวเอง** ระหว่างทดสอบ ใช้ ID ทดสอบ (ค่าเริ่มต้น) เท่านั้น

## หมายเหตุ
- สิทธิ์ "ไม่มีโฆษณา" จำไว้ในเครื่อง และกู้คืนจาก Google Play ทุกครั้งที่เปิดแอป (ลงใหม่/เปลี่ยนเครื่องก็ได้คืน)
- การซื้อถูก acknowledge ทันทีในแอป (ถ้าไม่ทำ Google จะคืนเงินอัตโนมัติใน 3 วัน)
- ยังไม่มีเซิร์ฟเวอร์ตรวจสอบใบเสร็จ ถ้าต้องการกันการโกงจริงจังให้เพิ่มการตรวจผ่าน Google Play Developer API ภายหลัง
- Build บน GitHub ยังเป็น debug APK ที่ปิดโฆษณา — การลง Play ต้องใช้ release (.aab) ที่เซ็นด้วยกุญแจจริง
