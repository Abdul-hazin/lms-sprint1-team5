package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.io.File;

import edu.vsu.lms.controller.AuthController;
import edu.vsu.lms.controller.GameStatsController;
import edu.vsu.lms.controller.UserAdminController;
import edu.vsu.lms.model.Role;
import edu.vsu.lms.model.User;
import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Game;
import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.persistence.LeagueXmlLoader;

public class AdminDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final AuthController auth;
    private final UserAdminController userAdmin = new UserAdminController();
    private final DefaultListModel<String> usersModel = new DefaultListModel<>();
    private final JList<String> usersList = new JList<>(usersModel);
    private final Runnable onLogout;

    // ✅ shared game stats controller from AppState
    private final GameStatsController gameStatsController =
            AppState.getInstance().getGameStatsController();

    public AdminDashboardPanel(AuthController auth, Runnable onLogout) {
        this.auth = auth;
        this.onLogout = onLogout;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ===== HEADER (TOP) =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));

        JLabel hello = new JLabel("Logged in as: " +
                (auth.getCurrentUser() != null ? auth.getCurrentUser().toString() : "?"));

        JButton btnRefresh       = new JButton("Refresh Users");
        JButton btnAdd           = new JButton("Add User");
        JButton btnLeagues       = new JButton("Leagues…");
        JButton btnTeams         = new JButton("Teams…");
        JButton btnSchedule      = new JButton("Schedule...");
        JButton btnResults       = new JButton("Record Result");
        JButton btnUpcoming      = new JButton("Upcoming Games");
        JButton btnViewStats     = new JButton("View Game Stats");  // ✅ NEW
        JButton btnLoadLeagueXml = new JButton("Load League XML");
        JButton btnEditStats = new JButton("Edit Game Stats");
        JButton btnStandings = new JButton("Standings…");
        JButton btnPlayerStats = new JButton("Player Stats…");
        JButton btnTeamStats = new JButton("Team Stats…");
        JButton btnPower = new JButton("Power Rankings");

        top.add(hello);
        top.add(btnRefresh);
        top.add(btnAdd);
        top.add(btnLeagues);
        top.add(btnTeams);
        top.add(btnSchedule);
        top.add(btnResults);
        top.add(btnUpcoming);
        top.add(btnViewStats);
        top.add(btnStandings); 
        top.add(btnPlayerStats);
        top.add(btnTeamStats);
        top.add(btnPower);  // ✅ NEW button visible on top
        add(top, BorderLayout.NORTH);

        // ===== CENTER (USERS LIST) =====
        add(new JScrollPane(usersList), BorderLayout.CENTER);

        // ===== FOOTER (BOTTOM) =====
        JButton btnLogout = new JButton("Logout");
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setForeground(Color.RED);

        JPanel bottom = new JPanel(new BorderLayout());

        // LEFT side: Load XML
        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftBottom.add(btnLoadLeagueXml);
        leftBottom.add(btnEditStats);

        // RIGHT side: Delete, Upcoming, Logout
        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightBottom.add(deleteBtn);
        rightBottom.add(btnUpcoming);
        rightBottom.add(btnLogout);

        bottom.add(leftBottom, BorderLayout.WEST);
        bottom.add(rightBottom, BorderLayout.EAST);

        add(bottom, BorderLayout.SOUTH);

        // ===== EVENT LISTENERS =====
        btnRefresh.addActionListener(e -> refreshUsers());
        btnAdd.addActionListener(e -> showAddUserDialog());
        btnLeagues.addActionListener(e -> showLeaguesDialog());
        btnTeams.addActionListener(e -> showTeamsDialog());
        btnSchedule.addActionListener(e -> showScheduleDialog());
        btnResults.addActionListener(e -> showRecordResultDialog());
        btnUpcoming.addActionListener(e -> showUpcomingGamesDialog());
        btnLoadLeagueXml.addActionListener(e -> loadLeagueFromXml());
        btnViewStats.addActionListener(e -> showGameStatsReportDialog());
        btnEditStats.addActionListener(e -> showGameStatsEditorDialog());

        btnPower.addActionListener(e -> {
    String leagueName = JOptionPane.showInputDialog(this, "Enter League Name:");
    if (leagueName == null || leagueName.isBlank()) return;

    AppState appState = AppState.getInstance();
    League league = appState.getLeagues().get(leagueName);
    if (league == null) {
        JOptionPane.showMessageDialog(this,
                "League \"" + leagueName + "\" not found.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Power Rankings", Dialog.ModalityType.APPLICATION_MODAL);
    d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    d.setContentPane(new PowerRankingsPanel(leagueName, gameStatsController));
    d.setSize(1000, 600);
    d.setLocationRelativeTo(this);
    d.setVisible(true);
});


        btnTeamStats.addActionListener(e -> {
    String leagueName = JOptionPane.showInputDialog(this, "Enter League Name:");
    if (leagueName == null || leagueName.isBlank()) return;

    AppState appState = AppState.getInstance();
    if (!appState.getLeagues().containsKey(leagueName)) {
        JOptionPane.showMessageDialog(this,
                "League \"" + leagueName + "\" not found.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Team Stats", Dialog.ModalityType.APPLICATION_MODAL);
    d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    d.setContentPane(new TeamStatsPanel(
            leagueName,
            appState.getGameStatsController()
    ));
    d.setSize(750, 500);
    d.setLocationRelativeTo(this);
    d.setVisible(true);
});

           

        btnPlayerStats.addActionListener(e -> {
        String leagueName = JOptionPane.showInputDialog(this, "Enter League Name:");
        if (leagueName == null || leagueName.isBlank()) return;

        AppState appState = AppState.getInstance();
        if (!appState.getLeagues().containsKey(leagueName)) {
         JOptionPane.showMessageDialog(this,
                "League \"" + leagueName + "\" not found.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
         return;
        }

    JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Player Stats", Dialog.ModalityType.APPLICATION_MODAL);
    d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    d.setContentPane(new PlayerStatsPanel(
            leagueName,
            appState.getGameStatsController()   // shared controller
    ));
    d.setSize(700, 500);
    d.setLocationRelativeTo(this);
    d.setVisible(true);
});

        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to log out?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && onLogout != null) {
                onLogout.run();
            }
        });
        btnStandings.addActionListener(e -> {
             String leagueName = JOptionPane.showInputDialog(this, "Enter League Name:");
            if (leagueName == null || leagueName.isBlank()) return;

             JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                 "League Standings", Dialog.ModalityType.APPLICATION_MODAL);
             d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
             d.setContentPane(new LeagueStandingsPanel(leagueName));
             d.setSize(500, 400);
             d.setLocationRelativeTo(this);
             d.setVisible(true);
        });

        // Delete selected user
        deleteBtn.addActionListener(e -> {
            String selected = usersList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a user to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete:\n" + selected + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Extract ID from parentheses in User.toString()
                String userId = null;
                int start = selected.indexOf('(');
                int end = selected.indexOf(')');
                if (start != -1 && end != -1 && end > start) {
                    userId = selected.substring(start + 1, end).trim();
                }

                boolean deleted = userAdmin.deleteUser(userId);

                if (deleted) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully.");
                    refreshUsers();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete user (user may not exist or cannot be deleted).");
                }
            }
        });

        // Initial load
        refreshUsers();
    }

    // ===== HELPERS =====

    private void refreshUsers() {
        usersModel.clear();
        List<User> users = userAdmin.listUsersSorted();
        for (User u : users) {
            usersModel.addElement(u.toString());
        }
    }

    private void showAddUserDialog() {
        JTextField id    = new JTextField();
        JTextField first = new JTextField();
        JTextField last  = new JTextField();
        JComboBox<Role> role = new JComboBox<>(Role.values());
        JPasswordField pw = new JPasswordField();

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("ID:"));       p.add(id);
        p.add(new JLabel("First:"));    p.add(first);
        p.add(new JLabel("Last:"));     p.add(last);
        p.add(new JLabel("Role:"));     p.add(role);
        p.add(new JLabel("Password:")); p.add(pw);

        int ok = JOptionPane.showConfirmDialog(this, p, "Add User", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            boolean added = userAdmin.addUser(
                    id.getText().trim(),
                    first.getText().trim(),
                    last.getText().trim(),
                    (Role) role.getSelectedItem(),
                    new String(pw.getPassword())
            );
            if (!added) {
                JOptionPane.showMessageDialog(this,
                        "Failed to add user. Check duplicate ID or password policy (≥6 chars, upper/lower/digit/special).");
            } else {
                refreshUsers();
            }
        }
    }

    private void showLeaguesDialog() {
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Leagues", Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setContentPane(new LeaguesPanel());
        d.pack();
        d.setSize(400, 300);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showTeamsDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Teams", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        TeamsPanel teamsPanel = new TeamsPanel();
        dialog.setContentPane(teamsPanel);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(500, 400));
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private void showScheduleDialog() {
        String league = JOptionPane.showInputDialog(this, "Enter League Name:");
        if (league == null || league.isBlank()) return;

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Schedule", Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setContentPane(new SchedulePanel(league));
        d.pack();
        d.setSize(600, 400);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showRecordResultDialog() {
        String league = JOptionPane.showInputDialog(this, "Enter League Name:");
        if (league == null || league.isBlank()) return;

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Record Result", Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setContentPane(new RecordResultPanel(league));
        d.pack();
        d.setSize(500, 300);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showUpcomingGamesDialog() {
        String league = JOptionPane.showInputDialog(this, "Enter League Name:");
        if (league == null || league.isBlank()) return;

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Upcoming Games", Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setContentPane(new UpcomingGamesPanel(league));
        d.pack();
        d.setSize(500, 400);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    /**
     * Load a league + teams + players from an XML file.
     * Uses LeagueXmlLoader and stores the League in AppState.
     */
    private void loadLeagueFromXml() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File xmlFile = chooser.getSelectedFile();
        try {
            LeagueXmlLoader loader = new LeagueXmlLoader();
            League league = loader.loadLeagueFromFile(xmlFile);

            AppState appState = AppState.getInstance();
            appState.getLeagues().put(league.getName(), league);

            JOptionPane.showMessageDialog(this,
                    "Loaded league \"" + league.getName() + "\" with " +
                            league.getTeams().size() + " teams.",
                    "League Loaded",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading XML:\n" + ex.getMessage(),
                    "Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void showGameStatsEditorDialog() {
    String leagueName = JOptionPane.showInputDialog(this, "Enter League Name:");
    if (leagueName == null || leagueName.isBlank()) return;

    AppState appState = AppState.getInstance();
    League league = appState.getLeagues().get(leagueName);
    if (league == null) {
        JOptionPane.showMessageDialog(this,
                "League \"" + leagueName + "\" not found.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    if (league.getGames().isEmpty()) {
        JOptionPane.showMessageDialog(this,
                "No games found for league \"" + leagueName + "\".",
                "No Games",
                JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    java.util.List<Game> games = league.getGames();
    Game selected = (Game) JOptionPane.showInputDialog(
            this,
            "Select a game:",
            "Choose Game",
            JOptionPane.PLAIN_MESSAGE,
            null,
            games.toArray(),
            games.get(0)
    );
    if (selected == null) return;

    GameStatsController gsc = appState.getGameStatsController();

    JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Edit Game Stats", Dialog.ModalityType.APPLICATION_MODAL);
    d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    d.setContentPane(new GameStatsEditorPanel(league, selected, gsc));
    d.setSize(900, 600);
    d.setLocationRelativeTo(this);
    d.setVisible(true);
}


    // ✅ NEW: Show Game Stats Report
   
 private void showGameStatsReportDialog() {
        String leagueName = JOptionPane.showInputDialog(this, "Enter League Name:");
        if (leagueName == null || leagueName.isBlank()) return;

        AppState appState = AppState.getInstance();
        League league = appState.getLeagues().get(leagueName);
        if (league == null) {
            JOptionPane.showMessageDialog(this,
                    "League \"" + leagueName + "\" not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (league.getGames().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No games found for league \"" + leagueName + "\".",
                    "No Games",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        List<Game> games = league.getGames();
        Game selected = (Game) JOptionPane.showInputDialog(
                this,
                "Select a game:",
                "Choose Game",
                JOptionPane.PLAIN_MESSAGE,
                null,
                games.toArray(),
                games.get(0)
        );

        if (selected == null) return;

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Game Stats Report", Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setContentPane(new GameStatsReportPanel(selected, gameStatsController));
        d.setSize(1000, 600);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
}