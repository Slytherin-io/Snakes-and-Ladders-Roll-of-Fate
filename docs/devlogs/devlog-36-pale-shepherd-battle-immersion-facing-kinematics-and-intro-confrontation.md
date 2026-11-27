# Devlog 36: Pale Shepherd Overhaul: Gentle Redesign, Battle Immersion, Facing Kinematics, Top Dialogue & Victory Rewards

* **Date:** November 27, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Atmospheric Narrative, Gentle Pursuer Redesign, Board Dialogue Ergonomics & Immersion
* **Branch:** `feat/board-adventure-screen`

---

## Objectives
- **Transform The Pale Shepherd into a Gentle, Sorrowful Pursuer**: Shift the pursuer from an aggressive raid boss ("Harvester of Souls", "MORTAL CLASH") into a melancholic, quiet wanderer testing the resolve of travelers on the mountain path.
- **In-Battle Facing Kinematics & Visual Immersion**: Ensure The Pale Shepherd's battle sprite faces left directly toward the player character rather than gazing right away from combat, creating an intimate confrontation.
- **Top-Anchored Board Dialogue Architecture**: Reposition the pre-battle dialogue card from the bottom of the screen to the top (`top().padTop(20)`), keeping the board tiles, player character, The Pale Shepherd sprite, and bottom HUD elements completely visible and unobstructed.
- **Arrival-Only Dialogue Trigger**: Ensure the player character slides smoothly along the full descent trajectory directly to the shepherd's tile (`shepherdPos`). Remove premature midair halts so the dialogue triggers only after touchdown when both the character and the reaper are standing together at the exact same tile.
- **High-Contrast Readability & Dedicated Typography**: Eliminate baked dark brown vertex colors by creating dedicated white-base fonts (`shepherdDialogueTitleFont` at 26pt and `shepherdDialogueFont` at 22pt). Render bright, crystal-clear `#ffffff` body dialogue, `#ffd54f` glowing amber titles, and `#e0d4fc` lavender subtitles over the dark dusk card backdrop.
- **Gentle Combat Balancing & Tuned Scramble Toll**: Rebalance combat stats from 80 HP / 12 ATK to 45 HP / 5 ATK / 2 DEF (dealing ~3-5 damage, mitigated by guard to ~2 damage). Implement an escape gate (Turn 1 & 2 locked) and tune the Turn 3+ scramble penalty from -20 HP to -5 HP.
- **Substantial Rewards for Overcoming Death**: Provide game-changing rewards for players who stand their ground and defeat The Pale Shepherd in combat rather than escaping:
  - `+100 EXP` (advancing level progression).
  - Full HP restoration to maximum (`player.setHealth(player.getMaxHealth())`).
  - Permanent `+10 Max HP` (`player.modifyMaxHealth(10)`).
  - Permanent `+1 ATK` (`player.setAttack(player.getBaseAttack() + 1)`).
  - Legendary Relic: `"Pale Shepherd's Crook"` (+4 DEF, 3 uses) added to satchel with interactive item swap fallback.
  - Reaper banishment: 15 tiles back and 3 contemplation rounds pause.

---

## Work Completed

### 1. In-Battle Immersion, Facing Kinematics & Stat Rebalance ([BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java), [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java))
- **Sprite Inversion Facing Player**:
  - In `BattleScreen.java`, the enemy render pass detects when the combatant is The Pale Shepherd (`isPaleShepherdBattle()`) and flips the sprite horizontally, ensuring the hooded figure and crook face left toward the player character.
- **Gentle Stat Tuning**:
  - Health: Reduced from 80 HP to 45 HP.
  - Attack: Reduced from 12 to 5 (deals ~3-5 damage on normal hit, ~1-2 damage when guarded).
  - Defense: Set to 2.
  - Behavior: Designator updated to `"A Quiet Watcher"` (purging all references to `"Harvester of Souls"`).
  - Initial Contemplation Log: `"The Pale Shepherd raises his crook in quiet contemplation."`.
