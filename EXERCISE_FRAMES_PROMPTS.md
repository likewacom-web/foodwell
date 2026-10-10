# ภาพจังหวะท่าออกกำลังกาย (แบบภาพนิ่ง 1–3 ภาพต่อท่า)

แทนการเจนวิดีโอ: เจน **ภาพนิ่ง** ตามจังหวะของท่า แล้วแอปจะสลับภาพตามจังหวะการเคลื่อนไหวให้เอง
ระหว่างที่ยังไม่มีภาพ แอปใช้ตัวการ์ตูนเวกเตอร์ที่ขยับได้ (`workout_rig.js`) อยู่แล้ว จึงทยอยทำทีละท่าได้

## วิธีทำ

1. เปิดเครื่องมือเจนภาพที่ใส่ **ภาพอ้างอิง** ได้ แล้วใส่รูปจาก `art/` ตามที่ระบุในแต่ละท่า
2. เจน **ภาพที่ 1** ด้วยพรอมต์ของภาพที่ 1 เลือกภาพที่ท่าถูกต้องที่สุด
3. เจน **ภาพที่ 2, 3** โดยใส่ **ภาพที่ 1 ที่ได้** เป็นภาพอ้างอิงด้วย (ตัวละครจะได้ขนาดและตำแหน่งเท่ากัน ภาพจะไม่กระโดดตอนสลับ)
4. ตั้งชื่อไฟล์ `<id>_1.png`, `<id>_2.png`, … เช่น `squat_1.png`, `squat_2.png` แล้วส่งมา (ผมตัดพื้นเขียวและแปลงเป็น WebP ให้)
5. ท่าที่มีภาพเดียว (ท่าค้าง เช่น แพลงก์) แอปจะแสดงภาพนั้นนิ่ง ๆ

**Negative prompt** (ถ้ามีช่อง):

```
text, letters, logo, watermark, extra limbs, extra fingers, distorted anatomy, cropped body, multiple people, background scenery, floor, shadow, realistic photo, 3D render, style change
```

## สรุป

| ท่า | id | รูปอ้างอิง | จำนวนภาพ |
|---|---|---|---|
| 🚶 ย่ำเท้าอยู่กับที่ | `march` | `ref_side.png` | 2 |
| 🙆 หมุนแขน | `armc` | `ref_front.png` | 2 |
| 🦵 เหวี่ยงขา | `legsw` | `ref_side.png` | 2 |
| 🦵 สควอท | `squat` | `ref_side.png` | 2 |
| 💪 วิดพื้นแบบคุกเข่า | `kpush` | `ref_side.png` | 2 |
| 💪 วิดพื้น | `push` | `ref_side.png` | 2 |
| 🍑 ยกสะโพก | `bridge` | `ref_side.png` | 2 |
| 🚶 ลันจ์ถอยหลัง | `lunge` | `ref_side.png` | 2 |
| 🧱 แพลงก์ | `plank` | `ref_side.png` | 1 |
| 🦸 ซูเปอร์แมน | `superman` | `ref_side.png` | 2 |
| 🐞 เดดบัก | `deadbug` | `ref_three_quarter.png` | 2 |
| 🪑 ดิปกับเก้าอี้ | `dips` | `ref_side.png` | 2 |
| 🧱 นั่งพิงกำแพง | `wallsit` | `ref_side.png` | 1 |
| 🦶 เขย่งปลายเท้า | `calf` | `ref_side.png` | 2 |
| 🧱 แพลงก์ด้านข้าง | `splank` | `ref_front.png` | 1 |
| 🪜 ก้าวขึ้นบันได | `stepup` | `ref_side.png` | 2 |
| ⭐ กระโดดตบ | `jacks` | `ref_front.png` | 2 |
| 🏃 วิ่งยกเข่าสูง | `knees` | `ref_side.png` | 2 |
| ⛰️ เมาท์เทนไคลม์เบอร์ | `climb` | `ref_side.png` | 2 |
| ⛸️ สเก็ตเตอร์ | `skater` | `ref_front.png` | 2 |
| 🔥 เบอร์พี | `burpee` | `ref_side.png` | 3 |
| 🥊 ชกลม | `box` | `ref_three_quarter.png` | 2 |
| 🏋️ สควอทดัมเบล | `gsquat` | `ref_side.png` | 2 |
| 🏋️ ดันอก (เบนช์เพรส) | `bench` | `ref_side.png` | 2 |
| 🚣 ซีทเต็ดโรว์ | `row` | `ref_side.png` | 2 |
| ⬇️ แลตพูลดาวน์ | `latpd` | `ref_three_quarter.png` | 2 |
| 🏋️ ดันไหล่ดัมเบล | `ohp` | `ref_side.png` | 2 |
| 🏋️ โรมาเนียนเดดลิฟต์ | `rdl` | `ref_side.png` | 2 |
| 🦵 เลกเพรส | `lpress` | `ref_side.png` | 2 |
| 💪 ดัมเบลเคิร์ล | `curl` | `ref_front.png` | 2 |
| 💪 ไทรเซปส์เอ็กซ์เทนชัน | `triext` | `ref_side.png` | 2 |
| 🚶 เดินเร็ว | `walk` | `ref_side.png` | 2 |
| 🏃 วิ่งเหยาะ | `jog` | `ref_side.png` | 2 |
| 🚴 ปั่นจักรยาน | `bike` | `ref_side.png` | 2 |
| 🏃 เครื่องเดินวงรี | `ellip` | `ref_side.png` | 2 |
| 🐈 แมว-วัว | `catcow` | `ref_side.png` | 2 |
| 🧘 ท่าเด็ก | `child` | `ref_side.png` | 1 |
| 🦵 ยืดต้นขาหลัง | `ham` | `ref_side.png` | 2 |
| 🧎 ยืดสะโพกด้านหน้า | `hip` | `ref_side.png` | 2 |
| 🙆 ยืดอกและไหล่ | `chest` | `ref_three_quarter.png` | 1 |
| 🌀 บิดลำตัวนอน | `twist` | `ref_three_quarter.png` | 2 |
| 🐍 ท่างู | `cobra` | `ref_side.png` | 2 |
| 😮‍💨 พัก | `rest` | `ref_front.png` | 1 |

