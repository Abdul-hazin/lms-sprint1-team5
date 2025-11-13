package edu.vsu.lms.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import edu.vsu.lms.model.*;
import edu.vsu.lms.persistence.AppState;

public class LeaguePlayersPanel extends JPanel {
    private final String leagueName;
    private final JTable table;
    private final DefaultTableModel model;

    public LeaguePlayersPanel(String leagueName) {
        this.leagueName = leagueName;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel("All Players in League: " + leagueName);
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        add(header, BorderLayout.NORTH);

        // Table columns
        String[] columns = {"Team", "First Name", "Last Name", "Jersey #"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setEnabled(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    private void refresh() {
        model.setRowCount(0);
        var state = AppState.getInstance();
        var league = state.getLeagues().get(leagueName);
        if (league == null) return;

        List<PlayerRow> allPlayers = new ArrayList<>();
        for (Team t : league.getTeams().values()) {
            for (Player p : t.getPlayers()) {
                allPlayers.add(new PlayerRow(t.getName(), p.getFirstName(), p.getLastName(), p.getNumber()));
            }
        }

        // sort by last name, then first name, then jersey #
        allPlayers.sort(Comparator
                .comparing(PlayerRow::last)
                .thenComparing(PlayerRow::first)
                .thenComparingInt(PlayerRow::num));

        for (PlayerRow r : allPlayers) {
            model.addRow(new Object[]{r.team, r.first, r.last, r.num});
        }
    }

    // helper record-like class
    private static class PlayerRow {
        final String team;
        final String first;
        final String last;
        final int num;

        PlayerRow(String team, String first, String last, int num) {
            this.team = team;
            this.first = first;
            this.last = last;
            this.num = num;
        }

        String team() { return team; }
        String first() { return first; }
        String last() { return last; }
        int num() { return num; }
    }
}
