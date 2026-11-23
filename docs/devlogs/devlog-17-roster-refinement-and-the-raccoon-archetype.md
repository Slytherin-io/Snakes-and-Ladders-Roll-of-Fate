# Devlog 17: Playable Roster Refinement and the Raccoon Archetype

* **Date:** November 23, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Character System: Playable Roster Refinement
* **Branch:** `feat/character-selection`

---

## Objectives
- Refine the 4-champion playable roster by replacing the Bird with the **Raccoon (Crafty Scavenger)** archetype.
- Establish a complete mechanical utility spread across the 4 champions covering all core survival systems: **Movement, Defense, Offense, and Healing**.
- Rebalance starting inventory loadouts so each character enters the board with an immediately useful, distinct archetype asset.
- Update the automated verification suite (Test 19) to assert Raccoon base attributes and starting gear.

---

## Work Completed

### 1. Design Pivot: From Aerial Flight to Resourceful Scavenging
- Following review of player mechanics and board interactions, the original **Bird** archetype (aerial mobility and ladder ascent) exhibited significant thematic overlap with existing ladder tiles and movement consumables.
- To provide deeper mechanical diversity, the Bird was replaced with the **Raccoon (Crafty Scavenger)**.
- The Raccoon directly enhances our survival item economy by introducing field recovery and first-aid capabilities to the roster.

### 2. Raccoon Champion Specifications (`CharacterClass.java`)
- **`entities/CharacterClass.java`**:
  - Defined the `RACCOON` enum entry:
    - **Class Name:** Raccoon
    - **Title / Role:** Crafty Scavenger
    - **Base Attributes:** **HP:** 100 | **ATK:** 5 | **DEF:** 5 (Balanced survivor baseline).
    - **Trait:** *Scavenger's Instinct* (Resourceful survivor adept at finding remedies and salvaging supplies).
    - **Starting Asset:** *Scavenger's First Aid Kit* (Consumable restoring +25 HP).
  - Updated `getStartingItem()` to instantiate and return the Raccoon's consumable first-aid kit upon player initialization.

### 3. Four-Pillar Archetype Matrix
The finalized 4-champion roster now provides full coverage across every primary game system:

| Champion | Archetype Role | Base Attributes | Thematic Trait | Starting Asset Type | Gameplay Benefit |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **The Cat** | Nimble Prowler | HP 90 \| ATK 6 \| DEF 4 | *Feline Agility* | Consumable (*Cat's Agility Charm*) | Mobility: Leap 2 tiles to bypass hazards |
| **The Turtle** | Iron Guardian | HP 120 \| ATK 4 \| DEF 7 | *Iron Shell* | Equipment (*Turtle Shell Buckler*) | Defense: +3 DEF buff to absorb hazard damage |
| **The Fox** | Cunning Trickster | HP 95 \| ATK 7 \| DEF 3 | *Silver Tongue* | Equipment (*Fox's Hunting Fang*) | Offense: +3 ATK buff for swift combat victories |
| **The Raccoon** | Crafty Scavenger | HP 100 \| ATK 5 \| DEF 5 | *Scavenger's Instinct* | Consumable (*Scavenger's First Aid Kit*) | Sustain: +25 HP recovery after traps or battles |

### 4. Automated Verification Suite
- **`items/InventoryTest.java`**:
  - Updated **Test 19** to assert the new Raccoon archetype:
    - Confirmed presence of 4 distinct classes (Cat, Turtle, Fox, Raccoon).
    - Validated Raccoon base stats (100 HP, 5 ATK, 5 DEF) and entity class naming ("Raccoon").
    - Verified automatic placement of *Scavenger's First Aid Kit* into inventory slot 1.
  - Executed Gradle test suite (`.\gradlew :core:testInventory`) with 19/19 tests passing cleanly.

### 5. Console Gameplay Simulation Integration
- **`ConsoleTestGame.java`**:
  - The character selection prompt dynamically reads from `CharacterClass.values()`, presenting players with **Cat**, **Turtle**, **Fox**, and **Raccoon** alongside their attribute ratings and starting gear during game startup.

---

## Challenges & Solutions

- **Avoiding Mechanical Redundancy:** Having both Cat and Bird specialize in movement resulted in overlapping playstyles. Replacing Bird with Raccoon established a dedicated healing/sustain role, ensuring every player has a distinct strategic niche from Turn 1.
- **Starting Inventory Parity:** The roster now offers an even split of 2 equipment champions (Turtle and Fox) who manage durability charges, and 2 consumable champions (Cat and Raccoon) with powerful one-time survival actions.

---

## Next Steps
- Validate the Raccoon's sustain and healing gameplay in extended console simulation playthroughs.
- Keep the `feat/character-selection` branch ready for merging into `dev` when appropriate.
- Prepare the architecture for the upcoming unified GUI migration pass.
