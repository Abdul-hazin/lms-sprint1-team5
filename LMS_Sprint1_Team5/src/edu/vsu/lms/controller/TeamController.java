package edu.vsu.lms.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Team;
import edu.vsu.lms.persistence.AppState;
/**
 * TeamController
 * --------------
 * Simple league-scoped team management used by TeamsPanel.
 */
public class TeamController {

    private final AppState state = AppState.getInstance();

    /** Get a league by name, or null if it doesn't exist. */
    private League getLeague(String leagueName) {
        if (leagueName == null || leagueName.isBlank()) return null;
        return state.getLeagues().get(leagueName);
    }

    /** List teams in a league, sorted by name (case-insensitive). */
    public List<Team> listTeams(String leagueName) {
        League league = getLeague(leagueName);
        if (league == null) {
            return Collections.emptyList();
        }

        return league.getTeams()
                .values()
                .stream()
                .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                .collect(Collectors.toList());
    }

    /** Create a team in the given league. Returns false if league missing, duplicate, or invalid name. */
    public boolean createTeam(String leagueName, String teamName) {
        if (!isValidTeamName(teamName)) return false;

        League league = getLeague(leagueName);
        if (league == null) return false;

        Map<String, Team> teams = league.getTeams();

        // Prevent duplicate name (case-insensitive)
        boolean exists = teams.keySet().stream()
                .anyMatch(n -> n.equalsIgnoreCase(teamName));
        if (exists) return false;

        teams.put(teamName, new Team(teamName));
        state.save();
        return true;
    }

    /** Delete a team from the given league. Returns true if deleted. */
    public boolean deleteTeam(String leagueName, String teamName) {
        League league = getLeague(leagueName);
        if (league == null) return false;

        boolean removed = (league.getTeams().remove(teamName) != null);
        if (removed) {
            state.save();
        }
        return removed;
    }

    // ----- optional compatibility aliases -----

    public List<Team> getTeams(String leagueName) {  // alias for other UIs
        return listTeams(leagueName);
    }

    public boolean addTeam(String leagueName, String teamName) { // alias
        return createTeam(leagueName, teamName);
    }

    // ----- validation (same pattern as TeamsPanel) -----

    private boolean isValidTeamName(String name) {
        // 3–30 chars; letters, digits, spaces, dash, apostrophe, ampersand
        return name != null && name.matches("[A-Za-z0-9 '\\-&]{3,30}");
    }
}

