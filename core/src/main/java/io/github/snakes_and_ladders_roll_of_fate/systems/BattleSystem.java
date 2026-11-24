package io.github.snakes_and_ladders_roll_of_fate.systems;

import com.badlogic.gdx.utils.Timer;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Entity;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import java.util.Scanner;

/**
 * UI-driven battle state machine for LibGDX, with console loop support.
 * BattleScreen buttons call the onPlayerX() methods with enemy turns running on a Timer,
 * while ConsoleTestGame / tests can run the synchronous runBattle() method.
 */
public class BattleSystem {

    public enum BattleState { PLAYER_TURN, ENEMY_TURN, BATTLE_END }
    public enum BattleOutcome { VICTORY, DEFEAT, ESCAPED, WEAKENED }

    public interface BattleListener {
        void onDamageDealt(Entity attacker, Entity defender, int damage, boolean isCrit, boolean isMiss);
    }

    public interface EnemyAttackStarter {
        void onEnemyAttackStart();
    }

    private BattleState currentState = BattleState.BATTLE_END;
    private BattleOutcome finalOutcome = null;

    private BattleListener battleListener;
    private EnemyAttackStarter enemyAttackStarter;

    private Player player;
    private Enemy enemy;
    private final Dice combatDice = new Dice(20);
    private int lastRoll = 0;

    private int playerTempDef = 0;
    private String battleLog = "";
    private String battleState = "IDLE";

    public BattleSystem() {
        this.battleState = "IDLE";
    }

    public int calculateDamage(Entity attacker, Entity defender) {
        lastRoll = combatDice.roll(); // remember the roll so the UI can report crits/misses
        int baseDamage = Math.max(1, attacker.getAttack() - defender.getDefense());
        if (lastRoll == 20) return Math.max(2, baseDamage * 2 + 5);
        if (lastRoll == 1)  return 0;
        return baseDamage + ((lastRoll - 1) / 4);
    }

    public int getLastRoll()            { return lastRoll; }
    public boolean isLastRollCritical() { return lastRoll == 20; }
    public boolean isLastRollMiss()     { return lastRoll == 1; }

    public void startBattle(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
        this.playerTempDef = 0;
        this.finalOutcome = null;
        this.currentState = BattleState.PLAYER_TURN;
        this.battleState = "IN_PROGRESS";
        this.battleLog = "A wild " + enemy.getEnemyType() + " blocks your path!";
    }

    public void startBattle(Entity attacker, Entity defender) {
        if (attacker instanceof Player && defender instanceof Enemy) {
            startBattle((Player) attacker, (Enemy) defender);
        } else {
            this.battleState = "IN_PROGRESS";
        }
    }

    // --- GETTERS FOR THE UI ---
    public BattleState getCurrentState() { return currentState; }
    public BattleOutcome getFinalOutcome() { return finalOutcome; }
    public Player getPlayer() { return player; }
    public Enemy getEnemy() { return enemy; }
    public String getBattleLog() { return battleLog; }
    public String getBattleState() { return battleState; }

    public void setBattleListener(BattleListener listener) {
        this.battleListener = listener;
    }

    public void setEnemyAttackStarter(EnemyAttackStarter starter) {
        this.enemyAttackStarter = starter;
    }

    // --- PLAYER ACTIONS (called by Scene2D buttons) ---
    public void onPlayerAttack() {
        if (currentState != BattleState.PLAYER_TURN) return;
        int damage = calculateDamage(player, enemy);
        boolean isCrit = isLastRollCritical();
        boolean isMiss = isLastRollMiss();
        enemy.setHealth(Math.max(0, enemy.getHealth() - damage));

        if (battleListener != null) {
            battleListener.onDamageDealt(player, enemy, damage, isCrit, isMiss);
        }

        if (enemy.getHealth() <= 0) {
            battleLog = "You strike for " + damage + " damage! The " + enemy.getEnemyType() + " collapses!";
            endBattle(BattleOutcome.VICTORY);
        } else {
            battleLog = "You strike for " + damage + " damage!";
            triggerEnemyTurn();
        }
    }

