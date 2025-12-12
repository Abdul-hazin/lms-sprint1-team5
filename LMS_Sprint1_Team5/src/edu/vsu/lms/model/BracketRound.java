package edu.vsu.lms.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BracketRound
 * ------------
 * Represents one round of a single-elimination bracket,
 * e.g., "Round 1", "Semifinals", "Final".
 */
public class BracketRound implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int roundNumber;
    private final LocalDate date;
    private final List<Game> games = new ArrayList<>();

    public BracketRound(int roundNumber, LocalDate date) {
        this.roundNumber = roundNumber;
        this.date = date;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    /**
     * IMPORTANT: return the actual list so Bracket can modify it
     * when advancing winners.
     */
    public List<Game> getGames() {
        return games;   // ✅ was Collections.unmodifiableList(games)
    }

    /** Optional: read-only view if you ever need it elsewhere. */
    public List<Game> getGamesReadOnly() {
        return Collections.unmodifiableList(games);
    }

    public void addGame(Game game) {
        if (game != null) {
            games.add(game);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Round " + roundNumber + " (" + date + ")\n");
        for (Game g : games) {
            sb.append("  ").append(g.toString()).append("\n");
        }
        return sb.toString();
    }
}
