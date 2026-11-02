# 📓 Devlog 02: Game State Management & Turn Flow

* **Date:** November 2, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 3 – Game State Management
* **Branch:** `feat/game-state-management`

---

## 🎯 Objectives
- Implement core game state lifecycle methods: `startGame()`, `nextTurn()`, and `checkGameOver()`.
- Construct the initial transition flow from `MainMenuScreen` to the active `BoardScreen`.
- Support both Single Player and local Pass-and-Play game modes.
- Build and verify headless game logic via a console test runner.

## 🛠️ Work Completed
- **`RollOfFateGame.java`**: Implemented `startGame()` to initialize the board and players, round-robin turn cycling via `nextTurn()`, and victory/defeat evaluations in `checkGameOver()`.
- **`systems/GameMode.java`**: Created a type-safe `enum` (`SINGLE_PLAYER`, `PASS_AND_PLAY`) to eliminate magic numbers and strings.
- **`board/Board.java`**: Implemented prototype board generation and a formatted 10x10 grid console visualizer (`displayBoard()`).
- **`ConsoleTestGame.java`**: Created a dedicated console-based interactive test runner to simulate player setup, turns, and board printing without launching the LibGDX graphics window.

## 🚧 Challenges & Blockers Resolved
- **Game Mode Selection**: Replaced fragile string array indexing with a `GameMode` enum, preventing silent bugs and cleanly automating player count allocation.
- **Board Display Off-by-One Error**: Resolved an indexing mismatch in `displayBoard()` by switching to an enhanced for-each loop (`for (Tile tile : tiles)`), ensuring all 100 tiles display their true positions.
- **LibGDX Lifecycle Safety**: Ensured `super.render()` is invoked in `RollOfFateGame` to prevent black-screen rendering freezes.

## 🔜 Next Steps
- Implement procedural board generation logic (randomized snake & ladder placement) in `Board.java`.
- Wire dice roll movement mechanics into `BoardScreen.java`.