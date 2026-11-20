package io.github.snakes_and_ladders_roll_of_fate.items;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;

public class InventoryTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING INVENTORY BACKEND TESTS (OPTION A: TEMPORARY BUFFS) ===");

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
        if (added5) throw new AssertionError("5th item should be rejected!");

        // Display inventory
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
        // Shield is now at index 0 because potion was removed
        boolean usedShield1 = player.useItem(0); // First use (durability 2 -> 1)
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
        if (inv.size() != 3) {
            throw new AssertionError("Shield should still be in inventory with 1 durability remaining");
        }

        // End of Turn: Buff expires
        System.out.println("\n[Test 7] Ending Turn and Clearing Buffs...");
        player.clearTemporaryBuffs();
        if (player.getDefense() != 10) {
            throw new AssertionError("Defense should return to base 10 after buff expires, was: " + player.getDefense());
        }
        if (player.getTempDefense() != 0) {
            throw new AssertionError("Temp defense should be reset to 0, was: " + player.getTempDefense());
        }

        // 7. Second use of shield: durability 1 -> 0, shatters, removes from inventory
        System.out.println("\n[Test 8] Second use of Shield (Durability 1 -> 0, Shatter)...");
        boolean usedShield2 = player.useItem(0);
        if (!usedShield2) throw new AssertionError("Shield second use should succeed");
        if (player.getDefense() != 13) {
            throw new AssertionError("Defense should be boosted to 13 during this second turn, was: " + player.getDefense());
        }
        if (inv.size() != 2) {
            throw new AssertionError("Shield should shatter and be removed. Size should be 2, was: " + inv.size());
        }

        // Turn ends again: buff expires
        player.clearTemporaryBuffs();
        if (player.getDefense() != 10) {
            throw new AssertionError("Defense should return to base 10 after shatter turn expires");
        }

        inv.displayInventory();

        // 8. Test Weapon (Attack buff)
        System.out.println("\n[Test 9] Testing Weapon Battle Focus (Attack Buff)...");
        // Iron Sword is now at index 0
        boolean usedSword = player.useItem(0);
        if (!usedSword) throw new AssertionError("Sword usage should succeed");
        if (player.getAttack() != 15) {
            throw new AssertionError("Total attack should be 15, was: " + player.getAttack());
        }
        if (player.getBaseAttack() != 10) {
            throw new AssertionError("Base attack should remain 10, was: " + player.getBaseAttack());
        }

        player.clearTemporaryBuffs();
        if (player.getAttack() != 10) {
            throw new AssertionError("Attack should return to base 10 after buff expires");
        }

        System.out.println("\n=== ALL INVENTORY BACKEND TESTS PASSED PERFECTLY! ===");
    }
}
