package edu.vsu.lms.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * League
 * ------
 * Holds all teams in the league plus the regular-season games and
 * (for Sprint 3) an optional playoff Bracket.
 */
public class League implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final Map<String, Team> teams = new HashMap<>();

    // Existing regular-season schedule
    private final List<Game> games = new ArrayList<>();
    private boolean scheduleCreated = false;

    // ✅ NEW: Single-elimination playoff bracket
    private Bracket bracket;

    public League(String name) {
        this.name = name;
    }

    // ---------- Basic info ----------

    public String getName() {
        return name;
    }

    public Map<String, Team> getTeams() {
        return teams;
    }

    public void addTeam(Team team) {
        if (team != null && team.getName() != null && !team.getName().isBlank()) {
            teams.put(team.getName(), team);
        }
    }

    // ---------- Regular-season schedule ----------

    public boolean isScheduleCreated() {
        return scheduleCreated;
    }

    public List<Game> getGames() {
        return games;
    }

    public void addGame(Game g) {
        if (g != null) {
            games.add(g);
        }
    }

    public void clearSchedule() {
        games.clear();
        scheduleCreated = false;
    }

    public void setScheduleCreated(boolean val) {
        scheduleCreated = val;
    }

    // ---------- NEW: Bracket support (US 25–27) ----------

    /**
     * Returns the current playoff bracket, or null if none has been generated.
     */
    public Bracket getBracket() {
        return bracket;
    }

    /**
     * Sets the playoff bracket (used by Bracket scheduling logic).
     */
    public void setBracket(Bracket bracket) {
        this.bracket = bracket;
    }

    /**
     * @return true if a bracket has been generated.
     */
    public boolean hasBracket() {
        return bracket != null;
    }

    /**
     * Convenience method for Sprint 3:
     * Generate a single-elimination bracket from the current league standings.
     *
     * @param firstRoundDate The date to use for Round 1 games.
     */
    public void generateBracket(java.time.LocalDate firstRoundDate) {
        if (teams.size() < 2) {
            throw new IllegalStateException("Need at least 2 teams to create a bracket.");
        }
        this.bracket = Bracket.createSingleEliminationBracket(this, firstRoundDate);
    }
}
