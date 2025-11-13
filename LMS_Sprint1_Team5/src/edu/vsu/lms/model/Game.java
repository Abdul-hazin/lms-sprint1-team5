package edu.vsu.lms.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;

    private final LocalDate date;
    private final String homeTeam;
    private final String awayTeam;
    private String winner;
    private int homeScore;
    private int awayScore;

    public Game(LocalDate date, String homeTeam, String awayTeam) {
        this.date = date;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public LocalDate getDate() { return date; }
    public String getHomeTeam() { return homeTeam; }
    public String getAwayTeam() { return awayTeam; }
    public String getWinner() { return winner; }
    public int getHomeScore() { return homeScore; }
    public int getAwayScore() { return awayScore; }

    public void setResult(String winner, int homeScore, int awayScore) {
        this.winner = winner;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }
    public boolean hasResult() {
    return winner != null;
}


    @Override
    public String toString() {
        String result = (winner == null) ? "Not played yet"
            : String.format("%s def %s (%d-%d)", winner, 
                            winner.equals(homeTeam) ? awayTeam : homeTeam,
                            Math.max(homeScore, awayScore), Math.min(homeScore, awayScore));
        return String.format("%s: %s vs %s — %s", date, homeTeam, awayTeam, result);
    }
}
