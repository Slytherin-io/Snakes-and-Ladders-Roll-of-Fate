# 📜 Devlog 29: Single-Use Inventory Overhaul, Curses, Custom Pixel-Art Item Icons & Dynamic Fate Polish

* **Date:** November 25, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Inventory Rebalance, Board Utility Relics, Pixel-Art Icons & Event UI Polish
* **Branch:** `feat/board-adventure-screen`

---

## 🎯 Objectives
- **Eliminate Durability Snowballing via Single-Use Architecture**:
  - Convert all usable items (`CONSUMABLE` and `EQUIPMENT` tactical relics) to strictly single-use (`durability = 1`) and enforce 1-turn temporary combat buffs (`addTempAttack` / `addTempDefense`), eliminating multi-use hoarding and UI durability clutter.
- **Introduce High-Agency Board Utility Relics, Double-Edged Artifacts & Passive Curses**:
  - Add board-manipulating utilities (exact dice control, roll capping, snake pacification, map scouting, player transposition, and tile leaps).
  - Add risk/reward double-edged relics and negative passive `CURSE` burdens that clog satchel slots and penalize stats/rolls until cleansed or bartered away.
- **Upgrade Item Visuals with Tailored Pixel-Art Icons**:
  - Replace repetitive placeholder icons (e.g., scrolls showing red potion bottles) with 25 distinct `64x64` pixel-art RPG icons sliced from pixel sprite sheets and wired into both board and battle UIs.
- **Streamline Event Resolution Copy & Typography**:
  - Remove redundant `"Current"` / `"Total"` / `"Position"` suffixes from event outcome summaries and enlarge the resolution body text for effortless readability.
- **Integrate Run-Level Fate Modifiers, Visible Landmarks, Guard Riposte & Act-Themed Serpents**:
  - Support [BoardModifier.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/board/BoardModifier.java) presets, [TileLandmark.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/board/TileLandmark.java) POIs, Guard & Riposte counter-attacks in [BattleSystem.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/systems/BattleSystem.java), and 9 Act-scaled serpent species in [SnakeTile.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/board/SnakeTile.java).

---

## 🛠️ Work Completed

### 1. Single-Use Inventory Overhaul & Turn-Scoped Stat Buffs
- **Unified Single-Use Lifecycle** ([Item.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/items/Item.java), [ItemType.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/items/ItemType.java), [Inventory.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/items/Inventory.java)):
  - Enforced `durability = 1` / `maxDurability = 1` across all usable items so every activation feels decisive and frees its satchel slot immediately.
  - Updated `Item.use()` so all item-granted `ATTACK` and `DEFENSE` bonuses apply as temporary 1-turn buffs (`addTempAttack` / `addTempDefense`) rather than permanent stat creep, cleared automatically on `TURN_TRANSITION` in [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java).
  - Kept `ItemType.EQUIPMENT` items (`[1-USE RELIC]`) eligible for hazard deflection (`EQUIPMENT_DURABILITY`), sacrificing their single charge to ward off serpent fangs or dart traps.

---

### 2. Board Utility Relics, Double-Edged Artifacts & Passive Curses
Expanded [EventEffectType.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/systems/EventEffectType.java), [ItemGenerator.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/items/ItemGenerator.java), and [Player.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/entities/Player.java):

1. **Board-Centric Utility Relics (~35% loot pool)**:
   - **Loaded Golden Die** (`DICE_EXACT`): Reveals a 1–6 Golden Die Fate Picker popup when rolling in `BoardScreen`, allowing the player to choose their exact roll.
   - **Snake Charmer's Flute** (`SNAKE_WARD`): Grants a passive ward that automatically pacifies the next `SnakeTile` encounter, skipping both combat and downward slide.
   - **Heavy Lead Boots** (`DICE_CLAMP`): Caps the next dice roll at `2` max for precise landings on ladders or landmarks.
   - **Vaulting Hook** (`MOVE`): Leaps forward `2–4` tiles.
   - **Eye of the Pathfinder** (`SCOUT`): Reveals `5–8` uncharted tiles ahead via `EventEffect.ScoutHandler`.
   - **Transposition Orb** (`SWAP_PLAYER`): Swaps board positions with the furthest rival player ahead (or warps forward `+5` tiles in solo mode).

2. **Double-Edged Artifacts (~20% loot pool)**:
   - Implemented optional secondary effect slots on `Item` (`createDoubleEdged`):
     - **Berserker's Blood Draught**: Grants large `+ATK` for 1 turn at the cost of `15 HP` (guarded so players cannot fatally self-inflict via UI click).
     - **Idol of Greed**: Grants `+EXP` while capping the next dice roll at `3`.
     - **Blood-Pact Stride**: Leaps `+4` to `+6` tiles at the cost of `12 HP`.

