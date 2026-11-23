package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.items.ItemType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventRepository {

    // --- PROCEDURAL EVENT GENERATORS (ACT-AWARE & GROUNDED) ---

    // Generates a dynamic Merchant encounter scaled to Act I, Act II, or Act III
    public static Event createProceduralMerchantEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateMerchantStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event merchant = new Event(story[0], story[1], EventCategory.MERCHANT, 1, 100);

        List<EventChoice> merchantDeck = new ArrayList<>();

        if (act == 1) {
            // Act I: Gentle beginner trades (Shuffled pool)
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Blood Rite: Cut your palm upon the merchant's ritual dagger",
                10,
                Item.createEquipment("Bronze Dagger", EventEffectType.ATTACK, 2, 2, 3, "+2 ATK (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Knowledge Tribute: Recite ancient martial lore from memory",
                20,
                Item.createEquipment("Wooden Buckler", EventEffectType.DEFENSE, 2, 2, 4, "+2 DEF (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createBarterChoice(
                "Barter: Trade a relic from your pack for herbal medicine",
                Item.createConsumable("Minor Health Potion", EventEffectType.HEALTH, 25, "Restores 25 HP")
            ));
            merchantDeck.add(new EventChoice("Haggle: Negotiate sharply with the peddler", 10)
                .onSuccess(Item.createConsumable("Minor Health Potion", EventEffectType.HEALTH, 20, "Restores 20 HP"))
                .onFailure(EventEffectType.EXPERIENCE, 8));
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Mystery Relic: Offer life essence for a sealed package",
                8,
                null
            ).onSuccess(EventEffectType.ITEM));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Tribute for Provisions: Share field lore for refreshing spring water",
                15,
                null
            ).onSuccess(EventEffectType.HEALTH, 15, 25));
        } else if (act == 2) {
            // Act II: Substantial mid-game trades (Shuffled pool)
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Blood Rite: Swear a blood oath upon the tempered iron blade",
                20,
                Item.createEquipment("Iron Sword", EventEffectType.ATTACK, 4, 2, 4, "+4 ATK (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Knowledge Tribute: Impart strategic battlefield wisdom",
                35,
                Item.createEquipment("Reinforced Shield", EventEffectType.DEFENSE, 4, 2, 5, "+4 DEF (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createBarterChoice(
                "Barter: Relinquish gear in exchange for an alchemical elixir",
                Item.createConsumable("Greater Health Elixir", EventEffectType.HEALTH, 50, "Restores 50 HP")
            ));
            merchantDeck.add(new EventChoice("Haggle: Pressure the stubborn cavern trader", 11)
                .onSuccess(Item.createConsumable("Greater Health Elixir", EventEffectType.HEALTH, 40, "Restores 40 HP"))
                .onFailure(EventEffectType.EXPERIENCE, 18));
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Mystery Relic: Bind your blood to an unsealed iron coffer",
                16,
                null
            ).onSuccess(EventEffectType.ITEM));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Tribute for Concoction: Impart lost secrets for restorative draft",
                30,
                null
            ).onSuccess(EventEffectType.HEALTH, 30, 45));
        } else {
            // Act III: Mythic summit trades for high stakes (Shuffled pool)
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Blood Rite: Offer deep life essence for the mythical dragon blade",
                35,
                Item.createEquipment("Dragon-Slayer Blade", EventEffectType.ATTACK, 6, 3, 5, "+6 ATK (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Knowledge Tribute: Surrender memories of past triumphs for the summit's aegis",
                60,
                Item.createEquipment("Aegis of the Summit", EventEffectType.DEFENSE, 6, 3, 5, "+6 DEF (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createBarterChoice(
                "Barter: Sacrifice your finest inventory treasure for divine ambrosia",
                Item.createConsumable("Celestial Ambrosia", EventEffectType.HEALTH, 80, "Restores 80 HP")
            ));
            merchantDeck.add(new EventChoice("Haggle: Match wits against the celestial merchant", 13)
                .onSuccess(Item.createConsumable("Celestial Ambrosia", EventEffectType.HEALTH, 65, "Restores 65 HP"))
                .onFailure(EventEffectType.EXPERIENCE, 30));
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Mystery Relic: Pierce your heart's flame for an apex curiosity",
                28,
                null
            ).onSuccess(EventEffectType.ITEM));
        }

        Collections.shuffle(merchantDeck);
        // Present 2-3 unpredictable wares/deals
        merchant.addChoice(merchantDeck.get(0));
        merchant.addChoice(merchantDeck.get(1));
        if (merchantDeck.size() > 2) {
            merchant.addChoice(merchantDeck.get(2));
        }
        merchant.addChoice(new EventChoice("Decline and walk away safely"));
        return merchant;
    }

    // Generates a dynamic Shrine event with rich, randomized choice decks
    public static Event createProceduralShrineEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateShrineStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event shrine = new Event(story[0], story[1], EventCategory.SHRINE, 1, 100);

        List<EventChoice> choiceDeck = new ArrayList<>();

        if (act == 1) {
            // Act I Choices (5 varied options, shuffle and pick 2)
            choiceDeck.add(new EventChoice("Kneel and pray for peaceful vitality")
                .withEffect(EventEffectType.HEALTH, 15, 25));
            choiceDeck.add(new EventChoice("Meditate upon martial focus")
                .withEffect(EventEffectType.ATTACK, 1, 2));
            choiceDeck.add(new EventChoice("Whisper prayers for a protective ward")
                .withEffect(EventEffectType.DEFENSE, 1, 2));
            choiceDeck.add(new EventChoice("Offer fresh drops of blood upon the stone altar")
                .withEffect(EventEffectType.HEALTH, -8)
                .withEffect(EventEffectType.EXPERIENCE, 30, 50));
            choiceDeck.add(new EventChoice("Carefully search the mossy hollow beneath the statue", 10)
                .onSuccess(EventEffectType.ITEM)
                .onFailure(EventEffectType.HEALTH, -8));
            choiceDeck.add(new EventChoice("Sip from the clear mountain runoff basin")
                .withEffect(EventEffectType.HEALTH, 10, 20));
        } else if (act == 2) {
            // Act II Choices (6 varied options, shuffle and pick 2)
            choiceDeck.add(new EventChoice("Pray for scorching restorative warmth")
                .withEffect(EventEffectType.HEALTH, 25, 40));
            choiceDeck.add(new EventChoice("Channel the mountain's furious battle spirit")
                .withEffect(EventEffectType.ATTACK, 2, 3));
            choiceDeck.add(new EventChoice("Grasp the iron obelisk to fortify your defenses")
                .withEffect(EventEffectType.DEFENSE, 2, 3));
            choiceDeck.add(new EventChoice("Spill blood onto the sacrificial groove")
                .withEffect(EventEffectType.HEALTH, -15)
                .withEffect(EventEffectType.EXPERIENCE, 50, 80));
            choiceDeck.add(new EventChoice("Attempt to commune with the restless cavern phantom", 11)
                .onSuccess(EventEffectType.ATTACK, 2, 4)
                .onSuccess(EventEffectType.HEALTH, 15, 25)
                .onFailure(EventEffectType.HEALTH, -15));
            choiceDeck.add(new EventChoice("Reach deep into the dark basalt relic box", 12)
                .onSuccess(EventEffectType.ITEM)
                .onFailure(EventEffectType.HEALTH, -18));
            choiceDeck.add(new EventChoice("Drink the bubbling volcanic mineral water", 11)
                .onSuccess(EventEffectType.HEALTH, 30, 45)
                .onFailure(EventEffectType.HEALTH, -15));
        } else {
            // Act III Choices (6 high-stakes options, shuffle and pick 2)
            choiceDeck.add(new EventChoice("Bathe in the blinding celestial starlight")
                .withEffect(EventEffectType.HEALTH, 45, 70));
            choiceDeck.add(new EventChoice("Channel the raw solar wrath of the summit")
                .withEffect(EventEffectType.ATTACK, 4, 5));
            choiceDeck.add(new EventChoice("Invoke the ancient cosmic barrier")
                .withEffect(EventEffectType.DEFENSE, 3, 4));
            choiceDeck.add(new EventChoice("Surrender your vital essence to the celestial wyrm")
                .withEffect(EventEffectType.HEALTH, -25)
                .withEffect(EventEffectType.EXPERIENCE, 90, 140));
            choiceDeck.add(new EventChoice("Attempt to unseal the glowing Apex Vault", 13)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.EXPERIENCE, 40)
                .onFailure(EventEffectType.HEALTH, -30));
            choiceDeck.add(new EventChoice("Speak the forbidden cosmic incantation", 13)
                .onSuccess(EventEffectType.ATTACK, 4, 6)
                .onSuccess(EventEffectType.HEALTH, 20, 35)
                .onFailure(EventEffectType.HEALTH, -30));
        }

        Collections.shuffle(choiceDeck);
        shrine.addChoice(choiceDeck.get(0));
        shrine.addChoice(choiceDeck.get(1));
        shrine.addChoice(new EventChoice("Walk away peacefully"));

        return shrine;
    }

    // Generates a dynamic Snake event with combat, shield deflection, and retreat options
    public static Event createProceduralSnakeEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateSnakeStory(tilePosition);
        Event snake = new Event(story[0], story[1], EventCategory.SNAKE, 1, 100);

        snake.addChoice(new EventChoice("Draw weapon and engage the serpent in battle"));
        snake.addChoice(EventChoice.createEquipmentAssistedChoice(
            "Raise your shield to deflect the serpent's fangs",
            ItemType.EQUIPMENT
        ));
        snake.addChoice(new EventChoice("Flee down the slope to safety"));

        return snake;
    }

    // Generates a dynamic Ladder event with climbing dilemmas and shuffled options
    public static Event createProceduralLadderEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateLadderStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event ladder = new Event(story[0], story[1], EventCategory.LADDER, 1, 100);

        ladder.addChoice(new EventChoice("Ascend cautiously along the rungs"));

        List<EventChoice> ladderDeck = new ArrayList<>();

        if (act == 1) {
            ladderDeck.add(new EventChoice("Study the trail markings etched into the timber")
                .withEffect(EventEffectType.EXPERIENCE, 20, 35));
            ladderDeck.add(new EventChoice("Sprint recklessly up two rungs at once", 10)
                .onSuccess(EventEffectType.MOVE, 1)
                .onFailure(EventEffectType.HEALTH, -6));
            ladderDeck.add(new EventChoice("Test the hemp bindings before climbing")
                .withEffect(EventEffectType.DEFENSE, 1));
        } else if (act == 2) {
            ladderDeck.add(new EventChoice("Leap boldly over the missing chasm rungs", 11)
                .onSuccess(EventEffectType.MOVE, 2)
                .onFailure(EventEffectType.HEALTH, -16));
            ladderDeck.add(new EventChoice("Reinforce the creaking rungs with scrap wire", 11)
                .onSuccess(EventEffectType.DEFENSE, 2)
                .onFailure(EventEffectType.HEALTH, -10));
            ladderDeck.add(new EventChoice("Ascend with eyes shut, trusting your footing", 11)
                .onSuccess(EventEffectType.EXPERIENCE, 40, 65)
                .onFailure(EventEffectType.HEALTH, -12));
        } else {
            ladderDeck.add(new EventChoice("Vault across the ethereal bridge of starlight", 13)
                .onSuccess(EventEffectType.MOVE, 3)
                .onFailure(EventEffectType.HEALTH, -28));
            ladderDeck.add(new EventChoice("Channel cosmic energy through the glowing rungs", 12)
                .onSuccess(EventEffectType.ATTACK, 3, 5)
                .onFailure(EventEffectType.HEALTH, -20));
            ladderDeck.add(new EventChoice("Charge through the freezing gales without pausing", 12)
                .onSuccess(EventEffectType.MOVE, 2)
                .onFailure(EventEffectType.HEALTH, -22));
        }

        Collections.shuffle(ladderDeck);
        ladder.addChoice(ladderDeck.get(0));

        return ladder;
    }

    // --- STATIC DEFAULT EVENT DECK (Zoned by Act, Clean Narrative Text) ---
    public static List<Event> getDefaultEvents() {
        List<Event> events = new ArrayList<>();

        // Act I: Ancient Trail Cache (Tiles 1-30)
        Event cache = new Event("Fallen Scout's Pack", "You spot a weathered satchel lodged beneath roots.", EventCategory.BLESSING, 1, 30);
        cache.addChoice(new EventChoice("Scavenge supplies carefully from the satchel").withEffect(EventEffectType.ITEM));
        cache.addChoice(new EventChoice("Leave it undisturbed"));
        events.add(cache);

        // Act II: Hidden Springboard (Tiles 31-70)
        Event trap = new Event("Hidden Springboard", "A mechanical plate clicks beneath your boots!", EventCategory.TRAP, 31, 70);
        trap.addChoice(new EventChoice("Surrender to the mechanical launch plate").withEffect(EventEffectType.MOVE, 2));
        trap.addChoice(new EventChoice("Tumble violently onto the stone floor").withEffect(EventEffectType.HEALTH, -15, -8));
        events.add(trap);

        // Act II: Poison Dart Trap (Tiles 31-70)
        Event dartTrap = new Event("Poison Dart Trap", "Concealed wall slots hiss as darts target you!", EventCategory.TRAP, 31, 70);
        dartTrap.addChoice(EventChoice.createEquipmentAssistedChoice("Raise your shield to block the whistling darts", ItemType.EQUIPMENT));
        dartTrap.addChoice(new EventChoice("Dive headfirst into the stone alcove", 11)
            .onSuccess(EventEffectType.EXPERIENCE, 35)
            .onFailure(EventEffectType.HEALTH, -18));
        events.add(dartTrap);

        // Act III: Dragon's Breath Vent (Tiles 71-99)
        Event dragonVent = new Event("Volcanic Wyrm Vent", "Scalding geothermal steam hisses violently from fissure vents!", EventCategory.TRAP, 71, 99);
        dragonVent.addChoice(EventChoice.createEquipmentAssistedChoice("Brace behind your shield against the scalding steam", ItemType.EQUIPMENT));
        dragonVent.addChoice(new EventChoice("Sprint blindly through the flaming vent", 13)
            .onSuccess(EventEffectType.MOVE, 2)
            .onFailure(EventEffectType.HEALTH, -30));
        dragonVent.addChoice(new EventChoice("Wait cautiously for the steam clouds to subside"));
        events.add(dragonVent);

        return events;
    }
}
