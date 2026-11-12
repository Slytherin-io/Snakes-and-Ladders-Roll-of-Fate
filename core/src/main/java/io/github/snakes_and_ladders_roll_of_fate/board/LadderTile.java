package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;

public class LadderTile extends Tile {
    private int destination;

    public LadderTile(int position, int destination) {
        super(position, "LADDER", "Climb forward to a higher tile!", EventCategory.LADDER);
        this.destination = destination;
    }

    @Override 
    public void onLand(Player player) {
        System.out.println(dialogue.createLadderDialogue());
        applyEffect(player);
    }

    public void applyEffect(Player player) {
        player.setPosition(destination);
        System.out.println("Going up to " + destination);
    }

    public int getDestination() { return destination; }
    public void setDestination(int destination) { this.destination = destination; }
}
