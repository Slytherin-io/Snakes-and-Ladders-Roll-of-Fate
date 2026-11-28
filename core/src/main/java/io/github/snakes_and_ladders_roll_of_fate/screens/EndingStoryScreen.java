package io.github.snakes_and_ladders_roll_of_fate.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
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
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;
import io.github.snakes_and_ladders_roll_of_fate.ui.UIUtils;

/**
 * Interactive Cinematic Ending Story Slideshow Screen.
 *
 * Displays a poignant storybook epilogue showing the animal adventurers finally finding
 * peace and setting down their burdens at the journey's end.
 *
 * Features:
 * - Bespoke storybook illustration slides with smooth cross-fade transitions.
 * - Subtle Ken Burns cinematic drift on imagery.
 * - Multi-beat personalized narrative tailored to the party's champion and companions.
 * - Keyboard (Arrows / Space / Enter / Esc) and mouse navigation.
 * - Smooth hand-off directly to GameSummaryScreen.
 */
public class EndingStoryScreen implements Screen {

    /**
     * Data representation of an individual ending slide.
     */
    public static class EndingSlide {
        public final String chapterTag;
        public final String title;
        public final String subtitle;
        public final String prose;
        public final String imagePath;
        public final Color accentColor;
        public final AudioManager.SoundEffect soundEffect;
        private Texture cachedTexture;
        private boolean loadAttempted = false;
        private boolean ownsTexture = false;

        public EndingSlide(String chapterTag, String title, String subtitle, String prose,
                           String imagePath, Color accentColor, AudioManager.SoundEffect soundEffect) {
            this.chapterTag = chapterTag;
            this.title = title;
            this.subtitle = subtitle;
            this.prose = prose;
            this.imagePath = imagePath;
            this.accentColor = accentColor;
            this.soundEffect = soundEffect;
        }

        public Texture getTexture(RollOfFateGame game) {
            if (cachedTexture != null) return cachedTexture;
            if (game != null && game.getAssetManager() != null && imagePath != null) {
                try {
                    if (game.getAssetManager().isLoaded(imagePath, Texture.class)) {
                        cachedTexture = game.getAssetManager().get(imagePath, Texture.class);
                        ownsTexture = false;
                        return cachedTexture;
                    }
                } catch (Exception ignored) {}
            }
            if (!loadAttempted) {
                loadAttempted = true;
                try {
                    if (imagePath != null && Gdx.files.internal(imagePath).exists()) {
                        cachedTexture = new Texture(Gdx.files.internal(imagePath));
                        cachedTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                        ownsTexture = true;
                    }
                } catch (Exception e) {
                    Gdx.app.log("EndingStoryScreen", "Unable to load slide texture: " + imagePath, e);
                }
            }
            return cachedTexture;
        }

        public void dispose() {
            if (cachedTexture != null && ownsTexture) {
                cachedTexture.dispose();
            }
            cachedTexture = null;
        }
    }

    private final RollOfFateGame game;
    private final Player hero;
    private final List<Player> party;
    private final EndingType endingType;
    private final boolean isVictory;

    private SpriteBatch batch;
    private Stage stage;

    // Fonts
    private BitmapFont chapterFont;
    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private BitmapFont proseFont;
    private BitmapFont buttonFont;
    private BitmapFont indicatorFont;

    // Slide state & transitions
    private final List<EndingSlide> slides = new ArrayList<>();
    private int currentSlideIndex = 0;
    private int prevSlideIndex = -1;
    private float slideTimer = 0f;
    private float transitionTimer = 1.0f;
    private static final float TRANSITION_DURATION = 0.65f;

    // Procedural Fallback Textures
    private Texture whitePixelTex;
    private Texture fallbackBgTex;
    private Texture vignetteTex;

    // UI Actors
    private Table storyCard;
    private Label chapterLabel;
    private Label titleLabel;
    private Label subtitleLabel;
    private Label proseLabel;
    private Label indicatorLabel;
    private Table navRow;
    private TextButton prevBtn;
    private TextButton nextBtn;
    private Cell<TextButton> nextBtnCell;
    private TextButton skipBtn;

    public EndingStoryScreen(RollOfFateGame game, Player hero, EndingType endingType) {
        this(game, hero, (game.getPlayers() != null) ? game.getPlayers() : new ArrayList<>(), endingType);
    }

