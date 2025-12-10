package edu.vsu.lms.view;

import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Team;
import edu.vsu.lms.persistence.AppState;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * LeagueStandingsPanel
 * --------------------
 * View League Standings
 *
 * Displays team name, wins, losses, percent wins,
 * ordered by wins (descending), then team name (ascending).
 */
public class LeagueStandingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String leagueName;
    private final JTable table;
    private final DefaultTableModel model;

    public LeagueStandingsPanel(String leagueName) {
        this.leagueName = leagueName;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("League Standings — " + leagueName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Place", "Team", "Wins", "Losses", "Win %"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // read-only table
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2 || columnIndex == 3) return Integer.class;
                if (columnIndex == 4) return Double.class;
                return String.class;
            }
        };

        table = new JTable(model);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadData());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnRefresh);
        add(bottom, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);

        League league = AppState.getInstance().getLeagues().get(leagueName);
        if (league == null) {
            JOptionPane.showMessageDialog(this,
                    "League \"" + leagueName + "\" not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Map<String, Team> teamsMap = league.getTeams();
        List<Team> teams = new ArrayList<>(teamsMap.values());

        // sort by wins DESC, then team name ASC
        teams.sort(Comparator
                .comparingInt(Team::getWins).reversed()
                .thenComparing(Team::getName, String.CASE_INSENSITIVE_ORDER));

        int place = 1;
        for (Team t : teams) {
            int w = t.getWins();
            int l = t.getLosses();
            int total = w + l;
            double pct = (total == 0) ? 0.0 : (w * 1.0 / total) * 100.0;

            model.addRow(new Object[]{
                    place++,
                    t.getName(),
                    w,
                    l,
                    Math.round(pct * 10.0) / 10.0  // one decimal place
            });
        }
    }
}