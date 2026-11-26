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
    // 1. WANDERING MERCHANT DILEMMAS (Physical Barter & Supplies)
    // =========================================================================
    public static Event createProceduralMerchantEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateMerchantStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event merchant = new Event(story[0], story[1], EventCategory.MERCHANT, 1, 100);

        List<EventChoice> merchantDeck = new ArrayList<>();

        if (act == 1) {
            merchantDeck.add(EventChoice.createBarterChoice(
                "[Barter Supplies] Trade 1 item from your bag for a Healing Potion (+25 HP & Lore)",
                Item.createConsumable("Minor Health Potion", EventEffectType.HEALTH, 25, "Restores 25 HP")
            ).withEffect(EventEffectType.EXPERIENCE, 20));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "[Buy Trail Map] Spend 15 EXP to reveal 12 uncharted tiles ahead",
                15,
                null
            ).withEffect(EventEffectType.SCOUT, 12).withEffect(EventEffectType.HEALTH, 10));
            merchantDeck.add(new EventChoice("[Haggle for Mystery Box] Gamble (D20): High roll wins rare loot; fail costs 10 HP", 11)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.EXPERIENCE, 15)
                .onFailure(EventEffectType.HEALTH, -10));
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "[Buy Sharp Dagger] Pay 10 HP blood price for a Bronze Dagger (+3 ATK)",
                10,
                Item.createEquipment("Bronze Dagger", EventEffectType.ATTACK, 3, 2, 3, "+3 ATK (1 Turn Buff)")
            ));
        } else if (act == 2) {
            merchantDeck.add(EventChoice.createBarterChoice(
                "[Barter Supplies] Trade 1 item from your bag for a Greater Elixir (+50 HP & Lore)",
                Item.createConsumable("Greater Health Elixir", EventEffectType.HEALTH, 50, "Restores 50 HP")
            ).withEffect(EventEffectType.EXPERIENCE, 35));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "[Buy Cavern Map] Spend 25 EXP to reveal 16 uncharted tiles ahead",
                25,
                null
            ).withEffect(EventEffectType.SCOUT, 16).withEffect(EventEffectType.HEALTH, 15));
            merchantDeck.add(new EventChoice("[Haggle for Mystery Box] Gamble (D20): High roll wins rare loot; fail costs 16 HP", 12)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.EXPERIENCE, 25)
                .onFailure(EventEffectType.HEALTH, -16));
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "[Buy Tempered Sword] Pay 18 HP blood price for an Iron Sword (+5 ATK)",
                18,
                Item.createEquipment("Iron Sword", EventEffectType.ATTACK, 5, 2, 4, "+5 ATK (1 Turn Buff)")
            ));
        } else {
            merchantDeck.add(EventChoice.createBarterChoice(
                "[Barter Supplies] Trade 1 item from your bag for Celestial Ambrosia (+80 HP & Lore)",
                Item.createConsumable("Celestial Ambrosia", EventEffectType.HEALTH, 80, "Restores 80 HP")
            ).withEffect(EventEffectType.EXPERIENCE, 55));
            merchantDeck.add(EventChoice.createExpCostChoice(
                "[Buy Summit Map] Spend 35 EXP to reveal 20 uncharted tiles ahead",
                35,
                null
            ).withEffect(EventEffectType.SCOUT, 20).withEffect(EventEffectType.HEALTH, 25));
            merchantDeck.add(new EventChoice("[Haggle for Mystery Box] Gamble (D20): High roll wins rare loot; fail costs 24 HP", 13)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.EXPERIENCE, 40)
                .onFailure(EventEffectType.HEALTH, -24));
            merchantDeck.add(EventChoice.createBloodCostChoice(
                "[Buy Dragon Blade] Pay 28 HP blood price for a Dragon-Slayer Blade (+7 ATK)",
                28,
                Item.createEquipment("Dragon-Slayer Blade", EventEffectType.ATTACK, 7, 3, 5, "+7 ATK (1 Turn Buff)")
            ));
        }

        Collections.shuffle(merchantDeck);
        merchant.addChoice(merchantDeck.get(0));
        merchant.addChoice(merchantDeck.get(1));
        merchant.addChoice(merchantDeck.get(2));
        merchant.addChoice(new EventChoice("[Politely Decline] Save your supplies and continue your climb"));
        return merchant;
    }

    // =========================================================================
    // 2. ANCIENT SHRINE DILEMMAS (Physical Relics & Blessings)
    // =========================================================================
    public static Event createProceduralShrineEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateShrineStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event shrine = new Event(story[0], story[1], EventCategory.SHRINE, 1, 100);

        List<EventChoice> choiceDeck = new ArrayList<>();

        if (act == 1) {
            choiceDeck.add(new EventChoice("[Drink Spring Water] Drink clear sacred water to restore 35 HP")
                .withEffect(EventEffectType.HEALTH, 35)
                .withEffect(EventEffectType.EXPERIENCE, 10));
            choiceDeck.add(new EventChoice("[Touch Blazing Runestone] Sacrifice 10 HP to permanently gain +2 Attack")
                .withEffect(EventEffectType.HEALTH, -10)
                .withEffect(EventEffectType.ATTACK, 2)
                .withEffect(EventEffectType.EXPERIENCE, 15));
            choiceDeck.add(new EventChoice("[Search Runic Hollow] Gamble (D20): High roll grants rare loot & power; fail triggers trap!", 11)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.ATTACK, 1)
                .onFailure(EventEffectType.HEALTH, -14)
                .onFailure(EventEffectType.MOVE, -1));
        } else if (act == 2) {
            choiceDeck.add(new EventChoice("[Drink Spring Water] Drink clear sacred water to restore 55 HP")
                .withEffect(EventEffectType.HEALTH, 55)
                .withEffect(EventEffectType.EXPERIENCE, 20));
            choiceDeck.add(new EventChoice("[Touch Blazing Runestone] Sacrifice 16 HP to permanently gain +3 Attack")
                .withEffect(EventEffectType.HEALTH, -16)
                .withEffect(EventEffectType.ATTACK, 3)
                .withEffect(EventEffectType.EXPERIENCE, 25));
            choiceDeck.add(new EventChoice("[Search Runic Hollow] Gamble (D20): High roll grants rare loot & power; fail triggers trap!", 12)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.ATTACK, 2)
                .onFailure(EventEffectType.HEALTH, -20)
                .onFailure(EventEffectType.MOVE, -2));
        } else {
            choiceDeck.add(new EventChoice("[Drink Spring Water] Drink celestial nectar to restore 75 HP")
                .withEffect(EventEffectType.HEALTH, 75)
                .withEffect(EventEffectType.EXPERIENCE, 30));
            choiceDeck.add(new EventChoice("[Touch Blazing Runestone] Sacrifice 22 HP to permanently gain +4 Attack")
                .withEffect(EventEffectType.HEALTH, -22)
                .withEffect(EventEffectType.ATTACK, 4)
                .withEffect(EventEffectType.EXPERIENCE, 35));
            choiceDeck.add(new EventChoice("[Search Runic Hollow] Gamble (D20): High roll grants rare loot & power; fail triggers trap!", 13)
                .onSuccess(EventEffectType.ITEM)
                .onSuccess(EventEffectType.ATTACK, 3)
                .onFailure(EventEffectType.HEALTH, -28)
                .onFailure(EventEffectType.MOVE, -2));
        }

        Collections.shuffle(choiceDeck);
        shrine.addChoice(choiceDeck.get(0));
        shrine.addChoice(choiceDeck.get(1));
        shrine.addChoice(choiceDeck.get(2));

        return shrine;
    }

    // =========================================================================
    // 3. SERPENT AMBUSH DILEMMAS (Combat vs Tactical Bypass vs Tail Slide)
    // =========================================================================
    public static Event createProceduralSnakeEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateSnakeStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event snake = new Event(story[0], story[1], EventCategory.SNAKE, 1, 100);

        // Option 1: Combat Engagement (Defeat serpent to hold tile, earn battle drop & EXP)
        snake.addChoice(new EventChoice("[Draw Weapon & Fight!] Slay serpent in battle to hold ground and claim battle loot!")
            .triggersCombat());

        // Option 2: Tactical Bypass (Avoid sliding down; trades shield, EXP, or a glancing blow)
        List<EventChoice> counterPool = new ArrayList<>();
        counterPool.add(EventChoice.createEquipmentAssistedChoice(
            "[Brace Behind Shield] Raise your shield to deflect the fangs and hold ground safely",
            ItemType.EQUIPMENT
        ).skipsSpecialMove());

        int bribeExp = (act == 1) ? 15 : ((act == 2) ? 25 : 40);
        counterPool.add(EventChoice.createExpCostChoice(
            "[Distract with Travel Lore] Spend cunning travel lore (" + bribeExp + " EXP) to slip past without falling",
            bribeExp,
            null
        ).withEffect(EventEffectType.MOVE, 1).skipsSpecialMove());

        int dashHpCost = (act == 1) ? 10 : ((act == 2) ? 16 : 24);
        counterPool.add(EventChoice.createBloodCostChoice(
            "[Dodge Past Coils] Take a glancing tail lash (-" + dashHpCost + " HP) to hold ground and rush +2 tiles",
            dashHpCost,
            null
        ).withEffect(EventEffectType.MOVE, 2).skipsSpecialMove());

        Collections.shuffle(counterPool);
        snake.addChoice(counterPool.get(0));

        // Option 3: Strategic Tail Slide (Avoids lethal battle, slides safely down the tail)
        int plungeExp = (act == 1) ? 15 : ((act == 2) ? 25 : 35);
        snake.addChoice(new EventChoice("[Slide Down the Tail] Drop safely down the serpent's tail (Avoids combat, drops back)")
            .withEffect(EventEffectType.EXPERIENCE, plungeExp));

        return snake;
    }

    // =========================================================================
    // 4. CELESTIAL LADDER DILEMMAS (Safe Climb vs Apex Leap vs Scavenge)
    // =========================================================================
    public static Event createProceduralLadderEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateLadderStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event ladder = new Event(story[0], story[1], EventCategory.LADDER, 1, 100);

        // Choice 1: Standard Safe Climb
        int safeExp = (act == 1) ? 12 : ((act == 2) ? 18 : 25);
        ladder.addChoice(new EventChoice("[Climb Steadily] Climb safely to the upper landing (+EXP)")
            .withEffect(EventEffectType.EXPERIENCE, safeExp));

        // Choice 2: Risky Apex Leap (Chance to jump past ladder top, or fall and take damage)
        int vaultDc = (act == 1) ? 11 : ((act == 2) ? 12 : 13);
        int vaultBonus = (act == 1) ? 3 : 4;
        int vaultFailHp = (act == 1) ? -10 : ((act == 2) ? -16 : -22);
        ladder.addChoice(new EventChoice("[Leap for the Apex!] Gamble (D20): Soar +" + vaultBonus + " tiles past top, or slip and fall!", vaultDc)
            .onSuccess(EventEffectType.MOVE, vaultBonus)
            .onSuccess(EventEffectType.EXPERIENCE, safeExp)
            .onFailure(EventEffectType.HEALTH, vaultFailHp));

        // Choice 3: Scavenge Base Relics (Stay at base instead of climbing, pry relics)
        int scavengeExp = (act == 1) ? 30 : ((act == 2) ? 45 : 65);
        ladder.addChoice(new EventChoice("[Scavenge the Base] Don't climb: Stay on this tile and pry loose relics from the rungs")
            .withEffect(EventEffectType.ITEM)
            .withEffect(EventEffectType.EXPERIENCE, scavengeExp)
            .skipsSpecialMove());

        return ladder;
    }

    // =========================================================================
    // 5. CROSSROADS DILEMMAS (Physical Route Choices)
    // =========================================================================
    public static Event createProceduralCrossroadsEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateCrossroadsStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event crossroads = new Event(story[0], story[1], EventCategory.CROSSROADS, 1, 100);

        List<EventChoice> forkDeck = new ArrayList<>();

        int ridgeDmg = (act == 1) ? -10 : ((act == 2) ? -16 : -24);
        int ridgeMove = (act == 3) ? 5 : 4;
        forkDeck.add(new EventChoice("[Scramble Up Cliffway] Rush ahead +" + ridgeMove + " tiles (Fast route, takes " + Math.abs(ridgeDmg) + " scraping damage)")
            .withEffect(EventEffectType.MOVE, ridgeMove)
            .withEffect(EventEffectType.HEALTH, ridgeDmg));

        int valleyHeal = (act == 1) ? 25 : ((act == 2) ? 40 : 60);
        forkDeck.add(new EventChoice("[Rest by the Brook] Relax by gentle water: Restore " + valleyHeal + " HP and scout 10 tiles ahead")
            .withEffect(EventEffectType.HEALTH, valleyHeal)
            .withEffect(EventEffectType.SCOUT, 10));

        int detourDc = (act == 1) ? 10 : ((act == 2) ? 11 : 12);
        forkDeck.add(new EventChoice("[Cross Swaying Bridge] Gamble (D20): Rush +5 tiles across frayed ropes, or slip back!", detourDc)
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
    // 6. TREASURE CACHE DILEMMAS (Clear Chest Interactions)
    // =========================================================================
    public static Event createProceduralTreasureEvent(int tilePosition) {
        String[] story = EventStoryGenerator.generateTreasureStory(tilePosition);
        int act = EventStoryGenerator.getAct(tilePosition);
        Event treasure = new Event(story[0], story[1], EventCategory.TREASURE, 1, 100);

        List<EventChoice> chestDeck = new ArrayList<>();

        int needleDmg = (act == 1) ? -10 : ((act == 2) ? -15 : -22);
        int disarmDc = (act == 1) ? 10 : ((act == 2) ? 11 : 12);
        int disarmExp = (act == 1) ? 25 : ((act == 2) ? 40 : 60);
        chestDeck.add(new EventChoice("[Carefully Pick Lock] Gamble (D20): Disarm trap for pristine relic; fail pricks poison needle!", disarmDc)
            .onSuccess(EventEffectType.ITEM)
            .onSuccess(EventEffectType.EXPERIENCE, disarmExp)
            .onFailure(EventEffectType.HEALTH, needleDmg));

        chestDeck.add(new EventChoice("[Force Chest Open] Guaranteed relic & EXP, but trap blast deals " + Math.abs(needleDmg) + " damage")
            .withEffect(EventEffectType.ITEM)
            .withEffect(EventEffectType.EXPERIENCE, 15)
            .withEffect(EventEffectType.HEALTH, needleDmg));

        int safeExp = (act == 1) ? 20 : ((act == 2) ? 35 : 50);
        chestDeck.add(new EventChoice("[Survey Campsite] Leave chest alone: Safely scout 10 tiles ahead and gain EXP")
            .withEffect(EventEffectType.EXPERIENCE, safeExp)
            .withEffect(EventEffectType.SCOUT, 10));

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
