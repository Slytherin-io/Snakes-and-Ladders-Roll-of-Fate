package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.systems.Dice;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

public class BoardScreen implements Screen {
    private RollOfFateGame game;
    private Board board;
    private Table diceUI;
    private Image playerSprite;

    private Dice movementDice = new Dice(6);

    public BoardScreen(RollOfFateGame game) {
        this.game = game;
    }

    public void rollForMovement(Player player) {
        int steps = movementDice.roll();
        player.move(steps);
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
