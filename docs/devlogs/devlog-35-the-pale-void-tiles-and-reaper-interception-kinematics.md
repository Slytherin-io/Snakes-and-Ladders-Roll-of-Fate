# Devlog 35: The Pale Void Tiles, Reaper Slide Interception & Halved Tumble Damage

* **Date:** November 27, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Void Boundaries, Interception Kinematics & Traversal Terror
* **Branch:** `feat/board-adventure-screen`

---

## Objectives
- **Eliminate Reaper Teleport Rewinding**: Completely remove the immersion-breaking mechanic where The Pale Shepherd teleported backwards to trail behind a player who slid down a snake past him. The pursuer is now an unyielding, forward-creeping boundary.
- **The Pale Void Formulation**: Formulate "The Pale Void" as the dynamic domain trailing in the wake of Death. Any board tile at or behind the pursuer ($\text{tileIndex} \le \text{paleShepherdTile}$) is actively swallowed by the Void, visually shaded in obsidian-crimson stone with creeping fog veils and marked on the minimap track.
- **Snake Void Interception Kinematics**: When a player chooses `[Slide Down the Tail]` on a snake whose destination falls into the Void ($\text{destTile} \le \text{paleShepherdTile}$):
  - The hero cannot cross Death's boundary: the downward slide abruptly halts right on The Pale Shepherd's tile.
  - Tumble damage from the descent is **cut in half** (Natural 20 = 0 DMG; 10–19 = 5 DMG; 1–9 = 10 DMG).
  - The landing triggers a camera lock, screen shake, audio stinger, and immediately launches the **Mortal Clash** against The Pale Shepherd in `BattleScreen`.
- **Safe Traversal Preserved**: If the snake's destination is safely ahead of the reaper ($\text{destTile} > \text{paleShepherdTile}$), normal sliding and standard tumble damage occur without triggering combat.

---

## Work Completed

### 1. The Pale Void Tiles & Visual Feedback (`BoardScreen.java`)
- **Dynamic Tile Color Grading**:
  - In `renderTiles()`, any tile where `i <= game.getPaleShepherdTile()` (when active) is shaded in deep desaturated obsidian stone (`#3d1f29`) with spectral crimson undertones.
  - A dynamic, undulating crimson-charcoal fog veil is rendered across the slab surface:
    ```java
    float voidMistPulse = 0.28f + 0.10f * MathUtils.sinDeg(stateTime * 90f + (i * 30f));
    batch.setColor(0.18f, 0.05f, 0.08f, voidMistPulse);
    batch.draw(whitePixelTex, tileLeft, tileY, TILE_WIDTH + 1f, TILE_HEIGHT + 12f);
    ```
- **Minimap Shroud Overlay**:
  - Added `voidMiniMapOverlay` to `miniMapTrackGroup` in `buildMiniMap()`.
  - As The Pale Shepherd advances, the progress bar track behind him (`0` to `shepherdTile`) turns deep obsidian-crimson (`#260810`), giving players instant situational awareness of how much of the mountain has been consumed.

### 2. Snake Void Interception & Halved Tumble Damage (`BoardScreen.java`)
- **Interception Decision & Halved Tumble**:
  - In `handleChoiceSelected` for `EventCategory.SNAKE`, the game evaluates:
    ```java
    int dest = ((SnakeTile) tile).getDestination();
    int shepherdPos = game.getPaleShepherdTile();
    boolean entersVoid = (shepherdPos > 0 && dest <= shepherdPos);
    ```
  - If `entersVoid`:
    - The slide target is clamped directly to the reaper's tile (`specialSlideTarget = shepherdPos`).
    - The Acrobatics D20 tumble damage is cut in half:
      - **Natural 20**: 0 DMG (`Fall Damage: None (Roll: 20)` in emerald green).
      - **10–19**: **-5 HP** (`Fall Damage: -5 HP (Roll: X, Halved by Interception)` in crimson red).
      - **1–9**: **-10 HP** (`Fall Damage: -10 HP (Roll: X, Halved by Interception)` in crimson red).
    - Feedback line: `"Intercepted by The Pale Shepherd at Tile " + shepherdPos + "!"`.
    - Flagged `isSlideInterceptedByReaper = true`.
  - If `!entersVoid`:
    - Normal slide to `dest` with standard tumble damage (0, 10, or 20 HP).

### 3. Physical Touchdown & Combat Initiation (`BoardScreen.java`)
- In `SpecialMovePhase.LANDED`:
  - Removed the old code block that relocated the reaper to `finalPos - 3` and set pause rounds.
  - When landing on the reaper's tile:
    - If `isSlideInterceptedByReaper`: screen shakes, top banner announces `"INTERCEPTED BY DEATH! MORTAL CLASH IN THE VOID!"`, and `triggerPaleShepherdEncounter(player)` launches `BattleScreen`.
  - The hero enters combat with their HP reduced only by the halved tumble damage, maintaining fairness while preserving mortal stakes.

---

## Verification & Testing

### 1. Automated Test Suite (`BoardAdventureBugFixTest.java`)
Updated and executed the test suite with 10 passing tests:
- **Test 3 (Bug B - Snake Slide Void Interception & Halved Tumble Damage)**:
  - Verified slide towards Tile 10 with reaper at Tile 20 correctly flags `entersVoid = true`.
  - Verified slide target is clamped to Tile 20.
  - Verified tumble damage calculation is strictly halved: Natural 20 = 0 DMG, 10–19 = 5 DMG, 1–9 = 10 DMG.
  - Verified reaper tile remains locked at Tile 20 (zero rewinding).
  - Verified safe slide to Tile 25 (ahead of reaper) does not enter Void.
- **Other Tests**:
  - Event landing triggers (Test 1).
  - Pursuer anti-overshoot clamping (Test 2).
  - Guaranteed pushback and pause rounds (Test 4).
  - Random dormancy range 4–7 (Test 5).
  - Standard Acrobatics D20 tumble damage (Test 6).
  - Serpent Slayer stat mastery (+1 ATK, +5 Max HP) and exclusive loot drops (Test 7).
  - Serpent's Bane reaper pushback and Adrenaline Surge roll (Test 8).
  - Pale Shepherd victory banish (+50 EXP, 15 tiles, 3 pause rounds) and escape resolution (Test 9).
  - Dawn Warden Phase 2 transition (Test 10).

### 2. Comprehensive Test Pass
- `gradlew.bat testBugFixes`: Passed (10/10 tests).
- `gradlew.bat testBoard`: Passed (1,000 board simulations verified).
- `gradlew.bat compileJava :lwjgl3:compileJava`: Built cleanly without errors.
