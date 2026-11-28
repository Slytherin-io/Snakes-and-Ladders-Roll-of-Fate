package io.github.snakes_and_ladders_roll_of_fate.systems;

import com.badlogic.gdx.utils.Timer;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
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
    private enum SerpentArchetype { VENOMOUS, REGEN_TANK, AGILE_STRIKER, STANDARD }

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
    private int enemyMaxHp = 0;
    private final Dice combatDice = new Dice(20);
    private int lastRoll = 0;

    private int playerTempDef = 0;
    private boolean isPlayerGuarding = false;
    private int playerPoisonTurns = 0;
    private int playerPoisonDamage = 0;
    private String playerDotLabel = "Poison";
    private boolean isDawnWarden = false;
    private int bossPhase = 1;
    private int phase2MaxHp = 150;
    private boolean justEnteredPhase2 = false;
    private String battleLog = "";
    private String battleState = "IDLE";
    private boolean turtleFortressActive = false;
    private int combatRound = 1;

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

    public int getLastRoll()              { return lastRoll; }
    public boolean isLastRollCritical()   { return lastRoll == 20; }
    public boolean isLastRollMiss()       { return lastRoll == 1; }
    public boolean isPlayerGuarding()     { return isPlayerGuarding; }
    public int getBossPhase()             { return bossPhase; }
    public int getPhase2MaxHp()           { return phase2MaxHp; }
    public int getCombatRound()           { return combatRound; }

    public boolean consumeJustEnteredPhase2() {
        boolean triggered = justEnteredPhase2;
        justEnteredPhase2 = false;
        return triggered;
    }

    public void startBattle(Player player, Enemy enemy) {
        this.player = player;
        this.enemy = enemy;
        if (player != null) {
            player.setInBattle(true);
            player.activateBattleBoons();
        }
        this.enemyMaxHp = (enemy != null) ? Math.max(1, enemy.getHealth()) : 100;
        this.playerTempDef = 0;
        this.isPlayerGuarding = false;
        this.playerPoisonTurns = 0;
        this.playerPoisonDamage = 0;
        this.playerDotLabel = "Poison";
        this.isDawnWarden = (enemy != null && enemy.getEnemyType() != null && enemy.getEnemyType().contains("Dawn Warden"));
        this.bossPhase = 1;
        this.combatRound = 1;
        this.phase2MaxHp = Math.max(150, this.enemyMaxHp + 30);
        this.justEnteredPhase2 = false;
        this.finalOutcome = null;
        this.currentState = BattleState.PLAYER_TURN;
        this.battleState = "IN_PROGRESS";
        this.turtleFortressActive = false;
        if (enemy != null && enemy.getEnemyType() != null && enemy.getEnemyType().contains("Pale Shepherd")) {
            this.battleLog = "The Pale Shepherd raises his crook in quiet contemplation.";
        } else if (enemy != null && enemy.getEnemyType() != null && enemy.getEnemyType().startsWith("The ")) {
            this.battleLog = enemy.getEnemyType() + " descends before you to test your spirit!";
        } else {
            this.battleLog = "A wild " + (enemy != null ? enemy.getEnemyType() : "serpent") + " blocks your path!";
        }
    }

    private boolean tryTriggerDawnWardenPhase2() {
        if (!isDawnWarden || bossPhase != 1 || enemy == null || enemy.getHealth() > 0) {
            return false;
        }
        bossPhase = 2;
        justEnteredPhase2 = true;
        enemyMaxHp = phase2MaxHp;
        enemy.setHealth(phase2MaxHp);
        enemy.setAttack(enemy.getAttack() + 3);
        enemy.setDefense(enemy.getDefense() + 2);
        enemy.setBehavior("Awakened Solar Chimera");
        battleLog = "THE DAWN WARDEN SHEDS HIS RESTRAINT - PHASE II: SOLAR ASCENSION!";
        return true;
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
    public void setBattleLog(String battleLog) { this.battleLog = battleLog; }
    public String getBattleState() { return battleState; }

    public void setBattleListener(BattleListener listener) {
        this.battleListener = listener;
    }

    public void setEnemyAttackStarter(EnemyAttackStarter starter) {
        this.enemyAttackStarter = starter;
    }

    private SerpentArchetype resolveSerpentArchetype(Enemy target) {
        if (target == null || target.getEnemyType() == null) return SerpentArchetype.STANDARD;
        String name = target.getEnemyType();
        if (name.contains("Python") || name.contains("Carapace") || name.contains("Basilisk")) {
            return SerpentArchetype.REGEN_TANK;
        }
        if (name.contains("Viper") || name.contains("Cobra") || name.contains("Wyrm")) {
            return SerpentArchetype.VENOMOUS;
        }
        if (name.contains("Asp") || name.contains("Adder") || name.contains("Rattler") || name.contains("Celestial")) {
            return SerpentArchetype.AGILE_STRIKER;
        }
        return SerpentArchetype.STANDARD;
    }

    private int applyGuardedMitigation(int raw) {
        int reduced = raw / 2;
        int damage = Math.max(0, reduced - Math.max(1, player.getDefense() / 2));
        if (raw > 0 && damage >= raw) {
            damage = Math.max(0, raw / 2);
        }
        return damage;
    }

    private int rollRiposteDamage(int riposteRoll) {
        int baseRiposte = (player.getCharacterClass() == CharacterClass.TURTLE)
            ? (player.getAttack() + 2)
            : Math.max(2, player.getAttack() / 2);
        return baseRiposte + ((riposteRoll - 1) / 5);
    }

    // --- PLAYER ACTIONS (called by Scene2D buttons) ---
    public void onPlayerAttack() {
        if (currentState != BattleState.PLAYER_TURN) return;

        // Agile/Stealthy serpents have a 20% evasion chance
        if (resolveSerpentArchetype(enemy) == SerpentArchetype.AGILE_STRIKER && Math.random() < 0.20) {
            if (battleListener != null) {
                battleListener.onDamageDealt(player, enemy, 0, false, true);
            }
            battleLog = "The " + enemy.getEnemyType() + " evades your attack!";
            triggerEnemyTurn();
            return;
        }

        int damage = calculateDamage(player, enemy);
        boolean isCrit = isLastRollCritical();
        boolean isMiss = isLastRollMiss();
        enemy.setHealth(Math.max(0, enemy.getHealth() - damage));

        if (battleListener != null) {
            battleListener.onDamageDealt(player, enemy, damage, isCrit, isMiss);
        }

        if (enemy.getHealth() <= 0) {
            if (tryTriggerDawnWardenPhase2()) {
                triggerEnemyTurn(1.50f);
                return;
            }
            battleLog = "You strike for " + damage + " damage! The " + enemy.getEnemyType() + " collapses!";
            endBattle(BattleOutcome.VICTORY);
        } else {
            battleLog = "You strike for " + damage + " damage!";
            triggerEnemyTurn(0.8f);
        }
    }

    public boolean isTurtleFortressActive() {
        return turtleFortressActive;
    }

    public void setTurtleFortressActive(boolean active) {
        this.turtleFortressActive = active;
    }

    public void onPlayerDefend() {
        if (currentState != BattleState.PLAYER_TURN) return;
        isPlayerGuarding = true;
        playerTempDef += 5;
        battleLog = "You raise your guard! (Damage halved and crits negated!)";
        triggerEnemyTurn();
    }

    public void onPlayerEscape() {
        if (currentState != BattleState.PLAYER_TURN) return;
        if (enemy != null && enemy.getEnemyType() != null && enemy.getEnemyType().contains("Pale Shepherd")) {
            battleLog = "You break through the obsidian mist and scramble for your life!";
            endBattle(BattleOutcome.ESCAPED);
            return;
        }
        int roll = combatDice.roll();
        if (roll >= 10) {
            battleLog = "Escape roll " + roll + " - you slip away!";
            endBattle(BattleOutcome.ESCAPED);
        } else {
            battleLog = "Escape roll " + roll + " - the " + enemy.getEnemyType() + " cuts you off!";
            triggerEnemyTurn();
        }
    }

    /** Routes through Inventory/Player API; blocks curses, board-only items, and lethal trade-off costs. */
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

        if (item.isCurse()) {
            battleLog = "Cursed burdens cannot be used! Cleanse at a Sanctuary or barter it.";
            return;
        }
        EventEffectType eff = item.getEffectType();
        if (eff == EventEffectType.MOVE || eff == EventEffectType.DICE_EXACT || eff == EventEffectType.DICE_CLAMP
                || eff == EventEffectType.SNAKE_WARD || eff == EventEffectType.SWAP_PLAYER || eff == EventEffectType.SCOUT) {
            battleLog = "That relic can only be used on the adventure board!";
            return;
        }
        if (item.getSecondaryEffectType() == EventEffectType.HEALTH && item.getSecondaryEffectValue() < 0
                && player.getHealth() + item.getSecondaryEffectValue() <= 0) {
            battleLog = "You are too wounded to survive the cost of " + item.getName() + "!";
            return;
        }

        boolean success = player.useItem(slotIndex);
        if (success) {
            battleLog = "You used " + item.getName() + "! (" + item.getEffect() + ")";
            triggerEnemyTurn();
        } else {
            battleLog = "You fumbled and cannot use " + item.getName() + "!";
        }
    }

    public void onPlayerUseSkill() {
        if (currentState != BattleState.PLAYER_TURN) return;
        if (!player.canUseSkill()) {
            battleLog = "Skill on cooldown! (" + player.getSkillCooldown() + " turns left)";
            return;
        }

        io.github.snakes_and_ladders_roll_of_fate.skills.ClassSkill skill = player.getClassSkill();
        if (skill == null) {
            battleLog = "No active class skill!";
            return;
        }

        io.github.snakes_and_ladders_roll_of_fate.skills.SkillResult result = skill.executeCombat(player, enemy, this);
        if (!result.isSuccess()) {
            battleLog = result.getMessage();
            return;
        }

        battleLog = result.getMessage();
        player.useSkill(); // Starts 4-turn cooldown

        if (enemy.getHealth() <= 0) {
            if (tryTriggerDawnWardenPhase2()) {
                player.clearTemporaryBuffs();
                currentState = BattleState.PLAYER_TURN;
                return;
            }
            battleLog += " The " + enemy.getEnemyType() + " collapses!";
            endBattle(BattleOutcome.VICTORY);
        } else {
            triggerEnemyTurn();
        }
    }

    // --- ENEMY TURN (delayed so the player can read the log) ---
    private void triggerEnemyTurn() {
        triggerEnemyTurn(0.8f);
    }

    private void triggerEnemyTurn(float delaySeconds) {
        currentState = BattleState.ENEMY_TURN;
        if (com.badlogic.gdx.Gdx.app == null) {
            executeEnemyAttack();
            return;
        }
        Timer.schedule(new Timer.Task() {
            @Override public void run() {
                if (enemyAttackStarter != null) {
                    enemyAttackStarter.onEnemyAttackStart();
                } else {
                    executeEnemyAttack();
                }
            }
        }, delaySeconds);
    }

    public void executeEnemyAttack() {
        if (currentState != BattleState.ENEMY_TURN) return;

        StringBuilder log = new StringBuilder();

        // Turtle's Iron Fortress: Complete immunity to attack and heavy defense-scaled riposte
        if (turtleFortressActive) {
            log.append("🛡️ IRON FORTRESS! You take 0 damage! ");
            int riposteDmg = player.getBaseDefense() * 3;
            int newEnemyHp = Math.max(0, enemy.getHealth() - riposteDmg);
            enemy.setHealth(newEnemyHp);
            log.append("Riposte deals ").append(riposteDmg).append(" damage! ");
            turtleFortressActive = false;

            if (battleListener != null) {
                battleListener.onDamageDealt(player, enemy, riposteDmg, true, false);
            }

            if (newEnemyHp <= 0) {
                if (tryTriggerDawnWardenPhase2()) {
                    player.clearTemporaryBuffs();
                    currentState = BattleState.PLAYER_TURN;
                    player.tickSkillCooldown();
                    return;
                }
                log.append("The ").append(enemy.getEnemyType()).append(" collapses from the riposte!");
                battleLog = log.toString();
                endBattle(BattleOutcome.VICTORY);
                player.tickSkillCooldown();
                return;
            }

            battleLog = log.toString();
            currentState = BattleState.PLAYER_TURN;
            player.tickSkillCooldown();
            return;
        }

        SerpentArchetype archetype = resolveSerpentArchetype(enemy);

        // Regenerative Tanks recover +5 HP at turn start (capped at their starting max HP)
        if (archetype == SerpentArchetype.REGEN_TANK) {
            int before = enemy.getHealth();
            int after = Math.min(enemyMaxHp, before + 5);
            enemy.setHealth(after);
            if (after > before) {
                log.append(enemy.getEnemyType()).append(" regenerates ").append(after - before).append(" HP! ");
            }
        }

        int raw = calculateDamage(enemy, player);
        final int enemyRoll = lastRoll;
        boolean isCrit = isLastRollCritical();
        boolean isMiss = isLastRollMiss();

        if (!isPlayerGuarding && archetype == SerpentArchetype.AGILE_STRIKER && enemyRoll >= 18) {
            if (!isCrit) {
                raw *= 2;
            }
            isCrit = true;
            log.append("CRITICAL STRIKE! ");
        }

        int damage;
        if (isPlayerGuarding) {
            if (isCrit) {
                isCrit = false;
                int baseDamage = Math.max(1, enemy.getAttack() - player.getDefense());
                raw = baseDamage + ((combatDice.roll() - 1) / 4);
            }
            damage = applyGuardedMitigation(raw);
            if (isDawnWarden && playerPoisonTurns > 0) {
                playerPoisonTurns = 0;
                log.append("Guard extinguishes Solar Burn! ");
            }
        } else {
            damage = Math.max(0, raw - playerTempDef);
        }
        playerTempDef = 0;

        int newHealth = player.getHealth() - damage;
        if (battleListener != null) {
            battleListener.onDamageDealt(enemy, player, damage, isCrit, isMiss);
        }

        if (newHealth <= 0) {
            player.setHealth(0);
            isPlayerGuarding = false;
            log.append(enemy.getEnemyType()).append(" deals ").append(damage).append(" damage - you have fallen in battle...");
            battleLog = log.toString();
            endBattle(BattleOutcome.DEFEAT);
            return;
        }

        player.setHealth(newHealth);

        // Guard mitigation (Teammate proposal: standard guard blocks damage/crits without generic riposte)
        if (isPlayerGuarding) {
            isPlayerGuarding = false;
            log.append("🛡️ Blocked! Took ").append(damage).append(" reduced damage! ");
        } else {
            log.append(enemy.getEnemyType()).append(" deals ").append(damage).append(" damage! ");
        }

        // Status DoT application: Venomous serpents poison; Dawn Warden Phase 2 inflicts Solar Burn
        if (archetype == SerpentArchetype.VENOMOUS && enemyRoll >= 15 && damage > 0) {
            playerPoisonTurns = 3;
            playerPoisonDamage = 3;
            playerDotLabel = "Poison";
            log.append("Venomous bite! (Poisoned for 3 turns) ");
        } else if (isDawnWarden && bossPhase >= 2 && enemyRoll >= 14 && damage > 0) {
            playerPoisonTurns = 2;
            playerPoisonDamage = 3;
            playerDotLabel = "Solar Burn";
            log.append("Solar flames sear you! (2 turns - Defend to douse) ");
        }

        // Status tick at end of enemy turn
        if (playerPoisonTurns > 0) {
            player.setHealth(player.getHealth() - playerPoisonDamage);
            playerPoisonTurns--;
            log.append("(").append(playerDotLabel).append(" deals ").append(playerPoisonDamage).append(" dmg!) ");
            if (player.getHealth() <= 0) {
                player.setHealth(0);
                log.append("You succumb to the affliction...");
                battleLog = log.toString();
                endBattle(BattleOutcome.DEFEAT);
                return;
            }
        }

        player.clearTemporaryBuffs();
        combatRound++;
        battleLog = log.toString();
        currentState = BattleState.PLAYER_TURN;
        player.tickSkillCooldown();
    }

    public void endBattle() {
        endBattle(BattleOutcome.VICTORY);
    }

    private void endBattle(BattleOutcome outcome) {
        if (player != null) {
            player.setInBattle(false);
            player.clearBattleBonuses();
            player.clearTemporaryBuffs();
        }
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
            System.out.println("1. Attack  2. Defend  3. Use Item  4. Escape");
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
            }

            boolean playerTurnEnded = false;
            switch (choice) {
                case 1: // Attack
                    if (resolveSerpentArchetype(enemy) == SerpentArchetype.AGILE_STRIKER && Math.random() < 0.20) {
                        System.out.println("The " + enemy.getEnemyType() + " evades your attack!");
                        playerTurnEnded = true;
                        break;
                    }
                    int playerDamage = calculateDamage(player, enemy);
                    enemy.setHealth(Math.max(0, enemy.getHealth() - playerDamage));
                    System.out.println("You strike the " + enemy.getEnemyType() + " for " + playerDamage + " damage!");
                    playerTurnEnded = true;
                    break;

                case 2: // Defend
                    isPlayerGuarding = true;
                    player.addTempDefense(5);
                    System.out.println("You raise your guard! (Damage halved, crits negated, riposte readied!)");
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

            if (enemy.getHealth() <= 0) {
                System.out.println("\n🏆 VICTORY! The " + enemy.getEnemyType() + " has been slain!");
                endBattle(BattleOutcome.VICTORY);
                return BattleOutcome.VICTORY;
            }

            if (playerTurnEnded) {
                enemyConsoleTurn(player, enemy);
                player.clearTemporaryBuffs();

                if (enemy.getHealth() <= 0) {
                    System.out.println("\n🏆 VICTORY! The " + enemy.getEnemyType() + " was felled by your riposte!");
                    endBattle(BattleOutcome.VICTORY);
                    return BattleOutcome.VICTORY;
                }

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
        SerpentArchetype archetype = resolveSerpentArchetype(enemy);

        if (archetype == SerpentArchetype.REGEN_TANK) {
            int before = enemy.getHealth();
            int after = Math.min(enemyMaxHp, before + 5);
            enemy.setHealth(after);
            if (after > before) {
                System.out.println("  [REGEN] " + enemy.getEnemyType() + " regenerates " + (after - before) + " HP! (Current HP: " + after + ")");
            }
        }

        int raw = calculateDamage(enemy, player);
        final int enemyRoll = lastRoll;
        boolean isCrit = isLastRollCritical();

        if (!isPlayerGuarding && archetype == SerpentArchetype.AGILE_STRIKER && enemyRoll >= 18) {
            if (!isCrit) raw *= 2;
            System.out.println("  ⚔️ CRITICAL STRIKE!");
        }

        int damage;
        if (isPlayerGuarding) {
            if (isCrit) {
                int baseDamage = Math.max(1, enemy.getAttack() - player.getDefense());
                raw = baseDamage + ((combatDice.roll() - 1) / 4);
                System.out.println("  [DEFLECTED] Guard negated the serpent's critical strike!");
            }
            damage = applyGuardedMitigation(raw);
            System.out.println("  [GUARD] Shield absorbed " + (raw - damage) + " damage! (Damage taken: " + damage + ")");
        } else {
            damage = raw;
        }

        int newHealth = player.getHealth() - damage;
        if (newHealth <= 0) {
            player.setHealth(0);
            System.out.println("The " + enemy.getEnemyType() + " dealt " + damage + " damage. You have been defeated...");
            isPlayerGuarding = false;
            return;
        }

        player.setHealth(newHealth);
        System.out.println("The " + enemy.getEnemyType() + " dealt " + damage + " damage. Your HP: " + player.getHealth());

        if (isPlayerGuarding) {
            isPlayerGuarding = false;
            int riposteDamage = rollRiposteDamage(combatDice.roll());
            int newEnemyHp = Math.max(0, enemy.getHealth() - riposteDamage);
            enemy.setHealth(newEnemyHp);
            System.out.println("  ⚡ [RIPOSTE] You counter-attacked for " + riposteDamage + " damage! (" + enemy.getEnemyType() + " HP: " + newEnemyHp + ")");
            if (newEnemyHp <= 0) {
                return;
            }
        }

        if (archetype == SerpentArchetype.VENOMOUS && enemyRoll >= 15 && damage > 0) {
            playerPoisonTurns = 3;
            playerPoisonDamage = 3;
            System.out.println("  🧪 The " + enemy.getEnemyType() + "'s venom seeps in! (Poisoned for 3 turns)");
        }

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
            return player.useItem(itemChoice - 1);
        } catch (Exception e) {
            System.out.println("Invalid selection.");
            return false;
        }
    }
}