- **Decoupled In-Battle Intro Popup**:
  - `isIntroDialogueActive = isDawnWardenBattle()`. Since the encounter dialogue now occurs organically on the board screen, `BattleScreen` immediately presents interactive combat buttons (`ATTACK`, `DEFEND`, `ITEM`, `LOCKED`).
- **Tuned Scramble Toll & Escape Gating**:
  - Turn 1: `LOCKED (2)`
  - Turn 2: `LOCKED (1)`
  - Turn 3+: `SCRAMBLE (-5 HP)` in terracotta leather with floating alert `"MIST CLEARS: SCRAMBLE UNLOCKED!"`.
  - Escaping inflicts a light parting wound of -5 HP (reduced from -20 HP) and propels the hero 3 tiles ahead, while pausing the pursuer for 1 round.
- **Defeat Outcome**:
  - Title: `QUIET REST`
  - Subtitle: `"THE PALE SHEPHERD GENTLY CARRIES YOUR SPIRIT"`
  - Message: `"REST, WEARY WANDERER"`

### 2. Snake Slide Trajectory & Arrival-Only Trigger ([BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java))
- **Trajectory Retargeting on Void Descent**:
  - In `handleChoiceSelected()`, when a snake slide leads into the void past the reaper (`entersVoid = (shepherdPos > 0 && dest <= shepherdPos)`), `specialSlideTarget` is set directly to `shepherdPos`.
  - Tumble damage is halved (20 -> 0, 10-19 -> 5, 1-9 -> 10 HP) and logged in the outcome card (`"Caught upon descent by The Pale Shepherd at Tile " + shepherdPos + "!"`).
- **Removed Premature Mid-Flight Halts**:
  - Eliminated `SpecialMovePhase.INTERCEPTED_MIDAIR` and `SpecialMovePhase.INTERCEPTED_DESCENT`.
  - The character slides along the full descent arc until `progress >= 1.0f`, landing directly on Tile `shepherdPos`.
- **Arrival Trigger on Reaper's Tile**:
  - In `case LANDED:`, after the touchdown recovery animation finishes on Tile `shepherdPos`:
    - Checks `if (game.getPaleShepherdTile() > 0 && finalPos == game.getPaleShepherdTile())`.
    - Triggers `showPaleShepherdBoardDialogue(player)` with both the player character and The Pale Shepherd standing together on the tile.

### 3. Top-Anchored Dialogue Card Architecture ([BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java))
- In `buildShepherdDialogueUI()`:
  - Container table positioned at `top().padTop(20)`.
  - Card dimensions set to 960x225 px with an 18px rounded corner radius and 2px border.
  - Background styled in dark dusk violet: `Color.valueOf("#150c1ef8")` with crisp amethyst border `Color.valueOf("#a779c2")`.
  - Temporarily hides the top ribbon banner (`bannerTable.setVisible(false)`) while dialogue is active, restoring it cleanly (`bannerTable.setVisible(true)`) upon transition to battle or dialogue conclusion.
  - Integrated a top-right rounded pill button `[SKIP INTRO]` (`#371f45` fill, `#b382c4` border, `#ffd54f` text) that allows immediate transition directly to combat without stat dilemmas or traps.

### 4. High-Contrast Typography & Dedicated Fonts ([BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java))
- **Dedicated White-Base BitmapFonts**:
  - Declared `shepherdDialogueTitleFont` (26pt) and `shepherdDialogueFont` (22pt).
  - Initialized with `Color.WHITE` in `initAssets()`, completely eliminating the dark brown vertex tint inherited from `eventDescFont`.
  - Properly disposed in `dispose()`.
