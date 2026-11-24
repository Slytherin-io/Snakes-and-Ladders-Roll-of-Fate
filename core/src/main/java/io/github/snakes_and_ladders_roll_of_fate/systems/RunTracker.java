package io.github.snakes_and_ladders_roll_of_fate.systems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.snakes_and_ladders_roll_of_fate.entities.Player;

/**
 * Tracks comprehensive run statistics for each player throughout a board session,
 * computes cooperative player accolades/highlights for the end-game summary screen,
 * and generates engaging bardic chronicles of the expedition.
 */
public class RunTracker {

    public static class PlayerStats {
        private final Player player;
        private int totalTurns = 0;
        private int tilesMoved = 0;
        private int highestTile = 1;
        private int laddersClimbed = 0;
        private int ladderTilesGained = 0;
        private int snakesEncountered = 0;
        private int snakesSlain = 0;
        private int snakeTilesLost = 0;
        private int combatDamageDealt = 0;
        private int combatDamageTaken = 0;
        private int eventsResolved = 0;
        private int itemsCollected = 0;
        private int itemsUsed = 0;
        private int sixesRolled = 0;
        private int totalDiceRolls = 0;
        private int natural20s = 0;
        private boolean reachedThrone = false;
        private boolean fallenInBattle = false;
        private String accoladeTitle = "THE DAUNTLESS VOYAGER";
        private String accoladeDescription = "Braved the perilous path across the ancient stepping stones.";

        public PlayerStats(Player player) {
            this.player = player;
            if (player != null) {
                this.highestTile = player.getPosition();
            }
        }

        public Player getPlayer() { return player; }
        public int getTotalTurns() { return totalTurns; }
        public int getTilesMoved() { return tilesMoved; }
        public int getHighestTile() { return highestTile; }
        public int getLaddersClimbed() { return laddersClimbed; }
        public int getLadderTilesGained() { return ladderTilesGained; }
        public int getSnakesEncountered() { return snakesEncountered; }
        public int getSnakesSlain() { return snakesSlain; }
        public int getSnakeTilesLost() { return snakeTilesLost; }
        public int getCombatDamageDealt() { return combatDamageDealt; }
        public int getCombatDamageTaken() { return combatDamageTaken; }
        public int getEventsResolved() { return eventsResolved; }
        public int getItemsCollected() { return itemsCollected; }
        public int getItemsUsed() { return itemsUsed; }
        public int getSixesRolled() { return sixesRolled; }
        public int getTotalDiceRolls() { return totalDiceRolls; }
        public int getNatural20s() { return natural20s; }
        public boolean isReachedThrone() { return reachedThrone; }
        public boolean isFallenInBattle() { return fallenInBattle; }
        public String getAccoladeTitle() { return accoladeTitle; }
        public String getAccoladeDescription() { return accoladeDescription; }

        public void setAccolade(String title, String description) {
            this.accoladeTitle = title;
            this.accoladeDescription = description;
        }
    }

    private final Map<Player, PlayerStats> statsMap = new HashMap<>();

    public RunTracker() {}

    public synchronized PlayerStats getOrCreateStats(Player player) {
        if (player == null) return null;
        return statsMap.computeIfAbsent(player, PlayerStats::new);
    }

