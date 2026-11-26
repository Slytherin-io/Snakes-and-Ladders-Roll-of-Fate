package io.github.snakes_and_ladders_roll_of_fate.systems;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.items.ItemType;
import java.util.ArrayList;
import java.util.List;

public class EventChoice {
    public enum ChoiceRequirementType {
        NONE,
        MIN_HEALTH,
        MIN_EXPERIENCE,
        CONSUME_ANY_ITEM,
        EQUIPMENT_DURABILITY,
        HAS_CURSE
    }

    @FunctionalInterface
    public interface ItemBarterHandler {
        Item chooseItemToTrade(Player player);
    }

    private static ItemBarterHandler itemBarterHandler = null;

    private String text;
    private int difficultyClass; // If 0, guaranteed outcome without roll
    private List<EventEffect> successEffects;
    private List<EventEffect> failureEffects;
    private ChoiceRequirementType requirementType = ChoiceRequirementType.NONE;
    private int requirementValue = 0;
    private ItemType requiredItemType = null;
    private boolean lastCheckSucceeded = true;
    private boolean skipsSpecialMove = false;
    private boolean triggersCombat = false;
    private static final Dice d20 = new Dice(20);

    // Guaranteed choice constructor
    public EventChoice(String text) {
        this(text, 0);
    }

    // Risky choice constructor requiring a D20 skill check
    public EventChoice(String text, int difficultyClass) {
        this.text = text;
        this.difficultyClass = difficultyClass;
        this.successEffects = new ArrayList<>();
        this.failureEffects = new ArrayList<>();
    }

    public static void setItemBarterHandler(ItemBarterHandler handler) {
        itemBarterHandler = handler;
    }

    public static ItemBarterHandler getItemBarterHandler() {
        return itemBarterHandler;
    }

    // Factory method: Blood Rite (Cost: HP)
    public static EventChoice createBloodCostChoice(String text, int hpCost, Item rewardItem) {
        EventChoice choice = new EventChoice(text);
        choice.requirementType = ChoiceRequirementType.MIN_HEALTH;
        choice.requirementValue = hpCost;
        if (rewardItem != null) {
            choice.onSuccess(rewardItem);
        }
        return choice;
    }

    // Factory method: Knowledge Tribute (Cost: EXP)
    public static EventChoice createExpCostChoice(String text, int expCost, Item rewardItem) {
        EventChoice choice = new EventChoice(text);
        choice.requirementType = ChoiceRequirementType.MIN_EXPERIENCE;
        choice.requirementValue = expCost;
        if (rewardItem != null) {
            choice.onSuccess(rewardItem);
        }
        return choice;
    }

    // Factory method: Barter (Cost: Trade 1 item)
    public static EventChoice createBarterChoice(String text, Item rewardItem) {
        EventChoice choice = new EventChoice(text);
        choice.requirementType = ChoiceRequirementType.CONSUME_ANY_ITEM;
        if (rewardItem != null) {
            choice.onSuccess(rewardItem);
        }
        return choice;
    }

    // Factory method: Equipment-Assisted Choice (Expends single-use relic to deflect hazard safely)
    public static EventChoice createEquipmentAssistedChoice(String text, ItemType requiredType) {
        EventChoice choice = new EventChoice(text);
        choice.requirementType = ChoiceRequirementType.EQUIPMENT_DURABILITY;
        choice.requiredItemType = requiredType;
        return choice;
    }

    // Factory method: Cleanse Curse Choice (Only visible/available when player carries a Curse)
    public static EventChoice createCleanseCurseChoice(String text) {
        EventChoice choice = new EventChoice(text);
        choice.requirementType = ChoiceRequirementType.HAS_CURSE;
        return choice;
    }

    // Fluent helper for custom EventEffect instances (e.g., curse drops)
    public EventChoice onSuccess(EventEffect effect) {
        if (effect != null) {
            this.successEffects.add(effect);
        }
        return this;
    }

    public EventChoice onFailure(EventEffect effect) {
        if (effect != null) {
            this.failureEffects.add(effect);
        }
        return this;
    }

    // Fluent helper for item rewards
    public EventChoice onSuccess(Item item) {
        this.successEffects.add(new EventEffect(item));
        return this;
    }

    public EventChoice onFailure(Item item) {
        this.failureEffects.add(new EventEffect(item));
        return this;
    }

