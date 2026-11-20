package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Entity;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;

import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
public class SnakeTile extends Tile {
    private int destination;

    public SnakeTile(int position, int destination) {
        super(position, "SNAKE", "Slide backwards to a lower tile!", EventCategory.SNAKE);
        this.destination = destination;
    }

    @Override
    public void onLand(Player player) {
        startCombat(player);
    }

    public int getSnakeAttack() {
        return 6 + (getPosition() / 7);
    }

    public int getSnakeHealth() {
        return 15 + (getPosition() / 2);
    }

    public void startCombat(Player player) {
        int snakeAttack = getSnakeAttack();
        int snakeHealth = getSnakeHealth();
        System.out.println("\n** COMBAT INITIATED: " + player.getName() + " vs SERPENT (HP: " + snakeHealth + ", ATK: " + snakeAttack + ") **");
        BattleSystem newBattle = new BattleSystem();
        Entity snake = new Enemy("SNAKE");
        snake.setAttack(snakeAttack);
        snake.setHealth(snakeHealth);
        newBattle.startBattle(player, snake);
        while (player.getHealth() > 0 && snake.getHealth() > 0){
            int playerDamage = newBattle.calculateDamage(player, snake);
            int newSnakeHealth = snake.getHealth() - playerDamage; // Calculate first, don't apply yet

            if (newSnakeHealth <= 0) {
                snake.setHealth(0); // Clamp to 0
                int expGained = 15 + (getPosition() / 2);
                player.gainExperience(expGained);
                System.out.println("You dealt " + playerDamage + " damage. Snake HP: 0");
                System.out.println("  [EXP] +" + expGained + " (Total: " + player.getExperience() + ")");
                System.out.println("  [VICTORY] The slain serpent dropped loot!");
                player.addItem(Item.createRandomLoot());
                break;
            } else {
                snake.setHealth(newSnakeHealth); // Safe to apply
                System.out.println("You dealt " + playerDamage + " damage. Snake HP: " + snake.getHealth());
            }
            int snakeDamage = newBattle.calculateDamage(snake, player);
            int newPlayerHealth = player.getHealth() - snakeDamage; // Calculate first

            if (newPlayerHealth <= 0) {
                if (player.getHealth() > 25) {
                    // Mortal strike safeguard: The blow throws the player down the slope clinging to life
                    player.setHealth(1);
                    System.out.println("Snake dealt " + snakeDamage + " damage. Your HP: 1");
                    System.out.println("  [SURVIVAL INSTINCT] The massive blow hurls you backwards off the ledge!");
                    applyEffect(player);
                    break;
                } else {
                    // Truly lethal: Player chose to fight while already critically wounded
                    player.setHealth(0); // Clamp to 0
                    System.out.println("Snake dealt " + snakeDamage + " damage. Your HP: 0");
                    break;
                }
            } else if (newPlayerHealth <= 25) {
                player.setHealth(newPlayerHealth);
                System.out.println("Snake dealt " + snakeDamage + " damage. Your HP: " + player.getHealth());
                System.out.println("You are too weak to continue fighting! You slide down the snake...");
                applyEffect(player);
                break;
            } else {
                player.setHealth(newPlayerHealth);
                System.out.println("Snake dealt " + snakeDamage + " damage. Your HP: " + player.getHealth());
            }
        }
    }

    public void applyEffect(Player player) {
        player.setPosition(destination);
        System.out.println("  [SLIDE] Slid down to Tile " + destination);
    }


    public int getDestination() { return destination; }
    public void setDestination(int destination) { this.destination = destination; }
}
