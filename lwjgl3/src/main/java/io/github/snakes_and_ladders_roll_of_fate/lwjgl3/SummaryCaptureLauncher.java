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
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.screens.GameSummaryScreen;
import io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/**
 * Headless/Windowed automated visual capture tool for GameSummaryScreen.
 * Generates screenshots of both Multiplayer Victory, Multiplayer Defeat, and Single Player Victory.
 */
public class SummaryCaptureLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Capture Summary");
        config.setWindowedMode(1920, 1080);
        config.setDecorated(false);
        config.useVsync(false);

        new Lwjgl3Application(new CaptureGame(), config);
    }

    private static class CaptureGame extends RollOfFateGame {
        private int frameCount = 0;
        private int capturePhase = 0;

        @Override
        public void create() {
            AssetManager am = new AssetManager();
            Assets.loadAll(am);
            am.finishLoading();

            // Set reflection or getter
            try {
                java.lang.reflect.Field field = RollOfFateGame.class.getDeclaredField("assetManager");
                field.setAccessible(true);
                field.set(this, am);
            } catch (Exception e) {
                e.printStackTrace();
            }

            setupMultiplayerSession();
        }

        private void setupMultiplayerSession() {
            List<Player> mockParty = new ArrayList<>();
            Player p1 = new Player("Player 1", CharacterClass.CAT);
            p1.setHealth(95);
            p1.setPosition(100);

            Player p2 = new Player("Player 2", CharacterClass.FOX);
            p2.setHealth(70);
            p2.setPosition(84);

            Player p3 = new Player("Player 3", CharacterClass.TURTLE);
            p3.setHealth(100);
            p3.setPosition(68);

            Player p4 = new Player("Player 4", CharacterClass.RACCOON);
            p4.setHealth(82);
            p4.setPosition(75);

            mockParty.add(p1);
            mockParty.add(p2);
            mockParty.add(p3);
            mockParty.add(p4);

            // Populate runTracker with rich feats
            RunTracker tracker = getRunTracker();
            tracker.recordTurn(p1); tracker.recordTurn(p1); tracker.recordTurn(p1);
            tracker.recordRoll(p1, 6); tracker.recordRoll(p1, 4);
            tracker.recordSteps(p1, 10, 100);
            tracker.recordLadder(p1, 20, 48);
            tracker.recordSnakeCombat(p1, 35, true, true);
            tracker.recordItemUsed(p1);
            tracker.recordThroneClaimed(p1);

            tracker.recordTurn(p2); tracker.recordTurn(p2);
            tracker.recordSteps(p2, 84, 84);
            tracker.recordSnakeEncounter(p2);
            tracker.recordSnakeCombat(p2, 42, true, false);
            tracker.recordDamageTaken(p2, 20);

            tracker.recordTurn(p3); tracker.recordTurn(p3);
            tracker.recordSteps(p3, 68, 68);
            tracker.recordLadder(p3, 10, 35);
            tracker.recordLadder(p3, 40, 68);
            tracker.recordDamageTaken(p3, 30);

            tracker.recordTurn(p4); tracker.recordTurn(p4);
            tracker.recordSteps(p4, 75, 75);
            tracker.recordItemCollected(p4);
            tracker.recordItemCollected(p4);
            tracker.recordItemUsed(p4);
            tracker.recordEventResolved(p4);
            tracker.recordRoll(p4, 6); tracker.recordRoll(p4, 6);

            try {
                java.lang.reflect.Field pField = RollOfFateGame.class.getDeclaredField("players");
                pField.setAccessible(true);
                pField.set(this, mockParty);
            } catch (Exception e) {
                e.printStackTrace();
            }

            setScreen(new GameSummaryScreen(this, true, p1));
        }

        @Override
        public void render() {
            super.render();
            frameCount++;

            // Wait 5 frames for rendering to stabilize
            if (capturePhase == 0 && frameCount >= 5) {
                capturePhase = 1;
                saveScreenshot("screenshots/victory_multiplayer_summary.png");

                // Switch to Defeat screen
                Player heroDefeated = getPlayers().get(0);
                heroDefeated.setHealth(0);
                setScreen(new GameSummaryScreen(this, false, heroDefeated));
                frameCount = 0;
            } else if (capturePhase == 1 && frameCount >= 5) {
                capturePhase = 2;
                saveScreenshot("screenshots/defeat_multiplayer_summary.png");

                // Switch to Single Player Victory
                Player soloPlayer = getPlayers().get(0);
                soloPlayer.setHealth(95);
                soloPlayer.setPosition(100);
                List<Player> soloList = new ArrayList<>();
                soloList.add(soloPlayer);
                try {
                    java.lang.reflect.Field pField = RollOfFateGame.class.getDeclaredField("players");
                    pField.setAccessible(true);
                    pField.set(this, soloList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                setScreen(new GameSummaryScreen(this, true, soloList.get(0)));
                frameCount = 0;
            } else if (capturePhase == 2 && frameCount >= 5) {
                capturePhase = 3;
                saveScreenshot("screenshots/victory_singleplayer_summary.png");
                Gdx.app.exit();
            }
        }

        private void saveScreenshot(String path) {
            Pixmap pixmap = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            int w = pixmap.getWidth();
            int h = pixmap.getHeight();
            Pixmap flipped = new Pixmap(w, h, pixmap.getFormat());
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    flipped.drawPixel(x, h - 1 - y, pixmap.getPixel(x, y));
                }
            }
            pixmap.dispose();

            Gdx.files.local("screenshots").mkdirs();
            PixmapIO.writePNG(Gdx.files.local(path), flipped);
            flipped.dispose();
            System.out.println("CAPTURE_SUCCESS: " + path);
        }
    }
}
