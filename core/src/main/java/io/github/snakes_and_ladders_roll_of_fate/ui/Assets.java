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

    // =========================================================================
    // Character sprites
    // PLACEHOLDER NOTE: When your real character pixel sprites are ready,
    // update the file paths below (e.g. "img/characters/cat.png")
    // =========================================================================
    public static final AssetDescriptor<Texture> CHAR_CAT =
        new AssetDescriptor<>("img/char_cat.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_TURTLE =
        new AssetDescriptor<>("img/char_turtle.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_FOX =
        new AssetDescriptor<>("img/char_fox.png", Texture.class);
    public static final AssetDescriptor<Texture> CHAR_RACCOON =
        new AssetDescriptor<>("img/char_raccoon.png", Texture.class);

    // Background video
    public static final String MENU_VIDEO = "video/menu_bg.webm";

    /**
     * Helper to retrieve the texture descriptor for any CharacterClass enum.
     */
    public static AssetDescriptor<Texture> getCharacterTexture(io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass characterClass) {
        switch (characterClass) {
            case CAT: return CHAR_CAT;
            case TURTLE: return CHAR_TURTLE;
            case FOX: return CHAR_FOX;
            case RACCOON: return CHAR_RACCOON;
            default: return CHAR_CAT;
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
    }
}
