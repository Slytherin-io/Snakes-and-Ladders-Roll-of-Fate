# Devlog 32: The Dawn Warden Summit Trial & The Pale Shepherd Pursuer

* **Date:** November 26, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Climax Encounter, Pursuer Dynamics & Narrative Harmonization
* **Branch:** `feat/board-adventure-screen`

---

## Objectives
- **Implement Climax Boss Trial at Tile 100**: Replace the immediate, passive victory trigger on Tile 100 with a turn-based boss battle against **The Dawn Warden** (The Solar Lion Chimera), testing the player's gathered relics, vitality, and combat skill before claiming rebirth.
- **Implement The Pale Shepherd Pursuer Mechanic**: Introduce a visible, atmospheric pursuer entity following players along the ascending stones, providing palpable urgency, preventing stalling, and providing strategic tension when snakes drop players down the mountain.
- **Verb-Driven Physical Ambush Dilemmas**: Design intuitive, choice-driven encounters when the Shepherd catches a player (or a player slides into the Shepherd), replacing complex math with tangible actions: struggling for life with vitality, soothing the lantern with an inventory memory item, rolling a D20 trial of will, or yielding to peaceful rest.
- **Dynamic 2-Row Animated Sprites**: Integrate transparent 2-row animated spritesheets for both The Dawn Warden (`assets/img/boss_dawn_warden.png`) and The Pale Shepherd (`assets/img/char_pale_shepherd.png`), featuring idle breathing/lantern pulsing and active attack/glide animations.
- **Full System Verification**: Verify compilation and ensure zero regressions across the two-pass board generator and 22-part inventory/combat integration test suite.

---

## Work Completed

### 1. The Dawn Warden Summit Trial (`BattleScreen.java`, `BoardScreen.java`, `Assets.java`, `BattleSystem.java`)
- **Climax Encounter atop Tile 100**:
  - Reaching or climbing to Tile 100 now routes through `triggerDawnWardenSummitTrial(Player player)`, which instantiates `The Dawn Warden` (`120 HP`, `14 ATK`, `6 DEF`, behavior: `"Celestial Guardian"`, reward: `"Dawn's Blessing (Rebirth)"`) and launches `BattleScreen`.
  - The battle screen recognizes boss battles via `isBossBattle()`, displaying the summit header `"SUMMIT TRIAL: THE DAWN WARDEN!"` with a regal amber banner (`#b86518`).
  - Escape is disabled during the summit trial.
- **Centered 3-Button Action Layout**:
  - In boss encounters where escape is impossible, `btnEscape` is completely omitted from the layout table rather than merely hidden.
  - The 3 remaining action buttons (`[ ATTACK ]`, `[ DEFEND ]`, `[ ITEM ]`) are placed in `bottomBar.bottom().center().padBottom(30)` with `DEFEND` situated precisely at the horizontal screen center (`X = 960`).
- **Boss Sprite Animation Smoothing & Baseline Anchoring**:
  - Resolved animation jitter caused by inter-frame paw drift (which previously shifted left by 21.7px and dipped 8px on Frame 2).
  - Realigned all 4 frames in [boss_dawn_warden.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/boss_dawn_warden.png) to a shared paw anchor (`X = 160.0`, `Y = 375` on Row 0 and `Y = 710` on Row 1).
  - Disabled artificial sine-wave bobbing (`eBob = 0f` for boss battles) so the majestic chimera stands grounded on its stone pedestal while wings and radiant aura flare smoothly.
  - Tuned frame duration to `0.22f` with `Animation.PlayMode.LOOP`.
- **Refined Encounter Phrasing**:
  - In `BattleSystem.java`, updated battle log initialization so proper nouns starting with `"The "` announce `"The Dawn Warden descends before you to test your spirit!"` rather than `"A wild The Dawn Warden..."`.
- **Victory & Defeat Dialogues & Dynamic Button Sizing**:
  - **Modal Action Button Dynamic Sizing**: Fixed text overflow on `continueBtn` where `"VIEW EXPEDITION SUMMARY"` previously exceeded the fixed 260px button width. Sized cells dynamically (`460px` for `VIEW EXPEDITION SUMMARY`, `340px` for `CLAIM REBIRTH`, `380px` for `VIEW RUN SUMMARY`, `280px` for `CONTINUE`) with `pad(10, 36, 10, 36)` and explicit hierarchy invalidation so text is framed with clean margins.
  - **Triumph**: Defeating the Warden triggers `"GLORIOUS REBIRTH!"` (*"The golden gates swing open to the living world! Vitality Restored: You awaken in the sunlit morning!"*), recording the throne claim in `RunTracker` and advancing to `GameSummaryScreen(game, true, player)`.
  - **Fallen**: Falling to the Warden opens `"PEACEFUL SLEEP"` (*"The Pale Shepherd covers you with a warm blanket of rest. No more pain. No more cold."*), routing to `GameSummaryScreen(game, false, player)`.

