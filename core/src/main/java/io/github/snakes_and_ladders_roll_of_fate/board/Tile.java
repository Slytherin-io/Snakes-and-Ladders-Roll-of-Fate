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
    private boolean revealed = false;

    public Tile(int position, String type, String effect, EventCategory category) {
        this.position = position;
        this.type = type;
        this.effect = effect;
        this.category = category;
        this.revealed = false;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }


    public static Tile createRandom(int position, String previousType) {
        if (position == 1 || position == 100) {
            return new NormalTile(position);
        }

        int roll = tileGen.nextInt(100);
        int pickType; // 0 = Normal, 1 = Ladder, 2 = Snake

        // Weighted spawn rates by Act to ensure balanced board progression
        if (position <= 30) {
            // Act I: 70% Normal, 18% Ladder, 12% Snake (~5 ladders, ~3 snakes)
            if (roll < 70) pickType = 0;
            else if (roll < 88) pickType = 1;
            else pickType = 2;
        } else if (position <= 70) {
            // Act II: 72% Normal, 12% Ladder, 16% Snake (~4 ladders, ~6 snakes)
            if (roll < 72) pickType = 0;
            else if (roll < 84) pickType = 1;
            else pickType = 2;
        } else {
            // Act III: 70% Normal, 6% Ladder, 24% Snake (~1-2 ladders, ~7 snakes)
            if (roll < 70) pickType = 0;
            else if (roll < 76) pickType = 1;
            else pickType = 2;
        }

        // Prevent consecutive Ladders or Snakes (anti-clustering)
        if (pickType == 1 && "LADDER".equals(previousType)) {
            pickType = 0;
        } else if (pickType == 2 && "SNAKE".equals(previousType)) {
            pickType = 0;
        }

        switch (pickType) {
            case 0:
                return new NormalTile(position);
            case 1:
                // LADDER: Ensure meaningful forward jump (at least 6 tiles, up to 18)
                if (position > 93) return new NormalTile(position); // Too close to the end
                int maxLadderJump = Math.min(18, 100 - position);
                int ladderDest = position + 6 + (maxLadderJump > 6 ? tileGen.nextInt(maxLadderJump - 5) : 0);
                return new LadderTile(position, Math.min(99, ladderDest));
            case 2:
                // SNAKE: Ensure meaningful backward drop (at least 6 tiles, up to 20)
                if (position < 7) return new NormalTile(position); // Too close to the start
                int maxSnakeDrop = Math.min(20, position - 1);
                int snakeDest = position - 6 - (maxSnakeDrop > 6 ? tileGen.nextInt(maxSnakeDrop - 5) : 0);
                return new SnakeTile(position, Math.max(1, snakeDest));
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
