package io.github.snakes_and_ladders_roll_of_fate.skills;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/**
 * Turtle (Iron Guardian) Board Skill: Slow & Steady.
 * Polymorphic implementation: Cleanses all active curses and executes a disciplined 4-tile
 * march with complete hazard and serpent ambush immunity.
 */
public class TurtleShellSkill implements ClassSkill {

    @Override
    public String getSkillName() {
        return "Slow & Steady";
    }

    @Override
    public String getActionTitle() {
        return "SAFE MARCH";
    }

    @Override
    public String getActionSubtext() {
        return "MOVE +4 (NO SNAKE)";
    }

    @Override
    public AssetDescriptor<Texture> getIconAsset() {
        return Assets.ITEM_SHIELD_IRON;
    }

    @Override
    public String getDescription() {
        return "Cleanses curses and marches +4 tiles safely, immune to snakes.";
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
            return new SkillResult(false, "Cannot march while incapacitated or at summit.");
        }
        int cleansed = player.cleanseCurses();
        player.setHazardImmune(true);
        String msg = "Turtle activated Slow & Steady! Cleansed " + cleansed + " curses and marching 4 safe tiles!";
        return new SkillResult(true, msg, 4, true);
    }

    @Override
    public String getCombatSkillName() {
        return "Iron Fortress";
    }

    @Override
    public String getCombatDescription() {
        return "Block all damage from the next attack and retaliate with a crushing riposte!";
    }

    @Override
    public SkillResult executeCombat(Player player, Enemy enemy, BattleSystem battleSystem) {
        if (battleSystem == null) {
            return new SkillResult(false, "No active battle.");
        }
        battleSystem.setTurtleFortressActive(true);
        return new SkillResult(true, "IRON FORTRESS! You brace for impact and prepare to riposte!");
    }
}
