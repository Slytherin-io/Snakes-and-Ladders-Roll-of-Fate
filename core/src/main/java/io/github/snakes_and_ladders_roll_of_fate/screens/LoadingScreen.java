package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;
import io.github.snakes_and_ladders_roll_of_fate.ui.UIUtils;

/**
 * Animated Loading Screen shown between character selection and the board game.
 * Features the 36-frame pixel snake spritesheet loop, animated status dots, and gameplay lore tips.
 */
public class LoadingScreen implements Screen {
    private final RollOfFateGame game;
    private final Runnable onFinished;

    private Stage stage;
    private Animation<TextureRegion> snakeAnimation;
    private float stateTime = 0f;

    private BitmapFont titleFont;
    private BitmapFont tipFont;
    private Label statusLabel;
    private Label tipLabel;

    // Minimum display duration (approx 1.5 complete loops of the 1.44s animation)
    private final float minDuration = 2.2f;
    private boolean transitionStarted = false;

    // Flavorful game tips to cycle through
    private static final String[] GAMEPLAY_TIPS = {
        "Ladders grant blessings, but snakes demand a bitter toll.",
        "Watch your step: treacherous trap tiles lurk in the shadows.",
        "Consumables can turn the tide of battle in mortal peril.",
        "Roll of Fate: Every turn brings you closer to glory or doom.",
        "Equip your starting gear wisely to absorb hazard damage."
    };

    public LoadingScreen(RollOfFateGame game, Runnable onFinished) {
        this.game = game;
        this.onFinished = onFinished;
    }

    @Override
    public void show() {
        if (game.getAudioManager() != null) {
            game.getAudioManager().stopMusic();
        }
        stage = new Stage(new ExtendViewport(1920, 1080));

        // Create typography using the Sniglet font
        titleFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 30, Color.valueOf("#f4e4c1"));
        tipFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 20, Color.valueOf("#c9935a"));

        // Load and split the 6x6 snake spritesheet (576x576 total, 96x96 per frame)
        Texture snakeSheet = game.getAssetManager().get(Assets.LOADING_SNAKE);
        TextureRegion[][] grid = TextureRegion.split(snakeSheet, 96, 96);
        TextureRegion[] frames = new TextureRegion[36];
        int idx = 0;
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 6; c++) {
                frames[idx++] = grid[r][c];
            }
        }

        // 36 frames at 0.04s = ~1.44s for a full loop
        snakeAnimation = new Animation<>(0.04f, frames);
        snakeAnimation.setPlayMode(Animation.PlayMode.LOOP);

        // Custom Actor to render the animated snake without allocating garbage per frame
        Actor snakeActor = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                TextureRegion frame = snakeAnimation.getKeyFrame(stateTime, true);
                Color c = getColor();
                batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
                batch.draw(frame, getX(), getY(), getWidth(), getHeight());
            }
        };

        // Status text with animated dots
        statusLabel = new Label("PREPARING BOARD", new Label.LabelStyle(titleFont, Color.valueOf("#f4e4c1")));
        statusLabel.setAlignment(Align.center);

        // Random tip selected on load
        String randomTip = GAMEPLAY_TIPS[(int) (Math.random() * GAMEPLAY_TIPS.length)];
        tipLabel = new Label(randomTip, new Label.LabelStyle(tipFont, Color.valueOf("#c9935a")));
        tipLabel.setAlignment(Align.center);
        tipLabel.setWrap(true);

        // Assemble the centered layout: Snake -> Status -> Tip
        Table centerTable = new Table();
        centerTable.setFillParent(true);
        centerTable.add(snakeActor).size(192, 192).padBottom(24).center().row();
        centerTable.add(statusLabel).padBottom(12).center().row();
        centerTable.add(tipLabel).width(720).center().row();

        stage.addActor(centerTable);

        // Gentle fade-in entrance
        stage.getRoot().getColor().a = 0f;
        stage.getRoot().addAction(Actions.fadeIn(0.25f));
    }

    @Override
    public void render(float delta) {
        // Dark rich fantasy background
        Gdx.gl.glClearColor(0.09f, 0.06f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stateTime += delta;

        // Animate the dots: "PREPARING BOARD" -> "." -> ".." -> "..."
        int dots = (int) (stateTime * 2.5f) % 4;
        String dotStr = dots == 1 ? "." : (dots == 2 ? ".." : (dots == 3 ? "..." : ""));
        statusLabel.setText("PREPARING BOARD" + dotStr);

        // Once the minimum loop duration is complete, start a smooth fade-out and transition
        if (stateTime >= minDuration && !transitionStarted) {
            transitionStarted = true;
            stage.addAction(Actions.sequence(
                Actions.fadeOut(0.35f),
                Actions.run(() -> {
                    if (onFinished != null) {
                        onFinished.run();
                    }
                })
            ));
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (titleFont != null) titleFont.dispose();
        if (tipFont != null) tipFont.dispose();
    }
}
