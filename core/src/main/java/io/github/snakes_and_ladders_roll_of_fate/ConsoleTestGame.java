package io.github.snakes_and_ladders_roll_of_fate;

import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.Dice;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import io.github.snakes_and_ladders_roll_of_fate.systems.GameMode;
import io.github.snakes_and_ladders_roll_of_fate.board.Tile;
import io.github.snakes_and_ladders_roll_of_fate.board.NormalTile;
import io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile;
import io.github.snakes_and_ladders_roll_of_fate.systems.Dialogue;
import io.github.snakes_and_ladders_roll_of_fate.systems.Event;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;
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
    private boolean hasUsedItemThisTurn = false;

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
        while (!checkGameOver()) {
            Player currentPlayer = getCurrentPlayer();
            if (currentPlayer == null) break;

            boolean turnActionDecided = false;

            while (!turnActionDecided && !checkGameOver()) {
                System.out.println("\n-------------------------------------------");
                String atkBuff = currentPlayer.getTempAttack() > 0 ? " (+" + currentPlayer.getTempAttack() + " Buff)" : "";
                String defBuff = currentPlayer.getTempDefense() > 0 ? " (+" + currentPlayer.getTempDefense() + " Buff)" : "";
                System.out.printf("%s's Turn | Tile: %d | HP: %d | ATK: %d%s | DEF: %d%s | EXP: %d (Lv %d)%n",
                    currentPlayer.getName(),
                    currentPlayer.getPosition(),
                    currentPlayer.getHealth(),
                    currentPlayer.getAttack(),
                    atkBuff,
                    currentPlayer.getDefense(),
                    defBuff,
                    currentPlayer.getExperience(),
                    currentPlayer.getLevel()
                );
                System.out.println("[1] Roll Movement Dice (D6)");
                System.out.println("[2] Use Item from Inventory" + (hasUsedItemThisTurn ? " [USED THIS TURN]" : ""));
                System.out.println("[3] View Inventory / Equipment");
                System.out.print("Select Action (1-3): ");

                int action = 1;
                if (sc.hasNextInt()) {
                    action = sc.nextInt();
                    sc.nextLine();
                } else {
                    sc.nextLine();
                    continue;
                }

                switch (action) {
                    case 1:
                        turnActionDecided = true;
                        break;
                    case 2:
                        handleUseItem(currentPlayer);
                        break;
                    case 3:
                        handleViewInventory(currentPlayer);
                        break;
                    default:
                        System.out.println("  [INVALID] Please select a number between 1 and 3.");
                        break;
                }
            }

            if (checkGameOver()) break;

            // Roll Dice and Move
            rollForMovement(currentPlayer);
            System.out.println("\n> " + currentPlayer.getName() + " rolled a " + movementDice.getValue() + "!");

            // Resolve tile polymorphically
            Tile currentTile = board.getTile(currentPlayer.getPosition());
            if (currentTile != null) {
                resolveTile(currentTile, currentPlayer);
            }

            // Check win condition after tile effect has finished
            if (checkGameOver()) {
                if (currentPlayer != null && currentPlayer.getHealth() <= 0) {
                    System.out.println("\n" + currentPlayer.getName() + " died! " + getGameState());
                    players.remove(currentTurn);

                    if (players.isEmpty()) {
                        System.out.println("\nGAME OVER. " + getGameState());
                        break;
                    }

                    if (currentTurn >= players.size()) {
                        currentTurn = 0;
                    }
                    hasUsedItemThisTurn = false;
                    continue;
                }

                System.out.println("\n" + currentPlayer.getName() + " REACHED THE GOAL! " + getGameState());
                break;
            }

            // If Pass-and-Play, Pass turn to next player
            // else move to next roll
            nextTurn();
        }
    }

    private void handleUseItem(Player player) {
        if (hasUsedItemThisTurn) {
            System.out.println("\n  [RESTRICTION] You have already used an item this turn! Limit 1 item per turn.");
            return;
        }

        if (player.getInventory() == null || player.getInventory().isEmpty()) {
            System.out.println("\n  [NOTICE] Your inventory is currently empty!");
            return;
        }

        player.getInventory().displayInventory();
        System.out.print("Select item slot to use (1-" + player.getInventory().size() + ", or 0 to cancel): ");

        if (sc.hasNextInt()) {
            int slot = sc.nextInt();
            sc.nextLine();

            if (slot == 0) {
                System.out.println("  [CANCELLED] Item usage cancelled.");
                return;
            }

            boolean success = player.useItem(slot - 1);
            if (success) {
                hasUsedItemThisTurn = true;
            }
        } else {
            sc.nextLine();
            System.out.println("  [INVALID] Invalid input.");
        }
    }

    private void handleViewInventory(Player player) {
        if (player.getInventory() != null) {
            player.getInventory().displayInventory();
        } else {
            System.out.println("\n=== INVENTORY (0/4 SLOTS) ===");
            System.out.println("  (Empty)");
        }
    }

    private void resolveTile(Tile currentTile, Player currentPlayer) {
        System.out.printf("\n** ENTERED %s TILE (Tile %d) **\n", currentTile.getType(), currentTile.getPosition());
        System.out.println("> " + currentTile.getEffect());

        EventCategory category = currentTile.getEventCategory();

        if (category == EventCategory.SNAKE) {
            // Trigger Snake Dilemma before sliding
            Event event = eventManager.selectEventForTile(currentTile.getPosition(), EventCategory.SNAKE);
            int choice = 2; // Default to sliding if skipped
            if (event != null) {
                eventManager.triggerEvent();
                System.out.print("Enter your choice (1-" + event.getChoices().size() + "): ");
                if (sc.hasNextInt()) {
                    choice = sc.nextInt();
                    sc.nextLine();
                    eventManager.resolveEvent(choice, currentPlayer);
                }
            }

            // If Choice 1 was selected and player survived, they resisted the drop!
            if (choice == 1 && currentPlayer.getHealth() > 0) {
                System.out.println("  [RESISTED] You stood your ground and fought off the serpent, resisting the slide!");
            } else {
                // Otherwise, the serpent drags the player down
                currentTile.onLand(currentPlayer);
            }
        } else if (category == EventCategory.LADDER) {
            // Trigger Ladder Event before / during climb
            Event event = eventManager.selectEventForTile(currentTile.getPosition(), EventCategory.LADDER);
            if (event != null) {
                eventManager.triggerEvent();
                System.out.print("Enter your choice (1-" + event.getChoices().size() + "): ");
                if (sc.hasNextInt()) {
                    int choice = sc.nextInt();
                    sc.nextLine();
                    eventManager.resolveEvent(choice, currentPlayer);
                }
            }
            // Player climbs up to destination
            currentTile.onLand(currentPlayer);
        } else {
            // Normal tile: safe landing dialogue + 35% chance for ambient encounter
            currentTile.onLand(currentPlayer);
            if (Math.random() < 0.35) {
                Event event = eventManager.selectEventForTile(currentPlayer.getPosition(), EventCategory.NORMAL);
                if (event != null) {
                    eventManager.triggerEvent();
                    System.out.print("Enter your choice (1-" + event.getChoices().size() + "): ");
                    if (sc.hasNextInt()) {
                        int choice = sc.nextInt();
                        sc.nextLine();
                        eventManager.resolveEvent(choice, currentPlayer);
                    }
                }
            }
        }
        System.out.println("Current Position: " + currentPlayer.getPosition());
    }

    public void displayPlayers() {
        for (Player player : players) {
            System.out.println("\n=== " + player.getName() + " ===");
            System.out.printf("HP: %d%n", player.getHealth());
            System.out.printf("Attack: %d%n", player.getAttack());
            System.out.printf("Defense: %d%n", player.getDefense());
            System.out.printf("Position: %d%n", player.getPosition());
            if (player.getInventory() != null) {
                player.getInventory().displayInventory();
            }
        }
    }

    public void getUserInput() {
        do {
            System.out.print("Enter Choice (1-4): ");
            int choice = -1;
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine();
            } else {
                sc.nextLine();
                continue;
            }

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
            if (sc.hasNextInt()) {
                this.totalPlayers = sc.nextInt();
                sc.nextLine();
            } else {
                sc.nextLine();
            }
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

            // Starter loadout: 1 consumable potion, 1 equipment shield with randomized durability (2-4 uses)
            player.addItem(Item.createConsumable("Minor Health Potion", EventEffectType.HEALTH, 25, "Restores 25 HP"));
            player.addItem(Item.createEquipment("Wooden Shield", EventEffectType.DEFENSE, 3, 2, 4, "+3 DEF (1 Turn Buff)"));

            players.add(player);
        }
    }

    public void nextTurn() {
        if (!players.isEmpty() && currentTurn < players.size()) {
            players.get(currentTurn).clearTemporaryBuffs();
        }
        this.hasUsedItemThisTurn = false;
        if (!players.isEmpty()) {
            this.currentTurn = (currentTurn + 1) % players.size();
        }
    }

    public boolean checkGameOver() {
        if (players.isEmpty()) {
            gameState = "GAME OVER";
            return true;
        }

        if (currentTurn >= players.size()) {
            currentTurn = 0;
        }

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
        if (players.isEmpty()) return null;
        if (currentTurn >= players.size()) {
            currentTurn = 0;
        }
        return players.get(currentTurn);
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
            int modeIndex = -1;
            if (sc.hasNextInt()) {
                modeIndex = sc.nextInt();
                sc.nextLine();
            } else {
                sc.nextLine();
                continue;
            }

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
