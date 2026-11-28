package io.github.snakes_and_ladders_roll_of_fate.skills;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/**
 * Cat (Nimble Prowler) Board Skill: Lucky Paw.
 * Polymorphic implementation: Feline fortune guarantees a maximum roll of 6 on your next roll.
 * Cats always land on their feet!
 */
public class CatAgilitySkill implements ClassSkill {

    @Override
    public String getSkillName() {
        return "Lucky Paw";
    }

    @Override
    public String getActionTitle() {
        return "LUCKY PAW";
    }

    @Override
    public String getActionSubtext() {
        return "NEXT ROLL = 6";
    }

    @Override
    public AssetDescriptor<Texture> getIconAsset() {
        return Assets.ICON_DICE_D6;
    }

    @Override
    public String getDescription() {
        return "Makes your next dice roll an automatic 6.";
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
            return new SkillResult(false, "Cannot use Lucky Paw while incapacitated or at summit.");
        }
        player.setLuckyPawActive(true);
        return new SkillResult(true, "Cat activated Lucky Paw! Your next dice roll is guaranteed to be 6!");
    }

    @Override
    public String getCombatSkillName() {
        return "Double Strike";
    }

    @Override
    public String getCombatDescription() {
        return "Strike twice in a single turn!";
    }

    @Override
    public SkillResult executeCombat(Player player, Enemy enemy, BattleSystem battleSystem) {
        if (enemy == null || battleSystem == null) {
            return new SkillResult(false, "No valid target.");
        }
        int dmg1 = battleSystem.calculateDamage(player, enemy);
        enemy.setHealth(Math.max(0, enemy.getHealth() - dmg1));
        int dmg2 = 0;
        if (enemy.getHealth() > 0) {
            dmg2 = battleSystem.calculateDamage(player, enemy);
            enemy.setHealth(Math.max(0, enemy.getHealth() - dmg2));
        }
        return new SkillResult(true, "DOUBLE STRIKE! You hit for " + dmg1 + " and " + dmg2 + " damage!");
    }
}
