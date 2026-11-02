package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;

public class BattleScreen implements Screen {
    private RollOfFateGame game;
    private BattleSystem battleSystem;
    private TextButton attackBtn;
    private Image enemySprite;

    public BattleScreen(RollOfFateGame game) {
        this.game = game;
    }

    public void updateHealthUI() {
        // Will implement later
    }

    public void executeTurn() {
        // Will implement later
    }

    @Override
    public void show() {
        // Prepare screen here.
    }

    @Override
    public void render(float delta) {
        // Draw your here. "delta" is the time since last render in seconds.
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when application is paused.
    }

    @Override
    public void resume() {
        // Invoked when application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}