## วอร์มอัพ

### `march` · 🚶 ย่ำเท้าอยู่กับที่ (March in place)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `march_1.png`

```
Using the character in the reference image, draw them standing tall, right knee lifted to hip height, left arm swung forward, right arm back. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `march_2.png`

```
Using the character in the reference image, draw them standing tall, left knee lifted to hip height, right arm swung forward, left arm back. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `armc` · 🙆 หมุนแขน (Arm circles)

**รูปอ้างอิง:** `art/ref_front.png`

**ภาพที่ 1** → `armc_1.png`

```
Using the character in the reference image, draw them standing tall, both arms straight out to the sides at shoulder height, slightly lowered. Front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `armc_2.png`

```
Using the character in the reference image, draw them standing tall, both arms straight out to the sides, slightly raised above shoulder height. Front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `legsw` · 🦵 เหวี่ยงขา (Leg swings)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `legsw_1.png`

```
Using the character in the reference image, draw them standing beside a plain grey wall on the right, right hand on the wall, near leg swung forward. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `legsw_2.png`

```
Using the character in the reference image, draw them standing beside a plain grey wall on the right, right hand on the wall, near leg swung back. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

## เวท — ที่บ้าน

### `squat` · 🦵 สควอท (Squats)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `squat_1.png`

```
Using the character in the reference image, draw them standing tall, feet shoulder-width, arms straight forward at shoulder height. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `squat_2.png`

```
Using the character in the reference image, draw them bottom of a squat: hips back and down, thighs parallel to the floor, chest up, arms straight forward. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `kpush` · 💪 วิดพื้นแบบคุกเข่า (Knee push-ups)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `kpush_1.png`

```
Using the character in the reference image, draw them knee push-up top: on knees and straight arms, hands under shoulders, straight line from head to knees. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `kpush_2.png`

```
Using the character in the reference image, draw them knee push-up bottom: elbows bent, chest just above the floor, body straight from head to knees. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `push` · 💪 วิดพื้น (Push-ups)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `push_1.png`

```
Using the character in the reference image, draw them push-up top: high plank on straight arms, body straight from head to heels. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `push_2.png`

```
Using the character in the reference image, draw them push-up bottom: elbows bent about 45 degrees, chest just above the floor, body straight. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `bridge` · 🍑 ยกสะโพก (Glute bridges)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `bridge_1.png`

```
Using the character in the reference image, draw them lying on the back, knees bent, feet flat, arms on the floor along the body. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `bridge_2.png`

```
Using the character in the reference image, draw them glute bridge top: hips lifted so knees, hips and shoulders form a straight line, feet and shoulders on the floor. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `lunge` · 🚶 ลันจ์ถอยหลัง (Reverse lunges)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `lunge_1.png`

