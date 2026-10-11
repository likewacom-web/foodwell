# Exercise pictures still to draw

The first 20 moves are finished (key frames in this folder, clips in assets/anim). The other 47 moves still use
the vector cat (workout_rig.js). Their poses are listed in `poses_todo.py` (view, start pose A, end pose B).

## How to continue
1. For each move generate A, then B (with A as the image reference, so props and character match), then the middle
   frame M (references A and B, "exactly halfway between"). Prompt style: see EXERCISE_ART_PROMPTS.md / earlier ones.
   Character references (Canva media ids): side `MAHXnc0RAeM` (squat_a), `MAHXncQFE-o` (walk_a); front `MAHXnQubxXo`
   (jacks_a); floor `MAHXnR4Yj2k` (plank_a), `MAHXnWXeysE` (bridge_a); gym `MAHXnjOqTmg` (row_a).
2. Put them on 4000x5000 pages (4x5 grid of 1000 px) in Canva design DAHXnp9Vwfo, download the pages as PNG.
3. Slice to `<id>_1.webp` (A), `<id>_2.webp` (B), `<id>_3.webp` (M), add the move to SEQ/CYCLE in build_clips.py
   with order [1,3,2,3], run it, and add `<id>:'v'` to ANIM in index.html.

## Already generated in Canva (media ids)
- `legsw_a` MAHXoqXDq-Q
- `kpush_a` MAHXojEfNPg
- `dips_a` MAHXpvDZClU
- `deadbug_a` MAHXpzt3iU4
