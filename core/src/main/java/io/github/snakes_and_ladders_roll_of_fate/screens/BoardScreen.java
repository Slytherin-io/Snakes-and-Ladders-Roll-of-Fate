package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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

import java.util.List;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.board.LadderTile;
import io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile;
import io.github.snakes_and_ladders_roll_of_fate.board.Tile;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.items.ItemType;
import io.github.snakes_and_ladders_roll_of_fate.systems.Dice;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
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
        TILE_ACTION_PAUSE,
        TURN_TRANSITION
    }
    private TurnState turnState = TurnState.WAITING_FOR_ROLL;

    // Dice & Animation State
    private int currentRollValue = 6;
    private float diceTumbleTimer = 0f;
    private float stateTime = 0f;

    // Step-by-Step Hopping State
    private int hopCurrentTile = 1;
    private int hopTargetTile = 1;
    private float stepTimer = 0f;
    private static final float STEP_DURATION = 0.26f;

    // Action Pause Timer (Ladders / Snakes)
    private float actionPauseTimer = 0f;
    private int specialSlideTarget = -1;

    // HUD UI References
    private Label topBannerLabel;
    private Button rollButton;
    private Label diceLabel;
    private Label hudPlayerName;
    private Label hudPlayerHp;
    private Label hudPlayerStats;
    private Image hudPlayerAvatar;
    private Label miniMapProgressLabel;
    private Table inventoryPopup;
    private Table inventorySlotsTable;
    private Label inventoryTitleLabel;
    private Label inventoryCapacityLabel;
    private TextButton inventoryLootBtn;

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

        titleFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 26, Color.WHITE);
        topBannerFont  = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 32, Color.valueOf("#fffdfa"));
        topActFont     = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 23, Color.valueOf("#fdf5e2"));
        topPauseFont   = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 25, Color.WHITE);
        rollBigFont    = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 30, Color.WHITE);
        rollSubFont    = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 21, Color.valueOf("#fbf2dc"));
        hudNameFont    = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 25, nameBrown);
        hudHpFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 22, crimson);
        hudStatsFont   = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 19, warmBrown);
        playerTagFont  = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 19, Color.WHITE);
        badgeFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 22, Color.WHITE);
        tileNumFont    = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 24, goldLight);
        tileBadgeFont  = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 15, Color.WHITE);
        miniFont       = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 16, darkBrown);
        sliderFont     = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 24, darkBrown);
        modalTitleFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 28, darkBrown);
        modalBodyFont  = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 20, darkBrown);

        // --- Slice Parallax Act Backgrounds ---
        initParallaxSlices();

        // --- Build In-Game HUD Overlay ---
        buildPauseButton();
        buildTopBanner();
        buildMiniMap();
        buildBottomHud();
        buildInventoryPopup();
        buildPauseAndSettingsModals(darkBrown, pillBrown);

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

        miniMapProgressLabel = new Label("ACT I   |   TILE 1 / 100", new Label.LabelStyle(topActFont, Color.valueOf("#fdf5e2")));
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

        Image diceIcon = new Image(game.getAssetManager().get(Assets.ICON_DICE_D6));
        diceIcon.setScaling(Scaling.fit);

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
        statsTextTable.add(hudPlayerName).left().padBottom(3).row();

        // Level 2: Secondary Vital - Health with Heart Icon
        Table hpRow = new Table();
        hpRow.left();
        Image heartIcon = new Image(game.getAssetManager().get(Assets.ICON_HEART));
        heartIcon.setScaling(Scaling.fit);
        hudPlayerHp = new Label("HP: 120 / 120", new Label.LabelStyle(hudHpFont, crimson));
        hpRow.add(heartIcon).size(20, 20).padRight(6).center();
        hpRow.add(hudPlayerHp).left().center();
        statsTextTable.add(hpRow).left().padBottom(3).row();

        // Level 3: Tertiary Supporting Attributes - Attack & Defense
        Table statsRow = new Table();
        statsRow.left();
        hudPlayerStats = new Label("ATK  4    |    DEF  7", new Label.LabelStyle(hudStatsFont, warmBrown));
        statsRow.add(hudPlayerStats).left().center();
        statsTextTable.add(statsRow).left();

        statusCard.add(hudPlayerAvatar).size(68, 68).padLeft(18).padRight(16).center();
        statusCard.add(statsTextTable).expandX().left().padRight(18).center();

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
        inventoryTitleLabel = new Label("PLAYER 1'S SATCHEL", new Label.LabelStyle(titleFont, Color.WHITE));
        inventoryCapacityLabel = new Label("0 / 4 SLOTS", new Label.LabelStyle(playerTagFont, Color.valueOf("#ffd54f")));

        Table badgeTable = new Table();
        badgeTable.setBackground(UIUtils.createRoundedDrawable(140, 36, 10, pillBrown));
        badgeTable.add(inventoryCapacityLabel).center();

        headerTable.add(inventoryTitleLabel).left();
        headerTable.add().expandX();
        headerTable.add(badgeTable).size(140, 36).right();

        inventoryPopup.add(headerTable).width(570).padTop(20).padBottom(6).center().row();

        Label tip = new Label("Carry limit: 4 items per player. Tap USE to activate an item.", new Label.LabelStyle(miniFont, Color.WHITE));
        tip.setWrap(true);
        tip.setAlignment(Align.center);
        inventoryPopup.add(tip).width(570).padBottom(14).center().row();

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

        inventoryTitleLabel.setText(player.getName().toUpperCase() + "'S SATCHEL");

        int itemCount = player.getInventory().size();
        int capacity = player.getInventory().getCapacity(); // strictly 4
        inventoryCapacityLabel.setText(itemCount + " / " + capacity + " SLOTS");

        if (player.getInventory().isFull()) {
            inventoryCapacityLabel.setColor(Color.valueOf("#ff6b6b"));
            inventoryLootBtn.setText("FULL (4/4)");
            inventoryLootBtn.setColor(0.6f, 0.6f, 0.6f, 0.5f);
            inventoryLootBtn.setDisabled(true);
        } else {
            inventoryCapacityLabel.setColor(Color.valueOf("#ffd54f"));
            inventoryLootBtn.setText("+ LOOT");
            inventoryLootBtn.setColor(Color.WHITE);
            inventoryLootBtn.setDisabled(false);
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
                    ? Color.valueOf("#69f0ae")  // Luminous mint green
                    : Color.valueOf("#40c4ff"); // Luminous sky cyan
                Label typeLbl = new Label(typeStr, new Label.LabelStyle(miniFont, typeColor));

                Label dotLbl = new Label(" • ", new Label.LabelStyle(miniFont, Color.valueOf("#ffd54f")));
                Label effectLbl = new Label(item.getEffect(), new Label.LabelStyle(miniFont, Color.WHITE));
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

                if (turnState != TurnState.WAITING_FOR_ROLL) {
                    useBtn.setDisabled(true);
                    useBtn.setColor(1f, 1f, 1f, 0.4f);
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
        Player player = game.getCurrentPlayer();
        if (player == null || player.getInventory() == null) return;

        Item item = player.getInventory().getItem(slotIndex);
        if (item == null) return;

        String itemName = item.getName();
        EventEffectType type = item.getEffectType();
        String effectDesc = item.getEffect();

        // Use the item via player
        boolean success = player.useItem(slotIndex);
        if (!success) return;

        // Banner feedback
        topBannerLabel.setText(player.getName().toUpperCase() + " USED " + itemName.toUpperCase() + "! (" + effectDesc + ")");

        // If it was a movement item, advance player
        if (type == EventEffectType.MOVE) {
            int newPos = Math.min(100, Math.max(1, player.getPosition()));
            player.setPosition(newPos);
            updateMiniMapProgress();
            handleTileArrival(player, newPos);
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
        Player player = game.getCurrentPlayer();
        if (player == null || player.getInventory() == null) return;

        if (player.getInventory().isFull()) {
            topBannerLabel.setText("SATCHEL IS FULL! (" + player.getInventory().size() + "/4 SLOTS)");
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
        showPauseMenu();
    }

    private void resumeGame() {
        isPaused = false;
        modalScrim.setVisible(false);
        pauseModalTable.setVisible(false);
        settingsModalTable.setVisible(false);
        leaveConfirmModalTable.setVisible(false);
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

    /**
     * Starts the dice tumbling sequence.
     */
    private void triggerRoll() {
        turnState = TurnState.ROLLING_DICE;
        diceTumbleTimer = 0.55f;
        rollButton.setColor(1f, 1f, 1f, 0.5f);

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
                currentRollValue = 1 + (int) ((stateTime * 15f) % 6);
                diceLabel.setText("ROLL: " + currentRollValue);

                if (diceTumbleTimer <= 0f) {
                    currentRollValue = movementDice.roll();
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
                        turnState = TurnState.TILE_ACTION_PAUSE;
                        actionPauseTimer = 0.8f;
                        handleTileArrival(player, hopTargetTile);
                    }
                }
                break;

            case TILE_ACTION_PAUSE:
                actionPauseTimer -= delta;
                if (actionPauseTimer <= 0f) {
                    if (specialSlideTarget != -1) {
                        player.setPosition(specialSlideTarget);
                        updateMiniMapProgress();
                        specialSlideTarget = -1;
                        actionPauseTimer = 0.7f;
                    } else {
                        if (player.getPosition() >= 100) {
                            topBannerLabel.setText("VICTORY! " + player.getName().toUpperCase() + " CLAIMS THE THRONE OF FATE!");
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
                    updateHudDisplay();
                    rollButton.setColor(Color.WHITE);
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
    private void handleTileArrival(Player player, int tileIndex) {
        Tile tile = board.getTile(tileIndex);
        if (tile == null) return;

        if (tile instanceof SnakeTile) {
            SnakeTile snake = (SnakeTile) tile;
            int dest = Math.max(1, tileIndex - 12);
            specialSlideTarget = dest;
            topBannerLabel.setText("SERPENT AMBUSH ON TILE " + tileIndex + "! RETREATING TO TILE " + dest + "!");
        } else if (tile instanceof LadderTile) {
            LadderTile ladder = (LadderTile) tile;
            int dest = Math.min(100, tileIndex + 14);
            specialSlideTarget = dest;
            topBannerLabel.setText("LADDER ASCENT ON TILE " + tileIndex + "! CLIMBING UP TO TILE " + dest + "!");
        } else {
            topBannerLabel.setText(player.getName().toUpperCase() + " REACHED TILE " + tileIndex + " - SAFE GROUND.");
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

            Texture charTex = game.getAssetManager().get(Assets.getCharacterTexture(player.getCharacterClass()));
            float tileCenterX = getTileCenterX(player.getPosition());

            // If sharing a tile with the active player, offset slightly behind
            boolean sharesWithActive = (player.getPosition() == activePos && turnState != TurnState.STEPPING_TILES);
            float drawX = sharesWithActive ? (tileCenterX - 85f) : (tileCenterX - 60f);
            float drawY = TILE_BOTTOM_Y + 36f;
            float size = sharesWithActive ? 106f : 120f;

            if (sharesWithActive) {
                // Subtle depth tint so the active player in front pops prominently
                batch.setColor(0.85f, 0.85f, 0.9f, 0.80f);
            } else {
                batch.setColor(Color.WHITE);
            }

            batch.draw(charTex, drawX, drawY, size, size);
            batch.setColor(Color.WHITE);
        }

        // 2. Render ACTIVE player LAST (guaranteed on the topmost layer and fully visible)
        if (activePlayer != null) {
            Texture charTex = game.getAssetManager().get(Assets.getCharacterTexture(activePlayer.getCharacterClass()));
            float drawX;
            float drawY = TILE_BOTTOM_Y + 36f;

            if (turnState == TurnState.STEPPING_TILES) {
                float fromX = getTileCenterX(hopCurrentTile);
                float toX = getTileCenterX(Math.min(100, hopCurrentTile + 1));
                float progress = MathUtils.clamp(stepTimer / STEP_DURATION, 0f, 1f);

                drawX = MathUtils.lerp(fromX, toX, progress) - 60f;
                float hopArc = (float) Math.sin(progress * Math.PI) * 55f;
                drawY += hopArc;
            } else {
                drawX = getTileCenterX(activePlayer.getPosition()) - 60f;
            }

            batch.setColor(Color.WHITE);
            batch.draw(charTex, drawX, drawY, 120f, 120f);
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
        hudPlayerStats.setText("ATK  " + player.getAttack() + "    |    DEF  " + player.getDefense());

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
        miniMapProgressLabel.setText(actStr + "   |   TILE " + pos + " / 100");
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
