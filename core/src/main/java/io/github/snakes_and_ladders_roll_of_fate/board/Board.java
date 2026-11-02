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
        tiles.set(8, new LadderTile(9, 31));   // Tile 9 climbs to 31
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
