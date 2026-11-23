# 📓 Devlog 18: Main Menu GUI Flow, Modals & Character Selection

* **Date:** November 23, 2026
* **Author:** Dorothy Anne R. Domanico (Lead Designer & Asset Developer)
* **Username:** `chrysolace`
* **Sprint / Milestone:** Week 6 – Main Menu GUI Flow & Pre-Game Setup
* **Branch:** `feat/main-menu-gui`

---

## 🎯 Objectives
- Expand the Main Menu into a complete pre-game setup flow, transitioning players seamlessly from the title screen into gameplay.
- Build thematic modal interfaces using an enlarged ancient scroll aesthetic (`scroll.png`) and Sniglet typography.
- Implement the **Settings GUI** with interactive audio volume sliders.
- Implement the **Game Mode Selection** modal (Single Player vs. Pass & Play).
- Implement the **Player Count Selection** modal (strictly clamped between 2 and 4 players).
- Implement the **Character Selection** carousel with hero locking (**Approach A**), ensuring each player gets a unique champion per game.
- Ensure full screen responsiveness across any resolution or window resize using `ExtendViewport(1920, 1080)`.

---

## 🛠️ Work Completed

### 1. Typography & UI Foundation
- **Sniglet Font Integration**: Integrated `Sniglet-ExtraBold.ttf` and `Sniglet-Regular.ttf` via `UIUtils.createFont(...)` for a friendly, readable fantasy aesthetic across all labels, stats, and badges.
- **Scroll Modal Asset Refinement**: Cropped `scroll.png` to tightly bound visible pixels (342×263 px) and scaled it to `840×646 px`, providing ample flat parchment space for UI content.
- **Responsive Viewport**: Upgraded the Stage viewport to `ExtendViewport(1920, 1080)` to maintain perfect UI scaling on any display resolution without black bars or letterboxing.

### 2. Settings Modal
- Built the interactive parchment Settings dialog accessible via the **Settings** menu button.
- Added **Music** and **SFX** volume sliders (scaled 0 to 10) featuring:
  - Thematic audio icons (`icon_sound.png`, `icon_music.png`).
  - Dynamic live number readouts updating as sliders are moved.
  - A clean centered layout and a rounded brown `[ BACK ]` button returning to the title screen.

### 3. Game Mode Selection Modal
- Clicking **Play Game** opens the **Select Game Mode** parchment modal.
- Added two interactive rounded cards with hover scale effects:
  - **Single Player** (*Solo Game*): Direct 1-player adventure.
  - **Pass & Play** (*2 to 4 Players*): Local multiplayer couch co-op.
- Added generous badge padding and spacious card gaps so no elements touch the scroll borders.

### 4. Player Count Modal (Pass & Play)
- Selecting **Pass & Play** directs the player to a dedicated **Number of Players** modal.
- Includes left (`<`) and right (`>`) arrow stepper buttons flanking a centered count display.
- **Clamping Logic**: Strictly clamped between **2 and 4 players**. When at 2 players, the left arrow dims and disables; at 4 players, the right arrow dims and disables.
- Action buttons:
  - `[ BACK ]`: Returns smoothly to Game Mode selection.
  - `[ ENTER ]`: Advances to Hero Selection with the chosen player count.

### 5. Character Selection Carousel
- Built an interactive hero carousel showcasing the 4 playable archetypes: **Turtle**, **Cat**, **Fox**, and **Raccoon**.
- **Hero Presentation**:
  - Central character frame with placeholder sprites (`char_turtle.png`, `char_cat.png`, `char_fox.png`, `char_raccoon.png`).
  - Hero Name, Title, base RPG attributes (`HP`, `ATK`, `DEF`), and Perk / starting item information.
  - Dynamic top badge updating per turn: `PLAYER 1: SELECT HERO` -> `PLAYER 2: SELECT HERO` -> etc.
- **Approach A (Hero Locking)**:
  - When Player 1 locks in a hero, that character is marked as taken.
  - Subsequent players browsing to that hero see a red `LOCKED BY PLAYER X` banner, the sprite is dimmed, and the `[ ENTER ]` button turns into a disabled `[ TAKEN ]` button.
  - Clicking `[ BACK ]` steps backward to previous players, unlocking their choices.
- **Text Bounds & Word Wrapping**:
  - Bound all text labels to 460px and enabled multi-line text wrapping (`setWrap(true)`), ensuring hero titles and perks never overflow into the wooden scroll curls.
  - Replaced the missing bullet glyph with a clean vertical pipe separator (`|`) to prevent rendering boxes.

### 6. Game Launch Integration
- Updated `RollOfFateGame.java` with `startGameWithClasses(List<CharacterClass> chosenClasses)`:
  - Automatically instantiates players with their chosen character classes, attributes, and starting loadouts.
  - Launches `BoardScreen` once all players have confirmed their champions.

---

## 🔍 Key Challenges & Solutions
- **Video Decoding Viewport Reset**: `gdx-video` resets the OpenGL viewport to the video resolution upon decoding, trapping the video in the corner on window resize. Resolved by explicitly enforcing `glViewport(0, 0, screenW, screenH)` and re-centering the projection matrix before drawing the frame.
- **Scroll Edge Overlap**: Long perk descriptions stretched past the flat parchment area and overlapped the scroll rolls. Resolved by setting `setWrap(true)`, splitting perks into two balanced lines, and constraining text cell widths to 460px.
- **Glyph Compatibility**: The bullet symbol `•` was absent from default FreeType ASCII ranges, causing empty square artifacts (`[]`). Resolved by switching to a standard pipe separator (`|`).

---

## 🔜 Next Steps
- Implement the **Loading Screen** using the 36-frame pixel snake spritesheet loop (`loading_snake_loop.png`) to provide an animated transition into gameplay.
- Begin development of graphical **Board Generation** on `BoardScreen` (100-tile board grid, snakes, ladders, and pawn placements).
