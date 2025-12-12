package edu.vsu.lms.view;

import edu.vsu.lms.model.*;
import edu.vsu.lms.persistence.AppState;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * BracketRecordResultPanel
 * ------------------------
 * Records and simulates results for PLAYOFF BRACKET GAMES ONLY.
 */
public class BracketRecordResultPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final League league;

    private final JComboBox<Game> gameBox;
    private final JTextField homeScoreField;
    private final JTextField awayScoreField;
    private final JLabel lblHomeTeam;
    private final JLabel lblAwayTeam;

    public BracketRecordResultPanel(String leagueName) {
        AppState state = AppState.getInstance();
        this.league = state.getLeagues().get(leagueName);

        if (league == null) {
            throw new IllegalArgumentException("League not found: " + leagueName);
        }

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Bracket Results — " + league.getName());
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        // ===== CENTER FORM =====
        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: select game
        gc.gridx = 0;
        gc.gridy = 0;
        center.add(new JLabel("Bracket Game:"), gc);

        gameBox = new JComboBox<>();
        loadBracketGames();

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

        // ===== BOTTOM BUTTONS =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnSimulate = new JButton("Simulate Result");
        JButton btnSave     = new JButton("Save Result");
        JButton btnClose    = new JButton("Close");

        bottom.add(btnSimulate);
        bottom.add(btnSave);
        bottom.add(btnClose);

        add(bottom, BorderLayout.SOUTH);

        // ===== LISTENERS =====
        gameBox.addActionListener(e -> updateTeamLabels());

        btnSave.addActionListener(e -> saveResultFromFields());
        btnSimulate.addActionListener(e -> simulateBracketGame());

        btnClose.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JDialog d) {
                d.dispose();
            }
        });

        updateTeamLabels();
    }

    // ==================== BRACKET GAMES ONLY ====================

    private void loadBracketGames() {
        gameBox.removeAllItems();

        if (!league.hasBracket()) return;

        List<Game> allGames = new ArrayList<>();

        for (BracketRound r : league.getBracket().getRounds()) {
            for (Game g : r.getGames()) {
                if (!g.hasResult()) {
                    allGames.add(g);
                }
            }
        }

        for (Game g : allGames) {
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

    // ==================== SAVE RESULT ====================

    private void saveResultFromFields() {
        Game g = getSelectedGame();
        if (g == null) {
            JOptionPane.showMessageDialog(this, "No bracket game selected.");
            return;
        }

        if (g.hasResult()) {
            JOptionPane.showMessageDialog(this,
                    "This bracket game is already finalized and cannot be edited.");
            return;
        }

        int homeScore, awayScore;
        try {
            homeScore = Integer.parseInt(homeScoreField.getText().trim());
            awayScore = Integer.parseInt(awayScoreField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Scores must be whole numbers.");
            return;
        }

        applyResultToLeague(g, homeScore, awayScore);
        JOptionPane.showMessageDialog(this, "Bracket result saved.");
        refreshAfterSave();
    }

    // ==================== SIMULATE RESULT ====================

    private void simulateBracketGame() {
        Game g = getSelectedGame();
        if (g == null) {
            JOptionPane.showMessageDialog(this, "No bracket game selected.");
            return;
        }

        if (g.hasResult()) {
            JOptionPane.showMessageDialog(this,
                    "This bracket game is already finalized and cannot be edited.");
            return;
        }

        int homeScore = ThreadLocalRandom.current().nextInt(80, 121);
        int awayScore = ThreadLocalRandom.current().nextInt(80, 121);

        // avoid ties
        if (homeScore == awayScore) {
            if (ThreadLocalRandom.current().nextBoolean()) homeScore++;
            else awayScore++;
        }

        homeScoreField.setText(String.valueOf(homeScore));
        awayScoreField.setText(String.valueOf(awayScore));

        applyResultToLeague(g, homeScore, awayScore);

        JOptionPane.showMessageDialog(this,
                "Simulated bracket game:\n" +
                        g.getHomeTeam() + " " + homeScore +
                        " - " + awayScore + " " + g.getAwayTeam());

        refreshAfterSave();
    }

    // ==================== APPLY RESULT + AUTO-ADVANCE ====================

    private void applyResultToLeague(Game g, int homeScore, int awayScore) {
        String homeName = g.getHomeTeam();
        String awayName = g.getAwayTeam();

        Team homeTeam = league.getTeams().get(homeName);
        Team awayTeam = league.getTeams().get(awayName);

        if (homeTeam == null || awayTeam == null) {
            JOptionPane.showMessageDialog(this,
                    "One or both teams not found in league.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (homeScore == awayScore) {
            JOptionPane.showMessageDialog(this,
                    "Tie games are not allowed in playoff games.",
                    "Invalid Result",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String winner = (homeScore > awayScore) ? homeName : awayName;

        // Update Game model
        g.setResult(winner, homeScore, awayScore);

        // Update team records
        if (winner.equals(homeName)) {
            homeTeam.addWin();
            awayTeam.addLoss();
        } else {
            awayTeam.addWin();
            homeTeam.addLoss();
        }

        // 🔥 AUTO-ADVANCE WINNER INTO NEXT ROUND
        league.getBracket().advanceWinner(g);
          // 🔥 SAVE updated league + bracket to disk
        AppState.getInstance().save();

        // 🔄 refresh UI (game list + labels)
        refreshAfterSave();
    }

    private void refreshAfterSave() {
        loadBracketGames();
        updateTeamLabels();
    }
}
