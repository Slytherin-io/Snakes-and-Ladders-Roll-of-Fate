package io.github.snakes_and_ladders_roll_of_fate.ui;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {
    public static final AssetDescriptor<Skin> UI_SKIN =
        new AssetDescriptor<>("ui/uiskin.json", Skin.class);

    public static final AssetDescriptor<Texture> TITLE =
        new AssetDescriptor<>("img/menu_title.png", Texture.class);

    public static final AssetDescriptor<Texture> BUTTON_TEX =
        new AssetDescriptor<>("img/button_tex2.png", Texture.class);

    public static final String MENU_VIDEO = "video/menu_bg.webm";

    public static void loadAll(AssetManager manager) {
        manager.load(UI_SKIN);
        manager.load(TITLE);
        manager.load(BUTTON_TEX);
    }
}
