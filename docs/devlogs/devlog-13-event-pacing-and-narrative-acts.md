# Devlog 13: Event Pacing, Bad-Luck Protection, and Narrative Act Stratification

* **Date:** November 20, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Encounters: Event Pacing & Narrative Acts
* **Branch:** `feat/event-pacing-and-narrative-acts`

---

## Objectives
- Implement an intelligent pacing controller for ambient tile encounters to eliminate event fatigue.
- Introduce encounter cooldowns and a dynamic bad-luck protection curve.
- Stratify procedural events, merchant wares, shrines, and hazards across three distinct narrative Acts (Act I, Act II, Act III).
- Give ladder tiles selective encounter pacing (blending pure climbing movement with high-risk ascent gambles).
- Verify all pacing, cooldown, and narrative scaling logic via automated integration tests.

---

## Work Completed

### 1. Dynamic Pacing Controller & Bad-Luck Protection
- **`entities/Player.java`**:
  - Added `turnsSinceLastEvent` tracking, initialized to `99` so players are eligible for an encounter upon embarking.
  - Added helper methods `incrementTurnsSinceLastEvent()` and `resetTurnsSinceLastEvent()`.
- **`systems/EventManager.java`**:
  - Implemented `shouldTriggerAmbientEvent(Player player)` governed by:
    - **Cooldown Period (`COOLDOWN_TURNS = 1`):** Ensures at least one turn of breathing room after any encounter before an ambient event can trigger.
    - **Base Ambient Probability (`BASE_AMBIENT_CHANCE = 0.32`):** 32% base encounter rate on normal tiles.
    - **Bad-Luck Protection:** Increases ambient encounter probability by +5% per turn traversed beyond the cooldown, ensuring players do not encounter long, monotonous stretches without gameplay variety.
  - Implemented `shouldTriggerLadderEvent()` with `LADDER_EVENT_CHANCE = 0.40` (40% chance of a high-risk climbing dilemma, 60% smooth immediate ascent).

### 2. Three-Act Narrative Stratification (100-Tile Progression)
- **`systems/EventStoryGenerator.java`**:
  - Introduced `getAct(tilePosition)` partitioning the 100-tile board into three distinct atmospheric regions:
    - **Act I: The Foothills of Ruin (Tiles 1–33):** Minor venomous adders, crumbling scout caches, weathered stone fonts, and humble nomadic scavengers.
    - **Act II: The Sunken Catacombs (Tiles 34–66):** Shadowy horned basilisks, mechanical springboards, poison dart traps, and sinister alchemists.
    - **Act III: The Spire of Fate (Tiles 67–100):** Ancient wyrms, volcanic geothermal vents, celestial font shrines, and mythic relic traders.
  - Expanded atmospheric story pools for Shrines, Snakes, Ladders, and Merchants with specialized prefixes, subjects, and thematic hooks tailored to each Act.

### 3. Act-Tiered Event Repository & Wares
- **`systems/EventRepository.java`**:
  - **Stratified Wandering Merchant:**
    - *Act I:* Humble trades (10 HP Blood Rite for Bronze Dagger; 20 EXP for Wooden Buckler; Barter for Minor Health Potion).
    - *Act II:* Mid-game trades (20 HP Blood Rite for Iron Sword; 35 EXP for Reinforced Shield; Barter for Greater Health Elixir).
    - *Act III:* Summit trades (35 HP Blood Rite for Dragon-Slayer Blade; 60 EXP for Aegis of the Summit; Barter for Celestial Ambrosia).
  - **Stratified Shrines & Ladders:**
    - Scaled shrine blessings and stat enhancements proportionally to Act progression.
    - Tuned ladder dice check thresholds (DC 9 in Act I, DC 11 in Act II, DC 12–13 in Act III).
  - **Zoned Ambient Deck:**
    - Act I: Fallen Scout's Pack (Scavenge loot).
    - Act II: Hidden Springboard & Poison Dart Trap (Mechanical hazards).
    - Act III: Volcanic Wyrm Vent (High-damage geothermal hazard with shield brace option).

### 4. Console Gameplay Simulation Integration
- **`ConsoleTestGame.java`**:
  - Integrated `shouldTriggerAmbientEvent()` into normal tile resolution. If triggered, player's encounter counter resets to 0; otherwise, the counter increments and peaceful atmospheric dialogue is displayed.
  - Integrated `shouldTriggerLadderEvent()` into ladder resolution. Special climbing dilemmas trigger only on selective rolls, with standard climbs executing immediately.

### 5. Automated Verification Suite
- **`items/InventoryTest.java`**:
  - Added **Test 16**: Verified pacing cooldown locks (0% chance when cooldown is active) and dynamic bad-luck protection scaling (+5% per step).
  - Added **Test 17**: Verified Act stratification across Acts I, II, and III, confirming story titles, scaled merchant wares, and appropriate item tiers.
  - Executed Gradle test suite (`.\gradlew :core:testInventory`) with 17/17 tests passing cleanly.

---

## Challenges & Solutions

- **Event Fatigue vs. Monotony:** Triggering events on every tile overwhelmed players with text menus, while low static probabilities produced long, uneventful dry spells. Resolved by combining a hard 1-turn cooldown with dynamic bad-luck protection (+5% per safe tile), maintaining engaging yet unhurried game pacing.
- **Narrative & Gameplay Incongruity:** Early encounters in Act I previously offered late-game gear or demanded prohibitive EXP costs, while late-game events felt trivial. Stratifying the generator into three Acts ensured story tone, hazard severity, and merchant prices align with the player's board journey.

---

## Next Steps
- Rebalance Snake combat statistics and tune the BattleSystem damage curve to eliminate one-hit lethal strikes on higher tiles.
- Implement the "Mortal Retreat" combat safeguard so players below 25 HP slide down safely rather than facing instant permadeath.
- Design and construct the graphical Scene2D Event Modal Dialog overlay in `BoardScreen` to replace console input with interactive choice buttons.
