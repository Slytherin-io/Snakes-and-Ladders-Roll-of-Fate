# 📓 Devlog 19: Loading Screen & 2D Side-Scrolling Adventure Board

* **Date:** November 23, 2026
* **Author:** Dorothy Anne R. Domanico (Lead Designer & Asset Developer)
* **Sprint / Milestone:** Week 6 – Loading Screen, 2D Adventure Board & In-Game HUD Refinements
* **Branch:** `feat/board-adventure-screen`

---

## 🎯 Objectives
- Implement an animated **Loading Screen** with a 36-frame pixel snake spritesheet loop (`loading_snake_spritesheet.png`) bridging the transition from Hero Selection into gameplay.
- Transition the board view from a static top-down grid into a **2D Side-Scrolling Adventure Trail** spanning 3 visual Acts:
  - **Act I**: Lush Green Forest (Tiles 1–30)
  - **Act II**: Deep Sapphire Twilight Forest (Tiles 31–70)
  - **Act III**: Crimson Blood-Moon Twilight Abyss (Tiles 71–100)
- Implement **Option C Grounded Stepping Stones**: stone slabs embedded naturally into the terrain turf across all 100 tiles.
- Implement **HUD-Only Tile Numbering**: remove tile numbers from the world stepping stones to keep the trail immersive and ancient, relying on the top-left Mini-Map pill as the single source of truth.
- Implement **Multiplayer Same-Tile Layering**: ensure the active player is always rendered on the topmost layer in front of other players and centered when multiple pawns share a tile.
- Build and refine the complete **In-Game HUD Overlay**:
  - Top-left 100-Tile linear Mini-Map with 3-Act color segment indicators.
  - Top ribbon guidance banner showing active player turn status and tile announcements.
  - In-Game Pause modal with volume sliders and leave game confirmation.
  - Left Inventory satchel button with an interactive 4-slot inventory popup.
  - Center D6 Roll button with animated dice tumbling.
  - Right Active Player Status Card with solid color styling and a clear 3-tier textual hierarchy.

---

## 🛠️ Work Completed

### 1. Animated Loading Screen
- Built `LoadingScreen.java` integrating `loading_snake_spritesheet.png` (36 frames organized in a 6×6 grid).
- Configured animation timing at 0.045s per frame with `Animation.PlayMode.LOOP`.
- Added atmospheric background dimming and a clean subtext label (`"ENTERING THE ROLL OF FATE..."`) centered beneath the looping serpent.
- Connected the transition flow seamlessly in `RollOfFateGame.java`, passing a completion callback that loads `BoardScreen`.

### 2. 2D Side-Scrolling Parallax Environments
- Sliced and composited multi-layer parallax backgrounds across the 3 Acts:
  - **Layer 3 (Far Canopy & Sky)**: High-altitude parallax backdrop (0.15× scroll) extending beyond the screen top.
  - **Layer 2 (Midground Silhouettes & Ruins)**: Distant ancient tree trunks and stone arches (0.35× scroll) tucked behind the ground platform.
  - **Layer 1 (Foreground Terrain Platform)**: Flat turf ledge (1.0× world scroll) embedding the stepping stones.
  - **Layer 0 (Subterranean Foundation)**: Solid dark bedrock base (`#20202a` for Act 1, `#0a204f` for Act 2, `#2e1534` for Act 3) supporting the bottom HUD console.
- Fixed asset slicing boundaries (`y = 464, h = 110`) and applied a 2-pixel horizontal overlap during tiling, eliminating all transparent edge slits and the horizontal line leak in Acts 2 and 3.

### 3. Grounded Stepping Stones (Option C) & HUD-Only Numbering
- Embedded 100 stone slabs directly along the terrain turf at `y = 230f` with `180 × 55 px` dimensions and `230 px` spacing.
- **HUD-Only Numbering**: Removed stamped numbers from regular stone slabs in the world, creating an ancient, uncluttered trail.
- Special tiles retain dedicated visual badges centered on the stone:
  - Golden `+ LADDER` badge on ladder ascent tiles.
  - Crimson `- SNAKE` badge on serpent ambush tiles.