    public EndingStoryScreen(RollOfFateGame game, Player hero, List<Player> party, EndingType endingType) {
        this.game = game;
        this.endingType = (endingType != null) ? endingType : EndingType.REBIRTH_TRIUMPH;
        this.isVictory = (this.endingType == EndingType.REBIRTH_TRIUMPH);
        this.party = (party != null && !party.isEmpty()) ? new ArrayList<>(party) : ((game.getPlayers() != null) ? new ArrayList<>(game.getPlayers()) : new ArrayList<>());

        if (hero != null) {
            this.hero = hero;
        } else if (game.getCurrentPlayer() != null) {
            this.hero = game.getCurrentPlayer();
        } else if (!this.party.isEmpty()) {
            this.hero = this.party.get(0);
        } else {
            this.hero = null;
        }

        buildSlides();
    }

    /**
     * Assembles the chronological slide sequence honoring the run's narrative context.
     */
    private void buildSlides() {
        slides.clear();
        boolean isSolo = party == null || party.size() <= 1;
        CharacterClass heroClass = (hero != null && hero.getCharacterClass() != null)
            ? hero.getCharacterClass()
            : CharacterClass.CAT;

        String heroName = (hero != null) ? hero.getName().toUpperCase() : "THE TRAVELER";

        if (isVictory) {
            // Beat 1: The Hundredth Stone - Glorious Summit Triumph
            slides.add(new EndingSlide(
                "EPILOGUE | THE SUMMIT",
                "TRIUMPH AT THE HUNDREDTH STONE",
                "The Gates of Light Break Across the Cloud Sea",
                "The final stone is conquered! The Dawn Warden yields, and the bitter mountain wind transforms into a glorious celestial dawn. "
                + "Beyond the ancient precipice, brilliant morning light cascades across the endless sea of clouds.",
                "img/ending_dawn_victory.png",
                Color.valueOf("#ffd54f"),
                AudioManager.SoundEffect.DAWN_REBIRTH
            ));

            // Beat 2: The Guardians' Accolade (tailored for solo vs. fellowship)
            slides.add(new EndingSlide(
                "EPILOGUE | THE GUARDIANS",
                "HONOR OF THE DAWN",
                "The Dawn Warden Bows & The Pale Shepherd Salutes",
                isSolo
                    ? "The Dawn Warden spreads its great solar wings, bowing its crowned head in noble reverence to the victor: 'Rise, champion. You stood unwavering before the celestial storm.' "
                      + "Beside the ancient arch, The Pale Shepherd lowers his golden scythe in solemn salute to the living legend."
                    : "The Dawn Warden spreads its great solar wings, bowing its crowned head in noble reverence to your fellowship: 'Rise, champions. Together, your unwavering spirits have conquered the summit of fate.' "
                      + "Beside the ancient arch, The Pale Shepherd lowers his golden scythe in solemn salute to the living fellowship.",
                "img/ending_warden_concession.png",
                Color.valueOf("#ffe0a3"),
                null
            ));

            // Beat 3: The Champion's Triumph
            slides.add(createChampionVictorySlide(heroClass, heroName, isSolo));

            // Beat 4: Solo Ascension vs. Fellowship Triumph
            if (isSolo) {
                slides.add(new EndingSlide(
                    "EPILOGUE | ASCENSION",
                    "A LEGEND WRITTEN IN STONE",
                    heroName + " - Champion of the Mountain of Fate",
                    "Golden sunlight cascades across the heights, honoring the lone hero whose courage never faltered. "
                    + "Your triumphant ascent will be sung across the valleys below, an eternal testament to those who dare defy fate.",
                    "img/ending_dawn_victory.png",
                    Color.valueOf("#ffd54f"),
                    AudioManager.SoundEffect.SANCTUARY_HEAL
                ));
            } else {
                slides.add(new EndingSlide(
                    "EPILOGUE | THE FELLOWSHIP",
                    "TRIUMPH OF THE FELLOWSHIP",
                    "Champions Standing Together in the Dawn of Glory",
                    buildFellowshipText("Through venomous coils, dizzying ladders, and divine judgment, ",
                        " stood unbroken. Together they claimed the summit of fate, their joyful cheers echoing forever across the skies."),
                    "img/ending_fellowship_victory.png",
                    Color.valueOf("#a5d6a7"),
                    AudioManager.SoundEffect.SANCTUARY_HEAL
                ));
            }

        } else {
            // Beat 1: Mountain Hush
            slides.add(new EndingSlide(
                "EPILOGUE | TWILIGHT",
                "THE MOUNTAIN HUSH",
                "The Wind Softens Over the Ascending Stones",
                "The struggle ceases, and the cold sting of stone and venom gently fades into the mist. "
                + "A quiet twilight settles across the ridge in soothing stillness.",
                "img/ending_dawn_summit.png",
                Color.valueOf("#90caf9"),
                AudioManager.SoundEffect.SANCTUARY_HEAL
            ));

            // Beat 2: The Shepherd's Solace
            slides.add(new EndingSlide(
                "EPILOGUE | PEACEFUL REST",
                "THE GENTLE REAPER",
                "Held in the Quiet Solace of The Pale Shepherd",
                isSolo
                    ? "The Pale Shepherd hovers upon the stones, lowering his golden scythe and draping a warm blanket of quiet rest over tired limbs. "
                      + "'No more cold, little traveler,' whispers the crimson cowl. 'Close your eyes - you have done well.'"
                    : "The Pale Shepherd hovers upon the stones, lowering his golden scythe and gathering every weary soul beneath his quiet care. "
                      + "'No more cold, little travelers,' whispers the crimson cowl. 'Rest side by side - you have done well.'",
                "img/ending_guardians_farewell.png",
                Color.valueOf("#ce93d8"),
                null
            ));

            // Beat 3: The Hero's Slumber
            slides.add(createChampionRestSlide(heroClass, heroName, isSolo));

            // Beat 4 (Pass & Play only): Companions Sleeping Side by Side
            if (!isSolo) {
                slides.add(new EndingSlide(
                    "EPILOGUE | SLUMBER",
                    "TOGETHER BEYOND DAWN",
                    "Safe Within the Memory of the Mountain",
                    buildFellowshipText("Beneath starlit skies, ",
                        " rest safely together within the lantern's gentle glow. None walk into the quiet night alone."),
                    "img/ending_fellowship_rest.png",
                    Color.valueOf("#ffe0a3"),
                    null
                ));
            }
        }
    }

