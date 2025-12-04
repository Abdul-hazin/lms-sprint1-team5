package edu.vsu.lms.view;

import edu.vsu.lms.controller.GameStatsController;
import edu.vsu.lms.model.*;
import edu.vsu.lms.persistence.AppState;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GameStatsEditorPanel
 * --------------------
 * Used for the "Load Game Stats" user story.
 *
 * Allows a League Admin / League Official to enter stats for
 * each player in a single Game:
 *  - FT attempted / made
 *  - 2P attempted / made
 *  - 3P attempted / made
 *  - Assists
 *  - Fouls (0–6, fouled out)
 */
public class GameStatsEditorPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final League league;
    private final Game game;
    private final GameStatsController statsController;

    // UI components
    private JLabel lblGameInfo;
    private JLabel lblSelectedTeam;
    private JLabel lblSelectedPlayer;
    private JLabel lblFouledOut;

    private JList<Player> homeList;
    private JList<Player> awayList;

    private JSpinner spFtAtt;
    private JSpinner spFtMade;
    private JSpinner sp2Att;
    private JSpinner sp2Made;
    private JSpinner sp3Att;
    private JSpinner sp3Made;
    private JSpinner spAssists;
    private JSpinner spFouls;

    public GameStatsEditorPanel(League league, Game game, GameStatsController statsController) {
    this.league = league;
    this.game = game;

    // ✅ If caller passes null, fall back to AppState shared controller
    GameStatsController fromAppState = AppState.getInstance().getGameStatsController();
    this.statsController = (statsController != null) ? statsController : fromAppState;

    initUI();
    loadPlayers();
}

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== TOP: Game info =====
        JPanel top = new JPanel(new BorderLayout());
        String title = String.format("Enter Stats - %s vs %s (%s)",
                game.getHomeTeam(), game.getAwayTeam(), game.getDate());
        lblGameInfo = new JLabel(title);
        lblGameInfo.setFont(lblGameInfo.getFont().deriveFont(Font.BOLD, 16f));
        top.add(lblGameInfo, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        // ===== CENTER: left = players, right = stat editors =====
        JPanel center = new JPanel(new BorderLayout(10, 10));

        // LEFT side: tabs for Home / Away players
        JTabbedPane tabs = new JTabbedPane();

        homeList = new JList<>(new DefaultListModel<>());
        awayList = new JList<>(new DefaultListModel<>());

        homeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        awayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tabs.addTab("Home: " + game.getHomeTeam(), new JScrollPane(homeList));
        tabs.addTab("Away: " + game.getAwayTeam(), new JScrollPane(awayList));

        // RIGHT side: editor panel
        JPanel editor = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        lblSelectedTeam = new JLabel("Team: -");
        lblSelectedPlayer = new JLabel("Player: -");
        lblFouledOut = new JLabel("Fouled out: No");

        spFtAtt = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        spFtMade = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        sp2Att = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        sp2Made = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        sp3Att = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        sp3Made = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
        spAssists = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
        spFouls = new JSpinner(new SpinnerNumberModel(0, 0, 6, 1));

        int row = 0;

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 2;
        editor.add(lblSelectedTeam, gc);
        row++;

        gc.gridy = row;
        editor.add(lblSelectedPlayer, gc);
        row++;

        gc.gridwidth = 1;

        // FT row
        gc.gridx = 0; gc.gridy = row;
        editor.add(new JLabel("Free Throws (Att / Made):"), gc);
        gc.gridx = 1;
        editor.add(spFtAtt, gc);
        gc.gridx = 2;
        editor.add(spFtMade, gc);
        row++;

        // 2P row
        gc.gridx = 0; gc.gridy = row;
        editor.add(new JLabel("2-Pointers (Att / Made):"), gc);
        gc.gridx = 1;
        editor.add(sp2Att, gc);
        gc.gridx = 2;
        editor.add(sp2Made, gc);
        row++;

        // 3P row
        gc.gridx = 0; gc.gridy = row;
        editor.add(new JLabel("3-Pointers (Att / Made):"), gc);
        gc.gridx = 1;
        editor.add(sp3Att, gc);
        gc.gridx = 2;
        editor.add(sp3Made, gc);
        row++;

        // Assists
        gc.gridx = 0; gc.gridy = row;
        editor.add(new JLabel("Assists:"), gc);
        gc.gridx = 1;
        editor.add(spAssists, gc);
        row++;

        // Fouls
        gc.gridx = 0; gc.gridy = row;
        editor.add(new JLabel("Fouls (0–6):"), gc);
        gc.gridx = 1;
        editor.add(spFouls, gc);
        gc.gridx = 2;
        editor.add(lblFouledOut, gc);
        row++;

        // Buttons
        JButton btnSave = new JButton("Save Stats");
        JButton btnClose = new JButton("Close");

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 1;
        editor.add(btnSave, gc);
        gc.gridx = 1;
        editor.add(btnClose, gc);

        center.add(tabs, BorderLayout.WEST);
        center.add(editor, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        // ===== LISTENERS =====

        homeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (!homeList.isSelectionEmpty()) {
                    awayList.clearSelection();
                    Player p = homeList.getSelectedValue();
                    loadStatsForPlayer(game.getHomeTeam(), p);
                }
            }
        });

        awayList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (!awayList.isSelectionEmpty()) {
                    homeList.clearSelection();
                    Player p = awayList.getSelectedValue();
                    loadStatsForPlayer(game.getAwayTeam(), p);
                }
            }
        });

        spFouls.addChangeListener(e -> updateFoulLabel());

        btnSave.addActionListener(e -> saveCurrentPlayerStats());
        btnClose.addActionListener(e -> {
            // Close parent dialog, if any
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JDialog) {
                w.dispose();
            }
        });
    }

    private void loadPlayers() {
        Team homeTeam = league.getTeams().get(game.getHomeTeam());
        Team awayTeam = league.getTeams().get(game.getAwayTeam());

        DefaultListModel<Player> homeModel = (DefaultListModel<Player>) homeList.getModel();
        DefaultListModel<Player> awayModel = (DefaultListModel<Player>) awayList.getModel();

        homeModel.clear();
        awayModel.clear();

        if (homeTeam != null) {
            for (Player p : sortPlayers(homeTeam.getPlayers())) {
                homeModel.addElement(p);
            }
        }
        if (awayTeam != null) {
            for (Player p : sortPlayers(awayTeam.getPlayers())) {
                awayModel.addElement(p);
            }
        }
    }

    private List<Player> sortPlayers(Iterable<Player> players) {
        List<Player> list = new ArrayList<>();
        for (Player p : players) list.add(p);

        list.sort(Comparator
                .comparing(Player::getLastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Player::getFirstName, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(Player::getNumber));
        return list;
    }

    private void loadStatsForPlayer(String teamName, Player p) {
        if (p == null) return;

        lblSelectedTeam.setText("Team: " + teamName);
        lblSelectedPlayer.setText("Player: " + p.toString());

        GameStats gs = statsController.getOrCreateGameStats(game);
        PlayerGameStats s = gs.getOrCreatePlayerStats(p, teamName);

        spFtAtt.setValue(s.getFreeThrowsAttempted());
        spFtMade.setValue(s.getFreeThrowsMade());
        sp2Att.setValue(s.getTwoPointersAttempted());
        sp2Made.setValue(s.getTwoPointersMade());
        sp3Att.setValue(s.getThreePointersAttempted());
        sp3Made.setValue(s.getThreePointersMade());
        spAssists.setValue(s.getAssists());
        spFouls.setValue(s.getFouls());

        updateFoulLabel();
    }

    private void updateFoulLabel() {
        int fouls = (Integer) spFouls.getValue();
        lblFouledOut.setText("Fouled out: " + (fouls >= 6 ? "Yes" : "No"));
    }

    private void saveCurrentPlayerStats() {
        // Determine which player & team is selected
        Player p = null;
        String teamName = null;

        if (!homeList.isSelectionEmpty()) {
            p = homeList.getSelectedValue();
            teamName = game.getHomeTeam();
        } else if (!awayList.isSelectionEmpty()) {
            p = awayList.getSelectedValue();
            teamName = game.getAwayTeam();
        }

        if (p == null || teamName == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a player first.",
                    "No Player Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int ftAtt = (Integer) spFtAtt.getValue();
        int ftMade = (Integer) spFtMade.getValue();
        int twoAtt = (Integer) sp2Att.getValue();
        int twoMade = (Integer) sp2Made.getValue();
        int threeAtt = (Integer) sp3Att.getValue();
        int threeMade = (Integer) sp3Made.getValue();
        int assists = (Integer) spAssists.getValue();
        int fouls = (Integer) spFouls.getValue();

        // basic checks
        if (ftMade > ftAtt || twoMade > twoAtt || threeMade > threeAtt) {
            JOptionPane.showMessageDialog(this,
                    "Made shots cannot exceed attempts.",
                    "Invalid Values",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Save into stats model
        GameStats gs = statsController.getOrCreateGameStats(game);
        PlayerGameStats s = gs.getOrCreatePlayerStats(p, teamName);

        try {
            s.setFreeThrowStats(ftAtt, ftMade);
            s.setTwoPointStats(twoAtt, twoMade);
            s.setThreePointStats(threeAtt, threeMade);
            s.setAssists(assists);
            s.setFouls(fouls);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving stats: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        updateFoulLabel();

        JOptionPane.showMessageDialog(this,
                "Stats saved for " + p.toString(),
                "Saved",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
