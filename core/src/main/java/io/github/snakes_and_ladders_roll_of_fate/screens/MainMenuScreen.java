package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.audio.AudioManager;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.systems.GameMode;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;
import io.github.snakes_and_ladders_roll_of_fate.ui.UIUtils;

/**
 * Main Menu Screen: displays animated background, title logo, action buttons,
 * the Game Mode selection modal, the Character Selection modal, and the Settings modal.
 */
public class MainMenuScreen implements Screen {
    private RollOfFateGame game;
    private VideoPlayer videoPlayer;
    private SpriteBatch batch;
    private Stage stage;

    // Navigation tables for the different views on this screen
    private Table menuTable;
    private Table settingsTable;
    private Table gameModeTable;
    private Table playerCountTable;
    private Table characterSelectTable;

    // Sniglet font instances for UI labels and titles
    private BitmapFont badgeFont;
    private BitmapFont textFont;
    private BitmapFont numFont;
    private BitmapFont modeTitleFont;
    private BitmapFont modeSubtitleFont;
    private BitmapFont instructionFont;
    private BitmapFont statFont;
    private BitmapFont perkFont;
    private BitmapFont countNumFont;

    // Pass & Play Player Count State (Clamped between 2 and 4)
    private int passPlayPlayerCount = 2;
    private Label countNumLabel;
    private TextButton countLeftArrow;
    private TextButton countRightArrow;

    // Character Selection State
    private int currentCharacterIndex = 0;
    private int selectingPlayerIndex = 0;
    private int totalSelectingPlayers = 1;
    private List<CharacterClass> chosenClasses = new ArrayList<>();

    // Character Select UI references for live updates
    private final Map<CharacterClass, Animation<TextureRegion>> charAnimations = new HashMap<>();
    private float charAnimTimer = 0f;
    private Label characterBadge;
    private Image characterImage;
    private Label charNameLabel;
    private Label charTitleLabel;
    private Label charStatsLabel;
    private Label charPerkLabel;
    private Label lockBanner;
    private TextButton enterButton;

