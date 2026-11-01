package io.github.snakes_and_ladders_roll_of_fate.systems;

import java.util.List;

public class Event {
    private String title;
    private String description;
    private List<String> choices;

    public Event(String title, String description, List<String> choices) {
        this.title = title;
        this.description = description;
        this.choices = choices;
    }

    public void trigger() {
        // Will implement later
    }

    public void resolve(int choiceIndex) {
        // Will implement later
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getChoices() { return choices; } 
}
