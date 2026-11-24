# 📜 Devlog 28: Biome Environment Harmonization, Immersive Act Transitions & Multi-Phase Ascension Kinematics

* **Date:** November 24, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – World Atmosphere, Biome Progression, Kinematics & Discovery Mechanics
* **Branch:** `feat/board-adventure-screen`

---

## 🎯 Objectives
- **Starting Platform Visual Grounding & Transparency Harmonization**:
  - Remove the jarring opaque black rectangle rendered underneath the starting platform (Tile 1 / Start Camp).
  - Introduce translucent slate paver slabs with soft biome-aligned color tints that seamlessly blend into the parallax background environment.
- **Immersive Multi-Stage Act Transitions**:
  - Implement smooth, atmospheric visual and environmental cross-fading when traversing between **Act I: Jungle Canopy (Tiles 1–30)**, **Act II: Sunken Ruins & Swamps (Tiles 31–70)**, and **Act III: Infernal Volcanic Summit (Tiles 71–100)**.
  - Anchor milestones at Sanctuary (Tile 30) and the Abyss Gate (Tile 70) with restorative audio-visual feedback and milestone arrival banners.
- **Multi-Phase Stepped Ladder Ascension Kinematics**:
  - Fix the abrupt, instant vertical snap when stepping onto a ladder tile that broke player immersion.
  - Engineer a multi-phase kinematic motion pipeline: crouching anticipation windup, parabolic celestial elevation arc soaring high above the ground plane, and touchdown recovery landing.
- **Dynamic "Step-On" Fog-of-War Discovery System**:
  - Elevate gameplay suspense, unexpectedness, and exploratory delight by cloaking tiles under an animated Fog of War mist veil (`? UNCHARTED ?`).
  - Reveal a tile's true nature (safe paver, narrative event, ladder, or serpent hazard) only when a hero's paws physically touch down upon it.
- **Decoupled Typography Hierarchy & Numerical Readability**:
  - Decouple the Pause/Settings menu typography from the interactive Event & Narrative Choice system to allow independent styling and layout tuning.
  - Integrate the clean, high-legibility Fredoka and Quicksand font families for player statistics, health, and experience meters on the bottom-bar HUD card.
  - Enable bilinear texture filtering in FreeType font baking to eliminate edge distortion and pixel jitter across scaled desktop displays.

---

## 🛠️ Work Completed

### 1. Starting Platform Redesign & Biome Color Harmonization
- **Diagnosis of Visual Artifact**:
  - The starting staging platform to the left of Tile 1 was previously filled with an opaque solid black rectangle (`Color.BLACK` box), creating a harsh, artificial edge that clashed with the vibrant pixel-art jungle floor.
- **Translucent Biome-Aligned Paving**:
  - Removed the solid black fill and replaced it with an ambient-tinted translucent slab system in [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java):
    - **Act I (Jungle Canopy)**: Deep foliage moss tint (`#2a3a2a` with 72% opacity).
    - **Act II (Sunken Swamps)**: Submerged midnight slate tint (`#1c2d3a` with 72% opacity).
    - **Act III (Volcanic Summit)**: Basalt ash obsidian tint (`#3a1c1c` with 75% opacity).
  - The platform now naturally inherits the lighting and atmospheric mood of each Act, grounding the heroes smoothly into the scene.

---

### 2. Immersive Parallax & Biome Transitions Between Acts
- **Continuous 3-Tier Parallax Blending**:
  - In `renderParallaxBackground()`, implemented dynamic alpha interpolation across the three background planes (Far Canopy/Mountains, Mid Silhouettes, and Foreground Ground foliage).
  - As the player journeys through milestone boundaries (Tile 30 $\rightarrow$ 31 and Tile 70 $\rightarrow$ 71), background layers gently cross-fade, seamlessly shifting the atmosphere from lush sunlit greens to moody aquatic blues, and finally to glowing infernal embers.
- **Milestone Sanctuaries & Recovery Rest Stops**:
  - **Tile 30 (Sanctuary - Act I Finale)**: Serves as a restorative respite before entering the perilous swamps. Stepping onto Tile 30 triggers `sfx_sanctuary.ogg`, plays a soothing healing shimmer, and displays the banner:
    `"<HERO> REACHED SANCTUARY (ACT I FINALE)! REST BEFORE ENTERING THE SUNKEN RUINS."`
  - **Tile 70 (Abyss Gate - Act II Finale)**: Fortifies heroes before the harrowing volcanic ascent. Triggers restorative effects and warns the party:
    `"<HERO> REACHED THE ABYSS GATE (ACT II FINALE)! GIRD RESOLVE FOR THE INFERNAL VOID."`

---

### 3. Multi-Phase Stepped Ladder Ascension Kinematics
- **Eliminating Instant Vertical Teleportation**:
  - Previously, landing on a ladder caused the player sprite to instantly jump across the board, disorienting the player and failing to convey the sense of upward ascension.
