package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleOutcome;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;
import java.util.Random;

/**
 * Snake tile for the LibGDX board.
 * It no longer runs combat: BoardScreen detects the landing and opens BattleScreen,
 * and BattleScreen calls resolveOutcome() exactly once when the fight ends.
 */
public class SnakeTile extends Tile {
    private int destination;
    private static final Random random = new Random();

    public SnakeTile(int position, int destination) {
        super(position, "SNAKE", "Slide backwards to a lower tile!", EventCategory.SNAKE);
        this.destination = destination;
    }

    @Override
    public void onLand(Player player) {
        // Intentionally empty: BoardScreen intercepts the landing and launches BattleScreen.
    }

    /** Factory: builds a balanced, position-scaled snake for the BattleScreen. */
    public Enemy createSnakeEnemy() {
        int pos = getPosition();
        Enemy snake;
        switch (random.nextInt(3)) {
            case 0:
                snake = new Enemy("Venomous Viper");
                snake.setBehavior("Aggressive (High ATK, Low HP)");
                snake.setAttack(6 + (pos / 10));
                snake.setHealth(25 + (pos / 2));
                snake.setDefense(1 + (pos / 20));
                snake.setReward("Viper Venom Sac");
                break;
            case 1:
                snake = new Enemy("Armored Python");
                snake.setBehavior("Defensive (Low ATK, High HP)");
                snake.setAttack(3 + (pos / 12));
                snake.setHealth(35 + (pos / 2));
                snake.setDefense(2 + (pos / 15));
                snake.setReward("Python Scales");
                break;
            default:
                snake = new Enemy("Shadow Asp");
                snake.setBehavior("Balanced (Stealthy)");
                snake.setAttack(5 + (pos / 10));
                snake.setHealth(30 + (pos / 2));
                snake.setDefense(1 + (pos / 20));
                snake.setReward("Shadow Fang");
                break;
        }
        return snake;
    }

    /**
     * Called ONCE by BattleScreen when the battle reaches BATTLE_END.
     * Applies EXP / loot / slide and returns a message for the UI.
     */
    public String resolveOutcome(Player player, Enemy snake, BattleOutcome outcome) {
        switch (outcome) {
            case VICTORY: {
                int expGained = 15 + (getPosition() / 2);
                player.gainExperience(expGained);
                Item loot = Item.createRandomLoot(player.getLevel()); // tier-scaled loot
                boolean added = player.addItem(loot);
                String msg = "VICTORY! +" + expGained + " EXP";
                msg += added ? " | Loot: " + loot.getName() : " | Satchel full - loot lost!";
                return msg;
            }
            case ESCAPED:
                applyEffect(player);
                return "You escape, but the tail swipes you down to Tile " + destination + "!";
            case WEAKENED:
                applyEffect(player);
                return "Barely conscious, you tumble down the coils to Tile " + destination + "...";
            case DEFEAT:
            default:
                return "You have been slain by the " + snake.getEnemyType() + "...";
        }
    }

    public void applyEffect(Player player) {
        player.setPosition(destination);
    }

    public int getDestination() { return destination; }
    public void setDestination(int destination) { this.destination = destination; }
}
