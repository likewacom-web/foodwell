# พรอมต์เจนรูปท่าออกกำลังกาย (แมว MeowFit)

แต่ละท่ามี **2 รูป**: **A = ท่าเริ่ม** และ **B = ท่าสุด** แอปจะสลับ A ↔ B ไปมาให้ดูเหมือนขยับ
ทำ 20 ท่าแรกก่อน (ท่าที่ใช้บ่อยที่สุดในโปรแกรม) ท่าที่ยังไม่มีรูปจะใช้ตัวการ์ตูนแมวแบบวาดด้วยโค้ดไปก่อน

## วิธีให้ได้แมวหน้าตาเดียวกันทุกรูป (สำคัญมาก)
1. เจน **squat_a** เป็นรูปแรก จนได้แมวที่ชอบ → ใช้รูปนี้เป็น **รูปอ้างอิงตัวละคร** ทุกครั้งต่อจากนี้
   (ChatGPT: แนบรูปแล้วพิมพ์ "same character, same style" · Midjourney: `--cref <ลิงก์รูป> --sref <ลิงก์รูป>` · Gemini/Ideogram: แนบรูปอ้างอิง)
2. ตอนเจน **B** ของแต่ละท่า ให้แนบรูป **A ของท่านั้น** ด้วย แล้วบอก "same character, same camera, same size, only the pose changes"
3. ขนาด **1024×1024** สี่เหลี่ยมจัตุรัส · พื้นหลัง**สีชมพูอ่อนเรียบ** ไม่มีลวดลาย
4. ตั้งชื่อไฟล์ตามช่อง "ไฟล์" แล้ว zip ส่งมาให้ Claude (จะครอป ย่อ บีบอัด และใส่ในแอปให้เอง)

## [CHARACTER] — วางต่อท้ายทุกพรอมต์
```
cute kawaii chibi cartoon illustration of the MeowFit mascot: a small fluffy white cat with round head, pink inner ears, pink cheeks and big shiny dark eyes, standing on two legs like a person, wearing a coral-pink sporty t-shirt, dark navy leggings and pink sneakers, white fluffy tail; full body visible, seen from the side facing right unless stated, character centered and filling about 70% of the image height, feet resting on an invisible floor line near the bottom, soft clean shading, smooth thick outlines, plain solid very light pink background (#FFF1F5), no floor pattern, no text, no letters, no watermark, square 1024x1024
```

## พรอมต์รายท่า
> รูปแบบ: `<ท่า> , [CHARACTER]`

