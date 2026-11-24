package io.github.snakes_and_ladders_roll_of_fate.systems;

import java.util.Random;

public class EventStoryGenerator {
    private static final Random random = new Random();

    private static String pick(String[] pool) {
        return pool[random.nextInt(pool.length)];
    }

    public static int getAct(int tilePosition) {
        if (tilePosition <= 30) return 1;
        if (tilePosition <= 70) return 2;
        return 3;
    }

    // ==========================================
    // --- ACT I: THE FORGOTTEN FOOTHILLS (1-30)
    // ==========================================

    private static final String[] ACT1_SHRINE_TITLES = {
        "Overgrown Woodland Shrine", "Altar of the Spring Breeze", "Weathered Trail Marker", "Wayfarer's Hearth"
    };
    private static final String[] ACT1_SHRINE_DESCRIPTIONS = {
        "A mossy woodland idol radiates a gentle, soothing warmth.",
        "A clear mountain basin whispers tranquil blessings of safe passage.",
        "An ancient runestone glows with pale green restorative light.",
        "Prayer ribbons flutter softly in a sunlit forest glade."
    };

    private static final String[] ACT1_SNAKE_TITLES = {
        "Emerald Grass Viper", "Hissing Thicket Stalker", "Bramble-Coiled Adder", "Meadow Pit Viper"
    };
    private static final String[] ACT1_SNAKE_DESCRIPTIONS = {
        "An emerald grass viper hisses, barring the wooded trail!",
        "A mottled pit viper coils to strike from a mossy bough!",
        "A territorial banded adder rattles its tail warningly!",
        "A venomous green serpent snaps defensively from the brush!"
    };

    private static final String[] ACT1_LADDER_TITLES = {
        "Old Woodland Ropeway", "Carved Stone Steps", "Sturdy Timber Ladder", "Wayfarer's Footholds"
    };
    private static final String[] ACT1_LADDER_DESCRIPTIONS = {
        "A sturdy timber ladder offers a peaceful shortcut upward.",
        "Shallow stone footholds beckon toward the higher trail.",
        "A knotted ropeway provides steady footing into the hills.",
        "A rustic flight of wooden steps welcomes your ascent."
    };

    private static final String[] ACT1_MERCHANT_TITLES = {
        "The Forest Herbalist", "Nomadic Tinker", "Traveling Storyteller", "Village Peddler"
    };
    private static final String[] ACT1_MERCHANT_DESCRIPTIONS = {
        "A kindly herbalist offers modest remedies for the road ahead.",
        "A cheery tinker displays sturdy ironware and basic wares.",
        "A weathered wanderer shares useful supplies from a worn knapsack.",
        "A friendly peddler offers honest trades for weary boots."
    };

    // ==========================================
    // --- ACT II: THE CRAGS & CAVERNS (31-70)
    // ==========================================

    private static final String[] ACT2_SHRINE_TITLES = {
        "Shrine of the Broken Spear", "Cavern Font of Stygian Waters", "The Hermit's Iron Obelisk", "Altar of the Tempest"
    };
    private static final String[] ACT2_SHRINE_DESCRIPTIONS = {
        "A menacing iron obelisk pulses with aggressive martial power.",
        "A volcanic mineral font radiates heat that hardens resolve.",
        "A basalt stone monument hums with ancient defensive wards.",
        "A weeping effigy bound in iron whispers grim warnings."
    };

    private static final String[] ACT2_SNAKE_TITLES = {
        "Gorgon Basilisk Ambush", "Shadow-Horned Python", "Obsidian Cliff Viper", "Chasm Dread-Cobra"
    };
    private static final String[] ACT2_SNAKE_DESCRIPTIONS = {
        "A shadow-horned basilisk lunges from the sulfurous mist!",
        "Obsidian cliff vipers drip corrosive venom from jagged rocks!",
        "A monstrous dread-cobra bars the narrow mountain ledge!",
        "A horned serpent strikes, demanding flesh or sacrifice!"
    };

    private static final String[] ACT2_LADDER_TITLES = {
        "Chasm Suspension Ladder", "Rusted Iron Cliffway", "Fragile Vine Ropeway", "The Miner's Scaffold"
    };
    private static final String[] ACT2_LADDER_DESCRIPTIONS = {
        "A dizzying iron chain ladder dares a treacherous climb.",
        "A weathered ropeway promises a daring shortcut over the chasm.",
        "Precarious scaffolding groans in the howling cavern wind.",
        "An iron-spiked cliffway scales the sheer cavern rockface."
    };

    private static final String[] ACT2_MERCHANT_TITLES = {
        "The Shady Smuggler", "Exiled Battle-Smith", "Alchemist of the Red Mire", "Grave-Robbing Peddler"
    };
    private static final String[] ACT2_MERCHANT_DESCRIPTIONS = {
        "A scarred smuggler deals in forbidden arms from the shadows.",
        "An exiled smith tests tempered blades beside an iron brazier.",
        "A hooded alchemist brews potent elixirs in steaming cauldrons.",
        "A cavern peddler profits from the gear of fallen climbers."
    };

    // ==========================================
    // --- ACT III: THE SPIRE OF FATE (71-100)
    // ==========================================

