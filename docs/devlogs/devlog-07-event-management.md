# 📓 Devlog 07: Dynamic Event Management & Interactive Encounters

* **Date:** November 3, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 4 – Dynamic Event Management & Interactive Encounters
* **Branch:** `feat/event-encounters`

---

## 🎯 Objectives
- Implement an interactive `Event` and `EventManager` lifecycle (`trigger()`, `resolve()`) to introduce risk/reward decision-making into board traversal.
- Utilize modern Java functional interfaces (`BiConsumer<Player, Integer>`) to cleanly decouple event definitions from outcome resolution logic.
- Populate diverse tabletop RPG event scenarios (e.g., *Ancient Shrine*, *Wandering Peddler*, *Hidden Springboard Trap*) capable of altering player health, attack, defense, experience, and tile position.
- Integrate a probabilistic trigger hook into `ConsoleTestGame.java` (35% encounter chance on `NormalTile`) to test real-time player dilemma resolution during game loop simulation.

## 🛠️ Work Completed
- **`systems/Event.java`**:
  - Implemented core encounter data structure encapsulating `title`, `description`, `choices` (`List<String>`), and an optional functional callback `outcome` (`BiConsumer<Player, Integer>`).
  - Added formatted CLI presentation in `trigger(Player player)` that dynamically lists all numbered options.
  - Implemented safe choice dispatch in `resolve(int choiceIndex, Player player)` with boundary checks against invalid numeric inputs.
- **`systems/EventManager.java`**:
  - Built a centralized event orchestrator managing an internal registry of game events.
  - Implemented `loadDefaultEvents()` with three diverse branching encounters:
    1. **Ancient Shrine**: Celestial blessing granting vitality (+25 HP), martial power (+2 Attack), or peaceful departure.
    2. **Wandering Peddler**: Shady trade-off risking health for knowledge (-10 HP, +30 EXP), defensive parry training (+2 Defense), or polite decline.
    3. **Hidden Springboard Trap**: Kinetic dilemma riding mechanical momentum (+2 tiles forward) vs. an evasive dodge roll (-10 HP safe bruise).
  - Added randomized event selection (`selectEvent()`) using `java.util.Random`.
  - Added convenience wrapper methods (`triggerEvent()`, `resolveEvent()`) supporting both default and player-targeted resolution.
- **`ConsoleTestGame.java`**:
  - Integrated `EventManager` into the game runner's initialization.
  - Added an encounter probability check (`Math.random() < 0.35`) upon landing on any `NormalTile`.
  - Implemented synchronous scanner handling to capture choice inputs, flush remaining newlines, and immediately execute event consequences before turn progression.

## 🚧 Challenges & Blockers Resolved
- **Decoupled Consequence Execution via Functional Callbacks**: Rather than bloating the class hierarchy with dozens of rigid event subclasses or maintaining a giant, error-prone `switch` statement inside `EventManager`, adopted Java's `BiConsumer<Player, Integer>`. Each event encapsulates its own execution logic cleanly, directly mutating player stats or triggering movement upon selection.
- **Turn Pacing & Scanner Buffer Management**: Ensured that reading choice integers via `sc.nextInt()` properly flushes the trailing newline (`sc.nextLine()`), preventing skipped prompts or erratic loop skipping during the subsequent turn's "Press ENTER to roll" pause.
- **Elevating Neutral Board Spaces**: Transformed static `NormalTile` spaces from passive rest stops into dynamic, suspenseful decision points, ensuring high replayability without relying on external cloud APIs or violating proposal requirements.

## 🔜 Next Steps
- Bridge `EventManager` with `BattleSystem` to trigger combat encounters when players land on monster ambush tiles or choose aggressive options.
- Expand event outcomes to support item acquisition, inventory utilization, and temporary status effects.
- Translate event prompt displays into LibGDX Scene2D UI dialog windows for graphical gameplay on `BoardScreen`.
