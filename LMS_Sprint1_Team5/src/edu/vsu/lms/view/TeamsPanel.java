package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import edu.vsu.lms.controller.LeagueController;
import edu.vsu.lms.persistence.AppState;

public class TeamsPanel extends JPanel {
    private final LeagueController ctrl = new LeagueController();
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private final String activeLeague;

    public TeamsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ensure we have at least one league and use it
        activeLeague = AppState.getInstance().getOrInitDefaultLeague();

        // header
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.add(new JLabel("League: " + activeLeague));
        add(top, BorderLayout.NORTH);

        // list
        add(new JScrollPane(list), BorderLayout.CENTER);

        // footer buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnAdd = new JButton("Add Team");
        JButton btnRemove = new JButton("Remove Selected");
        bottom.add(btnAdd);
        bottom.add(btnRemove);
        add(bottom, BorderLayout.SOUTH);

        // actions
        btnAdd.addActionListener(e -> onAddTeam());
        btnRemove.addActionListener(e -> onRemoveSelected());

        refresh();
    }

    private void onAddTeam() {
        String name = JOptionPane.showInputDialog(this, "Team name:");
        if (name == null) return; // cancel
        name = name.trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Team name cannot be empty.");
            return;
        }
        boolean ok = ctrl.addTeam(activeLeague, name);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Duplicate or invalid team name.");
        }
        refresh();
    }

    private void onRemoveSelected() {
        int idx = list.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select a team first.");
            return;
        }
        String team = model.get(idx);
        int ok = JOptionPane.showConfirmDialog(this,
                "Remove team: " + team + " ?",
                "Confirm", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        // Optional: implement removeTeam in LeagueController if you want this to work now.
        // For Sprint 1 you can skip removal, or add the method below.
        boolean removed = removeTeam(activeLeague, team);
        if (!removed) JOptionPane.showMessageDialog(this, "Failed to remove team.");
        refresh();
    }

    // Minimal inline remove using controller's state (keeps TeamsPanel self-sufficient for Sprint 1).
    private boolean removeTeam(String leagueName, String teamName) {
        var state = AppState.getInstance();
        var lg = state.getLeagues().get(leagueName);
        if (lg == null || !lg.getTeams().containsKey(teamName)) return false;
        lg.getTeams().remove(teamName);
        state.save();
        return true;
    }

    private void refresh() {
        model.clear();
        for (String t : ctrl.listTeams(activeLeague)) {
            model.addElement(t);
        }
    }
}

