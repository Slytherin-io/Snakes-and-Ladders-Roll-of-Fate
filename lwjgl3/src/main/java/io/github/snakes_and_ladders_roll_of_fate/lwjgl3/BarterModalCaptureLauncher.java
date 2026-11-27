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
import io.github.snakes_and_ladders_roll_of_fate.items.BarterCategory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.screens.BoardScreen;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventChoice;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/**
 * Capture tool to preview and verify the simplified Barter Exchange modal UI.
 */
public class BarterModalCaptureLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Capture Barter Modal");
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

            Board b = new Board(100);
            b.generateBoard();
            try {
                java.lang.reflect.Field bField = RollOfFateGame.class.getDeclaredField("board");
                bField.setAccessible(true);
                bField.set(this, b);
            } catch (Exception e) {
                e.printStackTrace();
            }

            mockParty = new ArrayList<>();
            Player p1 = new Player("Player 1", CharacterClass.CAT);
            p1.setHealth(85);
            p1.setPosition(31);

            // Populate items matching user's exact inventory
            Item item1 = Item.createConsumable("Minor Health Potion", EventEffectType.HEALTH, 25, "Restores 25 HP");
            item1.setTier(1);

            Item item2 = Item.createEquipment("Iron Talon", EventEffectType.ATTACK, 4, 1, 1, "+4 ATK (1-Use Relic)");
            item2.setTier(2);

            Item item3 = Item.createConsumable("Idol of Greed", EventEffectType.HEALTH, 30, "+30 HP & double gamble");
            item3.setTier(2);

            p1.getInventory().addItem(item1);
            p1.getInventory().addItem(item2);
            p1.getInventory().addItem(item3);
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

            // Trigger showBarterItemSelector directly
            try {
                EventChoice barterChoice = EventChoice.createEqualBarterChoice("Barter Armament", BarterCategory.ARMAMENT);
                java.lang.reflect.Method mBarter = BoardScreen.class.getDeclaredMethod("showBarterItemSelector", Player.class, EventChoice.class, io.github.snakes_and_ladders_roll_of_fate.board.Tile.class);
                mBarter.setAccessible(true);
                mBarter.invoke(boardScreen, p1, barterChoice, b.getTile(31));

                // Make modal visible
                java.lang.reflect.Field fModal = BoardScreen.class.getDeclaredField("eventModalTable");
                fModal.setAccessible(true);
                com.badlogic.gdx.scenes.scene2d.ui.Table table = (com.badlogic.gdx.scenes.scene2d.ui.Table) fModal.get(boardScreen);
                table.setVisible(true);

                java.lang.reflect.Field fScrim = BoardScreen.class.getDeclaredField("modalScrim");
                fScrim.setAccessible(true);
                com.badlogic.gdx.scenes.scene2d.ui.Table scrim = (com.badlogic.gdx.scenes.scene2d.ui.Table) fScrim.get(boardScreen);
                scrim.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void render() {
            super.render();
            frameCount++;

            if (frameCount == 15) {
                saveScreenshot("screenshots/barter_modal_full.png", null);
                // Crop around the parchment scroll
                saveScreenshot("screenshots/barter_modal_crop.png", new int[]{460, 180, 1000, 720});
                Gdx.app.exit();
            }
        }

        private void saveScreenshot(String path, int[] crop) {
            byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, 1920, 1080, true);
            Pixmap full = new Pixmap(1920, 1080, Pixmap.Format.RGBA8888);
            full.getPixels().put(pixels);
            full.getPixels().flip();

            Pixmap toSave;
            if (crop != null) {
                toSave = new Pixmap(crop[2], crop[3], Pixmap.Format.RGBA8888);
                toSave.drawPixmap(full, 0, 0, crop[0], crop[1], crop[2], crop[3]);
                full.dispose();
            } else {
                toSave = full;
            }

            try {
                Gdx.files.local(path).parent().mkdirs();
                PixmapIO.writePNG(Gdx.files.local(path), toSave);
                System.out.println("CAPTURE_SUCCESS: " + path);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                toSave.dispose();
            }
        }
    }
}
