# Devlog 36: Pale Shepherd Gentle Redesign, Midair Interception Kinematics & Board Dialogue

* **Date:** November 27, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Atmospheric Narrative, Gentle Pursuer Redesign & Board Midair Kinematics
* **Branch:** `feat/board-adventure-screen`

---

## Objectives
- **Redesign The Pale Shepherd as a Gentle, Sorrowful Pursuer**: Transform the entity from an aggressive raid boss / executioner into a melancholic, gentle wanderer guiding and testing weary travelers on the mountain trail.
- **Purge Spoilers & Aggressive Titles**: Remove aggressive titles and spoiler descriptions such as "Harvester of Souls" and "MORTAL CLASH: THE PALE SHEPHERD!". Standardize banner and naming to simply "THE PALE SHEPHERD" with behavior designated as "A Quiet Watcher".
- **Board Screen Midair Interception Kinematics**: When a snake slide sends the player toward a tile occupied by or behind The Pale Shepherd, the player visually begins sliding along the arc toward the snake's designated position, but is smoothly halted midair above the board tiles.
- **Midair Hover & Mist Cushioning**: Suspend the player midair with a gentle sinusoidal hover bob and an ethereal swirling dark/crimson mist cushion beneath their paws/feet.
- **Pre-Battle Board Dialogue Scroll**: Render an atmospheric dusk parchment dialogue card directly over the board tiles with a top-right `[SKIP INTRO]` button and a 3-beat gentle dialogue progression.
- **Clean Skip Intro Transition (No Stat Buff Traps)**: Ensure `[SKIP INTRO]` immediately closes the dialogue, completes a gentle descent, and enters battle without skipping buffs or trapping the player in resolve dilemmas.
- **Gentle Combat Balancing & Tuned Scramble Toll**: Rebalance stats from 80 HP / 12 ATK to 45 HP / 5 ATK / 2 DEF (dealing ~3-5 damage, mitigated by guard to ~2 damage). Reduce the Turn 3+ scramble penalty from -20 HP to -5 HP.
- **Facing Kinematics & Radial Aura**: Maintain horizontal sprite inversion facing left toward the challenger with subtle circular radial void aura.

---

## Work Completed

### 1. Midair Interception Kinematics & Descent (`BoardScreen.java`)
- **Expanded Locomotion Phases**:
  - Added `SpecialMovePhase.INTERCEPTED_MIDAIR` and `SpecialMovePhase.INTERCEPTED_DESCENT` to the special locomotion finite state machine.
- **Flight Interception Detection**:
  - In `updateSpecialMove()`, during `SpecialMovePhase.IN_FLIGHT` on snake slides (`!isLadderMove`):
    - When `isSlideInterceptedByReaper` is true, the flight calculates the exact progress fraction where the slide passes the shepherd's tile (`MathUtils.clamp((startTile - shepherdPos) / (float)(startTile - destTile), 0.30f, 0.70f)`).
    - Upon reaching this point, the slide immediately halts mid-flight.
    - Transitions to `SpecialMovePhase.INTERCEPTED_MIDAIR`, resets phase timer, triggers `REAPER_APPEAR` SFX at subtle volume, and displays the gentle board dialogue scroll.
- **Camera Tracking & Elevation Alignment**:
  - In `updateCameraTracking()`, `INTERCEPTED_MIDAIR` and `INTERCEPTED_DESCENT` smoothly track `specialMoveCurrentX` and `specialMoveCurrentY`, keeping the hovering character and shepherd centered.
- **Gentle Landing Descent**:
  - When dialogue concludes or the player clicks `[SKIP INTRO]`:
    - Transitions to `SpecialMovePhase.INTERCEPTED_DESCENT` over 0.45 seconds using `Interpolation.sineOut`.
    - Lerps smoothly from the midair coordinates down to `(getTileCenterX(shepherdPos), getCharacterDrawY(shepherdPos))`.
    - On touchdown, sets the player position to `shepherdPos`, concludes special move, and launches `triggerPaleShepherdEncounter(player)`.

### 2. Character Midair Hover & Mist Rendering (`BoardScreen.java`)
- In `renderCharacters()`:
  - During `INTERCEPTED_MIDAIR`, the character's vertical draw position oscillates with a gentle sine bob: `specialMoveCurrentY + sin(stateTime * 2.5f) * 6f`.
  - The character uses idle keyframe 0 (`charTex`).
  - Directly beneath the character's feet, a soft dual-layer mist cushion is drawn using `softShadowTex`:
    - Deep dusk foundation: `Color(0.20f, 0.10f, 0.28f, 0.50f + 0.15f * pulse)` at 90x22 px.
    - Subtle crimson/amethyst core: `Color(0.60f, 0.15f, 0.35f, 0.25f + 0.10f * pulse)` at 70x16 px.

