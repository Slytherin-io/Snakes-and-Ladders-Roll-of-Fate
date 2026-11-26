# 📜 Devlog 25: Character Sprite Harmonization, Combat Mortality & Post-Battle Camera Transitions

* **Date:** November 24, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Visual Polish, Entity Harmonization, Combat Mortality & Locomotion Continuity
* **Branch:** `feat/board-adventure-screen`

---

## 🎯 Objectives
- **Character Asset Proportional Scaling & Height Harmonization**:
  - Enlarge all character walk spritesheets and idle avatars by 75% ($1.75\times$ scale) with nearest-neighbor pixel preservation to ensure characters look prominent and readable on the 1080p board.
  - Normalize the ground contact baseline across all 4 animal archetypes (Cat, Turtle, Fox, Raccoon) to resolve the visual glitch where characters appeared sunken or submerged into the stone slabs.
  - Harmonize relative character sizes so that the Fox, Raccoon, and Turtle stand at an equal, balanced eye-level height matching the Cat.
- **HUD Profile & Character Selection Carousel Polish**:
  - Expand the character portrait avatar in the active player's bottom-bar status HUD card so that hero identities stand out clearly beside their stats.
  - Enlarge the preview frame slot in the Character Selection carousel with live looping walk animations.
- **Combat Safeguard Resolution & True Player Mortality**:
  - Remove legacy combat safeguards that previously prevented player health from dropping below 1 HP, restoring genuine mortality across all battle encounters.
  - Ensure that lethal damage cleanly triggers `BattleOutcome.DEFEAT`, feeding directly into single-player defeat chronicles or multiplayer companion tracking.
- **Post-Battle Locomotion Continuity & Camera Glide Transitions**:
  - Eliminate the jarring camera snap and premature turn skipping that occurred when a player escaped combat from a snake tile.
  - Re-center the board camera on the returning player at the serpent tile, execute the full sliding animation down the serpent coils with dynamic hazard dust trails, pause on touchdown landing, and smoothly advance the turn while gliding the camera to the next player.

---

## 🛠️ Work Completed