    public EventChoice onSuccess(EventEffectType type) {
        if (type == EventEffectType.ITEM) {
            this.successEffects.add(EventEffect.createRandomLootEffect());
        } else {
            this.successEffects.add(new EventEffect(type, 1));
        }
        return this;
    }

    public EventChoice onFailure(EventEffectType type) {
        if (type == EventEffectType.ITEM) {
            this.failureEffects.add(EventEffect.createRandomLootEffect());
        } else {
            this.failureEffects.add(new EventEffect(type, 1));
        }
        return this;
    }

    // Fluent helper for guaranteed or successful effects (range)
    public EventChoice onSuccess(EventEffectType type, int minValue, int maxValue) {
        if (type == EventEffectType.ITEM) {
            this.successEffects.add(EventEffect.createRandomLootEffect());
        } else {
            this.successEffects.add(new EventEffect(type, minValue, maxValue));
        }
        return this;
    }

    // Fluent helper for guaranteed or successful effects (fixed)
    public EventChoice onSuccess(EventEffectType type, int value) {
        return onSuccess(type, value, value);
    }

    // Fluent helper for failure effects (range)
    public EventChoice onFailure(EventEffectType type, int minValue, int maxValue) {
        if (type == EventEffectType.ITEM) {
            this.failureEffects.add(EventEffect.createRandomLootEffect());
        } else {
            this.failureEffects.add(new EventEffect(type, minValue, maxValue));
        }
        return this;
    }

    // Fluent helper for failure effects (fixed)
    public EventChoice onFailure(EventEffectType type, int value) {
        return onFailure(type, value, value);
    }

    // Backwards-compatible alias for guaranteed effects
    public EventChoice withEffect(EventEffectType type, int minValue, int maxValue) {
        return onSuccess(type, minValue, maxValue);
    }

    public EventChoice withEffect(EventEffectType type, int value) {
        return onSuccess(type, value, value);
    }

    public EventChoice withEffect(EventEffectType type) {
        return onSuccess(type);
    }

    public EventChoice withEffect(Item item) {
        return onSuccess(item);
    }

    // Checks if the player meets all conditions to select this choice
    public boolean isAvailable(Player player) {
        if (player == null) return false;
        switch (requirementType) {
            case MIN_HEALTH:
                return player.getHealth() > requirementValue;
            case MIN_EXPERIENCE:
                return player.getTotalExperience() >= requirementValue;
            case CONSUME_ANY_ITEM:
                return player.getInventory() != null && !player.getInventory().isEmpty();
            case EQUIPMENT_DURABILITY:
                return player.hasItemType(requiredItemType != null ? requiredItemType : ItemType.EQUIPMENT);
            case HAS_CURSE:
                return player.hasCurse();
            case NONE:
            default:
                return true;
        }
    }

    public String getRequirementDescription() {
        switch (requirementType) {
            case MIN_HEALTH:
                return "(Requires more than " + requirementValue + " HP)";
            case MIN_EXPERIENCE:
                return "(Requires " + requirementValue + " EXP)";
            case CONSUME_ANY_ITEM:
                return "(Requires an item in inventory to barter)";
            case EQUIPMENT_DURABILITY:
                return "(Requires a 1-use relic to deflect)";
            case HAS_CURSE:
                return "(Requires a Curse in satchel)";
            case NONE:
            default:
                return "";
        }
    }

    public static class EventResult {
        public boolean checkSucceeded = true;
        public int d20Roll = 0;
        public int difficultyClass = 0;
        public List<String> consequences = new ArrayList<>();
        public boolean shieldDeflected = false;
        public Item brokenItem = null;
        public Item tradedItem = null;
    }

    // Executes the choice, checking requirements and rolling a D20 if a DC is present
    public void execute(Player player) {
        executeWithResult(player);
    }

