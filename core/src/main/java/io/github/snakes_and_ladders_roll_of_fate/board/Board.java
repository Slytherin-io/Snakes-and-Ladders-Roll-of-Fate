package io.github.snakes_and_ladders_roll_of_fate.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Manages the game board consisting of connected tiles.
 * Uses a robust Two-Pass Generation Algorithm:
 * - Pass 1 (Type Layout): Enforces Act-based distribution, anti-clustering (at least 2 safe tiles between specials),
 *   and guaranteed milestone safe zones (Tile 1 Start, Tile 30 Act I Sanctuary, Tile 70 Act II Gate, Tile 100 Throne).
 * - Pass 2 (Safe Destination Resolution): Guarantees ladders and snakes jump meaningful distances (>= 6 tiles),
 *   never land on other hazards/ladders (zero infinite loops or chained traps), and land only on safe Normal tiles.
 */
public class Board {
    private List<Tile> tiles;
    private int boardSize;
    private Random random;

    private enum PlannedType {
        NORMAL,
        LADDER,
        SNAKE
    }

    public Board(int boardSize) {
        this(boardSize, new Random());
    }

    public Board(int boardSize, Random random) {
        this.boardSize = boardSize;
        this.tiles = new ArrayList<>();
        this.random = (random != null) ? random : new Random();
    }

    /**
     * Executes the Two-Pass Board Generation algorithm.
     */
    public void generateBoard() {
        this.tiles.clear();

        PlannedType[] planned = new PlannedType[boardSize + 1];
        int[] destinations = new int[boardSize + 1];

        // Default all to NORMAL
        for (int i = 1; i <= boardSize; i++) {
            planned[i] = PlannedType.NORMAL;
            destinations[i] = i;
        }

        // =========================================================================
        // PASS 1: Layout & Type Assignment with Anti-Clustering
        // =========================================================================
        // Milestone safe zones: Tile 1 (Start), 2-3 (runway), 30 (Act I Shrine), 70 (Act II Shrine),
        // 98-99 (final approach), 100 (Throne of Fate)
        Set<Integer> protectedSafeTiles = new HashSet<>();
        protectedSafeTiles.add(1);
        protectedSafeTiles.add(2);
        protectedSafeTiles.add(3);
        protectedSafeTiles.add(30);
        protectedSafeTiles.add(70);
        protectedSafeTiles.add(boardSize - 2);
        protectedSafeTiles.add(boardSize - 1);
        protectedSafeTiles.add(boardSize);

        int lastSpecialPos = -999;

        for (int pos = 4; pos <= boardSize - 3; pos++) {
            if (protectedSafeTiles.contains(pos)) {
                continue;
            }

            // Anti-clustering: enforce at least 2 safe tiles between any special tiles
            if (pos - lastSpecialPos <= 2) {
                continue;
            }

            // Act-based probability weighting
            int roll = random.nextInt(100);
            PlannedType chosen = PlannedType.NORMAL;

            if (pos <= 30) {
                // Act I: 20% Ladder, 10% Snake, 70% Normal (High climbing momentum)
                if (roll < 20) chosen = PlannedType.LADDER;
                else if (roll < 30) chosen = PlannedType.SNAKE;
            } else if (pos <= 70) {
                // Act II: 14% Ladder, 18% Snake, 68% Normal (Balanced adventure tension)
                if (roll < 14) chosen = PlannedType.LADDER;
                else if (roll < 32) chosen = PlannedType.SNAKE;
            } else {
                // Act III: 8% Ladder, 24% Snake, 68% Normal (High stakes perilous endgame)
                if (roll < 8) chosen = PlannedType.LADDER;
                else if (roll < 32) chosen = PlannedType.SNAKE;
            }

            // Validate that special tiles have room for minimum 6-tile jumps
            if (chosen == PlannedType.LADDER) {
                if (pos + 6 <= boardSize - 1) {
                    planned[pos] = PlannedType.LADDER;
                    lastSpecialPos = pos;
                }
            } else if (chosen == PlannedType.SNAKE) {
                if (pos - 6 >= 1) {
                    planned[pos] = PlannedType.SNAKE;
                    lastSpecialPos = pos;
                }
            }
        }

        // =========================================================================
        // PASS 2: Safe Destination Resolution & Loop Elimination
        // =========================================================================
        Set<Integer> reservedDestinations = new HashSet<>();

        // 1. Resolve Ladders (must jump >= 6 tiles forward to a safe Normal tile)
        for (int pos = 1; pos <= boardSize; pos++) {
            if (planned[pos] != PlannedType.LADDER) continue;

            int minDest = pos + 6;
            int maxDest = Math.min(boardSize - 1, pos + 18); // Don't skip directly over the Throne

            List<Integer> candidates = new ArrayList<>();
            for (int candidate = minDest; candidate <= maxDest; candidate++) {
                if (planned[candidate] == PlannedType.NORMAL && !reservedDestinations.contains(candidate)) {
                    candidates.add(candidate);
                }
            }

            if (!candidates.isEmpty()) {
                Collections.shuffle(candidates, random);
                int chosenDest = candidates.get(0);
                destinations[pos] = chosenDest;
                reservedDestinations.add(chosenDest);
            } else {
                // If no safe forward landing exists, revert to a normal tile
                planned[pos] = PlannedType.NORMAL;
            }
        }

        // 2. Resolve Snakes (must drop >= 6 tiles backward to a safe Normal tile)
        for (int pos = 1; pos <= boardSize; pos++) {
            if (planned[pos] != PlannedType.SNAKE) continue;

            int minDest = Math.max(1, pos - 20);
            int maxDest = pos - 6;

            List<Integer> candidates = new ArrayList<>();
            for (int candidate = minDest; candidate <= maxDest; candidate++) {
                if (planned[candidate] == PlannedType.NORMAL && !reservedDestinations.contains(candidate)) {
                    candidates.add(candidate);
                }
            }

            if (!candidates.isEmpty()) {
                Collections.shuffle(candidates, random);
                int chosenDest = candidates.get(0);
                destinations[pos] = chosenDest;
                reservedDestinations.add(chosenDest);
            } else {
                // If no safe backward landing exists, revert to a normal tile
                planned[pos] = PlannedType.NORMAL;
            }
        }

        // =========================================================================
        // PASS 3: Tile Instantiation
        // =========================================================================
        for (int pos = 1; pos <= boardSize; pos++) {
            switch (planned[pos]) {
                case LADDER:
                    this.tiles.add(new LadderTile(pos, destinations[pos]));
                    break;
                case SNAKE:
                    this.tiles.add(new SnakeTile(pos, destinations[pos]));
                    break;
                case NORMAL:
                default:
                    this.tiles.add(new NormalTile(pos));
                    break;
            }
        }

        // =========================================================================
        // PASS 4: Fog of War / Initial Discovered Landmarks
        // =========================================================================
        // Safe starting clearing and key monumental landmarks start revealed
        if (this.tiles.size() >= 3) {
            this.tiles.get(0).setRevealed(true); // Tile 1 (Start Camp)
            this.tiles.get(1).setRevealed(true); // Tile 2 (Runway)
            this.tiles.get(2).setRevealed(true); // Tile 3 (Runway)
        }
        if (this.tiles.size() >= 30) {
            this.tiles.get(29).setRevealed(true); // Tile 30 (Sanctuary landmark)
        }
        if (this.tiles.size() >= 70) {
            this.tiles.get(69).setRevealed(true); // Tile 70 (Abyss Gate landmark)
        }
        if (this.tiles.size() >= boardSize) {
            this.tiles.get(boardSize - 1).setRevealed(true); // Tile 100 (Throne of Fate)
        }
    }

