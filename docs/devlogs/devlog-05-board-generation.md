# 📓 Devlog 05: Randomized Board Generation

* **Date:** November 2, 2026
* **Author:** John Emmanuel Q. Seda (Lead Programmer)
* **Sprint / Milestone:** Week 4 – Randomized Board Generation
* **Branch:** `feat/board-generation`

---

## 🎯 Objectives
- Implement randomized board generation (`generateBoard()`).
- Populate balanced snake and ladder tile positions across the 100-tile board grid.

## 🛠️ Work Completed
- **`board/Tile.java`**: Implemented `createRandom()` to pick which tile type will be generated.
- **`board/Board.java`**: Implemented `generateBoard()` to generate tiles with random tile types using `createRandom()` into a list of tiles
- 
## 🚧 Challenges & Blockers Resolved
- **Randomization of tile types**: Assigning different tile types to the board, and maintaining balance to the distribution of ladder and snake tiles

## 🔜 Next Steps
- Collaborate with teammate on implementing the battle system to the game for combat gameplay.

