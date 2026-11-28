package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.audio.AudioManager;
import io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Entity;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.screens.MainMenuScreen;
import io.github.snakes_and_ladders_roll_of_fate.screens.GameSummaryScreen;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleState;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleOutcome;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;
import io.github.snakes_and_ladders_roll_of_fate.ui.UIUtils;

import java.util.HashMap;
import java.util.Map;

public class BattleScreen implements Screen {
    // Dynamic Attack Effects & Particle System Data Structures
    public enum SlashStyle {
        PLAYER_SWORD,
        SNAKE_FANGS,
        DAWN_SOLAR_CLAW,
        SHEPHERD_SCYTHE,
        CAT_DUAL_CLAW,
        FOX_VITAL_PIERCE,
        RACCOON_HOOK_SWIPE,
        TURTLE_RIPOSTE
    }

    public static class HitSpark {
        float x, y, vx, vy;
        float timer, maxDuration, size, gravity;
        Color color;

        public HitSpark(float x, float y, float vx, float vy, float duration, float size, Color color) {
            this(x, y, vx, vy, duration, size, 750f, color);
        }

        public HitSpark(float x, float y, float vx, float vy, float duration, float size, float gravity, Color color) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.timer = duration; this.maxDuration = duration; this.size = size;
            this.gravity = gravity;
            this.color = color;
        }
    }

    public static class SlashEffect {
        float x, y;
        float timer;
        float maxDuration;
        SlashStyle style;
        boolean isCrit;
        float angle;

        public SlashEffect(float x, float y, float duration, SlashStyle style, boolean isCrit, float angle) {
            this.x = x; this.y = y;
            this.timer = duration;
            this.maxDuration = duration;
            this.style = style;
            this.isCrit = isCrit;
            this.angle = angle;
        }
    }

    public static class ShieldPulse {
        float x, y;
        float timer;
        float maxDuration;
        float startRadius;
        float expandDistance;
        Color outerColor;
        Color innerColor;

        public ShieldPulse(float x, float y, float duration) {
            this(x, y, duration, 50f, 90f, new Color(0.3f, 0.85f, 1.0f, 1f), new Color(1.0f, 0.9f, 0.4f, 1f));
        }

        public ShieldPulse(float x, float y, float duration, float startRadius, float expandDistance, Color outerColor, Color innerColor) {
            this.x = x; this.y = y;
            this.timer = duration;
            this.maxDuration = duration;
            this.startRadius = startRadius;
            this.expandDistance = expandDistance;
            this.outerColor = outerColor;
            this.innerColor = innerColor;
        }
    }

    public static class DamageNumber {
        String text;
        float x, startY;
        float timer;
        float maxDuration;
        Color color;
        boolean isCrit;

        public DamageNumber(String text, float x, float y, float duration, Color color, boolean isCrit) {
            this.text = text;
            this.x = x;
            this.startY = y;
            this.timer = duration;
            this.maxDuration = duration;
            this.color = color;
            this.isCrit = isCrit;
        }
    }

    private final RollOfFateGame game;
    private final Player player;
    private final SnakeTile snakeTile;
    private final Enemy enemy;
    private int enemyMaxHp;
    private final BattleSystem battleSystem;

    // Combat FX Lists & Kinematics
    private final Array<HitSpark> hitSparks = new Array<>();
    private final Array<SlashEffect> slashEffects = new Array<>();
    private final Array<ShieldPulse> shieldPulses = new Array<>();
    private final Array<DamageNumber> damageNumbers = new Array<>();

    private static final float PLAYER_LUNGE_DURATION = 0.44f;
    private static final float ENEMY_LUNGE_DURATION = 0.46f;
    private static final float FLINCH_DURATION = 0.32f;

    private float playerLungeTimer = 0f;
    private float enemyLungeTimer = 0f;
    private boolean playerAttackLanded = false;
    private boolean enemyAttackLanded = false;

    private float playerFlinchTimer = 0f;
    private float enemyFlinchTimer = 0f;

    // Class Skill Animation States & Kinematics
    private float skillAnimTimer = 0f;
    private float skillAnimDuration = 0f;
    private CharacterClass activeSkillClass = null;
    private boolean skillPhase1Done = false;
    private boolean skillPhase2Done = false;

    private float screenShakeTimer = 0f;
    private float screenShakeDuration = 0f;
    private float screenShakeIntensity = 0f;

    // Cinematic Dawn Warden Entrance, Enrage & Solar FX State
    private static final float BOSS_ENTRANCE_DURATION = 1.90f;
    private static final float BOSS_DESCENT_START = 0.45f;
    private static final float BOSS_LAND_TIME = 1.25f;
    private boolean bossEntranceActive = false;
    private float bossEntranceTimer = 0f;
    private boolean bossLanded = true;
    private boolean bossEnraged = false;
    private float bossVeilAlpha = 0f;
    private float bossFlashAlpha = 0f;
    private float bossEmberSpawnTimer = 0f;

    // Rendering
    private SpriteBatch batch;
    private OrthographicCamera worldCamera;
    private ExtendViewport worldViewport;
    private Stage hudStage;
    private Texture whitePixelTex;
    private Texture radialGlowTex;

    // Parallax Slices
    private TextureRegion act1Far, act1Mid, act1Ground;
    private TextureRegion act2Far, act2Mid, act2Ground;
    private TextureRegion act3Far, act3Mid, act3Ground;

    // Layout Constants (MATCHING BoardScreen EXACTLY)
    private static final float GROUND_BASE_Y = 110f;
    private static final float TILE_WIDTH = 180f;
    private static final float TILE_HEIGHT = 55f;
    private static final float TILE_BOTTOM_Y = 230f;
    private static final float CHAR_DISPLAY_SIZE = 210f; // Match BoardScreen
    private static final float CHAR_DRAW_Y = TILE_BOTTOM_Y + 14f; // Match BoardScreen (244f)
    private static final float ENEMY_DRAW_Y = 282f; // Grounded on top surface of stone slab platform

    // Entity Positions (Centered on their respective tiles)
    private static final float PLAYER_X = 600f;
    private static final float ENEMY_X = 1300f;

    // Fonts (MATCHING BoardScreen Hierarchy)
    private BitmapFont titleFont, topBannerFont, hudHpFont, battleLogFont, playerTagFont, miniFont;
    private BitmapFont damageFont, critFont;
    private BitmapFont modalTitleFont, modalSubFont, modalRewardFont, modalNarrativeFont, modalStatusFont;
    private BitmapFont introSpeakerFont, introBodyFont, introBtnFont;

    // Pause, Settings, & Leave Menu Fonts & UI References
    private BitmapFont topPauseFont;
    private BitmapFont pauseBadgeFont;
    private BitmapFont pauseTitleFont;
    private BitmapFont pauseSubFont;
    private BitmapFont pauseBodyFont;
    private BitmapFont pauseBtnFont;
    private BitmapFont pauseTipFont;
    private BitmapFont sliderFont;

    private boolean isPaused = false;
    private Button pauseBtn;
    private Table pauseModalScrim;
    private Table pauseModalTable;
    private Table settingsModalTable;
    private Table leaveConfirmModalTable;

    // UI Elements
    private Label topBannerLabel, battleLogLabel, playerHpLabel, enemyHpLabel, enemyNameLabel;
    private Table topBannerWrap, topBannerBox, playerHpWrap, enemyHpWrap;
    private Table battleLogContainer;
    private TextButton btnAttack, btnDefend, btnItem, btnEscape, btnSkill, continueBtn;
    private Table bottomBar;
    private Table bossIntroTable;
    private Label introDialogueLabel;
    private Table introActionsRow;
    private TextButton introAdvanceBtn;
    private TextButton introSkipBtn;
    private boolean isIntroDialogueActive = false;
    private int introBeatIndex = 0;
    private boolean isEpilogueDialogueActive = false;
    private boolean isEpilogueVictory = false;
    private int epilogueBeatIndex = 0;
    private boolean scrambleAnnounced = false;
    private Cell<TextButton> continueBtnCell;
    private Table itemPopup;
    private Table continueTable;
    private Table victoryCard;
    private Label modalTitleLabel;
    private Label modalSubtitleLabel;
    private Label rewardExpLabel;
    private Label rewardLootLabel;
    private Label rewardStatusLabel;

    // Textures & Animations
    private Texture enemyTex;
    private Animation<TextureRegion> playerIdleAnim;
    private Animation<TextureRegion> enemyIdleAnim;
    private Animation<TextureRegion> enemyAttackAnim;
    private int currentSnakeAct = 1;
    private float stateTime = 0f;
    private boolean outcomeResolved = false;
    private String outcomeMessage = "";

    private int resolveSnakeAct() {
        if (snakeTile != null) {
            return (snakeTile.getPosition() <= 30) ? 1 : ((snakeTile.getPosition() <= 70) ? 2 : 3);
        } else if (player != null && player.getPosition() > 0) {
            return (player.getPosition() <= 30) ? 1 : ((player.getPosition() <= 70) ? 2 : 3);
        } else if (enemy != null && enemy.getEnemyType() != null) {
            String eType = enemy.getEnemyType();
            if (eType.contains("Obsidian") || eType.contains("Crag") || eType.contains("Rattler")) {
                return 2;
            } else if (eType.contains("Magma") || eType.contains("Abyssal") || eType.contains("Celestial")) {
                return 3;
            }
        }
        return 1;
    }

    public BattleScreen(RollOfFateGame game, Player player, SnakeTile snakeTile) {
        this(game, player, snakeTile != null ? snakeTile.createSnakeEnemy() : null, snakeTile);
    }

    public BattleScreen(RollOfFateGame game, Player player, Enemy bossEnemy) {
        this(game, player, bossEnemy, null);
    }

    public BattleScreen(RollOfFateGame game, Player player, Enemy enemy, SnakeTile snakeTile) {
        this.game = game;
        this.player = player;
        this.snakeTile = snakeTile;
        this.enemy = enemy;
        this.enemyMaxHp = (this.enemy != null) ? this.enemy.getHealth() : 100;
        this.battleSystem = new BattleSystem();
    }

    public boolean isDawnWardenBattle() {
        return enemy != null && enemy.getEnemyType() != null && enemy.getEnemyType().contains("Dawn Warden");
    }

    public boolean isPaleShepherdBattle() {
        return enemy != null && enemy.getEnemyType() != null && enemy.getEnemyType().contains("Pale Shepherd");
    }

    public boolean isBossBattle() {
        return isDawnWardenBattle() || isPaleShepherdBattle() || snakeTile == null;
    }

    private void triggerScreenShake(float intensity, float duration) {
        this.screenShakeIntensity = intensity;
        this.screenShakeDuration = duration;
        this.screenShakeTimer = duration;
    }

    private void spawnSlash(float x, float y, boolean isEnemyAttack, boolean isCrit) {
        if (!isEnemyAttack) {
            slashEffects.add(new SlashEffect(x, y, 0.30f, SlashStyle.PLAYER_SWORD, isCrit, -32f));
        } else if (isDawnWardenBattle()) {
            slashEffects.add(new SlashEffect(x, y + 10f, 0.36f, SlashStyle.DAWN_SOLAR_CLAW, isCrit, -28f));
        } else if (isPaleShepherdBattle()) {
            slashEffects.add(new SlashEffect(x, y + 10f, 0.34f, SlashStyle.SHEPHERD_SCYTHE, isCrit, 25f));
        } else {
            slashEffects.add(new SlashEffect(x, y, 0.32f, SlashStyle.SNAKE_FANGS, isCrit, 0f));
        }
    }

    private void spawnHitSparks(float x, float y, boolean isEnemyAttack, int count, boolean isCrit) {
        Color[] playerColors = new Color[]{
            new Color(1f, 1f, 1f, 1f),
            new Color(1f, 0.9f, 0.2f, 1f),
            new Color(1f, 0.55f, 0.1f, 1f),
            new Color(1f, 0.2f, 0.2f, 1f)
        };
        Color[] solarColors = new Color[]{
            new Color(1f, 0.96f, 0.72f, 1f),
            new Color(1f, 0.80f, 0.22f, 1f),
            new Color(1f, 0.45f, 0.12f, 1f),
            new Color(1f, 0.22f, 0.08f, 1f)
        };
        Color[] shepherdColors = new Color[]{
            new Color(1f, 0.28f, 0.32f, 1f),
            new Color(0.95f, 0.85f, 0.45f, 1f),
            new Color(0.55f, 0.85f, 1f, 1f)
        };
        Color[] snakeColors = new Color[]{
            new Color(0.85f, 1f, 0.85f, 1f),
            new Color(0.25f, 1f, 0.2f, 1f),
            new Color(0.1f, 0.85f, 0.3f, 1f),
            new Color(0.75f, 0.2f, 1f, 1f)
        };

        Color[] palette;
        if (!isEnemyAttack) {
            palette = playerColors;
        } else if (isDawnWardenBattle()) {
            palette = solarColors;
        } else if (isPaleShepherdBattle()) {
            palette = shepherdColors;
        } else {
            palette = snakeColors;
        }

        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(0f, 360f) * MathUtils.degRad;
            float speed = MathUtils.random(isCrit ? 220f : 160f, isCrit ? 600f : 440f);
            float vx = MathUtils.cos(angle) * speed;
            float vy = MathUtils.sin(angle) * speed + MathUtils.random(50f, 180f);
            float duration = MathUtils.random(0.32f, 0.54f);
            float size = MathUtils.random(3.5f, isCrit ? 6.5f : 5.0f);
            Color c = palette[MathUtils.random(palette.length - 1)];
            hitSparks.add(new HitSpark(x, y, vx, vy, duration, size, c));
        }
    }

    private void spawnShockwave(float x, float y, float startRadius, float expandDistance, Color outerColor, Color innerColor, float duration) {
        shieldPulses.add(new ShieldPulse(x, y, duration, startRadius, expandDistance, outerColor, innerColor));
    }

    private void spawnSolarBurst(float x, float y, int count) {
        Color[] solarPalette = new Color[]{
            Color.valueOf("#fff3c4"),
            Color.valueOf("#ffd54f"),
            Color.valueOf("#ff9100"),
            Color.valueOf("#ff5252")
        };
        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(-15f, 195f) * MathUtils.degRad;
            float speed = MathUtils.random(180f, 640f);
            float vx = MathUtils.cos(angle) * speed;
            float vy = MathUtils.sin(angle) * speed + MathUtils.random(40f, 160f);
            float duration = MathUtils.random(0.40f, 0.80f);
            float size = MathUtils.random(4.0f, 7.5f);
            Color c = solarPalette[MathUtils.random(solarPalette.length - 1)];
            hitSparks.add(new HitSpark(x + MathUtils.random(-50f, 50f), y, vx, vy, duration, size, 520f, c));
        }
    }

    private void spawnRisingEmber(float cx, float cy) {
        Color c = bossEnraged
            ? (MathUtils.randomBoolean() ? Color.valueOf("#ff6e40") : Color.valueOf("#ffd54f"))
            : (MathUtils.randomBoolean() ? Color.valueOf("#ffe082") : Color.valueOf("#ffab40"));
        float vx = MathUtils.random(-36f, 36f);
        float vy = MathUtils.random(75f, 160f);
        float dur = MathUtils.random(0.75f, 1.35f);
        float size = MathUtils.random(3.5f, 6.0f);
        hitSparks.add(new HitSpark(cx + MathUtils.random(-125f, 125f), cy + MathUtils.random(0f, 95f), vx, vy, dur, size, -45f, c));
    }

    private void spawnHealSparks(float x, float y) {
        Color[] healColors = new Color[]{
            new Color(0.3f, 1f, 0.4f, 1f),
            new Color(0.7f, 1f, 0.5f, 1f),
            new Color(1f, 0.95f, 0.4f, 1f)
        };
        for (int i = 0; i < 18; i++) {
            float vx = MathUtils.random(-80f, 80f);
            float vy = MathUtils.random(120f, 320f);
            float duration = MathUtils.random(0.45f, 0.75f);
            float size = MathUtils.random(3.5f, 5.5f);
            Color c = healColors[MathUtils.random(healColors.length - 1)];
            hitSparks.add(new HitSpark(x + MathUtils.random(-45f, 45f), y + MathUtils.random(-30f, 30f), vx, vy, duration, size, c));
        }
    }

    private void spawnShieldPulse(float x, float y) {
        shieldPulses.add(new ShieldPulse(x, y, 0.45f));
    }

    private void spawnDamageNumber(float x, float y, int damage, boolean isCrit, boolean isMiss) {
        if (isMiss) {
            damageNumbers.add(new DamageNumber("MISS!", x, y, 1.1f, Color.valueOf("#d0d0d0"), false));
        } else if (damage == 0) {
            damageNumbers.add(new DamageNumber("BLOCKED!", x, y, 1.1f, Color.valueOf("#4fc3f7"), false));
        } else if (isCrit) {
            damageNumbers.add(new DamageNumber("CRIT! -" + damage, x, y, 1.25f, Color.valueOf("#ff3333"), true));
        } else {
            damageNumbers.add(new DamageNumber("-" + damage, x, y, 1.1f, Color.valueOf("#ffbb33"), false));
        }
    }

    private float getEnemyCenterY() {
        if (isBossBattle()) {
            return ENEMY_DRAW_Y + 70f;
        } else if (currentSnakeAct == 1) {
            return 282f + 53f; // Centered on ~107px tall Forest Green Snake
        } else if (currentSnakeAct == 2) {
            return 282f + 70f; // Centered on ~139px tall Sapphire Horned Serpent
        } else {
            return 282f + 76f; // Centered on ~152px tall Crimson Magma Wyrm
        }
    }

    private void spawnTurtleFortressSparks(float cx, float cy) {
        Color[] fortressColors = new Color[]{
            Color.valueOf("#2e7d32"),
            Color.valueOf("#4caf50"),
            Color.valueOf("#69f0ae"),
            Color.valueOf("#ffd700"),
            Color.valueOf("#fff9c4")
        };
        for (int i = 0; i < 22; i++) {
            float angle = MathUtils.random(0f, 360f) * MathUtils.degRad;
            float speed = MathUtils.random(90f, 240f);
            float vx = MathUtils.cos(angle) * speed;
            float vy = MathUtils.sin(angle) * speed + MathUtils.random(30f, 120f);
            float dur = MathUtils.random(0.40f, 0.70f);
            float size = MathUtils.random(4f, 7f);
            Color c = fortressColors[MathUtils.random(fortressColors.length - 1)];
            hitSparks.add(new HitSpark(cx, cy, vx, vy, dur, size, 220f, c));
        }
    }

    private void spawnScavengeLootSparks(float cx, float cy) {
        Color[] lootColors = new Color[]{
            Color.valueOf("#ffd700"), // Gold
            Color.valueOf("#ffea00"), // Yellow coin
            Color.valueOf("#00e676"), // Emerald ration
            Color.valueOf("#69f0ae"), // Mint heal
            Color.valueOf("#ffffff")  // Sparkle
        };
        for (int i = 0; i < 26; i++) {
            float angle = MathUtils.random(0f, 360f) * MathUtils.degRad;
            float speed = MathUtils.random(120f, 320f);
            float vx = MathUtils.cos(angle) * speed;
            float vy = MathUtils.sin(angle) * speed + MathUtils.random(60f, 180f);
            float dur = MathUtils.random(0.45f, 0.75f);
            float size = MathUtils.random(4f, 7.5f);
            Color c = lootColors[MathUtils.random(lootColors.length - 1)];
            hitSparks.add(new HitSpark(cx, cy, vx, vy, dur, size, 380f, c));
        }
    }

    private void startPlayerSkillAnimation() {
        CharacterClass cls = player.getCharacterClass();
        if (cls == null) {
            battleSystem.onPlayerUseSkill();
            syncUi();
            return;
        }

        activeSkillClass = cls;
        skillPhase1Done = false;
        skillPhase2Done = false;

        switch (cls) {
            case CAT:
                skillAnimDuration = 0.72f;
                skillAnimTimer = skillAnimDuration;
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.ATTACK_SLASH);
                }
                spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "⚡ DOUBLE STRIKE!", Color.valueOf("#00e5ff"), true);
                break;

            case TURTLE:
                skillAnimDuration = 0.65f;
                skillAnimTimer = skillAnimDuration;
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.DEFEND_GUARD);
                }
                spawnShieldPulse(PLAYER_X, CHAR_DRAW_Y + 80f);
                spawnShockwave(PLAYER_X, CHAR_DRAW_Y + 80f, 30f, 130f, Color.valueOf("#2e7d32"), Color.valueOf("#ffd700"), 0.55f);
                spawnShockwave(PLAYER_X, CHAR_DRAW_Y + 80f, 15f, 85f, Color.valueOf("#66bb6a"), Color.valueOf("#ffffff"), 0.45f);
                spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "🛡️ IRON FORTRESS!", Color.valueOf("#69f0ae"), true);
                spawnTurtleFortressSparks(PLAYER_X, CHAR_DRAW_Y + 80f);
                break;

            case FOX:
                skillAnimDuration = 0.65f;
                skillAnimTimer = skillAnimDuration;
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.SCOUT_FLARE);
                }
                spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "🎯 VITAL STRIKE!", Color.valueOf("#ff9100"), true);
                break;

            case RACCOON:
                skillAnimDuration = 0.60f;
                skillAnimTimer = skillAnimDuration;
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.ATTACK_SLASH);
                }
                spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "🎒 SCAVENGE STRIKE!", Color.valueOf("#ffd54f"), true);
                break;
        }
    }

    private void spawnFloatingText(float x, float y, String text, Color color, boolean isCrit) {
        damageNumbers.add(new DamageNumber(text, x, y, 1.15f, color, isCrit));
    }

    private void spawnCelebrationSparks(float cx, float cy) {
        Color[] goldPalette = new Color[]{
            new Color(1f, 0.95f, 0.4f, 1f),
            new Color(1f, 0.8f, 0.2f, 1f),
            new Color(1f, 1f, 1f, 1f),
            new Color(1f, 0.6f, 0.1f, 1f),
            new Color(0.4f, 1f, 0.5f, 1f)
        };
        for (int i = 0; i < 40; i++) {
            float angle = MathUtils.random(0f, 360f) * MathUtils.degRad;
            float speed = MathUtils.random(150f, 520f);
            float vx = MathUtils.cos(angle) * speed;
            float vy = MathUtils.sin(angle) * speed + MathUtils.random(100f, 260f);
            float duration = MathUtils.random(0.6f, 1.2f);
            float size = MathUtils.random(4.5f, 7.5f);
            Color c = goldPalette[MathUtils.random(goldPalette.length - 1)];
            hitSparks.add(new HitSpark(cx + MathUtils.random(-160f, 160f), cy + MathUtils.random(-50f, 50f), vx, vy, duration, size, c));
        }
    }

    @Override
    public void show() {
        if (game.getAudioManager() != null) {
            game.getAudioManager().playMusic(
                isBossBattle() ? AudioManager.MusicTrack.BOSS : AudioManager.MusicTrack.BATTLE);
            if (isDawnWardenBattle() || isPaleShepherdBattle()) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.BOSS_APPEAR);
            }
        }
        batch = new SpriteBatch();
        worldCamera = new OrthographicCamera();
        worldViewport = new ExtendViewport(1920, 1080, worldCamera);
        worldCamera.position.set(950f, 540f, 0f); // Center between the two tiles
        worldCamera.update();
        hudStage = new Stage(new ExtendViewport(1920, 1080));
        Gdx.input.setInputProcessor(hudStage);

        // 1x1 White Texture & Soft Radial Glow Circle Texture
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE); px.fill();
        whitePixelTex = new Texture(px); px.dispose();

        radialGlowTex = createRadialGlowTexture(128);

        // Load Fredoka Fonts (Option 1 pairing matching BoardScreen)
        titleFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 26, Color.WHITE);
        topBannerFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 32, Color.valueOf("#fffdfa"));
        hudHpFont = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 22, Color.valueOf("#a81717"));
        battleLogFont = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 26, Color.WHITE);
        playerTagFont = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 19, Color.WHITE);
        miniFont = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 16, Color.WHITE);
        damageFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 30, Color.WHITE);
        critFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 38, Color.valueOf("#ffe082"));
        modalTitleFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 40, Color.valueOf("#ffd54f"));
        modalSubFont = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 22, Color.valueOf("#fbf2dc"));
        modalRewardFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 30, Color.WHITE);
        modalNarrativeFont = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 26, Color.WHITE);
        modalStatusFont = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 24, Color.WHITE);
        introSpeakerFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 19, Color.valueOf("#ffd54f"));
        introBodyFont = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 21, Color.valueOf("#fff8ea"));
        introBtnFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 18, Color.WHITE);

        Color darkBrown = Color.valueOf("#5a2d0c");
        Color pillBrown = Color.valueOf("#854b25");
        topPauseFont   = UIUtils.createFont("ui/Fredoka-Bold.ttf", 25, Color.WHITE);
        pauseBadgeFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 22, Color.WHITE);
        pauseTitleFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 30, darkBrown);
        pauseSubFont   = UIUtils.createFont("ui/Fredoka-Bold.ttf", 26, darkBrown);
        pauseBodyFont  = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 22, darkBrown);
        pauseBtnFont   = UIUtils.createFont("ui/Fredoka-Bold.ttf", 22, Color.WHITE);
        pauseTipFont   = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 22, Color.valueOf("#54280e"));
        sliderFont     = UIUtils.createFont("ui/Fredoka-Bold.ttf", 24, darkBrown);

        currentSnakeAct = resolveSnakeAct();

        // Register Battle Listeners for Dynamic FX
        battleSystem.setBattleListener((attacker, defender, damage, isCrit, isMiss) -> {
            if (attacker == player) {
                if (game.getRunTracker() != null) {
                    game.getRunTracker().recordSnakeCombat(player, damage, false, isCrit);
                }
                float enemyHitY = getEnemyCenterY();

                // Riposte counter-strike when Turtle's Iron Fortress deflects during enemy turn
                if (player.getCharacterClass() == CharacterClass.TURTLE && battleSystem.getCurrentState() == BattleState.ENEMY_TURN) {
                    spawnShockwave(PLAYER_X, CHAR_DRAW_Y + 80f, 40f, 160f, Color.valueOf("#ffd700"), Color.valueOf("#ffffff"), 0.45f);
                    spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "🛡️ DEFLECTED! 0 DMG", Color.valueOf("#4fc3f7"), true);
                    slashEffects.add(new SlashEffect(ENEMY_X, enemyHitY, 0.35f, SlashStyle.TURTLE_RIPOSTE, true, 0f));
                    spawnTurtleFortressSparks(ENEMY_X, enemyHitY);
                    spawnFloatingText(ENEMY_X, enemyHitY + 110f, "⚡ CRUSHING RIPOSTE!", Color.valueOf("#ffd54f"), true);
                    enemyFlinchTimer = FLINCH_DURATION;
                    spawnDamageNumber(ENEMY_X, enemyHitY + 65f, damage, true, false);
                    triggerScreenShake(18f, 0.35f);
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.BOSS_ROAR);
                    }
                    return;
                }

                // If not in a skill animation, spawn generic sword slash & sparks
                if (skillAnimTimer <= 0f) {
                    spawnSlash(ENEMY_X, enemyHitY, false, isCrit);
                    spawnHitSparks(ENEMY_X, enemyHitY, false, isCrit ? 26 : 16, isCrit);
                }
                enemyFlinchTimer = FLINCH_DURATION;
                spawnDamageNumber(ENEMY_X, enemyHitY + 65f, damage, isCrit, isMiss);
                triggerScreenShake(isCrit ? 15f : 8f, isCrit ? 0.28f : 0.18f);
            } else {
                if (game.getRunTracker() != null) {
                    game.getRunTracker().recordDamageTaken(player, damage);
                }
                spawnSlash(PLAYER_X, CHAR_DRAW_Y + 90f, true, isCrit);
                spawnHitSparks(PLAYER_X, CHAR_DRAW_Y + 90f, true, isCrit ? 26 : 16, isCrit);
                playerFlinchTimer = FLINCH_DURATION;
                spawnDamageNumber(PLAYER_X, CHAR_DRAW_Y + 160f, damage, isCrit, isMiss);
                triggerScreenShake(isCrit ? 16f : 9f, isCrit ? 0.30f : 0.20f);
            }
        });

        battleSystem.setEnemyAttackStarter(() -> {
            enemyLungeTimer = ENEMY_LUNGE_DURATION;
            enemyAttackLanded = false;
        });

        // Load Parallax
        initParallaxSlices();

        // Load Player Animation (Idle pose from walk sheet)
        CharacterClass cls = player.getCharacterClass();
        if (cls != null) {
            Texture sheet = game.getAssetManager().get(Assets.getCharacterWalkSheet(cls));
            TextureRegion[][] grid = TextureRegion.split(sheet, Assets.CHAR_FRAME_SIZE, Assets.CHAR_FRAME_SIZE);
            // Use first frame as idle
            playerIdleAnim = new Animation<>(0.1f, grid[0][0]);
        }

        // Load Enemy Animation
        if (isPaleShepherdBattle()) {
            Texture shepherdSheet = game.getAssetManager().get(Assets.CHAR_PALE_SHEPHERD);
            TextureRegion[][] shepherdGrid = TextureRegion.split(shepherdSheet, 256, 256);
            for (int r = 0; r < shepherdGrid.length; r++) {
                for (TextureRegion frame : shepherdGrid[r]) {
                    if (!frame.isFlipX()) {
                        frame.flip(true, false);
                    }
                }
            }
            enemyIdleAnim = new Animation<>(0.20f, shepherdGrid[0]);
            enemyIdleAnim.setPlayMode(Animation.PlayMode.LOOP);
            enemyAttackAnim = new Animation<>(0.16f, shepherdGrid[1]);
            enemyAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        } else if (isDawnWardenBattle() || isBossBattle()) {
            Texture bossSheet = game.getAssetManager().get(Assets.BOSS_DAWN_WARDEN);
            TextureRegion[][] bossGrid = TextureRegion.split(bossSheet, 344, 384);
            enemyIdleAnim = new Animation<>(0.22f, bossGrid[0]);
            enemyIdleAnim.setPlayMode(Animation.PlayMode.LOOP);
            enemyAttackAnim = new Animation<>(0.18f, bossGrid[1]);
            enemyAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        } else {
            // Act-stratified serpent combat: Act I (Green Snake), Act II (Sapphire), Act III (Crimson Magma)
            int act = currentSnakeAct;

            AssetDescriptor<Texture> snakeAsset = Assets.getSnakeTextureForAct(act);
            Texture snakeSheet = game.getAssetManager().get(snakeAsset);
            TextureRegion[][] snakeGrid = TextureRegion.split(snakeSheet, 128, 128);
            TextureRegion[] frames = snakeGrid[0];
            for (TextureRegion frame : frames) {
                if (frame.isFlipX()) {
                    frame.flip(true, false); // Ensure facing left towards hero
                }
            }
            // Idle animation: coiled breathing ready stance (frames 0 & 1)
            enemyIdleAnim = new Animation<>(0.26f, new TextureRegion[]{frames[0], frames[1]});
            enemyIdleAnim.setPlayMode(Animation.PlayMode.LOOP);

            // Attack animation: strike & fangs/venom sequence (frames 2, 3, 4, 5)
            enemyAttackAnim = new Animation<>(0.13f, new TextureRegion[]{frames[2], frames[3], frames[4], frames[5]});
            enemyAttackAnim.setPlayMode(Animation.PlayMode.NORMAL);
        }

        // Start Battle
        battleSystem.startBattle(player, enemy);
        isIntroDialogueActive = isDawnWardenBattle();
        introBeatIndex = 0;
        scrambleAnnounced = false;
        bossEntranceActive = isDawnWardenBattle();
        bossEntranceTimer = 0f;
        bossLanded = !isDawnWardenBattle();
        bossEnraged = false;
        bossVeilAlpha = isDawnWardenBattle() ? 0.65f : 0f;
        bossFlashAlpha = 0f;

        // Build UI
        buildTopBanner();
        buildPauseButton();
        buildBattleLog();
        buildHealthPills();
        buildActionButtons();
        buildBossIntroOverlay();
        buildItemPopup();
        buildContinueOverlay();
        buildPauseAndSettingsModals(darkBrown, pillBrown);
        syncUi();

    }

    private void initParallaxSlices() {
        Texture bg1 = game.getAssetManager().get(Assets.ACT1_BG);
        Texture bg2 = game.getAssetManager().get(Assets.ACT2_BG);
        Texture bg3 = game.getAssetManager().get(Assets.ACT3_BG);
        act1Far = new TextureRegion(bg1, 7, 0, 1010, 264);
        act1Mid = new TextureRegion(bg1, 7, 268, 1010, 188);
        act1Ground = new TextureRegion(bg1, 7, 464, 1010, 108);
        act2Far = new TextureRegion(bg2, 8, 0, 1004, 264);
        act2Mid = new TextureRegion(bg2, 8, 268, 998, 188);
        act2Ground = new TextureRegion(bg2, 10, 464, 1002, 110);
        act3Far = new TextureRegion(bg3, 7, 0, 1006, 264);
        act3Mid = new TextureRegion(bg3, 7, 268, 1004, 188);
        act3Ground = new TextureRegion(bg3, 6, 464, 1008, 110);
    }

    private Texture createRadialGlowTexture(int size) {
        Pixmap glow = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        glow.setBlending(Pixmap.Blending.None);
        float half = size * 0.5f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x + 0.5f - half) / half;
                float dy = (y + 0.5f - half) / half;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float falloff = Math.max(0f, 1f - dist);
                float alpha = falloff * falloff * falloff; // Smooth cubic feathering, zero box edges
                glow.setColor(1f, 1f, 1f, alpha);
                glow.drawPixel(x, y);
            }
        }
        Texture tex = new Texture(glow);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        glow.dispose();
        return tex;
    }

    private void buildTopBanner() {
        int act;
        String bannerText;
        Color bannerBgColor;
        if (isPaleShepherdBattle()) {
            act = (player.getPosition() <= 30) ? 1 : ((player.getPosition() <= 70) ? 2 : 3);
            bannerText = "THE PALE SHEPHERD";
            bannerBgColor = Color.valueOf("#3b263b");
        } else if (isDawnWardenBattle()) {
            act = 3;
            bannerText = "SUMMIT TRIAL: THE DAWN WARDEN!";
            bannerBgColor = Color.valueOf("#b86518");
        } else {
            act = (snakeTile != null) ? ((snakeTile.getPosition() <= 30) ? 1 : ((snakeTile.getPosition() <= 70) ? 2 : 3)) : 1;
            String actRoman = (act == 1) ? "ACT I" : ((act == 2) ? "ACT II" : "ACT III");
            bannerText = actRoman + " AMBUSH: " + (enemy != null ? enemy.getEnemyType().toUpperCase() : "SNAKE") + "!";
            bannerBgColor = Color.valueOf("#854b25");
        }
        topBannerLabel = new Label(bannerText,
            new Label.LabelStyle(topBannerFont, Color.valueOf("#fffdfa")));
        topBannerLabel.setAlignment(Align.center);
        topBannerBox = new Table();
        topBannerBox.setBackground(UIUtils.createRoundedDrawable(920, 70, 20, bannerBgColor));
        topBannerBox.add(topBannerLabel).pad(8, 36, 8, 36);
        topBannerWrap = new Table();
        topBannerWrap.setFillParent(true);
        topBannerWrap.top().padTop(22);
        topBannerWrap.add(topBannerBox).center();
        hudStage.addActor(topBannerWrap);
    }

    /**
     * Top-Right Pause Button (|| PAUSE) matching BoardScreen proportions and styling.
     */
    private void buildPauseButton() {
        Color pillBrown = Color.valueOf("#854b25");
        Button.ButtonStyle pauseStyle = new Button.ButtonStyle();
        pauseStyle.up = UIUtils.createRoundedDrawable(164, 70, 18, pillBrown);
        pauseStyle.over = UIUtils.createRoundedDrawable(164, 70, 18, Color.valueOf("#9c572b"));

        pauseBtn = new Button(pauseStyle);
        UIUtils.addScaleHoverEffect(pauseBtn);

        Label pauseLabel = new Label("|| PAUSE", new Label.LabelStyle(topPauseFont, Color.WHITE));
        pauseBtn.add(pauseLabel).center();

        pauseBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                if (!isPaused) {
                    pauseGame();
                } else {
                    resumeGame();
                }
            }
        });

        Table container = new Table();
        container.setFillParent(true);
        container.top().right().padTop(22).padRight(35);
        container.add(pauseBtn).size(164, 70);

        hudStage.addActor(container);
    }

    private void buildBattleLog() {
        String initialLog = isPaleShepherdBattle() ? "The Pale Shepherd raises his crook in quiet contemplation." : "The battle begins!";
        battleLogLabel = new Label(initialLog, new Label.LabelStyle(battleLogFont, Color.valueOf("#fffdfa")));
        battleLogLabel.setAlignment(Align.center);
        battleLogLabel.setWrap(true);
        Table logBox = new Table();
        Color bgLeather = Color.valueOf("#2d1305ea"); // Rich translucent mahogany leather
        Color borderGold = Color.valueOf("#c89259");  // Warm leather border
        logBox.setBackground(UIUtils.createRoundedDrawable(880, 80, 16, bgLeather, borderGold, 3));
        logBox.add(battleLogLabel).pad(10, 30, 10, 30).width(820).center();
        battleLogContainer = new Table();
        battleLogContainer.setFillParent(true);
        battleLogContainer.top().padTop(110); // Raised much higher, directly below top banner
        battleLogContainer.add(logBox).center();
        hudStage.addActor(battleLogContainer);
    }

    private void buildHealthPills() {
        Color pillLeather = Color.valueOf("#c89259");
        Color pillBorder  = Color.valueOf("#854b25");

        // Player HP (Bottom Left)
        int maxHp = player.getMaxHealth();
        playerHpLabel = new Label("HP: " + player.getHealth() + " / " + maxHp, new Label.LabelStyle(hudHpFont, Color.valueOf("#a81717")));
        Table pPill = new Table();
        pPill.setBackground(UIUtils.createRoundedDrawable(340, 75, 12, pillLeather, pillBorder, 3));
        Table pBox = new Table();
        Label pName = new Label(player.getName().toUpperCase(), new Label.LabelStyle(playerTagFont, Color.valueOf("#361704")));
        pBox.add(pName).left().row();
        pBox.add(playerHpLabel).left();
        pPill.add(pBox).pad(8, 20, 8, 20);

        playerHpWrap = new Table();
        playerHpWrap.setFillParent(true);
        playerHpWrap.bottom().left().padBottom(160).padLeft(50);
        playerHpWrap.add(pPill).size(340, 75);
        hudStage.addActor(playerHpWrap);

        // Enemy HP (Bottom Right - Mirroring Player HP)
        enemyHpLabel = new Label("HP: " + enemy.getHealth() + " / " + enemyMaxHp, new Label.LabelStyle(hudHpFont, Color.valueOf("#a81717")));
        Table ePill = new Table();
        ePill.setBackground(UIUtils.createRoundedDrawable(340, 75, 12, pillLeather, pillBorder, 3));
        Table eBox = new Table();
        enemyNameLabel = new Label(enemy.getEnemyType().toUpperCase(), new Label.LabelStyle(playerTagFont, Color.valueOf("#361704")));
        eBox.add(enemyNameLabel).right().row();
        eBox.add(enemyHpLabel).right();
        ePill.add(eBox).pad(8, 20, 8, 20);

        enemyHpWrap = new Table();
        enemyHpWrap.setFillParent(true);
        enemyHpWrap.bottom().right().padBottom(160).padRight(50);
        enemyHpWrap.add(ePill).size(340, 75);
        hudStage.addActor(enemyHpWrap);
    }

    private void buildActionButtons() {
        Color pillBrown = Color.valueOf("#854b25");
        Color leather = Color.valueOf("#c89259");
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = UIUtils.createRoundedDrawable(180, 100, 15, leather, pillBrown, 3);
        style.over = UIUtils.createRoundedDrawable(180, 100, 15, Color.valueOf("#d8a46f"), pillBrown, 3);
        style.font = titleFont;
        style.fontColor = Color.WHITE;
        btnAttack = new TextButton("ATTACK", style);
        btnDefend = new TextButton("DEFEND", style);
        btnItem = new TextButton("ITEM", style);
        btnEscape = new TextButton("ESCAPE", style);

        String skillName = player.getCharacterClass() != null ? player.getCharacterClass().getName().toUpperCase() + " SKILL" : "SKILL";
        btnSkill = new TextButton(skillName, style);

        // Attack Listener
        btnAttack.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (battleSystem.getCurrentState() == BattleState.PLAYER_TURN && playerLungeTimer <= 0f && enemyLungeTimer <= 0f && skillAnimTimer <= 0f) {
                    if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.ATTACK_SLASH);
                    playerLungeTimer = PLAYER_LUNGE_DURATION;
                    playerAttackLanded = false;
                    syncUi();
                }
            }
        });

        // Defend Listener
        btnDefend.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (battleSystem.getCurrentState() == BattleState.PLAYER_TURN && playerLungeTimer <= 0f && enemyLungeTimer <= 0f && skillAnimTimer <= 0f) {
                    if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.DEFEND_GUARD);
                    spawnShieldPulse(PLAYER_X, CHAR_DRAW_Y + 80f);
                    spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "GUARD", Color.valueOf("#ffd54f"), false);
                    battleSystem.onPlayerDefend();
                    syncUi();
                }
            }
        });

        // Skill Listener
        btnSkill.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (battleSystem.getCurrentState() == BattleState.PLAYER_TURN && playerLungeTimer <= 0f && enemyLungeTimer <= 0f && skillAnimTimer <= 0f) {
                    if (!player.canUseSkill()) {
                        if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                        spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "COOLDOWN (" + player.getSkillCooldown() + ")", Color.valueOf("#ff7043"), false);
                        return;
                    }
                    startPlayerSkillAnimation();
                    syncUi();
                }
            }
        });

        // Item Listener
        btnItem.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (battleSystem.getCurrentState() == BattleState.PLAYER_TURN && playerLungeTimer <= 0f && enemyLungeTimer <= 0f && skillAnimTimer <= 0f) {
                    if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    if (!itemPopup.isVisible()) refreshItemPopup();
                    itemPopup.setVisible(!itemPopup.isVisible());
                }
            }
        });

        // Escape Listener
        btnEscape.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (battleSystem.getCurrentState() == BattleState.PLAYER_TURN && playerLungeTimer <= 0f && enemyLungeTimer <= 0f && skillAnimTimer <= 0f) {
                    if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    battleSystem.onPlayerEscape();
                    syncUi();
                }
            }
        });

        // FIXED: Single layout block - no duplicates
        bottomBar = new Table();
        bottomBar.setFillParent(true);
        bottomBar.bottom().padBottom(30);

        if (isBossBattle()) {
            // Boss battles: 4 buttons (no escape)
            bottomBar.add(btnAttack).size(180, 100).padRight(20);
            bottomBar.add(btnDefend).size(180, 100).padRight(20);
            bottomBar.add(btnSkill).size(180, 100).padRight(20);
            bottomBar.add(btnItem).size(180, 100);
        } else {
            // Regular battles: 5 buttons
            bottomBar.add(btnAttack).size(180, 100).padRight(10);
            bottomBar.add(btnDefend).size(180, 100).padRight(10);
            bottomBar.add(btnSkill).size(180, 100).padRight(10);
            bottomBar.add(btnItem).size(180, 100).padRight(10);
            bottomBar.add(btnEscape).size(180, 100);
        }

        hudStage.addActor(bottomBar);
    }
    /**
     * Builds the in-arena dramatic intro confrontation for Boss trials (The Dawn Warden and The Pale Shepherd).
     * Sits neatly in the bottom center space between the HP pills, below both fighters.
     */
    private void buildBossIntroOverlay() {
        bossIntroTable = new Table();
        bossIntroTable.setFillParent(true);
        bossIntroTable.bottom().padBottom(22);
        bossIntroTable.setVisible(false);

        if (!isDawnWardenBattle() && !isPaleShepherdBattle()) {
            hudStage.addActor(bossIntroTable);
            return;
        }

        Table card = new Table();
        if (isPaleShepherdBattle()) {
            card.setBackground(UIUtils.createRoundedDrawable(
                1660, 190, 18, Color.valueOf("#180609f6"), Color.valueOf("#a62828"), 3));
        } else {
            card.setBackground(UIUtils.createRoundedDrawable(
                1660, 190, 18, Color.valueOf("#261105f4"), Color.valueOf("#c89259"), 3));
        }
        card.pad(14, 38, 16, 38);

        // Header Row: Speaker Label + Skip
        Table headerRow = new Table();
        Label speakerLbl;
        if (isPaleShepherdBattle()) {
            speakerLbl = new Label("THE PALE SHEPHERD  |  A QUIET WATCHER",
                new Label.LabelStyle(introSpeakerFont, Color.valueOf("#ffd54f")));
        } else {
            speakerLbl = new Label("THE DAWN WARDEN  |  GUARDIAN OF THE GATE",
                new Label.LabelStyle(introSpeakerFont, Color.valueOf("#ffd54f")));
        }
        speakerLbl.setAlignment(Align.left);

        TextButton.TextButtonStyle skipStyle = new TextButton.TextButtonStyle();
        skipStyle.font = miniFont;
        skipStyle.fontColor = isPaleShepherdBattle() ? Color.valueOf("#cf9696") : Color.valueOf("#cfb896");
        skipStyle.overFontColor = Color.WHITE;
        introSkipBtn = new TextButton("SKIP INTRO", skipStyle);
        introSkipBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                if (isEpilogueDialogueActive) {
                    finishDawnWardenEpilogue();
                } else if (isPaleShepherdBattle()) {
                    finishPaleShepherdIntro(0, 0, "The Pale Shepherd raises his crook in quiet contemplation.");
                } else {
                    finishDawnWardenIntro(0, 0, "The Dawn Warden descends before you to test your spirit!");
                }
            }
        });

        headerRow.add(speakerLbl).left().expandX();
        headerRow.add(introSkipBtn).right();
        card.add(headerRow).width(1580).padBottom(6).row();

        // Dialogue Body
        introDialogueLabel = new Label("", new Label.LabelStyle(introBodyFont, Color.valueOf("#fff8ea")));
        introDialogueLabel.setAlignment(Align.center);
        introDialogueLabel.setWrap(true);
        card.add(introDialogueLabel).width(1580).minHeight(64).center().padBottom(10).row();

        // Action Row (dynamically populated per beat)
        introActionsRow = new Table();
        card.add(introActionsRow).width(1580).center();

        TextButton.TextButtonStyle advStyle = new TextButton.TextButtonStyle();
        if (isPaleShepherdBattle()) {
            advStyle.up = UIUtils.createRoundedDrawable(280, 46, 12, Color.valueOf("#691515"), Color.valueOf("#b32b2b"), 2);
            advStyle.over = UIUtils.createRoundedDrawable(280, 46, 12, Color.valueOf("#8f1d1d"), Color.valueOf("#ff5252"), 2);
        } else {
            advStyle.up = UIUtils.createRoundedDrawable(280, 46, 12, Color.valueOf("#854b25"), Color.valueOf("#c89259"), 2);
            advStyle.over = UIUtils.createRoundedDrawable(280, 46, 12, Color.valueOf("#a66032"), Color.valueOf("#ffd54f"), 2);
        }
        advStyle.font = introBtnFont;
        advStyle.fontColor = Color.WHITE;
        introAdvanceBtn = new TextButton("CONTINUE  (1/3)", advStyle);
        UIUtils.addScaleHoverEffect(introAdvanceBtn);
        introAdvanceBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                if (isEpilogueDialogueActive) {
                    epilogueBeatIndex++;
                    refreshDawnWardenEpilogueBeat();
                } else {
                    introBeatIndex++;
                    if (isDawnWardenBattle()) {
                        refreshDawnWardenIntroBeat();
                    } else if (isPaleShepherdBattle()) {
                        refreshPaleShepherdIntroBeat();
                    }
                }
            }
        });

        bossIntroTable.add(card).width(1660).minHeight(186).center();
        hudStage.addActor(bossIntroTable);
        if (isDawnWardenBattle()) {
            refreshDawnWardenIntroBeat();
        } else if (isPaleShepherdBattle()) {
            refreshPaleShepherdIntroBeat();
        }
    }

    private String getPaleShepherdClassReflectiveLine() {
        CharacterClass cls = (player != null) ? player.getCharacterClass() : null;
        if (cls == null) {
            return "\"Every step was borrowed time. Let go of the climb... and rest in the dark.\"";
        }
        switch (cls) {
            case CAT:
                return "\"Nine lives, they whispered of your kind. Did you squander eight to end here in the cold?\"";
            case TURTLE:
                return "\"Heavy iron and ancient stone... yet the hardest shell turns brittle beneath my scythe.\"";
            case FOX:
                return "\"You danced past every snare and outsmarted mortal hunters. But none bargain with the harvest.\"";
            case RACCOON:
                return "\"A satchel heavy with stolen trinkets. Leave them behind, scavenger... you will not need them where we go.\"";
            default:
                return "\"Every step was borrowed time. Let go of the climb... and rest in the dark.\"";
        }
    }

    private void refreshPaleShepherdIntroBeat() {
        if (introDialogueLabel == null || introActionsRow == null) return;
        introActionsRow.clear();

        if (introBeatIndex == 0) {
            introDialogueLabel.setText(
                "A suffocating frost grips the air as obsidian mist coils around your paws. "
                + "\"You have run far along these stone ribs, little soul... yet every road carved in this mountain leads only to me.\"");
            introAdvanceBtn.setText("CONTINUE  (1/3)");
            introActionsRow.add(introAdvanceBtn).size(260, 46).center();
        } else if (introBeatIndex == 1) {
            introDialogueLabel.setText(getPaleShepherdClassReflectiveLine());
            introAdvanceBtn.setText("CONFRONT  (2/3)");
            introActionsRow.add(introAdvanceBtn).size(260, 46).center();
        } else {
            introDialogueLabel.setText("The golden scythe hums with mortal finality. How do you meet the inevitable?");

            TextButton.TextButtonStyle atkChoiceStyle = new TextButton.TextButtonStyle();
            atkChoiceStyle.up = UIUtils.createRoundedDrawable(580, 50, 12, Color.valueOf("#7a1a1a"), Color.valueOf("#d45d5d"), 2);
            atkChoiceStyle.over = UIUtils.createRoundedDrawable(580, 50, 12, Color.valueOf("#9e2424"), Color.valueOf("#ff7b7b"), 2);
            atkChoiceStyle.font = introBtnFont;
            atkChoiceStyle.fontColor = Color.valueOf("#fff8ea");

            TextButton.TextButtonStyle defChoiceStyle = new TextButton.TextButtonStyle();
            defChoiceStyle.up = UIUtils.createRoundedDrawable(580, 50, 12, Color.valueOf("#261c3b"), Color.valueOf("#6e559e"), 2);
            defChoiceStyle.over = UIUtils.createRoundedDrawable(580, 50, 12, Color.valueOf("#3b2a5c"), Color.valueOf("#9174cb"), 2);
            defChoiceStyle.font = introBtnFont;
            defChoiceStyle.fontColor = Color.valueOf("#fff8ea");

            TextButton atkChoiceBtn = new TextButton("\"NOT TODAY, REAPER!\"  (+2 ATK)", atkChoiceStyle);
            atkChoiceBtn.getLabel().setWrap(true);
            atkChoiceBtn.getLabel().setAlignment(Align.center);
            UIUtils.addScaleHoverEffect(atkChoiceBtn);
            atkChoiceBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    }
                    finishPaleShepherdIntro(2, 0, "You defy Death itself with razor-sharp fury! (+2 ATK on Turn 1)");
                }
            });

            TextButton defChoiceBtn = new TextButton("\"I REFUSE TO FALL!\"  (+3 DEF)", defChoiceStyle);
            defChoiceBtn.getLabel().setWrap(true);
            defChoiceBtn.getLabel().setAlignment(Align.center);
            UIUtils.addScaleHoverEffect(defChoiceBtn);
            defChoiceBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.DEFEND_GUARD);
                    }
                    finishPaleShepherdIntro(0, 3, "You brace your spirit against the reaper's mortal chill! (+3 DEF on Turn 1)");
                }
            });

            introActionsRow.add(atkChoiceBtn).size(580, 50).padRight(28);
            introActionsRow.add(defChoiceBtn).size(580, 50);
        }
    }

    private void finishPaleShepherdIntro(int tempAtkBonus, int tempDefBonus, String startLog) {
        isIntroDialogueActive = false;
        if (bossIntroTable != null) {
            bossIntroTable.setVisible(false);
        }
        if (tempAtkBonus > 0) {
            player.addBattleAttack(tempAtkBonus);
            spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "DEFIANCE: +" + tempAtkBonus + " ATK", Color.valueOf("#ff5252"), false);
        }
        if (tempDefBonus > 0) {
            player.addBattleDefense(tempDefBonus);
            spawnShieldPulse(PLAYER_X, CHAR_DRAW_Y + 80f);
            spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "STEEL WILL: +" + tempDefBonus + " DEF", Color.valueOf("#80d8ff"), false);
        }
        battleSystem.setBattleLog(startLog);
        syncUi();
    }

    private String getClassReflectiveLine() {
        CharacterClass cls = (player != null) ? player.getCharacterClass() : null;
        if (cls == null) {
            return "No step upward can outrun a heart that has already grown still.";
        }
        switch (cls) {
            case CAT:
                return "Even nimble paws cannot outrun a heart that has already grown still.";
            case TURTLE:
                return "Even an Iron Guardian's shell could not lock that final hour out.";
            case FOX:
                return "No clever bargain can unwrite the mortal life you left behind.";
            case RACCOON:
                return "No salvaged relic can piece back a breath that already ended.";
            default:
                return "No step upward can outrun a heart that has already grown still.";
        }
    }

    private void refreshDawnWardenIntroBeat() {
        if (introDialogueLabel == null || introActionsRow == null) return;
        introActionsRow.clear();

        if (introBeatIndex == 0) {
            introDialogueLabel.setText(
                "So far you have climbed, little soul - past fang, frost, and abyss. " + getClassReflectiveLine());
            introAdvanceBtn.setText("CONTINUE  (1/3)");
            introActionsRow.add(introAdvanceBtn).size(260, 46).center();
        } else if (introBeatIndex == 1) {
            introDialogueLabel.setText(
                "The Pale Shepherd does not hunt you out of cruelty - he waits for a spirit still clinging to yesterday. Why, then, do you still claw at the dawn?");
            introAdvanceBtn.setText("ANSWER  (2/3)");
            introActionsRow.add(introAdvanceBtn).size(260, 46).center();
        } else {
            introDialogueLabel.setText("Speak your resolve before the golden gates ignite:");

            TextButton.TextButtonStyle atkChoiceStyle = new TextButton.TextButtonStyle();
            atkChoiceStyle.up = UIUtils.createRoundedDrawable(580, 50, 12, Color.valueOf("#8c2a24"), Color.valueOf("#d46e66"), 2);
            atkChoiceStyle.over = UIUtils.createRoundedDrawable(580, 50, 12, Color.valueOf("#a8342d"), Color.valueOf("#f08a82"), 2);
            atkChoiceStyle.font = introBtnFont;
            atkChoiceStyle.fontColor = Color.valueOf("#fff8ea");

            TextButton.TextButtonStyle defChoiceStyle = new TextButton.TextButtonStyle();
            defChoiceStyle.up = UIUtils.createRoundedDrawable(580, 50, 12, Color.valueOf("#2f5778"), Color.valueOf("#759ec2"), 2);
            defChoiceStyle.over = UIUtils.createRoundedDrawable(580, 50, 12, Color.valueOf("#3b6b94"), Color.valueOf("#96bfe3"), 2);
            defChoiceStyle.font = introBtnFont;
            defChoiceStyle.fontColor = Color.valueOf("#fff8ea");

            TextButton atkChoiceBtn = new TextButton("\"MY STORY ISN'T FINISHED YET!\"  (+2 ATK)", atkChoiceStyle);
            atkChoiceBtn.getLabel().setWrap(true);
            atkChoiceBtn.getLabel().setAlignment(Align.center);
            UIUtils.addScaleHoverEffect(atkChoiceBtn);
            atkChoiceBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    }
                    finishDawnWardenIntro(2, 0, "You answer with fierce resolve! (+2 ATK on Turn 1)");
                }
            });

            TextButton defChoiceBtn = new TextButton("\"TEST MY SCARS, GUARDIAN!\"  (+3 DEF)", defChoiceStyle);
            defChoiceBtn.getLabel().setWrap(true);
            defChoiceBtn.getLabel().setAlignment(Align.center);
            UIUtils.addScaleHoverEffect(defChoiceBtn);
            defChoiceBtn.addListener(new ChangeListener() {
                @Override public void changed(ChangeEvent event, Actor actor) {
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.DEFEND_GUARD);
                    }
                    finishDawnWardenIntro(0, 3, "You brace against the solar fire! (+3 DEF on Turn 1)");
                }
            });

            introActionsRow.add(atkChoiceBtn).size(580, 50).padRight(28);
            introActionsRow.add(defChoiceBtn).size(580, 50);
        }
    }

    private void finishDawnWardenIntro(int tempAtkBonus, int tempDefBonus, String startLog) {
        isIntroDialogueActive = false;
        if (bossIntroTable != null) {
            bossIntroTable.setVisible(false);
        }
        if (tempAtkBonus > 0) {
            player.addBattleAttack(tempAtkBonus);
            spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "RESOLVE: +" + tempAtkBonus + " ATK", Color.valueOf("#ffd54f"), false);
        }
        if (tempDefBonus > 0) {
            player.addBattleDefense(tempDefBonus);
            spawnShieldPulse(PLAYER_X, CHAR_DRAW_Y + 80f);
            spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "STEADFAST: +" + tempDefBonus + " DEF", Color.valueOf("#80d8ff"), false);
        }
        battleSystem.setBattleLog(startLog);
        syncUi();
    }

    private void startDawnWardenEpilogue(boolean victory) {
        isEpilogueDialogueActive = true;
        isEpilogueVictory = victory;
        epilogueBeatIndex = 0;

        if (victory) {
            bossEnraged = false;
            if (topBannerLabel != null) {
                topBannerLabel.setText("THE DAWN WARDEN BOWS BEFORE YOU");
            }
            if (topBannerBox != null) {
                topBannerBox.setBackground(UIUtils.createRoundedDrawable(
                    920, 70, 20, Color.valueOf("#8f5916"), Color.valueOf("#ffd54f"), 3));
            }
        } else {
            if (topBannerLabel != null) {
                topBannerLabel.setText("THE DAWN WARDEN STEADIES YOUR FALL");
            }
        }

        if (introSkipBtn != null) {
            introSkipBtn.setText("SKIP");
        }
        refreshDawnWardenEpilogueBeat();
        if (bossIntroTable != null) {
            bossIntroTable.setVisible(true);
        }
    }

    private String getWardenVictoryRecognizeLine() {
        CharacterClass cls = (player != null) ? player.getCharacterClass() : null;
        if (cls == null) {
            return "\"Enough, brave soul... You shattered even the second sun. Not out of blind defiance - your heart still burns for those waiting beyond the gate.\"";
        }
        switch (cls) {
            case CAT:
                return "\"Enough, little prowler... Even with nine scars upon your fur, you refused to stay asleep. Someone still waits for you beyond this gate.\"";
            case TURTLE:
                return "\"Enough, iron guardian... Your shell cracked, yet your resolve outlasted stone and flame. Not even death could make you lay down what you chose to protect.\"";
            case FOX:
                return "\"Enough, clever trickster... No bargain or sleight won this trial - only a heart that refused to trade away tomorrow.\"";
            case RACCOON:
                return "\"Enough, weary scavenger... Every relic in your satchel weighed far less than the breath you fought so hard to reclaim.\"";
            default:
                return "\"Enough, brave soul... You shattered even the second sun. Your heart still burns for those waiting beyond the gate.\"";
        }
    }

    private void refreshDawnWardenEpilogueBeat() {
        if (introDialogueLabel == null || introActionsRow == null) return;
        introActionsRow.clear();

        if (isEpilogueVictory) {
            if (epilogueBeatIndex == 0) {
                introDialogueLabel.setText(getWardenVictoryRecognizeLine());
                introAdvanceBtn.setText("CONTINUE  (1/2)");
                introActionsRow.add(introAdvanceBtn).size(280, 46).center();
            } else {
                introDialogueLabel.setText(
                    "\"Go now. The Pale Shepherd will understand. Claim the morning the mountain owed you - and step back into the living dawn.\"");

                TextButton.TextButtonStyle gateStyle = new TextButton.TextButtonStyle();
                gateStyle.up = UIUtils.createRoundedDrawable(380, 50, 12, Color.valueOf("#2e7d32"), Color.valueOf("#ffd54f"), 2);
                gateStyle.over = UIUtils.createRoundedDrawable(380, 50, 12, Color.valueOf("#388e3c"), Color.WHITE, 2);
                gateStyle.font = introBtnFont;
                gateStyle.fontColor = Color.WHITE;

                TextButton gateBtn = new TextButton("STEP THROUGH THE GATE", gateStyle);
                UIUtils.addScaleHoverEffect(gateBtn);
                gateBtn.addListener(new ChangeListener() {
                    @Override public void changed(ChangeEvent event, Actor actor) {
                        if (game.getAudioManager() != null) {
                            game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                        }
                        finishDawnWardenEpilogue();
                    }
                });
                introActionsRow.add(gateBtn).size(380, 50).center();
            }
        } else {
            if (epilogueBeatIndex == 0) {
                introDialogueLabel.setText(
                    "\"Be still, wanderer... Your steps have grown heavy, and your spirit is running cold. Even a flame this fierce was never meant to burn alone forever.\"");
                introAdvanceBtn.setText("CONTINUE  (1/2)");
                introActionsRow.add(introAdvanceBtn).size(280, 46).center();
            } else {
                boolean hasSurvivors = game.getPlayers().size() > 1 && !game.isPartyWiped();
                if (hasSurvivors) {
                    introDialogueLabel.setText(
                        "\"Rest gently at the summit. Your companions still walk the stones below - let your lantern keep their path warm until dawn.\"");
                } else {
                    introDialogueLabel.setText(
                        "\"The climb does not have to hurt anymore. Rest beneath the golden eaves - there is no shame in finally laying down the weight you carried so far.\"");
                }

                TextButton.TextButtonStyle restStyle = new TextButton.TextButtonStyle();
                restStyle.up = UIUtils.createRoundedDrawable(360, 50, 12, Color.valueOf("#5c3518"), Color.valueOf("#c89259"), 2);
                restStyle.over = UIUtils.createRoundedDrawable(360, 50, 12, Color.valueOf("#7a4823"), Color.valueOf("#ffd54f"), 2);
                restStyle.font = introBtnFont;
                restStyle.fontColor = Color.valueOf("#fff8ea");

                TextButton restBtn = new TextButton(hasSurvivors ? "REST AT THE SUMMIT" : "CLOSE YOUR EYES", restStyle);
                UIUtils.addScaleHoverEffect(restBtn);
                restBtn.addListener(new ChangeListener() {
                    @Override public void changed(ChangeEvent event, Actor actor) {
                        if (game.getAudioManager() != null) {
                            game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                        }
                        finishDawnWardenEpilogue();
                    }
                });
                introActionsRow.add(restBtn).size(360, 50).center();
            }
        }
    }

    private void finishDawnWardenEpilogue() {
        isEpilogueDialogueActive = false;
        if (bossIntroTable != null) {
            bossIntroTable.setVisible(false);
        }
        if (pauseBtn != null) {
            pauseBtn.setVisible(false);
        }
        if (isEpilogueVictory) {
            if (topBannerLabel != null) {
                topBannerLabel.setText("SUMMIT CONQUERED!");
            }
            spawnShockwave(ENEMY_X, ENEMY_DRAW_Y + 120f, 70f, 310f, Color.valueOf("#ffd54f"), Color.WHITE, 0.70f);
            spawnCelebrationSparks(960f, 540f);
            if (game.getAudioManager() != null) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.DAWN_REBIRTH);
            }
        } else {
            if (topBannerLabel != null) {
                topBannerLabel.setText("REST AT THE SUMMIT");
            }
            if (game.getAudioManager() != null) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.PLAYER_HURT, 0.45f);
            }
        }
        if (continueTable != null) {
            continueTable.setVisible(true);
        }
    }

    private void buildContinueOverlay() {
        continueTable = new Table();
        continueTable.setFillParent(true);
        continueTable.setBackground(UIUtils.createRoundedDrawable(1920, 1080, 0, new Color(0f, 0f, 0f, 0.60f)));
        continueTable.setVisible(false);

        // Center Modal Dialog Card
        victoryCard = new Table();
        Color bgMahogany = Color.valueOf("#261105fa");
        Color borderGold = Color.valueOf("#c89259");
        victoryCard.setBackground(UIUtils.createRoundedDrawable(800, 540, 22, bgMahogany, borderGold, 4));
        victoryCard.pad(24, 34, 24, 34);

        // 1. Header Title
        modalTitleLabel = new Label("VICTORY!", new Label.LabelStyle(modalTitleFont, Color.valueOf("#ffd54f")));
        modalTitleLabel.setAlignment(Align.center);
        victoryCard.add(modalTitleLabel).center().padBottom(4).row();

        // 2. Subtitle
        modalSubtitleLabel = new Label("THE ENEMY HAS BEEN DEFEATED", new Label.LabelStyle(modalSubFont, Color.valueOf("#fbf2dc")));
        modalSubtitleLabel.setAlignment(Align.center);
        modalSubtitleLabel.setWrap(true);
        victoryCard.add(modalSubtitleLabel).width(720).center().padBottom(14).row();

        // 3. Gold Accent Divider
        Table divider = new Table();
        divider.setBackground(UIUtils.createRoundedDrawable(720, 2, 1, Color.valueOf("#c89259b0")));
        victoryCard.add(divider).size(720, 2).padBottom(16).center().row();

        // 4. Reward Inset Box
        Table rewardBox = new Table();
        rewardBox.setBackground(UIUtils.createRoundedDrawable(720, 230, 14, Color.valueOf("#150802f0"), Color.valueOf("#854b25"), 2));
        rewardBox.pad(16, 24, 16, 24);
        rewardBox.center();

        rewardExpLabel = new Label("EXP Gained: +16 EXP", new Label.LabelStyle(modalRewardFont, Color.WHITE));
        rewardExpLabel.setAlignment(Align.center);
        rewardExpLabel.setWrap(true);

        rewardLootLabel = new Label("Spoils of War: Minor Dagger", new Label.LabelStyle(modalNarrativeFont, Color.WHITE));
        rewardLootLabel.setAlignment(Align.center);
        rewardLootLabel.setWrap(true);

        rewardStatusLabel = new Label("Hero Health: Shelly (HP: 67 / 90)", new Label.LabelStyle(modalStatusFont, Color.WHITE));
        rewardStatusLabel.setAlignment(Align.center);
        rewardStatusLabel.setWrap(true);

        rewardBox.add(rewardExpLabel).width(670).center().padBottom(12).row();
        rewardBox.add(rewardLootLabel).width(670).center().padBottom(12).row();
        rewardBox.add(rewardStatusLabel).width(670).center().row();

        victoryCard.add(rewardBox).size(720, 230).padBottom(20).center().row();

        // 5. Action Button
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = UIUtils.createRoundedDrawable(280, 62, 14, Color.valueOf("#2e7d32"), Color.valueOf("#1b5e20"), 2);
        btnStyle.over = UIUtils.createRoundedDrawable(280, 62, 14, Color.valueOf("#388e3c"), Color.valueOf("#2e7d32"), 2);
        btnStyle.font = titleFont;
        btnStyle.fontColor = Color.WHITE;

        continueBtn = new TextButton("CONTINUE", btnStyle);
        continueBtn.pad(10, 36, 10, 36);
        UIUtils.addScaleHoverEffect(continueBtn);
        continueBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                if (isDawnWardenBattle()) {
                    BattleOutcome outcome = battleSystem.getFinalOutcome();
                    boolean won = (outcome == BattleOutcome.VICTORY);
                    if (won) {
                        if (game.getRunTracker() != null) {
                            game.getRunTracker().recordThroneClaimed(player);
                        }
                        game.setScreen(new EndingStoryScreen(
                            game, player,
                            io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker.EndingType.REBIRTH_TRIUMPH));
                        return;
                    }
                    if (game.getRunTracker() != null) {
                        game.getRunTracker().recordPlayerFallen(player);
                    }
                    if (game.getPlayers().size() <= 1 || game.isPartyWiped()) {
                        game.setScreen(new EndingStoryScreen(
                            game, player,
                            io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker.EndingType.PEACEFUL_REST));
                        return;
                    }
                    game.setScreen(new BoardScreen(game, BattleOutcome.DEFEAT, enemy));
                    return;
                }
                if (isPaleShepherdBattle()) {
                    BattleOutcome outcome = battleSystem.getFinalOutcome();
                    if (outcome == null) {
                        outcome = (player.getHealth() <= 0) ? BattleOutcome.DEFEAT : BattleOutcome.ESCAPED;
                    }
                    if (outcome == BattleOutcome.DEFEAT || player.getHealth() <= 0) {
                        if (game.getRunTracker() != null) {
                            game.getRunTracker().recordPlayerFallen(player);
                        }
                        if (game.getPlayers().size() <= 1 || game.isPartyWiped()) {
                            game.setScreen(new GameSummaryScreen(
                                game, false, player,
                                io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker.EndingType.HARVESTED_BY_SHEPHERD));
                            return;
                        }
                    }
                    game.setScreen(new BoardScreen(game, outcome, enemy));
                    return;
                }
                if (player.getHealth() <= 0) {
                    if (game.getRunTracker() != null) {
                        game.getRunTracker().recordPlayerFallen(player);
                    }
                    if (game.getPlayers().size() <= 1 || game.isPartyWiped()) {
                        game.setScreen(new GameSummaryScreen(
                            game, false, player,
                            io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker.EndingType.FALLEN_ON_TRAIL));
                        return;
                    }
                }
                BattleOutcome outcome = battleSystem.getFinalOutcome();
                if (outcome == null) {
                    outcome = BattleOutcome.ESCAPED;
                }
                if (snakeTile != null) {
                    game.setScreen(new BoardScreen(game, outcome, snakeTile));
                } else {
                    game.nextTurn();
                    game.setScreen(new BoardScreen(game));
                }
            }
        });
        continueBtnCell = victoryCard.add(continueBtn).minWidth(280).height(62).padTop(4).center();

        continueTable.add(victoryCard).size(800, 540).center();
        hudStage.addActor(continueTable);
    }

    /**
     * Builds the unified Parchment Scroll Modals for Pause, Settings, and Leave Game confirmation in BattleScreen.
     */
    private void buildPauseAndSettingsModals(Color darkBrown, Color pillBrown) {
        // Fullscreen translucent backdrop that softly dims the scene without obscuring it
        pauseModalScrim = new Table();
        pauseModalScrim.setFillParent(true);
        pauseModalScrim.setBackground(UIUtils.createRoundedDrawable(1920, 1080, 0, new Color(0f, 0f, 0f, 0.45f)));
        pauseModalScrim.setVisible(false);
        pauseModalScrim.setTouchable(Touchable.enabled);
        hudStage.addActor(pauseModalScrim);

        TextButton.TextButtonStyle menuBtnStyle = new TextButton.TextButtonStyle();
        menuBtnStyle.up = UIUtils.createRoundedDrawable(340, 52, 16, pillBrown);
        menuBtnStyle.over = UIUtils.createRoundedDrawable(340, 52, 16, Color.valueOf("#9c572b"));
        menuBtnStyle.font = pauseBtnFont;
        menuBtnStyle.fontColor = Color.WHITE;

        TextButton.TextButtonStyle dangerBtnStyle = new TextButton.TextButtonStyle();
        dangerBtnStyle.up = UIUtils.createRoundedDrawable(340, 52, 16, Color.valueOf("#8a2424"));
        dangerBtnStyle.over = UIUtils.createRoundedDrawable(340, 52, 16, Color.valueOf("#a63232"));
        dangerBtnStyle.font = pauseBtnFont;
        dangerBtnStyle.fontColor = Color.WHITE;

        // =====================================================================
        // 1. PAUSE MODAL
        // =====================================================================
        Label pauseBadge = new Label("BATTLE PAUSED", new Label.LabelStyle(pauseBadgeFont, Color.WHITE));
        pauseBadge.setAlignment(Align.center);
        Table pauseBadgeTable = new Table();
        pauseBadgeTable.setBackground(UIUtils.createRoundedDrawable(340, 58, 18, pillBrown));
        pauseBadgeTable.add(pauseBadge).pad(6, 36, 6, 36);

        Table pauseContent = new Table();
        Label pauseSub = new Label("The clash halts at your command.", new Label.LabelStyle(pauseSubFont, darkBrown));
        pauseSub.setAlignment(Align.center);
        pauseContent.add(pauseSub).padTop(12).padBottom(20).center().row();

        TextButton resumeBtn = new TextButton("RESUME", menuBtnStyle);
        UIUtils.addScaleHoverEffect(resumeBtn);
        resumeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                resumeGame();
            }
        });

        TextButton settingsBtn = new TextButton("SETTINGS", menuBtnStyle);
        UIUtils.addScaleHoverEffect(settingsBtn);
        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                showSettingsMenu();
            }
        });

        TextButton leaveBtn = new TextButton("FORFEIT & LEAVE", dangerBtnStyle);
        UIUtils.addScaleHoverEffect(leaveBtn);
        leaveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                showLeaveConfirmMenu();
            }
        });

        pauseContent.add(resumeBtn).size(340, 52).padBottom(12).center().row();
        pauseContent.add(settingsBtn).size(340, 52).padBottom(12).center().row();
        pauseContent.add(leaveBtn).size(340, 52).padBottom(16).center().row();

        Label escTip = new Label("Press ESC or P to resume", new Label.LabelStyle(pauseTipFont, Color.valueOf("#54280e")));
        escTip.setAlignment(Align.center);
        pauseContent.add(escTip).padTop(4).padBottom(10).center();

        Image pauseScrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        pauseScrollBg.setScaling(Scaling.stretch);

        Stack pauseScrollStack = new Stack();
        pauseScrollStack.add(pauseScrollBg);
        pauseScrollStack.add(pauseContent);

        pauseModalTable = new Table();
        pauseModalTable.setFillParent(true);
        pauseModalTable.setVisible(false);
        pauseModalTable.add(pauseBadgeTable).size(340, 58).padBottom(-22).center().row();
        pauseModalTable.add(pauseScrollStack).size(800, 580).center().row();

        hudStage.addActor(pauseModalTable);

        // =====================================================================
        // 2. SETTINGS MODAL
        // =====================================================================
        Label settingsBadge = new Label("SETTINGS", new Label.LabelStyle(pauseBadgeFont, Color.WHITE));
        settingsBadge.setAlignment(Align.center);
        Table settingsBadgeTable = new Table();
        settingsBadgeTable.setBackground(UIUtils.createRoundedDrawable(280, 58, 18, pillBrown));
        settingsBadgeTable.add(settingsBadge).pad(6, 36, 6, 36);

        Slider.SliderStyle sliderStyle = UIUtils.createSliderStyle(darkBrown, darkBrown);

        Slider musicSlider = new Slider(0, 10, 1, false, sliderStyle);
        int musicVal = game.getAudioManager() != null ? Math.round(game.getAudioManager().getMusicVolume() * 10f) : 7;
        musicSlider.setValue(musicVal);
        Label musicNum = new Label(String.valueOf(musicVal), new Label.LabelStyle(sliderFont, darkBrown));
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int val = (int) musicSlider.getValue();
                musicNum.setText(String.valueOf(val));
                if (game.getAudioManager() != null) {
                    game.getAudioManager().setMusicVolume(val / 10f);
                }
            }
        });

        Slider sfxSlider = new Slider(0, 10, 1, false, sliderStyle);
        int sfxVal = game.getAudioManager() != null ? Math.round(game.getAudioManager().getSfxVolume() * 10f) : 8;
        sfxSlider.setValue(sfxVal);
        Label sfxNum = new Label(String.valueOf(sfxVal), new Label.LabelStyle(sliderFont, darkBrown));
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int val = (int) sfxSlider.getValue();
                sfxNum.setText(String.valueOf(val));
                if (game.getAudioManager() != null) {
                    game.getAudioManager().setSfxVolume(val / 10f);
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
            }
        });

        Image soundIcon = new Image(game.getAssetManager().get(Assets.ICON_SOUND));
        Image musicIcon = new Image(game.getAssetManager().get(Assets.ICON_MUSIC));

        Table settingsContent = new Table();

        Label musicLabel = new Label("MUSIC VOLUME", new Label.LabelStyle(pauseTitleFont, darkBrown));
        settingsContent.add(musicLabel).colspan(3).center().padBottom(10).row();
        settingsContent.add(musicIcon).size(38, 38).padRight(16);
        settingsContent.add(musicSlider).width(340).padRight(16);
        settingsContent.add(musicNum).width(44).left().padBottom(26).row();

        Label sfxLabel = new Label("SFX VOLUME", new Label.LabelStyle(pauseTitleFont, darkBrown));
        settingsContent.add(sfxLabel).colspan(3).center().padBottom(10).row();
        settingsContent.add(soundIcon).size(38, 38).padRight(16);
        settingsContent.add(sfxSlider).width(340).padRight(16);
        settingsContent.add(sfxNum).width(44).left().row();

        Image settingsScrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        settingsScrollBg.setScaling(Scaling.stretch);

        Stack settingsScrollStack = new Stack();
        settingsScrollStack.add(settingsScrollBg);
        settingsScrollStack.add(settingsContent);

        TextButton backSettingsBtn = new TextButton("BACK", menuBtnStyle);
        UIUtils.addScaleHoverEffect(backSettingsBtn);
        backSettingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                showPauseMenu();
            }
        });

        settingsModalTable = new Table();
        settingsModalTable.setFillParent(true);
        settingsModalTable.setVisible(false);
        settingsModalTable.add(settingsBadgeTable).size(280, 58).padBottom(-22).center().row();
        settingsModalTable.add(settingsScrollStack).size(800, 580).center().row();
        settingsModalTable.add(backSettingsBtn).size(180, 52).padTop(-22).center().row();

        hudStage.addActor(settingsModalTable);

        // =====================================================================
        // 3. LEAVE GAME CONFIRMATION MODAL
        // =====================================================================
        Label leaveBadge = new Label("LEAVE BATTLE?", new Label.LabelStyle(pauseBadgeFont, Color.WHITE));
        leaveBadge.setAlignment(Align.center);
        Table leaveBadgeTable = new Table();
        leaveBadgeTable.setBackground(UIUtils.createRoundedDrawable(300, 58, 18, Color.valueOf("#8a2424")));
        leaveBadgeTable.add(leaveBadge).pad(6, 36, 6, 36);

        Table leaveContent = new Table();

        Label leaveWarningTitle = new Label("Abandon Current Battle?", new Label.LabelStyle(pauseTitleFont, darkBrown));
        leaveWarningTitle.setAlignment(Align.center);
        leaveContent.add(leaveWarningTitle).padTop(10).padBottom(16).center().row();

        Label leaveWarningBody = new Label("Are you sure you want to return to the main menu?\nAny unsaved progress on this battle will be lost.", new Label.LabelStyle(pauseBodyFont, darkBrown));
        leaveWarningBody.setAlignment(Align.center);
        leaveWarningBody.setWrap(true);
        leaveContent.add(leaveWarningBody).width(500).padBottom(28).center().row();

        Table leaveBtnRow = new Table();
        TextButton stayBtn = new TextButton("STAY", menuBtnStyle);
        UIUtils.addScaleHoverEffect(stayBtn);
        stayBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                showPauseMenu();
            }
        });

        TextButton confirmLeaveBtn = new TextButton("LEAVE", dangerBtnStyle);
        UIUtils.addScaleHoverEffect(confirmLeaveBtn);
        confirmLeaveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                leaveGameToMainMenu();
            }
        });

        leaveBtnRow.add(stayBtn).size(170, 52).padRight(20);
        leaveBtnRow.add(confirmLeaveBtn).size(170, 52);
        leaveContent.add(leaveBtnRow).padBottom(12).center();

        Image leaveScrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        leaveScrollBg.setScaling(Scaling.stretch);

        Stack leaveScrollStack = new Stack();
        leaveScrollStack.add(leaveScrollBg);
        leaveScrollStack.add(leaveContent);

        leaveConfirmModalTable = new Table();
        leaveConfirmModalTable.setFillParent(true);
        leaveConfirmModalTable.setVisible(false);
        leaveConfirmModalTable.add(leaveBadgeTable).size(300, 58).padBottom(-22).center().row();
        leaveConfirmModalTable.add(leaveScrollStack).size(800, 580).center().row();

        hudStage.addActor(leaveConfirmModalTable);
    }

    private void pauseGame() {
        if (continueTable != null && continueTable.isVisible()) return;
        isPaused = true;
        if (itemPopup != null) itemPopup.setVisible(false);
        showPauseMenu();
    }

    private void resumeGame() {
        isPaused = false;
        pauseModalScrim.setVisible(false);
        pauseModalTable.setVisible(false);
        settingsModalTable.setVisible(false);
        leaveConfirmModalTable.setVisible(false);
    }

    private void showPauseMenu() {
        pauseModalScrim.setVisible(true);
        pauseModalTable.setVisible(true);
        settingsModalTable.setVisible(false);
        leaveConfirmModalTable.setVisible(false);
    }

    private void showSettingsMenu() {
        pauseModalScrim.setVisible(true);
        pauseModalTable.setVisible(false);
        settingsModalTable.setVisible(true);
        leaveConfirmModalTable.setVisible(false);
    }

    private void showLeaveConfirmMenu() {
        pauseModalScrim.setVisible(true);
        pauseModalTable.setVisible(false);
        settingsModalTable.setVisible(false);
        leaveConfirmModalTable.setVisible(true);
    }

    private void leaveGameToMainMenu() {
        game.setScreen(new MainMenuScreen(game));
    }

    private void syncUi() {
        BattleState state = battleSystem.getCurrentState();
        boolean isAnimating = (playerLungeTimer > 0f || enemyLungeTimer > 0f || skillAnimTimer > 0f);
        boolean dialogueActive = isIntroDialogueActive || isEpilogueDialogueActive;
        boolean playerTurn = (!bossEntranceActive && !dialogueActive && state == BattleState.PLAYER_TURN && !isAnimating);
        boolean ended = (state == BattleState.BATTLE_END);

        if (topBannerWrap != null) {
            topBannerWrap.setVisible(bossLanded);
        }
        boolean showHpPills = bossLanded && !ended && !bossEntranceActive && !dialogueActive;
        if (playerHpWrap != null) {
            playerHpWrap.setVisible(showHpPills);
        }
        if (enemyHpWrap != null) {
            enemyHpWrap.setVisible(showHpPills);
        }
        if (bossIntroTable != null) {
            bossIntroTable.setVisible(!bossEntranceActive && (isIntroDialogueActive || (isEpilogueDialogueActive && ended)));
        }
        if (bottomBar != null) {
            bottomBar.setVisible(!bossEntranceActive && !dialogueActive && !ended);
        }
        if (battleLogContainer != null && !ended) {
            battleLogContainer.setVisible(!bossEntranceActive && !dialogueActive);
        }

        // True Phase 2 Transition when Phase 1 HP reaches 0
        if (battleSystem.consumeJustEnteredPhase2()) {
            bossEnraged = true;
            enemyMaxHp = battleSystem.getPhase2MaxHp();
            bossFlashAlpha = 0.58f;
            triggerScreenShake(22f, 0.50f);
            spawnShockwave(ENEMY_X, ENEMY_DRAW_Y + 140f, 65f, 320f, Color.valueOf("#ff6d00"), Color.valueOf("#ffd54f"), 0.65f);
            spawnSolarBurst(ENEMY_X, ENEMY_DRAW_Y + 130f, 44);
            spawnFloatingText(ENEMY_X, ENEMY_DRAW_Y + 290f, "PHASE II: SOLAR ASCENSION!", Color.valueOf("#ffd54f"), true);

            if (topBannerLabel != null) {
                topBannerLabel.setText("PHASE II: AWAKENED DAWN WARDEN!");
            }
            if (topBannerBox != null) {
                topBannerBox.setBackground(UIUtils.createRoundedDrawable(
                    920, 70, 20, Color.valueOf("#8c1d18"), Color.valueOf("#ffd54f"), 3));
            }
            if (enemyNameLabel != null) {
                enemyNameLabel.setText("DAWN WARDEN (PHASE II)");
            }
            if (game.getAudioManager() != null) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.BOSS_ROAR);
            }
        }

        // Update Text
        battleLogLabel.setText(ended ? outcomeMessage : battleSystem.getBattleLog());

        int maxHp = player.getMaxHealth();
        playerHpLabel.setText("HP: " + Math.max(0, player.getHealth()) + " / " + maxHp);
        enemyHpLabel.setText("HP: " + Math.max(0, enemy.getHealth()) + " / " + enemyMaxHp);

        // Lock buttons during enemy turn, animation, or end
        btnAttack.setDisabled(!playerTurn);
        btnDefend.setDisabled(!playerTurn);
        btnItem.setDisabled(!playerTurn);
        if (isDawnWardenBattle()) {
            btnEscape.setDisabled(true);
            btnEscape.setVisible(false);
        } else if (isPaleShepherdBattle()) {
            btnEscape.setVisible(true);
            int round = battleSystem.getCombatRound();
            if (round < 3) {
                btnEscape.setDisabled(true);
                int roundsLeft = 3 - round;
                btnEscape.setText("LOCKED (" + roundsLeft + ")");
                btnEscape.getLabel().setFontScale(0.82f);
                btnEscape.getLabel().setAlignment(Align.center);
                btnEscape.setColor(0.55f, 0.40f, 0.40f, 0.55f);
            } else {
                btnEscape.setDisabled(!playerTurn);
                btnEscape.setText("SCRAMBLE\n(-5 HP)");
                btnEscape.getLabel().setFontScale(0.72f);
                btnEscape.getLabel().setAlignment(Align.center);
                btnEscape.setColor(1.0f, 0.38f, 0.38f, playerTurn ? 1f : 0.5f);

                if (!scrambleAnnounced && !ended) {
                    scrambleAnnounced = true;
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    }
                    spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "MIST CLEARS: SCRAMBLE UNLOCKED!", Color.valueOf("#ffd54f"), false);
                    battleLogLabel.setText("An opening appears in the twilight mist! You can attempt a quiet scramble!");
                }
            }
        } else {
            btnEscape.setDisabled(!playerTurn);
            btnEscape.setVisible(true);
            btnEscape.setText("ESCAPE");
            btnEscape.getLabel().setFontScale(1f);
            btnEscape.setColor(1, 1, 1, playerTurn ? 1f : 0.5f);
        }

        if (continueTable != null && continueTable.isVisible()) {
            if (pauseBtn != null) pauseBtn.setVisible(false);
        } else if (pauseBtn != null && !isPaused) {
            pauseBtn.setVisible(true);
        }

        // Dim disabled buttons
        float alpha = playerTurn ? 1f : 0.5f;
        btnAttack.setColor(1, 1, 1, alpha);
        btnDefend.setColor(1, 1, 1, alpha);
        btnItem.setColor(1, 1, 1, alpha);
        btnEscape.setColor(1, 1, 1, alpha);
        boolean skillReady = playerTurn && player.canUseSkill();
        btnSkill.setDisabled(!skillReady);

        if (!player.canUseSkill()) {
            btnSkill.setText("COOLDOWN (" + player.getSkillCooldown() + ")");
            btnSkill.setColor(0.6f, 0.6f, 0.6f, 0.5f); // Dimmed grey
        } else {
            io.github.snakes_and_ladders_roll_of_fate.skills.ClassSkill cs = player.getClassSkill();
            String skillBtnText = (cs != null) ? cs.getCombatSkillName().toUpperCase() : "SKILL";
            btnSkill.setText(skillBtnText);
            btnSkill.setColor(1, 1, 1, playerTurn ? 1f : 0.5f);
        }
        if (!isDawnWardenBattle() && !isPaleShepherdBattle()) {
            btnEscape.setColor(1, 1, 1, alpha);
        }

        // Handle End State (wait for lunge return animation to finish cleanly first)
        if (ended && !outcomeResolved && !isAnimating) {
            outcomeResolved = true;
            BattleOutcome o = battleSystem.getFinalOutcome();
            if (isPaleShepherdBattle()) {
                if (o == BattleOutcome.VICTORY) {
                    outcomeMessage = "The Pale Shepherd yields with a soft nod, dissolving into gentle mist.";
                } else if (o == BattleOutcome.ESCAPED) {
                    outcomeMessage = "You slipped past The Pale Shepherd! (-5 HP Parting Wound)";
                } else {
                    outcomeMessage = "The Pale Shepherd gently carries your spirit.";
                }
            } else if (isDawnWardenBattle()) {
                outcomeMessage = (o == BattleOutcome.VICTORY) ? "The Dawn Warden bestows the breath of life!" : "Your spirit finds peace atop the clouds.";
            } else if (snakeTile != null) {
                outcomeMessage = snakeTile.resolveOutcome(player, enemy, o); // Apply EXP/Loot/Slide
            } else {
                outcomeMessage = "The encounter has ended.";
            }

            // Hide redundant top battle log box so the center modal is the clear hero element
            if (battleLogContainer != null) {
                battleLogContainer.setVisible(false);
            }

            if (isPaleShepherdBattle()) {
                if (o == BattleOutcome.VICTORY) {
                    topBannerLabel.setText("MIST SETTLES");
                    modalTitleLabel.setText("QUIET TRIUMPH");
                    modalTitleLabel.setColor(Color.valueOf("#ffd54f"));
                    modalSubtitleLabel.setText("THE PALE SHEPHERD BESTOWS HIS GENTLE BLESSING");

                    rewardExpLabel.setText("+100 EXP  |  Full Health Restored  |  Permanent +10 Max HP & +1 ATK");
                    rewardExpLabel.setColor(Color.valueOf("#69f0ae"));

                    rewardLootLabel.setText("Acquired: Pale Shepherd's Crook (+4 DEF)  |  Reaper Paused 3 Rounds");
                    rewardLootLabel.setColor(Color.valueOf("#ffd54f"));

                    rewardStatusLabel.setText("Hero Health: " + (player.getMaxHealth() + 10) + " / " + (player.getMaxHealth() + 10) + " HP (Fully Restored & Empowered)");
                    rewardStatusLabel.setColor(Color.valueOf("#ffe0b2"));

                    continueBtn.setText("RESUME ASCENT");
                    continueBtn.getStyle().up = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#2e7d32"), Color.valueOf("#1b5e20"), 2);
                    continueBtn.getStyle().over = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#388e3c"), Color.valueOf("#2e7d32"), 2);
                    if (continueBtnCell != null) continueBtnCell.width(340);

                    spawnCelebrationSparks(960f, 540f);
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.LEVEL_UP);
                    }
                } else if (o == BattleOutcome.ESCAPED) {
                    topBannerLabel.setText("QUIET FLIGHT");
                    modalTitleLabel.setText("HASTY ESCAPE");
                    modalTitleLabel.setColor(Color.valueOf("#ffb74d"));
                    modalSubtitleLabel.setText("YOU SLIPPED AWAY FROM THE PALE SHEPHERD!");

                    rewardExpLabel.setText("Suffered light parting wound (-5 HP)");
                    rewardExpLabel.setColor(Color.valueOf("#ff8a80"));

                    rewardLootLabel.setText("Scrambled 3 tiles ahead! Shepherd paused for 1 round.");
                    rewardLootLabel.setColor(Color.valueOf("#ffd54f"));

                    rewardStatusLabel.setText("Hero Health: " + Math.max(1, player.getHealth() - 5) + " / " + maxHp + " HP");
                    rewardStatusLabel.setColor(Color.valueOf("#ffe0b2"));

                    continueBtn.setText("SCRAMBLE AHEAD");
                    continueBtn.getStyle().up = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#854b25"), Color.valueOf("#54280e"), 2);
                    continueBtn.getStyle().over = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#a66032"), Color.valueOf("#854b25"), 2);
                    if (continueBtnCell != null) continueBtnCell.width(340);
                } else {
                    topBannerLabel.setText("REST, WEARY WANDERER");
                    modalTitleLabel.setText("QUIET REST");
                    modalTitleLabel.setColor(Color.valueOf("#b39ddb"));
                    modalSubtitleLabel.setText("THE PALE SHEPHERD GENTLY CARRIES YOUR SPIRIT");

                    rewardExpLabel.setText("Your ascent has ended on the mountain trail.");
                    rewardExpLabel.setColor(Color.valueOf("#cfd8dc"));

                    rewardLootLabel.setText("No more climbing, no more pain.");
                    rewardLootLabel.setColor(Color.valueOf("#ffffff"));

                    rewardStatusLabel.setText("Hero Status: Fallen (0 / " + maxHp + " HP)");
                    rewardStatusLabel.setColor(Color.valueOf("#cfd8dc"));

                    if (game.getPlayers().size() <= 1 || game.isPartyWiped()) {
                        continueBtn.setText("VIEW EXPEDITION SUMMARY");
                        continueBtn.getStyle().up = UIUtils.createRoundedDrawable(460, 62, 14, Color.valueOf("#854b25"), Color.valueOf("#54280e"), 2);
                        continueBtn.getStyle().over = UIUtils.createRoundedDrawable(460, 62, 14, Color.valueOf("#a66032"), Color.valueOf("#854b25"), 2);
                        if (continueBtnCell != null) continueBtnCell.width(460);
                    } else {
                        continueBtn.setText("RETURN TO BOARD");
                        continueBtn.getStyle().up = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#854b25"), Color.valueOf("#54280e"), 2);
                        continueBtn.getStyle().over = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#a66032"), Color.valueOf("#854b25"), 2);
                        if (continueBtnCell != null) continueBtnCell.width(340);
                    }
                }
                victoryCard.invalidateHierarchy();
                continueTable.setVisible(true);
                return;
            }

            if (isDawnWardenBattle()) {
                if (o == BattleOutcome.VICTORY) {
                    if (game.getRunTracker() != null) {
                        game.getRunTracker().recordThroneClaimed(player);
                    }
                    topBannerLabel.setText("SUMMIT CONQUERED!");
                    modalTitleLabel.setText("GLORIOUS REBIRTH!");
                    modalTitleLabel.setColor(Color.valueOf("#ffd54f"));
                    modalSubtitleLabel.setText("THE DAWN WARDEN HAS BESTOWED THE BREATH OF LIFE!");

                    rewardExpLabel.setText("The golden gates swing open to the living world!");
                    rewardExpLabel.setColor(Color.valueOf("#69f0ae"));

                    rewardLootLabel.setText("Vitality Restored: You awaken in the sunlit morning!");
                    rewardLootLabel.setColor(Color.valueOf("#ffd54f"));

                    rewardStatusLabel.setText("Hero Health: " + player.getHealth() + " / " + maxHp + " HP   |   Triumph of the Living!");
                    rewardStatusLabel.setColor(Color.valueOf("#ffe0b2"));

                    continueBtn.setText("CLAIM REBIRTH");
                    continueBtn.getStyle().up = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#2e7d32"), Color.valueOf("#1b5e20"), 2);
                    continueBtn.getStyle().over = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#388e3c"), Color.valueOf("#2e7d32"), 2);
                    if (continueBtnCell != null) continueBtnCell.width(340);
                } else {
                    topBannerLabel.setText("REST AT THE SUMMIT");
                    modalTitleLabel.setText("PEACEFUL SLEEP");
                    modalTitleLabel.setColor(Color.valueOf("#ffa726"));
                    modalSubtitleLabel.setText("YOUR SPIRIT RESTS TRANQUIL ATOP THE SPIRE OF FATE");

                    rewardExpLabel.setText("The Dawn Warden covers you with a warm blanket of rest.");
                    rewardExpLabel.setColor(Color.valueOf("#ffb74d"));

                    rewardLootLabel.setText("The long climb has ended. No more cold, no more pain.");
                    rewardLootLabel.setColor(Color.valueOf("#ffffff"));

                    rewardStatusLabel.setText("Hero Status: At Peace (0 / " + maxHp + " HP)");
                    rewardStatusLabel.setColor(Color.valueOf("#cfd8dc"));

                    if (game.getPlayers().size() <= 1 || game.isPartyWiped()) {
                        continueBtn.setText("VIEW EXPEDITION SUMMARY");
                        continueBtn.getStyle().up = UIUtils.createRoundedDrawable(460, 62, 14, Color.valueOf("#854b25"), Color.valueOf("#54280e"), 2);
                        continueBtn.getStyle().over = UIUtils.createRoundedDrawable(460, 62, 14, Color.valueOf("#a66032"), Color.valueOf("#854b25"), 2);
                        if (continueBtnCell != null) continueBtnCell.width(460);
                    } else {
                        continueBtn.setText("RETURN TO BOARD");
                        continueBtn.getStyle().up = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#854b25"), Color.valueOf("#54280e"), 2);
                        continueBtn.getStyle().over = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#a66032"), Color.valueOf("#854b25"), 2);
                        if (continueBtnCell != null) continueBtnCell.width(340);
                    }
                }
                victoryCard.invalidateHierarchy();
                startDawnWardenEpilogue(o == BattleOutcome.VICTORY);
                return;
            }

            if (o == BattleOutcome.VICTORY) {
                if (game.getRunTracker() != null) {
                    game.getRunTracker().recordSnakeCombat(player, 0, true, false);
                }
                topBannerLabel.setText("VICTORY!");
                modalTitleLabel.setText("VICTORY!");
                modalTitleLabel.setColor(Color.valueOf("#ffd54f"));
                modalSubtitleLabel.setText("THE " + enemy.getEnemyType().toUpperCase() + " HAS BEEN VANQUISHED!");

                int exp = (snakeTile != null) ? (15 + (snakeTile.getPosition() / 2)) : 25;
                rewardExpLabel.setText("EXP Gained: +" + exp + " EXP  (Total: " + player.getTotalExperience() + " EXP)");
                rewardExpLabel.setColor(Color.valueOf("#ffd54f"));

                if (outcomeMessage.contains("Loot: ")) {
                    if (game.getRunTracker() != null) {
                        game.getRunTracker().recordItemCollected(player);
                    }
                    String lootStr = outcomeMessage.substring(outcomeMessage.indexOf("Loot: ") + 6);
                    rewardLootLabel.setText("Spoils of War: " + lootStr + " (Added to Satchel)");
                    rewardLootLabel.setColor(Color.valueOf("#69f0ae"));
                } else if (outcomeMessage.contains("Satchel full")) {
                    rewardLootLabel.setText("Satchel Full: Spoils could not be carried!");
                    rewardLootLabel.setColor(Color.valueOf("#ffb74d"));
                } else {
                    rewardLootLabel.setText("Spoils of War: None");
                    rewardLootLabel.setColor(Color.valueOf("#cfd8dc"));
                }

                rewardStatusLabel.setText("Hero Health: " + player.getHealth() + " / " + maxHp + " HP   |   Level " + player.getLevel());
                rewardStatusLabel.setColor(Color.valueOf("#ffe0b2"));

                continueBtn.setText("CONTINUE");
                continueBtn.getStyle().up = UIUtils.createRoundedDrawable(280, 62, 14, Color.valueOf("#2e7d32"), Color.valueOf("#1b5e20"), 2);
                continueBtn.getStyle().over = UIUtils.createRoundedDrawable(280, 62, 14, Color.valueOf("#388e3c"), Color.valueOf("#2e7d32"), 2);
                if (continueBtnCell != null) continueBtnCell.width(280);

                spawnCelebrationSparks(960f, 540f);
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.LEVEL_UP);
                }
            } else if (o == BattleOutcome.DEFEAT) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.PLAYER_HURT);
                }
                if (game.getRunTracker() != null) {
                    game.getRunTracker().recordPlayerFallen(player);
                }
                topBannerLabel.setText("DEFEAT");
                modalTitleLabel.setText("DEFEAT");
                modalTitleLabel.setColor(Color.valueOf("#ff5252"));
                modalSubtitleLabel.setText("YOU HAVE FALLEN TO THE " + enemy.getEnemyType().toUpperCase());

                rewardExpLabel.setText("Combat Result: Slain in battle");
                rewardExpLabel.setColor(Color.valueOf("#ff6b6b"));

                rewardLootLabel.setText(outcomeMessage);
                rewardLootLabel.setColor(Color.valueOf("#ffffff"));

                rewardStatusLabel.setText("Hero Status: Fallen (0 / " + maxHp + " HP)");
                rewardStatusLabel.setColor(Color.valueOf("#cfd8dc"));

                if (game.getPlayers().size() <= 1 || game.isPartyWiped()) {
                    continueBtn.setText("VIEW RUN SUMMARY");
                    continueBtn.getStyle().up = UIUtils.createRoundedDrawable(380, 62, 14, Color.valueOf("#8a2424"), Color.valueOf("#541616"), 2);
                    continueBtn.getStyle().over = UIUtils.createRoundedDrawable(380, 62, 14, Color.valueOf("#a82828"), Color.valueOf("#8a2424"), 2);
                    if (continueBtnCell != null) continueBtnCell.width(380);
                } else {
                    continueBtn.setText("CONTINUE");
                    continueBtn.getStyle().up = UIUtils.createRoundedDrawable(280, 62, 14, Color.valueOf("#8a2424"), Color.valueOf("#541616"), 2);
                    continueBtn.getStyle().over = UIUtils.createRoundedDrawable(280, 62, 14, Color.valueOf("#a82828"), Color.valueOf("#8a2424"), 2);
                    if (continueBtnCell != null) continueBtnCell.width(280);
                }
            } else { // ESCAPED or WEAKENED
                topBannerLabel.setText("RETREAT!");
                modalTitleLabel.setText("SURVIVAL RETREAT");
                modalTitleLabel.setColor(Color.valueOf("#ffa726"));
                modalSubtitleLabel.setText("SURVIVAL INSTINCT PREVAILS");

                rewardExpLabel.setText("Escaped Combat: Slid down the slope");
                rewardExpLabel.setColor(Color.valueOf("#ffb74d"));

                rewardLootLabel.setText(outcomeMessage);
                rewardLootLabel.setColor(Color.valueOf("#ffffff"));

                int destTile = (snakeTile != null) ? snakeTile.getDestination() : 1;
                rewardStatusLabel.setText("Hero Health: " + player.getHealth() + " / " + maxHp + " HP   |   Tile " + destTile);
                rewardStatusLabel.setColor(Color.valueOf("#ffe0b2"));

                continueBtn.setText("CONTINUE");
                continueBtn.getStyle().up = UIUtils.createRoundedDrawable(280, 62, 14, Color.valueOf("#854b25"), Color.valueOf("#54280e"), 2);
                continueBtn.getStyle().over = UIUtils.createRoundedDrawable(280, 62, 14, Color.valueOf("#a66032"), Color.valueOf("#854b25"), 2);
                if (continueBtnCell != null) continueBtnCell.width(280);
            }

            victoryCard.invalidateHierarchy();
            continueTable.setVisible(true);
        }
    }
    private void buildItemPopup() {
        itemPopup = new Table();
        // Dark leather background matching your other UI
        itemPopup.setBackground(UIUtils.createRoundedDrawable(620, 420, 18, Color.valueOf("#3d1b06f8")));
        itemPopup.setVisible(false);

        // Center the popup at the bottom of the screen
        Table wrap = new Table();
        wrap.setFillParent(true);
        wrap.bottom().center().padBottom(150);
        wrap.add(itemPopup).size(620, 420);
        hudStage.addActor(wrap);
    }

    private void refreshItemPopup() {
        itemPopup.clear(); // Clear old rows

        Inventory inv = player.getInventory();
        if (inv == null) return;

        // Title
        itemPopup.add(new Label(player.getName().toUpperCase() + "'S SATCHEL",
                new Label.LabelStyle(titleFont, Color.valueOf("#ffd54f"))))
            .padTop(16).padBottom(12).center().row();

        // Loop through the 4 inventory slots
        for (int i = 0; i < inv.getCapacity(); i++) {
            final int slotIndex = i; // Capture index for the listener
            Item item = inv.getItem(i);

            if (item == null) continue; // Skip empty slots

            Table row = new Table();
            row.pad(6, 12, 6, 12);

            boolean isCurse = item.isCurse();
            io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType eff = item.getEffectType();
            boolean isBoardOnly = (eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.MOVE
                || eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.DICE_EXACT
                || eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.DICE_CLAMP
                || eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.SNAKE_WARD
                || eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.SWAP_PLAYER
                || eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.SCOUT);
            boolean lethalCost = (item.getSecondaryEffectType() == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.HEALTH
                && item.getSecondaryEffectValue() < 0
                && player.getHealth() + item.getSecondaryEffectValue() <= 0);
            boolean canUseInBattle = !isCurse && !isBoardOnly && !lethalCost;

            // Item Icon
            Texture iconTex = game.getAssetManager().get(Assets.getItemIcon(item));
            Image iconImg = new Image(iconTex);
            iconImg.setScaling(Scaling.fit);
            if (isCurse) {
                iconImg.setColor(Color.valueOf("#ff8a80"));
            }
            row.add(iconImg).size(36, 36).padRight(10).center();

            // Item Name & Effect Summary
            String rowText = item.getName() + " (" + item.getEffect() + ")";
            Color rowColor = isCurse ? Color.valueOf("#fca5a5") : (isBoardOnly ? Color.valueOf("#d6ccc2") : Color.WHITE);
            Label nameLbl = new Label(rowText, new Label.LabelStyle(titleFont, rowColor));
            nameLbl.setWrap(true);
            row.add(nameLbl).width(394).left().padRight(10);

            // USE Button
            TextButton.TextButtonStyle useStyle = new TextButton.TextButtonStyle();
            useStyle.up = UIUtils.createRoundedDrawable(105, 40, 10,
                canUseInBattle ? Color.valueOf("#2e7d32") : (isCurse ? Color.valueOf("#6b1a1a") : Color.valueOf("#5c4a3e")));
            useStyle.font = titleFont;
            useStyle.fontColor = Color.WHITE;

            String btnText = isCurse ? "CURSED" : (isBoardOnly ? "BOARD" : (lethalCost ? "LOW HP" : "USE"));
            TextButton useBtn = new TextButton(btnText, useStyle);

            if (canUseInBattle) {
                useBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                        if (game.getAudioManager() != null) {
                            game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                        }
                        spawnHealSparks(PLAYER_X, CHAR_DRAW_Y + 80f);
                        spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, item.getName().toUpperCase(), Color.valueOf("#81c784"), false);
                        battleSystem.onPlayerUseItem(slotIndex);
                        itemPopup.setVisible(false); // Close popup after using
                        syncUi(); // Refresh HP/Log
                    }
                });
            } else {
                useBtn.setDisabled(true);
                useBtn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            }

            row.add(useBtn).size(105, 40).right();
            itemPopup.add(row).width(580).height(56).padBottom(6).center().row();
        }

        // Close Button
        TextButton.TextButtonStyle closeStyle = new TextButton.TextButtonStyle();
        closeStyle.up = UIUtils.createRoundedDrawable(140, 44, 12, Color.valueOf("#854b25"));
        closeStyle.font = titleFont;
        closeStyle.fontColor = Color.WHITE;

        TextButton closeBtn = new TextButton("CLOSE", closeStyle);
        closeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                itemPopup.setVisible(false);
            }
        });
        itemPopup.add(closeBtn).size(140, 44).padTop(16).center().row();
    }

    @Override
    public void render(float delta) {
        float hudDelta = delta;

        // Hotkeys: Toggle pause with ESC or P, or step back in modal hierarchy
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (isPaused) {
                if (settingsModalTable.isVisible() || leaveConfirmModalTable.isVisible()) {
                    showPauseMenu();
                } else {
                    resumeGame();
                }
            } else if (continueTable == null || !continueTable.isVisible()) {
                pauseGame();
            }
        }

        if (isPaused) {
            delta = 0f;
        }

        stateTime += delta;
        int tile = (snakeTile != null) ? snakeTile.getPosition() : 100;

        // Cinematic Dawn Warden Entrance Sequence
        if (bossEntranceActive && !isPaused) {
            if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                completeBossEntrance();
            } else {
                bossEntranceTimer += delta;
                if (bossEntranceTimer < BOSS_DESCENT_START) {
                    bossVeilAlpha = 0.65f - (bossEntranceTimer / BOSS_DESCENT_START) * 0.15f;
                    bossEmberSpawnTimer -= delta;
                    if (bossEmberSpawnTimer <= 0f) {
                        bossEmberSpawnTimer = 0.08f;
                        spawnRisingEmber(ENEMY_X, ENEMY_DRAW_Y + 260f);
                    }
                } else if (!bossLanded && bossEntranceTimer >= BOSS_LAND_TIME) {
                    bossLanded = true;
                    bossFlashAlpha = 0.55f;
                    bossVeilAlpha = 0.42f;
                    triggerScreenShake(22f, 0.45f);
                    spawnShockwave(ENEMY_X, ENEMY_DRAW_Y + 35f, 65f, 290f, Color.valueOf("#ffd54f"), Color.valueOf("#fff8e1"), 0.65f);
                    spawnSolarBurst(ENEMY_X, ENEMY_DRAW_Y + 45f, 44);
                } else if (bossLanded) {
                    float post = (bossEntranceTimer - BOSS_LAND_TIME) / Math.max(0.1f, BOSS_ENTRANCE_DURATION - BOSS_LAND_TIME);
                    bossVeilAlpha = Math.max(0f, 0.42f * (1f - post));
                }

                if (bossEntranceTimer >= BOSS_ENTRANCE_DURATION) {
                    completeBossEntrance();
                }
            }
        } else if (bossVeilAlpha > 0f && !isPaused) {
            bossVeilAlpha = Math.max(0f, bossVeilAlpha - delta * 1.4f);
        }

        if (bossFlashAlpha > 0f && !isPaused) {
            bossFlashAlpha = Math.max(0f, bossFlashAlpha - delta * 1.55f);
        }

        // Ambient floating solar embers behind Dawn Warden during combat
        if (isDawnWardenBattle() && bossLanded && enemy != null && enemy.getHealth() > 0 && !isPaused) {
            bossEmberSpawnTimer -= delta;
            if (bossEmberSpawnTimer <= 0f) {
                bossEmberSpawnTimer = bossEnraged ? 0.09f : 0.18f;
                spawnRisingEmber(ENEMY_X, ENEMY_DRAW_Y + 40f);
            }
        }

        // Update Timers & Kinematics
        if (playerLungeTimer > 0f) {
            playerLungeTimer -= delta;
            float progress = 1.0f - (playerLungeTimer / PLAYER_LUNGE_DURATION);
            if (progress >= 0.48f && !playerAttackLanded) {
                playerAttackLanded = true;
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.MONSTER_HIT);
                }
                battleSystem.onPlayerAttack();
                syncUi();
            }
            if (playerLungeTimer <= 0f) {
                syncUi();
            }
        }

        if (skillAnimTimer > 0f) {
            skillAnimTimer -= delta;
            float p = 1.0f - (skillAnimTimer / skillAnimDuration);
            float enemyY = getEnemyCenterY();

            if (activeSkillClass == CharacterClass.CAT) {
                // Cat Double Strike: Strike 1 at p >= 0.28, Strike 2 at p >= 0.62
                if (p >= 0.28f && !skillPhase1Done) {
                    skillPhase1Done = true;
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.MONSTER_HIT);
                    }
                    slashEffects.add(new SlashEffect(ENEMY_X, enemyY, 0.28f, SlashStyle.CAT_DUAL_CLAW, false, -38f));
                    spawnHitSparks(ENEMY_X, enemyY, false, 18, false);
                    enemyFlinchTimer = FLINCH_DURATION * 0.7f;
                    triggerScreenShake(7f, 0.16f);
                }
                if (p >= 0.62f && !skillPhase2Done) {
                    skillPhase2Done = true;
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.ATTACK_SLASH);
                        game.getAudioManager().playSound(AudioManager.SoundEffect.MONSTER_HIT);
                    }
                    slashEffects.add(new SlashEffect(ENEMY_X, enemyY, 0.32f, SlashStyle.CAT_DUAL_CLAW, true, +38f));
                    spawnHitSparks(ENEMY_X, enemyY, false, 28, true);
                    enemyFlinchTimer = FLINCH_DURATION;
                    triggerScreenShake(14f, 0.26f);
                    battleSystem.onPlayerUseSkill();
                    syncUi();
                }
            } else if (activeSkillClass == CharacterClass.TURTLE) {
                // Turtle Iron Fortress: Activates at p >= 0.30
                if (p >= 0.30f && !skillPhase1Done) {
                    skillPhase1Done = true;
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.LEVEL_UP);
                    }
                    battleSystem.onPlayerUseSkill();
                    syncUi();
                }
            } else if (activeSkillClass == CharacterClass.FOX) {
                // Fox Vital Strike: Hits with guaranteed critical pierce at p >= 0.50
                if (p >= 0.50f && !skillPhase1Done) {
                    skillPhase1Done = true;
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.ATTACK_SLASH);
                        game.getAudioManager().playSound(AudioManager.SoundEffect.MONSTER_HIT);
                    }
                    slashEffects.add(new SlashEffect(ENEMY_X, enemyY, 0.38f, SlashStyle.FOX_VITAL_PIERCE, true, -42f));
                    spawnSolarBurst(ENEMY_X, enemyY, 24);
                    spawnHitSparks(ENEMY_X, enemyY, false, 32, true);
                    enemyFlinchTimer = FLINCH_DURATION;
                    triggerScreenShake(16f, 0.32f);
                    battleSystem.onPlayerUseSkill();
                    syncUi();
                }
            } else if (activeSkillClass == CharacterClass.RACCOON) {
                // Raccoon Scavenge Strike: Hits at p >= 0.48
                if (p >= 0.48f && !skillPhase1Done) {
                    skillPhase1Done = true;
                    int enemyHpBefore = enemy != null ? enemy.getHealth() : 0;
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.ITEM_EQUIP);
                        game.getAudioManager().playSound(AudioManager.SoundEffect.MONSTER_HIT);
                    }
                    slashEffects.add(new SlashEffect(ENEMY_X, enemyY, 0.32f, SlashStyle.RACCOON_HOOK_SWIPE, false, -25f));
                    spawnScavengeLootSparks(ENEMY_X, enemyY);
                    enemyFlinchTimer = FLINCH_DURATION;
                    triggerScreenShake(9f, 0.20f);
                    battleSystem.onPlayerUseSkill();
                    syncUi();

                    // If finishing blow triggered rations loot
                    if (enemyHpBefore > 0 && enemy != null && enemy.getHealth() <= 0) {
                        spawnHealSparks(PLAYER_X, CHAR_DRAW_Y + 70f);
                        spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "🎒 +25 HP RATIONS!", Color.valueOf("#69f0ae"), true);
                    }
                }
            }

            if (skillAnimTimer <= 0f) {
                activeSkillClass = null;
                syncUi();
            }
        }

        if (enemyLungeTimer > 0f) {
            enemyLungeTimer -= delta;
            float progress = 1.0f - (enemyLungeTimer / ENEMY_LUNGE_DURATION);
            if (progress >= 0.48f && !enemyAttackLanded) {
                enemyAttackLanded = true;
                if (game.getAudioManager() != null) {
                    if (isDawnWardenBattle()) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.BOSS_ROAR);
                    }
                    game.getAudioManager().playSound(AudioManager.SoundEffect.PLAYER_HURT);
                }
                battleSystem.executeEnemyAttack();
                syncUi();
            }
            if (enemyLungeTimer <= 0f) {
                syncUi();
            }
        }

        if (playerFlinchTimer > 0f) playerFlinchTimer -= delta;
        if (enemyFlinchTimer > 0f) enemyFlinchTimer -= delta;

        // Screen Shake Calculation
        float shakeX = 0f;
        float shakeY = 0f;
        if (screenShakeTimer > 0f) {
            screenShakeTimer -= delta;
            float progress = screenShakeTimer / screenShakeDuration;
            float currentIntensity = screenShakeIntensity * progress;
            shakeX = (MathUtils.random() * 2f - 1f) * currentIntensity;
            shakeY = (MathUtils.random() * 2f - 1f) * currentIntensity;
        }
        worldCamera.position.set(950f + shakeX, 540f + shakeY, 0f);
        worldCamera.update();

        // Sky Color
        Color sky = (tile <= 30) ? Color.valueOf("#42828a") : ((tile <= 70) ? Color.valueOf("#183864") : Color.valueOf("#4a1424"));
        Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // World Render
        worldViewport.apply();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        renderParallaxBackground(tile);
        if (bossVeilAlpha > 0f) {
            batch.setColor(0.08f, 0.03f, 0.06f, bossVeilAlpha);
            batch.draw(whitePixelTex, worldCamera.position.x - worldViewport.getWorldWidth() / 2f - 200f, 0f,
                worldViewport.getWorldWidth() + 400f, worldViewport.getWorldHeight() + 200f);
            batch.setColor(Color.WHITE);
        }
        renderArena(delta);
        renderEffects(delta);
        if (bossFlashAlpha > 0f) {
            batch.setColor(1f, 0.90f, 0.55f, bossFlashAlpha);
            batch.draw(whitePixelTex, worldCamera.position.x - worldViewport.getWorldWidth() / 2f - 200f, 0f,
                worldViewport.getWorldWidth() + 400f, worldViewport.getWorldHeight() + 200f);
            batch.setColor(Color.WHITE);
        }
        batch.end();

        // HUD Render
        hudStage.getViewport().apply();
        hudStage.act(hudDelta);
        hudStage.draw();
        syncUi(); // Keep UI in sync with state machine
        // --- KEYBOARD: NUM 1-5 for Battle Actions (Disabled when Item Popup is open) ---
        if ((itemPopup == null || !itemPopup.isVisible()) && battleSystem.getCurrentState() == BattleState.PLAYER_TURN && playerLungeTimer <= 0f && enemyLungeTimer <= 0f && skillAnimTimer <= 0f) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
                btnAttack.fire(new ChangeListener.ChangeEvent());
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
                btnDefend.fire(new ChangeListener.ChangeEvent());
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
                btnSkill.fire(new ChangeListener.ChangeEvent());
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_4)) {
                btnItem.fire(new ChangeListener.ChangeEvent());
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_5)) {
                btnEscape.fire(new ChangeListener.ChangeEvent());
            }
        }

