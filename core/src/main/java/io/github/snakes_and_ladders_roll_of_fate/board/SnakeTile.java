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
    /** Factory: builds balanced, tier-scaled snakes to prevent late-game damage sponges. */
    public Enemy createSnakeEnemy() {
        int pos = getPosition();
        Enemy snake;

        // 1. Determine Board Zone for predictable, fair scaling
        int tier;
        if (pos <= 33) tier = 1;       // Early Game (Tiles 1-33)
        else if (pos <= 66) tier = 2;  // Mid-Game (Tiles 34-66)
        else tier = 3;                 // Late Game (Tiles 67-99)

        // 2. Generate Variation based on Tier
        switch (random.nextInt(3)) {
            case 0: // VENOMOUS VIPER (Glass Cannon / Aggressive)
                snake = new Enemy("Venomous Viper");
                snake.setBehavior("Aggressive");
                snake.setAttack(4 + (tier * 3));      // ATK: 7, 10, 13
                snake.setHealth(15 + (tier * 10));    // HP:  25, 35, 45
                snake.setDefense(tier);               // DEF: 1,  2,  3
                snake.setReward("Viper Venom Sac");
                break;

            case 1: // ARMORED PYTHON (Tank / Defensive)
                snake = new Enemy("Armored Python");
                snake.setBehavior("Defensive");
                snake.setAttack(2 + (tier * 2));      // ATK: 4,  6,  8 (Low threat)
                snake.setHealth(25 + (tier * 15));    // HP:  40, 55, 70 (High bulk)
                snake.setDefense(1 + tier);           // DEF: 2,  3,  4 (Capped! Prevents damage immunity)
                snake.setReward("Python Scales");
                break;

            default: // SHADOW ASP (Rogue / Balanced)
                snake = new Enemy("Shadow Asp");
                snake.setBehavior("Stealthy");
                snake.setAttack(3 + (tier * 3));      // ATK: 6,  9,  12
                snake.setHealth(20 + (tier * 12));    // HP:  32, 44, 56
                snake.setDefense(tier);               // DEF: 1,  2,  3
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
