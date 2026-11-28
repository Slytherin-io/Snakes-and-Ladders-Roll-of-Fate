package io.github.snakes_and_ladders_roll_of_fate.skills;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
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
     * @return Short action title for the bottom HUD button (e.g. "FLIP ROLL", "SAFE MARCH").
     */
    String getActionTitle();

    /**
     * @return Compact description of what the skill does (e.g. "1 <-> 6 INVERT", "+4 SAFE TILES").
     */
    String getActionSubtext();

    /**
     * @return Dedicated ability icon asset descriptor (e.g. dice, shield, flute, satchel).
     */
    AssetDescriptor<Texture> getIconAsset();

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

    /**
     * @return Display name for active combat skill (e.g. "Double Strike", "Iron Fortress").
     */
    default String getCombatSkillName() {
        return getSkillName();
    }

    /**
     * @return Compact description of what the combat skill does in battle.
     */
    default String getCombatDescription() {
        return getDescription();
    }

    /**
     * Polymorphically executes the unique character combat skill in battle.
     * @param player The activating player entity.
     * @param enemy The defending enemy entity.
     * @param battleSystem The active battle system context.
     * @return Result of the combat action with narrative feedback.
     */
    default SkillResult executeCombat(Player player, io.github.snakes_and_ladders_roll_of_fate.entities.Enemy enemy, io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem battleSystem) {
        return new SkillResult(false, "No active combat skill.");
    }
}
