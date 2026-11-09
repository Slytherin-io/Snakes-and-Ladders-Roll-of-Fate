package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import java.util.ArrayList;
import java.util.List;

public class EventChoice {
    private String text;
    private List<EventEffect> effects;

    public EventChoice(String text) {
        this.text = text;
        this.effects = new ArrayList<>();
    }

    public EventChoice withEffect(EventEffectType type, int value) {
        this.effects.add(new EventEffect(type, value));
        return this;
    }

    public void execute(Player player) {
        if (effects.isEmpty()) {
            System.out.println("Nothing happens. You safely proceed");
            return;
        }
        
        for (EventEffect effect : effects) {
            effect.apply(player);
        }
    }

    public String getText() { return text; }
}
