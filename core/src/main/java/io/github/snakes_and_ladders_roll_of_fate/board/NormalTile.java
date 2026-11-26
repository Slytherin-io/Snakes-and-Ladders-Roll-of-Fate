package io.github.snakes_and_ladders_roll_of_fate.board;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.systems.EventCategory;

public class NormalTile extends Tile {
    public NormalTile(int position) { super(position, "NORMAL", "Safe tile with no effect.", EventCategory.NORMAL); }

    @Override
    public void onLand(Player player) {
        System.out.println(dialogue.createNormalDialogue());
        player.gainExperience((int) (10 + getPosition() * 0.05)); // player gains 10 + 5% of tile position as exp
    }
}
