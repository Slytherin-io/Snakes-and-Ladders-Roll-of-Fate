package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Enemy;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem;
import io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleOutcome;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType;
import java.util.Random;
import java.util.Scanner;

/**
 * Snake tile for the board.
 * In the GUI, BoardScreen detects the landing, shows the ambush narrative modal,
 * and launches BattleScreen if the player engages in combat.
 * For console play and unit tests, startCombat() runs the encounter synchronously.
 */
public class SnakeTile extends Tile {
    private int destination;
    private static final Random random = new Random();
    private static final Scanner scanner = new Scanner(System.in);

    public SnakeTile(int position, int destination) {
        super(position, "SNAKE", "Slide backwards to a lower tile!", EventCategory.SNAKE);
        this.destination = destination;
    }

    @Override
    public void onLand(Player player) {
        startCombat(player);
    }

    private static final String[][] SPECIES_NAMES = {
        {"Emerald Viper", "Obsidian Cobra", "Magma Wyrm"},                 // Venomous / Aggressive
        {"Forest Python", "Crag Carapace Serpent", "Abyssal Basilisk"},     // Defensive / Regenerating
        {"Moss Adder", "Cave Rattler", "Apex Celestial Serpent"}            // Stealthy / Evasive
    };

    private static final String[][] SPECIES_REWARDS = {
        {"Emerald Scales", "Obsidian Scale", "Heart of Magma"},
        {"Python Leather", "Crag Serpent Plate", "Basilisk Carapace"},
        {"Adder Venom Sac", "Rattler Fang", "Celestial Serpent Crest"}
    };

    /**
     * Factory: builds Act-themed, tier-balanced serpents (matching upstream's capped stats
     * to prevent late-game damage sponges while keeping Act I-III species variety).
     * Act I (Tiles 1-30): Emerald Viper, Forest Python, Moss Adder
     * Act II (Tiles 31-70): Obsidian Cobra, Crag Carapace Serpent, Cave Rattler
     * Act III (Tiles 71-100): Magma Wyrm, Abyssal Basilisk, Apex Celestial Serpent
     */
    public Enemy createSnakeEnemy() {
        int pos = getPosition();
        int tier = (pos <= 30) ? 1 : ((pos <= 70) ? 2 : 3);
        int idx = tier - 1;
        int variant = random.nextInt(3);

        Enemy snake = new Enemy(SPECIES_NAMES[variant][idx]);
        snake.setReward(SPECIES_REWARDS[variant][idx]);

        switch (variant) {
            case 0: // Venomous / Aggressive (Glass Cannon — Toxic Bite)
                snake.setBehavior("Aggressive (Venomous)");
                snake.setAttack(4 + (tier * 3));      // ATK: 7, 10, 13
                snake.setHealth(15 + (tier * 10));    // HP:  25, 35, 45
                snake.setDefense(tier);               // DEF: 1,  2,  3
                break;

            case 1: // Defensive / Regenerating Tank (High Bulk, Capped DEF)
                snake.setBehavior("Defensive (Regenerating)");
                snake.setAttack(2 + (tier * 2));      // ATK: 4,  6,  8
                snake.setHealth(25 + (tier * 15));    // HP:  40, 55, 70
                snake.setDefense(1 + tier);           // DEF: 2,  3,  4 (Capped to prevent immunity)
                break;

            default: // Stealthy / Evasive Striker (20% Dodge, Crit Strikes)
                snake.setBehavior("Stealthy (Evasive)");
                snake.setAttack(3 + (tier * 3));      // ATK: 6,  9,  12
                snake.setHealth(20 + (tier * 12));    // HP:  32, 44, 56
                snake.setDefense(tier);               // DEF: 1,  2,  3
                break;
        }
        return snake;
    }

    /** Runs synchronous combat for console gameplay and tests. */
    public void startCombat(Player player) {
        Enemy snake = createSnakeEnemy();
        System.out.println("\n🐍 A wild " + snake.getEnemyType() + " blocks your path!");

        BattleSystem battle = new BattleSystem();
        BattleOutcome outcome = battle.runBattle(player, snake, scanner);

        String msg = resolveOutcome(player, snake, outcome);
        System.out.println(msg);
    }

    public Item createSnakeSlayerLoot(int pos) {
        int act = (pos <= 30) ? 1 : ((pos <= 70) ? 2 : 3);
        if (act == 1) {
            return (random.nextBoolean())
                ? Item.createConsumable("Venom Coating Vial", EventEffectType.ATTACK, 4, "+4 ATK (Serpent venom for 1 combat)")
                : Item.createEquipment("Serpent Scale Buckler", EventEffectType.DEFENSE, 2, 2, 4, "+2 DEF (Tough viper hide shield)");
        } else if (act == 2) {
            return (random.nextBoolean())
                ? Item.createEquipment("Shed Serpent Skin", EventEffectType.HEALTH, 30, 2, 4, "+30 Max HP Ward & Camouflage")
                : Item.createEquipment("Crag Carapace Plate", EventEffectType.DEFENSE, 3, 2, 5, "+3 DEF (Crag serpent forged plate)");
        } else {
            return (random.nextBoolean())
                ? Item.createEquipment("Apex Serpent Fang", EventEffectType.ATTACK, 6, 3, 5, "+6 ATK (Apex celestial serpent venom blade)")
                : Item.createConsumable("Heart of Magma", EventEffectType.HEALTH, 60, "Restores 60 HP and stokes fierce inner fire");
        }
    }

    /**
     * Called by BattleScreen (or console combat) when the battle reaches BATTLE_END.
     * Applies EXP / loot / slide and returns a message for the UI/console.
     */
    public String resolveOutcome(Player player, Enemy snake, BattleOutcome outcome) {
        switch (outcome) {
            case VICTORY: {
                int expGained = 20 + (getPosition() / 2);
                player.gainExperience(expGained);

                // Slayer Stat Mastery: Permanent stat increase on serpent kill!
                player.setAttack(player.getBaseAttack() + 1);
                player.modifyMaxHealth(5);

                Item loot = createSnakeSlayerLoot(getPosition());
                boolean added = player.addItem(loot);
                String msg = "VICTORY! Slayed " + snake.getEnemyType() + "! +" + expGained + " EXP | Perm +1 ATK & +5 Max HP!";
                msg += added ? " | Loot: " + loot.getName() : " | Satchel full - loot lost!";
                return msg;
            }
            case ESCAPED:
                applyEffect(player);
                return "You escape, but the tail swipes you down to Tile " + destination + "!";
            case WEAKENED:
                applyEffect(player);
                return "Barely conscious, you tumble down the coils to Tile " + destination + "!";
            case DEFEAT:
            default:
                return "You have been slain by the " + snake.getEnemyType() + "!";
        }
    }

    public void applyEffect(Player player) {
        player.setPosition(destination);
        System.out.println("  [SLIDE] You slid down to Tile " + destination);
    }

    public int getDestination() { return destination; }
    public void setDestination(int destination) { this.destination = destination; }

    public int getSnakeAttack() {
        return 6 + (getPosition() / 7);
    }

    public int getSnakeHealth() {
        return 15 + (getPosition() / 2);
    }
}
