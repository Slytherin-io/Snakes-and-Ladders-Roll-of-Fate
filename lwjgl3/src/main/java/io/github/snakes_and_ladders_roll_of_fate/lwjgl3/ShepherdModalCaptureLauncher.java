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
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.screens.BoardScreen;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/**
 * Capture launcher to preview The Pale Shepherd encounter modal with standard choice card styling.
 */
public class ShepherdModalCaptureLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Capture Pale Shepherd Modal");
        config.setWindowedMode(1920, 1080);
        config.setDecorated(false);
        config.useVsync(false);

        new Lwjgl3Application(new CaptureGameBoard(), config);
    }

    private static class CaptureGameBoard extends RollOfFateGame {
        private int frameCount = 0;
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
            p1.setHealth(85);
            p1.setPosition(22);
            p1.getInventory().addItem(new Item("Minor Health Potion", 1, 1, "Restores 25 HP"));
            mockParty.add(p1);

            try {
                java.lang.reflect.Field pField = RollOfFateGame.class.getDeclaredField("players");
                pField.setAccessible(true);
                pField.set(this, mockParty);

                java.lang.reflect.Field tField = RollOfFateGame.class.getDeclaredField("currentTurn");
                tField.setAccessible(true);
                tField.set(this, 0);

                setPaleShepherdTile(22);
            } catch (Exception e) {
                e.printStackTrace();
            }

            boardScreen = new BoardScreen(this);
            setScreen(boardScreen);

            // Trigger gentle board dialogue with player and shepherd both at Tile 22
            try {
                java.lang.reflect.Method mDialogue = BoardScreen.class.getDeclaredMethod("showPaleShepherdBoardDialogue", Player.class);
                mDialogue.setAccessible(true);
                mDialogue.invoke(boardScreen, p1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void render() {
            super.render();
            frameCount++;

            if (frameCount == 15) {
                saveScreenshot("screenshots/shepherd_board_dialogue_beat1.png", null);
                try {
                    java.lang.reflect.Field fBeat = BoardScreen.class.getDeclaredField("shepherdBoardDialogueBeat");
                    fBeat.setAccessible(true);
                    fBeat.set(boardScreen, 2);
                    java.lang.reflect.Method mRefresh = BoardScreen.class.getDeclaredMethod("refreshShepherdBoardDialogueBeat", Player.class);
                    mRefresh.setAccessible(true);
                    mRefresh.invoke(boardScreen, mockParty.get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (frameCount == 25) {
                saveScreenshot("screenshots/shepherd_board_dialogue_beat3.png", null);
                try {
                    java.lang.reflect.Method mComplete = BoardScreen.class.getDeclaredMethod("triggerPaleShepherdEncounter", Player.class);
                    mComplete.setAccessible(true);
                    mComplete.invoke(boardScreen, mockParty.get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (frameCount == 35) {
                saveScreenshot("screenshots/shepherd_gentle_battle_turn1.png", null);
                if (getScreen() instanceof io.github.snakes_and_ladders_roll_of_fate.screens.BattleScreen) {
                    io.github.snakes_and_ladders_roll_of_fate.screens.BattleScreen bs =
                        (io.github.snakes_and_ladders_roll_of_fate.screens.BattleScreen) getScreen();
                    try {
                        java.lang.reflect.Field fSys = io.github.snakes_and_ladders_roll_of_fate.screens.BattleScreen.class.getDeclaredField("battleSystem");
                        fSys.setAccessible(true);
                        io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem sys =
                            (io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem) fSys.get(bs);
                        java.lang.reflect.Field fRound = io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.class.getDeclaredField("combatRound");
                        fRound.setAccessible(true);
                        fRound.set(sys, 3);
                        java.lang.reflect.Method mSync = io.github.snakes_and_ladders_roll_of_fate.screens.BattleScreen.class.getDeclaredMethod("syncUi");
                        mSync.setAccessible(true);
                        mSync.invoke(bs);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (frameCount >= 45) {
                saveScreenshot("screenshots/shepherd_gentle_battle_scramble.png", null);
                Gdx.app.exit();
            }
        }

        private void saveScreenshot(String path, int[] crop) {
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
