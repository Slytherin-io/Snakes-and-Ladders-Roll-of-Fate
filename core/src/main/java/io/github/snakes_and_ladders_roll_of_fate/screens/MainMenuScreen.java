package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

import io.github.snakes_and_ladders_roll_of_fate.ui.UIUtils;
import org.w3c.dom.Text;

import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;
import com.badlogic.gdx.files.FileHandle;

public class MainMenuScreen implements Screen {
    private RollOfFateGame game;
    private VideoPlayer videoPlayer;
    private SpriteBatch batch;
    private Stage stage;

    private TextButton playButton;
    private TextButton exitButton;

    public MainMenuScreen(RollOfFateGame game) {
        this.game = game;
    }

    public void handleInput() {}

    @Override
    public void show() {
        batch = new SpriteBatch();
        videoPlayer = VideoPlayerCreator.createVideoPlayer();

        videoPlayer.setOnCompletionListener(new VideoPlayer.CompletionListener() {
            @Override
            public void onCompletionListener(FileHandle file) {
                try {
                    videoPlayer.play(file);
                } catch (Exception e) {
                    Gdx.app.error("Video", "Loop failed", e);
                }
            }
        });

        try {
            videoPlayer.play(Gdx.files.internal(Assets.MENU_VIDEO));
        } catch (Exception e) {
            Gdx.app.error("Video", "Error playing video", e);
        }

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Texture titleTexture = game.getAssetManager().get(Assets.TITLE);

        Image titleImage = new Image(titleTexture);

        Skin skin = game.getAssetManager().get(Assets.UI_SKIN);

        TextButton playButton = new TextButton("Play Game", skin);
        TextButton settingsButton = new TextButton("Settings", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        playButton.getLabel().setFontScale(2.5f);
        settingsButton.getLabel().setFontScale(2.5f);
        exitButton.getLabel().setFontScale(2.5f);

        playButton.setColor(Color.valueOf("#a8642cCC"));
        settingsButton.setColor(Color.valueOf("#a8642cCC"));
        exitButton.setColor(Color.valueOf("#a8642cCC"));

        playButton.getLabel().setAlignment(Align.center);
        settingsButton.getLabel().setAlignment(Align.center);
        exitButton.getLabel().setAlignment(Align.center);

        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // game.startGame(); // Starts the game!
            }
        });

        UIUtils.addScaleHoverEffect(playButton);
        UIUtils.addScaleHoverEffect(settingsButton);
        UIUtils.addScaleHoverEffect(exitButton);

        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // game.startGame(); // Starts the game
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit(); // Closes the application
            }
        });

        Table table = new Table();
        table.setFillParent(true);

        table.add(titleImage).size(800,400).padBottom(15).center().row();
        table.add(playButton).size(400, 80).padBottom(20).center().row();
        table.add(settingsButton).size(400,80).padBottom(20).center().row();
        table.add(exitButton).size(400,80).center().row();

        titleImage.setScaling(Scaling.fit);

        stage.addActor(table);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        videoPlayer.update();
        Texture frame = videoPlayer.getTexture();
        if (frame != null) {
            batch.begin();
            batch.draw(frame, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
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
        stage.dispose();
        if (batch != null) batch.dispose();
        if (videoPlayer != null) videoPlayer.dispose();
    }
}
