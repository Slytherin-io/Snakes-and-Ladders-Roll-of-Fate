package io.github.snakes_and_ladders_roll_of_fate;

import io.github.snakes_and_ladders_roll_of_fate.board.Board;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.Dice;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventChoice;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffect;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import io.github.snakes_and_ladders_roll_of_fate.systems.GameMode;
import io.github.snakes_and_ladders_roll_of_fate.board.Tile;
import io.github.snakes_and_ladders_roll_of_fate.board.NormalTile;
import io.github.snakes_and_ladders_roll_of_fate.board.SnakeTile;
import io.github.snakes_and_ladders_roll_of_fate.board.LadderTile;
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

        // Configure Option B Interactive Item Swap Handler
        EventEffect.setItemSwapHandler((player, newItem) -> {
            System.out.printf("%n  [INVENTORY FULL] You found [%s], but your bag is full (%d/%d).%n",
                newItem.getName(), player.getInventory().size(), player.getInventory().getCapacity());
            player.getInventory().displayInventory();
            System.out.print("Select a slot (1-" + player.getInventory().size() + ") to discard and replace, or 0 to leave it behind: ");
            if (sc.hasNextInt()) {
                int slot = sc.nextInt();
                sc.nextLine();
                if (slot >= 1 && slot <= player.getInventory().size()) {
                    player.replaceItem(slot - 1, newItem);
                } else {
                    System.out.println("  [ABANDONED] Left " + newItem.getName() + " behind.");
                }
            } else {
                if (sc.hasNext()) sc.nextLine();
                System.out.println("  [ABANDONED] Left " + newItem.getName() + " behind.");
            }
        });

        // Configure Interactive Item Barter Handler
        EventChoice.setItemBarterHandler(player -> {
            if (player.getInventory() == null || player.getInventory().isEmpty()) return null;
            System.out.println("\nSelect an item to give to the merchant in trade:");
            player.getInventory().displayInventory();
            System.out.print("Choose slot to trade away (1-" + player.getInventory().size() + "): ");
            if (sc.hasNextInt()) {
                int slot = sc.nextInt();
                sc.nextLine();
                if (slot >= 1 && slot <= player.getInventory().size()) {
                    return player.getInventory().removeItem(slot - 1);
                }
            } else {
                if (sc.hasNext()) sc.nextLine();
            }
            return player.getInventory().removeItem(0);
        });

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
            int oldPos = currentPlayer.getPosition();
            rollForMovement(currentPlayer);
            int newPos = currentPlayer.getPosition();
            System.out.println("\n> " + currentPlayer.getName() + " rolled a " + movementDice.getValue() + "!");
            checkActTransition(oldPos, newPos);

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
            SnakeTile snakeTile = (SnakeTile) currentTile;
            Event event = eventManager.selectEventForTile(currentTile.getPosition(), EventCategory.SNAKE);
            int choice = 3; // Default to fleeing if skipped

            if (event != null) {
                eventManager.triggerEvent(currentPlayer);
                System.out.print("Enter your choice (1-" + event.getChoices().size() + "): ");
                if (sc.hasNextInt()) {
                    choice = sc.nextInt();
                    sc.nextLine();
                    eventManager.resolveEvent(choice, currentPlayer);
                }
            }

            EventChoice chosen = (event != null && choice >= 1 && choice <= event.getChoices().size())
                ? event.getChoices().get(choice - 1) : null;

            if (chosen != null && chosen.getRequirementType() == EventChoice.ChoiceRequirementType.EQUIPMENT_DURABILITY) {
                // Shield deflection: successfully held ground without battle or slide!
                System.out.println("  [HELD GROUND] You warded off the serpent and held your position!");
            } else if (choice == 1) {
                // Choice 1: Draw weapon and engage in battle
                snakeTile.startCombat(currentPlayer);
            } else {
                // Choice 3 (or retreat/flee): Slide down safely without combat!
                System.out.println("  [RETREAT] You fled down the slope to escape the beast!");
                snakeTile.applyEffect(currentPlayer);
            }
        } else if (category == EventCategory.LADDER) {
            LadderTile ladderTile = (LadderTile) currentTile;
            boolean canClimb = true;

            if (eventManager.shouldTriggerLadderEvent()) {
                Event event = eventManager.selectEventForTile(currentTile.getPosition(), EventCategory.LADDER);
                if (event != null) {
                    eventManager.triggerEvent(currentPlayer);
                    System.out.print("Enter your choice (1-" + event.getChoices().size() + "): ");
                    if (sc.hasNextInt()) {
                        int choice = sc.nextInt();
                        sc.nextLine();
                        eventManager.resolveEvent(choice, currentPlayer);

                        EventChoice chosen = (choice >= 1 && choice <= event.getChoices().size())
                            ? event.getChoices().get(choice - 1) : null;
                        if (chosen != null && chosen.getDifficultyClass() > 0 && !chosen.didLastCheckSucceed()) {
                            System.out.println("  [SLIPPED] Your grip gave out! You tumbled off the ladder and could not ascend!");
                            canClimb = false;
                        }
                    }
                }
            } else {
                System.out.println("  [STABLE ASCENT] The ladder holds firm. You climb swiftly up the slope!");
            }

            if (canClimb) {
                ladderTile.applyEffect(currentPlayer);
            }
        } else {
            // Normal tile: Dynamic pacing check with cooldown & bad-luck protection
            if (eventManager.shouldTriggerAmbientEvent(currentPlayer)) {
                Event event = eventManager.selectEventForTile(currentPlayer.getPosition(), EventCategory.NORMAL);
                if (event != null) {
                    eventManager.triggerEvent(currentPlayer);
                    System.out.print("Enter your choice (1-" + event.getChoices().size() + "): ");
                    if (sc.hasNextInt()) {
                        int choice = sc.nextInt();
                        sc.nextLine();
                        eventManager.resolveEvent(choice, currentPlayer);
                    }
                    currentPlayer.resetEventCooldown();
                }
            } else {
                // Safe peaceful landing dialogue and cooldown increment
                currentPlayer.incrementTurnsSinceLastEvent();
                currentTile.onLand(currentPlayer);
            }
        }
        System.out.println("Current Position: " + currentPlayer.getPosition());
    }

    private void checkActTransition(int oldPos, int newPos) {
        if (oldPos <= 30 && newPos > 30 && newPos <= 70) {
            System.out.println("\n[ZONE TRANSITION] Entering Act II: The Jagged Crags & Caverns (Tiles 31-70)!");
            System.out.println("  The path narrows along sheer drops. Hazards grow harsher and merchants demand heavier tolls.");
        } else if (oldPos <= 70 && newPos > 70) {
            System.out.println("\n[ZONE TRANSITION] Entering Act III: The Spire of Fate & Dragon's Summit (Tiles 71-100)!");
            System.out.println("  The summit is in sight! Cosmic tempests rage as mythic trials and apex serpents guard the peak.");
        }
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
        CharacterClass[] availableClasses = CharacterClass.values();
        for (int i = 1; i <= totalPlayers; i++ ) {
            System.out.println("\n--- SELECT ANIMAL CHAMPION FOR PLAYER " + i + " ---");
            for (int c = 0; c < availableClasses.length; c++) {
                CharacterClass cls = availableClasses[c];
                System.out.printf("  [%d] %s (%s) - HP: %d | ATK: %d | DEF: %d | EXP Bonus: %d | Gear: %s%n",
                    (c + 1), cls.getName(), cls.getTitle(), cls.getBaseHealth(), cls.getBaseAttack(), cls.getBaseDefense(),cls.getExpBonus(), cls.getStartingItemName());
            }
            int choice = 1;
            while (true) {
                System.out.print("Enter choice (1-4, default 1): ");
                String input = sc.nextLine().trim();
                if (input.isEmpty()) {
                    choice = 1;
                    break;
                }
                try {
                    int val = Integer.parseInt(input);
                    if (val >= 1 && val <= availableClasses.length) {
                        choice = val;
                        break;
                    }
                } catch (NumberFormatException ignored) {}
                System.out.println("Invalid choice. Please enter 1 to 4.");
            }
            CharacterClass chosenClass = availableClasses[choice - 1];
            Player player = new Player("Player " + i, chosenClass);
            player.setPosition(1);
            System.out.printf("  [CHOSEN] Player %d embarked as %s the %s%n", i, chosenClass.getName(), chosenClass.getTitle());
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
