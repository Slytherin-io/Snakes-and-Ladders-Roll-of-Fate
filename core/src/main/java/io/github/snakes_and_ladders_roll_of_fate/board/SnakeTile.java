package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Entity;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleOutcome;
import java.util.Random;
import java.util.Scanner;

public class SnakeTile extends Tile {
    private int destination;
    private static final Random random = new Random();
    private static final Scanner scanner = new Scanner(System.in); // Shared scanner for console input

    public SnakeTile(int position, int destination) {
        super(position, "SNAKE", "Slide backwards to a lower tile!", EventCategory.SNAKE);
        this.destination = destination;
    }

    @Override
    public void onLand(Player player) {
        startCombat(player);
    }

    // Generates a random snake variation using Enemy attributes
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