```
Using the character in the reference image, draw them standing tall, hands on hips. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `lunge_2.png`

```
Using the character in the reference image, draw them reverse lunge bottom: one foot stepped back, both knees bent about 90 degrees, back knee just above the floor, torso upright, hands on hips. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `plank` · 🧱 แพลงก์ (Plank)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `plank_1.png`

```
Using the character in the reference image, draw them forearm plank: elbows under shoulders, body straight from head to heels, looking at the floor. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `superman` · 🦸 ซูเปอร์แมน (Superman)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `superman_1.png`

```
Using the character in the reference image, draw them lying face down, arms stretched forward on the floor, legs straight on the floor. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `superman_2.png`

```
Using the character in the reference image, draw them superman: arms, chest and legs lifted a few centimetres off the floor at the same time, belly on the floor. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `deadbug` · 🐞 เดดบัก (Dead bug)

**รูปอ้างอิง:** `art/ref_three_quarter.png`

**ภาพที่ 1** → `deadbug_1.png`

```
Using the character in the reference image, draw them lying on the back, arms pointing straight up, knees bent 90 degrees above the hips (tabletop). Three-quarter front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `deadbug_2.png`

```
Using the character in the reference image, draw them lying on the back, one arm reaching overhead toward the floor and the opposite leg straight out just above the floor, other arm and leg still in tabletop. Three-quarter front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `dips` · 🪑 ดิปกับเก้าอี้ (Chair dips)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `dips_1.png`

```
Using the character in the reference image, draw them sitting on the front edge of a sturdy wooden chair, hands gripping the edge beside the hips, knees bent 90 degrees, hips just off the seat with straight arms. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `dips_2.png`

```
Using the character in the reference image, draw them chair dip bottom: elbows bent to 90 degrees pointing back, hips lowered in front of the chair, knees bent. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `wallsit` · 🧱 นั่งพิงกำแพง (Wall sit)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `wallsit_1.png`

```
Using the character in the reference image, draw them wall sit: back flat against a plain grey wall, knees bent 90 degrees, thighs parallel to the floor, arms crossed on the chest. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `calf` · 🦶 เขย่งปลายเท้า (Calf raises)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `calf_1.png`

```
Using the character in the reference image, draw them standing tall, feet flat, hands on hips. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `calf_2.png`

```
Using the character in the reference image, draw them standing tall up on the balls of the feet, heels raised high, hands on hips. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `splank` · 🧱 แพลงก์ด้านข้าง (Side plank)

**รูปอ้างอิง:** `art/ref_front.png`

**ภาพที่ 1** → `splank_1.png`

```
Using the character in the reference image, draw them side plank: propped on one straight arm, hips lifted so the body is a straight diagonal line, top arm raised to the ceiling. Front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `stepup` · 🪜 ก้าวขึ้นบันได (Step-ups)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `stepup_1.png`

```
Using the character in the reference image, draw them standing in front of a low wooden box, one foot placed on top of the box. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `stepup_2.png`

```
Using the character in the reference image, draw them standing fully on top of the low wooden box, both legs straight, arms by the sides. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

## คาร์ดิโอ / HIIT — ที่บ้าน

### `jacks` · ⭐ กระโดดตบ (Jumping jacks)

**รูปอ้างอิง:** `art/ref_front.png`

**ภาพที่ 1** → `jacks_1.png`

```
Using the character in the reference image, draw them standing, feet together, arms by the sides. Front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `jacks_2.png`

```
Using the character in the reference image, draw them jumping jack: feet wide apart, both arms straight overhead. Front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `knees` · 🏃 วิ่งยกเข่าสูง (High knees)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `knees_1.png`

```
Using the character in the reference image, draw them running in place, right knee driven up to hip height, arms bent and pumping. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `knees_2.png`

```
Using the character in the reference image, draw them running in place, left knee driven up to hip height, arms bent and pumping. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `climb` · ⛰️ เมาท์เทนไคลม์เบอร์ (Mountain climbers)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `climb_1.png`

```
Using the character in the reference image, draw them high plank on straight arms, right knee pulled toward the chest, left leg straight back. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `climb_2.png`

```
Using the character in the reference image, draw them high plank on straight arms, left knee pulled toward the chest, right leg straight back. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `skater` · ⛸️ สเก็ตเตอร์ (Skaters)

**รูปอ้างอิง:** `art/ref_front.png`

**ภาพที่ 1** → `skater_1.png`

```
Using the character in the reference image, draw them landing on the right foot, left foot swept behind the right leg, arms swinging across the body to the right. Front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `skater_2.png`

