package io.github.snakes_and_ladders_roll_of_fate.items;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<Item> items;
    private int capacity;

    public Inventory(int capacity) {
        this.capacity = capacity;
        this.items = new ArrayList<>();
    }

    public Inventory() {
        this(10);
    }

    public boolean addItem(Item item) {
        if (isFull()) return false;
        return items.add(item);
    }

    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    public void useItem(int index) {
        if (index >= 0 && index < items.size()) {
            Item item = items.get(index);
            item.use();
        }
    }

    public boolean isFull() {
        return items.size() >= capacity;
    }

    // Getters and Setters
    public List<Item> getItems() { return items; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

}
