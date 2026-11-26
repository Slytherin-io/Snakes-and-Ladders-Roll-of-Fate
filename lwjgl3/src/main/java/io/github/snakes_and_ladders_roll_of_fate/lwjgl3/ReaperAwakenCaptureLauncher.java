package io.github.snakes_and_ladders_roll_of_fate.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.screens.BoardScreen;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/**
 * Capture launcher to preview and verify The Pale Shepherd awakening bulb burnout effect.
 */
public class ReaperAwakenCaptureLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Capture Reaper Awaken Burnout");
        config.setWindowedMode(1920, 1080);
        config.setDecorated(false);
        config.useVsync(false);

        new Lwjgl3Application(new CaptureGameBoard(), config);
    }

    private static class CaptureGameBoard extends RollOfFateGame {
        private float timeAccum = 0f;
        private BoardScreen boardScreen;
        private List<Player> mockParty;
        private boolean capturedFlicker = false;
        private boolean capturedBlackout = false;
        private boolean capturedReveal = false;
        private boolean capturedRestored = false;

        @Override
        public void create() {
            AssetManager am = new AssetManager();
            Assets.loadAll(am);
            am.finishLoading();

            try {
                java.lang.reflect.Field field = RollOfFateGame.class.getDeclaredField("assetManager");
                field.setAccessible(true);
                field.set(this, am);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Board b = new Board(100);
                b.generateBoard();
                java.lang.reflect.Field bField = RollOfFateGame.class.getDeclaredField("board");
                bField.setAccessible(true);
                bField.set(this, b);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mockParty = new ArrayList<>();
            Player p1 = new Player("Player 1", CharacterClass.CAT);
            p1.setPosition(8);
            mockParty.add(p1);

            try {
                java.lang.reflect.Field pField = RollOfFateGame.class.getDeclaredField("players");
                pField.setAccessible(true);
                pField.set(this, mockParty);

                java.lang.reflect.Field tField = RollOfFateGame.class.getDeclaredField("currentTurn");
                tField.setAccessible(true);
                tField.set(this, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            boardScreen = new BoardScreen(this);
            setScreen(boardScreen);

            // Trigger the awakening sequence directly
            boardScreen.triggerPaleShepherdAwakening();
        }

        @Override
        public void render() {
            super.render();
            float dt = Gdx.graphics.getDeltaTime();
            timeAccum += dt;

            // 1. Violent dying filament surge / flicker (~0.58s)
            if (!capturedFlicker && timeAccum >= 0.58f) {
                capturedFlicker = true;
                saveScreenshot("screenshots/reaper_burnout_flicker.png");
            }
            // 2. The POP & Total Pitch Blackout (~0.85s)
            else if (!capturedBlackout && timeAccum >= 0.85f) {
                capturedBlackout = true;
                saveScreenshot("screenshots/reaper_burnout_blackout.png");
            }
            // 3. Cold Spectral Vignette Reveal with reaper hovering on Tile 1 (~1.85s)
            else if (!capturedReveal && timeAccum >= 1.85f) {
                capturedReveal = true;
                saveScreenshot("screenshots/reaper_burnout_reveal.png");
            }
            // 4. Clean restoration back to sunlight (~3.20s)
            else if (!capturedRestored && timeAccum >= 3.20f) {
                capturedRestored = true;
                saveScreenshot("screenshots/reaper_burnout_restored.png");
                Gdx.app.exit();
            }
        }

        private void saveScreenshot(String path) {
            Pixmap full = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            int w = full.getWidth();
            int h = full.getHeight();

            Pixmap flipped = new Pixmap(w, h, full.getFormat());
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    flipped.drawPixel(x, h - 1 - y, full.getPixel(x, y));
                }
            }
            full.dispose();

            Gdx.files.local("screenshots").mkdirs();
            PixmapIO.writePNG(Gdx.files.local(path), flipped);
            flipped.dispose();
            System.out.println("CAPTURE_SUCCESS: " + path);
        }
    }
}
