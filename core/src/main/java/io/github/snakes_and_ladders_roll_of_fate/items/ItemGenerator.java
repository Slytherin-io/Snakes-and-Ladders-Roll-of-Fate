package io.github.snakes_and_ladders_roll_of_fate.items;

import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import java.util.Random;

public class ItemGenerator {
    private static final Random random = new Random();

    // --- Name Generation Pools ---
    private static final String[] PREFIXES = {
        "Minor", "Greater", "Ancient", "Rusty", "Blessed", "Cursed",
        "Iron", "Crystal", "Shadow", "Radiant", "Swift", "Sturdy"
    };

    private static final String[] CONSUMABLE_BASES = {
        "Potion", "Elixir", "Draught", "Tonic", "Scroll", "Feather", "Tome", "Torch", "Lens"
    };

    private static final String[] EQUIPMENT_BASES = {
        "Sword", "Shield", "Dagger", "Amulet", "Ring", "Gauntlets", "Boots", "Blade"
    };

    /**
     * Generates a random item scaled to a specific tier.
     * @param tier The tier of the item (e.g., Player Level, or Board Tile / 10).
     * @return A procedurally generated Item.
     */
    public static Item generateItem(int tier) {
        // Ensure tier is at least 1 to prevent division/zero issues
        tier = Math.max(1, tier);

        // 1. Determine if it's a Consumable or Equipment (50/50 chance)
        boolean isConsumable = random.nextBoolean();
        ItemType type = isConsumable ? ItemType.CONSUMABLE : ItemType.EQUIPMENT;

        // 2. Determine the Effect Type randomly
        EventEffectType[] effects = EventEffectType.values();
        EventEffectType effectType = effects[random.nextInt(effects.length)];

        // 3. Generate Name
        String name = generateName(type);

        // 4. Calculate Effect Value (Scales dynamically with tier)
        int effectValue = calculateEffectValue(effectType, tier);

        // 5. Generate Description
        String effectDesc = generateDescription(effectType, effectValue, type);

        // 6. Create and return the item using your existing factory methods
        if (isConsumable) {
            return Item.createConsumable(name, effectType, effectValue, effectDesc);
        } else {
            // Equipment durability scales slightly with tier
            int minDur = 2 + (tier / 2);
            int maxDur = 4 + tier;
            return Item.createEquipment(name, effectType, effectValue, minDur, maxDur, effectDesc);
        }
    }

    private static String generateName(ItemType type) {
        String prefix = PREFIXES[random.nextInt(PREFIXES.length)];
        String base = (type == ItemType.CONSUMABLE)
            ? CONSUMABLE_BASES[random.nextInt(CONSUMABLE_BASES.length)]
            : EQUIPMENT_BASES[random.nextInt(EQUIPMENT_BASES.length)];

        return prefix + " " + base;
    }

    private static int calculateEffectValue(EventEffectType effect, int tier) {
        int baseValue;
        int variance;

        // Scale base values depending on the effect type
        switch (effect) {
            case HEALTH:
                baseValue = 15 + (tier * 10); // e.g., Tier 1 = 25 HP, Tier 5 = 65 HP
                variance = 10;
                break;
            case ATTACK:
            case DEFENSE:
                baseValue = 2 + (tier * 2);   // e.g., Tier 1 = 4 ATK/DEF, Tier 5 = 12 ATK/DEF
                variance = 2;
                break;
            case MOVE:
                baseValue = 2 + (tier / 2);   // e.g., Tier 1 = 2 tiles, Tier 5 = 4 tiles
                variance = 1;
                break;
            case SCOUT:
                baseValue = 3 + (tier / 2);   // e.g., Tier 1 = 3-4 tiles, Tier 4 = 5 tiles
                variance = 1;
                break;
            case EXPERIENCE:
                baseValue = 20 + (tier * 15); // e.g., Tier 1 = 35 EXP, Tier 5 = 95 EXP
                variance = 15;
                break;
            default:
                baseValue = 10;
                variance = 5;
        }

        // Apply random variance (+/-)
        int randomSpread = random.nextInt(variance * 2 + 1) - variance;
        return Math.max(1, baseValue + randomSpread);
    }

    private static String generateDescription(EventEffectType effect, int value, ItemType type) {
        switch (effect) {
            case HEALTH:
                return "Restores " + value + " HP";
            case ATTACK:
                return type == ItemType.EQUIPMENT ? "+" + value + " ATK (1 Turn Buff)" : "Grants +" + value + " ATK";
            case DEFENSE:
                return type == ItemType.EQUIPMENT ? "+" + value + " DEF (1 Turn Buff)" : "Grants +" + value + " DEF";
            case MOVE:
                return "Advances " + value + " tiles";
            case SCOUT:
                return "Scouts and reveals " + value + " tiles ahead";
            case EXPERIENCE:
                return "Grants " + value + " EXP";
            default:
                return "Mystic Energy";
        }
    }
}