```
Using the character in the reference image, draw them landing on the left foot, right foot swept behind the left leg, arms swinging across the body to the left. Front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `burpee` · 🔥 เบอร์พี (Burpees)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `burpee_1.png`

```
Using the character in the reference image, draw them deep squat with both hands on the floor in front of the feet. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `burpee_2.png`

```
Using the character in the reference image, draw them high plank on straight arms, body straight. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 3** → `burpee_3.png`

```
Using the character in the reference image, draw them jumping up from the floor, both arms straight overhead. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `box` · 🥊 ชกลม (Shadow boxing)

**รูปอ้างอิง:** `art/ref_three_quarter.png`

**ภาพที่ 1** → `box_1.png`

```
Using the character in the reference image, draw them boxing stance with fists by the chin, right arm punching straight forward. Three-quarter front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `box_2.png`

```
Using the character in the reference image, draw them boxing stance with fists by the chin, left arm punching straight forward. Three-quarter front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

## เวท — ยิม

### `gsquat` · 🏋️ สควอทดัมเบล (Goblet squats)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `gsquat_1.png`

```
Using the character in the reference image, draw them standing tall, holding one dumbbell vertically against the chest with both hands. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `gsquat_2.png`

```
Using the character in the reference image, draw them goblet squat bottom: thighs parallel to the floor, chest up, dumbbell held at the chest, elbows inside the knees. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `bench` · 🏋️ ดันอก (เบนช์เพรส) (Bench press)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `bench_1.png`

```
Using the character in the reference image, draw them lying on a flat gym bench, feet on the floor, two dumbbells pressed straight up above the chest. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `bench_2.png`

```
Using the character in the reference image, draw them lying on a flat gym bench, dumbbells lowered to chest level, elbows bent below the bench line. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `row` · 🚣 ซีทเต็ดโรว์ (Seated row)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `row_1.png`

```
Using the character in the reference image, draw them sitting upright at a seated cable row machine, feet on the footplate, arms straight forward holding the handle. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `row_2.png`

```
Using the character in the reference image, draw them sitting upright, handle pulled to the belly, elbows behind the body, shoulder blades squeezed. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `latpd` · ⬇️ แลตพูลดาวน์ (Lat pulldown)

**รูปอ้างอิง:** `art/ref_three_quarter.png`

**ภาพที่ 1** → `latpd_1.png`

```
Using the character in the reference image, draw them sitting at a lat pulldown machine, thighs under the pads, arms straight up holding a wide bar. Three-quarter front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `latpd_2.png`

```
Using the character in the reference image, draw them bar pulled down to the upper chest, elbows pointing down and slightly back. Three-quarter front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `ohp` · 🏋️ ดันไหล่ดัมเบล (Shoulder press)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `ohp_1.png`

```
Using the character in the reference image, draw them sitting on an upright gym bench, two dumbbells held at shoulder height, palms forward. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `ohp_2.png`

```
Using the character in the reference image, draw them dumbbells pressed straight overhead, arms straight. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `rdl` · 🏋️ โรมาเนียนเดดลิฟต์ (Romanian deadlift)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `rdl_1.png`

```
Using the character in the reference image, draw them standing tall holding two dumbbells in front of the thighs. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `rdl_2.png`

```
Using the character in the reference image, draw them Romanian deadlift: hips pushed back, flat back nearly parallel to the floor, knees slightly bent, dumbbells at mid-shin. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `lpress` · 🦵 เลกเพรส (Leg press)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `lpress_1.png`

```
Using the character in the reference image, draw them sitting in a leg press machine, back on the backrest, feet on the platform, knees bent close to the chest. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `lpress_2.png`

```
Using the character in the reference image, draw them legs pushed out almost straight against the platform, knees not locked. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `curl` · 💪 ดัมเบลเคิร์ล (Biceps curls)

**รูปอ้างอิง:** `art/ref_front.png`

**ภาพที่ 1** → `curl_1.png`

```
Using the character in the reference image, draw them standing tall, dumbbells hanging by the sides, palms forward. Front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `curl_2.png`

```
Using the character in the reference image, draw them dumbbells curled up to the shoulders, elbows tucked at the sides. Front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `triext` · 💪 ไทรเซปส์เอ็กซ์เทนชัน (Triceps extension)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `triext_1.png`

```
Using the character in the reference image, draw them standing tall, one dumbbell held overhead with both hands, arms straight. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `triext_2.png`

```
Using the character in the reference image, draw them elbows bent, dumbbell lowered behind the head, upper arms pointing up. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

## คาร์ดิโอแบบต่อเนื่อง

### `walk` · 🚶 เดินเร็ว (Brisk walk)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `walk_1.png`

