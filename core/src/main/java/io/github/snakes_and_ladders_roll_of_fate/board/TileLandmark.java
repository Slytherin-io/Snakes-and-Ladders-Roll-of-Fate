package io.github.snakes_and_ladders_roll_of_fate.board;

/**
 * Visible Points of Interest (POIs) stamped onto safe Normal tiles during board generation.
 * Unlike hidden ambient encounters, revealed landmarks are visible on the board so players
 * can plan dice rolls, scouting, and movement items tactically.
 */
public enum TileLandmark {
    NONE,
    SHRINE,
    MERCHANT,
    TREASURE,
    CROSSROADS
}
