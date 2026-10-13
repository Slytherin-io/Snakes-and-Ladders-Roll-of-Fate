# Project Structure Guide

Welcome to the repository for **Snakes and Ladders: Roll of Fate**. This guide outlines the directory structure of our LibGDX project and where specific code and assets should be placed to maintain a clean Model-View-Controller (MVC) architecture.

## 📁 Root Directory
The top level of our repository contains the Gradle configuration and the main modules of our game.

* **`assets/`**: The home for all visual and audio files. UI skins, fonts, character sprites, and board textures go here. The `core` module loads files directly from this folder.
* **`core/`**: The heart of the game. This contains all platform-independent Java code, game logic, and MVC architecture.
* **`lwjgl3/`**: The desktop-specific launcher module. It contains the `Lwjgl3Launcher.java` file which configures window size, framerate, and application title. This will rarely be modified.
* **`docs/`**: (To be created) The home for all project documentation, meeting minutes, and architectural references.
* **`.gitignore`**: Specifies files and directories that Git should ignore (like local IDE settings and compiled build caches).
* **`build.gradle` / `settings.gradle` / `gradlew`**: The Gradle build configuration and wrapper files.

---

## 🧠 Core Module Architecture (`core/src/main/java/...`)
We follow an Object-Oriented design separated by logical domains. Place new Java classes in the appropriate packages inside the `core` module:

* **`RollOfFateGame.java`**: The main entry point that extends LibGDX's `Game` class, holds the `AssetManager`, and manages screen transitions.

### 📦 Packages

* **`screens/` (Views)**
    * *Contains:* `MainMenuScreen`, `BoardScreen`, `BattleScreen`.
    * *Purpose:* These classes handle rendering (drawing the UI with Scene2D) and capturing user input (clicks/key presses).

* **`entities/` (Models)**
    * *Contains:* `Entity`, `Player`, `Enemy`.
    * *Purpose:* Manage core attributes like health, defense, attack, and leveling.

* **`board/` (Models)**
    * *Contains:* `Board`, `Tile`, `SnakeTile`, `LadderTile`.
    * *Purpose:* Handle the data structure of the map and tile properties.

* **`systems/` (Controllers)**
    * *Contains:* `BattleSystem`, `EventManager`, `Dice`.
    * *Purpose:* The managers that enforce game rules. Keep this logic separated from the `screens` package so the rules can run independently of the graphics.

* **`items/` (Models)**
    * *Contains:* `Item`, `Inventory`.
    * *Purpose:* Handle consumable objects and player inventory capacity.

---

## 🛠️ Development Rules
1. **Asset Loading:** Always use LibGDX's `AssetManager` inside the main `RollOfFateGame` class to load files from the `assets/` folder. Do not instantiate new `Texture` objects inside individual screens to prevent memory leaks.
2. **UI Building:** Use `Scene2D.ui` for menus, combat action buttons, and HUD vitals.
3. **Adding Dependencies:** If adding a new LibGDX extension (e.g., FreeType for custom fonts), update the root `build.gradle` file and reload the Gradle project in your IDE.
