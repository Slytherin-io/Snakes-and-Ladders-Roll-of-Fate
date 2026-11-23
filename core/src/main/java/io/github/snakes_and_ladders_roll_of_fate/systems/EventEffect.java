package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import java.util.Random;

public class EventEffect {
    @FunctionalInterface
    public interface ItemSwapHandler {
        void handleSwap(Player player, Item newItem);
    }

    private static ItemSwapHandler itemSwapHandler = null;

    private EventEffectType type;
    private int minValue;
    private int maxValue;
    private Item itemReward;
    private boolean isRandomLoot = false;
    private static final Random random = new Random();

    // Range constructor: value varies between minValue and maxValue
    public EventEffect(EventEffectType type, int minValue, int maxValue) {
        this.type = type;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    // Fixed value constructor: minValue equals maxValue
    public EventEffect(EventEffectType type, int value) {
        this(type, value, value);
    }

    // Item reward constructor (specific item)
    public EventEffect(Item itemReward) {
        this.type = EventEffectType.ITEM;
        this.itemReward = itemReward;
        this.isRandomLoot = false;
    }

    // Factory for random loot drop effect
    public static EventEffect createRandomLootEffect() {
        EventEffect effect = new EventEffect(EventEffectType.ITEM, 0);
        effect.isRandomLoot = true;
        return effect;
    }

    public static void setItemSwapHandler(ItemSwapHandler handler) {
        itemSwapHandler = handler;
    }

    public static ItemSwapHandler getItemSwapHandler() {
        return itemSwapHandler;
    }

    // Resolves the effect with math variance and bounds checking
    public void apply(Player player) {
        apply(player, null);
    }

    public void apply(Player player, java.util.List<String> logMessages) {
        int resolvedValue = calculateResolvedValue();

        switch (type) {
            case HEALTH:
                int newHealth = Math.min(player.getMaxHealth(), Math.max(0, player.getHealth() + resolvedValue));
                player.setHealth(newHealth);
                String hpMsg = String.format("Vitality: %s%d HP (Current: %d/%d)", (resolvedValue >= 0 ? "+" : ""), resolvedValue, player.getHealth(), player.getMaxHealth());
                System.out.println("  [HP] " + hpMsg);
                if (logMessages != null) logMessages.add(hpMsg);
                break;

            case ATTACK:
                player.setAttack(player.getAttack() + resolvedValue);
                String atkMsg = String.format("Martial Might: %s%d ATK (Current: %d)", (resolvedValue >= 0 ? "+" : ""), resolvedValue, player.getAttack());
                System.out.println("  [Attack] " + atkMsg);
                if (logMessages != null) logMessages.add(atkMsg);
                break;

            case DEFENSE:
                player.setDefense(player.getDefense() + resolvedValue);
                String defMsg = String.format("Ward Fortitude: %s%d DEF (Current: %d)", (resolvedValue >= 0 ? "+" : ""), resolvedValue, player.getDefense());
                System.out.println("  [Defense] " + defMsg);
                if (logMessages != null) logMessages.add(defMsg);
                break;

            case EXPERIENCE:
                int expGain = Math.max(0, Math.abs(resolvedValue));
                player.gainExperience(expGain);
                String expMsg = String.format("Knowledge: +%d EXP (Total: %d)", expGain, player.getExperience());
                System.out.println("  [EXP] " + expMsg);
                if (logMessages != null) logMessages.add(expMsg);
                break;

            case MOVE:
                player.move(resolvedValue);
                String mvMsg = String.format("Stride: %s%d tiles (Position: Tile %d)", (resolvedValue >= 0 ? "+" : ""), resolvedValue, player.getPosition());
                System.out.println("  [Move] " + mvMsg);
                if (logMessages != null) logMessages.add(mvMsg);
                break;

            case ITEM:
                Item toGrant = isRandomLoot ? Item.createRandomLoot(player.getLevel()) : itemReward;
                if (toGrant != null) {
                    boolean added = player.addItem(toGrant);
                    if (added) {
                        String itemMsg = "Acquired Relic: " + toGrant.getName() + " (" + toGrant.getEffect() + ")";
                        System.out.println("  " + itemMsg);
                        if (logMessages != null) logMessages.add(itemMsg);
                    } else {
                        if (itemSwapHandler != null) {
                            itemSwapHandler.handleSwap(player, toGrant);
                        } else {
                            String fullMsg = "Satchel full! Left " + toGrant.getName() + " behind.";
                            System.out.println("  [INVENTORY FULL] " + fullMsg);
                            if (logMessages != null) logMessages.add(fullMsg);
                        }
                    }
                }
                break;
        }
    }

    // Helper to calculate a random number between minValue and maxValue
    private int calculateResolvedValue() {
        if (minValue == maxValue) {
            return minValue;
        }
        int spread = Math.abs(maxValue - minValue) + 1;
        int base = Math.min(minValue, maxValue);
        return base + random.nextInt(spread);
    }

    // Getters
    public EventEffectType getType() { return type; }
    public int getMinValue() { return minValue; }
    public int getMaxValue() { return maxValue; }
}
