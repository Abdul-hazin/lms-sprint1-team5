package edu.vsu.lms.controller;

import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScheduleController {
    private final AppState state = AppState.getInstance();

    // small helper to store home/away before we know the exact date
    private static class Matchup {
        final String home;
        final String away;

        Matchup(String home, String away) {
            this.home = home;
            this.away = away;
        }
    }

    /**
     * Generate a double round-robin schedule:
     *  - Each team plays every other team twice (home + away)
     *  - Each "round" is assigned to the next date that falls on one of playDays
     *  - With two playDays (e.g. Tue/Sat), teams play once per day -> 2 games per week
     */
    public boolean generateSchedule(String leagueName, List<DayOfWeek> playDays, LocalDate startDate) {
        League league = state.getLeagues().get(leagueName);
        if (league == null || league.isScheduleCreated()) return false;

        // Get team names
        List<String> teamNames = new ArrayList<>(league.getTeams().keySet());
        if (teamNames.size() < 2) return false;

        // Sort for consistent schedule
        Collections.sort(teamNames, String.CASE_INSENSITIVE_ORDER);

        // Handle odd number of teams using a BYE
        final String BYE = "__BYE__";
        if (teamNames.size() % 2 == 1) {
            teamNames.add(BYE);
        }

        int n = teamNames.size();
        int roundsSingle = n - 1;          // single round-robin
        int totalRounds = roundsSingle * 2; // double round-robin

        // 1) Build single round-robin with the "circle method"
        List<List<Matchup>> rounds = new ArrayList<>();
        List<String> rotated = new ArrayList<>(teamNames);

        for (int round = 0; round < roundsSingle; round++) {
            List<Matchup> matchups = new ArrayList<>();

            for (int i = 0; i < n / 2; i++) {
                String home = rotated.get(i);
                String away = rotated.get(n - 1 - i);

                // Skip BYE games
                if (home.equals(BYE) || away.equals(BYE)) continue;

                matchups.add(new Matchup(home, away));
            }

            rounds.add(matchups);

            // Rotate everything except the first team
            // [ T0, T1, T2, T3, T4 ] -> [ T0, T4, T1, T2, T3 ]
            String first = rotated.get(0);
            List<String> rest = rotated.subList(1, rotated.size());
            Collections.rotate(rest, 1);
        }

        // 2) Mirror for the second half (swap home/away)
        int originalRounds = rounds.size();
        for (int r = 0; r < originalRounds; r++) {
            List<Matchup> base = rounds.get(r);
            List<Matchup> mirrored = new ArrayList<>();
            for (Matchup m : base) {
                mirrored.add(new Matchup(m.away, m.home)); // reverse home/away
            }
            rounds.add(mirrored);
        }

        // 3) Generate dates for each round on allowed playDays
        List<LocalDate> roundDates = generateRoundDates(startDate, playDays, totalRounds);

        // 4) Create Game objects and add to league
        for (int r = 0; r < totalRounds; r++) {
            LocalDate date = roundDates.get(r);
            for (Matchup m : rounds.get(r)) {
                Game g = new Game(date, m.home, m.away);
                league.addGame(g);
            }
        }

        league.setScheduleCreated(true);
        state.save();
        return true;
    }

    /**
     * Generate exactly totalRounds dates, each on one of the playDays,
     * starting at or after startDate, in chronological order.
     */
    private List<LocalDate> generateRoundDates(LocalDate startDate,
                                               List<DayOfWeek> playDays,
                                               int totalRounds) {
        List<LocalDate> dates = new ArrayList<>();
        if (playDays == null || playDays.isEmpty()) {
            // If no days specified, just use the start date's day of week
            playDays = List.of(startDate.getDayOfWeek());
        }

        LocalDate current = startDate.minusDays(1);
        while (dates.size() < totalRounds) {
            current = current.plusDays(1);
            if (playDays.contains(current.getDayOfWeek())) {
                dates.add(current);
            }
        }
        return dates;
    }

    // -------------------------------------------------
    // Your existing recordResult stays the same
    // -------------------------------------------------
    public boolean recordResult(String leagueName, LocalDate date,
                                String home, String away,
                                int homeScore, int awayScore) {
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
 