    public void onPlayerDefend() {
        if (currentState != BattleState.PLAYER_TURN) return;
        playerTempDef += 5;
        battleLog = "You brace yourself! (+5 DEF against the next hit)";
        triggerEnemyTurn();
    }

    public void onPlayerEscape() {
        if (currentState != BattleState.PLAYER_TURN) return;
        int roll = combatDice.roll();
        if (roll >= 10) {
            battleLog = "Escape roll " + roll + " - you slip away!";
            endBattle(BattleOutcome.ESCAPED);
        } else {
            battleLog = "Escape roll " + roll + " - the " + enemy.getEnemyType() + " cuts you off!";
            triggerEnemyTurn();
        }
    }

    /** Routes through your real Inventory/Player API; MOVE items are blocked mid-battle. */
    public void onPlayerUseItem(int slotIndex) {
        if (currentState != BattleState.PLAYER_TURN) return;

        Inventory inv = player.getInventory();
        if (inv == null || inv.size() == 0) {
            battleLog = "Your satchel is empty!";
            return;
        }

        Item item = inv.getItem(slotIndex);
        if (item == null) {
            battleLog = "That slot is empty!";
            return;
        }

        // Block movement items during battle so they don't break the board state
        if (item.getEffectType() == EventEffectType.MOVE) {
            battleLog = "You cannot travel the board mid-battle!";
            return;
        }

        // Use the item via the Player class (handles durability, consumption, etc.)
        boolean success = player.useItem(slotIndex);

        if (success) {
            battleLog = "You used " + item.getName() + "! (" + item.getEffect() + ")";
            triggerEnemyTurn(); // Using an item consumes the player's turn
        } else {
            battleLog = "You fumbled and cannot use " + item.getName() + "!";
        }
    }

    // --- ENEMY TURN (delayed so the player can read the log) ---
    private void triggerEnemyTurn() {
        currentState = BattleState.ENEMY_TURN;
        Timer.schedule(new Timer.Task() {
            @Override public void run() {
                if (enemyAttackStarter != null) {
                    enemyAttackStarter.onEnemyAttackStart();
                } else {
                    executeEnemyAttack();
                }
            }
        }, 0.8f);
    }

    public void executeEnemyAttack() {
        if (currentState != BattleState.ENEMY_TURN) return;
        int raw = calculateDamage(enemy, player);
        int damage = Math.max(0, raw - playerTempDef);
        boolean isCrit = isLastRollCritical();
        boolean isMiss = isLastRollMiss();
        playerTempDef = 0;

        int newHealth = player.getHealth() - damage;

        if (battleListener != null) {
            battleListener.onDamageDealt(enemy, player, damage, isCrit, isMiss);
        }

        if (newHealth <= 0) {
            player.setHealth(0);
            battleLog = enemy.getEnemyType() + " deals " + damage + " damage - you have fallen in battle...";
            endBattle(BattleOutcome.DEFEAT);
            return;
        }
        player.setHealth(newHealth);
        battleLog = enemy.getEnemyType() + " deals " + damage + " damage!";
        currentState = BattleState.PLAYER_TURN;
    }

    public void endBattle() {
        endBattle(BattleOutcome.VICTORY);
    }

    private void endBattle(BattleOutcome outcome) {
        this.finalOutcome = outcome;
        this.currentState = BattleState.BATTLE_END;
        this.battleState = "ENDED";
    }

