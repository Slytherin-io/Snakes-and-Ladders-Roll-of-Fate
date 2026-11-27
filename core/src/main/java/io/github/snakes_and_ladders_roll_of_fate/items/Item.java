package io.github.snakes_and_ladders_roll_of_fate.items;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;

/**
 * Represents a single-use item, tactical relic, double-edged artifact, or passive curse burden.
 * All usable items have a single charge (durability = 1) to keep inventory slots dynamic and balanced.
 */
public class Item {
    private String name;
    private int amount;
    private ItemType itemType;
    private int durability;
    private int maxDurability;
    private EventEffectType effectType;
    private int effectValue;
    private EventEffectType secondaryEffectType;
    private int secondaryEffectValue;
    private String effect;
    private int tier = 1;

    // Full constructor (all items strictly single-use: durability = 1)
    public Item(String name, ItemType itemType, int durability, EventEffectType effectType, int effectValue, String effect) {
        this.name = name;
        this.amount = 1;
        this.itemType = itemType;
        this.durability = 1;
        this.maxDurability = 1;
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.effect = effect;
    }

    // Backwards-compatible constructor
    public Item(String name, int amount, int durability, String effect) {
        this(name, ItemType.CONSUMABLE, 1, EventEffectType.HEALTH, 20, effect);
        this.amount = amount;
    }

    // Factory method for single-use Consumables / Board Utilities
    public static Item createConsumable(String name, EventEffectType effectType, int effectValue, String effect) {
        return new Item(name, ItemType.CONSUMABLE, 1, effectType, effectValue, effect);
    }

    // Factory method for single-use Equipment / Tactical Relics (usable for 1-turn buff or hazard deflection)
    public static Item createEquipment(String name, EventEffectType effectType, int effectValue, int minDurability, int maxDurability, String effect) {
        return new Item(name, ItemType.EQUIPMENT, 1, effectType, effectValue, effect);
    }

    // Factory method for Double-Edged Relics (powerful benefit paired with a cost/drawback)
    public static Item createDoubleEdged(String name, EventEffectType primaryType, int primaryVal,
                                         EventEffectType secondaryType, int secondaryVal, String effect) {
        Item item = new Item(name, ItemType.CONSUMABLE, 1, primaryType, primaryVal, effect);
        item.secondaryEffectType = secondaryType;
        item.secondaryEffectValue = secondaryVal;
        return item;
    }

    // Factory method for Passive Curse Burdens (clogs a satchel slot and applies a passive penalty until cleansed or bartered)
    public static Item createCurse(String name, EventEffectType penaltyType, int penaltyValue, String effect) {
        return new Item(name, ItemType.CURSE, 1, penaltyType, penaltyValue, effect);
    }

    // Factory method for generating random beneficial loot drops (Defaults to Tier 1)
    public static Item createRandomLoot() {
        return ItemGenerator.generateItem(1);
    }

    // Overloaded method to generate scaled loot based on a tier (e.g., Player Level or Board Position)
    public static Item createRandomLoot(int tier) {
        return ItemGenerator.generateItem(tier);
    }

    // Applies the item effect(s) to the player and consumes its single charge
    public void use(Player player) {
        if (itemType == ItemType.CURSE) {
            System.out.println("  [CURSED] " + name + " cannot be discarded! Cleanse it at a Sanctuary or Barter it away.");
            return;
        }

        if (effectType != null) {
            applySingleEffect(player, effectType, effectValue);
        }
        if (secondaryEffectType != null) {
            applySingleEffect(player, secondaryEffectType, secondaryEffectValue);
        }

        reduceDurability();

        if (durability <= 0) {
            if (itemType == ItemType.EQUIPMENT) {
                System.out.println("  [EXPENDED] " + name + " was expended in action.");
            } else {
                System.out.println("  [CONSUMED] " + name + " has been used up.");
            }
        }
    }

