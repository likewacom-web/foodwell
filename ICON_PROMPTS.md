# พรอมต์เจนไอคอนแอป

ให้เข้าชุดกับมาสคอตแมวขาวและถ้วยรางวัลในแอป (kawaii, ชมพู) แต่เรียบกว่า เพราะไอคอนต้องอ่านออกตอนเล็กแค่ 48 px บนหน้าจอมือถือ

## กติกาไอคอนที่ดี (สำคัญ)
- **ไม่มีตัวหนังสือ** ในรูป (ชื่อแอปแสดงใต้ไอคอนอยู่แล้ว และเปลี่ยนชื่อทีหลังได้โดยไม่ต้องทำไอคอนใหม่)
- รูปหลัก **อยู่กลางภาพ ขนาดราว 60% ของภาพ** — Android จะตัดขอบเป็นวงกลม/สี่เหลี่ยมมน ส่วนที่ชิดขอบจะโดนตัด
- พื้นหลัง **สีเรียบหรือไล่สีอ่อน ๆ** ไม่มีลวดลายเยอะ, ไม่ใช้พื้นดำ (ไอคอนบนหน้าจอมืดจะจม)
- ขนาด **1024×1024** สี่เหลี่ยมจัตุรัส PNG ไม่ต้องทำมุมมนเอง

## [ICON STYLE] — วางต่อท้ายทุกพรอมต์
```
mobile app icon, kawaii chibi style, a small fluffy white cat mascot with pink cheeks and big shiny eyes, thick soft rounded shapes, bold clean silhouette that reads at small size, soft pastel pink to peach gradient background, gentle soft shading, subtle highlight, centered composition with generous empty margin around the subject (subject fills about 60% of the canvas), flat vector-like finish, no text, no letters, no watermark, no border, square 1024x1024
```

## แบบที่แนะนำ (เลือก 1 หรือเจนหลายแบบมาเทียบ)

**A. แมวกอดชามสลัด** ⭐ แนะนำ — สื่อ "กินดี" ชัดที่สุด
```
the white cat hugging a round bowl of fresh green salad with a cherry tomato and a leaf on top, the cat peeking over the bowl smiling, [ICON STYLE]
```

**B. หน้าแมว + ใบไม้** — เรียบ มินิมอล จำง่าย
```
just the head of the white cat, a single fresh green leaf sprouting on top of its head like a cowlick, small pink heart on its cheek, [ICON STYLE]
```

**C. แมวถือช้อนส้อม + หัวใจ** — สื่อแอปอาหาร + สุขภาพ
```
the white cat holding a small fork and spoon crossed in front of it, a pink heart floating above its head, tiny golden sparkle, [ICON STYLE]
```

**D. อุ้งเท้าแมวในจานอาหาร** — ไม่มีตัวแมว ดูเป็นโลโก้มากกว่า
```
a round white plate seen from above, a big pink cat paw print made of fruits and vegetables on the plate (strawberry, kiwi, broccoli, carrot slices), a small green leaf, [ICON STYLE]
```

**E. แมวนั่งบนตาชั่ง** — เน้นลดน้ำหนัก/ฟิต (เหมาะถ้าใช้ชื่อแนว Purrfit / MeowFit)
```
the white cat sitting happily on a tiny mint green bathroom scale, holding a red apple, small sparkles, [ICON STYLE]
```

## ถ้าเครื่องมือทำพื้นใสได้ (ดีที่สุดสำหรับ Android)
เจนแยก 2 ชั้นจะได้ไอคอนที่คมและขยับตามธีมมือถือได้ (adaptive icon)
- **ชั้นหน้า (foreground):** พรอมต์แบบที่เลือก แต่เปลี่ยน `soft pastel pink to peach gradient background` เป็น `transparent background, PNG with alpha`
- **ชั้นหลัง (background):** ไม่ต้องเจน บอกสีที่ชอบมาก็พอ (เช่น ชมพู #FF9EC4 → พีช #FFD1B8)

## เครื่องมือ
Midjourney (`--ar 1:1 --style raw`), ChatGPT/DALL·E, Gemini, Ideogram, Leonardo ได้หมด
ถ้ารองรับรูปอ้างอิง ให้แนบรูปถ้วยรางวัลในแอป 1 รูป (`art/trophy_style_reference.jpg`) จะได้แมวหน้าตาเดียวกัน

## ส่งมาให้ Claude
ส่งไฟล์ PNG 1024×1024 มา (หรือชั้นหน้าแบบพื้นใส + สีพื้นหลัง) แล้ว Claude จะทำให้ครบ:
ไอคอน Android ทุกขนาด (mipmap), adaptive icon, ไอคอนกลม, ไอคอน Play Store 512×512 และไอคอนในแอป/หน้าเว็บ
