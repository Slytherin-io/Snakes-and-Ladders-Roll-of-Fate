package io.github.snakes_and_ladders_roll_of_fate.ui;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Central registry for all game asset paths.
 * Keeping them here avoids typos and lets the IDE auto-complete paths.
 */
public class Assets {
    // UI Skin and text styles
    public static final AssetDescriptor<Skin> UI_SKIN =
        new AssetDescriptor<>("ui/uiskin.json", Skin.class);

    // Main menu visuals
    public static final AssetDescriptor<Texture> TITLE =
        new AssetDescriptor<>("img/menu_title.png", Texture.class);

    public static final AssetDescriptor<Texture> BUTTON_TEX =
        new AssetDescriptor<>("img/button_tex2.png", Texture.class);

    // Settings dialog graphics
    public static final AssetDescriptor<Texture> SCROLL =
        new AssetDescriptor<>("img/scroll.png", Texture.class);

    public static final AssetDescriptor<Texture> ICON_SOUND =
        new AssetDescriptor<>("img/icon_sound.png", Texture.class);

    public static final AssetDescriptor<Texture> ICON_MUSIC =
        new AssetDescriptor<>("img/icon_music.png", Texture.class);

    // Game Mode selection icons
    public static final AssetDescriptor<Texture> ICON_SINGLE =
        new AssetDescriptor<>("img/icon_single.png", Texture.class);

    public static final AssetDescriptor<Texture> ICON_MULTI =
        new AssetDescriptor<>("img/icon_multi.png", Texture.class);

    // Character sprites (Frame 0: Idle / Portrait, 224x224 - 75% enlarged)
    public static final int CHAR_FRAME_SIZE = 224;

