# Devlog 38: Seamless Board Parallax Reconstruction, Full Keyboard Accessibility & Epilogue Audio/Pacing Polish

* **Date:** November 28, 2026
* **Authors:** John Calvin D. Samson (Project Leader) & John Emmanuel Q. Seda (Lead Programmer)
* **Sprint / Milestone:** Week 6 - Pre-Release Polish: Seamless 3-Act Parallax, Full Keyboard Navigation & Defeat Epilogue Refinement
* **Branch:** `feat/board-adventure-screen`

---

## Objectives
- **Class Skill Combat Unification & Visual FX** (`4c38f08`, `0502494`): Unify class skills (`CatAgilitySkill`, `TurtleShellSkill`, `FoxCunningSkill`, `RaccoonScavengeSkill`) with dedicated battle animations, particle FX, and audio cues inside `BattleScreen`.
- **BoardScreen Parallax Reconstruction**: Refine the 3-Act board background using the existing `act1_green.png`, `act2_blue.png`, and `act3_red.png` sheets to match the reference layer order, crisp pixel proportions, sky haze tones, and seamless infinite horizontal scrolling.
- **Full Conventional Keyboard Accessibility**: Ensure every button, modal, slider, dice picker, barter/swap card, dialogue prompt, and end-game screen across the entire game is operable via conventional keyboard inputs (`WASD`, `Arrow Keys`, `Tab`/`Shift+Tab`, `Enter`/`Space`, `Esc`/`Backspace`, and number hotkeys `1–9`).
- **Epilogue Pacing, Slide Separation & Dedicated Audio**: Give Victory and Defeat epilogues dedicated looping atmospheric tracks (`music_act4.mp3` and `music_act5.mp3`), hide remaining slide counters, separate Solo vs. Pass-and-Play conclusions on Slide 4, and route all solo/party-wipe defeat endings through `EndingStoryScreen`.
- **UI Typography & Glyph Cleanup**: Eliminate unsupported Unicode emoji characters that rendered as missing-glyph tofu boxes (`□□□`) in Fredoka fonts during boss combat logs and floating FX text.

---

## Work Completed

### 1. Seamless 3-Act Board Parallax Reconstruction ([BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java))
- **True Depth Ordering & Proportional Scale**:
  - Corrected the layer stack from back to front across all Acts (including during crossfade transitions):
    1. **Far Silhouettes (`0.12x` parallax)**: `silY = GROUND_BASE_Y + 235f` (`345f`), `silH = 440f`, `silAlpha = 0.52f`, allowing distant silhouettes to peek naturally above the main canopy without obscuring foreground tree trunks.
    2. **Main Detailed Forest & Blood Moon (`0.35x` parallax)**: `treeY = GROUND_BASE_Y + 62f` (`172f`), `treeH = 560f`, restoring natural pixel-art proportions behind the turf horizon instead of vertically stretching canopies off-screen.
    3. **Foreground Terrain Ground (`1.0x` world-locked)**: `groundY = GROUND_BASE_Y` (`110f`), `groundH = 248f`, sharing a unified ~2.2x texel scale with the forest layers.
- **Eliminating Seams & Border Halos (`extractSeamlessLayer` & `drawRepeatLayer`)**:
  - Replaced raw `TextureRegion` slicing on the 1024x585 packed sheets with runtime `Pixmap` extraction into dedicated `Texture` layers wrapped with `TextureWrap.Repeat` (horizontal) and `ClampToEdge` (vertical).
  - Cropped strictly inside opaque pixel bounds per sheet strip and blended horizontal wrap seams (`36–56px` alpha-weighted smoothstep crossfade) to prevent bilinear gutter bleed and vertical tile seams.
  - Drawn via a single viewport-wide UV-scrolled quad per pass (`drawRepeatLayer`), cleanly disposed via `generatedParallaxTextures` in `dispose()`.
- **Palette Calibration**:
  - Matched `glClearColor` atmospheric sky colors (`#2d5b61` Act I teal haze, `#1a4074` Act II deep sapphire, `#39263d` Act III dusky plum) and pixel-matched subterranean foundation fills (`#20202a`, `#173066`, `#311634`) to the bottom edge of each Act's ground platform.

---

### 2. Full Conventional Keyboard Accessibility ([UIUtils.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/ui/UIUtils.java), [MainMenuScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/MainMenuScreen.java), [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java), [BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java), [EndingStoryScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/EndingStoryScreen.java), [GameSummaryScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/GameSummaryScreen.java))
- **Shared Focus & Activation Helpers (`UIUtils`)**:
  - Added `activateButton` (guarding disabled/invisible controls), `stepFocus` (cleanly skipping disabled buttons), `applyFocusVisuals` (consistent gold `#ffe082` tint and `1.045f` focus scale), `justPressedNumberKey`, and unified `WASD`/Arrow/Confirm/Back input checks.
