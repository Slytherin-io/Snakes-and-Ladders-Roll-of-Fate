package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.board.LadderTile;
import io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile;
import io.github.snakes_and_ladders_roll_of_fate.board.Tile;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Entity;
import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.items.ItemType;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.systems.Dice;
import io.github.snakes_and_ladders_roll_of_fate.systems.Event;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventChoice;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffect;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventManager;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;
import io.github.snakes_and_ladders_roll_of_fate.ui.UIUtils;

/**
 * 2D Side-Scrolling Adventure Board Screen (Option C: Grounded Stepping Stones).
 * Features:
 * - Perfectly aligned 3-Act Parallax Layers (Canopy, Midground Silhouettes, Foreground Platform).
 * - Fully responsive window scaling using dual ExtendViewports (World & HUD Stage).
 * - Option C Stepping Stones rooted directly into the turf, with numbers and ladder/snake indicators.
 * - Dynamic camera tracking, step-by-step hopping physics, and full In-Game HUD overlay.
 */
public class BoardScreen implements Screen {
    private final RollOfFateGame game;
    private final Board board;
    private final Dice movementDice = new Dice(6);

    // Rendering & Viewports
    private SpriteBatch batch;
    private OrthographicCamera worldCamera;
    private ExtendViewport worldViewport;
    private Stage hudStage;
    private Texture whitePixelTex;

    // Fonts (Large, crisp, readable hierarchy)
    private BitmapFont titleFont;
    private BitmapFont topBannerFont;
    private BitmapFont topActFont;
    private BitmapFont topPauseFont;
    private BitmapFont rollBigFont;
    private BitmapFont rollSubFont;
    private BitmapFont hudNameFont;
    private BitmapFont hudHpFont;
    private BitmapFont hudExpFont;
    private BitmapFont hudStatsFont;
    private BitmapFont tileNumFont;
    private BitmapFont tileBadgeFont;
    private BitmapFont playerTagFont;
    private BitmapFont badgeFont;
    private BitmapFont miniFont;
    private BitmapFont sliderFont;
    private BitmapFont modalTitleFont;
    private BitmapFont modalBodyFont;

    // Parallax Slices for the 3 Acts (Far Canopy, Mid Silhouettes, Foreground Ground)
    private TextureRegion act1Far, act1Mid, act1Ground;
    private TextureRegion act2Far, act2Mid, act2Ground;
    private TextureRegion act3Far, act3Mid, act3Ground;

    // Layout Alignment Constants (Elevated by +110f to give the bottom HUD console generous room)
    private static final float GROUND_BASE_Y = 110f;    // Base anchor of the terrain ground slice
    private static final float GROUND_SURFACE_Y = 245f; // Flat grass line of the ground platform
    private static final float TILE_WIDTH = 180f;
    private static final float TILE_HEIGHT = 55f;
    private static final float TILE_BOTTOM_Y = 230f;    // Elevated stone slab baseline
    private static final float TILE_SPACING = 230f;     // Horizontal distance between tile centers
    private static final float FIRST_TILE_X = 400f;

    // Movement & Turn State Machine
    private enum TurnState {
        WAITING_FOR_ROLL,
        ROLLING_DICE,
        STEPPING_TILES,
        RESOLVING_TILE_EVENT,
        TILE_ACTION_PAUSE,
        TURN_TRANSITION
    }
    private TurnState turnState = TurnState.WAITING_FOR_ROLL;

    // Event System & Modals
    private final EventManager eventManager = new EventManager();
    private boolean isEventModalActive = false;
    private Table eventModalTable;
    private Table eventBadgeTable;
    private Label eventBadgeLabel;
    private Table eventContentTable;
    private TextButton.TextButtonStyle eventChoiceBtnStyle;
    private TextButton.TextButtonStyle disabledEventChoiceBtnStyle;
    private TextButton.TextButtonStyle combatStrikeBtnStyle;
    private Event activeCurrentEvent;
    private Tile activeCurrentTile;

    // Dice & Animation State
    private int currentRollValue = 6;
    private float diceTumbleTimer = 0f;
    private float diceFaceShuffleTimer = 0f;
    private float stateTime = 0f;
    private Image diceIcon;
    private final TextureRegionDrawable[] diceDrawables = new TextureRegionDrawable[7];

    // Step-by-Step Walking State
    private int hopCurrentTile = 1;
    private int hopTargetTile = 1;
    private float stepTimer = 0f;
    private static final float STEP_DURATION = 0.26f;

    // Character Walking Animations & Sizing (75% enlarged: 120f * 1.75 = 210f)
    private static final float CHAR_DISPLAY_SIZE = 210f;
    private static final float CHAR_INACTIVE_SIZE = 186f;
    // Ground alignment: with feet at Y=183 in 224px cell (41px above cell bottom),
    // and stone slab top walking surface at Y=282f, drawY = 282 - (41/224)*210 = 243.56f => TILE_BOTTOM_Y + 14f.
    private static final float CHAR_DRAW_Y = TILE_BOTTOM_Y + 14f;
    private static final float CHAR_DRAW_Y_INACTIVE_SHARED = TILE_BOTTOM_Y + 18f;
    private final Map<CharacterClass, Animation<TextureRegion>> walkAnimations = new HashMap<>();
    private float walkStateTime = 0f;

    // Action Pause Timer (Ladders / Snakes)
    private float actionPauseTimer = 0f;
    private int specialSlideTarget = -1;

    // HUD UI References
    private Label topBannerLabel;
    private Button rollButton;
    private Label diceLabel;
    private Label hudPlayerName;
    private Label hudPlayerHp;
    private Label hudPlayerExp;
    private Label hudPlayerStats;
    private Image hudPlayerAvatar;
    private Label miniMapProgressLabel;
    private Table inventoryPopup;
    private Table inventorySlotsTable;
    private Label inventoryTitleLabel;
    private Label inventoryCapacityLabel;
    private Label inventoryTipLabel;
    private TextButton inventoryLootBtn;
    private boolean itemUsedThisTurn = false;
    private boolean isItemMovement = false;

    // Pause, Settings, & Leave Game Modals
    private boolean isPaused = false;
    private Table modalScrim;
    private Table pauseModalTable;
    private Table settingsModalTable;
    private Table leaveConfirmModalTable;