```
Using the character in the reference image, draw them walking, right leg forward, left arm forward. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `walk_2.png`

```
Using the character in the reference image, draw them walking, left leg forward, right arm forward. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `jog` · 🏃 วิ่งเหยาะ (Easy jog)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `jog_1.png`

```
Using the character in the reference image, draw them jogging, right knee forward, left foot pushing off behind, arms bent. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `jog_2.png`

```
Using the character in the reference image, draw them jogging, left knee forward, right foot pushing off behind, arms bent. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `bike` · 🚴 ปั่นจักรยาน (Cycling)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `bike_1.png`

```
Using the character in the reference image, draw them riding a stationary exercise bike, right pedal at the top, left pedal at the bottom. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `bike_2.png`

```
Using the character in the reference image, draw them riding a stationary exercise bike, left pedal at the top, right pedal at the bottom. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `ellip` · 🏃 เครื่องเดินวงรี (Elliptical)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `ellip_1.png`

```
Using the character in the reference image, draw them on an elliptical trainer, right foot forward, left hand pushing the handle. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `ellip_2.png`

```
Using the character in the reference image, draw them on an elliptical trainer, left foot forward, right hand pushing the handle. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

## ยืดเหยียด / คูลดาวน์

### `catcow` · 🐈 แมว-วัว (Cat-cow)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `catcow_1.png`

```
Using the character in the reference image, draw them on hands and knees, back arched down, head and tailbone lifted (cow). Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `catcow_2.png`

```
Using the character in the reference image, draw them on hands and knees, back rounded up toward the ceiling, chin tucked (cat). Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `child` · 🧘 ท่าเด็ก (Child's pose)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `child_1.png`

```
Using the character in the reference image, draw them child's pose: kneeling, hips sitting back on the heels, torso folded over the thighs, forehead on the floor, arms stretched forward on the floor. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `ham` · 🦵 ยืดต้นขาหลัง (Hamstring stretch)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `ham_1.png`

```
Using the character in the reference image, draw them sitting on the floor, one leg straight forward, the other knee bent with the foot near the hip, torso upright. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `ham_2.png`

```
Using the character in the reference image, draw them same seat, leaning forward from the hips reaching both hands toward the toes of the straight leg. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `hip` · 🧎 ยืดสะโพกด้านหน้า (Hip flexor stretch)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `hip_1.png`

```
Using the character in the reference image, draw them half kneeling: back knee on the floor, front foot flat with knee at 90 degrees, torso upright, hands on the front knee. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `hip_2.png`

```
Using the character in the reference image, draw them same position with the hips shifted forward to stretch the front of the back hip, torso upright. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `chest` · 🙆 ยืดอกและไหล่ (Chest & shoulder stretch)

**รูปอ้างอิง:** `art/ref_three_quarter.png`

**ภาพที่ 1** → `chest_1.png`

```
Using the character in the reference image, draw them standing tall, hands clasped behind the back, arms straight and lifted slightly, chest open. Three-quarter front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `twist` · 🌀 บิดลำตัวนอน (Lying twist)

**รูปอ้างอิง:** `art/ref_three_quarter.png`

**ภาพที่ 1** → `twist_1.png`

```
Using the character in the reference image, draw them lying on the back, arms out in a T on the floor, knees bent and pointing up. Three-quarter front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `twist_2.png`

```
Using the character in the reference image, draw them lying on the back, arms out in a T, both bent knees dropped to one side, head turned the other way. Three-quarter front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

### `cobra` · 🐍 ท่างู (Cobra)

**รูปอ้างอิง:** `art/ref_side.png`

**ภาพที่ 1** → `cobra_1.png`

```
Using the character in the reference image, draw them lying face down, hands on the floor under the shoulders, elbows bent. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

**ภาพที่ 2** → `cobra_2.png`

```
Using the character in the reference image, draw them cobra: chest pressed up on straight arms, hips on the floor, shoulders relaxed. Side view, facing right. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```

## ไม่บังคับ

### `rest` · 😮‍💨 พัก (Rest)

**รูปอ้างอิง:** `art/ref_front.png`

**ภาพที่ 1** → `rest_1.png`

```
Using the character in the reference image, draw them standing relaxed, holding a water bottle at chest height. Front view. Keep exactly the same character as the reference: same face, hair, pink t-shirt, navy shorts, white shoes, colours, proportions and flat 2D line style. Full body in frame, centered, same size as a standing character filling about 80% of the image height. Plain solid chroma-green background (#00FF00), no floor, no shadow, no text, square 1:1.
```
