package edu.vsu.lms.controller;

import java.util.List;
import java.util.stream.Collectors;

import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Team;

public class LeagueController {
    private final AppState state = AppState.getInstance();

    /* ---------- CREATE ---------- */

    public boolean createLeague(String name){
        if (name == null || name.isBlank()) return false;
        if (state.getLeagues().containsKey(name)) return false;

        state.getLeagues().put(name, new League(name));
        state.save(); // persist immediately
        return true;
    }

    public boolean addTeam(String leagueName, String teamName){
        League lg = state.getLeagues().get(leagueName);
        if (lg == null || teamName == null || teamName.isBlank()) return false;
        if (lg.getTeams().containsKey(teamName)) return false;

        lg.getTeams().put(teamName, new Team(teamName));
        state.save(); // persist immediately
        return true;
    }

    /* ---------- READ ---------- */

    public List<String> listLeagues() {
        return state.getLeagues()
                    .keySet()
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());
    }

    public List<String> listTeams(String leagueName){
        League lg = state.getLeagues().get(leagueName);
        if (lg == null) return List.of();
        return lg.getTeams().keySet().stream().sorted().collect(Collectors.toList());
    }

    /* ---------- DELETE ---------- */

    /** Delete a team inside a league. */
    public boolean deleteTeam(String leagueName, String teamName) {
        League lg = state.getLeagues().get(leagueName);
        if (lg == null) return false;
        if (!lg.getTeams().containsKey(teamName)) return false;

        lg.getTeams().remove(teamName);
        state.save(); // persist immediately
        return true;
    }

    /** Safe delete: fails if the league still has teams. */
    public boolean deleteLeague(String leagueName) {
        return deleteLeague(leagueName, /*force*/ false);
    }

    /**
     * Delete a league; if force==true, clears its teams first.
     * Returns false if league doesn't exist, or (force==false and it has teams).
     */
    public boolean deleteLeague(String leagueName, boolean force) {
        League lg = state.getLeagues().get(leagueName);
        if (lg == null) return false;

        if (!lg.getTeams().isEmpty() && !force) {
            return false; // block deletion if it still has teams
        }
        if (force) {
            lg.getTeams().clear(); // cascade delete teams
        }

        state.getLeagues().remove(leagueName);
        state.save(); // persist immediately
        return true;
    }
}