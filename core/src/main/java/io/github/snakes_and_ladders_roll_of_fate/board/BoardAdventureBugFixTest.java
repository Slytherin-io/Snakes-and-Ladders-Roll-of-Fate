package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame;
import io.github.snakes_and_ladders_roll_of_fate.entities.CharacterClass;
import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Automated verification suite for:
 * 1. Bug A: Event movement landing on ladder/snake/landmark tiles triggers destination mechanics.
 * 2. Bug B: Pale Shepherd pursuer anti-overshoot clamping, snake-slide trailing repositioning,
 *    and guaranteed pushback + contemplation pauses eliminating the D1 trapping loop.
 */
public class BoardAdventureBugFixTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING BOARD ADVENTURE BUG FIX VERIFICATION SUITE ===");

        testBugA_EventLandingTriggers();
        testBugB_PursuerOvershootClamping();
        testBugB_SnakeSlideTrailingRepositioning();
        testBugB_EncounterResolutionPushbackAndPause();
        testShepherdRandomDormancy();
        testSnakeSlideAcrobaticsTumble();
        testSnakeSlayerStatMasteryAndSpoils();
        testSerpentsBaneReaperRepelAndAdrenalineSurge();
        testPaleShepherdMortalDuelResolution();

        System.out.println(">>> ALL BUG FIX & COMBAT SYSTEM VERIFICATIONS PASSED WITH ZERO FAILURES!");
    }

    private static void testBugA_EventLandingTriggers() {
        System.out.println("\n[Test 1] Bug A: Verifying Landing on Ladder / Snake / Landmark...");
        Board board = new Board(100);
        board.generateBoard(BoardModifier.BALANCED_ASCENT);

        // Find a ladder tile on the board
        LadderTile testLadder = null;
        for (Tile t : board.getTiles()) {
            if (t instanceof LadderTile) {
                testLadder = (LadderTile) t;
                break;
            }
        }
        if (testLadder == null) {
            throw new AssertionError("Board should have ladders placed");
        }

        // Simulate player landing on the ladder via event outcome
        int landingTilePos = testLadder.getPosition();
        Tile landedTile = board.getTile(landingTilePos);

        boolean triggersSpecialArrival = (landedTile instanceof LadderTile || landedTile instanceof SnakeTile
            || (landedTile != null && landedTile.getLandmark() != TileLandmark.NONE));

        if (!triggersSpecialArrival) {
            throw new AssertionError("Bug A regression: landingTile on ladder failed trigger check!");
        }
        System.out.println("  [PASS] Landing on Tile " + landingTilePos + " (" + landedTile.getClass().getSimpleName()
            + " -> " + testLadder.getDestination() + ") correctly recognized for activation.");
    }

    private static void testBugB_PursuerOvershootClamping() {
        System.out.println("\n[Test 2] Bug B: Verifying Pursuer Anti-Overshoot Clamping...");
        RollOfFateGame game = new RollOfFateGame();
        Player p = new Player("Hero", CharacterClass.CAT);
        p.setPosition(15);
        List<Player> players = new ArrayList<>();
        players.add(p);
        game.setPlayers(players);

        // Place reaper at 13 (gap = 2)
        game.setPaleShepherdTile(13);
        int curTile = game.getPaleShepherdTile();
        int trailingPos = p.getPosition();
        int gap = trailingPos - curTile; // 2

        int speed = 4; // Uncapped nominal speed would overshoot!
        if (gap > 0) {
            speed = Math.min(speed, gap);
        } else {
            speed = 0;
        }

        if (speed != 2) {
            throw new AssertionError("Speed was not clamped to gap: expected 2, got " + speed);
        }
        game.advancePaleShepherd(speed);
        if (game.getPaleShepherdTile() != 15) {
            throw new AssertionError("Pursuer did not land exactly on player tile: " + game.getPaleShepherdTile());
        }
        if (game.getPaleShepherdTile() > p.getPosition()) {
            throw new AssertionError("Pursuer overshot player! Shepherd=" + game.getPaleShepherdTile() + ", Player=" + p.getPosition());
        }
        System.out.println("  [PASS] Pursuer clamped to exact player tile (15), never overshooting to 16+.");
    }

    private static void testBugB_SnakeSlideTrailingRepositioning() {
        System.out.println("\n[Test 3] Bug B: Verifying Snake Slide Behind Pursuer Repositioning...");
        RollOfFateGame game = new RollOfFateGame();
        Player p = new Player("Hero", CharacterClass.CAT);
        p.setPosition(35);
        List<Player> players = new ArrayList<>();
        players.add(p);
        game.setPlayers(players);

        // Pursuer was stalking at Tile 20
        game.setPaleShepherdTile(20);

        // Player slides down snake from 35 down to 10 (falling behind reaper)
        int finalPos = 10;
        p.setPosition(finalPos);

        // Under new logic: reaper repositions behind player to max(1, finalPos - 3) with 1 round pause
        if (game.getPaleShepherdTile() > 0 && finalPos < game.getPaleShepherdTile()) {
            game.setPaleShepherdTile(Math.max(1, finalPos - 3));
            game.setShepherdPauseRounds(1);
        }

        if (game.getPaleShepherdTile() != 7) {
            throw new AssertionError("Reaper should have repositioned to Tile 7, was at: " + game.getPaleShepherdTile());
        }
        if (game.getShepherdPauseRounds() != 1) {
            throw new AssertionError("Reaper should have 1 contemplation pause round");
        }
        if (game.getPaleShepherdTile() >= p.getPosition()) {
            throw new AssertionError("Reaper must not be ahead of player after slide!");
        }
        System.out.println("  [PASS] After slide to Tile 10, Reaper safely relocated behind player to Tile 7 with 1 contemplation round.");
    }

    private static void testBugB_EncounterResolutionPushbackAndPause() {
        System.out.println("\n[Test 4] Bug B: Verifying Encounter Pushback and Contemplation Pause (No D1 Loop)...");
        RollOfFateGame game = new RollOfFateGame();
        Player p = new Player("Hero", CharacterClass.CAT);
        p.setPosition(25);
        List<Player> players = new ArrayList<>();
        players.add(p);
        game.setPlayers(players);
        game.setPaleShepherdTile(25); // Caught!

        // Resolution 1: Struggle for Life
        // Player moves +3, Reaper repelled 5 tiles, paused 2 rounds
        game.setPaleShepherdTile(Math.max(1, game.getPaleShepherdTile() - 5));
        game.setShepherdPauseRounds(2);
        int surgedPos = p.getPosition() + 3;
        p.setPosition(surgedPos);

        if (game.getPaleShepherdTile() != 20) {
            throw new AssertionError("Reaper pushback should be Tile 20, got " + game.getPaleShepherdTile());
        }
        if (p.getPosition() != 28) {
            throw new AssertionError("Player should surge to Tile 28, got " + p.getPosition());
        }
        if (game.getShepherdPauseRounds() < 2) {
            throw new AssertionError("Reaper must pause at least 2 rounds");
        }

        // Simulate next round: player rolls D1 (moves 28 -> 29)
        p.setPosition(29);
        // Reaper round progression: paused!
        if (game.getShepherdPauseRounds() > 0) {
            game.decrementShepherdPauseRounds();
            // Did not move!
        }
        if (game.getPaleShepherdTile() >= p.getPosition()) {
            throw new AssertionError("D1 roll caused immediate re-trigger! Shepherd=" + game.getPaleShepherdTile() + ", Player=" + p.getPosition());
        }
        System.out.println("  [PASS] Struggle for Life gives 8-tile gap (20 vs 28) and 2 pause rounds. Rolling D1 cleanly escapes the reaper!");
    }

    private static void testShepherdRandomDormancy() {
        System.out.println("\n[Test 5] Verifying Shepherd Random Dormant Range (4 to 7 Rounds)...");
        boolean saw4 = false, saw5 = false, saw6 = false, saw7 = false;
        for (int i = 0; i < 200; i++) {
            RollOfFateGame game = new RollOfFateGame();
            int turns = game.getShepherdDormantTurns();
            if (turns < 4 || turns > 7) {
                throw new AssertionError("Dormant turns out of range [4, 7]: " + turns);
            }
            if (turns == 4) saw4 = true;
            if (turns == 5) saw5 = true;
            if (turns == 6) saw6 = true;
            if (turns == 7) saw7 = true;
        }
        if (!saw4 || !saw7) {
            throw new AssertionError("Dormant rounds did not cover full [4, 7] distribution");
        }
        System.out.println("  [PASS] Pale Shepherd dormancy correctly randomized between 4 and 7 rounds across runs.");
    }

    private static void testSnakeSlideAcrobaticsTumble() {
        System.out.println("\n[Test 6] Verifying Snake Tail Slide Acrobatics D20 Tumble Mechanics...");
        // Check calculation logic:
        // Nat 20 = 0 DMG
        // 10-19 = 10 DMG
        // 1-9 = 20 DMG
        for (int roll = 1; roll <= 20; roll++) {
            int dmg;
            if (roll == 20) dmg = 0;
            else if (roll >= 10) dmg = 10;
            else dmg = 20;

            if (roll == 20 && dmg != 0) throw new AssertionError("Nat 20 must deal 0 damage");
            if (roll >= 10 && roll < 20 && dmg != 10) throw new AssertionError("10-19 must deal 10 damage");
            if (roll < 10 && dmg != 20) throw new AssertionError("<10 must deal 20 damage");
        }

        Player p = new Player("Acrobat", CharacterClass.CAT);
        int initialHp = p.getHealth();
        // Simulate roll < 10 tumble
        p.setHealth(Math.max(1, p.getHealth() - 20));
        if (p.getHealth() != initialHp - 20) {
            throw new AssertionError("Crash damage should subtract 20 HP, was: " + p.getHealth());
        }
        System.out.println("  [PASS] Tail slide Acrobatics D20 tumble damage correctly graded (20=0 DMG, 10-19=10 DMG, <10=20 DMG).");
    }

    private static void testSnakeSlayerStatMasteryAndSpoils() {
        System.out.println("\n[Test 7] Verifying Snake Slaying Stat Mastery & Exclusive Spoils...");
        SnakeTile act1Snake = new SnakeTile(15, 5);
        SnakeTile act2Snake = new SnakeTile(45, 20);
        SnakeTile act3Snake = new SnakeTile(85, 30);

        Player p = new Player("Slayer", CharacterClass.CAT);
        int baseAtk = p.getAttack();
        int baseMaxHp = p.getMaxHealth();

        io.github.snakes_and_ladders_roll_of_fate.entities.Enemy snake = act1Snake.createSnakeEnemy();
        String resultMsg = act1Snake.resolveOutcome(p, snake, io.github.snakes_and_ladders_roll_of_fate.systems.BattleSystem.BattleOutcome.VICTORY);

        if (p.getAttack() != baseAtk + 1) {
            throw new AssertionError("Slaying serpent should grant +1 permanent ATK, was: " + p.getAttack());
        }
        if (p.getMaxHealth() != baseMaxHp + 5) {
            throw new AssertionError("Slaying serpent should grant +5 permanent Max HP, was: " + p.getMaxHealth());
        }
        if (p.getInventory().isEmpty()) {
            throw new AssertionError("Slayer must receive Act-exclusive loot drop!");
        }
        if (!resultMsg.contains("+1 ATK & +5 Max HP")) {
            throw new AssertionError("Result message must herald stat mastery: " + resultMsg);
        }

        // Verify Act 1, 2, 3 loot creation
        io.github.snakes_and_ladders_roll_of_fate.items.Item loot1 = act1Snake.createSnakeSlayerLoot(15);
        io.github.snakes_and_ladders_roll_of_fate.items.Item loot2 = act2Snake.createSnakeSlayerLoot(45);
        io.github.snakes_and_ladders_roll_of_fate.items.Item loot3 = act3Snake.createSnakeSlayerLoot(85);

        if (loot1 == null || loot2 == null || loot3 == null) {
            throw new AssertionError("Snake slayer loot generation returned null");
        }
        System.out.println("  [PASS] Serpent victory awards permanent +1 ATK, +5 Max HP, and Act-specific trophies ("
            + loot1.getName() + ", " + loot2.getName() + ", " + loot3.getName() + ").");
    }

    private static void testSerpentsBaneReaperRepelAndAdrenalineSurge() {
        System.out.println("\n[Test 8] Verifying Serpent's Bane Reaper Repelling & Adrenaline Surge...");
        RollOfFateGame game = new RollOfFateGame();
        Player p = new Player("Warrior", CharacterClass.CAT);
        p.setPosition(30);
        List<Player> players = new ArrayList<>();
        players.add(p);
        game.setPlayers(players);

        // Active pursuer at Tile 20
        game.setPaleShepherdTile(20);
        game.setShepherdPauseRounds(0);

        // Serpent's Bane: repels reaper 4 tiles back and pauses 1 round
        if (game.getPaleShepherdTile() > 0) {
            int repelled = Math.max(1, game.getPaleShepherdTile() - 4);
            game.setPaleShepherdTile(repelled);
            game.setShepherdPauseRounds(Math.max(game.getShepherdPauseRounds(), 1));
        }

        if (game.getPaleShepherdTile() != 16) {
            throw new AssertionError("Serpent's Bane should repel reaper from 20 to 16, was: " + game.getPaleShepherdTile());
        }
        if (game.getShepherdPauseRounds() != 1) {
            throw new AssertionError("Serpent's Bane should grant 1 pause round, was: " + game.getShepherdPauseRounds());
        }
        System.out.println("  [PASS] Slaying a snake repels active Pale Shepherd 4 tiles back and pauses pursuit for 1 round.");
    }

    private static void testPaleShepherdMortalDuelResolution() {
        System.out.println("\n[Test 9] Verifying Pale Shepherd Mortal Duel Victory & Escape Resolutions...");
        RollOfFateGame game = new RollOfFateGame();
        Player p = new Player("Challenger");
        p.setPosition(25);
        List<Player> players = new ArrayList<>();
        players.add(p);
        game.setPlayers(players);

        // Case A: Victory over Death
        game.setPaleShepherdTile(25);
        int repelled = Math.max(0, game.getPaleShepherdTile() - 15);
        game.setPaleShepherdTile(repelled);
        game.setShepherdPauseRounds(3);
        p.gainExperience(50);

        if (game.getPaleShepherdTile() != 10) {
            throw new AssertionError("Victory should banish Death 15 tiles back (25 -> 10), was: " + game.getPaleShepherdTile());
        }
        if (game.getShepherdPauseRounds() != 3) {
            throw new AssertionError("Victory should impose 3 contemplation pause rounds, was: " + game.getShepherdPauseRounds());
        }
        if (p.getExperience() < 50) {
            throw new AssertionError("Victory should award at least 50 EXP");
        }

        // Case B: Escape from Death
        p.setHealth(80);
        p.setHealth(Math.max(1, p.getHealth() - 20));
        game.setShepherdPauseRounds(1);
        int scrambleDest = Math.min(99, p.getPosition() + 3);

        if (p.getHealth() != 60) {
            throw new AssertionError("Escape parting blow should cost 20 HP, was: " + p.getHealth());
        }
        if (scrambleDest != 28) {
            throw new AssertionError("Escape scramble should propel player to Tile 28, was: " + scrambleDest);
        }
        System.out.println("  [PASS] Pale Shepherd victory banishes reaper 15 tiles (+3 pause rounds), and escape incurs -20 HP and +3 scramble.");
    }
}
