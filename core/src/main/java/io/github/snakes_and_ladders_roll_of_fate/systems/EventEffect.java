package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import java.util.Random;

public class EventEffect {
    private EventEffectType type;
    private int minValue;
    private int maxValue;
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

    // Resolves the effect with math variance and bounds checking
    public void apply(Player player) {
        int resolvedValue = calculateResolvedValue();

        switch (type) {
            case HEALTH:
                int newHealth = Math.max(0, player.getHealth() + resolvedValue);
                player.setHealth(newHealth);
                System.out.printf("  [HP] %s%d (Current: %d)%n", (resolvedValue >= 0 ? "+" : ""), resolvedValue, player.getHealth());
                break;

            case ATTACK:
                player.setAttack(player.getAttack() + resolvedValue);
                System.out.printf("  [Attack] %s%d (Current: %d)%n", (resolvedValue >= 0 ? "+" : ""), resolvedValue, player.getAttack());
                break;

            case DEFENSE:
                player.setDefense(player.getDefense() + resolvedValue);
                System.out.printf("  [Defense] %s%d (Current: %d)%n", (resolvedValue >= 0 ? "+" : ""), resolvedValue, player.getDefense());
                break;

            case EXPERIENCE:
                player.gainExperience(resolvedValue);
                System.out.printf("  [EXP] +%d (Current: %d)%n", resolvedValue, player.getExperience());
                break;

            case MOVE:
                player.move(resolvedValue);
                System.out.printf("  [Move] %s%d tiles (New Position: Tile %d)%n", (resolvedValue >= 0 ? "+" : ""), resolvedValue, player.getPosition());
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
