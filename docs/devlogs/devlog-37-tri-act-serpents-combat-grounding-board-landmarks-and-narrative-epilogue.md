# Devlog 37: Tri-Act Serpent Evolution, Battle Scale & Platform Grounding, Board Visual Landmarks, Barter Ergonomics & Cinematic Story Epilogue

* **Date:** November 27, 2026
* **Author:** Dorothy Anne R. Domanico (Lead Designer & Asset Developer)
* **Sprint / Milestone:** Week 6 – Act-Stratified Serpent Evolution, Combat Scale & Grounding, Board Visual Landmarks, Class Balance & Cinematic Epilogue
* **Branch:** `feat/board-adventure-screen`

---

## Objectives
- **Tri-Act Serpent Visual & Combat Evolution**: Differentiate serpent encounters across the mountain climb by integrating dedicated, distinct sprite sheets for Act I (Forest Green Snake), Act II (Sapphire Horned Serpent), and Act III (Crimson Magma Wyrm), complete with coiled breathing idle animations and forward-striking venom lunges.
- **BattleScreen Platform Grounding & Combat FX Recalibration**: Eliminate the floating serpent issue in `BattleScreen`. Align serpent sprites squarely on top of the stone combat platform with zero air gap, dampen vertical breathing bob, and anchor hit slashes, sparks, and floating damage numbers directly over the serpent's body core.
- **BoardScreen Act-Stratified Snake Tiles & Visual Landmarks**: Replace the abstract procedural hazard crest on `SnakeTile` instances with dynamic, animated serpent landmarks that visually match the exact serpent species awaiting climbers in battle for that Act, scaled proportionally (~65% player size) and anchored to the tile ledge.
- **Character Class Balancing, Concise Tooltips & Dedicated Asset Integration**:
  - Implement Fox Recommendation B ("Cunning Ambush" / serpent charm glide).
  - Simplify HUD action subtext and descriptions across all 4 animal classes to be concise, punchy, and instantly readable.
  - Replace the Raccoon's duplicate inventory backpack asset with a dedicated explorer satchel icon for the "Scavenge" skill.
- **Streamlined Equal-Exchange Barter Modal**: Overhaul the parchment merchant trade dialogue to replace dense text strings with high-contrast, structured `GIVE:` and `RECEIVE:` cards, clear tier badges, and bounded widths that respect the parchment scroll margins.
- **Cinematic Ending Story Slideshow Screen & Narrative Epilogues**: Introduce an interactive storybook epilogue screen (`EndingStoryScreen.java`) featuring bespoke illustrated slides, Ken Burns camera drift, atmospheric audio, and personalized narrative conclusions for each companion class before reaching the final expedition summary.
- **Multiplayer Pass-and-Play Synchronization**: Synchronize reaper progression in 3–4 player games so the pursuer advances once per full board round rather than on every individual player's turn, and ensure boss defeats cleanly hand off turns to surviving teammates.
- **Build Stability & Automated Regression Verification**: Resolve missing procedural texture declarations and verify 100% test pass rates across all bug fix and inventory test suites.

---

## Work Completed

### 1. Tri-Act Serpent Visual & Combat Evolution ([BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java), [Assets.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/ui/Assets.java))
- **Act-Stratified Sprite Sheets**:
  - **Act I (Tiles 1–30) – Forest Green Snake** (`assets/img/enemy_snake_act1.png`): The classic emerald forest serpent with yellow underbelly, re-centered in 128x128 cells facing left toward the climber.
  - **Act II (Tiles 31–70) – Sapphire Horned Serpent** (`assets/img/enemy_snake_act2.png`): An aggressive cerulean serpent with jagged horns and venomous fangs, reflecting the harsh crags of the alpine mid-mountain.
  - **Act III (Tiles 71–100) – Crimson Magma Wyrm** (`assets/img/enemy_snake_act3.png`): A fiery, spined dragon-serpent with volcanic dorsal ridges and dripping venom, guarding the approach to the celestial summit.
