# 📜 Devlog 21: Interactive Events, Turn-by-Turn Combat & Complete Progression HUD

* **Date:** November 24, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Interactive Tile Events, Dynamic Combat & Complete Progression HUD
* **Branch:** `feat/board-adventure-screen`

---

## 🎯 Objectives
- **Interactive Tile Event Modals**: Connect stepping stone landing events with rich interactive parchment scroll modals (Treasure Chests, Shrines, Wandering Merchants, Blessings, Traps, and Serpent Ambush encounters).
- **Turn-by-Turn Interactive Combat (Option A)**: Replace automatic combat calculations on Snake Tiles with an interactive, turn-by-turn D20 duel engine featuring real-time health bars, combat dice rolls, dynamic log updates, critical hits, mortal retreats, and victory loot drops.
- **Unpredictable Procedural Encounters**: Implement randomized, shuffled choice decks with hidden consequences that reveal outcomes only after an action is picked, avoiding repetitive or predictable options.
- **Visual Container Styling & Boundary Hardening**:
  - Refine modal and HUD containers with solid leather and parchment themes, establishing a strong typography hierarchy.
  - Enlarge the parchment scroll asset (`940 × 690`) and enforce strict auto-wrapping (`setWrap(true)`) and cell width constraints (`460–490px`) to prevent any text from overextending beyond parchment boundaries.
- **Health Cap Safeguards**: Prevent player HP from exceeding maximum base health from healing consumables, equipment buffs, level-ups, or event effects.
- **Complete EXP & Progression System**:
  - Redesign the active player HUD status card to display Level and EXP alongside Player Identity, HP, and ATK/DEF.
  - Fix the critical bug where failing checks in events resulted in negative EXP subtractions (`Knowledge: +-30 EXP` yielding `-43 / 100`).
  - Implement surplus EXP rollover upon leveling up.
  - Award board progression EXP for safe ground tile arrivals and stable ladder ascents.
- **Application Window Branding**: Register custom taskbar and titlebar application icons across all standard OS resolutions (16, 32, 48, 64, 128, and 256 px).

---

## 🛠️ Work Completed

### 1. Interactive Event Modal System & Procedural Encounters
- **Tile Landing Event Connection**: Linked `handleTileArrival(player, tileIndex)` in `BoardScreen.java` to `EventManager`. Landing on special or ambient tiles triggers interactive modal overlays:
  - **Treasure & Blessings**: Choice to scavenge or pass safely.
  - **Ancient Shrines**: Prayer, martial meditation, ward channeling, or blood sacrifices for powerful boons.
  - **Wandering Merchants**: Dynamic trades including Blood Rites (HP cost for equipment), Knowledge Tributes (EXP cost for gear), Barter (swapping items from satchel), and Haggling.
  - **Traps**: Reaction checks, dodging, and equipment-assisted deflections consuming 1 gear durability.
  - **Climbing Dilemmas**: Configured ladders with a 40% chance of an interactive dilemma (e.g. leaping over gaps, reinforcing rungs, studying markings) and 60% smooth direct climbs.
- **Unpredictability & Procedural Variety**: In `EventRepository.java`, encounter choice decks are dynamically populated based on the active Act (Act I Forest, Act II Twilight, Act III Abyss) and shuffled using `Collections.shuffle()`.
- **Hidden Consequences**: Outcomes and stat changes are revealed only upon resolving the chosen option via the resolution scroll view.

### 2. Turn-by-Turn Interactive Combat on Snake Tiles
- **Serpent Clash Encounter**: When a player lands on a Snake Tile, instead of an instant slide-down, an interactive encounter initiates with the serpent (Shadow Asp, Armored Python, or Apex Wyrm).
- **Dedicated Duel UI**: Formatted within the parchment scroll showing real-time player and enemy stats:
  - `Player Name`, `HP: current / max`, `ATK`, `DEF`.
  - `Enemy Name`, `HP: current / max`, `ATK`, `DEF`.
- **`[STRIKE (ROLL D20)]` Turn Engine**:
  - Player initiates each strike round by rolling a D20.
  - Natural 20 triggers a Critical Hit (+50% bonus damage); Natural 1 triggers a Grazing Blow.
  - Enemy retaliates based on archetype behavioral profiles (Aggressive, Defensive, or Balanced).
  - Both combatants' HP bars deplete dynamically in real time.
- **Victory & Retreat States**:
  - **Slaying the Serpent**: Grants progression EXP, rolls random loot, and anchors the player on the tile (`specialSlideTarget = -1`), conquering the snake.
  - **Mortal Retreat Safeguard**: If reduced below critical HP (or upon defeat), the player tumbles backward down the snake's coils to the destination tile, surviving the encounter.

