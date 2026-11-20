package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.items.ItemType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventRepository {

    // --- PROCEDURAL EVENT GENERATORS (MYSTERIOUS & UNPREDICTABLE) ---

    // Generates a dynamic Merchant encounter with Blood, Knowledge, and Barter trades
    public static Event createProceduralMerchantEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateMerchantStory();
        Event merchant = new Event(story[0], story[1], EventCategory.MERCHANT, 1, 100);

        // Choice 1: Blood Rite (Sacrifice 20 HP for Iron Sword)
        merchant.addChoice(EventChoice.createBloodCostChoice(
            "Blood Rite: Sacrifice 20 HP for an Iron Sword (+4 ATK)",
            20,
            Item.createEquipment("Iron Sword", EventEffectType.ATTACK, 4, 2, 4, "+4 ATK (1 Turn Buff)")
        ));

        // Choice 2: Knowledge Tribute (Pay 35 EXP for Reinforced Shield)
        merchant.addChoice(EventChoice.createExpCostChoice(
            "Knowledge Tribute: Pay 35 EXP for a Reinforced Shield (+4 DEF)",
            35,
            Item.createEquipment("Reinforced Shield", EventEffectType.DEFENSE, 4, 2, 5, "+4 DEF (1 Turn Buff)")
        ));

        // Choice 3: Barter (Trade any item from your bag for Greater Health Elixir)
        merchant.addChoice(EventChoice.createBarterChoice(
            "Barter: Trade any item from your bag for a Greater Health Elixir (+50 HP)",
            Item.createConsumable("Greater Health Elixir", EventEffectType.HEALTH, 50, "Restores 50 HP")
        ));

        // Choice 4: Decline
        merchant.addChoice(new EventChoice("Decline and walk away safely"));

        return merchant;
    }

    // Generates a dynamic Shrine event with a generated story and 2 shuffled choices
    public static Event createProceduralShrineEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateShrineStory();
        Event shrine = new Event(story[0], story[1], EventCategory.SHRINE, 1, 100);

        List<EventChoice> choiceDeck = new ArrayList<>();
        choiceDeck.add(new EventChoice("Pray for vitality").withEffect(EventEffectType.HEALTH, 15, 35));
        choiceDeck.add(new EventChoice("Pray for martial strength").withEffect(EventEffectType.ATTACK, 1, 3));
        choiceDeck.add(new EventChoice("Pray for protective aegis").withEffect(EventEffectType.DEFENSE, 1, 2));
        choiceDeck.add(new EventChoice("Offer blood upon the altar")
            .withEffect(EventEffectType.HEALTH, -10)
            .withEffect(EventEffectType.EXPERIENCE, 40, 75));
        choiceDeck.add(new EventChoice("Commune with the ancient spirit", 11)
            .onSuccess(EventEffectType.ATTACK, 2, 4)
            .onSuccess(EventEffectType.HEALTH, 15, 25)
            .onFailure(EventEffectType.HEALTH, -15, -8));
        choiceDeck.add(new EventChoice("Reach into the altar's glowing hollow", 12)
            .onSuccess(EventEffectType.EXPERIENCE, 50, 90)
            .onSuccess(EventEffectType.DEFENSE, 1, 2)
            .onFailure(EventEffectType.HEALTH, -20, -10));
        choiceDeck.add(new EventChoice("Pry open the stone relic box behind the altar", 12)
            .onSuccess(EventEffectType.ITEM)
            .onFailure(EventEffectType.HEALTH, -15, -8));

        // Shuffle deck and pick 2 unique choices
        Collections.shuffle(choiceDeck);
        shrine.addChoice(choiceDeck.get(0));
        shrine.addChoice(choiceDeck.get(1));
        shrine.addChoice(new EventChoice("Walk away peacefully")); // Guaranteed safe exit

        return shrine;
    }

    // Generates a dynamic Snake event with a generated story and shuffled reaction choice
    public static Event createProceduralSnakeEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateSnakeStory();
        Event snake = new Event(story[0], story[1], EventCategory.SNAKE, 1, 100);

        List<EventChoice> daringChoices = new ArrayList<>();
        daringChoices.add(EventChoice.createEquipmentAssistedChoice(
            "Deflect the serpent's fangs with your shield (Uses 1 durability)",
            ItemType.EQUIPMENT
        ));
        daringChoices.add(new EventChoice("Attempt to parry the serpent with your blade", 11)
            .onSuccess(EventEffectType.EXPERIENCE, 25, 45)
            .onFailure(EventEffectType.HEALTH, -20, -10));
        daringChoices.add(new EventChoice("Brace behind your shield for the strike", 9)
            .onSuccess(EventEffectType.DEFENSE, 1)
            .onFailure(EventEffectType.HEALTH, -15, -8));
        daringChoices.add(new EventChoice("Vault aggressively over the coils", 13)
            .onSuccess(EventEffectType.MOVE, 1)
            .onFailure(EventEffectType.HEALTH, -25, -15));

        Collections.shuffle(daringChoices);
        snake.addChoice(daringChoices.get(0)); // Daring challenge option
        snake.addChoice(new EventChoice("Flee down the slope")); // Safe fallback option

        return snake;
    }

    // Generates a dynamic Ladder event with a generated story and shuffled climb choices
    public static Event createProceduralLadderEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateLadderStory();
        Event ladder = new Event(story[0], story[1], EventCategory.LADDER, 1, 100);

        List<EventChoice> climbChoices = new ArrayList<>();
        climbChoices.add(new EventChoice("Study the glowing base runes")
            .withEffect(EventEffectType.EXPERIENCE, 25, 50));
        climbChoices.add(new EventChoice("Channel celestial energy into your weapon", 10)
            .onSuccess(EventEffectType.ATTACK, 2, 4)
            .onFailure(EventEffectType.HEALTH, -10, -5));
        climbChoices.add(new EventChoice("Inspect the reinforced iron rungs")
            .withEffect(EventEffectType.DEFENSE, 1, 2));

        Collections.shuffle(climbChoices);
        ladder.addChoice(new EventChoice("Ascend directly to the summit"));
        ladder.addChoice(climbChoices.get(0));
        ladder.addChoice(climbChoices.get(1));

        return ladder;
    }

    // --- STATIC DEFAULT EVENT DECK (Backwards Compatible) ---
    public static List<Event> getDefaultEvents() {
        List<Event> events = new ArrayList<>();

        // Peddler (Tiles 1-100)
        Event peddler = new Event("Wandering Peddler", "A shady dealer offers a forbidden potion.", EventCategory.MERCHANT, 1, 100);
        peddler.addChoice(EventChoice.createBloodCostChoice("Drink the blood draught (Cost: 15 HP for 40 EXP)", 15, null)
            .withEffect(EventEffectType.EXPERIENCE, 40));
        peddler.addChoice(new EventChoice("Decline the offer"));
        events.add(peddler);

        // Springboard Trap (Tiles 20-80)
        Event trap = new Event("Hidden Springboard", "A mechanical plate clicks beneath your boots!", EventCategory.TRAP, 20, 80);
        trap.addChoice(new EventChoice("Ride the mechanical launch forward!").withEffect(EventEffectType.MOVE, 2));
        trap.addChoice(new EventChoice("Dive to the stone floor").withEffect(EventEffectType.HEALTH, -15, -5));
        events.add(trap);

        // Poison Dart Trap with equipment deflection
        Event dartTrap = new Event("Poison Dart Trap", "Concealed wall slots hiss as darts target you!", EventCategory.TRAP, 10, 90);
        dartTrap.addChoice(EventChoice.createEquipmentAssistedChoice("Raise shield to deflect darts (Uses 1 durability)", ItemType.EQUIPMENT));
        dartTrap.addChoice(new EventChoice("Dive into the stone alcove (DC 11)", 11)
            .onSuccess(EventEffectType.EXPERIENCE, 25)
            .onFailure(EventEffectType.HEALTH, -15));
        events.add(dartTrap);

        return events;
    }
}
