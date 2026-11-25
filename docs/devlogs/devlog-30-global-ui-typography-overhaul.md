# Devlog 30: Global UI Typography Overhaul (Fredoka Pairing)

* **Date:** November 25, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – UI Legibility & Typographic Consistency Pass
* **Branch:** `feat/board-adventure-screen`

---

## Objectives
- Resolve text legibility issues caused by `Sniglet-ExtraBold.ttf` closing up internal letter counters (`O`, `A`, `E`, `D`, `P`, `R`) in uppercase badges, mode cards, and small labels.
- Evaluate side-by-side FreeType font pairings (`Fredoka-Bold` + `Fredoka-SemiBold`, `Fredoka-Bold` + `Quicksand`, and `Sniglet-Regular`) against the existing parchment and pill UI palette.
- Apply **Option 1 (`Fredoka-Bold` + `Fredoka-SemiBold`)** consistently across every graphical screen in the application while preserving existing sizing, colors, and layout alignment.

---

## Work Completed

### 1. Visual Font Evaluation & Selection
- Compared the existing `Sniglet-ExtraBold` modal typography against three alternatives available in `assets/ui/` using identical parchment (`#f5dfad`), mahogany pill (`#854b25`), and card (`#cf9b63`) styling.
- Selected **Option 1 (`Fredoka-Bold.ttf` + `Fredoka-SemiBold.ttf`)** for its open, rounded letterforms that maintain the game's cozy fantasy tone while remaining sharp at small and medium sizes (14px–32px).

### 2. Unified Fredoka Font Migration Across All 5 Screens
Replaced all `Sniglet-ExtraBold.ttf` and `Sniglet-Regular.ttf` font instances across the GUI layer (70 font lines updated):

- **[MainMenuScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/MainMenuScreen.java#L129-L142):**
  - Assigned `ui/Fredoka-Bold.ttf` to `badgeFont`, `textFont`, `numFont`, `modeTitleFont`, and `countNumFont`.
  - Assigned `ui/Fredoka-SemiBold.ttf` to `modeSubtitleFont` (`SOLO GAME`, `2 TO 4 PLAYERS`), `instructionFont`, `statFont`, and `perkFont`.
- **[BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java#L281-L321):**
  - Assigned `ui/Fredoka-Bold.ttf` to top ribbons, Act banners, pause/event badges, tile numbers/badges, slider headers, roll button header, modal titles, and D20 outcome badges.
  - Assigned `ui/Fredoka-SemiBold.ttf` to HUD stats, player tags, mini-map labels, roll subtext, pause/event body copy, choice buttons, and resolution outcomes.
- **[BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java#L317-L330):**
  - Assigned `ui/Fredoka-Bold.ttf` to combat titles, top banners, damage/crit numbers, victory/defeat modal headers, and reward titles.
  - Assigned `ui/Fredoka-SemiBold.ttf` to HP labels, combat logs, player tags, mini labels, and modal status/narrative text.
- **[GameSummaryScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/GameSummaryScreen.java#L118-L137):**
  - Assigned `ui/Fredoka-Bold.ttf` to summary titles, section headers, hero names, buttons, stat values, chip values, and ledger values.
  - Assigned `ui/Fredoka-SemiBold.ttf` to subtitles, accolades, body copy, chip titles, and stat/ledger keys.
- **[LoadingScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/LoadingScreen.java#L66-L68):**
  - Assigned `ui/Fredoka-Bold.ttf` to `titleFont` and `ui/Fredoka-SemiBold.ttf` to `tipFont`.

---

## Challenges & Solutions

- **Uppercase Stroke Density on Small UI Badges:** `Sniglet-ExtraBold` has heavy rounded strokes with tight internal negative space, causing short all-caps labels (`SELECT GAME MODE`, `SOLO GAME`, `2 TO 4 PLAYERS`, `TILE 14`) to bleed together visually. Migrating headings to `Fredoka-Bold` and secondary/body text to `Fredoka-SemiBold` establishes clear weight hierarchy without changing button dimensions or wrapping bounds.

---

## Verification

- Verified clean Java compilation across both modules via `.\gradlew compileJava :lwjgl3:compileJava` (exited with code `0`).
- Confirmed via codebase search (`git grep -n "Sniglet" core/src/ lwjgl3/src/`) that zero active `Sniglet*.ttf` font loads remain in the UI layer.

---

## Next Steps
- Review the updated typography live in the desktop client (`.\gradlew :lwjgl3:run`), then commit and push the changes on `feat/board-adventure-screen`.
