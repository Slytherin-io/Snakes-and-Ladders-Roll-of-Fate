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
 * Headless/Windowed capture tool to preview and verify real-time mini-map progress monitoring.
 */
public class BoardMiniMapCaptureLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Capture Board HUD and Skills");
        config.setWindowedMode(1920, 1080);
        config.setDecorated(false);
        config.useVsync(false);

        new Lwjgl3Application(new CaptureGameBoard(), config);
    }

    private static class CaptureGameBoard extends RollOfFateGame {
        private int frameCount = 0;
        private int capturePhase = 0;
        private BoardScreen boardScreen;
        private List<Player> mockParty;

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

            // Setup Board
            try {
                Board b = new Board(100);
                b.generateBoard();
                java.lang.reflect.Field bField = RollOfFateGame.class.getDeclaredField("board");
                bField.setAccessible(true);
                bField.set(this, b);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Setup 4-player party
            mockParty = new ArrayList<>();
            Player p1 = new Player("Player 1", CharacterClass.CAT);
            p1.setPosition(94); // Exactly matching Act III Tile 94
            p1.setHealth(95);

            Player p2 = new Player("Player 2", CharacterClass.FOX);
            p2.setPosition(52); // Act II
            p2.setHealth(80);

            Player p3 = new Player("Player 3", CharacterClass.TURTLE);
            p3.setPosition(25); // Act I
            p3.setHealth(100);

            Player p4 = new Player("Player 4", CharacterClass.RACCOON);
            p4.setPosition(10); // Act I
            p4.setHealth(88);

            mockParty.add(p1);
            mockParty.add(p2);
            mockParty.add(p3);
            mockParty.add(p4);

            try {
                java.lang.reflect.Field pField = RollOfFateGame.class.getDeclaredField("players");
                pField.setAccessible(true);
                pField.set(this, mockParty);

                java.lang.reflect.Field tField = RollOfFateGame.class.getDeclaredField("currentTurn");
                tField.setAccessible(true);
                tField.set(this, 0); // Player 1 active turn

                setPaleShepherdTile(5); // Awakened Pale Shepherd stalking on Tile 5
            } catch (Exception e) {
                e.printStackTrace();
            }

            boardScreen = new BoardScreen(this);
            setScreen(boardScreen);
        }

        @Override
        public void render() {
            super.render();
            frameCount++;

            if (frameCount < 5) return; // Allow frames to render

            switch (capturePhase) {
                case 0:
                    // Cat (P1) Inactive
                    saveScreenshot("screenshots/board_hud_skill_cat_inactive.png", new int[]{290, 930, 200, 130});
                    // Make Ready
                    mockParty.get(0).setSkillCooldown(0);
                    updateDisplay();
                    capturePhase = 1;
                    frameCount = 0;
                    break;
                case 1:
                    // Cat (P1) Ready
                    saveScreenshot("screenshots/board_hud_skill_cat_ready.png", new int[]{290, 930, 200, 130});
                    // Switch to Fox (P2)
                    setTurn(1);
                    capturePhase = 2;
                    frameCount = 0;
                    break;
                case 2:
                    // Fox (P2) Inactive
                    saveScreenshot("screenshots/board_hud_skill_fox_inactive.png", new int[]{290, 930, 200, 130});
                    mockParty.get(1).setSkillCooldown(0);
                    updateDisplay();
                    capturePhase = 3;
                    frameCount = 0;
                    break;
                case 3:
                    // Fox (P2) Ready
                    saveScreenshot("screenshots/board_hud_skill_fox_ready.png", new int[]{290, 930, 200, 130});
                    // Switch to Turtle (P3)
                    setTurn(2);
                    capturePhase = 4;
                    frameCount = 0;
                    break;
                case 4:
                    // Turtle (P3) Inactive
                    saveScreenshot("screenshots/board_hud_skill_turtle_inactive.png", new int[]{290, 930, 200, 130});
                    mockParty.get(2).setSkillCooldown(0);
                    updateDisplay();
                    capturePhase = 5;
                    frameCount = 0;
                    break;
                case 5:
                    // Turtle (P3) Ready
                    saveScreenshot("screenshots/board_hud_skill_turtle_ready.png", new int[]{290, 930, 200, 130});
                    // Switch to Raccoon (P4)
                    setTurn(3);
                    capturePhase = 6;
                    frameCount = 0;
                    break;
                case 6:
                    // Raccoon (P4) Inactive
                    saveScreenshot("screenshots/board_hud_skill_raccoon_inactive.png", new int[]{290, 930, 200, 130});
                    mockParty.get(3).setSkillCooldown(0);
                    updateDisplay();
                    capturePhase = 7;
                    frameCount = 0;
                    break;
                case 7:
                    // Raccoon (P4) Ready
                    saveScreenshot("screenshots/board_hud_skill_raccoon_ready.png", new int[]{290, 930, 200, 130});
                    // Final full captures
                    saveScreenshot("screenshots/board_hud_bottom_ready.png", new int[]{20, 915, 1880, 160});
                    saveScreenshot("screenshots/board_hud_skill_full.png", null);
                    Gdx.app.exit();
                    break;
            }
        }

        private void setTurn(int turn) {
            try {
                java.lang.reflect.Field tField = RollOfFateGame.class.getDeclaredField("currentTurn");
                tField.setAccessible(true);
                tField.set(this, turn);
                updateDisplay();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void updateDisplay() {
            try {
                java.lang.reflect.Method m = BoardScreen.class.getDeclaredMethod("updateHudDisplay");
                m.setAccessible(true);
                m.invoke(boardScreen);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void saveScreenshot(String path, int[] crop) {
            Pixmap full = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            int w = full.getWidth();
            int h = full.getHeight();

            // Flip Y for standard LibGDX framebuffers
            Pixmap flipped = new Pixmap(w, h, full.getFormat());
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    flipped.drawPixel(x, h - 1 - y, full.getPixel(x, y));
                }
            }
            full.dispose();

            Pixmap toSave;
            if (crop != null) {
                int cx = crop[0];
                int cy = crop[1];
                int cw = Math.min(crop[2], w - cx);
                int ch = Math.min(crop[3], h - cy);
                toSave = new Pixmap(cw, ch, flipped.getFormat());
                toSave.drawPixmap(flipped, 0, 0, cx, cy, cw, ch);
                flipped.dispose();
            } else {
                toSave = flipped;
            }

            Gdx.files.local("screenshots").mkdirs();
            PixmapIO.writePNG(Gdx.files.local(path), toSave);
            toSave.dispose();
            System.out.println("CAPTURE_SUCCESS: " + path);
        }
    }
}
