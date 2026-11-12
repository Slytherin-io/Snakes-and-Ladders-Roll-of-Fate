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
    public Event selectEventForTile(int playerTilePosition, EventCategory category) {
        if (category == EventCategory.SNAKE) {
            this.activeEvent = EventRepository.createProceduralSnakeEvent(playerTilePosition);
            return activeEvent;
        }

        if (category == EventCategory.LADDER) {
            this.activeEvent = EventRepository.createProceduralLadderEvent(playerTilePosition);
            return activeEvent;
        }

        // For NORMAL / ambient encounters: 50% chance to generate a procedural Shrine, or roll from existing pool
        if (random.nextBoolean()) {
            this.activeEvent = EventRepository.createProceduralShrineEvent(playerTilePosition);
            return activeEvent;
        }

        List<Event> eligibleEvents = new ArrayList<>();

        for (Event event : events) {
            boolean matchesTile = event.isEligibleForTile(playerTilePosition);
            boolean matchesCategory;

            if (category == null || category == EventCategory.NORMAL) {
                // Normal / general tiles match ambient categories (SHRINE, MERCHANT, TRAP, BLESSING, NORMAL)
                matchesCategory = (event.getCategory() != EventCategory.SNAKE && event.getCategory() != EventCategory.LADDER);
            } else {
                matchesCategory = (event.getCategory() == category);
            }

            if (matchesTile && matchesCategory) {
                eligibleEvents.add(event);
            }
        }

        // Fallback to a procedural shrine if pool is empty
        if (eligibleEvents.isEmpty()) {
            this.activeEvent = EventRepository.createProceduralShrineEvent(playerTilePosition);
            return activeEvent;
        }

        // Pick randomly among the eligible events
        this.activeEvent = eligibleEvents.get(random.nextInt(eligibleEvents.size()));
        return activeEvent;
    }

    public Event selectEventForTile(int playerTilePosition) {
        return selectEventForTile(playerTilePosition, null);
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
