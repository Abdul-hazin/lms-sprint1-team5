package edu.vsu.lms.view;

import edu.vsu.lms.model.Game;
import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Team;
import edu.vsu.lms.model.Player;
import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.controller.GameStatsController;
import edu.vsu.lms.model.GameStats;
import edu.vsu.lms.model.PlayerGameStats;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RecordResultPanel
 * -----------------
 * Lets a user pick a game in a league and:
 *  - enter the final score and save it, OR
 *  - simulate a result (team score) and also simulate player stats
 *    for all players in that game.
 *
 * Also supports:
 *  - "Simulate Up To Date": simulate results + stats for all unsimulated
 *    games whose date is <= a chosen cutoff.
 *
 * Used by League Admin / League Official.
 */
public class RecordResultPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String leagueName;
    private final League league;
    private final GameStatsController statsController;

    private final JComboBox<Game> gameBox;
    private final JTextField homeScoreField;
    private final JTextField awayScoreField;
    private final JLabel lblHomeTeam;
    private final JLabel lblAwayTeam;

    public RecordResultPanel(String leagueName) {
        this.leagueName = leagueName;
        AppState state = AppState.getInstance();
        this.league = state.getLeagues().get(leagueName);
        this.statsController = state.getGameStatsController();

        if (league == null) {
            throw new IllegalArgumentException("League not found: " + leagueName);
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== TOP: title =====
        JLabel title = new JLabel("Record Result — " + leagueName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        // ===== CENTER: form =====
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.anchor = GridBagConstraints.WEST;

        // Row 0: select game
        gc.gridx = 0;
        gc.gridy = 0;
        center.add(new JLabel("Game:"), gc);

        gameBox = new JComboBox<>();
        loadGames();

        gc.gridx = 1;
        gc.gridwidth = 2;
        center.add(gameBox, gc);

        gc.gridwidth = 1;

        // Row 1: home team label
        gc.gridx = 0;
        gc.gridy = 1;
        center.add(new JLabel("Home Team:"), gc);

        lblHomeTeam = new JLabel("-");
        gc.gridx = 1;
        center.add(lblHomeTeam, gc);

        // Row 2: away team label
        gc.gridx = 0;
        gc.gridy = 2;
        center.add(new JLabel("Away Team:"), gc);

        lblAwayTeam = new JLabel("-");
        gc.gridx = 1;
        center.add(lblAwayTeam, gc);

        // Row 3: home score
        gc.gridx = 0;
        gc.gridy = 3;
        center.add(new JLabel("Home Score:"), gc);

        homeScoreField = new JTextField(5);
        gc.gridx = 1;
        center.add(homeScoreField, gc);

        // Row 4: away score
        gc.gridx = 0;
        gc.gridy = 4;
        center.add(new JLabel("Away Score:"), gc);

        awayScoreField = new JTextField(5);
        gc.gridx = 1;
        center.add(awayScoreField, gc);

        add(center, BorderLayout.CENTER);

        // ===== BOTTOM: buttons =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnSimulateToDate = new JButton("Simulate Up To Date");      // ✅ NEW
        JButton btnSimulate       = new JButton("Simulate Result + Player Stats");
        JButton btnSave           = new JButton("Save Result");
        JButton btnClose          = new JButton("Close");

        bottom.add(btnSimulateToDate);   // ✅ NEW
        bottom.add(btnSimulate);
        bottom.add(btnSave);
        bottom.add(btnClose);

        add(bottom, BorderLayout.SOUTH);

        // ===== LISTENERS =====

        // When user selects a game, update team labels
        gameBox.addActionListener(e -> updateTeamLabels());

        // Save button: use whatever is typed in the fields
        btnSave.addActionListener(e -> saveResultFromFields());

        // Single-game simulate
        btnSimulate.addActionListener(e -> simulateAndSaveResult());

        // ✅ NEW: simulate all games up to a given date
        btnSimulateToDate.addActionListener(e -> simulateUpToDate());

        // Close dialog if opened in a JDialog
        btnClose.addActionListener(e -> {
            java.awt.Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JDialog) {
                w.dispose();
            }
        });

        // initialize labels based on first game
        updateTeamLabels();
    }

    // ===================== Data Loading / Helpers =====================

    private void loadGames() {
        List<Game> games = league.getGames();
        gameBox.removeAllItems();
        for (Game g : games) {
            gameBox.addItem(g);
        }
    }

    private Game getSelectedGame() {
        return (Game) gameBox.getSelectedItem();
    }

    private void updateTeamLabels() {
        Game g = getSelectedGame();
        if (g == null) {
            lblHomeTeam.setText("-");
            lblAwayTeam.setText("-");
            return;
        }
        lblHomeTeam.setText(g.getHomeTeam());
        lblAwayTeam.setText(g.getAwayTeam());
    }

    // ===================== Manual Save =====================

    private void saveResultFromFields() {
        Game g = getSelectedGame();
        if (g == null) {
            JOptionPane.showMessageDialog(this,
                    "No game selected.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int homeScore;
        int awayScore;
        try {
            homeScore = Integer.parseInt(homeScoreField.getText().trim());
            awayScore = Integer.parseInt(awayScoreField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Scores must be whole numbers.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (homeScore < 0 || awayScore < 0) {
            JOptionPane.showMessageDialog(this,
                    "Scores cannot be negative.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        applyResultToLeague(g, homeScore, awayScore, false);

        JOptionPane.showMessageDialog(this,
                "Result saved:\n" +
                        g.getHomeTeam() + " " + homeScore + " - " +
                        awayScore + " " + g.getAwayTeam(),
                "Game Result",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ===================== Single-Game Simulation =====================

    private void simulateAndSaveResult() {
        Game g = getSelectedGame();
        if (g == null) {
            JOptionPane.showMessageDialog(this,
                    "No game selected.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int[] scores = simulateSingleGame(g);
        if (scores == null) {
            return; // error already shown
        }

        int homeScore = scores[0];
        int awayScore = scores[1];

        // Show scores in UI for the selected game
        homeScoreField.setText(String.valueOf(homeScore));
        awayScoreField.setText(String.valueOf(awayScore));

        JOptionPane.showMessageDialog(this,
                "Simulated game result and player stats saved.\n" +
                        g.getHomeTeam() + " " + homeScore + " - " +
                        awayScore + " " + g.getAwayTeam(),
                "Simulation Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Simulate a single game (team scores + player stats) and return
     * [homeScore, awayScore].
     */
    private int[] simulateSingleGame(Game g) {
        if (g == null) return null;

        // Simple random scoring: 80–120 per team
        int homeScore = ThreadLocalRandom.current().nextInt(80, 121);
        int awayScore = ThreadLocalRandom.current().nextInt(80, 121);

        // Avoid ties
        if (homeScore == awayScore) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                homeScore++;
            } else {
                awayScore++;
            }
        }

        // Apply result to league/teams (includes winner + W/L update)
        if (!applyResultToLeague(g, homeScore, awayScore, true)) {
            return null; // something went wrong (e.g. tie or missing team)
        }

        // Simulate player box scores
        simulatePlayerStatsForGame(g, homeScore, awayScore);

        return new int[]{homeScore, awayScore};
    }

    // ===================== Simulate Up To Date =====================

    /**
     * Asks for a cutoff date and simulates all games in this league whose
     * date is <= that cutoff and which do not yet have a result.
     */
    private void simulateUpToDate() {
        String input = JOptionPane.showInputDialog(
                this,
                "Simulate all games up to and including date (YYYY-MM-DD):",
                "Simulate Up To Date",
                JOptionPane.QUESTION_MESSAGE);

        if (input == null || input.isBlank()) {
            return; // user cancelled
        }

        LocalDate cutoff;
        try {
            cutoff = LocalDate.parse(input.trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Please use YYYY-MM-DD.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int simulatedCount = 0;
        for (Game g : league.getGames()) {
            if (!g.hasResult() && !g.getDate().isAfter(cutoff)) {
                int[] scores = simulateSingleGame(g);
                if (scores != null) {
                    simulatedCount++;
                }
            }
        }

        JOptionPane.showMessageDialog(this,
                "Simulated " + simulatedCount +
                        " game(s) on or before " + cutoff + ".",
                "Simulation Complete",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ===================== League / Team Updates =====================

    /**
     * Applies result to the Game and updates team W/L records.
     * Returns false if result is invalid (e.g. tie or missing teams).
     */
    private boolean applyResultToLeague(Game g, int homeScore, int awayScore, boolean simulated) {
        String homeName = g.getHomeTeam();
        String awayName = g.getAwayTeam();

        Team homeTeam = league.getTeams().get(homeName);
        Team awayTeam = league.getTeams().get(awayName);

        if (homeTeam == null || awayTeam == null) {
            JOptionPane.showMessageDialog(this,
                    "One or both teams not found in league.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (homeScore == awayScore) {
            JOptionPane.showMessageDialog(this,
                    "Tie games are not allowed. Adjust the scores.",
                    "Invalid Result",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        String winner = (homeScore > awayScore) ? homeName : awayName;

        // Update Game model
        g.setResult(winner, homeScore, awayScore);

        // Update team records (no undo of previous result here)
        if (winner.equals(homeName)) {
            homeTeam.addWin();
            awayTeam.addLoss();
        } else {
            awayTeam.addWin();
            homeTeam.addLoss();
        }

        return true;
    }

    // ===================== Player Stat Simulation =====================

    private void simulatePlayerStatsForGame(Game g, int homeScore, int awayScore) {
        if (statsController == null) {
            System.err.println("GameStatsController is null – cannot simulate player stats.");
            return;
        }

        GameStats gameStats = statsController.getOrCreateGameStats(g);

        Team homeTeam = league.getTeams().get(g.getHomeTeam());
        Team awayTeam = league.getTeams().get(g.getAwayTeam());

        if (homeTeam == null || awayTeam == null) {
            System.err.println("Teams not found for game when simulating stats: " + g);
            return;
        }

        java.util.List<Player> homePlayers = new java.util.ArrayList<>(homeTeam.getPlayers());
        java.util.List<Player> awayPlayers = new java.util.ArrayList<>(awayTeam.getPlayers());

        if (homePlayers.isEmpty() && awayPlayers.isEmpty()) {
            System.err.println("No players found to simulate stats for.");
            return;
        }

        simulateTeamPlayerStats(gameStats, g.getHomeTeam(), homePlayers, homeScore);
        simulateTeamPlayerStats(gameStats, g.getAwayTeam(), awayPlayers, awayScore);
    }

    private void simulateTeamPlayerStats(GameStats gameStats,
                                         String teamName,
                                         java.util.List<Player> players,
                                         int teamPoints) {
        if (players == null || players.isEmpty()) return;

        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // Assign random "weights" to decide who scores more
        double[] weights = new double[players.size()];
        double sum = 0.0;
        for (int i = 0; i < players.size(); i++) {
            double w = 0.5 + rnd.nextDouble(); // 0.5–1.5
            weights[i] = w;
            sum += w;
        }

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            PlayerGameStats s = gameStats.getOrCreatePlayerStats(p, teamName);

            double share = weights[i] / sum;
            int pointsTarget = (int) Math.round(teamPoints * share);

            // Random shot profile
            int threeAtt = rnd.nextInt(0, 8); // 0–7 threes
            int threeMade = (int) Math.round(threeAtt * (0.30 + rnd.nextDouble() * 0.15));
            if (threeMade > threeAtt) threeMade = threeAtt;

            int twoAtt = rnd.nextInt(2, 16); // 2–15 twos
            int twoMade = (int) Math.round(twoAtt * (0.40 + rnd.nextDouble() * 0.20));
            if (twoMade > twoAtt) twoMade = twoAtt;

            int ftAtt = rnd.nextInt(0, 7); // 0–6 FTs
            int ftMade = (int) Math.round(ftAtt * (0.65 + rnd.nextDouble() * 0.20));
            if (ftMade > ftAtt) ftMade = ftAtt;

            int points = 2 * twoMade + 3 * threeMade + ftMade;

            // If far under target, bump with extra FTs a bit
            if (points < pointsTarget - 4) {
                int extra = Math.min(pointsTarget - points, 4);
                ftAtt += extra;
                ftMade += extra;
                points += extra;
            }

            int assists = rnd.nextInt(0, 8); // 0–7 assists

            int fouls = rnd.nextInt(0, 6);   // 0–5
            if (rnd.nextDouble() < 0.05) {   // small chance of foul out
                fouls = 6;
            }

            // Save into stats
            s.setFreeThrowStats(ftAtt, ftMade);
            s.setTwoPointStats(twoAtt, twoMade);
            s.setThreePointStats(threeAtt, threeMade);
            s.setAssists(assists);
            s.setFouls(fouls);
        }
        // Note: total sum of player points won't be perfectly equal to teamPoints,
        // but that's fine for a simulation.
    }
}
