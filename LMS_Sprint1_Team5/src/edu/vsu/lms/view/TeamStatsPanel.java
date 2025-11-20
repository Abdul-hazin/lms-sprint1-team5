package edu.vsu.lms.view;

import edu.vsu.lms.controller.GameStatsController;
import edu.vsu.lms.model.*;
import edu.vsu.lms.persistence.AppState;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * TeamStatsPanel
 * --------------
 * "View Team Stats" user story.
 *
 * Shows per-game averages for the team and each player
 * across all games in the selected league.
 */
public class TeamStatsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String leagueName;
    private final League league;
    private final GameStatsController statsController;

    private final JComboBox<String> teamCombo = new JComboBox<>();
    private final JTextArea statsArea = new JTextArea();

    public TeamStatsPanel(String leagueName, GameStatsController statsController) {
        this.leagueName = leagueName;

        AppState state = AppState.getInstance();
        this.league = state.getLeagues().get(leagueName);
        if (league == null) {
            throw new IllegalArgumentException("League not found: " + leagueName);
        }

        // If null, fall back to shared controller
        this.statsController = (statsController != null)
                ? statsController
                : state.getGameStatsController();

        initUI();
        loadTeams();
    }

    // ---------- UI ----------

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Team Stats — " + leagueName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        // left side: team dropdown
        JPanel left = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 0;
        left.add(new JLabel("Team:"), gc);

        gc.gridx = 1;
        teamCombo.setPreferredSize(new Dimension(220, teamCombo.getPreferredSize().height));
        left.add(teamCombo, gc);

        add(left, BorderLayout.WEST);

        // center: text table
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(statsArea), BorderLayout.CENTER);

        teamCombo.addActionListener(e -> recomputeStats());
    }

    private void loadTeams() {
        teamCombo.removeAllItems();
        List<String> names = new ArrayList<>(league.getTeams().keySet());
        names.sort(String.CASE_INSENSITIVE_ORDER);

        for (String n : names) {
            teamCombo.addItem(n);
        }

        if (teamCombo.getItemCount() > 0) {
            teamCombo.setSelectedIndex(0);
            recomputeStats();
        } else {
            statsArea.setText("No teams in this league.");
        }
    }

    // ---------- COMPUTATION ----------

    private static class PlayerAgg {
        String name;
        int gamesPlayed;
        int ftAtt, ftMade;
        int twoAtt, twoMade;
        int threeAtt, threeMade;
        int assists, fouls;

        int totalPoints() {
            return ftMade + 2 * twoMade + 3 * threeMade;
        }
    }

    /** Rebuilds the stats table for the selected team. */
    private void recomputeStats() {
        String teamName = (String) teamCombo.getSelectedItem();
        if (teamName == null) {
            statsArea.setText("");
            return;
        }

        Team team = league.getTeams().get(teamName);
        if (team == null) {
            statsArea.setText("Team not found: " + teamName);
            return;
        }

        // --- per-player aggregation ---
        List<Player> players = new ArrayList<>(team.getPlayers());
        players.sort(Comparator
                .comparing(Player::getLastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Player::getFirstName, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(Player::getNumber));

        List<PlayerAgg> rows = new ArrayList<>();
        for (Player p : players) {
            PlayerAgg agg = new PlayerAgg();
            agg.name = p.getFirstName() + " " + p.getLastName();

            // loop all games in league; only ones where this team participates
            for (Game g : league.getGames()) {
                boolean isHome = teamName.equals(g.getHomeTeam());
                boolean isAway = teamName.equals(g.getAwayTeam());
                if (!isHome && !isAway) continue;

                GameStats gs = statsController.getOrCreateGameStats(g);
                PlayerGameStats s = gs.getOrCreatePlayerStats(p, teamName);

                int sum =
                        s.getFreeThrowsAttempted() +
                        s.getTwoPointersAttempted() +
                        s.getThreePointersAttempted() +
                        s.getAssists() +
                        s.getFouls();

                // count as "played" only if they did something
                if (sum > 0) {
                    agg.gamesPlayed++;
                    agg.ftAtt    += s.getFreeThrowsAttempted();
                    agg.ftMade   += s.getFreeThrowsMade();
                    agg.twoAtt   += s.getTwoPointersAttempted();
                    agg.twoMade  += s.getTwoPointersMade();
                    agg.threeAtt += s.getThreePointersAttempted();
                    agg.threeMade+= s.getThreePointersMade();
                    agg.assists  += s.getAssists();
                    agg.fouls    += s.getFouls();
                }
            }

            rows.add(agg);
        }

        // --- team totals (sum of players) ---
        int teamGamesWithStats = 0;
        // count games where at least one player has non-zero stats
        for (Game g : league.getGames()) {
            boolean isHome = teamName.equals(g.getHomeTeam());
            boolean isAway = teamName.equals(g.getAwayTeam());
            if (!isHome && !isAway) continue;

            boolean anyStats = false;
            for (Player p : players) {
                GameStats gs = statsController.getOrCreateGameStats(g);
                PlayerGameStats s = gs.getOrCreatePlayerStats(p, teamName);
                int sum =
                        s.getFreeThrowsAttempted() +
                        s.getTwoPointersAttempted() +
                        s.getThreePointersAttempted() +
                        s.getAssists() +
                        s.getFouls();
                if (sum > 0) {
                    anyStats = true;
                    break;
                }
            }
            if (anyStats) teamGamesWithStats++;
        }

        int teamFtAtt = 0, teamFtMade = 0;
        int teamTwoAtt = 0, teamTwoMade = 0;
        int teamThreeAtt = 0, teamThreeMade = 0;
        int teamAst = 0, teamFouls = 0;

        for (PlayerAgg r : rows) {
            teamFtAtt    += r.ftAtt;
            teamFtMade   += r.ftMade;
            teamTwoAtt   += r.twoAtt;
            teamTwoMade  += r.twoMade;
            teamThreeAtt += r.threeAtt;
            teamThreeMade+= r.threeMade;
            teamAst      += r.assists;
            teamFouls    += r.fouls;
        }

        int teamPoints = teamFtMade + 2 * teamTwoMade + 3 * teamThreeMade;

        double gpTeam = (teamGamesWithStats == 0) ? 1.0 : teamGamesWithStats;

        double teamPtsPerGame   = teamPoints / gpTeam;
        double teamAstPerGame   = teamAst / gpTeam;
        double teamFoulsPerGame = teamFouls / gpTeam;

        double teamFtPct   = pct(teamFtMade, teamFtAtt);
        double teamTwoPct  = pct(teamTwoMade, teamTwoAtt);
        double teamThreePct= pct(teamThreeMade, teamThreeAtt);

        // record & win %
        int wins = team.getWins();
        int losses = team.getLosses();
        int recordGames = wins + losses;
        double winPct = (recordGames == 0) ? 0.0 : (wins * 100.0 / recordGames);

        // --- build text table ---
        StringBuilder sb = new StringBuilder();

        // title line like: "Los Angeles Lakers, 5 games, 4-1, 80.0%"
        sb.append(teamName)
          .append(", ")
          .append(recordGames).append(" games, ")
          .append(wins).append("-").append(losses)
          .append(", ")
          .append(String.format("%.1f%%", winPct))
          .append("\n\n");

        // header
        sb.append(String.format("%-25s %6s %7s %7s %7s   %5s %5s %5s%n",
                "Name", "games", "pts", "asst", "foul", "f", "2", "3"));
        sb.append("---------------------------------------------------------------------\n");

        // team row first
        sb.append(String.format("%-25s %6.1f %7.1f %7.1f %7.1f   %5s %5s %5s%n",
                teamName,
                gpTeam,
                teamPtsPerGame,
                teamAstPerGame,
                teamFoulsPerGame,
                fmtPct(teamFtPct),
                fmtPct(teamTwoPct),
                fmtPct(teamThreePct)));

        // player rows
        for (PlayerAgg r : rows) {
            if (r.gamesPlayed == 0) continue; // skip never-played players

            double gp = r.gamesPlayed;
            int pts = r.totalPoints();

            double ptsPerGame   = pts / gp;
            double astPerGame   = r.assists / gp;
            double foulsPerGame = r.fouls / gp;

            double ftPct   = pct(r.ftMade, r.ftAtt);
            double twoPct  = pct(r.twoMade, r.twoAtt);
            double threePct= pct(r.threeMade, r.threeAtt);

            sb.append(String.format("%-25s %6.1f %7.1f %7.1f %7.1f   %5s %5s %5s%n",
                    r.name,
                    gp,
                    ptsPerGame,
                    astPerGame,
                    foulsPerGame,
                    fmtPct(ftPct),
                    fmtPct(twoPct),
                    fmtPct(threePct)));
        }

        if (teamGamesWithStats == 0) {
            sb.append("\n(No recorded stats yet for this team.)");
        }

        statsArea.setText(sb.toString());
        statsArea.setCaretPosition(0);
    }

    private static double pct(int made, int att) {
        if (att <= 0) return 0.0;
        return made * 100.0 / att;
    }

    private static String fmtPct(double p) {
        return String.format("%.1f%%", p);
    }
}
