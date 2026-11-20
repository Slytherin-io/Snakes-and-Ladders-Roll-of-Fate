package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.Dialogue;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;

import java.util.Random;

public abstract class Tile {
    private int position;
    private String type;
    private String effect;
    protected static Dialogue dialogue = new Dialogue();
    private static final Random tileGen = new Random();
    private EventCategory category;

    public Tile(int position, String type, String effect, EventCategory category) {
        this.position = position;
        this.type = type;
        this.effect = effect;
        this.category = category;
    }


    public static Tile createRandom(int position, String previousType) {
        if (position == 1 || position == 100) {
            return new NormalTile(position);
        }

        int pickType;

        // Prevent consecutive Ladders or Snakes
        if ("LADDER".equals(previousType)) {
            // Pick between Normal (0) and Snake (2)
            pickType = (tileGen.nextInt(2) == 0) ? 0 : 2;
        } else if ("SNAKE".equals(previousType)) {
            // Pick between Normal (0) and Ladder (1)
            pickType = tileGen.nextInt(2); // Returns 0 or 1
        } else {
            // Normal or null (first tile), pick any of the 3
            pickType = tileGen.nextInt(3);
        }

        switch (pickType) {
            case 0:
                return new NormalTile(position);
            case 1:
                // LADDER: Ensure it actually moves forward and doesn't pass 100
                int maxLadderJump = Math.min(20, 100 - position);
                int ladderDest = position + 1 + tileGen.nextInt(maxLadderJump);
                return new LadderTile(position, ladderDest);
            case 2:
                // SNAKE: Ensure it actually moves backward and doesn't pass 1
                int maxSnakeDrop = Math.min(20, position - 1);
                int snakeDest = position - 1 - tileGen.nextInt(maxSnakeDrop);
                return new SnakeTile(position, snakeDest);
            default:
                return new NormalTile(position);
        }
    }

    public abstract void onLand(Player player);

    // Getters and Setters
    public int getPosition() { return position; }

    public void setPosition(int position) { this.position = position; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public String getEffect() { return effect; }

    public void setEffect(String effect) { this.effect = effect; }

    public EventCategory getEventCategory() { return category; }
}
