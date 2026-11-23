# 📜 Devlog 20: Character Sprite Animations, Height Normalization & Ground Alignment

* **Date:** November 23, 2026
* **Author:** Dorothy Anne R. Domanico (Lead Designer & Asset Developer)
* **Sprint / Milestone:** Week 6 – Animated Hero Pixel Sprites & Realistic Walking Locomotion
* **Branch:** `feat/board-adventure-screen`

---

## 🎯 Objectives
- Replace static character placeholder graphics with true animated pixel character spritesheets for all 4 hero archetypes:
  - **Turtle (Iron Guardian)**: 4-frame deliberate walk cycle.
  - **Raccoon (Crafty Scavenger)**: 6-frame agile walk cycle.
  - **Fox (Cunning Trickster)**: 6-frame sleek trot cycle.
  - **Cat (Nimble Prowler)**: 3-frame dynamic stride cycle.
- **Visual Height Normalization**: Proportionally enlarge the turtle, fox, and raccoon sprites so that their visual standing height matches the cat (~110 px in Frame 0):
  - **Cat**: $1.75\times$ scale (Frame 0: 158 × 111 px).
  - **Turtle**: $2.20\times$ scale (Frame 0: 194 × 110 px).
  - **Fox**: $2.29\times$ scale (Frame 0: 142 × 110 px).
  - **Raccoon**: $2.56\times$ scale (Frame 0: 131 × 110 px).
- Standardize all character assets inside uniform **`224×224 px`** frame cells:
  - Anchored on a unified ground contact baseline (`maxY = 183`).
  - Centered horizontally (`centerX = 112`).
  - Extracted clean `224×224 px` Frame 0 avatars as the static hero portraits.
- Transition board player movement from a parabolic hopping arc into realistic, ground-aligned **walking locomotion** across the stepping stones.
- Fix character rendering in `BoardScreen.java`:
  - Resolved TextureRegion slicing bug where `split(sheet, 128, 128)` cut off the bottom 96px of the character.
  - Calibrated character display size (`CHAR_DISPLAY_SIZE = 210f`) and vertical anchor (`CHAR_DRAW_Y = TILE_BOTTOM_Y + 14f`), seating paws firmly on top of the stone slab.
- Integrate animated character walk cycles into both `BoardScreen` and `MainMenuScreen`.

---

## 🛠️ Work Completed

### 1. Proportional Height Normalization & Asset Pipeline
- Analyzed the raw source art and identified initial height disparities: Cat was 63px raw, Turtle 50px, Fox 48px, Raccoon 43px.
- Calibrated custom scaling factors with 100% pixel sharpness using nearest-neighbor sampling:
  - `char_cat_walk.png` (672×224, 3 frames @ $1.75\times$).
  - `char_turtle_walk.png` (896×224, 4 frames @ $2.20\times$).
  - `char_fox_walk.png` (1344×224, 6 frames @ $2.29\times$).
  - `char_raccoon_walk.png` (1344×224, 6 frames @ $2.56\times$).
- Sliced and aligned every frame to a universal ground contact line at `maxY = 183` and horizontal center at `x = 112`.
- Generated 224×224 static hero avatars (`char_*.png`) from Frame 0.

### 2. Assets Registry & Engine Integration
- `Assets.java`:
  - Declared `CHAR_FRAME_SIZE = 224`.
  - Registered descriptors: `CHAR_CAT_WALK`, `CHAR_TURTLE_WALK`, `CHAR_FOX_WALK`, `CHAR_RACCOON_WALK`.
  - Helper functions `getCharacterWalkSheet()` and `getCharacterWalkFrameCount()`.

### 3. Realistic Walking Locomotion & Ground Seating on the 2D Board
- Fixed `initCharacterAnimations()` in `BoardScreen.java` to split by `Assets.CHAR_FRAME_SIZE` (224).
- Enlarged in-game rendering size to `CHAR_DISPLAY_SIZE = 210f` (75% larger than original 120f).
- Set `CHAR_DRAW_Y = TILE_BOTTOM_Y + 14f`:
  - With feet at $Y = 183$ in the 224px cell (41px above bottom), the foot contact point is $244\text{f} + 41 \times (210 / 224) = 282.4\text{f}$.
  - Matches the top walking surface of the stone slab ($282.25\text{f}$), eliminating sunken or floating sprites.
- In `renderPlayers()`:
  - Active walking: animates walk cycles smoothly between stepping stone centers during `STEPPING_TILES`.
  - Active standing: displays Frame 0 idle pose firmly seated on the stone slab.
  - Inactive multiplayer layering: displays Frame 0 at `186f` size with depth tinting when sharing a tile.

### 4. Live Hero Preview in Character Selection Carousel
- `MainMenuScreen.java` splits by `Assets.CHAR_FRAME_SIZE` and renders preview image at `165×165`.
- All 4 hero archetypes now present with identical visual standing height (~110px).

---

## 🔍 Verification & Testing
- `.\gradlew.bat compileJava`: Compiled cleanly with zero errors.
- `.\gradlew.bat processResources`: Synchronized all normalized 224×224 textures to `lwjgl3/build/resources/main/img`.
- Slicing and pixel integrity verified: all frames maintain 100% pixel fidelity with zero distortion, clipping, or blurring.
