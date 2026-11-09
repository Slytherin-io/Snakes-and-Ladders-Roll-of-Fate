package io.github.snakes_and_ladders_roll_of_fate.systems;

import java.util.ArrayList;
import java.util.List;

public class EventRepository {
    public static List<Event> getDefaultEvents() {
        List<Event> events = new ArrayList<>();
        // Event 1: Ancient Shrine (Tiles 1-35)
        Event shrine = new Event(
            "Ancient Shrine", 
            "A glowing stone shrine radiates holy warmth.", 
            EventCategory.SHRINE, 
            1, 35
        );
        shrine.addChoice(new EventChoice("Pray for vitality").withEffect(EventEffectType.HEALTH, 25));
        shrine.addChoice(new EventChoice("Pray for strength").withEffect(EventEffectType.ATTACK, 2));
        shrine.addChoice(new EventChoice("Walk away peacefully"));
        events.add(shrine);

        // Event 2: Cursed Peddler (Tiles 1-100)
        Event peddler = new Event(
            "Wandering Peddler", 
            "A shady dealer offers a forbidden potion.", 
            EventCategory.MERCHANT, 
            1, 100
        );
        peddler.addChoice(new EventChoice("Drink the blood draught")
            .withEffect(EventEffectType.HEALTH, -10)
            .withEffect(EventEffectType.EXPERIENCE, 40));
        peddler.addChoice(new EventChoice("Decline offer"));
        events.add(peddler);

        // Event 3: Springboard Trap (Tiles 20-80)
        Event trap = new Event(
            "Hidden Springboard", 
            "A mechanical plate clicks beneath your boots!", 
            EventCategory.TRAP, 
            20, 80
        );
        trap.addChoice(new EventChoice("Ride the launch forward!").withEffect(EventEffectType.MOVE, 2));
        trap.addChoice(new EventChoice("Dodge and hit the floor").withEffect(EventEffectType.HEALTH, -10));
        events.add(trap);
        
        return events;
    }
}
