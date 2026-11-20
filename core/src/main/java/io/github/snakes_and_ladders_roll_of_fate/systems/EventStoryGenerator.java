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
    private static final String[] ACT1_SHRINE_PREFIXES = {
        "Nestled beneath ancient willow branches,",
        "Beside a trickling crystal stream,",
        "Carved into a moss-covered boulder,",
        "Where wild lavender blooms along the path,"
    };
    private static final String[] ACT1_SHRINE_SUBJECTS = {
        "a humble wooden idol of the Forest Keeper",
        "a smooth basin of clear mountain runoff",
        "an ancient carved runestone bound with ivy",
        "a sunlit clearing marked with prayer ribbons"
    };
    private static final String[] ACT1_SHRINE_AURAS = {
        "radiates a gentle, warming vitality.",
        "whispers tranquil promises of safe passage.",
        "soothes your travel-weary muscles.",
        "glows with a pale green, restorative glimmer."
    };

    private static final String[] ACT1_SNAKE_TITLES = {
        "Emerald Grass Viper", "Hissing Thicket Stalker", "Bramble-Coiled Adder", "Meadow Pit Viper"
    };
    private static final String[] ACT1_SNAKE_PREFIXES = {
        "Rustling through the tall meadow grass,",
        "Dropping from a low-hanging mossy branch,",
        "Coiled silently around a sun-warmed milestone,",
        "Slithering between the cracked cobblestones,"
    };
    private static final String[] ACT1_SNAKE_SUBJECTS = {
        "a swift emerald grass viper",
        "a mottled green pit viper with needle fangs",
        "a territorial banded adder",
        "a venomous serpent with vibrant jade scales"
    };
    private static final String[] ACT1_SNAKE_THREATS = {
        "hisses violently, barring the wooded trail!",
        "coils to strike at your ankles!",
        "rattles its tail warningly, demanding you turn back!",
        "snaps defensively, threatening to trip you down the slope!"
    };

    private static final String[] ACT1_LADDER_TITLES = {
        "Old Woodland Ropeway", "Carved Stone Steps", "Sturdy Timber Ladder", "Wayfarer's Footholds"
    };
    private static final String[] ACT1_LADDER_PREFIXES = {
        "Supported by ancient oak trunks,",
        "Carved gently into the low earthen embankment,",
        "Reinforced with freshly bound hemp rope,",
        "Resting steadfast against the grassy ledge,"
    };
    private static final String[] ACT1_LADDER_SUBJECTS = {
        "a well-maintained wooden ladder",
        "a short series of shallow stone footholds",
        "a knotted rope dangling from a sturdy bough",
        "a rustic flight of timber steps"
    };
    private static final String[] ACT1_LADDER_CALLS = {
        "offers an easy, peaceful shortcut through the foliage.",
        "beckons you toward the higher trail.",
        "provides steady footing into the hills above.",
        "sways gently in the mountain breeze, welcoming your ascent."
    };

    private static final String[] ACT1_MERCHANT_TITLES = {
        "The Forest Herbalist", "Nomadic Tinker", "Traveling Storyteller", "Village Peddler"
    };
    private static final String[] ACT1_MERCHANT_PREFIXES = {
        "Resting on a tree stump by the road,",
        "Unpacking a worn leather knapsack beside a campfire,",
        "Polishing copper trinkets under the shade of a hazel tree,",
        "Humming a countryside tune with a donkey nearby,"
    };
    private static final String[] ACT1_MERCHANT_SUBJECTS = {
        "a kindly herbalist carrying fresh dried roots",
        "a cheery tinker surrounded by sturdy ironware",
        "a weathered wanderer with bundles of useful supplies",
        "a humble trader offering traveler's remedies"
    };
    private static final String[] ACT1_MERCHANT_PITCHES = {
        "greets you with a warm nod and offers modest wares.",
        "shares advice for the climbing road ahead.",
        "offers honest trades to help you start your journey.",
        "displays basic potions and sturdy tools for weary boots."
    };

    // ==========================================
    // --- ACT II: THE CRAGS & CAVERNS (31-70)
    // ==========================================

    private static final String[] ACT2_SHRINE_TITLES = {
        "Shrine of the Broken Spear", "Cavern Font of Stygian Waters", "The Hermit's Iron Obelisk", "Altar of the Tempest"
    };
    private static final String[] ACT2_SHRINE_PREFIXES = {
        "Perched over a sheer, bottomless ravine,",
        "Hidden inside a sulfur-scented cavern pocket,",
        "Carved out of cold, jagged basalt,",
        "Surrounded by wind-blasted skeletal trees,"
    };
    private static final String[] ACT2_SHRINE_SUBJECTS = {
        "a menacing iron obelisk etched with martial oaths",
        "a dark font of bubbling volcanic mineral water",
        "a stone monument adorned with broken shields",
        "a weeping effigy bound in rusted iron chains"
    };
    private static final String[] ACT2_SHRINE_AURAS = {
        "pulses with aggressive, surging power.",
        "radiates bitter heat that hardens your resolve.",
        "hums with defensive wards forged in old battles.",
        "whispers warnings of mortal betrayal."
    };

    private static final String[] ACT2_SNAKE_TITLES = {
        "Gorgon Basilisk Ambush", "Shadow-Horned Python", "Obsidian Cliff Viper", "Chasm Dread-Cobra"
    };
    private static final String[] ACT2_SNAKE_PREFIXES = {
        "Striking from the jagged crags above,",
        "Slithering out from a dark subterranean chasm,",
        "Coiling tightly around the crumbling narrow cliff-path,",
        "Emerging from a pool of bubbling sulfuric sludge,"
    };
    private static final String[] ACT2_SNAKE_SUBJECTS = {
        "a massive shadow-horned basilisk with petrifying eyes",
        "a pair of obsidian cliff vipers dripping corrosive venom",
        "a monstrous chasm dread-cobra as thick as a tree trunk",
        "a blood-red pit viper bearing jagged bone spikes"
    };
    private static final String[] ACT2_SNAKE_THREATS = {
        "lunges with bone-crushing force toward your throat!",
        "coils menacingly, threatening to drag you down into the rocky chasm!",
        "bars the only narrow mountain ledge with bared fangs!",
        "wraps around the crumbling rocks, demanding flesh or sacrifice!"
    };

    private static final String[] ACT2_LADDER_TITLES = {
        "Chasm Suspension Ladder", "Rusted Iron Cliffway", "Fragile Vine Ropeway", "The Miner's Scaffold"
    };
    private static final String[] ACT2_LADDER_PREFIXES = {
        "Anchored into the splintering slate rockface,",
        "Dangling precariously over an echoing bottomless drop,",
        "Constructed from rusted mining chains and weathered pine,",
        "Groaning under the howling gale force winds,"
    };
    private static final String[] ACT2_LADDER_SUBJECTS = {
        "a dizzying iron-chain ladder swaying in the abyss",
        "a weathered ropeway with missing wooden rungs",
        "a precarious miners' scaffolding lashed with rope",
        "a sheer rock-face ladder reinforced with iron spikes"
    };
    private static final String[] ACT2_LADDER_CALLS = {
        "dares you to brave a treacherous, wind-swept climb.",
        "promises a massive shortcut over deadly cavern pits.",
        "sways violently in the abyss, testing your courage.",
        "creaks underfoot, inviting an ambitious but perilous leap."
    };

    private static final String[] ACT2_MERCHANT_TITLES = {
        "The Shady Smuggler", "Exiled Battle-Smith", "Alchemist of the Red Mire", "Grave-Robbing Peddler"
    };
    private static final String[] ACT2_MERCHANT_PREFIXES = {
        "Squatting beside an iron brazier in a windless alcove,",
        "Concealed behind a heavy cloak of wolf pelts,",
        "Tapping a bloodstained dagger against an ornate strongbox,",
        "Peering from the shadows of an abandoned minecart,"
    };
    private static final String[] ACT2_MERCHANT_SUBJECTS = {
        "a scarred battle-smith testing the edge of tempered blades",
        "a sinister smuggler dealing in forbidden artifacts",
        "a hooded apothecary surrounded by pungent boiling cauldrons",
        "a calculating opportunist who profits from fallen climbers"
    };
    private static final String[] ACT2_MERCHANT_PITCHES = {
        "demands heavy survival tolls in blood, lore, or steel.",
        "offers deadly arms that could mean the difference between life and death.",
        "whispers that morality has no place on these treacherous slopes.",
        "reveals rare military gear for those desperate enough to pay."
    };

    // ==========================================
    // --- ACT III: THE SPIRE OF FATE (71-100)
    // ==========================================

    private static final String[] ACT3_SHRINE_TITLES = {
        "Altar of the Celestial Wyrm", "Shrine of the Ascendant Flame", "Pinnacle Font of Eternity", "The Void Sanctum"
    };
    private static final String[] ACT3_SHRINE_PREFIXES = {
        "Standing at the edge of the sky amidst swirling thunderclouds,",
        "Carved out of luminescent celestial meteor-stone,",
        "Encircled by hovering runic halos above the frozen void,",
        "Where the mountain pierces the starry cosmos,"
    };
    private static final String[] ACT3_SHRINE_SUBJECTS = {
        "a majestic golden altar radiating blinding solar fire",
        "a floating crystalline monolith humming with cosmic energy",
        "a draconian dais adorned with petrified dragon wings",
        "a font of shimmering celestial starlight"
    };
    private static final String[] ACT3_SHRINE_AURAS = {
        "bestows godlike vitality upon those who dare approach.",
        "burns with raw, untamed celestial fury.",
        "demands a supreme sacrifice in exchange for absolute triumph.",
        "whispers the secrets of fate as the final peak draws near."
    };

    private static final String[] ACT3_SNAKE_TITLES = {
        "The Sky-Wyrm Dread-Lord", "Serpent of the Eternal Abyss", "Ancient Celestial Ouroboros", "Apex Titan Viper"
    };
    private static final String[] ACT3_SNAKE_PREFIXES = {
        "Descending from the eye of the swirling maelstrom,",
        "Coiled around the spire itself with scales like armored plates,",
        "Bursting through the sea of clouds with deafening thunder,",
        "Rising from the frozen precipice with eyes of burning hellfire,"
    };
    private static final String[] ACT3_SNAKE_SUBJECTS = {
        "the mythical Sky-Wyrm with razor-sharp celestial talons",
        "the ancient Apex Titan Viper whose breath turns stone to ash",
        "a colossal Dread-Lord dragon-serpent crowned in lightning",
        "a gargantuan celestial leviathan blocking the final ascent"
    };
    private static final String[] ACT3_SNAKE_THREATS = {
        "unleashes a roar that shakes the very mountain to its foundation!",
        "strikes with cataclysmic power, threatening to cast you down 50 tiles!",
        "bared fangs crackle with lightning as it demands your final reckoning!",
        "coils entirely around the peak, guarding the throne of victory!"
    };

    private static final String[] ACT3_LADDER_TITLES = {
        "The Stairway of the Stars", "Golden Ethereal Rungs", "The Summit's Champion Way", "Bifrost Ascent"
    };
    private static final String[] ACT3_LADDER_PREFIXES = {
        "Weaving seamlessly through the heavens themselves,",
        "Forged from pure condensed starlight and cosmic mana,",
        "Defying gravity as it floats over the top of the world,",
        "Radiating a blinding aura of golden triumph,"
    };
    private static final String[] ACT3_LADDER_SUBJECTS = {
        "a magnificent spiral of solid light ascending into the clouds",
        "an ethereal flight of golden steps humming with divine resonance",
        "an ancient titan staircase carved from the mountain's crown",
        "a shimmering bridge of pure celestial energy"
    };
    private static final String[] ACT3_LADDER_CALLS = {
        "leads directly toward glorious victory at the mountain's zenith!",
        "beckons the worthy to take their place among legends.",
        "promises the ultimate shortcut to conquer the summit.",
        "glows with triumphant brilliance, welcoming the conqueror of fate."
    };

    private static final String[] ACT3_MERCHANT_TITLES = {
        "The Fate-Weaver", "Collector of Ascended Souls", "Cosmic Relic Dealer", "The Hermit of the Apex"
    };
    private static final String[] ACT3_MERCHANT_PREFIXES = {
        "Hovering serenely above the clouds in meditative levitation,",
        "Sitting upon a throne of fallen meteorites at the edge of the world,",
        "Cloaked in fabric spun from starlight and cosmic dust,",
        "Appearing in a flash of blinding celestial radiance,"
    };
    private static final String[] ACT3_MERCHANT_SUBJECTS = {
        "an ethereal entity with eyes that reflect the multiverse",
        "a timeless oracle who barters the very fabric of destiny",
        "a cosmic collector offering mythical game-winning relics",
        "an ancient master testing whether your spirit is worthy"
    };
    private static final String[] ACT3_MERCHANT_PITCHES = {
        "offers supreme artifacts in exchange for your greatest sacrifices.",
        "warns that only the boldest gambles will claim the peak.",
        "presents masterwork relics that can seal your ascension.",
        "smiles as fate hangs in the balance on your final steps."
    };

    // ==========================================
    // --- PUBLIC GENERATORS (ACT-AWARE)
    // ==========================================

    public static String[] generateShrineStory(int tilePosition) {
        int act = getAct(tilePosition);
        if (act == 1) {
            String title = pick(ACT1_SHRINE_TITLES);
            String desc = String.format("%s %s %s", pick(ACT1_SHRINE_PREFIXES), pick(ACT1_SHRINE_SUBJECTS), pick(ACT1_SHRINE_AURAS));
            return new String[] { title, desc };
        } else if (act == 2) {
            String title = pick(ACT2_SHRINE_TITLES);
            String desc = String.format("%s %s %s", pick(ACT2_SHRINE_PREFIXES), pick(ACT2_SHRINE_SUBJECTS), pick(ACT2_SHRINE_AURAS));
            return new String[] { title, desc };
        } else {
            String title = pick(ACT3_SHRINE_TITLES);
            String desc = String.format("%s %s %s", pick(ACT3_SHRINE_PREFIXES), pick(ACT3_SHRINE_SUBJECTS), pick(ACT3_SHRINE_AURAS));
            return new String[] { title, desc };
        }
    }

    public static String[] generateSnakeStory(int tilePosition) {
        int act = getAct(tilePosition);
        if (act == 1) {
            String title = pick(ACT1_SNAKE_TITLES);
            String desc = String.format("%s %s %s", pick(ACT1_SNAKE_PREFIXES), pick(ACT1_SNAKE_SUBJECTS), pick(ACT1_SNAKE_THREATS));
            return new String[] { title, desc };
        } else if (act == 2) {
            String title = pick(ACT2_SNAKE_TITLES);
            String desc = String.format("%s %s %s", pick(ACT2_SNAKE_PREFIXES), pick(ACT2_SNAKE_SUBJECTS), pick(ACT2_SNAKE_THREATS));
            return new String[] { title, desc };
        } else {
            String title = pick(ACT3_SNAKE_TITLES);
            String desc = String.format("%s %s %s", pick(ACT3_SNAKE_PREFIXES), pick(ACT3_SNAKE_SUBJECTS), pick(ACT3_SNAKE_THREATS));
            return new String[] { title, desc };
        }
    }

    public static String[] generateLadderStory(int tilePosition) {
        int act = getAct(tilePosition);
        if (act == 1) {
            String title = pick(ACT1_LADDER_TITLES);
            String desc = String.format("%s %s %s", pick(ACT1_LADDER_PREFIXES), pick(ACT1_LADDER_SUBJECTS), pick(ACT1_LADDER_CALLS));
            return new String[] { title, desc };
        } else if (act == 2) {
            String title = pick(ACT2_LADDER_TITLES);
            String desc = String.format("%s %s %s", pick(ACT2_LADDER_PREFIXES), pick(ACT2_LADDER_SUBJECTS), pick(ACT2_LADDER_CALLS));
            return new String[] { title, desc };
        } else {
            String title = pick(ACT3_LADDER_TITLES);
            String desc = String.format("%s %s %s", pick(ACT3_LADDER_PREFIXES), pick(ACT3_LADDER_SUBJECTS), pick(ACT3_LADDER_CALLS));
            return new String[] { title, desc };
        }
    }

    public static String[] generateMerchantStory(int tilePosition) {
        int act = getAct(tilePosition);
        if (act == 1) {
            String title = pick(ACT1_MERCHANT_TITLES);
            String desc = String.format("%s %s %s", pick(ACT1_MERCHANT_PREFIXES), pick(ACT1_MERCHANT_SUBJECTS), pick(ACT1_MERCHANT_PITCHES));
            return new String[] { title, desc };
        } else if (act == 2) {
            String title = pick(ACT2_MERCHANT_TITLES);
            String desc = String.format("%s %s %s", pick(ACT2_MERCHANT_PREFIXES), pick(ACT2_MERCHANT_SUBJECTS), pick(ACT2_MERCHANT_PITCHES));
            return new String[] { title, desc };
        } else {
            String title = pick(ACT3_MERCHANT_TITLES);
            String desc = String.format("%s %s %s", pick(ACT3_MERCHANT_PREFIXES), pick(ACT3_MERCHANT_SUBJECTS), pick(ACT3_MERCHANT_PITCHES));
            return new String[] { title, desc };
        }
    }

    // Backward compatibility overloads (defaulting to mid-board position 50)
    public static String[] generateShrineStory() { return generateShrineStory(50); }
    public static String[] generateSnakeStory() { return generateSnakeStory(50); }
    public static String[] generateLadderStory() { return generateLadderStory(50); }
    public static String[] generateMerchantStory() { return generateMerchantStory(50); }
}
