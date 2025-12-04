package edu.vsu.lms.view;

import edu.vsu.lms.controller.GameStatsController;
import edu.vsu.lms.model.*;
import edu.vsu.lms.persistence.AppState;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * PowerRankingsPanel
 * ------------------
 * One row per team in a league:
 *  Name, Games, Wins, Losses, %Win, pts, asst, foul, F%, 2%, 3%
 *
 * pts / asst / foul are per-game averages over all games.
 * Accuracy columns are season-long percentages.
 *
 * Rows ordered by: wins desc, %win desc, team name.
 */
public class PowerRankingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String leagueName;
    private final League league;
    private final GameStatsController statsController;

    private JTable table;

    public PowerRankingsPanel(String leagueName, GameStatsController statsController) {
        this.leagueName = leagueName;

        AppState state = AppState.getInstance();
        this.league = state.getLeagues().get(leagueName);
        if (league == null) {
            throw new IllegalArgumentException("League not found: " + leagueName);
        }

        // fall back to shared controller if null
        this.statsController = (statsController != null)
                ? statsController
                : state.getGameStatsController();

        initUI();
    }

    // ---------- UI ----------

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Power Rankings — " + leagueName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel subtitle = new JLabel(
                "pts / asst / foul = per-game averages · Ordered by wins, then %Win");
        subtitle.setFont(subtitle.getFont().deriveFont(Font.ITALIC, 11f));

        JPanel header = new JPanel(new BorderLayout());
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        PowerTableModel model = buildModel();
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true); // allow user to resort if they want

        // some column sizing / alignment
        table.getColumnModel().getColumn(0).setPreferredWidth(180); // team name

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);

        for (int c = 1; c < model.getColumnCount(); c++) {
            table.getColumnModel().getColumn(c).setCellRenderer(right);
        }

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    // ---------- MODEL BUILD ----------

    private static class TeamRow {
        String name;
        int games, wins, losses;
        double winPct;       // 0–100
        double ptsPerGame;
        double astPerGame;
        double foulPerGame;
        double ftPct;        // 0–100
        double twoPct;
        double threePct;
    }

    private PowerTableModel buildModel() {
        List<TeamRow> rows = new ArrayList<>();

        // aggregate stats per team
        for (Team team : league.getTeams().values()) {
            TeamRow r = new TeamRow();
            r.name   = team.getName();
            r.wins   = team.getWins();
            r.losses = team.getLosses();
            r.games  = r.wins + r.losses;
            r.winPct = (r.games == 0) ? 0.0 : (r.wins * 100.0 / r.games);

            int ftAtt = 0, ftMade = 0;
            int twoAtt = 0, twoMade = 0;
            int threeAtt = 0, threeMade = 0;
            int assists = 0, fouls = 0;

            for (Game g : league.getGames()) {
                boolean playsInGame = r.name.equals(g.getHomeTeam()) ||
                                      r.name.equals(g.getAwayTeam());
                if (!playsInGame) continue;

                GameStats gs = statsController.getOrCreateGameStats(g);

                for (Player p : team.getPlayers()) {
                    PlayerGameStats s = gs.getOrCreatePlayerStats(p, r.name);

                    ftAtt    += s.getFreeThrowsAttempted();
                    ftMade   += s.getFreeThrowsMade();
                    twoAtt   += s.getTwoPointersAttempted();
                    twoMade  += s.getTwoPointersMade();
                    threeAtt += s.getThreePointersAttempted();
                    threeMade+= s.getThreePointersMade();
                    assists  += s.getAssists();
                    fouls    += s.getFouls();
                }
            }

            double games = (r.games == 0) ? 1.0 : r.games;
            int totalPoints = ftMade + 2 * twoMade + 3 * threeMade;

            r.ptsPerGame   = totalPoints / games;
            r.astPerGame   = assists / games;
            r.foulPerGame  = fouls / games;

            r.ftPct    = pct(ftMade,   ftAtt);
            r.twoPct   = pct(twoMade,  twoAtt);
            r.threePct = pct(threeMade,threeAtt);

            rows.add(r);
        }

        // sort: wins desc, %win desc, name asc
        rows.sort(Comparator
                .comparingInt((TeamRow r) -> r.wins).reversed()
                .thenComparingDouble((TeamRow r) -> r.winPct).reversed()
                .thenComparing(r -> r.name, String.CASE_INSENSITIVE_ORDER));

        return new PowerTableModel(rows);
    }

    private static double pct(int made, int att) {
        if (att <= 0) return 0.0;
        return made * 100.0 / att;
    }

    // ---------- TABLE MODEL ----------

    private static class PowerTableModel extends AbstractTableModel {
        private final String[] cols = {
                "Name", "Games", "Wins", "Losses", "%Win",
                "pts", "asst", "foul", "F%", "2%", "3%"
        };

        private final List<TeamRow> rows;

        PowerTableModel(List<TeamRow> rows) {
            this.rows = rows;
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            TeamRow r = rows.get(rowIndex);
            switch (columnIndex) {
                case 0: return r.name;
                case 1: return r.games;
                case 2: return r.wins;
                case 3: return r.losses;
                case 4: return String.format("%.0f%%", r.winPct);
                case 5: return String.format("%.1f", r.ptsPerGame);
                case 6: return String.format("%.1f", r.astPerGame);
                case 7: return String.format("%.1f", r.foulPerGame);
                case 8: return String.format("%.1f%%", r.ftPct);
                case 9: return String.format("%.1f%%", r.twoPct);
                case 10:return String.format("%.1f%%", r.threePct);
            }
            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            // everything rendered as String for consistent formatting
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;  // read-only table
        }
    }
}