    public static final AssetDescriptor<Texture> CHAR_CAT =
        new AssetDescriptor<>("img/char_cat.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_TURTLE =
        new AssetDescriptor<>("img/char_turtle.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_FOX =
        new AssetDescriptor<>("img/char_fox.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_RACCOON =
        new AssetDescriptor<>("img/char_raccoon.png", Texture.class);

    // Character Head Icons (for Mini-Map Progress Tracking & HUD Monitoring)
    public static final AssetDescriptor<Texture> CHAR_CAT_HEAD =
        new AssetDescriptor<>("img/char_cat_head.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_TURTLE_HEAD =
        new AssetDescriptor<>("img/char_turtle_head.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_FOX_HEAD =
        new AssetDescriptor<>("img/char_fox_head.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_RACCOON_HEAD =
        new AssetDescriptor<>("img/char_raccoon_head.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_PALE_SHEPHERD_HEAD =
        new AssetDescriptor<>("img/char_pale_shepherd_head.png", Texture.class);

    // Animated character walk spritesheets (224x224 per frame, 75% enlarged, grounded at common baseline)
    public static final AssetDescriptor<Texture> CHAR_CAT_WALK =
        new AssetDescriptor<>("img/char_cat_walk.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_TURTLE_WALK =
        new AssetDescriptor<>("img/char_turtle_walk.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_FOX_WALK =
        new AssetDescriptor<>("img/char_fox_walk.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_RACCOON_WALK =
        new AssetDescriptor<>("img/char_raccoon_walk.png", Texture.class);

    // Animated loading spinner spritesheet (576x576, 6x6 grid, 96x96 per frame)
    public static final AssetDescriptor<Texture> LOADING_SNAKE =
        new AssetDescriptor<>("img/loading_snake_spritesheet.png", Texture.class);

    // Enemy combat spritesheets (Act-stratified: 768x128, 6 frames of 128x128)
    // Act I: Forest Green Snake (Authentic pixel art sheet)
    // Act II: Sapphire Horned Serpent (Cavern/Mist horned viper)
    // Act III: Crimson Magma Wyrm (Volcanic horned dragon)
    public static final AssetDescriptor<Texture> ENEMY_SNAKE =
        new AssetDescriptor<>("img/enemy_snake.png", Texture.class);
    public static final AssetDescriptor<Texture> ENEMY_SNAKE_ACT1 =
        new AssetDescriptor<>("img/enemy_snake_act1.png", Texture.class);
    public static final AssetDescriptor<Texture> ENEMY_SNAKE_ACT2 =
        new AssetDescriptor<>("img/enemy_snake_act2.png", Texture.class);
    public static final AssetDescriptor<Texture> ENEMY_SNAKE_ACT3 =
        new AssetDescriptor<>("img/enemy_snake_act3.png", Texture.class);

    // Boss combat spritesheet (1376x768, 2 rows of 4 frames @ 344x384)
    public static final AssetDescriptor<Texture> BOSS_DAWN_WARDEN =
        new AssetDescriptor<>("img/boss_dawn_warden.png", Texture.class);

    // Pale Shepherd pursuer spritesheet (1376x768, 2 rows of 4 frames @ 344x384)
    public static final AssetDescriptor<Texture> CHAR_PALE_SHEPHERD =
        new AssetDescriptor<>("img/char_pale_shepherd.png", Texture.class);

    // Ending Story Epilogue Illustrations (16:9 1920x1080 / 1280x720)
    public static final AssetDescriptor<Texture> ENDING_DAWN_SUMMIT =
        new AssetDescriptor<>("img/ending_dawn_summit.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_CAT_REST =
        new AssetDescriptor<>("img/ending_cat_rest.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_TURTLE_REST =
        new AssetDescriptor<>("img/ending_turtle_rest.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_FOX_REST =
        new AssetDescriptor<>("img/ending_fox_rest.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_RACCOON_REST =
        new AssetDescriptor<>("img/ending_raccoon_rest.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_FELLOWSHIP_REST =
        new AssetDescriptor<>("img/ending_fellowship_rest.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_GUARDIANS_FAREWELL =
        new AssetDescriptor<>("img/ending_guardians_farewell.png", Texture.class);

    // Victory Epilogue Illustrations (16:9 1920x1080 / 1280x720)
    public static final AssetDescriptor<Texture> ENDING_DAWN_VICTORY =
        new AssetDescriptor<>("img/ending_dawn_victory.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_WARDEN_CONCESSION =
        new AssetDescriptor<>("img/ending_warden_concession.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_CAT_VICTORY =
        new AssetDescriptor<>("img/ending_cat_victory.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_TURTLE_VICTORY =
        new AssetDescriptor<>("img/ending_turtle_victory.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_FOX_VICTORY =
        new AssetDescriptor<>("img/ending_fox_victory.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_RACCOON_VICTORY =
        new AssetDescriptor<>("img/ending_raccoon_victory.png", Texture.class);
    public static final AssetDescriptor<Texture> ENDING_FELLOWSHIP_VICTORY =
        new AssetDescriptor<>("img/ending_fellowship_victory.png", Texture.class);

    // =========================================================================
    // In-Game Board: Parallax Act Backgrounds
    // =========================================================================
    public static final AssetDescriptor<Texture> ACT1_BG =
        new AssetDescriptor<>("img/act1_green.png", Texture.class);
    public static final AssetDescriptor<Texture> ACT2_BG =
        new AssetDescriptor<>("img/act2_blue.png", Texture.class);
    public static final AssetDescriptor<Texture> ACT3_BG =
        new AssetDescriptor<>("img/act3_red.png", Texture.class);

    // Option C: Grounded Stepping Stone Tiles (180x60)
    public static final AssetDescriptor<Texture> TILE_STONE_NORMAL =
        new AssetDescriptor<>("img/tile_stone_normal.png", Texture.class);
    public static final AssetDescriptor<Texture> TILE_STONE_SNAKE =
        new AssetDescriptor<>("img/tile_stone_snake.png", Texture.class);
    public static final AssetDescriptor<Texture> TILE_STONE_LADDER =
        new AssetDescriptor<>("img/tile_stone_ladder.png", Texture.class);

    // In-Game HUD Visuals
    public static final AssetDescriptor<Texture> ICON_DICE_D6 =
        new AssetDescriptor<>("img/icon_dice_d6.png", Texture.class);

    // Dice Faces (dice_1.png through dice_6.png)
    public static final AssetDescriptor<Texture> DICE_1 =
        new AssetDescriptor<>("img/dice_1.png", Texture.class);
    public static final AssetDescriptor<Texture> DICE_2 =
        new AssetDescriptor<>("img/dice_2.png", Texture.class);
    public static final AssetDescriptor<Texture> DICE_3 =
        new AssetDescriptor<>("img/dice_3.png", Texture.class);
    public static final AssetDescriptor<Texture> DICE_4 =
        new AssetDescriptor<>("img/dice_4.png", Texture.class);
    public static final AssetDescriptor<Texture> DICE_5 =
        new AssetDescriptor<>("img/dice_5.png", Texture.class);
    public static final AssetDescriptor<Texture> DICE_6 =
        new AssetDescriptor<>("img/dice_6.png", Texture.class);

    public static final AssetDescriptor<Texture> ICON_SATCHEL =
        new AssetDescriptor<>("img/icon_bag.png", Texture.class);
    public static final AssetDescriptor<Texture> ICON_HEART =
        new AssetDescriptor<>("img/icon_heart.png", Texture.class);
    public static final AssetDescriptor<Texture> ICON_SHIELD =
        new AssetDescriptor<>("img/icon_shield.png", Texture.class);

    // Item Icons (64x64)
    public static final AssetDescriptor<Texture> ITEM_POTION =
        new AssetDescriptor<>("img/item_potion.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_SWORD =
        new AssetDescriptor<>("img/item_sword.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_SHIELD =
        new AssetDescriptor<>("img/item_shield.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_FEATHER =
        new AssetDescriptor<>("img/item_feather.png", Texture.class);

    // Rich Pixel-Art RPG Item Assets (64x64 sliced)
    public static final AssetDescriptor<Texture> ITEM_SWORD_RELIC =
        new AssetDescriptor<>("img/item_sword_relic.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_DAGGER =
        new AssetDescriptor<>("img/item_dagger.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_SWORD_DRAGON =
        new AssetDescriptor<>("img/item_sword_dragon.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_BUCKLER =
        new AssetDescriptor<>("img/item_buckler.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_SHIELD_IRON =
        new AssetDescriptor<>("img/item_shield_iron.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_AEGIS =
        new AssetDescriptor<>("img/item_aegis.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_BLOOD_DRAUGHT =
        new AssetDescriptor<>("img/item_blood_draught.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_ELIXIR =
        new AssetDescriptor<>("img/item_elixir.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_AMBROSIA =
        new AssetDescriptor<>("img/item_ambrosia.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_FANG =
        new AssetDescriptor<>("img/item_fang.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_CURSE_TOTEM =
        new AssetDescriptor<>("img/item_curse_totem.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_BLOOD_SCROLL =
        new AssetDescriptor<>("img/item_blood_scroll.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_SCROLL =
        new AssetDescriptor<>("img/item_scroll.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_CURSE_SLAB =
        new AssetDescriptor<>("img/item_curse_slab.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_ORB_RING =
        new AssetDescriptor<>("img/item_orb_ring.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_LEAD_BOOTS =
        new AssetDescriptor<>("img/item_lead_boots.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_SWIFT_BOOTS =
        new AssetDescriptor<>("img/item_swift_boots.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_SHACKLES =
        new AssetDescriptor<>("img/item_shackles.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_TORCH =
        new AssetDescriptor<>("img/item_torch.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_IDOL =
        new AssetDescriptor<>("img/item_idol.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_FLUTE =
        new AssetDescriptor<>("img/item_flute.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_HOOK =
        new AssetDescriptor<>("img/item_hook.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_SNAKE_DAGGER =
        new AssetDescriptor<>("img/item_snake_dagger.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_FLAME_SWORD =
        new AssetDescriptor<>("img/item_flame_sword.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_DARK_BLADE =
        new AssetDescriptor<>("img/item_dark_blade.png", Texture.class);

    // Background video
    public static final String MENU_VIDEO = "video/menu_bg.webm";

    /**
     * Helper to retrieve the serpent spritesheet for a specific Act (1 = Emerald, 2 = Sapphire, 3 = Crimson).
     */
    public static AssetDescriptor<Texture> getSnakeTextureForAct(int act) {
        switch (act) {
            case 2: return ENEMY_SNAKE_ACT2;
            case 3: return ENEMY_SNAKE_ACT3;
            case 1:
            default: return ENEMY_SNAKE_ACT1;
        }
    }

    /**
     * Helper to retrieve the serpent spritesheet based on board tile position.
     * Act I: 1-30, Act II: 31-70, Act III: 71-100.
     */
    public static AssetDescriptor<Texture> getSnakeTextureForPosition(int position) {
        int act = (position <= 30) ? 1 : ((position <= 70) ? 2 : 3);
        return getSnakeTextureForAct(act);
    }

    /**
     * Helper to retrieve the texture descriptor for any CharacterClass enum.
     */
    public static AssetDescriptor<Texture> getCharacterTexture(io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass characterClass) {
        if (characterClass == null) return CHAR_CAT;
        switch (characterClass) {
            case CAT: return CHAR_CAT;
            case TURTLE: return CHAR_TURTLE;
            case FOX: return CHAR_FOX;
            case RACCOON: return CHAR_RACCOON;
            default: return CHAR_CAT;
        }
    }

    /**
     * Helper to retrieve the head icon descriptor for any CharacterClass enum.
     */
    public static AssetDescriptor<Texture> getCharacterHeadTexture(io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass characterClass) {
        if (characterClass == null) return CHAR_CAT_HEAD;
        switch (characterClass) {
            case CAT: return CHAR_CAT_HEAD;
            case TURTLE: return CHAR_TURTLE_HEAD;
            case FOX: return CHAR_FOX_HEAD;
            case RACCOON: return CHAR_RACCOON_HEAD;
            default: return CHAR_CAT_HEAD;
        }
    }

    /**
     * Helper to retrieve the animated walking spritesheet descriptor for any CharacterClass enum.
     */
    public static AssetDescriptor<Texture> getCharacterWalkSheet(io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass characterClass) {
        if (characterClass == null) return CHAR_CAT_WALK;
        switch (characterClass) {
            case CAT: return CHAR_CAT_WALK;
            case TURTLE: return CHAR_TURTLE_WALK;
            case FOX: return CHAR_FOX_WALK;
            case RACCOON: return CHAR_RACCOON_WALK;
            default: return CHAR_CAT_WALK;
        }
    }

    /**
     * Number of frames in each character's walking cycle spritesheet.
     */
    public static int getCharacterWalkFrameCount(io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass characterClass) {
        if (characterClass == null) return 4;
        switch (characterClass) {
            case CAT: return 3;
            case TURTLE: return 4;
            case FOX: return 6;
            case RACCOON: return 6;
            default: return 4;
        }
    }

    /**
     * Helper to retrieve the texture descriptor for a dice face value (1-6).
     */
    public static AssetDescriptor<Texture> getDiceTexture(int faceValue) {
        switch (faceValue) {
            case 1: return DICE_1;
            case 2: return DICE_2;
            case 3: return DICE_3;
            case 4: return DICE_4;
            case 5: return DICE_5;
            case 6: return DICE_6;
            default: return DICE_6;
        }
    }

    /**
     * Resolves a tailored pixel-art icon for an Item using its specific identity and effect type.
     */
    public static AssetDescriptor<Texture> getItemIcon(io.github.snakes_and_ladders_roll_of_fate.items.Item item) {
        if (item == null) return ITEM_POTION;
        String name = (item.getName() != null) ? item.getName().toLowerCase() : "";

        // Specific Curses
        if (name.contains("obsidian") || name.contains("slab")) return ITEM_CURSE_SLAB;
        if (name.contains("hex") || name.contains("totem")) return ITEM_CURSE_TOTEM;
        if (name.contains("shackle")) return ITEM_SHACKLES;

        // Specific Double-Edged & Special Relics
        if (name.contains("blood") && name.contains("draught")) return ITEM_BLOOD_DRAUGHT;
        if (name.contains("blood") || name.contains("pact")) return ITEM_BLOOD_SCROLL;
        if (name.contains("idol") || name.contains("greed")) return ITEM_IDOL;
        if (name.contains("scroll") || name.contains("wisdom") || name.contains("tome")) return ITEM_SCROLL;
        if (name.contains("flute") || name.contains("charmer")) return ITEM_FLUTE;
        if (name.contains("hook") || name.contains("vault")) return ITEM_HOOK;
        if (name.contains("lead") && name.contains("boot")) return ITEM_LEAD_BOOTS;
        if (name.contains("agility") || name.contains("swift")) return ITEM_SWIFT_BOOTS;
        if (name.contains("pathfinder") || name.contains("torch") || name.contains("flare")) return ITEM_TORCH;
        if (name.contains("transposition") || name.contains("orb")) return ITEM_ORB_RING;
        if (name.contains("golden die") || name.contains("loaded")) return ICON_DICE_D6;

        // Specific Weapons & Shields
        if (name.contains("fang")) return ITEM_FANG;
        if (name.contains("dagger")) return ITEM_DAGGER;
        if (name.contains("dragon")) return ITEM_FLAME_SWORD;
        if (name.contains("buckler")) return ITEM_BUCKLER;
        if (name.contains("aegis")) return ITEM_AEGIS;
        if (name.contains("shield") || name.contains("ward")) return ITEM_SHIELD_IRON;
        if (name.contains("ambrosia")) return ITEM_AMBROSIA;
        if (name.contains("elixir")) return ITEM_ELIXIR;
        if (name.contains("first aid") || name.contains("potion")) return ITEM_POTION;
        if (name.contains("blade") || name.contains("sword") || name.contains("edge") || name.contains("talon")) return ITEM_SWORD_RELIC;

        if (item.isCurse()) return ITEM_CURSE_TOTEM;
        return getItemIcon(item.getEffectType());
    }

    /**
     * Helper to retrieve an icon for an item based on its effect type.
     */
    public static AssetDescriptor<Texture> getItemIcon(io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType effectType) {
        if (effectType == null) return ITEM_POTION;
        switch (effectType) {
            case HEALTH: return ITEM_ELIXIR;
            case ATTACK: return ITEM_SWORD_RELIC;
            case DEFENSE: return ITEM_SHIELD_IRON;
            case EXPERIENCE: return ITEM_SCROLL;
            case MOVE: return ITEM_HOOK;
            case SCOUT: return ITEM_TORCH;
            case DICE_EXACT: return ICON_DICE_D6;
            case DICE_CLAMP: return ITEM_LEAD_BOOTS;
            case SNAKE_WARD: return ITEM_FLUTE;
            case SWAP_PLAYER: return ITEM_ORB_RING;
            default: return ITEM_POTION;
        }
    }

    /**
     * Loads everything we need up front.
     */
    public static void loadAll(AssetManager manager) {
        manager.load(UI_SKIN);
        manager.load(TITLE);
        manager.load(BUTTON_TEX);
        manager.load(SCROLL);
        manager.load(ICON_SOUND);
        manager.load(ICON_MUSIC);
        manager.load(ICON_SINGLE);
        manager.load(ICON_MULTI);
        manager.load(CHAR_CAT);
        manager.load(CHAR_TURTLE);
        manager.load(CHAR_FOX);
        manager.load(CHAR_RACCOON);
        manager.load(CHAR_CAT_HEAD);
        manager.load(CHAR_TURTLE_HEAD);
        manager.load(CHAR_FOX_HEAD);
        manager.load(CHAR_RACCOON_HEAD);
        manager.load(CHAR_CAT_WALK);
        manager.load(CHAR_TURTLE_WALK);
        manager.load(CHAR_FOX_WALK);
        manager.load(CHAR_RACCOON_WALK);
        manager.load(LOADING_SNAKE);
        manager.load(ENEMY_SNAKE);
        manager.load(ENEMY_SNAKE_ACT1);
        manager.load(ENEMY_SNAKE_ACT2);
        manager.load(ENEMY_SNAKE_ACT3);
        manager.load(BOSS_DAWN_WARDEN);
        manager.load(CHAR_PALE_SHEPHERD);
        manager.load(CHAR_PALE_SHEPHERD_HEAD);
        manager.load(ACT1_BG);
        manager.load(ACT2_BG);
        manager.load(ACT3_BG);
        manager.load(TILE_STONE_NORMAL);
        manager.load(TILE_STONE_SNAKE);
        manager.load(TILE_STONE_LADDER);
        manager.load(ICON_DICE_D6);
        manager.load(DICE_1);
        manager.load(DICE_2);
        manager.load(DICE_3);
        manager.load(DICE_4);
        manager.load(DICE_5);
        manager.load(DICE_6);
        manager.load(ICON_SATCHEL);
        manager.load(ICON_HEART);
        manager.load(ICON_SHIELD);
        manager.load(ITEM_POTION);
        manager.load(ITEM_SWORD);
        manager.load(ITEM_SHIELD);
        manager.load(ITEM_FEATHER);
        manager.load(ITEM_SWORD_RELIC);
        manager.load(ITEM_DAGGER);
        manager.load(ITEM_SWORD_DRAGON);
        manager.load(ITEM_BUCKLER);
        manager.load(ITEM_SHIELD_IRON);
        manager.load(ITEM_AEGIS);
        manager.load(ITEM_BLOOD_DRAUGHT);
        manager.load(ITEM_ELIXIR);
        manager.load(ITEM_AMBROSIA);
        manager.load(ITEM_FANG);
        manager.load(ITEM_CURSE_TOTEM);
        manager.load(ITEM_BLOOD_SCROLL);
        manager.load(ITEM_SCROLL);
        manager.load(ITEM_CURSE_SLAB);
        manager.load(ITEM_ORB_RING);
        manager.load(ITEM_LEAD_BOOTS);
        manager.load(ITEM_SWIFT_BOOTS);
        manager.load(ITEM_SHACKLES);
        manager.load(ITEM_TORCH);
        manager.load(ITEM_IDOL);
        manager.load(ITEM_FLUTE);
        manager.load(ITEM_HOOK);
        manager.load(ITEM_SNAKE_DAGGER);
        manager.load(ITEM_FLAME_SWORD);
        manager.load(ITEM_DARK_BLADE);
        manager.load(ENDING_DAWN_SUMMIT);
        manager.load(ENDING_CAT_REST);
        manager.load(ENDING_TURTLE_REST);
        manager.load(ENDING_FOX_REST);
        manager.load(ENDING_RACCOON_REST);
        manager.load(ENDING_FELLOWSHIP_REST);
        manager.load(ENDING_GUARDIANS_FAREWELL);
        manager.load(ENDING_DAWN_VICTORY);
        manager.load(ENDING_WARDEN_CONCESSION);
        manager.load(ENDING_CAT_VICTORY);
        manager.load(ENDING_TURTLE_VICTORY);
        manager.load(ENDING_FOX_VICTORY);
        manager.load(ENDING_RACCOON_VICTORY);
        manager.load(ENDING_FELLOWSHIP_VICTORY);
    }
}