- **Dynamic Asset Loading**:
  - Added `Assets.getSnakeTextureForAct(int act)` to return the appropriate `AssetDescriptor<Texture>`.
  - In `BattleScreen.initCharacterAnimations()`, determine the current Act based on `snakeTile.getPosition()`, `player.getPosition()`, or the enemy's type name.
- **Dual Animation States (Idle & Lunge)**:
  - **Idle Animation** (`enemyIdleAnim`): Loops frames 0 and 1 at 0.26s per frame, creating an organic, coiled breathing rhythm.
  - **Attack Strike Animation** (`enemyAttackAnim`): Plays frames 2, 3, 4, and 5 at 0.13s per frame. During `enemyLungeTimer > 0f`, the animation progress is keyed directly to the physical lunge kinematics (`lungeProgress = 1.0f - (enemyLungeTimer / ENEMY_LUNGE_DURATION)`), ensuring fangs and venom are bared at the apex of the strike.

```java
// BattleScreen.java: Act-stratified serpent animation setup
AssetDescriptor<Texture> snakeAsset = Assets.getSnakeTextureForAct(act);
Texture snakeSheet = game.getAssetManager().get(snakeAsset);
TextureRegion[][] snakeGrid = TextureRegion.split(snakeSheet, 128, 128);
TextureRegion[] frames = snakeGrid[0];
for (TextureRegion frame : frames) {
    if (frame.isFlipX()) frame.flip(true, false); // Face left toward hero
}
enemyIdleAnim = new Animation<>(0.26f, new TextureRegion[]{frames[0], frames[1]});
enemyIdleAnim.setPlayMode(Animation.PlayMode.LOOP);

enemyAttackAnim = new Animation<>(0.13f, new TextureRegion[]{frames[2], frames[3], frames[4], frames[5]});
enemyAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
```

---