### 3. Visual Styling Refinement & Parchment Boundary Hardening
- **Enlarged Scroll Asset**: Scaled the modal scroll background to `940 × 690` in `BoardScreen.java`, providing over 85px of clean parchment breathing room above and below the spindles.
- **Text Boundary & Auto-Wrapping Constraints**:
  - Fixed issue where long outcome descriptions (such as `"Acquired Relic: Minor Amulet (Mystic Energy)"`) overextended past the modal paper edge.
  - Enforced `lineLbl.setWrap(true)` and constrained all outcome cards and table cells to a strict `460px`–`490px` width.
- **Solid Leather Containers & Textual Hierarchy**:
  - Updated Inventory, Event Badges, and Player HUD cards to solid, high-contrast leather styling (`#c89f6d` fill, `#6e4822` border).
  - Established clean color and typographic hierarchy:
    - Headers: Dark Espresso Brown (`#361704`, 24pt bold).
    - Vitals: Crimson Red (`#a81717`, 20pt bold) with heart icon.
    - Progression: Rich Amber Bronze (`#693708`, 18pt bold).
    - Supporting Stats: Warm Chocolate Brown (`#5c2f0f`, 18pt bold).

### 4. Health Clamping & Over-Heal Safeguards
- In `Player.java`, overrode `setHealth(int health)` to enforce:
  $$\text{health} = \min(\text{maxHealth}, \max(0, \text{health}))$$
- In `EventEffect.java` and `Item.java`, guarded all healing effects to never inflate HP beyond `player.getMaxHealth()`.
- Added a full-health consumption check in `BoardScreen.java`, displaying an in-game warning banner when attempting to drink healing potions at maximum health.

### 5. EXP System Overhaul & Active Player HUD Card
- **Active Player Status Card Integration**:
  - Added real-time Level and EXP progress to the bottom-right HUD card:
    - Line 1: `P1: PLAYER 1`
    - Line 2: `[heart] HP: 90 / 90`
    - Line 3: `LVL 1    -    EXP: 0 / 100`
    - Line 4: `ATK 24    -    DEF 6`
  - Re-anchored the hero avatar cell to `110 × 90` with `Scaling.fit` for crisp, aligned presentation.
- **Negative EXP Bug Fix**:
  - Diagnosed cause: in `EventRepository.java`, merchant "Haggle" failures had negative values (`-8`, `-18`, `-30`), resulting in `Knowledge: +-30 EXP` and driving player EXP negative (e.g. `-43 / 100`).
  - Converted Haggle failure outcomes to positive consolation knowledge EXP (`+8`, `+18`, `+30`).
  - Hardened `EventEffect.java` with `Math.max(0, Math.abs(resolvedValue))` so EXP effects are strictly additive.
  - Clamped `Player.setExperience(int)` with `Math.max(0, experience)`. EXP can never be negative.
  - Added zero-check guard in `Player.gainExperience(int exp)` to reject negative or zero values.
- **Surplus EXP Rollover**:
  - Fixed level-up logic in `Player.java`: replaced `setExperience(0)` with `this.experience -= expThresh` in a while-loop. Surplus experience is preserved and counts toward the next level threshold.
  - Exposed dynamic threshold via `getExpThresh()`, increasing by 10% on each level up.
- **Tile Landing Progression EXP**:
  - Added EXP awards in `BoardScreen.java` when landing on safe ground (`10 + tile * 0.05`) and when climbing stable ladders (`10 + tile / 3`), dynamically updating the HUD card.

### 6. Application Branding & Icons
- Configured multi-size icon loading in `Lwjgl3Launcher.java` using `app_icon.png` and generated pixel-crisp PNG icons at 16×16, 32×32, 48×48, 64×64, 128×128, and 256×256 px.

---

## 🔍 Verification & Testing

1. **Automated Unit Tests (`InventoryTest.java`)**:
   - `[Test 11-13]`: Validated Blood Rite (HP cost), Knowledge Tribute (EXP requirement check & deduction), and 1-for-1 item barter.
   - `[Test 14]`: Verified maximum health clamping across consumable usage, equipment buffs, and event healing.
   - `[Test 15]`: Tested Level 1 start (0 EXP), negative EXP rejection, Cat +15% bonus calculation (50 EXP $\to$ 57 EXP), and level-up rollover ($57 + 57 = 114 \ge 100 \implies$ Level 2 with 14 / 110 EXP).
   - Ran `java -cp "core/build/classes/java/main" io.github.snakes_and_ladders_roll_of_fate.items.InventoryTest`: all 15 tests passed with code 0.

2. **Build & Compilation Verification**:
   - Executed `.\gradlew.bat test :core:compileJava :lwjgl3:compileJava`.
   - Build completed with **zero errors** (Exit code: 0).
