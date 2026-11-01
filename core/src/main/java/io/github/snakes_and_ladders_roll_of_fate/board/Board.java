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
        // Will implement later
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