    /**
     * Reveals a range of tiles (used by scouting items / divination perks).
     * @return the number of previously unrevealed tiles that were uncovered.
     */
    public int revealRange(int fromTile, int toTile) {
        int count = 0;
        int start = Math.max(1, fromTile);
        int end = Math.min(boardSize, toTile);
        for (int i = start; i <= end; i++) {
            Tile tile = getTile(i);
            if (tile != null && !tile.isRevealed()) {
                tile.setRevealed(true);
                count++;
            }
        }
        return count;
    }

    public void displayBoard() {
        System.out.println("\n=== SNAKES AND LADDERS: ROLL OF FATE (ADVENTURE BOARD) ===");
        for (Tile tile : tiles) {
            String desc = tile.getType();
            if (tile instanceof LadderTile) {
                desc += "(->" + ((LadderTile) tile).getDestination() + ")";
            } else if (tile instanceof SnakeTile) {
                desc += "(->" + ((SnakeTile) tile).getDestination() + ")";
            }
            System.out.print(String.format("%3d:%-10s|", tile.getPosition(), desc));

            if (tile.getPosition() % 10 == 0) {
                System.out.println();
            }
        }
        System.out.println();
    }

    public Tile getTile(int position) {
        if (position >= 1 && position <= tiles.size()) {
            return tiles.get(position - 1);
        }
        return null;
    }

    public int getLadderCount() {
        int count = 0;
        for (Tile t : tiles) {
            if (t instanceof LadderTile) count++;
        }
        return count;
    }

    public int getSnakeCount() {
        int count = 0;
        for (Tile t : tiles) {
            if (t instanceof SnakeTile) count++;
        }
        return count;
    }

    public int getNormalCount() {
        int count = 0;
        for (Tile t : tiles) {
            if (t instanceof NormalTile) count++;
        }
        return count;
    }

    // Getters and Setters
    public int getBoardSize() { return boardSize; }

    public List<Tile> getTiles() { return tiles; }
}
