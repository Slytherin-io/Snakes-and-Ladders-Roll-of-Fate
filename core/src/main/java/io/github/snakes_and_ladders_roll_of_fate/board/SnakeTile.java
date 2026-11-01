package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

public class SnakeTile extends Tile {
    private int destination;
    
    public SnakeTile(int position, int destination) {
        super(position, "SNAKE", "Slide backwards to a lower tile!");
        this.destination = destination;
    }

    @Override 
    public void onLand(Player player) {
        applyEffect(player);
    }

    public void applyEffect(Player player) {
        player.setPosition(destination);
    }

    public int getDestination() { return destination; }
    public void setDestination(int destination) { this.destination = destination; }
}
