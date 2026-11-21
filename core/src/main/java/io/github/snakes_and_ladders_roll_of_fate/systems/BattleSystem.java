package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.*;
import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import java.util.Scanner;

public class BattleSystem {
    private Entity attacker;
    private Entity defender;
    private String battleState;
    private Dice combatDice = new Dice(20);

    // Enum to track how the battle ended
    public enum BattleOutcome { VICTORY, DEFEAT, ESCAPED, WEAKENED }

    public BattleSystem() {
        this.battleState = "IDLE";
    }

    public void startBattle(Entity attacker, Entity defender) {
        this.attacker = attacker;
        this.defender = defender;
        this.battleState = "IN_PROGRESS";
    }

    public int calculateDamage(Entity attacker, Entity defender) {
        int roll = combatDice.roll();
        int baseDamage = Math.max(1, attacker.getAttack() - defender.getDefense());
        if (roll == 20) {
            System.out.println("  ⚔️ CRITICAL HIT (Rolled a 20)!");
            return Math.max(2, baseDamage * 2 + 5);
        } else if (roll == 1) {
            System.out.println("  💨 CRITICAL MISS (Rolled a 1)!");
            return 0;
        } else {
            int rollBonus = (roll - 1) / 4;
            return baseDamage + rollBonus;
        }
    }

    // Runs the turn-based battle loop
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

            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. You hesitate, losing your turn!");
                enemyTurn(player, enemy);
                checkBattleEnd(player);
                if (player.getHealth() == 0) return BattleOutcome.DEFEAT;
                if (player.getHealth() <= 25) return BattleOutcome.WEAKENED;
                continue;
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
                        endBattle();
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
                endBattle();
                return BattleOutcome.VICTORY;
            }

            // Enemy Turn (if player didn't escape)
            if (playerTurnEnded) {
                enemyTurn(player, enemy);

                // Clear temporary defense buff after enemy attacks
                player.clearTemporaryBuffs();

                // Check battle end conditions
                if (player.getHealth() == 0) {
                    endBattle();
                    return BattleOutcome.DEFEAT;
                } else if (player.getHealth() <= 25) {
                    System.out.println("\n⚠️ You are too weak to continue fighting!");
                    endBattle();
                    return BattleOutcome.WEAKENED;
                }
            }
        }

        endBattle();
        return player.getHealth() > 0 ? BattleOutcome.VICTORY : BattleOutcome.DEFEAT;
    }

    private void enemyTurn(Player player, Enemy enemy) {
        System.out.println("\n-> " + enemy.getEnemyType() + " attacks!");
        int enemyDamage = calculateDamage(enemy, player);
        int newHealth = player.getHealth() - enemyDamage;

        if (newHealth <= 0) {
            // Mortal strike safeguard: Preserves your original logic where a lethal blow
            // knocks you down the slope with 1 HP if you were previously healthy.
            if (player.getHealth() > 25) {
                player.setHealth(1);
                System.out.println("The " + enemy.getEnemyType() + " dealt " + enemyDamage + " damage. Your HP: 1");
                System.out.println("  [SURVIVAL INSTINCT] The massive blow hurls you backwards off the ledge!");
            } else {
                player.setHealth(0);
                System.out.println("The " + enemy.getEnemyType() + " dealt " + enemyDamage + " damage. You have been defeated...");
            }
        } else {
            player.setHealth(newHealth);
            System.out.println("The " + enemy.getEnemyType() + " dealt " + enemyDamage + " damage. Your HP: " + player.getHealth());
        }
    }

    private void checkBattleEnd(Player player) {
        // Helper to ensure we catch state changes from invalid inputs
    }

    private boolean useItemMenu(Player player, Scanner scanner) {
        // Uses your exact Inventory class structure
        Inventory inventory = player.getInventory();

        if (inventory == null || inventory.isEmpty()) {
            System.out.println("Your inventory is empty!");
            return false;
        }

        // Uses the built-in display method from your Inventory class
        inventory.displayInventory();
        System.out.println("0. Cancel");
        System.out.print("Select item slot to use: ");

        try {
            int itemChoice = Integer.parseInt(scanner.nextLine());
            if (itemChoice == 0) return false;

            // Calls the Player's useItem method which safely routes to the Inventory class
            // We subtract 1 because the UI shows 1-based indexing, but the List is 0-based
            boolean used = player.useItem(itemChoice - 1);
            return used;
        } catch (Exception e) {
            System.out.println("Invalid selection.");
            return false;
        }
    }

    public void endBattle() {
        this.battleState = "ENDED";
    }

    public String getBattleState() {
        return battleState;
    }
}
