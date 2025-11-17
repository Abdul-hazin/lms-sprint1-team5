package edu.vsu.lms.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.Game;
import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Team;

public class LeagueController {
    private final AppState state = AppState.getInstance();

    /* ---------- DTOs for Sprint 2 ---------- */

    // US 16: League standings row
    public static class Standing {
        public String teamName;
        public int wins;
        public int losses;

        public double getWinPct() {
            int total = wins + losses;
            return total == 0 ? 0.0 : (wins * 1.0 / total);
        }
    }

    // US 17: One row in the games summary
    public static class GameSummary {
        public LocalDate date;
        public String winnerName;
        public int winnerScore;
        public String loserName;
        public int loserScore;
    }

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

    /**
     * US 16 - View League Standings
     * Returns a list of Standing objects ordered by:
     *  - wins (descending)
     *  - then team name (ascending)
     */
    public List<Standing> getLeagueStandings(String leagueName) {
        League lg = state.getLeagues().get(leagueName);
        if (lg == null) return Collections.emptyList();

        Map<String, Standing> map = new HashMap<>();

        // Iterate over all games in this league
        for (Game g : lg.getGames()) {
            if (!g.hasResult()) continue;

            String winner = g.getWinner();
            String loser  = g.getLoser();
            if (winner == null || loser == null) continue;

            Standing sw = map.computeIfAbsent(winner, t -> {
                Standing s = new Standing();
                s.teamName = t;
                return s;
            });
            sw.wins++;

            Standing sl = map.computeIfAbsent(loser, t -> {
                Standing s = new Standing();
                s.teamName = t;
                return s;
            });
            sl.losses++;
        }

        // Make sure every team appears, even if 0–0
        for (Team t : lg.getTeams().values()) {
            map.computeIfAbsent(t.getName(), name -> {
                Standing s = new Standing();
                s.teamName = name;
                return s;
            });
        }

        List<Standing> list = new ArrayList<>(map.values());
        list.sort(
            Comparator
                .comparingInt((Standing s) -> -s.wins) // more wins first
                .thenComparing(s -> s.teamName)        // then name
        );

        return list;
    }

    /**
     * US 17 - View Games Summary Stats
     * Returns all games (or within a date range) ordered by date.
     * If fromDate or toDate is null, that side of the range is open.
     */
    public List<GameSummary> getGameSummaries(
            String leagueName,
            LocalDate fromDate,
            LocalDate toDate) {

        League lg = state.getLeagues().get(leagueName);
        if (lg == null) return Collections.emptyList();

        List<GameSummary> list = new ArrayList<>();

        for (Game g : lg.getGames()) {
            if (!g.hasResult()) continue;

            LocalDate d = g.getDate();
            if (fromDate != null && d.isBefore(fromDate)) continue;
            if (toDate   != null && d.isAfter(toDate))    continue;

            GameSummary s = new GameSummary();
            s.date        = d;
            s.winnerName  = g.getWinner();
            s.loserName   = g.getLoser();
            s.winnerScore = g.getWinningScore();
            s.loserScore  = g.getLosingScore();

            list.add(s);
        }

        list.sort(Comparator.comparing(gs -> gs.date)); // order they were played
        return list;
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
