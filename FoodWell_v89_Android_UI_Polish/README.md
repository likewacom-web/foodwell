# FoodWell v89 — Android UI Polish

ต่อจาก v88 Error Recovery

ปรับปรุงชั้น Android/WebView สำหรับการใช้งานบนมือถือ:
- ปรับ status/navigation bar ให้เข้ากับธีม FoodWell
- ปิด overscroll และ zoom ที่ไม่จำเป็น
- ปรับ touch interaction และขนาด input ให้เหมาะกับ Android
- รองรับ safe-area สำหรับหน้าจอที่มี gesture/navigation inset
- รองรับ prefers-reduced-motion
- อัปเดต bridge version เป็น v89

APK สร้างอัตโนมัติด้วย GitHub Actions (.github/workflows/android-apk.yml)
