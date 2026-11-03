package io.github.snakes_and_ladders_roll_of_fate.systems;

import java.util.Random;

public class Dice {
    private int sides;
    private int value;
    private Random random;

    public Dice(int sides) {
        this.sides = sides;
        this.value = 1;
        this.random = new Random();
    }

    public int roll() {
        this.value = random.nextInt(sides) + 1;
        return value;
    }

    // Getters and Setters
    public int getSides() { return sides; }
    public void setSides(int sides) { this.sides = sides; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
}
