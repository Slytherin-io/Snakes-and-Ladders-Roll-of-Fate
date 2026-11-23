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

    // Item Placeholder Icons (64x64)
    public static final AssetDescriptor<Texture> ITEM_POTION =
        new AssetDescriptor<>("img/item_potion.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_SWORD =
        new AssetDescriptor<>("img/item_sword.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_SHIELD =
        new AssetDescriptor<>("img/item_shield.png", Texture.class);
    public static final AssetDescriptor<Texture> ITEM_FEATHER =
        new AssetDescriptor<>("img/item_feather.png", Texture.class);

    // Background video
    public static final String MENU_VIDEO = "video/menu_bg.webm";

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
     * Helper to retrieve appropriate placeholder icon for an item based on its effect type.
     */
    public static AssetDescriptor<Texture> getItemIcon(io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType effectType) {
        if (effectType == null) return ITEM_POTION;
        switch (effectType) {
            case HEALTH: return ITEM_POTION;
            case ATTACK: return ITEM_SWORD;
            case DEFENSE: return ITEM_SHIELD;
            case MOVE: return ITEM_FEATHER;
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
        manager.load(CHAR_CAT_WALK);
        manager.load(CHAR_TURTLE_WALK);
        manager.load(CHAR_FOX_WALK);
        manager.load(CHAR_RACCOON_WALK);
        manager.load(LOADING_SNAKE);
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
    }
}
