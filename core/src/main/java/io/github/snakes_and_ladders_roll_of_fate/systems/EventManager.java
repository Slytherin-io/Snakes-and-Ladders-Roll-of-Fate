package io.github.snakes_and_ladders_roll_of_fate.systems;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private List<Event> events;
    private Event activeEvent;

    public EventManager() {
        this.events = new ArrayList<>();
    }

    public Event selectEvent() {
        if (events.isEmpty()) return null;
        this.activeEvent = events.get(0);
        return activeEvent;
    }

    public void triggerEvent() {
        if (activeEvent != null) {
            activeEvent.trigger();
        }
    }

    public void resolveEvent(int choiceIndex) {
        if (activeEvent != null) {
            activeEvent.resolve(choiceIndex);
        }
    }

    // Getters & Setters
    public Event getActiveEvent() {
        return activeEvent;
    } 
}
