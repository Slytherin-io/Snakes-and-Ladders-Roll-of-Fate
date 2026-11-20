package io.github.snakes_and_ladders_roll_of_fate.items;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.Event;
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
        Event merchantEvent = EventRepository.createProceduralMerchantEvent(10);
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

        System.out.println("\n=== ALL INVENTORY & EVENT INTEGRATION TESTS PASSED PERFECTLY! ===");
    }
}
