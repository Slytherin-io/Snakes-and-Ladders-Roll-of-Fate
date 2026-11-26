# 📓 Devlog 10: GUI Integration & Main Menu Screen

* **Date:** November 20, 2026
* **Author:** Dorothy Anne R. Domanico (Lead Designer & Asset Developer)
* **Sprint / Milestone:** Week 6 – GUI Integration & Main Menu Screen
* **Branch:** `feat/main-menu-gui`

---

## 🎯 Objectives
- Build a polished, visual Main Menu GUI to transition the game from console testing to a graphical desktop experience.
- Implement an animated, looping video background using `gdx-video` for an atmospheric dark-fantasy entrance.
- Establish a centralized asset management pipeline (`Assets.java`) to safely load and unload textures, skins, and videos.
- Add interactive UI animations (button hover scaling) and fullscreen/windowed screen controls.

## 🛠️ Work Completed
- **`ui/Assets.java`**:
  - Created a centralized asset registry using LibGDX's `AssetDescriptor`.
  - Registered the UI Skin (`uiskin.json`), title banner (`menu_title.png`), button textures, and background video (`menu_bg.webm`).
  - Added `loadAll(AssetManager manager)` to load all required menu assets cleanly on game startup.
- **`screens/MainMenuScreen.java`**:
  - Built the main menu layout using Scene2D `Table` and `ScreenViewport` for automatic scaling across different screen sizes.
  - Added the prominent game title graphic and three central menu buttons: **Play Game**, **Settings**, and **Exit**.
  - Integrated `VideoPlayer` (`gdx-video`) to stream and seamlessly loop the animated background video (`menu_bg.webm`).
  - Connected action listeners: **Exit** safely closes the game via `Gdx.app.exit()`, while **Play Game** and **Settings** are ready for screen switching.
  - Handled proper memory disposal of the `Stage`, `SpriteBatch`, and `VideoPlayer` in `dispose()`.
- **`ui/UIUtils.java`**:
  - Created reusable UI helper methods, including `addScaleHoverEffect(TextButton button)`.
  - Added smooth hover micro-interactions (buttons scale to 1.05x on mouse hover and return to 1.0x on exit).
  - Implemented programmatic rounded rectangle generation using `Pixmap` and 9-patch drawables.
- **`RollOfFateGame.java` & `Lwjgl3Launcher.java`**:
  - Configured game launch to initialize `AssetManager`, load menu assets synchronously, and display `MainMenuScreen`.
  - Added an `F11` toggle key in `RollOfFateGame.render()` to let players switch between Fullscreen and Windowed mode (1280x720).
  - Added `gdx-video` native dependencies in `core/build.gradle` and `lwjgl3/build.gradle`.

## 🚧 Challenges & Blockers Resolved
- **Seamless Video Looping**: The default video player does not automatically loop when a video finishes. Resolved by attaching an `OnCompletionListener` that catches completion and immediately restarts playback from file.
- **Button Scaling Origin**: Scaling Scene2D buttons on hover can make them jump awkwardly if their origin is set to the bottom-left corner. Resolved by enabling `button.setTransform(true)` and anchoring `button.setOrigin(Align.center)` so hover scaling expands evenly from the center.
- **Aspect Ratio & Screen Resizing**: Different display resolutions can distort UI elements. Addressed by wrapping the menu elements in a Scene2D `Table` inside a `ScreenViewport`, maintaining clean centered alignment when resizing the window.

## 🔜 Next Steps
- Implement the pre-board generation UI setup screens:
  - **Game Mode Selection UI**: Interface to choose between Single Player and Pass-and-Play modes.
  - **Player Selection & Setup UI**: Screen to configure the number of players (2–4) and assign player names.
- Connect the main menu **Play Game** button to transition into the game mode and player selection screens.
- Proceed to the graphical `BoardScreen` to render the 100-tile board grid, tile obstacles, and player pawns once player setup is complete.
