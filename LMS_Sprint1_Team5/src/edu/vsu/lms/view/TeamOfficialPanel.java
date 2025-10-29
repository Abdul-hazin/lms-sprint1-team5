package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import edu.vsu.lms.persistence.AppState;


public class TeamOfficialPanel extends JPanel {

    private final AppState appState = AppState.getInstance();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Runnable onLogout;

    public TeamOfficialPanel(Runnable onLogout) {
        this.onLogout = onLogout;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel topBar = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Team Official Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> onLogout.run());

        topBar.add(title, BorderLayout.CENTER);
        topBar.add(logoutBtn, BorderLayout.EAST);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(topBar, BorderLayout.NORTH);

        JPanel sidebar = new JPanel(new GridLayout(5, 1, 10, 10));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnLeagues = new JButton("View Leagues");
        JButton btnTeamPlayers = new JButton("View Team Players");
        JButton btnLeaguePlayers = new JButton("View League Players");
        JButton btnSchedule = new JButton("View Schedule");

        sidebar.add(btnLeagues);
        sidebar.add(btnTeamPlayers);
        sidebar.add(btnLeaguePlayers);
        sidebar.add(btnSchedule);
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        LeaguesPanel leaguesPanel = new LeaguesPanel();

        String defaultLeague = appState.getOrInitDefaultLeague();

        PlayersPanel teamPlayersPanel = new PlayersPanel(defaultLeague, "Default Team");

        LeaguePlayersPanel leaguePlayersPanel = new LeaguePlayersPanel(defaultLeague);

        SchedulePanel schedulePanel = new SchedulePanel(defaultLeague);

        contentPanel.add(leaguesPanel, "LEAGUES");
        contentPanel.add(teamPlayersPanel, "TEAM_PLAYERS");
        contentPanel.add(leaguePlayersPanel, "LEAGUE_PLAYERS");
        contentPanel.add(schedulePanel, "SCHEDULE");

        btnLeagues.addActionListener(e -> cardLayout.show(contentPanel, "LEAGUES"));
        btnTeamPlayers.addActionListener(e -> cardLayout.show(contentPanel, "TEAM_PLAYERS"));
        btnLeaguePlayers.addActionListener(e -> cardLayout.show(contentPanel, "LEAGUE_PLAYERS"));
        btnSchedule.addActionListener(e -> cardLayout.show(contentPanel, "SCHEDULE"));

        cardLayout.show(contentPanel, "LEAGUES");
    }
}
