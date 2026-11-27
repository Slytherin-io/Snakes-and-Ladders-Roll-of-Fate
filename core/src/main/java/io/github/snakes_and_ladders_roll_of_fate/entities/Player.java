package io.github.snakes_and_ladders_roll_of_fate.entities;

import io.github.snakes_and_ladders_roll_of_fate.items.Inventory;
import io.github.snakes_and_ladders_roll_of_fate.items.Item;
import io.github.snakes_and_ladders_roll_of_fate.items.ItemType;
import io.github.snakes_and_ladders_roll_of_fate.skills.ClassSkill;
import io.github.snakes_and_ladders_roll_of_fate.skills.SkillResult;

public class Player extends Entity {
    private String name;
    private Inventory inventory;
    private int level;
    private int expThresh;
    private int experience;
    private int totalExperience;
    private int tempAttack = 0;
    private int tempDefense = 0;
    private int turnsSinceLastEvent = 99; // Starts ready for encounter
    private CharacterClass characterClass;
    private int maxHealthBonus = 0;

    // Polymorphic Class Skill (Starts inactive on full cooldown)
    private ClassSkill classSkill;
    private int skillCooldown = 4;

    // Active Board Utility Modifiers (Single-Use Item Effects)
    private boolean pendingDiceChoice = false;
    private int maxDiceRollCap = 0; // 0 = uncapped, >0 = next roll capped to max
    private boolean hasSnakeWard = false;

    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.experience = 0;
        this.totalExperience = 0;
        this.inventory = new Inventory(4);
        this.expThresh = 100;
    }

    public Player(String name, CharacterClass characterClass) {
        this(name);
        applyCharacterClass(characterClass);
    }

    public void applyCharacterClass(CharacterClass characterClass) {
        this.characterClass = characterClass;
        this.maxHealthBonus = 0;
        if (characterClass != null) {
            setEntityClass(characterClass.getName());
            setHealth(characterClass.getBaseHealth());
            setAttack(characterClass.getBaseAttack());
            setDefense(characterClass.getBaseDefense());
            this.classSkill = characterClass.createSkill();
            // All players start with their skill inactive on full 4-turn cooldown
            this.skillCooldown = (this.classSkill != null) ? this.classSkill.getMaxCooldown() : 4;
        }
    }

    public CharacterClass getCharacterClass() {
        return characterClass;
    }

    public int getMaxHealth() {
        int base = characterClass != null ? characterClass.getBaseHealth() : 100;
        return Math.max(20, base + maxHealthBonus);
    }

    public void modifyMaxHealth(int delta) {
        this.maxHealthBonus += delta;
        int newMax = getMaxHealth();
        if (delta > 0) {
            setHealth(getHealth() + delta);
        } else if (getHealth() > newMax) {
            setHealth(newMax);
        }
    }

    @Override
    public void setHealth(int health) {
        super.setHealth(Math.min(getMaxHealth(), Math.max(0, health)));
    }

    public boolean useItem(int slotIndex) {
        if (inventory == null) return false;
        return inventory.useItem(slotIndex, this);
    }

    public void useItem(Item item) {
        if (item != null) {
            item.use(this);
        }
    }

    public boolean addItem(Item item) {
        if (inventory == null) {
            this.inventory = new Inventory(4);
        }
        return inventory.addItem(item);
    }

    public boolean replaceItem(int index, Item newItem) {
        if (inventory == null) return false;
        return inventory.replaceItem(index, newItem);
    }

    public boolean hasItemType(ItemType type) {
        if (inventory == null) return false;
        for (Item item : inventory.getItems()) {
            if (item.getItemType() == type) return true;
        }
        return false;
    }

    public Item findItemByType(ItemType type) {
        if (inventory == null) return null;
        for (Item item : inventory.getItems()) {
            if (item.getItemType() == type) return item;
        }
        return null;
    }

    public boolean hasItemNamed(String name) {
        if (inventory == null || name == null) return false;
        for (Item item : inventory.getItems()) {
            if (name.equalsIgnoreCase(item.getName())) return true;
        }
        return false;
    }

    public void addTempAttack(int amount) {
        this.tempAttack += amount;
    }

    public void addTempDefense(int amount) {
        this.tempDefense += amount;
    }

    public int getTempAttack() { return tempAttack; }
    public int getTempDefense() { return tempDefense; }

    public int getBaseAttack() { return super.getAttack(); }
    public int getBaseDefense() { return super.getDefense(); }

    public int getCurseAttackPenalty() {
        if (inventory == null) return 0;
        int penalty = 0;
        for (Item item : inventory.getItems()) {
            if (item.isCurse() && item.getEffectType() == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.ATTACK) {
                penalty += item.getEffectValue();
            }
        }
        return penalty;
    }

    public int getCurseDefensePenalty() {
        if (inventory == null) return 0;
        int penalty = 0;
        for (Item item : inventory.getItems()) {
            if (item.isCurse() && item.getEffectType() == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.DEFENSE) {
                penalty += item.getEffectValue();
            }
        }
        return penalty;
    }

    public int getCurseDiceModifier() {
        if (inventory == null) return 0;
        int mod = 0;
        for (Item item : inventory.getItems()) {
            if (item.isCurse() && item.getEffectType() == io.github.snakes_and_ladders_roll_of_fate.systems.EventEffectType.MOVE) {
                mod += item.getEffectValue();
            }
        }
        return mod;
    }

    public boolean hasCurse() {
        return hasItemType(ItemType.CURSE);
    }

    /**
     * Purifies all passive curse burdens from the player's inventory (e.g., at a Sanctuary or Shrine).
     * @return Number of curses cleansed.
     */
    public int cleanseCurses() {
        if (inventory == null) return 0;
        int removed = 0;
        java.util.Iterator<Item> it = inventory.getItems().iterator();
        while (it.hasNext()) {
            if (it.next().isCurse()) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    @Override
    public int getAttack() {
        return Math.max(1, super.getAttack() + tempAttack + getCurseAttackPenalty());
    }

    @Override
    public int getDefense() {
        return Math.max(0, super.getDefense() + tempDefense + getCurseDefensePenalty());
    }

    public boolean hasActiveBuffs() {
        return tempAttack > 0 || tempDefense > 0;
    }

    public void clearTemporaryBuffs() {
        if (hasActiveBuffs()) {
            System.out.printf("  [BUFF EXPIRED] Turn bonuses worn off. Attack: %d, Defense: %d%n",
                getBaseAttack(), getBaseDefense());
            this.tempAttack = 0;
            this.tempDefense = 0;
        }
    }

    public void gainExperience(int exp) {
        if (exp <= 0) return;

        // 1. Get the percentage (defaults to 0 if no class is selected)
        int bonusPercent = (characterClass != null) ? characterClass.getExpBonus() : 0;

        // 2. Calculate the bonus EXP using integer math (e.g., 50 * 20 / 100 = 10)
        int bonusExp = (exp * bonusPercent) / 100;
        int totalExpGained = exp + bonusExp;

        this.experience += totalExpGained;
        this.totalExperience += totalExpGained;

        // Optional: Notify the player that they received a class bonus
        if (bonusExp > 0) {
            System.out.printf("  [CLASS BONUS] %s earns +%d bonus EXP (%d%%)!%n",
                characterClass.getName().trim(), bonusExp, bonusPercent);
        }

        // Check for level up (preserve rollover surplus experience)
        while (this.experience >= expThresh) {
            this.experience -= expThresh;
            levelUp();
            this.expThresh = (int) (this.expThresh + (this.expThresh * 0.10)); // Exp threshold is increased by 10% every level
        }
    }

    public void levelUp() {
        this.level++;
        setAttack(getBaseAttack() + 1);   // +1 Attack every level (prevents endgame snowballing)
        setDefense(getBaseDefense() + 1); // +1 Defense every level
        setHealth(getHealth() + 20);      // Heal up on level up (clamped to maxHealth in setHealth)
        System.out.println("Leveled Up!");//For level up tests
    }

    public void makeChoice(int choiceIndex) {}

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) {
        this.experience = Math.max(0, experience);
        if (this.totalExperience < this.experience) {
            this.totalExperience = this.experience;
        }
    }
    public int getTotalExperience() { return totalExperience; }
    public void setTotalExperience(int totalExperience) { this.totalExperience = Math.max(0, totalExperience); }
    public void spendTotalExperience(int amount) {
        if (amount <= 0) return;
        this.totalExperience = Math.max(0, this.totalExperience - amount);
    }
    public int getExpThresh() { return expThresh; }
    public void setExpThresh(int expThresh) { this.expThresh = expThresh; }

    public Inventory getInventory() { return inventory; }
    public void setInventory(Inventory inventory) { this.inventory = inventory; }

    public int getTurnsSinceLastEvent() { return turnsSinceLastEvent; }
    public void incrementTurnsSinceLastEvent() { this.turnsSinceLastEvent++; }
    public void resetEventCooldown() { this.turnsSinceLastEvent = 0; }

    public boolean hasPendingDiceChoice() { return pendingDiceChoice; }
    public void setPendingDiceChoice(boolean pendingDiceChoice) { this.pendingDiceChoice = pendingDiceChoice; }

    public int getMaxDiceRollCap() { return maxDiceRollCap; }
    public void setMaxDiceRollCap(int maxDiceRollCap) { this.maxDiceRollCap = maxDiceRollCap; }

    public boolean hasSnakeWard() { return hasSnakeWard; }
    public void setHasSnakeWard(boolean hasSnakeWard) { this.hasSnakeWard = hasSnakeWard; }

    // --- Polymorphic Class Skill Helpers ---
    public ClassSkill getClassSkill() { return classSkill; }
    public void setClassSkill(ClassSkill classSkill) { this.classSkill = classSkill; }

    public int getSkillCooldown() { return skillCooldown; }
    public void setSkillCooldown(int skillCooldown) { this.skillCooldown = Math.max(0, skillCooldown); }

    public boolean isSkillReady() {
        return classSkill != null && skillCooldown <= 0;
    }

    public void decrementSkillCooldown() {
        if (skillCooldown > 0) {
            skillCooldown--;
        }
    }

    /**
     * Polymorphically executes the active ClassSkill and resets cooldown.
     */
    public SkillResult useClassSkill(io.github.snakes_and_ladders_roll_of_fate.RollOfFateGame game) {
        if (!isSkillReady()) {
            return new SkillResult(false, (classSkill != null ? classSkill.getSkillName() : "Skill") + " is recharging (" + skillCooldown + " turns remaining).");
        }
        SkillResult result = classSkill.execute(this, game);
        if (result != null && result.isSuccess()) {
            this.skillCooldown = classSkill.getMaxCooldown();
        }
        return result;
    }
}
