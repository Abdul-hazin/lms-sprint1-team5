package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import edu.vsu.lms.controller.LeagueController;
import edu.vsu.lms.persistence.AppState;

public class LeagueOfficialPanel extends JPanel {
    private final LeagueController ctrl = new LeagueController();
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private final JComboBox<String> leagueCombo = new JComboBox<>();

    // 👇 callback to MainFrame
    private final Runnable onLogout;

    public LeagueOfficialPanel(Runnable onLogout) {
        this.onLogout = onLogout;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 🏟️ Header with league selector
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.add(new JLabel("Select League:"));
        top.add(leagueCombo);
        add(top, BorderLayout.NORTH);

        // 🧾 Team list
        add(new JScrollPane(list), BorderLayout.CENTER);

        // ⚙️ Buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnAddTeam = new JButton("Add Team");
        JButton btnViewPlayers = new JButton("Players");
        JButton btnViewLeaguePlayers = new JButton("View All League Players");
        JButton btnLogout = new JButton("Logout"); // 🆕 logout button

        bottom.add(btnAddTeam);
        bottom.add(btnViewPlayers);
        bottom.add(btnViewLeaguePlayers);
        bottom.add(btnLogout); // 🆕 added
        add(bottom, BorderLayout.SOUTH);

        // 🔗 Actions
        btnAddTeam.addActionListener(e -> onAddTeam());
        btnViewPlayers.addActionListener(e -> onViewTeamPlayers());
        btnViewLeaguePlayers.addActionListener(e -> onViewLeaguePlayers());
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && onLogout != null) {
                onLogout.run();
            }
        });

        leagueCombo.addActionListener(e -> refresh());

        // 🧠 Load all leagues
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
}