- **Screen-by-Screen Coverage**:
  - **`MainMenuScreen`**: `Up`/`Down`/`W`/`S`/`Tab` navigation with visual focus indicators across Main Menu, Game Mode, 2-row Player Count (`2`/`3`/`4`, `-`/`+`), 2-row Hero Select (`Q`/`E`, `1–4`, `<`/`>`), and Settings volume sliders (`Left`/`Right`/`A`/`D`).
  - **`BoardScreen`**: Consolidated `handleKeyboardInput()` enforcing strict modal priority: Pause/Settings/Leave modals -> Golden Die Picker (`1–6`, arrows) -> Inventory (`1–4`, `Up`/`Down`, `Tab`/`I`/`Esc`/`Backspace` to close without leaking rolls) -> Event/Barter/Item Swap choices (`1–9`, `Up`/`Down`/`W`/`S`, `Esc`/`Backspace` cancel), D20 `TAP TO REVEAL` (`Space`/`Enter`), Serpent Strike, `CONTINUE` -> Board idle HUD (`Space`/`Enter`/`R` Roll, `Q`/`E` Skill, `Tab`/`I` Inventory, `Esc`/`P` Pause).
  - **`BattleScreen`**: Consolidated `handleBattleKeyboardInput()` supporting Pause/Settings/Forfeit modals, Boss intro/epilogue dialogue cards (`Backspace` skips intro), post-battle `continueBtn` (`Space`/`Enter`/`1`), Item Popup (`1–4`, `Up`/`Down`, `Esc`/`Backspace`/`I`/`Tab`), and `PLAYER_TURN` actions (`1–5`, `Left`/`Right`/`A`/`D`/`Tab` + `Enter`/`Space`, `Q` Skill, `I` Item).
  - **`EndingStoryScreen` & `GameSummaryScreen`**: Added `A`/`D`/`Left`/`Right`/`Space`/`Enter`/`Backspace`/`Esc` slide controls and full `GameSummaryScreen` navigation (`1–4`, `V` Toggle Summary, `R` Replay Epilogue, `Esc`/`Backspace` Main Menu).

---

### 3. Epilogue Pacing, Slide Separation & `music_act5` Defeat Music ([AudioManager.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/audio/AudioManager.java), [EndingStoryScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/EndingStoryScreen.java), [BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java), [BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java))
- **Dedicated Looping Epilogue Soundtracks**:
  - Added `MusicTrack.EPILOGUE_VICTORY` (`music_act4.mp3` fallback) and `MusicTrack.EPILOGUE_REST` prioritizing `"music_act5"` (`assets/audio/music/music_act5.mp3`), looping cleanly in `EndingStoryScreen.show()`.
  - Updated `BattleScreen.startDawnWardenEpilogue(false)` to crossfade into `EPILOGUE_REST` (`music_act5.mp3`) immediately upon boss defeat dialogue, continuing seamlessly into `EndingStoryScreen`.
  - Routed solo and full-party defeats (`HARVESTED_BY_SHEPHERD`, `FALLEN_ON_TRAIL`) in `BattleScreen` and `BoardScreen` through `EndingStoryScreen` before `GameSummaryScreen` so every defeat path experiences the epilogue accompanied by `music_act5`.
- **Clean Slide Structure & Hidden Counts**:
  - Removed slide counters from `chapterLabel` and hid `indicatorLabel` (`SLIDE X OF Y`).
  - Separated Solo vs. Pass-and-Play storytelling on Slide 4 (Solo Victory ends on solo Morning Rebirth rather than group fellowship art; Solo Defeat concludes on personal hero slumber; Pass-and-Play showcases all companion names on the Fellowship sanctuary/slumber slide).

---

### 4. Typography & Missing Glyph Fixes ([BattleSystem.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/systems/BattleSystem.java), [BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java))
- Removed unsupported emoji characters (`🛡️` in `BattleSystem` guard/riposte combat logs and `🎒` in Raccoon floating rations text) that rendered as `□□□` boxes in the Fredoka bitmap fonts, and verified zero remaining non-ASCII glyphs in runtime UI strings.

---

## Verification
Ran `.\gradlew.bat compileJava :lwjgl3:compileJava testBoard testInventory testBugFixes` (exited with code `0`):
- **`compileJava` & `:lwjgl3:compileJava`**: Clean compilation across `core` and `lwjgl3` modules.
- **`testBoard`**: 1,000 two-pass board generation simulations passed with zero invariant violations.
- **`testInventory`**: All 23 inventory, merchant trade, barter, curse cleansing, and in-battle item tests (`Tests 1–23`) passed.
- **`testBugFixes`**: All 11 board, pursuer, snake combat, Dawn Warden Phase 2, and multiplayer boss defeat hand-off checks (`Tests 1–11`) passed.
