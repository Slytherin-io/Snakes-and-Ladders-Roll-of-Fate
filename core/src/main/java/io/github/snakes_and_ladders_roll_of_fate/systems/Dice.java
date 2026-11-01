package io.github.snakes_and_ladders_roll_of_fate.systems;

import com.badlogic.gdx.math.MathUtils;

public class Dice {
    private int sides;
    private int value;

    public Dice(int sides) {
        this.sides = sides;
        this.value = 1;
    }

    public int roll() {
        this.value = MathUtils.random(1, sides);
        return value;
    }

    // Getters and Setters
    public int getSides() { return sides; }
    public void setSides(int sides) { this.sides = sides; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
