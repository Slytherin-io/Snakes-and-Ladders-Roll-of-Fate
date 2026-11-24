# 📜 Devlog 27: Audio Architecture, Universal Tactile UI Feedback & Custom Music Engine

* **Date:** November 24, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Audio Engineering, Tactile Sound Feedback & Dynamic Music Integration
* **Branch:** `feat/board-adventure-screen`

---

## 🎯 Objectives
- **Decommission Synthetic Music & Transition to Custom Audio Engine**:
  - Completely remove the previous low-quality synthetic 8-bit tracks (`music_act3.ogg`, `music_battle.ogg`, `music_defeat.ogg`, `music_menu.ogg`, `music_victory.ogg`, and `music_act2.ogg`).
  - Transition the game's audio architecture to a user-customizable system where players can place their own music files into `assets/audio/music/`.
- **Multi-Format & Resilient Path Resolution**:
  - Upgrade [AudioManager.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/audio/AudioManager.java) to dynamically resolve `.ogg` (native Vorbis), `.mp3`, and `.wav` formats.
  - Implement intuitive aliases (`main_menu`, `music_menu`, `menu`, `mainmenu`, etc.) and multi-directory search paths (`audio/music/`, `assets/audio/music/`) so tracks play regardless of whether the game is launched from Gradle (`workingDir = assets`) or an IDE project root.
  - Ensure zero crashes or log-spam when tracks are missing, falling back to clean silence with all sound effects remaining active.
- **Universal Tactile UI Feedback (`sfx_click.ogg`)**:
  - Audit and wire crisp audio clicks to every interactive button across all screens, specifically narrative event choices, HUD dialogs, inventory actions, combat controls, and pause/settings menus.
- **Complete Combat Audio Feedback & Battle Music Streaming**:
  - Synchronize sound effects for physical sword strikes, guard deflections, monster impacts, player damage, victory fanfares, and battle music.
- **Settings GUI Visual Polish & Volume Persistence**:
  - Fix the inverted icon display in the Settings menu (swapping misplaced speaker and musical note icons).
  - Bind Music and SFX volume sliders to persistent player preferences.
- **Player Custom Theme Integration & Streaming Diagnostics**:
  - Resolve the issue where an uploaded custom Main Menu theme ("Funny Bit" upbeat chiptune) failed to play in an active game session.
  - Convert and optimize the audio file into native libGDX Ogg Vorbis while maintaining backward-compatible MP3 fallbacks, and verify playback using an automated OpenAL audio probe.

---

## 🛠️ Work Completed

### 1. Legacy Audio Decommissioning & Audio Engine Architecture
- **Clean Asset Wipe**:
  - Safely terminated background game processes holding active file descriptors and erased all obsolete generated music tracks from [assets/audio/music/](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/audio/music).
- **Multi-Format Discovery & Directory-Agnostic Resolver**:
  - Extended `AudioManager.java` with a robust multi-pass track resolver:
    ```java
    private static final String[] SUPPORTED_MUSIC_EXTENSIONS = { ".ogg", ".mp3", ".wav" };
    private static final String[] MUSIC_SEARCH_DIRS = {
        "audio/music/",
        "assets/audio/music/",
        "music/",
        "assets/music/"
    };
    ```
  - Prioritizes native Ogg Vorbis (`.ogg`) for zero-latency looping and lowest CPU overhead, followed by `.mp3` (via JLayer) and uncompressed `.wav`.
- **Intuitive Track Aliasing System**:
  - Redefined `MusicTrack` aliases to accommodate natural naming patterns:
    - **`MENU`**: `music_menu`, `menu`, `main_menu`, `music_main_menu`, `mainmenu`, `music_mainmenu`
    - **`ACT1`**: `music_act1`, `act1`, `act_1`, `music_act_1`
    - **`ACT2`**: `music_act2`, `act2`, `act_2`, `music_act_2`
    - **`ACT3`**: `music_act3`, `act3`, `act_3`, `music_act_3`
    - **`BATTLE`**: `music_battle`, `battle`, `combat`, `music_combat`
    - **`VICTORY`**: `music_victory`, `victory`, `win`, `music_win`
    - **`DEFEAT`**: `music_defeat`, `defeat`, `gameover`, `music_gameover`, `loss`
