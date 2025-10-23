package edu.vsu.lms.controller;

import java.util.List;
import java.util.stream.Collectors;
import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Team;

public class LeagueController {
    private final AppState state = AppState.getInstance();

    public boolean createLeague(String name){
        if(name==null || name.isBlank()) return false;
        if(state.getLeagues().containsKey(name)) return false;
        state.getLeagues().put(name, new League(name));
        return true;
    }
    public boolean addTeam(String leagueName, String teamName){
        League lg = state.getLeagues().get(leagueName);
        if(lg==null || teamName==null || teamName.isBlank()) return false;
        if(lg.getTeams().containsKey(teamName)) return false;
        lg.getTeams().put(teamName, new Team(teamName));
        return true;
    }
    public List<String> listTeams(String leagueName){
        League lg = state.getLeagues().get(leagueName);
        if(lg==null) return List.of();
        return lg.getTeams().keySet().stream().sorted().collect(Collectors.toList());
    }
    public List<String> listLeagues() {
        return state.getLeagues()
                    .keySet()
                    .stream()
                    .sorted()
                    .collect(Collectors.toList());
    }
}
