package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

public abstract class Tile {
    private int position;
    private String type;
    private String effect;

    public Tile(int position, String type, String effect) {
        this.position = position;
        this.type = type;
        this.effect = effect;
    }

    public abstract void onLand(Player player);

    // Getters and Setters
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }

}
