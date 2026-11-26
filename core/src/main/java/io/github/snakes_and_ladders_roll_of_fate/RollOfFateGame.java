package io.github.snakes_and_ladders_roll_of_fate;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Screen;

import java.util.List;
import java.util.ArrayList;

import io.github.snakes_and_ladders_roll_of_fate.screens.BoardScreen;
import io.github.snakes_and_ladders_roll_of_fate.screens.LoadingScreen;
import io.github.snakes_and_ladders_roll_of_fate.screens.MainMenuScreen;
import io.github.snakes_and_ladders_roll_of_fate.audio.AudioManager;
import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.Dice;
import io.github.snakes_and_ladders_roll_of_fate.systems.GameMode;
import io.github.snakes_and_ladders_roll_of_fate.systems.RunTracker;
import io.github.snakes_and_ladders_roll_of_fate.ui.Assets;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class RollOfFateGame extends Game {
    private AssetManager assetManager;
    private Screen currentScreen;
    private Board board;
    private List<Player> players;
    private int totalPlayers;
    private int currentTurn;
    private String gameState;
    private GameMode selectedGameMode;
    private RunTracker runTracker = new RunTracker();
    private List<io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass> lastChosenClasses = new ArrayList<>();
    private AudioManager audioManager = new AudioManager();

    // The Pale Shepherd (The Pursuer of the Ascending Trail)
    private int paleShepherdTile = 0;
    private int shepherdDormantTurns = 3;
    private int totalRounds = 0;
    private int shepherdPauseRounds = 0;

    @Override
    public void create() {
        assetManager = new AssetManager();

        Assets.loadAll(assetManager);

        assetManager.finishLoading();

        audioManager.init();

        gameState = "MENU";
        setScreen(new MainMenuScreen(this));
    }

    public void startGame() {
        if (selectedGameMode == null) {
            setGameMode(GameMode.SINGLE_PLAYER);
        }

        players = new ArrayList<>();
        runTracker = new RunTracker();
        paleShepherdTile = 0;
        totalRounds = 0;
        shepherdPauseRounds = 0;
        createPlayer();

        currentTurn = 0; // Player 1

        board = new Board(100);
        board.generateBoard();

        gameState = "PLAYING";
        setScreen(new LoadingScreen(this, () -> setScreen(new BoardScreen(this))));
    }

    public void startGame(GameMode mode) {
        setGameMode(mode);
        startGame();
    }

    /**
     * Starts the game with each player assigned to their chosen CharacterClass.
     */
    public void startGameWithClasses(List<io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass> chosenClasses) {
        this.lastChosenClasses = new ArrayList<>(chosenClasses);
        this.totalPlayers = chosenClasses.size();
        this.players = new ArrayList<>();
        this.runTracker = new RunTracker();
        this.paleShepherdTile = 0;
        this.totalRounds = 0;
        this.shepherdPauseRounds = 0;

        for (int i = 0; i < chosenClasses.size(); i++) {
            Player player = new Player("Player " + (i + 1), chosenClasses.get(i));
            player.setPosition(1);
            // Give an additional minor health potion alongside class starter item
            player.addItem(io.github.snakes_and_ladders_roll_of_fate.items.Item.createConsumable(
                "Minor Health Potion", io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.HEALTH, 25, "Restores 25 HP"
            ));
            players.add(player);
        }

        currentTurn = 0;
        board = new Board(100);
        board.generateBoard();

        gameState = "PLAYING";
        setScreen(new LoadingScreen(this, () -> setScreen(new BoardScreen(this))));
    }

    public void createPlayer() {
        players.clear();
        for (int i = 1; i <= totalPlayers; i++ ) {
            Player player = new Player("Player " + i);
            player.setHealth(100);
            player.setPosition(1);
            player.setAttack(5);
            player.setDefense(5);
            player.addItem(io.github.snakes_and_ladders_roll_of_fate.items.Item.createConsumable(
                "Minor Health Potion", io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.HEALTH, 25, "Restores 25 HP"
            ));
            player.addItem(io.github.snakes_and_ladders_roll_of_fate.items.Item.createEquipment(
                "Iron Sword", io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.ATTACK, 4, 1, 1, "+4 ATK (1-Use Relic)"
            ));
            players.add(player);
        }
    }

    public void nextTurn() {
        if (players.isEmpty()) return;
        int livingCount = 0;
        for (Player p : players) {
            if (p.getHealth() > 0) livingCount++;
        }
        if (livingCount == 0) return; // All dead

        for (int i = 0; i < players.size(); i++) {
            currentTurn = (currentTurn + 1) % players.size();
            if (players.get(currentTurn).getHealth() > 0) {
                break;
            }
        }
    }

    public boolean isPartyWiped() {
        if (players == null || players.isEmpty()) return true;
        for (Player p : players) {
            if (p.getHealth() > 0) return false;
        }
        return true;
    }

    public boolean checkGameOver() {
        if (players.isEmpty()) return false;

        Player currentPlayer = players.get(currentTurn);

        // Defeat Condition
        if (currentPlayer.getHealth() <= 0) {
            gameState = "DEFEAT";
            return true;
        }

        // Victory Condition
        if (currentPlayer.getPosition() >= board.getBoardSize()) {
            gameState = "VICTORY!";
            return true;
        }

        return false;
    }

    @Override
    public void setScreen(Screen screen) {
        super.setScreen(screen);
        this.currentScreen = screen;
    }

    @Override
    public void render() {
        // Press F11 anytime to toggle between Movable Window and Fullscreen
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1280, 720); // Switch to windowed
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode()); // Switch to fullscreen
            }
        }

        // Press F12 or Print Screen anytime to save a clean, uncompressed PNG screenshot
