package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import edu.vsu.lms.controller.LeagueController;
import edu.vsu.lms.persistence.AppState;
import java.util.List;

public class TeamsPanel extends JPanel {
    private final LeagueController ctrl = new LeagueController();
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private final JComboBox<String> leagueCombo = new JComboBox<>();

    public TeamsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 🏟️ Header with league selector
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.add(new JLabel("Select League:"));
        top.add(leagueCombo);
        add(top, BorderLayout.NORTH);

        // Team list
        add(new JScrollPane(list), BorderLayout.CENTER);

        // ⚙️ Bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnAdd = new JButton("Add Team");
        JButton btnRemove = new JButton("Remove Selected");
        JButton btnViewPlayers = new JButton("View Players");
        bottom.add(btnAdd);
        bottom.add(btnRemove);
        bottom.add(btnViewPlayers);
        add(bottom, BorderLayout.SOUTH);

        // 🔗 Event listeners
        btnAdd.addActionListener(e -> onAddTeam());
        btnRemove.addActionListener(e -> onRemoveSelected());
        btnViewPlayers.addActionListener(e -> onViewPlayers());
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
        String league = (String) leagueCombo.getSelectedItem();
        if (league == null) return;

        String name = JOptionPane.showInputDialog(this, "Enter team name:");
        if (name == null || name.trim().isEmpty()) return;

        boolean ok = ctrl.addTeam(league, name.trim());
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Duplicate or invalid team name.");
        }
        refresh();
    }

    private void onRemoveSelected() {
        String league = (String) leagueCombo.getSelectedItem();
        if (league == null) return;

        String team = list.getSelectedValue();
        if (team == null) {
            JOptionPane.showMessageDialog(this, "Select a team first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove team '" + team + "' from " + league + "?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        var state = AppState.getInstance();
        var lg = state.getLeagues().get(league);
        if (lg != null && lg.getTeams().containsKey(team)) {
            lg.getTeams().remove(team);
            state.save();
            refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to remove team.");
        }
    }

    private void onViewPlayers() {
        String league = (String) leagueCombo.getSelectedItem();
        if (league == null) {
            JOptionPane.showMessageDialog(this, "Select a league first.");
            return;
        }

        String team = list.getSelectedValue();
        if (team == null) {
            JOptionPane.showMessageDialog(this, "Select a team first.");
            return;
        }

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Players in " + team, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setContentPane(new PlayersPanel(league, team));
        dlg.pack();
        dlg.setSize(450, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void refresh() {
        model.clear();
        String league = (String) leagueCombo.getSelectedItem();
        if (league == null) return;

        List<String> teams = ctrl.listTeams(league);
        teams.sort(String::compareToIgnoreCase);
        for (String t : teams) model.addElement(t);
    }
}