### 3. Board Dialogue Scroll UI (`BoardScreen.java`)
- Constructed `buildShepherdDialogueUI()` on `hudStage`:
  - Positioned along the lower middle screen (`bottom().padBottom(36)`), width 960 px, height 240 px.
  - Container card styled with dusk obsidian parchment: `UIUtils.createRoundedDrawable(960, 240, 18, Color.valueOf("#1a1024f2"), Color.valueOf("#72528a"), 2)`.
  - Header Row:
    - Title: `THE PALE SHEPHERD` in warm antique ivory (`#e8d5b5`).
    - Subtitle: `A Soft Whisper In The Mist` in soft ethereal lavender (`#b39ddb`).
    - Top-right action: `[SKIP INTRO]` button.
  - Body Label: Wrapped center text using `eventDescFont`.
  - Multi-Beat Gentle Narrative:
    - **Beat 1**:
      *"Softly now, little wanderer. You were falling so fast."*
      Button: `[CONTINUE (1/3)]` (`#4a3055` / `#8f629f`).
    - **Beat 2**:
      *"The stones on this mountain are sharp, and your paws are bruised. Do you stumble because you are weary... or because you are rushing toward the end?"*
      Button: `[CONTINUE (2/3)]` (`#4a3055` / `#8f629f`).
    - **Beat 3**:
      *"If your heart still yearns to reach the summit, show me the warmth left in your spirit."*
      Button: `[STAND UP & FACE HIM]` (`#7a4430` / `#c88258`).
  - Clicking `[SKIP INTRO]` or `[STAND UP & FACE HIM]` immediately triggers `completeShepherdDialogue()`.

### 4. Gentle Pale Shepherd Stat Rebalance & Battle Overhaul (`BoardScreen.java`, `BattleScreen.java`, `BattleSystem.java`)
- **Spoiler Removal**:
  - Behavior set to `"A Quiet Watcher"` (purged `"Harvester of Souls"`).
  - Banner title set to `"THE PALE SHEPHERD"` with soft dusk-violet backdrop (`#3b263b`) replacing `"MORTAL CLASH: THE PALE SHEPHERD!"`.
- **Gentle Combat Parameters**:
  - Health: 45 HP (reduced from 80 HP).
  - Attack: 5 (reduced from 12). Deals ~3-5 damage on normal hit, ~1-2 damage when guarded.
  - Defense: 2 (reduced from 4).
  - Initial Contemplation Log: `"The Pale Shepherd raises his crook in quiet contemplation."`
- **Decoupled Battle Intro**:
  - `isIntroDialogueActive = isDawnWardenBattle()`. Since the encounter dialogue occurred on the board screen, `BattleScreen` immediately presents interactive combat buttons (`ATTACK`, `DEFEND`, `ITEM`, `LOCKED`).
- **Tuned Scramble Toll (-5 HP)**:
  - Turn 1: `LOCKED (2)`
  - Turn 2: `LOCKED (1)`
  - Turn 3+: `SCRAMBLE (-5 HP)` in terracotta leather with floating alert `"MIST CLEARS: SCRAMBLE UNLOCKED!"`.
  - Escaping inflicts a light parting wound of -5 HP (reduced from -20 HP) and propels the hero 3 tiles ahead.
- **Atmospheric End Modals**:
  - Victory: `"The Pale Shepherd yields with a soft nod, dissolving into gentle mist."` | Title: `QUIET TRIUMPH`.
  - Escaped: `"You slipped past The Pale Shepherd! (-5 HP Parting Wound)"` | Title: `HASTY ESCAPE`.
  - Defeat: `"REST, WEARY WANDERER"` | Title: `QUIET REST` | Subtitle: `"THE PALE SHEPHERD GENTLY CARRIES YOUR SPIRIT"`.

---

## Verification & Screenshots
- **Automated Verification**:
  - `./gradlew compileJava :lwjgl3:compileJava testBugFixes testBoard testInventory testClassSkills` executed with 100% passing tests and zero warnings/errors.
  - `BoardAdventureBugFixTest.java:Test 9` updated to verify the refined -5 HP escape penalty.
  - 1,000 board generator simulations passed all invariants.
- **Visual Capture**:
  - `shepherd_board_dialogue_beat1.png`: Verified the board screen with the cat hero suspended midair cushioned by mist, The Pale Shepherd hovering nearby, and the gentle dusk dialogue scroll showing Beat 1 with `[SKIP INTRO]` and `[CONTINUE (1/3)]`.
  - `shepherd_board_dialogue_beat3.png`: Verified Beat 3 with `[STAND UP & FACE HIM]`.
  - `shepherd_gentle_battle_turn1.png`: Verified the gentle `BattleScreen` interface showing `"THE PALE SHEPHERD"` top banner, initial log `"The Pale Shepherd raises his crook in quiet contemplation."`, rebalanced 45 HP, flipped sprite facing left, and `LOCKED (2)` turn gate.
  - `shepherd_gentle_battle_scramble.png`: Verified Turn 3 with floating alert `"MIST CLEARS: SCRAMBLE UNLOCKED!"` and the updated `SCRAMBLE (-5 HP)` button.

