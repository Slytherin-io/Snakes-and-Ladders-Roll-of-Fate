package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import java.util.List;
import java.util.function.BiConsumer;

public class Event {
    private String title;
    private String description;
    private List<String> choices;
    private BiConsumer<Player, Integer> outcome;
    private Player targetPlayer;

    public Event(String title, String description, List<String> choices) {
        this(title, description, choices, null);
    }

    public Event(String title, String description, List<String> choices, BiConsumer<Player, Integer> outcome) {
        this.title = title;
        this.description = description;
        this.choices = choices;
        this.outcome = outcome;
    }

    public void trigger() {
        System.out.println("\n📜 === [EVENT] " + title.toUpperCase() + " ===");
        System.out.println(description);
        System.out.println("What will you do?");
        if (choices != null) {
            for (int i = 0; i < choices.size(); i++) {
                System.out.printf("  [%d] %s%n", (i + 1), choices.get(i));
            }
        }
    }

    public void trigger(Player player) {
        this.targetPlayer = player;
        trigger();
    }

    public void resolve(int choiceIndex) {
        if (targetPlayer != null) {
            resolve(choiceIndex, targetPlayer);
        } else {
            System.out.println("No active player associated with this event.");
        }
    }

    public void resolve(int choiceIndex, Player player) {
        if (choices == null || choiceIndex < 1 || choiceIndex > choices.size()) {
            System.out.println("⚠️ Invalid choice. The opportunity fades away.");
            return;
        }

        System.out.println("\n> " + player.getName() + " chose: " + choices.get(choiceIndex - 1));
        if (outcome != null) {
            outcome.accept(player, choiceIndex);
        }
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getChoices() { return choices; }

    public void setOutcome(BiConsumer<Player, Integer> outcome) {
        this.outcome = outcome;
    }
}
