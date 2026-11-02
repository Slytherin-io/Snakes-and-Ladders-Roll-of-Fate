package io.github.snakes_and_ladders_roll_of_fate.board;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private List<Tile> tiles;
    private int boardSize;

    public Board(int boardSize) {
        this.boardSize = boardSize;
        this.tiles = new ArrayList<>();
    }

    public void generateBoard() {
        tiles.clear();
        for (int i = 1; i <= boardSize; i++) {
            tiles.add(new NormalTile(i));
        }
        // test snake and ladder for testing
        tiles.set(13, new SnakeTile(14, 4));   // Tile 14 slides to 4
        tiles.set(29, new SnakeTile(30, 18));   // Tile 30 slides to 18
        tiles.set(48, new SnakeTile(49, 26));   // Tile 49 slides to 26
        tiles.set(52, new SnakeTile(53, 10));   // Tile 53 slides to 10
        tiles.set(69, new SnakeTile(70, 56));   // Tile 70 slides to 56
        tiles.set(81, new SnakeTile(82, 54));   // Tile 82 slides to 54
        tiles.set(98, new SnakeTile(99, 69));   // Tile 99 slides to 69

        tiles.set(8, new LadderTile(9, 31));   // Tile 9 climbs to 31
        tiles.set(15, new LadderTile(16, 28));   // Tile 16 climbs to 28
        tiles.set(31, new LadderTile(32, 50));   // Tile 32 climbs to 50
        tiles.set(42, new LadderTile(43, 67));   // Tile 43 climbs to 67
        tiles.set(60, new LadderTile(61, 80));   // Tile 61 climbs to 80
        tiles.set(73, new LadderTile(74, 91));   // Tile 74 climbs to 91
        tiles.set(82, new LadderTile(83, 89));   // Tile 83 climbs to 89
    }

    public void displayBoard() {
        System.out.println("\n=== SNAKES AND LADDERS: ROLL OF FATE (TEST BOARD) ===");
        for (Tile tile : tiles) {
            System.out.print(String.format("%4d:%-7s|", tile.getPosition(), tile.getType()));

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

    // Getters and Setters
    public int getBoardSize() { return boardSize; }

    public List<Tile> getTiles() { return tiles; }


}
