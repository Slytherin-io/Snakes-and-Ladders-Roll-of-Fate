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
import com.badlogic.gdx.scenes.scene2d.Group;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.audio.AudioManager;
import io.github.snakes_and_ladders_roll_of_fate.screens.GameSummaryScreen;
import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.board.BoardModifier;
import io.github.snakes_and_ladders_roll_of_fate.board.LadderTile;
import io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile;
import io.github.snakes_and_ladders_roll_of_fate.board.Tile;
import io.github.snakes_and_ladders_roll_of_fate.board.TileLandmark;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Entity;
import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.skills.ClassSkill;
import io.github.snakes_and_ladders_roll_of_fate.skills.SkillResult;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.items.ItemType;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleOutcome;
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
    private Texture vignetteTex;
    private Texture softShadowTex;
    private float reaperAwakenTimer = 0f;

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
    // Pause, Settings, & Leave Menu Fonts (Decoupled from Event Choices)
    private BitmapFont pauseBadgeFont;
    private BitmapFont pauseTitleFont;
    private BitmapFont pauseSubFont;
    private BitmapFont pauseBodyFont;
    private BitmapFont pauseBtnFont;
    private BitmapFont pauseTipFont;

    // Event & Choice System Fonts (Decoupled from Pause Menu)
    private BitmapFont eventBadgeFont;
    private BitmapFont eventTitleFont;
    private BitmapFont eventDescFont;
    private BitmapFont choiceCardTitleFont;
    private BitmapFont choiceBtnFont;
    private BitmapFont choiceActionBtnFont;
    private BitmapFont choiceTagFont;
    private BitmapFont choiceOutcomeTitleFont;
    private BitmapFont choiceOutcomeBodyFont;
    private BitmapFont choiceOutcomeValueFont;
    private BitmapFont choiceD20Font;

    // Parallax Slices for the 3 Acts (Far Canopy, Mid Silhouettes, Foreground Ground)
    private TextureRegion act1Far, act1Mid, act1Ground;
    private TextureRegion act2Far, act2Mid, act2Ground;
    private TextureRegion act3Far, act3Mid, act3Ground;

    // Layout Alignment Constants (Elevated to give the bottom HUD console generous room)
    private static final float GROUND_BASE_Y = 110f;    // Base anchor of the terrain ground slice
    private static final float GROUND_SURFACE_Y = 245f; // Flat grass line of the ground platform
    private static final float TILE_WIDTH = 230f;       // Continuous connected pathway matching TILE_SPACING (zero gaps)
    private static final float TILE_HEIGHT = 55f;
    private static final float TILE_BOTTOM_Y = 220f;    // Act I baseline stone slab bottom Y
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
    private Table dicePickerPopup;
    private Table eventBadgeTable;
    private Label eventBadgeLabel;
    private Table eventContentTable;
    private TextButton.TextButtonStyle eventChoiceBtnStyle;
    private Button.ButtonStyle eventChoiceCardStyle;
    private Button.ButtonStyle eventChoiceCardDisabledStyle;
    private TextButton.TextButtonStyle cancelEventChoiceBtnStyle;
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
    private final Map<CharacterClass, Animation<TextureRegion>> walkAnimations = new HashMap<>();
    private float walkStateTime = 0f;

    // The Pale Shepherd Pursuer Animation & Ambush State
    private Animation<TextureRegion> shepherdIdleAnim;
    private Animation<TextureRegion> shepherdWalkAnim;
    private boolean isShepherdAmbushActive = false;

    // Cinematic Camera Override for Events (e.g. Pale Shepherd Awakening)
    private float cameraFocusOverrideTimer = 0f;
    private int cameraFocusTile = -1;

    // Action Pause Timer (Ladders / Snakes)
    private float actionPauseTimer = 0f;
    private int specialSlideTarget = -1;

    // Special Locomotion Animation (Ascending Ladder Celestial Arc & Descending Snake Slide)
    private enum SpecialMovePhase {
        NONE,
        WINDUP,     // Standing on start tile, power gathers & discovery registers (0.8s)
        IN_FLIGHT,  // Smooth soaring flight across the arc
        LANDED      // Touchdown landing recovery on destination tile (0.6s)
    }
    private boolean isSpecialMoving = false;
    private SpecialMovePhase specialMovePhase = SpecialMovePhase.NONE;
    private float specialMovePhaseTimer = 0f;
    private boolean isLadderMove = true;
    private int specialMoveStartTile = 1;
    private int specialMoveDestTile = 1;
    private float specialMoveElapsed = 0f;
    private float specialMoveDuration = 1.35f;
    private float specialMoveArcHeight = 150f;
    private float specialMoveCurrentX = 0f;
    private float specialMoveCurrentY = 0f;

    // Fog of War / Discovery Flash FX
    private int revealFlashTile = -1;
    private float revealFlashTimer = 0f;

    // HUD UI References
    private Label topBannerLabel;
    private Table bannerTable;
    private Button rollButton;
    private Label diceLabel;
    private Label hudPlayerName;
    private Label hudPlayerHp;
    private Label hudPlayerExp;
    private Label hudPlayerStats;
    private Image hudPlayerAvatar;
    private Label miniMapProgressLabel;
    private Table miniMapTable;
    private Group miniMapTrackGroup;
    private final Map<Player, Image> playerMiniMapMarkers = new HashMap<>();
    private Image shepherdMiniMapMarker;
    private Image voidMiniMapOverlay;
    private boolean isSlideInterceptedByReaper = false;
    private float shepherdDisplayX = -1f;
    private float shepherdDisplayY = -1f;
    private boolean isShepherdGliding = false;
    private Label hudInventorySubLabel;
    private Table inventoryPopup;
    private Table inventorySlotsTable;
    private Label inventoryTitleLabel;
    private Label inventoryCapacityLabel;
    private Label inventoryTipLabel;
    private boolean itemUsedThisTurn = false;
    private boolean isItemMovement = false;

    // Class Skill Button UI (Compact, beside Inventory Satchel)
    private Button skillButton;
    private Button.ButtonStyle skillReadyStyle;
    private Button.ButtonStyle skillCooldownStyle;
    private Image skillIcon;
    private Label skillNameLabel;
    private Label skillStatusLabel;
    private BitmapFont skillTitleFont;
    private BitmapFont skillSubFont;

    // Pause, Settings, & Leave Game Modals
    private boolean isPaused = false;
    private Table modalScrim;
    private Table pauseModalTable;
    private Table settingsModalTable;
    private Table leaveConfirmModalTable;

    // Act Transition Immersion & Toast UI
    private Table actTransitionToastTable;
    private Label actToastHeaderLabel;
    private Label actToastTitleLabel;
    private Label actToastSubtitleLabel;
    private int lastTrackedAct = 1;

    // Post-Battle Resolution Transition Fields
    private BattleOutcome pendingBattleOutcome = null;
    private SnakeTile pendingBattleTile = null;
    private Enemy pendingBossEnemy = null;

    public BoardScreen(RollOfFateGame game) {
        this(game, null, (SnakeTile) null);
    }

    public BoardScreen(RollOfFateGame game, BattleOutcome battleOutcome, SnakeTile battleTile) {
        this.game = game;
        this.pendingBattleOutcome = battleOutcome;
        this.pendingBattleTile = battleTile;
        this.pendingBossEnemy = null;
        this.board = game.getBoard() != null ? game.getBoard() : new Board(100);
        if (game.getBoard() == null) {
            this.board.generateBoard();
        }
    }

    public BoardScreen(RollOfFateGame game, BattleOutcome battleOutcome, Enemy bossEnemy) {
        this.game = game;
        this.pendingBattleOutcome = battleOutcome;
        this.pendingBattleTile = null;
        this.pendingBossEnemy = bossEnemy;
        this.board = game.getBoard() != null ? game.getBoard() : new Board(100);
        if (game.getBoard() == null) {
            this.board.generateBoard();
        }
    }

    @Override
    public void show() {
        updateActMusic();
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

        // 256x256 Smooth Radial Vignette for eerie atmosphere and blackout cutscenes
        Pixmap vig = new Pixmap(256, 256, Pixmap.Format.RGBA8888);
        int half = 128;
        for (int y = 0; y < 256; y++) {
            for (int x = 0; x < 256; x++) {
                float dx = (x - half) / (float) half;
                float dy = (y - half) / (float) half;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float a = 0f;
                if (dist > 0.38f) {
                    a = MathUtils.clamp((dist - 0.38f) / 0.62f, 0f, 1f);
                    a = a * a * (3f - 2f * a); // smoothstep
                }
                vig.setColor(0.01f, 0.02f, 0.05f, a);
                vig.drawPixel(x, y);
            }
        }
        vignetteTex = new Texture(vig);
        vig.dispose();

        // 64x32 Feathered Soft Oval Shadow for floating entities
        Pixmap shadowPx = new Pixmap(64, 32, Pixmap.Format.RGBA8888);
        for (int sy = 0; sy < 32; sy++) {
            for (int sx = 0; sx < 64; sx++) {
                float nx = (sx - 31.5f) / 32f;
                float ny = (sy - 15.5f) / 16f;
                float d = (float) Math.sqrt(nx * nx + ny * ny);
                if (d < 1.0f) {
                    float a = (1.0f - d) * (1.0f - d);
                    shadowPx.setColor(1f, 1f, 1f, a);
                    shadowPx.drawPixel(sx, sy);
                }
            }
        }
        softShadowTex = new Texture(shadowPx);
        shadowPx.dispose();

        // --- Load Fredoka Fonts (Crisp, legible Option 1 pairing across all UI layers) ---
        Color darkBrown = Color.valueOf("#5a2d0c");
        Color nameBrown = Color.valueOf("#361704");
        Color pillBrown = Color.valueOf("#854b25");
        Color goldLight = Color.valueOf("#f6eedb");
        Color crimson   = Color.valueOf("#a81717");
        Color warmBrown = Color.valueOf("#5c2f0f");
        Color expBrown  = Color.valueOf("#693708");

        titleFont      = UIUtils.createFont("ui/Fredoka-Bold.ttf", 26, Color.WHITE);
        topBannerFont  = UIUtils.createFont("ui/Fredoka-Bold.ttf", 32, Color.valueOf("#fffdfa"));
        topActFont     = UIUtils.createFont("ui/Fredoka-Bold.ttf", 23, Color.valueOf("#fdf5e2"));
        topPauseFont   = UIUtils.createFont("ui/Fredoka-Bold.ttf", 25, Color.WHITE);
        rollBigFont    = UIUtils.createFont("ui/Fredoka-Bold.ttf", 30, Color.WHITE);
        rollSubFont    = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 21, Color.valueOf("#fbf2dc"));
        hudNameFont    = UIUtils.createFont("ui/Fredoka-Bold.ttf", 24, nameBrown);
        hudHpFont      = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 21, crimson);
        hudExpFont     = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 19, expBrown);
        hudStatsFont   = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 19, warmBrown);
        playerTagFont  = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 19, Color.WHITE);
        badgeFont      = UIUtils.createFont("ui/Fredoka-Bold.ttf", 22, Color.WHITE);
        tileNumFont    = UIUtils.createFont("ui/Fredoka-Bold.ttf", 24, goldLight);
        tileBadgeFont  = UIUtils.createFont("ui/Fredoka-Bold.ttf", 15, Color.WHITE);
        miniFont       = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 16, Color.WHITE);
        sliderFont     = UIUtils.createFont("ui/Fredoka-Bold.ttf", 24, darkBrown);
        // Pause, Settings, & Leave Menu Fonts (Decoupled from Event Choices)
        pauseBadgeFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 22, Color.WHITE);
        pauseTitleFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 30, darkBrown);
        pauseSubFont   = UIUtils.createFont("ui/Fredoka-Bold.ttf", 26, darkBrown);
        pauseBodyFont  = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 22, darkBrown);
        pauseBtnFont   = UIUtils.createFont("ui/Fredoka-Bold.ttf", 22, Color.WHITE);
        pauseTipFont   = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 22, Color.valueOf("#54280e"));

        // Event & Choice System Fonts (Decoupled from Pause Menu)
        eventBadgeFont         = UIUtils.createFont("ui/Fredoka-Bold.ttf", 21, Color.WHITE);
        eventTitleFont         = UIUtils.createFont("ui/Fredoka-Bold.ttf", 32, Color.valueOf("#361704"));
        eventDescFont          = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 20, Color.valueOf("#5a3616"));
        choiceCardTitleFont    = UIUtils.createFont("ui/Fredoka-Bold.ttf", 20, Color.valueOf("#fff8ea"));
        choiceBtnFont          = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 17, Color.valueOf("#f2dcc2"));
        choiceActionBtnFont    = UIUtils.createFont("ui/Fredoka-Bold.ttf", 20, Color.valueOf("#fff8ea"));
        choiceTagFont          = UIUtils.createFont("ui/Fredoka-Bold.ttf", 13, Color.WHITE);
        choiceOutcomeTitleFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 30, Color.valueOf("#361704"));
        choiceOutcomeBodyFont  = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 21, Color.valueOf("#5a3616"));
        choiceOutcomeValueFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 22, Color.WHITE);
        choiceD20Font          = UIUtils.createFont("ui/Fredoka-Bold.ttf", 19, Color.WHITE);

        // Class Skill Fonts
        skillTitleFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 18, nameBrown);
        skillSubFont   = UIUtils.createFont("ui/Fredoka-Bold.ttf", 13, Color.valueOf("#6d3a17"));

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
        buildActTransitionBanner();
        buildMiniMap();
        buildBottomHud();
        buildInventoryPopup();
        buildDicePickerPopup();
        buildPauseAndSettingsModals(darkBrown, pillBrown);
        buildEventModal(darkBrown, pillBrown);

        // Configure global item swap handler for full satchel situations
        EventEffect.setItemSwapHandler(new EventEffect.ItemSwapHandler() {
            @Override
            public void handleSwap(Player player, Item newItem) {
                showItemSwapPrompt(player, newItem);
            }
        });

        // Configure global scout handler for event-driven map foresight
        EventEffect.setScoutHandler(new EventEffect.ScoutHandler() {
            @Override
            public int scoutTiles(Player player, int tilesAhead) {
                int start = player.getPosition() + 1;
                int end = Math.min(board.getBoardSize(), player.getPosition() + tilesAhead);
                int uncovered = board.revealRange(start, end);
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.SCOUT_FLARE);
                }
                return uncovered;
            }
        });

        // Initial camera placement on active player or returning battle tile
        Player current = game.getCurrentPlayer();
        if (pendingBattleOutcome != null && pendingBattleTile != null) {
            int battleTilePos = pendingBattleTile.getPosition();
            if (current != null) {
                current.setPosition(battleTilePos);
                Tile curTile = board.getTile(battleTilePos);
                if (curTile != null && !curTile.isRevealed()) {
                    curTile.setRevealed(true);
                }
            }
            float startX = getTileCenterX(battleTilePos);
            lastTrackedAct = (battleTilePos <= 30) ? 1 : ((battleTilePos <= 70) ? 2 : 3);
            float startY = 540f + (getTileBottomY(battleTilePos) - 220f);
            worldCamera.position.set(startX, startY, 0f);
            worldCamera.update();

            updateHudDisplay();

            if (pendingBattleOutcome == BattleOutcome.ESCAPED || pendingBattleOutcome == BattleOutcome.WEAKENED) {
                if (current != null) {
                    topBannerLabel.setText(current.getName().toUpperCase() + " ESCAPED - SLIDING DOWN TO TILE " + pendingBattleTile.getDestination() + "!");
                }
                rollButton.setColor(1f, 1f, 1f, 0.5f);
                startSpecialMove(battleTilePos, pendingBattleTile.getDestination(), false);
            } else if (pendingBattleOutcome == BattleOutcome.VICTORY) {
                // Slayed the Serpent!
                // Serpent's Bane: Repels The Pale Shepherd 4 tiles back and pauses him for 1 round!
                if (game.getPaleShepherdTile() > 0) {
                    int repelled = Math.max(1, game.getPaleShepherdTile() - 4);
                    game.setPaleShepherdTile(repelled);
                    shepherdDisplayX = getTileCenterX(repelled);
                    shepherdDisplayY = getCharacterDrawY(repelled);
                    game.setShepherdPauseRounds(Math.max(game.getShepherdPauseRounds(), 1));
                    updateMiniMapProgress();
                }
                // Adrenaline Surge: Victorious player immediately gains an extra roll!
                if (current != null) {
                    topBannerLabel.setText(current.getName().toUpperCase() + " SLAYED THE SERPENT! REAPER REPELLED! ADRENALINE SURGE: ROLL AGAIN!");
                }
                turnState = TurnState.WAITING_FOR_ROLL;
                rollButton.setColor(1f, 1f, 1f, 1f);
            } else if (pendingBattleOutcome == BattleOutcome.DEFEAT) {
                if (current != null) {
                    topBannerLabel.setText(current.getName().toUpperCase() + " HAS FALLEN IN BATTLE! NEXT TRAVELER'S TURN...");
                }
                rollButton.setColor(1f, 1f, 1f, 0.5f);
                turnState = TurnState.TURN_TRANSITION;
                actionPauseTimer = 1.2f;
            }

            pendingBattleOutcome = null;
            pendingBattleTile = null;
        } else if (pendingBattleOutcome != null && pendingBossEnemy != null) {
            // Returning from Boss / Pale Shepherd combat!
            int curPos = (current != null) ? current.getPosition() : 1;
            float startX = getTileCenterX(curPos);
            lastTrackedAct = (curPos <= 30) ? 1 : ((curPos <= 70) ? 2 : 3);
            float startY = 540f + (getTileBottomY(curPos) - 220f);
            worldCamera.position.set(startX, startY, 0f);
            worldCamera.update();

            updateHudDisplay();

            if (pendingBossEnemy.getEnemyType() != null && pendingBossEnemy.getEnemyType().contains("Pale Shepherd")) {
                if (pendingBattleOutcome == BattleOutcome.VICTORY) {
                    if (current != null) {
                        current.gainExperience(50);
                    }
                    int repelled = Math.max(0, game.getPaleShepherdTile() - 15);
                    game.setPaleShepherdTile(repelled);
                    shepherdDisplayX = getTileCenterX(repelled);
                    shepherdDisplayY = getCharacterDrawY(repelled);
                    game.setShepherdPauseRounds(3);
                    updateMiniMapProgress();
                    if (current != null) {
                        topBannerLabel.setText(current.getName().toUpperCase() + " STRUCK DOWN DEATH! REAPER BANISHED 15 TILES! ROLL THE DICE!");
                    }
                    turnState = TurnState.WAITING_FOR_ROLL;
                    rollButton.setColor(1f, 1f, 1f, 1f);
                } else if (pendingBattleOutcome == BattleOutcome.ESCAPED) {
                    // Parting blow taken: -20 HP
                    if (current != null) {
                        current.setHealth(Math.max(1, current.getHealth() - 20));
                    }
                    game.setShepherdPauseRounds(1);
                    int scrambleDest = (current != null) ? Math.min(99, current.getPosition() + 3) : 1;
                    if (current != null) {
                        topBannerLabel.setText(current.getName().toUpperCase() + " SCRAMBLED AWAY FROM DEATH! (-20 HP, +3 TILES AHEAD)!");
                    }
                    startSpecialMove(curPos, scrambleDest, false);
                } else {
                    if (current != null) {
                        topBannerLabel.setText(current.getName().toUpperCase() + " HAS BEEN CLAIMED BY THE PALE SHEPHERD...");
                    }
                    rollButton.setColor(1f, 1f, 1f, 0.5f);
                    turnState = TurnState.TURN_TRANSITION;
                    actionPauseTimer = 1.2f;
                }
            }

            pendingBattleOutcome = null;
            pendingBossEnemy = null;
        } else {
            if (current != null) {
                Tile curTile = board.getTile(current.getPosition());
                if (curTile != null && !curTile.isRevealed()) {
                    curTile.setRevealed(true);
                }
            }
            float startX = current != null ? getTileCenterX(current.getPosition()) : FIRST_TILE_X;
            int currentPos = (current != null) ? current.getPosition() : 1;
            lastTrackedAct = (currentPos <= 30) ? 1 : ((currentPos <= 70) ? 2 : 3);
            float startY = 540f + (getTileBottomY(currentPos) - 220f);
            worldCamera.position.set(startX, startY, 0f);
            worldCamera.update();

            updateHudDisplay();
        }
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

        if (game.getAssetManager().isLoaded(Assets.CHAR_PALE_SHEPHERD)) {
            Texture shepherdSheet = game.getAssetManager().get(Assets.CHAR_PALE_SHEPHERD);
            TextureRegion[][] shepherdGrid = TextureRegion.split(shepherdSheet, 256, 256);
            shepherdIdleAnim = new Animation<>(0.20f, shepherdGrid[0]);
            shepherdIdleAnim.setPlayMode(Animation.PlayMode.LOOP);
            shepherdWalkAnim = new Animation<>(0.14f, shepherdGrid[1]);
            shepherdWalkAnim.setPlayMode(Animation.PlayMode.LOOP);
        }
    }

    /**
     * Top Ribbon Banner showing current turn guidance and live tile events.
     */
    private void buildTopBanner() {
        String startBanner = (board.getActiveModifier() != null)
            ? "FATE: " + board.getActiveModifier().getDisplayName().toUpperCase() + "  -  ROLL THE DICE!"
            : "PLAYER 1'S TURN - ROLL THE DICE!";
        topBannerLabel = new Label(startBanner, new Label.LabelStyle(topBannerFont, Color.valueOf("#fffdfa")));
        topBannerLabel.setAlignment(Align.center);
        topBannerLabel.setWrap(true);

        bannerTable = new Table();
        bannerTable.setBackground(UIUtils.createRoundedDrawable(920, 70, 20, Color.valueOf("#854b25")));
        bannerTable.add(topBannerLabel).width(860).pad(10, 24, 10, 24).center();
    }

    /**
     * Builds the cinematic Act Transition Toast card that glides in when crossing biomes.
     * Unified in the same top-center column with bannerTable to strictly prevent any overlaps.
     */
    private void buildActTransitionBanner() {
        actTransitionToastTable = new Table();
        actTransitionToastTable.setBackground(UIUtils.createRoundedDrawable(760, 100, 22, Color.valueOf("#120d1cf0")));
        actTransitionToastTable.setTouchable(Touchable.disabled);

        actToastHeaderLabel = new Label("~ ENTERING ACT II ~", new Label.LabelStyle(miniFont, Color.valueOf("#ffe082")));
        actToastHeaderLabel.setAlignment(Align.center);

        actToastTitleLabel = new Label("THE SUNKEN RUINS", new Label.LabelStyle(topBannerFont, Color.valueOf("#80d8ff")));
        actToastTitleLabel.setAlignment(Align.center);

        actToastSubtitleLabel = new Label("Ancient shadows whisper through the deep sapphire mist", new Label.LabelStyle(miniFont, Color.valueOf("#e1f5fe")));
        actToastSubtitleLabel.setAlignment(Align.center);

        actTransitionToastTable.add(actToastHeaderLabel).padTop(10).center().row();
        actTransitionToastTable.add(actToastTitleLabel).padTop(2).padBottom(2).center().row();
        actTransitionToastTable.add(actToastSubtitleLabel).padBottom(10).center();

        actTransitionToastTable.setVisible(false);
        actTransitionToastTable.getColor().a = 0f;

        Table topCenterContainer = new Table();
        topCenterContainer.setFillParent(true);
        topCenterContainer.top().padTop(22);
        topCenterContainer.add(bannerTable).width(920).minHeight(70).center().row();
        topCenterContainer.add(actTransitionToastTable).size(760, 100).padTop(18).center();

        hudStage.addActor(topCenterContainer);
    }

    private void showActTransitionToast(int act) {
        if (actTransitionToastTable == null) return;

        if (act == 2) {
            actToastHeaderLabel.setText("~ ENTERING ACT II ~");
            actToastTitleLabel.setText("THE SUNKEN RUINS");
            actToastTitleLabel.setStyle(new Label.LabelStyle(topBannerFont, Color.valueOf("#80d8ff")));
            actToastSubtitleLabel.setText("Ancient shadows whisper through the deep sapphire mist");
            topBannerLabel.setText("ENTERING ACT II: THE SUNKEN RUINS! ANCIENT MYSTERIES AWAIT.");
        } else if (act == 3) {
            actToastHeaderLabel.setText("~ ENTERING ACT III ~");
            actToastTitleLabel.setText("THE INFERNAL VOID");
            actToastTitleLabel.setStyle(new Label.LabelStyle(topBannerFont, Color.valueOf("#ff8a80")));
            actToastSubtitleLabel.setText("The final perilous ascent towards the Throne of Fate");
            topBannerLabel.setText("ENTERING ACT III: THE INFERNAL VOID! THE THRONE OF FATE AWAITS.");
        }

        actTransitionToastTable.clearActions();
        actTransitionToastTable.setVisible(true);
        actTransitionToastTable.getColor().a = 0f;
        actTransitionToastTable.setTransform(true);
        actTransitionToastTable.setScale(0.92f);
        actTransitionToastTable.setOrigin(Align.center);

        actTransitionToastTable.addAction(Actions.sequence(
            Actions.parallel(Actions.fadeIn(0.45f, Interpolation.fade), Actions.scaleTo(1.0f, 1.0f, 0.45f, Interpolation.swingOut)),
            Actions.delay(3.2f),
            Actions.parallel(Actions.fadeOut(0.7f, Interpolation.fade), Actions.scaleTo(0.95f, 0.95f, 0.7f, Interpolation.fade)),
            Actions.run(() -> actTransitionToastTable.setVisible(false))
        ));
    }

    /**
     * Top-Left Linear 100-Tile Progress Mini-Map with Act color indicators
     * and real-time player position monitoring markers.
     */
    private void buildMiniMap() {
        miniMapTable = new Table();
        miniMapTable.setBackground(UIUtils.createRoundedDrawable(390, 84, 18, Color.valueOf("#4a2307dd")));

        miniMapProgressLabel = new Label("ACT I   -   TILE 1 / 100", new Label.LabelStyle(topActFont, Color.valueOf("#fdf5e2")));
        miniMapProgressLabel.setAlignment(Align.center);

        // Visual 3-act progress bar track with player monitoring markers
        miniMapTrackGroup = new Group();
        miniMapTrackGroup.setSize(340, 36);

        // 3-Act Bar Track Segments
        Image act1Seg = new Image(UIUtils.createRoundedDrawable(102, 8, 4, Color.valueOf("#2e7d32")));
        act1Seg.setBounds(0, 4, 102, 8);
        Image act2Seg = new Image(UIUtils.createRoundedDrawable(128, 8, 4, Color.valueOf("#1565c0")));
        act2Seg.setBounds(106, 4, 128, 8);
        Image act3Seg = new Image(UIUtils.createRoundedDrawable(102, 8, 4, Color.valueOf("#c62828")));
        act3Seg.setBounds(238, 4, 102, 8);

        miniMapTrackGroup.addActor(act1Seg);
        miniMapTrackGroup.addActor(act2Seg);
        miniMapTrackGroup.addActor(act3Seg);

        // The Pale Void track overlay: dark obsidian-crimson shroud covering tiles <= shepherdTile
        voidMiniMapOverlay = new Image(UIUtils.createRoundedDrawable(10, 8, 4, Color.valueOf("#260810"), Color.valueOf("#4a0e17"), 1));
        voidMiniMapOverlay.setBounds(0, 4, 0, 8);
        voidMiniMapOverlay.setVisible(false);
        miniMapTrackGroup.addActor(voidMiniMapOverlay);

        // Populate player head markers
        initPlayerMiniMapMarkers();

        miniMapTable.add(miniMapProgressLabel).padTop(6).padBottom(2).center().row();
        miniMapTable.add(miniMapTrackGroup).size(340, 36).padBottom(6).center();

        Table container = new Table();
        container.setFillParent(true);
        container.top().left().padTop(22).padLeft(35);
        container.add(miniMapTable).size(390, 84);

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
        hudInventorySubLabel = new Label("0 / 4 SLOTS", new Label.LabelStyle(miniFont, Color.valueOf("#6d3a17")));
        invTextTable.add(invLabel).left().padBottom(2).row();
        invTextTable.add(hudInventorySubLabel).left();

        inventoryBtn.add(satchelIcon).size(56, 56).padLeft(18).padRight(14).center();
        inventoryBtn.add(invTextTable).center().padRight(18);

        inventoryBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
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

        // --- 1.5. Compact Active Class Skill Button (Beside Inventory Satchel) ---
        Color cooldownSolid  = Color.valueOf("#7d5638");
        Color cooldownHover  = Color.valueOf("#8d6342");
        Color cooldownBorder = Color.valueOf("#54341e");

        skillReadyStyle = new Button.ButtonStyle();
        skillReadyStyle.up = UIUtils.createRoundedDrawable(185, 120, 20, leatherSolid, containerBorder, 3);
        skillReadyStyle.over = UIUtils.createRoundedDrawable(185, 120, 20, leatherHover, containerBorder, 3);

        skillCooldownStyle = new Button.ButtonStyle();
        skillCooldownStyle.up = UIUtils.createRoundedDrawable(185, 120, 20, cooldownSolid, cooldownBorder, 3);
        skillCooldownStyle.over = UIUtils.createRoundedDrawable(185, 120, 20, cooldownHover, cooldownBorder, 3);

        skillButton = new Button(skillCooldownStyle);
        UIUtils.addScaleHoverEffect(skillButton);

        skillIcon = new Image();
        skillIcon.setScaling(Scaling.fit);

        Table skillTextTable = new Table();
        skillNameLabel = new Label("SKILL", new Label.LabelStyle(skillTitleFont, nameBrown));
        skillNameLabel.setAlignment(Align.left);
        skillStatusLabel = new Label("4 TURNS", new Label.LabelStyle(skillSubFont, Color.valueOf("#fbf2dc")));
        skillStatusLabel.setAlignment(Align.left);

        skillTextTable.add(skillNameLabel).left().padBottom(2).row();
        skillTextTable.add(skillStatusLabel).left();

        skillButton.add(skillIcon).size(42, 42).padLeft(12).padRight(10).center();
        skillButton.add(skillTextTable).left().padRight(12).center();

        skillButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                if (turnState != TurnState.WAITING_FOR_ROLL || isEventModalActive || cameraFocusOverrideTimer > 0f) {
                    return;
                }
                Player current = game.getCurrentPlayer();
                if (current == null) return;

                if (!current.isSkillReady()) {
                    int cd = current.getSkillCooldown();
                    String sName = (current.getClassSkill() != null) ? current.getClassSkill().getSkillName().toUpperCase() : "SKILL";
                    topBannerLabel.setText(sName + " RECHARGING (" + cd + (cd == 1 ? " TURN" : " TURNS") + " REMAINING)!");
                    return;
                }

                SkillResult res = current.useClassSkill(game);
                if (res != null && res.isSuccess()) {
                    topBannerLabel.setText(res.getMessage().toUpperCase());
                    updateHudDisplay();
                    updateMiniMapProgress();

                    if (res.getBonusMovement() > 0) {
                        hopCurrentTile = current.getPosition();
                        cameraFocusTile = current.getPosition();
                        if (current.getPosition() >= 100) {
                            triggerDawnWardenSummitTrial(current);
                        }
                    }
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
                if (turnState == TurnState.WAITING_FOR_ROLL && cameraFocusOverrideTimer <= 0f) {
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    }
                    triggerRoll();
                }
            }
        });

        // --- 3. Right: Active Player Status Card ---
        Table statusCard = new Table();
        statusCard.setBackground(UIUtils.createRoundedDrawable(430, 120, 20, leatherSolid, containerBorder, 3));

        hudPlayerAvatar = new Image(game.getAssetManager().get(Assets.CHAR_TURTLE));
        hudPlayerAvatar.setScaling(Scaling.fit);
        hudPlayerAvatar.setAlign(Align.center);

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
        bottomBar.add(skillButton).size(185, 120).padLeft(14);
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
        inventoryPopup.setBackground(UIUtils.createRoundedDrawable(630, 580, 20, Color.valueOf("#3d1b06f8"), Color.valueOf("#6d3a17"), 2));
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

        // Footer Actions: Centered "CLOSE" button
        Table footerTable = new Table();

        TextButton.TextButtonStyle closeStyle = new TextButton.TextButtonStyle();
        closeStyle.up = UIUtils.createRoundedDrawable(180, 48, 14, pillBrown);
        closeStyle.over = UIUtils.createRoundedDrawable(180, 48, 14, Color.valueOf("#9c572b"));
        closeStyle.font = badgeFont;
        closeStyle.fontColor = Color.WHITE;

        TextButton closeBtn = new TextButton("CLOSE", closeStyle);
        UIUtils.addScaleHoverEffect(closeBtn);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                inventoryPopup.setVisible(false);
            }
        });

        footerTable.add(closeBtn).size(180, 48).center();

        inventoryPopup.add(footerTable).width(570).padBottom(20).center();

        Table popupContainer = new Table();
        popupContainer.setFillParent(true);
        popupContainer.bottom().left().padBottom(155).padLeft(35);
        popupContainer.add(inventoryPopup).size(630, 580);

        hudStage.addActor(popupContainer);
    }

    /**
     * Builds the Golden Die 1-6 Fate Picker popup for choosing an exact dice roll.
     */
    private void buildDicePickerPopup() {
        Color darkLeather = Color.valueOf("#3d1b06f5");
        Color borderGold = Color.valueOf("#ffd54f");

        dicePickerPopup = new Table();
        dicePickerPopup.setBackground(UIUtils.createRoundedDrawable(600, 175, 20, darkLeather, borderGold, 3));
        dicePickerPopup.setVisible(false);

        Label header = new Label("GOLDEN DIE ACTIVE - CHOOSE YOUR EXACT ROLL:", new Label.LabelStyle(playerTagFont, borderGold));
        dicePickerPopup.add(header).padTop(14).padBottom(12).center().row();

        Table diceRow = new Table();
        for (int face = 1; face <= 6; face++) {
            final int chosenFace = face;
            Button.ButtonStyle faceStyle = new Button.ButtonStyle();
            faceStyle.up = UIUtils.createRoundedDrawable(76, 76, 14, Color.valueOf("#854b25"), borderGold, 2);
            faceStyle.over = UIUtils.createRoundedDrawable(76, 76, 14, Color.valueOf("#a66032"), Color.WHITE, 2);

            Button faceBtn = new Button(faceStyle);
            Image faceImg = new Image(diceDrawables[face]);
            faceImg.setScaling(Scaling.fit);
            faceBtn.add(faceImg).size(52, 52).center();
            UIUtils.addScaleHoverEffect(faceBtn);

            faceBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                        game.getAudioManager().playSound(AudioManager.SoundEffect.DICE_ROLL);
                    }
                    dicePickerPopup.setVisible(false);
                    Player p = game.getCurrentPlayer();
                    if (p != null) {
                        p.setPendingDiceChoice(false);
                    }
                    resolveDiceRollResult(chosenFace, " (GOLDEN DIE)");
                }
            });

            diceRow.add(faceBtn).size(76, 76).pad(0, 8, 0, 8);
        }
        dicePickerPopup.add(diceRow).padBottom(14).center();

        Table wrap = new Table();
        wrap.setFillParent(true);
        wrap.bottom().center().padBottom(165);
        wrap.add(dicePickerPopup).size(600, 175);
        hudStage.addActor(wrap);
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
        updateInventoryButtonCount();

        if (inventoryTipLabel != null) {
            if (itemUsedThisTurn) {
                inventoryTipLabel.setText("Item already used this turn. (Limit: 1 item per turn)");
                inventoryTipLabel.setColor(Color.valueOf("#ffd54f"));
            } else {
                inventoryTipLabel.setText("Limit: 1 item per turn. Tap USE before rolling the dice.");
                inventoryTipLabel.setColor(Color.valueOf("#fffdfa"));
            }
        }

        if (player.getInventory().isFull()) {
            inventoryCapacityLabel.setColor(Color.valueOf("#ff6b6b"));
        } else {
            inventoryCapacityLabel.setColor(Color.valueOf("#ffd54f"));
        }

        // Render exactly 4 slots (Slot 0..3)
        for (int i = 0; i < capacity; i++) {
            Table slotRow = new Table();
            slotRow.pad(6, 12, 6, 12);

            // Fixed icon box container so ALL slots (populated and empty) have identical alignment
            Table iconBox = new Table();

            if (i < itemCount) {
                Item item = player.getInventory().getItem(i);
                boolean isCurse = item.isCurse();
                Color rowBg = isCurse ? Color.valueOf("#4a1212cc") : Color.valueOf("#54280ecc");
                Color rowBorder = isCurse ? Color.valueOf("#a63232") : Color.valueOf("#854b25");
                slotRow.setBackground(UIUtils.createRoundedDrawable(580, 80, 12, rowBg, rowBorder, 1));

                // Item Icon centered inside fixed icon box
                Texture iconTex = game.getAssetManager().get(Assets.getItemIcon(item));
                Image iconImg = new Image(iconTex);
                iconImg.setScaling(Scaling.fit);
                if (isCurse) {
                    iconImg.setColor(Color.valueOf("#ff8a80"));
                }
                iconBox.add(iconImg).size(42, 42).center();
                slotRow.add(iconBox).size(48, 48).padLeft(2).padRight(12).center();

                // Info text: Name on line 1, metadata under title on line 2
                Table infoTable = new Table();
                infoTable.left();

                // Line 1: Item Name alone (Bright crisp white, or soft crimson for curses)
                Label nameLbl = new Label(item.getName(), new Label.LabelStyle(playerTagFont, isCurse ? Color.valueOf("#fecaca") : Color.WHITE));
                infoTable.add(nameLbl).left().row();

                // Line 2: Type badge + separator + effect description (Under title)
                Table subInfo = new Table();
                subInfo.left();

                String typeStr;
                Color typeColor;
                if (isCurse) {
                    typeStr = "[CURSE - PASSIVE]";
                    typeColor = Color.valueOf("#fca5a5");
                } else if (item.getItemType() == ItemType.EQUIPMENT) {
                    typeStr = "[1-USE RELIC]";
                    typeColor = Color.valueOf("#93c5fd");
                } else {
                    typeStr = "[CONSUMABLE]";
                    typeColor = Color.valueOf("#86efac");
                }
                Label typeLbl = new Label(typeStr, new Label.LabelStyle(miniFont, typeColor));

                Label dotLbl = new Label(" - ", new Label.LabelStyle(miniFont, Color.valueOf("#fef08a")));
                Label effectLbl = new Label(item.getEffect(), new Label.LabelStyle(miniFont, Color.valueOf("#fffdfa")));
                effectLbl.setWrap(true);

                subInfo.add(typeLbl).left();
                subInfo.add(dotLbl).left();
                subInfo.add(effectLbl).width(215).left();
                infoTable.add(subInfo).padTop(3).left();

                // Fixed width table (350px) + expanding spacer prevents any text overflow from touching USE button
                slotRow.add(infoTable).width(350).left();
                slotRow.add().expandX();

                // USE button (Disabled cleanly for passive Curses or insufficient HP on blood-cost items)
                boolean lethalSelfCost = (item.getSecondaryEffectType() == EventEffectType.HEALTH
                    && item.getSecondaryEffectValue() < 0
                    && player.getHealth() + item.getSecondaryEffectValue() <= 0);
                TextButton.TextButtonStyle useStyle = new TextButton.TextButtonStyle();
                useStyle.up = UIUtils.createRoundedDrawable(88, 42, 10,
                    isCurse ? Color.valueOf("#7f1d1d") : Color.valueOf("#2e7d32"));
                useStyle.over = UIUtils.createRoundedDrawable(88, 42, 10,
                    isCurse ? Color.valueOf("#7f1d1d") : Color.valueOf("#388e3c"));
                useStyle.font = playerTagFont;
                useStyle.fontColor = Color.WHITE;

                TextButton useBtn = new TextButton("USE", useStyle);
                UIUtils.addScaleHoverEffect(useBtn);

                final int slotIndex = i;
                useBtn.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        if (game.getAudioManager() != null) {
                            game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                        }
                        handleUseInventoryItem(slotIndex);
                    }
                });

                if (isCurse) {
                    useBtn.setText("CURSE");
                    useBtn.setDisabled(true);
                    useBtn.setTouchable(Touchable.disabled);
                    useBtn.setColor(1f, 0.8f, 0.8f, 0.75f);
                } else if (lethalSelfCost) {
                    useBtn.setText("LOW HP");
                    useBtn.setDisabled(true);
                    useBtn.setTouchable(Touchable.disabled);
                    useBtn.setColor(1f, 1f, 1f, 0.45f);
                } else if (turnState != TurnState.WAITING_FOR_ROLL || itemUsedThisTurn) {
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
                // Empty Slot with clear container styling and label
                slotRow.setBackground(UIUtils.createRoundedDrawable(580, 80, 12, Color.valueOf("#2a1204cc"), Color.valueOf("#54280e88"), 1));

                Image emptyIcon = new Image(game.getAssetManager().get(Assets.ICON_SATCHEL));
                emptyIcon.setScaling(Scaling.fit);
                emptyIcon.setColor(1f, 1f, 1f, 0.60f);
                iconBox.add(emptyIcon).size(38, 38).center();
                slotRow.add(iconBox).size(48, 48).padLeft(2).padRight(12).center();

                Table emptyTextTable = new Table();
                emptyTextTable.left();
                Label emptyLbl = new Label("[Slot " + (i + 1) + "] Empty Slot", new Label.LabelStyle(playerTagFont, Color.valueOf("#e0b888")));
                Label emptySub = new Label("Vacant inventory slot", new Label.LabelStyle(miniFont, Color.valueOf("#a87850")));
                emptyTextTable.add(emptyLbl).left().row();
                emptyTextTable.add(emptySub).padTop(2).left();

                slotRow.add(emptyTextTable).width(350).left();
                slotRow.add().expandX();
                slotRow.add().size(88, 42).right(); // Placeholder alignment spacer
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

        if (item.isCurse()) {
            topBannerLabel.setText("CURSED BURDENS CANNOT BE DISCARDED! CLEANSE AT A SANCTUARY OR BARTER AWAY.");
            return;
        }

        String itemName = item.getName();
        EventEffectType type = item.getEffectType();
        EventEffectType secType = item.getSecondaryEffectType();
        int secVal = item.getSecondaryEffectValue();
        String effectDesc = item.getEffect();
        int effectVal = item.getEffectValue();

        // Prevent consuming pure health items when already at max health
        if (type == EventEffectType.HEALTH && player.getHealth() >= player.getMaxHealth()) {
            topBannerLabel.setText(player.getName().toUpperCase() + " IS ALREADY AT FULL HEALTH (" + player.getMaxHealth() + " HP)!");
            return;
        }
        // Prevent fatal self-damage on double-edged blood items
        if (secType == EventEffectType.HEALTH && secVal < 0 && player.getHealth() + secVal <= 0) {
            topBannerLabel.setText("TOO WEAK TO ENDURE " + itemName.toUpperCase() + "! REQUIRES MORE THAN " + (-secVal) + " HP.");
            return;
        }

        int startPos = player.getPosition();

        // Use the item via player
        boolean success = player.useItem(slotIndex);
        if (!success) return;

        if (game.getRunTracker() != null) {
            game.getRunTracker().recordItemUsed(player);
        }

        itemUsedThisTurn = true;

        // Auto-close inventory popup so player returns to the action!
        if (inventoryPopup != null) {
            inventoryPopup.setVisible(false);
        }
        if (dicePickerPopup != null) {
            dicePickerPopup.setVisible(false);
        }

        // Banner feedback
        topBannerLabel.setText(player.getName().toUpperCase() + " USED " + itemName.toUpperCase() + "! (" + effectDesc + ")");

        if (game.getAudioManager() != null) {
            if (secType == EventEffectType.HEALTH && secVal < 0) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.PLAYER_HURT);
            } else if (type == EventEffectType.HEALTH) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.POTION_DRINK);
            } else if (type == EventEffectType.SCOUT || type == EventEffectType.SNAKE_WARD) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.SCOUT_FLARE);
            } else if (type == EventEffectType.SWAP_PLAYER) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.LADDER_SOAR);
            } else {
                game.getAudioManager().playSound(AudioManager.SoundEffect.ITEM_EQUIP);
            }
        }

        // Board Utility Execution Handlers
        if (type == EventEffectType.MOVE) {
            int targetPos = Math.min(100, Math.max(1, startPos + effectVal));
            player.setPosition(startPos);
            hopCurrentTile = startPos;
            hopTargetTile = targetPos;
            stepTimer = 0f;
            isItemMovement = true;
            turnState = TurnState.STEPPING_TILES;
            topBannerLabel.setText(player.getName().toUpperCase() + " USED " + itemName.toUpperCase() + "! LEAPING " + effectVal + " TILES...");
        } else if (type == EventEffectType.SCOUT) {
            int start = startPos + 1;
            int end = Math.min(100, startPos + effectVal);
            int newlyRevealed = board.revealRange(start, end);
            revealFlashTile = end;
            revealFlashTimer = 1.4f;
            topBannerLabel.setText(player.getName().toUpperCase() + " USED " + itemName.toUpperCase() + "! SCOUTED TILES " + start + " TO " + end + " (" + newlyRevealed + " NEWLY UNCOVERED)!");
        } else if (type == EventEffectType.DICE_EXACT) {
            diceLabel.setText("PICK 1-6");
            topBannerLabel.setText("GOLDEN DIE ACTIVE! TAP [ROLL] TO PICK YOUR EXACT NUMBER (1-6)!");
        } else if (type == EventEffectType.DICE_CLAMP) {
            diceLabel.setText("MAX " + effectVal);
            topBannerLabel.setText("LEAD BOOTS EQUIPPED! NEXT ROLL CAPPED AT " + effectVal + " MAX!");
        } else if (type == EventEffectType.SNAKE_WARD) {
            topBannerLabel.setText("SNAKE CHARMER'S FLUTE READY! NEXT SERPENT ENCOUNTER WILL BE PACIFIED!");
        } else if (type == EventEffectType.SWAP_PLAYER) {
            Player targetRival = null;
            int highestPos = startPos;
            for (Player other : game.getPlayers()) {
                if (other != player && other.getHealth() > 0 && other.getPosition() < 100 && other.getPosition() > highestPos) {
                    highestPos = other.getPosition();
                    targetRival = other;
                }
            }
            if (targetRival != null) {
                targetRival.setPosition(startPos);
                player.setPosition(highestPos);
                Tile landed = board.getTile(highestPos);
                if (landed != null && !landed.isRevealed()) {
                    landed.setRevealed(true);
                }
                revealFlashTile = highestPos;
                revealFlashTimer = 1.2f;
                topBannerLabel.setText("TRANSPOSITION! " + player.getName().toUpperCase() + " SWAPPED POSITIONS WITH "
                    + targetRival.getName().toUpperCase() + " TO TILE " + highestPos + "!");
            } else {
                // Solo fallback or already leading: warp forward with stepping animation
                int targetPos = Math.min(100, Math.max(1, startPos + effectVal));
                hopCurrentTile = startPos;
                hopTargetTile = targetPos;
                stepTimer = 0f;
                isItemMovement = true;
                turnState = TurnState.STEPPING_TILES;
                topBannerLabel.setText(player.getName().toUpperCase() + " WARPED FORWARD " + effectVal + " TILES!");
            }
        }

        if (secType == EventEffectType.DICE_CLAMP) {
            diceLabel.setText("MAX " + secVal);
        }

        // Update active player stats & mini-map
        updateHudDisplay();

        // Refresh inventory UI
        refreshInventoryUI();
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
        Label pauseBadge = new Label("GAME PAUSED", new Label.LabelStyle(pauseBadgeFont, Color.WHITE));
        pauseBadge.setAlignment(Align.center);
        Table pauseBadgeTable = new Table();
        pauseBadgeTable.setBackground(UIUtils.createRoundedDrawable(340, 58, 18, pillBrown));
        pauseBadgeTable.add(pauseBadge).pad(6, 36, 6, 36);

        Table pauseContent = new Table();
        Label pauseSub = new Label("The journey awaits your command.", new Label.LabelStyle(pauseSubFont, darkBrown));
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

        TextButton leaveBtn = new TextButton("LEAVE GAME", dangerBtnStyle);
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
        Label leaveBadge = new Label("LEAVE GAME?", new Label.LabelStyle(pauseBadgeFont, Color.WHITE));
        leaveBadge.setAlignment(Align.center);
        Table leaveBadgeTable = new Table();
        leaveBadgeTable.setBackground(UIUtils.createRoundedDrawable(300, 58, 18, Color.valueOf("#8a2424")));
        leaveBadgeTable.add(leaveBadge).pad(6, 36, 6, 36);

        Table leaveContent = new Table();

        Label leaveWarningTitle = new Label("Abandon Current Adventure?", new Label.LabelStyle(pauseTitleFont, darkBrown));
        leaveWarningTitle.setAlignment(Align.center);
        leaveContent.add(leaveWarningTitle).padTop(10).padBottom(16).center().row();

        Label leaveWarningBody = new Label("Are you sure you want to return to the main menu?\nAny unsaved progress on this board will be lost.", new Label.LabelStyle(pauseBodyFont, darkBrown));
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
        isPaused = true;
        if (inventoryPopup != null) inventoryPopup.setVisible(false);
        if (dicePickerPopup != null) dicePickerPopup.setVisible(false);
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
        eventChoiceBtnStyle.up = UIUtils.createRoundedDrawable(530, 48, 12, Color.valueOf("#7a3d16"), Color.valueOf("#54280e"), 2);
        eventChoiceBtnStyle.over = UIUtils.createRoundedDrawable(530, 48, 12, Color.valueOf("#9c572b"), Color.valueOf("#54280e"), 2);
        eventChoiceBtnStyle.font = choiceActionBtnFont;
        eventChoiceBtnStyle.fontColor = Color.valueOf("#fff8ea");

        eventChoiceCardStyle = new Button.ButtonStyle();
        eventChoiceCardStyle.up = UIUtils.createRoundedDrawable(530, 68, 12, Color.valueOf("#7a3d16"), Color.valueOf("#54280e"), 2);
        eventChoiceCardStyle.over = UIUtils.createRoundedDrawable(530, 68, 12, Color.valueOf("#9c572b"), Color.valueOf("#54280e"), 2);

        eventChoiceCardDisabledStyle = new Button.ButtonStyle();
        eventChoiceCardDisabledStyle.up = UIUtils.createRoundedDrawable(530, 68, 12, Color.valueOf("#554338bb"), Color.valueOf("#443329cc"), 2);

        cancelEventChoiceBtnStyle = new TextButton.TextButtonStyle();
        cancelEventChoiceBtnStyle.up = UIUtils.createRoundedDrawable(280, 48, 12, Color.valueOf("#8a2e24"), Color.valueOf("#541b15"), 2);
        cancelEventChoiceBtnStyle.over = UIUtils.createRoundedDrawable(280, 48, 12, Color.valueOf("#a63d32"), Color.valueOf("#541b15"), 2);
        cancelEventChoiceBtnStyle.font = choiceActionBtnFont;
        cancelEventChoiceBtnStyle.fontColor = Color.valueOf("#fff8ea");

        disabledEventChoiceBtnStyle = new TextButton.TextButtonStyle();
        disabledEventChoiceBtnStyle.up = UIUtils.createRoundedDrawable(530, 48, 12, Color.valueOf("#554338aa"), Color.valueOf("#443329aa"), 2);
        disabledEventChoiceBtnStyle.font = choiceActionBtnFont;
        disabledEventChoiceBtnStyle.fontColor = Color.valueOf("#a89688");

        combatStrikeBtnStyle = new TextButton.TextButtonStyle();
        combatStrikeBtnStyle.up = UIUtils.createRoundedDrawable(380, 52, 16, Color.valueOf("#8a2424"), Color.valueOf("#5a1414"), 2);
        combatStrikeBtnStyle.over = UIUtils.createRoundedDrawable(380, 52, 16, Color.valueOf("#a63232"), Color.valueOf("#5a1414"), 2);
        combatStrikeBtnStyle.font = choiceActionBtnFont;
        combatStrikeBtnStyle.fontColor = Color.WHITE;

        // Top Event Badge (Seated cleanly on the top edge of the scroll frame)
        eventBadgeLabel = new Label("EVENT", new Label.LabelStyle(eventBadgeFont, Color.WHITE));
        eventBadgeLabel.setAlignment(Align.center);
        eventBadgeLabel.setWrap(true);
        eventBadgeTable = new Table();
        eventBadgeTable.setBackground(UIUtils.createRoundedDrawable(480, 54, 16, pillBrown, Color.valueOf("#2b1304"), 2));
        eventBadgeTable.add(eventBadgeLabel).width(440).pad(6, 16, 6, 16);

        // Content Table inside the parchment scroll
        eventContentTable = new Table();
        eventContentTable.center();
        eventContentTable.padTop(16);

        Image eventScrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        eventScrollBg.setScaling(Scaling.stretch);

        Stack eventScrollStack = new Stack();
        eventScrollStack.add(eventScrollBg);
        eventScrollStack.add(eventContentTable);

        eventModalTable = new Table();
        eventModalTable.setFillParent(true);
        eventModalTable.setVisible(false);
        eventModalTable.add(eventBadgeTable).size(480, 54).padBottom(-48).center().row();
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
        if (dicePickerPopup != null) {
            dicePickerPopup.setVisible(false);
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
            case CROSSROADS:
                badgeBgColor = Color.valueOf("#5b3275");
                badgePrefix = "MOUNTAIN CROSSROADS";
                break;
            case TREASURE:
                badgeBgColor = Color.valueOf("#a67c00");
                badgePrefix = "TREASURE CACHE";
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
        setEventBadge(badgePrefix + "  -  TILE " + tile.getPosition(), badgeBgColor);

        // 2. Event Title & Atmospheric Narrative Lore
        Label titleLbl = new Label(event.getTitle(), new Label.LabelStyle(eventTitleFont, Color.valueOf("#361704")));
        titleLbl.setAlignment(Align.center);
        titleLbl.setWrap(true);
        eventContentTable.add(titleLbl).width(580).padTop(4).padBottom(4).center().row();

        Label descLbl = new Label(event.getDescription(), new Label.LabelStyle(eventDescFont, Color.valueOf("#5a3616")));
        descLbl.setAlignment(Align.center);
        descLbl.setWrap(true);
        eventContentTable.add(descLbl).width(610).padBottom(12).center().row();

        // 3. Dynamic Choices (Hybrid Model: Structured 2-row cards with scannable risk/type tags)
        List<EventChoice> choices = event.getChoices();
        int visibleChoices = 0;
        for (int i = 0; i < choices.size(); i++) {
            final EventChoice choice = choices.get(i);
            boolean available = choice.isAvailable(player);
            if (!available) {
                if (choice.getRequirementType() == EventChoice.ChoiceRequirementType.CONSUME_ANY_ITEM
                        || choice.getRequirementType() == EventChoice.ChoiceRequirementType.HAS_CURSE
                        || event.getCategory() == EventCategory.MERCHANT) {
                    continue;
                }
            }
            visibleChoices++;
        }
        float cardGap = (visibleChoices >= 4) ? 7f : 10f;

        for (int i = 0; i < choices.size(); i++) {
            final int choiceIndex = i;
            final EventChoice choice = choices.get(i);
            boolean available = choice.isAvailable(player);

            // In the Hybrid model:
            // 1. Hide Barter / Cleanse choice if condition isn't met
            // 2. Hide unaffordable optional merchant purchases
            if (!available) {
                if (choice.getRequirementType() == EventChoice.ChoiceRequirementType.CONSUME_ANY_ITEM
                        || choice.getRequirementType() == EventChoice.ChoiceRequirementType.HAS_CURSE) {
                    continue;
                }
                if (event.getCategory() == EventCategory.MERCHANT) {
                    continue;
                }
            }

            Button choiceCard = buildChoiceCardButton(choice, available);

            if (available) {
                UIUtils.addScaleHoverEffect(choiceCard);
                choiceCard.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent evt, Actor actor) {
                        if (game.getAudioManager() != null) {
                            game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                        }
                        handleChoiceSelected(choice, choiceIndex, event, tile, player);
                    }
                });
            }

            eventContentTable.add(choiceCard).width(530).minHeight(54).padBottom(cardGap).center().row();
        }

        modalScrim.setVisible(true);
        eventModalTable.setVisible(true);
        turnState = TurnState.RESOLVING_TILE_EVENT;
    }

    private void setEventBadge(String text, Color bgColor) {
        eventBadgeTable.setBackground(UIUtils.createRoundedDrawable(480, 54, 16, bgColor, Color.valueOf("#2b1304"), 2));
        eventBadgeLabel.setText(text);
    }

    private String extractActionTitle(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) return "Event Choice";
        String trimmed = rawText.trim();
        if (trimmed.startsWith("[")) {
            int close = trimmed.indexOf(']');
            if (close > 1) {
                return trimmed.substring(1, close).trim();
            }
        }
        int colon = trimmed.indexOf(':');
        if (colon > 0 && colon < 42) {
            return trimmed.substring(0, colon).trim();
        }
        return trimmed;
    }

    private String extractActionSummary(String rawText) {
        if (rawText == null) return "";
        String trimmed = rawText.trim();
        String rest = "";
        if (trimmed.startsWith("[")) {
            int close = trimmed.indexOf(']');
            if (close >= 0 && close + 1 < trimmed.length()) {
                rest = trimmed.substring(close + 1).trim();
            }
        } else {
            int colon = trimmed.indexOf(':');
            if (colon > 0 && colon < 42 && colon + 1 < trimmed.length()) {
                rest = trimmed.substring(colon + 1).trim();
            }
        }
        if (rest.isEmpty()) return "";

        // Strip redundant "Gamble (D20):" prefix since the right badge displays D20 DC
        if (rest.regionMatches(true, 0, "Gamble (D20):", 0, "Gamble (D20):".length())) {
            rest = rest.substring("Gamble (D20):".length()).trim();
        }
        if (!rest.isEmpty()) {
            rest = Character.toUpperCase(rest.charAt(0)) + rest.substring(1);
        }
        return rest;
    }

    private Button buildChoiceCardButton(EventChoice choice, boolean available) {
        String actionTitle = extractActionTitle(choice.getText());
        String summary = extractActionSummary(choice.getText());

        String tagText;
        Color tagColor;
        Color tagBorderColor;
        Color tagTextColor = Color.valueOf("#fff8ea");
        if (!available) {
            tagText = "LOCKED";
            tagColor = Color.valueOf("#5c493e");
            tagBorderColor = Color.valueOf("#8c7769");
            tagTextColor = Color.valueOf("#cfc0b4");
            String reqText;
            switch (choice.getRequirementType()) {
                case EQUIPMENT_DURABILITY:
                    reqText = "Requires a 1-Use Shield/Relic in your satchel";
                    break;
                case MIN_HEALTH:
                    reqText = "Requires more than " + choice.getRequirementValue() + " HP";
                    break;
                case MIN_EXPERIENCE:
                    reqText = "Requires at least " + choice.getRequirementValue() + " EXP";
                    break;
                default:
                    reqText = "Requirement not met";
                    break;
            }
            summary = reqText;
        } else if (choice.getDifficultyClass() > 0) {
            tagText = "D20 (DC " + choice.getDifficultyClass() + ")";
            tagColor = Color.valueOf("#8f5e16");
            tagBorderColor = Color.valueOf("#d6a549");
        } else if (choice.isTriggersCombat()) {
            tagText = "COMBAT";
            tagColor = Color.valueOf("#8c2a24");
            tagBorderColor = Color.valueOf("#d46e66");
        } else if (choice.getRequirementType() == EventChoice.ChoiceRequirementType.CONSUME_ANY_ITEM) {
            tagText = "BARTER";
            tagColor = Color.valueOf("#855918");
            tagBorderColor = Color.valueOf("#cfa259");
        } else if (choice.getRequirementType() == EventChoice.ChoiceRequirementType.HAS_CURSE) {
            tagText = "PURIFY";
            tagColor = Color.valueOf("#276654");
            tagBorderColor = Color.valueOf("#6eb5a2");
        } else if (choice.getRequirementType() == EventChoice.ChoiceRequirementType.EQUIPMENT_DURABILITY) {
            tagText = "SHIELD";
            tagColor = Color.valueOf("#2f5778");
            tagBorderColor = Color.valueOf("#759ec2");
        } else if (choice.getRequirementType() == EventChoice.ChoiceRequirementType.MIN_HEALTH
                || choice.getRequirementType() == EventChoice.ChoiceRequirementType.MIN_EXPERIENCE
                || summary.contains("Sacrifice") || summary.contains("damage") || summary.contains("-")) {
            tagText = "TRADE-OFF";
            tagColor = Color.valueOf("#914621");
            tagBorderColor = Color.valueOf("#d68860");
        } else {
            tagText = "SAFE";
            tagColor = Color.valueOf("#2f6338");
            tagBorderColor = Color.valueOf("#7db072");
        }

        Button cardBtn = new Button(available ? eventChoiceCardStyle : eventChoiceCardDisabledStyle);
        cardBtn.pad(8, 16, 8, 16);

        Label titleLbl = new Label(actionTitle, new Label.LabelStyle(choiceCardTitleFont,
            available ? Color.valueOf("#fff8ea") : Color.valueOf("#b8a89a")));
        titleLbl.setAlignment(Align.left);
        titleLbl.setWrap(true);

        Table tagPill = new Table();
        tagPill.setBackground(UIUtils.createRoundedDrawable(110, 24, 6, tagColor, tagBorderColor, 2));
        Label tagLbl = new Label(tagText, new Label.LabelStyle(choiceTagFont, Color.WHITE));
        tagLbl.setColor(tagTextColor);
        tagLbl.setAlignment(Align.center);
        tagPill.add(tagLbl).pad(2, 9, 2, 9);

        if (summary.isEmpty()) {
            cardBtn.add(titleLbl).width(368).expandX().left().padRight(10);
            cardBtn.add(tagPill).right();
        } else {
            cardBtn.add(titleLbl).width(368).expandX().left().padRight(10);
            cardBtn.add(tagPill).right().row();

            Label summaryLbl = new Label(summary, new Label.LabelStyle(choiceBtnFont,
                available ? Color.valueOf("#f2dcc2") : Color.valueOf("#9e8e80")));
            summaryLbl.setAlignment(Align.left);
            summaryLbl.setWrap(true);
            cardBtn.add(summaryLbl).colspan(2).width(494).left().padTop(3);
        }

        return cardBtn;
    }

    /**
     * Dispatches the player's selected choice to combat, barter, or direct resolution.
     * Tracks any event-driven MOVE offsets so special locomotion animates smoothly after the modal closes.
     */
    private void handleChoiceSelected(EventChoice choice, int choiceIndex, Event event, Tile tile, Player player) {
        if (event.getCategory() == EventCategory.SNAKE && (choice.isTriggersCombat() || choiceIndex == 0)) {
            // Stand & Slay: Launch BattleScreen!
            modalScrim.setVisible(false);
            eventModalTable.setVisible(false);
            isEventModalActive = false;
            game.setScreen(new BattleScreen(game, player, (SnakeTile) tile));
            return;
        }

        if (choice.getRequirementType() == EventChoice.ChoiceRequirementType.CONSUME_ANY_ITEM) {
            // Barter choice: Open satchel item selection picker!
            showBarterItemSelector(player, choice, tile);
            return;
        }

        int posBefore = player.getPosition();
        EventChoice.EventResult res = choice.executeWithResult(player);
        int moveDelta = player.getPosition() - posBefore;
        if (moveDelta != 0) {
            // Defer visual displacement until modal closes so special locomotion plays smoothly
            player.setPosition(posBefore);
        }

        String cleanTitle = extractActionTitle(choice.getText());
        if (event.getCategory() == EventCategory.SNAKE) {
            if (choice.isSkipsSpecialMove() || choice.getRequirementType() == EventChoice.ChoiceRequirementType.EQUIPMENT_DURABILITY) {
                // Tactical counter or shield deflection: avoid sliding down (and apply any vault forward)
                specialSlideTarget = (moveDelta != 0) ? Math.max(1, Math.min(100, posBefore + moveDelta)) : -1;
                showOutcomeView(cleanTitle, res, false);
                return;
            } else {
                // Tail Slide: Acrobatics D20 Tumble check!
                int dest = ((SnakeTile) tile).getDestination();
                int shepherdPos = game.getPaleShepherdTile();
                boolean entersVoid = (shepherdPos > 0 && dest <= shepherdPos);

                int tumbleRoll = com.badlogic.gdx.math.MathUtils.random(1, 20);
                int tumbleDmg;
                String rollDesc;

                if (entersVoid) {
                    // Intercepted by The Pale Shepherd mid-slide!
                    // Halved tumble damage: 20 -> 0, 10-19 -> 5, 1-9 -> 10
                    if (tumbleRoll == 20) {
                        tumbleDmg = 0;
                        rollDesc = "Fall Damage: None (Roll: 20)";
                    } else if (tumbleRoll >= 10) {
                        tumbleDmg = 5;
                        rollDesc = "Fall Damage: -5 HP (Roll: " + tumbleRoll + ", Halved by Interception)";
                    } else {
                        tumbleDmg = 10;
                        rollDesc = "Fall Damage: -10 HP (Roll: " + tumbleRoll + ", Halved by Interception)";
                    }
                    if (tumbleDmg > 0) {
                        player.setHealth(Math.max(1, player.getHealth() - tumbleDmg));
                    }
                    res.consequences.clear();
                    res.consequences.add(rollDesc);
                    res.consequences.add("Intercepted by The Pale Shepherd at Tile " + shepherdPos + "!");
                    isSlideInterceptedByReaper = true;
                    specialSlideTarget = shepherdPos;
                    showOutcomeView(cleanTitle, res, true);
                    return;
                } else {
                    // Safe slide ahead of the reaper
                    if (tumbleRoll == 20) {
                        tumbleDmg = 0;
                        rollDesc = "Fall Damage: None (Roll: 20)";
                    } else if (tumbleRoll >= 10) {
                        tumbleDmg = 10;
                        rollDesc = "Fall Damage: -10 HP (Roll: " + tumbleRoll + ")";
                    } else {
                        tumbleDmg = 20;
                        rollDesc = "Fall Damage: -20 HP (Roll: " + tumbleRoll + ")";
                    }
                    if (tumbleDmg > 0) {
                        player.setHealth(Math.max(1, player.getHealth() - tumbleDmg));
                    }
                    res.consequences.clear();
                    res.consequences.add(rollDesc);
                    res.consequences.add("Slid down to Tile " + dest + ".");
                    isSlideInterceptedByReaper = false;
                    specialSlideTarget = dest;
                    showOutcomeView(cleanTitle, res, true);
                    return;
                }
            }
        }

        if (event.getCategory() == EventCategory.LADDER) {
            if (choice.isSkipsSpecialMove() || (choice.getDifficultyClass() > 0 && !res.checkSucceeded)) {
                specialSlideTarget = (moveDelta != 0) ? Math.max(1, Math.min(100, posBefore + moveDelta)) : -1;
            } else {
                specialSlideTarget = Math.min(100, ((LadderTile) tile).getDestination() + Math.max(0, moveDelta));
            }
        } else if (moveDelta != 0) {
            specialSlideTarget = Math.max(1, Math.min(100, posBefore + moveDelta));
        } else {
            specialSlideTarget = -1;
        }

        showOutcomeView(cleanTitle, res, false);
    }

    /**
     * Option A: Interactive Turn-by-Turn Combat Clash against the Serpent.
     * Player taps [STRIKE] each round to roll combat dice, watching HP bars and log update dynamically.
     * Stays strictly within the enlarged scroll and features light, high-readability container styling.
     */
    private void showCombatView(final Player player, final SnakeTile tile) {
        eventContentTable.clear();

        setEventBadge("SERPENT BATTLE  -  TILE " + tile.getPosition(), Color.valueOf("#8a2424"));

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

        Label vsLabel = new Label("VS", new Label.LabelStyle(eventTitleFont, Color.valueOf("#8a2424")));

        // Serpent Info Box
        Table snakeBox = new Table();
        String snakeNameStr = snake.getEnemyType().toUpperCase();
        Label sName = new Label(snakeNameStr, new Label.LabelStyle(hudNameFont, Color.valueOf("#361704")));
        final Label sHp = new Label("HP: " + snake.getHealth() + " / " + snakeMaxHp, new Label.LabelStyle(hudHpFont, Color.valueOf("#a81717")));
        Label sAtk = new Label("ATK " + snakeAtk + "  *  DEF " + snake.getDefense(), new Label.LabelStyle(miniFont, Color.valueOf("#54280e")));
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

        final Label combatLog = new Label("The coiled serpent lunges from the foliage!\nTap [STRIKE] to roll your combat dice!", new Label.LabelStyle(eventDescFont, Color.valueOf("#361704")));
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
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
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
                    if (game.getRunTracker() != null) {
                        game.getRunTracker().recordSnakeCombat(player, pDamage, true, pCrit);
                    }
                    int exp = 15 + (tile.getPosition() / 2);
                    player.gainExperience(exp);
                    Item loot = Item.createRandomLoot();
                    boolean added = player.addItem(loot);
                    if (added && game.getRunTracker() != null) {
                        game.getRunTracker().recordItemCollected(player);
                    }
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
                if (game.getRunTracker() != null) {
                    game.getRunTracker().recordDamageTaken(player, sDamage);
                }

                if (pHealth <= 0) {
                    combatOver[0] = true;
                    player.setHealth(0);
                    if (game.getRunTracker() != null) {
                        game.getRunTracker().recordPlayerFallen(player);
                    }
                    pHp.setText("HP: 0 / " + player.getMaxHealth());
                    combatLog.setText("You struck for " + pDamage + " damage! (Serpent HP: " + sHealth + ")\n"
                        + "[SERPENT D20: " + sRoll + "] The serpent delivered a lethal bite for " + sDamage + " damage!\n"
                        + "You have fallen in battle...");
                    actionBtn.setText("ACCEPT DEFEAT");
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

        setEventBadge("BARTER  -  SELECT AN ITEM", Color.valueOf("#9c6818"));

        Label titleLbl = new Label("Select Item to Trade", new Label.LabelStyle(eventTitleFont, Color.valueOf("#361704")));
        titleLbl.setAlignment(Align.center);
        eventContentTable.add(titleLbl).padTop(10).padBottom(4).center().row();

        Label subLbl = new Label("The merchant inspects your inventory. Choose which relic to trade away:", new Label.LabelStyle(eventDescFont, Color.valueOf("#4a2c11")));
        subLbl.setAlignment(Align.center);
        subLbl.setWrap(true);
        eventContentTable.add(subLbl).width(530).padBottom(12).center().row();

        int itemCount = (player.getInventory() != null) ? player.getInventory().size() : 0;
        for (int i = 0; i < itemCount; i++) {
            final int slotIndex = i;
            final Item it = player.getInventory().getItem(i);

            String typeLabel = it.isCurse() ? "[CURSE - TRADE AWAY!]" : (it.getItemType() == ItemType.CONSUMABLE ? "[Consumable]" : "[1-Use Relic]");
            String itDesc = it.getName() + "  -  " + typeLabel;
            TextButton itemBtn = new TextButton(itDesc, eventChoiceBtnStyle);
            itemBtn.getLabel().setWrap(true);
            itemBtn.getLabel().setAlignment(Align.center);
            itemBtn.pad(6, 18, 6, 18);
            itemBtn.getLabelCell().width(480);
            UIUtils.addScaleHoverEffect(itemBtn);
            itemBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent evt, Actor actor) {
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    }
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
            eventContentTable.add(itemBtn).width(530).minHeight(48).padBottom(6).center().row();
        }

        TextButton cancelBtn = new TextButton("CANCEL & RETURN", cancelEventChoiceBtnStyle);
        UIUtils.addScaleHoverEffect(cancelBtn);
        cancelBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent evt, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                showEventChoiceView(activeCurrentEvent, tile);
            }
        });
        eventContentTable.add(cancelBtn).size(280, 48).padTop(6).center().row();
    }

    /**
     * Interactive Satchel Full Item Swap Prompt when receiving loot with 4/4 capacity.
     */
    private void showItemSwapPrompt(final Player player, final Item newItem) {
        eventContentTable.clear();

        setEventBadge("INVENTORY FULL (4/4)  -  ITEM SWAP", Color.valueOf("#a63232"));

        Label titleLbl = new Label("Inventory Full!", new Label.LabelStyle(eventTitleFont, Color.valueOf("#361704")));
        titleLbl.setAlignment(Align.center);
        eventContentTable.add(titleLbl).padTop(10).padBottom(4).center().row();

        Label subLbl = new Label("You discovered [" + newItem.getName() + "], but your inventory is full.\nSelect an item to discard and replace, or leave it behind:",
            new Label.LabelStyle(eventDescFont, Color.valueOf("#4a2c11")));
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
            swapSlotBtn.pad(6, 18, 6, 18);
            swapSlotBtn.getLabelCell().width(480);
            UIUtils.addScaleHoverEffect(swapSlotBtn);
            swapSlotBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent evt, Actor actor) {
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    }
                    player.replaceItem(slot, newItem);
                    EventChoice.EventResult res = new EventChoice.EventResult();
                    res.consequences.add("Replaced [" + existing.getName() + "] with [" + newItem.getName() + "].");
                    showOutcomeView("Item Swapped", res, false);
                }
            });
            eventContentTable.add(swapSlotBtn).width(530).minHeight(48).padBottom(6).center().row();
        }

        TextButton leaveBtn = new TextButton("LEAVE " + newItem.getName().toUpperCase() + " BEHIND", cancelEventChoiceBtnStyle);
        UIUtils.addScaleHoverEffect(leaveBtn);
        leaveBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent evt, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                EventChoice.EventResult res = new EventChoice.EventResult();
                res.consequences.add("Left [" + newItem.getName() + "] behind.");
                showOutcomeView("Discovery Left Behind", res, false);
            }
        });
        eventContentTable.add(leaveBtn).width(440).minHeight(48).padTop(6).center().row();

        modalScrim.setVisible(true);
        eventModalTable.setVisible(true);
        turnState = TurnState.RESOLVING_TILE_EVENT;
    }

    /**
     * Outcome feedback screen revealing the clean action title, D20 check banner,
     * and semantically color-coded consequences inside an auto-sized parchment inset card.
     */
    private void showOutcomeView(String choiceTitle, EventChoice.EventResult res, boolean isRetreat) {
        eventContentTable.clear();

        setEventBadge("EVENT RESOLUTION", Color.valueOf("#7a4823"));

        Label titleLbl = new Label(choiceTitle, new Label.LabelStyle(choiceOutcomeTitleFont, Color.valueOf("#361704")));
        titleLbl.setAlignment(Align.center);
        titleLbl.setWrap(true);
        eventContentTable.add(titleLbl).width(550).padTop(6).padBottom(8).center().row();

        // D20 Skill Check Outcome Banner (if applicable)
        if (res.difficultyClass > 0) {
            Color d20Color = res.checkSucceeded ? Color.valueOf("#1d5c3a") : Color.valueOf("#8a2424");
            Color d20Border = res.checkSucceeded ? Color.valueOf("#113b24") : Color.valueOf("#5e1414");
            String d20Text = String.format("D20 CHECK: ROLLED %d (DC %d) - %s",
                res.d20Roll, res.difficultyClass, (res.checkSucceeded ? "SUCCESS!" : "FAILED!"));

            Table d20Badge = new Table();
            d20Badge.setBackground(UIUtils.createRoundedDrawable(490, 42, 10, d20Color, d20Border, 2));
            Label d20Lbl = new Label(d20Text, new Label.LabelStyle(choiceD20Font, Color.WHITE));
            d20Lbl.setAlignment(Align.center);
            d20Lbl.setWrap(true);
            d20Badge.add(d20Lbl).width(470).center();

            eventContentTable.add(d20Badge).size(490, 42).padBottom(12).center().row();
        }

        // Consequences List Inset Box (Light Cream Container with Dark Mahogany Border & Color-Coded Rows)
        Table outcomeBox = new Table();
        outcomeBox.setBackground(UIUtils.createRoundedDrawable(530, 80, 12, Color.valueOf("#faedd7"), Color.valueOf("#854b25"), 3));
        outcomeBox.pad(14, 22, 14, 22);

        Table linesTable = new Table();
        linesTable.center();

        for (int i = 0; i < res.consequences.size(); i++) {
            String msg = res.consequences.get(i);
            Color valueColor = resolveOutcomeLineColor(msg);
            int colonIdx = msg.indexOf(':');

            if (colonIdx > 0 && colonIdx + 1 < msg.length() && !msg.startsWith("Unavailable")) {
                String labelPart = msg.substring(0, colonIdx).trim();
                String valuePart = msg.substring(colonIdx + 1).trim();

                Table rowTbl = new Table();
                Label keyLbl = new Label(labelPart, new Label.LabelStyle(choiceOutcomeBodyFont, Color.valueOf("#5a3616")));
                keyLbl.setAlignment(Align.left);

                Label valLbl = new Label(valuePart, new Label.LabelStyle(choiceOutcomeValueFont, Color.WHITE));
                valLbl.setColor(valueColor);
                valLbl.setAlignment(Align.right);
                valLbl.setWrap(true);

                rowTbl.add(keyLbl).left().expandX().padRight(12);
                rowTbl.add(valLbl).width(305).right();
                linesTable.add(rowTbl).width(484).padTop(4).padBottom(4).row();
            } else {
                Label lineLbl = new Label(msg, new Label.LabelStyle(choiceOutcomeValueFont, Color.WHITE));
                lineLbl.setColor(valueColor);
                lineLbl.setAlignment(Align.center);
                lineLbl.setWrap(true);
                linesTable.add(lineLbl).width(484).padTop(4).padBottom(4).center().row();
            }

            if (i < res.consequences.size() - 1) {
                Table divider = new Table();
                divider.setBackground(UIUtils.createRoundedDrawable(484, 1, 0, Color.valueOf("#854b2538")));
                linesTable.add(divider).size(484, 1).padTop(3).padBottom(3).row();
            }
        }

        outcomeBox.add(linesTable).width(484).center();
        eventContentTable.add(outcomeBox).width(530).minHeight(86).padBottom(16).center().row();

        // Action Button: CONTINUE
        TextButton continueBtn = new TextButton("CONTINUE", eventChoiceBtnStyle);
        continueBtn.getLabel().setWrap(true);
        UIUtils.addScaleHoverEffect(continueBtn);
        continueBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent evt, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                closeEventModal();
            }
        });
        eventContentTable.add(continueBtn).size(280, 50).center().row();
    }

    private Color resolveOutcomeLineColor(String msg) {
        if (msg == null) return Color.valueOf("#361704");
        if (msg.contains(": -") || msg.contains(" -") || msg.startsWith("Cursed")
                || msg.startsWith("Hex:") || msg.contains("Satchel full")
                || msg.contains("lashed out") || msg.startsWith("Left [")
                || msg.startsWith("Unavailable")) {
            return Color.valueOf("#a81d1d");
        }
        if (msg.contains(": +") || msg.startsWith("Acquired Relic")
                || msg.startsWith("Purified:") || msg.startsWith("Protection:")
                || msg.startsWith("Blessing:") || msg.startsWith("Scouted:")
                || msg.startsWith("Deflected:") || msg.startsWith("Replaced [")
                || msg.contains("Damage: None")) {
            return Color.valueOf("#1d5c3a");
        }
        if (msg.startsWith("Traded away:") || msg.startsWith("Expended:")) {
            return Color.valueOf("#8a5410");
        }
        return Color.valueOf("#361704");
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
        if (player != null && game.getRunTracker() != null) {
            game.getRunTracker().recordEventResolved(player);
        }

        if (player != null && player.getHealth() <= 0) {
            if (game.getRunTracker() != null) {
                game.getRunTracker().recordPlayerFallen(player);
            }
            if (game.getPlayers().size() <= 1 || game.isPartyWiped()) {
                game.setScreen(new GameSummaryScreen(
                    game, false, player,
                    io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker.EndingType.FALLEN_ON_TRAIL));
                return;
            }
            topBannerLabel.setText(player.getName().toUpperCase() + " DIED! NEXT TRAVELER'S TURN...");
            turnState = TurnState.TURN_TRANSITION;
            actionPauseTimer = 1.0f;
            return;
        }

        boolean wasShepherdAmbush = isShepherdAmbushActive;
        isShepherdAmbushActive = false;

        if (specialSlideTarget != -1) {
            boolean isLadder = (specialSlideTarget > player.getPosition());
            if (wasShepherdAmbush) {
                // After surging forward away from the Shepherd, player continues into roll phase
                isItemMovement = true;
            }
            startSpecialMove(player.getPosition(), specialSlideTarget, isLadder);
            specialSlideTarget = -1;
        } else if (wasShepherdAmbush) {
            turnState = TurnState.WAITING_FOR_ROLL;
            topBannerLabel.setText(player.getName().toUpperCase() + "'S TURN - ROLL THE DICE!");
        } else {
            if (player != null && player.getPosition() >= 100) {
                triggerDawnWardenSummitTrial(player);
                return;
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

    private void updateActMusic() {
        if (game.getAudioManager() == null) return;
        Player player = game.getCurrentPlayer();
        int pos = (player != null) ? player.getPosition() : 1;
        AudioManager.MusicTrack track;
        if (pos <= 30) {
            track = AudioManager.MusicTrack.ACT1;
        } else if (pos <= 70) {
            track = AudioManager.MusicTrack.ACT2;
        } else {
            track = AudioManager.MusicTrack.ACT3;
        }
        boolean immediate = (game.getAudioManager().getCurrentTrack() != AudioManager.MusicTrack.ACT1 &&
                             game.getAudioManager().getCurrentTrack() != AudioManager.MusicTrack.ACT2 &&
                             game.getAudioManager().getCurrentTrack() != AudioManager.MusicTrack.ACT3);
        game.getAudioManager().playMusic(track, true, immediate);
    }

    /**
     * Starts the dice tumbling sequence, or opens the Golden Die 1-6 picker if active.
     */
    private void triggerRoll() {
        Player player = game.getCurrentPlayer();
        if (player != null && player.hasPendingDiceChoice()) {
            if (dicePickerPopup != null) {
                dicePickerPopup.setVisible(!dicePickerPopup.isVisible());
                topBannerLabel.setText("GOLDEN DIE ACTIVE! CHOOSE YOUR EXACT ROLL (1 - 6):");
            }
            return;
        }
        if (game.getAudioManager() != null) {
            game.getAudioManager().playSound(AudioManager.SoundEffect.DICE_ROLL);
        }
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

    /**
     * Finalizes a dice roll (random or Golden Die exact pick) and begins tile-by-tile movement.
     */
    private void resolveDiceRollResult(int finalRoll, String modifierNote) {
        Player player = game.getCurrentPlayer();
        if (player == null) return;

        currentRollValue = MathUtils.clamp(finalRoll, 1, 6);
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
        rollButton.setColor(1f, 1f, 1f, 0.65f);

        if (game.getRunTracker() != null) {
            game.getRunTracker().recordRoll(player, currentRollValue);
            game.getRunTracker().recordTurn(player);
        }

        hopCurrentTile = player.getPosition();
        hopTargetTile = Math.min(100, hopCurrentTile + currentRollValue);

        topBannerLabel.setText(player.getName().toUpperCase() + " ROLLED A " + currentRollValue + modifierNote + "! ADVANCING...");
        stepTimer = 0f;
        turnState = TurnState.STEPPING_TILES;
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
            if (turnState == TurnState.STEPPING_TILES || (isSpecialMoving && specialMovePhase == SpecialMovePhase.IN_FLIGHT)) {
                walkStateTime += delta;
            } else {
                walkStateTime = 0f;
            }
            if (revealFlashTimer > 0f) {
                revealFlashTimer -= delta;
                if (revealFlashTimer <= 0f) revealFlashTile = -1;
            }
            updateTurnLogic(delta);
            updateCameraTracking(delta);
        }

        // 2. Clear Screen with smoothly interpolated Act Atmospheric Sky Color
        float camTilePos = 1f + (worldCamera.position.x - FIRST_TILE_X) / TILE_SPACING;
        Color act1Sky = Color.valueOf("#42828a"); // Act 1 Atmospheric forest teal daytime sky
        Color act2Sky = Color.valueOf("#183864"); // Act 2 Vibrant deep sapphire twilight sky
        Color act3Sky = Color.valueOf("#4a1424"); // Act 3 Dramatic crimson twilight sky

        Color skyColor;
        if (camTilePos <= 27f) {
            skyColor = act1Sky;
        } else if (camTilePos < 33f) {
            float t = Interpolation.smooth.apply((camTilePos - 27f) / 6f);
            skyColor = new Color(act1Sky).lerp(act2Sky, t);
        } else if (camTilePos <= 67f) {
            skyColor = act2Sky;
        } else if (camTilePos < 73f) {
            float t = Interpolation.smooth.apply((camTilePos - 67f) / 6f);
            skyColor = new Color(act2Sky).lerp(act3Sky, t);
        } else {
            skyColor = act3Sky;
        }
        Gdx.gl.glClearColor(skyColor.r, skyColor.g, skyColor.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 3. Render World (Parallax + Ground + Stepping Stones + Characters)
        worldViewport.apply();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();

        renderParallaxBackground(camTilePos);
        renderGroundedSteppingStones();
        renderPaleShepherd(delta);
        renderPlayers();

        batch.end();

        // 4. Render Fixed UI Stage Overlay & Modals
        hudStage.getViewport().apply();
        hudStage.act(delta);
        hudStage.draw();

        // 5. Render Eerie Reaper Appearance "Bulb Going Out" Screen Burnout Effect
        renderBulbBurnoutEffect(delta);
    }

    /**
     * Updates movement state machine, dice tumbling, and tile triggers.
     */
    private void updateTurnLogic(float delta) {
        Player player = game.getCurrentPlayer();
        if (player == null) return;

        if (reaperAwakenTimer > 0f) {
            reaperAwakenTimer = Math.max(0f, reaperAwakenTimer - delta);
        }

        if (cameraFocusOverrideTimer > 0f) {
            cameraFocusOverrideTimer -= delta;
            if (cameraFocusOverrideTimer <= 0f) {
                cameraFocusOverrideTimer = 0f;
                cameraFocusTile = -1;
                topBannerLabel.setText(player.getName().toUpperCase() + "'S TURN - ROLL THE DICE!");
            }
        }

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
                    int rawRoll = movementDice.roll();
                    StringBuilder modNote = new StringBuilder();

                    int cap = player.getMaxDiceRollCap();
                    if (cap > 0) {
                        if (rawRoll > cap) {
                            rawRoll = cap;
                        }
                        player.setMaxDiceRollCap(0);
                        modNote.append(" (CAPPED MAX ").append(cap).append(")");
                    }

                    int curseMod = player.getCurseDiceModifier();
                    if (curseMod != 0) {
                        rawRoll = Math.max(1, rawRoll + curseMod);
                        modNote.append(" (").append(curseMod).append(" CURSE)");
                    }

                    resolveDiceRollResult(rawRoll, modNote.toString());
                }
                break;

            case STEPPING_TILES:
                stepTimer += delta;
                if (stepTimer >= STEP_DURATION) {
                    stepTimer = 0f;
                    hopCurrentTile++;
                    player.setPosition(hopCurrentTile);
                    if (game.getAudioManager() != null) {
                        game.getAudioManager().playSound(AudioManager.SoundEffect.STEP, 0.65f, 0.08f);
                    }
                    if (game.getRunTracker() != null) {
                        game.getRunTracker().recordSteps(player, 1, hopCurrentTile);
                    }
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
                if (isSpecialMoving) {
                    float startX = getTileCenterX(specialMoveStartTile);
                    float destX = getTileCenterX(specialMoveDestTile);
                    float startY = getCharacterDrawY(specialMoveStartTile);
                    float destY = getCharacterDrawY(specialMoveDestTile);

                    switch (specialMovePhase) {
                        case WINDUP:
                            // 1. ANTICIPATION WIND-UP:
                            // Character stands on the tile while ladder surges with light and discovery registers
                            specialMoveCurrentX = startX;
                            specialMoveCurrentY = startY;
                            specialMovePhaseTimer -= delta;

                            if (specialMovePhaseTimer <= 0f) {
                                specialMovePhase = SpecialMovePhase.IN_FLIGHT;
                                specialMoveElapsed = 0f;
                                if (game.getAudioManager() != null) {
                                    if (isLadderMove) {
                                        game.getAudioManager().playSound(AudioManager.SoundEffect.LADDER_SOAR);
                                    } else {
                                        game.getAudioManager().playSound(AudioManager.SoundEffect.SNAKE_SLIDE);
                                    }
                                }
                            }
                            break;

                        case IN_FLIGHT:
                            // 2. SOARING FLIGHT ACROSS THE CELESTIAL ARC:
                            specialMoveElapsed += delta;
                            float progress = MathUtils.clamp(specialMoveElapsed / specialMoveDuration, 0f, 1f);

                            if (isLadderMove) {
                                float tX = Interpolation.smooth.apply(progress);
                                specialMoveCurrentX = MathUtils.lerp(startX, destX, tX);

                                float tY = Interpolation.linear.apply(progress);
                                float arc = (float) Math.sin(progress * Math.PI) * specialMoveArcHeight;
                                specialMoveCurrentY = MathUtils.lerp(startY, destY, tY) + arc;
                            } else {
                                float tX = Interpolation.pow2In.apply(progress);
                                specialMoveCurrentX = MathUtils.lerp(startX, destX, tX);

                                float tY = Interpolation.linear.apply(progress);
                                float dip = (float) Math.sin(progress * Math.PI) * specialMoveArcHeight;
                                specialMoveCurrentY = MathUtils.lerp(startY, destY, tY) - dip;
                            }

                            if (progress >= 1.0f) {
                                specialMoveCurrentX = destX;
                                specialMoveCurrentY = destY;
                                player.setPosition(specialMoveDestTile);
                                updateMiniMapProgress();

                                // Reveal destination tile upon landing through ladder or slide
                                Tile destTile = board.getTile(specialMoveDestTile);
                                boolean destWasUnrevealed = (destTile != null && !destTile.isRevealed());
                                if (destTile != null && !destTile.isRevealed()) {
                                    destTile.setRevealed(true);
                                }
                                if (destWasUnrevealed) {
                                    revealFlashTile = specialMoveDestTile;
                                    revealFlashTimer = 1.0f;
                                    int discoveryExp = (int) (10 + specialMoveDestTile * 0.05f);
                                    player.gainExperience(discoveryExp);
                                }
                                updateHudDisplay();

                                if (isLadderMove) {
                                    if (specialMoveDestTile >= 100) {
                                        topBannerLabel.setText("SUMMIT REACHED! THE DAWN WARDEN AWAKENS TO TEST YOUR SPIRIT!");
                                    } else if (specialMoveDestTile == 30) {
                                        int cleansed = player.cleanseCurses();
                                        String cleanseNote = cleansed > 0 ? " (" + cleansed + " CURSE(S) PURIFIED!)" : "";
                                        topBannerLabel.setText(player.getName().toUpperCase() + " REACHED SANCTUARY (ACT I FINALE) VIA CELESTIAL LADDER!" + cleanseNote);
                                    } else if (specialMoveDestTile == 70) {
                                        int cleansed = player.cleanseCurses();
                                        String cleanseNote = cleansed > 0 ? " (" + cleansed + " CURSE(S) PURIFIED!)" : "";
                                        topBannerLabel.setText(player.getName().toUpperCase() + " REACHED THE ABYSS GATE (ACT II FINALE) VIA CELESTIAL LADDER!" + cleanseNote);
                                    } else if (destWasUnrevealed) {
                                        topBannerLabel.setText(player.getName().toUpperCase() + " REACHED TILE " + specialMoveDestTile + "! ASCENT COMPLETE! (+" + (int) (10 + specialMoveDestTile * 0.05f) + " EXP)");
                                    } else {
                                        topBannerLabel.setText(player.getName().toUpperCase() + " REACHED TILE " + specialMoveDestTile + "! ASCENT COMPLETE!");
                                    }
                                } else {
                                    int tilesLost = Math.max(0, specialMoveStartTile - specialMoveDestTile);
                                    if (game.getRunTracker() != null) {
                                        game.getRunTracker().recordSnakeSlide(player, tilesLost);
                                    }
                                    topBannerLabel.setText(player.getName().toUpperCase() + " SLID DOWN TO TILE " + specialMoveDestTile + "!");
                                }

                                specialMovePhase = SpecialMovePhase.LANDED;
                                specialMovePhaseTimer = 0.65f; // Touchdown landing pause
                                if (game.getAudioManager() != null && isLadderMove) {
                                    game.getAudioManager().playSound(AudioManager.SoundEffect.LADDER_LAND);
                                }
                            }
                            break;

                        case LANDED:
                            // 3. TOUCHDOWN LANDING RECOVERY:
                            specialMoveCurrentX = destX;
                            specialMoveCurrentY = destY;
                            specialMovePhaseTimer -= delta;

                            if (specialMovePhaseTimer <= 0f) {
                                isSpecialMoving = false;
                                specialMovePhase = SpecialMovePhase.NONE;

                                int finalPos = player.getPosition();
                                if (finalPos >= 100) {
                                    triggerDawnWardenSummitTrial(player);
                                    return;
                                }

                                // Check if player was intercepted by the reaper or landed directly on the pursuer's tile
                                if (game.getPaleShepherdTile() > 0 && finalPos == game.getPaleShepherdTile()) {
                                    if (isSlideInterceptedByReaper) {
                                        isSlideInterceptedByReaper = false;
                                        topBannerLabel.setText("INTERCEPTED BY DEATH! MORTAL CLASH IN THE VOID!");
                                    }
                                    triggerPaleShepherdEncounter(player);
                                    return;
                                }

                                // Check if destination tile triggers a ladder, snake, or landmark (Fix Bug A)
                                Tile landedTile = board.getTile(finalPos);
                                if (landedTile instanceof LadderTile || landedTile instanceof SnakeTile
                                    || (landedTile != null && landedTile.getLandmark() != TileLandmark.NONE)) {
                                    handleTileArrival(player, finalPos);
                                    return;
                                }

                                if (isItemMovement) {
                                    isItemMovement = false;
                                    turnState = TurnState.WAITING_FOR_ROLL;
                                    topBannerLabel.setText(player.getName().toUpperCase() + " REACHED TILE " + finalPos + "! ROLL THE DICE!");
                                } else {
                                    turnState = TurnState.TURN_TRANSITION;
                                    actionPauseTimer = 0.5f;
                                }
                            }
                            break;

                        case NONE:
                        default:
                            break;
                    }
                } else {
                    actionPauseTimer -= delta;
                    if (actionPauseTimer <= 0f) {
                        if (player.getPosition() >= 100) {
                            triggerDawnWardenSummitTrial(player);
                            return;
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
                    if (player.getPosition() >= 100) {
                        triggerDawnWardenSummitTrial(player);
                        return;
                    }
                    player.clearTemporaryBuffs();
                    game.nextTurn();
                    Player nextPlayer = game.getCurrentPlayer();
                    if (nextPlayer != null) {
                        nextPlayer.decrementSkillCooldown();
                    }
                    itemUsedThisTurn = false;
                    updateHudDisplay();
                    refreshInventoryUI();
                    rollButton.setColor(Color.WHITE);
                    if (diceIcon != null) {
                        diceIcon.setRotation(0f);
                        diceIcon.setScale(1f);
                    }

                    // The Pale Shepherd Pursuer Round Progression
                    if (game.getCurrentTurn() == 0 || game.getPlayers().size() == 1) {
                        game.incrementTotalRounds();
                        handlePaleShepherdRoundProgression();
                        if (isEventModalActive) {
                            return;
                        }
                    }

                    nextPlayer = game.getCurrentPlayer();
                    if (nextPlayer != null && nextPlayer.hasPendingDiceChoice()) {
                        diceLabel.setText("PICK 1-6");
                    } else if (nextPlayer != null && nextPlayer.getMaxDiceRollCap() > 0) {
                        diceLabel.setText("MAX " + nextPlayer.getMaxDiceRollCap());
                    } else {
                        diceLabel.setText("D6");
                    }
                    turnState = TurnState.WAITING_FOR_ROLL;
                    updateActMusic();
                    topBannerLabel.setText(nextPlayer.getName().toUpperCase() + "'S TURN - ROLL THE DICE!");
                }
                break;

            case WAITING_FOR_ROLL:
            default:
                break;
        }
    }

    /**
     * Renders The Pale Shepherd pursuer hovering with eerie crimson mist behind the players.
     */
    private void renderPaleShepherd(float delta) {
        int shepherdTile = game.getPaleShepherdTile();
        if (shepherdTile <= 0) return;

        float targetCenterX = getTileCenterX(shepherdTile);
        float targetGroundY = getCharacterDrawY(shepherdTile);

        if (shepherdDisplayX < 0) {
            shepherdDisplayX = targetCenterX;
            shepherdDisplayY = targetGroundY;
        }

        float dist = Math.abs(targetCenterX - shepherdDisplayX);
        if (dist > 1.5f) {
            shepherdDisplayX += (targetCenterX - shepherdDisplayX) * Math.min(1f, delta * 4.5f);
            shepherdDisplayY += (targetGroundY - shepherdDisplayY) * Math.min(1f, delta * 4.5f);
            isShepherdGliding = true;
        } else {
            shepherdDisplayX = targetCenterX;
            shepherdDisplayY = targetGroundY;
            isShepherdGliding = false;
        }

        // When sharing a tile with a player, offset the pursuer slightly left and elevated
        // so both the reaper and player sprites remain distinctly visible without swallowing/occlusion
        boolean sharesTileWithPlayer = false;
        for (Player p : game.getPlayers()) {
            if (p.getHealth() > 0 && p.getPosition() == shepherdTile) {
                sharesTileWithPlayer = true;
                break;
            }
        }
        float tileCenterX = shepherdDisplayX + (sharesTileWithPlayer ? -32f : 0f);
        float groundY = shepherdDisplayY + (sharesTileWithPlayer ? 26f : 0f);
        float size = CHAR_DISPLAY_SIZE;

        // Smooth floating hover oscillation: gentle, steady sine wave when idle, steady height when gliding
        float hoverOffset = isShepherdGliding ? 4f : ((float) Math.sin(stateTime * 2.2f) * 8f);
        float drawY = groundY + 16f + hoverOffset;
        float drawX = tileCenterX - (size / 2f);

        // Ground shadow & crimson mist pool beneath the hovering reaper
        float shadowScale = MathUtils.clamp(1.0f - (hoverOffset / 32f), 0.70f, 1.25f);
        float pulse = 0.5f + 0.5f * MathUtils.sinDeg(stateTime * 180f);

        if (softShadowTex != null) {
            // Soft feathered elliptical ground shadow on the mountain stone
            batch.setColor(0.08f, 0.02f, 0.04f, 0.65f * shadowScale);
            batch.draw(softShadowTex, tileCenterX - 50f * shadowScale, groundY - 2f, 100f * shadowScale, 26f * shadowScale);

            // Ambient soft crimson spectral mist pool under the reaper
            batch.setColor(0.70f, 0.06f, 0.12f, (0.24f + 0.10f * pulse));
            batch.draw(softShadowTex, tileCenterX - 65f * shadowScale, groundY - 6f, 130f * shadowScale, 34f * shadowScale);
        }

        // Render animated reaper sprite: Glide if gliding between tiles, Idle hover if stationary!
        TextureRegion frame;
        if (isShepherdGliding && shepherdWalkAnim != null) {
            frame = shepherdWalkAnim.getKeyFrame(stateTime, true);
        } else if (shepherdIdleAnim != null) {
            frame = shepherdIdleAnim.getKeyFrame(stateTime, true);
        } else {
            frame = null;
        }

        if (frame != null) {
            batch.setColor(Color.WHITE);
            batch.draw(frame, drawX, drawY, size, size);
        }
        batch.setColor(Color.WHITE);
    }

    /**
     * Triggers the eerie cutscene where The Pale Shepherd awakens on Tile 1.
     * Features a sudden bulb burnout flicker / blackout effect and chilling eerie SFX.
     */
    public void triggerPaleShepherdAwakening() {
        game.setPaleShepherdTile(1);
        shepherdDisplayX = getTileCenterX(1);
        shepherdDisplayY = getCharacterDrawY(1);
        cameraFocusTile = 1;
        cameraFocusOverrideTimer = 3.2f;
        reaperAwakenTimer = 3.2f;
        topBannerLabel.setText("A CHILLING SHADOW RISES... THE PALE SHEPHERD AWAKENS ON TILE 1!");
        if (game.getAudioManager() != null) {
            game.getAudioManager().playSound(AudioManager.SoundEffect.REAPER_APPEAR);
        }
        updateMiniMapProgress();
    }

    /**
     * Renders a gentle, photosensitive-safe lightbulb waver and atmospheric dusk dimming
     * when The Pale Shepherd awakens. Strictly avoids high-contrast white flashes or violent strobes.
     */
    private void renderBulbBurnoutEffect(float delta) {
        if (reaperAwakenTimer <= 0f) return;

        float totalDuration = 3.2f;
        float elapsed = totalDuration - reaperAwakenTimer;
        float overlayAlpha = 0f;
        Color overlayColor = Color.valueOf("#060813"); // Deep cold dusk shadow (no white flashes)
        float vignetteAlpha = 0f;

        // Stage 1: Slight, gentle bulb waver (0.0s to 0.6s) - subtle dimming dip without harsh strobing
        if (elapsed < 0.60f) {
            if (elapsed < 0.18f) {
                float t = elapsed / 0.18f;
                overlayAlpha = MathUtils.lerp(0f, 0.28f, Interpolation.smooth.apply(t));
                vignetteAlpha = MathUtils.lerp(0f, 0.20f, t);
            } else if (elapsed < 0.32f) {
                float t = (elapsed - 0.18f) / 0.14f;
                overlayAlpha = MathUtils.lerp(0.28f, 0.12f, Interpolation.smooth.apply(t));
                vignetteAlpha = 0.18f;
            } else {
                float t = (elapsed - 0.32f) / 0.28f;
                overlayAlpha = MathUtils.lerp(0.12f, 0.38f, Interpolation.smooth.apply(t));
                vignetteAlpha = MathUtils.lerp(0.18f, 0.50f, t);
            }
        }
        // Stage 2: Atmospheric cold shadow hold (0.6s to 1.8s) - soft dusk mood, world stays visible
        else if (elapsed < 1.80f) {
            float t = (elapsed - 0.60f) / 1.20f;
            overlayAlpha = MathUtils.lerp(0.38f, 0.30f, Interpolation.smooth.apply(t));
            vignetteAlpha = MathUtils.lerp(0.50f, 0.40f, t);
        }
        // Stage 3: Smooth dissipation back to normal daylight (1.8s to 3.2s)
        else {
            float t = MathUtils.clamp((elapsed - 1.80f) / 1.40f, 0f, 1f);
            overlayAlpha = MathUtils.lerp(0.30f, 0f, Interpolation.smooth.apply(t));
            vignetteAlpha = MathUtils.lerp(0.40f, 0f, Interpolation.smooth.apply(t));
        }

        // Render full-screen overlay in HUD coordinate space (dynamically spans entire responsive viewport)
        hudStage.getViewport().apply();
        batch.setProjectionMatrix(hudStage.getCamera().combined);
        batch.begin();

        float hudW = Math.max(hudStage.getViewport().getWorldWidth(), hudStage.getCamera().viewportWidth) + 20f;
        float hudH = Math.max(hudStage.getViewport().getWorldHeight(), hudStage.getCamera().viewportHeight) + 20f;
        float hudX = hudStage.getCamera().position.x - (hudW / 2f);
        float hudY = hudStage.getCamera().position.y - (hudH / 2f);

        if (overlayAlpha > 0.005f) {
            batch.setColor(overlayColor.r, overlayColor.g, overlayColor.b, overlayAlpha);
            batch.draw(whitePixelTex, hudX, hudY, hudW, hudH);
        }

        if (vignetteAlpha > 0.005f && vignetteTex != null) {
            batch.setColor(0.02f, 0.01f, 0.05f, vignetteAlpha);
            batch.draw(vignetteTex, hudX, hudY, hudW, hudH);
        }

        batch.setColor(Color.WHITE);
        batch.end();
    }

    private int getTrailingPlayerPosition() {
        int minPos = 100;
        List<Player> players = game.getPlayers();
        if (players == null || players.isEmpty()) {
            Player cur = game.getCurrentPlayer();
            return (cur != null) ? cur.getPosition() : 1;
        }
        for (Player p : players) {
            if (p.getHealth() > 0 && p.getPosition() < minPos) {
                minPos = p.getPosition();
            }
        }
        return minPos;
    }

    /**
     * Advances The Pale Shepherd at the end of each round and checks for catch-up encounters.
     */
    private void handlePaleShepherdRoundProgression() {
        int rounds = game.getTotalRounds();
        int dormant = game.getShepherdDormantTurns();

        if (rounds < dormant) {
            return;
        } else if (rounds == dormant) {
            triggerPaleShepherdAwakening();
            return;
        }

        if (game.getShepherdPauseRounds() > 0) {
            game.decrementShepherdPauseRounds();
            topBannerLabel.setText("THE PALE SHEPHERD PAUSES IN CONTEMPLATION (" + game.getShepherdPauseRounds() + " ROUND(S) REMAINING)!");
            return;
        }

        int curTile = game.getPaleShepherdTile();
        int trailingPos = getTrailingPlayerPosition();
        int gap = trailingPos - curTile;

        // Dynamic, menacing pursuer pacing so the reaper matches the fast pace of the party:
        int speed;
        if (gap >= 18) {
            speed = (curTile >= 30) ? 5 : 4;
        } else if (gap >= 8) {
            speed = (curTile >= 30) ? 4 : 3;
        } else if (gap >= 4) {
            speed = 2;
        } else {
            speed = 1;
        }

        // Multiplayer compensation: party advances multiple turns per round
        if (game.getPlayers().size() > 1 && gap >= 10) {
            speed = Math.min(6, speed + 1);
        }

        // Pursuer must always trail behind and never overshoot players
        if (gap > 0) {
            speed = Math.min(speed, gap);
        } else {
            speed = 0;
        }

        if (speed > 0) {
            game.advancePaleShepherd(speed);
        }
        updateMiniMapProgress();

        // Check if any active player was intercepted on the shepherd's tile
        Player interceptedPlayer = null;
        for (Player p : game.getPlayers()) {
            if (p.getHealth() > 0 && p.getPosition() == game.getPaleShepherdTile()) {
                interceptedPlayer = p;
                break;
            }
        }

        if (interceptedPlayer != null) {
            triggerPaleShepherdEncounter(interceptedPlayer);
        } else if (speed > 0) {
            topBannerLabel.setText("THE PALE SHEPHERD ADVANCES TO TILE " + game.getPaleShepherdTile() + " (+" + speed + " TILES)!");
        }
    }

    /**
     * Triggers the mortal clash encounter when The Pale Shepherd catches a player.
     * Launches direct, high-stakes combat against Death itself!
     */
    private void triggerPaleShepherdEncounter(final Player player) {
        if (player == null || player.getHealth() <= 0) return;

        if (game.getAudioManager() != null) {
            game.getAudioManager().playSound(AudioManager.SoundEffect.REAPER_APPEAR, 0.85f);
        }

        if (inventoryPopup != null) inventoryPopup.setVisible(false);
        if (dicePickerPopup != null) dicePickerPopup.setVisible(false);
        if (modalScrim != null) modalScrim.setVisible(false);
        if (eventModalTable != null) eventModalTable.setVisible(false);
        isEventModalActive = false;
        isShepherdAmbushActive = false;

        // Build Pale Shepherd Boss Enemy with high-stakes pursuer stats
        Enemy shepherd = new Enemy("The Pale Shepherd");
        shepherd.setBehavior("Harvester of Souls");
        shepherd.setHealth(80);
        shepherd.setAttack(12);
        shepherd.setDefense(4);
        shepherd.setReward("Shattered Scythe Shard");

        topBannerLabel.setText("THE PALE SHEPHERD DESCENDS! MORTAL DUEL!");
        game.setScreen(new BattleScreen(game, player, shepherd));
    }

    /**
     * Triggers the climatic Summit Trial against The Dawn Warden atop Tile 100.
     */
    private void triggerDawnWardenSummitTrial(Player player) {
        Enemy dawnWarden = new Enemy("The Dawn Warden");
        dawnWarden.setHealth(120);
        dawnWarden.setAttack(14);
        dawnWarden.setDefense(7);
        dawnWarden.setBehavior("Celestial Guardian");
        dawnWarden.setReward("Dawn's Blessing (Rebirth)");

        topBannerLabel.setText("SUMMIT REACHED! THE DAWN WARDEN AWAKENS!");
        game.setScreen(new BattleScreen(game, player, dawnWarden));
    }

    /**
     * Handles event, snake, or ladder trigger when landing on a tile.
     */
    private void handleTileArrival(Player player, int tileIndex) {
        Tile tile = board.getTile(tileIndex);
        if (tile == null) return;

        boolean wasUnrevealed = !tile.isRevealed();
        if (wasUnrevealed) {
            tile.setRevealed(true);
            revealFlashTile = tileIndex;
            revealFlashTimer = 1.0f;
            if (game.getAudioManager() != null) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.FOG_REVEAL);
            }
        }

        if (tileIndex >= 100) {
            topBannerLabel.setText("SUMMIT REACHED! THE DAWN WARDEN AWAKENS TO TEST YOUR SPIRIT!");
            turnState = TurnState.TILE_ACTION_PAUSE;
            actionPauseTimer = 1.0f;
            return;
        }

        // 1. SNAKE TILE -> Check Snake Charmer's Flute ward first, otherwise trigger serpent ambush narrative modal
        if (tile instanceof SnakeTile) {
            if (player.hasSnakeWard()) {
                player.setHasSnakeWard(false);
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.SANCTUARY_HEAL);
                }
                EventChoice.EventResult wardRes = new EventChoice.EventResult();
                wardRes.shieldDeflected = true;
                wardRes.consequences.add("Snake Charmer's Flute played an enchanting melody!");
                wardRes.consequences.add("The serpent on Tile " + tileIndex + " coiled harmlessly aside.");
                wardRes.consequences.add("Avoided combat and held your ground safely!");
                specialSlideTarget = -1;
                isEventModalActive = true;
                modalScrim.setVisible(true);
                eventModalTable.setVisible(true);
                turnState = TurnState.RESOLVING_TILE_EVENT;
                topBannerLabel.setText("SNAKE WARD! " + player.getName().toUpperCase() + " PACIFIED THE SERPENT ON TILE " + tileIndex + "!");
                showOutcomeView("Serpent Pacified!", wardRes, false);
                return;
            }
            if (game.getRunTracker() != null) {
                game.getRunTracker().recordSnakeEncounter(player);
            }
            SnakeTile snakeTile = (SnakeTile) tile;
            Event event = eventManager.selectEventForTile(tileIndex, EventCategory.SNAKE);
            if (wasUnrevealed) {
                topBannerLabel.setText("AMBUSH! " + player.getName().toUpperCase() + " UNCOVERED A SERPENT PIT ON TILE " + tileIndex + "!");
            } else {
                topBannerLabel.setText("SERPENT AMBUSH ON TILE " + tileIndex + "!");
            }
            showEventChoiceView(event, snakeTile);
            return;
        }
        // 2. LADDER TILE -> Handle climbing
        else if (tile instanceof LadderTile) {
            LadderTile ladder = (LadderTile) tile;
            if (game.getRunTracker() != null) {
                game.getRunTracker().recordLadder(player, tileIndex, ladder.getDestination());
            }
            int discoveryBonus = wasUnrevealed ? 20 : 0;
            if (discoveryBonus > 0) {
                player.gainExperience(discoveryBonus);
            }
            // 40% chance for an interactive climbing dilemma; 60% for smooth direct climb
            if (eventManager.shouldTriggerLadderEvent()) {
                Event event = eventManager.selectEventForTile(tileIndex, EventCategory.LADDER);
                topBannerLabel.setText("LADDER ASCENT ON TILE " + tileIndex + "! A CLIMBING DILEMMA ARISES...");
                showEventChoiceView(event, ladder);
            } else {
                int dest = ladder.getDestination();
                int expEarned = 10 + (tileIndex / 3);
                player.gainExperience(expEarned);
                if (wasUnrevealed) {
                    topBannerLabel.setText("DISCOVERY! " + player.getName().toUpperCase() + " UNCOVERED A CELESTIAL LADDER ON TILE " + tileIndex + " -> TILE " + dest + "! (+" + (expEarned + discoveryBonus) + " EXP)");
                } else {
                    topBannerLabel.setText("STABLE ASCENT ON TILE " + tileIndex + "! CLIMBING UP TO TILE " + dest + "! (+" + expEarned + " EXP)");
                }
                updateHudDisplay();
                startSpecialMove(tileIndex, dest, true);
            }
        }
        // 3. NORMAL TILE -> Visible Landmarks (POIs), Ambient events, or safe ground
        else {
            TileLandmark landmark = tile.getLandmark();
            if (landmark != TileLandmark.NONE) {
                EventCategory poiCat = EventCategory.NORMAL;
                String poiBanner = "AN ENCOUNTER ON TILE " + tileIndex + "!";
                if (landmark == TileLandmark.SHRINE) {
                    poiCat = EventCategory.SHRINE;
                    poiBanner = "ANCIENT SHRINE DISCOVERED ON TILE " + tileIndex + "!";
                } else if (landmark == TileLandmark.MERCHANT) {
                    poiCat = EventCategory.MERCHANT;
                    poiBanner = "WANDERING MERCHANT ENCOUNTERED ON TILE " + tileIndex + "!";
                } else if (landmark == TileLandmark.TREASURE) {
                    poiCat = EventCategory.TREASURE;
                    poiBanner = "ANCIENT TREASURE CACHE UNCOVERED ON TILE " + tileIndex + "!";
                } else if (landmark == TileLandmark.CROSSROADS) {
                    poiCat = EventCategory.CROSSROADS;
                    poiBanner = "MOUNTAIN CROSSROADS REACHED ON TILE " + tileIndex + " - CHOOSE YOUR PATH!";
                }
                Event event = eventManager.selectEventForTile(tileIndex, poiCat);
                player.resetEventCooldown();
                topBannerLabel.setText(poiBanner);
                showEventChoiceView(event, tile);
            } else if (eventManager.shouldTriggerAmbientEvent(player)) {
                Event event = eventManager.selectEventForTile(tileIndex, EventCategory.NORMAL);
                player.resetEventCooldown();
                topBannerLabel.setText("AN ENCOUNTER ON TILE " + tileIndex + "!");
                showEventChoiceView(event, tile);
            } else {
                player.incrementTurnsSinceLastEvent();
                float expMult = (board.getActiveModifier() != null) ? board.getActiveModifier().getDiscoveryExpMultiplier() : 1.0f;
                int expEarned = Math.round((10 + tileIndex * 0.05f) * expMult);
                player.gainExperience(expEarned);
                if (isItemMovement) {
                    isItemMovement = false;
                    topBannerLabel.setText(player.getName().toUpperCase() + " ADVANCED TO TILE " + tileIndex + " - SAFE GROUND. ROLL THE DICE!");
                    turnState = TurnState.WAITING_FOR_ROLL;
                } else {
                    if (tileIndex == 30) {
                        if (game.getAudioManager() != null) {
                            game.getAudioManager().playSound(AudioManager.SoundEffect.SANCTUARY_HEAL);
                        }
                        int cleansed = player.cleanseCurses();
                        String cleanseNote = cleansed > 0 ? " (" + cleansed + " CURSE(S) PURIFIED!)" : "";
                        topBannerLabel.setText(player.getName().toUpperCase() + " REACHED SANCTUARY (ACT I FINALE)! REST BEFORE ENTERING THE SUNKEN RUINS." + cleanseNote);
                    } else if (tileIndex == 70) {
                        if (game.getAudioManager() != null) {
                            game.getAudioManager().playSound(AudioManager.SoundEffect.SANCTUARY_HEAL);
                        }
                        int cleansed = player.cleanseCurses();
                        String cleanseNote = cleansed > 0 ? " (" + cleansed + " CURSE(S) PURIFIED!)" : "";
                        topBannerLabel.setText(player.getName().toUpperCase() + " REACHED THE ABYSS GATE (ACT II FINALE)! GIRD RESOLVE FOR THE INFERNAL VOID." + cleanseNote);
                    } else if (wasUnrevealed) {
                        topBannerLabel.setText(player.getName().toUpperCase() + " UNCOVERED TILE " + tileIndex + " - SAFE GROUND. (+" + expEarned + " EXP)");
                    } else {
                        topBannerLabel.setText(player.getName().toUpperCase() + " REACHED TILE " + tileIndex + " - SAFE GROUND. (+" + expEarned + " EXP)");
                    }
                    turnState = TurnState.TILE_ACTION_PAUSE;
                    actionPauseTimer = 0.8f;
                }
                updateHudDisplay();
            }
        }
    }
    /**
     * Smoothly pans the world camera to follow the active player, tracking both X and stepped Y elevation.
     */
    private void updateCameraTracking(float delta) {
        Player current = game.getCurrentPlayer();
        if (current == null) return;

        float targetX;
        float targetY;
        float baselineY = worldCamera.viewportHeight / 2f;

        if (cameraFocusOverrideTimer > 0f && cameraFocusTile > 0) {
            targetX = getTileCenterX(cameraFocusTile);
            targetY = baselineY + (getTileBottomY(cameraFocusTile) - 220f);
        } else if (isSpecialMoving) {
            targetX = specialMoveCurrentX;
            if (specialMovePhase == SpecialMovePhase.WINDUP) {
                targetY = baselineY + (getTileBottomY(specialMoveStartTile) - 220f);
            } else if (specialMovePhase == SpecialMovePhase.LANDED) {
                targetY = baselineY + (getTileBottomY(specialMoveDestTile) - 220f);
            } else {
                float moveProgress = MathUtils.clamp(specialMoveElapsed / specialMoveDuration, 0f, 1f);
                float startElev = getTileBottomY(specialMoveStartTile) - 220f;
                float destElev = getTileBottomY(specialMoveDestTile) - 220f;
                float currentElev = MathUtils.lerp(startElev, destElev, moveProgress);
                targetY = baselineY + currentElev;
            }
        } else if (turnState == TurnState.STEPPING_TILES) {
            float fromX = getTileCenterX(hopCurrentTile);
            float toX = getTileCenterX(Math.min(100, hopCurrentTile + 1));
            float alpha = stepTimer / STEP_DURATION;
            targetX = MathUtils.lerp(fromX, toX, alpha);

            float fromElev = getTileBottomY(hopCurrentTile) - 220f;
            float toElev = getTileBottomY(Math.min(100, hopCurrentTile + 1)) - 220f;
            targetY = baselineY + MathUtils.lerp(fromElev, toElev, alpha);
        } else {
            targetX = getTileCenterX(current.getPosition());
            targetY = baselineY + (getTileBottomY(current.getPosition()) - 220f);
        }

        worldCamera.position.x += (targetX - worldCamera.position.x) * (delta * 4.5f);
        worldCamera.position.y += (targetY - worldCamera.position.y) * (delta * 3.5f);
        worldCamera.update();
    }

    /**
     * Renders seamlessly composited multi-layer parallax backgrounds with smooth crossfading between Acts.
     * Layering order (from back to front):
     * 1. Layer 3 (Far Canopy & Sky): Sits in the back, extends to screen top, 0.15x parallax.
     * 2. Layer 2 (Midground Silhouettes & Ruins): Sits behind the ground ledge, 0.35x parallax.
     * 3. Layer 1 (Foreground Ground Platform): Anchored at y=0, turf embeds stepping stones, 1.0x scroll.
     */
    private void renderParallaxBackground(float camTilePos) {
        float camX = worldCamera.position.x;
        float camY = worldCamera.position.y;
        float viewW = worldCamera.viewportWidth;
        float viewH = worldCamera.viewportHeight;
        float leftVisible = camX - viewW / 2f;
        float rightVisible = camX + viewW / 2f;
        float bottomVisible = camY - viewH / 2f;

        // 0. Subterranean Foundation (Layer 0) - Smoothly interpolated
        Color act1Lower = Color.valueOf("#20202a");
        Color act2Lower = Color.valueOf("#0a204f");
        Color act3Lower = Color.valueOf("#2e1534");

        Color lowerBgColor;
        if (camTilePos <= 27f) {
            lowerBgColor = act1Lower;
        } else if (camTilePos < 33f) {
            float t = Interpolation.smooth.apply((camTilePos - 27f) / 6f);
            lowerBgColor = new Color(act1Lower).lerp(act2Lower, t);
        } else if (camTilePos <= 67f) {
            lowerBgColor = act2Lower;
        } else if (camTilePos < 73f) {
            float t = Interpolation.smooth.apply((camTilePos - 67f) / 6f);
            lowerBgColor = new Color(act2Lower).lerp(act3Lower, t);
        } else {
            lowerBgColor = act3Lower;
        }
        float foundationBottom = bottomVisible - 100f;
        float foundationTop = GROUND_BASE_Y + 25f;
        float foundationH = Math.max(50f, foundationTop - foundationBottom);
        batch.setColor(lowerBgColor);
        batch.draw(whitePixelTex, leftVisible - 200f, foundationBottom, viewW + 400f, foundationH);
        batch.setColor(Color.WHITE);

        // 1, 2, 3. Parallax Layers Crossfade
        if (camTilePos <= 27f) {
            drawParallaxSliceSet(act1Far, act1Mid, act1Ground, 1.0f, camX, leftVisible, rightVisible, viewH);
        } else if (camTilePos < 33f) {
            float blend = Interpolation.smooth.apply((camTilePos - 27f) / 6f);
            drawParallaxSliceSet(act1Far, act1Mid, act1Ground, 1.0f, camX, leftVisible, rightVisible, viewH);
            drawParallaxSliceSet(act2Far, act2Mid, act2Ground, blend, camX, leftVisible, rightVisible, viewH);
        } else if (camTilePos <= 67f) {
            drawParallaxSliceSet(act2Far, act2Mid, act2Ground, 1.0f, camX, leftVisible, rightVisible, viewH);
        } else if (camTilePos < 73f) {
            float blend = Interpolation.smooth.apply((camTilePos - 67f) / 6f);
            drawParallaxSliceSet(act2Far, act2Mid, act2Ground, 1.0f, camX, leftVisible, rightVisible, viewH);
            drawParallaxSliceSet(act3Far, act3Mid, act3Ground, blend, camX, leftVisible, rightVisible, viewH);
        } else {
            drawParallaxSliceSet(act3Far, act3Mid, act3Ground, 1.0f, camX, leftVisible, rightVisible, viewH);
        }
    }

    private void drawParallaxSliceSet(TextureRegion farSlice, TextureRegion midSlice, TextureRegion groundSlice, float alpha,
                                      float camX, float leftVisible, float rightVisible, float viewH) {
        if (alpha <= 0.001f) return;

        // 1. Layer 3 (Backdrop): Far Canopy / Deep Forest (0.15x parallax)
        float farH = Math.max(1050f, viewH) + 380f;
        float farW = farH * ((float) farSlice.getRegionWidth() / farSlice.getRegionHeight());
        float stepFar = farW - 2f;
        float farOffset = (camX * 0.15f) % stepFar;
        if (farOffset < 0) farOffset += stepFar;

        float farStart = leftVisible - farOffset;
        while (farStart > leftVisible - farW) farStart -= stepFar;
        batch.setColor(1f, 1f, 1f, alpha);
        for (float x = farStart; x < rightVisible + farW; x += stepFar) {
            batch.draw(farSlice, x, 140f, farW, farH);
        }

        // 2. Layer 2 (Midground): Silhouettes & Ancient Ruins (0.35x parallax)
        float midH = 620f;
        float midW = midH * ((float) midSlice.getRegionWidth() / midSlice.getRegionHeight());
        float stepMid = midW - 2f;
        float midOffset = (camX * 0.35f) % stepMid;
        if (midOffset < 0) midOffset += stepMid;

        float midStart = leftVisible - midOffset;
        while (midStart > leftVisible - midW) midStart -= stepMid;
        for (float x = midStart; x < rightVisible + midW; x += stepMid) {
            batch.draw(midSlice, x, 180f, midW, midH);
        }

        // 3. Layer 1 (Foreground): Terrain Ground Platform (1.0x scroll with world)
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
        batch.setColor(Color.WHITE);
    }

    /**
     * Renders the connected translucent flagstone walkway across all 100 tiles.
     * Features:
     * - Tiered stepped elevation across Acts (Act I at 220f, Act II at 275f, Act III at 330f).
     * - Grand carved ascending stone staircases at Act boundaries (Tile 30 Sanctuary & Tile 70 Abyss Gate).
     * - Solid bedrock and retaining masonry foundation under elevated tiers (zero floating gaps).
     * - Upright illuminated ladders with celestial ascension beams and glowing rungs on Ladder tiles.
     * - Hazard warning crests and glowing serpent eyes on Snake tiles.
     * - Translucent opacity (78-85%) harmonizing with each Act's atmospheric background palette.
     * - High-contrast engraved tile numbers and destination indicators (+ LADDER -> dest, - SNAKE -> dest).
     */
    private void renderGroundedSteppingStones() {
        float camX = worldCamera.position.x;
        float viewW = worldCamera.viewportWidth;
        float camLeft = camX - viewW / 2f - 200f;
        float camRight = camX + viewW / 2f + 200f;

        Texture stoneNormal = game.getAssetManager().get(Assets.TILE_STONE_NORMAL);
        Texture stoneSnake  = game.getAssetManager().get(Assets.TILE_STONE_SNAKE);
        Texture stoneLadder = game.getAssetManager().get(Assets.TILE_STONE_LADDER);

        for (int i = 1; i <= 100; i++) {
            float tileLeft = getTileCenterX(i) - TILE_WIDTH / 2f;
            float tileRight = tileLeft + TILE_WIDTH;
            if (tileRight < camLeft || tileLeft > camRight) continue;

            float tileY = getTileBottomY(i);
            Tile tile = board.getTile(i);
            boolean isRevealed = tile.isRevealed();
            Texture slabTex = stoneNormal;
            if (isRevealed) {
                if (tile instanceof SnakeTile) slabTex = stoneSnake;
                else if (tile instanceof LadderTile) slabTex = stoneLadder;
            }

            // =========================================================================
            // Elevated Plateau Retaining Wall Foundation Masonry
            // =========================================================================
            if (i >= 31 && i <= 70) {
                // Act II Elevated Sunken Ruins Foundation (Height: 275 - 110 = 165px)
                float wallH = tileY - GROUND_BASE_Y;
                batch.setColor(0.08f, 0.16f, 0.24f, 0.88f);
                batch.draw(whitePixelTex, tileLeft, GROUND_BASE_Y, TILE_WIDTH + 1f, wallH);
                // Horizontal stone coursing groove
                batch.setColor(0.18f, 0.28f, 0.40f, 0.50f);
                batch.draw(whitePixelTex, tileLeft, GROUND_BASE_Y + 75f, TILE_WIDTH + 1f, 2f);
                // Vertical block seams
                batch.setColor(0.04f, 0.08f, 0.14f, 0.65f);
                batch.draw(whitePixelTex, tileLeft, GROUND_BASE_Y, 2f, wallH);
            } else if (i >= 71) {
                // Act III Elevated Celestial Peak Foundation (Height: 330 - 110 = 220px)
                float wallH = tileY - GROUND_BASE_Y;
                batch.setColor(0.16f, 0.08f, 0.18f, 0.88f);
                batch.draw(whitePixelTex, tileLeft, GROUND_BASE_Y, TILE_WIDTH + 1f, wallH);
                // Horizontal stone coursing grooves
                batch.setColor(0.32f, 0.18f, 0.35f, 0.50f);
                batch.draw(whitePixelTex, tileLeft, GROUND_BASE_Y + 70f, TILE_WIDTH + 1f, 2f);
                batch.draw(whitePixelTex, tileLeft, GROUND_BASE_Y + 145f, TILE_WIDTH + 1f, 2f);
                // Vertical block seams
                batch.setColor(0.07f, 0.03f, 0.09f, 0.65f);
                batch.draw(whitePixelTex, tileLeft, GROUND_BASE_Y, 2f, wallH);
            }

            // =========================================================================
            // Translucent Atmospheric Color Grading (Smooth Biome Transition Gradient)
            // =========================================================================
            boolean isVoidTile = (game.getPaleShepherdTile() > 0 && i <= game.getPaleShepherdTile());
            if (isVoidTile) {
                // Swallowed by The Pale Void: desaturated dark obsidian with spectral crimson undertone
                batch.setColor(0.24f, 0.12f, 0.16f, 0.88f);
            } else if (isRevealed && tile instanceof LadderTile) {
                batch.setColor(1.0f, 0.92f, 0.70f, 0.85f);
            } else if (isRevealed && tile instanceof SnakeTile) {
                batch.setColor(0.98f, 0.70f, 0.74f, 0.85f);
            } else if (isRevealed && tile.getLandmark() == TileLandmark.SHRINE) {
                batch.setColor(0.68f, 0.84f, 1.0f, 0.88f);
            } else if (isRevealed && tile.getLandmark() == TileLandmark.MERCHANT) {
                batch.setColor(1.0f, 0.86f, 0.62f, 0.88f);
            } else if (isRevealed && tile.getLandmark() == TileLandmark.TREASURE) {
                batch.setColor(1.0f, 0.94f, 0.56f, 0.88f);
            } else if (isRevealed && tile.getLandmark() == TileLandmark.CROSSROADS) {
                batch.setColor(0.86f, 0.72f, 1.0f, 0.88f);
            } else {
                Color act1Normal = Color.valueOf("#d9f0e0"); // Soft mossy forest green
                Color act2Normal = Color.valueOf("#c7dbfa"); // Cool moonlit slate blue
                Color act3Normal = Color.valueOf("#f2bfc7"); // Scorched ruby-slate

                Color base;
                if (i <= 27) {
                    base = act1Normal;
                } else if (i < 33) {
                    float t = (i - 27f) / 6f;
                    base = new Color(act1Normal).lerp(act2Normal, t);
                } else if (i <= 67) {
                    base = act2Normal;
                } else if (i < 73) {
                    float t = (i - 67f) / 6f;
                    base = new Color(act2Normal).lerp(act3Normal, t);
                } else {
                    base = act3Normal;
                }
                float slabAlpha = isRevealed ? 0.78f : 0.64f;
                batch.setColor(base.r, base.g, base.b, slabAlpha);
            }

            // Draw stone paver slab (with +1f overlap to eliminate any OpenGL subpixel seams)
            batch.draw(slabTex, tileLeft, tileY, TILE_WIDTH + 1f, TILE_HEIGHT);
            batch.setColor(Color.WHITE);

            // Creeping Void mist veil across consumed tiles, or Fog of War on unrevealed tiles
            if (isVoidTile) {
                float voidMistPulse = 0.28f + 0.10f * MathUtils.sinDeg(stateTime * 90f + (i * 30f));
                batch.setColor(0.18f, 0.05f, 0.08f, voidMistPulse);
                batch.draw(whitePixelTex, tileLeft, tileY, TILE_WIDTH + 1f, TILE_HEIGHT + 12f);
                batch.setColor(Color.WHITE);
            } else if (!isRevealed) {
                float fogPulse = 0.22f + 0.08f * MathUtils.sinDeg(stateTime * 120f + (i * 25f));
                batch.setColor(0.78f, 0.88f, 0.98f, fogPulse);
                batch.draw(whitePixelTex, tileLeft, tileY + 3f, TILE_WIDTH, TILE_HEIGHT - 5f);
                batch.setColor(Color.WHITE);
            }

            // Top walking surface highlight
            batch.setColor(1f, 1f, 1f, 0.20f);
            batch.draw(whitePixelTex, tileLeft, tileY + TILE_HEIGHT - 2f, TILE_WIDTH, 2f);

            // =========================================================================
            // Vertical Mortar Joints & Grand Transition Staircases
            // =========================================================================
            if (i == 30) {
                // Grand Sanctuary Stepped Ascent (Ascending from 220f+55f to 275f+55f)
                // 3 carved stone steps on the right edge of Tile 30 leading up +55px to Tile 31
                float step1X = tileRight - 75f;
                float step1W = 75f;
                float step1Y = tileY + TILE_HEIGHT;
                float step1H = 18f;
                batch.setColor(0.24f, 0.36f, 0.34f, 0.88f);
                batch.draw(whitePixelTex, step1X, step1Y, step1W, step1H);
                batch.setColor(0.60f, 0.95f, 0.85f, 0.60f);
                batch.draw(whitePixelTex, step1X, step1Y + step1H - 2f, step1W, 2f);

                float step2X = tileRight - 48f;
                float step2W = 48f;
                float step2Y = step1Y + 18f;
                float step2H = 18f;
                batch.setColor(0.22f, 0.34f, 0.32f, 0.90f);
                batch.draw(whitePixelTex, step2X, step2Y, step2W, step2H);
                batch.setColor(0.60f, 0.95f, 0.85f, 0.70f);
                batch.draw(whitePixelTex, step2X, step2Y + step2H - 2f, step2W, 2f);

                float step3X = tileRight - 22f;
                float step3W = 22f;
                float step3Y = step2Y + 18f;
                float step3H = 19f;
                batch.setColor(0.20f, 0.32f, 0.30f, 0.92f);
                batch.draw(whitePixelTex, step3X, step3Y, step3W, step3H);
                batch.setColor(0.60f, 0.95f, 0.85f, 0.80f);
                batch.draw(whitePixelTex, step3X, step3Y + step3H - 2f, step3W, 2f);
            } else if (i == 70) {
                // Grand Abyss Gate Stepped Ascent (Ascending from 275f+55f to 330f+55f)
                // 3 carved stone steps on the right edge of Tile 70 leading up +55px to Tile 71
                float step1X = tileRight - 75f;
                float step1W = 75f;
                float step1Y = tileY + TILE_HEIGHT;
                float step1H = 18f;
                batch.setColor(0.32f, 0.18f, 0.34f, 0.88f);
                batch.draw(whitePixelTex, step1X, step1Y, step1W, step1H);
                batch.setColor(0.85f, 0.55f, 0.95f, 0.60f);
                batch.draw(whitePixelTex, step1X, step1Y + step1H - 2f, step1W, 2f);

                float step2X = tileRight - 48f;
                float step2W = 48f;
                float step2Y = step1Y + 18f;
                float step2H = 18f;
                batch.setColor(0.28f, 0.16f, 0.30f, 0.90f);
                batch.draw(whitePixelTex, step2X, step2Y, step2W, step2H);
                batch.setColor(0.85f, 0.55f, 0.95f, 0.70f);
                batch.draw(whitePixelTex, step2X, step2Y + step2H - 2f, step2W, 2f);

                float step3X = tileRight - 22f;
                float step3W = 22f;
                float step3Y = step2Y + 18f;
                float step3H = 19f;
                batch.setColor(0.24f, 0.14f, 0.26f, 0.92f);
                batch.draw(whitePixelTex, step3X, step3Y, step3W, step3H);
                batch.setColor(0.85f, 0.55f, 0.95f, 0.80f);
                batch.draw(whitePixelTex, step3X, step3Y + step3H - 2f, step3W, 2f);
            } else if (i < 100) {
                // Standard flat joint between consecutive tiles
                float jointX = tileRight;
                batch.setColor(0.06f, 0.10f, 0.08f, 0.40f);
                batch.draw(whitePixelTex, jointX - 1f, tileY, 2f, TILE_HEIGHT);
                batch.setColor(1f, 1f, 1f, 0.15f);
                batch.draw(whitePixelTex, jointX + 1f, tileY + 1f, 1f, TILE_HEIGHT - 2f);
            }

            // Left curb border cap on Tile 1
            if (i == 1) {
                batch.setColor(1f, 1f, 1f, 0.35f);
                batch.draw(whitePixelTex, tileLeft, tileY, 3f, TILE_HEIGHT);
            }
            // Right curb border cap on Tile 100
            if (i == 100) {
                batch.setColor(1.0f, 0.85f, 0.30f, 0.50f);
                batch.draw(whitePixelTex, tileRight - 3f, tileY, 3f, TILE_HEIGHT);
            }

            // Ceremonial Milestone Portal Beacons (Tile 30 Sanctuary & Tile 70 Abyss Gate)
            if (i == 30) {
                batch.setColor(0.50f, 0.95f, 0.85f, 0.70f);
                batch.draw(whitePixelTex, tileLeft, tileY, 4f, TILE_HEIGHT + 24f);
                batch.draw(whitePixelTex, tileRight - 4f, tileY, 4f, TILE_HEIGHT + 24f);
                batch.setColor(0.50f, 0.95f, 0.85f, 0.35f);
                batch.draw(whitePixelTex, tileLeft, tileY + TILE_HEIGHT + 20f, TILE_WIDTH, 4f);
            } else if (i == 70) {
                batch.setColor(0.85f, 0.45f, 0.95f, 0.70f);
                batch.draw(whitePixelTex, tileLeft, tileY, 4f, TILE_HEIGHT + 24f);
                batch.draw(whitePixelTex, tileRight - 4f, tileY, 4f, TILE_HEIGHT + 24f);
                batch.setColor(0.85f, 0.45f, 0.95f, 0.35f);
                batch.draw(whitePixelTex, tileLeft, tileY + TILE_HEIGHT + 20f, TILE_WIDTH, 4f);
            }
            batch.setColor(Color.WHITE);

            // =========================================================================
            // Visual Structural Features: Upright Ladder & Snake Hazard Runes (REVEALED ONLY)
            // =========================================================================
            if (isRevealed && tile instanceof LadderTile) {
                float ladderX = tileLeft + 35f;
                float ladderY = tileY + TILE_HEIGHT;
                float ladderH = 80f;
                float ladderW = 34f;
                float glowAlpha = 0.65f + 0.30f * MathUtils.sinDeg(stateTime * 180f);

                // Ascension draft beam
                batch.setColor(1.0f, 0.92f, 0.55f, 0.16f * glowAlpha);
                batch.draw(whitePixelTex, ladderX - 8f, ladderY, ladderW + 16f, ladderH + 40f);

                // Rails
                batch.setColor(0.92f, 0.72f, 0.22f, 0.88f);
                batch.draw(whitePixelTex, ladderX, ladderY, 4f, ladderH);
                batch.draw(whitePixelTex, ladderX + ladderW - 4f, ladderY, 4f, ladderH);

                // Rungs
                batch.setColor(1.0f, 0.88f, 0.45f, glowAlpha);
                for (float ry = ladderY + 14f; ry < ladderY + ladderH - 4f; ry += 16f) {
                    batch.draw(whitePixelTex, ladderX + 2f, ry, ladderW - 4f, 3f);
                }
                batch.setColor(Color.WHITE);
            } else if (isRevealed && tile instanceof SnakeTile) {
                float snakeX = tileLeft + 32f;
                float snakeY = tileY + TILE_HEIGHT;
                float snakeW = 40f;
                float pulse = 0.65f + 0.30f * MathUtils.sinDeg(stateTime * 200f);

                // Hazard aura along slab surface
                batch.setColor(0.85f, 0.15f, 0.20f, 0.25f * pulse);
                batch.draw(whitePixelTex, tileLeft + 12f, tileY + TILE_HEIGHT, TILE_WIDTH - 24f, 8f);

                // Hazard Crest
                batch.setColor(0.95f, 0.25f, 0.30f, 0.85f);
                batch.draw(whitePixelTex, snakeX, snakeY, 4f, 22f);
                batch.draw(whitePixelTex, snakeX + snakeW - 4f, snakeY, 4f, 22f);
                batch.draw(whitePixelTex, snakeX, snakeY + 10f, snakeW, 3f);

                // Glowing Serpent Eyes
                batch.setColor(1.0f, 0.10f, 0.15f, pulse);
                batch.draw(whitePixelTex, snakeX + 10f, snakeY + 15f, 5f, 5f);
                batch.draw(whitePixelTex, snakeX + snakeW - 15f, snakeY + 15f, 5f, 5f);
                batch.setColor(Color.WHITE);
            }

            // =========================================================================
            // Tile Typography & Badges
            // =========================================================================
            if (!isRevealed) {
                // UNCHARTED BADGE (Fog of War)
                float pulse = 0.60f + 0.35f * MathUtils.sinDeg(stateTime * 140f + (i * 22f));
                tileBadgeFont.setColor(0.08f, 0.12f, 0.16f, 0.80f);
                tileBadgeFont.draw(batch, "? UNCHARTED ?", tileLeft + 1f, tileY + 27f, TILE_WIDTH, Align.center, false);
                tileBadgeFont.setColor(0.72f, 0.86f, 0.98f, pulse);
                tileBadgeFont.draw(batch, "? UNCHARTED ?", tileLeft, tileY + 28f, TILE_WIDTH, Align.center, false);

                miniFont.setColor(0.75f, 0.86f, 0.94f, 0.85f);
                miniFont.draw(batch, "TILE " + i, tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);

            } else if (tile instanceof LadderTile) {
                LadderTile ladder = (LadderTile) tile;
                int dest = ladder.getDestination();

                tileBadgeFont.setColor(Color.valueOf("#201202"));
                tileBadgeFont.draw(batch, "+ LADDER -> " + dest, tileLeft + 1f, tileY + 27f, TILE_WIDTH, Align.center, false);
                tileBadgeFont.setColor(Color.valueOf("#ffe082"));
                tileBadgeFont.draw(batch, "+ LADDER -> " + dest, tileLeft, tileY + 28f, TILE_WIDTH, Align.center, false);
                miniFont.setColor(Color.valueOf("#fff8e1"));
                miniFont.draw(batch, "TILE " + i, tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);

            } else if (tile instanceof SnakeTile) {
                SnakeTile snake = (SnakeTile) tile;
                int dest = snake.getDestination();

                tileBadgeFont.setColor(Color.valueOf("#260404"));
                tileBadgeFont.draw(batch, "- SNAKE -> " + dest, tileLeft + 1f, tileY + 27f, TILE_WIDTH, Align.center, false);
                tileBadgeFont.setColor(Color.valueOf("#ff6b6b"));
                tileBadgeFont.draw(batch, "- SNAKE -> " + dest, tileLeft, tileY + 28f, TILE_WIDTH, Align.center, false);
                miniFont.setColor(Color.valueOf("#ffebee"));
                miniFont.draw(batch, "TILE " + i, tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);

            } else {
                if (i == 1) {
                    miniFont.setColor(Color.valueOf("#81c784"));
                    miniFont.draw(batch, "START CAMP", tileLeft, tileY + 27f, TILE_WIDTH, Align.center, false);
                    miniFont.setColor(Color.valueOf("#c8e6c9"));
                    miniFont.draw(batch, "TILE 1", tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);
                } else if (i == 30) {
                    miniFont.setColor(Color.valueOf("#80cbc4"));
                    miniFont.draw(batch, "SANCTUARY", tileLeft, tileY + 27f, TILE_WIDTH, Align.center, false);
                    miniFont.setColor(Color.valueOf("#b2dfdb"));
                    miniFont.draw(batch, "ACT I FINALE (30)", tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);
                } else if (i == 70) {
                    miniFont.setColor(Color.valueOf("#b39ddb"));
                    miniFont.draw(batch, "ABYSS GATE", tileLeft, tileY + 27f, TILE_WIDTH, Align.center, false);
                    miniFont.setColor(Color.valueOf("#d1c4e9"));
                    miniFont.draw(batch, "ACT II FINALE (70)", tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);
                } else if (i == 100) {
                    miniFont.setColor(Color.valueOf("#ffd700"));
                    miniFont.draw(batch, "THRONE OF FATE", tileLeft, tileY + 27f, TILE_WIDTH, Align.center, false);
                    miniFont.setColor(Color.valueOf("#fff9c4"));
                    miniFont.draw(batch, "* VICTORY (100) *", tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);
                } else if (tile.getLandmark() == TileLandmark.SHRINE) {
                    tileBadgeFont.setColor(Color.valueOf("#0d1e33"));
                    tileBadgeFont.draw(batch, "* SHRINE *", tileLeft + 1f, tileY + 27f, TILE_WIDTH, Align.center, false);
                    tileBadgeFont.setColor(Color.valueOf("#90caf9"));
                    tileBadgeFont.draw(batch, "* SHRINE *", tileLeft, tileY + 28f, TILE_WIDTH, Align.center, false);
                    miniFont.setColor(Color.valueOf("#e3f2fd"));
                    miniFont.draw(batch, "TILE " + i, tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);
                } else if (tile.getLandmark() == TileLandmark.MERCHANT) {
                    tileBadgeFont.setColor(Color.valueOf("#2e1802"));
                    tileBadgeFont.draw(batch, "* MERCHANT *", tileLeft + 1f, tileY + 27f, TILE_WIDTH, Align.center, false);
                    tileBadgeFont.setColor(Color.valueOf("#ffcc80"));
                    tileBadgeFont.draw(batch, "* MERCHANT *", tileLeft, tileY + 28f, TILE_WIDTH, Align.center, false);
                    miniFont.setColor(Color.valueOf("#fff3e0"));
                    miniFont.draw(batch, "TILE " + i, tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);
                } else if (tile.getLandmark() == TileLandmark.TREASURE) {
                    tileBadgeFont.setColor(Color.valueOf("#2b2100"));
                    tileBadgeFont.draw(batch, "* CACHE *", tileLeft + 1f, tileY + 27f, TILE_WIDTH, Align.center, false);
                    tileBadgeFont.setColor(Color.valueOf("#fff176"));
                    tileBadgeFont.draw(batch, "* CACHE *", tileLeft, tileY + 28f, TILE_WIDTH, Align.center, false);
                    miniFont.setColor(Color.valueOf("#fffde7"));
                    miniFont.draw(batch, "TILE " + i, tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);
                } else if (tile.getLandmark() == TileLandmark.CROSSROADS) {
                    tileBadgeFont.setColor(Color.valueOf("#1f0c2e"));
                    tileBadgeFont.draw(batch, "* CROSSROADS *", tileLeft + 1f, tileY + 27f, TILE_WIDTH, Align.center, false);
                    tileBadgeFont.setColor(Color.valueOf("#ce93d8"));
                    tileBadgeFont.draw(batch, "* CROSSROADS *", tileLeft, tileY + 28f, TILE_WIDTH, Align.center, false);
                    miniFont.setColor(Color.valueOf("#f3e5f5"));
                    miniFont.draw(batch, "TILE " + i, tileLeft, tileY + 45f, TILE_WIDTH, Align.center, false);
                } else if (i % 10 == 0) {
                    miniFont.setColor(Color.valueOf("#ffd54f"));
                    miniFont.draw(batch, "* TILE " + i + " *", tileLeft, tileY + 34f, TILE_WIDTH, Align.center, false);
                } else {
                    miniFont.setColor(Color.valueOf("#d4c4ac"));
                    miniFont.draw(batch, String.valueOf(i), tileLeft, tileY + 34f, TILE_WIDTH, Align.center, false);
                }
            }

            // =========================================================================
            // Discovery Burst FX (Triggered upon stepping onto an uncharted tile or scouting)
            // =========================================================================
            if (i == revealFlashTile && revealFlashTimer > 0f) {
                float flashAlpha = MathUtils.clamp(revealFlashTimer, 0f, 1f);
                if (tile instanceof LadderTile) {
                    batch.setColor(1.0f, 0.95f, 0.50f, 0.65f * flashAlpha);
                } else if (tile instanceof SnakeTile) {
                    batch.setColor(0.95f, 0.20f, 0.25f, 0.65f * flashAlpha);
                } else {
                    batch.setColor(0.55f, 0.95f, 0.80f, 0.55f * flashAlpha);
                }
                batch.draw(whitePixelTex, tileLeft - 8f, tileY - 6f, TILE_WIDTH + 16f, TILE_HEIGHT + 14f);
                batch.setColor(Color.WHITE);
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
            boolean sharesWithActive = (player.getPosition() == activePos && turnState != TurnState.STEPPING_TILES && !isSpecialMoving);
            float drawX = sharesWithActive ? (tileCenterX - 145f) : (tileCenterX - (CHAR_DISPLAY_SIZE / 2f));
            float drawY = sharesWithActive ? (getTileBottomY(player.getPosition()) + 18f) : getCharacterDrawY(player.getPosition());
            float size = sharesWithActive ? CHAR_INACTIVE_SIZE : CHAR_DISPLAY_SIZE;

            boolean isDead = (player.getHealth() <= 0);
            if (isDead) {
                batch.setColor(0.35f, 0.35f, 0.40f, 0.50f);
                drawY -= 8f;
                batch.draw(idleFrame, drawX, drawY, size, size * 0.72f);
            } else {
                if (sharesWithActive) {
                    batch.setColor(0.85f, 0.85f, 0.9f, 0.80f);
                } else {
                    batch.setColor(Color.WHITE);
                }
                batch.draw(idleFrame, drawX, drawY, size, size);
            }
            batch.setColor(Color.WHITE);
        }

        // 2. Render ACTIVE player LAST (guaranteed on the topmost layer and fully visible)
        if (activePlayer != null) {
            Animation<TextureRegion> anim = walkAnimations.get(activePlayer.getCharacterClass());
            Texture charTex = game.getAssetManager().get(Assets.getCharacterTexture(activePlayer.getCharacterClass()));
            float drawX;
            float drawY;
            TextureRegion activeFrame;

            if (isSpecialMoving) {
                drawX = specialMoveCurrentX - (CHAR_DISPLAY_SIZE / 2f);
                drawY = specialMoveCurrentY;

                if (specialMovePhase == SpecialMovePhase.WINDUP) {
                    // Standing firmly on the start tile while discovery and celestial power register
                    activeFrame = (anim != null) ? anim.getKeyFrames()[0] : new TextureRegion(charTex);

                    if (isLadderMove) {
                        float pulse = 0.5f + 0.5f * MathUtils.sinDeg(stateTime * 360f);
                        batch.setColor(1.0f, 0.90f, 0.40f, 0.45f * pulse);
                        float beamW = 60f + pulse * 20f;
                        batch.draw(whitePixelTex, specialMoveCurrentX - beamW / 2f, drawY - 4f, beamW, 22f);
                        batch.setColor(Color.WHITE);
                    }
                } else if (specialMovePhase == SpecialMovePhase.LANDED) {
                    // Touchdown landing recovery on destination tile
                    activeFrame = (anim != null) ? anim.getKeyFrames()[0] : new TextureRegion(charTex);

                    if (isLadderMove) {
                        float landAlpha = MathUtils.clamp(specialMovePhaseTimer / 0.65f, 0f, 1f);
                        batch.setColor(1.0f, 0.95f, 0.60f, 0.55f * landAlpha);
                        float ringW = 90f + (1f - landAlpha) * 40f;
                        batch.draw(whitePixelTex, specialMoveCurrentX - ringW / 2f, drawY - 4f, ringW, 12f);
                        batch.setColor(Color.WHITE);
                    }
                } else {
                    // IN_FLIGHT: active walking/climbing motion along the soaring arc
                    activeFrame = (anim != null) ? anim.getKeyFrame(walkStateTime, true) : new TextureRegion(charTex);

                    if (isLadderMove) {
                        float progress = MathUtils.clamp(specialMoveElapsed / specialMoveDuration, 0f, 1f);

                        // Dynamic Drop Shadow on the ground directly below the ascending character
                        float groundY = MathUtils.lerp(
                            getCharacterDrawY(specialMoveStartTile),
                            getCharacterDrawY(specialMoveDestTile),
                            progress
                        ) - 6f;
                        float heightAboveGround = Math.max(0f, specialMoveCurrentY - groundY);
                        float shadowScale = MathUtils.clamp(1f - (heightAboveGround / 220f), 0.35f, 1.0f);
                        float shadowAlpha = MathUtils.clamp(0.45f - (heightAboveGround / 400f), 0.15f, 0.45f);

                        batch.setColor(0.04f, 0.08f, 0.06f, shadowAlpha);
                        float shadowW = 110f * shadowScale;
                        float shadowH = 22f * shadowScale;
                        batch.draw(whitePixelTex, specialMoveCurrentX - shadowW / 2f, groundY, shadowW, shadowH);

                        // Golden Ascending Particle Shimmer / Celestial Rung trail under character's feet
                        float trailPulse = 0.6f + 0.35f * MathUtils.sinDeg(stateTime * 360f);
                        batch.setColor(1.0f, 0.88f, 0.40f, 0.50f * trailPulse);
                        for (int s = 0; s < 4; s++) {
                            float sparkY = specialMoveCurrentY - 12f - (s * 16f);
                            float sparkX = specialMoveCurrentX + MathUtils.sinDeg((stateTime * 240f) + (s * 75f)) * 18f;
                            batch.draw(whitePixelTex, sparkX - 3f, sparkY, 6f, 6f);
                        }
                        batch.setColor(Color.WHITE);
                    } else {
                        // Snake slide: crimson hazard dust trail behind player
                        batch.setColor(0.95f, 0.20f, 0.25f, 0.45f);
                        for (int s = 0; s < 4; s++) {
                            float sparkY = specialMoveCurrentY + 8f + (s * 10f);
                            float sparkX = specialMoveCurrentX + 15f + (s * 18f);
                            batch.draw(whitePixelTex, sparkX - 4f, sparkY, 8f, 8f);
                        }
                        batch.setColor(Color.WHITE);
                    }
                }
            } else if (turnState == TurnState.STEPPING_TILES) {
                float fromX = getTileCenterX(hopCurrentTile);
                float toX = getTileCenterX(Math.min(100, hopCurrentTile + 1));
                float progress = MathUtils.clamp(stepTimer / STEP_DURATION, 0f, 1f);

                drawX = MathUtils.lerp(fromX, toX, progress) - (CHAR_DISPLAY_SIZE / 2f);
                float fromY = getCharacterDrawY(hopCurrentTile);
                float toY = getCharacterDrawY(Math.min(100, hopCurrentTile + 1));
                drawY = MathUtils.lerp(fromY, toY, progress);
                activeFrame = (anim != null) ? anim.getKeyFrame(walkStateTime, true) : new TextureRegion(charTex);
            } else {
                drawX = getTileCenterX(activePlayer.getPosition()) - (CHAR_DISPLAY_SIZE / 2f);
                drawY = getCharacterDrawY(activePlayer.getPosition());
                activeFrame = (anim != null) ? anim.getKeyFrames()[0] : new TextureRegion(charTex);
            }

            boolean isDead = (activePlayer.getHealth() <= 0);
            if (isDead) {
                batch.setColor(0.35f, 0.35f, 0.40f, 0.50f);
                batch.draw(activeFrame, drawX, drawY - 8f, CHAR_DISPLAY_SIZE, CHAR_DISPLAY_SIZE * 0.72f);
            } else {
                batch.setColor(Color.WHITE);
                batch.draw(activeFrame, drawX, drawY, CHAR_DISPLAY_SIZE, CHAR_DISPLAY_SIZE);
            }
            batch.setColor(Color.WHITE);
        }
    }

    /**
     * Updates HUD elements to reflect active player stats and current progress.
     */
    private void updateHudDisplay() {
        Player player = game.getCurrentPlayer();
        if (player == null) return;

        hudPlayerName.setText("P" + (game.getCurrentTurn() + 1) + ": " + player.getName().toUpperCase());
        int maxHp = player.getMaxHealth();
        hudPlayerHp.setText("HP: " + player.getHealth() + " / " + maxHp);
        hudPlayerExp.setText("LVL " + player.getLevel() + "    -    EXP: " + player.getExperience() + " / " + player.getExpThresh());
        hudPlayerStats.setText("ATK  " + player.getAttack() + "    -    DEF  " + player.getDefense());

        Texture avatarTex = game.getAssetManager().get(Assets.getCharacterTexture(player.getCharacterClass()));
        hudPlayerAvatar.setDrawable(new TextureRegionDrawable(new TextureRegion(avatarTex)));
        hudPlayerAvatar.setScaling(Scaling.fit);
        hudPlayerAvatar.setAlign(Align.center);

        updateMiniMapProgress();
        updateInventoryButtonCount();
        updateSkillButtonDisplay(player);

        if (inventoryPopup != null && inventoryPopup.isVisible()) {
            refreshInventoryUI();
        }
    }

    private void updateSkillButtonDisplay(Player player) {
        if (skillButton == null || player == null) return;
        ClassSkill skill = player.getClassSkill();
        if (skill == null) {
            skillButton.setVisible(false);
            return;
        }
        skillButton.setVisible(true);

        if (player.getCharacterClass() != null && game.getAssetManager().isLoaded(Assets.getCharacterHeadTexture(player.getCharacterClass()))) {
            Texture headTex = game.getAssetManager().get(Assets.getCharacterHeadTexture(player.getCharacterClass()));
            skillIcon.setDrawable(new TextureRegionDrawable(new TextureRegion(headTex)));
        }

        skillNameLabel.setText(skill.getSkillName().toUpperCase());

        if (player.isSkillReady()) {
            skillButton.setStyle(skillReadyStyle);
            skillStatusLabel.setText("READY (USE)");
            skillStatusLabel.setColor(Color.valueOf("#206a24")); // Vibrant active green
            skillNameLabel.setColor(Color.valueOf("#361704"));
        } else {
            skillButton.setStyle(skillCooldownStyle);
            int cd = player.getSkillCooldown();
            skillStatusLabel.setText(cd + (cd == 1 ? " TURN" : " TURNS"));
            skillStatusLabel.setColor(Color.valueOf("#fbf2dc")); // Readable cream on cooldown leather
            skillNameLabel.setColor(Color.valueOf("#fff3dd"));
        }
    }

    private void updateInventoryButtonCount() {
        if (hudInventorySubLabel == null) return;
        Player player = game.getCurrentPlayer();
        if (player == null || player.getInventory() == null) {
            hudInventorySubLabel.setText("0 / 4 SLOTS");
            hudInventorySubLabel.setColor(Color.valueOf("#6d3a17"));
            return;
        }
        int itemCount = player.getInventory().size();
        int capacity = player.getInventory().getCapacity();
        if (itemCount >= capacity) {
            hudInventorySubLabel.setText(itemCount + " / " + capacity + " SLOTS (FULL)");
            hudInventorySubLabel.setColor(Color.valueOf("#b85d19"));
        } else {
            hudInventorySubLabel.setText(itemCount + " / " + capacity + " SLOTS");
            hudInventorySubLabel.setColor(Color.valueOf("#6d3a17"));
        }
    }

    private void updateMiniMapProgress() {
        Player player = game.getCurrentPlayer();
        if (player == null) return;

        int pos = player.getPosition();
        int currentAct = (pos <= 30) ? 1 : ((pos <= 70) ? 2 : 3);

        if (currentAct > lastTrackedAct) {
            showActTransitionToast(currentAct);
            lastTrackedAct = currentAct;
        }

        String actStr = (currentAct == 1) ? "ACT I" : ((currentAct == 2) ? "ACT II" : "ACT III");
        miniMapProgressLabel.setText(actStr + "   -   TILE " + pos + " / 100");

        updatePlayerMiniMapMarkers(true);
    }

    private float getMiniMapTileX(int tile, float markerWidth) {
        int t = MathUtils.clamp(tile, 1, 100);
        float centerX;
        if (t <= 30) {
            float frac = (t - 1) / 29f;
            centerX = frac * 102f;
        } else if (t <= 70) {
            float frac = (t - 30) / 40f;
            centerX = 106f + frac * 128f;
        } else {
            float frac = (t - 70) / 30f;
            centerX = 238f + frac * 102f;
        }
        return MathUtils.clamp(centerX - markerWidth / 2f, 0f, 340f - markerWidth);
    }

    private void initPlayerMiniMapMarkers() {
        if (miniMapTrackGroup == null) return;
        playerMiniMapMarkers.clear();

        if (shepherdMiniMapMarker == null && game.getAssetManager().isLoaded(Assets.CHAR_PALE_SHEPHERD_HEAD)) {
            Texture shepherdHeadTex = game.getAssetManager().get(Assets.CHAR_PALE_SHEPHERD_HEAD);
            float aspect = (float) shepherdHeadTex.getWidth() / (float) shepherdHeadTex.getHeight();
            float markerH = 22f;
            float markerW = markerH * aspect;
            shepherdMiniMapMarker = new Image(shepherdHeadTex);
            shepherdMiniMapMarker.setSize(markerW, markerH);
            shepherdMiniMapMarker.setScaling(Scaling.fit);
            miniMapTrackGroup.addActor(shepherdMiniMapMarker);
        }
        if (shepherdMiniMapMarker != null) {
            shepherdMiniMapMarker.setVisible(game.getPaleShepherdTile() > 0);
        }

        List<Player> party = game.getPlayers();
        if (party == null || party.isEmpty()) {
            if (game.getCurrentPlayer() != null) {
                party = Collections.singletonList(game.getCurrentPlayer());
            } else {
                return;
            }
        }

        for (Player p : party) {
            CharacterClass cls = p.getCharacterClass() != null ? p.getCharacterClass() : CharacterClass.CAT;
            Texture headTex = game.getAssetManager().get(Assets.getCharacterHeadTexture(cls));
            float aspect = (float) headTex.getWidth() / (float) headTex.getHeight();
            float markerH = 22f;
            float markerW = markerH * aspect;
            Image marker = new Image(headTex);
            marker.setSize(markerW, markerH);
            marker.setScaling(Scaling.fit);
            miniMapTrackGroup.addActor(marker);
            playerMiniMapMarkers.put(p, marker);
        }

        updatePlayerMiniMapMarkers(false);
    }

    private void updatePlayerMiniMapMarkers(boolean animate) {
        if (miniMapTrackGroup == null) return;

        List<Player> party = game.getPlayers();
        if (party == null || party.isEmpty()) return;

        for (Player p : party) {
            if (!playerMiniMapMarkers.containsKey(p)) {
                CharacterClass cls = p.getCharacterClass() != null ? p.getCharacterClass() : CharacterClass.CAT;
                Texture headTex = game.getAssetManager().get(Assets.getCharacterHeadTexture(cls));
                float aspect = (float) headTex.getWidth() / (float) headTex.getHeight();
                float markerH = 22f;
                float markerW = markerH * aspect;
                Image marker = new Image(headTex);
                marker.setSize(markerW, markerH);
                marker.setScaling(Scaling.fit);
                miniMapTrackGroup.addActor(marker);
                playerMiniMapMarkers.put(p, marker);
            }
        }

        Player activePlayer = game.getCurrentPlayer();

        Map<Player, Float> rawXMap = new HashMap<>();
        for (Player p : party) {
            Image marker = playerMiniMapMarkers.get(p);
            float mW = (marker != null) ? marker.getWidth() : 22f;
            rawXMap.put(p, getMiniMapTileX(p.getPosition(), mW));
        }

        List<List<Player>> clusters = new ArrayList<>();
        List<Player> unclustered = new ArrayList<>(party);

        while (!unclustered.isEmpty()) {
            Player ref = unclustered.remove(0);
            List<Player> cluster = new ArrayList<>();
            cluster.add(ref);
            float refX = rawXMap.get(ref);

            for (int i = unclustered.size() - 1; i >= 0; i--) {
                Player other = unclustered.get(i);
                if (Math.abs(rawXMap.get(other) - refX) <= 14f) {
                    cluster.add(other);
                    unclustered.remove(i);
                }
            }
            clusters.add(cluster);
        }

        float baseY = 9f;

        for (List<Player> cluster : clusters) {
            int size = cluster.size();
            for (int i = 0; i < size; i++) {
                Player p = cluster.get(i);
                Image marker = playerMiniMapMarkers.get(p);
                if (marker == null) continue;

                float offsetX = 0f;
                float offsetY = 0f;

                if (size == 2) {
                    offsetX = (i == 0) ? -5f : 5f;
                    offsetY = (i == 0) ? -2f : 5f;
                } else if (size == 3) {
                    offsetX = (i == 0) ? -7f : ((i == 1) ? 0f : 7f);
                    offsetY = (i == 0) ? -2f : ((i == 1) ? 5f : -2f);
                } else if (size >= 4) {
                    offsetX = (i == 0) ? -9f : ((i == 1) ? -3f : ((i == 2) ? 3f : 9f));
                    offsetY = (i % 2 == 0) ? -2f : 6f;
                }

                float targetX = MathUtils.clamp(rawXMap.get(p) + offsetX, 0f, 340f - marker.getWidth());
                float targetY = baseY + offsetY;

                marker.clearActions();
                if (animate) {
                    marker.addAction(Actions.moveTo(targetX, targetY, 0.35f, Interpolation.smooth));
                } else {
                    marker.setPosition(targetX, targetY);
                }

                if (p == activePlayer) {
                    marker.setColor(Color.WHITE);
                } else {
                    marker.setColor(0.85f, 0.85f, 0.85f, 0.90f);
                }
            }
        }

        // Update Pale Shepherd pursuer position on the mini-map track
        int shepherdTile = game.getPaleShepherdTile();
        if (shepherdMiniMapMarker == null && game.getAssetManager().isLoaded(Assets.CHAR_PALE_SHEPHERD_HEAD)) {
            Texture shepherdHeadTex = game.getAssetManager().get(Assets.CHAR_PALE_SHEPHERD_HEAD);
            float aspect = (float) shepherdHeadTex.getWidth() / (float) shepherdHeadTex.getHeight();
            float markerH = 22f;
            float markerW = markerH * aspect;
            shepherdMiniMapMarker = new Image(shepherdHeadTex);
            shepherdMiniMapMarker.setSize(markerW, markerH);
            shepherdMiniMapMarker.setScaling(Scaling.fit);
            miniMapTrackGroup.addActor(shepherdMiniMapMarker);
        }

        if (shepherdMiniMapMarker != null) {
            if (shepherdTile > 0) {
                shepherdMiniMapMarker.setVisible(true);
                float sW = shepherdMiniMapMarker.getWidth();
                float targetX = getMiniMapTileX(shepherdTile, sW);
                float targetY = baseY + 4f;
                shepherdMiniMapMarker.clearActions();
                if (animate) {
                    shepherdMiniMapMarker.addAction(Actions.moveTo(targetX, targetY, 0.35f, Interpolation.smooth));
                } else {
                    shepherdMiniMapMarker.setPosition(targetX, targetY);
                }
                shepherdMiniMapMarker.toFront();

                if (voidMiniMapOverlay != null) {
                    float targetVoidW = Math.max(0, getMiniMapTileX(shepherdTile, 0));
                    voidMiniMapOverlay.setWidth(targetVoidW);
                    voidMiniMapOverlay.setVisible(targetVoidW > 0);
                }
            } else {
                shepherdMiniMapMarker.setVisible(false);
                if (voidMiniMapOverlay != null) {
                    voidMiniMapOverlay.setVisible(false);
                }
            }
        }

        if (activePlayer != null && playerMiniMapMarkers.containsKey(activePlayer)) {
            playerMiniMapMarkers.get(activePlayer).toFront();
        }
    }

    /**
     * Tiered Elevation Profile across the 3 Acts:
     * - Act I (Tiles 1-30): Lowland Valley Forest baseline (Y = 220f)
     * - Act II (Tiles 31-70): Elevated Sunken Ruins plateau (Y = 275f, +55px higher)
     * - Act III (Tiles 71-100): High Celestial Peak / Throne of Fate (Y = 330f, +110px higher)
     */
    public static float getTileBottomY(int tileIndex) {
        if (tileIndex <= 30) {
            return 220f;
        } else if (tileIndex <= 70) {
            return 275f;
        } else {
            return 330f;
        }
    }

    /**
     * Character foot anchor dynamically aligned to each tile's top walking surface.
     */
    public static float getCharacterDrawY(int tileIndex) {
        return getTileBottomY(tileIndex) + 14f;
    }

    /**
     * Initiates phased 3D celestial arc ascension for ladders or tumbling slide for snakes:
     * 1. Wind-up anticipation pause on start tile (power gathers, banner readable)
     * 2. Smooth soaring flight across the arc
     * 3. Touchdown recovery on destination tile
     */
    private void startSpecialMove(int startTile, int destTile, boolean isLadder) {
        if (game.getAudioManager() != null) {
            if (isLadder) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.LADDER_WINDUP);
            } else {
                game.getAudioManager().playSound(AudioManager.SoundEffect.SNAKE_HISS);
            }
        }
        this.specialMoveStartTile = startTile;
        this.specialMoveDestTile = destTile;
        this.isLadderMove = isLadder;
        this.isSpecialMoving = true;
        this.specialMovePhase = SpecialMovePhase.WINDUP;
        this.specialMovePhaseTimer = isLadder ? 0.85f : 0.65f; // Wind-up anticipation pause
        this.specialMoveElapsed = 0f;
        float jumpDist = Math.abs(destTile - startTile);
        if (isLadder) {
            this.specialMoveDuration = MathUtils.clamp(1.20f + (jumpDist * 0.04f), 1.35f, 1.95f);
            this.specialMoveArcHeight = MathUtils.clamp(120f + (jumpDist * 4.5f), 135f, 205f);
        } else {
            this.specialMoveDuration = MathUtils.clamp(0.95f + (jumpDist * 0.035f), 1.05f, 1.55f);
            this.specialMoveArcHeight = 35f;
        }
        this.specialMoveCurrentX = getTileCenterX(startTile);
        this.specialMoveCurrentY = getCharacterDrawY(startTile);
        this.turnState = TurnState.TILE_ACTION_PAUSE;
        this.actionPauseTimer = 0f;
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
        if (vignetteTex != null) vignetteTex.dispose();
        if (softShadowTex != null) softShadowTex.dispose();

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
        if (pauseBadgeFont != null) pauseBadgeFont.dispose();
        if (pauseTitleFont != null) pauseTitleFont.dispose();
        if (pauseSubFont != null) pauseSubFont.dispose();
        if (pauseBodyFont != null) pauseBodyFont.dispose();
        if (pauseBtnFont != null) pauseBtnFont.dispose();
        if (pauseTipFont != null) pauseTipFont.dispose();

        if (eventBadgeFont != null) eventBadgeFont.dispose();
        if (eventTitleFont != null) eventTitleFont.dispose();
        if (eventDescFont != null) eventDescFont.dispose();
        if (choiceCardTitleFont != null) choiceCardTitleFont.dispose();
        if (choiceBtnFont != null) choiceBtnFont.dispose();
        if (choiceActionBtnFont != null) choiceActionBtnFont.dispose();
        if (choiceTagFont != null) choiceTagFont.dispose();
        if (choiceOutcomeTitleFont != null) choiceOutcomeTitleFont.dispose();
        if (choiceOutcomeBodyFont != null) choiceOutcomeBodyFont.dispose();
        if (choiceOutcomeValueFont != null) choiceOutcomeValueFont.dispose();
        if (choiceD20Font != null) choiceD20Font.dispose();
        if (skillTitleFont != null) skillTitleFont.dispose();
        if (skillSubFont != null) skillSubFont.dispose();
    }
}
