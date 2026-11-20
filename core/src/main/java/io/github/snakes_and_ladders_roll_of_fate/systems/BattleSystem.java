package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.*;

public class BattleSystem {
    private Entity attacker;
    private Entity defender;
    private String battleState;
    private Dice combatDice = new Dice(20);

    public BattleSystem() {
        this.battleState = "IDLE";
    }

    public void startBattle(Entity attacker, Entity defender) {
        this.attacker = attacker;
        this.defender = defender;
        this.battleState = "IN_PROGRESS";
    }

    public int calculateDamage(Entity attacker, Entity defender) {
        int roll = combatDice.roll();
        int baseDamage = Math.max(1, attacker.getAttack() - defender.getDefense());

        if (roll == 20) {
            System.out.println("CRITICAL HIT (Rolled a 20)!"); // for console testing
            return Math.max(2, baseDamage * 2 + 5);
        } else if (roll == 1) {
            System.out.println("CRITICAL MISS (Rolled a 1)!"); // for console testing
            return 0;
        } else {
            // Scaled dice variance: roll between 2-19 adds 0 to 4 bonus damage, preventing one-hit kills
            int rollBonus = (roll - 1) / 4;
            return baseDamage + rollBonus;
        }

    }

    public void endBattle() {
        this.battleState = "ENDED";
    }

    // Getters & Setters
    public String getBattleState() {
        return battleState;
    }

}
