package edu.vsu.lms.view;

import edu.vsu.lms.controller.GameStatsController;
import edu.vsu.lms.model.*;
import edu.vsu.lms.persistence.AppState;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * PlayerStatsPanel
 * ----------------
 * "View Player Stats" user story.
 *
 * Shows totals and per-game averages of a player's stats
 * across all games in a league.
 *
 * Roles: League Admin, League Official, Team Official.
 */
public class PlayerStatsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final String leagueName;
    private final League league;
    private final GameStatsController statsController;

    // UI
    private final JComboBox<String> teamCombo = new JComboBox<>();
    private final JComboBox<Player> playerCombo = new JComboBox<>();
    private final JTextArea statsArea = new JTextArea();

    public PlayerStatsPanel(String leagueName, GameStatsController statsController) {
        this.leagueName = leagueName;

        AppState state = AppState.getInstance();
        this.league = state.getLeagues().get(leagueName);
        if (league == null) {
            throw new IllegalArgumentException("League not found: " + leagueName);
        }

        // if caller passes null, use shared one from AppState
        this.statsController = (statsController != null)
                ? statsController
                : state.getGameStatsController();

        initUI();
        loadTeams();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- TOP: Title ----
        JLabel title = new JLabel("Player Stats — " + leagueName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        // ---- NORTH: selection controls (team + player) ----
        JPanel selectors = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        selectors.add(new JLabel("Team:"), gc);

        gc.gridx = 1;
        teamCombo.setPreferredSize(new Dimension(200, teamCombo.getPreferredSize().height));
        selectors.add(teamCombo, gc);

        gc.gridx = 0; gc.gridy = 1;
        selectors.add(new JLabel("Player:"), gc);

        gc.gridx = 1;
        playerCombo.setPreferredSize(new Dimension(200, playerCombo.getPreferredSize().height));
        selectors.add(playerCombo, gc);

        add(selectors, BorderLayout.WEST);

        // ---- CENTER: stats area ----
        statsArea.setEditable(false);
        statsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(statsArea), BorderLayout.CENTER);

        // ---- LISTENERS ----
        teamCombo.addActionListener(e -> loadPlayersForSelectedTeam());
        playerCombo.addActionListener(e -> recomputeStats());
    }

    private void loadTeams() {
        teamCombo.removeAllItems();

        List<String> teamNames = new ArrayList<>(league.getTeams().keySet());
        teamNames.sort(String.CASE_INSENSITIVE_ORDER);

        for (String name : teamNames) {
            teamCombo.addItem(name);
        }

        if (teamCombo.getItemCount() > 0) {
            teamCombo.setSelectedIndex(0);
            loadPlayersForSelectedTeam();
        }
    }

    private void loadPlayersForSelectedTeam() {
        playerCombo.removeAllItems();

        String teamName = (String) teamCombo.getSelectedItem();
        if (teamName == null) {
            statsArea.setText("");
            return;
        }

        Team team = league.getTeams().get(teamName);
        if (team == null) {
            statsArea.setText("No such team: " + teamName);
            return;
        }

        List<Player> players = new ArrayList<>(team.getPlayers());
        players.sort(Comparator
                .comparing(Player::getLastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Player::getFirstName, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(Player::getNumber));

        for (Player p : players) {
            playerCombo.addItem(p);
        }

        if (playerCombo.getItemCount() > 0) {
            playerCombo.setSelectedIndex(0);
            recomputeStats();
        } else {
            statsArea.setText("No players on this team.");
        }
    }

    /** Recompute totals + averages for currently selected player. */
    private void recomputeStats() {
        String teamName = (String) teamCombo.getSelectedItem();
        Player player = (Player) playerCombo.getSelectedItem();
        if (teamName == null || player == null) {
            statsArea.setText("");
            return;
        }

        // Accumulators
        int gamesPlayed = 0;

        int ftAtt = 0, ftMade = 0;
        int twoAtt = 0, twoMade = 0;
        int threeAtt = 0, threeMade = 0;
        int assists = 0;
        int fouls = 0;

        for (Game g : league.getGames()) {
            GameStats gs = statsController.getOrCreateGameStats(g);
            PlayerGameStats s = gs.getOrCreatePlayerStats(player, teamName);

            // Treat "all zeros" as didn't play in that game
            boolean played = s.getFreeThrowsAttempted() > 0 ||
                             s.getTwoPointersAttempted() > 0 ||
                             s.getThreePointersAttempted() > 0 ||
                             s.getAssists() > 0 ||
                             s.getFouls() > 0;

            if (!played) continue;

            gamesPlayed++;

            ftAtt    += s.getFreeThrowsAttempted();
            ftMade   += s.getFreeThrowsMade();
            twoAtt   += s.getTwoPointersAttempted();
            twoMade  += s.getTwoPointersMade();
            threeAtt += s.getThreePointersAttempted();
            threeMade+= s.getThreePointersMade();
            assists  += s.getAssists();
            fouls    += s.getFouls();
        }

        int totalPoints = ftMade * 1 + twoMade * 2 + threeMade * 3;

        double g = (gamesPlayed == 0) ? 1.0 : gamesPlayed; // avoid divide-by-zero

        double ptsPerGame     = totalPoints / g;
        double astPerGame     = assists / g;
        double foulsPerGame   = fouls / g;
        double ftPct          = (ftAtt    == 0) ? 0.0 : (ftMade   * 100.0 / ftAtt);
        double twoPct         = (twoAtt   == 0) ? 0.0 : (twoMade  * 100.0 / twoAtt);
        double threePct       = (threeAtt == 0) ? 0.0 : (threeMade* 100.0 / threeAtt);

        StringBuilder sb = new StringBuilder();
        sb.append("Player: ").append(player.toString()).append("\n");
        sb.append("Team:   ").append(teamName).append("\n");
        sb.append("Games with stats: ").append(gamesPlayed).append("\n\n");

        sb.append(String.format("%-20s %8s %10s%n", "STAT", "TOTAL", "PER GAME"));
        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-20s %8d %10.1f%n", "Points", totalPoints, ptsPerGame));
        sb.append(String.format("%-20s %8d %10.1f%n", "Assists", assists, astPerGame));
        sb.append(String.format("%-20s %8d %10.1f%n", "Fouls", fouls, foulsPerGame));
        sb.append("\n");
        sb.append(String.format("%-20s %8d %10d%n", "FT Att", ftAtt, Math.round(ftAtt / g)));
        sb.append(String.format("%-20s %8d %10d%n", "FT Made", ftMade, Math.round(ftMade / g)));
        sb.append(String.format("%-20s %8.1f%%%10s%n", "FT %", ftPct, ""));
        sb.append("\n");
        sb.append(String.format("%-20s %8d %10d%n", "2P Att", twoAtt, Math.round(twoAtt / g)));
        sb.append(String.format("%-20s %8d %10d%n", "2P Made", twoMade, Math.round(twoMade / g)));
        sb.append(String.format("%-20s %8.1f%%%10s%n", "2P %", twoPct, ""));
        sb.append("\n");
        sb.append(String.format("%-20s %8d %10d%n", "3P Att", threeAtt, Math.round(threeAtt / g)));
        sb.append(String.format("%-20s %8d %10d%n", "3P Made", threeMade, Math.round(threeMade / g)));
        sb.append(String.format("%-20s %8.1f%%%10s%n", "3P %", threePct, ""));

        if (gamesPlayed == 0) {
            sb.append("\n\n(No games with recorded stats for this player yet.)");
        }

        statsArea.setText(sb.toString());
        statsArea.setCaretPosition(0);
    }
}
