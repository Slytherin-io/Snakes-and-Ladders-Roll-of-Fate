package io.github.snakes_and_ladders_roll_of_fate.skills;

import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;

/**
 * Automated verification suite for the Polymorphic ClassSkill system.
 */
public class ClassSkillVerificationTest {

    public static void main(String[] args) {
        System.out.println("=== RUNNING POLYMORPHIC CLASS SKILL VERIFICATION SUITE ===");

        testAllClassesImplementPolymorphicSkills();
        testSkillsStartInactiveOnCooldown();
        testCooldownDecrementAndReadyState();
        testSkillUIMetadata();
        testCatLuckyPaw();
        testTurtleSlowAndSteady();
        testFoxSerpentWhisperer();
        testRaccoonScrapSalvage();
        testInventoryStartsEmpty();

        System.out.println(">>> ALL CLASS SKILL SYSTEM VERIFICATIONS PASSED WITH ZERO FAILURES!");
    }

    private static void testAllClassesImplementPolymorphicSkills() {
        System.out.println("\n[Test 1] Verifying polymorphic ClassSkill assignment across all classes...");
        for (CharacterClass cc : CharacterClass.values()) {
            Player p = new Player("Hero", cc);
            ClassSkill skill = p.getClassSkill();
            if (skill == null) {
                throw new AssertionError("Player with class " + cc + " should have a non-null ClassSkill!");
            }
            if (skill.getMaxCooldown() != 4) {
                throw new AssertionError("Skill " + skill.getSkillName() + " must have 4-turn cooldown!");
            }
            System.out.println("  Class: " + cc.getName() + " -> Skill: " + skill.getSkillName() + " (" + skill.getClass().getSimpleName() + ")");
        }
    }

    private static void testSkillsStartInactiveOnCooldown() {
        System.out.println("\n[Test 2] Verifying that skills start INACTIVE on full 4-turn cooldown...");
        for (CharacterClass cc : CharacterClass.values()) {
            Player p = new Player("Hero", cc);
            if (p.isSkillReady()) {
                throw new AssertionError("Player should NOT start with skill ready! Must start inactive.");
            }
            if (p.getSkillCooldown() != 4) {
                throw new AssertionError("Initial cooldown should be 4, got: " + p.getSkillCooldown());
            }
        }
        System.out.println("  All character skills correctly initialize as inactive with 4-turn cooldown.");
    }

    private static void testCooldownDecrementAndReadyState() {
        System.out.println("\n[Test 3] Verifying turn cooldown decrement and ready transition...");
        Player p = new Player("Hero", CharacterClass.CAT);

        // Turn 1 complete -> cooldown 3
        p.decrementSkillCooldown();
        if (p.getSkillCooldown() != 3 || p.isSkillReady()) throw new AssertionError("Expected CD 3, not ready");

        // Turn 2 complete -> cooldown 2
        p.decrementSkillCooldown();
        if (p.getSkillCooldown() != 2 || p.isSkillReady()) throw new AssertionError("Expected CD 2, not ready");

        // Turn 3 complete -> cooldown 1
        p.decrementSkillCooldown();
        if (p.getSkillCooldown() != 1 || p.isSkillReady()) throw new AssertionError("Expected CD 1, not ready");

        // Turn 4 complete -> cooldown 0 (READY!)
        p.decrementSkillCooldown();
        if (p.getSkillCooldown() != 0 || !p.isSkillReady()) throw new AssertionError("Expected CD 0, ready for use!");

        System.out.println("  Cooldown decrements properly across 4 turns and transitions to READY.");
    }

    private static void testSkillUIMetadata() {
        System.out.println("\n[Test 4] Verifying Skill UI Action titles, subtexts, and icons...");
        for (CharacterClass cc : CharacterClass.values()) {
            Player p = new Player("Hero", cc);
            ClassSkill skill = p.getClassSkill();
            if (skill.getActionTitle() == null || skill.getActionTitle().isEmpty()) {
                throw new AssertionError("Skill " + skill.getSkillName() + " must have a non-empty action title!");
            }
            if (skill.getActionSubtext() == null || skill.getActionSubtext().isEmpty()) {
                throw new AssertionError("Skill " + skill.getSkillName() + " must have a non-empty action subtext!");
            }
            if (skill.getIconAsset() == null) {
                throw new AssertionError("Skill " + skill.getSkillName() + " must have a non-null icon asset descriptor!");
            }
            System.out.println("  " + cc.getName() + " -> [" + skill.getActionTitle() + "] (" + skill.getActionSubtext() + ") Icon: " + skill.getIconAsset().fileName);
        }
    }