// --- KEYBOARD: NUM 1-4 for Item Usage (When Item Popup is open) ---
        if (itemPopup != null && itemPopup.isVisible() && battleSystem.getCurrentState() == BattleState.PLAYER_TURN) {
            Inventory inv = player.getInventory();
            if (inv != null) {
                for (int i = 0; i < inv.getCapacity(); i++) {
                    int keyNum = Input.Keys.NUM_1 + i;
                    int numpadNum = Input.Keys.NUMPAD_1 + i;
                    if (Gdx.input.isKeyJustPressed(keyNum) || Gdx.input.isKeyJustPressed(numpadNum)) {
                        if (i < inv.size()) {
                            Item item = inv.getItem(i);
                            // Re-use the exact same validation logic as the USE button
                            boolean isCurse = item.isCurse();
                            io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType eff = item.getEffectType();
                            boolean isBoardOnly = (eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.MOVE
                                || eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.DICE_EXACT
                                || eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.DICE_CLAMP
                                || eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.SNAKE_WARD
                                || eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.SWAP_PLAYER
                                || eff == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.SCOUT);
                            boolean lethalCost = (item.getSecondaryEffectType() == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.HEALTH
                                && item.getSecondaryEffectValue() < 0
                                && player.getHealth() + item.getSecondaryEffectValue() <= 0);
                            boolean canUseInBattle = !isCurse && !isBoardOnly && !lethalCost;

                            if (canUseInBattle) {
                                if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                                spawnHealSparks(PLAYER_X, CHAR_DRAW_Y + 80f);
                                spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, item.getName().toUpperCase(), Color.valueOf("#81c784"), false);
                                battleSystem.onPlayerUseItem(i);
                                itemPopup.setVisible(false);
                                syncUi();
                            }
                        }
                    }
                }
            }
        }

        if (!isPaused) {
            syncUi(); // Keep UI in sync with state machine
        }
    }

    private void renderArena(float delta) {
        Texture stoneNormal = game.getAssetManager().get(Assets.TILE_STONE_NORMAL);
        Texture stoneSnake = game.getAssetManager().get(isBossBattle() ? Assets.TILE_STONE_LADDER : Assets.TILE_STONE_SNAKE);

        // Draw Tiles
        batch.draw(stoneNormal, PLAYER_X - TILE_WIDTH/2, TILE_BOTTOM_Y, TILE_WIDTH, TILE_HEIGHT);
        batch.draw(stoneSnake, ENEMY_X - TILE_WIDTH/2, TILE_BOTTOM_Y, TILE_WIDTH, TILE_HEIGHT);

        // Player Kinematics
        float pLungeX = 0f;
        float pLungeY = 0f;
        float pRotation = 0f;
        float pScaleX = 1.0f;
        float pScaleY = 1.0f;
        boolean drawGhostTrail = false;

        if (skillAnimTimer > 0f) {
            float p = 1.0f - (skillAnimTimer / skillAnimDuration);
            if (activeSkillClass == CharacterClass.CAT) {
                drawGhostTrail = true;
                if (p < 0.48f) {
                    float s = p / 0.48f;
                    pLungeX = MathUtils.sin(s * MathUtils.PI) * 520f;
                    pLungeY = MathUtils.sin(s * MathUtils.PI) * 20f;
                    pRotation = -MathUtils.sin(s * MathUtils.PI) * 10f;
                } else {
                    float s = (p - 0.48f) / 0.52f;
                    pLungeX = MathUtils.sin(s * MathUtils.PI) * 550f;
                    pLungeY = MathUtils.sin(s * MathUtils.PI) * 32f;
                    pRotation = -MathUtils.sin(s * MathUtils.PI) * 14f;
                }
            } else if (activeSkillClass == CharacterClass.TURTLE) {
                float brace = MathUtils.sin(p * MathUtils.PI);
                pLungeY = -brace * 12f;
                pScaleX = 1.0f + brace * 0.14f;
                pScaleY = 1.0f - brace * 0.10f;
            } else if (activeSkillClass == CharacterClass.FOX) {
                float s = MathUtils.sin(p * MathUtils.PI);
                pLungeX = s * 540f;
                pLungeY = s * 115f;
                pRotation = -s * 20f;
            } else if (activeSkillClass == CharacterClass.RACCOON) {
                float s = MathUtils.sin(p * MathUtils.PI);
                pLungeX = s * 510f;
                pLungeY = s * 35f;
                pRotation = -p * 360f;
            }
        } else if (playerLungeTimer > 0f) {
            float p = 1.0f - (playerLungeTimer / PLAYER_LUNGE_DURATION);
            if (p < 0.15f) {
                float w = p / 0.15f;
                pLungeX = -MathUtils.sin(w * MathUtils.PI) * 25f;
                pRotation = 4f;
            } else {
                float s = (p - 0.15f) / 0.85f;
                pLungeX = MathUtils.sin(s * MathUtils.PI) * 520f;
                pLungeY = MathUtils.sin(s * MathUtils.PI) * 28f;
                pRotation = -MathUtils.sin(s * MathUtils.PI) * 12f;
            }
        }
        float pFlinchX = 0f;
        if (playerFlinchTimer > 0f) {
            float p = playerFlinchTimer / FLINCH_DURATION;
            pFlinchX = -MathUtils.sin(playerFlinchTimer * 40f) * p * 26f;
        }

        // Draw Player Ghost Afterimage Trail (Cat Double Strike)
        TextureRegion pFrame = playerIdleAnim != null ? playerIdleAnim.getKeyFrame(stateTime, true) : null;
        float bob = (player.getHealth() <= 0) ? 0f : MathUtils.sin(stateTime * 3f) * 4f;
        float drawPX = PLAYER_X + pLungeX + pFlinchX;
        float drawPY = CHAR_DRAW_Y + (player.getHealth() <= 0 ? -12f : bob) + pLungeY;
        float drawH = (player.getHealth() <= 0) ? CHAR_DISPLAY_SIZE * 0.72f : CHAR_DISPLAY_SIZE;

        if (drawGhostTrail && pFrame != null && Math.abs(pLungeX) > 20f) {
            batch.setColor(0.15f, 0.85f, 1.0f, 0.40f);
            batch.draw(pFrame,
                drawPX - CHAR_DISPLAY_SIZE / 2 - Math.signum(pLungeX) * 38f, drawPY,
                CHAR_DISPLAY_SIZE / 2, CHAR_DISPLAY_SIZE / 2,
                CHAR_DISPLAY_SIZE, drawH,
                pScaleX, pScaleY, pRotation);
            batch.setColor(Color.WHITE);
        }

        // Turtle Fortress Barrier Aura (Drawn around player platform)
        if (radialGlowTex != null && (battleSystem.isTurtleFortressActive() || (skillAnimTimer > 0f && activeSkillClass == CharacterClass.TURTLE))) {
            float barrierCenterY = CHAR_DRAW_Y + 75f;
            float pulse = 0.70f + 0.30f * MathUtils.sin(stateTime * 4.5f);

            // Shimmering outer emerald dome
            batch.setColor(0.12f, 0.85f, 0.35f, 0.32f * pulse);
            batch.draw(radialGlowTex, PLAYER_X - 150f, barrierCenterY - 150f, 300f, 300f);

            // Radiant gold-emerald inner shield core
            batch.setColor(1.0f, 0.90f, 0.30f, 0.26f * pulse);
            batch.draw(radialGlowTex, PLAYER_X - 100f, barrierCenterY - 100f, 200f, 200f);

            // Rotating crystalline fortress rune motes
            for (int k = 0; k < 4; k++) {
                float ang = stateTime * 2.2f + k * (MathUtils.PI / 2f);
                float rx = PLAYER_X + MathUtils.cos(ang) * 115f;
                float ry = barrierCenterY + MathUtils.sin(ang) * 55f;
                batch.setColor(0.4f, 1.0f, 0.6f, 0.85f);
                batch.draw(whitePixelTex, rx - 5f, ry - 5f, 5f, 5f, 10f, 10f, 1f, 1f, 45f + stateTime * 90f, 0, 0, 1, 1, false, false);
                batch.setColor(1.0f, 1.0f, 1.0f, 0.95f);
                batch.draw(whitePixelTex, rx - 2.5f, ry - 2.5f, 2.5f, 2.5f, 5f, 5f, 1f, 1f, 45f + stateTime * 90f, 0, 0, 1, 1, false, false);
            }
            batch.setColor(Color.WHITE);
        }

        // Draw Player
        if (pFrame != null) {
            float pAlpha = (player.getHealth() <= 0) ? 0.45f : 1.0f;
            if (playerFlinchTimer > 0.12f) {
                batch.setColor(1f, 0.25f, 0.25f, 1f); // Red hurt flash
            } else if (player.getHealth() <= 0) {
                batch.setColor(0.45f, 0.45f, 0.50f, pAlpha); // Fallen knocked out tint
            } else {
                batch.setColor(Color.WHITE);
            }
            batch.draw(pFrame,
                drawPX - CHAR_DISPLAY_SIZE / 2, drawPY,
                CHAR_DISPLAY_SIZE / 2, CHAR_DISPLAY_SIZE / 2,
                CHAR_DISPLAY_SIZE, drawH,
                pScaleX, pScaleY, pRotation);
            batch.setColor(Color.WHITE);
        }

        // Enemy Kinematics
        float eLungeX = 0f;
        float eLungeY = 0f;
        float eRotation = 0f;
        if (enemyLungeTimer > 0f) {
            float p = 1.0f - (enemyLungeTimer / ENEMY_LUNGE_DURATION);
            if (p < 0.18f) {
                float w = p / 0.18f;
                eLungeX = MathUtils.sin(w * MathUtils.PI) * 35f;
                eRotation = -6f;
            } else {
                float s = (p - 0.18f) / 0.82f;
                eLungeX = -MathUtils.sin(s * MathUtils.PI) * 520f;
                eLungeY = MathUtils.sin(s * MathUtils.PI) * 26f;
                eRotation = MathUtils.sin(s * MathUtils.PI) * 12f;
            }
        }
        float eFlinchX = 0f;
        if (enemyFlinchTimer > 0f) {
            float p = enemyFlinchTimer / FLINCH_DURATION;
            eFlinchX = MathUtils.sin(enemyFlinchTimer * 40f) * p * 26f;
        }

        // Draw Enemy
        TextureRegion eFrame;
        if (enemyLungeTimer > 0f && enemyAttackAnim != null) {
            float lungeProgress = 1.0f - (enemyLungeTimer / ENEMY_LUNGE_DURATION);
            float animTime = lungeProgress * enemyAttackAnim.getAnimationDuration();
            eFrame = enemyAttackAnim.getKeyFrame(animTime, true);
        } else if (isDawnWardenBattle() && bossEntranceActive && !bossLanded && enemyAttackAnim != null) {
            eFrame = enemyAttackAnim.getKeyFrame(stateTime, true);
        } else {
            eFrame = enemyIdleAnim != null ? enemyIdleAnim.getKeyFrame(stateTime, true) : null;
        }
        float eBob = isBossBattle() ? 0f : (MathUtils.sin(stateTime * 3f + 1f) * 2.5f);
        float descentOffsetY = 0f;
        float entranceScale = 1.0f;
        float entranceAlphaScale = 1.0f;
        if (isDawnWardenBattle() && bossEntranceActive) {
            if (bossEntranceTimer < BOSS_DESCENT_START) {
                entranceAlphaScale = 0f;
            } else if (!bossLanded) {
                float p = Math.min(1f, (bossEntranceTimer - BOSS_DESCENT_START) / Math.max(0.05f, BOSS_LAND_TIME - BOSS_DESCENT_START));
                float eased = 1f - (1f - p) * (1f - p);
                descentOffsetY = (1f - eased) * 520f;
                entranceScale = 0.86f + eased * 0.14f;
                entranceAlphaScale = Math.min(1f, p * 2.2f);
            }
        }

        if (eFrame != null && entranceAlphaScale > 0f) {
            float enemyAlpha = ((enemy.getHealth() <= 0) ? 0.35f : 1.0f) * entranceAlphaScale;
            float hover = isPaleShepherdBattle() ? ((float) Math.sin(stateTime * 2.2f) * 8f) : 0f;
            float drawEX = ENEMY_X + eLungeX + eFlinchX;

            float enemyW;
            float enemyH;
            float bottomPadPx;
            if (isPaleShepherdBattle()) {
                enemyW = 280f;
                enemyH = (enemy.getHealth() <= 0) ? 220f : 280f;
                bottomPadPx = 0f;
            } else if (isBossBattle()) {
                enemyW = 320f;
                enemyH = (enemy.getHealth() <= 0) ? 250f : 350f;
                bottomPadPx = 0f;
            } else {
                // Act-stratified serpent combat sizing: visible body scaled to match hero's stature (~150-180px wide, ~110-150px tall)
                if (currentSnakeAct == 1) {
                    enemyW = 440f;
                    enemyH = 440f;
                    bottomPadPx = 17f;
                } else if (currentSnakeAct == 2) {
                    enemyW = 270f;
                    enemyH = 270f;
                    bottomPadPx = 17f;
                } else {
                    enemyW = 290f;
                    enemyH = 290f;
                    bottomPadPx = 18f;
                }
                if (enemy.getHealth() <= 0) {
                    enemyH *= 0.72f;
                }
            }
            enemyW *= entranceScale;
            enemyH *= entranceScale;

            float enemyBaseY;
            if (isPaleShepherdBattle() || isBossBattle()) {
                enemyBaseY = ENEMY_DRAW_Y;
            } else {
                float padOffset = (bottomPadPx / 128f) * enemyH;
                enemyBaseY = 282f - padOffset;
            }
            float drawEY = enemyBaseY + (isPaleShepherdBattle() ? (16f + hover) : (enemy.getHealth() <= 0 ? -12f : eBob)) + eLungeY + descentOffsetY;

            // Smooth circular radial solar aura behind The Dawn Warden (zero square edges)
            if (isDawnWardenBattle() && enemy.getHealth() > 0 && bossLanded && radialGlowTex != null) {
                float pulse = 0.5f + 0.5f * MathUtils.sin(stateTime * (bossEnraged ? 6f : 3.2f));
                Color haloOuter = bossEnraged ? new Color(1f, 0.35f, 0.10f, 0.28f + pulse * 0.16f) : new Color(1f, 0.78f, 0.25f, 0.22f + pulse * 0.14f);
                Color haloInner = bossEnraged ? new Color(1f, 0.78f, 0.30f, 0.30f + pulse * 0.16f) : new Color(1f, 0.94f, 0.58f, 0.24f + pulse * 0.14f);
                float centerY = drawEY + enemyH * 0.48f;
                batch.setColor(haloOuter);
                batch.draw(radialGlowTex, drawEX - 190f, centerY - 190f, 380f, 380f);
                batch.setColor(haloInner);
                batch.draw(radialGlowTex, drawEX - 125f, centerY - 125f, 250f, 250f);
            } else if (isPaleShepherdBattle() && enemy.getHealth() > 0 && radialGlowTex != null) {
                float pulse = 0.5f + 0.5f * MathUtils.sin(stateTime * 2.8f);
                Color haloOuter = new Color(0.38f, 0.05f, 0.08f, 0.35f + pulse * 0.15f);
                Color haloInner = new Color(0.85f, 0.12f, 0.18f, 0.22f + pulse * 0.12f);
                float centerY = drawEY + enemyH * 0.50f;
                batch.setColor(haloOuter);
                batch.draw(radialGlowTex, drawEX - 170f, centerY - 170f, 340f, 340f);
                batch.setColor(haloInner);
                batch.draw(radialGlowTex, drawEX - 110f, centerY - 110f, 220f, 220f);
            }

            if (enemyFlinchTimer > 0.12f) {
                batch.setColor(1f, 0.25f, 0.25f, enemyAlpha); // Red hurt flash
            } else if (isPaleShepherdBattle()) {
                batch.setColor(1.0f, 0.88f, 0.88f, enemyAlpha); // Spectral Pale Crimson Light
            } else if (isDawnWardenBattle() || isBossBattle()) {
                if (bossEnraged) {
                    batch.setColor(1.0f, 0.82f, 0.62f, enemyAlpha); // Intense Solar Fury glow
                } else {
                    batch.setColor(1.0f, 0.95f, 0.80f, enemyAlpha); // Radiant Golden Dawn Light
                }
            } else {
                if (currentSnakeAct == 1) {
                    batch.setColor(0.85f, 1.0f, 0.88f, enemyAlpha); // Act 1: Verdant Moss / Emerald Hue
                } else if (currentSnakeAct == 2) {
                    batch.setColor(0.80f, 0.88f, 1.0f, enemyAlpha); // Act 2: Deep Cavern Sapphire / Indigo Hue
                } else {
                    batch.setColor(1.0f, 0.78f, 0.72f, enemyAlpha); // Act 3: Scorched Magma / Crimson Ember Hue
                }
            }

            batch.draw(eFrame,
                drawEX - enemyW / 2, drawEY,
                enemyW / 2, enemyH / 2,
                enemyW, enemyH,
                1f, 1f, eRotation);
            batch.setColor(Color.WHITE);
        }

        // Fox Vital Strike Target Reticle (Locks onto enemy core during skill leap)
        if (skillAnimTimer > 0f && activeSkillClass == CharacterClass.FOX && enemy != null && enemy.getHealth() > 0) {
            float p = 1.0f - (skillAnimTimer / skillAnimDuration);
            float reticleAlpha = (p < 0.55f) ? Math.min(1.0f, p * 3.5f) : Math.max(0f, 1.0f - (p - 0.55f) / 0.30f);
            if (reticleAlpha > 0f) {
                float targetCX = ENEMY_X;
                float targetCY = getEnemyCenterY();
                float contract = (p < 0.45f) ? (1.6f - (p / 0.45f) * 0.6f) : 1.0f;
                float reticleR = 48f * contract;
                float spinAngle = p * 120f;

                int sides = 16;
                float segThick = 2.5f;
                for (int s = 0; s < sides; s++) {
                    float a1 = spinAngle + (s / (float) sides) * 360f;
                    float a2 = spinAngle + ((s + 0.65f) / (float) sides) * 360f;
                    float x1 = targetCX + MathUtils.cosDeg(a1) * reticleR;
                    float y1 = targetCY + MathUtils.sinDeg(a1) * reticleR;
                    float x2 = targetCX + MathUtils.cosDeg(a2) * reticleR;
                    float y2 = targetCY + MathUtils.sinDeg(a2) * reticleR;
                    float dx = x2 - x1;
                    float dy = y2 - y1;
                    float dLen = (float) Math.sqrt(dx * dx + dy * dy);
                    float segAngle = MathUtils.radDeg * MathUtils.atan2(dy, dx);

                    batch.setColor(1.0f, 0.2f, 0.15f, reticleAlpha * 0.85f);
                    batch.draw(whitePixelTex, x1, y1 - segThick / 2, 0f, segThick / 2, dLen, segThick, 1f, 1f, segAngle, 0, 0, 1, 1, false, false);
                }

                float crossArm = 24f;
                float innerGap = 8f;
                float crossThick = 2f;
                batch.setColor(1.0f, 0.85f, 0.2f, reticleAlpha * 0.95f);
                batch.draw(whitePixelTex, targetCX - crossThick / 2, targetCY + innerGap, crossThick, crossArm);
                batch.draw(whitePixelTex, targetCX - crossThick / 2, targetCY - innerGap - crossArm, crossThick, crossArm);
                batch.draw(whitePixelTex, targetCX - innerGap - crossArm, targetCY - crossThick / 2, crossArm, crossThick);
                batch.draw(whitePixelTex, targetCX + innerGap, targetCY - crossThick / 2, crossArm, crossThick);

                batch.setColor(1.0f, 0.1f, 0.1f, reticleAlpha);
                batch.draw(whitePixelTex, targetCX - 3f, targetCY - 3f, 6f, 6f);
                batch.setColor(Color.WHITE);
            }
        }
    }

    private void completeBossEntrance() {
        bossEntranceActive = false;
        bossLanded = true;
        bossVeilAlpha = 0f;
        syncUi();
    }

    private void renderEffects(float delta) {
        // 1. Shield Pulses & Radial Shockwaves
        for (int i = shieldPulses.size - 1; i >= 0; i--) {
            ShieldPulse pulse = shieldPulses.get(i);
            pulse.timer -= delta;
            if (pulse.timer <= 0f) {
                shieldPulses.removeIndex(i);
                continue;
            }
            float t = 1.0f - (pulse.timer / pulse.maxDuration);
            float alpha = pulse.timer / pulse.maxDuration;
            float radius = pulse.startRadius + t * pulse.expandDistance;
            float segThick = Math.max(2f, (1.0f - t) * 14f);
            int sides = 14;
            for (int s = 0; s < sides; s++) {
                float a1 = (s * 360f / sides) * MathUtils.degRad;
                float a2 = ((s + 1) * 360f / sides) * MathUtils.degRad;
                float x1 = pulse.x + MathUtils.cos(a1) * radius;
                float y1 = pulse.y + MathUtils.sin(a1) * radius;
                float x2 = pulse.x + MathUtils.cos(a2) * radius;
                float y2 = pulse.y + MathUtils.sin(a2) * radius;
                float dx = x2 - x1;
                float dy = y2 - y1;
                float dLen = (float) Math.sqrt(dx * dx + dy * dy);
                float angle = MathUtils.radDeg * MathUtils.atan2(dy, dx);

                batch.setColor(pulse.outerColor.r, pulse.outerColor.g, pulse.outerColor.b, alpha * 0.72f);
                batch.draw(whitePixelTex, x1, y1 - segThick / 2, 0f, segThick / 2, dLen, segThick, 1f, 1f, angle, 0, 0, 1, 1, false, false);
                batch.setColor(pulse.innerColor.r, pulse.innerColor.g, pulse.innerColor.b, alpha * 0.92f);
                batch.draw(whitePixelTex, x1, y1 - (segThick * 0.4f) / 2, 0f, (segThick * 0.4f) / 2, dLen, segThick * 0.4f, 1f, 1f, angle, 0, 0, 1, 1, false, false);
            }
        }

        // 2. Slashes (Player Sword, Snake Fangs, Dawn Warden Solar Claws, Pale Shepherd Scythe)
        for (int i = slashEffects.size - 1; i >= 0; i--) {
            SlashEffect slash = slashEffects.get(i);
            slash.timer -= delta;
            if (slash.timer <= 0f) {
                slashEffects.removeIndex(i);
                continue;
            }
            float t = 1.0f - (slash.timer / slash.maxDuration);
            float alpha = slash.timer / slash.maxDuration;

            if (slash.style == SlashStyle.SNAKE_FANGS) {
                float fangLen = Math.min(230f, (t / 0.3f) * 230f);
                float fangThick = Math.max(2f, (1.0f - t) * 22f);

                float lx = slash.x - 35f;
                float ly = slash.y;
                float la = -65f;
                batch.setColor(0.65f, 0.1f, 0.95f, alpha * 0.5f);
                batch.draw(whitePixelTex, lx - fangLen / 2, ly - (fangThick * 1.6f) / 2, fangLen / 2, (fangThick * 1.6f) / 2, fangLen, fangThick * 1.6f, 1f, 1f, la, 0, 0, 1, 1, false, false);
                batch.setColor(0.2f, 1.0f, 0.2f, alpha * 0.9f);
                batch.draw(whitePixelTex, lx - fangLen / 2, ly - fangThick / 2, fangLen / 2, fangThick / 2, fangLen * 0.95f, fangThick, 1f, 1f, la, 0, 0, 1, 1, false, false);
                batch.setColor(0.9f, 1.0f, 0.85f, alpha);
                batch.draw(whitePixelTex, lx - fangLen / 2, ly - (fangThick * 0.35f) / 2, fangLen / 2, (fangThick * 0.35f) / 2, fangLen * 0.9f, fangThick * 0.35f, 1f, 1f, la, 0, 0, 1, 1, false, false);

                float rx = slash.x + 35f;
                float ry = slash.y;
                float ra = +65f;
                batch.setColor(0.65f, 0.1f, 0.95f, alpha * 0.5f);
                batch.draw(whitePixelTex, rx - fangLen / 2, ry - (fangThick * 1.6f) / 2, fangLen / 2, (fangThick * 1.6f) / 2, fangLen, fangThick * 1.6f, 1f, 1f, ra, 0, 0, 1, 1, false, false);
                batch.setColor(0.2f, 1.0f, 0.2f, alpha * 0.9f);
                batch.draw(whitePixelTex, rx - fangLen / 2, ry - fangThick / 2, fangLen / 2, fangThick / 2, fangLen * 0.95f, fangThick, 1f, 1f, ra, 0, 0, 1, 1, false, false);
                batch.setColor(0.9f, 1.0f, 0.85f, alpha);
                batch.draw(whitePixelTex, rx - fangLen / 2, ry - (fangThick * 0.35f) / 2, fangLen / 2, (fangThick * 0.35f) / 2, fangLen * 0.9f, fangThick * 0.35f, 1f, 1f, ra, 0, 0, 1, 1, false, false);
            } else if (slash.style == SlashStyle.DAWN_SOLAR_CLAW) {
                float len = Math.min(310f, (t / 0.28f) * 310f);
                float thickness = Math.max(2.5f, (1.0f - t) * 20f);
                float[] offsets = { -26f, 0f, 26f };
                for (float off : offsets) {
                    float cx = slash.x + off * 0.7f;
                    float cy = slash.y + off;
                    batch.setColor(1.0f, 0.38f, 0.08f, alpha * 0.55f);
                    batch.draw(whitePixelTex, cx - len / 2, cy - (thickness * 1.6f) / 2, len / 2, (thickness * 1.6f) / 2, len, thickness * 1.6f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                    batch.setColor(1.0f, 0.85f, 0.25f, alpha * 0.92f);
                    batch.draw(whitePixelTex, cx - len / 2, cy - thickness / 2, len / 2, thickness / 2, len * 0.95f, thickness, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                    batch.setColor(1.0f, 0.98f, 0.85f, alpha);
                    batch.draw(whitePixelTex, cx - len / 2, cy - (thickness * 0.35f) / 2, len / 2, (thickness * 0.35f) / 2, len * 0.9f, thickness * 0.35f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                }
            } else if (slash.style == SlashStyle.SHEPHERD_SCYTHE) {
                float len = Math.min(300f, (t / 0.28f) * 300f);
                float thickness = Math.max(2.5f, (1.0f - t) * 24f);
                batch.setColor(0.85f, 0.12f, 0.20f, alpha * 0.55f);
                batch.draw(whitePixelTex, slash.x - len / 2, slash.y - (thickness * 1.7f) / 2, len / 2, (thickness * 1.7f) / 2, len, thickness * 1.7f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                batch.setColor(1.0f, 0.82f, 0.35f, alpha * 0.9f);
                batch.draw(whitePixelTex, slash.x - len / 2, slash.y - thickness / 2, len / 2, thickness / 2, len * 0.95f, thickness, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
            } else if (slash.style == SlashStyle.CAT_DUAL_CLAW) {
                float len = Math.min(260f, (t / 0.22f) * 260f);
                float thickness = Math.max(2.2f, (1.0f - t) * 18f);
                float[] rakeOffsets = { -16f, 0f, 16f };
                for (float off : rakeOffsets) {
                    float cx = slash.x + MathUtils.cosDeg(slash.angle + 90f) * off;
                    float cy = slash.y + MathUtils.sinDeg(slash.angle + 90f) * off;
                    batch.setColor(0.0f, 0.85f, 1.0f, alpha * 0.65f);
                    batch.draw(whitePixelTex, cx - len / 2, cy - (thickness * 1.6f) / 2, len / 2, (thickness * 1.6f) / 2, len, thickness * 1.6f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                    batch.setColor(0.9f, 0.95f, 1.0f, alpha * 0.95f);
                    batch.draw(whitePixelTex, cx - len / 2, cy - thickness / 2, len / 2, thickness / 2, len * 0.96f, thickness, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                    batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                    batch.draw(whitePixelTex, cx - len / 2, cy - (thickness * 0.35f) / 2, len / 2, (thickness * 0.35f) / 2, len * 0.9f, thickness * 0.35f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                }
            } else if (slash.style == SlashStyle.FOX_VITAL_PIERCE) {
                float beamLen = Math.min(380f, (t / 0.20f) * 380f);
                float beamThick = Math.max(3f, (1.0f - t) * 26f);
                batch.setColor(1.0f, 0.15f, 0.20f, alpha * 0.65f);
                batch.draw(whitePixelTex, slash.x - beamLen / 2, slash.y - (beamThick * 1.8f) / 2, beamLen / 2, (beamThick * 1.8f) / 2, beamLen, beamThick * 1.8f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                batch.setColor(1.0f, 0.82f, 0.15f, alpha * 0.95f);
                batch.draw(whitePixelTex, slash.x - beamLen / 2, slash.y - beamThick / 2, beamLen / 2, beamThick / 2, beamLen * 0.98f, beamThick, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                batch.draw(whitePixelTex, slash.x - beamLen / 2, slash.y - (beamThick * 0.35f) / 2, beamLen / 2, (beamThick * 0.35f) / 2, beamLen * 0.92f, beamThick * 0.35f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                float needleLen = beamLen * 0.45f;
                float needleThick = beamThick * 0.6f;
                batch.setColor(1.0f, 0.4f, 0.2f, alpha * 0.6f);
                batch.draw(whitePixelTex, slash.x - needleLen / 2, slash.y - needleThick / 2, needleLen / 2, needleThick / 2, needleLen, needleThick, 1f, 1f, slash.angle + 90f, 0, 0, 1, 1, false, false);
                batch.setColor(1.0f, 0.95f, 0.7f, alpha * 0.9f);
                batch.draw(whitePixelTex, slash.x - needleLen / 2, slash.y - (needleThick * 0.35f) / 2, needleLen / 2, (needleThick * 0.35f) / 2, needleLen * 0.85f, needleThick * 0.35f, 1f, 1f, slash.angle + 90f, 0, 0, 1, 1, false, false);
            } else if (slash.style == SlashStyle.RACCOON_HOOK_SWIPE) {
                float arcLen = Math.min(290f, (t / 0.24f) * 290f);
                float thickness = Math.max(2.8f, (1.0f - t) * 22f);
                batch.setColor(0.3f, 0.85f, 0.95f, alpha * 0.55f);
                batch.draw(whitePixelTex, slash.x - arcLen / 2, slash.y - (thickness * 1.6f) / 2, arcLen / 2, (thickness * 1.6f) / 2, arcLen, thickness * 1.6f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                batch.setColor(1.0f, 0.88f, 0.35f, alpha * 0.92f);
                batch.draw(whitePixelTex, slash.x - arcLen / 2, slash.y - thickness / 2, arcLen / 2, thickness / 2, arcLen * 0.95f, thickness, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                batch.draw(whitePixelTex, slash.x - arcLen / 2, slash.y - (thickness * 0.35f) / 2, arcLen / 2, (thickness * 0.35f) / 2, arcLen * 0.9f, thickness * 0.35f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                float hookTrail = arcLen * 0.65f;
                batch.setColor(1.0f, 0.78f, 0.2f, alpha * 0.45f);
                batch.draw(whitePixelTex, slash.x - hookTrail / 2 - 14f, slash.y - (thickness * 0.8f) / 2 + 10f, hookTrail / 2, (thickness * 0.8f) / 2, hookTrail, thickness * 0.8f, 1f, 1f, slash.angle - 22f, 0, 0, 1, 1, false, false);
            } else if (slash.style == SlashStyle.TURTLE_RIPOSTE) {
                float waveLen = Math.min(340f, (t / 0.22f) * 340f);
                float thickness = Math.max(3.5f, (1.0f - t) * 32f);
                batch.setColor(0.05f, 0.85f, 0.35f, alpha * 0.65f);
                batch.draw(whitePixelTex, slash.x - waveLen / 2, slash.y - (thickness * 1.8f) / 2, waveLen / 2, (thickness * 1.8f) / 2, waveLen, thickness * 1.8f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                batch.setColor(1.0f, 0.85f, 0.25f, alpha * 0.95f);
                batch.draw(whitePixelTex, slash.x - waveLen / 2, slash.y - thickness / 2, waveLen / 2, thickness / 2, waveLen * 0.95f, thickness, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                batch.draw(whitePixelTex, slash.x - waveLen / 2, slash.y - (thickness * 0.35f) / 2, waveLen / 2, (thickness * 0.35f) / 2, waveLen * 0.9f, thickness * 0.35f, 1f, 1f, slash.angle, 0, 0, 1, 1, false, false);
                float wingLen = waveLen * 0.5f;
                float wingThick = thickness * 0.6f;
                batch.setColor(0.1f, 0.9f, 0.5f, alpha * 0.5f);
                batch.draw(whitePixelTex, slash.x - wingLen / 2, slash.y - wingThick / 2 + 18f, wingLen / 2, wingThick / 2, wingLen, wingThick, 1f, 1f, slash.angle + 35f, 0, 0, 1, 1, false, false);
                batch.draw(whitePixelTex, slash.x - wingLen / 2, slash.y - wingThick / 2 - 18f, wingLen / 2, wingThick / 2, wingLen, wingThick, 1f, 1f, slash.angle - 35f, 0, 0, 1, 1, false, false);
            } else {
                // Radiant Sword Slash Arc
                float len = Math.min(270f, (t / 0.3f) * 270f);
                float thickness = Math.max(2f, (1.0f - t) * 24f);
                float angle = slash.angle;

                batch.setColor(1.0f, 0.7f, 0.1f, alpha * 0.5f);
                batch.draw(whitePixelTex, slash.x - len / 2, slash.y - (thickness * 1.6f) / 2, len / 2, (thickness * 1.6f) / 2, len, thickness * 1.6f, 1f, 1f, angle, 0, 0, 1, 1, false, false);

                batch.setColor(1.0f, 0.95f, 0.35f, alpha * 0.85f);
                batch.draw(whitePixelTex, slash.x - len / 2, slash.y - thickness / 2, len / 2, thickness / 2, len * 0.95f, thickness, 1f, 1f, angle, 0, 0, 1, 1, false, false);

                batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                batch.draw(whitePixelTex, slash.x - len / 2, slash.y - (thickness * 0.35f) / 2, len / 2, (thickness * 0.35f) / 2, len * 0.9f, thickness * 0.35f, 1f, 1f, angle, 0, 0, 1, 1, false, false);

                float trailLen = len * 0.75f;
                float trailThick = thickness * 0.55f;
                batch.setColor(1.0f, 0.8f, 0.2f, alpha * 0.45f);
                batch.draw(whitePixelTex, slash.x - trailLen / 2 - 12f, slash.y - trailThick / 2 + 14f, trailLen / 2, trailThick / 2, trailLen, trailThick, 1f, 1f, angle + 14f, 0, 0, 1, 1, false, false);

                if (slash.isCrit) {
                    float crossAngle = angle + 80f;
                    batch.setColor(1.0f, 0.3f, 0.1f, alpha * 0.55f);
                    batch.draw(whitePixelTex, slash.x - len / 2, slash.y - (thickness * 1.6f) / 2, len / 2, (thickness * 1.6f) / 2, len, thickness * 1.6f, 1f, 1f, crossAngle, 0, 0, 1, 1, false, false);

                    batch.setColor(1.0f, 0.9f, 0.35f, alpha * 0.85f);
                    batch.draw(whitePixelTex, slash.x - len / 2, slash.y - thickness / 2, len / 2, thickness / 2, len * 0.95f, thickness, 1f, 1f, crossAngle, 0, 0, 1, 1, false, false);

                    batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                    batch.draw(whitePixelTex, slash.x - len / 2, slash.y - (thickness * 0.35f) / 2, len / 2, (thickness * 0.35f) / 2, len * 0.9f, thickness * 0.35f, 1f, 1f, crossAngle, 0, 0, 1, 1, false, false);
                }
            }
        }

        // 3. Hit Sparks & Particles
        for (int i = hitSparks.size - 1; i >= 0; i--) {
            HitSpark s = hitSparks.get(i);
            s.timer -= delta;
            if (s.timer <= 0f) {
                hitSparks.removeIndex(i);
                continue;
            }
            s.x += s.vx * delta;
            s.y += s.vy * delta;
            s.vy -= s.gravity * delta;
            s.vx *= (1.0f - 2.5f * delta); // Drag

            float p = s.timer / s.maxDuration;
            float alpha = Math.min(1.0f, p * 1.5f);
            float speed = (float) Math.sqrt(s.vx * s.vx + s.vy * s.vy);
            float sparkLen = Math.max(s.size, speed * 0.038f);
            float angle = MathUtils.radDeg * MathUtils.atan2(s.vy, s.vx);

            batch.setColor(s.color.r, s.color.g, s.color.b, s.color.a * alpha);
            batch.draw(whitePixelTex, s.x - sparkLen / 2, s.y - s.size / 2, sparkLen / 2, s.size / 2, sparkLen, s.size, 1f, 1f, angle, 0, 0, 1, 1, false, false);
        }
        batch.setColor(Color.WHITE);

        // 4. Floating Combat Numbers & Text
        for (int i = damageNumbers.size - 1; i >= 0; i--) {
            DamageNumber num = damageNumbers.get(i);
            num.timer -= delta;
            if (num.timer <= 0f) {
                damageNumbers.removeIndex(i);
                continue;
            }
            float p = 1.0f - (num.timer / num.maxDuration);
            float rise = (float)(1.0 - Math.pow(1.0 - p, 2.5)) * 95f;
            float curY = num.startY + rise;
            float alpha = (num.timer < 0.35f) ? (num.timer / 0.35f) : 1.0f;

            float scale = (p < 0.15f) ? (0.8f + (p / 0.15f) * 0.45f) : (1.25f - (p - 0.15f) * 0.25f);
            if (num.isCrit) scale *= 1.25f;

            BitmapFont font = num.isCrit ? critFont : damageFont;
            font.getData().setScale(scale);

            // Drop shadow (offset 2px down-right)
            font.setColor(0f, 0f, 0f, alpha * 0.85f);
            font.draw(batch, num.text, num.x + 2f, curY - 2f, 0, Align.center, false);

            // Foreground text
            font.setColor(num.color.r, num.color.g, num.color.b, alpha);
            font.draw(batch, num.text, num.x, curY, 0, Align.center, false);

            font.getData().setScale(1.0f);
            font.setColor(Color.WHITE);
        }
    }

    private void renderParallaxBackground(int currentTile) {
        float camX = worldCamera.position.x;
        float viewW = worldViewport.getWorldWidth();
        float viewH = worldViewport.getWorldHeight();
        float leftVisible = camX - viewW / 2f;
        float rightVisible = camX + viewW / 2f;

        TextureRegion farSlice = (currentTile <= 30) ? act1Far : ((currentTile <= 70) ? act2Far : act3Far);
        TextureRegion midSlice = (currentTile <= 30) ? act1Mid : ((currentTile <= 70) ? act2Mid : act3Mid);
        TextureRegion groundSlice = (currentTile <= 30) ? act1Ground : ((currentTile <= 70) ? act2Ground : act3Ground);

        // Layer 0: Subterranean
        Color lowerBg = (currentTile <= 30) ? Color.valueOf("#20202a") : ((currentTile <= 70) ? Color.valueOf("#0a204f") : Color.valueOf("#2e1534"));
        batch.setColor(lowerBg);
        batch.draw(whitePixelTex, leftVisible - 200f, 0f, viewW + 400f, GROUND_BASE_Y + 25f);
        batch.setColor(Color.WHITE);

        // Layer 1: Far Canopy
        float farH = Math.max(920f, viewH - 190f) + 120f;
        float farW = farH * ((float) farSlice.getRegionWidth() / farSlice.getRegionHeight());
        float stepFar = farW - 2f;
        float farOffset = (camX * 0.15f) % stepFar;
        if (farOffset < 0) farOffset += stepFar;
        float farStart = leftVisible - farOffset;
        while (farStart > leftVisible - farW) farStart -= stepFar;
        for (float x = farStart; x < rightVisible + farW; x += stepFar) batch.draw(farSlice, x, 190f, farW, farH);

        // Layer 2: Midground
        float midH = 560f;
        float midW = midH * ((float) midSlice.getRegionWidth() / midSlice.getRegionHeight());
        float stepMid = midW - 2f;
        float midOffset = (camX * 0.35f) % stepMid;
        if (midOffset < 0) midOffset += stepMid;
        float midStart = leftVisible - midOffset;
        while (midStart > leftVisible - midW) midStart -= stepMid;
        for (float x = midStart; x < rightVisible + midW; x += stepMid) batch.draw(midSlice, x, 210f, midW, midH);

        // Layer 3: Ground
        float groundH = 260f;
        float groundW = groundH * ((float) groundSlice.getRegionWidth() / groundSlice.getRegionHeight());
        float stepGround = groundW - 2f;
        float groundOffset = camX % stepGround;
        if (groundOffset < 0) groundOffset += stepGround;
        float groundStart = leftVisible - groundOffset;
        while (groundStart > leftVisible - groundW) groundStart -= stepGround;
        for (float x = groundStart; x < rightVisible + groundW; x += stepGround) batch.draw(groundSlice, x, GROUND_BASE_Y, groundW, groundH);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        worldViewport.update(width, height, false);
        hudStage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (hudStage != null) hudStage.dispose();
        if (whitePixelTex != null) whitePixelTex.dispose();
        if (radialGlowTex != null) radialGlowTex.dispose();
        if (titleFont != null) titleFont.dispose();
        if (topBannerFont != null) topBannerFont.dispose();
        if (hudHpFont != null) hudHpFont.dispose();
        if (battleLogFont != null) battleLogFont.dispose();
        if (playerTagFont != null) playerTagFont.dispose();
        if (miniFont != null) miniFont.dispose();
        if (damageFont != null) damageFont.dispose();
        if (critFont != null) critFont.dispose();
        if (modalTitleFont != null) modalTitleFont.dispose();
        if (modalSubFont != null) modalSubFont.dispose();
        if (modalRewardFont != null) modalRewardFont.dispose();
        if (modalNarrativeFont != null) modalNarrativeFont.dispose();
        if (modalStatusFont != null) modalStatusFont.dispose();
        if (introSpeakerFont != null) introSpeakerFont.dispose();
        if (introBodyFont != null) introBodyFont.dispose();
        if (introBtnFont != null) introBtnFont.dispose();
        if (topPauseFont != null) topPauseFont.dispose();
        if (pauseBadgeFont != null) pauseBadgeFont.dispose();
        if (pauseTitleFont != null) pauseTitleFont.dispose();
        if (pauseSubFont != null) pauseSubFont.dispose();
        if (pauseBodyFont != null) pauseBodyFont.dispose();
        if (pauseBtnFont != null) pauseBtnFont.dispose();
        if (pauseTipFont != null) pauseTipFont.dispose();
        if (sliderFont != null) sliderFont.dispose();
    }
}
