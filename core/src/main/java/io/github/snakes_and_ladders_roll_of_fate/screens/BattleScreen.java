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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleState;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleOutcome;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;
import io.github.snakes_and_ladders_roll_of_fate.ui.UIUtils;

import java.util.HashMap;
import java.util.Map;

public class BattleScreen implements Screen {
    private final RollOfFateGame game;
    private final Player player;
    private final SnakeTile snakeTile;
    private final Enemy enemy;
    private final BattleSystem battleSystem;

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

    // Entity Positions (Centered on their respective tiles)
    private static final float PLAYER_X = 600f;
    private static final float ENEMY_X = 1300f;

    // Fonts (MATCHING BoardScreen Hierarchy)
    private BitmapFont titleFont, topBannerFont, hudHpFont, battleLogFont, playerTagFont, miniFont;

    // UI Elements
    private Label topBannerLabel, battleLogLabel, playerHpLabel, enemyHpLabel;
    private TextButton btnAttack, btnDefend, btnItem, btnEscape, continueBtn;
    private Table itemPopup;
    private Table continueTable;

    // Textures & Animations
    private Texture enemyTex;
    private Animation<TextureRegion> playerIdleAnim;
    private float stateTime = 0f;
    private boolean outcomeResolved = false;
    private String outcomeMessage = "";

    public BattleScreen(RollOfFateGame game, Player player, SnakeTile snakeTile) {
        this.game = game;
        this.player = player;
        this.snakeTile = snakeTile;
        this.enemy = snakeTile.createSnakeEnemy(); // Factory method from SnakeTile
        this.battleSystem = new BattleSystem();
    }