    private static void testCatLuckyPaw() {
        System.out.println("\n[Test 5] Verifying Cat 'Lucky Paw' (Guaranteed 6 Roll)...");
        Player p = new Player("CatHero", CharacterClass.CAT);
        p.setSkillCooldown(0); // Ready

        if (p.isLuckyPawActive()) throw new AssertionError("Cat should not start with Lucky Paw primed");
        SkillResult res = p.useClassSkill(null);
        if (!res.isSuccess() || !p.isLuckyPawActive()) {
            throw new AssertionError("Cat skill should succeed and prime Lucky Paw!");
        }
        if (p.getSkillCooldown() != 4) {
            throw new AssertionError("Skill cooldown must reset to 4 after use!");
        }
        System.out.println("  Cat primed Lucky Paw (guaranteed 6), cooldown reset to 4.");
    }

    private static void testTurtleSlowAndSteady() {
        System.out.println("\n[Test 6] Verifying Turtle 'Slow & Steady' (Cleanses curses & safe 4-step march)...");
        Player p = new Player("TurtleHero", CharacterClass.TURTLE);
        p.setSkillCooldown(0); // Ready

        // Add a mock curse to test cleansing
        p.getInventory().addItem(Item.createCurse("Mock Hex", EventEffectType.MOVE, -1, "Test penalty"));
        if (!p.hasCurse()) throw new AssertionError("Turtle should have a curse before cleansing");

        SkillResult res = p.useClassSkill(null);
        if (!res.isSuccess() || !res.isDirectMove() || res.getBonusMovement() != 4) {
            throw new AssertionError("Turtle skill must succeed with direct move of 4 safe steps!");
        }
        if (p.hasCurse()) {
            throw new AssertionError("Turtle skill must cleanse all active curses!");
        }
        if (!p.isHazardImmune()) {
            throw new AssertionError("Turtle must be granted hazard immunity for the march!");
        }
        if (p.getSkillCooldown() != 4) {
            throw new AssertionError("Skill cooldown must reset to 4 after use!");
        }
        System.out.println("  Turtle cleansed curses, gained hazard immunity, and prepared 4-tile direct march.");
    }

    private static void testFoxSerpentWhisperer() {
        System.out.println("\n[Test 7] Verifying Fox 'Serpent Whisperer' (+2 movement & snake charm)...");
        Player p = new Player("FoxHero", CharacterClass.FOX);
        p.setSkillCooldown(0); // Ready

        if (p.isSerpentWhispererActive()) throw new AssertionError("Fox should not start with Serpent Whisperer primed");
        SkillResult res = p.useClassSkill(null);
        if (!res.isSuccess() || !p.isSerpentWhispererActive()) {
            throw new AssertionError("Fox skill should succeed and prime Serpent Whisperer!");
        }
        if (p.getSkillCooldown() != 4) {
            throw new AssertionError("Skill cooldown must reset to 4!");
        }
        System.out.println("  Fox primed Serpent Whisperer (+2 movement and snake charm), cooldown reset to 4.");
    }

    private static void testRaccoonScrapSalvage() {
        System.out.println("\n[Test 8] Verifying Raccoon 'Scrap Salvage' (Loot table & Full bag +15 HP fallback)...");
        Player p = new Player("RaccoonHero", CharacterClass.RACCOON);
        p.setSkillCooldown(0); // Ready

        // Case A: Bag not full -> pulls loot
        SkillResult res1 = p.useClassSkill(null);
        if (!res1.isSuccess() || p.getInventory().size() != 1) {
            throw new AssertionError("Raccoon should have scavenged an item into inventory!");
        }
        System.out.println("  Scavenged loot: " + p.getInventory().getItems().get(0).getName());

        // Case B: Bag full -> converts scraps to +15 HP
        p.getInventory().addItem(Item.createConsumable("Item 2", EventEffectType.HEALTH, 10, ""));
        p.getInventory().addItem(Item.createConsumable("Item 3", EventEffectType.HEALTH, 10, ""));
        p.getInventory().addItem(Item.createConsumable("Item 4", EventEffectType.HEALTH, 10, ""));
        if (!p.getInventory().isFull()) throw new AssertionError("Inventory should now be full (4/4)");

        p.setHealth(50);
        p.setSkillCooldown(0); // Reset for test
        SkillResult res2 = p.useClassSkill(null);
        if (!res2.isSuccess() || p.getHealth() != 65) {
            throw new AssertionError("Full bag fallback should heal +15 HP (50 -> 65), got: " + p.getHealth());
        }
        if (p.getSkillCooldown() != 4) throw new AssertionError("Skill cooldown must reset to 4!");
        System.out.println("  Full bag triage fallback healed +15 HP (50 -> 65), cooldown reset to 4.");
    }

    private static void testInventoryStartsEmpty() {
        System.out.println("\n[Test 9] Verifying that player inventory starts 100% empty (0/4 slots)...");
        for (CharacterClass cc : CharacterClass.values()) {
            Player p = new Player("Adventurer", cc);
            if (p.getInventory().size() != 0) {
                throw new AssertionError("Inventory should start empty (0/4 slots), but had: " + p.getInventory().size());
            }
        }
        System.out.println("  All players start with 0/4 inventory slots free for exploration relics.");
    }
}