- **High-Contrast Palette**:
  - Speaker Name: `THE PALE SHEPHERD` in glowing warm amber (`#ffd54f`).
  - Subtitle: `A Soft Whisper In The Mist` in soft lavender (`#e0d4fc`).
  - Dialogue Body: Pure, crisp white (`#ffffff`) for maximum legibility.
  - Multi-Beat Narrative:
    - **Beat 1**: *"Softly now, little wanderer. You were falling so fast."* | Button: `[CONTINUE (1/3)]` (Amethyst pill `#512d61` / `#b583c7`).
    - **Beat 2**: *"The stones on this mountain are sharp, and your paws are bruised. Do you stumble because you are weary... or because you are rushing toward the end?"* | Button: `[CONTINUE (2/3)]`.
    - **Beat 3**: *"If your heart still yearns to reach the summit, show me the warmth left in your spirit."* | Button: `[STAND UP & FACE HIM]` (Terracotta pill `#8a472c` / `#e59368`).

### 5. Substantial Rewards for Defeating The Pale Shepherd ([BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java), [BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java))
- In `BoardScreen.java` (`pendingBattleOutcome == BattleOutcome.VICTORY`):
  - Awards `+100 EXP` (advancing player level progression).
  - Permanent stat upgrades: `+10 Max HP` via `player.modifyMaxHealth(10)` and `+1 ATK` via `player.setAttack(player.getBaseAttack() + 1)`.
  - Full heal: `player.setHealth(player.getMaxHealth())`.
  - Bestows Legendary Relic: `"Pale Shepherd's Crook"` (+4 DEF, 3 uses) created via `Item.createEquipment(...)`.
  - Full-satchel protection: If the player's satchel is at 4/4 capacity, the interactive item swap modal automatically opens so the player can select which relic to swap or discard.
  - Banishes The Pale Shepherd 15 tiles back and imposes 3 contemplation rounds.
- In `BattleScreen.java` (`showVictoryScreen`):
  - Header: `MIST SETTLES`
  - Title: `QUIET TRIUMPH` (`#ffd54f`)
  - Subtitle: `THE PALE SHEPHERD BESTOWS HIS GENTLE BLESSING`
  - Rewards Text: `+100 EXP  |  Full Health Restored  |  Permanent +10 Max HP & +1 ATK` (`#69f0ae`)
  - Loot Text: `Acquired: Pale Shepherd's Crook (+4 DEF)  |  Reaper Paused 3 Rounds` (`#ffd54f`)
  - Status Text: `Hero Health: [Full HP] / [Full HP] HP (Fully Restored & Empowered)` (`#ffe0b2`)

### 6. Automated Verification & Visual Capture ([BoardAdventureBugFixTest.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/board/BoardAdventureBugFixTest.java), [ShepherdModalCaptureLauncher.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/lwjgl3/src/main/java/io/github/snakes_and_ladders_roll_of_fate/lwjgl3/ShepherdModalCaptureLauncher.java))
- Updated `testPaleShepherdMortalDuelResolution()` in `BoardAdventureBugFixTest.java` to verify:
  - 100 EXP gain advancing player level.
  - +10 Max HP increase.
  - +1 permanent ATK increase.
  - Full health restoration.
  - Bestowal of `"Pale Shepherd's Crook"` in satchel.
  - 15-tile banishment and 3 pause rounds.
- Cleaned `ShepherdModalCaptureLauncher.java` to place both player and reaper on Tile 22, verifying board state and dialogue top placement.

---

## Verification Results
- **Automated Test Suites**:
  - `./gradlew compileJava :lwjgl3:compileJava testBugFixes testBoard testInventory testClassSkills` passed with 100% success rate across all 10 bug fix tests, 1,000 board simulations, inventory tests, and class skills.
- **Visual Capture Artifacts**:
  - `shepherd_dialogue_top_beat1.png`: Confirms top-anchored dialogue card (`top().padTop(20)`), bright white text, amber title, lavender subtitle, unobstructed board tiles, and both player character and reaper standing together on Tile 22.
  - `shepherd_dialogue_top_beat3.png`: Confirms Beat 3 text and `[STAND UP & FACE HIM]` terracotta action button.
  - `shepherd_gentle_battle_turn1.png`: Confirms gentle combat interface with flipped sprite facing left, 45 HP, and `"The Pale Shepherd raises his crook in quiet contemplation."`.
