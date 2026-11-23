package io.github.snakes_and_ladders_roll_of_fate.entities;

import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.items.ItemType;

public class Player extends Entity {
    private String name;
    private Inventory inventory;
    private int level;
    private int experience;
    private int tempAttack = 0;
    private int tempDefense = 0;
    private int turnsSinceLastEvent = 99; // Starts ready for encounter
    private CharacterClass characterClass;

    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.experience = 0;
        this.inventory = new Inventory(4);
    }

    public Player(String name, CharacterClass characterClass) {
        this(name);
        applyCharacterClass(characterClass);
    }

    public void applyCharacterClass(CharacterClass characterClass) {
        this.characterClass = characterClass;
        if (characterClass != null) {
            setEntityClass(characterClass.getName());
            setHealth(characterClass.getBaseHealth());
            setAttack(characterClass.getBaseAttack());
            setDefense(characterClass.getBaseDefense());
            Item startingItem = characterClass.getStartingItem();
            if (startingItem != null) {
                addItem(startingItem);
            }
        }
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    public int getMaxHealth() {
        return characterClass != null ? characterClass.getBaseHealth() : 100;
    }

    public boolean useItem(int slotIndex) {
        if (inventory == null) return false;
        return inventory.useItem(slotIndex, this);
    }

    public void useItem(Item item) {
        if (item != null) {
            item.use(this);
        }
    }

    public boolean addItem(Item item) {
        if (inventory == null) {
            this.inventory = new Inventory(4);
        }
        return inventory.addItem(item);
    }

    public boolean replaceItem(int index, Item newItem) {
        if (inventory == null) return false;
        return inventory.replaceItem(index, newItem);
    }

    public boolean hasItemType(ItemType type) {
        if (inventory == null) return false;
        for (Item item : inventory.getItems()) {
            if (item.getItemType() == type) return true;
        }
        return false;
    }

    public Item findItemByType(ItemType type) {
        if (inventory == null) return null;
        for (Item item : inventory.getItems()) {
            if (item.getItemType() == type) return item;
        }
        return null;
    }

    public boolean hasItemNamed(String name) {
        if (inventory == null || name == null) return false;
        for (Item item : inventory.getItems()) {
            if (name.equalsIgnoreCase(item.getName())) return true;
        }
        return false;
    }

    public void addTempAttack(int amount) {
        this.tempAttack += amount;
    }

    public void addTempDefense(int amount) {
        this.tempDefense += amount;
    }

    public int getTempAttack() { return tempAttack; }
    public int getTempDefense() { return tempDefense; }

    public int getBaseAttack() { return super.getAttack(); }
    public int getBaseDefense() { return super.getDefense(); }

    @Override
    public int getAttack() {
        return super.getAttack() + tempAttack;
    }

    @Override
    public int getDefense() {
        return super.getDefense() + tempDefense;
    }

    public boolean hasActiveBuffs() {
        return tempAttack > 0 || tempDefense > 0;
    }

    public void clearTemporaryBuffs() {
        if (hasActiveBuffs()) {
            System.out.printf("  [BUFF EXPIRED] Turn bonuses worn off. Attack: %d, Defense: %d%n",
                getBaseAttack(), getBaseDefense());
            this.tempAttack = 0;
            this.tempDefense = 0;
        }
    }

    public void gainExperience(int exp) {
        this.experience += exp;
        if (this.experience >= 100) {
            levelUp();
        }
    }

    public void levelUp() {
        this.level++;
        setAttack(getBaseAttack() + 2);   // +2 Attack every level
        setDefense(getBaseDefense() + 1); // +1 Defense every level
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

    public int getTurnsSinceLastEvent() { return turnsSinceLastEvent; }
    public void incrementTurnsSinceLastEvent() { this.turnsSinceLastEvent++; }
    public void resetEventCooldown() { this.turnsSinceLastEvent = 0; }
}