    private String buildFellowshipText(String prefix, String suffix) {
        if (party == null || party.isEmpty()) {
            return prefix + "the companions" + suffix;
        }
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < party.size(); i++) {
            if (i > 0) {
                names.append(i == party.size() - 1 ? " and " : ", ");
            }
            names.append(party.get(i).getName());
        }
        return prefix + names + suffix;
    }

    private EndingSlide createChampionVictorySlide(CharacterClass cClass, String name, boolean isSolo) {
        switch (cClass) {
            case TURTLE:
                return new EndingSlide(
                    "EPILOGUE | THE CHAMPION'S TRIUMPH",
                    "THE UNBREAKABLE AEGIS",
                    name + " - The Iron Guardian Ascends the Throne of Dawn",
                    (isSolo
                        ? "His iron shell bore the crushing fury of stone, venom, and the Warden's divine wrath without cracking. "
                        : "His steadfast shield stood as the bulwark of his fellowship against the summit's divine trial. ")
                    + "Standing victorious at the peak with shield held high, the steadfast guardian claims his rightful honor.",
                    "img/ending_turtle_victory.png",
                    Color.valueOf("#81c784"),
                    null
                );
            case FOX:
                return new EndingSlide(
                    "EPILOGUE | THE CHAMPION'S TRIUMPH",
                    "THE CUNNING CONQUEROR",
                    name + " - Master of Fate's Ascending Trails",
                    (isSolo
                        ? "Outwitting divine wrath with blinding speed and uncanny cunning, the clever scout bested the solar guardian. "
                        : "With razor-sharp wits and swift leaps, the nimble scout guided his company through the Warden's celestial storms. ")
                    + "Gazing boldly into the golden horizon, his tail held proud in the morning wind, the summit is his to claim.",
                    "img/ending_fox_victory.png",
                    Color.valueOf("#ffb74d"),
                    null
                );
            case RACCOON:
                return new EndingSlide(
                    "EPILOGUE | THE CHAMPION'S TRIUMPH",
                    "THE CROWNED FORTUNE-SEEKER",
                    name + " - Destiny's Most Audacious Hero",
                    (isSolo
                        ? "Armed with scraps, improvised alchemy, and boundless audacity, the little scavenger triumphed against impossible odds. "
                        : "Every shared remedy and resourceful concoction carried his companions to the very pinnacle of fate. ")
                    + "Leaping with pure joy at the precipice, holding the blazing summit relic toward the heavens, his triumph is complete.",
                    "img/ending_raccoon_victory.png",
                    Color.valueOf("#ffcc80"),
                    null
                );
            case CAT:
            default:
                return new EndingSlide(
                    "EPILOGUE | THE CHAMPION'S TRIUMPH",
                    "SOVEREIGN OF NINE LIVES",
                    name + " - The Shadow-Swift Monarch of the Dawn",
                    (isSolo
                        ? "Leaping across ninety-nine perilous stones with fearless grace, the swift prowler danced through the Warden's golden blades. "
                        : "With fearless grace and feline intuition, she led her fellowship through the trials of the mountain to glorious victory. ")
                    + "Standing proud upon the summit crag, her glowing agility charm ablaze with morning light, she surveys a conquered world.",
                    "img/ending_cat_victory.png",
                    Color.valueOf("#ffd54f"),
                    null
                );
        }
    }

    private EndingSlide createChampionRestSlide(CharacterClass cClass, String name, boolean isSolo) {
        switch (cClass) {
            case TURTLE:
                return new EndingSlide(
                    "EPILOGUE | THE HERO'S REST",
                    "THE UNBURDENED SHELL",
                    name + " - The Iron Guardian Lays Down His Shield",
                    (isSolo
                        ? "Through countless trials he bore the crushing weight of stone and venom upon his shell. "
                        : "Through countless trials he bore stone and venom upon his shell to shield his companions. ")
                    + "Here at the mountain's crest, his heavy buckler rests, and the sentinel's watch is finally done.",
                    "img/ending_turtle_rest.png",
                    Color.valueOf("#81c784"),
                    null
                );
            case FOX:
                return new EndingSlide(
                    "EPILOGUE | THE HERO'S REST",
                    "THE HEARTH OF PEACE",
                    name + " - The Cunning Trickster Finds Quiet Solace",
                    "A climb survived on narrow escapes and razor-sharp wit gives way to stillness. "
                    + "With no trap lurking in the brush, the clever traveler curls beneath the twilight pines in peace.",
                    "img/ending_fox_rest.png",
                    Color.valueOf("#ffb74d"),
                    null
                );
            case RACCOON:
                return new EndingSlide(
                    "EPILOGUE | THE HERO'S REST",
                    "THE SATCHEL LAID DOWN",
                    name + " - The Crafty Scavenger Embraces Stillness",
                    "He carried every scrap, remedy, and relic up the perilous slope, fearful of what might be lost. "
                    + "At the crest, he gently unbuckles his heavy satchel and rests with everything he needed all along.",
                    "img/ending_raccoon_rest.png",
                    Color.valueOf("#ffcc80"),
                    null
                );
            case CAT:
            default:
                return new EndingSlide(
                    "EPILOGUE | THE HERO'S REST",
                    "THE SUNLIT SLEEP",
                    name + " - The Nimble Prowler At Peace",
                    "For ninety-nine stones she leapt ahead of the serpent's strike, shadow-swift and unyielding. "
                    + "Now, in the sunlit grass, she no longer needs to outrun fate - she only needs to rest.",
                    "img/ending_cat_rest.png",
                    Color.valueOf("#ffd54f"),
                    null
                );
        }
    }

    @Override
    public void show() {
        if (game.getAudioManager() != null) {
            game.getAudioManager().playMusic(
                isVictory ? AudioManager.MusicTrack.EPILOGUE_VICTORY : AudioManager.MusicTrack.EPILOGUE_REST,
                true
            );
        }

        batch = new SpriteBatch();
        stage = new Stage(new ExtendViewport(1920, 1080));
        Gdx.input.setInputProcessor(stage);

        // Procedural Textures
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(Color.WHITE);
        px.fill();
        whitePixelTex = new Texture(px);
        px.dispose();

        // Procedural radial vignette
        Pixmap vig = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        for (int vy = 0; vy < 64; vy++) {
            for (int vx = 0; vx < 64; vx++) {
                float dx = (vx - 31.5f) / 32f;
                float dy = (vy - 31.5f) / 32f;
                float dist = Math.min(1f, (float) Math.sqrt(dx * dx + dy * dy));
                float alpha = dist * dist;
                vig.setColor(0f, 0f, 0f, alpha);
                vig.drawPixel(vx, vy);
            }
        }
        vignetteTex = new Texture(vig);
        vignetteTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        vig.dispose();

        // Fallback backdrop texture
        try {
            fallbackBgTex = game.getAssetManager().get(isVictory ? Assets.ACT1_BG : Assets.ACT2_BG);
        } catch (Exception e) {
            fallbackBgTex = null;
        }

        // Initialize Crisp Fredoka Fonts
        Color goldColor = Color.valueOf("#ffd54f");
        Color warmIvory = Color.valueOf("#fbf2dc");

        chapterFont   = UIUtils.createFont("ui/Fredoka-Bold.ttf", 16, goldColor);
        titleFont     = UIUtils.createFont("ui/Fredoka-Bold.ttf", 28, Color.WHITE);
        subtitleFont  = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 18, warmIvory);
        proseFont     = UIUtils.createFont("ui/Fredoka-SemiBold.ttf", 21, Color.WHITE);
        buttonFont    = UIUtils.createFont("ui/Fredoka-Bold.ttf", 21, Color.WHITE);
        indicatorFont = UIUtils.createFont("ui/Fredoka-Bold.ttf", 18, goldColor);

        buildUI();
        applySlide(0, false);

        // Keyboard & Global Click Navigation
        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D
                        || keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
                    advanceSlide();
                    return true;
                } else if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A
                        || keycode == Input.Keys.BACKSPACE) {
                    previousSlide();
                    return true;
                } else if (keycode == Input.Keys.ESCAPE) {
                    finishEnding();
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // If clicked above the bottom story card, advance slide
                if (y > 300 && button == Input.Buttons.LEFT) {
                    advanceSlide();
                    return true;
                }
                return false;
            }
        });
    }

    private void buildUI() {
        stage.clear();

        // 1. Top Bar: SKIP TO SUMMARY (Top-Right)
        Table topBar = new Table();
        topBar.setFillParent(true);
        topBar.top().right().padTop(24).padRight(36);

        TextButton.TextButtonStyle skipStyle = new TextButton.TextButtonStyle();
        skipStyle.up = UIUtils.createRoundedDrawable(220, 48, 12, Color.valueOf("#3b1f13cc"), Color.valueOf("#854b25"), 1);
        skipStyle.over = UIUtils.createRoundedDrawable(220, 48, 12, Color.valueOf("#54280ecc"), Color.valueOf("#ffd54f"), 1);
        skipStyle.font = UIUtils.createFont("ui/Fredoka-Bold.ttf", 16, Color.valueOf("#ffd54f"));
        skipStyle.fontColor = Color.valueOf("#ffd54f");

        skipBtn = new TextButton("SKIP TO SUMMARY  >>", skipStyle);
        UIUtils.addScaleHoverEffect(skipBtn);
        skipBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (game.getAudioManager() != null) {
                    game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
                }
                finishEnding();
            }
        });
        topBar.add(skipBtn).size(220, 48);
        stage.addActor(topBar);

        // 2. Bottom Storybook Card (1360x240 px, anchored at bottom)
        Table bottomContainer = new Table();
        bottomContainer.setFillParent(true);
        bottomContainer.bottom().padBottom(36);

        Color cardBg = isVictory ? Color.valueOf("#180d07f2") : Color.valueOf("#0d121df2");
        Color cardBorder = isVictory ? Color.valueOf("#854b25") : Color.valueOf("#4b5b75");

        storyCard = new Table();
        storyCard.setBackground(UIUtils.createRoundedDrawable(1360, 240, 16, cardBg, cardBorder, 2));
        storyCard.pad(18, 28, 16, 28);

        // Left Content Column (Story Header & Prose)
        Table contentTable = new Table();
        chapterLabel = new Label("EPILOGUE | THE SUMMIT", new Label.LabelStyle(chapterFont, chapterFont.getColor()));
        chapterLabel.setAlignment(Align.left);
        contentTable.add(chapterLabel).left().row();

        titleLabel = new Label("THE HUNDREDTH STONE", new Label.LabelStyle(titleFont, Color.valueOf("#ffd54f")));
        titleLabel.setAlignment(Align.left);
        contentTable.add(titleLabel).left().padTop(2).row();

        subtitleLabel = new Label("The Gates of Light Break Across the Cloud Sea", new Label.LabelStyle(subtitleFont, Color.valueOf("#fbf2dc")));
        subtitleLabel.setAlignment(Align.left);
        contentTable.add(subtitleLabel).left().padTop(2).padBottom(10).row();

        proseLabel = new Label("", new Label.LabelStyle(proseFont, Color.WHITE));
        proseLabel.setWrap(true);
        proseLabel.setAlignment(Align.topLeft);
        contentTable.add(proseLabel).width(1300).left().expandY().top().row();

        // Navigation Bar Row (Previous | Progress Indicator | Next/Finish)
        navRow = new Table();

        // [ < PREV ] Button
        TextButton.TextButtonStyle prevStyle = new TextButton.TextButtonStyle();
        prevStyle.up = UIUtils.createRoundedDrawable(160, 46, 12, Color.valueOf("#33180cbb"), Color.valueOf("#5e2d14"), 1);
        prevStyle.over = UIUtils.createRoundedDrawable(160, 46, 12, Color.valueOf("#4a2412bb"), Color.valueOf("#ffd54f"), 1);
        prevStyle.disabled = UIUtils.createRoundedDrawable(160, 46, 12, Color.valueOf("#20100788"), Color.valueOf("#30180a"), 1);
        prevStyle.font = buttonFont;
        prevStyle.fontColor = Color.WHITE;
        prevStyle.disabledFontColor = Color.valueOf("#6d5345");

        prevBtn = new TextButton("< PREV", prevStyle);
        UIUtils.addScaleHoverEffect(prevBtn);
        prevBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                previousSlide();
            }
        });
        navRow.add(prevBtn).size(160, 46).left();

        // Spacer / Hidden Slide Progress Indicator (keep slide count hidden)
        indicatorLabel = new Label("", new Label.LabelStyle(indicatorFont, indicatorFont.getColor()));
        indicatorLabel.setAlignment(Align.center);
        indicatorLabel.setVisible(false);
        navRow.add(indicatorLabel).expandX().center();

        // [ NEXT > ] Button
        TextButton.TextButtonStyle nextStyle = new TextButton.TextButtonStyle();
        nextStyle.up = UIUtils.createRoundedDrawable(280, 46, 12, Color.valueOf("#854b25"), Color.valueOf("#ffd54f"), 2);
        nextStyle.over = UIUtils.createRoundedDrawable(280, 46, 12, Color.valueOf("#a66032"), Color.valueOf("#ffe082"), 2);
        nextStyle.font = buttonFont;
        nextStyle.fontColor = Color.WHITE;

        nextBtn = new TextButton("NEXT >", nextStyle);
        nextBtn.pad(0, 20, 0, 20);
        UIUtils.addScaleHoverEffect(nextBtn);
        nextBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                advanceSlide();
            }
        });
        nextBtnCell = navRow.add(nextBtn).size(280, 46).right();

        contentTable.add(navRow).width(1300).bottom().padTop(6);

        storyCard.add(contentTable).width(1300).height(210);
        bottomContainer.add(storyCard).size(1360, 240);
        stage.addActor(bottomContainer);
    }

    private void applySlide(int index, boolean animateTransition) {
        if (index < 0 || index >= slides.size()) return;

        prevSlideIndex = currentSlideIndex;
        currentSlideIndex = index;
        slideTimer = 0f;
        transitionTimer = animateTransition ? 0f : TRANSITION_DURATION;

        EndingSlide slide = slides.get(index);

        // Update Labels (keep remaining slide counts hidden)
        chapterLabel.setText(slide.chapterTag.toUpperCase());
        chapterLabel.setColor(slide.accentColor);

        titleLabel.setText(slide.title);
        titleLabel.setColor(slide.accentColor);

        subtitleLabel.setText(slide.subtitle);
        proseLabel.setText(slide.prose);

        // Update Button States
        prevBtn.setDisabled(index == 0);
        prevBtn.setVisible(index > 0);

        if (index == slides.size() - 1) {
            nextBtn.setText("VIEW EXPEDITION SUMMARY");
            if (nextBtnCell != null) {
                nextBtnCell.width(410);
            }
        } else {
            nextBtn.setText("NEXT >");
            if (nextBtnCell != null) {
                nextBtnCell.width(280);
            }
        }
        if (navRow != null) {
            navRow.invalidateHierarchy();
        }

        // Trigger Audio Cue if specified
        if (slide.soundEffect != null && game.getAudioManager() != null) {
            game.getAudioManager().playSound(slide.soundEffect);
        }
    }

    private void advanceSlide() {
        if (game.getAudioManager() != null) {
            game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
        }
        if (currentSlideIndex < slides.size() - 1) {
            applySlide(currentSlideIndex + 1, true);
        } else {
            finishEnding();
        }
    }

    private void previousSlide() {
        if (currentSlideIndex > 0) {
            if (game.getAudioManager() != null) {
                game.getAudioManager().playSound(AudioManager.SoundEffect.CLICK);
            }
            applySlide(currentSlideIndex - 1, true);
        }
    }

    private void finishEnding() {
        game.setScreen(new GameSummaryScreen(game, isVictory, hero, endingType));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.03f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        slideTimer += delta;
        if (transitionTimer < TRANSITION_DURATION) {
            transitionTimer = Math.min(TRANSITION_DURATION, transitionTimer + delta);
        }
        float transitionProgress = Math.min(1.0f, transitionTimer / TRANSITION_DURATION);

        float worldW = stage.getViewport().getWorldWidth();
        float worldH = stage.getViewport().getWorldHeight();

        batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
        batch.begin();

        // 1. Render Outgoing Slide (if transitioning)
        if (transitionProgress < 1.0f && prevSlideIndex >= 0 && prevSlideIndex < slides.size()) {
            EndingSlide prevSlide = slides.get(prevSlideIndex);
            float prevAlpha = 1.0f - transitionProgress;
            renderSlideBackground(prevSlide, prevAlpha, worldW, worldH, 1.0f);
        }

        // 2. Render Incoming Slide
        if (currentSlideIndex >= 0 && currentSlideIndex < slides.size()) {
            EndingSlide curSlide = slides.get(currentSlideIndex);
            float curAlpha = transitionProgress;
            // Subtle Ken Burns slow zoom (1.0f to 1.035f over 12s)
            float driftScale = 1.0f + Math.min(0.04f, (slideTimer / 12f) * 0.04f);
            renderSlideBackground(curSlide, curAlpha, worldW, worldH, driftScale);
        }

        batch.setColor(Color.WHITE);
        batch.end();

        // 3. Update and Render UI Stage
        stage.act(delta);
        stage.draw();
    }

    /**
     * Renders an individual slide image with Ken Burns drift and alpha blending,
     * with graceful procedural parallax fallback if the image is absent.
     */
    private void renderSlideBackground(EndingSlide slide, float alpha, float worldW, float worldH, float scale) {
        Texture tex = slide.getTexture(game);
        if (tex != null) {
            // High-resolution story illustration (fills screen with centered Ken Burns scaling)
            batch.setColor(1f, 1f, 1f, alpha);
            float drawW = worldW * scale;
            float drawH = worldH * scale;
            float drawX = (worldW - drawW) / 2f;
            float drawY = (worldH - drawH) / 2f;
            batch.draw(tex, drawX, drawY, drawW, drawH);
        } else if (fallbackBgTex != null) {
            // Procedural layered fallback
            batch.setColor(1f, 1f, 1f, alpha);
            batch.draw(fallbackBgTex, 0, 0, worldW, worldH);

            // Warm golden aura in the center
            Color aura = slide.accentColor;
            batch.setColor(aura.r, aura.g, aura.b, alpha * 0.35f);
            batch.draw(whitePixelTex, worldW * 0.2f, worldH * 0.3f, worldW * 0.6f, worldH * 0.4f);
        } else {
            // Solid dark tone fallback
            batch.setColor(0.08f, 0.04f, 0.02f, alpha);
            batch.draw(whitePixelTex, 0, 0, worldW, worldH);
        }
    }

    @Override
    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
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
        if (stage != null) stage.dispose();
        if (whitePixelTex != null) whitePixelTex.dispose();

        for (EndingSlide slide : slides) {
            slide.dispose();
        }

        if (chapterFont != null) chapterFont.dispose();
        if (titleFont != null) titleFont.dispose();
        if (subtitleFont != null) subtitleFont.dispose();
        if (proseFont != null) proseFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (indicatorFont != null) indicatorFont.dispose();
    }
}
