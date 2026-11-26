# Devlog 33: Escalating Danger Board Generation, Tile Landing Trigger Harmonization & Pursuer Pacing Mechanics

* **Date:** November 26, 2026
* **Author:** John Calvin D. Samson (Project Leader)
* **Sprint / Milestone:** Week 6 – Pacing Overhaul, Hazard Scaling & Traversal Bug Hardening
* **Branch:** `feat/board-adventure-screen`

---

## Objectives
- **Escalating Danger Board Generation (Option A)**: Rebalance the procedural two-pass board generator to create a natural, escalating difficulty curve across the mountain. Snake frequency ramps sharply the higher and farther the player ascends, while ladders are capped globally at 10 and distributed across Acts (Act I $\le 5$, Act II $\le 3$, Act III $\le 2$).
- **Tile Landing Trigger Harmonization (Bug A Fix)**: Fix event choice movement and item displacements so that players landing on a ladder, snake, or POI landmark (Shrines, Merchants, Treasures, Crossroads) properly trigger the destination mechanics instead of bypassing them.
- **Pale Shepherd Pursuer Pacing & Anti-Overshoot Guarantees (Bug B Fix)**: Harden The Pale Shepherd's locomotion kinematics and encounter dynamics to prevent the pursuer from overtaking the party, reposition behind players when they slide down snakes, guarantee breathing room after encounter choices, and offset visual rendering when sharing a tile.
- **Automated Verification**: Implement comprehensive test assertions across 1,000 procedural board seeds and dedicated unit tests verifying edge-case mechanics.

---

## Work Completed

### 1. Escalating Danger Board Generation (`Board.java`, `BoardTest.java`)
- **Act-Specific Hazard Scaling**:
  - **Act I (Tiles 1–30)**: Base Ladder 22%, Base Snake 8%. Gentle onboarding providing early upward momentum.
  - **Act II (Tiles 31–70)**: Base Ladder 12%, Base Snake 22%. Increased hazard density and tactical risk.
  - **Act III (Tiles 71–100)**: Base Ladder 5%, Base Snake 38%. Purely chance-based sparse ladders and severe serpent ambushes near the summit.
- **Global & Regional Ladder Caps**:
  - Enforced strict ceilings: Global $\le 10$, Act I $\le 5$, Act II $\le 3$, Act III $\le 2$.
  - Ladders revert to safe normal tiles if safe unreserved destinations cannot be found in Pass 2, decrementing act counters accordingly.
- **Milestone & Anti-Clustering Safeguards**:
  - Milestones 1, 2, 3 (Start Camp), 30 (Sanctuary), 70 (Abyssal Gate), and 98, 99, 100 (Throne Approach) remain strictly normal.
  - Anti-clustering guarantees at least 2 safe normal tiles between special tiles.

### 2. Tile Event Landing Trigger Harmonization (`BoardScreen.java`)
- **Root Cause**: During event choice outcomes (e.g. sprinting ahead +3 tiles) or item movements, `SpecialMovePhase.LANDED` timed out and transitioned turns immediately without inspecting the destination tile.
- **Fix**: In `BoardScreen.java` (`SpecialMovePhase.LANDED`), upon landing timer expiry:
  - Inspect `Tile landedTile = board.getTile(finalPos)`.
  - If `landedTile instanceof LadderTile || landedTile instanceof SnakeTile || landedTile.getLandmark() != TileLandmark.NONE`, route immediately to `handleTileArrival(player, finalPos)`.
  - Ladders ascend smoothly, snakes trigger serpent ambushes/flute wards, and landmarks open their narrative dialogues.

### 3. Pale Shepherd Pursuer Mechanics & Anti-Trap Hardening (`BoardScreen.java`, `RollOfFateGame.java`)
- **Anti-Overshoot Pacing Clamping**:
  - In `handlePaleShepherdRoundProgression()`, pursuer speed is clamped to `Math.min(speed, gap)` where `gap = trailingPos - curTile`.
  - The reaper will at most intercept the trailing hero on their tile, but will never leap ahead into higher tiles.
- **Snake-Slide Trailing Repositioning**:
  - If a player slides down a snake past the pursuer (e.g. from 35 down to 10 while reaper was at 20), the reaper repositions to $\max(1, \text{finalPos} - 3)$ (Tile 7) and gains 1 round of contemplation pause.
  - The pursuer remains behind the player, preserving the thematic stalker fantasy without stranding the reaper ahead.
- **Guaranteed Respite & D1 Loop Elimination**:
  - **Struggle for Life**: Drains 20 HP, repels reaper 5 tiles back, grants 2 rounds of contemplation pause, and surges player +3 tiles forward.
  - **Soothe with Memory**: Sacrifices a relic, repels reaper 5 tiles back, and grants 2 rounds of pause.
  - **Trial of Will**: Success repels reaper 6 tiles with 2 pause rounds; failure drains 15 HP, repels reaper 4 tiles, grants 1 pause round, and scrambles player +2 tiles forward.
  - Even on a subsequent D1 roll, the hero maintains a safe buffer and escapes immediate re-triggering.
- **Non-Occluding Sprite Offset**:
  - In `renderPaleShepherd()`, when sharing a tile with an active player, the reaper is rendered with an offset of $X - 32\text{px}$ and elevated $Y + 26\text{px}$ in the background. Both character and pursuer sprites remain distinctly visible.

---

## Verification & Test Results

```text
=== RUNNING BOARD ADVENTURE BUG FIX VERIFICATION SUITE ===

[Test 1] Bug A: Verifying Landing on Ladder / Snake / Landmark...
  [PASS] Landing on Tile 14 (LadderTile -> 22) correctly recognized for activation.

[Test 2] Bug B: Verifying Pursuer Anti-Overshoot Clamping...
  [PASS] Pursuer clamped to exact player tile (15), never overshooting to 16+.

[Test 3] Bug B: Verifying Snake Slide Behind Pursuer Repositioning...
  [PASS] After slide to Tile 10, Reaper safely relocated behind player to Tile 7 with 1 contemplation round.

[Test 4] Bug B: Verifying Encounter Pushback and Contemplation Pause (No D1 Loop)...
  [PASS] Struggle for Life gives 8-tile gap (20 vs 28) and 2 pause rounds. Rolling D1 cleanly escapes the reaper!
>>> ALL BUG FIX VERIFICATIONS PASSED WITH ZERO FAILURES!

=== RUNNING TWO-PASS BOARD GENERATION TEST SUITE ===
>>> SUCCESS! 1,000 Board Generation simulations passed with zero invariant violations.
    - Ladders Distribution: Avg Total=6.55 (Cap <= 10) | Act I=3.61 (Cap <= 5) | Act II=2.34 (Cap <= 3) | Act III=0.61 (Cap <= 2)
    - Snakes Escalation:   Avg Total=12.88 | Act I=1.39 | Act II=5.63 | Act III=5.86
    - All milestones 1, 30, 70, 100 strictly safe
    - Minimum jump distance >= 6 strictly enforced
    - Anti-clustering (>= 2 safe tiles between specials) 100% verified
    - 100% of ladders and snakes land on safe Normal tiles (zero loops or chain traps)
```

All 22 inventory and combat integration tests passed with zero regressions.
