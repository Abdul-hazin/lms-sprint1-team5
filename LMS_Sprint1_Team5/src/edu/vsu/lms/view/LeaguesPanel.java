package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import edu.vsu.lms.controller.LeagueController;

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

        // Footer buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnAdd = new JButton("Add League");
        JButton btnDelete = new JButton("Delete");
        btns.add(btnAdd);
        btns.add(btnDelete);
        add(btns, BorderLayout.SOUTH);

        // Actions
        btnAdd.addActionListener(e -> onAddLeague());
        btnDelete.addActionListener(e -> onDeleteLeague());

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
        int idx = list.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select a league first.");
            return;
        }
        String leagueName = model.get(idx);

        // Try safe delete (blocked if league has teams)
        boolean deleted = ctrl.deleteLeague(leagueName);
        if (!deleted) {
            // Either it doesn't exist or it still has teams; offer “force delete”
            int choice = JOptionPane.showConfirmDialog(
                this,
                "This league may contain teams. Delete the league and ALL its teams?",
                "Confirm Delete",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.OK_OPTION) {
                deleted = ctrl.deleteLeague(leagueName, true); // cascade delete
            }
        }

        if (!deleted) {
            JOptionPane.showMessageDialog(this, "Delete failed.");
        } else {
            refresh();
        }
    }

    private void refresh() {
        model.clear();
        for (String leagueName : ctrl.listLeagues()) {
            model.addElement(leagueName);
        }
    }
}