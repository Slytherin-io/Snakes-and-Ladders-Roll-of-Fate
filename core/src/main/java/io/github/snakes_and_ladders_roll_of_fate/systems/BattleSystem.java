package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.*;

public class BattleSystem {
    private Entity attacker;
    private Entity defender;
    private String battleState;

    public BattleSystem() {
        this.battleState = "IDLE";
    }

    public void startBattle(Entity attacker, Entity defender) {
        this.attacker = attacker;
        this.defender = defender;
        this.battleState = "IN_PROGRESS";
    }

    public int calculateDamage(Entity attacker, Entity defender) {
        int damage = attacker.getAttack() - defender.getDefense();
        return Math.max(1, damage);
    }

    public void endBattle() {
        this.battleState = "ENDED";
    }

    // Getters & Setters
    public String getBattleState() {
        return battleState;
    }

}
