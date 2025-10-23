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

        JLabel header = new JLabel("Leagues");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        add(header, BorderLayout.NORTH);

        add(new JScrollPane(list), BorderLayout.CENTER);

        JButton add = new JButton("Add League");
        add.addActionListener(e -> onAddLeague());
        add(add, BorderLayout.SOUTH);

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

    private void refresh() {
        model.clear();
        for (String leagueName : ctrl.listLeagues()) {
            model.addElement(leagueName);
        }
    }
}

