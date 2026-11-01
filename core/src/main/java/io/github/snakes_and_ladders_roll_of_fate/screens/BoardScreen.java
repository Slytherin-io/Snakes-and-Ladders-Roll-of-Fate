package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.board.Board;

public class BoardScreen implements Screen {
    private RollOfFateGame game;
    private Board board;
    private Table diceUI;
    private Image playerSprite;

    public BoardScreen(RollOfFateGame game) {
        this.game = game;
    }

    public void updateCamera() {
        // Will implement later
    }

    public void triggerTile() {
        // Will implement later
    }

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
