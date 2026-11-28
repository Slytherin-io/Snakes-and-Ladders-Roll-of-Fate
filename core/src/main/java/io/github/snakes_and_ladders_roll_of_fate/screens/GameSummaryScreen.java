package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
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
import io.github.snakes_and_ladders_roll_of_fate.audio.AudioManager;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker.EndingType;
import io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker.PlayerStats;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;
import io.github.snakes_and_ladders_roll_of_fate.ui.UIUtils;

/**
 * Decluttered End-Game Victory & Defeat Conclusion Screen with on-demand summary toggle.
 *
 * Implements a clean two-layer architecture:
 * 1. Layer 1 (Default): Celebratory/Solemn Podium focusing on the animated hero pedestal,
 *    3 primary glanceable highlight metrics (Turns on Road, Serpents Slain, Ladders Scaled),
 *    and clean companion recognition in multiplayer—completely free of text walls.
 * 2. Layer 2 (Toggle): Complete Expedition Record & Bardic Chronicle showing the full
 *    dynamically generated bardic prose, detailed 4-quadrant ledger, and companion feats.
 */
public class GameSummaryScreen implements Screen {

    private final RollOfFateGame game;
    private final boolean isVictory;
    private final EndingType endingType;
    private final Player hero;
    private final List<Player> party;

    private SpriteBatch batch;
    private Stage stage;

    // Background parallax layers & textures
    private Texture bgTexture;
    private TextureRegion farSlice;
    private TextureRegion midSlice;
    private TextureRegion groundSlice;
    private Texture whitePixelTex;

    // View Switching & Toggle State
    private boolean showingSummaryDetails = false;
    private Table outcomeContainer;
    private Table summaryContainer;
    private Label ribbonTitleLabel;
    private Label ribbonSubtitleLabel;
    private TextButton toggleViewBtn;
    private final List<TextButton> bottomActionFocusables = new ArrayList<>();
    private int bottomActionFocusIdx = 0;

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
    private BitmapFont pillMetricFont;
    private BitmapFont pillLabelFont;

    // Animated Hero Pedestal
    private Animation<TextureRegion> heroWalkAnim;
    private Image heroImage;
    private float animTimer = 0f;

    public GameSummaryScreen(RollOfFateGame game, boolean isVictory, Player hero) {
        this(game, isVictory, hero, isVictory ? EndingType.REBIRTH_TRIUMPH : EndingType.FALLEN_ON_TRAIL);
    }

