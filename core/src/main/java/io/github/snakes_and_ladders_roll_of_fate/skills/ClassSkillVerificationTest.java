package io.github.snakes_and_ladders_roll_of_fate.skills;

import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

/**
 * Automated verification suite for the Polymorphic ClassSkill system.
 */
public class ClassSkillVerificationTest {

    public static void main(String[] args) {
        System.out.println("=== RUNNING POLYMORPHIC CLASS SKILL VERIFICATION SUITE ===");

        testAllClassesImplementPolymorphicSkills();
        testSkillsStartInactiveOnCooldown();
        testCooldownDecrementAndReadyState();
        testCatAgilitySkill();
        testTurtleShellSkill();
        testFoxCunningSkill();
        testRaccoonScavengeSkill();
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

    private static void testCatAgilitySkill() {
        System.out.println("\n[Test 4] Verifying Cat 'Prowl Leap' (+2 tiles forward)...");
        Player p = new Player("CatHero", CharacterClass.CAT);
        p.setPosition(15);
        p.setSkillCooldown(0); // Ready

        SkillResult res = p.useClassSkill(null);
        if (!res.isSuccess() || res.getBonusMovement() != 2) {
            throw new AssertionError("Cat skill should succeed with +2 movement!");
        }
        if (p.getPosition() != 17) {
            throw new AssertionError("Cat position should be 17, got: " + p.getPosition());
        }
        if (p.getSkillCooldown() != 4) {
            throw new AssertionError("Skill cooldown must reset to 4 after use!");
        }
        System.out.println("  Cat leaped +2 tiles (15 -> 17), cooldown reset to 4.");
    }

    private static void testTurtleShellSkill() {
        System.out.println("\n[Test 5] Verifying Turtle 'Shell Barrier' (Ward against hazard/serpent)...");
        Player p = new Player("TurtleHero", CharacterClass.TURTLE);
        p.setSkillCooldown(0); // Ready

        if (p.hasSnakeWard()) throw new AssertionError("Turtle should not start with active ward");
        SkillResult res = p.useClassSkill(null);
        if (!res.isSuccess() || !p.hasSnakeWard()) {
            throw new AssertionError("Turtle skill must activate Snake Ward!");
        }
        if (p.getSkillCooldown() != 4) {
            throw new AssertionError("Skill cooldown must reset to 4 after use!");
        }
        System.out.println("  Turtle activated Shell Barrier (Snake Ward enabled), cooldown reset to 4.");
    }

    private static void testFoxCunningSkill() {
        System.out.println("\n[Test 6] Verifying Fox 'Cunning Sleight' (+25 EXP & +3 Temp ATK)...");
        Player p = new Player("FoxHero", CharacterClass.FOX);
        p.setSkillCooldown(0); // Ready
        int initialExp = p.getExperience();
        int initialAtk = p.getAttack();

        SkillResult res = p.useClassSkill(null);
        if (!res.isSuccess()) throw new AssertionError("Fox skill should succeed!");
        if (p.getExperience() <= initialExp) throw new AssertionError("Fox should gain bonus EXP!");
        if (p.getAttack() <= initialAtk) throw new AssertionError("Fox should gain temporary ATK!");
        if (p.getSkillCooldown() != 4) throw new AssertionError("Skill cooldown must reset to 4!");
        System.out.println("  Fox gained EXP and ATK buff, cooldown reset to 4.");
    }

    private static void testRaccoonScavengeSkill() {
        System.out.println("\n[Test 7] Verifying Raccoon 'Forage & Patch' (+20 HP restore)...");
        Player p = new Player("RaccoonHero", CharacterClass.RACCOON);
        p.setHealth(40);
        p.setSkillCooldown(0); // Ready

        SkillResult res = p.useClassSkill(null);
        if (!res.isSuccess() || p.getHealth() != 60) {
            throw new AssertionError("Raccoon health should be 60, got: " + p.getHealth());
        }
        if (p.getSkillCooldown() != 4) throw new AssertionError("Skill cooldown must reset to 4!");
        System.out.println("  Raccoon healed +20 HP (40 -> 60), cooldown reset to 4.");
    }

    private static void testInventoryStartsEmpty() {
        System.out.println("\n[Test 8] Verifying that player inventory starts 100% empty (0/4 slots)...");
        for (CharacterClass cc : CharacterClass.values()) {
            Player p = new Player("Adventurer", cc);
            if (p.getInventory().size() != 0) {
                throw new AssertionError("Inventory should start empty (0/4 slots), but had: " + p.getInventory().size());
            }
        }
        System.out.println("  All players start with 0/4 inventory slots free for exploration relics.");
    }
}
