package io.github.snakes_and_ladders_roll_of_fate.skills;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

/**
 * Polymorphic contract for Character Class Board Skills.
 * Demonstrates the Strategy Pattern & Polymorphism for unique board gameplay.
 */
public interface ClassSkill {
    /**
     * @return Display name of the active skill.
     */
    String getSkillName();

    /**
     * @return Short flavor and gameplay description.
     */
    String getDescription();

    /**
     * @return Required turns between activations (default: 4 turns).
     */
    int getMaxCooldown();

    /**
     * Checks whether the skill can be activated in the current state.
     */
    boolean canUse(Player player);

    /**
     * Polymorphically executes the unique character skill on the adventure board.
     * @param player The activating player entity.
     * @param game The main game context.
     * @return Result of the activation with narrative feedback.
     */
    SkillResult execute(Player player, RollOfFateGame game);
}
