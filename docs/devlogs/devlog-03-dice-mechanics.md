# 📓 Devlog 03: Movement and Combat Dice Mechanics

* **Date:** November 2, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 4 – Working Dice Rolls & Core Movement
* **Branch:** `feat/dice-roll`

---

## 🎯 Objectives
- Implement dual dice mechanics: a 6-sided die (D6) for balanced board exploration and a 20-sided die (D20) for dynamic combat.
- Decouple `Dice` from external LibGDX dependencies to allow 100% portable, headless console testing.
- Construct the interactive movement game loop with user turn prompts and win-state evaluations.
- Integrate character progression stat scaling upon leveling up.

## 🛠️ Work Completed
- **`systems/Dice.java`**: Refactored to use standard Java (`java.util.Random`), enabling standalone execution across any terminal without requiring LibGDX JARs on the classpath.
- **`screens/BoardScreen.java`**: Instantiated `movementDice = new Dice(6)` and implemented `rollForMovement(Player player)` to advance players along the board.
- **`systems/BattleSystem.java`**: Instantiated `combatDice = new Dice(20)` and implemented tabletop-style damage calculation:
  - **Natural 20 (Critical Hit):** Deals double damage ($2 \times \text{Base Damage}$).
  - **Natural 1 (Critical Miss):** Attack misses completely (0 damage).
  - **Rolls 2–19:** Standard damage based on $(\text{Attack} - \text{Defense})$.
- **`entities/Player.java`**: Enhanced `levelUp()` to scale base attributes (`attack +2`, `defense +1`, `health +20`), keeping progression distinct from dice luck.
- **`ConsoleTestGame.java`**: Built the interactive `runGameLoop()` method allowing players to take turns, roll the D6, move forward, and trigger game-over checks.

## 🚧 Challenges & Blockers Resolved
- **Class vs. Instance Architecture**: Rather than creating duplicate classes (`D6Dice`, `D20Dice`), leveraged OOP reusability by creating two separate instances from the single, parameterized `Dice` blueprint.
- **Scanner Input Buffer Skip**: Resolved the classic Java `Scanner` bug where `sc.nextLine()` immediately consumed a leftover `\n` from previous `nextInt()` calls by flushing the buffer prior to entering the turn loop.
- **Library Dependency in Terminal Testing**: Eliminated `com.badlogic.gdx.math.MathUtils` from the domain layer in favor of `java.util.Random`, restoring instant terminal compilation.

## 🔜 Next Steps
- Implement polymorphic tile landing effects (`onLand(Player player)`) for `NormalTile`, `SnakeTile`, and `LadderTile`.
- Wire tile interactions into the movement flow so players automatically slide down snakes or climb up ladders upon landing.
