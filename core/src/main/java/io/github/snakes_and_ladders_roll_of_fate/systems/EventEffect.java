package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.items.ItemGenerator;
import java.util.Random;

public class EventEffect {
    @FunctionalInterface
    public interface ItemSwapHandler {
        void handleSwap(Player player, Item newItem);
    }

    @FunctionalInterface
    public interface ScoutHandler {
        int scoutTiles(Player player, int tilesAhead);
    }

    private static ItemSwapHandler itemSwapHandler = null;
    private static ScoutHandler scoutHandler = null;

    private EventEffectType type;
    private int minValue;
    private int maxValue;
    private Item itemReward;
    private boolean isRandomLoot = false;
    private boolean isCurseDrop = false;
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

    // Factory for inflicting a random passive Curse item onto the player
    public static EventEffect createCurseDropEffect() {
        EventEffect effect = new EventEffect(EventEffectType.ITEM, 0);
        effect.isCurseDrop = true;
        return effect;
    }

    public static void setItemSwapHandler(ItemSwapHandler handler) {
        itemSwapHandler = handler;
    }

    public static ItemSwapHandler getItemSwapHandler() {
        return itemSwapHandler;
    }

    public static void setScoutHandler(ScoutHandler handler) {
        scoutHandler = handler;
    }

    public static ScoutHandler getScoutHandler() {
        return scoutHandler;
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
                String hpMsg = String.format("Vitality: %s%d HP", (resolvedValue >= 0 ? "+" : ""), resolvedValue);
                System.out.println("  [HP] " + hpMsg + " (Current: " + player.getHealth() + "/" + player.getMaxHealth() + ")");
                if (logMessages != null) logMessages.add(hpMsg);
                break;

            case MAX_HEALTH:
                player.modifyMaxHealth(resolvedValue);
                String maxHpMsg = String.format("Max Vitality: %s%d Max HP", (resolvedValue >= 0 ? "+" : ""), resolvedValue);
                System.out.println("  [MAX HP] " + maxHpMsg + " (Now: " + player.getHealth() + "/" + player.getMaxHealth() + ")");
                if (logMessages != null) logMessages.add(maxHpMsg);
                break;

            case ATTACK:
                player.setAttack(Math.max(1, player.getBaseAttack() + resolvedValue));
                String atkMsg = String.format("Martial Might: %s%d ATK", (resolvedValue >= 0 ? "+" : ""), resolvedValue);
                System.out.println("  [Attack] " + atkMsg + " (Current: " + player.getBaseAttack() + ")");
                if (logMessages != null) logMessages.add(atkMsg);
                break;

            case DEFENSE:
                player.setDefense(Math.max(0, player.getBaseDefense() + resolvedValue));
                String defMsg = String.format("Ward Fortitude: %s%d DEF", (resolvedValue >= 0 ? "+" : ""), resolvedValue);
                System.out.println("  [Defense] " + defMsg + " (Current: " + player.getBaseDefense() + ")");
                if (logMessages != null) logMessages.add(defMsg);
                break;

            case EXPERIENCE:
                if (resolvedValue >= 0) {
                    player.gainExperience(resolvedValue);
                    String expMsg = String.format("Knowledge: +%d EXP", resolvedValue);
                    System.out.println("  [EXP] " + expMsg + " (Total: " + player.getTotalExperience() + ")");
                    if (logMessages != null) logMessages.add(expMsg);
                } else {
                    int cost = Math.abs(resolvedValue);
                    player.spendTotalExperience(cost);
                    String expLossMsg = String.format("Knowledge Sacrificed: -%d EXP", cost);
                    System.out.println("  [EXP] " + expLossMsg + " (Remaining Total: " + player.getTotalExperience() + ")");
                    if (logMessages != null) logMessages.add(expLossMsg);
                }
                break;

            case MOVE:
                player.move(resolvedValue);
                String mvMsg = String.format("Stride: %s%d tiles", (resolvedValue >= 0 ? "+" : ""), resolvedValue);
                System.out.println("  [Move] " + mvMsg + " (Position: Tile " + player.getPosition() + ")");
                if (logMessages != null) logMessages.add(mvMsg);
                break;

            case SCOUT:
                int tilesAhead = Math.max(1, resolvedValue);
                int uncovered = (scoutHandler != null) ? scoutHandler.scoutTiles(player, tilesAhead) : 0;
                String scoutMsg = String.format("Foresight: Scouted %d tiles ahead (%d uncharted uncovered)", tilesAhead, uncovered);
                System.out.println("  [SCOUT] " + scoutMsg);
                if (logMessages != null) logMessages.add(scoutMsg);
                break;

            case DICE_EXACT:
                player.setPendingDiceChoice(true);
                String exactMsg = "Blessing: Next dice roll allows choosing exact face (1-6)!";
                System.out.println("  [DICE CONTROL] " + exactMsg);
                if (logMessages != null) logMessages.add(exactMsg);
                break;

            case DICE_CLAMP:
                player.setMaxDiceRollCap(resolvedValue);
                String clampMsg = String.format("Hex: Next dice roll capped at %d!", resolvedValue);
                System.out.println("  [DICE CAP] " + clampMsg);
                if (logMessages != null) logMessages.add(clampMsg);
                break;

            case SNAKE_WARD:
                player.setHasSnakeWard(true);
                String wardMsg = "Protection: Next Serpent encounter will be pacified!";
                System.out.println("  [SNAKE WARD] " + wardMsg);
                if (logMessages != null) logMessages.add(wardMsg);
                break;

            case ITEM:
                Item toGrant;
                if (isCurseDrop) {
                    toGrant = ItemGenerator.generateCurse();
                } else if (isRandomLoot) {
                    toGrant = Item.createRandomLoot(player.getLevel());
                } else {
                    toGrant = itemReward;
                }
                if (toGrant != null) {
                    boolean added = player.addItem(toGrant);
                    if (added) {
                        String prefix = toGrant.isCurse() ? "Cursed Burden Inflicted: " : "Acquired Relic: ";
                        String itemMsg = prefix + toGrant.getName() + " (" + toGrant.getEffect() + ")";
                        System.out.println("  " + itemMsg);
                        if (logMessages != null) logMessages.add(itemMsg);
                    } else if (toGrant.isCurse()) {
                        // Full satchel resists picking up a curse but suffers minor backlash
                        player.setHealth(Math.max(1, player.getHealth() - 8));
                        String backlashMsg = "Satchel full! " + toGrant.getName() + " lashed out for -8 HP!";
                        System.out.println("  [CURSE BACKLASH] " + backlashMsg);
                        if (logMessages != null) logMessages.add(backlashMsg);
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

            default:
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
    public boolean isCurseDrop() { return isCurseDrop; }
}
