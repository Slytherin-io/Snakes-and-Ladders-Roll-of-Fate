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
        BattleSystem newBattle = new BattleSystem();
        Entity snake = new Enemy("SNAKE");
        snake.setAttack(getPosition()+1);
        snake.setHealth(getPosition());
        newBattle.startBattle(player, snake);
        while (player.getHealth() > 0 && snake.getHealth() >0){
            int playerDamage = newBattle.calculateDamage(player, snake);
            snake.setHealth(snake.getHealth() - playerDamage);
            System.out.println("You dealt " + playerDamage + " damage. Snake HP: " + snake.getHealth());//For Console Testing
            if (snake.getHealth() <= 0) {
                snake.setHealth(0);
                player.setExperience(getPosition() + 5);//Experience scales with Tile position
                break;
            }
            int snakeDamage = newBattle.calculateDamage(snake, player);
            player.setHealth(player.getHealth() - snakeDamage);
            System.out.println("Snake dealt " + snakeDamage + " damage. Your HP: " + player.getHealth());//For Console Testing

            if (player.getHealth() <= 25) {
                if(player.getHealth() > 0) {
                    player.setHealth(Math.max(0, player.getHealth())); // Prevent negative HP display
                    System.out.println("You are too weak to continue fighting! You slide down the snake...");//For Console Testing
                    applyEffect(player);
                    break; // Exit the loop immediately
                }
            }
            else if (player.getHealth() <=0) {
                player.setHealth(0);
                break;}
        }

        System.out.println(dialogue.createSnakeDialogue());
        applyEffect(player);
    }

    public void applyEffect(Player player) {
        player.setPosition(destination);
        System.out.println("Going down to " + destination);
    }


    public int getDestination() { return destination; }
    public void setDestination(int destination) { this.destination = destination; }
}
