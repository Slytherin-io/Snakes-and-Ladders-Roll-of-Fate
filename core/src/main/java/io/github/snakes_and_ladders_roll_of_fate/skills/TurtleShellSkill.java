package io.github.snakes_and_ladders_roll_of_fate.skills;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

/**
 * Turtle (Iron Guardian) Board Skill: Shell Barrier.
 * Polymorphic implementation: Erects an impenetrable shell barrier against hazards and snakes.
 */
public class TurtleShellSkill implements ClassSkill {

    @Override
    public String getSkillName() {
        return "Shell Barrier";
    }

    @Override
    public String getDescription() {
        return "Hunker down in iron armor: Protects against the next hazard or serpent slide.";
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
            return new SkillResult(false, "Cannot erect shell barrier while incapacitated.");
        }
        player.setHasSnakeWard(true);
        player.addTempDefense(3);
        return new SkillResult(true, "Turtle erected an impenetrable Shell Barrier! Next serpent/hazard is warded.");
    }
}