    // --- SYNCHRONOUS RUN BATTLE (for ConsoleTestGame and unit tests) ---
    public BattleOutcome runBattle(Player player, Enemy enemy, Scanner scanner) {
        startBattle(player, enemy);
        System.out.println("\n=========================================");
        System.out.println(" BATTLE START: " + player.getName() + " vs " + enemy.getEnemyType());
        System.out.println(" Enemy Behavior: " + enemy.getBehavior());
        System.out.println("=========================================");

        while (player.getHealth() > 0 && enemy.getHealth() > 0) {
            System.out.println("\n[ " + player.getName() + " | HP: " + player.getHealth() + " ]  vs  [ " + enemy.getEnemyType() + " | HP: " + enemy.getHealth() + " ]");
            System.out.println("1. Attack");
            System.out.println("2. Defend");
            System.out.println("3. Use Item");
            System.out.println("4. Escape");
            System.out.print("Choose your action: ");

            int choice = 1;
            if (scanner != null && scanner.hasNextLine()) {
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. You hesitate, losing your turn!");
                    enemyConsoleTurn(player, enemy);
                    if (player.getHealth() <= 0) return BattleOutcome.DEFEAT;
                    continue;
                }
            } else if (scanner == null || !scanner.hasNextLine()) {
                choice = 1; // Default to Attack in automated testing
            }

            boolean playerTurnEnded = false;

            switch (choice) {
                case 1: // Attack
                    int playerDamage = calculateDamage(player, enemy);
                    enemy.setHealth(Math.max(0, enemy.getHealth() - playerDamage));
                    System.out.println("You strike the " + enemy.getEnemyType() + " for " + playerDamage + " damage!");
                    playerTurnEnded = true;
                    break;

                case 2: // Defend
                    int defendBonus = 5;
                    player.addTempDefense(defendBonus);
                    System.out.println("You brace yourself! (+ " + defendBonus + " DEF for this turn)");
                    playerTurnEnded = true;
                    break;

                case 3: // Use Item
                    playerTurnEnded = useItemMenu(player, scanner);
                    if (!playerTurnEnded) {
                        System.out.println("You fumbled with your items and lost your turn!");
                    }
                    break;

                case 4: // Escape
                    int escapeRoll = combatDice.roll();
                    System.out.println("You attempt to escape... (Rolled: " + escapeRoll + ")");
                    if (escapeRoll >= 10) {
                        System.out.println("You successfully flee from the " + enemy.getEnemyType() + "!");
                        endBattle(BattleOutcome.ESCAPED);
                        return BattleOutcome.ESCAPED;
                    } else {
                        System.out.println("Escape failed! The " + enemy.getEnemyType() + " blocks your path.");
                        playerTurnEnded = true;
                    }
                    break;

                default:
                    System.out.println("Invalid choice. You hesitate, losing your turn!");
                    playerTurnEnded = true;
                    break;
            }

            // Check if enemy died from player's action
            if (enemy.getHealth() <= 0) {
                System.out.println("\n🏆 VICTORY! The " + enemy.getEnemyType() + " has been slain!");
                endBattle(BattleOutcome.VICTORY);
                return BattleOutcome.VICTORY;
            }

            // Enemy Turn (if player didn't escape)
            if (playerTurnEnded) {
                enemyConsoleTurn(player, enemy);

                // Clear temporary defense buff after enemy attacks
                player.clearTemporaryBuffs();

                // Check battle end conditions
                if (player.getHealth() <= 0) {
                    player.setHealth(0);
                    endBattle(BattleOutcome.DEFEAT);
                    return BattleOutcome.DEFEAT;
                }
            }
        }

        BattleOutcome outcome = player.getHealth() > 0 ? BattleOutcome.VICTORY : BattleOutcome.DEFEAT;
        endBattle(outcome);
        return outcome;
    }

    private void enemyConsoleTurn(Player player, Enemy enemy) {
        System.out.println("\n-> " + enemy.getEnemyType() + " attacks!");
        int enemyDamage = calculateDamage(enemy, player);
        int newHealth = player.getHealth() - enemyDamage;

        if (newHealth <= 0) {
            player.setHealth(0);
            System.out.println("The " + enemy.getEnemyType() + " dealt " + enemyDamage + " damage. You have been defeated...");
        } else {
            player.setHealth(newHealth);
            System.out.println("The " + enemy.getEnemyType() + " dealt " + enemyDamage + " damage. Your HP: " + player.getHealth());
        }
    }

    private boolean useItemMenu(Player player, Scanner scanner) {
        Inventory inventory = player.getInventory();

        if (inventory == null || inventory.isEmpty()) {
            System.out.println("Your inventory is empty!");
            return false;
        }

        inventory.displayInventory();
        System.out.println("0. Cancel");
        System.out.print("Select item slot to use: ");

        try {
            int itemChoice = Integer.parseInt(scanner.nextLine());
            if (itemChoice == 0) return false;

            boolean used = player.useItem(itemChoice - 1);
            return used;
        } catch (Exception e) {
            System.out.println("Invalid selection.");
            return false;
        }
    }
}
