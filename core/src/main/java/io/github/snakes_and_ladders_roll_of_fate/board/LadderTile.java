package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

public class LadderTile extends Tile {
    private int destination;

    public LadderTile(int position, int destination) {
        super(position, "LADDER", "Climb forward to a higher tile!");
        this.destination = destination;
    }

    @Override 
    public void onLand(Player player) {
        applyEffect(player);
    }

    public void applyEffect(Player player) {
        player.setPosition(destination);
        System.out.println("Going up to " + destination);
    }

    public int getDestination() { return destination; }
    public void setDestination(int destination) { this.destination = destination; }
}