    public MainMenuScreen(RollOfFateGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        if (game.getAudioManager() != null) {
            game.getAudioManager().playMusic(AudioManager.MusicTrack.MENU, true, true);
        }
        batch = new SpriteBatch();
        // ExtendViewport gives a responsive virtual 1920x1080 canvas that scales to any window resolution
        stage = new Stage(new ExtendViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);

        // --- Background Video Setup ---
        videoPlayer = VideoPlayerCreator.createVideoPlayer();
        videoPlayer.setOnCompletionListener(new VideoPlayer.CompletionListener() {
            @Override
            public void onCompletionListener(FileHandle file) {
                try {
                    videoPlayer.play(file); // Loop seamlessly
                } catch (Exception e) {
                    Gdx.app.error("Video", "Failed to loop video", e);
                }
            }
        });

        try {
            videoPlayer.play(Gdx.files.internal(Assets.MENU_VIDEO));
        } catch (Exception e) {
            Gdx.app.error("Video", "Error starting background video", e);
        }

        // --- Theme Colors & Sniglet Fonts ---
        Color darkBrown = Color.valueOf("#5a2d0c"); // Main font & icon color
        Color pillBrown = Color.valueOf("#854b25"); // Header badge & button color

        // Crisp FreeType Sniglet fonts - enlarged for clear visibility and readability
        badgeFont        = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 28, Color.WHITE);
        textFont         = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 28, darkBrown);
        numFont          = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 28, darkBrown);
        modeTitleFont    = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 30, darkBrown);
        modeSubtitleFont = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 22, darkBrown);
        instructionFont  = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 25, darkBrown);
        statFont         = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 24, darkBrown);
        perkFont         = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 21, Color.valueOf("#6d3b17"));
        countNumFont     = UIUtils.createFont("ui/Sniglet-ExtraBold.ttf", 64, darkBrown);

        // --- Main Menu Buttons & Title ---
        Texture titleTexture = game.getAssetManager().get(Assets.TITLE);
        Image titleImage = new Image(titleTexture);
        titleImage.setScaling(Scaling.fit);

        Skin skin = game.getAssetManager().get(Assets.UI_SKIN);

        TextButton playButton = new TextButton("Play Game", skin);
        TextButton settingsButton = new TextButton("Settings", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        playButton.getLabel().setFontScale(2.5f);
        settingsButton.getLabel().setFontScale(2.5f);
        exitButton.getLabel().setFontScale(2.5f);

        Color btnColor = Color.valueOf("#a8642cCC");
        playButton.setColor(btnColor);
        settingsButton.setColor(btnColor);
        exitButton.setColor(btnColor);

        playButton.getLabel().setAlignment(Align.center);
        settingsButton.getLabel().setAlignment(Align.center);
        exitButton.getLabel().setAlignment(Align.center);

        // Hover scale animations
        UIUtils.addScaleHoverEffect(playButton);
        UIUtils.addScaleHoverEffect(settingsButton);
        UIUtils.addScaleHoverEffect(exitButton);

        // Clicking "Play Game" opens the Game Mode dialog
        playButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                menuTable.setVisible(false);
                gameModeTable.setVisible(true);
            }
        });

        // Clicking "Settings" opens the Settings dialog
        settingsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                menuTable.setVisible(false);
                settingsTable.setVisible(true);
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                Gdx.app.exit();
            }
        });

        // Main Menu Layout
        menuTable = new Table();
        menuTable.setFillParent(true);
        menuTable.add(titleImage).size(800, 400).padBottom(15).center().row();
        menuTable.add(playButton).size(400, 80).padBottom(20).center().row();
        menuTable.add(settingsButton).size(400, 80).padBottom(20).center().row();
        menuTable.add(exitButton).size(400, 80).center().row();

        // --- Build Dialog Modals ---
        initCharacterAnimations();
        buildSettingsDialog(darkBrown, pillBrown);
        buildGameModeDialog(darkBrown, pillBrown);
        buildPlayerCountDialog(darkBrown, pillBrown);
        buildCharacterSelectDialog(darkBrown, pillBrown);

        // Add all views to stage (modals are hidden by default)
        stage.addActor(menuTable);
        stage.addActor(settingsTable);
        stage.addActor(gameModeTable);
        stage.addActor(playerCountTable);
        stage.addActor(characterSelectTable);
    }

    /**
     * Starts the Character Selection flow for the selected game mode.
     */
    private void openCharacterSelect(int playerCount) {
        totalSelectingPlayers = playerCount;
        chosenClasses.clear();
        selectingPlayerIndex = 0;
        currentCharacterIndex = 0;

        updateCharacterDisplay();
        gameModeTable.setVisible(false);
        characterSelectTable.setVisible(true);
    }

    /**
     * Builds the "Select Game Mode" scroll modal with Single Player and Pass & Play cards.
     */
    private void buildGameModeDialog(Color darkBrown, Color pillBrown) {
        Color cardBg = Color.valueOf("#c9935abb");
        Color cardHover = Color.valueOf("#d8a66ebb");
        Drawable cardDrawable = UIUtils.createRoundedDrawable(460, 98, 18, cardBg);
        Drawable cardHoverDrawable = UIUtils.createRoundedDrawable(460, 98, 18, cardHover);

        Button.ButtonStyle cardStyle = new Button.ButtonStyle();
        cardStyle.up = cardDrawable;
        cardStyle.over = cardHoverDrawable;

        // 1. Single Player Card Button -> opens Character Select for 1 Player
        Button singlePlayerBtn = new Button(cardStyle);
        UIUtils.addScaleHoverEffect(singlePlayerBtn);

        Image singleIcon = new Image(game.getAssetManager().get(Assets.ICON_SINGLE));
        singlePlayerBtn.add(singleIcon).size(60, 60).pad(14, 20, 14, 20);

        Table singleTextTable = new Table();
        Label singleTitle = new Label("SINGLE PLAYER", new Label.LabelStyle(modeTitleFont, darkBrown));
        Label singleSub = new Label("SOLO GAME", new Label.LabelStyle(modeSubtitleFont, darkBrown));
        singleTextTable.add(singleTitle).left().row();
        singleTextTable.add(singleSub).left().padTop(3);

        singlePlayerBtn.add(singleTextTable).expandX().left().padRight(16);

        singlePlayerBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                openCharacterSelect(1);
            }
        });

        // 2. Pass & Play Card Button -> opens Character Select for 2 Players
        Button passPlayBtn = new Button(cardStyle);
        UIUtils.addScaleHoverEffect(passPlayBtn);

        Image multiIcon = new Image(game.getAssetManager().get(Assets.ICON_MULTI));
        passPlayBtn.add(multiIcon).size(60, 60).pad(14, 20, 14, 20);

        Table multiTextTable = new Table();
        Label multiTitle = new Label("PASS & PLAY", new Label.LabelStyle(modeTitleFont, darkBrown));
        Label multiSub = new Label("2 TO 4 PLAYERS", new Label.LabelStyle(modeSubtitleFont, darkBrown));
        multiTextTable.add(multiTitle).left().row();
        multiTextTable.add(multiSub).left().padTop(3);

        passPlayBtn.add(multiTextTable).expandX().left().padRight(16);

        passPlayBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                // Open Player Count picker for Pass & Play mode
                gameModeTable.setVisible(false);
                updatePlayerCountDisplay();
                playerCountTable.setVisible(true);
            }
        });

        // 3. Stack cards over the parchment scroll (contained safely inside flat paper area)
        Table contentTable = new Table();
        contentTable.add(singlePlayerBtn).size(460, 98).padBottom(24).center().row();
        contentTable.add(passPlayBtn).size(460, 98).center().row();

        Image scrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        scrollBg.setScaling(Scaling.stretch);

        Stack scrollStack = new Stack();
        scrollStack.add(scrollBg);
        scrollStack.add(contentTable);

        // 4. Top "SELECT GAME MODE" badge with generous gaps
        Label modeBadge = new Label("SELECT GAME MODE", new Label.LabelStyle(badgeFont, Color.WHITE));
        modeBadge.setAlignment(Align.center);

        Table badgeTable = new Table();
        badgeTable.setBackground(UIUtils.createRoundedDrawable(380, 60, 20, pillBrown));
        badgeTable.add(modeBadge).pad(8, 40, 8, 40);

        // 5. Bottom "BACK" button
        TextButton.TextButtonStyle backStyle = new TextButton.TextButtonStyle();
        backStyle.up = UIUtils.createRoundedDrawable(180, 52, 18, pillBrown);
        backStyle.font = badgeFont;
        backStyle.fontColor = Color.WHITE;

        TextButton backButton = new TextButton("BACK", backStyle);
        UIUtils.addScaleHoverEffect(backButton);

        // Clicking "Back" returns to the main menu
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                gameModeTable.setVisible(false);
                menuTable.setVisible(true);
            }
        });

        // 6. Outer Game Mode Table
        gameModeTable = new Table();
        gameModeTable.setFillParent(true);
        gameModeTable.setVisible(false);

        gameModeTable.add(badgeTable).size(380, 60).padBottom(-22).center().row();
        gameModeTable.add(scrollStack).size(840, 646).center().row();
        gameModeTable.add(backButton).size(180, 52).padTop(-22).center().row();
    }

    /**
     * Builds the Player Count dialog for Pass & Play mode.
     * Lets the host pick between 2, 3, or 4 players using left and right arrows.
     */
    private void buildPlayerCountDialog(Color darkBrown, Color pillBrown) {
        // 1. Top Badge: "NUMBER OF PLAYERS"
        Label countBadge = new Label("NUMBER OF PLAYERS", new Label.LabelStyle(badgeFont, Color.WHITE));
        countBadge.setAlignment(Align.center);

        Table badgeTable = new Table();
        badgeTable.setBackground(UIUtils.createRoundedDrawable(420, 60, 20, pillBrown));
        badgeTable.add(countBadge).pad(8, 40, 8, 40);

        // 2. Center Stepper Content
        Table contentTable = new Table();

        // Multi-player icon
        Image multiIcon = new Image(game.getAssetManager().get(Assets.ICON_MULTI));
        contentTable.add(multiIcon).size(68, 68).padBottom(8).center().row();

        // Subtitle helper describing what to do - wrapped safely within flat parchment bounds
        Label subtitleLabel = new Label("Choose 2 to 4 Players", new Label.LabelStyle(instructionFont, darkBrown));
        subtitleLabel.setWrap(true);
        subtitleLabel.setAlignment(Align.center);
        contentTable.add(subtitleLabel).width(460).padBottom(20).center().row();

        // Left / Right Arrow buttons for adjusting the player count
        TextButton.TextButtonStyle arrowStyle = new TextButton.TextButtonStyle();
        arrowStyle.up = UIUtils.createRoundedDrawable(56, 56, 18, pillBrown);
        arrowStyle.font = badgeFont;
        arrowStyle.fontColor = Color.WHITE;

        countLeftArrow = new TextButton("<", arrowStyle);
        countRightArrow = new TextButton(">", arrowStyle);
        UIUtils.addScaleHoverEffect(countLeftArrow);
        UIUtils.addScaleHoverEffect(countRightArrow);

        // Center box displaying the selected player number
        Table countBox = new Table();
        countBox.setBackground(UIUtils.createRoundedDrawable(200, 115, 18, Color.valueOf("#c9935abb")));

        countNumLabel = new Label(String.valueOf(passPlayPlayerCount), new Label.LabelStyle(countNumFont, darkBrown));
        countNumLabel.setAlignment(Align.center);

        Label playersText = new Label("PLAYERS", new Label.LabelStyle(modeSubtitleFont, darkBrown));
        playersText.setAlignment(Align.center);

        countBox.add(countNumLabel).center().row();
        countBox.add(playersText).center();

        // Arrow click listeners with clamping logic (strictly 2 to 4 players)
        countLeftArrow.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (passPlayPlayerCount > 2) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    passPlayPlayerCount--;
                    updatePlayerCountDisplay();
                }
            }
        });

        countRightArrow.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (passPlayPlayerCount < 4) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                    passPlayPlayerCount++;
                    updatePlayerCountDisplay();
                }
            }
        });

        // Assemble stepper row: [ < ]  [ 2 PLAYERS ]  [ > ]
        Table stepperRow = new Table();
        stepperRow.add(countLeftArrow).size(56, 56).padRight(28);
        stepperRow.add(countBox).size(200, 115);
        stepperRow.add(countRightArrow).size(56, 56).padLeft(28);

        contentTable.add(stepperRow).center().row();

        // Parchment scroll background
        Image scrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        scrollBg.setScaling(Scaling.stretch);

        Stack scrollStack = new Stack();
        scrollStack.add(scrollBg);
        scrollStack.add(contentTable);

        // 3. Bottom Action Buttons: BACK & ENTER
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = UIUtils.createRoundedDrawable(180, 52, 18, pillBrown);
        btnStyle.font = badgeFont;
        btnStyle.fontColor = Color.WHITE;

        TextButton backButton = new TextButton("BACK", btnStyle);
        TextButton enterButton = new TextButton("ENTER", btnStyle);
        UIUtils.addScaleHoverEffect(backButton);
        UIUtils.addScaleHoverEffect(enterButton);

        // Clicking Back returns to Game Mode selection
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                playerCountTable.setVisible(false);
                gameModeTable.setVisible(true);
            }
        });

        // Clicking Enter opens Character Select for the chosen number of players
        enterButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                playerCountTable.setVisible(false);
                openCharacterSelect(passPlayPlayerCount);
            }
        });

        Table bottomBtnTable = new Table();
        bottomBtnTable.add(backButton).size(180, 52).padRight(30);
        bottomBtnTable.add(enterButton).size(180, 52);

        // 4. Outer Player Count Table
        playerCountTable = new Table();
        playerCountTable.setFillParent(true);
        playerCountTable.setVisible(false);

        playerCountTable.add(badgeTable).size(420, 60).padBottom(-22).center().row();
        playerCountTable.add(scrollStack).size(840, 646).center().row();
        playerCountTable.add(bottomBtnTable).padTop(-22).center().row();

        // Initial visual state of arrows
        updatePlayerCountDisplay();
    }

    /**
     * Updates the player count display and visually dims arrow buttons at min/max limits (2 or 4).
     */
    private void updatePlayerCountDisplay() {
        if (countNumLabel == null) return;
        countNumLabel.setText(String.valueOf(passPlayPlayerCount));

        // Lock / dim left arrow if at minimum (2 players)
        if (passPlayPlayerCount <= 2) {
            countLeftArrow.setColor(1f, 1f, 1f, 0.4f);
            countLeftArrow.setDisabled(true);
        } else {
            countLeftArrow.setColor(Color.WHITE);
            countLeftArrow.setDisabled(false);
        }

        // Lock / dim right arrow if at maximum (4 players)
        if (passPlayPlayerCount >= 4) {
            countRightArrow.setColor(1f, 1f, 1f, 0.4f);
            countRightArrow.setDisabled(true);
        } else {
            countRightArrow.setColor(Color.WHITE);
            countRightArrow.setDisabled(false);
        }
    }

    /**
     * Initializes animated walking spritesheets for each character class (224x224 enlarged frames).
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
            float frameDur = (cls == CharacterClass.CAT) ? 0.12f : 0.10f;
            Animation<TextureRegion> anim = new Animation<>(frameDur, frames);
            anim.setPlayMode(Animation.PlayMode.LOOP);
            charAnimations.put(cls, anim);
        }
    }

    /**
     * Builds the Character Selection dialog: carousel with character preview,
     * stats, locking mechanism (Approach A), and confirmation buttons.
     */
    private void buildCharacterSelectDialog(Color darkBrown, Color pillBrown) {
        // --- 1. Top Badge (Dynamic: shows which player is picking) ---
        characterBadge = new Label("PLAYER 1: SELECT HERO", new Label.LabelStyle(badgeFont, Color.WHITE));
        characterBadge.setAlignment(Align.center);

        Table badgeTable = new Table();
        badgeTable.setBackground(UIUtils.createRoundedDrawable(420, 60, 20, pillBrown));
        badgeTable.add(characterBadge).pad(8, 35, 8, 35);

        // --- 2. Carousel Row (< arrow, frame with sprite, > arrow) ---
        TextButton.TextButtonStyle arrowStyle = new TextButton.TextButtonStyle();
        arrowStyle.up = UIUtils.createRoundedDrawable(54, 54, 18, pillBrown);
        arrowStyle.font = badgeFont;
        arrowStyle.fontColor = Color.WHITE;

        TextButton leftArrow = new TextButton("<", arrowStyle);
        TextButton rightArrow = new TextButton(">", arrowStyle);
        UIUtils.addScaleHoverEffect(leftArrow);
        UIUtils.addScaleHoverEffect(rightArrow);

        // Arrow navigation clicks
        leftArrow.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.ITEM_EQUIP, 0.7f, 0.1f);
                CharacterClass[] values = CharacterClass.values();
                currentCharacterIndex = (currentCharacterIndex - 1 + values.length) % values.length;
                updateCharacterDisplay();
            }
        });

        rightArrow.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.ITEM_EQUIP, 0.7f, 0.1f);
                CharacterClass[] values = CharacterClass.values();
                currentCharacterIndex = (currentCharacterIndex + 1) % values.length;
                updateCharacterDisplay();
            }
        });

        // =========================================================================
        // Character Frame with Sprite Placeholder
        // NOTE FOR USER: The image below uses Assets.getCharacterTexture(cls).
        // When your real pixel character sprites are added into assets/img/,
        // they will automatically render inside this frame!
        // =========================================================================
        Table frameBox = new Table();
        frameBox.setBackground(UIUtils.createRoundedDrawable(190, 190, 20, Color.valueOf("#4a2307")));

        characterImage = new Image(game.getAssetManager().get(Assets.CHAR_TURTLE));
        characterImage.setScaling(Scaling.fit);
        frameBox.add(characterImage).size(175, 175).center();

        // Lock Banner (Displayed directly over the character if already taken by Player 1)
        lockBanner = new Label("LOCKED BY PLAYER 1", new Label.LabelStyle(modeSubtitleFont, Color.valueOf("#ff5252")));
        lockBanner.setAlignment(Align.center);
        lockBanner.setVisible(false);

        // Carousel row assembly
        Table carouselRow = new Table();
        carouselRow.add(leftArrow).size(54, 54).padRight(28);
        carouselRow.add(frameBox).size(190, 190);
        carouselRow.add(rightArrow).size(54, 54).padLeft(28);

        // --- 3. Clean Text & Stats Below Frame ---
        charNameLabel  = new Label("TURTLE", new Label.LabelStyle(modeTitleFont, darkBrown));
        charTitleLabel = new Label("Iron Guardian", new Label.LabelStyle(modeSubtitleFont, Color.valueOf("#854b25")));
        charStatsLabel = new Label("HP 120   |   ATK 4   |   DEF 7", new Label.LabelStyle(statFont, darkBrown));
        charPerkLabel  = new Label("Perk: Iron Shell\n(Turtle Shell Buckler)", new Label.LabelStyle(perkFont, Color.valueOf("#6d3b17")));

        charNameLabel.setAlignment(Align.center);
        charTitleLabel.setAlignment(Align.center);
        charStatsLabel.setAlignment(Align.center);
        charPerkLabel.setAlignment(Align.center);

        // Enable wrapping so text never spills over onto the scroll curls
        charTitleLabel.setWrap(true);
        charPerkLabel.setWrap(true);
        lockBanner.setWrap(true);

        // Stack the content inside the parchment area (confined to 460px width, well within the two curls)
        Table contentTable = new Table();
        contentTable.add(carouselRow).padBottom(4).center().row();
        contentTable.add(lockBanner).width(460).padBottom(2).center().row();
        contentTable.add(charNameLabel).width(460).center().row();
        contentTable.add(charTitleLabel).width(460).padBottom(6).center().row();
        contentTable.add(charStatsLabel).width(460).padBottom(4).center().row();
        contentTable.add(charPerkLabel).width(460).center().row();

        Image scrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        scrollBg.setScaling(Scaling.stretch);

        Stack scrollStack = new Stack();
        scrollStack.add(scrollBg);
        scrollStack.add(contentTable);

        // --- 4. Bottom Action Buttons: BACK & ENTER ---
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.up = UIUtils.createRoundedDrawable(180, 52, 18, pillBrown);
        btnStyle.font = badgeFont;
        btnStyle.fontColor = Color.WHITE;

        TextButton backButton = new TextButton("BACK", btnStyle);
        enterButton = new TextButton("ENTER", btnStyle);
        UIUtils.addScaleHoverEffect(backButton);
        UIUtils.addScaleHoverEffect(enterButton);

        // Back Button logic: allows Player 2+ to return to previous player, or Player 1 to return to previous menu
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                if (selectingPlayerIndex > 0) {
                    // Back up to the previous player and unlock their pick
                    selectingPlayerIndex--;
                    chosenClasses.remove(selectingPlayerIndex);
                    updateCharacterDisplay();
                } else {
                    // Return to previous screen: Player Count if multi-player, or Game Mode if solo
                    characterSelectTable.setVisible(false);
                    if (totalSelectingPlayers > 1) {
                        playerCountTable.setVisible(true);
                    } else {
                        gameModeTable.setVisible(true);
                    }
                }
            }
        });

        // Enter Button logic: locks the character for current player or starts the game
        enterButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                CharacterClass currentClass = CharacterClass.values()[currentCharacterIndex];

                // If already chosen by someone else (Approach A), do not allow selection
                if (chosenClasses.contains(currentClass)) return;

                // Lock choice for current player
                chosenClasses.add(currentClass);
                selectingPlayerIndex++;

                if (selectingPlayerIndex < totalSelectingPlayers) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.ITEM_EQUIP);
                    // Next player's turn: auto-advance to the first available class
                    for (int i = 0; i < CharacterClass.values().length; i++) {
                        if (!chosenClasses.contains(CharacterClass.values()[i])) {
                            currentCharacterIndex = i;
                            break;
                        }
                    }
                    updateCharacterDisplay();
                } else {
                    // All players are ready! Launch the game with their selected characters
                    game.getAudioManager().playSound(AudioManager.SoundEffect.SANCTUARY_HEAL);
                    game.startGameWithClasses(chosenClasses);
                }
            }
        });

        // Bottom row holding both buttons
        Table bottomBtnTable = new Table();
        bottomBtnTable.add(backButton).size(180, 52).padRight(30);
        bottomBtnTable.add(enterButton).size(180, 52);

        // --- 5. Outer Character Select Table ---
        characterSelectTable = new Table();
        characterSelectTable.setFillParent(true);
        characterSelectTable.setVisible(false);

        characterSelectTable.add(badgeTable).size(420, 60).padBottom(-22).center().row();
        characterSelectTable.add(scrollStack).size(840, 646).center().row();
        characterSelectTable.add(bottomBtnTable).padTop(-22).center().row();
    }

    /**
     * Updates the Character Select display to reflect the current character and player turn.
     * Implements Approach A: shows locked status and dims/disables ENTER if already taken.
     */
    private void updateCharacterDisplay() {
        CharacterClass[] classes = CharacterClass.values();
        CharacterClass cls = classes[currentCharacterIndex];

        // 1. Text labels
        charNameLabel.setText(cls.getName().toUpperCase());
        charTitleLabel.setText(cls.getTitle());
        charStatsLabel.setText("HP " + cls.getBaseHealth() + "   |   ATK " + cls.getBaseAttack() + "   |   DEF " + cls.getBaseDefense());

        // Keep perk description concise, readable, and split onto two lines so it stays inside the flat parchment
        String perk = cls.getPerkDescription();
        if (perk.contains(":")) {
            perk = perk.split(":")[0];
        }
        charPerkLabel.setText("Perk: " + perk + "\n(" + cls.getStartingItemName() + ")");

        // 2. Character sprite texture & initial frame
        charAnimTimer = 0f;
        Animation<TextureRegion> anim = charAnimations.get(cls);
        if (anim != null) {
            characterImage.setDrawable(new TextureRegionDrawable(anim.getKeyFrames()[0]));
        } else {
            Texture charTex = game.getAssetManager().get(Assets.getCharacterTexture(cls));
            characterImage.setDrawable(new TextureRegionDrawable(new TextureRegion(charTex)));
        }

        // 3. Top badge title: indicates which player is choosing
        if (totalSelectingPlayers == 1) {
            characterBadge.setText("CHOOSE YOUR HERO");
        } else {
            characterBadge.setText("PLAYER " + (selectingPlayerIndex + 1) + ": SELECT HERO");
        }

        // 4. Approach A (Locking mechanism):
        // If this character is already chosen by a previous player, show LOCKED and disable ENTER
        if (chosenClasses.contains(cls)) {
            int takenBy = chosenClasses.indexOf(cls) + 1;
            lockBanner.setText("LOCKED BY PLAYER " + takenBy);
            lockBanner.setVisible(true);

            // Dim sprite slightly
            characterImage.setColor(0.35f, 0.35f, 0.35f, 0.5f);

            // Disable Enter button
            enterButton.setDisabled(true);
            enterButton.setText("TAKEN");
            enterButton.setColor(Color.valueOf("#7a6356"));
        } else {
            lockBanner.setVisible(false);
            characterImage.setColor(Color.WHITE);

            enterButton.setDisabled(false);
            enterButton.setText("ENTER");
            enterButton.setColor(Color.valueOf("#854b25"));
        }
    }

    /**
     * Builds the Settings GUI: the parchment scroll, volume sliders, and back button.
     */
    private void buildSettingsDialog(Color darkBrown, Color pillBrown) {
        Slider.SliderStyle sliderStyle = UIUtils.createSliderStyle(darkBrown, darkBrown);

        // 1. Music volume controls
        Slider musicSlider = new Slider(0, 10, 1, false, sliderStyle);
        int musicVal = Math.round(game.getAudioManager().getMusicVolume() * 10f);
        musicSlider.setValue(musicVal);
        Label musicNum = new Label(String.valueOf(musicVal), new Label.LabelStyle(numFont, darkBrown));

        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int val = (int) musicSlider.getValue();
                musicNum.setText(String.valueOf(val));
                game.getAudioManager().setMusicVolume(val / 10f);
            }
        });

        // 2. Sound Effects (SFX) volume controls
        Slider sfxSlider = new Slider(0, 10, 1, false, sliderStyle);
        int sfxVal = Math.round(game.getAudioManager().getSfxVolume() * 10f);
        sfxSlider.setValue(sfxVal);
        Label sfxNum = new Label(String.valueOf(sfxVal), new Label.LabelStyle(numFont, darkBrown));

        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int val = (int) sfxSlider.getValue();
                sfxNum.setText(String.valueOf(val));
                game.getAudioManager().setSfxVolume(val / 10f);
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
            }
        });

        // Load speaker and musical note icons
        Image soundIcon = new Image(game.getAssetManager().get(Assets.ICON_SOUND));
        Image musicIcon = new Image(game.getAssetManager().get(Assets.ICON_MUSIC));

        // 3. Layout sliders centered over the parchment
        Table contentTable = new Table();

        // Music row
        Label musicLabel = new Label("MUSIC", new Label.LabelStyle(textFont, darkBrown));
        contentTable.add(musicLabel).colspan(3).center().padBottom(10).row();

        contentTable.add(musicIcon).size(38, 38).padRight(16);
        contentTable.add(musicSlider).width(360).padRight(16);
        contentTable.add(musicNum).width(44).left().padBottom(26).row();

        // SFX row
        Label sfxLabel = new Label("SFX", new Label.LabelStyle(textFont, darkBrown));
        contentTable.add(sfxLabel).colspan(3).center().padBottom(10).row();

        contentTable.add(soundIcon).size(38, 38).padRight(16);
        contentTable.add(sfxSlider).width(360).padRight(16);
        contentTable.add(sfxNum).width(44).left().row();

        // 4. Layer content table over the scroll graphic
        Image scrollBg = new Image(game.getAssetManager().get(Assets.SCROLL));
        scrollBg.setScaling(Scaling.stretch);

        Stack scrollStack = new Stack();
        scrollStack.add(scrollBg);
        scrollStack.add(contentTable);

        // 5. Top "SETTINGS" badge with generous horizontal padding
        Label settingsBadge = new Label("SETTINGS", new Label.LabelStyle(badgeFont, Color.WHITE));
        settingsBadge.setAlignment(Align.center);

        Table badgeTable = new Table();
        badgeTable.setBackground(UIUtils.createRoundedDrawable(280, 60, 20, pillBrown));
        badgeTable.add(settingsBadge).pad(8, 40, 8, 40);

        // 6. Bottom "BACK" button
        TextButton.TextButtonStyle backStyle = new TextButton.TextButtonStyle();
        backStyle.up = UIUtils.createRoundedDrawable(180, 52, 18, pillBrown);
        backStyle.font = badgeFont;
        backStyle.fontColor = Color.WHITE;

        TextButton backButton = new TextButton("BACK", backStyle);
        UIUtils.addScaleHoverEffect(backButton);

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                settingsTable.setVisible(false);
                menuTable.setVisible(true);
            }
        });

        // 7. Outer Settings Table
        settingsTable = new Table();
        settingsTable.setFillParent(true);
        settingsTable.setVisible(false);

        settingsTable.add(badgeTable).size(280, 60).padBottom(-22).center().row();
        settingsTable.add(scrollStack).size(840, 646).center().row();
        settingsTable.add(backButton).size(180, 52).padTop(-22).center().row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render background video frame across the full window
        if (videoPlayer != null) {
            videoPlayer.update();
            Texture frame = videoPlayer.getTexture();
            if (frame != null) {
                int screenW = Gdx.graphics.getWidth();
                int screenH = Gdx.graphics.getHeight();
                // Ensure OpenGL viewport and SpriteBatch matrix cover the full window
                Gdx.gl.glViewport(0, 0, screenW, screenH);
                batch.getProjectionMatrix().setToOrtho2D(0, 0, screenW, screenH);

                batch.begin();
                batch.draw(frame, 0, 0, screenW, screenH);
                batch.end();
            }
        }

        if (characterSelectTable != null && characterSelectTable.isVisible()) {
            charAnimTimer += delta;
            CharacterClass currentCls = CharacterClass.values()[currentCharacterIndex];
            Animation<TextureRegion> charAnim = charAnimations.get(currentCls);
            if (charAnim != null && characterImage != null) {
                TextureRegion frame = charAnim.getKeyFrame(charAnimTimer, true);
                characterImage.setDrawable(new TextureRegionDrawable(frame));
            }
        }

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Prevent math exceptions when minimized
        if (width <= 0 || height <= 0) return;

        stage.getViewport().update(width, height, true);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
        if (batch != null) batch.dispose();
        if (videoPlayer != null) videoPlayer.dispose();

        // Clean up generated FreeType fonts
        if (badgeFont != null) badgeFont.dispose();
        if (textFont != null) textFont.dispose();
        if (numFont != null) numFont.dispose();
        if (modeTitleFont != null) modeTitleFont.dispose();
        if (modeSubtitleFont != null) modeSubtitleFont.dispose();
        if (instructionFont != null) instructionFont.dispose();
        if (statFont != null) statFont.dispose();
        if (perkFont != null) perkFont.dispose();
        if (countNumFont != null) countNumFont.dispose();
    }
}
