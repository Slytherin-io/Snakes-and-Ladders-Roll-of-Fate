package io.github.snakes_and_ladders_roll_of_fate;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.Screen;

import java.util.List;
import java.util.ArrayList;

import io.github.snakes_and_ladders_roll_of_fate.screens.BoardScreen;
import io.github.snakes_and_ladders_roll_of_fate.screens.MainMenuScreen;
import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.Dice;
import io.github.snakes_and_ladders_roll_of_fate.systems.GameMode;

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

    @Override
    public void create() {
        assetManager = new AssetManager();
        gameState = "MENU";
        setScreen(new MainMenuScreen(this));
    }

    public void startGame() {
        setGameMode(GameMode.SINGLE_PLAYER); // Single Player (for testing only)

        players = new ArrayList<>();
        createPlayer();

        currentTurn = 0; // Player 1

        board = new Board(100);
        board.generateBoard();

        gameState = "PLAYING";
        setScreen(new BoardScreen(this));
    }

    public void createPlayer() {
        players.clear();
        for (int i = 1; i <= totalPlayers; i++ ) {
            Player player = new Player("Player " + i);
            player.setHealth(100);
            player.setPosition(1);
            player.setAttack(5);
            player.setDefense(5);
            players.add(player);
        }
    }

    public void nextTurn() {
        if (!players.isEmpty()) {
            this.currentTurn = (currentTurn + 1) % players.size();
        }
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
        super.render();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (assetManager != null) {
            assetManager.dispose();
        }
    }

    // Getters & Setters
    public AssetManager getAssetManager() { return assetManager; }
    public Screen getCurrentScreen() { return currentScreen; }
    public Board getBoard() { return board; }
    public List<Player> getPlayers() { return players; }
    public int getCurrentTurn() { return currentTurn; }
    public Player getCurrentPlayer() {
        return players.isEmpty() ? null : players.get(currentTurn);
    }
    public String getGameState() { return gameState; }

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
}
