package edu.vsu.lms.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

public class League implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Map<String, Team> teams = new HashMap<>();

    public League(String name){
         this.name = name; 
    }
    public String getName(){ 
        return name;
    }
    public Map<String, Team> getTeams(){ 
        return teams; 
    }
private final List<Game> games = new ArrayList<>();
private boolean scheduleCreated = false;

public boolean isScheduleCreated() { 
    return scheduleCreated;
 }
public List<Game> getGames() { 
    return games; 
}

public void addGame(Game g) {
     games.add(g); 
    }
public void clearSchedule() { 
    games.clear(); scheduleCreated = false;
 }
public void setScheduleCreated(boolean val) { 
    scheduleCreated = val; 
}
public void addTeam(Team team) {
    if (team != null && team.getName() != null && !team.getName().isBlank()) {
        teams.put(team.getName(), team);
    }
}


}
