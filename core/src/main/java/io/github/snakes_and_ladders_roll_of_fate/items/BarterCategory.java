package io.github.snakes_and_ladders_roll_of_fate.items;

public enum BarterCategory {
    ELIXIR("Restoration Elixirs", "Restorative potions and vital essences"),
    ARMAMENT("Combat Armaments", "Weapons and shields forged for battle"),
    RELIC("Tactical Relics", "Board-control dice and traversal relics");

    private final String displayName;
    private final String description;

    BarterCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
