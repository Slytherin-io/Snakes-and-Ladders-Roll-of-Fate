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
    private BoardModifier activeModifier = BoardModifier.BALANCED_ASCENT;

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
     * Executes the Two-Pass Board Generation algorithm with a dynamically rolled Run Fate Modifier.
     */
    public void generateBoard() {
        BoardModifier[] modifiers = BoardModifier.values();
        BoardModifier rolledModifier = modifiers[random.nextInt(modifiers.length)];
        generateBoard(rolledModifier);
    }

    /**
     * Executes the Multi-Pass Board Generation algorithm using a specific Run Fate Modifier:
     * - Pass 1: Layout & Act-weighted Ladders/Snakes modified by activeModifier (with anti-clustering)
     * - Pass 2: Safe Destination Resolution & Loop Elimination
     * - Pass 2.5: Visible Point of Interest (Shrine, Merchant, Treasure, Crossroads) Placement
     * - Pass 3: Tile Instantiation
     * - Pass 4: Fog of War / Initial Discovered Landmarks
     */
    public void generateBoard(BoardModifier modifier) {
        this.tiles.clear();
        this.activeModifier = (modifier != null) ? modifier : BoardModifier.BALANCED_ASCENT;

        PlannedType[] planned = new PlannedType[boardSize + 1];
        TileLandmark[] landmarks = new TileLandmark[boardSize + 1];
        int[] destinations = new int[boardSize + 1];

        // Default all to NORMAL with no special landmark
        for (int i = 1; i <= boardSize; i++) {
            planned[i] = PlannedType.NORMAL;
            landmarks[i] = TileLandmark.NONE;
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
        int totalLadders = 0;
        int act1Ladders = 0;
        int act2Ladders = 0;
        int act3Ladders = 0;

        for (int pos = 4; pos <= boardSize - 3; pos++) {
            if (protectedSafeTiles.contains(pos)) {
                continue;
            }

            // Anti-clustering: enforce at least 2 safe tiles between any special tiles
            if (pos - lastSpecialPos <= 2) {
                continue;
            }

            // Act-based probability weighting with Option A Escalating Danger:
            // - Act I: 22% ladder, 8% snake (max 5 ladders)
            // - Act II: 12% ladder, 22% snake (max 3 ladders)
            // - Act III: 5% ladder, 38% snake (max 2 ladders, purely chance-based up to cap)
            // Global cap: at most 10 ladders across the entire board.
            int act = (pos <= 30) ? 1 : ((pos <= 70) ? 2 : 3);
            int roll = random.nextInt(100);
            PlannedType chosen = PlannedType.NORMAL;

            int baseLadderPct;
            int baseSnakePct;
            if (act == 1) {
                baseLadderPct = (act1Ladders < 5 && totalLadders < 10) ? 22 : 0;
                baseSnakePct = 8;
            } else if (act == 2) {
                baseLadderPct = (act2Ladders < 3 && totalLadders < 10) ? 12 : 0;
                baseSnakePct = 22;
            } else {
                baseLadderPct = (act3Ladders < 2 && totalLadders < 10) ? 5 : 0;
                baseSnakePct = 38;
            }

            int ladderPct = (baseLadderPct > 0)
                ? Math.max(1, baseLadderPct + activeModifier.getLadderChanceDelta())
                : 0;
            int snakePct = Math.max(5, baseSnakePct + activeModifier.getSnakeChanceDelta());

            if (roll < ladderPct) {
                chosen = PlannedType.LADDER;
            } else if (roll < ladderPct + snakePct) {
                chosen = PlannedType.SNAKE;
            }

            // Validate that special tiles have room for minimum 6-tile jumps
            if (chosen == PlannedType.LADDER) {
                if (pos + activeModifier.getMinLadderJump() <= boardSize - 1) {
                    planned[pos] = PlannedType.LADDER;
                    lastSpecialPos = pos;
                    totalLadders++;
                    if (act == 1) act1Ladders++;
                    else if (act == 2) act2Ladders++;
                    else act3Ladders++;
                }
            } else if (chosen == PlannedType.SNAKE) {
                if (pos - activeModifier.getMinSnakeDrop() >= 1) {
                    planned[pos] = PlannedType.SNAKE;
                    lastSpecialPos = pos;
                }
            }
        }

        // =========================================================================
        // PASS 2: Safe Destination Resolution & Loop Elimination
        // =========================================================================
        Set<Integer> reservedDestinations = new HashSet<>();

        // 1. Resolve Ladders (must jump >= minLadderJump forward to a safe Normal tile)
        for (int pos = 1; pos <= boardSize; pos++) {
            if (planned[pos] != PlannedType.LADDER) continue;

            int minDest = pos + activeModifier.getMinLadderJump();
            int maxDest = Math.min(boardSize - 1, pos + activeModifier.getMaxLadderJump());

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
                totalLadders--;
                if (pos <= 30) act1Ladders--;
                else if (pos <= 70) act2Ladders--;
                else act3Ladders--;
            }
        }

        // 2. Resolve Snakes (must drop >= minSnakeDrop backward to a safe Normal tile)
        for (int pos = 1; pos <= boardSize; pos++) {
            if (planned[pos] != PlannedType.SNAKE) continue;

            int minDest = Math.max(1, pos - activeModifier.getMaxSnakeDrop());
            int maxDest = pos - activeModifier.getMinSnakeDrop();

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
        // PASS 2.5: Visible Points of Interest (POIs) & Crossroads Placement
        // =========================================================================
        Set<Integer> poiPositions = new HashSet<>();

        // Place 1 Crossroads dilemma per Act (strategic decision nodes)
        placePoiInBand(planned, landmarks, protectedSafeTiles, reservedDestinations, poiPositions, 12, 24, TileLandmark.CROSSROADS, 1);
        placePoiInBand(planned, landmarks, protectedSafeTiles, reservedDestinations, poiPositions, 42, 58, TileLandmark.CROSSROADS, 1);
        placePoiInBand(planned, landmarks, protectedSafeTiles, reservedDestinations, poiPositions, 76, 89, TileLandmark.CROSSROADS, 1);

        // Place extra Crossroads if Shrouded Labyrinth modifier is active
        if (activeModifier == BoardModifier.SHROUDED_LABYRINTH) {
            placePoiInBand(planned, landmarks, protectedSafeTiles, reservedDestinations, poiPositions, 25, 41, TileLandmark.CROSSROADS, 1);
            placePoiInBand(planned, landmarks, protectedSafeTiles, reservedDestinations, poiPositions, 59, 75, TileLandmark.CROSSROADS, 1);
        }

        // Distribute Shrines, Merchants, and Treasure Caches across the full board
        placePoiDistributed(planned, landmarks, protectedSafeTiles, reservedDestinations, poiPositions, TileLandmark.SHRINE, activeModifier.getTargetShrines());
        placePoiDistributed(planned, landmarks, protectedSafeTiles, reservedDestinations, poiPositions, TileLandmark.MERCHANT, activeModifier.getTargetMerchants());
        placePoiDistributed(planned, landmarks, protectedSafeTiles, reservedDestinations, poiPositions, TileLandmark.TREASURE, activeModifier.getTargetTreasures());

        // =========================================================================
        // PASS 3: Tile Instantiation
        // =========================================================================
        for (int pos = 1; pos <= boardSize; pos++) {
            Tile created;
            switch (planned[pos]) {
                case LADDER:
                    created = new LadderTile(pos, destinations[pos]);
                    break;
                case SNAKE:
                    created = new SnakeTile(pos, destinations[pos]);
                    break;
                case NORMAL:
                default:
                    created = new NormalTile(pos);
                    created.setLandmark(landmarks[pos]);
                    break;
            }
            this.tiles.add(created);
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

    /**
     * Places up to `targetCount` landmarks inside [minPos, maxPos] on clean Normal tiles.
     */
    private void placePoiInBand(PlannedType[] planned, TileLandmark[] landmarks,
                                Set<Integer> protectedSafeTiles, Set<Integer> reservedDestinations,
                                Set<Integer> poiPositions, int minPos, int maxPos,
                                TileLandmark landmark, int targetCount) {
        List<Integer> candidates = new ArrayList<>();
        for (int pos = Math.max(4, minPos); pos <= Math.min(boardSize - 3, maxPos); pos++) {
            if (isPoiCandidate(planned, landmarks, protectedSafeTiles, reservedDestinations, poiPositions, pos, 3)) {
                candidates.add(pos);
            }
        }
        Collections.shuffle(candidates, random);
        int placed = 0;
        for (int pos : candidates) {
            if (placed >= targetCount) break;
            if (!isNearExistingPoi(poiPositions, pos, 3)) {
                landmarks[pos] = landmark;
                poiPositions.add(pos);
                placed++;
            }
        }
    }

    /**
     * Distributes landmarks across the entire board evenly across Acts while maintaining spacing.
     */
    private void placePoiDistributed(PlannedType[] planned, TileLandmark[] landmarks,
                                     Set<Integer> protectedSafeTiles, Set<Integer> reservedDestinations,
                                     Set<Integer> poiPositions, TileLandmark landmark, int targetCount) {
        if (targetCount <= 0) return;
        List<Integer> candidates = new ArrayList<>();
        for (int pos = 4; pos <= boardSize - 3; pos++) {
            if (isPoiCandidate(planned, landmarks, protectedSafeTiles, reservedDestinations, poiPositions, pos, 3)) {
                candidates.add(pos);
            }
        }
        Collections.shuffle(candidates, random);
        int placed = 0;
        for (int pos : candidates) {
            if (placed >= targetCount) break;
            if (!isNearExistingPoi(poiPositions, pos, 3)) {
                landmarks[pos] = landmark;
                poiPositions.add(pos);
                placed++;
            }
        }
    }

    private boolean isPoiCandidate(PlannedType[] planned, TileLandmark[] landmarks,
                                   Set<Integer> protectedSafeTiles, Set<Integer> reservedDestinations,
                                   Set<Integer> poiPositions, int pos, int minSpacing) {
        if (planned[pos] != PlannedType.NORMAL) return false;
        if (landmarks[pos] != TileLandmark.NONE) return false;
        if (protectedSafeTiles.contains(pos)) return false;
        if (reservedDestinations.contains(pos)) return false;
        return !isNearExistingPoi(poiPositions, pos, minSpacing);
    }

    private boolean isNearExistingPoi(Set<Integer> poiPositions, int pos, int minSpacing) {
        for (int existing : poiPositions) {
            if (Math.abs(existing - pos) < minSpacing) {
                return true;
            }
        }
        return false;
    }

    public void displayBoard() {
        System.out.println("\n=== SNAKES AND LADDERS: ROLL OF FATE (ADVENTURE BOARD) ===");
        System.out.println("  [FATE MODIFIER] " + activeModifier.getDisplayName() + " - " + activeModifier.getDescription());
        for (Tile tile : tiles) {
            String desc = tile.getType();
            if (tile instanceof LadderTile) {
                desc += "(->" + ((LadderTile) tile).getDestination() + ")";
            } else if (tile instanceof SnakeTile) {
                desc += "(->" + ((SnakeTile) tile).getDestination() + ")";
            } else if (tile.getLandmark() != TileLandmark.NONE) {
                desc = tile.getLandmark().name();
            }
            System.out.print(String.format("%3d:%-11s|", tile.getPosition(), desc));

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

    public int getLaddersInAct(int act) {
        int start = (act == 1) ? 1 : ((act == 2) ? 31 : 71);
        int end = (act == 1) ? 30 : ((act == 2) ? 70 : boardSize);
        int count = 0;
        for (int i = start; i <= end && i <= tiles.size(); i++) {
            if (tiles.get(i - 1) instanceof LadderTile) count++;
        }
        return count;
    }

    public int getSnakesInAct(int act) {
        int start = (act == 1) ? 1 : ((act == 2) ? 31 : 71);
        int end = (act == 1) ? 30 : ((act == 2) ? 70 : boardSize);
        int count = 0;
        for (int i = start; i <= end && i <= tiles.size(); i++) {
            if (tiles.get(i - 1) instanceof SnakeTile) count++;
        }
        return count;
    }

    // Getters and Setters
    public int getBoardSize() { return boardSize; }

    public List<Tile> getTiles() { return tiles; }

    public BoardModifier getActiveModifier() { return activeModifier; }

    public int getPoiCount(TileLandmark landmark) {
        int count = 0;
        for (Tile t : tiles) {
            if (t.getLandmark() == landmark) count++;
        }
        return count;
    }
}
