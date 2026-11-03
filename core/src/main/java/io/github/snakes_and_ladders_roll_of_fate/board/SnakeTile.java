package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Entity;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
public class SnakeTile extends Tile {
    private int destination;

    public SnakeTile(int position, int destination) {
        super(position, "SNAKE", "Defeat to proceed, Fall below 25% health brings you down");
        this.destination = destination;
    }

    @Override
    public void onLand(Player player) {
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
        }

    }

    public void applyEffect(Player player) {
        player.setPosition(destination);
        System.out.println("Going down to " + destination);
    }


    public int getDestination() { return destination; }
    public void setDestination(int destination) { this.destination = destination; }
}
