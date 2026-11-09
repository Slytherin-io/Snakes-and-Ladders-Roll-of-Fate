package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private String title;
    private String description;
    private EventCategory category;
    private int minTile;
    private int maxTile;
    private List<EventChoice> choices;

    // Full constructor with tile bounds
    public Event(String title, String description, EventCategory category, int minTile, int maxTile) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.minTile = minTile;
        this.maxTile = maxTile;
        this.choices = new ArrayList<>();
    }

    // Overload defaulting to the entire board (Tiles 1-100)
    public Event(String title, String description, EventCategory category) {
        this(title, description, category, 1, 100);
    }

    public void addChoice(EventChoice choice) {
        this.choices.add(choice);
    } 

    // Checks if the player's current tile falls within this event's allowed range
    public boolean isEligibleForTile(int tilePosition) {
        return tilePosition >= minTile && tilePosition <= maxTile;
    }

    public void trigger() {
        System.out.printf("%n=== [EVENT: %s] %s ===%n", category, title.toUpperCase());
        System.out.println(description);
        System.out.println("What will you do?");
        for (int i = 0; i < choices.size(); i++) {
            System.out.printf("  [%d] %s%n", (i + 1), choices.get(i).getText());
        }
    }

    public void resolve(int choiceIndex, Player player) {
        if (choiceIndex < 1 || choiceIndex > choices.size()) {
            System.out.println("Invalid choice! The moment is lost.");
            return;
        }
        EventChoice chosen = choices.get(choiceIndex - 1);
        System.out.println("\n> You chose: " + chosen.getText());
        chosen.execute(player);
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public EventCategory getCategory() { return category; }
    public int getMinTile() { return minTile; }
    public int getMaxTile() { return maxTile; }
    public List<EventChoice> getChoices() { return choices; }
}
