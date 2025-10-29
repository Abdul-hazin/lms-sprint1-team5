package edu.vsu.lms.controller;

import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.*;
import java.time.*;
import java.util.*;

public class ScheduleController {
    private final AppState state = AppState.getInstance();

    public boolean generateSchedule(String leagueName, List<DayOfWeek> playDays, LocalDate startDate) {
        League league = state.getLeagues().get(leagueName);
        if (league == null || league.isScheduleCreated()) return false;

        var teams = new ArrayList<>(league.getTeams().keySet());
        if (teams.size() < 2) return false;

        List<Game> games = new ArrayList<>();
        LocalDate date = startDate;

        for (int i = 0; i < teams.size(); i++) {
            for (int j = i + 1; j < teams.size(); j++) {
                String t1 = teams.get(i);
                String t2 = teams.get(j);
                games.add(new Game(date, t1, t2));
                games.add(new Game(date.plusWeeks(1), t2, t1)); // reverse fixture

                // Advance date to next play day
                date = nextPlayDate(date, playDays);
            }
        }

        for (Game g : games) league.addGame(g);
        league.setScheduleCreated(true);
        state.save();
        return true;
    }

    private LocalDate nextPlayDate(LocalDate date, List<DayOfWeek> playDays) {
        for (int i = 1; i <= 7; i++) {
            LocalDate next = date.plusDays(i);
            if (playDays.contains(next.getDayOfWeek())) return next;
        }
        return date.plusDays(7);
    }
    public boolean recordResult(String leagueName, LocalDate date, String home, String away, int homeScore, int awayScore) {
    League league = state.getLeagues().get(leagueName);
    if (league == null) return false;

    for (Game g : league.getGames()) {
        if (g.getDate().equals(date) &&
            g.getHomeTeam().equals(home) &&
            g.getAwayTeam().equals(away)) {

            if (homeScore == awayScore) return false; // no ties
            String winner = (homeScore > awayScore) ? home : away;
            g.setResult(winner, homeScore, awayScore);

            Team t1 = league.getTeams().get(home);
            Team t2 = league.getTeams().get(away);

            if (winner.equals(home)) {
                t1.addWin();
                t2.addLoss();
            } else {
                t2.addWin();
                t1.addLoss();
            }

            state.save();
            return true;
        }
    }
    return false;
}

}
