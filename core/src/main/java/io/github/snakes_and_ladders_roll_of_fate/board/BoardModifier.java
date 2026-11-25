package io.github.snakes_and_ladders_roll_of_fate.board;

/**
 * Run-level Fate Modifiers rolled at the start of every board generation.
 * Dynamically shapes tile spawn probabilities, jump ranges, POI density,
 * and risk/reward multipliers so every playthrough has a distinct strategic identity.
 */
public enum BoardModifier {
    BALANCED_ASCENT(
        "Balanced Ascent",
        "Classic mountain pacing with balanced ladders, serpents, and roadside shrines.",
        0, 0, 1.0f, 1.0f, 4, 4, 3, 6, 18, 6, 20
    ),
    SERPENTS_ECLIPSE(
        "Serpent's Eclipse",
        "+8% Serpent density on the trail, but slain serpents yield +50% EXP.",
        -2, 8, 1.0f, 1.5f, 3, 4, 3, 6, 18, 6, 20
    ),
    MERCHANT_CARAVAN(
        "Merchant Caravan",
        "Abundant Traveling Merchants & Treasure Caches; ancient Shrines are scarce.",
        0, -2, 1.0f, 1.0f, 2, 7, 5, 6, 18, 6, 20
    ),
    CELESTIAL_ALIGNMENT(
        "Celestial Alignment",
        "Longer Celestial Ladders (+22 max) & extra Shrines, but deeper Serpent drops (-26 max).",
        5, 2, 1.0f, 1.0f, 6, 3, 3, 8, 22, 8, 26
    ),
    SHROUDED_LABYRINTH(
        "Shrouded Labyrinth",
        "Rich in Treasure Caches & Crossroads; +50% Discovery EXP on uncharted tiles.",
        0, 3, 1.5f, 1.15f, 4, 4, 6, 6, 18, 6, 22
    );

    private final String displayName;
    private final String description;
    private final int ladderChanceDelta;
    private final int snakeChanceDelta;
    private final float discoveryExpMultiplier;
    private final float combatExpMultiplier;
    private final int targetShrines;
    private final int targetMerchants;
    private final int targetTreasures;
    private final int minLadderJump;
    private final int maxLadderJump;
    private final int minSnakeDrop;
    private final int maxSnakeDrop;

    BoardModifier(String displayName, String description,
                  int ladderChanceDelta, int snakeChanceDelta,
                  float discoveryExpMultiplier, float combatExpMultiplier,
                  int targetShrines, int targetMerchants, int targetTreasures,
                  int minLadderJump, int maxLadderJump,
                  int minSnakeDrop, int maxSnakeDrop) {
        this.displayName = displayName;
        this.description = description;
        this.ladderChanceDelta = ladderChanceDelta;
        this.snakeChanceDelta = snakeChanceDelta;
        this.discoveryExpMultiplier = discoveryExpMultiplier;
        this.combatExpMultiplier = combatExpMultiplier;
        this.targetShrines = targetShrines;
        this.targetMerchants = targetMerchants;
        this.targetTreasures = targetTreasures;
        this.minLadderJump = minLadderJump;
        this.maxLadderJump = maxLadderJump;
        this.minSnakeDrop = minSnakeDrop;
        this.maxSnakeDrop = maxSnakeDrop;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public int getLadderChanceDelta() { return ladderChanceDelta; }
    public int getSnakeChanceDelta() { return snakeChanceDelta; }
    public float getDiscoveryExpMultiplier() { return discoveryExpMultiplier; }
    public float getCombatExpMultiplier() { return combatExpMultiplier; }
    public int getTargetShrines() { return targetShrines; }
    public int getTargetMerchants() { return targetMerchants; }
    public int getTargetTreasures() { return targetTreasures; }
    public int getMinLadderJump() { return minLadderJump; }
    public int getMaxLadderJump() { return maxLadderJump; }
    public int getMinSnakeDrop() { return minSnakeDrop; }
    public int getMaxSnakeDrop() { return maxSnakeDrop; }
}
