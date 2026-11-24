package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import java.util.ArrayList;
import java.util.List;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker;
import io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker.PlayerStats;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;
import io.github.snakes_and_ladders_roll_of_fate.ui.UIUtils;

/**
 * Game Summary Screen: Displays an engaging, poetic end-game debrief for both Victory
 * (Throne of Fate claimed at tile 100) and Defeat (party wipe / mortal fall).
 *
 * Features:
 * - Animated walking hero pedestal.
 * - Dynamic bardic chronicle summarizing the expedition's turns, combat, and milestones.
 * - Cooperative accolades spotlighting every companion's unique feat to minimize competition.
 * - Complete expedition ledger in single-player detailing exploration, combat, fortune, and satchel.
 * - Fully wrapped text formatting preventing any text overflow.
 */
public class GameSummaryScreen implements Screen {

    private final RollOfFateGame game;
    private final boolean isVictory;
    private final Player hero;
    private final List<Player> party;

    private SpriteBatch batch;
    private Stage stage;

    // Background texture & Dim overlay
    private Texture bgTexture;
    private Drawable darkOverlay;

    // Fonts
    private BitmapFont headerTitleFont;
    private BitmapFont headerSubtitleFont;
    private BitmapFont sectionTitleFont;
    private BitmapFont cardTitleFont;
    private BitmapFont heroNameFont;
    private BitmapFont accoladeFont;
    private BitmapFont bodyFont;
    private BitmapFont statLabelFont;
    private BitmapFont statValueFont;
    private BitmapFont buttonFont;
    private BitmapFont chipTitleFont;
    private BitmapFont chipValueFont;
    private BitmapFont ledgerKeyFont;
    private BitmapFont ledgerValFont;

    // Animated Hero Pedestal
    private Animation<TextureRegion> heroWalkAnim;
    private Image heroImage;
    private float animTimer = 0f;

