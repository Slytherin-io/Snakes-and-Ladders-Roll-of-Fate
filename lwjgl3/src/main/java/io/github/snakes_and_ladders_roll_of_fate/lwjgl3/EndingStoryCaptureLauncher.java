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
import io.github.snakes_and_ladders_roll_of_fate.screens.EndingStoryScreen;
import io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker.EndingType;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/**
 * Headless/Windowed automated visual capture tool for EndingStoryScreen.
 * Captures all narrative beats and character vignettes for both Victory and Defeat routes.
 */
public class EndingStoryCaptureLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Ending Story Slideshow Capture");
        config.setWindowedMode(1920, 1080);
        config.setDecorated(false);
        config.useVsync(false);

        new Lwjgl3Application(new CaptureGame(), config);
    }

    private static class CaptureGame extends RollOfFateGame {
        private int frameCount = 0;
        private int phase = 0;
        private EndingStoryScreen endingScreen;

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

            // Start with Cat victory sequence in party mode
            Player catHero = new Player("Luna", CharacterClass.CAT);
            catHero.setPosition(100);
            List<Player> party = new ArrayList<>();
            party.add(catHero);
            party.add(new Player("Rusty", CharacterClass.FOX));
            party.add(new Player("Shelldon", CharacterClass.TURTLE));
            party.add(new Player("Bandit", CharacterClass.RACCOON));

            endingScreen = new EndingStoryScreen(this, catHero, party, EndingType.REBIRTH_TRIUMPH);
            setScreen(endingScreen);
        }

        @Override
        public void render() {
            super.render();
            frameCount++;

            // Wait 10 frames for texture upload and layout stabilization
            if (phase == 0 && frameCount >= 10) {
                // Beat 1: Summit Dawn Victory
                saveScreenshot("screenshots/ending_slide_victory_summit.png");
                phase = 1;
                advanceScreenSlide();
                frameCount = 0;
            } else if (phase == 1 && frameCount >= 10) {
                // Beat 2: Warden Concession
                saveScreenshot("screenshots/ending_slide_warden_concession.png");
                phase = 2;
                advanceScreenSlide();
                frameCount = 0;
            } else if (phase == 2 && frameCount >= 10) {
                // Beat 3: Cat Victory
                saveScreenshot("screenshots/ending_slide_cat_victory.png");
                phase = 3;
                advanceScreenSlide();
                frameCount = 0;
            } else if (phase == 3 && frameCount >= 10) {
                // Beat 4: Fellowship Victory
                saveScreenshot("screenshots/ending_slide_fellowship_victory.png");
                phase = 4;

                // Test Turtle Victory
                Player turtleHero = new Player("Shelldon", CharacterClass.TURTLE);
                turtleHero.setPosition(100);
                endingScreen = new EndingStoryScreen(this, turtleHero, EndingType.REBIRTH_TRIUMPH);
                setScreen(endingScreen);
                advanceScreenSlide(); // Move to Slide 1
                advanceScreenSlide(); // Move to Slide 2 (Turtle Victory)
                frameCount = 0;
            } else if (phase == 4 && frameCount >= 10) {
                // Turtle Victory
                saveScreenshot("screenshots/ending_slide_turtle_victory.png");
                phase = 5;

                // Test Fox Victory
                Player foxHero = new Player("Rusty", CharacterClass.FOX);
                foxHero.setPosition(100);
                endingScreen = new EndingStoryScreen(this, foxHero, EndingType.REBIRTH_TRIUMPH);
                setScreen(endingScreen);
                advanceScreenSlide(); // Move to Slide 1
                advanceScreenSlide(); // Move to Slide 2 (Fox Victory)
                frameCount = 0;
            } else if (phase == 5 && frameCount >= 10) {
                // Fox Victory
                saveScreenshot("screenshots/ending_slide_fox_victory.png");
                phase = 6;

                // Test Raccoon Victory
                Player raccoonHero = new Player("Bandit", CharacterClass.RACCOON);
                raccoonHero.setPosition(100);
                endingScreen = new EndingStoryScreen(this, raccoonHero, EndingType.REBIRTH_TRIUMPH);
                setScreen(endingScreen);
                advanceScreenSlide(); // Move to Slide 1
                advanceScreenSlide(); // Move to Slide 2 (Raccoon Victory)
                frameCount = 0;
            } else if (phase == 6 && frameCount >= 10) {
                // Raccoon Victory
                saveScreenshot("screenshots/ending_slide_raccoon_victory.png");
                phase = 7;

                // Test Defeat Route with Cat Hero & Party
                Player catDefeat = new Player("Luna", CharacterClass.CAT);
                catDefeat.setPosition(100);
                List<Player> party = new ArrayList<>();
                party.add(catDefeat);
                party.add(new Player("Rusty", CharacterClass.FOX));
                endingScreen = new EndingStoryScreen(this, catDefeat, party, EndingType.PEACEFUL_REST);
                setScreen(endingScreen);
                advanceScreenSlide(); // Move to Slide 1 (Shepherd Solace)
                frameCount = 0;
            } else if (phase == 7 && frameCount >= 10) {
                // Defeat Shepherd Solace
                saveScreenshot("screenshots/ending_slide_shepherd_solace.png");
                phase = 8;
                advanceScreenSlide(); // Move to Slide 2 (Cat Rest)
                frameCount = 0;
            } else if (phase == 8 && frameCount >= 10) {
                // Defeat Cat Rest
                saveScreenshot("screenshots/ending_slide_cat_rest.png");
                phase = 9;
                advanceScreenSlide(); // Move to Slide 3 (Fellowship Rest)
                frameCount = 0;
            } else if (phase == 9 && frameCount >= 10) {
                // Defeat Fellowship Rest
                saveScreenshot("screenshots/ending_slide_fellowship_rest.png");
                phase = 10;
                System.out.println("ENDING_STORY_CAPTURE_COMPLETE");
                Gdx.app.exit();
            }
        }

        private void advanceScreenSlide() {
            try {
                java.lang.reflect.Method method = EndingStoryScreen.class.getDeclaredMethod("advanceSlide");
                method.setAccessible(true);
                method.invoke(endingScreen);
            } catch (Exception e) {
                e.printStackTrace();
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
