"""Builds the smooth exercise clips (assets/anim/<id>.webp, 32 frames each) from the drawn key frames here.

Key frames per move: 1 start, 2 end, 3 middle, 4 between start and middle, 5 between middle and end.
In-between frames are made with optical flow (OpenCV DIS) plus a cross-fade. Each clip's length matches
the move's tempo in workout_rig.js. Needs: pip install opencv-python-headless numpy pillow
Run from the repo root: python3 art/exercise_keyframes/build_clips.py
"""
import os, cv2, numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, '..', '..', 'FoodWell_v89_Android_UI_Polish/android/app/src/main/assets/anim')
SIZE, FRAMES = 480, 32
CYCLE = {"armc": 1400, "box": 600, "bridge": 1400, "calf": 1200, "catcow": 1800, "climb": 600, "hip": 2200,
         "jacks": 660, "knees": 480, "lunge": 1300, "march": 1440, "neck": 2800, "plank": 2600, "push": 1300,
         "row": 1400, "skater": 960, "squat": 1400, "superman": 1400, "walk": 1520, "wallsit": 2400}
F8, F4 = [1, 4, 3, 5, 2, 5, 3, 4], [1, 3, 2, 3]
SEQ = {m: F8 for m in 'armc box bridge calf catcow climb hip jacks knees lunge march push row skater superman'.split()}
SEQ.update(squat=[1, 3, 5, 2, 5, 3], walk=[1, 4, 3, 2, 3, 4], neck=F4, plank=F4, wallsit=F4)

dis = cv2.DISOpticalFlow_create(cv2.DISOPTICAL_FLOW_PRESET_MEDIUM)

def load(mv, n):
    im = np.array(Image.open(os.path.join(HERE, f'{mv}_{n}.webp')).convert('RGB'))
    return cv2.resize(cv2.cvtColor(im, cv2.COLOR_RGB2BGR), (SIZE, SIZE), interpolation=cv2.INTER_AREA)

def flow(a, b):
    return dis.calc(cv2.cvtColor(a, cv2.COLOR_BGR2GRAY), cv2.cvtColor(b, cv2.COLOR_BGR2GRAY), None)

def warp(img, fl, s):
    h, w = fl.shape[:2]
    gx, gy = np.meshgrid(np.arange(w, dtype=np.float32), np.arange(h, dtype=np.float32))
    return cv2.remap(img, gx + s * fl[..., 0], gy + s * fl[..., 1], cv2.INTER_LINEAR, borderMode=cv2.BORDER_REPLICATE)

def tween(a, b, n):
    fab, fba = flow(a, b), flow(b, a)
    return [cv2.addWeighted(warp(a, fba, t), 1 - t, warp(b, fab, 1 - t), t, 0)
            for t in (k / (n + 1) for k in range(1, n + 1))]

for mv, seq in SEQ.items():
    keys = {n: load(mv, n) for n in set(seq)}
    per = max(0, round(FRAMES / len(seq)) - 1)
    frames = []
    for i in range(len(seq)):
        a, b = keys[seq[i]], keys[seq[(i + 1) % len(seq)]]
        frames += [a] + tween(a, b, per)
    frames = [Image.fromarray(cv2.cvtColor(f, cv2.COLOR_BGR2RGB)) for f in frames]
    ms = max(20, round(CYCLE[mv] / len(frames)))
    frames[0].save(os.path.join(OUT, mv + '.webp'), save_all=True, append_images=frames[1:],
                   duration=ms, loop=0, quality=70, method=6)
    print(mv, len(frames), 'frames', ms, 'ms')