### 4. Player Hopping Physics & Multiplayer Layering
- **Dynamic Camera Tracking**: Smooth horizontal camera lerping following the active player across the 100 tiles.
- **Hopping Arc**: Step-by-step tile advance with a sinusoidal vertical leap (`hopArc` peaking at +55px).
- **Multiplayer Same-Tile Rendering**:
  - Inactive players sharing a tile are offset slightly to the side (`x = tileCenterX - 85f`) with a subtle depth treatment (`80%` opacity, `106 × 106 px`).
  - The **active player is rendered LAST** at full scale (`120 × 120 px`) and `100%` opacity, centered directly on the tile (`x = tileCenterX - 60f`). This guarantees the active player is always on the uppermost layer and fully visible.
- Removed floating overhead `P1`/`P2` tags to maintain a clean camera view.

### 5. In-Game HUD Console & Textual Hierarchy
- Raised the entire world platform by **+110px**, providing 86px of clear breathing room between the bottom HUD and the stone slabs.
- **Solid Container Styling**: Upgraded `UIUtils.createRoundedDrawable` with `Pixmap.Blending.None` and 3px borders, eliminating double-blended corner circle artifacts.
- **Active Player Status Card (`430 × 120 px`)**:
  - **Tier 1 (Header)**: Bold Player Name in `25pt` espresso brown (`#361704`).
  - **Tier 2 (Primary Vital)**: Vibrant `20 × 20 px` crimson heart icon paired with `22pt` bold health text (`HP: 90 / 90`).
  - **Tier 3 (Supporting Attributes)**: Compact `19pt` warm brown combat stats (`ATK  6    |    DEF  4`).
  - **Avatar**: `68 × 68 px` champion portrait on the left.
- **Inventory Satchel Button (`250 × 120 px`)**:
  - Left: `56 × 56 px` satchel icon.
  - Right: Two-tier text stack with `INVENTORY` (`24pt`) and `4 SLOTS` subtext (`16pt`).
- **Roll Button (`220 × 120 px`)**:
  - Large `60 × 60 px` D6 dice icon, `30pt` bold white `"ROLL"`, and `21pt` cream roll subtext (`"D6"` / `"ROLLED: X"`).

---

## 🔍 Key Challenges & Solutions
- **Glyph Compatibility**: Unicode symbols like bullets (`•`), double bars (`❚❚`), and triangles (`▲`/`▼`) rendered as missing glyph boxes (`[]`) because LibGDX FreeType font generators bake standard ASCII by default. Resolved by replacing all special symbols with clean ASCII equivalents (`|`, `||`, `+`, `-`).
- **Asset Slicing Leaks & Squished Line**: In Acts 2 and 3, a dark horizontal line appeared above the player. Analysis revealed that slicing starting at row 450 accidentally captured the 4-pixel bottom border of the midground silhouette strip. Resolved by slicing tightly at `y = 464, h = 110`.
- **Corner Double-Blending Artifacts**: Semi-transparent colors caused `UIUtils.createRoundedDrawable` to double-blend corner circles and rectangles. Resolved by enforcing `Pixmap.Blending.None` and switching to solid warm leather fills with 3px mahogany borders.
- **HUD Overlap & Legibility**: Initially, bottom HUD elements touched the ground platform and forced small fonts. Resolved by elevating the platform by +110px and increasing HUD heights to 120px with calibrated typographic tiers.

---

## 🔜 Next Steps
- Implement graphical animation for the tumbling dice sequence using the standalone `dice_1.png` through `dice_6.png` assets.
- Integrate animated character walk/hop spritesheets from the design lead once finalized.
- Connect tile landing events with interactive event modal popups (Treasure chests, Merchant encounters, and Combat challenges).
