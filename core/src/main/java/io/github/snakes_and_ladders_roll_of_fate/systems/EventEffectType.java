package io.github.snakes_and_ladders_roll_of_fate.systems;

public enum EventEffectType {
    HEALTH,       // Heals or damages the player
    ATTACK,       // Buffs or debuffs attack power
    DEFENSE,      // Buffs or debuffs defense
    EXPERIENCE,   // Grants EXP towards leveling up
    MOVE,         // Moves player forward (+) or backward (-)
    ITEM,         // Grants an item or loot drop to the player
    SCOUT         // Scouts and reveals uncharted tiles ahead
}