### 1. Character Asset Scaling, Baseline Grounding & Height Harmonization
- **75% Uniform Spritesheet Expansion**:
  - Upscaled all walk cycle spritesheets ([char_cat_walk.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/char_cat_walk.png), [char_turtle_walk.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/char_turtle_walk.png), [char_fox_walk.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/char_fox_walk.png), and [char_raccoon_walk.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/char_raccoon_walk.png)) into uniform $224 \times 224\text{ px}$ frame cells using nearest-neighbor sampling.
  - Sliced and aligned walk animations: Cat (3 frames), Turtle (4 frames), Fox (6 frames), and Raccoon (6 frames).
  - In [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java#L159), expanded in-game display dimensions to `CHAR_DISPLAY_SIZE = 210f` (up from `120f`).
- **Stone Slab Grounding & Anti-Submersion Alignment**:
  - Diagnosed the submerged sprite glitch where character paws were drawn inside the stone slab.
  - Re-anchored the ground contact line across all 4 archetype walk frames to a shared baseline at $Y = 183\text{--}184\text{ px}$ within the $224\text{ px}$ cell.
  - Updated `getCharacterDrawY(tileIndex)` in [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java#L3034-L3036) to return `getTileBottomY(tileIndex) + 14f`, perfectly seating the characters' feet on top of the 60px stone slab walking surface across all three Acts.
- **Proportional Height Harmonization**:
  - Rebalanced individual sprite scaling so that all heroes stand at an equivalent visual height:
    - **Fox (Cunning Trickster)**: Scaled to **$2.71\times$** ($168 \times 130\text{ px}$ content bounds).
    - **Raccoon (Crafty Scavenger)**: Scaled to **$3.02\times$** ($155 \times 130\text{ px}$ content bounds).
    - **Turtle (Iron Guardian)**: Scaled to **$2.20\times$** ($194 \times 110\text{ px}$ content bounds).
    - **Cat (Nimble Prowler)**: Scaled to **$1.75\times$** ($158 \times 111\text{ px}$ idle / $146\text{ px}$ stride bounds).
  - Generated updated high-resolution static avatars ([char_fox.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/char_fox.png), [char_raccoon.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/char_raccoon.png), [char_turtle.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/char_turtle.png), and [char_cat.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/char_cat.png)) cropped with 4px borders.

### 2. HUD Profile & Character Selection Carousel Polish
- **Player HUD Avatar Slot**:
  - In [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java#L664), expanded the active player's portrait slot in the bottom leather status card to `size(110, 90)` with `Scaling.fit`.
  - Characters now fill the vertical height of the status card, providing clear, bold visual identity alongside health, level, attack, and defense stats.
- **Hero Selection Carousel Frame**:
  - In [MainMenuScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/MainMenuScreen.java#L571), enlarged the sprite preview slot inside the dark wood frame (`frameBox`) from `165×165` to **`size(175, 175)`**.
  - Animated walk cycles continue looping smoothly in real time as players cycle between heroes with the arrow buttons.

### 3. Combat Safeguard Resolution & True Player Mortality
- **Mortality Safeguard Removal**:
  - Identified and removed an outdated combat test safeguard in `BattleSystem.java` and `Player.java` that was forcing player health to remain at 1 HP when taking lethal damage.
  - Implemented true damage calculations allowing HP to reach 0.
  - When a hero reaches 0 HP, `BattleSystem.executeEnemyTurn()` properly sets `outcome = BattleOutcome.DEFEAT` and triggers combat conclusion.
- **Expedition Defeat Flow**:
  - **Single-Player**: Combat defeat immediately transitions to `GameSummaryScreen` with a solemn dirge celebrating the fallen hero's bravery before the cycle began anew.
  - **Multiplayer**: The fallen hero's defeat is recorded in `RunTracker`, a fallen banner displays on the board, and subsequent turns automatically bypass the fallen traveler while living companions continue the ascent. Total party wipes trigger the collective defeat screen.

### 4. Post-Battle Locomotion Continuity & Camera Glide Transitions
- **Root Cause Resolution**:
  - In [BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java#L607-L621), the victory/outcome modal previously called `game.nextTurn()` directly on the `"CONTINUE"` click before instantiating `new BoardScreen(game)`. This caused `BoardScreen` to initialize centered on Player 2, completely skipping the fleeing player's slide down the snake and bypassing camera transitions.
  - Replaced the premature turn advance with a delegated transition: `game.setScreen(new BoardScreen(game, outcome, snakeTile))`.
- **Phased Post-Battle Resolution in [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java#L315-L365)**:
  - Added constructor `BoardScreen(RollOfFateGame game, BattleOutcome battleOutcome, SnakeTile battleTile)`.
  - **On `BattleOutcome.ESCAPED` or `WEAKENED`**:
    1. Resets player position and camera focus to the serpent tile (`pendingBattleTile.getPosition()`).
    2. Displays notification banner: `"[PLAYER] ESCAPED - SLIDING DOWN TO TILE [DEST]!"`.
    3. Initiates `startSpecialMove(battleTilePos, destTile, false)` with red hazard dust trail, tracking the tumbling adventurer down the coils.
    4. Upon touchdown landing on the destination stone, reveals unchartered destination tiles, awards discovery EXP, and synchronizes the minimap.
    5. Transitions to `TurnState.TURN_TRANSITION`, pauses $0.5\text{--}0.8\text{ s}$ for recovery, executes `game.nextTurn()`, and smoothly pans the camera across the board to the next active player via `(targetX - camera.x) * (delta * 4.5f)`.
  - **On `BattleOutcome.VICTORY`**:
    - Re-centers on the battlefield, announces vanquished serpent banner, pauses 1.0s, and smoothly glides to the next player.
  - **On `BattleOutcome.DEFEAT`**:
    - Announces fallen traveler banner, pauses 1.2s, and transitions turn to the next living companion.

---

## 🔍 Verification & Testing

1. **Test Suites & Regression Checks**:
   - `.\gradlew.bat :core:testInventory`: Ran all 20 integration tests verifying inventory management, stat buffs, equipment degradation, item effects, and mortality verification (`[Test 20] BattleSystem Execution & True Mortality Verification`). Passed 20/20 with zero failures.
   - `.\gradlew.bat compileJava`: Verified 100% clean compilation across all game modules.
2. **Visual & Kinetic Validation**:
   - Verified that character feet rest directly on the stepping stones during idle and walk cycles.
   - Verified that Fox and Raccoon sprites fill the selection carousel frame proportionally alongside Cat and Turtle.
   - Verified that escaping combat plays the full slide animation down the board and smoothly pans the camera to the next player without abrupt snapping.
   - Executed `.\gradlew.bat :lwjgl3:runCapture`, confirming proper generation of victory and defeat debrief screens.