| # | ไฟล์ | ท่า | A — ท่าเริ่ม | B — ท่าสุด |
|---|---|---|---|---|
| 1 | `squat_a` / `squat_b` | สควอท / Squat | `standing tall, feet shoulder-width apart, arms relaxed at the sides` | `deep squat, hips pushed back and down as if sitting on a chair, thighs parallel to the floor, back straight, both arms stretched straight forward for balance` |
| 2 | `walk_a` / `walk_b` | เดิน / Walk | `walking briskly, right leg forward heel touching the floor, left leg behind, arms swinging opposite` | `walking briskly, left leg forward, right leg behind, arms swinging the other way` |
| 3 | `plank_a` / `plank_b` | แพลงก์ / Plank | `forearm plank seen from the side, elbows under the shoulders, body in one straight line from head to heels, head to the right` | `same forearm plank, tummy tight, a small determined smile, tiny sweat drop` |
| 4 | `jacks_a` / `jacks_b` | กระโดดตบ / Jumping jacks | `seen from the front, standing with feet together and arms down at the sides` | `seen from the front, jumping with legs spread wide and both arms raised overhead, paws touching, happy face` |
| 5 | `march_a` / `march_b` | ย่ำเท้า / March in place | `marching in place, right knee lifted to hip height, left arm swinging forward` | `marching in place, left knee lifted to hip height, right arm swinging forward` |
| 6 | `climb_a` / `climb_b` | เมาท์เทนไคลม์เบอร์ / Mountain climbers | `high plank on straight arms, head to the right, right knee pulled in toward the chest` | `high plank on straight arms, head to the right, left knee pulled in toward the chest, right leg extended back` |
| 7 | `box_a` / `box_b` | ชกลม / Shadow boxing | `boxing stance facing right, knees slightly bent, both paws up guarding the face` | `throwing a straight punch with the front paw fully extended to the right, rear paw guarding the chin` |
| 8 | `push_a` / `push_b` | วิดพื้น / Push-up | `top of a push-up, arms straight, body in a straight line, head to the right` | `bottom of a push-up, elbows bent, chest close to the floor, body still straight` |
| 9 | `lunge_a` / `lunge_b` | ลันจ์ / Lunge | `standing tall, paws on the hips` | `reverse lunge, right foot forward with knee bent 90 degrees, left knee lowered near the floor behind, paws on the hips, torso upright` |
| 10 | `knees_a` / `knees_b` | วิ่งยกเข่าสูง / High knees | `running in place, right knee driven up to hip height, arms pumping` | `running in place, left knee driven up to hip height, arms pumping the other way` |
| 11 | `bridge_a` / `bridge_b` | ยกสะโพก / Glute bridge | `lying on the back, head to the left, knees bent, feet flat on the floor, arms on the floor` | `same position but hips lifted high so shoulders, hips and knees form a straight line` |
| 12 | `skater_a` / `skater_b` | สเก็ตเตอร์ / Skaters | `seen from the front, landing on the right leg, left leg crossed behind, arms swinging to the right` | `seen from the front, landing on the left leg, right leg crossed behind, arms swinging to the left` |
| 13 | `catcow_a` / `catcow_b` | แคท-คาว / Cat-cow | `on hands and knees, head to the right, back arched up high like a scared cat, chin tucked` | `on hands and knees, back gently dipped down, chest and head lifted, looking forward` |
| 14 | `calf_a` / `calf_b` | เขย่งปลายเท้า / Calf raises | `standing tall, feet flat, paws on the hips` | `rising high onto the tiptoes, heels lifted, paws on the hips` |
| 15 | `armc_a` / `armc_b` | หมุนแขน / Arm circles | `seen from the front, arms stretched out to the sides at shoulder height` | `seen from the front, arms out to the sides drawing small circles, light motion lines around the paws` |
| 16 | `wallsit_a` / `wallsit_b` | นั่งพิงกำแพง / Wall sit | `standing with the back against a plain light-grey wall on the left side` | `sliding down the wall into a seated position, thighs parallel to the floor, knees at 90 degrees, arms crossed, back flat against the wall` |
| 17 | `superman_a` / `superman_b` | ซูเปอร์แมน / Superman | `lying face down, head to the right, arms stretched forward, legs straight` | `face down with arms, chest and legs lifted off the floor at the same time like flying` |
| 18 | `row_a` / `row_b` | ซีทเต็ดโรว์ / Seated row | `sitting on a simple grey cable-row machine bench facing right, arms extended forward holding the handle, back straight` | `same machine, handle pulled to the belly, elbows bent back, shoulder blades squeezed` |
| 19 | `neck_a` / `neck_b` | ยืดคอ / Neck stretch | `seen from the front, standing relaxed, head tilted gently to the left, eyes closed calmly` | `seen from the front, head tilted gently to the right, eyes closed calmly` |
| 20 | `hip_a` / `hip_b` | ยืดสะโพก / Hip flexor stretch | `kneeling lunge, right foot forward, left knee on a small soft mat, torso upright` | `same kneeling lunge, hips pushed gently forward, arms raised overhead for a deeper stretch` |

## เคล็ดลับ
- ถ้าแมวหันผิดทาง: เติม `facing right, profile view` (หรือ `front view` สำหรับท่าที่บอกว่าเห็นจากด้านหน้า)
- ถ้าตัวใหญ่หรือเล็กไม่เท่ากันระหว่าง A กับ B: ย้ำ `same character size and same camera distance as the reference image`
- ถ้าขาออกมาเกินหรือขาด (ปัญหาที่ AI ชอบเป็น): เติม `exactly two arms and two legs, anatomically clear pose`
- ไม่ต้องลบพื้นหลังเอง Claude จะจัดการให้เข้ากับแอปทั้งโหมดสว่างและมืด
