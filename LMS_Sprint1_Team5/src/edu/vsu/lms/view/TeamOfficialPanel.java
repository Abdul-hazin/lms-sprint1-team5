package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

import edu.vsu.lms.persistence.AppState;

public class TeamOfficialPanel extends JPanel {

    private final AppState appState = AppState.getInstance();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Runnable onLogout;

    // Top bar
    private JComboBox<String> leagueBox;

    // Cards (all view-only)
    private TeamsPanel teamsPanel;                 // read-only teams (opens Players dialog)
    private LeaguePlayersPanel leaguePlayersPanel; // all players in league
    private LeaguesPanel leaguesPanel;             // read-only leagues list (optional)
    private SchedulePanel schedulePanel;           // NEW: view-only schedule

    public TeamOfficialPanel(Runnable onLogout) {
        this.onLogout = onLogout;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // ---- Top bar ----
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Team Official", SwingConstants.LEFT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightTop.add(new JLabel("League:"));
        leagueBox = new JComboBox<>();
        leagueBox.setPrototypeDisplayValue("Select a league with a long name");
        rightTop.add(leagueBox);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> onLogout.run());
        rightTop.add(logoutBtn);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(rightTop, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ---- Sidebar (nav) ----
        JPanel sidebar = new JPanel(new GridLayout(5, 1, 10, 10));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JButton btnLeagues = new JButton("View Leagues");
        JButton btnTeams = new JButton("View Teams");
        JButton btnLeaguePlayers = new JButton("View League Players");
        JButton btnSchedule = new JButton("View Schedule"); // NEW

        sidebar.add(btnLeagues);
        sidebar.add(btnTeams);
        sidebar.add(btnLeaguePlayers);
        sidebar.add(btnSchedule);
        add(sidebar, BorderLayout.WEST);

        // ---- Content ----
        add(contentPanel, BorderLayout.CENTER);

        String defaultLeague = appState.getOrInitDefaultLeague();
        populateLeagueBox(defaultLeague);

        // Panels (read-only)
        teamsPanel = new TeamsPanel(true, true);  // hides internal league controls (embedded)
        teamsPanel.loadTeamsForLeague(defaultLeague);
        contentPanel.add(teamsPanel, "TEAMS");

// League-wide players view
        leaguePlayersPanel = new LeaguePlayersPanel(defaultLeague);
        contentPanel.add(leaguePlayersPanel, "LEAGUE_PLAYERS");

        try { leaguesPanel = new LeaguesPanel(true); }
        catch (Throwable t) { leaguesPanel = new LeaguesPanel(); }

        schedulePanel = new SchedulePanel(defaultLeague); // NEW

        contentPanel.add(leaguesPanel, "LEAGUES");
        contentPanel.add(teamsPanel, "TEAMS");
        contentPanel.add(leaguePlayersPanel, "LEAGUE_PLAYERS");
        contentPanel.add(schedulePanel, "SCHEDULE");       // NEW

        // Nav actions
        btnLeagues.addActionListener(e -> cardLayout.show(contentPanel, "LEAGUES"));
        btnTeams.addActionListener(e -> cardLayout.show(contentPanel, "TEAMS"));
        btnLeaguePlayers.addActionListener(e -> cardLayout.show(contentPanel, "LEAGUE_PLAYERS"));
        btnSchedule.addActionListener(e -> cardLayout.show(contentPanel, "SCHEDULE")); // NEW

        // League switching syncs all cards
        leagueBox.addActionListener(e -> {
            String selected = (String) leagueBox.getSelectedItem();
            if (selected != null && !selected.isBlank()) onLeagueChanged(selected.trim());
        });

        // Default view
        cardLayout.show(contentPanel, "TEAMS");
    }

    /** Populate the league dropdown and select a preferred league. */
    private void populateLeagueBox(String preferSelect) {
        var names = appState.getLeagues().keySet().stream()
                .sorted(String::compareToIgnoreCase)
                .toArray(String[]::new);

        leagueBox.removeAllItems();
        Arrays.stream(names).forEach(leagueBox::addItem);

        if (preferSelect != null && !preferSelect.isBlank()
                && Arrays.stream(names).anyMatch(n -> n.equalsIgnoreCase(preferSelect))) {
            leagueBox.setSelectedItem(preferSelect);
        } else if (leagueBox.getItemCount() > 0) {
            leagueBox.setSelectedIndex(0);
        }
    }

    /** Sync all subviews when the league changes. */
    private void onLeagueChanged(String leagueName) {
        // Teams (read-only, opens Players dialog for selected team)
        if (teamsPanel != null) teamsPanel.loadTeamsForLeague(leagueName);

        // Recreate LeaguePlayersPanel (simple & safe)
        if (leaguePlayersPanel != null) contentPanel.remove(leaguePlayersPanel);
        leaguePlayersPanel = new LeaguePlayersPanel(leagueName);
        contentPanel.add(leaguePlayersPanel, "LEAGUE_PLAYERS");

        // Recreate SchedulePanel (so it reflects the new league)
        if (schedulePanel != null) contentPanel.remove(schedulePanel);
        schedulePanel = new SchedulePanel(leagueName);
        contentPanel.add(schedulePanel, "SCHEDULE");

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /** Programmatically select a league (optional helper). */
    public void selectLeague(String leagueName) {
        if (leagueName == null || leagueName.isBlank()) return;
        leagueBox.setSelectedItem(leagueName);
        if (!Objects.equals(leagueBox.getSelectedItem(), leagueName)) {
            for (int i = 0; i < leagueBox.getItemCount(); i++) {
                String s = leagueBox.getItemAt(i);
                if (s.equalsIgnoreCase(leagueName)) {
                    leagueBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }
}
