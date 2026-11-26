package io.github.snakes_and_ladders_roll_of_fate.systems;

public enum EventEffectType {
    HEALTH,       // Heals or damages the player
    MAX_HEALTH,   // Permanently increases or decreases max health
    ATTACK,       // Buffs or debuffs attack power (1-turn buff when used from an item)
    DEFENSE,      // Buffs or debuffs defense power (1-turn buff when used from an item)
    EXPERIENCE,   // Grants or trades EXP towards leveling up
    MOVE,         // Moves player forward (+) or backward (-)
    ITEM,         // Grants an item or loot drop to the player
    SCOUT,        // Scouts and reveals uncharted tiles ahead
    DICE_EXACT,   // Lets the player choose their exact roll (1-6) on their next roll
    DICE_CLAMP,   // Caps the player's next dice roll to a maximum value
    SNAKE_WARD,   // Pacifies the next Snake Tile landed on, skipping battle and slide
    SWAP_PLAYER   // Swaps positions with the leading rival player (or leaps forward in solo)
}
