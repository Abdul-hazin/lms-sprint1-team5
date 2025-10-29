package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import edu.vsu.lms.controller.LeagueController;
import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.League;

public class LeaguesPanel extends JPanel {
    private final LeagueController ctrl = new LeagueController();
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);

    public LeaguesPanel() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Header
        JLabel header = new JLabel("Leagues");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        add(header, BorderLayout.NORTH);

        // Center list
        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton add = new JButton("Add League");
        JButton delete = new JButton("Delete Selected"); // 🆕 new delete button
        bottom.add(add);
        bottom.add(delete);
        add(bottom, BorderLayout.SOUTH);

        add.addActionListener(e -> onAddLeague());
        delete.addActionListener(e -> onDeleteLeague()); // 🆕 action

        refresh();
    }

    private void onAddLeague() {
        String name = JOptionPane.showInputDialog(this, "League name:");
        if (name == null || name.isBlank()) return;
        boolean ok = ctrl.createLeague(name.trim());
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Duplicate or invalid league name.");
        }
        refresh();
    }

    private void onDeleteLeague() {
        String selected = list.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a league to delete.");
            return;
        }

        // prevent deleting the default league
        if (selected.equalsIgnoreCase("Default League")) {
            JOptionPane.showMessageDialog(this, "You cannot delete the Default League.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the league '" + selected + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        var state = AppState.getInstance();
        League league = state.getLeagues().remove(selected);
        if (league != null) {
            state.save();
            JOptionPane.showMessageDialog(this, "League '" + selected + "' deleted successfully.");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to delete league.");
        }

        refresh();
    }

    private void refresh() {
        model.clear();
        var leagues = AppState.getInstance().getLeagues().keySet().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        for (String leagueName : leagues) {
            model.addElement(leagueName);
        }
    }
}