package edu.vsu.lms.view;

import edu.vsu.lms.controller.LeagueController;
import edu.vsu.lms.controller.LeagueController.Standing;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Simple panel to display league standings (US 16).
 */
public class LeagueStandingsPanel extends JPanel {

    private final String leagueName;
    private final LeagueController leagueController = new LeagueController();
    private final DefaultTableModel model;

    public LeagueStandingsPanel(String leagueName) {
        this.leagueName = leagueName;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel title = new JLabel("Standings — " + leagueName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(
                new Object[]{"Team", "Wins", "Losses", "Win %"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // read-only
            }
        };

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom buttons
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadStandings());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(refreshBtn);
        add(bottom, BorderLayout.SOUTH);

        // Initial load
        loadStandings();
    }

    private void loadStandings() {
        model.setRowCount(0);
        List<Standing> standings = leagueController.getLeagueStandings(leagueName);
        for (Standing s : standings) {
            model.addRow(new Object[]{
                    s.teamName,
                    s.wins,
                    s.losses,
                    String.format("%.3f", s.getWinPct())
            });
        }
    }
}
