# 📓 Devlog 01: Core OOP Architecture & Class Scaffolding

* **Date:** November 1, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 3 – Core Class Development
* **Branch:** `dev`

---

## 🎯 Objectives
- Establish the package directory structure inside `core/src/main/java/`.
- Scaffold the 19 core classes conforming to the approved OOP proposal.
- Enforce the 4 OOP principles (Encapsulation, Inheritance, Polymorphism, Abstraction).

## 🛠️ Work Completed
- **`screens/`**: Created `MainMenuScreen`, `BoardScreen`, and `BattleScreen` implementing LibGDX's `Screen` lifecycle.
- **`entities/`**: Implemented encapsulated abstract `Entity` with inherited `Player` and `Enemy` subclasses.
- **`board/`**: Built polymorphic `Tile` hierarchy (`NormalTile`, `SnakeTile`, `LadderTile`) and composed `Board`.
- **`systems/`**: Created controllers for `Dice` (D20), `BattleSystem`, and `EventManager`.
- **`items/`**: Created `Item` and capacity-managed `Inventory`.

## 🚧 Challenges & Blockers Resolved
- Corrected UML relationship modeling: replaced improper inheritance with composition and aggregation.
- Enforced encapsulation by converting entity attributes from public to private with getters/setters.

## 🔜 Next Steps
- Implement procedural board generation logic in `Board.java`.
- Wire dice roll movement mechanics into `BoardScreen.java`.
