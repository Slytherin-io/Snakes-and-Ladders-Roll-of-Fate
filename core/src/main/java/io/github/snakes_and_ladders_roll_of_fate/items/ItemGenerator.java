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

        Item item;
        // 35% Board Utility Relics (High spatial agency on the board)
        if (roll < 35) {
            item = generateBoardUtilityItem(tier);
        } else if (roll < 55) {
            // 20% Double-Edged Artifacts (High reward paired with a tactical cost)
            item = generateDoubleEdgedItem(tier);
        } else {
            // 45% Combat & Healing Essentials (Single-use potions, persistent ATK/DEF relics, EXP tomes)
            item = generateCombatOrRecoveryItem(tier);
        }
        if (item != null) {
            item.setTier(tier);
        }
        return item;
    }

    /**
     * Generates a board-centric utility item (Dice control, Snake ward, Scout, Leap, Transposition).
     */
    public static Item generateBoardUtilityItem(int tier) {
        tier = Math.max(1, tier);
        int pick = random.nextInt(6);
        Item item;
        switch (pick) {
            case 0:
                item = Item.createConsumable(
                    "Loaded Golden Die",
                    EventEffectType.DICE_EXACT,
                    6,
                    "Pick your exact roll (1-6) on next turn"
                );
                break;
            case 1:
                item = Item.createConsumable(
                    "Snake Charmer's Flute",
                    EventEffectType.SNAKE_WARD,
                    1,
                    "Pacifies next Snake (skips fight & slide)"
                );
                break;
            case 2:
                item = Item.createConsumable(
                    "Heavy Lead Boots",
                    EventEffectType.DICE_CLAMP,
                    2,
                    "Caps your next dice roll at 2 max"
                );
                break;
            case 3: {
                int leap = Math.min(4, 2 + (tier / 2));
                item = Item.createConsumable(
                    "Vaulting Hook",
                    EventEffectType.MOVE,
                    leap,
                    "Leap forward " + leap + " tiles, skipping hazards"
                );
                break;
            }
            case 4: {
                int range = Math.min(8, 4 + tier);
                item = Item.createConsumable(
                    "Eye of the Pathfinder",
                    EventEffectType.SCOUT,
                    range,
                    "Reveals " + range + " uncharted tiles ahead"
                );
                break;
            }
            case 5:
            default:
                item = Item.createConsumable(
                    "Transposition Orb",
                    EventEffectType.SWAP_PLAYER,
                    5,
                    "Swap with lead rival (or +5 tiles solo)"
                );
                break;
        }
        if (item != null) item.setTier(tier);
        return item;
    }

    /**
     * Generates a double-edged relic offering high value in exchange for HP or dice penalties.
     */
    public static Item generateDoubleEdgedItem(int tier) {
        tier = Math.max(1, tier);
        int pick = random.nextInt(3);
        Item item;
        switch (pick) {
            case 0: {
                int atk = 7 + (tier * 2);
                item = Item.createDoubleEdged(
                    "Berserker's Blood Draught",
                    EventEffectType.ATTACK,
                    atk,
                    EventEffectType.HEALTH,
                    -15,
                    "+" + atk + " ATK (Next Battle), costs 15 HP"
                );
                break;
            }
            case 1: {
                int exp = 45 + (tier * 20);
                item = Item.createDoubleEdged(
                    "Idol of Greed",
                    EventEffectType.EXPERIENCE,
                    exp,
                    EventEffectType.DICE_CLAMP,
                    3,
                    "+" + exp + " EXP, caps next roll at 3"
                );
                break;
            }
            case 2:
            default: {
                int leap = Math.min(6, 4 + (tier / 2));
                item = Item.createDoubleEdged(
                    "Blood-Pact Stride",
                    EventEffectType.MOVE,
                    leap,
                    EventEffectType.HEALTH,
                    -12,
                    "Leap +" + leap + " tiles, costs 12 HP"
                );
                break;
            }
        }
        if (item != null) item.setTier(tier);
        return item;
    }

    /**
     * Generates single-use healing elixirs, persistent combat attack/defense relics, or EXP scrolls.
     */
    public static Item generateCombatOrRecoveryItem(int tier) {
        tier = Math.max(1, tier);
        int pick = random.nextInt(4);
        Item item;
        switch (pick) {
            case 0: {
                int heal = 20 + (tier * 12) + random.nextInt(11);
                String prefix = POTION_PREFIXES[Math.min(POTION_PREFIXES.length - 1, tier - 1)];
                item = Item.createConsumable(
                    prefix + " Health Elixir",
                    EventEffectType.HEALTH,
                    heal,
                    "Restores " + heal + " HP"
                );
                break;
            }
            case 1: {
                int atk = 3 + (tier * 2) + random.nextInt(2);
                String name = GEAR_PREFIXES[random.nextInt(GEAR_PREFIXES.length)] + " "
                    + WEAPON_BASES[random.nextInt(WEAPON_BASES.length)];
                item = Item.createEquipment(
                    name,
                    EventEffectType.ATTACK,
                    atk,
                    1, 1,
                    "+" + atk + " ATK (Ready for next battle)"
                );
                break;
            }
            case 2: {
                int def = 3 + (tier * 2) + random.nextInt(2);
                String name = GEAR_PREFIXES[random.nextInt(GEAR_PREFIXES.length)] + " "
                    + SHIELD_BASES[random.nextInt(SHIELD_BASES.length)];
                item = Item.createEquipment(
                    name,
                    EventEffectType.DEFENSE,
                    def,
                    1, 1,
                    "+" + def + " DEF (Ready for battle & ward)"
                );
                break;
            }
            case 3:
            default: {
                int exp = 25 + (tier * 15) + random.nextInt(15);
                item = Item.createConsumable(
                    "Ancient Wisdom Scroll",
                    EventEffectType.EXPERIENCE,
                    exp,
                    "Grants +" + exp + " EXP"
                );
                break;
            }
        }
        if (item != null) item.setTier(tier);
        return item;
    }

    /**
     * Generates an equal-level item for barter trade based on the offered item's tier.
     * Guarantees fair, equal-value exchange with the wandering merchant.
     * @param tier Quality tier of the offered item (1, 2, or 3).
     * @param category Desired merchandise category.
     * @return Equivalent tier Item.
     */
    public static Item generateEqualBarterReward(int tier, BarterCategory category) {
        tier = Math.max(1, Math.min(3, tier));
        if (category == null) category = BarterCategory.ELIXIR;

        Item reward;
        switch (category) {
            case ELIXIR:
                if (tier == 1) {
                    reward = Item.createConsumable("Minor Health Potion", EventEffectType.HEALTH, 30, "Restores 30 HP");
                } else if (tier == 2) {
                    reward = Item.createConsumable("Greater Health Elixir", EventEffectType.HEALTH, 55, "Restores 55 HP");
                } else {
                    reward = Item.createConsumable("Celestial Ambrosia", EventEffectType.HEALTH, 85, "Restores 85 HP");
                }
                break;
            case ARMAMENT:
                if (tier == 1) {
                    reward = Item.createEquipment("Bronze Dagger", EventEffectType.ATTACK, 3, 1, 1, "+3 ATK (Ready for next battle)");
                } else if (tier == 2) {
                    reward = Item.createEquipment("Tempered Steel Sword", EventEffectType.ATTACK, 5, 1, 1, "+5 ATK (Ready for next battle)");
                } else {
                    reward = Item.createEquipment("Dragon-Slayer Blade", EventEffectType.ATTACK, 8, 1, 1, "+8 ATK (Ready for next battle)");
                }
                break;
            case RELIC:
            default:
                if (tier == 1) {
                    reward = Item.createConsumable("Heavy Lead Boots", EventEffectType.DICE_CLAMP, 2, "Caps next dice roll at 2 max");
                } else if (tier == 2) {
                    reward = Item.createConsumable("Snake Charmer's Flute", EventEffectType.SNAKE_WARD, 1, "Pacifies next Snake encounter");
                } else {
                    reward = Item.createConsumable("Loaded Golden Die", EventEffectType.DICE_EXACT, 6, "Pick exact roll (1-6) next turn");
                }
                break;
        }
        reward.setTier(tier);
        return reward;
    }

    /**
     * Balanced, modest cleansing reward when a player trades a Curse burden to a merchant.
     */
    public static Item generateCurseCleansingReward() {
        Item reward = Item.createConsumable("Restorative Herbal Salve", EventEffectType.HEALTH, 18, "Restores 18 HP & Purifies Satchel");
        reward.setTier(1);
        return reward;
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
