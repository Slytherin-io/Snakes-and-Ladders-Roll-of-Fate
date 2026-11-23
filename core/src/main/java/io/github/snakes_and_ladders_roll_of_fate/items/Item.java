package io.github.snakes_and_ladders_roll_of_fate.items;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import java.util.Random;

public class Item {
    private String name;
    private int amount;
    private ItemType itemType;
    private int durability;
    private int maxDurability;
    private EventEffectType effectType;
    private int effectValue;
    private String effect;
    private static final Random random = new Random();

    // Full constructor
    public Item(String name, ItemType itemType, int durability, EventEffectType effectType, int effectValue, String effect) {
        this.name = name;
        this.amount = 1;
        this.itemType = itemType;
        this.durability = (itemType == ItemType.CONSUMABLE) ? 1 : Math.max(1, durability);
        this.maxDurability = this.durability;
        this.effectType = effectType;
        this.effectValue = effectValue;
        this.effect = effect;
    }

    // Backwards-compatible constructor
    public Item(String name, int amount, int durability, String effect) {
        this(name, ItemType.CONSUMABLE, durability, EventEffectType.HEALTH, 20, effect);
        this.amount = amount;
    }

    // Factory method for Consumables (single use, durability is 1)
    public static Item createConsumable(String name, EventEffectType effectType, int effectValue, String effect) {
        return new Item(name, ItemType.CONSUMABLE, 1, effectType, effectValue, effect);
    }

    // Factory method for Equipment with randomized durability within a min-max range
    public static Item createEquipment(String name, EventEffectType effectType, int effectValue, int minDurability, int maxDurability, String effect) {
        int spread = Math.abs(maxDurability - minDurability) + 1;
        int rolledDurability = Math.min(minDurability, maxDurability) + random.nextInt(spread);
        return new Item(name, ItemType.EQUIPMENT, rolledDurability, effectType, effectValue, effect);
    }

    // Factory method for generating random loot drops
    public static Item createRandomLoot() {
        int roll = random.nextInt(6);
        switch (roll) {
            case 0:
                return createConsumable("Minor Health Potion", EventEffectType.HEALTH, 25, "Restores 25 HP");
            case 1:
                return createConsumable("Greater Health Elixir", EventEffectType.HEALTH, 50, "Restores 50 HP");
            case 2:
                return createEquipment("Iron Sword", EventEffectType.ATTACK, 4, 2, 4, "+4 ATK (1 Turn Buff)");
            case 3:
                return createEquipment("Reinforced Shield", EventEffectType.DEFENSE, 4, 2, 5, "+4 DEF (1 Turn Buff)");
            case 4:
                return createConsumable("Hermes Feather", EventEffectType.MOVE, 3, "Advance 3 tiles forward");
            case 5:
            default:
                return createEquipment("Serpent Fang Dagger", EventEffectType.ATTACK, 6, 2, 3, "+6 ATK (1 Turn Buff)");
        }
    }

    // Applies the item effect to the player and handles durability degradation
    public void use(Player player) {
        if (effectType != null) {
            switch (effectType) {
                case HEALTH:
                    int newHealth = Math.min(player.getMaxHealth(), Math.max(0, player.getHealth() + effectValue));
                    player.setHealth(newHealth);
                    System.out.printf("  [HP] %s%d (Current: %d)%n", (effectValue >= 0 ? "+" : ""), effectValue, player.getHealth());
                    break;
                case ATTACK:
                    if (itemType == ItemType.EQUIPMENT) {
                        player.addTempAttack(effectValue);
                        System.out.printf("  [BUFF: BATTLE FOCUS] +%d Attack for this turn (Total ATK: %d [Base %d + Buff %d])%n",
                            effectValue, player.getAttack(), player.getBaseAttack(), player.getTempAttack());
                    } else {
                        player.setAttack(player.getBaseAttack() + effectValue);
                        System.out.printf("  [Attack] %s%d (Current: %d)%n", (effectValue >= 0 ? "+" : ""), effectValue, player.getAttack());
                    }
                    break;
                case DEFENSE:
                    if (itemType == ItemType.EQUIPMENT) {
                        player.addTempDefense(effectValue);
                        System.out.printf("  [BUFF: ACTIVE GUARD] +%d Defense for this turn (Total DEF: %d [Base %d + Buff %d])%n",
                            effectValue, player.getDefense(), player.getBaseDefense(), player.getTempDefense());
                    } else {
                        player.setDefense(player.getBaseDefense() + effectValue);
                        System.out.printf("  [Defense] %s%d (Current: %d)%n", (effectValue >= 0 ? "+" : ""), effectValue, player.getDefense());
                    }
                    break;
                case EXPERIENCE:
                    player.gainExperience(effectValue);
                    System.out.printf("  [EXP] +%d (Current: %d)%n", effectValue, player.getExperience());
                    break;
                case MOVE:
                    player.move(effectValue);
                    System.out.printf("  [Move] %s%d tiles (New Position: Tile %d)%n", (effectValue >= 0 ? "+" : ""), effectValue, player.getPosition());
                    break;
            }
        }

        reduceDurability();

        if (durability <= 0) {
            if (itemType == ItemType.CONSUMABLE) {
                System.out.println("  [CONSUMED] " + name + " has been used up.");
            } else {
                System.out.println("  [BROKEN] " + name + " ran out of durability and shattered!");
            }
        } else {
            System.out.printf("  [DURABILITY] %s remaining uses: %d/%d%n", name, durability, maxDurability);
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

    // Getters and Setters
    public String getName() { return name; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public ItemType getItemType() { return itemType; }
    public int getDurability() { return durability; }
    public int getMaxDurability() { return maxDurability; }
    public EventEffectType getEffectType() { return effectType; }
    public int getEffectValue() { return effectValue; }
    public String getEffect() { return effect; }
}
