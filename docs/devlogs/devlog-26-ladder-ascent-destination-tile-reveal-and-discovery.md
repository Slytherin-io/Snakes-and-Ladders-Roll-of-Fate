# 📜 Devlog 26: Ladder Destination Tile Reveal, Discovery FX & Fog-of-War Synchronization

* **Date:** November 24, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Board Progression, Discovery Mechanics & Fog-of-War Polish
* **Branch:** `feat/board-adventure-screen`

---

## 🎯 Objectives
- **Fix Unrevealed Landing Tiles After Ladder Ascents**: Resolve the issue where landing on a destination tile via a ladder ascent failed to uncover the tile, leaving it shrouded in Fog of War.
- **Synchronize Visual Discovery Feedback**: Ensure that touching down from a ladder triggers the golden discovery burst FX, lifts the animated mist veil, and displays the true flagstone badge.
- **Award Discovery Experience Points**: Grant exploratory progression EXP when a celestial ladder propels a player into previously uncharted board territory.
- **Screen Resumption Safeguard**: Ensure that whenever the board screen initializes or resumes, the active player's current tile is guaranteed to be revealed.

---

## 🛠️ Work Completed

### 1. Root Cause Analysis
- During standard dice movement, stepping across tiles evaluates `handleTileArrival()`, which reveals uncharted tiles (`tile.setRevealed(true)`) upon landing.
- When landing on a ladder (e.g., Tile 4 leading to Tile 14), `handleTileArrival()` properly revealed the starting ladder tile and launched the celestial arc ascension via `startSpecialMove()`.
- However, when the soaring flight reached its destination (`progress >= 1.0f`), the engine updated the player's position and mini-map tracker, but never called `setRevealed(true)` on the destination tile (`specialMoveDestTile`).
- As a result, the destination flagstone beneath the hero's paws remained covered in the animated semi-transparent Fog of War mist veil (`fogPulse`) and displayed `? UNCHARTED ?` indefinitely until another player landed directly on it via dice rolls.

### 2. Touchdown Tile Reveal & Golden Discovery Burst FX
In [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java) inside `updateSpecialMove()` during the `IN_FLIGHT -> LANDED` transition (`progress >= 1.0f`):
- Retrieved the destination tile object: `Tile destTile = board.getTile(specialMoveDestTile);`.
- Evaluated whether the landing site was previously uncharted: `boolean destWasUnrevealed = (destTile != null && !destTile.isRevealed());`.
- Immediately uncovered the tile: `destTile.setRevealed(true);`.
- If newly uncovered:
  - Triggered the golden discovery burst flash: `revealFlashTile = specialMoveDestTile;` and `revealFlashTimer = 1.0f;`.
  - Cleared the Fog of War mist veil and switched the paver slab to its proper biome texture.
  - Replaced the `? UNCHARTED ?` label with the true tile identity and number.
  - Awarded discovery experience points (`10 + specialMoveDestTile * 0.05f`) and updated the bottom-bar progression HUD card.

### 3. Context-Aware Top Banner Announcements
Updated the top notification banner upon ladder touchdown:
- **Throne of Fate (Tile 100)**: `"VICTORY! <HERO> REACHED THE THRONE OF FATE!"`
- **Sanctuary (Tile 30 - Act I Finale)**: `"<HERO> REACHED SANCTUARY (ACT I FINALE) VIA CELESTIAL LADDER!"`
- **Abyss Gate (Tile 70 - Act II Finale)**: `"<HERO> REACHED THE ABYSS GATE (ACT II FINALE) VIA CELESTIAL LADDER!"`
- **Uncharted Discovery**: `"<HERO> REACHED TILE <DEST>! ASCENT COMPLETE! (+<EXP> EXP)"`
- **Charted Tile**: `"<HERO> REACHED TILE <DEST>! ASCENT COMPLETE!"`

### 4. BoardScreen Initialization Safeguard
Added a proactive verification check in `BoardScreen.show()`:
- Whenever the board screen loads (such as resuming from a battle encounter or transitioning from menus), the engine inspects the current player's standing tile.
- If the current tile was somehow left unrevealed, it is immediately marked `setRevealed(true)`, ensuring heroes never stand on mystery fog tiles upon entering the screen.

---

## 🔍 Verification & Testing

1. **Board Generation & Movement Compatibility**:
   - `.\gradlew.bat test`: Verified that two-pass board generation, milestone guarantees (Tiles 1, 30, 70, 100 safe), and combat mortality rules pass without errors.
2. **Integration Test Suite**:
   - `.\gradlew.bat testInventory`: Ran all 20 integration tests across inventory management, merchant barters, consumable elixirs, and snake encounters with exit code 0.
3. **Visual Verification**:
   - Confirmed that ascending a ladder to an uncharted tile immediately lifts the fog veil, triggers the discovery flash, reveals the tile number, and updates player EXP.
