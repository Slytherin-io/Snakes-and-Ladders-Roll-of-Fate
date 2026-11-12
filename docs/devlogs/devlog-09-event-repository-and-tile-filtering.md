# 📓 Devlog 09: Data-Driven Event Repository, Tile Filtering & Turn Bugfixes

* **Date:** November 9, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 5 – Data-Driven Event Architecture & Turn Lifecycle Bugfixes
* **Branch:** `feat/event-encounters`

---

## 🎯 Objectives
- Transition the event encounter system from ad-hoc lambda functions to a modular, data-driven architecture.
- Implement the **Repository Pattern** to decouple event data storage from `EventManager` controller logic.
- Introduce tile-range zoning (`minTile` / `maxTile`) so encounters trigger contextually based on the player's board position.
- Diagnose and eliminate an `IndexOutOfBoundsException` caused by player removal during multiplayer turn cycles in `ConsoleTestGame.java`.

## 🛠️ Work Completed
- **`systems/EventCategory.java`**: Created an `enum` defining encounter themes (`SHRINE`, `TRAP`, `MERCHANT`, `BLESSING`) for structured classification.
- **`systems/EventEffectType.java` & `systems/EventEffect.java`**: Built a centralized, reusable effects engine supporting `HEALTH`, `ATTACK`, `DEFENSE`, `EXPERIENCE`, and `MOVE` actions with built-in boundary safety (`Math.max(0, ...)`).
- **`systems/EventChoice.java`**: Implemented the Builder pattern via `withEffect(type, value)`, enabling fluent chaining of multiple effects onto a single narrative choice.
- **`systems/Event.java`**: Upgraded the encounter model to encapsulate `EventCategory`, board boundaries (`minTile`, `maxTile`), and an eligibility validator (`isEligibleForTile(int tilePosition)`).
- **`systems/EventRepository.java`**: Established a dedicated data warehouse (`getDefaultEvents()`), extracting hardcoded narrative strings and choice payloads out of the manager.
- **`systems/EventManager.java`**:
  - Reduced `loadDefaultEvents()` to a single, clean delegation line querying `EventRepository`.
  - Implemented `selectEventForTile(int playerTilePosition)` to filter available events dynamically by player position before rolling a random encounter.
- **`ConsoleTestGame.java`**:
  - Connected `selectEventForTile(currentPlayer.getPosition())` into the `NormalTile` landing check.
  - Implemented bounds recovery when a player dies, ensuring `currentTurn` wraps safely to index `0` when the list shrinks.
  - Added defensive boundary guards inside `checkGameOver()` and `getCurrentPlayer()`.

## 🚧 Challenges & Blockers Resolved
- **Resolving the Turn Loop `IndexOutOfBoundsException`**: Identified a critical flaw where calling `players.remove(currentTurn)` when the last player in the list died left `currentTurn` pointing past the new list boundary (`currentTurn == players.size()`). When the loop resumed or evaluated `checkGameOver()`, calling `players.get(currentTurn)` threw a fatal out-of-bounds error. Resolved by wrapping `currentTurn = 0` whenever it meets or exceeds `players.size()`.
- **Decoupling Data from Controller Logic (Repository Pattern)**: Moving event construction into `EventRepository` prevents `EventManager` from becoming a bloated, multi-hundred-line file, adhering to the Single Responsibility Principle and paving the way for future JSON-based external asset loading.
- **Context-Aware Board Traversal**: Replaced flat random event picking with tile-bracketed zoning, ensuring low-level events (e.g., beginner shrines) appear early on (Tiles 1–35) while hazardous traps appear mid-to-late board (Tiles 20–80).

## 🔜 Next Steps
- Refine `EventManager` to support specialized event triggers when landing on Snake and Ladder obstacle tiles.
- Connect `BattleSystem` with `EventManager` to initiate turn-based combat encounters directly from dynamic event choices and monster ambushes.