//        if (Gdx.input.isKeyJustPressed(Input.Keys.F12) || Gdx.input.isKeyJustPressed(Input.Keys.PRINT_SCREEN)) {
//            com.badlogic.gdx.graphics.Pixmap pixmap = com.badlogic.gdx.utils.ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
//            com.badlogic.gdx.files.FileHandle file = Gdx.files.local("screenshots/screenshot_" + System.currentTimeMillis() + ".png");
//            com.badlogic.gdx.graphics.PixmapIO.writePNG(file, pixmap);
//            pixmap.dispose();
//            Gdx.app.log("Screenshot", "Saved to " + file.file().getAbsolutePath());
//        }

        if (audioManager != null) {
            audioManager.update(Gdx.graphics.getDeltaTime());
        }

        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (audioManager != null) {
            audioManager.dispose();
        }
        if (assetManager != null) {
            assetManager.dispose();
        }
    }

    // Getters & Setters
    public AudioManager getAudioManager() { return audioManager; }
    public AssetManager getAssetManager() { return assetManager; }
    public Screen getCurrentScreen() { return currentScreen; }
    public Board getBoard() { return board; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
    public int getCurrentTurn() { return currentTurn; }
    public Player getCurrentPlayer() {
        return players.isEmpty() ? null : players.get(currentTurn);
    }
    public String getGameState() { return gameState; }
    public RunTracker getRunTracker() { return runTracker; }
    public List<io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass> getLastChosenClasses() { return lastChosenClasses; }

    public void setTotalPlayers(int totalPlayers) {
        this.totalPlayers = totalPlayers;
    }

    public void setGameMode(GameMode mode) {
        setGameMode(mode, (mode == GameMode.SINGLE_PLAYER) ? 1 : 2);
    }

    public void setGameMode(GameMode mode, int playerCount) {
        this.selectedGameMode = mode;

        if (mode == GameMode.SINGLE_PLAYER) {
            this.totalPlayers = 1;
        } else {
            this.totalPlayers = Math.max(2, Math.min(4, playerCount));
        }
    }

    // The Pale Shepherd Accessors
    public int getPaleShepherdTile() { return paleShepherdTile; }
    public void setPaleShepherdTile(int tile) { this.paleShepherdTile = Math.max(0, Math.min(99, tile)); }
    public void advancePaleShepherd(int tiles) { this.paleShepherdTile = Math.max(0, Math.min(99, this.paleShepherdTile + tiles)); }
    public int getTotalRounds() { return totalRounds; }
    public void incrementTotalRounds() { this.totalRounds++; }
    public int getShepherdDormantTurns() { return shepherdDormantTurns; }
    public int getShepherdPauseRounds() { return shepherdPauseRounds; }
    public void setShepherdPauseRounds(int rounds) { this.shepherdPauseRounds = rounds; }
    public void decrementShepherdPauseRounds() { if (this.shepherdPauseRounds > 0) this.shepherdPauseRounds--; }
}
