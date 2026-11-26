# Devlog 34: The Pale Shepherd Mortal Duel, Snake Slaying Stat Mastery & Tail Slide Acrobatics Overhaul

* **Date:** November 26, 2026
* **Author:** Dorothy Anne R. Domanico (Design Lead)
* **Sprint / Milestone:** Week 6 – Combat Stakes, Pursuer High-Noon & Serpent Risk/Reward Overhaul
* **Branch:** `feat/board-adventure-screen`

---

## Objectives
- **Mortal Duel with The Pale Shepherd**: Replace the passive text-dialogue choices when intercepted by the grim pursuer with an adrenaline-pumping, direct mortal combat encounter in `BattleScreen`. When Death catches a hero or a hero lands on Death, battle initiates immediately. Win, escape, or fall.
- **Pale Shepherd Dynamic Pursuer Awakening**: Break predictable predictability by randomizing The Pale Shepherd's dormant sleep turns across a range of rounds (Rounds 4 to 7) upon campaign start, injecting tension from the earliest turns of Act I.
- **Snake Tile Risk/Reward Overhaul**:
  - **Acrobatics D20 Tail Slide**: Remove passive, consequence-free tail sliding with free EXP. Sliding down the tail is now a desperate evasive gamble: players earn zero free EXP and suffer tumble damage modulated by an Acrobatics D20 roll (Natural 20 = 0 DMG; 10–19 = 10 DMG; 1–9 = 20 DMG).
  - **Serpent Slayer Stat Mastery**: Slaying a snake in mortal combat permanently empowers the hero (+1 Permanent ATK, +5 Permanent Max HP, +5 Current HP) and yields Act-exclusive slayer relics (Venom Coating Vial, Serpent Scale Buckler, Shed Serpent Skin, Crag Carapace Plate, Apex Serpent Fang, Heart of Magma).
  - **Serpent's Bane & Adrenaline Surge**: Vanquishing a serpent unleashes fierce adrenaline: if active, The Pale Shepherd is repelled 4 tiles backwards and paused for 1 contemplation round, and the victorious hero immediately gains a free bonus roll (`TurnState.WAITING_FOR_ROLL`).
- **BattleScreen Character & Boss Sprite Harmonization**: Load `Assets.CHAR_PALE_SHEPHERD` with row-based idle and scythe-cleaving animations, crimson atmospheric lighting, and floating hover oscillation ($\pm 8\text{px}$) during combat.

---

## Work Completed

### 1. The Pale Shepherd Mortal Duel System (`BoardScreen.java`, `BattleScreen.java`)
- **Direct Combat Trigger**:
  - Removed the old `buildShepherdChoiceCard` dialogue table (`Struggle for Life`, `Soothe with Memory`, `Trial of Will`, `Yield to the Scythe`).
  - In `BoardScreen.triggerPaleShepherdEncounter(Player player)`, the pursuer now constructs a high-threat boss encounter:
    - **Enemy Type**: `"The Pale Shepherd"`
    - **Health**: 80 HP
    - **Attack**: 12 ATK
    - **Defense**: 4 DEF
    - **Behavior**: `"Harvester of Souls"`
    - **Trophy Drop**: `"Shattered Scythe Shard"`
  - Launches immediately into `BattleScreen(game, player, shepherd)`.
- **BattleScreen Visuals & Combat FX**:
  - Top banner displays `"MORTAL CLASH: THE PALE SHEPHERD!"` with blood-crimson backing (`#540d18`).
  - Loaded `Assets.CHAR_PALE_SHEPHERD` ($1024\times 1024$ sheet split into $256\times 256$ frames): Row 0 looped for floating idle; Row 1 for scythe reap attacks.
  - Rendered with spectral pale-crimson tinting and smooth sinusoidal floating hover oscillation.
  - Enabled the `[ESCAPE]` tactical option against Death (distinguishing from the climactic Tile 100 Dawn Warden trial where escape is locked).
- **Post-Battle Resolution & Routing**:
  - **Victory**: Banishes The Pale Shepherd 15 tiles backwards, grants 3 rounds of contemplation pause, awards +50 EXP, and returns the hero to the board in `WAITING_FOR_ROLL` state to roll again immediately.
  - **Escape**: Hero takes a -20 HP parting blow from the scythe, scrambles +3 tiles ahead, pauses the pursuer for 1 round, and continues their climb.
  - **Defeat**: Permanent death. Handled gracefully by the run tracker and expedition summary screen.

