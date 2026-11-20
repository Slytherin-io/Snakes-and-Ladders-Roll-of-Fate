# Devlog 12: Event & Inventory Integration and Wandering Merchant

* **Date:** November 20, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Encounters: Event & Inventory Integration
* **Branch:** `feat/inventory-system`

---

## Objectives
- Connect the inventory system directly with the Event Manager to allow event choices to award items and loot drops.
- Implement an interactive inventory swap menu (Option B) so players can choose which item to discard when their 4-slot bag is full.
- Design a Wandering Merchant encounter utilizing survival currencies (Health, Experience, and Item Barter) in place of traditional gold coins.
- Introduce conditional choice locking and equipment-assisted choices (e.g., using a shield to safely deflect trap damage).

---

## Work Completed

### 1. Item Rewards in Event Effects
- **`systems/EventEffectType.java`**: Added the `ITEM` effect type to support direct item awards and random loot drops.
- **`systems/EventEffect.java`**:
  - Implemented constructors and helpers for fixed item rewards and procedural random loot (`EventEffect.createRandomLootEffect()`).
  - Added an `ItemSwapHandler` callback hook to handle full inventory states gracefully across console and graphical frontends.

### 2. Option B: Interactive Inventory Swap on Full Capacity
- **`items/Inventory.java` & `entities/Player.java`**:
  - Added `replaceItem(int index, Item newItem)` to safely swap out an existing item at a specific slot when full.
- **`ConsoleTestGame.java`**:
  - Configured the console `ItemSwapHandler`. When an item is received with a 4/4 full inventory, the player is presented with their current 4 slots and can choose a slot (1–4) to discard and replace, or enter `0` to leave the new item behind.

### 3. Wandering Merchant with Survival Currencies
- **`systems/EventRepository.java` & `systems/EventStoryGenerator.java`**:
  - Built `createProceduralMerchantEvent(tilePosition)` with randomized atmospheric stories.
  - Implemented three distinct survival transaction types:
    1. **Blood Rite (Health as Currency):** Pay 20 HP to acquire an Iron Sword (+4 ATK).
    2. **Knowledge Tribute (EXP as Currency):** Pay 35 EXP to purchase a Reinforced Shield (+4 DEF).
    3. **Barter (Item for Item):** Trade any existing inventory item in exchange for a Greater Health Elixir (+50 HP).
    4. **Decline:** Safely walk away with no penalties.

### 4. Choice Requirement Locking & Equipment-Assisted Actions
- **`systems/EventChoice.java`**:
  - Added requirement checking (`isAvailable(player)`) for minimum health, minimum EXP, item presence, and equipment availability.
  - Implemented equipment-assisted choices (`createEquipmentAssistedChoice`): using an equipment item absorbs hazard damage and consumes 1 durability instead of taking direct health loss.
- **`systems/Event.java`**:
  - Updated `trigger(Player player)` to display `[LOCKED]` on choices where player requirements are not met (e.g., insufficient EXP or HP).
  - Updated `resolve(choiceIndex, player)` to prevent players from executing locked choices.

### 5. Battle System & Event Trigger Harmonization
- **`board/SnakeTile.java` & `ConsoleTestGame.java`**:
  - Rewired the Snake Tile resolution logic so the Battle System is triggered exclusively when the player actively chooses to engage (`Choice 1: Draw weapon and fight`).
  - Choosing to retreat (`Choice 3: Flee down slope`) slides the player down safely with zero combat engagement.
  - Choosing shield deflection (`Choice 2`) consumes 1 equipment durability and holds ground without fighting or sliding.
- **`board/LadderTile.java` & `board/NormalTile.java`**:
  - Eliminated duplicate dialogue calls (`dialogue.createLadderDialogue()` and `dialogue.createSnakeDialogue()`), allowing procedural event stories to take center stage.
  - Normal tiles now only print peaceful breathing dialogue when no ambient event occurs.

### 6. Automated Verification Suite
- **`items/InventoryTest.java`**:
  - Expanded the test suite to 15 automated tests covering item grants, Option B swap replacement, merchant blood rites, EXP costs, barter trades, requirement locking, equipment hazard deflection, and snake tile combat trigger harmonization.
  - Verified 100% test passage via Gradle (`.\gradlew :core:testInventory`).

---

## Challenges & Blockers Resolved

- **Economy Without Currency:** Because the game lacks gold or coins, standard merchant designs were not applicable. Resolved by utilizing survival resources (Health, EXP, and Item Barter), which creates higher stakes and strategic trade-offs.
- **Full Inventory During Event Rewards:** Automatically abandoning rewards felt punishing, while forcing an item into a full bag caused array errors. Resolved with Option B (Interactive Swap), giving players full agency over what gear to keep or discard.
- **Suicide & Bankrupt Choice Prevention:** Prevented players from accidentally killing themselves on Blood Rites or spending EXP they don't possess by adding strict requirement checks and displaying `[LOCKED]` indicators.
- **Combat Trigger Inversion & Double Dialogue Collision:** Choosing to flee previously triggered `SnakeTile.onLand()`, which forced the player into an automated battle to the death while printing a second, unrelated serpent story. Resolved by decoupling the slide from combat: fleeing immediately slides down with zero combat, while choosing to fight cleanly initiates the `BattleSystem`.

---

## Next Steps
- Translate the polished console simulation mechanics (Inventory, Merchant barters, and Snake combat encounters) into graphical Scene2D UI dialog windows for `BoardScreen` and `BattleScreen`.
- Implement visual animations and sound cues for acquiring items, shattering equipment, and taking damage in battles.
