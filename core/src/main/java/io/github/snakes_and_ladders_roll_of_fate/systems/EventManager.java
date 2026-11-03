package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import java.util.ArrayList;
import java.util.Arrays;
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
        // Event 1: Ancient Shrine
        events.add(new Event(
            "Ancient Shrine",
            "You stand before a moss-covered shrine humming with celestial warmth.",
            Arrays.asList("Pray for vigor (+25 HP)", "Pray for power (+2 Attack)", "Leave in peace"),
            (player, choice) -> {
                if (choice == 1) {
                    player.setHealth(player.getHealth() + 25);
                    System.out.println("Divine light restores your vitality! Current HP: " + player.getHealth());
                } else if (choice == 2) {
                    player.setAttack(player.getAttack() + 2);
                    System.out.println("Your weapon hums with holy strength! Current Attack: " + player.getAttack());
                } else {
                    System.out.println("You whisper a respectful prayer and journey onward.");
                }
            }
        ));

        // Event 2: Wandering Peddler
        events.add(new Event(
            "Wandering Peddler",
            "A shady cloaked merchant steps from the mist offering an enchanted satchel.",
            Arrays.asList("Drink strange elixir (+30 EXP, -10 HP)", "Practice defensive guard (+2 Defense)", "Politely decline"),
            (player, choice) -> {
                if (choice == 1) {
                    player.setHealth(Math.max(1, player.getHealth() - 10));
                    player.gainExperience(30);
                    System.out.println("The acrid elixir burns! (-10 HP, +30 EXP). Current HP: " + player.getHealth() + ", EXP: " + player.getExperience());
                } else if (choice == 2) {
                    player.setDefense(player.getDefense() + 2);
                    System.out.println("The peddler teaches you ancient parrying stances! Current Defense: " + player.getDefense());
                } else {
                    System.out.println("You nod and walk past as the peddler melts back into the fog.");
                }
            }
        ));

        // Event 3: Springboard Mechanism
        events.add(new Event(
            "Hidden Springboard Trap",
            "Click! A pressure plate triggers an ancient springboard mechanism beneath your boots!",
            Arrays.asList("Ride the momentum (Launch forward +2 tiles!)", "Tumble sideways (-10 HP, safe dodge)"),
            (player, choice) -> {
                if (choice == 1) {
                    player.move(2);
                    System.out.println("You ride the mechanical spring, vaulting forward 2 tiles! New Position: " + player.getPosition());
                } else {
                    player.setHealth(Math.max(1, player.getHealth() - 10));
                    System.out.println("You dive to the stone floor, dodging the launch but taking a bruise! (-10 HP). Current HP: " + player.getHealth());
                }
            }
        ));
    }

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

    public void resolveEvent(int choiceIndex) {
        if (activeEvent != null) {
            activeEvent.resolve(choiceIndex);
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
