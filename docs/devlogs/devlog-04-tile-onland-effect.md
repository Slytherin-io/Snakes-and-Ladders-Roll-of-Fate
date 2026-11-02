# 📓 Devlog 04: Polymorphic Tile Onland Effects & Resolution

* **Date:** November 2, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 4 – Snake & Ladder Tile Effects
* **Branch:** `feat/tile-onland-effect`

---

## 🎯 Objectives
- Implement polymorphic tile landing behavior (`onLand(Player player)`) across all board tile types.
- Populate balanced snake setbacks and ladder shortcuts across the 100-tile board grid.
- Integrate tile effect execution directly into the movement turn loop before game-over evaluations.
- Provide clear real-time console visual feedback when players encounter hazard and advance tiles.

## 🛠️ Work Completed
- **`board/LadderTile.java`**: Implemented `applyEffect(Player player)` to advance the player's position to the ladder's top destination.
- **`board/SnakeTile.java`**: Implemented `applyEffect(Player player)` to slide the player backwards to the snake's tail destination.
- **`board/Board.java`**: Configured 7 realistic snakes (e.g., 14 $\rightarrow$ 4, 99 $\rightarrow$ 69) and 7 ladders (e.g., 9 $\rightarrow$ 31, 74 $\rightarrow$ 91) with precise 0-indexed array mapping.
- **`ConsoleTestGame.java`**: Wired `currentTile.onLand(currentPlayer)` directly into `runGameLoop()`, displaying tile notifications and resolving destination adjustments dynamically.

## 🚧 Challenges & Blockers Resolved
- **Evaluation Order Correction**: Resolved an edge-case bug where `checkGameOver()` was executed *before* tile effects took place. Moving the win check *after* `onLand()` ensures that climbing a ladder to tile 100 triggers an immediate victory rather than waiting for an extra turn.
- **Narrative Print Sequencing**: Reordered console logging so tile entry headers (`** ENTERED LADDER TILE **`) print *before* positional updates occur, producing clean, intuitive game logs.

## 🔜 Next Steps
- Collaborate with teammate on procedural board generation algorithms to randomize tile placement.
- Prepare combat encounter triggers (`BattleTile` / battle hooks) for Week 5 feature integration.
