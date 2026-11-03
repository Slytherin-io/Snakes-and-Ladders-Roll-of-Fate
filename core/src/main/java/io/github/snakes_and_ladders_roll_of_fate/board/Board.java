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
        this.tiles.clear(); // Clear list in case we are regenerating
        String previousType = null;

        for (int position = 1; position <= this.boardSize; position++) {
            // Pass the previous tile's type into the generator
            Tile newTile = Tile.createRandom(position, previousType);
            this.tiles.add(newTile);

            // Update previousType for the next loop iteration
            previousType = newTile.getType();
        }
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