### 2. Randomized Pursuer Awakening (`RollOfFateGame.java`)
- Updated `shepherdDormantTurns` across default constructor, `startGame()`, and `startGameWithClasses()`:
  $$\text{shepherdDormantTurns} = \text{MathUtils.random}(4, 7)$$
- Players can no longer rely on a rigid Round 7 timer; the reaper may awaken as early as Round 4 or bide its time until Round 7.

### 3. Snake Tile Slayer Mastery & Tail Slide Acrobatics (`SnakeTile.java`, `EventRepository.java`, `BoardScreen.java`)
- **Acrobatics D20 Tail Slide**:
  - Updated narrative choice in `EventRepository.java`: `"[Slide Down the Tail] Plunge down the serpent's tail (Acrobatics D20: 20=0 DMG | 10-19=10 DMG | <10=20 DMG)"`.
  - Removed the old passive free EXP reward on slide down.
  - In `BoardScreen.java` (`handleChoiceSelected`), sliding down rolls an Acrobatics D20 check:
    - **Natural 20**: Flawless landing! Avoids all tumble damage (0 DMG).
    - **10–19**: Controlled slide down scales; takes standard tumble damage (-10 HP).
    - **1–9**: Rough crash landing on sharp spines; takes severe crash damage (-20 HP).
    - Clamped cleanly with `Math.max(1, player.getHealth() - tumbleDmg)` to preserve agency while heavily punishing careless retreats.
- **Slayer Stat Mastery & Act-Exclusive Trophies**:
  - In `SnakeTile.java` (`resolveOutcome`), slaying a serpent grants permanent progression:
    - Permanent +1 Attack (`player.setAttack(player.getAttack() + 1)`)
    - Permanent +5 Max Health and +5 Current Health
    - Exclusive Act-specific relics:
      - **Act I**: *Venom Coating Vial* (+4 ATK) or *Serpent Scale Buckler* (+2 DEF)
      - **Act II**: *Shed Serpent Skin* (+30 Max HP) or *Crag Carapace Plate* (+3 DEF)
      - **Act III**: *Apex Serpent Fang* (+6 ATK) or *Heart of Magma* (+60 HP)
- **Serpent's Bane & Adrenaline Surge**:
  - Slaying a serpent in combat repels active Pale Shepherd pursuers 4 tiles backwards and pauses pursuit for 1 round.
  - Hero immediately gains an Adrenaline Surge bonus roll (`turnState = TurnState.WAITING_FOR_ROLL`), letting them roll the dice immediately on the board.

---

## Verification & Testing

### 1. Automated Test Suite (`BoardAdventureBugFixTest.java`)
Executed all 9 automated verification tests in the project test suite with zero failures:
- **Test 1**: Bug A destination trigger on ladder/snake/landmarks.
- **Test 2**: Bug B pursuer overshoot clamping to player tile.
- **Test 3**: Bug B snake-slide trailing repositioning to $\max(1, \text{pos}-3)$ with 1 pause round.
- **Test 4**: Bug B pushback and contemplation pauses eliminating the D1 trapping loop.
- **Test 5**: Shepherd random dormancy range verification across 200 runs (strictly within $[4, 7]$).
- **Test 6**: Acrobatics D20 tumble damage tier verification ($20 \rightarrow 0$, $10\text{--}19 \rightarrow 10$, $<10 \rightarrow 20$).
- **Test 7**: Snake slaying permanent stat mastery (+1 ATK, +5 Max HP) and Act I-III trophy generation.
- **Test 8**: Serpent's Bane reaper 4-tile repel and 1-round pause verification.
- **Test 9**: Pale Shepherd mortal duel victory (+50 EXP, 15-tile banish, 3 pause rounds) and escape (-20 HP, +3 tiles scramble).

### 2. Comprehensive Test Pass
- `gradlew.bat testBugFixes`: Passed (9/9 tests).
- `gradlew.bat testBoard`: Passed (1,000 board generations simulated with 0 invariant violations).
- `gradlew.bat testInventory`: Passed (22 integration tests passed).
- `gradlew.bat compileJava :lwjgl3:compileJava`: Built cleanly without errors.