    private void applySingleEffect(Player player, EventEffectType type, int value) {
        switch (type) {
            case HEALTH:
                int newHealth = Math.min(player.getMaxHealth(), Math.max(0, player.getHealth() + value));
                player.setHealth(newHealth);
                System.out.printf("  [HP] %s%d (Current: %d/%d)%n", (value >= 0 ? "+" : ""), value, player.getHealth(), player.getMaxHealth());
                break;
            case ATTACK:
                if (player.isInBattle()) {
                    player.addBattleAttack(value);
                    System.out.printf("  [BUFF: BATTLE STRIKE] %s%d Attack for this battle (Total ATK: %d [Base %d + Buff %d])%n",
                        (value >= 0 ? "+" : ""), value, player.getAttack(), player.getBaseAttack(), player.getTempAttack());
                } else {
                    player.primeBattleAttack(value);
                    System.out.printf("  [BUFF: COMBAT BOON] %s%d Attack primed for next battle! (Total ATK: %d [Base %d + Buff %d])%n",
                        (value >= 0 ? "+" : ""), value, player.getAttack(), player.getBaseAttack(), player.getTempAttack());
                }
                break;
            case DEFENSE:
                if (player.isInBattle()) {
                    player.addBattleDefense(value);
                    System.out.printf("  [BUFF: BATTLE GUARD] %s%d Defense for this battle (Total DEF: %d [Base %d + Buff %d])%n",
                        (value >= 0 ? "+" : ""), value, player.getDefense(), player.getBaseDefense(), player.getTempDefense());
                } else {
                    player.primeBattleDefense(value);
                    System.out.printf("  [BUFF: ACTIVE WARD] %s%d Defense primed for next battle & hazard ward! (Total DEF: %d [Base %d + Buff %d])%n",
                        (value >= 0 ? "+" : ""), value, player.getDefense(), player.getBaseDefense(), player.getTempDefense());
                }
                break;
            case EXPERIENCE:
                player.gainExperience(value);
                System.out.printf("  [EXP] +%d (Current: %d)%n", value, player.getExperience());
                break;
            case MOVE:
                player.move(value);
                System.out.printf("  [Move] %s%d tiles (New Position: Tile %d)%n", (value >= 0 ? "+" : ""), value, player.getPosition());
                break;
            case SCOUT:
                System.out.printf("  [SCOUT] Reveals %d tiles ahead!%n", value);
                break;
            case DICE_EXACT:
                player.setPendingDiceChoice(true);
                System.out.println("  [DICE CONTROL] Choose your exact roll (1-6) on your next turn!");
                break;
            case DICE_CLAMP:
                player.setMaxDiceRollCap(value);
                System.out.printf("  [DICE CAP] Next dice roll is capped at %d max!%n", value);
                break;
            case SNAKE_WARD:
                player.setHasSnakeWard(true);
                System.out.println("  [SNAKE WARD] Next Serpent encounter will be pacified automatically!");
                break;
            case SWAP_PLAYER:
                System.out.println("  [TRANSPOSITION] Warping across the board!");
                break;
            default:
                break;
        }
    }

    public void reduceDurability() {
        if (this.durability > 0) {
            this.durability--;
        }
    }

    public boolean isBroken() {
        return this.durability <= 0;
    }

    public boolean isCurse() {
        return this.itemType == ItemType.CURSE;
    }

    public boolean isDoubleEdged() {
        return this.secondaryEffectType != null;
    }

    // Getters and Setters
    public String getName() { return name; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public ItemType getItemType() { return itemType; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public EventEffectType getEffectType() { return effectType; }
    public int getEffectValue() { return effectValue; }
    public EventEffectType getSecondaryEffectType() { return secondaryEffectType; }
    public int getSecondaryEffectValue() { return secondaryEffectValue; }
    public String getEffect() { return effect; }

    public int getTier() {
        if (itemType == ItemType.CURSE) return 0;
        if (tier > 1) return tier;
        return calculateInferredTier();
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public String getTierName() {
        int t = getTier();
        switch (t) {
            case 0: return "Curse";
            case 1: return "Tier I";
            case 2: return "Tier II";
            case 3: return "Tier III";
            default: return "Tier " + t;
        }
    }

    private int calculateInferredTier() {
        if (tier > 1) return tier;
        if (effectType == EventEffectType.HEALTH) {
            if (effectValue >= 70) return 3;
            if (effectValue >= 40) return 2;
            return 1;
        }
        if (effectType == EventEffectType.ATTACK || effectType == EventEffectType.DEFENSE) {
            if (effectValue >= 7) return 3;
            if (effectValue >= 4) return 2;
            return 1;
        }
        if (effectType == EventEffectType.MOVE) {
            if (effectValue >= 5) return 3;
            if (effectValue >= 3) return 2;
            return 1;
        }
        if (effectType == EventEffectType.SCOUT) {
            if (effectValue >= 16) return 3;
            if (effectValue >= 10) return 2;
            return 1;
        }
        if (effectType == EventEffectType.EXPERIENCE) {
            if (effectValue >= 70) return 3;
            if (effectValue >= 40) return 2;
            return 1;
        }
        if (effectType == EventEffectType.DICE_EXACT) return 3;
        if (effectType == EventEffectType.SNAKE_WARD) return 2;
        if (effectType == EventEffectType.DICE_CLAMP) return 1;
        return 1;
    }
}
