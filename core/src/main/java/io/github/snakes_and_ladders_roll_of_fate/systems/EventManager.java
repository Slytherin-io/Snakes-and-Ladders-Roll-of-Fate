package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventManager {
    public static final double BASE_AMBIENT_CHANCE = 0.28;
    public static final int COOLDOWN_TURNS = 1;
    public static final double LADDER_EVENT_CHANCE = 0.55;

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

    // Pacing Controller: Checks if an ambient encounter should trigger for the player
    public boolean shouldTriggerAmbientEvent(Player player) {
        if (player == null) return false;
        if (player.getTurnsSinceLastEvent() < COOLDOWN_TURNS) {
            return false;
        }
        // Bad-luck protection: +5% chance for every safe tile traversed beyond the cooldown
        double dynamicChance = BASE_AMBIENT_CHANCE + ((player.getTurnsSinceLastEvent() - COOLDOWN_TURNS) * 0.05);
        return random.nextDouble() < dynamicChance;
    }

    // Ladder Selectivity: 30% chance for special climbing dilemma; 70% direct smooth climb
    public boolean shouldTriggerLadderEvent() {
        return random.nextDouble() < LADDER_EVENT_CHANCE;
    }

    // Selects an event specifically eligible for the player's current tile and act
    public Event selectEventForTile(int playerTilePosition, EventCategory category) {
        if (category == EventCategory.SNAKE) {
            this.activeEvent = EventRepository.createProceduralSnakeEvent(playerTilePosition);
            return activeEvent;
        }

        if (category == EventCategory.LADDER) {
            this.activeEvent = EventRepository.createProceduralLadderEvent(playerTilePosition);
            return activeEvent;
        }

        if (category == EventCategory.MERCHANT) {
            this.activeEvent = EventRepository.createProceduralMerchantEvent(playerTilePosition);
            return activeEvent;
        }

        if (category == EventCategory.SHRINE) {
            this.activeEvent = EventRepository.createProceduralShrineEvent(playerTilePosition);
            return activeEvent;
        }

        if (category == EventCategory.CROSSROADS) {
            this.activeEvent = EventRepository.createProceduralCrossroadsEvent(playerTilePosition);
            return activeEvent;
        }

        if (category == EventCategory.TREASURE || category == EventCategory.BLESSING) {
            this.activeEvent = EventRepository.createProceduralTreasureEvent(playerTilePosition);
            return activeEvent;
        }

        // For plain NORMAL / ambient encounters: 35% Shrine, 25% Merchant, 15% Treasure, 25% static zoned traps
        int ambientRoll = random.nextInt(100);
        if (ambientRoll < 35) {
            this.activeEvent = EventRepository.createProceduralShrineEvent(playerTilePosition);
            return activeEvent;
        } else if (ambientRoll < 60) {
            this.activeEvent = EventRepository.createProceduralMerchantEvent(playerTilePosition);
            return activeEvent;
        } else if (ambientRoll < 75) {
            this.activeEvent = EventRepository.createProceduralTreasureEvent(playerTilePosition);
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
        if (activeEvent != null) {
            activeEvent.trigger(player);
        }
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
