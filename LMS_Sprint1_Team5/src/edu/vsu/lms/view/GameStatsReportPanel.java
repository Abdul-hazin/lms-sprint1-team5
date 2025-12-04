package edu.vsu.lms.view;

import edu.vsu.lms.controller.GameStatsController;
import edu.vsu.lms.model.Game;
import edu.vsu.lms.model.PlayerGameStats;
import edu.vsu.lms.persistence.AppState;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GameStatsReportPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Game game;
    private final GameStatsController statsController;

    private JLabel lblSummary;
    private JLabel lblWinner;
    private JLabel lblLoser;

    private JTable winnerTable;
    private JTable loserTable;

    private DefaultTableModel winnerModel;
    private DefaultTableModel loserModel;

    public GameStatsReportPanel(Game game, GameStatsController statsController) {
        this.game = game;
        this.statsController = statsController;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----- TOP LABELS -----
        JPanel top = new JPanel(new GridLayout(0, 1, 4, 4));

        lblSummary = new JLabel("Game Stats Summary");
        lblSummary.setFont(lblSummary.getFont().deriveFont(Font.BOLD, 16f));

        lblWinner = new JLabel("Winner: -");
        lblLoser = new JLabel("Loser: -");

        top.add(lblSummary);
        top.add(lblWinner);
        top.add(lblLoser);

        add(top, BorderLayout.NORTH);

        // ----- TABLE MODELS -----
        String[] cols = {
                "Jersey", "First", "Last", "Pos",
                "FT Att", "FT Made", "FT %",
                "2P Att", "2P Made", "2P %",
                "3P Att", "3P Made", "3P %",
                "Points", "Assists", "Fouls", "Fouled Out"
        };

        winnerModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        loserModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        winnerTable = new JTable(winnerModel);
        loserTable = new JTable(loserModel);

        // ----- PANELS WITH TITLES -----
        JPanel winnerPanel = new JPanel(new BorderLayout());
        winnerPanel.setBorder(new TitledBorder("Winning Team Players"));
        winnerPanel.add(new JScrollPane(winnerTable), BorderLayout.CENTER);

        JPanel loserPanel = new JPanel(new BorderLayout());
        loserPanel.setBorder(new TitledBorder("Losing Team Players"));
        loserPanel.add(new JScrollPane(loserTable), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                winnerPanel, loserPanel);
        split.setResizeWeight(0.5);

        add(split, BorderLayout.CENTER);
    }

    private void loadData() {
        if (game == null) return;

        // ✔ Use YOUR Game.java: Strings, not Team objects
        String home = game.getHomeTeam();
        String away = game.getAwayTeam();

        int homePts = statsController.getTeamTotalPoints(game, home);
        int awayPts = statsController.getTeamTotalPoints(game, away);

        lblSummary.setText(home + " vs " + away);

        String winnerName;
        String loserName;
        int winnerPts;
        int loserPts;

        if (homePts > awayPts) {
            winnerName = home;
            winnerPts = homePts;
            loserName = away;
            loserPts = awayPts;
        } else if (awayPts > homePts) {
            winnerName = away;
            winnerPts = awayPts;
            loserName = home;
            loserPts = homePts;
        } else {
            winnerName = home + " (tie)";
            loserName = away + " (tie)";
            winnerPts = homePts;
            loserPts = awayPts;
        }

        lblWinner.setText("Winner: " + winnerName + " — " + winnerPts + " points");
        lblLoser.setText("Loser: " + loserName + " — " + loserPts + " points");

        // Fill tables
        fillTeamTable(winnerModel, winnerName);
        fillTeamTable(loserModel, loserName);
    }

    private void fillTeamTable(DefaultTableModel model, String teamName) {
        model.setRowCount(0); // clear

        // Remove "(tie)" if added
        String clean = teamName.replace(" (tie)", "");

        List<PlayerGameStats> list = statsController.getSortedStatsForTeam(game, clean);
        for (PlayerGameStats s : list) {
            var p = s.getPlayer();
            model.addRow(new Object[]{
                    p.getNumber(),
                    p.getFirstName(),
                    p.getLastName(),
                    p.getPosition(),
                    s.getFreeThrowsAttempted(), s.getFreeThrowsMade(), fmt(s.getFreeThrowPercent()),
                    s.getTwoPointersAttempted(), s.getTwoPointersMade(), fmt(s.getTwoPointPercent()),
                    s.getThreePointersAttempted(), s.getThreePointersMade(), fmt(s.getThreePointPercent()),
                    s.getTotalPoints(),
                    s.getAssists(),
                    s.getFouls(),
                    s.isFouledOut() ? "Yes" : "No"
            });
        }
    }

    private String fmt(double pct) {
        return String.format("%.1f%%", pct);
    }
}
