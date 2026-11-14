package io.github.snakes_and_ladders_roll_of_fate.entities;

import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;

public class Player extends Entity {
    private String name;
    private Inventory inventory;
    private int level;
    private int experience;

    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.experience = 0;
    }

    public void useItem(Item item) {}

    public void gainExperience(int exp) {
        this.experience += exp;
        if (this.experience >= 100) {
            levelUp();
        }
    }

    public void levelUp() {
        this.level++;
        setAttack(getAttack() + 2);   // +2 Attack every level
        setDefense(getDefense() + 1); // +1 Defense every level
        setHealth(getHealth() + 20);// Heal up on level up!
        System.out.println("Leveled Up!");//For level up tests
    }

    public void makeChoice(int choiceIndex) {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) {this.experience = experience;}

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }
}
