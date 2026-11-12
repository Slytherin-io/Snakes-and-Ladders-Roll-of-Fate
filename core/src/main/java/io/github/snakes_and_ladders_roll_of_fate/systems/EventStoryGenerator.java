package io.github.snakes_and_ladders_roll_of_fate.systems;

import java.util.Random;

public class EventStoryGenerator {
    private static final Random random = new Random();

    // Helper to pick a random string from an array
    private static String pick(String[] pool) {
        return pool[random.nextInt(pool.length)];
    }

    // --- SHRINE / BLESSING POOLS ---
    private static final String[] SHRINE_TITLES = {
        "Forgotten Moon Shrine", "Consecrated Altar", "Celestial Waypoint", "Shrine of the Old Gods"
    };
    private static final String[] SHRINE_PREFIXES = {
        "Hidden deep within the overgrowth,",
        "Perched atop a wind-swept rocky crest,",
        "Carved into the cold limestone cavern,",
        "Illuminated by a shaft of pale moonlight,"
    };
    private static final String[] SHRINE_SUBJECTS = {
        "a moss-covered weeping goddess statue",
        "an ancient runic obelisk",
        "a consecrated marble font",
        "a serene crystal pedestal"
    };
    private static final String[] SHRINE_AURAS = {
        "radiates a soothing restorative warmth.",
        "pulses with dormant martial power.",
        "whispers ancient combat techniques on the wind.",
        "shimmers with protective celestial wards."
    };

    // --- SNAKE AMBUSH POOLS ---
    private static final String[] SNAKE_TITLES = {
        "Venomous Serpent Ambush", "Serpent of the Abyss", "Coiling Dread Pit", "Viper Roadblock"
    };
    private static final String[] SNAKE_PREFIXES = {
        "From the jagged shadows above,",
        "Lurking beneath fractured flagstones,",
        "With a venomous, bone-chilling rattle,",
        "Emerging from the suffocating fog,"
    };
    private static final String[] SNAKE_SUBJECTS = {
        "a gargantuan emerald viper",
        "a shadowy horned basilisk",
        "a pair of blood-red adders",
        "an ancient scaled dread-serpent"
    };
    private static final String[] SNAKE_THREATS = {
        "coils menacingly, threatening to drag you down the slope!",
        "strikes with lightning speed toward your path!",
        "bars the mountain trail with bared dripping fangs!",
        "wraps around the rocky ledge, demanding blood!"
    };

    // --- LADDER ASCENT POOLS ---
    private static final String[] LADDER_TITLES = {
        "Celestial Ascendance", "Ancient Mountain Ropeway", "Arcane Brass Stairway", "Pilgrim's Pathway"
    };
    private static final String[] LADDER_PREFIXES = {
        "Anchored deep into the sheer cliffside,",
        "Carved by forgotten pilgrims through the rock,",
        "Suspended over a dizzying void,",
        "Humming with faint arcane resonance,"
    };
    private static final String[] LADDER_SUBJECTS = {
        "a grand flight of weathered marble steps",
        "a sturdy iron-rung ladder braided with glowing moss",
        "a spiral of shimmering ethereal rungs",
        "an ancient reinforced chain ascent"
    };
    private static final String[] LADDER_CALLS = {
        "beckons you toward the heights above.",
        "promises a safe bypass over mortal perils below.",
        "sways gently in the wind, daring an ambitious climb.",
        "stands steadfast against the dark abyss."
    };

    // --- PUBLIC GENERATORS ---
    public static String[] generateShrineStory() {
        String title = pick(SHRINE_TITLES);
        String desc = String.format("%s %s %s", pick(SHRINE_PREFIXES), pick(SHRINE_SUBJECTS), pick(SHRINE_AURAS));
        return new String[] { title, desc };
    }

    public static String[] generateSnakeStory() {
        String title = pick(SNAKE_TITLES);
        String desc = String.format("%s %s %s", pick(SNAKE_PREFIXES), pick(SNAKE_SUBJECTS), pick(SNAKE_THREATS));
        return new String[] { title, desc };
    }

    public static String[] generateLadderStory() {
        String title = pick(LADDER_TITLES);
        String desc = String.format("%s %s %s", pick(LADDER_PREFIXES), pick(LADDER_SUBJECTS), pick(LADDER_CALLS));
        return new String[] { title, desc };
    }
}