- **Graceful Silence Fallback**:
  - When `resolveMusicFile(track)` returns `null`, the audio manager cleanly stops any currently playing track and resets the audio channel without throwing exceptions, allowing players to adventure in peaceful silence if desired.

---

### 2. Universal Tactile UI Audio Feedback (`sfx_click.ogg`)
Audited all interactive scenes and wired `game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK)` to every user-facing button:

1. **Board Adventure Screen & Event Modals** ([BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java)):
   - **Narrative Event Choices**: Attached to all dynamically rendered choice options in `showEventChoiceView()` (e.g., Altar of the Tempest: *"Drink the bubbling volcanic mineral water"*, *"Pray for scorching restorative warmth"*, *"Walk away peacefully"*).
   - **Event Outcome Dismissal**: Wired to the **CONTINUE** button in `showOutcomeView()`.
   - **Interactive Serpent Strike**: Wired to the **STRIKE (ROLL D20)** combat button in `showEventCombatView()`.
   - **Merchant Barter Relic Picker**: Wired to all inventory item selection rows and the **CANCEL & RETURN** button in `showBarterPickerView()`.
   - **Full Inventory Item Swap Prompt**: Wired to all item replacement slots and the **LEAVE BEHIND** button in `showItemSwapPrompt()`.
   - **HUD Controls**:
     - Satchel **INVENTORY** toggle button (bottom-left).
     - **`|| PAUSE`** menu button (top-right).
     - Central **D6 ROLL** button (plays click alongside organic dice tumble sound).
   - **Inventory Management Modal**:
     - **+ LOOT** action button and **CLOSE** button.
     - Per-slot **USE** consumable elixir and equipment buttons.
   - **Pause, Settings & Confirmation Modals**:
     - Pause menu **RESUME**, **SETTINGS**, and **LEAVE GAME** buttons.
     - Settings **BACK** button and Volume slider adjustment ticks.
     - Leave game confirmation **STAY** and **LEAVE** buttons.

2. **Turn-Based Combat Screen** ([BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java)):
   - **Flee / Escape**: Escape button now confirms with tactile click.
   - **Item Bag & Management**: Item drawer toggle button, item slot **USE** button, and popup **CLOSE** button.
   - **Post-Battle Outcome**: Victory / Defeat **CONTINUE** button leading back to the board or chronicle summary.

3. **Game Chronicle Summary & Main Menu Screens** ([GameSummaryScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/GameSummaryScreen.java) & [MainMenuScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/MainMenuScreen.java)):
   - **PLAY AGAIN** and **MAIN MENU** navigation buttons.
   - Mode selection (Single Player / Pass & Play) and Player Count steppers.
   - Character selection carousel navigation arrows and hero confirmation buttons.

---

### 3. Turn-Based Battle Audio & Combat FX Synchronization
Enhanced [BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java) with complete combat sound integration:
- **Melee Attack & Impact Sync**: Initiating an attack triggers `AudioManager.SoundEffect.ATTACK_SLASH`. When the hero's lunge reaches midpoint ($progress \ge 0.48$), physical blade contact triggers `AudioManager.SoundEffect.MONSTER_HIT`.
- **Defensive Guard**: Activating guard triggers `AudioManager.SoundEffect.DEFEND_GUARD` alongside golden shield particle pulses and floating `+5 DEF GUARD` combat text.
- **Damage & Retaliation**: When the enemy serpent counter-attacks, physical impact triggers `AudioManager.SoundEffect.PLAYER_HURT`.
- **Combat Resolution**:
  - Slain enemies trigger `AudioManager.SoundEffect.LEVEL_UP` triumph fanfare paired with celebratory particle bursts.
  - Player defeat triggers `AudioManager.SoundEffect.PLAYER_HURT` with defeat chronicle routing.
- **Dynamic Battle BGM**: Automatically activates `MusicTrack.BATTLE` when entering `BattleScreen.show()`.

---

