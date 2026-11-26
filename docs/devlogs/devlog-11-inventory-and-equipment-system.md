# Devlog 11: Inventory Management & Equipment System Backend

* **Date:** November 20, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Core Mechanics: Inventory & Equipment System
* **Branch:** `feat/inventory-system`

---

## Objectives
- Design and implement a robust, lightweight inventory system for players with a strict 4-slot non-stacking capacity to maintain tactical pressure and avoid console/UI clutter.
- Differentiate item archetypes into single-use `CONSUMABLE` items (e.g., healing potions, teleportation feathers) and multi-use `EQUIPMENT` items (e.g., weapons, shields) with randomized durability.
- Establish dynamic durability mechanics bounded within defined ranges per game run, degrading on each usage until shattering.
- Address stat inflation and combat balance by implementing temporary turn/combat buffs (Option A: "Active Guard" and "Battle Focus") instead of permanent stat stacking.
- Enforce turn action economy by restricting players to at most one item activation per turn prior to rolling movement dice.
- Integrate victory loot rewards upon defeating hostile board encounters (e.g., slaying a serpent on a `SnakeTile`).

---

## Work Completed

### 1. Item Classification & Durability Lifecycle
- **`items/ItemType.java`**:
  - Created an enum defining the two primary item classifications: `CONSUMABLE` and `EQUIPMENT`.
- **`items/Item.java`**:
  - Implemented the full item constructor alongside static factory methods:
    - `Item.createConsumable(...)`: Instantiates single-use items with a fixed durability of 1.
    - `Item.createEquipment(...)`: Instantiates gear with a randomized durability value rolled between configurable `minDurability` and `maxDurability` bounds.
    - `Item.createRandomLoot()`: Generates randomized loot drops from a weighted table of consumables and equipment.
  - Implemented durability degradation (`reduceDurability()`) and breaking detection (`isBroken()`).
  - Refactored `use(Player player)` to apply effects based on `EventEffectType`:
    - Consumables apply instant permanent recovery (`HEALTH`, `EXPERIENCE`, `MOVE`).
    - Equipment applies active turn buffs (`ATTACK`, `DEFENSE`) without permanently inflating base stats.

### 2. Inventory Slot Management
- **`items/Inventory.java`**:
  - Configured a fixed default capacity of 4 non-stacking slots (`List<Item>`).
  - Added capacity validation: `addItem(Item item)` prevents pickups when full and provides `[INVENTORY FULL]` feedback.
  - Implemented `useItem(int index, Player player)`: invokes `item.use(player)` and automatically removes spent consumables or broken equipment (`items.remove(index)`).
  - Implemented `displayInventory()`: prints formatted slot indices (1–4), item names, durability ratios (`x/y`), and effect descriptions.

### 3. Player Entity Stat Architecture
- **`entities/Player.java`**:
  - Initialized a 4-slot `Inventory` in the player constructor (`new Inventory(4)`).
  - Added transient turn modifiers (`tempAttack`, `tempDefense`) alongside base attributes.
  - Overrode `getAttack()` and `getDefense()` to dynamically return `base + temp`, ensuring combat calculations in `BattleSystem` calculate boosted stats seamlessly.
  - Implemented `clearTemporaryBuffs()` to reset transient modifiers back to base stats at the conclusion of each turn with `[BUFF EXPIRED]` logging.
  - Updated `levelUp()` to increment base attributes (`getBaseAttack() + 2`, `getBaseDefense() + 1`) to prevent temporary buffs from being baked into permanent leveling math.

### 4. Turn Action Economy & Console Integration
- **`ConsoleTestGame.java`**:
  - Added turn action options before movement dice are rolled:
    - `[1] Roll Movement Dice (D6)`
    - `[2] Use Item from Inventory`
    - `[3] View Inventory / Equipment`
  - Added a `hasUsedItemThisTurn` action economy flag, limiting players to one item activation per turn.
  - Updated `nextTurn()` to clear active player buffs via `currentPlayer.clearTemporaryBuffs()` and reset the `hasUsedItemThisTurn` flag.
  - Equipped players with starter kits (`Minor Health Potion`, `Wooden Shield`).
- **`board/SnakeTile.java`**:
  - Added victory loot drops: slaying a serpent in battle grants a random item reward (`player.addItem(Item.createRandomLoot())`).

### 5. Automated Verification Suite
- **`items/InventoryTest.java`**:
  - Created a headless verification suite executed via Gradle (`.\gradlew :core:testInventory`).
  - Validated consumable consumption, equipment durability decay across multiple turns, inventory full rejection, shattered item removal, temporary buff application, and turn expiration.

---

## Challenges & Blockers Resolved