    public synchronized void recordTurn(Player player) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) stats.totalTurns++;
    }

    public synchronized void recordRoll(Player player, int rollValue) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) {
            stats.totalDiceRolls++;
            if (rollValue == 6) stats.sixesRolled++;
        }
    }

    public synchronized void recordSteps(Player player, int steps, int newTile) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) {
            stats.tilesMoved += steps;
            if (newTile > stats.highestTile) {
                stats.highestTile = newTile;
            }
        }
    }

    public synchronized void recordLadder(Player player, int fromTile, int toTile) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) {
            stats.laddersClimbed++;
            stats.ladderTilesGained += Math.max(0, toTile - fromTile);
            if (toTile > stats.highestTile) {
                stats.highestTile = toTile;
            }
        }
    }

    public synchronized void recordSnakeEncounter(Player player) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) stats.snakesEncountered++;
    }

    public synchronized void recordSnakeCombat(Player player, int damageDealt, boolean slain, boolean natural20) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) {
            stats.combatDamageDealt += damageDealt;
            if (slain) stats.snakesSlain++;
            if (natural20) stats.natural20s++;
        }
    }

    public synchronized void recordSnakeSlide(Player player, int tilesLost) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) {
            stats.snakeTilesLost += Math.max(0, tilesLost);
        }
    }

    public synchronized void recordDamageTaken(Player player, int damage) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) {
            stats.combatDamageTaken += damage;
        }
    }

    public synchronized void recordEventResolved(Player player) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) stats.eventsResolved++;
    }

    public synchronized void recordItemCollected(Player player) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) stats.itemsCollected++;
    }

    public synchronized void recordItemUsed(Player player) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) stats.itemsUsed++;
    }

    public synchronized void recordThroneClaimed(Player player) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) {
            stats.reachedThrone = true;
            stats.highestTile = 100;
        }
    }

    public synchronized void recordPlayerFallen(Player player) {
        PlayerStats stats = getOrCreateStats(player);
        if (stats != null) {
            stats.fallenInBattle = true;
        }
    }

    /**
     * Assigns unique, positive accolades to each player in the party.
     * Ensures minimal competition by spotlighting each adventurer's best feat.
     */
    public synchronized void computeAccolades(List<Player> players) {
        if (players == null || players.isEmpty()) return;

        Set<String> usedAccolades = new HashSet<>();

        // 1. First Pass: Crown the Champion who reached the Throne
        for (Player p : players) {
            PlayerStats ps = getOrCreateStats(p);
            if (ps.isReachedThrone()) {
                ps.setAccolade("THE FATE CHAMPION", "Ascended all 100 tiles and claimed the Throne of Fate!");
                usedAccolades.add(ps.getAccoladeTitle());
                break;
            }
        }

        // 2. Second Pass: Assign distinct superlatives to remaining players based on strongest merits
        for (Player p : players) {
            PlayerStats ps = getOrCreateStats(p);
            if (ps.isReachedThrone()) continue; // Already crowned

            // Score candidate categories
            if (!usedAccolades.contains("THE SERPENT BANE") && (ps.snakesSlain > 0 || ps.combatDamageDealt >= 20)) {
                ps.setAccolade("THE SERPENT BANE", "Dealt " + ps.combatDamageDealt + " combat damage and vanquished coiled serpents.");
                usedAccolades.add("THE SERPENT BANE");
            } else if (!usedAccolades.contains("THE SKYWARD STRIDER") && (ps.laddersClimbed > 0 || ps.ladderTilesGained >= 15)) {
                ps.setAccolade("THE SKYWARD STRIDER", "Conquered the vertical heights, ascending " + ps.ladderTilesGained + " tiles via ladders.");
                usedAccolades.add("THE SKYWARD STRIDER");
            } else if (!usedAccolades.contains("THE MASTER SCAVENGER") && (ps.itemsCollected > 0 || ps.itemsUsed > 0)) {
                ps.setAccolade("THE MASTER SCAVENGER", "Kept the satchel ready, gathering and wielding powerful relics.");
                usedAccolades.add("THE MASTER SCAVENGER");
            } else if (!usedAccolades.contains("FORTUNE'S CHOSEN") && (ps.sixesRolled >= 2 || ps.natural20s > 0)) {
                ps.setAccolade("FORTUNE'S CHOSEN", "Blessed by destiny, rolling " + ps.sixesRolled + " maximum sixes on the dice.");
                usedAccolades.add("FORTUNE'S CHOSEN");
            } else if (!usedAccolades.contains("THE IRON BASTION") && ps.combatDamageTaken >= 15) {
                ps.setAccolade("THE IRON BASTION", "Endured " + ps.combatDamageTaken + " damage and held the line through mortal peril.");
                usedAccolades.add("THE IRON BASTION");
            } else if (!usedAccolades.contains("THE MYSTIC SAGE") && ps.eventsResolved > 0) {
                ps.setAccolade("THE MYSTIC SAGE", "Braved " + ps.eventsResolved + " ambient dilemmas and ancient tile encounters.");
                usedAccolades.add("THE MYSTIC SAGE");
            } else {
                ps.setAccolade("THE DAUNTLESS VOYAGER", "Traversed " + ps.tilesMoved + " tiles with steadfast courage on the road to destiny.");
                usedAccolades.add("THE DAUNTLESS VOYAGER");
            }
        }
    }

    /**
     * Generates a poetic, engaging bardic narrative paragraph summarizing the expedition.
     */
    public synchronized String generateBardicNarrative(boolean isVictory, Player hero, List<Player> players) {
        int totalPartyTurns = 0;
        int totalLadders = 0;
        int totalSlain = 0;
        int totalEvents = 0;

        for (Player p : players) {
            PlayerStats ps = getOrCreateStats(p);
            totalPartyTurns += ps.totalTurns;
            totalLadders += ps.laddersClimbed;
            totalSlain += ps.snakesSlain;
            totalEvents += ps.eventsResolved;
        }

        String heroName = (hero != null) ? hero.getName().toUpperCase() : "THE HERO";
        String heroClass = (hero != null && hero.getCharacterClass() != null) ? hero.getCharacterClass().getTitle() : "Adventurer";

        if (isVictory) {
            StringBuilder sb = new StringBuilder();
            sb.append("The bards of the ancient realm strike their golden lyres! Across ")
              .append(totalPartyTurns).append(" fateful turns of fortune, ")
              .append(heroName).append(" the ").append(heroClass);

            if (players.size() > 1) {
                sb.append(" and their valiant companions");
            }

            sb.append(" braved the treacherous wilderness. ");

            if (totalSlain > 0) {
                sb.append("With blade and instinct, ")
                  .append(totalSlain).append(" lurking serpents were felled in fierce combat. ");
            }
            if (totalLadders > 0) {
                sb.append("Scaling ancient ladders, the party bypassed deadly crags. ");
            }

            sb.append("At long last, ").append(heroName)
              .append(" mounted the hundredth stone and claimed the Throne of Fate, sealing their name in the chronicles of eternity!");
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("A solemn mist settles over the winding stones. After ")
              .append(totalPartyTurns).append(" valiant turns through venomous thickets and shifting terrain, ")
              .append(heroName).append(" the ").append(heroClass);

            if (players.size() > 1) {
                sb.append(" and the fellowship");
            }

            sb.append(" succumbed to the perilous hazards of the wild. ");

            if (totalSlain > 0) {
                sb.append("Even in defeat, their courage was unyielding - felling ")
                  .append(totalSlain).append(" beasts along the way. ");
            }

            sb.append("Though the Throne of Fate eludes them today, their epic tale of endurance shall inspire wanderers for generations to come.");
            return sb.toString();
        }
    }
}
