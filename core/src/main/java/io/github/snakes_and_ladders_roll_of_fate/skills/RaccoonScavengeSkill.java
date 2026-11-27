package io.github.snakes_and_ladders_roll_of_fate.skills;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

/**
 * Raccoon (Crafty Scavenger) Board Skill: Forage & Patch.
 * Polymorphic implementation: Scavenges emergency herbal remedies to restore +20 HP.
 */
public class RaccoonScavengeSkill implements ClassSkill {

    @Override
    public String getSkillName() {
        return "Forage & Patch";
    }

    @Override
    public String getDescription() {
        return "Scavenge survival remedies: Restores +20 HP to withstand mortal danger.";
    }

    @Override
    public int getMaxCooldown() {
        return 4;
    }

    @Override
    public boolean canUse(Player player) {
        return player != null && player.getHealth() > 0;
    }

    @Override
    public SkillResult execute(Player player, RollOfFateGame game) {
        if (!canUse(player)) {
            return new SkillResult(false, "Cannot forage while incapacitated.");
        }
        int oldHp = player.getHealth();
        player.setHealth(oldHp + 20);
        int healed = player.getHealth() - oldHp;
        return new SkillResult(true, "Raccoon scavenged herbal remedies and patched wounds! (+" + healed + " HP)");
    }
}