    private static final String[] ACT3_SHRINE_TITLES = {
        "Altar of the Celestial Wyrm", "Shrine of the Ascendant Flame", "Pinnacle Font of Eternity", "The Void Sanctum"
    };
    private static final String[] ACT3_SHRINE_DESCRIPTIONS = {
        "A golden celestial altar blazes with blinding solar fire.",
        "A crystalline monolith hums with raw cosmic energy.",
        "A draconian dais radiates supreme power near the clouds.",
        "A starry font bestows divine vitality upon the worthy."
    };

    private static final String[] ACT3_SNAKE_TITLES = {
        "The Sky-Wyrm Dread-Lord", "Serpent of the Eternal Abyss", "Ancient Celestial Ouroboros", "Apex Titan Viper"
    };
    private static final String[] ACT3_SNAKE_DESCRIPTIONS = {
        "The mythical Sky-Wyrm descends with crackling thunder!",
        "An ancient titan viper breathes scalding ash across the peak!",
        "A celestial dread-serpent roars, shaking the mountain!",
        "A titanic dragon-wyrm coils tightly around the final spire!"
    };

    private static final String[] ACT3_LADDER_TITLES = {
        "The Stairway of the Stars", "Golden Ethereal Rungs", "The Summit's Champion Way", "Bifrost Ascent"
    };
    private static final String[] ACT3_LADDER_DESCRIPTIONS = {
        "A spiral of solid light ascends into the starry heavens.",
        "Golden ethereal steps hum with divine triumph above the clouds.",
        "An ancient titan staircase leads directly to the mountain's crown.",
        "A bridge of celestial energy beckons toward glorious victory."
    };

    private static final String[] ACT3_MERCHANT_TITLES = {
        "The Fate-Weaver", "Collector of Ascended Souls", "Cosmic Relic Dealer", "The Hermit of the Apex"
    };
    private static final String[] ACT3_MERCHANT_DESCRIPTIONS = {
        "A celestial fate-weaver offers supreme relics at the peak.",
        "An ascended collector barters mythical tools of destiny.",
        "A cosmic dealer tests whether your spirit is worthy of victory.",
        "A timeless oracle watches as fate hangs in the balance."
    };

    // ==========================================
    // --- PUBLIC GENERATORS (ACT-AWARE)
    // ==========================================

    public static String[] generateShrineStory(int tilePosition) {
        int act = getAct(tilePosition);
        if (act == 1) {
            String title = pick(ACT1_SHRINE_TITLES);
            String desc = pick(ACT1_SHRINE_DESCRIPTIONS);
            return new String[] { title, desc };
        } else if (act == 2) {
            String title = pick(ACT2_SHRINE_TITLES);
            String desc = pick(ACT2_SHRINE_DESCRIPTIONS);
            return new String[] { title, desc };
        } else {
            String title = pick(ACT3_SHRINE_TITLES);
            String desc = pick(ACT3_SHRINE_DESCRIPTIONS);
            return new String[] { title, desc };
        }
    }

    public static String[] generateSnakeStory(int tilePosition) {
        int act = getAct(tilePosition);
        if (act == 1) {
            String title = pick(ACT1_SNAKE_TITLES);
            String desc = pick(ACT1_SNAKE_DESCRIPTIONS);
            return new String[] { title, desc };
        } else if (act == 2) {
            String title = pick(ACT2_SNAKE_TITLES);
            String desc = pick(ACT2_SNAKE_DESCRIPTIONS);
            return new String[] { title, desc };
        } else {
            String title = pick(ACT3_SNAKE_TITLES);
            String desc = pick(ACT3_SNAKE_DESCRIPTIONS);
            return new String[] { title, desc };
        }
    }

    public static String[] generateLadderStory(int tilePosition) {
        int act = getAct(tilePosition);
        if (act == 1) {
            String title = pick(ACT1_LADDER_TITLES);
            String desc = pick(ACT1_LADDER_DESCRIPTIONS);
            return new String[] { title, desc };
        } else if (act == 2) {
            String title = pick(ACT2_LADDER_TITLES);
            String desc = pick(ACT2_LADDER_DESCRIPTIONS);
            return new String[] { title, desc };
        } else {
            String title = pick(ACT3_LADDER_TITLES);
            String desc = pick(ACT3_LADDER_DESCRIPTIONS);
            return new String[] { title, desc };
        }
    }

    public static String[] generateMerchantStory(int tilePosition) {
        int act = getAct(tilePosition);
        if (act == 1) {
            String title = pick(ACT1_MERCHANT_TITLES);
            String desc = pick(ACT1_MERCHANT_DESCRIPTIONS);
            return new String[] { title, desc };
        } else if (act == 2) {
            String title = pick(ACT2_MERCHANT_TITLES);
            String desc = pick(ACT2_MERCHANT_DESCRIPTIONS);
            return new String[] { title, desc };
        } else {
            String title = pick(ACT3_MERCHANT_TITLES);
            String desc = pick(ACT3_MERCHANT_DESCRIPTIONS);
            return new String[] { title, desc };
        }
    }

    // Backward compatibility overloads (defaulting to mid-board position 50)
    public static String[] generateShrineStory() { return generateShrineStory(50); }
    public static String[] generateSnakeStory() { return generateSnakeStory(50); }
    public static String[] generateLadderStory() { return generateLadderStory(50); }
    public static String[] generateMerchantStory() { return generateMerchantStory(50); }
}
