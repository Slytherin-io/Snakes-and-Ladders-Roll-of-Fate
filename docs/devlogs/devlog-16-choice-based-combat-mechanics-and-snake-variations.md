# Devlog 16: Choice based combat mechanics and snake variations
* **Date** : November 22, 2026
* **Author**: John Emmanuel Q. Seda (Project Leader)
* **Sprint / Milestone** *: Week 6 – Combat Mechanics & Enemy Archetypes
* **Branch**: `feat/choice-based-combat`
---
## Objectives
- Transition the combat system from an automatic damage-exchange loop to an interactive, turn-based menu system.
- Implement core combat actions: Attack, Defend, Use Item, and Escape, driven by console input.
- Integrate the player's `Inventory` system into the combat flow, allowing the tactical use of consumables and equipment mid-battle.
- Introduce enemy variations for Snake tiles, utilizing the `Enemy` class attributes to create distinct combat encounters with unique stat distributions.
- Implement post-battle resolution logic, including experience gain, random loot drops, and survival/defeat state handling.

## Work Completed
### 1. Interactive Turn-Based Combat Loop (`BattleSystem.java`)
   - Replaced the passive damage calculation loop with an interactive `runBattle()` method driven by a `Scanner` for console input.
   - Implemented a 4-option action menu:
   - Attack: Uses the d20 `combatDice` to calculate damage, preserving Critical Hit (Rolled 20) and Critical Miss (Rolled 1) mechanics.
   - Defend: Applies a temporary +5 Defense buff for the current turn, mitigating incoming enemy damage before the buff is cleared.
   - Use Item: Opens the inventory menu, allowing players to consume potions or equip gear mid-fight.
   - Escape: Rolls a d20 to determine if the player can flee (requires a 10 or higher). Success ends the battle; failure results in a lost turn and enemy retaliation.
   - Added the "Mortal Strike Safeguard": If a player takes a lethal blow while above 25 HP, they survive with 1 HP and are forced to slide down the snake, preserving the risk/reward tension of the board.

### 2. Inventory Integration in Combat (`BattleSystem.java` & `Inventory.java`)
   - Seamlessly routed combat item usage through `player.getInventory().displayInventory()` and `player.useItem(index)`.
   - Ensured that items with limited durability (like the Fox's Hunting Fang or Turtle Shell Buckler) degrade properly when used in battle and are automatically removed from the inventory when broken or fully consumed.

### 3. Enemy Variations and Archetypes (`SnakeTile.java` & `Enemy.java`)
   - Replaced the generic "SERPENT" with a `createSnakeVariation()` factory method that spawns one of three distinct snake archetypes based on a random roll:
     - Venomous Viper: Aggressive behavior (High ATK, Low HP).
     - Armored Python: Defensive behavior (Low ATK, High HP).
     - Shadow Asp: Balanced/Stealthy behavior.
   - Scaled enemy stats dynamically based on the `SnakeTile`'s board position to ensure difficulty progression as the player climbs the board.

### 4. Post-Battle Resolution and Loot System (`SnakeTile.java` & `Item.java`)
   - Implemented a `BattleOutcome` enum (`VICTORY`, `DEFEAT`, `ESCAPED`, `WEAKENED`) to handle different exit states cleanly.
   - Victory: Awards scaling EXP and triggers `Item.createRandomLoot()` to drop a randomized consumable or equipment piece (e.g., Minor Health Potion, Iron Sword, Hermes Feather, Serpent Fang Dagger).
   - Escape/Weakness/Defeat: Triggers the snake's sliding effect (`applyEffect()`), pushing the player backward on the board, with specific narrative flavor text for each outcome.

## Challenges & Solutions
- Inventory State Management During Combat: Initially, attempting to pull a raw `List<Item>` from the Player class to handle combat item usage caused synchronization issues with item durability and removal.
Solution: Refactored the battle system to interact directly with the `Inventory` object via the `Player` wrapper (`player.useItem(slotIndex)`). This ensures the `Inventory` class handles its own state, durability reduction, and broken item cleanup natively without duplicating logic.

- Balancing the Escape Mechanic: A simple 50/50 escape chance felt too arbitrary and disconnected from the core dice-rolling theme of the game.
Solution: Tied the escape success to the existing d20 `combatDice`. Rolling a 10 or higher allows escape, maintaining consistency with the game's core RNG mechanics and punishing failed escapes with a lost turn and enemy retaliation.

## Next Steps
- Implement visual feedback, animations, and UI prompts for the combat menu in the LibGDX Scene2D GUI.