- **3-Phase Kinematic Progression Pipeline**:
  Inside [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java#L2100-L2285), structured ladder movements into distinct emotional and physical beats:
  1. **Phase 1: Crouch & Windup Anticipation (0.45s)**:
     - The hero pauses on the starting ladder tile, compressing downward with a spring-loaded anticipation squash.
     - Triggers `AudioManager.SoundEffect.LADDER_WINDUP` audio cue, alerting players that a dramatic celestial leap is about to occur.
  2. **Phase 2: Celestial Parabolic Flight Arc (1.20s)**:
     - The hero launches along a smooth parametric flight trajectory:
       $$Y_{\text{flight}} = \text{lerp}(Y_{\text{start}}, Y_{\text{dest}}, t) + \sin(\pi \cdot t) \cdot H_{\text{arc}}$$
     - Elevates up to $180\text{ px}$ above the ground plane, accompanied by `AudioManager.SoundEffect.LADDER_SOAR` and trailing stardust motes, visually communicating true vertical ascent above the tree line.
     - The camera smoothly glides with the player throughout the flight path.
  3. **Phase 3: Touchdown Recovery & Grounding (0.65s)**:
     - Upon reaching the destination tile, the hero lands with impact squash, triggering `AudioManager.SoundEffect.LADDER_LAND` and a golden flagstone discovery flash.
     - Holds for a 0.65-second recovery window to allow players to register their new board position before the turn concludes.

---

### 4. Dynamic "Step-On" Fog-of-War Discovery System
- **Balancing Suspense & Playability (Option 2 Implementation)**:
  - Addressed game design feedback seeking unpredictable board exploration without sacrificing strategic clarity.
  - Board tiles are initially rendered as mysterious unrevealed pavers cloaked in semi-transparent pulsating fog particles with the badge `? UNCHARTED ?`.
- **Active Arrival Reveal Mechanic**:
  - During turn locomotion, passing over tiles keeps them shrouded; only the final destination tile landed on by the dice roll triggers `tile.setRevealed(true)`.
  - Upon touchdown:
    - Triggers `AudioManager.SoundEffect.FOG_REVEAL` with golden shimmer burst particles.
    - Unveils the true tile nature (Normal, Special Narrative Event, Ladder, or Serpent).
    - Grants exploratory progression experience points:
      $$\text{EXP}_{\text{discovery}} = 10 + \lfloor \text{TileIndex} \times 0.05 \rfloor$$
    - Updates the active player's bottom status card and broadcasts the discovery on the top notification banner.

---

### 5. Decoupled UI Typography & Readability Polish
- **Decoupling Menu & Choice Type Systems**:
  - Separated Pause/Settings typography from narrative event dialogs to eliminate style conflicts and allow distinct sizing:
    - **Pause & System Modals**: `pauseBadgeFont`, `pauseTitleFont`, `pauseSubFont`, `pauseBodyFont`, `pauseBtnFont`, `pauseTipFont`.
    - **Narrative Event Dialogs**: `eventBadgeFont`, `eventTitleFont`, `eventDescFont`, `choiceBtnFont`, `choiceOutcomeTitleFont`, `choiceOutcomeBodyFont`, `choiceD20Font`.
- **Integration of Fredoka and Quicksand Typefaces**:
  - Incorporated `Fredoka-SemiBold.ttf`, `Fredoka-Bold.ttf`, `Fredoka.ttf`, and `Quicksand.ttf` into [assets/ui/](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/ui).
  - Applied Fredoka to the player's HUD status card (`hudHpFont`, `hudExpFont`, and `hudStatsFont`) to deliver crisp, modern numerical legibility that remains readable across all desktop display scales.
- **Bilinear FreeType Texture Filtering**:
  - In [UIUtils.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/ui/UIUtils.java#L107-L108), configured `Texture.TextureFilter.Linear` for both minification and magnification filters during FreeType font generation:
    ```java
    parameter.minFilter = Texture.TextureFilter.Linear;
    parameter.magFilter = Texture.TextureFilter.Linear;
    ```
  - This guarantees smooth rasterization, eliminating shimmering and jagged pixel edges when text renders on scaled 1080p virtual viewports.

---

## 🔍 Verification & Testing

1. **Board Generation Test Suite**:
   - Executed `.\gradlew.bat testBoard` (with `BypassSandbox: true`):
     - **1,000 / 1,000 Board Generation simulations passed with zero invariant violations**.
     - Guaranteed milestone safety (Tiles 1, 30, 70, 100 strictly safe), jump distance minimums ($\ge 6$ tiles), and anti-clustering separation ($\ge 2$ safe tiles between special tiles).
2. **Inventory & Combat Integration Test Suite**:
   - Executed `.\gradlew.bat testInventory`:
     - **20 / 20 integration tests passed**, confirming equipment durability, satchel full swaps, consumable potions, merchant tributes, and combat mortality rules.
3. **Desktop Compilation & Execution**:
   - Executed `.\gradlew.bat compileJava :lwjgl3:compileJava` with zero errors.
   - Verified that the starting platform blends naturally with the background, ladder ascents follow the 3-phase trajectory, act transitions cross-fade seamlessly, and tiles unveil dynamically upon player arrival.
