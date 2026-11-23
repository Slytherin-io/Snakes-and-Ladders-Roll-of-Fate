/*package io.github.snakes_and_ladders_roll_of_fate.items;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.systems.Event;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventChoice;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffect;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventRepository;

public class InventoryTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING INVENTORY & EVENT INTEGRATION TESTS ===");

        Player player = new Player("Hero");
        player.setHealth(50);
        player.setAttack(10);
        player.setDefense(10);

        Inventory inv = player.getInventory();
        if (inv == null) throw new AssertionError("Inventory should not be null!");
        if (inv.getCapacity() != 4) throw new AssertionError("Inventory capacity should be 4, was: " + inv.getCapacity());

        // 1. Add Consumable
        System.out.println("\n[Test 1] Adding Consumable...");
        Item potion = Item.createConsumable("Minor Health Potion", EventEffectType.HEALTH, 25, "Restores 25 HP");
        boolean added1 = player.addItem(potion);
        if (!added1) throw new AssertionError("Potion should be added successfully");

        // 2. Add Equipment (fixed durability for testing)
        System.out.println("\n[Test 2] Adding Equipment with 2 Durability...");
        Item shield = new Item("Wooden Shield", ItemType.EQUIPMENT, 2, EventEffectType.DEFENSE, 3, "+3 DEF (1 Turn Buff)");
        boolean added2 = player.addItem(shield);
        if (!added2) throw new AssertionError("Shield should be added successfully");

        // 3. Add 2 more items to fill capacity to 4/4
        System.out.println("\n[Test 3] Filling inventory to 4/4 slots...");
        Item sword = new Item("Iron Sword", ItemType.EQUIPMENT, 3, EventEffectType.ATTACK, 5, "+5 ATK (1 Turn Buff)");
        Item feather = Item.createConsumable("Hermes Feather", EventEffectType.MOVE, 2, "Move 2 tiles");
        player.addItem(sword);
        player.addItem(feather);
        if (!inv.isFull()) throw new AssertionError("Inventory should now be full (4/4)");

        // 4. Try adding 5th item - should be rejected with [INVENTORY FULL]
        System.out.println("\n[Test 4] Attempting to add 5th item when full...");
        Item extra = Item.createConsumable("Extra Potion", EventEffectType.HEALTH, 10, "+10 HP");
        boolean added5 = player.addItem(extra);
        if (added5) throw new AssertionError("5th item should be rejected when full!");

        inv.displayInventory();

        // 5. Test Consumable Usage
        System.out.println("\n[Test 5] Using consumable potion from Slot 1...");
        int initialHp = player.getHealth();
        boolean usedSlot1 = player.useItem(0); // Uses Minor Health Potion
        if (!usedSlot1) throw new AssertionError("Slot 1 usage should succeed");
        if (player.getHealth() != initialHp + 25) {
            throw new AssertionError("Player HP should be " + (initialHp + 25) + ", was " + player.getHealth());
        }
        if (inv.size() != 3) {
            throw new AssertionError("Consumable should be removed after use. Size should be 3, was: " + inv.size());
        }

        inv.displayInventory();

        // 6. Test Equipment Usage, Temporary Turn Buff, Durability Degradation, and Expiration
        System.out.println("\n[Test 6] Testing Equipment Temporary Buff & Durability Degradation...");
        boolean usedShield1 = player.useItem(0); // Uses Wooden Shield (durability 2 -> 1)
        if (!usedShield1) throw new AssertionError("Shield first use should succeed");
        if (player.getDefense() != 13) {
            throw new AssertionError("Total defense should be 13, was: " + player.getDefense());
        }
        if (player.getTempDefense() != 3) {
            throw new AssertionError("Temporary defense buff should be 3, was: " + player.getTempDefense());
        }
        if (player.getBaseDefense() != 10) {
            throw new AssertionError("Base defense should remain 10, was: " + player.getBaseDefense());
        }

        // End of Turn: Buff expires
        System.out.println("\n[Test 7] Ending Turn and Clearing Buffs...");
        player.clearTemporaryBuffs();
        if (player.getDefense() != 10) {
            throw new AssertionError("Defense should return to base 10 after buff expires, was: " + player.getDefense());
        }

        // Second use of shield: durability 1 -> 0, shatters, removes from inventory
        System.out.println("\n[Test 8] Second use of Shield (Durability 1 -> 0, Shatter)...");
        boolean usedShield2 = player.useItem(0);
        if (!usedShield2) throw new AssertionError("Shield second use should succeed");
        if (inv.size() != 2) {
            throw new AssertionError("Shield should shatter and be removed. Size should be 2, was: " + inv.size());
        }
        player.clearTemporaryBuffs();

        // 7. EventEffect Item Grant
        System.out.println("\n[Test 9] EventEffect granting an item...");
        EventEffect grantItemEffect = new EventEffect(Item.createConsumable("Greater Health Elixir", EventEffectType.HEALTH, 50, "Restores 50 HP"));
        grantItemEffect.apply(player);
        if (inv.size() != 3) {
            throw new AssertionError("Inventory size should be 3 after event granted item, was: " + inv.size());
        }
        if (!player.hasItemNamed("Greater Health Elixir")) {
            throw new AssertionError("Player should possess Greater Health Elixir!");
        }

        // 8. Option B: Interactive Swap Handler when full
        System.out.println("\n[Test 10] Testing Option B Interactive Swap on Full Inventory...");
        player.addItem(Item.createConsumable("Stoneskin Potion", EventEffectType.DEFENSE, 2, "+2 DEF"));
        if (!inv.isFull()) throw new AssertionError("Inventory should be 4/4 full now");

        // Set up test swap handler to swap slot 0 (which is Iron Sword)
        final boolean[] swapCalled = { false };
        EventEffect.setItemSwapHandler((p, newItem) -> {
            swapCalled[0] = true;
            p.replaceItem(0, newItem);
        });

        Item holyRelic = Item.createEquipment("Holy Avenger", EventEffectType.ATTACK, 8, 3, 3, "+8 ATK (1 Turn Buff)");
        EventEffect grantRelic = new EventEffect(holyRelic);
        grantRelic.apply(player);

        if (!swapCalled[0]) throw new AssertionError("ItemSwapHandler should have been invoked!");
        if (!player.hasItemNamed("Holy Avenger")) throw new AssertionError("Player should now possess Holy Avenger!");
        if (inv.size() != 4) throw new AssertionError("Inventory should remain at 4/4 capacity!");

        inv.displayInventory();

        // 9. Merchant Encounter: Blood Rite (HP Payment)
        System.out.println("\n[Test 11] Merchant Blood Rite (HP Cost for Item)...");
        Event merchantEvent = new Event("Test Merchant", "Merchant Encounter", EventCategory.MERCHANT, 1, 100);
        merchantEvent.addChoice(EventChoice.createBloodCostChoice("Blood Rite", 20, Item.createEquipment("Iron Sword", EventEffectType.ATTACK, 5, 3, 3, "+5 ATK (1 Turn Buff)")));
        merchantEvent.addChoice(EventChoice.createExpCostChoice("Knowledge Tribute", 35, Item.createEquipment("Amulet of Wisdom", EventEffectType.DEFENSE, 3, 2, 2, "+3 DEF (1 Turn Buff)")));
        merchantEvent.addChoice(EventChoice.createBarterChoice("Barter", Item.createConsumable("Greater Health Elixir", EventEffectType.HEALTH, 50, "Restores 50 HP")));

        int hpBeforeRite = player.getHealth();
        // Choice 1 is Blood Rite (Cost: 20 HP for Iron Sword)
        // Free up a slot for the test
        player.getInventory().removeItem(3);
        merchantEvent.resolve(1, player);
        if (player.getHealth() != hpBeforeRite - 20) {
            throw new AssertionError("Player HP should be reduced by 20, was: " + player.getHealth());
        }
        if (!player.hasItemNamed("Iron Sword")) {
            throw new AssertionError("Player should have received Iron Sword from Blood Rite!");
        }

        // 10. Merchant Encounter: Knowledge Tribute & Requirement Locking
        System.out.println("\n[Test 12] Merchant Knowledge Tribute Requirement Locking...");
        player.setExperience(10); // Less than the 35 EXP required
        EventChoice expChoice = merchantEvent.getChoices().get(1); // Choice 2: Knowledge Tribute (35 EXP)
        if (expChoice.isAvailable(player)) {
            throw new AssertionError("Knowledge Tribute should be locked when EXP < 35!");
        }
        int expBefore = player.getExperience();
        merchantEvent.resolve(2, player); // Attempting to choose locked option
        if (player.getExperience() != expBefore) {
            throw new AssertionError("Locked choice should not deduct EXP!");
        }

        // Now give player enough EXP and retry
        player.setExperience(50);
        if (!expChoice.isAvailable(player)) {
            throw new AssertionError("Knowledge Tribute should be available when EXP >= 35!");
        }
        // Free a slot
        player.getInventory().removeItem(player.getInventory().size() - 1);
        merchantEvent.resolve(2, player);
        if (player.getExperience() != 15) {
            throw new AssertionError("Player EXP should be 50 - 35 = 15, was: " + player.getExperience());
        }

        // 11. Merchant Encounter: Barter (Trade Item for Item)
        System.out.println("\n[Test 13] Merchant Barter (Item for Item)...");
        int invSizeBeforeBarter = player.getInventory().size();
        EventChoice.setItemBarterHandler(p -> p.getInventory().removeItem(0)); // Trade first item
        merchantEvent.resolve(3, player); // Choice 3: Barter
        if (player.getInventory().size() != invSizeBeforeBarter) {
            throw new AssertionError("Inventory size should be identical after 1-for-1 barter trade!");
        }
        if (!player.hasItemNamed("Greater Health Elixir")) {
            throw new AssertionError("Player should have received Greater Health Elixir from Barter!");
        }

        // 12. Equipment-Assisted Hazard Deflection
        System.out.println("\n[Test 14] Equipment-Assisted Trap Deflection...");
        EventChoice shieldDeflect = EventChoice.createEquipmentAssistedChoice("Raise shield against darts", ItemType.EQUIPMENT);
        if (!shieldDeflect.isAvailable(player)) {
            throw new AssertionError("Shield deflection should be available when player has equipment!");
        }
        Item eqItem = player.findItemByType(ItemType.EQUIPMENT);
        int durabilityBefore = eqItem.getDurability();
        shieldDeflect.execute(player);
        if (eqItem.getDurability() != durabilityBefore - 1) {
            throw new AssertionError("Equipment durability should be reduced by 1 on deflection!");
        }

        // 13. Snake Tile & Combat Trigger Harmonization
        System.out.println("\n[Test 15] Snake Tile Trigger Harmonization (Flee vs Combat)...");
        io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile snakeTile =
            new io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile(15, 5);

        // Test Flee: Slides down with zero combat damage
        Player fleePlayer = new Player("Runner");
        fleePlayer.setHealth(100);
        fleePlayer.setPosition(15);
        snakeTile.applyEffect(fleePlayer);
        if (fleePlayer.getPosition() != 5) {
            throw new AssertionError("Fleeing player should slide down to Tile 5, was: " + fleePlayer.getPosition());
        }
        if (fleePlayer.getHealth() != 100) {
            throw new AssertionError("Fleeing player should take zero combat damage, was: " + fleePlayer.getHealth());
        }

        // Test Combat: Start combat runs BattleSystem and awards loot on victory
        Player fightPlayer = new Player("Warrior");
        fightPlayer.setHealth(100);
        fightPlayer.setAttack(50); // High attack to guarantee victory in round 1
        fightPlayer.setPosition(15);
        snakeTile.startCombat(fightPlayer);
        if (fightPlayer.getInventory().isEmpty()) {
            throw new AssertionError("Victorious warrior should receive loot drop from slain serpent!");
        }

        // 14. Event Pacing Controller & Cooldown Tests
        System.out.println("\n[Test 16] Pacing Controller Cooldown & Bad-Luck Protection...");
        io.github.snakes_and_ladders_roll_of_fate.systems.EventManager eventMgr =
            new io.github.snakes_and_ladders_roll_of_fate.systems.EventManager();

        Player pacingPlayer = new Player("Scout");
        pacingPlayer.resetEventCooldown(); // 0 turns elapsed
        if (eventMgr.shouldTriggerAmbientEvent(pacingPlayer)) {
            throw new AssertionError("Ambient event should NOT trigger immediately after reset (0 turns elapsed)");
        }
        pacingPlayer.incrementTurnsSinceLastEvent(); // 1 turn elapsed
        // Now eligible (turnsSinceLastEvent >= 1)
        if (pacingPlayer.getTurnsSinceLastEvent() != 1) {
            throw new AssertionError("Player should have 1 turn elapsed");
        }

        // 15. Act Stratification & Narrative Scaling Tests
        System.out.println("\n[Test 17] Act Stratification & Scaling (Act I, II, III)...");
        int act1 = io.github.snakes_and_ladders_roll_of_fate.systems.EventStoryGenerator.getAct(15);
        int act2 = io.github.snakes_and_ladders_roll_of_fate.systems.EventStoryGenerator.getAct(50);
        int act3 = io.github.snakes_and_ladders_roll_of_fate.systems.EventStoryGenerator.getAct(85);

        if (act1 != 1) throw new AssertionError("Tile 15 should be in Act 1, was: " + act1);
        if (act2 != 2) throw new AssertionError("Tile 50 should be in Act 2, was: " + act2);
        if (act3 != 3) throw new AssertionError("Tile 85 should be in Act 3, was: " + act3);

        Event act1Merchant = EventRepository.createProceduralMerchantEvent(15);
        Event act3Merchant = EventRepository.createProceduralMerchantEvent(85);

        // Verify procedural merchant events contain requirement choices
        boolean act1HasCost = act1Merchant.getChoices().stream().anyMatch(c -> c.getRequirementValue() > 0);
        boolean act3HasCost = act3Merchant.getChoices().stream().anyMatch(c -> c.getRequirementValue() > 0);
        if (!act1HasCost || !act3HasCost) {
            throw new AssertionError("Merchant events should contain requirement-based choices");
        }

        // 16. Combat Rebalance & Mortal Retreat Safeguard Tests
        System.out.println("\n[Test 18] Snake Combat Stat Rebalance & Mortal Retreat Safeguard...");
        io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile lowSnake =
            new io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile(14, 4);
        io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile midSnake =
            new io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile(49, 20);
        io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile highSnake =
            new io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile(98, 40);

        // Verify Act-based stat scaling
        if (lowSnake.getSnakeAttack() != 8 || lowSnake.getSnakeHealth() != 22) {
            throw new AssertionError("Tile 14 Snake should have 8 ATK and 22 HP, was: " + lowSnake.getSnakeAttack() + " / " + lowSnake.getSnakeHealth());
        }
        if (midSnake.getSnakeAttack() != 13 || midSnake.getSnakeHealth() != 39) {
            throw new AssertionError("Tile 49 Snake should have 13 ATK and 39 HP, was: " + midSnake.getSnakeAttack() + " / " + midSnake.getSnakeHealth());
        }
        if (highSnake.getSnakeAttack() != 20 || highSnake.getSnakeHealth() != 64) {
            throw new AssertionError("Tile 98 Snake should have 20 ATK and 64 HP, was: " + highSnake.getSnakeAttack() + " / " + highSnake.getSnakeHealth());
        }

        // Verify BattleSystem damage calculation bounded variance
        io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem bs =
            new io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem();
        Player atkEntity = new Player("Atk");
        atkEntity.setAttack(10);
        Player defEntity = new Player("Def");
        defEntity.setDefense(5);
        for (int i = 0; i < 50; i++) {
            int dmg = bs.calculateDamage(atkEntity, defEntity);
            // With baseDamage = 5: Miss = 0, Normal = 5 to 9, Crit = 15. Must never exceed 15
            if (dmg > 15) {
                throw new AssertionError("Damage should be bounded to at most 15, was: " + dmg);
            }
        }

        // Verify Standard Retreat: Player reduced below 25 HP slides down
        Player retreatPlayer = new Player("Retreater");
        retreatPlayer.setHealth(40);
        retreatPlayer.setAttack(1);
        retreatPlayer.setDefense(0);
        retreatPlayer.setPosition(98);
        highSnake.startCombat(retreatPlayer);
        if (retreatPlayer.getHealth() > 25 || retreatPlayer.getHealth() <= 0) {
            throw new AssertionError("Player taking non-lethal hit below 25 HP should survive in retreat range, was: " + retreatPlayer.getHealth());
        }
        if (retreatPlayer.getPosition() != 40) {
            throw new AssertionError("Retreating player should slide down to destination (40), was: " + retreatPlayer.getPosition());
        }

        // Verify Mortal Retreat Safeguard: Player with >25 HP taking lethal damage (>26 dmg) -> Clamps to 1 HP & slides down
        Player woundedPlayer = new Player("Survivor");
        woundedPlayer.setHealth(26);
        woundedPlayer.setAttack(1); // Low attack so snake hits back
        woundedPlayer.setDefense(-10); // Guarantees incoming damage exceeds 26
        woundedPlayer.setPosition(98);
        highSnake.startCombat(woundedPlayer);
        if (woundedPlayer.getHealth() != 1) {
            throw new AssertionError("Player who entered combat with >25 HP should survive lethal blow with 1 HP, was: " + woundedPlayer.getHealth());
        }
        if (woundedPlayer.getPosition() != 40) {
            throw new AssertionError("Player surviving lethal blow should slide down to destination (40), was: " + woundedPlayer.getPosition());
        }

        // Verify Lethal Death when entering combat with <= 25 HP
        Player doomedPlayer = new Player("Doomed");
        doomedPlayer.setHealth(15);
        doomedPlayer.setAttack(1);
        doomedPlayer.setDefense(-10);
        doomedPlayer.setPosition(98);
        highSnake.startCombat(doomedPlayer);
        if (doomedPlayer.getHealth() != 0) {
            throw new AssertionError("Critically injured player (<=25 HP) should die if taking lethal combat damage, was: " + doomedPlayer.getHealth());
        }

        // 17. Character Class Archetypes & Starting Gear Tests
        System.out.println("\n[Test 19] Character Class Selection & Starting Gear...");
        CharacterClass[] classes = CharacterClass.values();
        if (classes.length != 4) {
            throw new AssertionError("There should be 4 playable character classes, found: " + classes.length);
        }

        // Test Turtle Archetype (Tank)
        Player turtlePlayer = new Player("Shelly", CharacterClass.TURTLE);
        if (turtlePlayer.getHealth() != 120) throw new AssertionError("Turtle HP should be 120, was: " + turtlePlayer.getHealth());
        if (turtlePlayer.getAttack() != 4) throw new AssertionError("Turtle ATK should be 4, was: " + turtlePlayer.getAttack());
        if (turtlePlayer.getDefense() != 7) throw new AssertionError("Turtle DEF should be 7, was: " + turtlePlayer.getDefense());
        if (!"Turtle".equals(turtlePlayer.getEntityClass())) throw new AssertionError("Entity class should be Turtle");
        if (turtlePlayer.getInventory().getItems().size() != 1) throw new AssertionError("Turtle should start with 1 item");
        if (!"Turtle Shell Buckler".equals(turtlePlayer.getInventory().getItems().get(0).getName())) {
            throw new AssertionError("Turtle should start with Turtle Shell Buckler");
        }

        // Test Cat Archetype (Agile)
        Player catPlayer = new Player("Shadow", CharacterClass.CAT);
        if (catPlayer.getHealth() != 90) throw new AssertionError("Cat HP should be 90, was: " + catPlayer.getHealth());
        if (catPlayer.getAttack() != 6) throw new AssertionError("Cat ATK should be 6, was: " + catPlayer.getAttack());
        if (catPlayer.getDefense() != 4) throw new AssertionError("Cat DEF should be 4, was: " + catPlayer.getDefense());
        if (!"Cat's Agility Charm".equals(catPlayer.getInventory().getItems().get(0).getName())) {
            throw new AssertionError("Cat should start with Cat's Agility Charm");
        }

        // Test Fox Archetype (Trickster)
        Player foxPlayer = new Player("Reynard", CharacterClass.FOX);
        if (foxPlayer.getHealth() != 95) throw new AssertionError("Fox HP should be 95, was: " + foxPlayer.getHealth());
        if (foxPlayer.getAttack() != 7) throw new AssertionError("Fox ATK should be 7, was: " + foxPlayer.getAttack());
        if (foxPlayer.getDefense() != 3) throw new AssertionError("Fox DEF should be 3, was: " + foxPlayer.getDefense());
        if (!"Fox's Hunting Fang".equals(foxPlayer.getInventory().getItems().get(0).getName())) {
            throw new AssertionError("Fox should start with Fox's Hunting Fang");
        }

        // Test Raccoon Archetype (Scavenger)
        Player raccoonPlayer = new Player("Bandit", CharacterClass.RACCOON);
        if (raccoonPlayer.getHealth() != 100) throw new AssertionError("Raccoon HP should be 100, was: " + raccoonPlayer.getHealth());
        if (raccoonPlayer.getAttack() != 5) throw new AssertionError("Raccoon ATK should be 5, was: " + raccoonPlayer.getAttack());
        if (raccoonPlayer.getDefense() != 4) throw new AssertionError("Raccoon DEF should be 4, was: " + raccoonPlayer.getDefense());
        if (!"Scavenger's First Aid Kit".equals(raccoonPlayer.getInventory().getItems().get(0).getName())) {
            throw new AssertionError("Raccoon should start with Scavenger's First Aid Kit");
        }

        // Test using Turtle's starting equipment in combat stance
        turtlePlayer.useItem(0); // Uses Turtle Shell Buckler (+3 DEF)
        if (turtlePlayer.getDefense() != 10) { // Base 7 + Buff 3
            throw new AssertionError("Turtle DEF with active buff should be 10, was: " + turtlePlayer.getDefense());
        }
        turtlePlayer.clearTemporaryBuffs();
        if (turtlePlayer.getDefense() != 7) {
            throw new AssertionError("Turtle DEF after turn should reset to base 7, was: " + turtlePlayer.getDefense());
        }

        // Test 14: Ensure health never exceeds maxHealth from item effects, events, or level up
        System.out.println("\n[Test 14] Ensuring health never exceeds maxHealth...");
        Player healCapPlayer = new Player("CapHero", CharacterClass.RACCOON);
        if (healCapPlayer.getHealth() != 100) throw new AssertionError("Raccoon base health should be 100");

        // 1. Using healing item while at full health
        healCapPlayer.useItem(0); // Uses Scavenger's First Aid Kit (+25 HP)
        if (healCapPlayer.getHealth() > healCapPlayer.getMaxHealth()) {
            throw new AssertionError("Health should not exceed maxHealth (100), was: " + healCapPlayer.getHealth());
        }
        if (healCapPlayer.getHealth() != 100) {
            throw new AssertionError("Health should remain 100, was: " + healCapPlayer.getHealth());
        }

        // 2. Using healing item while wounded (e.g. at 90 HP, +25 HP should cap at 100)
        healCapPlayer.setHealth(90);
        Item extraPotion = Item.createConsumable("Potion", EventEffectType.HEALTH, 25, "+25 HP");
        extraPotion.use(healCapPlayer);
        if (healCapPlayer.getHealth() > 100) {
            throw new AssertionError("Health should cap at 100, was: " + healCapPlayer.getHealth());
        }
        if (healCapPlayer.getHealth() != 100) {
            throw new AssertionError("Health should be capped at 100, was: " + healCapPlayer.getHealth());
        }

        // 3. Event Effect healing (+50 HP)
        healCapPlayer.setHealth(95);
        EventEffect bigHeal = new EventEffect(EventEffectType.HEALTH, 50);
        bigHeal.apply(healCapPlayer);
        if (healCapPlayer.getHealth() > 100) {
            throw new AssertionError("EventEffect should not exceed maxHealth, was: " + healCapPlayer.getHealth());
        }
        if (healCapPlayer.getHealth() != 100) {
            throw new AssertionError("Health should be capped at 100, was: " + healCapPlayer.getHealth());
        }

        // Test 15: Experience & Threshold progression
        System.out.println("\n[Test 15] Testing Player EXP and Level Threshold progression...");
        Player expPlayer = new Player("ExpHero", CharacterClass.CAT);
        if (expPlayer.getLevel() != 1) throw new AssertionError("Player should start at Level 1");
        if (expPlayer.getExperience() != 0) throw new AssertionError("Player should start at 0 EXP");
        if (expPlayer.getExpThresh() != 100) throw new AssertionError("Initial exp threshold should be 100");

        // Verify EXP cannot drop below 0
        expPlayer.setExperience(-50);
        if (expPlayer.getExperience() != 0) throw new AssertionError("EXP should be clamped to 0, was: " + expPlayer.getExperience());
        expPlayer.gainExperience(-20);
        if (expPlayer.getExperience() != 0) throw new AssertionError("Negative gainExperience should be ignored, was: " + expPlayer.getExperience());

        // Cat class has +15% bonus EXP
        // 50 base exp + 15% bonus = 57 exp
        expPlayer.gainExperience(50);
        if (expPlayer.getExperience() != 57) {
            throw new AssertionError("EXP should be 57 with Cat +15% bonus, was: " + expPlayer.getExperience());
        }
        if (expPlayer.getLevel() != 1) throw new AssertionError("Player should still be Level 1");

        // Gain another 50 exp (50 + 7 bonus = 57 exp). Total 57 + 57 = 114 >= 100 => Level up!
        // Surplus 114 - 100 = 14 carries over to Level 2
        expPlayer.gainExperience(50);
        if (expPlayer.getLevel() != 2) throw new AssertionError("Player should have leveled up to Level 2");
        if (expPlayer.getExperience() != 14) throw new AssertionError("Rollover EXP should be 14, was: " + expPlayer.getExperience());
        // expThresh increases by 10% on level up (100 + 10 = 110)
        if (expPlayer.getExpThresh() != 110) {
            throw new AssertionError("Exp threshold should increase to 110 on Level 2, was: " + expPlayer.getExpThresh());
        }

        System.out.println("\n=== ALL INVENTORY & EVENT INTEGRATION TESTS PASSED PERFECTLY! ===");
    }
}*/
