package io.github.snakes_and_ladders_roll_of_fate.skills;

/**
 * Result data object containing outcome and narrative feedback from activating a ClassSkill.
 */
public class SkillResult {
    private final boolean success;
    private final String message;
    private final int bonusMovement;

    public SkillResult(boolean success, String message) {
        this(success, message, 0);
    }

    public SkillResult(boolean success, String message, int bonusMovement) {
        this.success = success;
        this.message = message;
        this.bonusMovement = bonusMovement;
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
}
