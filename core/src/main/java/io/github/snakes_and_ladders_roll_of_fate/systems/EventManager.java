package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventManager {
    private List<Event> events;
    private Event activeEvent;
    private Random random;

    public EventManager() {
        this.events = new ArrayList<>();
        this.random = new Random();
        loadDefaultEvents();
    }

    private void loadDefaultEvents() {
        this.events = EventRepository.getDefaultEvents();
    }

    // Selects an event specifically eligible for the player's current tile
    public Event selectEventForTile(int playerTilePosition) {
        List<Event> eligibleEvents = new ArrayList<>();

        for (Event event : events) {
            if (event.isEligibleForTile(playerTilePosition)) {
                eligibleEvents.add(event);
            }
        }

        // If no events match this tile, return null
        if (eligibleEvents.isEmpty()) {
            this.activeEvent = null;
            return null;
        }

        // Pick randomly among the eligible events
        this.activeEvent = eligibleEvents.get(random.nextInt(eligibleEvents.size()));
        return activeEvent;
    }

    // Fallback: Selects any event regardless of tile position
    public Event selectEvent() {
        if (events.isEmpty()) return null;
        this.activeEvent = events.get(random.nextInt(events.size()));
        return activeEvent;
    }

    public void triggerEvent() {
        if (activeEvent != null) {
            activeEvent.trigger();
        }
    }

    public void triggerEvent(Player player) {
        triggerEvent();
    }

    public void resolveEvent(int choiceIndex, Player player) {
        if (activeEvent != null) {
            activeEvent.resolve(choiceIndex, player);
        }
    }

    public void addEvent(Event event) {
        this.events.add(event);
    }

    // Getters & Setters
    public Event getActiveEvent() {
        return activeEvent;
    }

    public List<Event> getEvents() {
        return events;
    }
}
