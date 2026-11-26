package io.github.snakes_and_ladders_roll_of_fate.entities;

public class Enemy extends Entity {
    private String enemyType;
    private String behavior;
    private String reward;


    public Enemy(String enemyType) {
        this.enemyType = enemyType;

    }

    public void defend() {}

    public String dropReward() {
        return reward;
    }

    // Getters and Setters
    public String getEnemyType() { return enemyType; }

    public String getBehavior() { return behavior; }
    public void setBehavior(String behavior) { this.behavior = behavior; }

    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }
}