### 4. Settings GUI Visual Polish & Volume Persistence
Resolved audio settings discrepancies in [MainMenuScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/MainMenuScreen.java):
- **Icon Inversion Bug Fix**: Fixed a visual bug in the parchment settings dialog where the speaker icon was drawn next to the Music slider and the musical note icon was next to the SFX slider. Assigned `musicIcon` to Music and `soundIcon` to SFX.
- **Bi-Directional Volume Persistence**: Sliders now dynamically initialize from `game.getAudioManager().getMusicVolume()` and `getSfxVolume()`. Adjusting slider handles calls `setMusicVolume()` / `setSfxVolume()`, immediately flushing changes to `Preferences` (`snakes_ladders_audio_prefs`).

---

### 5. Screen Music Lifecycle Guards & Procedural Sound Tooling
- **Loading & Summary Screen Music Lifecycle**:
  - In [LoadingScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/LoadingScreen.java), added `audioManager.stopMusic()` on screen initialization to ensure loading transitions into the board are clean without music bleeding.
  - In [GameSummaryScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/GameSummaryScreen.java), automatically streams `MusicTrack.VICTORY` or `MusicTrack.DEFEAT` non-looping upon chronicle summary entry.
- **Procedural Sound Generation Utilities**:
  - Retained offline synthesis scripts in [scripts/](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/scripts) (`audio_core.js`, `generate_audio.js`, `generate_audio_v2.js`) used for procedural generation of the 18 dedicated sound effects in `assets/audio/sfx/`.

---

### 6. Custom Music Integration & Playback Diagnostics
- **Root Cause Analysis of Initial Playback Failure**:
  - The player added the custom theme under `main_menu.mp3`. Previously, `MusicTrack.MENU` only registered `music_menu` and `menu`, causing the path resolver to miss the file.
  - The game instance was already running in the background when the file was placed; libGDX does not poll the filesystem for changes during an active scene.
  - MP3 files containing extended ID3v2.4 / XMP metadata tags can incur decoder stalls on desktop JLayer implementations.
- **Conversion to Native Vorbis**:
  - Leveraged `ffmpeg` to transcode the user's uploaded upbeat theme (*"Funny Bit"*) from MP3 into high-fidelity Ogg Vorbis at 44.1 kHz stereo:
    ```powershell
    ffmpeg -i "assets\audio\music\menu.mp3" -c:a libvorbis -q:a 5 "assets\audio\music\menu.ogg" -y
    ```
  - Replicated the file across all canonical naming aliases (`main_menu.ogg`, `music_menu.ogg`, `menu.ogg`, and corresponding `.mp3` fallbacks).
- **OpenAL Audio Probe Verification**:
  - Constructed an automated test probe (`AudioProbe.java`) in `:lwjgl3` that initialized LWJGL3's OpenAL backend and verified:
    ```
    === STARTING AUDIO PROBE ===
    Working directory: ...\Snakes-and-Ladders-Roll-of-Fate\assets
    Resolved MENU path: audio/music/music_menu.ogg
    File exists check: true
    Music created successfully: com.badlogic.gdx.backends.lwjgl3.audio.Ogg$Music@64485a47
    music.isPlaying(): true
    === AUDIO PROBE PASSED ===
    ```

---

## 🔍 Verification & Testing

1. **Board Generation Test Suite**:
   - Executed `.\gradlew.bat testBoard` (with `BypassSandbox: true`):
     - **1,000 / 1,000 simulations passed with 0 invariant violations**.
     - Milestone safety (Tiles 1, 30, 70, 100), anti-clustering rules ($\ge 2$ safe tiles between specials), and minimum jump distances ($\ge 6$ tiles) strictly verified.
2. **Inventory & Combat Integration Test Suite**:
   - Executed `.\gradlew.bat testInventory`:
     - **20 / 20 integration tests passed**, confirming equipment durability degradation, consumables, satchel capacity limits (4/4), interactive barter swaps, and combat mortality safeguards.
3. **Compilation & Packaging**:
   - Executed `.\gradlew.bat compileJava :lwjgl3:compileJava` with exit code 0.
4. **Interactive Verification**:
   - Verified that every button click triggers tactile auditory feedback.
   - Verified that combat swings, guard pulses, monster hits, and player hurts trigger synced audio.
   - Verified that Settings volume sliders persist cleanly and icons accurately reflect Music and SFX.
   - Verified that launching the desktop game starts the custom upbeat chiptune theme seamlessly upon Title Screen initialization.