3. **Passive Curse Burdens (`ItemType.CURSE`)**:
   - Added three passive burdens inflicted by failed risky shrine/vault checks and the new *Whispering Hex Pedestal* trap in [EventRepository.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/systems/EventRepository.java):
     - **Cursed Obsidian Slab**: `-1` dice roll modifier while held.
     - **Viper's Hex Totem**: `-2 ATK` in combat while held.
     - **Rusted Iron Shackles**: `-2 DEF` in combat while held.
   - **Counterplay & Removal**: Curses occupy an inventory slot and block normal `USE` or combat use; players can cleanse them automatically upon reaching **Sanctuaries (Tiles 30 & 70)**, purify them at Shrines (`ChoiceRequirementType.HAS_CURSE` in [EventChoice.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/systems/EventChoice.java)), or pawn them off via Merchant Barter (`CONSUME_ANY_ITEM`).
   - Added `[BOARD]` / `[CURSED]` / `[LOW HP]` button guards in [BattleSystem.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/systems/BattleSystem.java) and [BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java).

---

### 3. Custom Pixel-Art Item Icons Pipeline
- **Sprite Extraction**:
  - Sliced 20 icons (`16x16` grid $\rightarrow$ `64x64` nearest-neighbor) and 5 relic icons (`32x32` grid $\rightarrow$ `64x64` nearest-neighbor) into [assets/img/](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img), including `item_scroll.png`, `item_hook.png`, `item_flute.png`, `item_lead_boots.png`, `item_swift_boots.png`, `item_torch.png`, `item_orb_ring.png`, `item_idol.png`, `item_blood_draught.png`, `item_blood_scroll.png`, `item_curse_slab.png`, `item_curse_totem.png`, `item_shackles.png`, `item_fang.png`, `item_buckler.png`, and tier-specific blades/shields/elixirs.
- **Identity-Aware Resolution**:
  - Updated [Assets.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/ui/Assets.java) with `Assets.getItemIcon(Item)` so every item renders its matching sprite in both `BoardScreen.refreshInventoryUI()` and `BattleScreen.refreshItemPopup()` (with a crimson tint on curses).

---

### 4. Event Resolution Copy & Typography Polish
- **Concise Outcome Formatting**:
  - Updated [EventEffect.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/systems/EventEffect.java) and [EventChoice.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/systems/EventChoice.java) to omit `"Current"`, `"Total"`, and `"Position"` parentheticals from modal outcome strings, showing clean deltas such as `Ward Fortitude: +3 DEF` and `Martial Might: -1 ATK`.
- **Larger Resolution Body Text**:
  - Increased `choiceOutcomeBodyFont` in [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java) from `21` to `26` and widened `outcomeBox` text bounds (`460` $\rightarrow$ `485`, height `140` $\rightarrow$ `145`) for clean line wrapping.

---

### 5. Dynamic Fate Modifiers, POIs, Guard/Riposte & Serpent Variants
- **Board Generation Modifiers & POIs** ([Board.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/board/Board.java), [BoardModifier.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/board/BoardModifier.java), [TileLandmark.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/board/TileLandmark.java)):
  - Added 5 run-level modifiers (`BALANCED_ASCENT`, `SERPENTS_ECLIPSE`, `MERCHANT_CARAVAN`, `CELESTIAL_ALIGNMENT`, `SHROUDED_LABYRINTH`) and visible POI landmarks (`SHRINE`, `MERCHANT`, `TREASURE`, `CROSSROADS`) alongside Crossroads & Treasure procedural stories in [EventStoryGenerator.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/systems/EventStoryGenerator.java).
- **Guard Mitigation & Riposte Counter-Attack** ([BattleSystem.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/systems/BattleSystem.java)):
  - Defending now negates incoming crits, halves damage, and triggers a riposte strike back at the enemy serpent.
- **9 Act-Scaled Serpent Species** ([SnakeTile.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/board/SnakeTile.java)):
  - Replaced generic snakes with Act-themed serpents scaling from Act I (*Moss Adder*, *Forest Python*, *Emerald Viper*) through Act II (*Cave Rattler*, *Crag Carapace Serpent*, *Obsidian Cobra*) to Act III (*Magma Wyrm*, *Abyssal Basilisk*, *Apex Celestial Serpent*).

---

## 🔍 Verification & Testing

1. **Board Generation Test Suite**:
   - `.\gradlew.bat testBoard`: **1,000 / 1,000** simulations across all 5 `BoardModifier` presets passed with zero invariant violations ([BoardTest.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/board/BoardTest.java)).
2. **Inventory, Event & Combat Integration Suite**:
   - `.\gradlew.bat testInventory`: **22 / 22** test blocks passed in [InventoryTest.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/items/InventoryTest.java), verifying single-use enforcement, board utilities (`Loaded Golden Die`, `Heavy Lead Boots`, `Snake Charmer's Flute`), double-edged items (`Berserker's Blood Draught`), passive curses, Sanctuary/Shrine cleansing, `BattleSystem` guards, and `MAX_HEALTH` / `SCOUT` dilemmas.
3. **Desktop Build**:
   - `.\gradlew.bat compileJava :lwjgl3:compileJava`: Exited cleanly with code `0`.
