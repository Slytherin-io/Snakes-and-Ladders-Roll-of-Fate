package io.github.snakes_and_ladders_roll_of_fate.items;

import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import java.util.Random;

/**
 * Generates single-use tactical relics, board-control utilities, double-edged artifacts, and passive curses.
 */
public class ItemGenerator {
    private static final Random random = new Random();

    // --- Name Generation Pools ---
    private static final String[] POTION_PREFIXES = { "Minor", "Greater", "Radiant", "Blessed", "Alchemical" };
    private static final String[] GEAR_PREFIXES = { "Iron", "Crystal", "Blessed", "Tempered", "Ancient", "Sturdy" };
    private static final String[] WEAPON_BASES = { "Blade", "Fang", "Dagger", "Edge", "Talon" };
    private static final String[] SHIELD_BASES = { "Buckler", "Aegis", "Ward", "Shield", "Talisman" };

    /**
     * Generates a random single-use beneficial or double-edged item scaled to the given tier.
     * @param tier Scaling tier (e.g. Player Level or Act/Tile tier).
     * @return A single-use Item.
     */
    public static Item generateItem(int tier) {
        tier = Math.max(1, tier);
        int roll = random.nextInt(100);

        // 35% Board Utility Relics (High spatial agency on the board)
        if (roll < 35) {
            return generateBoardUtilityItem(tier);
        }
        // 20% Double-Edged Artifacts (High reward paired with a tactical cost)
        if (roll < 55) {
            return generateDoubleEdgedItem(tier);
        }
        // 45% Combat & Healing Essentials (Single-use potions, 1-turn ATK/DEF relics, EXP tomes)
        return generateCombatOrRecoveryItem(tier);
    }

    /**
     * Generates a board-centric utility item (Dice control, Snake ward, Scout, Leap, Transposition).
     */
    public static Item generateBoardUtilityItem(int tier) {
        int pick = random.nextInt(6);
        switch (pick) {
            case 0:
                return Item.createConsumable(
                    "Loaded Golden Die",
                    EventEffectType.DICE_EXACT,
                    6,
                    "Pick your exact roll (1-6) on next turn"
                );
            case 1:
                return Item.createConsumable(
                    "Snake Charmer's Flute",
                    EventEffectType.SNAKE_WARD,
                    1,
                    "Pacifies next Snake (skips fight & slide)"
                );
            case 2:
                return Item.createConsumable(
                    "Heavy Lead Boots",
                    EventEffectType.DICE_CLAMP,
                    2,
                    "Caps your next dice roll at 2 max"
                );
            case 3: {
                int leap = Math.min(4, 2 + (tier / 2));
                return Item.createConsumable(
                    "Vaulting Hook",
                    EventEffectType.MOVE,
                    leap,
                    "Leap forward " + leap + " tiles, skipping hazards"
                );
            }
            case 4: {
                int range = Math.min(8, 4 + tier);
                return Item.createConsumable(
                    "Eye of the Pathfinder",
                    EventEffectType.SCOUT,
                    range,
                    "Reveals " + range + " uncharted tiles ahead"
                );
            }
            case 5:
            default:
                return Item.createConsumable(
                    "Transposition Orb",
                    EventEffectType.SWAP_PLAYER,
                    5,
                    "Swap with lead rival (or +5 tiles solo)"
                );
        }
    }

    /**
     * Generates a double-edged relic offering high value in exchange for HP or dice penalties.
     */
    public static Item generateDoubleEdgedItem(int tier) {
        int pick = random.nextInt(3);
        switch (pick) {
            case 0: {
                int atk = 7 + (tier * 2);
                return Item.createDoubleEdged(
                    "Berserker's Blood Draught",
                    EventEffectType.ATTACK,
                    atk,
                    EventEffectType.HEALTH,
                    -15,
                    "+" + atk + " ATK (1 Turn), costs 15 HP"
                );
            }
            case 1: {
                int exp = 45 + (tier * 20);
                return Item.createDoubleEdged(
                    "Idol of Greed",
                    EventEffectType.EXPERIENCE,
                    exp,
                    EventEffectType.DICE_CLAMP,
                    3,
                    "+" + exp + " EXP, caps next roll at 3"
                );
            }
            case 2:
            default: {
                int leap = Math.min(6, 4 + (tier / 2));
                return Item.createDoubleEdged(
                    "Blood-Pact Stride",
                    EventEffectType.MOVE,
                    leap,
                    EventEffectType.HEALTH,
                    -12,
                    "Leap +" + leap + " tiles, costs 12 HP"
                );
            }
        }
    }

    /**
     * Generates single-use healing elixirs, 1-use combat attack/defense relics, or EXP scrolls.
     */
    public static Item generateCombatOrRecoveryItem(int tier) {
        int pick = random.nextInt(4);
        switch (pick) {
            case 0: {
                int heal = 20 + (tier * 12) + random.nextInt(11);
                String prefix = POTION_PREFIXES[Math.min(POTION_PREFIXES.length - 1, tier - 1)];
                return Item.createConsumable(
                    prefix + " Health Elixir",
                    EventEffectType.HEALTH,
                    heal,
                    "Restores " + heal + " HP"
                );
            }
            case 1: {
                int atk = 3 + (tier * 2) + random.nextInt(2);
                String name = GEAR_PREFIXES[random.nextInt(GEAR_PREFIXES.length)] + " "
                    + WEAPON_BASES[random.nextInt(WEAPON_BASES.length)];
                return Item.createEquipment(
                    name,
                    EventEffectType.ATTACK,
                    atk,
                    1, 1,
                    "+" + atk + " ATK (1 Turn / 1 Use)"
                );
            }
            case 2: {
                int def = 3 + (tier * 2) + random.nextInt(2);
                String name = GEAR_PREFIXES[random.nextInt(GEAR_PREFIXES.length)] + " "
                    + SHIELD_BASES[random.nextInt(SHIELD_BASES.length)];
                return Item.createEquipment(
                    name,
                    EventEffectType.DEFENSE,
                    def,
                    1, 1,
                    "+" + def + " DEF (1 Turn / Deflect)"
                );
            }
            case 3:
            default: {
                int exp = 25 + (tier * 15) + random.nextInt(15);
                return Item.createConsumable(
                    "Ancient Wisdom Scroll",
                    EventEffectType.EXPERIENCE,
                    exp,
                    "Grants +" + exp + " EXP"
                );
            }
        }
    }

    /**
     * Generates a passive Curse item that clogs a satchel slot and penalizes the player
     * until cleansed at a Sanctuary (Tiles 30 & 70) or bartered away to a merchant.
     */
    public static Item generateCurse() {
        int pick = random.nextInt(3);
        switch (pick) {
            case 0:
                return Item.createCurse(
                    "Cursed Obsidian Slab",
                    EventEffectType.MOVE,
                    -1,
                    "Passive: -1 Dice Roll (Cleanse/Barter)"
                );
            case 1:
                return Item.createCurse(
                    "Viper's Hex Totem",
                    EventEffectType.ATTACK,
                    -2,
                    "Passive: -2 ATK (Cleanse/Barter)"
                );
            case 2:
            default:
                return Item.createCurse(
                    "Rusted Iron Shackles",
                    EventEffectType.DEFENSE,
                    -2,
                    "Passive: -2 DEF (Cleanse/Barter)"
                );
        }
    }
}
