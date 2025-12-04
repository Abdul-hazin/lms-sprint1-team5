package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.time.LocalDate;

import edu.vsu.lms.controller.GameStatsController;
import edu.vsu.lms.controller.LeagueController;
import edu.vsu.lms.controller.ScheduleController;
import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Game;
import edu.vsu.lms.persistence.AppState;

public class LeagueOfficialPanel extends JPanel {
    private final LeagueController ctrl = new LeagueController();
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private final JComboBox<String> leagueCombo = new JComboBox<>();

    // Existing
    private final GameStatsController gameStatsController =
            AppState.getInstance().getGameStatsController();

    // For bracket scheduling
    private final ScheduleController scheduleController = new ScheduleController();

    private final Runnable onLogout;

    public LeagueOfficialPanel(Runnable onLogout) {
        this.onLogout = onLogout;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // =======================
        // Buttons (create first)
        // =======================
        JButton btnAddTeam = new JButton("Add Team");
        JButton btnViewPlayers = new JButton("Players");
        JButton btnViewLeaguePlayers = new JButton("View All League Players");
        JButton btnLogout = new JButton("Logout");
        JButton btnUpcoming      = new JButton("Upcoming Games");
        JButton btnViewStats     = new JButton("View Game Stats");
        JButton btnEditStats     = new JButton("Edit Game Stats");
        JButton btnStandings     = new JButton("Standings…");
        JButton btnPlayerStats   = new JButton("Player Stats…");
        JButton btnTeamStats     = new JButton("Team Stats…");
        JButton btnPower         = new JButton("Power Rankings");

        // Bracket buttons
        JButton btnScheduleBracket = new JButton("Schedule Playoff Bracket");
        JButton btnViewBracket     = new JButton("View Bracket");
        JButton btnBracketResults  = new JButton("Bracket Results");

        // =======================
        // TOP: league + bracket
        // =======================
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        // Row 1 – League dropdown
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1.add(new JLabel("Select League:"));
        row1.add(leagueCombo);

        // Row 2 – Bracket buttons
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.add(btnScheduleBracket);
        row2.add(btnViewBracket);
        row2.add(btnBracketResults);

        top.add(row1);
        top.add(row2);

        add(top, BorderLayout.NORTH);

        // =======================
        // CENTER: team list
        // =======================
        add(new JScrollPane(list), BorderLayout.CENTER);

        // =======================
        // BOTTOM: other buttons
        // =======================
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.add(btnAddTeam);
        bottom.add(btnViewPlayers);
        bottom.add(btnViewLeaguePlayers);
        bottom.add(btnEditStats);
        bottom.add(btnStandings);
        bottom.add(btnPlayerStats);
        bottom.add(btnTeamStats);
        bottom.add(btnPower);
        bottom.add(btnUpcoming);
        bottom.add(btnViewStats);
        bottom.add(btnLogout);
        add(bottom, BorderLayout.SOUTH);

        // =======================
        // Actions
        // =======================
        btnAddTeam.addActionListener(e -> onAddTeam());
        btnViewPlayers.addActionListener(e -> onViewTeamPlayers());
        btnViewLeaguePlayers.addActionListener(e -> onViewLeaguePlayers());
        btnUpcoming.addActionListener(e -> showUpcomingGamesDialog());
        btnViewStats.addActionListener(e -> showGameStatsReportDialog());
        btnEditStats.addActionListener(e -> showGameStatsEditorDialog());
        btnViewStats.addActionListener(e -> showGameStatsReportDialog());
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && onLogout != null) {
                onLogout.run();
            }
        });

        // US 25 – Schedule Bracket
        btnScheduleBracket.addActionListener(e -> {
            String currentLeague = (String) leagueCombo.getSelectedItem();
            if (currentLeague == null) {
                JOptionPane.showMessageDialog(this, "Select a league first.");
                return;
            }

            LocalDate firstRoundDate = LocalDate.now().plusDays(7);
            String msg = scheduleController.scheduleBracket(currentLeague, firstRoundDate);
            JOptionPane.showMessageDialog(this, msg);
        });

        // US 26 – View Bracket
        btnViewBracket.addActionListener(e -> {
            String currentLeague = (String) leagueCombo.getSelectedItem();
            if (currentLeague == null) {
                JOptionPane.showMessageDialog(this, "Select a league first.");
                return;
            }

            AppState appState = AppState.getInstance();
            League league = appState.getLeagues().get(currentLeague);
            if (league == null || !league.hasBracket()) {
                JOptionPane.showMessageDialog(this,
                        "No bracket has been created for this league.",
                        "No Bracket",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String text = league.getBracket().formatBracket();
            JOptionPane.showMessageDialog(this, text);
        });

        // US 27 – View Bracket Results
        btnBracketResults.addActionListener(e -> {
            String currentLeague = (String) leagueCombo.getSelectedItem();
            if (currentLeague == null) {
                JOptionPane.showMessageDialog(this, "Select a league first.");
                return;
            }

            AppState appState = AppState.getInstance();
            League league = appState.getLeagues().get(currentLeague);
            if (league == null || !league.hasBracket()) {
                JOptionPane.showMessageDialog(this,
                        "No bracket has been created for this league.",
                        "No Bracket",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String text = league.getBracket().formatResults(league);
            JOptionPane.showMessageDialog(this, text);
        });

        leagueCombo.addActionListener(e -> refresh());

        // Load all leagues
        refreshLeagueList();
        refresh();
    }

    private void refreshLeagueList() {
        leagueCombo.removeAllItems();
        var leagues = AppState.getInstance().getLeagues().keySet().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        for (String league : leagues) {
            leagueCombo.addItem(league);
        }
        if (leagueCombo.getItemCount() == 0) {
            String def = AppState.getInstance().getOrInitDefaultLeague();
            leagueCombo.addItem(def);
        }
    }

    private void onAddTeam() {
        String currentLeague = (String) leagueCombo.getSelectedItem();
        if (currentLeague == null) return;

        String name = JOptionPane.showInputDialog(this, "Enter new team name:");
        if (name == null || name.trim().isEmpty()) return;

        boolean ok = ctrl.addTeam(currentLeague, name.trim());
        if (!ok) JOptionPane.showMessageDialog(this, "Duplicate or invalid team name.");
        refresh();
    }

    private void onViewTeamPlayers() {
        String currentLeague = (String) leagueCombo.getSelectedItem();
        if (currentLeague == null) {
            JOptionPane.showMessageDialog(this, "Select a league first.");
            return;
        }

        String team = list.getSelectedValue();
        if (team == null) {
            JOptionPane.showMessageDialog(this, "Select a team first.");
            return;
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Players in " + team,
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setContentPane(new PlayersPanel(currentLeague, team));
        dlg.pack();
        dlg.setSize(450, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void onViewLeaguePlayers() {
        String currentLeague = (String) leagueCombo.getSelectedItem();
        if (currentLeague == null) {
            JOptionPane.showMessageDialog(this, "Select a league first.");
            return;
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "All Players in " + currentLeague,
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setContentPane(new LeaguePlayersPanel(currentLeague));
        dlg.pack();
        dlg.setSize(500, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void showUpcomingGamesDialog() {
        String currentLeague = (String) leagueCombo.getSelectedItem();
        if (currentLeague == null) {
            JOptionPane.showMessageDialog(this, "Select a league first.");
            return;
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Upcoming Games in " + currentLeague,
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setContentPane(new UpcomingGamesPanel(currentLeague));
        dlg.pack();
        dlg.setSize(500, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void refresh() {
        model.clear();
        String currentLeague = (String) leagueCombo.getSelectedItem();
        if (currentLeague == null) return;

        var teams = ctrl.listTeams(currentLeague);
        teams.sort(String::compareToIgnoreCase);
        for (String t : teams) {
            model.addElement(t);
        }
    }

    private void showGameStatsReportDialog() {
        String leagueName = JOptionPane.showInputDialog(this, "Enter League Name:");
        if (leagueName == null || leagueName.isBlank()) return;

        AppState appState = AppState.getInstance();
        League league = appState.getLeagues().get(leagueName);
        if (league == null) {
            JOptionPane.showMessageDialog(this,
                    "League \"" + leagueName + "\" not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (league.getGames().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No games found for league \"" + leagueName + "\".",
                    "No Games",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Game> games = league.getGames();
        Game selected = (Game) JOptionPane.showInputDialog(
                this,
                "Select a game:",
                "Choose Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                games.toArray(),
                games.get(0)
        );

        if (selected == null) return;

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Game Stats Report", Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setContentPane(new GameStatsReportPanel(selected, gameStatsController));
        d.setSize(1000, 600);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showGameStatsEditorDialog() {
        String leagueName = JOptionPane.showInputDialog(this, "Enter League Name:");
        if (leagueName == null || leagueName.isBlank()) return;

        AppState appState = AppState.getInstance();
        League league = appState.getLeagues().get(leagueName);
        if (league == null) {
            JOptionPane.showMessageDialog(this,
                    "League \"" + leagueName + "\" not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (league.getGames().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No games found for league \"" + leagueName + "\".",
                    "No Games",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        java.util.List<Game> games = league.getGames();
        Game selected = (Game) JOptionPane.showInputDialog(
                this,
                "Select a game:",
                "Choose Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                games.toArray(),
                games.get(0)
        );
        if (selected == null) return;

        GameStatsController gsc = appState.getGameStatsController();

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Edit Game Stats", Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setContentPane(new GameStatsEditorPanel(league, selected, gsc));
        d.setSize(900, 600);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
}
