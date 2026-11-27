package io.github.snakes_and_ladders_roll_of_fate.skills;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
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
        return "+2 & GLIDE";
    }

    @Override
    public AssetDescriptor<Texture> getIconAsset() {
        return Assets.ITEM_FLUTE;
    }

    @Override
    public String getDescription() {
        return "Cunning serpent charmer: Grants +2 movement. If you land on a Snake, charms it to negate the drop and glide +3 tiles forward!";
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
}
