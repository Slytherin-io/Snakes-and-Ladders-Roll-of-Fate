package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleOutcome;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;
import java.util.Random;
import java.util.Scanner;

/**
 * Snake tile for the board.
 * In the GUI, BoardScreen detects the landing, shows the ambush narrative modal,
 * and launches BattleScreen if the player engages in combat.
 * For console play and unit tests, startCombat() runs the encounter synchronously.
 */
public class SnakeTile extends Tile {
    private int destination;
    private static final Random random = new Random();
    private static final Scanner scanner = new Scanner(System.in);

    public SnakeTile(int position, int destination) {
        super(position, "SNAKE", "Slide backwards to a lower tile!", EventCategory.SNAKE);
        this.destination = destination;
    }

    @Override
    public void onLand(Player player) {
        startCombat(player);
    }

    /** Factory: builds a balanced, position-scaled snake for combat and the BattleScreen. */
    public Enemy createSnakeEnemy() {
        int pos = getPosition();
        Enemy snake;
        switch (random.nextInt(3)) {
            case 0:
                snake = new Enemy("Venomous Viper");
                snake.setBehavior("Aggressive (High ATK, Low HP)");
                snake.setAttack(8 + (pos / 5));
                snake.setHealth(25 + (pos / 2));
                snake.setDefense(1 + (pos / 20));
                snake.setReward("Viper Venom Sac");
                break;
            case 1:
                snake = new Enemy("Armored Python");
                snake.setBehavior("Defensive (Low ATK, High HP)");
                snake.setAttack(4 + (pos / 6));
                snake.setHealth(35 + (pos / 2));
                snake.setDefense(2 + (pos / 15));
                snake.setReward("Python Scales");
                break;
            default:
                snake = new Enemy("Shadow Asp");
                snake.setBehavior("Balanced (Stealthy)");
                snake.setAttack(6 + (pos / 5));
                snake.setHealth(30 + (pos / 2));
                snake.setDefense(1 + (pos / 20));
                snake.setReward("Shadow Fang");
                break;
        }
        return snake;
    }

    /** Runs synchronous combat for console gameplay and tests. */
    public void startCombat(Player player) {
        Enemy snake = createSnakeEnemy();
        System.out.println("\n🐍 A wild " + snake.getEnemyType() + " blocks your path!");

        BattleSystem battle = new BattleSystem();
        BattleOutcome outcome = battle.runBattle(player, snake, scanner);

        String msg = resolveOutcome(player, snake, outcome);
        System.out.println(msg);
    }

    /**
     * Called by BattleScreen (or console combat) when the battle reaches BATTLE_END.
     * Applies EXP / loot / slide and returns a message for the UI/console.
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
                return "Barely conscious, you tumble down the coils to Tile " + destination + "!";
            case DEFEAT:
            default:
                return "You have been slain by the " + snake.getEnemyType() + "!";
        }
    }

    public void applyEffect(Player player) {
        player.setPosition(destination);
        System.out.println("  [SLIDE] You slid down to Tile " + destination);
    }

    public int getDestination() { return destination; }
    public void setDestination(int destination) { this.destination = destination; }

    public int getSnakeAttack() {
        return 6 + (getPosition() / 7);
    }

    public int getSnakeHealth() {
        return 15 + (getPosition() / 2);
    }
}