    public GameSummaryScreen(RollOfFateGame game, boolean isVictory, Player hero) {
        this.game = game;
        this.isVictory = isVictory;
        this.party = (game.getPlayers() != null) ? new ArrayList<>(game.getPlayers()) : new ArrayList<>();

        if (hero != null) {
            this.hero = hero;
        } else if (game.getCurrentPlayer() != null) {
            this.hero = game.getCurrentPlayer();
        } else if (!party.isEmpty()) {
            this.hero = party.get(0);
        } else {
            this.hero = null;
        }

        // Compute accolades across all party members
        if (game.getRunTracker() != null) {
            game.getRunTracker().computeAccolades(this.party);
        }
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        stage = new Stage(new ExtendViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);

        // 1. Background Setup
        bgTexture = game.getAssetManager().get(isVictory ? Assets.ACT1_BG : Assets.ACT3_BG);
        Color overlayColor = isVictory ? Color.valueOf("#120b06ee") : Color.valueOf("#160808f2");
        darkOverlay = UIUtils.createRoundedDrawable(32, 32, 0, overlayColor);

        // 2. Initialize Crisp Sniglet Fonts
        Color goldColor = Color.valueOf("#ffd54f");
        Color softCrimson = Color.valueOf("#ff8a80");
        Color warmIvory = Color.valueOf("#fbf2dc");
        Color dimText = Color.valueOf("#cfd8dc");

        headerTitleFont    = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 40, isVictory ? goldColor : softCrimson);
        headerSubtitleFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 22, warmIvory);
        sectionTitleFont   = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 26, isVictory ? goldColor : softCrimson);
        cardTitleFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 24, warmIvory);
        heroNameFont       = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 30, Color.WHITE);
        accoladeFont       = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 21, goldColor);
        bodyFont           = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 20, warmIvory);
        statLabelFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 19, dimText);
        statValueFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 20, isVictory ? goldColor : Color.valueOf("#ffb74d"));
        buttonFont         = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 26, Color.WHITE);
        chipTitleFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 14, Color.valueOf("#b0bec5"));
        chipValueFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 17, isVictory ? goldColor : Color.valueOf("#ffb74d"));
        ledgerKeyFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 18, Color.valueOf("#e0e0e0"));
        ledgerValFont      = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 18, isVictory ? goldColor : Color.valueOf("#ffb74d"));

        // 3. Setup Animated Walking Hero Pedestal
        CharacterClass heroClass = (hero != null) ? hero.getCharacterClass() : CharacterClass.CAT;
        if (heroClass == null) heroClass = CharacterClass.CAT;

        Texture walkSheet = game.getAssetManager().get(Assets.getCharacterWalkSheet(heroClass));
        int frameCount = Assets.getCharacterWalkFrameCount(heroClass);
        TextureRegion[][] grid = TextureRegion.split(walkSheet, Assets.CHAR_FRAME_SIZE, Assets.CHAR_FRAME_SIZE);
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = grid[0][i];
        }
        heroWalkAnim = new Animation<>(0.18f, frames);
        heroWalkAnim.setPlayMode(Animation.PlayMode.LOOP);
        heroImage = new Image(new TextureRegionDrawable(frames[0]));
        heroImage.setScaling(Scaling.fit);

        // 4. Construct Root Layout Table (1920x1080 canvas)
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(20, 40, 20, 40);

        // --- TOP RIBBON ---
        Table ribbonTable = buildTopRibbon();
        rootTable.add(ribbonTable).width(1840).height(88).padBottom(16).row();

        // --- MAIN BODY (TWO COLUMNS) ---
        Table bodyTable = new Table();

        // Left Column: Hero Spotlight + Bardic Chronicle
        Table leftCol = buildLeftColumn();
        bodyTable.add(leftCol).width(570).height(820).top().padRight(30);

        // Right Column: Companion Highlights (Multiplayer) or Expedition Ledger (Single Player)
        Table rightCol = buildRightColumn();
        bodyTable.add(rightCol).width(1240).height(820).top();

        rootTable.add(bodyTable).width(1840).height(820).row();

        // --- BOTTOM ACTION ROW ---
        Table actionRow = buildBottomActions();
        rootTable.add(actionRow).width(1840).height(80).padTop(12);

        stage.addActor(rootTable);
    }

    /**
     * Builds the top celebratory/solemn header ribbon.
     */
    private Table buildTopRibbon() {
        Color bannerBg = isVictory ? Color.valueOf("#6b3f11e6") : Color.valueOf("#541616e6");
        Color bannerBorder = isVictory ? Color.valueOf("#ffd54f") : Color.valueOf("#ff5252");
        Drawable bannerDrawable = UIUtils.createRoundedDrawable(1840, 88, 16, bannerBg, bannerBorder, 3);

        Table ribbon = new Table();
        ribbon.setBackground(bannerDrawable);
        ribbon.pad(6, 24, 6, 24);

        String mainTitle = isVictory
            ? "VICTORY! - THE THRONE OF FATE IS CLAIMED"
            : "DEFEAT - THE CYCLE CLAIMS ANOTHER SOUL";

        String subTitle = isVictory
            ? "The arduous pilgrimage reaches its legendary summit! Honor and songs to the valiant travelers!"
            : "The perilous wilds claim the fallen, yet songs of their defiance echo through the stones of eternity.";

        Label titleLbl = new Label(mainTitle, new Label.LabelStyle(headerTitleFont, headerTitleFont.getColor()));
        titleLbl.setAlignment(Align.center);

        Label subLbl = new Label(subTitle, new Label.LabelStyle(headerSubtitleFont, headerSubtitleFont.getColor()));
        subLbl.setAlignment(Align.center);

        ribbon.add(titleLbl).center().row();
        ribbon.add(subLbl).center().padTop(2);

        return ribbon;
    }

    /**
     * Builds the left column containing the Hero Pedestal card and Bardic Chronicle card.
     */
    private Table buildLeftColumn() {
        Table col = new Table();

        // 1. Hero Pedestal Card
        Color cardBg = isVictory ? Color.valueOf("#26140cee") : Color.valueOf("#261010ee");
        Color cardBorder = isVictory ? Color.valueOf("#854b25") : Color.valueOf("#8a2424");
        Drawable pedestalDrawable = UIUtils.createRoundedDrawable(570, 360, 14, cardBg, cardBorder, 2);

        Table pedestalTable = new Table();
        pedestalTable.setBackground(pedestalDrawable);
        pedestalTable.pad(12, 16, 12, 16);

        // Accolade Pill at top of Pedestal
        String heroAccolade = "THE DAUNTLESS VOYAGER";
        if (game.getRunTracker() != null && hero != null) {
            PlayerStats ps = game.getRunTracker().getOrCreateStats(hero);
            if (ps != null && ps.getAccoladeTitle() != null) {
                heroAccolade = ps.getAccoladeTitle();
            }
        }
        if (isVictory) {
            heroAccolade = "THE FATE CHAMPION";
        } else if (hero != null && hero.getHealth() <= 0) {
            heroAccolade = "THE FALLEN HERO";
        }

        Drawable pillBg = UIUtils.createRoundedDrawable(340, 36, 10, Color.valueOf("#854b25"), Color.valueOf("#ffd54f"), 2);
        Table pillTable = new Table();
        pillTable.setBackground(pillBg);
        Label pillLabel = new Label(heroAccolade, new Label.LabelStyle(accoladeFont, Color.valueOf("#ffd54f")));
        pillLabel.setAlignment(Align.center);
        pillTable.add(pillLabel).pad(4, 16, 4, 16);
        pedestalTable.add(pillTable).center().padBottom(6).row();

        // Animated Walking Hero Sprite
        pedestalTable.add(heroImage).size(150, 150).center().padBottom(6).row();

        // Hero Name & Class
        String hName = (hero != null) ? hero.getName().toUpperCase() : "PLAYER 1";
        String hClass = (hero != null && hero.getCharacterClass() != null) ? hero.getCharacterClass().getTitle() : "Adventurer";
        int hLevel = (hero != null) ? hero.getLevel() : 1;

        Label nameLabel = new Label(hName, new Label.LabelStyle(heroNameFont, Color.WHITE));
        nameLabel.setAlignment(Align.center);
        pedestalTable.add(nameLabel).center().row();

        Label classLabel = new Label(hClass + "  |  Level " + hLevel, new Label.LabelStyle(bodyFont, Color.valueOf("#fbf2dc")));
        classLabel.setAlignment(Align.center);
        pedestalTable.add(classLabel).center().padTop(2);

        col.add(pedestalTable).width(570).height(355).padBottom(15).row();

        // 2. Bardic Narrative Card
        Drawable chronicleDrawable = UIUtils.createRoundedDrawable(570, 450, 14, cardBg, cardBorder, 2);
        Table chronicleTable = new Table();
        chronicleTable.setBackground(chronicleDrawable);
        chronicleTable.pad(16, 20, 16, 20);

        Label chronicleHeader = new Label("CHRONICLE OF DESTINY", new Label.LabelStyle(sectionTitleFont, sectionTitleFont.getColor()));
        chronicleHeader.setAlignment(Align.center);
        chronicleTable.add(chronicleHeader).center().padBottom(10).row();

        String narrativeText = "The bards assemble to record the deeds of courage across the stones...";
        if (game.getRunTracker() != null) {
            narrativeText = game.getRunTracker().generateBardicNarrative(isVictory, hero, party);
        }

        Label narrativeLabel = new Label(narrativeText, new Label.LabelStyle(bodyFont, Color.valueOf("#fbf2dc")));
        narrativeLabel.setWrap(true);
        narrativeLabel.setAlignment(Align.topLeft);

        // Constrain width strictly to 520px so text wraps naturally across 6-10 lines
        chronicleTable.add(narrativeLabel).width(520).top().left().expand().fillY();

        col.add(chronicleTable).width(570).height(450);

        return col;
    }

    /**
     * Builds the right column:
     * - In Multiplayer: 2x2 grid of Companion Highlight Cards with unique positive accolades.
     * - In Single Player: 2x2 grid of detailed Expedition Ledger Cards.
     */
    private Table buildRightColumn() {
        Table col = new Table();

        if (party.size() > 1) {
            // --- MULTIPLAYER COOPERATIVE HIGHLIGHTS ---
            Label sectionLbl = new Label("PARTY HONORS & EXPLOITS", new Label.LabelStyle(sectionTitleFont, sectionTitleFont.getColor()));
            Label subLbl = new Label("Spotlighting every companion's valor and pivotal feats across the wild", new Label.LabelStyle(headerSubtitleFont, Color.valueOf("#cfd8dc")));
            col.add(sectionLbl).left().row();
            col.add(subLbl).left().padBottom(12).row();

            Table gridTable = new Table();
            int index = 0;
            for (Player p : party) {
                Table pCard = buildPlayerAccoladeCard(p);
                gridTable.add(pCard).width(605).height(350).pad(6);

                index++;
                if (index % 2 == 0) {
                    gridTable.row();
                }
            }
            col.add(gridTable).width(1240).top().expandY();
        } else {
            // --- SINGLE PLAYER EXPEDITION LEDGER ---
            Label sectionLbl = new Label("EXPEDITION RECORD & LEDGER", new Label.LabelStyle(sectionTitleFont, sectionTitleFont.getColor()));
            Label subLbl = new Label("A comprehensive accounting of trials, combat, and fortune across the stones", new Label.LabelStyle(headerSubtitleFont, Color.valueOf("#cfd8dc")));
            col.add(sectionLbl).left().row();
            col.add(subLbl).left().padBottom(12).row();

            Table gridTable = new Table();
            PlayerStats stats = (game.getRunTracker() != null && hero != null)
                ? game.getRunTracker().getOrCreateStats(hero)
                : new PlayerStats(hero);

            // 4 Quadrants
            Table card1 = buildLedgerCard("TERRAIN & EXPLORATION", Color.valueOf("#4caf50"), new String[][]{
                {"Highest Stone:", stats.getHighestTile() + " / 100"},
                {"Stepping Stones:", stats.getTilesMoved() + " Steps Traversed"},
                {"Ladders Scaled:", stats.getLaddersClimbed() + " (" + stats.getLadderTilesGained() + " Vertical Tiles)"},
                {"Serpent Hazards:", stats.getSnakesEncountered() + " (" + stats.getSnakeTilesLost() + " Tiles Lost)"}
            });

            Table card2 = buildLedgerCard("COMBAT & COMBAT VALOR", Color.valueOf("#e53935"), new String[][]{
                {"Serpents Slain:", stats.getSnakesSlain() + " Slain in Combat"},
                {"Damage Inflicted:", stats.getCombatDamageDealt() + " Damage Dealt"},
                {"Damage Absorbed:", stats.getCombatDamageTaken() + " Damage Absorbed"},
                {"D20 Critical Hits:", stats.getNatural20s() + " Natural 20 Strikes"}
            });

            Table card3 = buildLedgerCard("FORTUNE & THE DICE", Color.valueOf("#ffb300"), new String[][]{
                {"Dice Rolls:", stats.getTotalDiceRolls() + " D6 Tumblings"},
                {"Sixes Rolled:", stats.getSixesRolled() + " Max Sixes Rolled"},
                {"Tile Dilemmas:", stats.getEventsResolved() + " Ambient Encounters"},
                {"Expedition Turns:", stats.getTotalTurns() + " Turns on Road"}
            });

            Table card4 = buildLedgerCard("SATCHEL & SURVIVAL", Color.valueOf("#00acc1"), new String[][]{
                {"Spoils Gathered:", stats.getItemsCollected() + " Relics Carried"},
                {"Potions Ingested:", stats.getItemsUsed() + " Consumed in Need"},
                {"Final Vitality:", (hero != null ? hero.getHealth() : 0) + " / " + (hero != null ? hero.getMaxHealth() : 100) + " HP"},
                {"Expedition Status:", isVictory ? "Throne Claimed!" : "Fallen in Peril"}
            });

            gridTable.add(card1).width(605).height(350).pad(6);
            gridTable.add(card2).width(605).height(350).pad(6).row();
            gridTable.add(card3).width(605).height(350).pad(6);
            gridTable.add(card4).width(605).height(350).pad(6);

            col.add(gridTable).width(1240).top().expandY();
        }

        return col;
    }

    /**
     * Builds a single player's highlight accolade card for multiplayer.
     */
    private Table buildPlayerAccoladeCard(Player p) {
        Color cardBg = isVictory ? Color.valueOf("#26140cee") : Color.valueOf("#261010ee");
        Color cardBorder = isVictory ? Color.valueOf("#854b25") : Color.valueOf("#8a2424");
        Drawable cardDrawable = UIUtils.createRoundedDrawable(605, 350, 14, cardBg, cardBorder, 2);

        Table card = new Table();
        card.setBackground(cardDrawable);
        card.pad(14, 18, 14, 18);

        PlayerStats stats = (game.getRunTracker() != null) ? game.getRunTracker().getOrCreateStats(p) : new PlayerStats(p);

        // Top Row: Player Avatar + Name + Class
        Table topRow = new Table();
        CharacterClass pClass = p.getCharacterClass() != null ? p.getCharacterClass() : CharacterClass.CAT;
        Texture walkSheet = game.getAssetManager().get(Assets.getCharacterWalkSheet(pClass));
        TextureRegion[][] grid = TextureRegion.split(walkSheet, Assets.CHAR_FRAME_SIZE, Assets.CHAR_FRAME_SIZE);
        Image avatar = new Image(grid[0][0]);
        avatar.setScaling(Scaling.fit);
        topRow.add(avatar).size(68, 68).padRight(14);

        Table nameTable = new Table();
        nameTable.left();
        Label nameLbl = new Label(p.getName().toUpperCase(), new Label.LabelStyle(cardTitleFont, Color.WHITE));
        Label classLbl = new Label(pClass.getTitle() + "  |  Level " + p.getLevel(), new Label.LabelStyle(statLabelFont, Color.valueOf("#fbf2dc")));
        nameTable.add(nameLbl).left().row();
        nameTable.add(classLbl).left();
        topRow.add(nameTable).left().expandX();

        card.add(topRow).width(565).padBottom(10).row();

        // Accolade Badge Pill
        Drawable pillBg = UIUtils.createRoundedDrawable(565, 36, 10, Color.valueOf("#6b3f11"), Color.valueOf("#ffd54f"), 2);
        Table pillTable = new Table();
        pillTable.setBackground(pillBg);
        String title = (stats != null) ? stats.getAccoladeTitle() : "THE DAUNTLESS VOYAGER";
        Label pillLabel = new Label(title, new Label.LabelStyle(accoladeFont, Color.valueOf("#ffd54f")));
        pillLabel.setAlignment(Align.center);
        pillTable.add(pillLabel).pad(4, 12, 4, 12);
        card.add(pillTable).width(565).height(36).padBottom(8).row();

        // Accolade Narrative Description (Strictly wrapped)
        String desc = (stats != null) ? stats.getAccoladeDescription() : "Traversed the perilous stones with steadfast courage.";
        Label descLabel = new Label(desc, new Label.LabelStyle(bodyFont, Color.valueOf("#fbf2dc")));
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.topLeft);
        card.add(descLabel).width(565).height(55).top().left().padBottom(10).row();

        // 4 Stat Chips (2x2 grid, vertically stacked label & value to prevent crowding)
        Table chipsTable = new Table();
        chipsTable.add(buildStatChip("Ladders Scaled", stats != null ? (stats.getLaddersClimbed() + " (" + stats.getLadderTilesGained() + " Tiles)") : "0")).width(275).pad(3);
        chipsTable.add(buildStatChip("Serpents Slain", stats != null ? (stats.getSnakesSlain() + " (" + stats.getCombatDamageDealt() + " Dmg)") : "0")).width(275).pad(3).row();
        chipsTable.add(buildStatChip("Dice of Fate", stats != null ? (stats.getTotalDiceRolls() + " (" + stats.getSixesRolled() + " Sixes)") : "0")).width(275).pad(3);
        chipsTable.add(buildStatChip("Satchel Relics", stats != null ? (stats.getItemsUsed() + " Used / " + stats.getItemsCollected() + " Found") : "0")).width(275).pad(3);

        card.add(chipsTable).width(565).height(95).top();

        return card;
    }

    /**
     * Builds a single stat chip for multiplayer cards (vertical stack).
     */
    private Table buildStatChip(String label, String value) {
        Table chip = new Table();
        Drawable chipBg = UIUtils.createRoundedDrawable(275, 44, 8, Color.valueOf("#1a0d07cc"), Color.valueOf("#54280e"), 1);
        chip.setBackground(chipBg);
        chip.pad(3, 10, 3, 10);

        Label lbl = new Label(label.toUpperCase(), new Label.LabelStyle(chipTitleFont, chipTitleFont.getColor()));
        Label val = new Label(value, new Label.LabelStyle(chipValueFont, chipValueFont.getColor()));
        lbl.setAlignment(Align.left);
        val.setAlignment(Align.left);

        chip.add(lbl).left().expandX().row();
        chip.add(val).left().expandX();
        return chip;
    }

    /**
     * Builds a single category card for the single-player ledger.
     */
    private Table buildLedgerCard(String categoryTitle, Color headerAccent, String[][] statRows) {
        Color cardBg = isVictory ? Color.valueOf("#26140cee") : Color.valueOf("#261010ee");
        Color cardBorder = isVictory ? Color.valueOf("#854b25") : Color.valueOf("#8a2424");
        Drawable cardDrawable = UIUtils.createRoundedDrawable(605, 350, 14, cardBg, cardBorder, 2);

        Table card = new Table();
        card.setBackground(cardDrawable);
        card.pad(16, 20, 16, 20);

        // Header
        Label titleLbl = new Label(categoryTitle, new Label.LabelStyle(cardTitleFont, headerAccent));
        titleLbl.setAlignment(Align.left);
        card.add(titleLbl).left().padBottom(12).row();

        // Stat Rows (fixed width columns to guarantee zero overflow)
        Table rowsTable = new Table();
        for (String[] rowData : statRows) {
            Table row = new Table();
            Drawable rowBg = UIUtils.createRoundedDrawable(565, 52, 8, Color.valueOf("#1a0d07bb"), Color.valueOf("#45200b"), 1);
            row.setBackground(rowBg);
            row.pad(6, 12, 6, 12);

            Label keyLbl = new Label(rowData[0], new Label.LabelStyle(ledgerKeyFont, ledgerKeyFont.getColor()));
            Label valLbl = new Label(rowData[1], new Label.LabelStyle(ledgerValFont, ledgerValFont.getColor()));
            keyLbl.setAlignment(Align.left);
            valLbl.setAlignment(Align.right);

            row.add(keyLbl).width(280).left();
            row.add(valLbl).width(250).right();

            rowsTable.add(row).width(565).padBottom(8).row();
        }

        card.add(rowsTable).width(565).top().expandY();
        return card;
    }

    /**
     * Builds the bottom action bar containing PLAY AGAIN and MAIN MENU buttons.
     */
    private Table buildBottomActions() {
        Table table = new Table();

        // 1. PLAY AGAIN Button
        TextButton.TextButtonStyle playAgainStyle = new TextButton.TextButtonStyle();
        playAgainStyle.up = UIUtils.createRoundedDrawable(320, 64, 14, Color.valueOf("#2e7d32"), Color.valueOf("#1b5e20"), 2);
        playAgainStyle.over = UIUtils.createRoundedDrawable(320, 64, 14, Color.valueOf("#388e3c"), Color.valueOf("#2e7d32"), 2);
        playAgainStyle.font = buttonFont;
        playAgainStyle.fontColor = Color.WHITE;

        TextButton playAgainBtn = new TextButton("PLAY AGAIN", playAgainStyle);
        UIUtils.addScaleHoverEffect(playAgainBtn);
        playAgainBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getLastChosenClasses() != null && !game.getLastChosenClasses().isEmpty()) {
                    game.startGameWithClasses(game.getLastChosenClasses());
                } else {
                    game.startGame();
                }
            }
        });

        // 2. MAIN MENU Button
        TextButton.TextButtonStyle menuStyle = new TextButton.TextButtonStyle();
        menuStyle.up = UIUtils.createRoundedDrawable(320, 64, 14, Color.valueOf("#854b25"), Color.valueOf("#54280e"), 2);
        menuStyle.over = UIUtils.createRoundedDrawable(320, 64, 14, Color.valueOf("#a66032"), Color.valueOf("#854b25"), 2);
        menuStyle.font = buttonFont;
        menuStyle.fontColor = Color.WHITE;

        TextButton menuBtn = new TextButton("MAIN MENU", menuStyle);
        UIUtils.addScaleHoverEffect(menuBtn);
        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        table.add(playAgainBtn).size(320, 64).padRight(40);
        table.add(menuBtn).size(320, 64);

        return table;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.04f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        animTimer += delta;

        // Update animated hero pedestal sprite frame
        if (heroWalkAnim != null && heroImage != null) {
            heroImage.setDrawable(new TextureRegionDrawable(heroWalkAnim.getKeyFrame(animTimer)));
        }

        // Draw background and dim scrim
        batch.begin();
        if (bgTexture != null) {
            batch.draw(bgTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (batch != null) batch.dispose();

        // Dispose custom FreeType fonts
        if (headerTitleFont != null) headerTitleFont.dispose();
        if (headerSubtitleFont != null) headerSubtitleFont.dispose();
        if (sectionTitleFont != null) sectionTitleFont.dispose();
        if (cardTitleFont != null) cardTitleFont.dispose();
        if (heroNameFont != null) heroNameFont.dispose();
        if (accoladeFont != null) accoladeFont.dispose();
        if (bodyFont != null) bodyFont.dispose();
        if (statLabelFont != null) statLabelFont.dispose();
        if (statValueFont != null) statValueFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (chipTitleFont != null) chipTitleFont.dispose();
        if (chipValueFont != null) chipValueFont.dispose();
        if (ledgerKeyFont != null) ledgerKeyFont.dispose();
        if (ledgerValFont != null) ledgerValFont.dispose();
    }
}
