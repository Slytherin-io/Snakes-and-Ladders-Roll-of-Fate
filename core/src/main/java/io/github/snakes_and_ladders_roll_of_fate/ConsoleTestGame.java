package io.github.snakes_and_ladders_roll_of_fate;

import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

import io.github.snakes_and_ladders_roll_of_fate.systems.Dice;
import io.github.snakes_and_ladders_roll_of_fate.systems.GameMode;
import io.github.snakes_and_ladders_roll_of_fate.board.Tile;
import io.github.snakes_and_ladders_roll_of_fate.board.NormalTile;
import io.github.snakes_and_ladders_roll_of_fate.systems.Dialogue;
import io.github.snakes_and_ladders_roll_of_fate.systems.Event;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventManager;

import java.util.ArrayList;
import java.util.List;

import java.util.Scanner;

public class ConsoleTestGame {
    private Board board;
    private List<Player> players;
    private int totalPlayers;
    private int currentTurn;
    private String gameState;
    private GameMode selectedGameMode;
    private Dice movementDice = new Dice(6);
    private EventManager eventManager = new EventManager();

    private Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        ConsoleTestGame game = new ConsoleTestGame();
        game.displayMainMenu();
        game.getUserInput();
    }

    public void displayMainMenu() {
        System.out.println("=== SNAKES AND LADDERS: ROLL OF FATE (CONSOLE TEST) ===");
        System.out.println("[1] Start Game");
        System.out.println("[2] Settings");
        System.out.println("[3] Credits");
        System.out.println("[4] Exit");
    }

    public void startGame() {
        setGameMode(); // Single Player (for testing only)

        players = new ArrayList<>();
        createPlayer();
        displayPlayers();

        currentTurn = 0;

        board = new Board(100);
        board.generateBoard();
        board.displayBoard();

        gameState = "PLAYING";

        runGameLoop();
    }

    public void runGameLoop() {
        sc.nextLine();

        while (!checkGameOver()) {
            Player currentPlayer = getCurrentPlayer();

            // Roll Dice
            System.out.println("\n-------------------------------------------");
            System.out.println(currentPlayer.getName() + "'s turn (Position: " + currentPlayer.getPosition() + ")");
            System.out.print("Press ENTER to roll...");
            sc.nextLine();

            // Move player with D6
            rollForMovement(currentPlayer);
            System.out.println(movementDice.getValue());

            // Resolve tile polymorphically
            Tile currentTile = board.getTile(currentPlayer.getPosition());
            if (currentTile != null) {
                System.out.printf("\n** ENTERED %s TILE (Tile %d) **\n", currentTile.getType(), currentTile.getPosition());
                System.out.println("> " + currentTile.getEffect());
                currentTile.onLand(currentPlayer);
                System.out.println("Current Position: " + currentPlayer.getPosition());
                System.out.println(currentPlayer.getHealth());

                // Trigger dynamic events on normal tiles
                if (currentTile instanceof NormalTile && Math.random() < 0.35) {
                    Event event = eventManager.selectEvent();
                    if (event != null) {
                        eventManager.triggerEvent(currentPlayer);
                        System.out.print("Enter your choice (1-" + event.getChoices().size() + "): ");
                        if (sc.hasNextInt()) {
                            int choice = sc.nextInt();
                            sc.nextLine(); // flush newline
                            eventManager.resolveEvent(choice, currentPlayer);
                        }
                    }
                }
            }

            // Check win condition after tile effect has finished
            if (checkGameOver()) {
                System.out.println("\n" + currentPlayer.getName() + " REACHED THE GOAL! " + getGameState());
                break;
            }

            // If Pass-and-Play, Pass turn to next player
            // else move to next roll
            nextTurn();
        }
    }

    public void displayPlayers() {
        for (Player player : players) {
            System.out.println("\n=== " + player.getName() + " ===");
            System.out.printf("HP: %d%n", player.getHealth());
            System.out.printf("Attack: %d%n", player.getAttack());
            System.out.printf("Defense: %d%n", player.getDefense());
            System.out.printf("Position: %d%n", player.getPosition());
        }
    }

    public void getUserInput() {
        do {
            System.out.print("Enter Choice (1-4): ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    startGame();
                    break;
                case 2:
                    System.out.println("Settings Clicked");
                    break;
                case 3:
                    System.out.println("Credits Clicked");
                    break;
                case 4:
                    System.out.println("Exiting Program. Thank you for playing!");
                    break;
                default:
                    System.out.println("Please enter a valid number (1-4)");
                    break;
            }
            if (choice >= 1 && choice <= 4) break;
        } while (true);
    }

    public int setTotalPlayer() {
        do {
            System.out.print("Enter total number of Players (2-4): ");
            this.totalPlayers = sc.nextInt();
        } while (totalPlayers < 2 || totalPlayers > 4);
        return this.totalPlayers;
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

    public void rollForMovement(Player player) {
        int steps = movementDice.roll();
        player.move(steps);

    }
    public void rollForDamage(Player player){

    }

    // Getters & Setters
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

    public void setGameMode() {
        do {
            System.out.println("\n[1] SINGLE PLAYER");
            System.out.println("[2] PASS AND PLAY");
            System.out.print("Select Game Mode (1-2): ");
            int modeIndex = sc.nextInt();

            switch (modeIndex) {
                case 1:
                    this.selectedGameMode = GameMode.SINGLE_PLAYER;
                    this.totalPlayers = 1;
                    break;
                case 2:
                    this.selectedGameMode = GameMode.PASS_AND_PLAY;
                    int playerCount = setTotalPlayer();
                    this.totalPlayers = Math.max(2, Math.min(4, playerCount));
                    break;
                default:
                    System.out.println("Please select a valid Game Mode number (1-2): ");
                    break;
            }

            if (modeIndex >= 1 && modeIndex <= 2) break;

        } while(true);
    }
}