    @Override
    public void show() {
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

        // Load Fonts (Exact match to BoardScreen)
        titleFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 26, Color.WHITE);
        topBannerFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 32, Color.valueOf("#fffdfa"));
        hudHpFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 22, Color.valueOf("#a81717"));
        battleLogFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 26, Color.WHITE);
        playerTagFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 19, Color.WHITE);
        miniFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 16, Color.WHITE);

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

        // Load Enemy Texture (Placeholder until snake asset is added)
        enemyTex = game.getAssetManager().get(Assets.CHAR_TURTLE); // Replace with Assets.CHAR_SNAKE later

        // Start Battle
        battleSystem.startBattle(player, enemy);

        // Build UI
        buildTopBanner();
        buildBattleLog();
        buildHealthPills();
        buildActionButtons();
        buildItemPopup();
        buildContinueOverlay();
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
        topBannerLabel = new Label("⚔️ AMBUSH: " + enemy.getEnemyType().toUpperCase() + "! ⚔️",
            new Label.LabelStyle(topBannerFont, Color.valueOf("#fffdfa")));
        topBannerLabel.setAlignment(Align.center);
        Table banner = new Table();
        banner.setBackground(UIUtils.createRoundedDrawable(920, 70, 20, Color.valueOf("#854b25")));
        banner.add(topBannerLabel).pad(8, 36, 8, 36);
        Table outer = new Table();
        outer.setFillParent(true);
        outer.top().padTop(22);
        outer.add(banner).center();
        hudStage.addActor(outer);
    }

    private void buildBattleLog() {
        battleLogLabel = new Label("Battle Start!", new Label.LabelStyle(battleLogFont, Color.WHITE));
        battleLogLabel.setAlignment(Align.center);
        battleLogLabel.setWrap(true);
        Table logBox = new Table();
        logBox.setBackground(UIUtils.createRoundedDrawable(860, 120, 15, new Color(0f, 0f, 0f, 0.6f)));
        logBox.add(battleLogLabel).pad(16, 40, 16, 40).width(780);
        Table container = new Table();
        container.setFillParent(true);
        container.center();
        container.add(logBox).center();
        hudStage.addActor(container);
    }

    private void buildHealthPills() {
        // Player HP (Bottom Left)
        int maxHp = player.getCharacterClass() != null ? player.getCharacterClass().getBaseHealth() : 100;
        playerHpLabel = new Label("HP: " + player.getHealth() + " / " + maxHp, new Label.LabelStyle(hudHpFont, Color.valueOf("#a81717")));
        Table pPill = new Table();
        pPill.setBackground(UIUtils.createRoundedDrawable(320, 60, 10, Color.valueOf("#c89259")));
        pPill.add(playerHpLabel).pad(10, 20, 10, 20);
        Table pWrap = new Table();
        pWrap.setFillParent(true);
        pWrap.bottom().left().padBottom(160).padLeft(50);
        pWrap.add(pPill).size(320, 60);
        hudStage.addActor(pWrap);

        // Enemy HP (Top Right)
        enemyHpLabel = new Label(enemy.getEnemyType() + " HP: " + enemy.getHealth(), new Label.LabelStyle(hudHpFont, Color.valueOf("#a81717")));
        Table ePill = new Table();
        ePill.setBackground(UIUtils.createRoundedDrawable(420, 60, 10, Color.valueOf("#5a2d0c")));
        ePill.add(enemyHpLabel).pad(10, 20, 10, 20);
        Table eWrap = new Table();
        eWrap.setFillParent(true);
        eWrap.top().right().padTop(120).padRight(50);
        eWrap.add(ePill).size(420, 60);
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
            @Override public void changed(ChangeEvent e, Actor a) { battleSystem.onPlayerAttack(); syncUi(); }
        });
        btnDefend.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { battleSystem.onPlayerDefend(); syncUi(); }
        });
        btnEscape.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) { battleSystem.onPlayerEscape(); syncUi(); }
        });
        btnItem.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                // Only open if it's the player's turn
                if (battleSystem.getCurrentState() == BattleState.PLAYER_TURN) {
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
        bottomBar.add(btnAttack).size(200, 100).padRight(20);
        bottomBar.add(btnDefend).size(200, 100).padRight(20);
        bottomBar.add(btnItem).size(200, 100).padRight(20);
        bottomBar.add(btnEscape).size(200, 100);
        hudStage.addActor(bottomBar);
    }

    private void buildContinueOverlay() {
        continueTable = new Table();
        continueTable.setFillParent(true);
        continueTable.setBackground(UIUtils.createRoundedDrawable(1920, 1080, 0, new Color(0f, 0f, 0f, 0.35f)));
        continueTable.setVisible(false);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.up = UIUtils.createRoundedDrawable(320, 80, 16, Color.valueOf("#2e7d32"));
        style.font = titleFont;
        style.fontColor = Color.WHITE;
        continueBtn = new TextButton("CONTINUE", style);
        continueBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a) {
                game.setScreen(new BoardScreen(game));
            }
        });
        continueTable.add(continueBtn).size(320, 80).center();
        hudStage.addActor(continueTable);
    }

    private void syncUi() {
        BattleState state = battleSystem.getCurrentState();
        boolean playerTurn = (state == BattleState.PLAYER_TURN);
        boolean ended = (state == BattleState.BATTLE_END);

        // Update Text
        battleLogLabel.setText(ended ? outcomeMessage : battleSystem.getBattleLog());

        int maxHp = player.getCharacterClass() != null ? player.getCharacterClass().getBaseHealth() : 100;
        playerHpLabel.setText("HP: " + player.getHealth() + " / " + maxHp);
        enemyHpLabel.setText(enemy.getEnemyType() + " HP: " + enemy.getHealth());

        // Lock buttons during enemy turn or end
        btnAttack.setDisabled(!playerTurn);
        btnDefend.setDisabled(!playerTurn);
        btnItem.setDisabled(!playerTurn);
        btnEscape.setDisabled(!playerTurn);

        // Dim disabled buttons
        float alpha = playerTurn ? 1f : 0.5f;
        btnAttack.setColor(1, 1, 1, alpha);
        btnDefend.setColor(1, 1, 1, alpha);
        btnItem.setColor(1, 1, 1, alpha);
        btnEscape.setColor(1, 1, 1, alpha);

        // Handle End State
        if (ended && !outcomeResolved) {
            outcomeResolved = true;
            BattleOutcome o = battleSystem.getFinalOutcome();
            outcomeMessage = snakeTile.resolveOutcome(player, enemy, o); // Apply EXP/Loot/Slide

            if (o == BattleOutcome.VICTORY) topBannerLabel.setText("🏆 VICTORY! 🏆");
            else if (o == BattleOutcome.DEFEAT) topBannerLabel.setText("💀 DEFEAT 💀");
            else topBannerLabel.setText("⚠️ RETREAT! ⚠️");

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

            // Item Name
            Label nameLbl = new Label(item.getName(), new Label.LabelStyle(titleFont, Color.WHITE));
            row.add(nameLbl).expandX().left().padRight(10);

            // USE Button
            TextButton.TextButtonStyle useStyle = new TextButton.TextButtonStyle();
            useStyle.up = UIUtils.createRoundedDrawable(90, 40, 10, Color.valueOf("#2e7d32"));
            useStyle.font = titleFont;
            useStyle.fontColor = Color.WHITE;

            TextButton useBtn = new TextButton("USE", useStyle);

            // THIS IS THE KEY PART: Pass the specific slotIndex to the BattleSystem
            useBtn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                    battleSystem.onPlayerUseItem(slotIndex);
                    itemPopup.setVisible(false); // Close popup after using
                    syncUi(); // Refresh HP/Log
                }
            });

            row.add(useBtn).size(90, 40).right();
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
                itemPopup.setVisible(false);
            }
        });
        itemPopup.add(closeBtn).size(140, 44).padTop(16).center().row();
    }

    @Override
    public void render(float delta) {
        stateTime += delta;
        int tile = snakeTile.getPosition();

        // Sky Color
        Color sky = (tile <= 30) ? Color.valueOf("#42828a") : ((tile <= 70) ? Color.valueOf("#183864") : Color.valueOf("#4a1424"));
        Gdx.gl.glClearColor(sky.r, sky.g, sky.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // World Render
        worldViewport.apply();
        batch.setProjectionMatrix(worldCamera.combined);
        batch.begin();
        renderParallaxBackground(tile);
        renderArena();
        batch.end();

        // HUD Render
        hudStage.getViewport().apply();
        hudStage.act(delta);
        hudStage.draw();

        syncUi(); // Keep UI in sync with state machine
    }

    private void renderArena() {
        Texture stoneNormal = game.getAssetManager().get(Assets.TILE_STONE_NORMAL);
        Texture stoneSnake = game.getAssetManager().get(Assets.TILE_STONE_SNAKE);

        // Draw Tiles
        batch.draw(stoneNormal, PLAYER_X - TILE_WIDTH/2, TILE_BOTTOM_Y, TILE_WIDTH, TILE_HEIGHT);
        batch.draw(stoneSnake, ENEMY_X - TILE_WIDTH/2, TILE_BOTTOM_Y, TILE_WIDTH, TILE_HEIGHT);

        // Draw Player (Idle Bobbing Animation)
        TextureRegion pFrame = playerIdleAnim != null ? playerIdleAnim.getKeyFrame(stateTime, true) : null;
        if (pFrame != null) {
            float bob = MathUtils.sin(stateTime * 3f) * 4f;
            batch.draw(pFrame, PLAYER_X - CHAR_DISPLAY_SIZE/2, CHAR_DRAW_Y + bob, CHAR_DISPLAY_SIZE, CHAR_DISPLAY_SIZE);
        }

        // Draw Enemy (Idle Bobbing)
        float eBob = MathUtils.sin(stateTime * 3f + 1f) * 4f;
        batch.draw(enemyTex, ENEMY_X - CHAR_DISPLAY_SIZE/2, CHAR_DRAW_Y + eBob, CHAR_DISPLAY_SIZE, CHAR_DISPLAY_SIZE);
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
    }
}
