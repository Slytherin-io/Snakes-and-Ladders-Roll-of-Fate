package io.github.snakes_and_ladders_roll_of_fate.systems;

// import java.util.ArrayList;
// import java.util.List;
import java.util.Random;

public class Dialogue {
    // Snake Prefixes
    private String [] snakePrefixes = {
        "Suddenly,", 
        "Emerging from the mist,", 
        "With a bone-chilling rattle,"
    };

    // Snake Subjects
    private String [] snakes = {
        "a gargantuan emerald viper", 
        "a shadowy horned python", 
        "a pair of blood-red adders"};
    
    // Snake Actions
    private String [] snakeActions = {
        "blocks your ascent!", 
        "demands a sacrifice of flesh!", 
        "coils tightly around the path!"
    };
    
    // Ladder Prefixes (Atmosphere / Revelation)
    private String[] ladderPrefixes = {
        "Beaming through the canopy,",
        "Carved into the sheer cliffside,",
        "Humming with faint arcane energy,",
        "Anchored deep into the stone,"
    };

    // Ladder Subjects (The Structure)
    private String[] ladders = {
        "a spiral of glowing ethereal steps",
        "a sturdy chain of enchanted brass rungs",
        "an ancient rope ladder braided with glowing moss",
        "a grand set of weathered marble stairs"
    };

    // Ladder Actions (The Ascent)
    private String[] ladderActions = {
        "beckons you toward the heavens!",
        "promises a swift bypass over mortal perils!",
        "sways gently, inviting an ambitious leap!",
        "stands steadfast, defying the dark abyss below!"
    };

    // Normal Prefixes (Atmosphere / Pacing)
    private String[] normalPrefixes = {
        "Under a pale, silent sky,",
        "Stepping past ancient cobblestones,",
        "Amidst a quiet rustle of the wind,",
        "Guided by the faint glow of lanterns,"
    };

    // Normal Subjects (Environment / State of the Path)
    private String[] normalScenes = {
        "a peaceful roadside shrine",
        "a sheltered pocket between the rocks",
        "an undisturbed stretch of the high road",
        "a weathered milestone marker"
    };

    // Normal Actions (Player's Respite)
    private String[] normalActions = {
        "allows you a brief moment to catch your breath.",
        "offers quiet respite before the next trial.",
        "stretches ahead without danger in sight.",
        "leaves you in contemplative silence as you steady your resolve."
    };

    private Random random;

    public Dialogue () {
        this.random = new Random();
    }

    private String getRandomElement(String[] pool) {
        return pool[random.nextInt(pool.length)];
    }

    public String createSnakeDialogue() {
        return String.format("%s %s %s", getRandomElement(snakePrefixes), getRandomElement(snakes), getRandomElement(snakeActions));
    }
    
    public String createLadderDialogue() {
        return String.format("%s %s %s", getRandomElement(ladderPrefixes), getRandomElement(ladders), getRandomElement(ladderActions));
    }

    public String createNormalDialogue() {
        return String.format("%s %s %s", getRandomElement(normalPrefixes), getRandomElement(normalScenes), getRandomElement(normalActions));
    }
    
}