# พรอมต์สำหรับเจนอนิเมชันท่าออกกำลังกาย

ใช้คู่กับ [`EXERCISES.md`](EXERCISES.md) · ชื่อไฟล์ผลลัพธ์ = **id** ของท่า เช่น `squat.webp` · ไฟล์ CSV: [`exercise_prompts.csv`](exercise_prompts.csv)

## ขั้นตอนแนะนำ

1. **สร้างตัวละครต้นแบบก่อน 1 ภาพ** ด้วยพรอมต์ "ตัวละครต้นแบบ" ด้านล่าง แล้วเลือกภาพที่ชอบที่สุด
   - ✅ ทำแล้ว: `art/character_ai_turnaround.jpg` · ภาพแยกมุมพร้อมใช้ (ไม่มีตัวหนังสือ, 1024×1024): `art/ref_front.png` (ท่ามุมหน้า), `art/ref_side.png` (ท่ามุมข้าง), `art/ref_three_quarter.png` (ท่ามุมเฉียง)
2. **เจนทีละท่าแบบ image-to-video** โดยใส่ภาพต้นแบบเป็นภาพเริ่มต้นหรือภาพอ้างอิงตัวละคร (character reference) แล้วใช้พรอมต์ของท่านั้น ตัวละครจะได้หน้าตาเหมือนกันทุกไฟล์
3. ถ้าเครื่องมือมีช่อง negative prompt ให้ใส่ข้อความในหัวข้อ "Negative prompt"
4. ตรวจท่าทางกับคอลัมน์ "วิธีทำ" ใน EXERCISES.md โดยเฉพาะหลังตรง เข่าไม่บิด และศอกถูกมุม ท่าที่ผิดอาจทำให้ผู้ใช้บาดเจ็บ
5. แปลงเป็น WebP เคลื่อนไหว พื้นหลังโปร่งใส (คำสั่งด้านล่าง) แล้ววางที่ `android/app/src/main/assets/anim/<id>.webp`

