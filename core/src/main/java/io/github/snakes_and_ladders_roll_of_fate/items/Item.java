package io.github.snakes_and_ladders_roll_of_fate.items;

public class Item {
    private String name;
    private int amount;
    private int durability;
    private String effect;

    public Item(String name, int amount, int durability, String effect) {
        this.name = name;
        this.amount = amount;
        this.durability = durability;
        this.effect = effect;
    }

    public void use() {
        // Will implement later
    }

    public void enhance() {
        // Will implement later
    }

    public void reduceDurability() {
        if (this.durability > 0) {
            this.durability--;
        }
    }

    // Getters and Setters
    public String getName() { return name; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public int getDurability() { return durability; }

    public String getEffect() { return effect; }
}