    public EventResult executeWithResult(Player player) {
        EventResult result = new EventResult();
        if (!isAvailable(player)) {
            System.out.println("  [UNAVAILABLE] You do not meet the requirement: " + getRequirementDescription());
            result.checkSucceeded = false;
            result.consequences.add("Unavailable: " + getRequirementDescription());
            return result;
        }

        // Apply resource requirement / payment
        switch (requirementType) {
            case MIN_HEALTH:
                player.setHealth(player.getHealth() - requirementValue);
                System.out.printf("  [BLOOD SACRIFICE] Paid %d HP (Remaining HP: %d)%n", requirementValue, player.getHealth());
                result.consequences.add(String.format("Sacrifice: -%d HP", requirementValue));
                break;
            case MIN_EXPERIENCE:
                player.spendTotalExperience(requirementValue);
                System.out.printf("  [KNOWLEDGE TRIBUTE] Paid %d EXP (Remaining Total EXP: %d)%n", requirementValue, player.getTotalExperience());
                result.consequences.add(String.format("Tribute: -%d EXP", requirementValue));
                break;
            case CONSUME_ANY_ITEM:
                Item traded = null;
                if (itemBarterHandler != null) {
                    traded = itemBarterHandler.chooseItemToTrade(player);
                } else if (player.getInventory() != null && !player.getInventory().isEmpty()) {
                    traded = player.getInventory().removeItem(0);
                }
                if (traded != null) {
                    result.tradedItem = traded;
                    System.out.println("  [BARTER] Traded away " + traded.getName() + " in exchange.");
                    result.consequences.add("Traded away: " + traded.getName());
                }
                break;
            case EQUIPMENT_DURABILITY:
                Item eq = player.findItemByType(requiredItemType != null ? requiredItemType : ItemType.EQUIPMENT);
                if (eq != null) {
                    eq.reduceDurability();
                    result.shieldDeflected = true;
                    System.out.printf("  [DEFLECTED] %s absorbed the blow! Remaining uses: %d/%d%n",
                        eq.getName(), eq.getDurability(), eq.getMaxDurability());
                    result.consequences.add(String.format("Deflected: %s absorbed the impact!", eq.getName()));
                    if (eq.isBroken()) {
                        result.brokenItem = eq;
                        System.out.println("  [EXPENDED] " + eq.getName() + " shattered from the impact!");
                        result.consequences.add("Expended: " + eq.getName() + " broke after protecting you!");
                        player.getInventory().removeItem(eq);
                    }
                }
                break;
            case HAS_CURSE:
                int cleansed = player.cleanseCurses();
                System.out.println("  [PURIFIED] Cleansed " + cleansed + " curse(s) from satchel!");
                result.consequences.add("Purified: Cleansed " + cleansed + " cursed burden(s) from your satchel!");
                break;
            case NONE:
            default:
                break;
        }

        if (difficultyClass > 0) {
            int roll = d20.roll();
            result.d20Roll = roll;
            result.difficultyClass = difficultyClass;
            System.out.printf("  [D20 ROLL] Rolled %d against DC %d...%n", roll, difficultyClass);

            if (roll >= difficultyClass) {
                lastCheckSucceeded = true;
                result.checkSucceeded = true;
                System.out.println("  [SUCCESS] You passed the check!");
                applyEffects(player, successEffects, result.consequences);
            } else {
                lastCheckSucceeded = false;
                result.checkSucceeded = false;
                System.out.println("  [FAILURE] You failed the check!");
                applyEffects(player, failureEffects, result.consequences);
            }
        } else {
            // Guaranteed choice
            lastCheckSucceeded = true;
            result.checkSucceeded = true;
            applyEffects(player, successEffects, result.consequences);
        }

        if (result.consequences.isEmpty()) {
            result.consequences.add("You safely proceed on your path.");
        }

        return result;
    }

    private void applyEffects(Player player, List<EventEffect> effectsList, List<String> logMessages) {
        if (effectsList.isEmpty()) {
            System.out.println("  Nothing happens. You safely proceed.");
            return;
        }
        for (EventEffect effect : effectsList) {
            effect.apply(player, logMessages);
        }
    }

    private void applyEffects(Player player, List<EventEffect> effectsList) {
        applyEffects(player, effectsList, null);
    }

    // Formats choice label purely as narrative text, keeping difficulty and consequences hidden
    public String getText() {
        return text;
    }

    public int getDifficultyClass() { return difficultyClass; }
    public List<EventEffect> getSuccessEffects() { return successEffects; }
    public List<EventEffect> getFailureEffects() { return failureEffects; }
    public ChoiceRequirementType getRequirementType() { return requirementType; }
    public int getRequirementValue() { return requirementValue; }
    public ItemType getRequiredItemType() { return requiredItemType; }
    public boolean didLastCheckSucceed() { return lastCheckSucceeded; }

    public EventChoice skipsSpecialMove() {
        this.skipsSpecialMove = true;
        return this;
    }

    public boolean isSkipsSpecialMove() { return skipsSpecialMove; }

    public EventChoice triggersCombat() {
        this.triggersCombat = true;
        return this;
    }

    public boolean isTriggersCombat() { return triggersCombat; }
}