- **Stat Snowballing vs. Equipment Value (Option A vs. Permanent Stacking):**
  - *Challenge:* Allowing equipment to grant permanent stats per durability use caused rapid stat inflation. A player using a 3-durability shield would permanently gain +9 Defense, making them completely immune to early-and-mid board serpent damage (`attack - defense <= 0`). It also encouraged players to spam all items immediately on safe tiles rather than saving them for danger.
  - *Resolution:* Implemented Option A (Temporary Turn Buffs). Equipment charges now activate temporary stances (`[BUFF: ACTIVE GUARD]`, `[BUFF: BATTLE FOCUS]`) that last for the active turn and expire when the turn passes. This keeps baseline stats balanced while rewarding tactical item usage right before entering hazardous tiles.

- **Slot Index Sliding on Item Removal:**
  - *Challenge:* Removing an item when durability hits 0 or when a potion is consumed shifts all subsequent list indices left by one, which could cause out-of-bounds errors if index handling is improper.
  - *Resolution:* Encapsulated slot removal inside `Inventory.useItem(int index, Player player)`. Removal is evaluated immediately after `item.use(player)` checks `item.isBroken()`, and slot choices are re-validated against current `items.size()` on every turn.

- **Level-Up Stat Protection:**
  - *Challenge:* If `levelUp()` increased `getAttack()` while a temporary sword buff was active, the temporary buff would be permanently added to the player's base attack.
  - *Resolution:* Explicitly referenced `getBaseAttack()` and `getBaseDefense()` inside `levelUp()`, completely isolating permanent growth from temporary gear enhancements.

---

## Verification & Test Results

Execution of `.\gradlew :core:testInventory` confirmed 100% test passage:

```text
=== RUNNING INVENTORY BACKEND TESTS (OPTION A: TEMPORARY BUFFS) ===

[Test 1] Adding Consumable...
  [ACQUIRED] Added Minor Health Potion to inventory.

[Test 2] Adding Equipment with 2 Durability...
  [ACQUIRED] Added Wooden Shield to inventory.

[Test 3] Filling inventory to 4/4 slots...
  [ACQUIRED] Added Iron Sword to inventory.
  [ACQUIRED] Added Hermes Feather to inventory.

[Test 4] Attempting to add 5th item when full...
  [INVENTORY FULL] Cannot carry Extra Potion (Capacity: 4)

=== INVENTORY (4/4 SLOTS) ===
  [1] Minor Health Potion - Restores 25 HP
  [2] Wooden Shield (Durability: 2/2) - +3 DEF (1 Turn Buff)
  [3] Iron Sword (Durability: 3/3) - +5 ATK (1 Turn Buff)
  [4] Hermes Feather - Move 2 tiles

[Test 5] Using consumable potion from Slot 1...
> Hero uses Minor Health Potion...
  [HP] +25 (Current: 75)
  [CONSUMED] Minor Health Potion has been used up.

[Test 6] Testing Equipment Temporary Buff & Durability Degradation...
> Hero uses Wooden Shield...
  [BUFF: ACTIVE GUARD] +3 Defense for this turn (Total DEF: 13 [Base 10 + Buff 3])
  [DURABILITY] Wooden Shield remaining uses: 1/2

[Test 7] Ending Turn and Clearing Buffs...
  [BUFF EXPIRED] Turn bonuses worn off. Attack: 10, Defense: 10

[Test 8] Second use of Shield (Durability 1 -> 0, Shatter)...
> Hero uses Wooden Shield...
  [BUFF: ACTIVE GUARD] +3 Defense for this turn (Total DEF: 13 [Base 10 + Buff 3])
  [BROKEN] Wooden Shield ran out of durability and shattered!
  [BUFF EXPIRED] Turn bonuses worn off. Attack: 10, Defense: 10

=== INVENTORY (2/4 SLOTS) ===
  [1] Iron Sword (Durability: 3/3) - +5 ATK (1 Turn Buff)
  [2] Hermes Feather - Move 2 tiles

[Test 9] Testing Weapon Battle Focus (Attack Buff)...
> Hero uses Iron Sword...
  [BUFF: BATTLE FOCUS] +5 Attack for this turn (Total ATK: 15 [Base 10 + Buff 5])
  [DURABILITY] Iron Sword remaining uses: 2/3
  [BUFF EXPIRED] Turn bonuses worn off. Attack: 10, Defense: 10

=== ALL INVENTORY BACKEND TESTS PASSED PERFECTLY! ===
```

---

## Next Steps
- Bridge `Inventory` and item rewards into `EventManager` procedural encounters (e.g., finding treasure chests in shrines or trading with wandering merchants).
- Translate the console inventory menu into LibGDX Scene2D graphical UI dialogs for `BoardScreen`.
- Implement visual durability indicators and equipment icons for the graphical HUD.
