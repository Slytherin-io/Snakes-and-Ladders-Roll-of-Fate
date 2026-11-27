package io.github.snakes_and_ladders_roll_of_fate.skills;

/**
 * Result data object containing outcome and narrative feedback from activating a ClassSkill.
 */
public class SkillResult {
    private final boolean success;
    private final String message;
    private final int bonusMovement;
    private final boolean directMove;

    public SkillResult(boolean success, String message) {
        this(success, message, 0, false);
    }

    public SkillResult(boolean success, String message, int bonusMovement) {
        this(success, message, bonusMovement, false);
    }

    public SkillResult(boolean success, String message, int bonusMovement, boolean directMove) {
        this.success = success;
        this.message = message;
        this.bonusMovement = bonusMovement;
        this.directMove = directMove;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getBonusMovement() {
        return bonusMovement;
    }

    public boolean isDirectMove() {
        return directMove;
    }
}
