package io.github.snakes_and_ladders_roll_of_fate.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import java.util.List;

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
     * Generates a solid rounded rectangle 9-patch so it can stretch to any button or banner size.
     */
    public static Drawable createRoundedDrawable(int width, int height, int radius, Color color) {
        return createRoundedDrawable(width, height, radius, color, null, 0);
    }

    /**
     * Generates a solid rounded rectangle 9-patch with a crisp solid border.
     * Uses Pixmap.Blending.None to eliminate any double-blending or corner circular artifacts.
     */
    public static Drawable createRoundedDrawable(int width, int height, int radius, Color fillColor, Color borderColor, int borderWidth) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(Color.CLEAR);
        pixmap.fill(); // Clear with transparency to prevent dirty pixels

        if (borderColor != null && borderWidth > 0) {
            // 1. Draw outer solid rounded rectangle with border color
            drawRoundedRect(pixmap, 0, 0, width, height, radius, borderColor);
            // 2. Draw inner solid rounded rectangle with fill color
            int innerR = Math.max(1, radius - borderWidth);
            drawRoundedRect(pixmap, borderWidth, borderWidth, width - 2 * borderWidth, height - 2 * borderWidth, innerR, fillColor);
        } else {
            drawRoundedRect(pixmap, 0, 0, width, height, radius, fillColor);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        // 9-patch keeps the corners sharp while allowing the middle to stretch cleanly
        NinePatch patch = new NinePatch(texture, radius, radius, radius, radius);
        return new NinePatchDrawable(patch);
    }

    private static void drawRoundedRect(Pixmap pixmap, int x, int y, int width, int height, int radius, Color color) {
        pixmap.setColor(color);
        pixmap.fillRectangle(x + radius, y, width - 2 * radius, height);
        pixmap.fillRectangle(x, y + radius, width, height - 2 * radius);
        pixmap.fillCircle(x + radius, y + radius, radius);
        pixmap.fillCircle(x + width - radius, y + radius, radius);
        pixmap.fillCircle(x + radius, y + height - radius, radius);
        pixmap.fillCircle(x + width - radius, y + height - radius, radius);
    }

    /**
     * Generates a crisp BitmapFont from a TrueType (.ttf) font at the exact size and color needed.
     */
    public static BitmapFont createFont(String ttfPath, int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(ttfPath));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
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

    /**
     * Procedurally generates a crisp icosahedron D20 emblem texture with faceted mahogany fill and gold trim.
     */
    public static Texture createD20EmblemTexture(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.SourceOver);
        pm.setColor(Color.CLEAR);
        pm.fill();

        float cx = size * 0.5f;
        float cy = size * 0.5f;
        float rOuter = size * 0.44f;
        float rInner = size * 0.28f;

        // 6 outer hexagon vertices (point-up)
        int[] ox = new int[6];
        int[] oy = new int[6];
        for (int i = 0; i < 6; i++) {
            double rad = Math.toRadians(-90 + i * 60);
            ox[i] = Math.round(cx + (float) Math.cos(rad) * rOuter);
            oy[i] = Math.round(cy + (float) Math.sin(rad) * rOuter);
        }

        // Outer mahogany body fill
        pm.setColor(Color.valueOf("#6e3817"));
        for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            pm.fillTriangle(Math.round(cx), Math.round(cy), ox[i], oy[i], ox[next], oy[next]);
        }

        // Inner pentagon-like lighter facet for 3D depth
        int[] ix = new int[5];
        int[] iy = new int[5];
        for (int i = 0; i < 5; i++) {
            double rad = Math.toRadians(-90 + i * 72);
            ix[i] = Math.round(cx + (float) Math.cos(rad) * rInner);
            iy[i] = Math.round(cy + (float) Math.sin(rad) * rInner);
        }
        pm.setColor(Color.valueOf("#924f24"));
        for (int i = 0; i < 5; i++) {
            int next = (i + 1) % 5;
            pm.fillTriangle(Math.round(cx), Math.round(cy), ix[i], iy[i], ix[next], iy[next]);
        }

        // Subtle warm highlight facets
        pm.setColor(Color.valueOf("#b0683488"));
        pm.fillTriangle(Math.round(cx), Math.round(cy), ix[0], iy[0], ix[1], iy[1]);

        // Inner facet lines
        pm.setColor(Color.valueOf("#e4b76e"));
        for (int i = 0; i < 5; i++) {
            int next = (i + 1) % 5;
            pm.drawLine(ix[i], iy[i], ix[next], iy[next]);
        }

        // Struts connecting outer vertices to inner facet corners
        pm.setColor(Color.valueOf("#ffd54f"));
        pm.drawLine(ox[0], oy[0], ix[0], iy[0]);
        pm.drawLine(ox[1], oy[1], ix[1], iy[1]);
        pm.drawLine(ox[2], oy[2], ix[2], iy[2]);
        pm.drawLine(ox[3], oy[3], ix[2], iy[2]);
        pm.drawLine(ox[3], oy[3], ix[3], iy[3]);
        pm.drawLine(ox[4], oy[4], ix[3], iy[3]);
        pm.drawLine(ox[5], oy[5], ix[4], iy[4]);

        // Bold outer hexagon outline (3px thick via center + 1px offsets)
        for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    pm.drawLine(ox[i] + dx, oy[i] + dy, ox[next] + dx, oy[next] + dy);
                }
            }
        }

        Texture tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return tex;
    }

    // =========================================================================
    // Shared Conventional Keyboard Navigation & Activation Helpers
    // =========================================================================

    public static boolean isShiftHeld() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    public static boolean isConfirmJustPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
    }

    public static boolean isBackJustPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE);
    }

    public static boolean isNavPrevVerticalJustPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W);
    }

    public static boolean isNavNextVerticalJustPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S);
    }

    public static boolean isNavPrevHorizontalJustPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A);
    }

    public static boolean isNavNextHorizontalJustPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D);
    }

    /**
     * Returns 0..maxCount-1 if keys 1-9 (top row or numpad) were just pressed, otherwise -1.
     */
    public static int justPressedNumberKey(int maxCount) {
        int limit = Math.min(9, maxCount);
        for (int i = 0; i < limit; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)
                || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1 + i)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isActorActivatable(Actor actor) {
        if (actor == null || !actor.isVisible() || actor.getTouchable() == Touchable.disabled) {
            return false;
        }
        if (actor instanceof Button) {
            return !((Button) actor).isDisabled();
        }
        return true;
    }

    public static boolean activateButton(Actor actor) {
        if (!isActorActivatable(actor)) return false;
        actor.fire(new ChangeListener.ChangeEvent());
        return true;
    }

    /**
     * Cycles index cleanly across a list of buttons/controls, skipping disabled/hidden items when possible.
     */
    public static int stepFocus(List<? extends Actor> items, int currentIndex, int delta) {
        if (items == null || items.isEmpty()) return 0;
        int n = items.size();
        int idx = (currentIndex < 0 || currentIndex >= n) ? 0 : currentIndex;
        for (int attempt = 0; attempt < n; attempt++) {
            idx = ((idx + delta) % n + n) % n;
            if (isActorActivatable(items.get(idx))) {
                break;
            }
        }
        applyFocusVisuals(items, idx);
        return idx;
    }

    /**
     * Applies crisp visual highlight and subtle scale emphasis to the focused button or slider.
     */
    public static void applyFocusVisuals(List<? extends Actor> items, int selectedIndex) {
        if (items == null || items.isEmpty()) return;
        Color focusedTint = Color.valueOf("#ffe082");
        for (int i = 0; i < items.size(); i++) {
            Actor a = items.get(i);
            if (a == null) continue;
            boolean focused = (i == selectedIndex);
            if (a instanceof Button) {
                Button btn = (Button) a;
                btn.setOrigin(Align.center);
                btn.setTransform(true);
                if (!btn.isDisabled()) {
                    btn.setColor(focused ? focusedTint : Color.WHITE);
                    btn.setScale(focused ? 1.045f : 1.0f);
                } else {
                    btn.setScale(1.0f);
                }
            } else if (a instanceof Slider) {
                a.setColor(focused ? focusedTint : Color.WHITE);
            }
        }
    }
}