### 2. Battle Platform Grounding & Combat Scale Calibration ([BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java))
- **Heroic Scale Calibration (Battle vs. Board Size Differentiation)**:
  - Previously, `BattleScreen.java` drew snake frames at `CHAR_DISPLAY_SIZE = 210f`. Because the Act I serpent only occupies a 46x31 px sub-rectangle inside its 128x128 canvas, it scaled down to only ~75x51 visible pixels on screen—less than half the player's character size!
  - We established a strict visual hierarchy between the adventure map and battle screens:
    - **Board Screen**: Serpent landmarks remain compact (~100x67 px, ~65% player size) coiled neatly atop their tiles so the trail path and tile numbers remain uncluttered.
    - **Battle Screen**: When entering mortal combat, the serpent is scaled up to match the heroic stature of the player character:
      - **Act I Forest Python**: Scaled to `enemyW = 440f, enemyH = 440f`, yielding a visible body of **158 x 107 px** (perfectly matching the Cat player's **148 x 104 px**).
      - **Act II Crag Carapace Serpent**: Scaled to `enemyW = 270f, enemyH = 270f`, yielding a visible body of **177 x 139 px** (imposing mid-mountain serpent stance).
      - **Act III Crimson Magma Wyrm**: Scaled to `enemyW = 290f, enemyH = 290f`, yielding a visible body of **163 x 152 px** (towering dragon-like wyrm silhouette).
- **Pixel-Perfect Platform Grounding Math**:
  - The stone combat slab platform surface is at `TILE_TOP_Y = 282f`.
  - Transparent bottom padding in each 128x128 sprite sheet was measured down to the exact pixel (17 px for Act I and II, 18 px for Act III).
  - Implemented dynamic base Y resolution:
    ```java
    float padOffset = (bottomPadPx / 128f) * enemyH;
    float enemyBaseY = 282f - padOffset;
    float drawEY = enemyBaseY + (isPaleShepherdBattle() ? (16f + hover) : (enemy.getHealth() <= 0 ? -12f : eBob)) + eLungeY + descentOffsetY;
    ```
  - This guarantees that whether the serpent is coiled alive or squashed on defeat (`enemyH *= 0.72f`), the lowest visible pixel of its body rests directly on the top surface of the stone platform slab with zero floating and zero clipping.
- **Breathing Bob Softening**:
  - Dampened vertical bobbing amplitude to `MathUtils.sin(stateTime * 3f + 1f) * 2.5f`. The serpent maintains subtle breathing motion without lifting off the slab.
- **Combat FX & Damage Floater Re-Centering**:
  - Combat slashes, hit sparks, and floating damage numbers now dynamically center on each Act's enlarged serpent body:
    - **Act I**: `enemyHitY = 282f + 53f = 335f` (damage numbers at `enemyHitY + 65f = 400f`).
    - **Act II**: `enemyHitY = 282f + 70f = 352f` (damage numbers at `enemyHitY + 65f = 417f`).
    - **Act III**: `enemyHitY = 282f + 76f = 358f` (damage numbers at `enemyHitY + 65f = 423f`).

---

### 3. BoardScreen Act-Stratified Snake Tiles & Visual Landmarks ([BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java))
- **Dynamic Board Tile Landmarks**:
  - Removed the abstract red procedural crest and eye dots from `SnakeTile` rendering in `BoardScreen.java`.
  - Added dedicated board serpent animation fields: `boardSnakeAnimAct1`, `boardSnakeAnimAct2`, and `boardSnakeAnimAct3`.
  - Initialized in `initCharacterAnimations()` from the respective Act sprite sheets, ensuring complete visual continuity between the adventure map and combat encounters.
- **Proportional Scaling (~65% Player Size)**:
  - Players are rendered at 150x150 px. To ensure the board hazard is clearly visible without overpowering the player or tile numbers, dimensions were calibrated per species:
    - **Act I**: 100 x 67 px (emerald serpent coiled low).
    - **Act II**: 110 x 86 px (horned sapphire serpent with raised crest).
    - **Act III**: 96 x 88 px (magma wyrm with arched back and dorsal spines).
- **Tile Ledge Grounding & Organic De-Synchronization**:
  - Grounded each serpent at `tileY + TILE_HEIGHT - 6f` with a soft elliptical drop shadow (`Color(0.08f, 0.04f, 0.04f, 0.35f)`).
  - Offset the animation keyframe sampling per tile index (`snakeTime = stateTime * 0.85f + (i * 0.45f)`), ensuring multiple snake tiles on screen do not coil and breathe in rigid lockstep.

```java
// BoardScreen.java: Act-stratified board snake tile rendering
int act = (i <= 30) ? 1 : ((i <= 70) ? 2 : 3);
Animation<TextureRegion> anim = (act == 1) ? boardSnakeAnimAct1 : ((act == 2) ? boardSnakeAnimAct2 : boardSnakeAnimAct3);

// Subtle grounding shadow along slab surface
batch.setColor(0.08f, 0.04f, 0.04f, 0.35f);
batch.draw(whitePixelTex, tileLeft + 30f, tileY + TILE_HEIGHT - 4f, TILE_WIDTH - 60f, 6f);

// Render Act-specific serpent coiled on top of the tile platform
if (anim != null) {
    float snakeTime = stateTime * 0.85f + (i * 0.45f);
    TextureRegion snakeFrame = anim.getKeyFrame(snakeTime, true);
    float snakeX = getTileCenterX(i) - snakeW / 2f + xOffset;
    float snakeY = tileY + TILE_HEIGHT + yOffset;

    batch.setColor(Color.WHITE);
    batch.draw(snakeFrame, snakeX, snakeY, snakeW, snakeH);
}
```

---

### 4. Character Class Balancing, Tooltips & Dedicated Asset Integration ([CatAgilitySkill.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/skills/CatAgilitySkill.java), [FoxCunningSkill.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/skills/FoxCunningSkill.java), [RaccoonScavengeSkill.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/skills/RaccoonScavengeSkill.java), [TurtleShellSkill.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/skills/TurtleShellSkill.java))
- **Dedicated Raccoon Scavenge Icon**:
  - Replaced the duplicate satchel asset with a dedicated pixel-art leather backpack with brass buckles (`assets/img/icon_scavenge.png`), visually separating the Raccoon's unique class skill from the player's generic inventory button.
- **Concise HUD Subtext & Descriptions**:
  - Replaced verbose sentences with clear, instantly scannable rules:
    - **Cat (Feline Fortune)**: Subtext `"NEXT ROLL = 6"` | Description: *"Makes your next dice roll an automatic 6."*
    - **Fox (Cunning Ambush - Rec B)**: Subtext `"+2 ROLL & DODGE SNAKE"` | Description: *"Adds +2 to your next roll. Landing on a snake glides +3 tiles forward instead of sliding."*
    - **Raccoon (Survival Scavenger)**: Subtext `"ITEM OR +15 HP"` | Description: *"Finds a random trail item, or heals +15 HP if your satchel is full."*
    - **Turtle (Iron Shell)**: Subtext `"MOVE +4 (NO SNAKE)"` | Description: *"Cleanses curses and marches +4 tiles safely, immune to snakes."*

---

### 5. Streamlined Equal-Exchange Barter Modal ([BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java), [UIUtils.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/ui/UIUtils.java))
- **High-Readability Structured Cards**:
  - Redesigned `showBarterItemSelector()` to display a clean two-line structured layout for each tradable item:
    - **Line 1 (`GIVE:` in warm cream `#e8be8b`)**: Name of the player's current item.
    - **Line 2 (`RECEIVE:` in soft mint green `#95d5aa`)**: Specific item offered in exchange with a concise effect preview (e.g. `Tempered Steel Sword (+6 ATK)`).
    - **Pill Badge**: Displays item tier (`TIER I`, `TIER II`, `TIER III`) or `PURIFY` badge in a rounded leather container.
  - Enforced a strict maximum width of 386px, guaranteeing that text wraps cleanly within the scroll's parchment margins without clipping against the wooden roller spindles.
- **Interactive D20 Rolling Anticipation System**:
  - Implemented `createD20EmblemTexture(int size)` in `UIUtils.java` generating a procedural icosahedron die emblem with mahogany facets and gold border struts.
  - Added dynamic tumbling numbers and audio ticks (`AudioManager.SoundEffect.DICE_ROLL`) before resolving critical success, pass, or failure states during event dilemma checks.

---

### 6. Branching Victory vs. Defeat Story Routes & Dedicated Victory Art ([EndingStoryScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/EndingStoryScreen.java), [Assets.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/ui/Assets.java))
- **Decoupled Victory & Defeat Epilogue Sequences**:
  - Previously, both summit victory (defeating the Dawn Warden) and defeat (falling to the boss) routed to the same resting/slumber illustrations.
  - Architected two distinct, fully illustrated narrative branches tailored to the outcome of the summit confrontation:
    - **Defeat Route (`EndingType.PEACEFUL_REST`)**: Features peaceful twilight and restful slumber as the fallen hero is comforted and laid to rest by The Pale Shepherd (`ending_dawn_summit.png`, `ending_guardians_farewell.png`, `ending_cat_rest.png`, `ending_turtle_rest.png`, `ending_fox_rest.png`, `ending_raccoon_rest.png`, `ending_fellowship_rest.png`).
    - **Victory Route (`EndingType.REBIRTH_TRIUMPH`)**: Features 7 brand-new, glorious golden-hour celebration illustrations where the living champions conquer the summit:
      - **Beat 1**: *Triumph at the Hundredth Stone* (`ending_dawn_victory.png`) – Radiant sunbeams through celestial stone arches above the clouds.
      - **Beat 2**: *Honor of the Dawn* (`ending_warden_concession.png`) – The Dawn Warden bows its crowned head in noble concession while The Pale Shepherd lowers his golden scythe in respect.
      - **Beat 3**: *The Champion's Triumph* (`createChampionVictorySlide`):
        - Cat: *Sovereign of Nine Lives* (`ending_cat_victory.png`) – Standing tall and proud in the morning breeze, agility pendant ablaze.
        - Turtle: *The Unbreakable Aegis* (`ending_turtle_victory.png`) – Shield raised heroically toward the sun.
        - Fox: *The Cunning Conqueror* (`ending_fox_victory.png`) – Cloak billowing and tail swishing proudly atop the peak.
        - Raccoon: *The Crowned Fortune-Seeker* (`ending_raccoon_victory.png`) – Leaping with joy holding the golden summit relic high.
      - **Beat 4**: *Triumph of the Fellowship* (`ending_fellowship_victory.png`) – All four companions cheering side-by-side at the peak.
- **Cinematic Polish & Audio Stems**:
  - Dynamic music track routing: `EPILOGUE_VICTORY` for triumphant summit victory and `EPILOGUE_REST` for peaceful twilight slumber.
  - Integrated Ken Burns slow pan/zoom (`camScale` and `camPanX/Y`), smooth cross-fade alpha interpolation, and keyboard/mouse controls.

---

### 7. Pass-and-Play Multiplayer Synchronization & Boss Hand-Off ([BoardScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BoardScreen.java), [BattleScreen.java](file:///c:/Users/John%20Calvin/Documents/DevJourn/Snakes-and-Ladders-Roll-of-Fate/core/src/main/java/io/github/snakes_and_ladders_roll_of_fate/screens/BattleScreen.java))
- **Round-Synchronized Pursuer Pacing**:
  - In multiplayer games (2–4 players), The Pale Shepherd pursuer now only advances after all active players have completed their turns (end-of-round synchronization), rather than advancing after each individual player's action. This prevents the reaper from accelerating at 4x speed in 4-player games.
- **Multiplayer Boss Defeat Turn Hand-Off**:
  - When a player falls in battle against the Dawn Warden, `BattleScreen` checks if surviving teammates remain in `game.getPlayers()`.
  - If teammates are alive, the victory card presents `"RETURN TO BOARD"` instead of jumping prematurely to the game-over screen, seamlessly handing the turn to the surviving player at their current tile.

---

## Visual Verification & Screenshots

| Visual Capture Artifact | Dimensions | Key Verification Highlights |
| :--- | :---: | :--- |
| `battle_act1_snake.png` | 1920 x 1080 | Act I Forest Python scaled to hero size (158x107 px visible body) squarely on stone platform with zero air gap, facing the hero. |
| `battle_act2_snake.png` | 1920 x 1080 | Act II Sapphire Horned Serpent scaled to 177x139 px visible body, grounded with indigo hue. |
| `battle_act3_snake.png` | 1920 x 1080 | Act III Crimson Magma Wyrm scaled to 163x152 px visible body, towering with volcanic spines and ember glow. |
| `board_act1_snake.png` | 1920 x 1080 | Act I green snake landmark on Board Tile 14 (100x67 px, ~65% player size), facing left toward oncoming climbers. |
| `board_act2_snake.png` | 1920 x 1080 | Act II sapphire serpent on Board Tile 42 (110x86 px) with subtle drop shadow along tile ledge. |
| `board_act3_snake.png` | 1920 x 1080 | Act III magma wyrm on Board Tile 84 (96x88 px) with desynchronized idle animation. |
| `barter_modal_full.png` | 1920 x 1080 | Simplified Equal-Exchange Barter modal; high-contrast `GIVE:` and `RECEIVE:` cards cleanly wrapped within parchment scroll margins. |
| `board_hud_skill_raccoon_ready.png` | 360 x 240 | Dedicated leather explorer backpack icon (`icon_scavenge.png`) and punchy `"ITEM OR +15 HP"` subtext. |
| `ending_slide_victory_summit.png` | 1920 x 1080 | Victory Beat 1: Glowing runic archway at the 100th stone with radiant golden sunbeams. |
| `ending_slide_warden_concession.png` | 1920 x 1080 | Victory Beat 2: The Dawn Warden bowing in concession and The Pale Shepherd dipping his scythe in salute. |
| `ending_slide_cat_victory.png` | 1920 x 1080 | Victory Beat 3: Solid black cat champion standing proud on the summit crag with glowing agility charm. |
| `ending_slide_turtle_victory.png` | 1920 x 1080 | Victory Beat 3: Turtle guardian standing victorious with iron shield raised toward the dawn sun. |
| `ending_slide_fox_victory.png` | 1920 x 1080 | Victory Beat 3: Fox scout grinning boldly into the sunrise with cloak and tail wind-swept atop the peak. |
| `ending_slide_raccoon_victory.png` | 1920 x 1080 | Victory Beat 3: Raccoon adventurer leaping with joy holding the golden summit relic high. |
| `ending_slide_fellowship_victory.png` | 1920 x 1080 | Victory Beat 4: All four companions cheering triumphantly side-by-side at the mountain summit. |
| `ending_slide_cat_rest.png` | 1920 x 1080 | Defeat Route: The nimble cat sleeping peacefully in the clover grass in gentle slumber. |
| `ending_slide_shepherd_solace.png` | 1920 x 1080 | Defeat Route: The Pale Shepherd providing quiet comfort and peaceful rest to the fallen traveler. |

---

## Test Suite Verification

All automated regression and feature test suites passed with 100% success rate:

```text
=== RUNNING BOARD ADVENTURE BUG FIX VERIFICATION SUITE ===
[Test 1] Bug A: Verifying Landing on Ladder / Snake / Landmark... [PASS]
[Test 2] Bug B: Verifying Pursuer Anti-Overshoot Clamping... [PASS]
[Test 3] Bug B: Verifying Snake Slide Void Interception & Halved Tumble Damage... [PASS]
[Test 4] Bug B: Verifying Encounter Pushback and Contemplation Pause (No D1 Loop)... [PASS]
[Test 5] Verifying Shepherd Random Dormant Range (4 to 7 Rounds)... [PASS]
[Test 6] Verifying Snake Tail Slide Acrobatics D20 Tumble Mechanics... [PASS]
[Test 7] Verifying Snake Slaying Stat Mastery & Exclusive Spoils... [PASS]
[Test 8] Verifying Serpent's Bane Reaper Repelling & Adrenaline Surge... [PASS]
[Test 9] Verifying Pale Shepherd Mortal Duel Victory & Escape Resolutions... [PASS]
[Test 10] Verifying Dawn Warden True Phase 2 Ascension & Balanced Scaling... [PASS]
[Test 11] Verifying Multiplayer Boss Defeat Returns Turn to Surviving Teammate... [PASS]
>>> ALL 11 BUG FIX & COMBAT SYSTEM VERIFICATIONS PASSED WITH ZERO FAILURES!

=== RUNNING INVENTORY & EVENT INTEGRATION TESTS ===
[Tests 1-23] All consumable usage, single-use equipment boons, interactive item swaps,
             equal-level barter transactions, curse purification, and double-edged relics passed!
>>> ALL 23 INVENTORY & EVENT INTEGRATION TESTS PASSED PERFECTLY!
```

---

## Milestones & Next Steps
- [x] **Tri-Act Serpent Combat & Visual Grounding**: Fully integrated Act I, II, and III sprites with coiled idle and lunging attack animations.
- [x] **Serpent Scale Calibration (Battle vs. Board Screen)**: Calibrated battle serpent scale to match the player character's visible stature (~150-180px wide, ~110-150px tall across Acts) while maintaining cute landmark scale on the board (~65%), with zero-gap platform grounding on `ENEMY_DRAW_Y = 282f`.
- [x] **Board Visual Landmark Continuity**: Grounded Act-stratified serpent models directly onto `SnakeTile` positions.
- [x] **Character Class Balance & Asset Polish**: Overhauled Fox Recommendation B, updated Raccoon Scavenge icon, and simplified HUD tooltips.
- [x] **Barter Ergonomics**: Implemented structured, bounded Give/Receive cards for seamless parchment readability.
- [x] **Cinematic Story Epilogue & Victory Routes**: Added `EndingStoryScreen.java` with dedicated victory vs. peaceful rest branching, class-specific champion portraits, and Ken Burns camera motion.
- [ ] **Final Audio Atmosphere Polish**: Integrate subtle unique audio cues for Act II sapphire serpent rattle and Act III magma wyrm hiss.
- [ ] **Pre-Release Playtest Session**: Conduct full 4-player pass-and-play expedition from Tile 1 to Tile 100 to review overall game pacing.
