package io.github.snakes_and_ladders_roll_of_fate.entities;

import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;

public enum CharacterClass {
    CAT(
        "Cat",
        "Nimble Prowler",
        90,
        6,
        4,
        15,
        "Feline Agility: High speed and quick strikes in mortal peril.",
        "Cat's Agility Charm",
        "Graceful leap: Advance 2 tiles forward"
    ),
    TURTLE(
        "Turtle",
        "Iron Guardian",
        120,
        4,
        7,
        5,
        "Iron Shell: Heavy armor and natural resilience against hazards.",
        "Turtle Shell Buckler",
        "+3 DEF (1 Turn Buff)"
    ),
    FOX(
        "Fox",
        "Cunning Trickster",
        95,
        7,
        3,
        20,
        "Silver Tongue: Sharp offensive instincts and keen survival senses.",
        "Fox's Hunting Fang",
        "+3 ATK (1 Turn Buff)"
    ),
    BIRD(
        "Bird",
        "Wind Ascendant",
        85,
        5,
        4,
        10,
        "Soaring Wings: Master of vertical elevation and aerial mobility.",
        "Feather of the Gale",
        "Wind gust: Soar 3 tiles forward"
    );

    private final String name;
    private final String title;
    private final int baseHealth;
    private final int baseAttack;
    private final int baseDefense;
    private final int expBonus;
    private final String perkDescription;
    private final String startingItemName;
    private final String startingItemDescription;

    CharacterClass(String name, String title, int baseHealth, int baseAttack, int baseDefense, int expBonus,
                   String perkDescription, String startingItemName, String startingItemDescription) {
        this.name = name;
        this.title = title;
        this.baseHealth = baseHealth;
        this.baseAttack = baseAttack;
        this.baseDefense = baseDefense;
        this.expBonus = expBonus;
        this.perkDescription = perkDescription;
        this.startingItemName = startingItemName;
        this.startingItemDescription = startingItemDescription;
    }

    public String getName() { return name; }
    public String getTitle() { return title; }
    public int getBaseHealth() { return baseHealth; }
    public int getBaseAttack() { return baseAttack; }
    public int getBaseDefense() { return baseDefense; }
    public int getExpBonus(){return expBonus; }
    public String getPerkDescription() { return perkDescription; }
    public String getStartingItemName() { return startingItemName; }
    public String getStartingItemDescription() { return startingItemDescription; }

    public Item getStartingItem() {
        switch (this) {
            case CAT:
                return Item.createConsumable(startingItemName, EventEffectType.MOVE, 2, startingItemDescription);
            case TURTLE:
                return Item.createEquipment(startingItemName, EventEffectType.DEFENSE, 3, 3, 3, startingItemDescription);
            case FOX:
                return Item.createEquipment(startingItemName, EventEffectType.ATTACK, 3, 3, 3, startingItemDescription);
            case BIRD:
                return Item.createConsumable(startingItemName, EventEffectType.MOVE, 3, startingItemDescription);
            default:
                return null;
        }
    }
}
