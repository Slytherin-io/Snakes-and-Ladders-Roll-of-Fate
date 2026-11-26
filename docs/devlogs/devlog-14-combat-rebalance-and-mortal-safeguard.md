# Devlog 14: Combat Rebalance and Mortal Retreat Safeguard

* **Date:** November 20, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Encounters: Combat Tuning & Survival Mechanics
* **Branch:** `feat/combat-rebalance`

---

## Objectives
- Eliminate lethal one-hit combat strikes by serpents on higher board tiles.
- Overhaul Snake combat statistics with tiered Act scaling (Act I, Act II, Act III).
- Bounded damage variance curves in the BattleSystem so character attributes and equipment drive combat results rather than wide 1–20 dice swings.
- Implement a "Mortal Retreat" safeguard so players above the retreat threshold are thrown down the slope clinging to life rather than facing instant permadeath.
- Integrate proportional experience rewards using incremental leveling methods.

---

## Work Completed

### 1. Tiered Enemy Scaling (Act-Based)
- **`board/SnakeTile.java`**:
  - Replaced the placeholder linear scaling (`snake.setAttack(getPosition() + 1)`) with smooth progression formulas:
    $$\text{Snake ATK} = 6 + \left\lfloor \frac{\text{tilePosition}}{7} \right\rfloor \qquad \text{Snake HP} = 15 + \left\lfloor \frac{\text{tilePosition}}{2} \right\rfloor$$
  - Added public getters `getSnakeAttack()` and `getSnakeHealth()` to enable direct inspection and clean UI rendering.
  - Progression Brackets:
    - **Act I (Tiles 1–33):** ATK 6–10, HP 15–31 (Deals 8–16 damage against starting player defenses).
    - **Act II (Tiles 34–66):** ATK 10–15, HP 32–48 (Deals 14–22 damage against mid-game defenses).
    - **Act III (Tiles 67–100):** ATK 15–20, HP 48–65 (Deals 18–28 damage against summit gear).

### 2. BattleSystem Bounded Damage Curves
- **`systems/BattleSystem.java`**:
  - Replaced raw flat $+d20$ damage variance with a normalized bonus:
    $$\text{rollBonus} = \left\lfloor \frac{\text{roll} - 1}{4} \right\rfloor \quad (+0 \text{ to } +4 \text{ bonus on rolls } 2\text{--}19)$$
  - Weapons (e.g. Iron Sword +4 ATK) and armor (Wooden Shield +3 DEF) now provide distinct, tangible tactical advantages rather than being eclipsed by pure dice randomness.
  - Critical hits (natural 20) deliver $\max(2, \text{baseDamage} \times 2 + 5)$.
  - Critical fumbles (natural 1) deliver $0$ damage.

### 3. Survival Instinct & Mortal Retreat Safeguard
- **`board/SnakeTile.java`**:
  - Refactored player health resolution in `startCombat()`:
    1. **Standard Retreat:** If incoming damage reduces player health to $\le 25\text{ HP}$, combat halts and the player slides down to the destination tile.
    2. **Mortal Strike Safeguard:** If a player enters combat above the retreat threshold ($>25\text{ HP}$) and suffers an incoming lethal blow ($\le 0\text{ HP}$), health is clamped to $1\text{ HP}$ (`[SURVIVAL INSTINCT] The massive blow hurls you backwards off the ledge!`) and the player slides down to safety.
    3. **True Lethality:** If a player chooses to engage while already critically injured ($\le 25\text{ HP}$), taking lethal damage results in a legitimate Game Over.
  - Rewired serpent defeat rewards to call `player.gainExperience(15 + pos/2)` instead of directly overwriting total EXP.

### 4. Automated Verification Suite
- **`items/InventoryTest.java`**:
  - Added **Test 18** validating:
    - Act I (Tile 14: 8 ATK, 22 HP), Act II (Tile 49: 13 ATK, 39 HP), and Act III (Tile 98: 20 ATK, 64 HP) stat scaling.
    - Damage formula bounding across 50 simulated combat rolls.
    - Standard $\le 25\text{ HP}$ retreat.
    - Mortal strike safeguard clamping to $1\text{ HP}$ and sliding down.
    - Legitimate death when entering combat with $\le 25\text{ HP}$.
  - Executed Gradle test suite (`.\gradlew :core:testInventory`) with 18/18 tests passing cleanly.

---

## Challenges & Solutions

- **Instant Permadeath vs. Retreat Mechanics:** The earlier combat loop evaluated `newPlayerHealth <= 0` prior to checking the retreat threshold (`player.getHealth() <= 25`). Because late-game snakes dealt upwards of 80–100 damage in a single strike, players jumped directly from high health to zero, bypassing the retreat fallback entirely. Resolved by decoupling lethal resolution into two distinct paths: players with $>25\text{ HP}$ trigger the Mortal Retreat Safeguard, while players entering $\le 25\text{ HP}$ face true mortality.
- **RNG Variance Overpowering Player Gear:** When a d20 roll added flat values from 1 to 20 directly to damage, equipment buffs (+3 DEF or +4 ATK) felt negligible. Normalizing the dice variance to a bounded $+0$ to $+4$ curve restored weapon and armor balance.

---

## Next Steps
- Implement the Scene2D Event Modal Dialog overlay in `BoardScreen` to replace console inputs with clickable choice buttons.
- Wire the Event Modal choice buttons to trigger `BattleScreen` on combat engagement or trigger board animations on deflection/retreat.
