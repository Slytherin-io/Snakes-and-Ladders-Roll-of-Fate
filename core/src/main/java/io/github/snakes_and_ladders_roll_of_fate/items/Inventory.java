package io.github.snakes_and_ladders_roll_of_fate.items;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
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
        this(4); // Default 4 slots
    }

    public boolean addItem(Item item) {
        if (isFull()) {
            System.out.println("  [INVENTORY FULL] Cannot carry " + item.getName() + " (Capacity: " + capacity + ")");
            return false;
        }
        items.add(item);
        System.out.println("  [ACQUIRED] Added " + item.getName() + " to inventory.");
        return true;
    }

    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    public Item removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.remove(index);
        }
        return null;
    }

    public boolean replaceItem(int index, Item newItem) {
        if (index >= 0 && index < items.size() && newItem != null) {
            Item oldItem = items.set(index, newItem);
            System.out.println("  [SWAPPED] Discarded " + oldItem.getName() + " and acquired " + newItem.getName() + ".");
            return true;
        }
        return false;
    }

    public boolean useItem(int index, Player player) {
        if (index < 0 || index >= items.size()) {
            System.out.println("  [INVALID SLOT] No item in slot " + (index + 1) + ".");
            return false;
        }

        Item item = items.get(index);
        if (item.getItemType() == ItemType.CURSE) {
            System.out.println("\n  [CURSED] " + item.getName() + " cannot be used or discarded! Cleanse at a Sanctuary or Barter it.");
            return false;
        }
        System.out.println("\n> " + player.getName() + " uses " + item.getName() + "...");
        item.use(player);

        // If the item ran out of durability or was consumed, remove it
        if (item.isBroken()) {
            items.remove(index);
        }
        return true;
    }

    // Overload for backwards compatibility
    public void useItem(int index) {
        if (index >= 0 && index < items.size()) {
            Item item = items.get(index);
            item.reduceDurability();
            if (item.isBroken()) {
                items.remove(index);
            }
        }
    }

    public void displayInventory() {
        System.out.println("\n=== INVENTORY (" + items.size() + "/" + capacity + " SLOTS) ===");
        if (items.isEmpty()) {
            System.out.println("  (Empty)");
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getItemType() == ItemType.CURSE) {
                System.out.printf("  [%d] %s [CURSE] - %s%n",
                    (i + 1), item.getName(), item.getEffect());
            } else if (item.getItemType() == ItemType.EQUIPMENT) {
                System.out.printf("  [%d] %s [1-Use Relic] - %s%n",
                    (i + 1), item.getName(), item.getEffect());
            } else {
                System.out.printf("  [%d] %s - %s%n",
                    (i + 1), item.getName(), item.getEffect());
            }
        }
    }

    public boolean isFull() {
        return items.size() >= capacity;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int size() {
        return items.size();
    }

    public Item getItem(int index) {
        if (index >= 0 && index < items.size()) {
            return items.get(index);
        }
        return null;
    }

    // Getters and Setters
    public List<Item> getItems() { return items; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
}
