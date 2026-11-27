package io.github.snakes_and_ladders_roll_of_fate.skills;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

/**
 * Fox (Cunning Trickster) Board Skill: Cunning Sleight.
 * Polymorphic implementation: Exploits sharp wits to gain bonus EXP and sharp temporary attack power.
 */
public class FoxCunningSkill implements ClassSkill {

    @Override
    public String getSkillName() {
        return "Cunning Sleight";
    }

    @Override
    public String getDescription() {
        return "Outwit the treacherous trail: Grants +25 bonus EXP and +3 temporary ATK.";
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
            return new SkillResult(false, "Cannot execute sleight while incapacitated.");
        }
        player.gainExperience(25);
        player.addTempAttack(3);
        return new SkillResult(true, "Fox outwitted the hazards! Gained +25 EXP and +3 ATK power.");
    }
}
