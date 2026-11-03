## 📓 Devlog 08: Turn-Based Battle System & Combat Loop Implementation
Date: November 3, 2026
Author: John Emmanuel Q. Seda (Lead Programmer)
Sprint / Milestone: Week 5 – Encounters: Battle System
Branch: `feat/battle-system`

## 🎯 Objectives
- Implement a core `BattleSystem` utilizing a 20-sided dice mechanic for turn-based combat resolution, including critical hit and miss calculations.
- Integrate combat encounters into `SnakeTile` to transform negative traversal events into interactive risk/reward battles.
- Establish entity stat scaling, damage calculation, and threshold-based health management to ensure smooth combat flow and prevent softlocks.
- Design a fallback retreat mechanic (25% HP threshold) to allow players to survive lethal encounters at the cost of board position. (**To be Changed**)

## 🛠️ Work Completed
- **`systems/BattleSystem.java`**:
  - Implemented core combat loop orchestrator managing attacker/defender states and battle lifecycle (`startBattle()`, `endBattle()`).
  - Designed `calculateDamage()` utilizing a 20-sided `Dice` roll, incorporating base damage (`max(1, atk - def)`), standard roll additions, and critical mechanics (Roll of 20 = double damage, Roll of 1 = 0 damage).

- `board/SnakeTile.java`:
  - Refactored `onLand()` to instantiate an `Enemy` and initiate a `BattleSystem` encounter instead of an automatic positional slide.
  - Implemented dynamic stat scaling where the Snake's health and attack power scale based on its tile position, creating a progressive difficulty curve.
  - Built a turn-based `while` loop resolving player and enemy attacks sequentially, with strict boundary conditions (`> 0`) to prevent infinite loops.
  - Implemented immediate health clamping (`setHealth(0)`) upon detecting lethal damage, ensuring entity states never drop into negative values and console outputs remain clean.
  - Added a fallback escape mechanic: if player health drops to 25 or below (`<= 25`), the battle breaks, and the player slides down the snake to safety, preserving the game state.
  - Integrated experience rewards (`player.setExperience(player.getPosition() * 2)`) upon successfully defeating the snake.

## 🚧 Challenges & Blockers Resolved
- **Threshold vs. Exact Match Logic:** Utilized inclusive threshold checks (`<= 25`, `<= 0`) instead of strict equality (`==`). This ensures that high-damage rolls that skip over exact numbers still correctly trigger the retreat or game-over states without failing silently.
- **Turn Execution & Death Resolution:** Designed the combat loop to evaluate health thresholds *immediately* after damage calculation, followed by a `break;` statement. This intentional turn-order design prevents the "dead men attacking" scenario, ensuring a defeated enemy cannot execute its turn.
- **Negative Health Display & State Protection:** Addressed the issue where high damage values would push health into the negatives, causing erratic UI/console displays. By evaluating the damage, checking for the `<= 0` death condition, and immediately clamping the value to `0` (or using `Math.max(0, health)` for the retreat threshold), the entity's internal state and visual outputs are completely protected.

## 🔜 Next Steps
- Bridge `BattleSystem` with `EventManager` to trigger combat encounters from event choices (e.g., aggressive options in the Ancient Shrine).
- Translate combat prompts, health tracking, and dice rolls into LibGDX Scene2D UI dialog windows for graphical gameplay on `BoardScreen`.
- Implement item drops, inventory management, and temporary status effects for defeated enemies.
