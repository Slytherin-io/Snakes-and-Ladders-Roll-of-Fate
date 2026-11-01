package io.github.snakes_and_ladders_roll_of_fate;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;

import io.github.snakes_and_ladders_roll_of_fate.screens.MainMenuScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RollOfFateGame extends Game {
    private AssetManager assetManager;

    @Override
    public void create() {
        assetManager = new AssetManager();

        setScreen(new MainMenuScreen(this));
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public void dispose() {
        super.dispose();
        assetManager.dispose();
    }
}
