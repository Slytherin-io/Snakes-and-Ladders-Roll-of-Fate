package io.github.snakes_and_ladders_roll_of_fate.entities;

public abstract class Entity {
    private int health;
    private int defense;
    private int attack;
    private int position;
    private String entityClass;
    private String status;

    public Entity() {}

    public int attack() {
        return attack;
    }

    public boolean escape() {
        return false;
    }

    public void block() {}

    public void move(int steps) {
        this.position += steps;
    }

    // Getters and Setters
    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = health; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    
    public String getEntityClass() { return entityClass; }
    public void setEntityClass(String entityClass) { this.entityClass = entityClass; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