### 2. The Pale Shepherd Pursuer Mechanics (`RollOfFateGame.java`, `BoardScreen.java`, `Assets.java`)
- **Pacing & Board Movement**:
  - **Grace Period (Rounds 1–3)**: The Shepherd remains dormant at Tile 0, allowing heroes to gain an initial foothold. On Round 3, a notification rings: *"A distant chime echoes... The Pale Shepherd awakens on Tile 1!"*
  - **Act-Scaled Pursuit Speed**:
    - **Act I (Tiles 1–30)**: Moves **1 tile per round**, providing gentle pressure while players explore early mechanics.
    - **Acts II & III (Tiles 31–100)**: Moves **2 tiles per round**, heightening tension on the treacherous upper cliffs.
- **Round-End Synchronization**:
  - In [RollOfFateGame.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/RollOfFateGame.java), added round tracking (`totalRounds`, `shepherdPauseRounds`, `paleShepherdTile`) that persists cleanly across screen transitions.
  - Advancements occur automatically upon round completion in `TURN_TRANSITION` when all players have finished their turn (or on each turn in single-player).

### 3. Physical, Verb-Driven Ambush Dilemma (`BoardScreen.java`)
- **Catch-Up & Slide Encounter Triggers**:
  - If the Shepherd advances to or past a player's tile, or if a player slides down a snake to or behind the Shepherd, `triggerPaleShepherdEncounter(player)` triggers immediately.
  - Opens the storybook scroll modal with 4 distinct, verb-driven dilemmas:
    1. **[Struggle for Life]**: Spend 20 HP of vital reserve to surge forward +3 tiles, pushing the Shepherd back 3 tiles. Available only when HP > 20.
    2. **[Soothe with Memory]**: Feed 1 item from the satchel into the Shepherd's soul lantern. The blue flame warms peacefully, causing the Shepherd to pause pursuit for 2 rounds.
    3. **[Trial of Will]**: Roll the D20 combat die against destiny. Rolling ≥ 10 repels the Shepherd 4 tiles; rolling < 10 drains 15 HP but allows the player to scramble forward 1 tile.
    4. **[Accept Peaceful Rest]**: Surrender peacefully to the gentle slumber, concluding the run and transitioning to `GameSummaryScreen` with the peaceful resting chronicle.
- **Seamless Flow**:
  - Survived ambushes transition directly back to `TurnState.WAITING_FOR_ROLL`, allowing the player to roll immediately with their newly bought ground.

### 4. Board Visual Integration & Eerie Hovering Presentation (`BoardScreen.java`, `char_pale_shepherd.png`)
- **Custom Crimson Reaper Spritesheet**:
  - Replaced temporary placeholder sprite with user's hooded crimson reaper with golden scythe spritesheet.
  - Processed into a 1024x1024 spritesheet ([assets/img/char_pale_shepherd.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/char_pale_shepherd.png)) comprising 4 rows of 4 frames (256x256 each) using crisp 4x nearest-neighbor scaling and 1:1 square aspect ratio:
    - **Row 0**: Idle hovering stance (4 frames).
    - **Row 1**: Spectral crimson glide trail (4 frames).
    - **Row 2**: Golden scythe slash (4 frames).
    - **Row 3**: Scythe throw / cast / demise (4 frames).
- **Eerie In-World Hovering Animation**:
  - In `renderPaleShepherd()`, the reaper floats above the mountain stone with a continuous vertical sine oscillation: `float hoverOffset = (float) Math.sin(stateTime * 2.8f) * 12f`.
  - Added dynamic stone ground shadow beneath the hovering entity that inversely scales with hover height (`float shadowScale = 1.0f - (hoverOffset / 28f)`), creating an authentic perception of depth and levitation.
  - Layered pulsing spectral crimson mist pools and ambient floating ember glows beneath and around the hovering reaper.
- **Cinematic Camera Hover Awakening**:
  - When the reaper awakens on Tile 1 (round 3), `cameraFocusOverrideTimer` (3.8s) and `cameraFocusTile` (1) take priority in `updateCameraTracking(delta)`.
  - The camera smoothly sweeps down the mountain trail to Tile 1, lingering and hovering directly over the newly awakened crimson reaper.
  - Simultaneously plays `AudioManager.SoundEffect.FOG_REVEAL` and broadcasts the top ribbon alert: `"A CHILLING SHADOW RISES... THE PALE SHEPHERD AWAKENS ON TILE 1!"`.
  - Dice roll input is held until the camera timer finishes, at which point the camera smoothly glides back up the mountain to center on the active player and restores their turn prompt.
