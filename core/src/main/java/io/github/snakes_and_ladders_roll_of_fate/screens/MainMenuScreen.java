package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;

public class MainMenuScreen implements Screen {
    private RollOfFateGame game;
    private TextButton playButton;
    private TextButton exitButton;

    public MainMenuScreen(RollOfFateGame game) {
        this.game = game;
    }

    public void handleInput() {}

    @Override
    public void show() {}

    @Override
    public void render(float delta) {}

    @Override
    public void resize(int width, int height) {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {}
}
