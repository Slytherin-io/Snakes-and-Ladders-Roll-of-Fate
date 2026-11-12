package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import java.util.ArrayList;
import java.util.List;

public class EventChoice {
    private String text;
    private int difficultyClass; // If 0, guaranteed outcome without roll
    private List<EventEffect> successEffects;
    private List<EventEffect> failureEffects;
    private static final Dice d20 = new Dice(20);

    // Guaranteed choice constructor
    public EventChoice(String text) {
        this(text, 0);
    }

    // Risky choice constructor requiring a D20 skill check
    public EventChoice(String text, int difficultyClass) {
        this.text = text;
        this.difficultyClass = difficultyClass;
        this.successEffects = new ArrayList<>();
        this.failureEffects = new ArrayList<>();
    }

    // Fluent helper for guaranteed or successful effects (range)
    public EventChoice onSuccess(EventEffectType type, int minValue, int maxValue) {
        this.successEffects.add(new EventEffect(type, minValue, maxValue));
        return this;
    }

    // Fluent helper for guaranteed or successful effects (fixed)
    public EventChoice onSuccess(EventEffectType type, int value) {
        return onSuccess(type, value, value);
    }

    // Fluent helper for failure effects (range)
    public EventChoice onFailure(EventEffectType type, int minValue, int maxValue) {
        this.failureEffects.add(new EventEffect(type, minValue, maxValue));
        return this;
    }

    // Fluent helper for failure effects (fixed)
    public EventChoice onFailure(EventEffectType type, int value) {
        return onFailure(type, value, value);
    }

    // Backwards-compatible alias for guaranteed effects
    public EventChoice withEffect(EventEffectType type, int minValue, int maxValue) {
        return onSuccess(type, minValue, maxValue);
    }

    public EventChoice withEffect(EventEffectType type, int value) {
        return onSuccess(type, value, value);
    }

    // Executes the choice, rolling a D20 if a Difficulty Class is present
    public void execute(Player player) {
        if (difficultyClass > 0) {
            int roll = d20.roll();
            System.out.printf("  [D20 ROLL] Rolled %d against DC %d...%n", roll, difficultyClass);

            if (roll >= difficultyClass) {
                System.out.println("  [SUCCESS] You passed the check!");
                applyEffects(player, successEffects);
            } else {
                System.out.println("  [FAILURE] You failed the check!");
                applyEffects(player, failureEffects);
            }
        } else {
            // Guaranteed choice
            applyEffects(player, successEffects);
        }
    }

    private void applyEffects(Player player, List<EventEffect> effectsList) {
        if (effectsList.isEmpty()) {
            System.out.println("  Nothing happens. You safely proceed.");
            return;
        }
        for (EventEffect effect : effectsList) {
            effect.apply(player);
        }
    }

    // Formats choice label purely as narrative text, keeping difficulty and consequences hidden
    public String getText() {
        return text;
    }

    public int getDifficultyClass() { return difficultyClass; }
    public List<EventEffect> getSuccessEffects() { return successEffects; }
    public List<EventEffect> getFailureEffects() { return failureEffects; }
}
