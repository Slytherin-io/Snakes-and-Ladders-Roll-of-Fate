package io.github.snakes_and_ladders_roll_of_fate.systems;

import com.badlogic.gdx.utils.Timer;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Entity;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import java.util.Scanner;

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

    // --- NEW: Status Effect Trackers ---
    private int playerPoisonTurns = 0;
    private int playerPoisonDamage = 0;

    public BattleSystem() { this.battleState = "IDLE"; }

    public int calculateDamage(Entity attacker, Entity defender) {
        lastRoll = combatDice.roll();
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
        this.playerPoisonTurns = 0;   // Reset poison on battle start
        this.playerPoisonDamage = 0;
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
    public void setBattleListener(BattleListener listener) { this.battleListener = listener; }
    public void setEnemyAttackStarter(EnemyAttackStarter starter) { this.enemyAttackStarter = starter; }

    // --- PLAYER ACTIONS (called by Scene2D buttons) ---
    public void onPlayerAttack() {
        if (currentState != BattleState.PLAYER_TURN) return;

        // --- SHADOW ASP: EVASION (20% Dodge Chance) ---
        if (enemy.getEnemyType().contains("Asp") && Math.random() < 0.20) {
            battleLog = "The Shadow Asp evades your attack!";
            triggerEnemyTurn();
            return;
        }

        int damage = calculateDamage(player, enemy);
        boolean isCrit = isLastRollCritical();
        boolean isMiss = isLastRollMiss();
        enemy.setHealth(Math.max(0, enemy.getHealth() - damage));

        if (battleListener != null) battleListener.onDamageDealt(player, enemy, damage, isCrit, isMiss);

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

    public void onPlayerUseItem(int slotIndex) {
        if (currentState != BattleState.PLAYER_TURN) return;
        Inventory inv = player.getInventory();
        if (inv == null || inv.size() == 0) { battleLog = "Your satchel is empty!"; return; }
        Item item = inv.getItem(slotIndex);
        if (item == null) { battleLog = "That slot is empty!"; return; }
        if (item.getEffectType() == EventEffectType.MOVE) { battleLog = "You cannot travel the board mid-battle!"; return; }

        boolean success = player.useItem(slotIndex);
        if (success) {
            battleLog = "You used " + item.getName() + "! (" + item.getEffect() + ")";
            triggerEnemyTurn();
        } else {
            battleLog = "You fumbled and cannot use " + item.getName() + "!";
        }
    }

    // --- ENEMY TURN (UI State Machine) ---
    private void triggerEnemyTurn() {
        currentState = BattleState.ENEMY_TURN;
        Timer.schedule(new Timer.Task() {
            @Override public void run() {
                if (enemyAttackStarter != null) enemyAttackStarter.onEnemyAttackStart();
                else executeEnemyAttack();
            }
        }, 0.8f);
    }

    public void executeEnemyAttack() {
        if (currentState != BattleState.ENEMY_TURN) return;

        StringBuilder log = new StringBuilder();

        // --- ARMORED PYTHON: REGENERATION ---
        if (enemy.getEnemyType().contains("Python")) {
            int healAmt = 5;
            int newEnemyHP = Math.min(100, enemy.getHealth() + healAmt); // Cap at 100 to prevent infinite scaling
            enemy.setHealth(newEnemyHP);
            log.append("The Python regenerates ").append(healAmt).append(" HP! ");
        }

        int raw = calculateDamage(enemy, player);

        // --- SHADOW ASP: CRITICAL STRIKE ---
        if (enemy.getEnemyType().contains("Asp") && lastRoll >= 18) {
            raw *= 2;
            log.append("CRITICAL STRIKE! ");
        }

        int damage = Math.max(0, raw - playerTempDef);
        boolean isCrit = isLastRollCritical();
        boolean isMiss = isLastRollMiss();
        playerTempDef = 0;

        int newHealth = player.getHealth() - damage;
        if (battleListener != null) battleListener.onDamageDealt(enemy, player, damage, isCrit, isMiss);

        if (newHealth <= 0) {
            player.setHealth(0);
            log.append(enemy.getEnemyType()).append(" deals ").append(damage).append(" damage - you have fallen in battle...");
            battleLog = log.toString();
            endBattle(BattleOutcome.DEFEAT);
            return;
        }

        player.setHealth(newHealth);
        log.append(enemy.getEnemyType()).append(" deals ").append(damage).append(" damage! ");

        // --- VENOMOUS VIPER: POISON STRIKE ---
        if (enemy.getEnemyType().contains("Viper") && lastRoll >= 15) {
            playerPoisonTurns = 3;
            playerPoisonDamage = 3;
            log.append("Venomous bite! (Poisoned for 3 turns) ");
        }

        // --- POISON TICK (Applies at the end of the enemy's turn) ---
        if (playerPoisonTurns > 0) {
            player.setHealth(player.getHealth() - playerPoisonDamage);
            playerPoisonTurns--;
            log.append("(Poison deals ").append(playerPoisonDamage).append(" damage!) ");
            if (player.getHealth() <= 0) {
                player.setHealth(0);
                log.append("The venom was too much...");
                battleLog = log.toString();
                endBattle(BattleOutcome.DEFEAT);
                return;
            }
        }

        battleLog = log.toString();
        currentState = BattleState.PLAYER_TURN;
    }

    public void endBattle() { endBattle(BattleOutcome.VICTORY); }
    private void endBattle(BattleOutcome outcome) {
        this.finalOutcome = outcome;
        this.currentState = BattleState.BATTLE_END;
        this.battleState = "ENDED";
    }

    // --- SYNCHRONOUS RUN BATTLE (Console / Tests) ---
    public BattleOutcome runBattle(Player player, Enemy enemy, Scanner scanner) {
        startBattle(player, enemy);
        System.out.println("\n=========================================");
        System.out.println(" BATTLE START: " + player.getName() + " vs " + enemy.getEnemyType());
        System.out.println(" Enemy Behavior: " + enemy.getBehavior());
        System.out.println("=========================================");

        while (player.getHealth() > 0 && enemy.getHealth() > 0) {
            System.out.println("\n[ " + player.getName() + " | HP: " + player.getHealth() + " ]  vs  [ " + enemy.getEnemyType() + " | HP: " + enemy.getHealth() + " ]");
            System.out.println("1. Attack  2. Defend  3. Use Item  4. Escape");
            System.out.print("Choose your action: ");

            int choice = 1;
            if (scanner != null && scanner.hasNextLine()) {
                try { choice = Integer.parseInt(scanner.nextLine().trim()); }
                catch (NumberFormatException e) {
                    System.out.println("Invalid input. You hesitate, losing your turn!");
                    enemyConsoleTurn(player, enemy);
                    if (player.getHealth() <= 0) return BattleOutcome.DEFEAT;
                    continue;
                }
            }

            boolean playerTurnEnded = false;
            switch (choice) {
                case 1: // Attack
                    // --- SHADOW ASP: EVASION ---
                    if (enemy.getEnemyType().contains("Asp") && Math.random() < 0.20) {
                        System.out.println("The Shadow Asp evades your attack!");
                        playerTurnEnded = true;
                        break;
                    }
                    int playerDamage = calculateDamage(player, enemy);
                    enemy.setHealth(Math.max(0, enemy.getHealth() - playerDamage));
                    System.out.println("You strike the " + enemy.getEnemyType() + " for " + playerDamage + " damage!");
                    playerTurnEnded = true;
                    break;
                case 2: // Defend
                    player.addTempDefense(5);
                    System.out.println("You brace yourself! (+5 DEF for this turn)");
                    playerTurnEnded = true;
                    break;
                case 3: // Use Item
                    playerTurnEnded = useItemMenu(player, scanner);
                    if (!playerTurnEnded) System.out.println("You fumbled with your items and lost your turn!");
                    break;
                case 4: // Escape
                    int escapeRoll = combatDice.roll();
                    System.out.println("You attempt to escape... (Rolled: " + escapeRoll + ")");
                    if (escapeRoll >= 10) {
                        System.out.println("You successfully flee!");
                        endBattle(BattleOutcome.ESCAPED);
                        return BattleOutcome.ESCAPED;
                    } else {
                        System.out.println("Escape failed!");
                        playerTurnEnded = true;
                    }
                    break;
                default:
                    System.out.println("Invalid choice. You hesitate, losing your turn!");
                    playerTurnEnded = true;
                    break;
            }

            if (enemy.getHealth() <= 0) {
                System.out.println("\n🏆 VICTORY! The " + enemy.getEnemyType() + " has been slain!");
                endBattle(BattleOutcome.VICTORY);
                return BattleOutcome.VICTORY;
            }

            if (playerTurnEnded) {
                enemyConsoleTurn(player, enemy);
                player.clearTemporaryBuffs();
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

        // --- ARMORED PYTHON: REGENERATION ---
        if (enemy.getEnemyType().contains("Python")) {
            int healAmt = 5;
            enemy.setHealth(Math.min(100, enemy.getHealth() + healAmt));
            System.out.println("  [REGEN] The Python regenerates " + healAmt + " HP! (Current HP: " + enemy.getHealth() + ")");
        }

        int enemyDamage = calculateDamage(enemy, player);

        // --- SHADOW ASP: CRITICAL STRIKE ---
        if (enemy.getEnemyType().contains("Asp") && lastRoll >= 18) {
            enemyDamage *= 2;
            System.out.println("  ⚔️ CRITICAL STRIKE!");
        }

        int newHealth = player.getHealth() - enemyDamage;
        if (newHealth <= 0) {
            player.setHealth(0);
            System.out.println("The " + enemy.getEnemyType() + " dealt " + enemyDamage + " damage. You have been defeated...");
        } else {
            player.setHealth(newHealth);
            System.out.println("The " + enemy.getEnemyType() + " dealt " + enemyDamage + " damage. Your HP: " + player.getHealth());
        }

        // --- VENOMOUS VIPER: POISON STRIKE ---
        if (enemy.getEnemyType().contains("Viper") && lastRoll >= 15) {
            playerPoisonTurns = 3;
            playerPoisonDamage = 3;
            System.out.println("  🧪 The Viper's venom seeps in! (Poisoned for 3 turns)");
        }

        // --- POISON TICK ---
        if (playerPoisonTurns > 0) {
            player.setHealth(player.getHealth() - playerPoisonDamage);
            playerPoisonTurns--;
            System.out.println("  🤢 Poison deals " + playerPoisonDamage + " damage! (HP: " + player.getHealth() + ")");
            if (player.getHealth() <= 0) {
                player.setHealth(0);
                System.out.println("The venom was too much... You have been defeated.");
            }
        }
    }

    private boolean useItemMenu(Player player, Scanner scanner) {
        Inventory inventory = player.getInventory();
        if (inventory == null || inventory.isEmpty()) { System.out.println("Your inventory is empty!"); return false; }
        inventory.displayInventory();
        System.out.println("0. Cancel");
        System.out.print("Select item slot to use: ");
        try {
            int itemChoice = Integer.parseInt(scanner.nextLine());
            if (itemChoice == 0) return false;
            return player.useItem(itemChoice - 1);
        } catch (Exception e) { System.out.println("Invalid selection."); return false; }
    }
}