> พื้นหลังเขียวสด (#00FF00) ใช้สำหรับตัดพื้นหลังออกภายหลัง จึงห้ามใส่สีเขียวในตัวละคร (เสื้อจึงเป็นสีชมพูตามสีแอป)

## ตัวละครต้นแบบ

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Character turnaround reference sheet: the same character standing relaxed in front view, side view and three-quarter view, arms by the sides.
```

## Negative prompt

```
text, letters, logo, watermark, extra limbs, extra fingers, distorted anatomy, cropped body, multiple people, background scenery, camera movement, motion blur, realistic photo, 3D render
```

## วอร์มอัพ

### `march` · 🚶 ย่ำเท้าอยู่กับที่ (March in place)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Front view, static camera. The character performs one repetition of marching in place: lifts the right knee to hip height while swinging the left arm forward, then switches sides, steady relaxed rhythm. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `armc` · 🙆 หมุนแขน (Arm circles)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Front view, static camera. The character performs one repetition of standing tall with arms straight out to the sides at shoulder height, drawing small forward circles with both arms that grow into big circles. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `legsw` · 🦵 เหวี่ยงขา (Leg swings)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of standing next to a wall with one hand resting on it, swinging the outside leg forward and back like a pendulum, upper body stays upright. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

## เวท — ที่บ้าน

### `squat` · 🦵 สควอท (Squats)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of bodyweight squat: feet shoulder-width, arms reach forward for balance, hips sit back and down until thighs are parallel to the floor, chest up, knees track over toes, then stands back up. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `kpush` · 💪 วิดพื้นแบบคุกเข่า (Knee push-ups)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of knee push-up: kneeling with hands wider than shoulders, straight line from head to knees, lowers chest toward the floor by bending elbows, then presses back up. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `push` · 💪 วิดพื้น (Push-ups)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of full push-up: straight plank line from head to heels, lowers chest close to the floor with elbows at about 45 degrees, then presses back up. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `bridge` · 🍑 ยกสะโพก (Glute bridges)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of glute bridge: lying on the back, knees bent, feet flat, arms by the sides, lifts hips up until knees, hips and shoulders form a straight line, holds briefly, lowers. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `lunge` · 🚶 ลันจ์ถอยหลัง (Reverse lunges)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of reverse lunge: steps one foot back and lowers until both knees bend about 90 degrees with the back knee just above the floor, torso upright, returns to standing. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `plank` · 🧱 แพลงก์ (Plank)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of forearm plank: elbows under shoulders, body in a straight line from head to heels, core braced; subtle breathing motion only, hips stay level. Very small movement, mostly a held pose with gentle breathing. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `superman` · 🦸 ซูเปอร์แมน (Superman)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of superman: lying face down, arms stretched overhead, lifts arms, chest and legs a few centimetres off the floor at the same time, holds, lowers. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `deadbug` · 🐞 เดดบัก (Dead bug)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Three-quarter front view, static camera. The character performs one repetition of dead bug: lying on the back with arms pointing to the ceiling and knees bent at 90 degrees above the hips, slowly extends the right arm overhead and the left leg straight out, returns, low back stays on the floor. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `dips` · 🪑 ดิปกับเก้าอี้ (Chair dips)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of chair dip: hands on the front edge of a sturdy chair behind the body, legs bent with feet on the floor, bends elbows to lower the hips toward the floor, then presses back up. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `wallsit` · 🧱 นั่งพิงกำแพง (Wall sit)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of wall sit: back flat against a wall, knees bent at 90 degrees with thighs parallel to the floor, arms crossed on the chest; holds still with calm breathing. Very small movement, mostly a held pose with gentle breathing. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `calf` · 🦶 เขย่งปลายเท้า (Calf raises)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of calf raise: standing tall, rises slowly onto the balls of the feet as high as possible, pauses, lowers the heels back down. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `splank` · 🧱 แพลงก์ด้านข้าง (Side plank)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Front view, static camera. The character performs one repetition of side plank: lying on one side propped on the forearm, lifts the hips so the body forms a straight line from head to feet, top hand on hip; holds with subtle breathing. Very small movement, mostly a held pose with gentle breathing. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `stepup` · 🪜 ก้าวขึ้นบันได (Step-ups)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of step-up: steps up onto a low sturdy box with the right foot, drives up to stand fully on the box, steps back down, alternating legs. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

## คาร์ดิโอ / HIIT — ที่บ้าน

### `jacks` · ⭐ กระโดดตบ (Jumping jacks)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Front view, static camera. The character performs one repetition of jumping jack: jumps feet out wide while raising both arms overhead, then jumps back to feet together with arms by the sides, light and bouncy. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `knees` · 🏃 วิ่งยกเข่าสูง (High knees)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of high knees: running in place, driving each knee up to hip height in quick alternation, arms pumping. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `climb` · ⛰️ เมาท์เทนไคลม์เบอร์ (Mountain climbers)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of mountain climbers: in a high plank on straight arms, drives the right knee toward the chest then quickly switches to the left, hips stay low. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `skater` · ⛸️ สเก็ตเตอร์ (Skaters)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Front view, static camera. The character performs one repetition of skater hops: leaps sideways onto the right foot, sweeping the left foot behind and the arms across the body, then leaps to the left side. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `burpee` · 🔥 เบอร์พี (Burpees)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of burpee: squats down, places hands on the floor, jumps feet back into a plank, jumps feet back to the hands, then jumps up with arms overhead. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `box` · 🥊 ชกลม (Shadow boxing)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Three-quarter front view, static camera. The character performs one repetition of shadow boxing: fighting stance with soft knees and fists up by the chin, throws alternating straight punches, light bouncing footwork. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

## เวท — ยิม

### `gsquat` · 🏋️ สควอทดัมเบล (Goblet squats)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of goblet squat: holds one dumbbell vertically against the chest with both hands, squats down until thighs are parallel, elbows inside the knees, stands back up. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `bench` · 🏋️ ดันอก (เบนช์เพรส) (Bench press)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of dumbbell bench press: lying on a flat gym bench, presses two dumbbells from chest level straight up over the shoulders, then lowers them slowly. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `row` · 🚣 ซีทเต็ดโรว์ (Seated row)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of seated cable row: sitting upright at a cable row machine, feet on the platform, pulls the handle to the belly, squeezing the shoulder blades, then extends the arms forward. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `latpd` · ⬇️ แลตพูลดาวน์ (Lat pulldown)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Three-quarter front view, static camera. The character performs one repetition of lat pulldown: seated at a lat pulldown machine, thighs under the pads, pulls the wide bar down to the upper chest with elbows pointing down, then lets it rise slowly. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `ohp` · 🏋️ ดันไหล่ดัมเบล (Shoulder press)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Front view, static camera. The character performs one repetition of seated dumbbell shoulder press: sitting on an upright bench, presses two dumbbells from shoulder height to overhead, then lowers them to the shoulders. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `rdl` · 🏋️ โรมาเนียนเดดลิฟต์ (Romanian deadlift)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of Romanian deadlift: holding two dumbbells in front of the thighs, knees slightly bent, hinges at the hips pushing them back with a flat back until the dumbbells reach mid-shin, then stands up. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `lpress` · 🦵 เลกเพรส (Leg press)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of leg press: seated in a leg press machine, feet shoulder-width on the platform, bends knees to bring the platform toward the chest, then pushes it away without locking the knees. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `curl` · 💪 ดัมเบลเคิร์ล (Biceps curls)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Front view, static camera. The character performs one repetition of standing dumbbell biceps curl: elbows tucked at the sides, curls both dumbbells up toward the shoulders, then lowers them slowly. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `triext` · 💪 ไทรเซปส์เอ็กซ์เทนชัน (Triceps extension)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of overhead triceps extension: standing, holds one dumbbell with both hands overhead, bends the elbows to lower it behind the head, then straightens the arms. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

## คาร์ดิโอแบบต่อเนื่อง

### `walk` · 🚶 เดินเร็ว (Brisk walk)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of brisk walking in place with a purposeful stride and natural arm swing (treadmill-style, no forward travel). Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `jog` · 🏃 วิ่งเหยาะ (Easy jog)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of easy jogging in place with relaxed arms and a light, short stride (treadmill-style, no forward travel). Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `bike` · 🚴 ปั่นจักรยาน (Cycling)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of riding a stationary bike at a steady pace, hands on the handlebars, pedals turning smoothly. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `ellip` · 🏃 เครื่องเดินวงรี (Elliptical)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of using an elliptical trainer machine, feet on the pedals moving in smooth ovals, hands on the moving handles. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

## ยืดเหยียด / คูลดาวน์

### `catcow` · 🐈 แมว-วัว (Cat-cow)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of cat-cow: on hands and knees, slowly arches the back with the belly lowering and the head lifting (cow), then rounds the back upward with the chin tucked (cat). Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `child` · 🧘 ท่าเด็ก (Child's pose)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of child's pose: kneeling, sits back on the heels and folds forward with arms stretched long on the floor and forehead down; slow breathing motion. Very small movement, mostly a held pose with gentle breathing. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `ham` · 🦵 ยืดต้นขาหลัง (Hamstring stretch)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of seated hamstring stretch: sitting with one leg straight out and the other bent, leans forward from the hips reaching toward the toes of the straight leg, holds. Very small movement, mostly a held pose with gentle breathing. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `hip` · 🧎 ยืดสะโพกด้านหน้า (Hip flexor stretch)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of half-kneeling hip flexor stretch: one knee on the floor and the other foot in front, gently shifts the hips forward with the torso upright, hands on the front knee, holds. Very small movement, mostly a held pose with gentle breathing. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `chest` · 🙆 ยืดอกและไหล่ (Chest & shoulder stretch)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Three-quarter front view, static camera. The character performs one repetition of chest and shoulder stretch: standing, clasps the hands behind the back, straightens the arms and lifts them slightly while opening the chest, holds. Very small movement, mostly a held pose with gentle breathing. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `twist` · 🌀 บิดลำตัวนอน (Lying twist)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Three-quarter front view, static camera. The character performs one repetition of lying spinal twist: lying on the back with arms out in a T, drops both bent knees to one side while turning the head to the other side, holds. Very small movement, mostly a held pose with gentle breathing. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

### `cobra` · 🐍 ท่างู (Cobra)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Side view, static camera. The character performs one repetition of cobra: lying face down with hands under the shoulders, gently presses the chest up while the hips stay on the floor, shoulders relaxed, then lowers. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

## ไม่บังคับ

### `rest` · 😮‍💨 พัก (Rest)

```
Flat 2D vector illustration of a friendly adult fitness character, gender-neutral, short dark hair, coral pink t-shirt (#E887AA), navy blue shorts, white sneakers, simple rounded shapes, soft cel shading, clean thin dark outlines, consistent body proportions, full body always in frame, centered, plain solid chroma-green background (#00FF00), no floor shadow. Front view, static camera. The character performs one repetition of standing relaxed, taking a deep breath and sipping from a water bottle. Smooth seamless loop: the last frame matches the first frame. Duration 3 seconds, square 1:1.
```

## แปลงวิดีโอเป็น WebP พื้นหลังโปร่งใส

ใช้ [ffmpeg](https://ffmpeg.org) (ฟรี) แปลงวิดีโอที่เจนได้ (`squat.mp4`) เป็นไฟล์สำหรับแอป:

```bash
ffmpeg -i squat.mp4 -vf "chromakey=0x00FF00:0.18:0.08,fps=15,scale=512:512:force_original_aspect_ratio=decrease,pad=512:512:-1:-1:color=0x00000000" \
  -c:v libwebp -lossless 0 -q:v 70 -loop 0 -an squat.webp
```

แปลงทุกไฟล์ในโฟลเดอร์พร้อมกัน:

```bash
for f in *.mp4; do ffmpeg -y -i "$f" -vf "chromakey=0x00FF00:0.18:0.08,fps=15,scale=512:512:force_original_aspect_ratio=decrease,pad=512:512:-1:-1:color=0x00000000" -c:v libwebp -lossless 0 -q:v 70 -loop 0 -an "${f%.mp4}.webp"; done
```

- ขนาดไฟล์ควรอยู่ราว 100–400 KB ต่อท่า (ทั้งหมดไม่ควรเกิน ~10 MB เพราะรวมอยู่ใน APK)
- ถ้าขอบตัวละครยังมีสีเขียวติด ให้ลดหรือเพิ่มตัวเลข `0.18` ทีละน้อย
