# Devlog 31: Victory & Defeat Screen Decluttering, Winner Deduplication, Parallax Slicing & Palette Harmonization

* **Date:** November 26, 2026
* **Author:** Dorothy Anne R. Domanico (Design Lead)
* **Sprint / Milestone:** Week 6 – Post-Run Experience, UI Decluttering & Screen Refinements
* **Branch:** `feat/board-adventure-screen`

---

## Objectives
- **Eliminate Text Walls and Clutter on Conclusion**: Transition the Victory and Defeat end-game screen (`GameSummaryScreen.java`) from a dense, text-heavy debrief into a clean, celebratory outcome podium with an on-demand toggle to view detailed chronicles.
- **Eliminate Duplicate Winner Cards in Multiplayer**: Ensure the conqueror/spotlighted hero on the central animated pedestal is never duplicated in the companion recognition row below.
- **Harmonize Action Button Color Palette**: Replace discordant web-style green and blue buttons with the game's handcrafted fantasy tavern palette (leather, mahogany, and gold/amber).
- **Fix Background Parallax Slicing & Layering**: Correct visual artifacts (floating grass platforms, horizontal seams, and duplicate tree canopy rows) caused by directly stretching multi-strip parallax background spritesheets across a 1080p viewport.
- **Automated Visual Verification**: Expand the LWJGL3 capture harness (`SummaryCaptureLauncher.java`) to validate 4-player, 2-player, defeat, and solo views across resolutions.

---

## Work Completed

### 1. Two-Layer Progressive Disclosure Architecture (`GameSummaryScreen.java`)
Redesigned `GameSummaryScreen` around a clean two-layer progressive disclosure pattern:
- **Layer 1: Outcome Podium (Default Clean View)**:
  - **Zero Wall of Text by Default**: Instead of immediately greeting players with over 500 words of narrative prose and 16 individual stat chips, the screen opens to a focused celebratory podium.
  - **Animated Hero Pedestal**: Highlights the conquering hero (or fallen champion in defeat) with an animated walking character sprite (`180×180` or `145×145`), class archetype title, level, and golden accolade pill (`THE FATE CHAMPION` / `THE FALLEN HERO`).
  - **3 Core Highlight Pills**: Focuses party achievements into three clean, glanceable metric badges:
    1. `EXPEDITION PACING`: Total collective party turns traversed across the stones.
    2. `COMBAT VALOR`: Total serpents slain in mortal combat.
    3. `TERRAIN MASTERY`: Total ladders scaled skyward.
  - **Compact Companion Honors**: Displays remaining party members with their class avatar, level, and awarded positive superlative accolade in a streamlined, paragraph-free card row.
- **Layer 2: Detailed Expedition Record & Bardic Chronicle (Toggled View)**:
  - Accessible via a prominent `[ 📜 VIEW EXPEDITION SUMMARY ]` toggle button in the bottom action bar.
  - **Left Column (570px)**: The dynamically generated Bardic Chronicle prose and hero card.
  - **Right Column (1240px)**: 4-quadrant expedition ledger cards for solo play (*Terrain & Exploration*, *Combat & Combat Valor*, *Fortune & The Dice*, *Satchel & Survival*) or a full 2×2 grid of detailed companion feat cards with merit narratives for multiplayer.
  - Seamlessly switch back to the celebratory podium anytime via `[ 🏆 BACK TO OUTCOME ]`.

### 2. Winner Card Deduplication in Multiplayer Lineup
- **The Issue**: In multiplayer outcome screens, the winner displayed on the central animated pedestal was also rendered in the bottom row under `COMPANION RECOGNITION & HONORS`, causing redundancy (e.g. Player 1 appearing twice in a 4-player game, or Player 2 appearing twice in a 2-player game).
- **The Solution**:
  - Filtered party members with `p != hero` when building the companion lineup.
  - In a 4-player match where Player 1 wins, the companion row displays only **Player 2**, **Player 3**, and **Player 4**.
  - In a 2-player match where Player 2 wins, the companion row displays only **Player 1**.
  - In single-player (or if no companions remain), the companion header and lineup table are cleanly omitted.

