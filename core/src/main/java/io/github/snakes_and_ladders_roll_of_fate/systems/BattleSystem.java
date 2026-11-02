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
            System.out.println("💥 CRITICAL HIT (Rolled a 20)!"); // for console testing
            return baseDamage * 2;
        } else if (roll == 1) {
            System.out.println("💨 CRITICAL MISS (Rolled a 1)!"); // for console testing
            return 0;
        } else {
            return baseDamage;
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
