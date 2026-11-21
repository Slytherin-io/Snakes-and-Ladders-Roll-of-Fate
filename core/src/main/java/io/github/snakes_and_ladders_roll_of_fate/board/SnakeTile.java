package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Entity;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
<<<<<<< Updated upstream
=======
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;
>>>>>>> Stashed changes
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleOutcome;
import java.util.Random;
import java.util.Scanner;

import static com.badlogic.gdx.math.MathUtils.random;

public class SnakeTile extends Tile {
    private int destination;
    private static final Random random = new Random();
    private static final Scanner scanner = new Scanner(System.in); // Shared scanner for console input
    public SnakeTile(int position, int destination) {
        super(position, "SNAKE", "Defeat to proceed, Fall below 25% health brings you down");
        this.destination = destination;
    }

    @Override
    public void onLand(Player player) {
<<<<<<< Updated upstream
        System.out.println(dialogue.createSnakeDialogue());
        BattleSystem newBattle = new BattleSystem();
        Entity snake = new Enemy("SNAKE");
        snake.setAttack(getPosition()+1);
        snake.setHealth(getPosition());
        newBattle.startBattle(player, snake);
        while (player.getHealth() > 0 && snake.getHealth() >0){
            int playerDamage = newBattle.calculateDamage(player, snake);
            int newSnakeHealth = snake.getHealth() - playerDamage; // Calculate first, don't apply yet

            if (newSnakeHealth <= 0) {
                snake.setHealth(0); // Clamp to 0
                player.setExperience(player.getPosition() * 2);
                System.out.println("You dealt " + playerDamage + " damage. Snake HP: 0");
                break;
            } else {
                snake.setHealth(newSnakeHealth); // Safe to apply
                System.out.println("You dealt " + playerDamage + " damage. Snake HP: " + snake.getHealth());
            }
            int snakeDamage = newBattle.calculateDamage(snake, player);
            int newPlayerHealth = player.getHealth() - snakeDamage; // Calculate first

            if (newPlayerHealth <= 0) {
                // GAME OVER: Health dropped to 0 or below
                player.setHealth(0); // Clamp to 0
                System.out.println("Snake dealt " + snakeDamage + " damage. Your HP: 0");
                break;
            }
            else {
                player.setHealth(newPlayerHealth); // Safe to apply
                System.out.println("Snake dealt " + snakeDamage + " damage. Your HP: " + player.getHealth());

                // FALLBACK: Health is still positive, but dropped below 25
                if (player.getHealth() <= 25) {
                    System.out.println("You are too weak to continue fighting! You slide down the snake...");
                    applyEffect(player);
                    break;
                }
            }
=======
        startCombat(player);
    }
    private Enemy createSnakeVariation() {
        int variationRoll = random.nextInt(3);
        Enemy snake;

        switch (variationRoll) {
            case 0:
                snake = new Enemy("Venomous Viper");
                snake.setBehavior("Aggressive (High ATK, Low HP)");
                snake.setAttack(8 + (getPosition() / 5));
                snake.setHealth(15 + (getPosition() / 3));
                snake.setReward("Viper Venom Sac");
                break;
            case 1:
                snake = new Enemy("Armored Python");
                snake.setBehavior("Defensive (Low ATK, High HP)");
                snake.setAttack(4 + (getPosition() / 6));
                snake.setHealth(25 + (getPosition() / 2));
                snake.setReward("Python Scales");
                break;
            default:
                snake = new Enemy("Shadow Asp");
                snake.setBehavior("Balanced (Stealthy)");
                snake.setAttack(6 + (getPosition() / 5));
                snake.setHealth(18 + (getPosition() / 2));
                snake.setReward("Shadow Fang");
                break;
        }
        return snake;
    }


    public void startCombat(Player player) {
        Enemy snake = createSnakeVariation();
        System.out.println("\n🐍 A wild " + snake.getEnemyType() + " blocks your path!");

        BattleSystem battle = new BattleSystem();
        BattleOutcome outcome = battle.runBattle(player, snake, scanner);

        // Handle Post-Battle Outcomes
        switch (outcome) {
            case VICTORY:
                int expGained = 15 + (getPosition() / 2);
                player.gainExperience(expGained);
                System.out.println("\n  [EXP] +" + expGained + " (Total: " + player.getExperience() + ")");

                // Drop random loot using your Item factory method
                Item loot = Item.createRandomLoot();
                player.addItem(loot);
                System.out.println("  [LOOT] The " + snake.getEnemyType() + " dropped: " + loot.getName() + " (" + loot.getEffect() + ")");
                break;

            case ESCAPED:
                System.out.println("\nYou managed to escape, but the snake's tail swipes at you, knocking you down the slope!");
                applyEffect(player);
                break;

            case WEAKENED:
                System.out.println("\nBarely conscious, you tumble backwards down the snake's coils to safety...");
                applyEffect(player);
                break;

            case DEFEAT:
                System.out.println("\n💀 You have been slain by the " + snake.getEnemyType() + "...");
                // Game Over logic would go here
                break;
>>>>>>> Stashed changes
        }

    }

    public void applyEffect(Player player) {
        player.setPosition(destination);
        System.out.println("Going down to " + destination);
    }

    public int getSnakeAttack() {
        return 6 + (getPosition() / 7);
    }

    public int getSnakeHealth() {
        return 15 + (getPosition() / 2);
    }
    public int getDestination() { return destination; }
    public void setDestination(int destination) { this.destination = destination; }
}