- **Mini-Map Monitor Tracking**:
  - Registered `CHAR_PALE_SHEPHERD_HEAD` ([assets/img/char_pale_shepherd_head.png](file:///c:/Users/John%20Calvin/Documents/DevJourn\Snakes-and-Ladders-Roll-of-Fate/assets/img/char_pale_shepherd_head.png)) featuring the hooded crimson reaper head with glowing cyan eyes.
  - When the Shepherd awakens (`paleShepherdTile > 0`), the entity is dynamically displayed on the top-left linear 100-tile progress track, smoothly animating in real time behind the player hero markers.
- **Dynamic Pursuer Speed & Spectral Glide**:
  - Replaced the sluggish 1-tile crawl with dynamic gap-based pacing:
    - **Long Range (gap >= 18 tiles)**: Surges **4 to 5 tiles per round** through the mist to prevent fast players from leaving it completely behind.
    - **Mid Range (gap >= 8 tiles)**: Pursues at **3 to 4 tiles per round**.
    - **Close Proximity (gap < 8 tiles)**: Menaces at **2 tiles per round** to provide intense proximity tension while giving players room to respond.
    - **Multiplayer Compensation**: +1 speed bonus when party size > 1 and gap >= 10 tiles.
  - Added smooth stepping interpolation: when moving across tiles, the reaper enters its spectral red ghost glide (`shepherdWalkAnim`), settling back into its floating hover (`shepherdIdleAnim`) upon arriving at its destination tile.
- **Encounter Harmonization & Choice Card UI**:
  - Updated encounter modal badge styling to crimson wine (`#4a0e17`) with rich mahogany header (`#4a0e17`).
  - Transformed the encounter modal choices from plain single-line `TextButton`s into full two-row interactive choice cards matching standard event choices:
    - **Row 1**: Left-aligned action title in bold font paired with a right-aligned rounded pill badge (`SACRIFICE`, `OFFERING`, `D20 (DC 10)`, `SURRENDER`, or `LOCKED`).
    - **Row 2**: Narrative description providing flavor and requirement context without spoiling mechanical outcome formulas.
  - Eliminated explicit mechanic formulas (`-> Surge +3 Tiles Ahead`, `-> Shepherd Halts for 2 Rounds`, and `>=10 Repels Shepherd 4 Tiles | <10 Drain 15 HP`) in favor of thematic, non-spoiler descriptions:
    - **Struggle for Life**: Tag: `SACRIFICE` (or `LOCKED`), Description: `"Endure the mortal chill. (Cost: 20 HP)"` (or `"Requires more than 20 HP to withstand."`).
    - **Soothe with Memory**: Tag: `OFFERING` (or `LOCKED`), Description: `"Present a relic to calm the restless spirit."` (or `"Your satchel holds no relics to offer."`).
    - **Trial of Will**: Tag: `D20 (DC 10)`, Description: `"Confront the reaper with inner resolve."`.
    - **Yield to the Scythe**: Tag: `SURRENDER`, Description: `"Accept peaceful rest upon the mountain."`.
  - Refined layout vertical margins and card padding so all 4 choices fit within the parchment scroll without overlapping the top banner or clipping beyond the bottom spindle.

### 5. Atmospheric Immersion: Eerie Awakening Audio & Bulb Burnout Sequence (`BoardScreen.java`, `AudioManager.java`, `sfx_reaper_appear.ogg`)
- **Procedural Eerie Sound Effect Design (`sfx_reaper_appear.ogg`)**:
  - Registered `REAPER_APPEAR("audio/sfx/sfx_reaper_appear.ogg")` in [AudioManager.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/audio/AudioManager.java).
  - Synthesized a multi-layered, 3.8-second eerie acoustic cue encoded in high-fidelity stereo Vorbis (44.1 kHz, 160 kbps):
    - **Layer 1 (Dying Filament Buzz & Pop, 0.0s - 0.7s)**: 60 Hz electrical mains buzz and raspy crackle culminating in an abrupt snap pop at 0.65s matching the visual filament failure.
    - **Layer 2 (Sub-Bass Dread Drop, 0.65s - 3.8s)**: Low sine sub-bass rumble dropping smoothly from 75 Hz to 35 Hz with exponential decay, providing physical weight through speakers/headphones.
    - **Layer 3 (Spectral Tritone Chimes, 0.9s - 3.5s)**: A dissonant musical cluster based on the tritone interval (`A2 = 110 Hz`, `C3 = 130.8 Hz`, `Eb3 = 155.6 Hz`, `Eb4 = 311.1 Hz`) ringing out with long reverberation.
    - **Layer 4 (Icy Mountain Wind, 0.6s - 3.8s)**: Filtered pink noise sweeping dynamically across stereo channels, simulating high-altitude mountain blizzard winds whistling through dead stone.
- **Bulb Burnout Visual Sequence (`renderBulbBurnoutEffect`)**:
  - Implemented a 4-phase full-screen lighting sequence rendered directly over `hudStage` in 1920x1080 screen space when The Pale Shepherd awakens on Tile 1:
    - **Phase 1: Filament Brownout & Dying Surge (0.00s - 0.65s)**: Screen undergoes rapid brownout dips (alpha 0.35f to 0.70f), violent tungsten filament flutter pulses at 25 Hz, and a final momentary amber-white filament flare at 0.60s before burning out completely.
    - **Phase 2: Total Pitch-Black Blackout (0.65s - 1.15s)**: Complete darkness (solid black overlay with alpha 1.0f) enveloping the entire world and HUD, leaving only silence and the sub-bass rumble.
    - **Phase 3: Cold Spectral Vignette Spotlight (1.15s - 2.60s)**: Procedural 256x256 radial vignette texture blends in, casting cold darkness over the periphery while spotlighting Tile 1 as the camera glides down the mountain to reveal the crimson reaper.
    - **Phase 4: Sunlight Restoration (2.60s - 3.80s)**: Vignette and dark shroud smoothly dissipate back to full daylight, returning camera control and restoring the hero turn prompt.
- **Violent Camera Tremor Jitter**:
  - In `updateCameraTracking(delta)`, added high-frequency electrical camera shake (`jitterX`, `jitterY` up to +/-7px at 38 Hz) synchronized with the filament surge and pop (0.50s - 0.70s), making the entire screen physically shudder as the light goes out.

### 6. Animation Refinement & Double-Sprite Bug Fix (`char_pale_shepherd.png`, `BoardScreen.java`)
- **Root Cause Analysis (Double Sprite Bug)**:
  - During initial spritesheet generation, `build_new_reaper_sheet.ps1` omitted the vertical row offset `($r * 64)` when blitting the 4 source bands (Idle, Glide, Slash, Demise) into the intermediate bitmap. Consequently, all 4 rows were rendered on top of each other into the first 64px row (Row 0), producing composite frames where the idle reaper was superposed with gliding phantom trails, raised slash arms, and second scythes ("double" appearance), while Rows 1–3 were left completely blank.
- **Clean Spritesheet Reconstruction (`1024x1024`, 4x4 Grid)**:
  - Re-extracted each band from `media_1795682469816_b4d78639.png` with correct vertical offsets ($0$px, $64$px, $128$px, $192$px) scaled $4\times$ via nearest-neighbor interpolation to $256\times 256$ frames in [assets/img/char_pale_shepherd.png](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/assets/img/char_pale_shepherd.png):
    - **Row 0**: Crisp 4-frame breathing idle animation ($Y = 0..255$) with zero ghosting or overlapping silhouettes.
    - **Row 1**: Dedicated 4-frame spectral glide animation ($Y = 256..511$) featuring the phantom mist trail.
    - **Row 2**: 4-frame scythe slash and attack animation ($Y = 512..767$).
    - **Row 3**: 4-frame dissipation and demise animation ($Y = 768..1023$).
  - Re-extracted a crisp, centered `char_pale_shepherd_head.png` ($104\times 80$) for the mini-map tracker without stray pixels.
- **Feathered Procedural Shadow & Smooth Hover Dynamics (`BoardScreen.java`)**:
  - Replaced the harsh solid rectangular `whitePixelTex` ground bars and background aura with a procedural $64\times 32$ feathered elliptical soft shadow (`softShadowTex`).
  - Tuned `hoverOffset` to a gentle, organic float ($\pm 8$px at $2.2\text{ rad/s}$ when idle, steady $4$px elevation when gliding).
  - Synchronized `shepherdIdleAnim` frame duration to `0.20f` loop for steady, natural breathing.

---

## Verification & Testing
- Executed the complete test suite:
  ```powershell
  .\gradlew.bat testBoard testInventory compileJava :lwjgl3:compileJava
  ```
- **Results**:
  - 1,000 Board Generation simulations passed with zero invariant violations.
  - All 22 Inventory, Event, Combat, and Milestone tests passed with zero failures.
  - LWJGL3 and Core Java modules compiled cleanly with zero errors.
