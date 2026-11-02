package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

public class NormalTile extends Tile {
    public NormalTile(int position) { super(position, "NORMAL", "Safe tile with no effect."); }

    @Override
    public void onLand(Player player) {
        // Safe tile, no changes to player
    }
}
