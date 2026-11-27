package io.github.snakes_and_ladders_roll_of_fate.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.screens.BattleScreen;
import io.github.snakes_and_ladders_roll_of_fate.screens.BoardScreen;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/**
 * Headless/visual verification launcher capturing:
 * 1. Act I Snake Combat (Emerald Horned Viper)
 * 2. Act II Snake Combat (Sapphire Horned Serpent)
 * 3. Act III Snake Combat (Crimson Magma Wyrm)
 * 4. Board Screen view of Act-stratified Snake tiles
 */
public class SnakeActBattleCaptureLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Capture Snake Act Battles");
        config.setWindowedMode(1920, 1080);
        config.setDecorated(false);
        config.useVsync(false);

        new Lwjgl3Application(new CaptureGame(), config);
    }

    private static class CaptureGame extends RollOfFateGame {
        private int step = 0;
        private int frameCount = 0;
        private Player player;
        private Board board;

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

            board = new Board(100);
            board.generateBoard();
            try {
                java.lang.reflect.Field bField = RollOfFateGame.class.getDeclaredField("board");
                bField.setAccessible(true);
                bField.set(this, board);
            } catch (Exception e) {
                e.printStackTrace();
            }

            player = new Player("Hero", CharacterClass.CAT);
            List<Player> party = new ArrayList<>();
            party.add(player);
            try {
                java.lang.reflect.Field pField = RollOfFateGame.class.getDeclaredField("players");
                pField.setAccessible(true);
                pField.set(this, party);

                java.lang.reflect.Field tField = RollOfFateGame.class.getDeclaredField("currentTurn");
                tField.setAccessible(true);
                tField.set(this, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Start with Act 1 Battle
            loadActBattle(1);
        }

        private void loadActBattle(int act) {
            int tilePos = (act == 1) ? 15 : ((act == 2) ? 45 : 85);
            player.setPosition(tilePos);
            SnakeTile snakeTile = new SnakeTile(tilePos, Math.max(1, tilePos - 10));
            BattleScreen battleScreen = new BattleScreen(this, player, snakeTile);
            setScreen(battleScreen);
        }

        @Override
        public void render() {
            super.render();
            frameCount++;

            // Step 0: Capture Act 1 Battle
            if (step == 0 && frameCount == 15) {
                saveScreenshot("screenshots/battle_act1_snake.png", null);
                saveScreenshot("screenshots/battle_act1_snake_crop.png", new int[]{1100, 360, 520, 520});
                saveScreenshot("screenshots/battle_act1_platform.png", new int[]{1160, 540, 300, 340});
                step = 1;
                frameCount = 0;
                loadActBattle(2);
            }
            // Step 1: Capture Act 2 Battle
            else if (step == 1 && frameCount == 15) {
                saveScreenshot("screenshots/battle_act2_snake.png", null);
                saveScreenshot("screenshots/battle_act2_snake_crop.png", new int[]{1100, 360, 520, 520});
                saveScreenshot("screenshots/battle_act2_platform.png", new int[]{1160, 540, 300, 340});
                step = 2;
                frameCount = 0;
                loadActBattle(3);
            }
            // Step 2: Capture Act 3 Battle
            else if (step == 2 && frameCount == 15) {
                saveScreenshot("screenshots/battle_act3_snake.png", null);
                saveScreenshot("screenshots/battle_act3_snake_crop.png", new int[]{1100, 360, 520, 520});
                saveScreenshot("screenshots/battle_act3_platform.png", new int[]{1160, 540, 300, 340});
                step = 3;
                frameCount = 0;

                // Setup Act 1 Board Snake Tile
                board.getTiles().set(13, new SnakeTile(14, 4));
                player.setPosition(13);
                BoardScreen bs1 = new BoardScreen(this);
                setScreen(bs1);
            }
            // Step 3: Capture Act 1 Board Snake
            else if (step == 3 && frameCount == 25) {
                saveScreenshot("screenshots/board_act1_snake.png", null);
                saveScreenshot("screenshots/board_act1_snake_crop.png", new int[]{850, 480, 500, 420});
                step = 4;
                frameCount = 0;

                // Setup Act 2 Board Snake Tile
                board.getTiles().set(44, new SnakeTile(45, 33));
                player.setPosition(44);
                BoardScreen bs2 = new BoardScreen(this);
                setScreen(bs2);
            }
            // Step 4: Capture Act 2 Board Snake
            else if (step == 4 && frameCount == 25) {
                saveScreenshot("screenshots/board_act2_snake.png", null);
                saveScreenshot("screenshots/board_act2_snake_crop.png", new int[]{850, 480, 500, 420});
                step = 5;
                frameCount = 0;

                // Setup Act 3 Board Snake Tile
                board.getTiles().set(84, new SnakeTile(85, 73));
                player.setPosition(84);
                BoardScreen bs3 = new BoardScreen(this);
                setScreen(bs3);
            }
            // Step 5: Capture Act 3 Board Snake
            else if (step == 5 && frameCount == 25) {
                saveScreenshot("screenshots/board_act3_snake.png", null);
                saveScreenshot("screenshots/board_act3_snake_crop.png", new int[]{850, 480, 500, 420});
                System.out.println("ALL SCREENSHOTS CAPTURED SUCCESSFULLY!");
                Gdx.app.exit();
            }
        }

        private void saveScreenshot(String path, int[] crop) {
            byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, 1920, 1080, true);
            Pixmap full = new Pixmap(1920, 1080, Pixmap.Format.RGBA8888);
            full.getPixels().put(pixels);
            full.getPixels().flip();

            File out = new File(path);
            out.getParentFile().mkdirs();

            if (crop != null) {
                int cx = crop[0], cy = crop[1], cw = crop[2], ch = crop[3];
                Pixmap cropped = new Pixmap(cw, ch, Pixmap.Format.RGBA8888);
                cropped.drawPixmap(full, 0, 0, cx, cy, cw, ch);
                PixmapIO.writePNG(Gdx.files.absolute(out.getAbsolutePath()), cropped);
                cropped.dispose();
            } else {
                PixmapIO.writePNG(Gdx.files.absolute(out.getAbsolutePath()), full);
            }
            full.dispose();
            System.out.println("Saved: " + out.getAbsolutePath());
        }
    }
}
