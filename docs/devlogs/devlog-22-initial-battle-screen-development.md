# 📜 Devlog 22: Initial Battle Screen Development & Turn-Based Encounter Architecture

* **Date:** November 24, 2026
* **Author:** John Emmanuel Q. Seda (Lead Programmer)
* **Sprint / Milestone:** Week 6 – Turn-Based Battle Screen & Encounter Architecture
* **Branch:** `feat/battle-screen`

---

## 🎯 Objectives
- Transition snake tile encounters from simple in-board text rolls into a dedicated, visual 2D battle scene (`BattleScreen.java`).
- Create an intuitive turn-based battle control panel featuring **ATTACK**, **DEFEND**, **ITEM**, and **ESCAPE** buttons.
- Render dynamic parallax backgrounds that adapt to the current board region (Act 1 Forest, Act 2 Twilight, Act 3 Abyss).
- Integrate the animated snake enemy sprite asset (`enemy_snake.png`).
- Connect the player's satchel to battle so items and potions can be used during combat.
- Establish smooth transitions back to the main adventure board upon combat victory, defeat, or escape.

---

## 🛠️ Work Completed

### 1. Dedicated BattleScreen & Parallax Viewport
- Created `BattleScreen.java` using LibGDX `ExtendViewport(1920, 1080)`.
- Implemented parallax background rendering using the game's Act 1, Act 2, and Act 3 environment textures. The battle arena reflects the player's current location on the game board.
- Positioned two stone platforms in the arena: one on the left for the player hero and one on the right for the enemy serpent.

### 2. Action Controls & Turn State Loop
- Built four primary action buttons in the bottom control bar:
  - **ATTACK**: Executes a standard physical strike against the enemy.
  - **DEFEND**: Raises defensive guard to reduce incoming damage on the enemy's turn.
  - **ITEM**: Opens the player satchel to use consumable potions.
  - **ESCAPE**: Attempts a tactical retreat down the snake's slope to survive.
- Styled buttons with the project's signature warm leather and mahogany theme (`UIUtils.createRoundedDrawable`).
- Coordinated actions with `BattleSystem.java`:
  - Enforced clear turn order: `PLAYER_TURN` → `ENEMY_TURN` → `BATTLE_END`.
  - Added button locking and visual dimming during enemy turns so players cannot mash buttons during actions.

### 3. Animated Enemy Sprite & Combat HUD
- Integrated `enemy_snake.png` (a 32×32 pixel animated serpent spritesheet).
- Built an animated idle loop for the snake so it breathes and coils naturally in battle.
- Added a top encounter banner showing the serpent type (`AMBUSH: SHADOW ASP!`).
- Created a top battle log container displaying real-time strike and damage information.
- Added a bottom-left Player Health pill displaying current HP versus maximum HP.

### 4. In-Combat Satchel (Item Popup)
- Created an interactive item popup window accessible via the **ITEM** button.
- Players can view items currently held in their satchel and tap potions to restore health mid-fight.
- The item popup dynamically closes upon consuming an item and resumes combat flow.

### 5. Combat Resolution & Board Return
- Integrated outcome handoff with `SnakeTile.java`:
  - **Victory**: Grants progression EXP, rolls random loot into the satchel, and keeps the player on their tile.
  - **Defeat / Retreat**: Triggers the snake slide mechanic, returning the player safely down to the tail destination tile.
- Added an initial continue overlay to return players to the main game board once the battle concludes.

---

## 🔍 Verification & Testing
- Verified `BattleScreen` loads cleanly when triggered on snake encounters.
- Confirmed turn progression: Player action executes, enemy retaliates, and combat properly ends when either combatant reaches 0 HP.
- Verified in-combat potion consumption heals the player and deducts from the inventory.
- Ensured automated tests in `ConsoleTestGame` and `InventoryTest` remain compatible and pass without regression.
