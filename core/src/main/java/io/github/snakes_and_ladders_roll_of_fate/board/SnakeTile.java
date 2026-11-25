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

    /**
     * Factory: builds an Act-themed, balanced, position-scaled serpent for combat and the BattleScreen.
     * Act I (Tiles 1-30): Kind of easy - Moss Adder, Forest Python, Emerald Viper
     * Act II (Tiles 31-70): Getting hard - Cave Rattler, Crag Carapace Serpent, Obsidian Cobra
     * Act III (Tiles 71-100): Difficult - Magma Wyrm, Abyssal Basilisk, Apex Celestial Serpent
     */
    public Enemy createSnakeEnemy() {
        int pos = getPosition();
        Enemy snake;
        int variant = random.nextInt(3);

        if (pos <= 30) {
            // Act I: The Verdant Trail (Tiles 1-30) - Kind of easy, introductory encounters
            switch (variant) {
                case 0:
                    snake = new Enemy("Moss Adder");
                    snake.setBehavior("Skittish Striker (Swift, Low HP)");
                    snake.setAttack(6 + (pos / 15));
                    snake.setHealth(20 + (pos / 3));
                    snake.setDefense(1);
                    snake.setReward("Adder Venom Sac");
                    break;
                case 1:
                    snake = new Enemy("Forest Python");
                    snake.setBehavior("Coiling Constrictor (High HP, Low ATK)");
                    snake.setAttack(4 + (pos / 15));
                    snake.setHealth(30 + (pos / 3));
                    snake.setDefense(3);
                    snake.setReward("Python Leather");
                    break;
                default:
                    snake = new Enemy("Emerald Viper");
                    snake.setBehavior("Balanced Ambush (Moderate ATK & HP)");
                    snake.setAttack(5 + (pos / 15));
                    snake.setHealth(24 + (pos / 3));
                    snake.setDefense(2);
                    snake.setReward("Emerald Scales");
                    break;
            }
        } else if (pos <= 70) {
            // Act II: The Jagged Crags & Sunken Caverns (Tiles 31-70) - Getting hard, tactical tests
            int actOffset = pos - 30;
            switch (variant) {
                case 0:
                    snake = new Enemy("Cave Rattler");
                    snake.setBehavior("Furious Rattler (High ATK, Threatening Strikes)");
                    snake.setAttack(13 + (actOffset / 10));
                    snake.setHealth(44 + (actOffset / 2));
                    snake.setDefense(2 + (actOffset / 20));
                    snake.setReward("Rattler Fang");
                    break;
                case 1:
                    snake = new Enemy("Crag Carapace Serpent");
                    snake.setBehavior("Stone-Scaled (Heavy DEF, High HP)");
                    snake.setAttack(8 + (actOffset / 12));
                    snake.setHealth(58 + (actOffset / 2));
                    snake.setDefense(6 + (actOffset / 15));
                    snake.setReward("Crag Serpent Plate");
                    break;
                default:
                    snake = new Enemy("Obsidian Cobra");
                    snake.setBehavior("Venomous Stalker (Balanced, Piercing Fangs)");
                    snake.setAttack(11 + (actOffset / 10));
                    snake.setHealth(50 + (actOffset / 2));
                    snake.setDefense(4 + (actOffset / 20));
                    snake.setReward("Obsidian Scale");
                    break;
            }
        } else {
            // Act III: The Spire of Fate & Dragon's Summit (Tiles 71-100) - Difficult, apex trials
            int actOffset = pos - 70;
            switch (variant) {
                case 0:
                    snake = new Enemy("Magma Wyrm");
                    snake.setBehavior("Infernal Rage (Devastating ATK, Fierce Strikes)");
                    snake.setAttack(22 + (actOffset / 6));
                    snake.setHealth(76 + actOffset);
                    snake.setDefense(4 + (actOffset / 15));
                    snake.setReward("Heart of Magma");
                    break;
                case 1:
                    snake = new Enemy("Abyssal Basilisk");
                    snake.setBehavior("Petrifying Gaze (Massive HP & Iron Armor)");
                    snake.setAttack(14 + (actOffset / 8));
                    snake.setHealth(92 + actOffset);
                    snake.setDefense(9 + (actOffset / 10));
                    snake.setReward("Basilisk Carapace");
                    break;
                default:
                    snake = new Enemy("Apex Celestial Serpent");
                    snake.setBehavior("Apex Predator (Supreme ATK & Resilience)");
                    snake.setAttack(19 + (actOffset / 7));
                    snake.setHealth(84 + actOffset);
                    snake.setDefense(7 + (actOffset / 12));
                    snake.setReward("Celestial Serpent Crest");
                    break;
            }
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
