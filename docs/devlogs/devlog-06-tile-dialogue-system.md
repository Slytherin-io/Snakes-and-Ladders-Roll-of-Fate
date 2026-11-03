# 📓 Devlog 06: Procedural Tile Dialogue & Narrative System

* **Date:** November 3, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 4 – Tile Narrative & Procedural Dialogue System
* **Branch:** `feat/tile-dialogue-system`

---

## 🎯 Objectives
- Build an atmospheric, procedural narrative layer to enrich board traversal during turn resolution.
- Implement randomized, combinatorial flavor text generation for every tile type (`NormalTile`, `LadderTile`, and `SnakeTile`).
- Integrate dialogue triggers directly into polymorphic `onLand(Player player)` executions across the `Tile` class hierarchy.
- Maintain lightweight, headless compatibility with standard Java (`java.util.Random`) for instant terminal simulation while remaining ready for LibGDX Scene2D UI integration.

## 🛠️ Work Completed
- **`systems/Dialogue.java`**: Created a modular procedural text generation engine utilizing combinatorial sentence pools (`[Prefix] [Subject] [Action]`):
  - **Snake Encounters (`createSnakeDialogue`)**: Synthesizes menacing ambush narratives (e.g., *"Suddenly, a gargantuan emerald viper blocks your ascent!"*, *"Emerging from the mist, a shadowy horned python demands a sacrifice of flesh!"*).
  - **Ladder Ascents (`createLadderDialogue`)**: Synthesizes awe-inspiring upward pathways and arcane shortcuts (e.g., *"Carved into the sheer cliffside, a grand set of weathered marble stairs beckons you toward the heavens!"*).
  - **Normal Tile Respite (`createNormalDialogue`)**: Delivers evocative environmental ambiance and contemplative pauses along the road (e.g., *"Under a pale, silent sky, a peaceful roadside shrine allows you a brief moment to catch your breath."*).
- **`board/Tile.java`**: Introduced a shared `protected static Dialogue dialogue` reference accessible across all derived tile types, centralizing dialogue generation without per-tile memory overhead.
- **`board/NormalTile.java`**: Hooked `dialogue.createNormalDialogue()` into `onLand(Player player)`, turning neutral spaces into meaningful atmospheric checkpoints.
- **`board/LadderTile.java`**: Configured `dialogue.createLadderDialogue()` to announce the ascent before executing positional displacement in `applyEffect(player)`.
- **`board/SnakeTile.java`**: Configured `dialogue.createSnakeDialogue()` to dramatically herald the ambush before penalizing the player via `applyEffect(player)`.
- **`ConsoleTestGame.java`**: Integrated dialogue system imports into the console turn loop to verify interactive storytelling output during automated or manual test play.

## 🚧 Challenges & Blockers Resolved
- **Dynamic Variety vs. Static Repetition**: Rather than hardcoding static, repetitive strings across 100 tiles, implemented a procedural 3-part grammar pool ($3 \times 3 \times 3 = 27$ snake variations, $4 \times 4 \times 4 = 64$ ladder variations, and $4 \times 4 \times 4 = 64$ normal tile variations) providing 155+ unique procedural descriptions.
- **Memory Optimization via Static Sharing**: Avoided instantiating 100 separate `Dialogue` instances across the board grid by managing a single shared static generator inside `Tile`, ensuring zero redundant memory footprint while preserving clean subclass inheritance.
- **Decoupled Architecture for Headless Testing**: Kept `Dialogue` decoupled from LibGDX UI widgets, enabling narrative verification in terminal test runners while preparing clean string payloads for eventual Scene2D dialog popups.

## 🔜 Next Steps
- Implement core `EventManager` and `Event` lifecycle methods (`trigger()`, `resolve()`) to handle interactive dilemmas and branching player choices.
- Populate diverse event encounters (e.g., stat gambles, merchant offerings, moral trials) that alter player health, items, or board position.
- Integrate `EventManager` into the active board turn loop alongside `Dialogue` for seamless narrative-to-event transitions.