    public BoardScreen(RollOfFateGame game) {
        this.game = game;
        this.board = game.getBoard() != null ? game.getBoard() : new Board(100);
        if (game.getBoard() == null) {
            this.board.generateBoard();
        }
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        // Responsive World Viewport: extends cleanly without stretching on any resolution
        worldCamera = new OrthographicCamera();
        worldViewport = new ExtendViewport(1920, 1080, worldCamera);

        // Responsive HUD Stage
        hudStage = new Stage(new ExtendViewport(1920, 1080));
        Gdx.input.setInputProcessor(hudStage);

        // 1x1 White Texture for bedrock procedural fill
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        whitePixelTex = new Texture(px);
        px.dispose();

        // --- Load Sniglet Fonts (Large, bold, highly legible hierarchy) ---
        Color darkBrown = Color.valueOf("#5a2d0c");
        Color nameBrown = Color.valueOf("#361704");
        Color pillBrown = Color.valueOf("#854b25");
        Color goldLight = Color.valueOf("#f6eedb");
        Color crimson   = Color.valueOf("#a81717");
        Color warmBrown = Color.valueOf("#5c2f0f");
        Color expBrown  = Color.valueOf("#693708");

        titleFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 26, Color.WHITE);
        topBannerFont  = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 32, Color.valueOf("#fffdfa"));
        topActFont     = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 23, Color.valueOf("#fdf5e2"));
        topPauseFont   = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 25, Color.WHITE);
        rollBigFont    = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 30, Color.WHITE);
        rollSubFont    = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 21, Color.valueOf("#fbf2dc"));
        hudNameFont    = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 24, nameBrown);
        hudHpFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 20, crimson);
        hudExpFont     = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 18, expBrown);
        hudStatsFont   = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 18, warmBrown);
        playerTagFont  = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 19, Color.WHITE);
        badgeFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 22, Color.WHITE);
        tileNumFont    = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 24, goldLight);
        tileBadgeFont  = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 15, Color.WHITE);
        miniFont       = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 16, Color.WHITE);
        sliderFont     = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 24, darkBrown);
        modalTitleFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 28, darkBrown);
        modalBodyFont  = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 20, darkBrown);

        // --- Slice Parallax Act Backgrounds ---
        initParallaxSlices();

        // --- Initialize Character Walking Animations ---
        initCharacterAnimations();

        // Initialize dice face drawables (dice_1.png through dice_6.png)
        for (int i = 1; i <= 6; i++) {
            diceDrawables[i] = new TextureRegionDrawable(game.getAssetManager().get(Assets.getDiceTexture(i)));
        }

        // --- Build In-Game HUD Overlay ---
        buildPauseButton();
        buildTopBanner();
        buildMiniMap();
        buildBottomHud();
        buildInventoryPopup();
        buildPauseAndSettingsModals(darkBrown, pillBrown);
        buildEventModal(darkBrown, pillBrown);

        // Configure global item swap handler for full satchel situations
        EventEffect.setItemSwapHandler(new EventEffect.ItemSwapHandler() {
            @Override
            public void handleSwap(Player player, Item newItem) {
                showItemSwapPrompt(player, newItem);
            }
        });

        // Initial camera placement on active player
        Player current = game.getCurrentPlayer();
        float startX = current != null ? getTileCenterX(current.getPosition()) : FIRST_TILE_X;
        worldCamera.position.set(startX, 540f, 0f);
        worldCamera.update();

        updateHudDisplay();
    }

    /**
     * Slices the 1024x585 background textures into Far Canopy (Top strip),
     * Mid Silhouettes (Middle strip), and Ground Platform (Bottom strip)
     * using exact pixel bounds to eliminate all artifact borders and gaps.
     */
    private void initParallaxSlices() {
        Texture bg1 = game.getAssetManager().get(Assets.ACT1_BG);
        Texture bg2 = game.getAssetManager().get(Assets.ACT2_BG);
        Texture bg3 = game.getAssetManager().get(Assets.ACT3_BG);

        // Exact pixel bounds cropped within 100% opaque boundaries to eliminate transparent edge artifacts:
        // Act 1 (Green Forest):
        act1Far    = new TextureRegion(bg1, 7, 0, 1010, 264);
        act1Mid    = new TextureRegion(bg1, 7, 268, 1010, 188);
        act1Ground = new TextureRegion(bg1, 7, 464, 1010, 108); // row 464..572: 100% solid rock at bottom

        // Act 2 (Blue Caverns):
        act2Far    = new TextureRegion(bg2, 8, 0, 1004, 264);
        act2Mid    = new TextureRegion(bg2, 8, 268, 998, 188);
        act2Ground = new TextureRegion(bg2, 10, 464, 1002, 110); // row 464..574: pure ground platform, eliminating mid-strip leak

        // Act 3 (Red Twilight):
        act3Far    = new TextureRegion(bg3, 7, 0, 1006, 264);
        act3Mid    = new TextureRegion(bg3, 7, 268, 1004, 188);
        act3Ground = new TextureRegion(bg3, 6, 464, 1008, 110); // row 464..574: pure ground platform, eliminating mid-strip leak
    }

    /**
     * Initializes walking animations for all character classes from their 224x224 spritesheet strips.
     */
    private void initCharacterAnimations() {
        for (CharacterClass cls : CharacterClass.values()) {
            Texture sheet = game.getAssetManager().get(Assets.getCharacterWalkSheet(cls));
            int frameCount = Assets.getCharacterWalkFrameCount(cls);
            TextureRegion[][] grid = TextureRegion.split(sheet, Assets.CHAR_FRAME_SIZE, Assets.CHAR_FRAME_SIZE);
            TextureRegion[] frames = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = grid[0][i];
            }
            float frameDur = (cls == CharacterClass.CAT) ? 0.09f : ((cls == CharacterClass.TURTLE) ? 0.07f : 0.06f);
            Animation<TextureRegion> anim = new Animation<>(frameDur, frames);
            anim.setPlayMode(Animation.PlayMode.LOOP);
            walkAnimations.put(cls, anim);
        }
    }

    /**
     * Top Ribbon Banner showing current turn guidance and live tile events.
     */
    private void buildTopBanner() {
        topBannerLabel = new Label("PLAYER 1'S TURN - ROLL THE DICE!", new Label.LabelStyle(topBannerFont, Color.valueOf("#fffdfa")));
        topBannerLabel.setAlignment(Align.center);

        Table bannerTable = new Table();
        bannerTable.setBackground(UIUtils.createRoundedDrawable(920, 70, 20, Color.valueOf("#854b25")));
        bannerTable.add(topBannerLabel).pad(8, 36, 8, 36);

        Table outerTop = new Table();
        outerTop.setFillParent(true);
        outerTop.top().padTop(22);
        outerTop.add(bannerTable).center();

        hudStage.addActor(outerTop);
    }

    /**
     * Top-Left Linear 100-Tile Progress Mini-Map with Act color indicators.
     */
    private void buildMiniMap() {
        Table miniMapTable = new Table();
        miniMapTable.setBackground(UIUtils.createRoundedDrawable(390, 70, 18, Color.valueOf("#4a2307dd")));

        miniMapProgressLabel = new Label("ACT I   -   TILE 1 / 100", new Label.LabelStyle(topActFont, Color.valueOf("#fdf5e2")));
        miniMapProgressLabel.setAlignment(Align.center);

        // Visual 3-act progress bar track
        Table barTrack = new Table();
        Table act1Seg = new Table();
        act1Seg.setBackground(UIUtils.createRoundedDrawable(102, 9, 4, Color.valueOf("#2e7d32")));
        Table act2Seg = new Table();
        act2Seg.setBackground(UIUtils.createRoundedDrawable(128, 9, 4, Color.valueOf("#1565c0")));
        Table act3Seg = new Table();
        act3Seg.setBackground(UIUtils.createRoundedDrawable(102, 9, 4, Color.valueOf("#c62828")));

        barTrack.add(act1Seg).size(102, 9).padRight(4);
        barTrack.add(act2Seg).size(128, 9).padRight(4);
        barTrack.add(act3Seg).size(102, 9);

        miniMapTable.add(miniMapProgressLabel).padTop(8).padBottom(5).center().row();
        miniMapTable.add(barTrack).padBottom(8).center();

        Table container = new Table();
        container.setFillParent(true);
        container.top().left().padTop(22).padLeft(35);
        container.add(miniMapTable).size(390, 70);

        hudStage.addActor(container);
    }

    /**
     * Bottom HUD: Inventory Satchel (Left), D6 Roll Button (Center), Active Player HUD (Right).
     * Refined with 100% solid color fills, crisp borders, and distinct textual hierarchy.
     */
    private void buildBottomHud() {
        Color pillBrown = Color.valueOf("#854b25");
        Color darkBrown = Color.valueOf("#5a2d0c");
        Color nameBrown = Color.valueOf("#361704");
        Color crimson   = Color.valueOf("#a81717");
        Color warmBrown = Color.valueOf("#5c2f0f");
        Color expBrown  = Color.valueOf("#693708");

        Color leatherSolid    = Color.valueOf("#c89259");
        Color leatherHover    = Color.valueOf("#d8a46f");
        Color containerBorder = Color.valueOf("#854b25");

        // --- 1. Left: INVENTORY Satchel Button ---
        Button.ButtonStyle satchelStyle = new Button.ButtonStyle();
        satchelStyle.up = UIUtils.createRoundedDrawable(250, 120, 20, leatherSolid, containerBorder, 3);
        satchelStyle.over = UIUtils.createRoundedDrawable(250, 120, 20, leatherHover, containerBorder, 3);

        Button inventoryBtn = new Button(satchelStyle);
        UIUtils.addScaleHoverEffect(inventoryBtn);

        Image satchelIcon = new Image(game.getAssetManager().get(Assets.ICON_SATCHEL));
        satchelIcon.setScaling(Scaling.fit);

        Table invTextTable = new Table();
        Label invLabel = new Label("INVENTORY", new Label.LabelStyle(hudNameFont, nameBrown));
        Label invSubLabel = new Label("4 SLOTS", new Label.LabelStyle(miniFont, Color.valueOf("#6d3a17")));
        invTextTable.add(invLabel).left().padBottom(2).row();
        invTextTable.add(invSubLabel).left();

        inventoryBtn.add(satchelIcon).size(56, 56).padLeft(18).padRight(14).center();
        inventoryBtn.add(invTextTable).center().padRight(18);

        inventoryBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Prevent opening inventory during dice rolling, tile stepping, combat/event modal, or turn pause
                if (turnState != TurnState.WAITING_FOR_ROLL || isEventModalActive) {
                    return;
                }
                if (inventoryPopup != null) {
                    boolean willShow = !inventoryPopup.isVisible();
                    if (willShow) {
                        refreshInventoryUI();
                    }
                    inventoryPopup.setVisible(willShow);
                }
            }
        });

        // --- 2. Center: Big D6 "ROLL" Button ---
        Button.ButtonStyle rollStyle = new Button.ButtonStyle();
        rollStyle.up = UIUtils.createRoundedDrawable(220, 120, 22, pillBrown, Color.valueOf("#5a2d0c"), 3);
        rollStyle.over = UIUtils.createRoundedDrawable(220, 120, 22, Color.valueOf("#9c572b"), Color.valueOf("#5a2d0c"), 3);

        rollButton = new Button(rollStyle);
        UIUtils.addScaleHoverEffect(rollButton);

        diceIcon = new Image(diceDrawables[6]);
        diceIcon.setScaling(Scaling.fit);
        diceIcon.setSize(60, 60);
        diceIcon.setOrigin(30, 30);

        Table diceInner = new Table();
        Label rollWord = new Label("ROLL", new Label.LabelStyle(rollBigFont, Color.WHITE));
        diceLabel = new Label("D6", new Label.LabelStyle(rollSubFont, Color.valueOf("#fbf2dc")));
        diceInner.add(rollWord).center().padBottom(4).row();
        diceInner.add(diceLabel).center();

        rollButton.add(diceIcon).size(60, 60).padLeft(16).padRight(14).center();
        rollButton.add(diceInner).center().padRight(16);

        rollButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (turnState == TurnState.WAITING_FOR_ROLL) {
                    triggerRoll();
                }
            }
        });

        // --- 3. Right: Active Player Status Card ---
        Table statusCard = new Table();
        statusCard.setBackground(UIUtils.createRoundedDrawable(430, 120, 20, leatherSolid, containerBorder, 3));

        hudPlayerAvatar = new Image(game.getAssetManager().get(Assets.CHAR_TURTLE));
        hudPlayerAvatar.setScaling(Scaling.fit);

        Table statsTextTable = new Table();
        statsTextTable.left();

        // Level 1: Primary Header - Bold Player Name
        hudPlayerName = new Label("PLAYER 1", new Label.LabelStyle(hudNameFont, nameBrown));
        statsTextTable.add(hudPlayerName).left().padBottom(2).row();

        // Level 2: Secondary Vital - Health with Heart Icon
        Table hpRow = new Table();
        hpRow.left();
        Image heartIcon = new Image(game.getAssetManager().get(Assets.ICON_HEART));
        heartIcon.setScaling(Scaling.fit);
        hudPlayerHp = new Label("HP: 120 / 120", new Label.LabelStyle(hudHpFont, crimson));
        hpRow.add(heartIcon).size(18, 18).padRight(6).center();
        hpRow.add(hudPlayerHp).left().center();
        statsTextTable.add(hpRow).left().padBottom(2).row();

        // Level 3: Progression - Level & Experience
        Table expRow = new Table();
        expRow.left();
        hudPlayerExp = new Label("LVL 1    -    EXP: 0 / 100", new Label.LabelStyle(hudExpFont, expBrown));
        expRow.add(hudPlayerExp).left().center();
        statsTextTable.add(expRow).left().padBottom(2).row();

        // Level 4: Supporting Combat Attributes - Attack & Defense
        Table statsRow = new Table();
        statsRow.left();
        hudPlayerStats = new Label("ATK  4    -    DEF  7", new Label.LabelStyle(hudStatsFont, warmBrown));
        statsRow.add(hudPlayerStats).left().center();
        statsTextTable.add(statsRow).left();

        statusCard.add(hudPlayerAvatar).size(110, 90).padLeft(16).padRight(14).center();
        statusCard.add(statsTextTable).expandX().left().padRight(16).center();

        // --- Assemble Bottom Bar Layout ---
        Table bottomBar = new Table();
        bottomBar.setFillParent(true);
        bottomBar.bottom().padBottom(24);

        bottomBar.add(inventoryBtn).size(250, 120).padLeft(35);
        bottomBar.add().expandX(); // Push Roll to center
        bottomBar.add(rollButton).size(220, 120);
        bottomBar.add().expandX(); // Push Stats to right
        bottomBar.add(statusCard).size(430, 120).padRight(35);

        hudStage.addActor(bottomBar);
    }

    /**
     * Builds the interactive 4-Slot Player Inventory Satchel Popup.
     */
    private void buildInventoryPopup() {
        Color pillBrown = Color.valueOf("#854b25");

        inventoryPopup = new Table();
        inventoryPopup.setBackground(UIUtils.createRoundedDrawable(630, 580, 20, Color.valueOf("#3d1b06f8")));
        inventoryPopup.setVisible(false);

        // Header Table
        Table headerTable = new Table();
        inventoryTitleLabel = new Label("PLAYER 1'S INVENTORY", new Label.LabelStyle(titleFont, Color.WHITE));
        inventoryCapacityLabel = new Label("0 / 4 SLOTS", new Label.LabelStyle(playerTagFont, Color.valueOf("#ffd54f")));

        Table badgeTable = new Table();
        badgeTable.setBackground(UIUtils.createRoundedDrawable(140, 36, 10, pillBrown));
        badgeTable.add(inventoryCapacityLabel).center();

        headerTable.add(inventoryTitleLabel).left();
        headerTable.add().expandX();
        headerTable.add(badgeTable).size(140, 36).right();

        inventoryPopup.add(headerTable).width(570).padTop(20).padBottom(6).center().row();

        inventoryTipLabel = new Label("Limit: 1 item per turn. Tap USE before rolling the dice.", new Label.LabelStyle(miniFont, Color.valueOf("#fffdfa")));
        inventoryTipLabel.setWrap(true);
        inventoryTipLabel.setAlignment(Align.center);
        inventoryPopup.add(inventoryTipLabel).width(570).padBottom(14).center().row();

        // Slots container (holds the 4 slots)
        inventorySlotsTable = new Table();
        inventoryPopup.add(inventorySlotsTable).width(580).padBottom(16).center().row();

        // Footer Actions: "+ LOOT" (test loot up to 4 items) and "CLOSE"
        Table footerTable = new Table();

        TextButton.TextButtonStyle lootBtnStyle = new TextButton.TextButtonStyle();
        lootBtnStyle.up = UIUtils.createRoundedDrawable(150, 46, 14, Color.valueOf("#2e7d32"));
        lootBtnStyle.over = UIUtils.createRoundedDrawable(150, 46, 14, Color.valueOf("#388e3c"));
        lootBtnStyle.font = playerTagFont;
        lootBtnStyle.fontColor = Color.WHITE;

        inventoryLootBtn = new TextButton("+ LOOT", lootBtnStyle);
        UIUtils.addScaleHoverEffect(inventoryLootBtn);
        inventoryLootBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleFindLoot();
            }
        });

        TextButton.TextButtonStyle closeStyle = new TextButton.TextButtonStyle();
        closeStyle.up = UIUtils.createRoundedDrawable(140, 46, 14, pillBrown);
        closeStyle.over = UIUtils.createRoundedDrawable(140, 46, 14, Color.valueOf("#9c572b"));
        closeStyle.font = badgeFont;
        closeStyle.fontColor = Color.WHITE;

        TextButton closeBtn = new TextButton("CLOSE", closeStyle);
        UIUtils.addScaleHoverEffect(closeBtn);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                inventoryPopup.setVisible(false);
            }
        });

        footerTable.add(inventoryLootBtn).size(150, 46).left();
        footerTable.add().expandX();
        footerTable.add(closeBtn).size(140, 46).right();

        inventoryPopup.add(footerTable).width(570).padBottom(20).center();

        Table popupContainer = new Table();
        popupContainer.setFillParent(true);
        popupContainer.bottom().left().padBottom(155).padLeft(35);
        popupContainer.add(inventoryPopup).size(630, 580);

        hudStage.addActor(popupContainer);
    }

    /**
     * Refreshes the 4 inventory slot rows for the current player.
     */
    private void refreshInventoryUI() {
        if (inventorySlotsTable == null) return;
        inventorySlotsTable.clear();

        Player player = game.getCurrentPlayer();
        if (player == null || player.getInventory() == null) return;

        inventoryTitleLabel.setText(player.getName().toUpperCase() + "'S INVENTORY");

        int itemCount = player.getInventory().size();
        int capacity = player.getInventory().getCapacity(); // strictly 4
        inventoryCapacityLabel.setText(itemCount + " / " + capacity + " SLOTS");

        if (inventoryTipLabel != null) {
            if (itemUsedThisTurn) {
                inventoryTipLabel.setText("Item already used this turn. (Limit: 1 item per turn)");
                inventoryTipLabel.setColor(Color.valueOf("#ffd54f"));
            } else {
                inventoryTipLabel.setText("Limit: 1 item per turn. Tap USE before rolling the dice.");
                inventoryTipLabel.setColor(Color.valueOf("#fffdfa"));
            }
        }

        if (itemUsedThisTurn) {
            inventoryLootBtn.setText("1 / TURN");
            inventoryLootBtn.setColor(0.6f, 0.6f, 0.6f, 0.4f);
            inventoryLootBtn.setDisabled(true);
            inventoryLootBtn.setTouchable(Touchable.disabled);
        } else if (player.getInventory().isFull()) {
            inventoryCapacityLabel.setColor(Color.valueOf("#ff6b6b"));
            inventoryLootBtn.setText("FULL (4/4)");
            inventoryLootBtn.setColor(0.6f, 0.6f, 0.6f, 0.5f);
            inventoryLootBtn.setDisabled(true);
            inventoryLootBtn.setTouchable(Touchable.disabled);
        } else {
            inventoryCapacityLabel.setColor(Color.valueOf("#ffd54f"));
            inventoryLootBtn.setText("+ LOOT");
            inventoryLootBtn.setColor(Color.WHITE);
            inventoryLootBtn.setDisabled(false);
            inventoryLootBtn.setTouchable(Touchable.enabled);
        }

        // Render exactly 4 slots (Slot 0..3)
        for (int i = 0; i < capacity; i++) {
            Table slotRow = new Table();
            slotRow.pad(6, 12, 6, 12);

            // Fixed icon box container so ALL slots (populated and empty) have identical alignment
            Table iconBox = new Table();

            if (i < itemCount) {
                Item item = player.getInventory().getItem(i);
                slotRow.setBackground(UIUtils.createRoundedDrawable(580, 80, 12, Color.valueOf("#54280ecc")));

                // Item Icon centered inside fixed icon box
                Texture iconTex = game.getAssetManager().get(Assets.getItemIcon(item.getEffectType()));
                Image iconImg = new Image(iconTex);
                iconImg.setScaling(Scaling.fit);
                iconBox.add(iconImg).size(42, 42).center();
                slotRow.add(iconBox).size(48, 48).padLeft(2).padRight(12).center();

                // Info text: Name on line 1, metadata under title on line 2
                Table infoTable = new Table();
                infoTable.left();

                // Line 1: Item Name alone (Bright crisp white, uncluttered)
                Label nameLbl = new Label(item.getName(), new Label.LabelStyle(playerTagFont, Color.WHITE));
                infoTable.add(nameLbl).left().row();

                // Line 2: Type/Durability badge + separator dot + effect description (Under title)
                Table subInfo = new Table();
                subInfo.left();

                String typeStr = (item.getItemType() == ItemType.CONSUMABLE)
                    ? "[CONSUMABLE]"
                    : "[DUR: " + item.getDurability() + "/" + item.getMaxDurability() + "]";
                Color typeColor = (item.getItemType() == ItemType.CONSUMABLE)
                    ? Color.valueOf("#86efac")  // Lighter, highly visible pastel mint green
                    : Color.valueOf("#93c5fd"); // Lighter, highly visible pastel sky blue
                Label typeLbl = new Label(typeStr, new Label.LabelStyle(miniFont, typeColor));

                Label dotLbl = new Label(" - ", new Label.LabelStyle(miniFont, Color.valueOf("#fef08a"))); // Clean hyphen separator, no [] missing glyph
                Label effectLbl = new Label(item.getEffect(), new Label.LabelStyle(miniFont, Color.valueOf("#fffdfa"))); // Crisp warm white
                effectLbl.setEllipsis(true);

                subInfo.add(typeLbl).left();
                subInfo.add(dotLbl).left();
                subInfo.add(effectLbl).width(260).left();
                infoTable.add(subInfo).padTop(3).left();

                // Fixed width table prevents any text overflow from pushing the USE button out
                slotRow.add(infoTable).width(400).left().padRight(8);

                // USE button
                TextButton.TextButtonStyle useStyle = new TextButton.TextButtonStyle();
                useStyle.up = UIUtils.createRoundedDrawable(88, 42, 10, Color.valueOf("#2e7d32"));
                useStyle.over = UIUtils.createRoundedDrawable(88, 42, 10, Color.valueOf("#388e3c"));
                useStyle.font = playerTagFont;
                useStyle.fontColor = Color.WHITE;

                TextButton useBtn = new TextButton("USE", useStyle);
                UIUtils.addScaleHoverEffect(useBtn);

                final int slotIndex = i;
                useBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        handleUseInventoryItem(slotIndex);
                    }
                });

                if (turnState != TurnState.WAITING_FOR_ROLL || itemUsedThisTurn) {
                    useBtn.setText(itemUsedThisTurn ? "USED" : "WAIT");
                    useBtn.setDisabled(true);
                    useBtn.setTouchable(Touchable.disabled);
                    useBtn.setColor(1f, 1f, 1f, 0.35f);
                } else {
                    useBtn.setText("USE");
                    useBtn.setDisabled(false);
                    useBtn.setTouchable(Touchable.enabled);
                    useBtn.setColor(Color.WHITE);
                }

                slotRow.add(useBtn).size(88, 42).right();
            } else {
                // Empty Slot
                slotRow.setBackground(UIUtils.createRoundedDrawable(580, 80, 12, Color.valueOf("#2a120488")));

                Image emptyIcon = new Image(game.getAssetManager().get(Assets.ICON_SATCHEL));
                emptyIcon.setScaling(Scaling.fit);
                emptyIcon.setColor(1f, 1f, 1f, 0.45f);
                iconBox.add(emptyIcon).size(38, 38).center();
                slotRow.add(iconBox).size(48, 48).padLeft(2).padRight(12).center();

                Label emptyLbl = new Label("[Slot " + (i + 1) + "] Empty Slot", new Label.LabelStyle(playerTagFont, Color.valueOf("#e0b888")));
                slotRow.add(emptyLbl).width(400).left().padRight(8);

                slotRow.add().size(88, 42); // Placeholder alignment spacer
            }

            inventorySlotsTable.add(slotRow).size(580, 80).padBottom(6).row();
        }
    }

    /**
     * Uses the item in the specified inventory slot for the active player.
     */
    private void handleUseInventoryItem(int slotIndex) {
        if (itemUsedThisTurn || turnState != TurnState.WAITING_FOR_ROLL) {
            topBannerLabel.setText("LIMIT REACHED: ONLY 1 ITEM CAN BE USED PER TURN!");
            return;
        }

        Player player = game.getCurrentPlayer();
        if (player == null || player.getInventory() == null) return;

        Item item = player.getInventory().getItem(slotIndex);
        if (item == null) return;

        String itemName = item.getName();
        EventEffectType type = item.getEffectType();
        String effectDesc = item.getEffect();
        int effectVal = item.getEffectValue();

        // Prevent consuming health items when already at max health
        if (type == EventEffectType.HEALTH && player.getHealth() >= player.getMaxHealth()) {
            topBannerLabel.setText(player.getName().toUpperCase() + " IS ALREADY AT FULL HEALTH (" + player.getMaxHealth() + " HP)!");
            return;
        }

        int startPos = player.getPosition();

        // Use the item via player
        boolean success = player.useItem(slotIndex);
        if (!success) return;

        itemUsedThisTurn = true;

        // Auto-close inventory popup so player returns to the action!
        if (inventoryPopup != null) {
            inventoryPopup.setVisible(false);
        }

        // Banner feedback
        topBannerLabel.setText(player.getName().toUpperCase() + " USED " + itemName.toUpperCase() + "! (" + effectDesc + ")");

        // If it was a movement item, advance player with walking animation
        if (type == EventEffectType.MOVE) {
            int targetPos = Math.min(100, Math.max(1, startPos + effectVal));
            player.setPosition(startPos);
            hopCurrentTile = startPos;
            hopTargetTile = targetPos;
            stepTimer = 0f;
            isItemMovement = true;
            turnState = TurnState.STEPPING_TILES;
            topBannerLabel.setText(player.getName().toUpperCase() + " USED " + itemName.toUpperCase() + "! ADVANCING " + effectVal + " TILES...");
        }

        // Update active player stats & mini-map
        updateHudDisplay();

        // Refresh inventory UI
        refreshInventoryUI();
    }

    /**
     * Adds random loot to active player for testing/adventure reward, strictly obeying 4-item capacity.
     */
    private void handleFindLoot() {
        if (itemUsedThisTurn || turnState != TurnState.WAITING_FOR_ROLL) {
            topBannerLabel.setText("CANNOT ACQUIRE LOOT AFTER USING AN ITEM THIS TURN!");
            return;
        }
        Player player = game.getCurrentPlayer();
        if (player == null || player.getInventory() == null) return;

        if (player.getInventory().isFull()) {
            topBannerLabel.setText("INVENTORY IS FULL! (" + player.getInventory().size() + "/4 SLOTS)");
            return;
        }

        Item loot = Item.createRandomLoot();
        boolean added = player.addItem(loot);
        if (added) {
            topBannerLabel.setText(player.getName().toUpperCase() + " FOUND " + loot.getName().toUpperCase() + "! (" + player.getInventory().size() + "/4 SLOTS)");
            refreshInventoryUI();
        }
    }

    /**
     * Top-Right Pause Button (|| PAUSE) matching top bar proportions and style.
     */
    private void buildPauseButton() {
        Color pillBrown = Color.valueOf("#854b25");
        Button.ButtonStyle pauseStyle = new Button.ButtonStyle();
        pauseStyle.up = UIUtils.createRoundedDrawable(164, 70, 18, pillBrown);
        pauseStyle.over = UIUtils.createRoundedDrawable(164, 70, 18, Color.valueOf("#9c572b"));

        Button pauseBtn = new Button(pauseStyle);
        UIUtils.addScaleHoverEffect(pauseBtn);

        Label pauseLabel = new Label("|| PAUSE", new Label.LabelStyle(topPauseFont, Color.WHITE));
        pauseBtn.add(pauseLabel).center();

        pauseBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
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

    /**
     * Builds the unified Parchment Scroll Modals for Pause, Settings, and Leave Game confirmation.
     */
    private void buildPauseAndSettingsModals(Color darkBrown, Color pillBrown) {
        // Fullscreen translucent backdrop that softly dims the world without obscuring it
        modalScrim = new Table();
        modalScrim.setFillParent(true);
        modalScrim.setBackground(UIUtils.createRoundedDrawable(1920, 1080, 0, new Color(0f, 0f, 0f, 0.35f)));
        modalScrim.setVisible(false);
        modalScrim.setTouchable(Touchable.enabled);
        hudStage.addActor(modalScrim);

        TextButton.TextButtonStyle menuBtnStyle = new TextButton.TextButtonStyle();
        menuBtnStyle.up = UIUtils.createRoundedDrawable(360, 58, 16, pillBrown);
        menuBtnStyle.over = UIUtils.createRoundedDrawable(360, 58, 16, Color.valueOf("#9c572b"));
        menuBtnStyle.font = badgeFont;
        menuBtnStyle.fontColor = Color.WHITE;

        TextButton.TextButtonStyle dangerBtnStyle = new TextButton.TextButtonStyle();
        dangerBtnStyle.up = UIUtils.createRoundedDrawable(360, 58, 16, Color.valueOf("#8a2424"));
        dangerBtnStyle.over = UIUtils.createRoundedDrawable(360, 58, 16, Color.valueOf("#a63232"));
        dangerBtnStyle.font = badgeFont;
        dangerBtnStyle.fontColor = Color.WHITE;

        // =====================================================================
        // 1. PAUSE MODAL
        // =====================================================================
        Label pauseBadge = new Label("GAME PAUSED", new Label.LabelStyle(badgeFont, Color.WHITE));
        pauseBadge.setAlignment(Align.center);
        Table pauseBadgeTable = new Table();
        pauseBadgeTable.setBackground(UIUtils.createRoundedDrawable(340, 58, 18, pillBrown));
        pauseBadgeTable.add(pauseBadge).pad(6, 36, 6, 36);

        Table pauseContent = new Table();
        Label pauseSub = new Label("The journey awaits your command.", new Label.LabelStyle(modalBodyFont, darkBrown));
        pauseSub.setAlignment(Align.center);
        pauseContent.add(pauseSub).padBottom(28).center().row();

        TextButton resumeBtn = new TextButton("RESUME", menuBtnStyle);
        UIUtils.addScaleHoverEffect(resumeBtn);
        resumeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                resumeGame();
            }
        });

        TextButton settingsBtn = new TextButton("SETTINGS", menuBtnStyle);
        UIUtils.addScaleHoverEffect(settingsBtn);
        settingsBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSettingsMenu();
            }
        });

        TextButton leaveBtn = new TextButton("LEAVE GAME", dangerBtnStyle);
        UIUtils.addScaleHoverEffect(leaveBtn);
        leaveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showLeaveConfirmMenu();
            }
        });

        pauseContent.add(resumeBtn).size(360, 58).padBottom(14).center().row();
        pauseContent.add(settingsBtn).size(360, 58).padBottom(14).center().row();
        pauseContent.add(leaveBtn).size(360, 58).padBottom(18).center().row();

        Label escTip = new Label("Press ESC or P to resume", new Label.LabelStyle(miniFont, Color.valueOf("#7a4823")));
        escTip.setAlignment(Align.center);
        pauseContent.add(escTip).center();

        Image pauseScrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        pauseScrollBg.setScaling(Scaling.stretch);

        Stack pauseScrollStack = new Stack();
        pauseScrollStack.add(pauseScrollBg);
        pauseScrollStack.add(pauseContent);

        pauseModalTable = new Table();
        pauseModalTable.setFillParent(true);
        pauseModalTable.setVisible(false);
        pauseModalTable.add(pauseBadgeTable).size(340, 58).padBottom(-22).center().row();
        pauseModalTable.add(pauseScrollStack).size(740, 540).center().row();

        hudStage.addActor(pauseModalTable);

        // =====================================================================
        // 2. SETTINGS MODAL
        // =====================================================================
        Label settingsBadge = new Label("SETTINGS", new Label.LabelStyle(badgeFont, Color.WHITE));
        settingsBadge.setAlignment(Align.center);
        Table settingsBadgeTable = new Table();
        settingsBadgeTable.setBackground(UIUtils.createRoundedDrawable(280, 58, 18, pillBrown));
        settingsBadgeTable.add(settingsBadge).pad(6, 36, 6, 36);

        Slider.SliderStyle sliderStyle = UIUtils.createSliderStyle(darkBrown, darkBrown);

        Slider musicSlider = new Slider(0, 10, 1, false, sliderStyle);
        musicSlider.setValue(10);
        Label musicNum = new Label("10", new Label.LabelStyle(sliderFont, darkBrown));
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                musicNum.setText(String.valueOf((int) musicSlider.getValue()));
            }
        });

        Slider sfxSlider = new Slider(0, 10, 1, false, sliderStyle);
        sfxSlider.setValue(10);
        Label sfxNum = new Label("10", new Label.LabelStyle(sliderFont, darkBrown));
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                sfxNum.setText(String.valueOf((int) sfxSlider.getValue()));
            }
        });

        Image soundIcon = new Image(game.getAssetManager().get(Assets.ICON_SOUND));
        Image musicIcon = new Image(game.getAssetManager().get(Assets.ICON_MUSIC));

        Table settingsContent = new Table();

        Label musicLabel = new Label("MUSIC VOLUME", new Label.LabelStyle(modalTitleFont, darkBrown));
        settingsContent.add(musicLabel).colspan(3).center().padBottom(10).row();
        settingsContent.add(soundIcon).size(38, 38).padRight(16);
        settingsContent.add(musicSlider).width(340).padRight(16);
        settingsContent.add(musicNum).width(44).left().padBottom(26).row();

        Label sfxLabel = new Label("SFX VOLUME", new Label.LabelStyle(modalTitleFont, darkBrown));
        settingsContent.add(sfxLabel).colspan(3).center().padBottom(10).row();
        settingsContent.add(musicIcon).size(38, 38).padRight(16);
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
                showPauseMenu();
            }
        });

        settingsModalTable = new Table();
        settingsModalTable.setFillParent(true);
        settingsModalTable.setVisible(false);
        settingsModalTable.add(settingsBadgeTable).size(280, 58).padBottom(-22).center().row();
        settingsModalTable.add(settingsScrollStack).size(740, 540).center().row();
        settingsModalTable.add(backSettingsBtn).size(180, 52).padTop(-22).center().row();

        hudStage.addActor(settingsModalTable);

        // =====================================================================
        // 3. LEAVE GAME CONFIRMATION MODAL
        // =====================================================================
        Label leaveBadge = new Label("LEAVE GAME?", new Label.LabelStyle(badgeFont, Color.WHITE));
        leaveBadge.setAlignment(Align.center);
        Table leaveBadgeTable = new Table();
        leaveBadgeTable.setBackground(UIUtils.createRoundedDrawable(300, 58, 18, Color.valueOf("#8a2424")));
        leaveBadgeTable.add(leaveBadge).pad(6, 36, 6, 36);

        Table leaveContent = new Table();

        Label leaveWarningTitle = new Label("Abandon Current Adventure?", new Label.LabelStyle(modalTitleFont, darkBrown));
        leaveWarningTitle.setAlignment(Align.center);
        leaveContent.add(leaveWarningTitle).padBottom(14).center().row();

        Label leaveWarningBody = new Label("Are you sure you want to return to the main menu?\nAny unsaved progress on this board will be lost.", new Label.LabelStyle(modalBodyFont, darkBrown));
        leaveWarningBody.setAlignment(Align.center);
        leaveWarningBody.setWrap(true);
        leaveContent.add(leaveWarningBody).width(460).padBottom(36).center().row();

        Table leaveBtnRow = new Table();
        TextButton stayBtn = new TextButton("STAY", menuBtnStyle);
        UIUtils.addScaleHoverEffect(stayBtn);
        stayBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showPauseMenu();
            }
        });

        TextButton confirmLeaveBtn = new TextButton("LEAVE", dangerBtnStyle);
        UIUtils.addScaleHoverEffect(confirmLeaveBtn);
        confirmLeaveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                leaveGameToMainMenu();
            }
        });

        leaveBtnRow.add(stayBtn).size(180, 56).padRight(24);
        leaveBtnRow.add(confirmLeaveBtn).size(180, 56);
        leaveContent.add(leaveBtnRow).center();

        Image leaveScrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        leaveScrollBg.setScaling(Scaling.stretch);

        Stack leaveScrollStack = new Stack();
        leaveScrollStack.add(leaveScrollBg);
        leaveScrollStack.add(leaveContent);

        leaveConfirmModalTable = new Table();
        leaveConfirmModalTable.setFillParent(true);
        leaveConfirmModalTable.setVisible(false);
        leaveConfirmModalTable.add(leaveBadgeTable).size(300, 58).padBottom(-22).center().row();
        leaveConfirmModalTable.add(leaveScrollStack).size(740, 540).center().row();

        hudStage.addActor(leaveConfirmModalTable);
    }

    private void pauseGame() {
        isPaused = true;
        if (inventoryPopup != null) inventoryPopup.setVisible(false);
        if (eventModalTable != null && isEventModalActive) eventModalTable.setVisible(false);
        showPauseMenu();
    }

    private void resumeGame() {
        isPaused = false;
        pauseModalTable.setVisible(false);
        settingsModalTable.setVisible(false);
        leaveConfirmModalTable.setVisible(false);
        if (isEventModalActive && eventModalTable != null) {
            modalScrim.setVisible(true);
            eventModalTable.setVisible(true);
        } else {
            modalScrim.setVisible(false);
        }
    }

    private void showPauseMenu() {
        modalScrim.setVisible(true);
        pauseModalTable.setVisible(true);
        settingsModalTable.setVisible(false);
        leaveConfirmModalTable.setVisible(false);
    }

    private void showSettingsMenu() {
        modalScrim.setVisible(true);
        pauseModalTable.setVisible(false);
        settingsModalTable.setVisible(true);
        leaveConfirmModalTable.setVisible(false);
    }

    private void showLeaveConfirmMenu() {
        modalScrim.setVisible(true);
        pauseModalTable.setVisible(false);
        settingsModalTable.setVisible(false);
        leaveConfirmModalTable.setVisible(true);
    }

    private void leaveGameToMainMenu() {
        game.setScreen(new MainMenuScreen(game));
    }

    // =========================================================================
    // INTERACTIVE EVENT MODAL SYSTEM (Tile Landing Events, Combat, Barter)
    // =========================================================================

    /**
     * Builds the unified Parchment Scroll Event Modal for tile landing encounters.
     * Scroll asset is enlarged to 940x690 and inner content cards are constrained to 530px,
     * ensuring choices, combat logs, and barter items never overlap the scroll's end or roller spindles.
     */
    private void buildEventModal(Color darkBrown, Color pillBrown) {
        eventChoiceBtnStyle = new TextButton.TextButtonStyle();
        eventChoiceBtnStyle.up = UIUtils.createRoundedDrawable(530, 46, 12, Color.valueOf("#7a3d16"), Color.valueOf("#54280e"), 2);
        eventChoiceBtnStyle.over = UIUtils.createRoundedDrawable(530, 46, 12, Color.valueOf("#9c572b"), Color.valueOf("#54280e"), 2);
        eventChoiceBtnStyle.font = playerTagFont;
        eventChoiceBtnStyle.fontColor = Color.valueOf("#fff8ea");

        disabledEventChoiceBtnStyle = new TextButton.TextButtonStyle();
        disabledEventChoiceBtnStyle.up = UIUtils.createRoundedDrawable(530, 46, 12, Color.valueOf("#554338aa"), Color.valueOf("#443329aa"), 2);
        disabledEventChoiceBtnStyle.font = playerTagFont;
        disabledEventChoiceBtnStyle.fontColor = Color.valueOf("#a89688");

        combatStrikeBtnStyle = new TextButton.TextButtonStyle();
        combatStrikeBtnStyle.up = UIUtils.createRoundedDrawable(380, 52, 16, Color.valueOf("#8a2424"), Color.valueOf("#5a1414"), 2);
        combatStrikeBtnStyle.over = UIUtils.createRoundedDrawable(380, 52, 16, Color.valueOf("#a63232"), Color.valueOf("#5a1414"), 2);
        combatStrikeBtnStyle.font = badgeFont;
        combatStrikeBtnStyle.fontColor = Color.WHITE;

        // Top Event Badge (Positioned to comfortably overlap the top of the enlarged scroll)
        eventBadgeLabel = new Label("EVENT", new Label.LabelStyle(badgeFont, Color.WHITE));
        eventBadgeLabel.setAlignment(Align.center);
        eventBadgeLabel.setWrap(true);
        eventBadgeTable = new Table();
        eventBadgeTable.setBackground(UIUtils.createRoundedDrawable(480, 60, 18, pillBrown));
        eventBadgeTable.add(eventBadgeLabel).width(440).pad(6, 16, 6, 16);

        // Content Table inside the parchment scroll
        eventContentTable = new Table();
        eventContentTable.center();

        Image eventScrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        eventScrollBg.setScaling(Scaling.stretch);

        Stack eventScrollStack = new Stack();
        eventScrollStack.add(eventScrollBg);
        eventScrollStack.add(eventContentTable);

        eventModalTable = new Table();
        eventModalTable.setFillParent(true);
        eventModalTable.setVisible(false);
        eventModalTable.add(eventBadgeTable).size(480, 60).padBottom(-26).center().row();
        eventModalTable.add(eventScrollStack).size(940, 690).center().row();

        hudStage.addActor(eventModalTable);
    }

    /**
     * Displays the interactive choices for the tile landing event.
     * All choices show pure thematic narrative text with zero spoiled numbers or consequences.
     * Spaced and framed inside the enlarged scroll with ample headroom above the bottom curled edge.
     */
    private void showEventChoiceView(final Event event, final Tile tile) {
        if (event == null || tile == null) return;
        activeCurrentEvent = event;
        activeCurrentTile = tile;
        isEventModalActive = true;
        if (inventoryPopup != null) {
            inventoryPopup.setVisible(false);
        }

        final Player player = game.getCurrentPlayer();
        eventContentTable.clear();

        // 1. Color-coded category badge
        Color badgeBgColor;
        String badgePrefix;
        switch (event.getCategory()) {
            case SNAKE:
                badgeBgColor = Color.valueOf("#8a2424");
                badgePrefix = "SERPENT AMBUSH";
                break;
            case MERCHANT:
                badgeBgColor = Color.valueOf("#9c6818");
                badgePrefix = "WANDERING MERCHANT";
                break;
            case SHRINE:
            case BLESSING:
                badgeBgColor = Color.valueOf("#2b4c7e");
                badgePrefix = "ANCIENT SHRINE";
                break;
            case LADDER:
                badgeBgColor = Color.valueOf("#1d5c3a");
                badgePrefix = "LADDER ASCENT";
                break;
            case TRAP:
            default:
                badgeBgColor = Color.valueOf("#7e3b18");
                badgePrefix = "HAZARDOUS TRAIL";
                break;
        }
        eventBadgeTable.setBackground(UIUtils.createRoundedDrawable(480, 60, 18, badgeBgColor));
        eventBadgeLabel.setText(badgePrefix + "  -  TILE " + tile.getPosition());

        // 2. Event Title & Atmospheric Narrative Lore
        Label titleLbl = new Label(event.getTitle(), new Label.LabelStyle(modalTitleFont, Color.valueOf("#361704")));
        titleLbl.setAlignment(Align.center);
        eventContentTable.add(titleLbl).padTop(14).padBottom(6).center().row();

        Label descLbl = new Label(event.getDescription(), new Label.LabelStyle(modalBodyFont, Color.valueOf("#4a2c11")));
        descLbl.setAlignment(Align.center);
        descLbl.setWrap(true);
        eventContentTable.add(descLbl).width(530).padBottom(18).center().row();

        // 3. Dynamic Choices (Hybrid Model: Strategic/tactical actions visible with requirements; unaffordable merchant stock hidden)
        List<EventChoice> choices = event.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            final int choiceIndex = i;
            final EventChoice choice = choices.get(i);
            boolean available = choice.isAvailable(player);

            // In the Hybrid model:
            // 1. Hide Barter choice if inventory is empty
            // 2. Hide unaffordable optional merchant purchases (MIN_EXPERIENCE or MIN_HEALTH at merchant)
            if (!available) {
                if (choice.getRequirementType() == EventChoice.ChoiceRequirementType.CONSUME_ANY_ITEM) {
                    continue;
                }
                if (event.getCategory() == EventCategory.MERCHANT) {
                    continue;
                }
            }

            String buttonText = choice.getText();
            if (!available) {
                String reqTag;
                switch (choice.getRequirementType()) {
                    case EQUIPMENT_DURABILITY:
                        reqTag = " [Requires Shield]";
                        break;
                    case MIN_HEALTH:
                        reqTag = " [Requires >" + choice.getRequirementValue() + " HP]";
                        break;
                    case MIN_EXPERIENCE:
                        reqTag = " [Requires " + choice.getRequirementValue() + " EXP]";
                        break;
                    default:
                        reqTag = " (Unavailable)";
                        break;
                }
                buttonText = buttonText + reqTag;
            }

            TextButton choiceBtn = new TextButton(buttonText, available ? eventChoiceBtnStyle : disabledEventChoiceBtnStyle);
            choiceBtn.getLabel().setWrap(true);
            choiceBtn.getLabel().setAlignment(Align.center);
            choiceBtn.pad(4, 16, 4, 16);
            choiceBtn.getLabelCell().width(480);

            if (available) {
                UIUtils.addScaleHoverEffect(choiceBtn);
                choiceBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent evt, Actor actor) {
                        handleChoiceSelected(choice, choiceIndex, event, tile, player);
                    }
                });
            }

            eventContentTable.add(choiceBtn).size(530, 46).padBottom(6).center().row();
        }

        modalScrim.setVisible(true);
        eventModalTable.setVisible(true);
        turnState = TurnState.RESOLVING_TILE_EVENT;
    }

    /**
     * Dispatches the player's selected choice to combat, barter, or direct resolution.
     */
    private void handleChoiceSelected(EventChoice choice, int choiceIndex, Event event, Tile tile, Player player) {
        if (event.getCategory() == EventCategory.SNAKE) {
            if (choiceIndex == 0) {
                // Choice 0: Draw weapon and engage in battle -> Launch BattleScreen!
                modalScrim.setVisible(false);
                eventModalTable.setVisible(false);
                isEventModalActive = false;
                game.setScreen(new BattleScreen(game, player, (SnakeTile) tile));
                return;
            } else if (choice.getRequirementType() == EventChoice.ChoiceRequirementType.EQUIPMENT_DURABILITY) {
                // Shield deflection: hold ground and ward off snake!
                specialSlideTarget = -1;
                EventChoice.EventResult res = choice.executeWithResult(player);
                showOutcomeView(choice.getText(), res, false);
                return;
            } else {
                // Flee / retreat down the slope!
                specialSlideTarget = ((SnakeTile) tile).getDestination();
                EventChoice.EventResult res = choice.executeWithResult(player);
                showOutcomeView(choice.getText(), res, true);
                return;
            }
        }

        if (choice.getRequirementType() == EventChoice.ChoiceRequirementType.CONSUME_ANY_ITEM) {
            // Barter choice: Open satchel item selection picker!
            showBarterItemSelector(player, choice, tile);
            return;
        }

        // Standard event choice resolution
        EventChoice.EventResult res = choice.executeWithResult(player);

        if (event.getCategory() == EventCategory.LADDER) {
            if (choice.getDifficultyClass() > 0 && !res.checkSucceeded) {
                specialSlideTarget = -1; // Slipped off, cannot ascend!
            } else {
                specialSlideTarget = ((LadderTile) tile).getDestination();
            }
        }

        showOutcomeView(choice.getText(), res, false);
    }

    /**
     * Option A: Interactive Turn-by-Turn Combat Clash against the Serpent.
     * Player taps [STRIKE] each round to roll combat dice, watching HP bars and log update dynamically.
     * Stays strictly within the enlarged scroll and features light, high-readability container styling.
     */
    private void showCombatView(final Player player, final SnakeTile tile) {
        eventContentTable.clear();

        eventBadgeTable.setBackground(UIUtils.createRoundedDrawable(480, 60, 18, Color.valueOf("#8a2424")));
        eventBadgeLabel.setText("SERPENT BATTLE  -  TILE " + tile.getPosition());

        // Build the serpent from the SAME factory BattleScreen uses (balanced, 3 variants)
        final Enemy snake = tile.createSnakeEnemy();
        final int snakeMaxHp = snake.getHealth();
        final int snakeAtk = snake.getAttack();

        final BattleSystem combatBattle = new BattleSystem();
        combatBattle.startBattle(player, snake);

        // Header: Player vs Serpent Status Card (Light Warm Parchment Container with Crisp 3px Mahogany Border)
        Table combatantsTable = new Table();
        combatantsTable.setBackground(UIUtils.createRoundedDrawable(530, 80, 14, Color.valueOf("#faedd7"), Color.valueOf("#854b25"), 3));
        combatantsTable.pad(8, 18, 8, 18);

        // Player Info Box
        Table playerBox = new Table();
        Label pName = new Label(player.getName().toUpperCase(), new Label.LabelStyle(hudNameFont, Color.valueOf("#361704")));
        final Label pHp = new Label("HP: " + player.getHealth() + " / " + player.getMaxHealth(), new Label.LabelStyle(hudHpFont, Color.valueOf("#a81717")));
        Label pAtk = new Label("ATK " + player.getAttack() + "  *  DEF " + player.getDefense(), new Label.LabelStyle(miniFont, Color.valueOf("#54280e")));
        playerBox.add(pName).left().row();
        playerBox.add(pHp).left().row();
        playerBox.add(pAtk).left();

        Label vsLabel = new Label("VS", new Label.LabelStyle(modalTitleFont, Color.valueOf("#8a2424")));

        // Serpent Info Box
        Table snakeBox = new Table();
        String snakeNameStr = (tile.getPosition() <= 30) ? "EMERALD VIPER" : ((tile.getPosition() <= 70) ? "CRAG ASP" : "APEX WYRM");
        Label sName = new Label(snakeNameStr, new Label.LabelStyle(hudNameFont, Color.valueOf("#361704")));
        final Label sHp = new Label("HP: " + snake.getHealth() + " / " + snakeMaxHp, new Label.LabelStyle(hudHpFont, Color.valueOf("#a81717")));
        Label sAtk = new Label("ATK " + snakeAtk + "  *  DEF 0", new Label.LabelStyle(miniFont, Color.valueOf("#54280e")));
        snakeBox.add(sName).right().row();
        snakeBox.add(sHp).right().row();
        snakeBox.add(sAtk).right();

        combatantsTable.add(playerBox).width(205).left();
        combatantsTable.add(vsLabel).width(80).center();
        combatantsTable.add(snakeBox).width(205).right();

        eventContentTable.add(combatantsTable).size(530, 80).padTop(10).padBottom(14).center().row();

        // Combat Narrative Log Inset (Light Cream Container with Crisp Dark Mahogany Text for Maximum Readability)
        Table logContainer = new Table();
        logContainer.setBackground(UIUtils.createRoundedDrawable(530, 140, 12, Color.valueOf("#faedd7"), Color.valueOf("#854b25"), 3));
        logContainer.pad(10, 18, 10, 18);

        final Label combatLog = new Label("The coiled serpent lunges from the foliage!\nTap [STRIKE] to roll your combat dice!", new Label.LabelStyle(modalBodyFont, Color.valueOf("#361704")));
        combatLog.setAlignment(Align.center);
        combatLog.setWrap(true);
        logContainer.add(combatLog).width(490).center();

        eventContentTable.add(logContainer).size(530, 140).padBottom(16).center().row();

        // Action Button: STRIKE (ROLL D20)
        final TextButton actionBtn = new TextButton("STRIKE (ROLL D20)", combatStrikeBtnStyle);
        UIUtils.addScaleHoverEffect(actionBtn);

        final boolean[] combatOver = new boolean[]{false};

        actionBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent evt, Actor actor) {
                if (combatOver[0]) {
                    closeEventModal();
                    return;
                }

                // 1. Player Attack Round
                int pDamage = combatBattle.calculateDamage(player, snake);
                int pRoll = combatBattle.getLastRoll();
                boolean pCrit = combatBattle.isLastRollCritical();
                boolean pMiss = combatBattle.isLastRollMiss();

                int sHealth = Math.max(0, snake.getHealth() - pDamage);
                snake.setHealth(sHealth);
                sHp.setText("HP: " + sHealth + " / " + snakeMaxHp);

                // Check Snake Slain
                if (sHealth <= 0) {
                    combatOver[0] = true;
                    int exp = 15 + (tile.getPosition() / 2);
                    player.gainExperience(exp);
                    Item loot = Item.createRandomLoot();
                    boolean added = player.addItem(loot);
                    specialSlideTarget = -1; // Slain! Hold ground!

                    String hitPrefix = pCrit ? "CRITICAL STRIKE (D20: 20)!" : (pMiss ? "GRAZING BLOW (D20: 1)!" : "DIRECT HIT (D20: " + pRoll + ")!");
                    combatLog.setText(hitPrefix + " You dealt " + pDamage + " damage and slew the beast!\n"
                        + "+" + exp + " EXP gained! Loot: " + loot.getName()
                        + (added ? " (Added to satchel)" : " (Satchel full!)"));
                    actionBtn.setText("CLAIM VICTORY & CONTINUE");
                    updateHudDisplay();
                    return;
                }

                // 2. Serpent Retaliation Round
                int sDamage = combatBattle.calculateDamage(snake, player);
                int sRoll = combatBattle.getLastRoll();
                boolean sCrit = combatBattle.isLastRollCritical();

                int pHealth = player.getHealth() - sDamage;

                if (pHealth <= 0) {
                    combatOver[0] = true;
                    if (player.getHealth() > 25) {
                        // Mortal strike safeguard: clinging to life, slides down slope
                        player.setHealth(1);
                        pHp.setText("HP: 1 / " + player.getMaxHealth());
                        specialSlideTarget = tile.getDestination();
                        combatLog.setText("You struck for " + pDamage + " damage! (Serpent HP: " + sHealth + ")\n"
                            + "[SERPENT D20: " + sRoll + "] The beast lands a MORTAL BLOW for " + sDamage + " damage!\n"
                            + "SURVIVAL INSTINCT: Cast backward down the slope!");
                        actionBtn.setText("FLEE DOWN THE SLOPE");
                    } else {
                        player.setHealth(0);
                        pHp.setText("HP: 0 / " + player.getMaxHealth());
                        combatLog.setText("You struck for " + pDamage + " damage! (Serpent HP: " + sHealth + ")\n"
                            + "[SERPENT D20: " + sRoll + "] The serpent delivered a lethal bite for " + sDamage + " damage!\n"
                            + "You have fallen in battle...");
                        actionBtn.setText("ACCEPT DEFEAT");
                    }
                    updateHudDisplay();
                    return;
                } else if (pHealth <= 25) {
                    combatOver[0] = true;
                    player.setHealth(pHealth);
                    pHp.setText("HP: " + pHealth + " / " + player.getMaxHealth());
                    specialSlideTarget = tile.getDestination();
                    combatLog.setText("You struck for " + pDamage + " damage! (Serpent HP: " + sHealth + ")\n"
                        + "[SERPENT D20: " + sRoll + "] Serpent struck for " + sDamage + " damage! (Your HP: " + pHealth + ")\n"
                        + "You are critically wounded and slide down the slope to safety!");
                    actionBtn.setText("SLIDE DOWN TO SAFETY");
                    updateHudDisplay();
                    return;
                } else {
                    player.setHealth(pHealth);
                    pHp.setText("HP: " + pHealth + " / " + player.getMaxHealth());
                    String playerLog = pCrit ? "CRITICAL HIT (D20: 20)! You dealt " + pDamage + " damage!" : "[D20: " + pRoll + "] You struck for " + pDamage + " damage!";
                    String snakeLog = sCrit ? "\n[SERPENT CRITICAL D20: 20!] Serpent struck back for " + sDamage + " damage!" : "\n[SERPENT D20: " + sRoll + "] Serpent struck back for " + sDamage + " damage! (Your HP: " + pHealth + ")";
                    combatLog.setText(playerLog + snakeLog);
                    updateHudDisplay();
                }
            }
        });

        eventContentTable.add(actionBtn).size(380, 52).center().row();
    }

    /**
     * Interactive Barter Satchel Slot Selector for trading an item with the merchant.
     */
    private void showBarterItemSelector(final Player player, final EventChoice choice, final Tile tile) {
        eventContentTable.clear();

        eventBadgeTable.setBackground(UIUtils.createRoundedDrawable(480, 60, 18, Color.valueOf("#9c6818")));
        eventBadgeLabel.setText("BARTER  -  SELECT AN ITEM");

        Label titleLbl = new Label("Select Item to Trade", new Label.LabelStyle(modalTitleFont, Color.valueOf("#361704")));
        titleLbl.setAlignment(Align.center);
        eventContentTable.add(titleLbl).padTop(10).padBottom(4).center().row();

        Label subLbl = new Label("The merchant inspects your inventory. Choose which relic to trade away:", new Label.LabelStyle(modalBodyFont, Color.valueOf("#4a2c11")));
        subLbl.setAlignment(Align.center);
        subLbl.setWrap(true);
        eventContentTable.add(subLbl).width(530).padBottom(12).center().row();

        int itemCount = (player.getInventory() != null) ? player.getInventory().size() : 0;
        for (int i = 0; i < itemCount; i++) {
            final int slotIndex = i;
            final Item it = player.getInventory().getItem(i);

            String itDesc = it.getName() + "  -  " + (it.getItemType() == ItemType.CONSUMABLE ? "[Consumable]" : "[Dur: " + it.getDurability() + "/" + it.getMaxDurability() + "]");
            TextButton itemBtn = new TextButton(itDesc, eventChoiceBtnStyle);
            itemBtn.getLabel().setWrap(true);
            itemBtn.getLabel().setAlignment(Align.center);
            itemBtn.pad(4, 16, 4, 16);
            itemBtn.getLabelCell().width(480);
            UIUtils.addScaleHoverEffect(itemBtn);
            itemBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent evt, Actor actor) {
                    Item traded = player.getInventory().removeItem(slotIndex);
                    EventChoice.EventResult res = new EventChoice.EventResult();
                    res.tradedItem = traded;
                    res.consequences.add("Traded away: " + (traded != null ? traded.getName() : "Item"));
                    for (EventEffect eff : choice.getSuccessEffects()) {
                        eff.apply(player, res.consequences);
                    }
                    showOutcomeView("Barter Trade Completed", res, false);
                }
            });
            eventContentTable.add(itemBtn).size(530, 46).padBottom(6).center().row();
        }

        TextButton cancelBtn = new TextButton("CANCEL & RETURN", disabledEventChoiceBtnStyle);
        UIUtils.addScaleHoverEffect(cancelBtn);
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent evt, Actor actor) {
                showEventChoiceView(activeCurrentEvent, tile);
            }
        });
        eventContentTable.add(cancelBtn).size(260, 44).padTop(6).center().row();
    }

    /**
     * Interactive Satchel Full Item Swap Prompt when receiving loot with 4/4 capacity.
     */
    private void showItemSwapPrompt(final Player player, final Item newItem) {
        eventContentTable.clear();

        eventBadgeTable.setBackground(UIUtils.createRoundedDrawable(480, 60, 18, Color.valueOf("#a63232")));
        eventBadgeLabel.setText("INVENTORY FULL (4/4)  -  ITEM SWAP");

        Label titleLbl = new Label("Inventory Full!", new Label.LabelStyle(modalTitleFont, Color.valueOf("#361704")));
        titleLbl.setAlignment(Align.center);
        eventContentTable.add(titleLbl).padTop(10).padBottom(4).center().row();

        Label subLbl = new Label("You discovered [" + newItem.getName() + "], but your inventory is full.\nSelect an item to discard and replace, or leave it behind:",
            new Label.LabelStyle(modalBodyFont, Color.valueOf("#4a2c11")));
        subLbl.setAlignment(Align.center);
        subLbl.setWrap(true);
        eventContentTable.add(subLbl).width(530).padBottom(10).center().row();

        for (int i = 0; i < player.getInventory().size(); i++) {
            final int slot = i;
            final Item existing = player.getInventory().getItem(i);
            String label = "Replace: " + existing.getName() + " (" + existing.getEffect() + ")";
            TextButton swapSlotBtn = new TextButton(label, eventChoiceBtnStyle);
            swapSlotBtn.getLabel().setWrap(true);
            swapSlotBtn.getLabel().setAlignment(Align.center);
            swapSlotBtn.pad(4, 16, 4, 16);
            swapSlotBtn.getLabelCell().width(480);
            UIUtils.addScaleHoverEffect(swapSlotBtn);
            swapSlotBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent evt, Actor actor) {
                    player.replaceItem(slot, newItem);
                    EventChoice.EventResult res = new EventChoice.EventResult();
                    res.consequences.add("Replaced [" + existing.getName() + "] with [" + newItem.getName() + "].");
                    showOutcomeView("Item Swapped", res, false);
                }
            });
            eventContentTable.add(swapSlotBtn).size(530, 46).padBottom(6).center().row();
        }

        TextButton leaveBtn = new TextButton("LEAVE " + newItem.getName().toUpperCase() + " BEHIND", disabledEventChoiceBtnStyle);
        UIUtils.addScaleHoverEffect(leaveBtn);
        leaveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent evt, Actor actor) {
                EventChoice.EventResult res = new EventChoice.EventResult();
                res.consequences.add("Left [" + newItem.getName() + "] behind.");
                showOutcomeView("Discovery Left Behind", res, false);
            }
        });
        eventContentTable.add(leaveBtn).size(400, 44).padTop(6).center().row();

        modalScrim.setVisible(true);
        eventModalTable.setVisible(true);
        turnState = TurnState.RESOLVING_TILE_EVENT;
    }

    /**
     * Outcome feedback screen revealing the true consequences and D20 check outcome AFTER selection.
     */
    private void showOutcomeView(String choiceTitle, EventChoice.EventResult res, boolean isRetreat) {
        eventContentTable.clear();

        eventBadgeTable.setBackground(UIUtils.createRoundedDrawable(480, 60, 18, Color.valueOf("#7a4823")));
        eventBadgeLabel.setText("EVENT RESOLUTION");

        Label titleLbl = new Label(choiceTitle, new Label.LabelStyle(modalTitleFont, Color.valueOf("#361704")));
        titleLbl.setAlignment(Align.center);
        titleLbl.setWrap(true);
        eventContentTable.add(titleLbl).width(530).padTop(12).padBottom(6).center().row();

        // D20 Skill Check Outcome Banner (if applicable)
        if (res.difficultyClass > 0) {
            Color d20Color = res.checkSucceeded ? Color.valueOf("#1d5c3a") : Color.valueOf("#8a2424");
            Color d20Border = res.checkSucceeded ? Color.valueOf("#113b24") : Color.valueOf("#5e1414");
            String d20Text = String.format("D20 CHECK: ROLLED %d (DC %d) - %s",
                res.d20Roll, res.difficultyClass, (res.checkSucceeded ? "SUCCESS!" : "FAILED!"));

            Table d20Badge = new Table();
            d20Badge.setBackground(UIUtils.createRoundedDrawable(480, 40, 10, d20Color, d20Border, 2));
            // Use playerTagFont (baked in pure white) so text pops with high contrast on red/green backgrounds
            Label d20Lbl = new Label(d20Text, new Label.LabelStyle(playerTagFont, Color.WHITE));
            d20Lbl.setAlignment(Align.center);
            d20Lbl.setWrap(true);
            d20Badge.add(d20Lbl).width(460).center();

            eventContentTable.add(d20Badge).size(480, 40).padBottom(12).center().row();
        }

        // Consequences List Inset Box (Light Cream Container with Dark Mahogany Text)
        Table outcomeBox = new Table();
        outcomeBox.setBackground(UIUtils.createRoundedDrawable(530, 135, 12, Color.valueOf("#faedd7"), Color.valueOf("#854b25"), 3));
        outcomeBox.pad(10, 20, 10, 20);

        Table linesTable = new Table();
        linesTable.center();

        for (String msg : res.consequences) {
            Label lineLbl = new Label(msg, new Label.LabelStyle(modalBodyFont, Color.valueOf("#361704")));
            lineLbl.setAlignment(Align.center);
            lineLbl.setWrap(true);
            linesTable.add(lineLbl).width(460).padBottom(6).center().row();
        }

        outcomeBox.add(linesTable).width(460).center();
        eventContentTable.add(outcomeBox).size(530, 135).padBottom(16).center().row();

        // Action Button: CONTINUE
        TextButton continueBtn = new TextButton("CONTINUE", eventChoiceBtnStyle);
        continueBtn.getLabel().setWrap(true);
        UIUtils.addScaleHoverEffect(continueBtn);
        continueBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent evt, Actor actor) {
                closeEventModal();
            }
        });
        eventContentTable.add(continueBtn).size(280, 48).center().row();
    }

    /**
     * Closes the active event modal and smoothly transitions turn or handles movement.
     */
    private void closeEventModal() {
        modalScrim.setVisible(false);
        eventModalTable.setVisible(false);
        isEventModalActive = false;
        updateHudDisplay();
        updateMiniMapProgress();

        Player player = game.getCurrentPlayer();
        if (player != null && player.getHealth() <= 0) {
            topBannerLabel.setText(player.getName().toUpperCase() + " DIED! GAME OVER.");
            turnState = TurnState.TURN_TRANSITION;
            actionPauseTimer = 1.0f;
            return;
        }

        if (specialSlideTarget != -1) {
            turnState = TurnState.TILE_ACTION_PAUSE;
            actionPauseTimer = 0.8f;
        } else {
            if (player != null && player.getPosition() >= 100) {
                topBannerLabel.setText("VICTORY! " + player.getName().toUpperCase() + " CLAIMS THE THRONE OF FATE!");
            } else if (isItemMovement) {
                isItemMovement = false;
                turnState = TurnState.WAITING_FOR_ROLL;
                topBannerLabel.setText(player.getName().toUpperCase() + "'S TURN - ROLL THE DICE!");
            } else {
                turnState = TurnState.TURN_TRANSITION;
                actionPauseTimer = 0.6f;
            }
        }
    }

    /**
     * Starts the dice tumbling sequence.
     */
    private void triggerRoll() {
        turnState = TurnState.ROLLING_DICE;
        diceTumbleTimer = 0.75f;
        diceFaceShuffleTimer = 0f;
        rollButton.setColor(1f, 1f, 1f, 0.65f);
        if (diceIcon != null) {
            diceIcon.clearActions();
        }
        diceLabel.setText("ROLLING...");
        topBannerLabel.setText("ROLLING THE DICE OF FATE...");
    }

    @Override
    public void render(float delta) {
        // Hotkeys: Toggle pause with ESC or P, or step back in modal hierarchy
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (isPaused) {
                if (settingsModalTable.isVisible() || leaveConfirmModalTable.isVisible()) {
                    showPauseMenu();
                } else {
                    resumeGame();
                }
            } else {
                pauseGame();
            }
        }

        // 1. Update Game Physics & Camera only when NOT paused
        if (!isPaused) {
            stateTime += delta;
            if (turnState == TurnState.STEPPING_TILES) {
                walkStateTime += delta;
            } else {
                walkStateTime = 0f;
            }
            updateTurnLogic(delta);
            updateCameraTracking(delta);
        }

        // 2. Clear Screen with the Act's Atmospheric Sky Color
        int currentTile = getTileIndexAtX(worldCamera.position.x);
        Color skyColor;
        if (currentTile <= 30) {
            skyColor = Color.valueOf("#42828a"); // Act 1 Atmospheric forest teal daytime sky
        } else if (currentTile <= 70) {
            skyColor = Color.valueOf("#183864"); // Act 2 Vibrant deep sapphire twilight sky
        } else {
            skyColor = Color.valueOf("#4a1424"); // Act 3 Dramatic crimson twilight sky
        }
        Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 3. Render World (Parallax + Ground + Stepping Stones + Characters)
        worldViewport.apply();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();

        renderParallaxBackground(currentTile);
        renderGroundedSteppingStones();
        renderPlayers();

        batch.end();

        // 4. Render Fixed UI Stage Overlay & Modals
        hudStage.getViewport().apply();
        hudStage.act(delta);
        hudStage.draw();
    }

    /**
     * Updates movement state machine, dice tumbling, and tile triggers.
     */
    private void updateTurnLogic(float delta) {
        Player player = game.getCurrentPlayer();
        if (player == null) return;

        switch (turnState) {
            case ROLLING_DICE:
                diceTumbleTimer -= delta;
                diceFaceShuffleTimer -= delta;

                // High-framerate tumbling face cycle with rotation wobble and scale pulse
                if (diceFaceShuffleTimer <= 0f) {
                    diceFaceShuffleTimer = 0.055f;
                    int randomFace = MathUtils.random(1, 6);
                    if (diceIcon != null && diceDrawables[randomFace] != null) {
                        diceIcon.setDrawable(diceDrawables[randomFace]);
                        diceIcon.setRotation(MathUtils.random(-18f, 18f));
                        diceIcon.setScale(MathUtils.random(0.92f, 1.15f));
                    }
                    diceLabel.setText("ROLL: " + randomFace);
                }

                if (diceTumbleTimer <= 0f) {
                    currentRollValue = movementDice.roll();
                    if (diceIcon != null && diceDrawables[currentRollValue] != null) {
                        diceIcon.setDrawable(diceDrawables[currentRollValue]);
                        diceIcon.setRotation(0f);
                        diceIcon.setScale(1f);
                        diceIcon.clearActions();
                        // Punchy landing bounce animation
                        diceIcon.addAction(Actions.sequence(
                            Actions.scaleTo(1.35f, 1.35f, 0.08f, Interpolation.pow2Out),
                            Actions.scaleTo(1.0f, 1.0f, 0.12f, Interpolation.bounceOut)
                        ));
                    }
                    diceLabel.setText("ROLLED: " + currentRollValue);

                    hopCurrentTile = player.getPosition();
                    hopTargetTile = Math.min(100, hopCurrentTile + currentRollValue);

                    topBannerLabel.setText(player.getName().toUpperCase() + " ROLLED A " + currentRollValue + "! ADVANCING...");
                    stepTimer = 0f;
                    turnState = TurnState.STEPPING_TILES;
                }
                break;

            case STEPPING_TILES:
                stepTimer += delta;
                if (stepTimer >= STEP_DURATION) {
                    stepTimer = 0f;
                    hopCurrentTile++;
                    player.setPosition(hopCurrentTile);
                    updateMiniMapProgress();

                    if (hopCurrentTile >= hopTargetTile) {
                        handleTileArrival(player, hopTargetTile);
                    }
                }
                break;

            case RESOLVING_TILE_EVENT:
                // Paused waiting for user interaction on the interactive event modal
                break;

            case TILE_ACTION_PAUSE:
                actionPauseTimer -= delta;
                if (actionPauseTimer <= 0f) {
                    if (specialSlideTarget != -1) {
                        player.setPosition(specialSlideTarget);
                        updateMiniMapProgress();
                        specialSlideTarget = -1;
                        if (isItemMovement) {
                            isItemMovement = false;
                            turnState = TurnState.WAITING_FOR_ROLL;
                            topBannerLabel.setText(player.getName().toUpperCase() + " REACHED TILE " + player.getPosition() + "! ROLL THE DICE!");
                        } else {
                            actionPauseTimer = 0.7f;
                        }
                    } else {
                        if (player.getPosition() >= 100) {
                            topBannerLabel.setText("VICTORY! " + player.getName().toUpperCase() + " CLAIMS THE THRONE OF FATE!");
                        } else if (isItemMovement) {
                            isItemMovement = false;
                            turnState = TurnState.WAITING_FOR_ROLL;
                            topBannerLabel.setText(player.getName().toUpperCase() + "'S TURN - ROLL THE DICE!");
                        } else {
                            turnState = TurnState.TURN_TRANSITION;
                            actionPauseTimer = 0.6f;
                        }
                    }
                }
                break;

            case TURN_TRANSITION:
                actionPauseTimer -= delta;
                if (actionPauseTimer <= 0f) {
                    game.nextTurn();
                    itemUsedThisTurn = false;
                    updateHudDisplay();
                    refreshInventoryUI();
                    rollButton.setColor(Color.WHITE);
                    if (diceIcon != null) {
                        diceIcon.setRotation(0f);
                        diceIcon.setScale(1f);
                    }
                    diceLabel.setText("D6");
                    turnState = TurnState.WAITING_FOR_ROLL;
                    topBannerLabel.setText(game.getCurrentPlayer().getName().toUpperCase() + "'S TURN - ROLL THE DICE!");
                }
                break;

            case WAITING_FOR_ROLL:
            default:
                break;
        }
    }

    /**
     * Handles event, snake, or ladder trigger when landing on a tile.
     */
    /**
     * Handles event, snake, or ladder trigger when landing on a tile.
     */
    private void handleTileArrival(Player player, int tileIndex) {
        Tile tile = board.getTile(tileIndex);
        if (tile == null) return;

        if (tileIndex >= 100) {
            topBannerLabel.setText("VICTORY! " + player.getName().toUpperCase() + " REACHED THE THRONE OF FATE!");
            turnState = TurnState.TILE_ACTION_PAUSE;
            actionPauseTimer = 1.0f;
            return;
        }

        // 1. SNAKE TILE -> Trigger serpent ambush narrative event modal (like ConsoleTestGame)
        if (tile instanceof SnakeTile) {
            SnakeTile snakeTile = (SnakeTile) tile;
            Event event = eventManager.selectEventForTile(tileIndex, EventCategory.SNAKE);
            topBannerLabel.setText("SERPENT AMBUSH ON TILE " + tileIndex + "!");
            showEventChoiceView(event, snakeTile);
            return;
        }
        // 2. LADDER TILE -> Handle climbing
        else if (tile instanceof LadderTile) {
            LadderTile ladder = (LadderTile) tile;
            // 40% chance for an interactive climbing dilemma; 60% for smooth direct climb
            if (eventManager.shouldTriggerLadderEvent()) {
                Event event = eventManager.selectEventForTile(tileIndex, EventCategory.LADDER);
                topBannerLabel.setText("LADDER ASCENT ON TILE " + tileIndex + "! A CLIMBING DILEMMA ARISES...");
                showEventChoiceView(event, ladder);
            } else {
                int dest = ladder.getDestination();
                specialSlideTarget = dest;
                int expEarned = 10 + (tileIndex / 3);
                player.gainExperience(expEarned);
                topBannerLabel.setText("STABLE ASCENT ON TILE " + tileIndex + "! CLIMBING UP TO TILE " + dest + "! (+" + expEarned + " EXP)");
                turnState = TurnState.TILE_ACTION_PAUSE;
                actionPauseTimer = 0.9f;
                updateHudDisplay();
            }
        }
        // 3. NORMAL TILE -> Ambient events or safe ground
        else {
            if (eventManager.shouldTriggerAmbientEvent(player)) {
                Event event = eventManager.selectEventForTile(tileIndex, EventCategory.NORMAL);
                player.resetEventCooldown();
                topBannerLabel.setText("AN ENCOUNTER ON TILE " + tileIndex + "!");
                showEventChoiceView(event, tile);
            } else {
                player.incrementTurnsSinceLastEvent();
                int expEarned = (int) (10 + tileIndex * 0.05f);
                player.gainExperience(expEarned);
                if (isItemMovement) {
                    isItemMovement = false;
                    topBannerLabel.setText(player.getName().toUpperCase() + " ADVANCED TO TILE " + tileIndex + " - SAFE GROUND. ROLL THE DICE!");
                    turnState = TurnState.WAITING_FOR_ROLL;
                } else {
                    topBannerLabel.setText(player.getName().toUpperCase() + " REACHED TILE " + tileIndex + " - SAFE GROUND. (+" + expEarned + " EXP)");
                    turnState = TurnState.TILE_ACTION_PAUSE;
                    actionPauseTimer = 0.8f;
                }
                updateHudDisplay();
            }
        }
    }
    /**
     * Smoothly pans the world camera to follow the active player, anchoring Y to viewport center.
     */
    private void updateCameraTracking(float delta) {
        Player current = game.getCurrentPlayer();
        if (current == null) return;

        float targetX;
        if (turnState == TurnState.STEPPING_TILES) {
            float fromX = getTileCenterX(hopCurrentTile);
            float toX = getTileCenterX(Math.min(100, hopCurrentTile + 1));
            float alpha = stepTimer / STEP_DURATION;
            targetX = MathUtils.lerp(fromX, toX, alpha);
        } else {
            targetX = getTileCenterX(current.getPosition());
        }

        worldCamera.position.x += (targetX - worldCamera.position.x) * (delta * 4.5f);
        // Anchor vertical position to half of current viewport height so y=0 is strictly the bottom
        worldCamera.position.y = worldCamera.viewportHeight / 2f;
        worldCamera.update();
    }

    /**
     * Renders seamlessly composited multi-layer parallax backgrounds across the 3 Acts.
     * Layering order (from back to front):
     * 1. Layer 3 (Far Canopy & Sky): Sits in the back, extends to screen top, 0.15x parallax.
     * 2. Layer 2 (Midground Silhouettes & Ruins): Sits behind the ground ledge, 0.35x parallax.
     * 3. Layer 1 (Foreground Ground Platform): Anchored at y=0, turf embeds stepping stones, 1.0x scroll.
     */
    private void renderParallaxBackground(int currentTile) {
        float camX = worldCamera.position.x;
        float viewW = worldCamera.viewportWidth;
        float viewH = worldCamera.viewportHeight;
        float leftVisible = camX - viewW / 2f;
        float rightVisible = camX + viewW / 2f;

        // Select slices based on Act
        TextureRegion farSlice    = (currentTile <= 30) ? act1Far    : ((currentTile <= 70) ? act2Far    : act3Far);
        TextureRegion midSlice    = (currentTile <= 30) ? act1Mid    : ((currentTile <= 70) ? act2Mid    : act3Mid);
        TextureRegion groundSlice = (currentTile <= 30) ? act1Ground : ((currentTile <= 70) ? act2Ground : act3Ground);

        // =========================================================================
        // 0. Layer 0 (Subterranean Foundation / Lower Console Backdrop):
        // Fills the area below the ground platform (y = 0 to GROUND_BASE_Y + 25f)
        // using the exact dark rock color of Layer 1 for the active Act.
        // This eliminates any floating look and creates a continuous, grounded lower section.
        // =========================================================================
        Color lowerBgColor = (currentTile <= 30)
            ? Color.valueOf("#20202a")
            : ((currentTile <= 70) ? Color.valueOf("#0a204f") : Color.valueOf("#2e1534"));
        batch.setColor(lowerBgColor);
        batch.draw(whitePixelTex, leftVisible - 200f, 0f, viewW + 400f, GROUND_BASE_Y + 25f);
        batch.setColor(Color.WHITE);

        // =========================================================================
        // 1. Layer 3 (Backdrop): Far Canopy / Deep Forest / Moon (0.15x parallax)
        // Elevated to y=190f, scaled to reach beyond screen top, seamlessly tiling with 2px overlap
        // =========================================================================
        float farH = Math.max(920f, viewH - 190f) + 120f;
        float farW = farH * ((float) farSlice.getRegionWidth() / farSlice.getRegionHeight());
        float stepFar = farW - 2f;
        float farOffset = (camX * 0.15f) % stepFar;
        if (farOffset < 0) farOffset += stepFar;

        float farStart = leftVisible - farOffset;
        while (farStart > leftVisible - farW) farStart -= stepFar;
        for (float x = farStart; x < rightVisible + farW; x += stepFar) {
            batch.draw(farSlice, x, 190f, farW, farH);
        }

        // =========================================================================
        // 2. Layer 2 (Midground): Silhouettes & Ancient Ruins (0.35x parallax)
        // Elevated to y=210f; its base is naturally hidden behind the ground platform ledge
        // =========================================================================
        float midH = 560f;
        float midW = midH * ((float) midSlice.getRegionWidth() / midSlice.getRegionHeight());
        float stepMid = midW - 2f;
        float midOffset = (camX * 0.35f) % stepMid;
        if (midOffset < 0) midOffset += stepMid;

        float midStart = leftVisible - midOffset;
        while (midStart > leftVisible - midW) midStart -= stepMid;
        for (float x = midStart; x < rightVisible + midW; x += stepMid) {
            batch.draw(midSlice, x, 210f, midW, midH);
        }

        // =========================================================================
        // 3. Layer 1 (Foreground): Terrain Ground Platform (1.0x scroll with world)
        // Sits at y=GROUND_BASE_Y (110f), seamlessly continuing into the lower background
        // =========================================================================
        float groundH = 260f;
        float groundW = groundH * ((float) groundSlice.getRegionWidth() / groundSlice.getRegionHeight());
        float stepGround = groundW - 2f;
        float groundOffset = (camX) % stepGround;
        if (groundOffset < 0) groundOffset += stepGround;

        float groundStart = leftVisible - groundOffset;
        while (groundStart > leftVisible - groundW) groundStart -= stepGround;
        for (float x = groundStart; x < rightVisible + groundW; x += stepGround) {
            batch.draw(groundSlice, x, GROUND_BASE_Y, groundW, groundH);
        }
    }

    /**
     * Option C: Renders the 100 Grounded Stepping Stones sitting directly on the grass platform.
     */
    private void renderGroundedSteppingStones() {
        float camLeft = worldCamera.position.x - 1100f;
        float camRight = worldCamera.position.x + 1100f;

        Texture stoneNormal = game.getAssetManager().get(Assets.TILE_STONE_NORMAL);
        Texture stoneSnake  = game.getAssetManager().get(Assets.TILE_STONE_SNAKE);
        Texture stoneLadder = game.getAssetManager().get(Assets.TILE_STONE_LADDER);

        for (int i = 1; i <= 100; i++) {
            float tileX = getTileCenterX(i) - TILE_WIDTH / 2f;
            if (tileX + TILE_WIDTH < camLeft || tileX > camRight) continue;

            Tile tile = board.getTile(i);
            Texture slabTex = stoneNormal;
            if (tile instanceof SnakeTile) slabTex = stoneSnake;
            else if (tile instanceof LadderTile) slabTex = stoneLadder;

            // Draw grounded stone slab embedded naturally into the grass platform
            batch.draw(slabTex, tileX, TILE_BOTTOM_Y, TILE_WIDTH, TILE_HEIGHT);

            // Special Tiles: Ladder or Snake Badge Overlay centered cleanly on the stone slab
            // Regular tiles remain clean and unnumbered (HUD mini-map is the single source of truth)
            if (tile instanceof LadderTile) {
                tileBadgeFont.setColor(Color.valueOf("#ffe082"));
                tileBadgeFont.draw(batch, "+ LADDER", tileX, TILE_BOTTOM_Y + 28f, TILE_WIDTH, Align.center, false);
            } else if (tile instanceof SnakeTile) {
                tileBadgeFont.setColor(Color.valueOf("#ff6b6b"));
                tileBadgeFont.draw(batch, "- SNAKE", tileX, TILE_BOTTOM_Y + 28f, TILE_WIDTH, Align.center, false);
            }
        }
    }

    /**
     * Renders all player character sprites.
     * When players share the same tile in multiplayer, the current active player is guaranteed
     * to be drawn on the uppermost layer (in front of all others), centered and fully visible.
     */
    private void renderPlayers() {
        List<Player> players = game.getPlayers();
        if (players.isEmpty()) return;

        Player activePlayer = game.getCurrentPlayer();
        int activePos = (activePlayer != null) ? activePlayer.getPosition() : -1;

        // 1. Render all INACTIVE players first (background layers)
        for (int p = 0; p < players.size(); p++) {
            Player player = players.get(p);
            if (player == activePlayer) continue; // Active player rendered last on the topmost layer

            Animation<TextureRegion> anim = walkAnimations.get(player.getCharacterClass());
            Texture charTex = game.getAssetManager().get(Assets.getCharacterTexture(player.getCharacterClass()));
            TextureRegion idleFrame = (anim != null) ? anim.getKeyFrames()[0] : new TextureRegion(charTex);
            float tileCenterX = getTileCenterX(player.getPosition());

            // If sharing a tile with the active player, offset slightly behind
            boolean sharesWithActive = (player.getPosition() == activePos && turnState != TurnState.STEPPING_TILES);
            float drawX = sharesWithActive ? (tileCenterX - 145f) : (tileCenterX - (CHAR_DISPLAY_SIZE / 2f));
            float drawY = sharesWithActive ? CHAR_DRAW_Y_INACTIVE_SHARED : CHAR_DRAW_Y;
            float size = sharesWithActive ? CHAR_INACTIVE_SIZE : CHAR_DISPLAY_SIZE;

            if (sharesWithActive) {
                // Subtle depth tint so the active player in front pops prominently
                batch.setColor(0.85f, 0.85f, 0.9f, 0.80f);
            } else {
                batch.setColor(Color.WHITE);
            }

            batch.draw(idleFrame, drawX, drawY, size, size);
            batch.setColor(Color.WHITE);
        }

        // 2. Render ACTIVE player LAST (guaranteed on the topmost layer and fully visible)
        if (activePlayer != null) {
            Animation<TextureRegion> anim = walkAnimations.get(activePlayer.getCharacterClass());
            Texture charTex = game.getAssetManager().get(Assets.getCharacterTexture(activePlayer.getCharacterClass()));
            float drawX;
            float drawY = CHAR_DRAW_Y;
            TextureRegion activeFrame;

            if (turnState == TurnState.STEPPING_TILES) {
                float fromX = getTileCenterX(hopCurrentTile);
                float toX = getTileCenterX(Math.min(100, hopCurrentTile + 1));
                float progress = MathUtils.clamp(stepTimer / STEP_DURATION, 0f, 1f);

                // Smooth ground-level walking stride between stepping stones (no parabolic hopping arc)
                drawX = MathUtils.lerp(fromX, toX, progress) - (CHAR_DISPLAY_SIZE / 2f);
                activeFrame = (anim != null) ? anim.getKeyFrame(walkStateTime, true) : new TextureRegion(charTex);
            } else {
                drawX = getTileCenterX(activePlayer.getPosition()) - (CHAR_DISPLAY_SIZE / 2f);
                activeFrame = (anim != null) ? anim.getKeyFrames()[0] : new TextureRegion(charTex);
            }

            batch.setColor(Color.WHITE);
            batch.draw(activeFrame, drawX, drawY, CHAR_DISPLAY_SIZE, CHAR_DISPLAY_SIZE);
        }
    }

    /**
     * Updates HUD elements to reflect active player stats and current progress.
     */
    private void updateHudDisplay() {
        Player player = game.getCurrentPlayer();
        if (player == null) return;

        hudPlayerName.setText("P" + (game.getCurrentTurn() + 1) + ": " + player.getName().toUpperCase());
        int maxHp = (player.getCharacterClass() != null ? player.getCharacterClass().getBaseHealth() : 100);
        hudPlayerHp.setText("HP: " + player.getHealth() + " / " + maxHp);
        hudPlayerExp.setText("LVL " + player.getLevel() + "    -    EXP: " + player.getExperience() + " / " + player.getExpThresh());
        hudPlayerStats.setText("ATK  " + player.getAttack() + "    -    DEF  " + player.getDefense());

        Texture avatarTex = game.getAssetManager().get(Assets.getCharacterTexture(player.getCharacterClass()));
        hudPlayerAvatar.setDrawable(new TextureRegionDrawable(new TextureRegion(avatarTex)));

        updateMiniMapProgress();

        if (inventoryPopup != null && inventoryPopup.isVisible()) {
            refreshInventoryUI();
        }
    }

    private void updateMiniMapProgress() {
        Player player = game.getCurrentPlayer();
        if (player == null) return;

        int pos = player.getPosition();
        String actStr = (pos <= 30) ? "ACT I" : ((pos <= 70) ? "ACT II" : "ACT III");
        miniMapProgressLabel.setText(actStr + "   -   TILE " + pos + " / 100");
    }

    private float getTileCenterX(int tileIndex) {
        return FIRST_TILE_X + (tileIndex - 1) * TILE_SPACING;
    }

    private int getTileIndexAtX(float worldX) {
        int idx = 1 + (int) ((worldX - FIRST_TILE_X + TILE_SPACING / 2f) / TILE_SPACING);
        return MathUtils.clamp(idx, 1, 100);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        // Responsively update both viewports without stretching
        worldViewport.update(width, height, false);
        hudStage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (hudStage != null) hudStage.dispose();
        if (whitePixelTex != null) whitePixelTex.dispose();

        if (titleFont != null) titleFont.dispose();
        if (topBannerFont != null) topBannerFont.dispose();
        if (topActFont != null) topActFont.dispose();
        if (topPauseFont != null) topPauseFont.dispose();
        if (rollBigFont != null) rollBigFont.dispose();
        if (rollSubFont != null) rollSubFont.dispose();
        if (hudNameFont != null) hudNameFont.dispose();
        if (hudHpFont != null) hudHpFont.dispose();
        if (hudExpFont != null) hudExpFont.dispose();
        if (hudStatsFont != null) hudStatsFont.dispose();
        if (tileNumFont != null) tileNumFont.dispose();
        if (tileBadgeFont != null) tileBadgeFont.dispose();
        if (playerTagFont != null) playerTagFont.dispose();
        if (badgeFont != null) badgeFont.dispose();
        if (miniFont != null) miniFont.dispose();
        if (sliderFont != null) sliderFont.dispose();
        if (modalTitleFont != null) modalTitleFont.dispose();
        if (modalBodyFont != null) modalBodyFont.dispose();
    }
}
