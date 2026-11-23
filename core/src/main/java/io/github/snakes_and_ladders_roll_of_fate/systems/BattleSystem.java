package io.github.snakes_and_ladders_roll_of_fate.systems;

import com.badlogic.gdx.utils.Timer;
import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Entity;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;

/**
 * UI-driven battle state machine for LibGDX.
 * No Scanner, no while-loop: BattleScreen buttons call the onPlayerX() methods,
 * and the enemy turn runs on a Timer so the 60 FPS render loop never freezes.
 */
public class BattleSystem {

    public enum BattleState { PLAYER_TURN, ENEMY_TURN, BATTLE_END }
    public enum BattleOutcome { VICTORY, DEFEAT, ESCAPED, WEAKENED }

    private BattleState currentState = BattleState.BATTLE_END;
    private BattleOutcome finalOutcome = null;

    private Player player;
    private Enemy enemy;
    private final Dice combatDice = new Dice(20);
    // 1. Add this field next to combatDice
    private int lastRoll = 0;

    // 2. Replace calculateDamage with this roll-recording version
    public int calculateDamage(Entity attacker, Entity defender) {
        lastRoll = combatDice.roll(); // remember the roll so the UI can report crits/misses
        int baseDamage = Math.max(1, attacker.getAttack() - defender.getDefense());
        if (lastRoll == 20) return Math.max(2, baseDamage * 2 + 5);
        if (lastRoll == 1)  return 0;
        return baseDamage + ((lastRoll - 1) / 4);
    }

    // 3. Add these three getters anywhere in the class
    public int getLastRoll()            { return lastRoll; }
    public boolean isLastRollCritical() { return lastRoll == 20; }
    public boolean isLastRollMiss()     { return lastRoll == 1; }

    private int playerTempDef = 0;
    private String battleLog = "";

    public void startBattle(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
        this.playerTempDef = 0;
        this.finalOutcome = null;
        this.currentState = BattleState.PLAYER_TURN;
        this.battleLog = "A wild " + enemy.getEnemyType() + " blocks your path!";
    }

    // --- GETTERS FOR THE UI ---
    public BattleState getCurrentState() { return currentState; }
    public BattleOutcome getFinalOutcome() { return finalOutcome; }
    public Player getPlayer() { return player; }
    public Enemy getEnemy() { return enemy; }
    public String getBattleLog() { return battleLog; }

    // --- PLAYER ACTIONS (called by Scene2D buttons) ---
    public void onPlayerAttack() {
        if (currentState != BattleState.PLAYER_TURN) return;
        int damage = calculateDamage(player, enemy);
        enemy.setHealth(Math.max(0, enemy.getHealth() - damage));
        if (enemy.getHealth() <= 0) {
            battleLog = "You strike for " + damage + " damage! The " + enemy.getEnemyType() + " collapses!";
            endBattle(BattleOutcome.VICTORY);
        } else {
            battleLog = "You strike for " + damage + " damage! (" + enemy.getEnemyType() + " HP: " + enemy.getHealth() + ")";
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
    // Update the signature to accept the slot index
    // Update the signature to accept the slot index
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
            @Override public void run() { executeEnemyAttack(); }
        }, 1.2f);
    }

    private void executeEnemyAttack() {
        if (currentState != BattleState.ENEMY_TURN) return;
        int raw = calculateDamage(enemy, player);
        int damage = Math.max(0, raw - playerTempDef);
        playerTempDef = 0;

        int newHealth = player.getHealth() - damage;
        if (newHealth <= 0) {
            if (player.getHealth() > 25) { // Mortal Strike Safeguard
                player.setHealth(1);
                battleLog = enemy.getEnemyType() + " deals " + damage + " - SURVIVAL INSTINCT leaves you at 1 HP!";
                endBattle(BattleOutcome.WEAKENED);
            } else {
                player.setHealth(0);
                battleLog = enemy.getEnemyType() + " deals " + damage + " - you fall...";
                endBattle(BattleOutcome.DEFEAT);
            }
            return;
        }
        player.setHealth(newHealth);
        if (player.getHealth() <= 25) {
            battleLog = enemy.getEnemyType() + " deals " + damage + " - you are too weak to continue!";
            endBattle(BattleOutcome.WEAKENED);
        } else {
            battleLog = enemy.getEnemyType() + " deals " + damage + " damage! (Your HP: " + player.getHealth() + ")";
            currentState = BattleState.PLAYER_TURN;
        }
    }

    private void endBattle(BattleOutcome outcome) {
        this.finalOutcome = outcome;
        this.currentState = BattleState.BATTLE_END;
    }
}