    public GameSummaryScreen(RollOfFateGame game, boolean isVictory, Player hero, EndingType endingType) {
        this.game = game;
        this.isVictory = isVictory;
        this.endingType = (endingType != null) ? endingType : (isVictory ? EndingType.REBIRTH_TRIUMPH : EndingType.FALLEN_ON_TRAIL);
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

    private Color getThemeCardBg() {
        switch (endingType) {
            case REBIRTH_TRIUMPH: return Color.valueOf("#26140cee");
            case PEACEFUL_REST:   return Color.valueOf("#161e2eee");
            default:              return Color.valueOf("#261010ee");
        }
    }

    private Color getThemeCardBorder() {
        switch (endingType) {
            case REBIRTH_TRIUMPH: return Color.valueOf("#854b25");
            case PEACEFUL_REST:   return Color.valueOf("#6d82a3");
            default:              return Color.valueOf("#8a2424");
        }
    }

    private Color getThemeAccentColor() {
        switch (endingType) {
            case REBIRTH_TRIUMPH: return Color.valueOf("#ffd54f");
            case PEACEFUL_REST:   return Color.valueOf("#ffe0a3");
            default:              return Color.valueOf("#ff8a80");
        }
    }

    private String getOutcomeMainTitle() {
        switch (endingType) {
            case REBIRTH_TRIUMPH:      return "GLORIOUS REBIRTH! - THE GOLDEN GATE OPENS ANEW";
            case PEACEFUL_REST:        return "PEACEFUL SLUMBER - REST BEYOND THE DAWN";
            case HARVESTED_BY_SHEPHERD: return "THE LANTERN DIMS - CLAIMED BY THE PALE SHEPHERD";
            case FALLEN_ON_TRAIL:
            default:                   return "DEFEAT - FALLEN UPON THE MOUNTAIN TRAIL";
        }
    }

    private String getOutcomeSubTitle() {
        switch (endingType) {
            case REBIRTH_TRIUMPH:
                return "You answered The Dawn Warden atop the Spire and awakened to sunlit morning once more!";
            case PEACEFUL_REST:
                return "You ceased outrunning a life already ended, finding warm stillness beneath twilight skies.";
            case HARVESTED_BY_SHEPHERD:
                return "You defied Death's golden scythe across the stones until the final ember gently faded.";
            case FALLEN_ON_TRAIL:
            default:
                return "The perilous wilds claim the fallen, yet echoes of their defiance endure across the stones.";
        }
    }

    private String getResolvedHeroAccolade() {
        switch (endingType) {
            case REBIRTH_TRIUMPH:      return "BEARER OF THE NEW DAWN";
            case PEACEFUL_REST:        return "AT PEACE ATOP THE SPIRE";
            case HARVESTED_BY_SHEPHERD: return "DEFIANT UNTO THE END";
            case FALLEN_ON_TRAIL:
            default:                   return (hero != null && hero.getHealth() <= 0) ? "THE FALLEN HERO" : "THE DAUNTLESS VOYAGER";
        }
    }

    private String getExpeditionStatusText(PlayerStats stats) {
        int tile = (hero != null && hero.getPosition() > 0) ? hero.getPosition() : (stats != null ? stats.getHighestTile() : 1);
        switch (endingType) {
            case REBIRTH_TRIUMPH:      return "Reborn at Dawn";
            case PEACEFUL_REST:        return "Peaceful Slumber";
            case HARVESTED_BY_SHEPHERD: return "Harvested on Tile " + tile;
            case FALLEN_ON_TRAIL:
            default:                   return "Fallen on Tile " + tile;
        }
    }

    @Override
    public void show() {
        if (game.getAudioManager() != null) {
            game.getAudioManager().playMusic(isVictory ? AudioManager.MusicTrack.VICTORY : AudioManager.MusicTrack.DEFEAT, false);
        }
        batch = new SpriteBatch();
        stage = new Stage(new ExtendViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);

        // 1. Background Setup (Parallax sliced layers matched to ending tone)
        if (endingType == EndingType.REBIRTH_TRIUMPH) {
            bgTexture   = game.getAssetManager().get(Assets.ACT1_BG);
            farSlice    = new TextureRegion(bgTexture, 7, 0, 1010, 264);
            midSlice    = new TextureRegion(bgTexture, 7, 268, 1010, 188);
            groundSlice = new TextureRegion(bgTexture, 7, 464, 1010, 108);
        } else if (endingType == EndingType.PEACEFUL_REST) {
            bgTexture   = game.getAssetManager().get(Assets.ACT2_BG);
            farSlice    = new TextureRegion(bgTexture, 8, 0, 1004, 264);
            midSlice    = new TextureRegion(bgTexture, 8, 268, 998, 188);
            groundSlice = new TextureRegion(bgTexture, 10, 464, 1002, 110);
        } else {
            bgTexture   = game.getAssetManager().get(Assets.ACT3_BG);
            farSlice    = new TextureRegion(bgTexture, 7, 0, 1006, 264);
            midSlice    = new TextureRegion(bgTexture, 7, 268, 1004, 188);
            groundSlice = new TextureRegion(bgTexture, 6, 464, 1008, 110);
        }

        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        whitePixelTex = new Texture(px);
        px.dispose();

        // 2. Initialize Crisp Fredoka Fonts
        Color goldColor = Color.valueOf("#ffd54f");
        Color warmAmber = Color.valueOf("#ffca80");
        Color accentTitle = getThemeAccentColor();
        Color valueTint = (endingType == EndingType.REBIRTH_TRIUMPH) ? goldColor : ((endingType == EndingType.PEACEFUL_REST) ? warmAmber : Color.valueOf("#ffb74d"));
        Color warmIvory = Color.valueOf("#fbf2dc");
        Color dimText = Color.valueOf("#cfd8dc");

        headerTitleFont    = UIUtils.createFont("ui/Fredoka-Bold.ttf", 38, accentTitle);
        headerSubtitleFont = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 20, warmIvory);
        sectionTitleFont   = UIUtils.createFont("ui/Fredoka-Bold.ttf", 24, accentTitle);
        cardTitleFont      = UIUtils.createFont("ui/Fredoka-Bold.ttf", 22, warmIvory);
        heroNameFont       = UIUtils.createFont("ui/Fredoka-Bold.ttf", 30, Color.WHITE);
        accoladeFont       = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 19, goldColor);
        bodyFont           = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 19, warmIvory);
        statLabelFont      = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 17, dimText);
        statValueFont      = UIUtils.createFont("ui/Fredoka-Bold.ttf", 19, valueTint);
        buttonFont         = UIUtils.createFont("ui/Fredoka-Bold.ttf", 23, Color.WHITE);
        chipTitleFont      = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 13, Color.valueOf("#b0bec5"));
        chipValueFont      = UIUtils.createFont("ui/Fredoka-Bold.ttf", 16, valueTint);
        ledgerKeyFont      = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 17, Color.valueOf("#e0e0e0"));
        ledgerValFont      = UIUtils.createFont("ui/Fredoka-Bold.ttf", 17, valueTint);
        pillMetricFont     = UIUtils.createFont("ui/Fredoka-Bold.ttf", 26, Color.WHITE);
        pillLabelFont      = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 14, Color.valueOf("#cfd8dc"));

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
        rootTable.add(ribbonTable).width(1840).height(84).padBottom(14).row();

        // --- MAIN BODY (STACK CONTAINER: OUTCOME vs DETAILED SUMMARY) ---
        Stack bodyStack = new Stack();

        outcomeContainer = buildOutcomeView();
        summaryContainer = buildSummaryView();
        summaryContainer.setVisible(false);

        bodyStack.add(outcomeContainer);
        bodyStack.add(summaryContainer);

        rootTable.add(bodyStack).width(1840).height(824).row();

        // --- BOTTOM ACTION ROW ---
        Table actionRow = buildBottomActions();
        rootTable.add(actionRow).width(1840).height(74).padTop(10);

        stage.addActor(rootTable);
    }

    /**
     * Toggles between the clean Outcome Podium and the detailed Expedition Summary.
     */
    public void toggleView() {
        showingSummaryDetails = !showingSummaryDetails;
        outcomeContainer.setVisible(!showingSummaryDetails);
        summaryContainer.setVisible(showingSummaryDetails);

        if (toggleViewBtn != null) {
            toggleViewBtn.setText(showingSummaryDetails ? "BACK TO OUTCOME" : "VIEW EXPEDITION SUMMARY");
        }
        updateRibbonText();
    }

    /**
     * Updates top ribbon typography depending on the active view.
     */
    private void updateRibbonText() {
        if (ribbonTitleLabel == null || ribbonSubtitleLabel == null) return;

        if (showingSummaryDetails) {
            ribbonTitleLabel.setText("EXPEDITION RECORD & BARDIC CHRONICLE");
            ribbonSubtitleLabel.setText("A comprehensive debriefing of deeds, milestones, and companion accolades across the stones");
        } else {
            ribbonTitleLabel.setText(getOutcomeMainTitle());
            ribbonSubtitleLabel.setText(getOutcomeSubTitle());
        }
    }

    /**
     * Builds the top celebratory/solemn header ribbon.
     */
    private Table buildTopRibbon() {
        Color bannerBg = (endingType == EndingType.REBIRTH_TRIUMPH)
            ? Color.valueOf("#6b3f11e6")
            : ((endingType == EndingType.PEACEFUL_REST) ? Color.valueOf("#1e2a45e6") : Color.valueOf("#541616e6"));
        Color bannerBorder = (endingType == EndingType.REBIRTH_TRIUMPH)
            ? Color.valueOf("#ffd54f")
            : ((endingType == EndingType.PEACEFUL_REST) ? Color.valueOf("#d6a549") : Color.valueOf("#ff5252"));
        Drawable bannerDrawable = UIUtils.createRoundedDrawable(1840, 84, 16, bannerBg, bannerBorder, 3);

        Table ribbon = new Table();
        ribbon.setBackground(bannerDrawable);
        ribbon.pad(6, 24, 6, 24);

        ribbonTitleLabel = new Label(getOutcomeMainTitle(), new Label.LabelStyle(headerTitleFont, headerTitleFont.getColor()));
        ribbonTitleLabel.setAlignment(Align.center);

        ribbonSubtitleLabel = new Label(getOutcomeSubTitle(), new Label.LabelStyle(headerSubtitleFont, headerSubtitleFont.getColor()));
        ribbonSubtitleLabel.setAlignment(Align.center);

        ribbon.add(ribbonTitleLabel).center().row();
        ribbon.add(ribbonSubtitleLabel).center().padTop(2);

        return ribbon;
    }

    /**
     * Builds the clean, uncluttered Outcome View (Layer 1):
     * - Hero pedestal centerpiece with animated sprite.
     * - 3 quick-glance highlight pills (Turns, Serpents Slain, Ladders Scaled).
     * - In multiplayer: a clean, celebratory companion lineup card row.
     */
    private Table buildOutcomeView() {
        Table table = new Table();
        table.top().center();

        Color cardBg = getThemeCardBg();
        Color cardBorder = getThemeCardBorder();

        // Compute 3 primary metrics across party or solo
        int displayTurns = 0;
        int displaySlain = 0;
        int displayLadders = 0;

        if (game.getRunTracker() != null) {
            if (party.size() > 1) {
                for (Player p : party) {
                    PlayerStats ps = game.getRunTracker().getOrCreateStats(p);
                    if (ps != null) {
                        displayTurns += ps.getTotalTurns();
                        displaySlain += ps.getSnakesSlain();
                        displayLadders += ps.getLaddersClimbed();
                    }
                }
            } else if (hero != null) {
                PlayerStats ps = game.getRunTracker().getOrCreateStats(hero);
                if (ps != null) {
                    displayTurns = ps.getTotalTurns();
                    displaySlain = ps.getSnakesSlain();
                    displayLadders = ps.getLaddersClimbed();
                }
            }
        }

        // Hero Accolade Title
        String heroAccolade = "THE DAUNTLESS VOYAGER";
        if (game.getRunTracker() != null && hero != null) {
            PlayerStats ps = game.getRunTracker().getOrCreateStats(hero);
            if (ps != null && ps.getAccoladeTitle() != null) {
                heroAccolade = ps.getAccoladeTitle();
            }
        }
        heroAccolade = getResolvedHeroAccolade();

        String hName = (hero != null) ? hero.getName().toUpperCase() : "PLAYER 1";
        String hClass = (hero != null && hero.getCharacterClass() != null) ? hero.getCharacterClass().getTitle() : "Adventurer";
        int hLevel = (hero != null) ? hero.getLevel() : 1;

        if (party.size() <= 1) {
            // =========================================================================
            // SINGLE PLAYER OUTCOME LAYOUT (Spacious & Focused)
            // =========================================================================
            Table pedestalTable = new Table();
            pedestalTable.setBackground(UIUtils.createRoundedDrawable(620, 420, 16, cardBg, cardBorder, 2));
            pedestalTable.pad(16, 24, 16, 24);

            // Accolade Pill
            Drawable pillBg = UIUtils.createRoundedDrawable(360, 38, 10, Color.valueOf("#854b25"), Color.valueOf("#ffd54f"), 2);
            Table pillTable = new Table();
            pillTable.setBackground(pillBg);
            Label pillLabel = new Label(heroAccolade, new Label.LabelStyle(accoladeFont, Color.valueOf("#ffd54f")));
            pillLabel.setAlignment(Align.center);
            pillTable.add(pillLabel).pad(4, 16, 4, 16);
            pedestalTable.add(pillTable).center().padBottom(10).row();

            // Animated Walking Hero Sprite
            pedestalTable.add(heroImage).size(180, 180).center().padBottom(10).row();

            // Hero Name & Class
            Label nameLabel = new Label(hName, new Label.LabelStyle(heroNameFont, Color.WHITE));
            nameLabel.setAlignment(Align.center);
            pedestalTable.add(nameLabel).center().row();

            Label classLabel = new Label(hClass + "  |  Level " + hLevel, new Label.LabelStyle(headerSubtitleFont, Color.valueOf("#fbf2dc")));
            classLabel.setAlignment(Align.center);
            pedestalTable.add(classLabel).center().padTop(4);

            table.add(pedestalTable).size(620, 420).padTop(16).padBottom(30).row();

            // 3 Clean Highlight Pills
            Table pillsRow = new Table();
            pillsRow.add(buildHighlightPill("EXPEDITION PACING", displayTurns + " TURNS", "Traversed across the stones", Color.valueOf("#ffd54f"), 340, 96)).padRight(24);
            pillsRow.add(buildHighlightPill("COMBAT VALOR", displaySlain + (displaySlain == 1 ? " BEAST" : " BEASTS"), "Serpents vanquished", Color.valueOf("#ff8a80"), 340, 96)).padRight(24);
            pillsRow.add(buildHighlightPill("TERRAIN MASTERY", displayLadders + (displayLadders == 1 ? " ASCENT" : " ASCENTS"), "Ladders climbed skyward", Color.valueOf("#a5d6a7"), 340, 96));

            table.add(pillsRow).center();

        } else {
            // =========================================================================
            // MULTIPLAYER OUTCOME LAYOUT (Hero Spotlight + 3 Pills + Companion Lineup)
            // =========================================================================
            Table topSection = new Table();

            // 1. Center Hero Pedestal
            Table pedestalTable = new Table();
            pedestalTable.setBackground(UIUtils.createRoundedDrawable(500, 310, 14, cardBg, cardBorder, 2));
            pedestalTable.pad(10, 16, 10, 16);

            Drawable pillBg = UIUtils.createRoundedDrawable(320, 34, 10, Color.valueOf("#854b25"), Color.valueOf("#ffd54f"), 2);
            Table pillTable = new Table();
            pillTable.setBackground(pillBg);
            Label pillLabel = new Label(heroAccolade, new Label.LabelStyle(accoladeFont, Color.valueOf("#ffd54f")));
            pillLabel.setAlignment(Align.center);
            pillTable.add(pillLabel).pad(3, 12, 3, 12);
            pedestalTable.add(pillTable).center().padBottom(6).row();

            pedestalTable.add(heroImage).size(145, 145).center().padBottom(4).row();

            Label nameLabel = new Label(hName, new Label.LabelStyle(cardTitleFont, Color.WHITE));
            nameLabel.setAlignment(Align.center);
            pedestalTable.add(nameLabel).center().row();

            Label classLabel = new Label(hClass + "  |  Level " + hLevel, new Label.LabelStyle(statLabelFont, Color.valueOf("#fbf2dc")));
            classLabel.setAlignment(Align.center);
            pedestalTable.add(classLabel).center().padTop(2);

            topSection.add(pedestalTable).size(500, 310).center();
            table.add(topSection).center().padTop(10).padBottom(18).row();

            // 2. Row of 3 Highlight Pills
            Table pillsRow = new Table();
            pillsRow.add(buildHighlightPill("EXPEDITION PACING", displayTurns + " TURNS", "Collective party turns", Color.valueOf("#ffd54f"), 320, 84)).padRight(20);
            pillsRow.add(buildHighlightPill("COMBAT VALOR", displaySlain + (displaySlain == 1 ? " BEAST" : " BEASTS"), "Party serpents slain", Color.valueOf("#ff8a80"), 320, 84)).padRight(20);
            pillsRow.add(buildHighlightPill("TERRAIN MASTERY", displayLadders + (displayLadders == 1 ? " ASCENT" : " ASCENTS"), "Party ladders scaled", Color.valueOf("#a5d6a7"), 320, 84));

            table.add(pillsRow).center().padBottom(20).row();

            // 3. Companion Recognition Lineup (Excluding the spotlighted winner on the pedestal)
            List<Player> companions = new ArrayList<>();
            for (Player p : party) {
                if (p != hero) {
                    companions.add(p);
                }
            }

            if (!companions.isEmpty()) {
                Label lineupHeader = new Label("COMPANION RECOGNITION & HONORS", new Label.LabelStyle(sectionTitleFont, sectionTitleFont.getColor()));
                lineupHeader.setAlignment(Align.center);
                table.add(lineupHeader).center().padBottom(8).row();

                Table lineupRow = new Table();
                int count = companions.size();
                float cardW = Math.min(380f, (1760f - (count * 16f)) / count);

                for (Player p : companions) {
                    Table compCard = buildCompactCompanionCard(p, cardW, 110f);
                    lineupRow.add(compCard).width(cardW).height(110f).pad(6);
                }

                table.add(lineupRow).center();
            }
        }

        return table;
    }

    /**
     * Builds a single highlight metric pill for the clean outcome screen.
     */
    private Table buildHighlightPill(String title, String metric, String subtitle, Color accentColor, float width, float height) {
        Color cardBg = getThemeCardBg();
        Color cardBorder = getThemeCardBorder();
        Drawable pillBg = UIUtils.createRoundedDrawable((int) width, (int) height, 12, cardBg, cardBorder, 2);

        Table pill = new Table();
        pill.setBackground(pillBg);
        pill.pad(8, 14, 8, 14);

        Label titleLbl = new Label(title.toUpperCase(), new Label.LabelStyle(chipTitleFont, accentColor));
        titleLbl.setAlignment(Align.center);

        Label metricLbl = new Label(metric, new Label.LabelStyle(pillMetricFont, Color.WHITE));
        metricLbl.setAlignment(Align.center);

        Label subLbl = new Label(subtitle, new Label.LabelStyle(pillLabelFont, Color.valueOf("#cfd8dc")));
        subLbl.setAlignment(Align.center);

        pill.add(titleLbl).center().row();
        pill.add(metricLbl).center().padTop(2).row();
        pill.add(subLbl).center().padTop(2);

        return pill;
    }

    /**
     * Builds a clean, compact companion honor card for the multiplayer outcome screen.
     * Completely free of long paragraphs and 16 stat chips.
     */
    private Table buildCompactCompanionCard(Player p, float width, float height) {
        Color cardBg = getThemeCardBg();
        Color cardBorder = getThemeCardBorder();
        Drawable cardBgDrawable = UIUtils.createRoundedDrawable((int) width, (int) height, 12, cardBg, cardBorder, 2);

        Table card = new Table();
        card.setBackground(cardBgDrawable);
        card.pad(10, 14, 10, 14);

        CharacterClass pClass = p.getCharacterClass() != null ? p.getCharacterClass() : CharacterClass.CAT;
        Texture charTex = game.getAssetManager().get(Assets.getCharacterTexture(pClass));
        Image avatar = new Image(charTex);
        avatar.setScaling(Scaling.fit);
        card.add(avatar).size(64, 64).padRight(12);

        Table infoTable = new Table();
        infoTable.left();

        Label nameLbl = new Label(p.getName().toUpperCase(), new Label.LabelStyle(cardTitleFont, Color.WHITE));
        Label classLbl = new Label(pClass.getTitle() + " (Lvl " + p.getLevel() + ")", new Label.LabelStyle(statLabelFont, Color.valueOf("#fbf2dc")));
        infoTable.add(nameLbl).left().row();
        infoTable.add(classLbl).left().padBottom(4).row();

        PlayerStats stats = (game.getRunTracker() != null) ? game.getRunTracker().getOrCreateStats(p) : null;
        String title = (stats != null && stats.getAccoladeTitle() != null) ? stats.getAccoladeTitle() : "THE DAUNTLESS VOYAGER";

        Drawable pillBg = UIUtils.createRoundedDrawable((int) (width - 100), 24, 6, Color.valueOf("#6b3f11"), Color.valueOf("#ffd54f"), 1);
        Table pillTable = new Table();
        pillTable.setBackground(pillBg);
        Label pillLabel = new Label(title, new Label.LabelStyle(chipTitleFont, Color.valueOf("#ffd54f")));
        pillLabel.setAlignment(Align.center);
        pillTable.add(pillLabel).pad(1, 8, 1, 8);

        infoTable.add(pillTable).left();
        card.add(infoTable).left().expandX();

        return card;
    }

    /**
     * Builds the detailed Expedition Summary View (Layer 2):
     * - Left Column: Bardic Chronicle story.
     * - Right Column: 4-Quadrant Ledger (Solo) or Full Accolade Grid (Multiplayer).
     */
    private Table buildSummaryView() {
        Table bodyTable = new Table();
        bodyTable.top();

        // Left Column: Hero Spotlight + Bardic Chronicle
        Table leftCol = buildLeftColumn();
        bodyTable.add(leftCol).width(570).height(820).top().padRight(30);

        // Right Column: Companion Highlights (Multiplayer) or Expedition Ledger (Single Player)
        Table rightCol = buildRightColumn();
        bodyTable.add(rightCol).width(1240).height(820).top();

        return bodyTable;
    }

    /**
     * Builds the left column containing the Hero Pedestal card and Bardic Chronicle card.
     */
    private Table buildLeftColumn() {
        Table col = new Table();
        Color cardBg = getThemeCardBg();
        Color cardBorder = getThemeCardBorder();

        // 1. Hero Pedestal Card
        Drawable pedestalDrawable = UIUtils.createRoundedDrawable(570, 340, 14, cardBg, cardBorder, 2);
        Table pedestalTable = new Table();
        pedestalTable.setBackground(pedestalDrawable);
        pedestalTable.pad(10, 16, 10, 16);

        String heroAccolade = "THE DAUNTLESS VOYAGER";
        if (game.getRunTracker() != null && hero != null) {
            PlayerStats ps = game.getRunTracker().getOrCreateStats(hero);
            if (ps != null && ps.getAccoladeTitle() != null) {
                heroAccolade = ps.getAccoladeTitle();
            }
        }
        heroAccolade = getResolvedHeroAccolade();

        Drawable pillBg = UIUtils.createRoundedDrawable(340, 34, 10, Color.valueOf("#854b25"), Color.valueOf("#ffd54f"), 2);
        Table pillTable = new Table();
        pillTable.setBackground(pillBg);
        Label pillLabel = new Label(heroAccolade, new Label.LabelStyle(accoladeFont, Color.valueOf("#ffd54f")));
        pillLabel.setAlignment(Align.center);
        pillTable.add(pillLabel).pad(3, 14, 3, 14);
        pedestalTable.add(pillTable).center().padBottom(6).row();

        // Hero portrait in summary left card
        CharacterClass heroClass = (hero != null) ? hero.getCharacterClass() : CharacterClass.CAT;
        if (heroClass == null) heroClass = CharacterClass.CAT;
        Texture charTex = game.getAssetManager().get(Assets.getCharacterTexture(heroClass));
        Image staticHeroImg = new Image(charTex);
        staticHeroImg.setScaling(Scaling.fit);
        pedestalTable.add(staticHeroImg).size(140, 140).center().padBottom(6).row();

        String hName = (hero != null) ? hero.getName().toUpperCase() : "PLAYER 1";
        String hClass = (hero != null && hero.getCharacterClass() != null) ? hero.getCharacterClass().getTitle() : "Adventurer";
        int hLevel = (hero != null) ? hero.getLevel() : 1;

        Label nameLabel = new Label(hName, new Label.LabelStyle(heroNameFont, Color.WHITE));
        nameLabel.setAlignment(Align.center);
        pedestalTable.add(nameLabel).center().row();

        Label classLabel = new Label(hClass + "  |  Level " + hLevel, new Label.LabelStyle(bodyFont, Color.valueOf("#fbf2dc")));
        classLabel.setAlignment(Align.center);
        pedestalTable.add(classLabel).center().padTop(2);

        col.add(pedestalTable).width(570).height(340).padBottom(14).row();

        // 2. Bardic Narrative Card
        Drawable chronicleDrawable = UIUtils.createRoundedDrawable(570, 466, 14, cardBg, cardBorder, 2);
        Table chronicleTable = new Table();
        chronicleTable.setBackground(chronicleDrawable);
        chronicleTable.pad(14, 20, 14, 20);

        Label chronicleHeader = new Label("CHRONICLE OF DESTINY", new Label.LabelStyle(sectionTitleFont, sectionTitleFont.getColor()));
        chronicleHeader.setAlignment(Align.center);
        chronicleTable.add(chronicleHeader).center().padBottom(8).row();

        String narrativeText = "The bards assemble to record the deeds of courage across the stones...";
        if (game.getRunTracker() != null) {
            narrativeText = game.getRunTracker().generateBardicNarrative(endingType, hero, party);
        }

        Label narrativeLabel = new Label(narrativeText, new Label.LabelStyle(bodyFont, Color.valueOf("#fbf2dc")));
        narrativeLabel.setWrap(true);
        narrativeLabel.setAlignment(Align.topLeft);

        chronicleTable.add(narrativeLabel).width(520).top().left().expand().fillY();
        col.add(chronicleTable).width(570).height(466);

        return col;
    }

    /**
     * Builds the right column of the detailed summary view:
     * - In Multiplayer: 2x2 grid of Companion Highlight Cards with unique positive accolades.
     * - In Single Player: 2x2 grid of detailed Expedition Ledger Cards.
     */
    private Table buildRightColumn() {
        Table col = new Table();

        if (party.size() > 1) {
            Label sectionLbl = new Label("PARTY HONORS & EXPLOITS", new Label.LabelStyle(sectionTitleFont, sectionTitleFont.getColor()));
            Label subLbl = new Label("Spotlighting every companion's valor and pivotal feats across the wild", new Label.LabelStyle(headerSubtitleFont, Color.valueOf("#cfd8dc")));
            col.add(sectionLbl).left().row();
            col.add(subLbl).left().padBottom(10).row();

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
            Label sectionLbl = new Label("EXPEDITION RECORD & LEDGER", new Label.LabelStyle(sectionTitleFont, sectionTitleFont.getColor()));
            Label subLbl = new Label("A comprehensive accounting of trials, combat, and fortune across the stones", new Label.LabelStyle(headerSubtitleFont, Color.valueOf("#cfd8dc")));
            col.add(sectionLbl).left().row();
            col.add(subLbl).left().padBottom(10).row();

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
                {"Expedition Status:", getExpeditionStatusText(stats)}
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
     * Builds a single player's highlight accolade card for multiplayer summary view.
     */
    private Table buildPlayerAccoladeCard(Player p) {
        Color cardBg = getThemeCardBg();
        Color cardBorder = getThemeCardBorder();
        Drawable cardBgDrawable = UIUtils.createRoundedDrawable(605, 350, 14, cardBg, cardBorder, 2);

        Table card = new Table();
        card.setBackground(cardBgDrawable);
        card.pad(14, 18, 14, 18);

        CharacterClass pClass = p.getCharacterClass() != null ? p.getCharacterClass() : CharacterClass.CAT;
        PlayerStats stats = (game.getRunTracker() != null) ? game.getRunTracker().getOrCreateStats(p) : null;

        Table topRow = new Table();
        topRow.left();

        Texture charTex = game.getAssetManager().get(Assets.getCharacterTexture(pClass));
        Image avatar = new Image(charTex);
        avatar.setScaling(Scaling.fit);
        topRow.add(avatar).size(60, 60).padRight(12);

        Table nameTable = new Table();
        nameTable.left();
        Label nameLbl = new Label(p.getName().toUpperCase(), new Label.LabelStyle(cardTitleFont, Color.WHITE));
        Label classLbl = new Label(pClass.getTitle() + "  |  Level " + p.getLevel(), new Label.LabelStyle(statLabelFont, Color.valueOf("#fbf2dc")));
        nameTable.add(nameLbl).left().row();
        nameTable.add(classLbl).left();
        topRow.add(nameTable).left().expandX();

        card.add(topRow).width(565).padBottom(8).row();

        Drawable pillBg = UIUtils.createRoundedDrawable(565, 34, 10, Color.valueOf("#6b3f11"), Color.valueOf("#ffd54f"), 2);
        Table pillTable = new Table();
        pillTable.setBackground(pillBg);
        String title = (stats != null && stats.getAccoladeTitle() != null) ? stats.getAccoladeTitle() : "THE DAUNTLESS VOYAGER";
        Label pillLabel = new Label(title, new Label.LabelStyle(accoladeFont, Color.valueOf("#ffd54f")));
        pillLabel.setAlignment(Align.center);
        pillTable.add(pillLabel).pad(3, 10, 3, 10);
        card.add(pillTable).width(565).height(34).padBottom(8).row();

        String desc = (stats != null && stats.getAccoladeDescription() != null) ? stats.getAccoladeDescription() : "Traversed the perilous stones with steadfast courage.";
        Label descLabel = new Label(desc, new Label.LabelStyle(bodyFont, Color.valueOf("#fbf2dc")));
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.topLeft);
        card.add(descLabel).width(565).height(55).top().left().padBottom(10).row();

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
        Color cardBg = getThemeCardBg();
        Color cardBorder = getThemeCardBorder();
        Drawable cardDrawable = UIUtils.createRoundedDrawable(605, 350, 14, cardBg, cardBorder, 2);

        Table card = new Table();
        card.setBackground(cardDrawable);
        card.pad(16, 20, 16, 20);

        Label titleLbl = new Label(categoryTitle, new Label.LabelStyle(cardTitleFont, headerAccent));
        titleLbl.setAlignment(Align.left);
        card.add(titleLbl).left().padBottom(12).row();

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
     * Builds the bottom action bar containing PLAY AGAIN, TOGGLE SUMMARY, and MAIN MENU buttons.
     * Uses the game's cohesive warm fantasy leather, mahogany, and gold/amber color palette.
     */
    private Table buildBottomActions() {
        Table table = new Table();

        // 1. PLAY AGAIN Button (Primary Call to Action - Warm tavern leather with gold border)
        TextButton.TextButtonStyle playAgainStyle = new TextButton.TextButtonStyle();
        playAgainStyle.up = UIUtils.createRoundedDrawable(280, 60, 14, Color.valueOf("#854b25"), Color.valueOf("#ffd54f"), 2);
        playAgainStyle.over = UIUtils.createRoundedDrawable(280, 60, 14, Color.valueOf("#a66032"), Color.valueOf("#ffe082"), 2);
        playAgainStyle.font = buttonFont;
        playAgainStyle.fontColor = Color.WHITE;

        TextButton playAgainBtn = new TextButton("PLAY AGAIN", playAgainStyle);
        UIUtils.addScaleHoverEffect(playAgainBtn);
        playAgainBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    game.getAudioManager().playSound(AudioManager.SoundEffect.SANCTUARY_HEAL);
                }
                if (game.getLastChosenClasses() != null && !game.getLastChosenClasses().isEmpty()) {
                    game.startGameWithClasses(game.getLastChosenClasses());
                } else {
                    game.startGame();
                }
            }
        });

        // 2. VIEW SUMMARY / BACK TO OUTCOME Toggle Button (Secondary Utility - Rich mahogany with amber border)
        TextButton.TextButtonStyle toggleStyle = new TextButton.TextButtonStyle();
        toggleStyle.up = UIUtils.createRoundedDrawable(420, 60, 14, Color.valueOf("#54280e"), Color.valueOf("#c89259"), 2);
        toggleStyle.over = UIUtils.createRoundedDrawable(420, 60, 14, Color.valueOf("#6d3b17"), Color.valueOf("#ffd54f"), 2);
        toggleStyle.font = buttonFont;
        toggleStyle.fontColor = Color.WHITE;

        toggleViewBtn = new TextButton("VIEW EXPEDITION SUMMARY", toggleStyle);
        UIUtils.addScaleHoverEffect(toggleViewBtn);
        toggleViewBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                toggleView();
            }
        });

        // 3. REPLAY EPILOGUE Button (Allows reviewing the storybook ending anytime)
        TextButton.TextButtonStyle epilogueStyle = new TextButton.TextButtonStyle();
        epilogueStyle.up = UIUtils.createRoundedDrawable(260, 60, 14, Color.valueOf("#261933"), Color.valueOf("#a779c2"), 2);
        epilogueStyle.over = UIUtils.createRoundedDrawable(260, 60, 14, Color.valueOf("#3b2650"), Color.valueOf("#ffd54f"), 2);
        epilogueStyle.font = buttonFont;
        epilogueStyle.fontColor = Color.valueOf("#ffd54f");

        TextButton epilogueBtn = new TextButton("REPLAY EPILOGUE", epilogueStyle);
        UIUtils.addScaleHoverEffect(epilogueBtn);
        epilogueBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                game.setScreen(new EndingStoryScreen(game, hero, party, endingType));
            }
        });

        // 4. MAIN MENU Button (Navigation - Dark leather with muted border)
        TextButton.TextButtonStyle menuStyle = new TextButton.TextButtonStyle();
        menuStyle.up = UIUtils.createRoundedDrawable(240, 60, 14, Color.valueOf("#4a2307"), Color.valueOf("#854b25"), 2);
        menuStyle.over = UIUtils.createRoundedDrawable(240, 60, 14, Color.valueOf("#5e2e0a"), Color.valueOf("#c89259"), 2);
        menuStyle.font = buttonFont;
        menuStyle.fontColor = Color.WHITE;

        TextButton menuBtn = new TextButton("MAIN MENU", menuStyle);
        UIUtils.addScaleHoverEffect(menuBtn);
        menuBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                game.setScreen(new MainMenuScreen(game));
            }
        });

        bottomActionFocusables.clear();
        bottomActionFocusables.add(playAgainBtn);
        bottomActionFocusables.add(toggleViewBtn);
        bottomActionFocusables.add(epilogueBtn);
        bottomActionFocusables.add(menuBtn);
        bottomActionFocusIdx = 0;
        UIUtils.applyFocusVisuals(bottomActionFocusables, bottomActionFocusIdx);

        table.add(playAgainBtn).size(260, 60).padRight(20);
        table.add(toggleViewBtn).size(420, 60).padRight(20);
        table.add(epilogueBtn).size(260, 60).padRight(20);
        table.add(menuBtn).size(240, 60);

        return table;
    }

    private void handleKeyboardInput() {
        if (bottomActionFocusables.isEmpty()) return;
        boolean tabPressed = Gdx.input.isKeyJustPressed(Input.Keys.TAB);
        int tabDelta = UIUtils.isShiftHeld() ? -1 : 1;

        UIUtils.applyFocusVisuals(bottomActionFocusables, bottomActionFocusIdx);

        if (UIUtils.isNavPrevHorizontalJustPressed() || (tabPressed && tabDelta < 0)) {
            bottomActionFocusIdx = UIUtils.stepFocus(bottomActionFocusables, bottomActionFocusIdx, -1);
        } else if (UIUtils.isNavNextHorizontalJustPressed() || tabPressed) {
            bottomActionFocusIdx = UIUtils.stepFocus(bottomActionFocusables, bottomActionFocusIdx, 1);
        } else if (UIUtils.isConfirmJustPressed()) {
            UIUtils.activateButton(bottomActionFocusables.get(bottomActionFocusIdx));
        } else if (UIUtils.isBackJustPressed()) {
            UIUtils.activateButton(bottomActionFocusables.get(3)); // MAIN MENU
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
            UIUtils.activateButton(bottomActionFocusables.get(1)); // TOGGLE SUMMARY
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            UIUtils.activateButton(bottomActionFocusables.get(2)); // REPLAY EPILOGUE
        } else {
            int num = UIUtils.justPressedNumberKey(bottomActionFocusables.size());
            if (num >= 0) {
                bottomActionFocusIdx = num;
                UIUtils.activateButton(bottomActionFocusables.get(num));
            }
        }
    }

    @Override
    public void render(float delta) {
        handleKeyboardInput();
        Gdx.gl.glClearColor(0.08f, 0.04f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        animTimer += delta;

        // Update animated hero pedestal sprite frame
        if (heroWalkAnim != null && heroImage != null) {
            heroImage.setDrawable(new TextureRegionDrawable(heroWalkAnim.getKeyFrame(animTimer)));
        }

        float viewW = stage.getViewport().getWorldWidth();
        float viewH = stage.getViewport().getWorldHeight();

        batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
        batch.begin();

        // 1. Subterranean foundation
        Color subColor = (endingType == EndingType.REBIRTH_TRIUMPH)
            ? Color.valueOf("#181822")
            : ((endingType == EndingType.PEACEFUL_REST) ? Color.valueOf("#0f172a") : Color.valueOf("#2e1534"));
        batch.setColor(subColor);
        if (whitePixelTex != null) {
            batch.draw(whitePixelTex, 0, 0, viewW, 140f);
        }
        batch.setColor(Color.WHITE);

        // 2. Far Canopy (Backdrop)
        if (farSlice != null) {
            drawTiledLayer(batch, farSlice, 110f, 980f, viewW);
        }

        // 3. Midground Silhouettes
        if (midSlice != null) {
            drawTiledLayer(batch, midSlice, 140f, 560f, viewW);
        }

        // 4. Foreground Ground Platform
        if (groundSlice != null) {
            drawTiledLayer(batch, groundSlice, 0f, 220f, viewW);
        }

        // 5. Dark Vignette / Contrast Wash for UI legibility
        Color washColor = (endingType == EndingType.REBIRTH_TRIUMPH)
            ? Color.valueOf("#120b06ba")
            : ((endingType == EndingType.PEACEFUL_REST) ? Color.valueOf("#0b1120be") : Color.valueOf("#180606c4"));
        batch.setColor(washColor);
        if (whitePixelTex != null) {
            batch.draw(whitePixelTex, 0, 0, viewW, viewH);
        }
        batch.setColor(Color.WHITE);

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    /**
     * Draws a seamlessly tiled and centered background parallax slice across the full viewport width.
     */
    private void drawTiledLayer(SpriteBatch batch, TextureRegion slice, float startY, float height, float viewW) {
        if (slice == null || height <= 0 || slice.getRegionHeight() <= 0) return;
        float width = height * ((float) slice.getRegionWidth() / slice.getRegionHeight());
        float step = width - 2f;
        float startX = (viewW - width) * 0.5f;
        while (startX > 0) {
            startX -= step;
        }
        for (float x = startX; x < viewW; x += step) {
            batch.draw(slice, x, startY, width, height);
        }
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
        if (whitePixelTex != null) whitePixelTex.dispose();

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
        if (pillMetricFont != null) pillMetricFont.dispose();
        if (pillLabelFont != null) pillLabelFont.dispose();
    }
}
