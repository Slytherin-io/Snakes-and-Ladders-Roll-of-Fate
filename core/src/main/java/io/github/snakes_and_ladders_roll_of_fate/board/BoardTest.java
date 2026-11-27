package io.github.snakes_and_ladders_roll_of_fate.board;

import java.util.List;
import java.util.Random;

/**
 * Rigorous test suite for the Two-Pass Board Generation system.
 * Verifies milestone safety, anti-clustering, destination validity,
 * and zero-loop invariants across 1,000 generated boards.
 */
public class BoardTest {
    public static void main(String[] args) {
        System.out.println("=== RUNNING TWO-PASS BOARD GENERATION TEST SUITE ===");

        // Run across 1,000 distinct seeds to verify edge cases across ALL BoardModifiers
        BoardModifier[] modifiers = BoardModifier.values();
        double totalLaddersSum = 0, act1LaddersSum = 0, act2LaddersSum = 0, act3LaddersSum = 0;
        double totalSnakesSum = 0, act1SnakesSum = 0, act2SnakesSum = 0, act3SnakesSum = 0;

        for (int run = 0; run < 1000; run++) {
            Random rng = new Random(run * 31337L + 7L);
            Board board = new Board(100, rng);
            BoardModifier mod = modifiers[run % modifiers.length];
            board.generateBoard(mod);

            if (board.getActiveModifier() != mod) {
                throw new AssertionError("Board activeModifier mismatch");
            }
            if (board.getPoiCount(TileLandmark.CROSSROADS) < 3) {
                throw new AssertionError("Board should place at least 3 Crossroads across Acts, found: " + board.getPoiCount(TileLandmark.CROSSROADS));
            }

            List<Tile> tiles = board.getTiles();
            if (tiles.size() != 100) {
                throw new AssertionError("Board must have exactly 100 tiles, had: " + tiles.size());
            }

            // Option A Escalating Danger verification: Ladder Caps
            int lCount = board.getLadderCount();
            int lAct1 = board.getLaddersInAct(1);
            int lAct2 = board.getLaddersInAct(2);
            int lAct3 = board.getLaddersInAct(3);
            if (lCount > 10) {
                throw new AssertionError("Board global ladders exceeded cap of 10, found: " + lCount);
            }
            if (lAct1 > 5) {
                throw new AssertionError("Act 1 ladders exceeded cap of 5, found: " + lAct1);
            }
            if (lAct2 > 3) {
                throw new AssertionError("Act 2 ladders exceeded cap of 3, found: " + lAct2);
            }
            if (lAct3 > 2) {
                throw new AssertionError("Act 3 ladders exceeded cap of 2, found: " + lAct3);
            }

            totalLaddersSum += lCount;
            act1LaddersSum += lAct1;
            act2LaddersSum += lAct2;
            act3LaddersSum += lAct3;

            totalSnakesSum += board.getSnakeCount();
            act1SnakesSum += board.getSnakesInAct(1);
            act2SnakesSum += board.getSnakesInAct(2);
            act3SnakesSum += board.getSnakesInAct(3);

            // 1. Milestone Safe Zones
            assertNormal(board, 1, "Tile 1 (Start Camp) must be NORMAL");
            assertNormal(board, 2, "Tile 2 (Runway) must be NORMAL");
            assertNormal(board, 3, "Tile 3 (Runway) must be NORMAL");
            assertNormal(board, 30, "Tile 30 (Act I Sanctuary) must be NORMAL");
            assertNormal(board, 70, "Tile 70 (Act II Abyssal Gate) must be NORMAL");
            assertNormal(board, 98, "Tile 98 (Throne Approach) must be NORMAL");
            assertNormal(board, 99, "Tile 99 (Throne Approach) must be NORMAL");
            assertNormal(board, 100, "Tile 100 (Throne of Fate) must be NORMAL");

            // 2. Anti-Clustering & Destination Validation
            int lastSpecialIndex = -999;
            for (int i = 1; i <= 100; i++) {
                Tile tile = board.getTile(i);
                if (tile == null) {
                    throw new AssertionError("Tile at " + i + " must not be null");
                }

                if (tile instanceof LadderTile) {
                    LadderTile ladder = (LadderTile) tile;
                    int dest = ladder.getDestination();

                    // Verify spacing from last special tile (at least 2 safe tiles between)
                    if (i - lastSpecialIndex <= 2) {
                        throw new AssertionError("Ladder at " + i + " clustered too close to previous special tile at " + lastSpecialIndex);
                    }
                    lastSpecialIndex = i;

                    // Meaningful forward jump
                    int jump = dest - i;
                    if (jump < 6) {
                        throw new AssertionError("Ladder at " + i + " had trivial jump of " + jump + " to " + dest);
                    }
                    if (dest > 100) {
                        throw new AssertionError("Ladder destination overshot board: " + dest);
                    }

                    // Safe landing guarantee: landing MUST be plain NormalTile (not special & not POI landmark)
                    Tile destTile = board.getTile(dest);
                    if (!(destTile instanceof NormalTile)) {
                        throw new AssertionError("Ladder at " + i + " landed on non-normal tile: " + destTile.getClass().getSimpleName() + " at " + dest);
                    }
                    if (destTile.getLandmark() != TileLandmark.NONE) {
                        throw new AssertionError("Ladder at " + i + " landed on a POI landmark: " + destTile.getLandmark() + " at " + dest);
                    }
                } else if (tile instanceof SnakeTile) {
                    SnakeTile snake = (SnakeTile) tile;
                    int dest = snake.getDestination();

                    // Verify spacing from last special tile (at least 2 safe tiles between)
                    if (i - lastSpecialIndex <= 2) {
                        throw new AssertionError("Snake at " + i + " clustered too close to previous special tile at " + lastSpecialIndex);
                    }
                    lastSpecialIndex = i;

                    // Meaningful backward drop
                    int drop = i - dest;
                    if (drop < 6) {
                        throw new AssertionError("Snake at " + i + " had trivial drop of " + drop + " to " + dest);
                    }
                    if (dest < 1) {
                        throw new AssertionError("Snake destination undershot board: " + dest);
                    }

                    // Safe landing guarantee: landing MUST be plain NormalTile (not special & not POI landmark)
                    Tile destTile = board.getTile(dest);
                    if (!(destTile instanceof NormalTile)) {
                        throw new AssertionError("Snake at " + i + " dropped onto non-normal tile: " + destTile.getClass().getSimpleName() + " at " + dest);
                    }
                    if (destTile.getLandmark() != TileLandmark.NONE) {
                        throw new AssertionError("Snake at " + i + " dropped onto a POI landmark: " + destTile.getLandmark() + " at " + dest);
                    }
                }
            }
        }

        System.out.println(">>> SUCCESS! 1,000 Board Generation simulations passed with zero invariant violations.");
        System.out.println(String.format("    - Ladders Distribution: Avg Total=%.2f (Cap <= 10) | Act I=%.2f (Cap <= 5) | Act II=%.2f (Cap <= 3) | Act III=%.2f (Cap <= 2)",
            totalLaddersSum / 1000.0, act1LaddersSum / 1000.0, act2LaddersSum / 1000.0, act3LaddersSum / 1000.0));
        System.out.println(String.format("    - Snakes Escalation:   Avg Total=%.2f | Act I=%.2f | Act II=%.2f | Act III=%.2f",
            totalSnakesSum / 1000.0, act1SnakesSum / 1000.0, act2SnakesSum / 1000.0, act3SnakesSum / 1000.0));
        System.out.println("    - All milestones 1, 30, 70, 100 strictly safe");
        System.out.println("    - Minimum jump distance >= 6 strictly enforced");
        System.out.println("    - Anti-clustering (>= 2 safe tiles between specials) 100% verified");
        System.out.println("    - 100% of ladders and snakes land on safe Normal tiles (zero loops or chain traps)");
    }

    private static void assertNormal(Board board, int pos, String msg) {
        Tile t = board.getTile(pos);
        if (!(t instanceof NormalTile)) {
            throw new AssertionError(msg + " (was " + (t != null ? t.getClass().getSimpleName() : "null") + ")");
        }
    }
}
