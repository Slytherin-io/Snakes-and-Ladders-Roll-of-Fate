package io.github.snakes_and_ladders_roll_of_fate.skills;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/**
 * Raccoon (Crafty Scavenger) Board Skill: Scrap Salvage.
 * Polymorphic implementation: Scavenges useful survival items from the trail.
 * Balanced loot table: 70% survival remedies, 25% tactical utility, 5% rare jackpot.
 * Fallback: If backpack is full, converts scraps into +15 HP field medicine.
 */
public class RaccoonScavengeSkill implements ClassSkill {

    @Override
    public String getSkillName() {
        return "Scrap Salvage";
    }

    @Override
    public String getActionTitle() {
        return "SCAVENGE";
    }

    @Override
    public String getActionSubtext() {
        return "ITEM OR +15 HP";
    }

    @Override
    public AssetDescriptor<Texture> getIconAsset() {
        return Assets.ITEM_HOOK;
    }

    @Override
    public String getDescription() {
        return "Finds a random trail item, or heals +15 HP if your satchel is full.";
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
            return new SkillResult(false, "Cannot scavenge while incapacitated.");
        }

        // If inventory is full, convert scraps into field triage medicine (+15 HP)
        if (player.getInventory() != null && player.getInventory().isFull()) {
            int oldHp = player.getHealth();
            player.setHealth(oldHp + 15);
            int healed = player.getHealth() - oldHp;
            return new SkillResult(true, "Backpack is full! Raccoon converted salvaged scraps into field medicine (+" + healed + " HP)!");
        }

        // Otherwise draw from balanced loot table:
        // 70% Survival (Bandage, Torch, Minor Potion)
        // 25% Utility (Swift Feather, Scout Torch, Lead Boots)
        // 5% Jackpot (Scavenger's Relic, Ambrosia)
        int roll = MathUtils.random(1, 100);
        Item loot;
        if (roll <= 70) {
            loot = Item.createConsumable("Scavenged Bandage", EventEffectType.HEALTH, 15, "Sterile field bandage. Restores 15 HP.");
        } else if (roll <= 95) {
            loot = Item.createConsumable("Swift Feather", EventEffectType.MOVE, 2, "Light feather. Grants +2 bonus movement.");
        } else {
            loot = Item.createEquipment("Scavenger's Relic", EventEffectType.ATTACK, 2, 2, 2, "Polished ancient relic. +2 ATK / +2 DEF.");
        }

        if (player.getInventory() != null) {
            player.getInventory().addItem(loot);
        }
        return new SkillResult(true, "Raccoon scavenged a [" + loot.getName() + "] from the mountain trail!");
    }

    @Override
    public String getCombatSkillName() {
        return "Scavenge Strike";
    }

    @Override
    public String getCombatDescription() {
        return "Strike the enemy and scavenge healing supplies on kill!";
    }

    @Override
    public SkillResult executeCombat(Player player, Enemy enemy, BattleSystem battleSystem) {
        if (enemy == null || battleSystem == null) {
            return new SkillResult(false, "No valid target.");
        }
        int scavDmg = battleSystem.calculateDamage(player, enemy);
        enemy.setHealth(Math.max(0, enemy.getHealth() - scavDmg));
        battleSystem.notifyDamageDealt(player, enemy, scavDmg, false, false);
        if (enemy.getHealth() <= 0) {
            Item loot = Item.createConsumable("Scavenged Rations", EventEffectType.HEALTH, 25, "Restores 25 HP");
            player.addItem(loot);
            return new SkillResult(true, "SCAVENGE STRIKE! Deals " + scavDmg + " damage! You loot " + loot.getName() + "!");
        } else {
            return new SkillResult(true, "Scavenge Strike deals " + scavDmg + " damage! (Loot on kill)");
        }
    }
}
