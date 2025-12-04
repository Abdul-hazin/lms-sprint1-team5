package edu.vsu.lms.model;

import java.io.Serializable;

/**
 * PlayerGameStats
 * ---------------
 * Tracks the stats for a single Player in a single Game.
 *
 * Fields:
 *  - free throws: attempted, made, percent
 *  - 2-pointers: attempted, made, percent
 *  - 3-pointers: attempted, made, percent
 *  - total points
 *  - assists
 *  - fouls (max 6, then fouled out)
 *
 * NOTE: No quarter-by-quarter tracking, only full-game totals.
 */
public class PlayerGameStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Player player;

    private int freeThrowsAttempted;
    private int freeThrowsMade;

    private int twoPointersAttempted;
    private int twoPointersMade;

    private int threePointersAttempted;
    private int threePointersMade;

    private int assists;
    private int fouls;  // 0–6, at 6 = fouled out

    public PlayerGameStats(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    // ---------- Increment methods ----------

    public void addFreeThrows(int attempted, int made) {
        if (attempted < 0 || made < 0 || made > attempted) {
            throw new IllegalArgumentException("Invalid free throw numbers");
        }
        this.freeThrowsAttempted += attempted;
        this.freeThrowsMade += made;
    }

    public void addTwoPointers(int attempted, int made) {
        if (attempted < 0 || made < 0 || made > attempted) {
            throw new IllegalArgumentException("Invalid 2-point numbers");
        }
        this.twoPointersAttempted += attempted;
        this.twoPointersMade += made;
    }

    public void addThreePointers(int attempted, int made) {
        if (attempted < 0 || made < 0 || made > attempted) {
            throw new IllegalArgumentException("Invalid 3-point numbers");
        }
        this.threePointersAttempted += attempted;
        this.threePointersMade += made;
    }

    public void addAssists(int assistsToAdd) {
        if (assistsToAdd < 0) {
            throw new IllegalArgumentException("Assists cannot be negative");
        }
        this.assists += assistsToAdd;
    }

    /**
     * Adds fouls, but never lets total exceed 6.
     */
    public void addFouls(int foulsToAdd) {
        if (foulsToAdd < 0) {
            throw new IllegalArgumentException("Fouls to add cannot be negative");
        }
        this.fouls += foulsToAdd;
        if (this.fouls > 6) {
            this.fouls = 6;
        }
    }

    // ---------- Raw getters ----------

    public int getFreeThrowsAttempted() { return freeThrowsAttempted; }
    public int getFreeThrowsMade()      { return freeThrowsMade; }

    public int getTwoPointersAttempted() { return twoPointersAttempted; }
    public int getTwoPointersMade()      { return twoPointersMade; }

    public int getThreePointersAttempted() { return threePointersAttempted; }
    public int getThreePointersMade()      { return threePointersMade; }

    public int getAssists() { return assists; }
    public int getFouls()   { return fouls; }

    // ---------- Derived stats ----------

    public int getTotalPoints() {
        return freeThrowsMade * 1
             + twoPointersMade * 2
             + threePointersMade * 3;
    }

    public double getFreeThrowPercent() {
        return calcPercent(freeThrowsMade, freeThrowsAttempted);
    }

    public double getTwoPointPercent() {
        return calcPercent(twoPointersMade, twoPointersAttempted);
    }

    public double getThreePointPercent() {
        return calcPercent(threePointersMade, threePointersAttempted);
    }

    public boolean isFouledOut() {
        return fouls >= 6;
    }

    private double calcPercent(int made, int attempted) {
        if (attempted == 0) return 0.0;
        return (made * 100.0) / attempted;
    }
    // ===== NEW: Setter-style methods for editor panels =====
public void setFreeThrowStats(int attempted, int made) {
    if (attempted < 0 || made < 0 || made > attempted) {
        throw new IllegalArgumentException("Invalid free throw numbers");
    }
    this.freeThrowsAttempted = attempted;
    this.freeThrowsMade = made;
}

public void setTwoPointStats(int attempted, int made) {
    if (attempted < 0 || made < 0 || made > attempted) {
        throw new IllegalArgumentException("Invalid 2P numbers");
    }
    this.twoPointersAttempted = attempted;
    this.twoPointersMade = made;
}

public void setThreePointStats(int attempted, int made) {
    if (attempted < 0 || made < 0 || made > attempted) {
        throw new IllegalArgumentException("Invalid 3P numbers");
    }
    this.threePointersAttempted = attempted;
    this.threePointersMade = made;
}

public void setAssists(int assists) {
    if (assists < 0) throw new IllegalArgumentException("Assists cannot be negative");
    this.assists = assists;
}

public void setFouls(int fouls) {
    if (fouls < 0) fouls = 0;
    if (fouls > 6) fouls = 6;
    this.fouls = fouls;
}

    @Override
    public String toString() {
        return String.format("%s: %d pts, %d ast, %d fouls (FT %d/%d, 2P %d/%d, 3P %d/%d)",
                player.toString(),
                getTotalPoints(),
                assists,
                fouls,
                freeThrowsMade, freeThrowsAttempted,
                twoPointersMade, twoPointersAttempted,
                threePointersMade, threePointersAttempted);
    }
}
