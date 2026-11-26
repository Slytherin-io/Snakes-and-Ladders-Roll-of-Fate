package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.items.ItemType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates non-recurring, Act-scaled Equal-Value Dilemmas across all encounter categories.
 * Every option balances the Survival Triangle (Health <-> Distance <-> Power/Items/EXP),
 * eliminating dominant "strictly best" choices and ensuring dynamic variety across runs.
 */
public class EventRepository {

    // =========================================================================
    // 1. WANDERING MERCHANT DILEMMAS (Asymmetric Trade Gambits)
    // =========================================================================
    public static Event createProceduralMerchantEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateMerchantStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event merchant = new Event(story[0], story[1], EventCategory.MERCHANT, 1, 100);

        List<EventChoice> merchantDeck = new ArrayList<>();

        if (act == 1) {
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Blood Rite: Cut your palm upon the merchant's ritual dagger",
                10,
                Item.createEquipment("Bronze Dagger", EventEffectType.ATTACK, 3, 2, 3, "+3 ATK (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Knowledge Tribute: Recite ancient martial lore from memory",
                20,
                Item.createEquipment("Wooden Buckler", EventEffectType.DEFENSE, 3, 2, 4, "+3 DEF (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createBarterChoice(
                "Alchemical Barter: Trade gear for tonic and field notes",
                Item.createConsumable("Minor Health Potion", EventEffectType.HEALTH, 25, "Restores 25 HP")
            ).withEffect(EventEffectType.EXPERIENCE, 20));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Cartographer's Scroll: Trade trail lore to chart the valley ahead",
                15,
                null
            ).withEffect(EventEffectType.SCOUT, 12).withEffect(EventEffectType.HEALTH, 10));
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Smuggler's Passage: Endure a bruising climb through hidden brush",
                12,
                null
            ).withEffect(EventEffectType.MOVE, 3));
            merchantDeck.add(new EventChoice("Shrewd Haggle: Match wits with the sly peddler", 11)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.EXPERIENCE, 15)
                .onFailure(EventEffectType.HEALTH, -10));
        } else if (act == 2) {
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Blood Rite: Swear a blood oath upon the tempered iron blade",
                18,
                Item.createEquipment("Iron Sword", EventEffectType.ATTACK, 5, 2, 4, "+5 ATK (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Knowledge Tribute: Impart strategic battlefield wisdom",
                35,
                Item.createEquipment("Reinforced Shield", EventEffectType.DEFENSE, 5, 2, 5, "+5 DEF (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createBarterChoice(
                "Alchemical Barter: Relinquish gear for elixir and combat lore",
                Item.createConsumable("Greater Health Elixir", EventEffectType.HEALTH, 50, "Restores 50 HP")
            ).withEffect(EventEffectType.EXPERIENCE, 35));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Cartographer's Lens: Pay lore to reveal deep cavern hazards",
                25,
                null
            ).withEffect(EventEffectType.SCOUT, 16).withEffect(EventEffectType.HEALTH, 15));
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Smuggler's Tunnel: Squeeze through jagged stone shortcuts",
                18,
                null
            ).withEffect(EventEffectType.MOVE, 4));
            merchantDeck.add(new EventChoice("Shrewd Haggle: Pressure the stubborn cavern trader", 12)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.EXPERIENCE, 25)
                .onFailure(EventEffectType.HEALTH, -16));
        } else {
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Blood Rite: Offer deep life essence for the mythical dragon blade",
                28,
                Item.createEquipment("Dragon-Slayer Blade", EventEffectType.ATTACK, 7, 3, 5, "+7 ATK (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Knowledge Tribute: Surrender memories of past triumphs for an aegis",
                50,
                Item.createEquipment("Aegis of the Summit", EventEffectType.DEFENSE, 7, 3, 5, "+7 DEF (1 Turn Buff)")
            ));
            merchantDeck.add(EventChoice.createBarterChoice(
                "Alchemical Barter: Trade a relic for ambrosia and enlightenment",
                Item.createConsumable("Celestial Ambrosia", EventEffectType.HEALTH, 80, "Restores 80 HP")
            ).withEffect(EventEffectType.EXPERIENCE, 55));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "Oracle's Astrolabe: Trade battle wisdom to map the summit approach",
                35,
                null
            ).withEffect(EventEffectType.SCOUT, 20).withEffect(EventEffectType.HEALTH, 25));
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "Grappling Ascent: Pay life force for a swift cliffline shortcut",
                25,
                null
            ).withEffect(EventEffectType.MOVE, 5));
            merchantDeck.add(new EventChoice("Shrewd Haggle: Match wits against the celestial merchant", 13)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.EXPERIENCE, 40)
                .onFailure(EventEffectType.HEALTH, -24));
        }

        Collections.shuffle(merchantDeck);
        merchant.addChoice(merchantDeck.get(0));
        merchant.addChoice(merchantDeck.get(1));
        merchant.addChoice(merchantDeck.get(2));
        merchant.addChoice(new EventChoice("Decline and preserve your resources"));
        return merchant;
    }

    // =========================================================================
    // 2. ANCIENT SHRINE DILEMMAS (The Law of Equivalent Exchange)
    // =========================================================================
    public static Event createProceduralShrineEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateShrineStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event shrine = new Event(story[0], story[1], EventCategory.SHRINE, 1, 100);

        List<EventChoice> choiceDeck = new ArrayList<>();

        if (act == 1) {
            choiceDeck.add(new EventChoice("Altar of Wrath: Embrace martial fury at the cost of max vitality")
                .withEffect(EventEffectType.ATTACK, 2)
                .withEffect(EventEffectType.MAX_HEALTH, -12));
            choiceDeck.add(new EventChoice("Stone Vow: Harden your shell while sacrificing offensive edge")
                .withEffect(EventEffectType.DEFENSE, 2)
                .withEffect(EventEffectType.ATTACK, -1));
            choiceDeck.add(new EventChoice("Font of Rebirth: Restore deep vitality at the price of battle focus")
                .withEffect(EventEffectType.HEALTH, 40)
                .withEffect(EventEffectType.EXPERIENCE, -20));
            choiceDeck.add(new EventChoice("Oracle's Brazier: Burn life incense to pierce the fog ahead")
                .withEffect(EventEffectType.HEALTH, -10)
                .withEffect(EventEffectType.SCOUT, 12)
                .withEffect(EventEffectType.EXPERIENCE, 20));
            choiceDeck.add(new EventChoice("Blood Reliquary: Spill blood into the basin to unseal a relic")
                .withEffect(EventEffectType.HEALTH, -14)
                .withEffect(EventEffectType.ITEM)
                .withEffect(EventEffectType.EXPERIENCE, 20));
            choiceDeck.add(new EventChoice("Test of the Old Gods: Reach blindly into the runic hollow", 11)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.ATTACK, 1)
                .onFailure(EventEffectType.HEALTH, -15)
                .onFailure(EventEffectType.MOVE, -1));
        } else if (act == 2) {
            choiceDeck.add(new EventChoice("Altar of Wrath: Forge fierce might by sacrificing max vitality")
                .withEffect(EventEffectType.ATTACK, 3)
                .withEffect(EventEffectType.MAX_HEALTH, -18));
            choiceDeck.add(new EventChoice("Iron Bulwark Vow: Temper your guard at the expense of ferocity")
                .withEffect(EventEffectType.DEFENSE, 3)
                .withEffect(EventEffectType.ATTACK, -1));
            choiceDeck.add(new EventChoice("Font of Rebirth: Wash away wounds by relinquishing hard-won lore")
                .withEffect(EventEffectType.HEALTH, 60)
                .withEffect(EventEffectType.EXPERIENCE, -35));
            choiceDeck.add(new EventChoice("Oracle's Brazier: Offer vitality to chart deep cavern perils")
                .withEffect(EventEffectType.HEALTH, -16)
                .withEffect(EventEffectType.SCOUT, 16)
                .withEffect(EventEffectType.EXPERIENCE, 35));
            choiceDeck.add(new EventChoice("Sacrificial Groove: Bleed upon the basalt altar for ancient gear")
                .withEffect(EventEffectType.HEALTH, -20)
                .withEffect(EventEffectType.ITEM)
                .withEffect(EventEffectType.EXPERIENCE, 30));
            choiceDeck.add(new EventChoice("Commune with Cavern Spirits: Channel volatile mystic energy", 12)
                .onSuccess(EventEffectType.ATTACK, 2)
                .onSuccess(EventEffectType.DEFENSE, 2)
                .onSuccess(EventEffectType.HEALTH, 20)
                .onFailure(EventEffectType.HEALTH, -22)
                .onFailure(EventEffectType.MOVE, -2));
        } else {
            choiceDeck.add(new EventChoice("Altar of Solar Wrath: Claim peak lethality by rending max vitality")
                .withEffect(EventEffectType.ATTACK, 5)
                .withEffect(EventEffectType.MAX_HEALTH, -24));
            choiceDeck.add(new EventChoice("Cosmic Aegis Vow: Solidify armor while forfeiting sharp reflexes")
                .withEffect(EventEffectType.DEFENSE, 4)
                .withEffect(EventEffectType.ATTACK, -2));
            choiceDeck.add(new EventChoice("Font of Ascension: Mend mortal flesh by sacrificing past mastery")
                .withEffect(EventEffectType.HEALTH, 85)
                .withEffect(EventEffectType.EXPERIENCE, -50));
            choiceDeck.add(new EventChoice("Celestial Astrolabe: Bleed vitality to unveil the entire peak")
                .withEffect(EventEffectType.HEALTH, -22)
                .withEffect(EventEffectType.SCOUT, 20)
                .withEffect(EventEffectType.EXPERIENCE, 50));
            choiceDeck.add(new EventChoice("Apex Reliquary: Pay heavy vitality to claim a summit treasure")
                .withEffect(EventEffectType.HEALTH, -28)
                .withEffect(EventEffectType.ITEM)
                .withEffect(EventEffectType.EXPERIENCE, 45));
            choiceDeck.add(new EventChoice("Speak the Forbidden Incantation: Wager fate at the summit altar", 13)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.ATTACK, 3)
                .onSuccess(EventEffectType.DEFENSE, 2)
                .onFailure(EventEffectType.HEALTH, -30)
                .onFailure(EventEffectType.MOVE, -2));
        }

        Collections.shuffle(choiceDeck);
        shrine.addChoice(choiceDeck.get(0));
        shrine.addChoice(choiceDeck.get(1));
        shrine.addChoice(choiceDeck.get(2));

        return shrine;
    }

    // =========================================================================
    // 3. SERPENT AMBUSH DILEMMAS (Combat vs Tactical Counter vs Tactical Plunge)
    // =========================================================================
    public static Event createProceduralSnakeEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateSnakeStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event snake = new Event(story[0], story[1], EventCategory.SNAKE, 1, 100);

        // Option 1: Combat Engagement (Risk HP/Slide for EXP & Tiered Combat Loot)
        snake.addChoice(new EventChoice("Stand & Slay: Draw weapon and engage the serpent in battle")
            .triggersCombat());

        // Option 2: Dynamic Tactical Counter (Avoids sliding down; trades durability, EXP, or HP/Item)
        List<EventChoice> counterPool = new ArrayList<>();
        counterPool.add(EventChoice.createEquipmentAssistedChoice(
            "Brace & Deflect: Raise your equipment to ward off the serpent's fangs",
            ItemType.EQUIPMENT
        ).skipsSpecialMove());

        int bribeExp = (act == 1) ? 18 : ((act == 2) ? 30 : 45);
        counterPool.add(EventChoice.createExpCostChoice(
            "Charm & Vault: Expend cunning battle focus to leap past the coils",
            bribeExp,
            null
        ).withEffect(EventEffectType.MOVE, 3).skipsSpecialMove());

        int dashHpCost = (act == 1) ? 12 : ((act == 2) ? 18 : 26);
        counterPool.add(EventChoice.createBloodCostChoice(
            "Daredevil Sprint: Take a glancing tail lash to vault past the ambush",
            dashHpCost,
            null
        ).withEffect(EventEffectType.MOVE, 2).skipsSpecialMove());

        Collections.shuffle(counterPool);
        snake.addChoice(counterPool.get(0));

        // Option 3: Dynamic Tactical Plunge (Trade board distance for guaranteed resources or recovery!)
        List<EventChoice> plungePool = new ArrayList<>();
        int plungeExp = (act == 1) ? 20 : ((act == 2) ? 35 : 50);
        int plungeHeal = (act == 1) ? 15 : ((act == 2) ? 22 : 32);

        plungePool.add(new EventChoice("Tactical Plunge: Slide down the coils while snatching dropped relics")
            .withEffect(EventEffectType.ITEM)
            .withEffect(EventEffectType.EXPERIENCE, plungeExp));

        plungePool.add(new EventChoice("Controlled Retreat: Slide safely to recover vitality and scout ahead")
            .withEffect(EventEffectType.HEALTH, plungeHeal)
            .withEffect(EventEffectType.SCOUT, 8));

        Collections.shuffle(plungePool);
        snake.addChoice(plungePool.get(0));

        return snake;
    }

    // =========================================================================
    // 4. CELESTIAL LADDER DILEMMAS (Safe Ascent vs Overextend Vault vs Scavenge)
    // =========================================================================
    public static Event createProceduralLadderEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateLadderStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event ladder = new Event(story[0], story[1], EventCategory.LADDER, 1, 100);

        // Choice 1: Standard Safe Ascent
        int safeExp = (act == 1) ? 12 : ((act == 2) ? 18 : 25);
        ladder.addChoice(new EventChoice("Methodical Ascent: Climb steadily to the upper landing")
            .withEffect(EventEffectType.EXPERIENCE, safeExp));

        // Choice 2: Risky Vault (Chance to soar +3/+4 tiles past ladder top, or fall & lose HP)
        int vaultDc = (act == 1) ? 11 : ((act == 2) ? 12 : 13);
        int vaultBonus = (act == 1) ? 3 : 4;
        int vaultFailHp = (act == 1) ? -12 : ((act == 2) ? -18 : -25);
        ladder.addChoice(new EventChoice("Vault the Apex: Risk a reckless leap beyond the highest rung", vaultDc)
            .onSuccess(EventEffectType.MOVE, vaultBonus)
            .onSuccess(EventEffectType.EXPERIENCE, safeExp)
            .onFailure(EventEffectType.HEALTH, vaultFailHp));

        // Choice 3: Harvest Base Relics (Forgo elevation completely for major loot & EXP)
        int scavengeExp = (act == 1) ? 35 : ((act == 2) ? 55 : 80);
        ladder.addChoice(new EventChoice("Scavenge the Rungs: Forgo climbing to pry relics from the base")
            .withEffect(EventEffectType.ITEM)
            .withEffect(EventEffectType.EXPERIENCE, scavengeExp)
            .skipsSpecialMove());

        return ladder;
    }

    // =========================================================================
    // 5. CROSSROADS DILEMMAS (Strategic Route Fork Commitments)
    // =========================================================================
    public static Event createProceduralCrossroadsEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateCrossroadsStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event crossroads = new Event(story[0], story[1], EventCategory.CROSSROADS, 1, 100);

        List<EventChoice> forkDeck = new ArrayList<>();

        int ridgeDmg = (act == 1) ? -12 : ((act == 2) ? -18 : -26);
        int ridgeMove = (act == 3) ? 5 : 4;
        forkDeck.add(new EventChoice("The Jagged Ridge: Scramble across harsh crags for rapid ground")
            .withEffect(EventEffectType.MOVE, ridgeMove)
            .withEffect(EventEffectType.HEALTH, ridgeDmg));

        int valleyHeal = (act == 1) ? 20 : ((act == 2) ? 35 : 50);
        forkDeck.add(new EventChoice("The Pilgrim's Valley: Rest along the gentle brook and chart ahead")
            .withEffect(EventEffectType.HEALTH, valleyHeal)
            .withEffect(EventEffectType.SCOUT, 10));

        int switchbackExp = (act == 1) ? 15 : ((act == 2) ? 25 : 40);
        forkDeck.add(EventChoice.createExpCostChoice(
            "The Smuggler's Switchback: Trade travel wisdom for shortcuts and stash",
            switchbackExp,
            null
        ).withEffect(EventEffectType.MOVE, 2).withEffect(EventEffectType.ITEM));

        int detourDc = (act == 1) ? 10 : ((act == 2) ? 11 : 12);
        forkDeck.add(new EventChoice("Uncharted Wind-Bridge: Gamble across frayed timber spans", detourDc)
            .onSuccess(EventEffectType.MOVE, 5)
            .onSuccess(EventEffectType.EXPERIENCE, 25)
            .onFailure(EventEffectType.MOVE, -2)
            .onFailure(EventEffectType.HEALTH, ridgeDmg / 2));

        Collections.shuffle(forkDeck);
        crossroads.addChoice(forkDeck.get(0));
        crossroads.addChoice(forkDeck.get(1));
        crossroads.addChoice(forkDeck.get(2));

        return crossroads;
    }

    // =========================================================================
    // 6. TREASURE CACHE DILEMMAS (Rigged Chests & Risky Lockpicking)
    // =========================================================================
    public static Event createProceduralTreasureEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateTreasureStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event treasure = new Event(story[0], story[1], EventCategory.TREASURE, 1, 100);

        List<EventChoice> chestDeck = new ArrayList<>();

        int needleDmg = (act == 1) ? -10 : ((act == 2) ? -16 : -24);
        chestDeck.add(new EventChoice("Pry the Lock: Force open the rigged clasp despite the needle trap")
            .withEffect(EventEffectType.ITEM)
            .withEffect(EventEffectType.EXPERIENCE, 15)
            .withEffect(EventEffectType.HEALTH, needleDmg));

        int disarmDc = (act == 1) ? 10 : ((act == 2) ? 11 : 13);
        int disarmExp = (act == 1) ? 30 : ((act == 2) ? 45 : 65);
        chestDeck.add(new EventChoice("Steady Disarm: Carefully unhook the brass tension wire", disarmDc)
            .onSuccess(EventEffectType.ITEM)
            .onSuccess(EventEffectType.EXPERIENCE, disarmExp)
            .onFailure(EventEffectType.HEALTH, needleDmg));

        int safeExp = (act == 1) ? 25 : ((act == 2) ? 40 : 60);
        chestDeck.add(new EventChoice("Salvage & Survey: Leave the inner lock alone and scout the area")
            .withEffect(EventEffectType.EXPERIENCE, safeExp)
            .withEffect(EventEffectType.SCOUT, 10));

        chestDeck.add(EventChoice.createBarterChoice(
            "Counterweight Swap: Place an item on the trigger plate to lift the relic",
            null
        ).withEffect(EventEffectType.ITEM).withEffect(EventEffectType.EXPERIENCE, 25));

        Collections.shuffle(chestDeck);
        treasure.addChoice(chestDeck.get(0));
        treasure.addChoice(chestDeck.get(1));
        treasure.addChoice(chestDeck.get(2));

        return treasure;
    }

    // =========================================================================
    // STATIC AMBIENT DECK (For non-POI Normal Tiles when Ambient Event triggers)
    // =========================================================================
    public static List<Event> getDefaultEvents() {
        List<Event> events = new ArrayList<>();

        // Act I: Ancient Trail Cache (Tiles 1-30)
        Event cache = new Event("Fallen Scout's Pack", "You spot a weathered satchel lodged beneath roots.", EventCategory.TREASURE, 1, 30);
        cache.addChoice(new EventChoice("Scavenge inside despite biting insects").withEffect(EventEffectType.ITEM).withEffect(EventEffectType.HEALTH, -8));
        cache.addChoice(new EventChoice("Inspect the scout's trail map").withEffect(EventEffectType.SCOUT, 8).withEffect(EventEffectType.EXPERIENCE, 15));
        events.add(cache);

        // Act II: Hidden Springboard (Tiles 31-70)
        Event trap = new Event("Hidden Springboard", "A mechanical plate clicks beneath your boots!", EventCategory.TRAP, 31, 70);
        trap.addChoice(new EventChoice("Ride the launch upward despite the hard landing").withEffect(EventEffectType.MOVE, 3).withEffect(EventEffectType.HEALTH, -14));
        trap.addChoice(new EventChoice("Drop into a controlled roll").withEffect(EventEffectType.EXPERIENCE, 25).withEffect(EventEffectType.HEALTH, -6));
        events.add(trap);

        // Act II: Poison Dart Trap (Tiles 31-70)
        Event dartTrap = new Event("Poison Dart Trap", "Concealed wall slots hiss as darts target you!", EventCategory.TRAP, 31, 70);
        dartTrap.addChoice(EventChoice.createEquipmentAssistedChoice("Raise your shield to block the whistling darts", ItemType.EQUIPMENT));
        dartTrap.addChoice(new EventChoice("Dive headfirst into the stone alcove", 11)
            .onSuccess(EventEffectType.EXPERIENCE, 40)
            .onSuccess(EventEffectType.SCOUT, 6)
            .onFailure(EventEffectType.HEALTH, -18));
        events.add(dartTrap);

        // Act III: Dragon's Breath Vent (Tiles 71-99)
        Event dragonVent = new Event("Volcanic Wyrm Vent", "Scalding geothermal steam hisses violently from fissure vents!", EventCategory.TRAP, 71, 99);
        dragonVent.addChoice(EventChoice.createEquipmentAssistedChoice("Brace behind your shield against the scalding steam", ItemType.EQUIPMENT));
        dragonVent.addChoice(new EventChoice("Dash through the steam plumes to gain ground", 13)
            .onSuccess(EventEffectType.MOVE, 3)
            .onSuccess(EventEffectType.EXPERIENCE, 35)
            .onFailure(EventEffectType.HEALTH, -26));
        dragonVent.addChoice(new EventChoice("Circle slowly around the fissure and study the peak").withEffect(EventEffectType.SCOUT, 10));
        events.add(dragonVent);

        return events;
    }
}
