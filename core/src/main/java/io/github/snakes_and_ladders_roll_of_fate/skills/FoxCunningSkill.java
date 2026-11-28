package io.github.snakes_and_ladders_roll_of_fate.skills;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/**
 * Fox (Cunning Trickster) Board Skill: Serpent Whisperer.
 * Polymorphic implementation: Grants +2 movement. If the Fox lands on a Snake, charms it
 * to negate the downward drop and glides +3 tiles forward instead!
 */
public class FoxCunningSkill implements ClassSkill {

    @Override
    public String getSkillName() {
        return "Serpent Whisperer";
    }

    @Override
    public String getActionTitle() {
        return "CHARM SNAKE";
    }

    @Override
    public String getActionSubtext() {
        return "+2 ROLL & DODGE SNAKE";
    }

    @Override
    public AssetDescriptor<Texture> getIconAsset() {
        return Assets.ITEM_FLUTE;
    }

    @Override
    public String getDescription() {
        return "Adds +2 to your next roll. Landing on a snake glides +3 tiles forward instead of sliding.";
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
            return new SkillResult(false, "Cannot use Serpent Whisperer while incapacitated or at summit.");
        }
        player.setSerpentWhispererActive(true);
        return new SkillResult(true, "Fox activated Serpent Whisperer! Next roll gets +2 tiles and charms serpents!");
    }

    @Override
    public String getCombatSkillName() {
        return "Vital Strike";
    }

    @Override
    public String getCombatDescription() {
        return "Strike a vital weak point for a guaranteed critical hit!";
    }

    @Override
    public SkillResult executeCombat(Player player, Enemy enemy, BattleSystem battleSystem) {
        if (enemy == null || battleSystem == null) {
            return new SkillResult(false, "No valid target.");
        }
        int baseDmg = Math.max(1, player.getAttack() - enemy.getDefense());
        int foxDmg = baseDmg * 2;
        enemy.setHealth(Math.max(0, enemy.getHealth() - foxDmg));
        return new SkillResult(true, "GUARANTEED CRIT! You strike a vital weak point for " + foxDmg + " damage!");
    }
}
