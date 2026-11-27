package io.github.snakes_and_ladders_roll_of_fate.skills;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

/**
 * Cat (Nimble Prowler) Board Skill: Prowl Leap.
 * Polymorphic implementation: Instantly leaps +2 tiles forward along the path.
 */
public class CatAgilitySkill implements ClassSkill {

    @Override
    public String getSkillName() {
        return "Prowl Leap";
    }

    @Override
    public String getDescription() {
        return "Agile feline reflex: Instantly leap +2 tiles forward on the board.";
    }

    @Override
    public int getMaxCooldown() {
        return 4;
    }

    @Override
    public boolean canUse(Player player) {
        return player != null && player.getHealth() > 0 && player.getPosition() < 100;
    }

    @Override
    public SkillResult execute(Player player, RollOfFateGame game) {
        if (!canUse(player)) {
            return new SkillResult(false, "Cannot leap while incapacitated or at summit.");
        }
        int oldPos = player.getPosition();
        int newPos = Math.min(100, oldPos + 2);
        player.setPosition(newPos);
        return new SkillResult(true, "Cat leaped +2 tiles forward with feline grace! (Tile " + oldPos + " -> " + newPos + ")", 2);
    }
}
