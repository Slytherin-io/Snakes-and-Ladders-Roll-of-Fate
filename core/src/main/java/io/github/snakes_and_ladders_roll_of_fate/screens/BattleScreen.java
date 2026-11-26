package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
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
    public static class HitSpark {
        float x, y;
        float vx, vy;
        float timer;
        float maxDuration;
        float size;
        Color color;

        public HitSpark(float x, float y, float vx, float vy, float duration, float size, Color color) {
            this.x = x; this.y = y;
            this.vx = vx; this.vy = vy;
            this.timer = duration;
            this.maxDuration = duration;
            this.size = size;
            this.color = color;
        }
    }

    public static class SlashEffect {
        float x, y;
        float timer;
        float maxDuration;
        boolean isSnakeBite;
        boolean isCrit;
        float angle;

        public SlashEffect(float x, float y, float duration, boolean isSnakeBite, boolean isCrit, float angle) {
            this.x = x; this.y = y;
            this.timer = duration;
            this.maxDuration = duration;
            this.isSnakeBite = isSnakeBite;
            this.isCrit = isCrit;
            this.angle = angle;
        }
    }

    public static class ShieldPulse {
        float x, y;
        float timer;
        float maxDuration;

        public ShieldPulse(float x, float y, float duration) {
            this.x = x; this.y = y;
            this.timer = duration;
            this.maxDuration = duration;
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
    private final int enemyMaxHp;
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

    private float screenShakeTimer = 0f;
    private float screenShakeDuration = 0f;
    private float screenShakeIntensity = 0f;

    // Rendering
    private SpriteBatch batch;
    private OrthographicCamera worldCamera;
    private ExtendViewport worldViewport;
    private Stage hudStage;
    private Texture whitePixelTex;

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
    private Label topBannerLabel, battleLogLabel, playerHpLabel, enemyHpLabel;
    private Table battleLogContainer;
    private TextButton btnAttack, btnDefend, btnItem, btnEscape, continueBtn;
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
    private float stateTime = 0f;
    private boolean outcomeResolved = false;
    private String outcomeMessage = "";

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

    private void spawnSlash(float x, float y, boolean isSnakeBite, boolean isCrit) {
        float angle = isSnakeBite ? 0f : -32f;
        slashEffects.add(new SlashEffect(x, y, isSnakeBite ? 0.32f : 0.30f, isSnakeBite, isCrit, angle));
    }

    private void spawnHitSparks(float x, float y, boolean isSnakeBite, int count, boolean isCrit) {
        Color[] playerColors = new Color[]{
            new Color(1f, 1f, 1f, 1f),
            new Color(1f, 0.9f, 0.2f, 1f),
            new Color(1f, 0.55f, 0.1f, 1f),
            new Color(1f, 0.2f, 0.2f, 1f)
        };
        Color[] snakeColors = new Color[]{
            new Color(0.85f, 1f, 0.85f, 1f),
            new Color(0.25f, 1f, 0.2f, 1f),
            new Color(0.1f, 0.85f, 0.3f, 1f),
            new Color(0.75f, 0.2f, 1f, 1f)
        };
        Color[] palette = isSnakeBite ? snakeColors : playerColors;

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
            game.getAudioManager().playMusic(AudioManager.MusicTrack.BATTLE);
        }
        batch = new SpriteBatch();
        worldCamera = new OrthographicCamera();
        worldViewport = new ExtendViewport(1920, 1080, worldCamera);
        worldCamera.position.set(950f, 540f, 0f); // Center between the two tiles
        worldCamera.update();
        hudStage = new Stage(new ExtendViewport(1920, 1080));
        Gdx.input.setInputProcessor(hudStage);

        // 1x1 White Texture
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE); px.fill();
        whitePixelTex = new Texture(px); px.dispose();

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

        // Register Battle Listeners for Dynamic FX
        battleSystem.setBattleListener((attacker, defender, damage, isCrit, isMiss) -> {
            if (attacker == player) {
                if (game.getRunTracker() != null) {
                    game.getRunTracker().recordSnakeCombat(player, damage, false, isCrit);
                }
                spawnSlash(ENEMY_X, ENEMY_DRAW_Y + 70f, false, isCrit);
                spawnHitSparks(ENEMY_X, ENEMY_DRAW_Y + 70f, false, isCrit ? 26 : 16, isCrit);
                enemyFlinchTimer = FLINCH_DURATION;
                spawnDamageNumber(ENEMY_X, ENEMY_DRAW_Y + 140f, damage, isCrit, isMiss);
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
            Texture snakeSheet = game.getAssetManager().get(Assets.ENEMY_SNAKE);
            TextureRegion[][] snakeGrid = TextureRegion.split(snakeSheet, 32, 32);
            for (TextureRegion frame : snakeGrid[0]) {
                if (!frame.isFlipX()) {
                    frame.flip(true, false);
                }
            }
            enemyIdleAnim = new Animation<>(0.15f, snakeGrid[0]);
            enemyAttackAnim = enemyIdleAnim;
        }

        // Start Battle
        battleSystem.startBattle(player, enemy);

        // Build UI
        buildTopBanner();
        buildPauseButton();
        buildBattleLog();
        buildHealthPills();
        buildActionButtons();
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

    private void buildTopBanner() {
        int act;
        String bannerText;
        Color bannerBgColor;
        if (isPaleShepherdBattle()) {
            act = (player.getPosition() <= 30) ? 1 : ((player.getPosition() <= 70) ? 2 : 3);
            bannerText = "MORTAL CLASH: THE PALE SHEPHERD!";
            bannerBgColor = Color.valueOf("#540d18");
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
        Table banner = new Table();
        banner.setBackground(UIUtils.createRoundedDrawable(920, 70, 20, bannerBgColor));
        banner.add(topBannerLabel).pad(8, 36, 8, 36);
        Table outer = new Table();
        outer.setFillParent(true);
        outer.top().padTop(22);
        outer.add(banner).center();
        hudStage.addActor(outer);
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
        battleLogLabel = new Label("The battle begins!", new Label.LabelStyle(battleLogFont, Color.valueOf("#fffdfa")));
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
        int maxHp = player.getCharacterClass() != null ? player.getCharacterClass().getBaseHealth() : 100;
        playerHpLabel = new Label("HP: " + player.getHealth() + " / " + maxHp, new Label.LabelStyle(hudHpFont, Color.valueOf("#a81717")));
        Table pPill = new Table();
        pPill.setBackground(UIUtils.createRoundedDrawable(340, 75, 12, pillLeather, pillBorder, 3));
        Table pBox = new Table();
        Label pName = new Label(player.getName().toUpperCase(), new Label.LabelStyle(playerTagFont, Color.valueOf("#361704")));
        pBox.add(pName).left().row();
        pBox.add(playerHpLabel).left();
        pPill.add(pBox).pad(8, 20, 8, 20);

        Table pWrap = new Table();
        pWrap.setFillParent(true);
        pWrap.bottom().left().padBottom(160).padLeft(50);
        pWrap.add(pPill).size(340, 75);
        hudStage.addActor(pWrap);

        // Enemy HP (Bottom Right - Mirroring Player HP)
        enemyHpLabel = new Label("HP: " + enemy.getHealth() + " / " + enemyMaxHp, new Label.LabelStyle(hudHpFont, Color.valueOf("#a81717")));
        Table ePill = new Table();
        ePill.setBackground(UIUtils.createRoundedDrawable(340, 75, 12, pillLeather, pillBorder, 3));
        Table eBox = new Table();
        Label eName = new Label(enemy.getEnemyType().toUpperCase(), new Label.LabelStyle(playerTagFont, Color.valueOf("#361704")));
        eBox.add(eName).right().row();
        eBox.add(enemyHpLabel).right();
        ePill.add(eBox).pad(8, 20, 8, 20);

        Table eWrap = new Table();
        eWrap.setFillParent(true);
        eWrap.bottom().right().padBottom(160).padRight(50);
        eWrap.add(ePill).size(340, 75);
        hudStage.addActor(eWrap);
    }

    private void buildActionButtons() {
        Color pillBrown = Color.valueOf("#854b25");
        Color leather = Color.valueOf("#c89259");
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = UIUtils.createRoundedDrawable(200, 100, 15, leather, pillBrown, 3);
        style.over = UIUtils.createRoundedDrawable(200, 100, 15, Color.valueOf("#d8a46f"), pillBrown, 3);
        style.font = titleFont;
        style.fontColor = Color.WHITE;

        btnAttack = new TextButton("ATTACK", style);
        btnDefend = new TextButton("DEFEND", style);
        btnItem = new TextButton("ITEM", style);
        btnEscape = new TextButton("ESCAPE", style);

        btnAttack.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (battleSystem.getCurrentState() == BattleState.PLAYER_TURN && playerLungeTimer <= 0f && enemyLungeTimer <= 0f) {
                    if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.ATTACK_SLASH);
                    playerLungeTimer = PLAYER_LUNGE_DURATION;
                    playerAttackLanded = false;
                    syncUi();
                }
            }
        });
        btnDefend.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (battleSystem.getCurrentState() == BattleState.PLAYER_TURN && playerLungeTimer <= 0f && enemyLungeTimer <= 0f) {
                    if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.DEFEND_GUARD);
                    spawnShieldPulse(PLAYER_X, CHAR_DRAW_Y + 80f);
                    spawnFloatingText(PLAYER_X, CHAR_DRAW_Y + 160f, "GUARD & RIPOSTE", Color.valueOf("#ffd54f"), false);
                    battleSystem.onPlayerDefend();
                    syncUi();
                }
            }
        });
        btnEscape.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                if (battleSystem.getCurrentState() == BattleState.PLAYER_TURN && playerLungeTimer <= 0f && enemyLungeTimer <= 0f) {
                    if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    battleSystem.onPlayerEscape();
                    syncUi();
                }
            }
        });
        btnItem.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                // Only open if it's the player's turn and no animations are playing
                if (battleSystem.getCurrentState() == BattleState.PLAYER_TURN && playerLungeTimer <= 0f && enemyLungeTimer <= 0f) {
                    if (game.getAudioManager() != null) game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    if (!itemPopup.isVisible()) {
                        refreshItemPopup(); // Rebuild the list so it's always fresh
                    }
                    itemPopup.setVisible(!itemPopup.isVisible()); // Toggle popup
                }
            }
        });

        Table bottomBar = new Table();
        bottomBar.setFillParent(true);
        bottomBar.bottom().padBottom(30);

        if (isDawnWardenBattle()) {
            bottomBar.add(btnAttack).size(200, 100).padRight(20);
            bottomBar.add(btnDefend).size(200, 100).padRight(20);
            bottomBar.add(btnItem).size(200, 100);
        } else {
            bottomBar.add(btnAttack).size(200, 100).padRight(20);
            bottomBar.add(btnDefend).size(200, 100).padRight(20);
            bottomBar.add(btnItem).size(200, 100).padRight(20);
            bottomBar.add(btnEscape).size(200, 100);
        }
        hudStage.addActor(bottomBar);
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
                    if (game.getRunTracker() != null && won) {
                        game.getRunTracker().recordThroneClaimed(player);
                    }
                    game.setScreen(new GameSummaryScreen(game, won, player));
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
                            game.setScreen(new GameSummaryScreen(game, false, player));
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
                        game.setScreen(new GameSummaryScreen(game, false, player));
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
        boolean isAnimating = (playerLungeTimer > 0f || enemyLungeTimer > 0f);
        boolean playerTurn = (state == BattleState.PLAYER_TURN && !isAnimating);
        boolean ended = (state == BattleState.BATTLE_END);

        // Update Text
        battleLogLabel.setText(ended ? outcomeMessage : battleSystem.getBattleLog());

        int maxHp = player.getCharacterClass() != null ? player.getCharacterClass().getBaseHealth() : 100;
        playerHpLabel.setText("HP: " + Math.max(0, player.getHealth()) + " / " + maxHp);
        enemyHpLabel.setText("HP: " + Math.max(0, enemy.getHealth()) + " / " + enemyMaxHp);

        // Lock buttons during enemy turn, animation, or end
        btnAttack.setDisabled(!playerTurn);
        btnDefend.setDisabled(!playerTurn);
        btnItem.setDisabled(!playerTurn);
        if (isDawnWardenBattle()) {
            btnEscape.setDisabled(true);
            btnEscape.setVisible(false);
        } else {
            btnEscape.setDisabled(!playerTurn);
            btnEscape.setVisible(true);
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
        if (!isDawnWardenBattle()) {
            btnEscape.setColor(1, 1, 1, alpha);
        }

        // Handle End State
        if (ended && !outcomeResolved) {
            outcomeResolved = true;
            BattleOutcome o = battleSystem.getFinalOutcome();
            if (isPaleShepherdBattle()) {
                if (o == BattleOutcome.VICTORY) {
                    outcomeMessage = "The Pale Shepherd dissolves into crimson mist, his golden scythe shattered!";
                } else if (o == BattleOutcome.ESCAPED) {
                    outcomeMessage = "You barely evaded Death's scythe! (-20 HP Parting Blow)";
                } else {
                    outcomeMessage = "The Pale Shepherd harvested your soul.";
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
                    topBannerLabel.setText("DEATH BANISHED!");
                    modalTitleLabel.setText("MORTAL TRIUMPH!");
                    modalTitleLabel.setColor(Color.valueOf("#ffd54f"));
                    modalSubtitleLabel.setText("YOU STRUCK DOWN THE PALE SHEPHERD!");

                    rewardExpLabel.setText("+50 EXP  |  The Pale Shepherd is banished 15 tiles back!");
                    rewardExpLabel.setColor(Color.valueOf("#69f0ae"));

                    rewardLootLabel.setText("Pursuit Halted for 3 Rounds of Contemplation!");
                    rewardLootLabel.setColor(Color.valueOf("#ffd54f"));

                    rewardStatusLabel.setText("Hero Health: " + player.getHealth() + " / " + maxHp + " HP   |   Mortal Defiance!");
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
                    topBannerLabel.setText("DESPERATE FLIGHT!");
                    modalTitleLabel.setText("HASTY ESCAPE!");
                    modalTitleLabel.setColor(Color.valueOf("#ffb74d"));
                    modalSubtitleLabel.setText("YOU BARELY EVADED THE GOLDEN SCYTHE!");

                    rewardExpLabel.setText("Suffered heavy parting wound (-20 HP)");
                    rewardExpLabel.setColor(Color.valueOf("#ff8a80"));

                    rewardLootLabel.setText("Scrambled 3 tiles ahead! Shepherd paused for 1 round.");
                    rewardLootLabel.setColor(Color.valueOf("#ffd54f"));

                    rewardStatusLabel.setText("Hero Health: " + Math.max(1, player.getHealth() - 20) + " / " + maxHp + " HP");
                    rewardStatusLabel.setColor(Color.valueOf("#ffe0b2"));

                    continueBtn.setText("SCRAMBLE AHEAD");
                    continueBtn.getStyle().up = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#854b25"), Color.valueOf("#54280e"), 2);
                    continueBtn.getStyle().over = UIUtils.createRoundedDrawable(340, 62, 14, Color.valueOf("#a66032"), Color.valueOf("#854b25"), 2);
                    if (continueBtnCell != null) continueBtnCell.width(340);
                } else {
                    topBannerLabel.setText("CLAIMED BY DEATH");
                    modalTitleLabel.setText("REST IN PEACE");
                    modalTitleLabel.setColor(Color.valueOf("#ef5350"));
                    modalSubtitleLabel.setText("THE PALE SHEPHERD HARVESTED YOUR SPIRIT");

                    rewardExpLabel.setText("Your ascent has ended on the mountain trail.");
                    rewardExpLabel.setColor(Color.valueOf("#cfd8dc"));

                    rewardLootLabel.setText("No more climbing, no more pain.");
                    rewardLootLabel.setColor(Color.valueOf("#ffffff"));

                    rewardStatusLabel.setText("Hero Status: Fallen (0 / " + maxHp + " HP)");
                    rewardStatusLabel.setColor(Color.valueOf("#cfd8dc"));

                    continueBtn.setText("VIEW EXPEDITION SUMMARY");
                    continueBtn.getStyle().up = UIUtils.createRoundedDrawable(460, 62, 14, Color.valueOf("#854b25"), Color.valueOf("#54280e"), 2);
                    continueBtn.getStyle().over = UIUtils.createRoundedDrawable(460, 62, 14, Color.valueOf("#a66032"), Color.valueOf("#854b25"), 2);
                    if (continueBtnCell != null) continueBtnCell.width(460);
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

                    spawnCelebrationSparks(960f, 540f);
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.LEVEL_UP);
                    }
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

                    continueBtn.setText("VIEW EXPEDITION SUMMARY");
                    continueBtn.getStyle().up = UIUtils.createRoundedDrawable(460, 62, 14, Color.valueOf("#854b25"), Color.valueOf("#54280e"), 2);
                    continueBtn.getStyle().over = UIUtils.createRoundedDrawable(460, 62, 14, Color.valueOf("#a66032"), Color.valueOf("#854b25"), 2);
                    if (continueBtnCell != null) continueBtnCell.width(460);
                }
                victoryCard.invalidateHierarchy();
                continueTable.setVisible(true);
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

        if (enemyLungeTimer > 0f) {
            enemyLungeTimer -= delta;
            float progress = 1.0f - (enemyLungeTimer / ENEMY_LUNGE_DURATION);
            if (progress >= 0.48f && !enemyAttackLanded) {
                enemyAttackLanded = true;
                if (game.getAudioManager() != null) {
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
        renderArena(delta);
        renderEffects(delta);
        batch.end();

        // HUD Render
        hudStage.getViewport().apply();
        hudStage.act(hudDelta);
        hudStage.draw();

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
        if (playerLungeTimer > 0f) {
            float p = 1.0f - (playerLungeTimer / PLAYER_LUNGE_DURATION);
            if (p < 0.15f) {
                float w = p / 0.15f;
                pLungeX = -MathUtils.sin(w * MathUtils.PI) * 25f;
                pRotation = 4f;
            } else {
                float s = (p - 0.15f) / 0.85f;
                pLungeX = MathUtils.sin(s * MathUtils.PI) * 260f;
                pLungeY = MathUtils.sin(s * MathUtils.PI) * 15f;
                pRotation = -MathUtils.sin(s * MathUtils.PI) * 10f;
            }
        }
        float pFlinchX = 0f;
        if (playerFlinchTimer > 0f) {
            float p = playerFlinchTimer / FLINCH_DURATION;
            pFlinchX = -MathUtils.sin(playerFlinchTimer * 40f) * p * 26f;
        }

        // Draw Player
        TextureRegion pFrame = playerIdleAnim != null ? playerIdleAnim.getKeyFrame(stateTime, true) : null;
        if (pFrame != null) {
            float bob = (player.getHealth() <= 0) ? 0f : MathUtils.sin(stateTime * 3f) * 4f;
            float pAlpha = (player.getHealth() <= 0) ? 0.45f : 1.0f;
            if (playerFlinchTimer > 0.12f) {
                batch.setColor(1f, 0.25f, 0.25f, 1f); // Red hurt flash
            } else if (player.getHealth() <= 0) {
                batch.setColor(0.45f, 0.45f, 0.50f, pAlpha); // Fallen knocked out tint
            } else {
                batch.setColor(Color.WHITE);
            }
            float drawPX = PLAYER_X + pLungeX + pFlinchX;
            float drawPY = CHAR_DRAW_Y + (player.getHealth() <= 0 ? -12f : bob) + pLungeY;
            float drawH = (player.getHealth() <= 0) ? CHAR_DISPLAY_SIZE * 0.72f : CHAR_DISPLAY_SIZE;
            batch.draw(pFrame,
                drawPX - CHAR_DISPLAY_SIZE / 2, drawPY,
                CHAR_DISPLAY_SIZE / 2, CHAR_DISPLAY_SIZE / 2,
                CHAR_DISPLAY_SIZE, drawH,
                1f, 1f, pRotation);
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
                eLungeX = -MathUtils.sin(s * MathUtils.PI) * 300f;
                eLungeY = MathUtils.sin(s * MathUtils.PI) * 20f;
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
        if (isBossBattle() && enemyLungeTimer > 0f && enemyAttackAnim != null) {
            eFrame = enemyAttackAnim.getKeyFrame(stateTime, true);
        } else {
            eFrame = enemyIdleAnim != null ? enemyIdleAnim.getKeyFrame(stateTime, true) : null;
        }
        float eBob = isBossBattle() ? 0f : (MathUtils.sin(stateTime * 3f + 1f) * 4f);
        if (eFrame != null) {
            float enemyAlpha = (enemy.getHealth() <= 0) ? 0.35f : 1.0f;
            if (enemyFlinchTimer > 0.12f) {
                batch.setColor(1f, 0.25f, 0.25f, enemyAlpha); // Red hurt flash
            } else if (isPaleShepherdBattle()) {
                batch.setColor(1.0f, 0.88f, 0.88f, enemyAlpha); // Spectral Pale Crimson Light
            } else if (isDawnWardenBattle() || isBossBattle()) {
                batch.setColor(1.0f, 0.95f, 0.80f, enemyAlpha); // Radiant Golden Dawn Light
            } else {
                int tilePos = (snakeTile != null) ? snakeTile.getPosition() : 1;
                if (tilePos <= 30) {
                    batch.setColor(0.85f, 1.0f, 0.88f, enemyAlpha); // Act 1: Verdant Moss / Emerald Hue
                } else if (tilePos <= 70) {
                    batch.setColor(0.80f, 0.88f, 1.0f, enemyAlpha); // Act 2: Deep Cavern Sapphire / Indigo Hue
                } else {
                    batch.setColor(1.0f, 0.78f, 0.72f, enemyAlpha); // Act 3: Scorched Magma / Crimson Ember Hue
                }
            }
            float hover = isPaleShepherdBattle() ? ((float) Math.sin(stateTime * 2.2f) * 8f) : 0f;
            float drawEX = ENEMY_X + eLungeX + eFlinchX;
            float drawEY = ENEMY_DRAW_Y + (isPaleShepherdBattle() ? (16f + hover) : (enemy.getHealth() <= 0 ? -12f : eBob)) + eLungeY;
            float enemyW = isPaleShepherdBattle() ? 280f : (isBossBattle() ? 320f : CHAR_DISPLAY_SIZE);
            float enemyH = isPaleShepherdBattle() ? ((enemy.getHealth() <= 0) ? 220f : 280f) : (isBossBattle() ? (enemy.getHealth() <= 0 ? 250f : 350f) : ((enemy.getHealth() <= 0) ? CHAR_DISPLAY_SIZE * 0.72f : CHAR_DISPLAY_SIZE));
            batch.draw(eFrame,
                drawEX - enemyW / 2, drawEY,
                enemyW / 2, enemyH / 2,
                enemyW, enemyH,
                1f, 1f, eRotation);
            batch.setColor(Color.WHITE);
        }
    }

    private void renderEffects(float delta) {
        // 1. Shield Pulses (Defend barrier)
        for (int i = shieldPulses.size - 1; i >= 0; i--) {
            ShieldPulse pulse = shieldPulses.get(i);
            pulse.timer -= delta;
            if (pulse.timer <= 0f) {
                shieldPulses.removeIndex(i);
                continue;
            }
            float t = 1.0f - (pulse.timer / pulse.maxDuration);
            float alpha = pulse.timer / pulse.maxDuration;
            float radius = 50f + t * 90f;
            float segThick = Math.max(2f, (1.0f - t) * 12f);
            int sides = 10;
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

                batch.setColor(0.3f, 0.85f, 1.0f, alpha * 0.7f);
                batch.draw(whitePixelTex, x1, y1 - segThick / 2, 0f, segThick / 2, dLen, segThick, 1f, 1f, angle, 0, 0, 1, 1, false, false);
                batch.setColor(1.0f, 0.9f, 0.4f, alpha * 0.9f);
                batch.draw(whitePixelTex, x1, y1 - (segThick * 0.4f) / 2, 0f, (segThick * 0.4f) / 2, dLen, segThick * 0.4f, 1f, 1f, angle, 0, 0, 1, 1, false, false);
            }
        }

        // 2. Slashes (Player sword arcs & Snake venom bite fangs)
        for (int i = slashEffects.size - 1; i >= 0; i--) {
            SlashEffect slash = slashEffects.get(i);
            slash.timer -= delta;
            if (slash.timer <= 0f) {
                slashEffects.removeIndex(i);
                continue;
            }
            float t = 1.0f - (slash.timer / slash.maxDuration);
            float alpha = slash.timer / slash.maxDuration;

            if (slash.isSnakeBite) {
                // Venom Fang Slashes (Dual inward-snapping toxic fangs)
                float fangLen = Math.min(230f, (t / 0.3f) * 230f);
                float fangThick = Math.max(2f, (1.0f - t) * 22f);

                // Left fang:
                float lx = slash.x - 35f;
                float ly = slash.y;
                float la = -65f;
                batch.setColor(0.65f, 0.1f, 0.95f, alpha * 0.5f);
                batch.draw(whitePixelTex, lx - fangLen / 2, ly - (fangThick * 1.6f) / 2, fangLen / 2, (fangThick * 1.6f) / 2, fangLen, fangThick * 1.6f, 1f, 1f, la, 0, 0, 1, 1, false, false);
                batch.setColor(0.2f, 1.0f, 0.2f, alpha * 0.9f);
                batch.draw(whitePixelTex, lx - fangLen / 2, ly - fangThick / 2, fangLen / 2, fangThick / 2, fangLen * 0.95f, fangThick, 1f, 1f, la, 0, 0, 1, 1, false, false);
                batch.setColor(0.9f, 1.0f, 0.85f, alpha);
                batch.draw(whitePixelTex, lx - fangLen / 2, ly - (fangThick * 0.35f) / 2, fangLen / 2, (fangThick * 0.35f) / 2, fangLen * 0.9f, fangThick * 0.35f, 1f, 1f, la, 0, 0, 1, 1, false, false);

                // Right fang:
                float rx = slash.x + 35f;
                float ry = slash.y;
                float ra = +65f;
                batch.setColor(0.65f, 0.1f, 0.95f, alpha * 0.5f);
                batch.draw(whitePixelTex, rx - fangLen / 2, ry - (fangThick * 1.6f) / 2, fangLen / 2, (fangThick * 1.6f) / 2, fangLen, fangThick * 1.6f, 1f, 1f, ra, 0, 0, 1, 1, false, false);
                batch.setColor(0.2f, 1.0f, 0.2f, alpha * 0.9f);
                batch.draw(whitePixelTex, rx - fangLen / 2, ry - fangThick / 2, fangLen / 2, fangThick / 2, fangLen * 0.95f, fangThick, 1f, 1f, ra, 0, 0, 1, 1, false, false);
                batch.setColor(0.9f, 1.0f, 0.85f, alpha);
                batch.draw(whitePixelTex, rx - fangLen / 2, ry - (fangThick * 0.35f) / 2, fangLen / 2, (fangThick * 0.35f) / 2, fangLen * 0.9f, fangThick * 0.35f, 1f, 1f, ra, 0, 0, 1, 1, false, false);
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
            s.vy -= 750f * delta; // Gravity
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
