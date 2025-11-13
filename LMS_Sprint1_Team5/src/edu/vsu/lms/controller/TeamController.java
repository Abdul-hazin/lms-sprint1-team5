package edu.vsu.lms.controller;

import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Team;
import edu.vsu.lms.persistence.AppState;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TeamController
 * --------------
 * League-scoped team management used by TeamsPanel.
 */
public class TeamController {

    private final AppState app = AppState.getInstance();

    // ----- helpers -----

    private League ensureLeague(String leagueName) {
        if (leagueName == null || leagueName.isBlank()) {
            leagueName = app.getOrInitDefaultLeague();
        }
        return app.getLeagues().computeIfAbsent(leagueName, League::new);
    }

    private Optional<League> getLeague(String leagueName) {
        if (leagueName == null || leagueName.isBlank()) {
            leagueName = app.getOrInitDefaultLeague();
        }
        return Optional.ofNullable(app.getLeagues().get(leagueName));
    }

    // ----- API used by TeamsPanel -----

    /** List teams in a league, sorted by name (case-insensitive). */
    public List<Team> listTeams(String leagueName) {
        return getLeague(leagueName)
                .map(l -> l.getTeams().values().stream()
                        .sorted(Comparator.comparing(Team::getName, String.CASE_INSENSITIVE_ORDER))
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    /** Create a team in the given league. Returns false if duplicate or invalid. */
    public boolean createTeam(String leagueName, String teamName) {
        if (!isValidTeamName(teamName)) return false;

        League league = ensureLeague(leagueName);
        Map<String, Team> teams = league.getTeams();

        // Prevent duplicate name (case-insensitive)
        boolean exists = teams.keySet().stream()
                .anyMatch(n -> n.equalsIgnoreCase(teamName));
        if (exists) return false;

        teams.put(teamName, new Team(teamName));
        app.save();
        return true;
    }

    /** Delete a team from the given league. */
    public boolean deleteTeam(String leagueName, String teamName) {
        League league = ensureLeague(leagueName);
        boolean removed = (league.getTeams().remove(teamName) != null);
        if (removed) app.save();
        return removed;
    }

    // ----- optional compatibility aliases -----

    public List<Team> getTeams(String leagueName) {  // alias for other UIs
        return listTeams(leagueName);
    }

    public boolean addTeam(String leagueName, String teamName) { // alias
        return createTeam(leagueName, teamName);
    }

    // ----- validation (mirrors TeamsPanel pattern) -----

    private boolean isValidTeamName(String name) {
        // 3–30 chars; letters, digits, spaces, dash, apostrophe, ampersand
        return name != null && name.matches("[A-Za-z0-9 '\\-&]{3,30}");
    }
}