### 3. Action Button Palette Harmonization
- **The Issue**: The bottom action buttons previously used jarring web colors—emerald green (`#2e7d32`) for `"PLAY AGAIN"` and ocean blue (`#1565c0`) for `"VIEW EXPEDITION SUMMARY"`—which broke visual immersion from the tavern parchment and leather theme.
- **The Solution**: Rebuilt button styles using cohesive warm fantasy tones:
  - **`PLAY AGAIN` (Primary Call to Action)**: Warm tavern leather (`#854b25`) with a radiant gold border (`#ffd54f`) and amber-gold hover state (`#a66032` / `#ffe082`).
  - **`VIEW EXPEDITION SUMMARY` / `BACK TO OUTCOME` (View Switcher)**: Deep rich mahogany (`#54280e`) with a warm amber border (`#c89259`) and gold hover highlight (`#6d3b17` / `#ffd54f`).
  - **`MAIN MENU` (Return Action)**: Dark earthy leather (`#4a2307`) with a muted warm brown border (`#854b25`) and hover state (`#5e2e0a` / `#c89259`).

### 4. Multi-Layer Parallax Background Slicing & Vignette Wash
- **The Issue**: `Assets.ACT1_BG` (`act1_green.png`) and `Assets.ACT3_BG` (`act3_red.png`) are 3-strip parallax spritesheets (`1024×585`), each containing a Far Canopy strip (top), Midground Silhouette strip (middle), and Ground Platform strip (bottom). Drawing the raw texture directly stretched across `1920×1080` caused horizontal seam tearing, a floating ground strip midway up the screen, and duplicate trees.
- **The Solution**:
  - Sliced the textures into precise sub-pixel regions:
    - `farSlice`: `y=0..264` (Canopy sky and distant foliage).
    - `midSlice`: `y=268..456` (Mid-depth tree trunks and dark silhouettes).
    - `groundSlice`: `y=464..572` (Earthen foundation and mossy grass ledge).
  - Composed the background in proper 2D back-to-front rendering order before `stage.draw()`:
    1. **Subterranean Foundation**: Solid `#181822` (victory) or `#2e1534` (defeat) base at `y = 0..140`.
    2. **Far Canopy**: Looped canopy sky slice drawn at `y = 110` (`980px` height) centered across the viewport.
    3. **Midground Silhouettes**: Tree trunks and dark silhouettes at `y = 140` (`560px` height).
    4. **Foreground Platform**: Mossy grass ledge anchored at `y = 0` (`220px` height) right beneath the action row.
    5. **Vignette & Contrast Wash**: Soft semi-transparent tint (`#120b06ba` for victory / `#180606c4` for defeat) ensuring high contrast and readability for all HUD cards, borders, and typography.

---

## Challenges & Solutions

- **Symmetric Parallax Tiling Across Arbitrary Viewports**: Centering and tiling spritesheet slices of variable aspect ratios without seam gaps or edge bleeding.
  - *Solution*: Implemented `drawTiledLayer(batch, slice, startY, height, viewW)`, which computes the scaled aspect width, steps by `width - 2f` to prevent edge subpixel bleeding, and offsets `startX = (viewW - width) * 0.5f` stepping backward until `< 0`, ensuring symmetric, seamless coverage across standard 16:9, ultra-wide, and windowed displays.
- **Multiplayer Lineup Layout Sizing**: Variable player counts (2 to 4) could cause cards to stretch or wrap awkwardly.
  - *Solution*: Dynamically computed card width via `Math.min(380f, (1760f - (count * 16f)) / count)`, perfectly balancing card proportions whether 1, 2, or 3 companions remain.

---

## Verification & Automated Visual Testing

1. **Visual Capture Pipeline (`SummaryCaptureLauncher.java`)**:
   - Expanded the automated headless/windowed capture runner to render and save:
     - `victory_multiplayer_outcome.png`: Verified 4-player victory with Player 1 spotlighted on pedestal, Players 2, 3, 4 under companion honors, sliced forest background, and warm buttons.
     - `victory_2player_outcome.png`: Verified 2-player victory with Player 2 (Turtle) on pedestal and only Player 1 (Cat) in companion lineup (zero duplication).
     - `defeat_multiplayer_outcome.png`: Verified solemn crimson Act 3 theme with twilight silhouette background and mahogany/leather buttons.
     - `victory_multiplayer_summary.png`: Verified toggled expedition chronicle and 2×2 companion exploit grid with `BACK TO OUTCOME` button.
     - `victory_singleplayer_outcome.png` & `victory_singleplayer_summary.png`: Verified single-player layout and 4-quadrant ledger.
2. **Automated Unit Testing & Java Compilation**:
   - Ran `.\gradlew.bat compileJava` across `:core` and `:lwjgl3` modules with zero errors.
   - Ran `.\gradlew.bat test` — 100% of unit test suites passed.

---

## Next Steps
- Change and enhance the snake enemy sprites in battle with dedicated Act-specific snake variations and animations.
