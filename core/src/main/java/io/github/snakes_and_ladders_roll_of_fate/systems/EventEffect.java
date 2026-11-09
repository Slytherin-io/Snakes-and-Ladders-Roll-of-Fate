package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

public class EventEffect {
    private EventEffectType type;
    private int value; 
    
    public EventEffect(EventEffectType type, int value) {
        this.type = type;
        this.value = value;
    }

    public void apply(Player player) {
        switch (type) {
            case HEALTH:
                int newHealth = Math.max(0, player.getHealth() + value);
                player.setHealth(newHealth);
                System.out.printf("  HP %s%d (Current: %d)%n", (value >= 0 ? "+" : ""), value, player.getHealth());
                break;
            case ATTACK:
                player.setAttack(player.getAttack() + value);
                System.out.printf("  Attack %s%d (Current: %d)%n", (value >= 0 ? "+" : ""), value, player.getAttack());
                break;
            case DEFENSE:
                player.setDefense(player.getDefense() + value);
                System.out.printf("  Defense %s%d (Current: %d)%n", (value >= 0 ? "+" : ""), value, player.getDefense());
                break;
            case EXPERIENCE:
                player.gainExperience(value);
                System.out.printf("  Gained %d EXP (Current: %d)%n", value, player.getExperience());
                break;
            case MOVE:
                player.move(value);
                System.out.printf("  Moved %s%d tiles (New Position: Tile %d)%n", (value >= 0 ? "+" : ""), value, player.getPosition());
                break;
        }
    }
}
