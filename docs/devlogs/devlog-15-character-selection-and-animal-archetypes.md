# Devlog 15: Character Selection and Animal Archetypes

* **Date:** November 21, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Pre-Board Generation: Character Selection & Animal Archetypes
* **Branch:** `feat/character-selection`

---

## Objectives
- Introduce a pre-board character selection system allowing players to select their playable animal champion prior to embarking onto the board.
- Establish four playable animal archetypes: **Cat**, **Turtle**, **Fox**, and **Raccoon**, each with distinct base attribute distributions, thematic roles, and starting gear.
- Integrate interactive character selection into the console gameplay simulation (`ConsoleTestGame`).
- Verify character class initialization, stat assignments, and starting inventory loadouts via automated integration tests.
- Maintain clean separation between backend mechanics and graphical screens, preparing the data model for the eventual unified GUI migration phase.

---

## Work Completed

### 1. Playable Animal Archetypes (`CharacterClass.java`)
- **`entities/CharacterClass.java`**:
  - Implemented the enum defining four distinct animal wanderers:
    1. **The Cat (Nimble Prowler):**
       - Base Stats: HP 90 | ATK 6 | DEF 4
       - Trait: Feline Agility (High speed and quick strikes in mortal peril).
       - Starting Asset: *Cat's Agility Charm* (Consumable: Advances 2 tiles forward).
    2. **The Turtle (Iron Guardian):**
       - Base Stats: HP 120 | ATK 4 | DEF 7
       - Trait: Iron Shell (Heavy armor and natural resilience against hazards and serpent strikes).
       - Starting Asset: *Turtle Shell Buckler* (Equipment: +3 DEF for 1 turn, 3 uses).
    3. **The Fox (Cunning Trickster):**
       - Base Stats: HP 95 | ATK 7 | DEF 3
       - Trait: Silver Tongue (Sharp offensive instincts and keen survival senses).
       - Starting Asset: *Fox's Hunting Fang* (Equipment: +3 ATK for 1 turn, 3 uses).
    4. **The Raccoon (Crafty Scavenger):**
       - Base Stats: HP 100 | ATK 5 | DEF 5
       - Trait: Scavenger's Instinct (Resourceful survivor adept at finding remedies and salvaging supplies).
       - Starting Asset: *Scavenger's First Aid Kit* (Consumable: Restores 25 HP).
  - Added factory helper `getStartingItem()` to populate character loadouts on spawn.

### 2. Player Entity Integration
- **`entities/Player.java`**:
  - Added `characterClass` field and constructor `Player(String name, CharacterClass characterClass)`.
  - Implemented `applyCharacterClass(CharacterClass characterClass)`: sets `entityClass`, assigns archetype HP/ATK/DEF stats, and automatically inserts their signature starting item into slot 1 of their 4-slot inventory bag.

### 3. Console Gameplay Simulation Integration
- **`ConsoleTestGame.java`**:
  - Added an interactive character selection menu during `createPlayer()`, allowing players to inspect all 4 animal classes and pick their champion before starting the board run:
    - Lists all available animal champions with their title, base HP, base ATK, base DEF, and starting gear.
    - Validates input (1–4) and automatically instantiates the chosen archetype with its corresponding stats and starting inventory item.

### 4. Automated Verification Suite
- **`items/InventoryTest.java`**:
  - Added **Test 19** verifying:
    - 4 playable animal classes are present.
    - Stat assignments for Turtle (120 HP, 4 ATK, 7 DEF), Cat (90 HP, 6 ATK, 4 DEF), Fox (95 HP, 7 ATK, 3 DEF), and Raccoon (100 HP, 5 ATK, 5 DEF).
    - Starting inventory gear generation (Buckler, Agility Charm, Hunting Fang, First Aid Kit).
    - Proper equipment activation and turn defense buff expiration for class gear.
  - 19/19 automated integration tests passing cleanly.

---

## Challenges & Solutions

- **Cosmetic Skins vs. Meaningful Gameplay Choices:** Making characters purely visual risks player disinterest. Giving each animal distinct base stats and a signature starting item immediately provides asymmetric advantages (e.g., Turtle can immediately block early hazards, Cat has movement agility, Fox has martial attack buffs, and Raccoon provides essential field healing).
- **Phased Implementation Discipline:** In alignment with project strategy, all character selection mechanics and archetype configurations are fully implemented and verified in the backend and console simulation first, deferring GUI screens and Scene2D layouts until all core systems are complete.

---

## Next Steps
- Validate console character selection flow through manual test runs.
- Prepare the architecture for the comprehensive GUI migration pass (transferring Board, Character Selection, Combat, and Inventory dialogs to LibGDX Scene2D).
