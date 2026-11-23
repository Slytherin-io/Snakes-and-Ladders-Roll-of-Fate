package io.github.snakes_and_ladders_roll_of_fate.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

/**
 * General UI helper methods for animations, procedural shapes, and font generation.
 */
public class UIUtils {

    /**
     * Gives buttons that bouncy game feel by scaling up 5% when hovered.
     */
    public static void addScaleHoverEffect(Button button) {
        button.setTransform(true);

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // pointer == -1 means a mouse hover (not a mobile touch drag)
                if (pointer == -1) {
                    button.setOrigin(Align.center);
                    button.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    button.setOrigin(Align.center);
                    button.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f));
                }
            }
        });
    }

    /**
     * Generates a rounded rectangle 9-patch so it can stretch to any button or banner size.
     */
    public static Drawable createRoundedDrawable(int width, int height, int radius, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.CLEAR);
        pixmap.fill(); // Clear with transparency to prevent dirty pixels

        pixmap.setColor(color);

        // Fill center rectangles
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);

        // Round off the 4 corners
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        // 9-patch keeps the corners sharp while allowing the middle to stretch cleanly
        NinePatch patch = new NinePatch(texture, radius, radius, radius, radius);
        return new NinePatchDrawable(patch);
    }

    /**
     * Generates a crisp BitmapFont from a TrueType (.ttf) font at the exact size and color needed.
     */
    public static BitmapFont createFont(String ttfPath, int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(ttfPath));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose(); // Generator is no longer needed once the font is baked
        return font;
    }

    /**
     * Builds a custom slider style with a rounded track and a circular knob.
     */
    public static Slider.SliderStyle createSliderStyle(Color trackColor, Color knobColor) {
        // Horizontal track line with rounded ends
        Drawable track = createRoundedDrawable(120, 8, 4, trackColor);

        // Circular knob with transparent background
        Pixmap knobPixmap = new Pixmap(26, 26, Pixmap.Format.RGBA8888);
        knobPixmap.setColor(Color.CLEAR);
        knobPixmap.fill();
        knobPixmap.setColor(knobColor);
        knobPixmap.fillCircle(13, 13, 12);
        Texture knobTex = new Texture(knobPixmap);
        knobPixmap.dispose();
        Drawable knob = new TextureRegionDrawable(knobTex);

        Slider.SliderStyle style = new Slider.SliderStyle();
        style.background = track;
        style.knob = knob;
        return style;
    }
}
