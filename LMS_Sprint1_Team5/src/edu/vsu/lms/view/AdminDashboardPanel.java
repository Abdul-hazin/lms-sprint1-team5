package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import edu.vsu.lms.controller.AuthController;
import edu.vsu.lms.controller.UserAdminController;
import edu.vsu.lms.model.Role;
import edu.vsu.lms.model.User;

public class AdminDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final AuthController auth;
    private final UserAdminController userAdmin = new UserAdminController();
    private final DefaultListModel<String> usersModel = new DefaultListModel<>();
    private final JList<String> usersList = new JList<>(usersModel);
    private final Runnable onLogout;

    public AdminDashboardPanel(AuthController auth, Runnable onLogout) {
        this.auth = auth;
        this.onLogout = onLogout;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Header
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel hello = new JLabel("Logged in as: " + (auth.getCurrentUser() != null ? auth.getCurrentUser().toString() : "?"));
        JButton btnRefresh = new JButton("Refresh Users");
        JButton btnAdd = new JButton("Add User");
        JButton btnLeagues = new JButton("Leagues…");
        JButton btnTeams = new JButton("Teams…"); // ✅ Added back
        JButton btnSchedule = new JButton("Schedule...");
        JButton btnResults = new JButton("Record Result");
       

        top.add(hello);
        top.add(btnRefresh);
        top.add(btnAdd);
        top.add(btnLeagues);
        top.add(btnTeams);
        add(top, BorderLayout.NORTH);
        top.add(btnSchedule);
        top.add(btnResults);

        // Users list in the center
        add(new JScrollPane(usersList), BorderLayout.CENTER);

        // Footer - Logout button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Logout");
        JButton btnUpcoming = new JButton("Upcoming Games");
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setForeground(Color.RED);


        bottom.add(deleteBtn);
        bottom.add(btnUpcoming);
        bottom.add(btnLogout);
        add(bottom, BorderLayout.SOUTH);

        // Event listeners
        btnRefresh.addActionListener(e -> refreshUsers());
        btnAdd.addActionListener(e -> showAddUserDialog());
        btnLeagues.addActionListener(e -> showLeaguesDialog());
        btnTeams.addActionListener(e -> showTeamsDialog()); // ✅ New event
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION && onLogout != null) {
                onLogout.run();
            }
        });

        btnSchedule.addActionListener(e -> {
            String league = JOptionPane.showInputDialog(this, "Enter League Name:");
                 if (league == null || league.isBlank()) return;

            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Schedule", Dialog.ModalityType.APPLICATION_MODAL);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.setContentPane(new SchedulePanel(league));
            d.pack();
            d.setSize(600, 400);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        });

        btnResults.addActionListener(e -> {
             String league = JOptionPane.showInputDialog(this, "Enter League Name:");
                if (league == null || league.isBlank()) return;

            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Record Result", Dialog.ModalityType.APPLICATION_MODAL);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.setContentPane(new RecordResultPanel(league));
            d.pack();
            d.setSize(500, 300);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        });
        btnUpcoming.addActionListener(e -> {
            String league = JOptionPane.showInputDialog(this, "Enter League Name:");
            if (league == null || league.isBlank()) return;

            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Upcoming Games", Dialog.ModalityType.APPLICATION_MODAL);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.setContentPane(new UpcomingGamesPanel(league));
            d.pack();
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
        // Extract ID from parentheses
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
            JOptionPane.showMessageDialog(this, "Failed to delete user (user may not exist or cannot be deleted).");
        }
    }
});
        


        refreshUsers();
    }

    private void refreshUsers() {
        usersModel.clear();
        List<User> users = userAdmin.listUsersSorted();
        for (User u : users) usersModel.addElement(u.toString());
    }

    private void showAddUserDialog() {
        JTextField id = new JTextField();
        JTextField first = new JTextField();
        JTextField last = new JTextField();
        JComboBox<Role> role = new JComboBox<>(Role.values());
        JPasswordField pw = new JPasswordField();

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("ID:")); p.add(id);
        p.add(new JLabel("First:")); p.add(first);
        p.add(new JLabel("Last:")); p.add(last);
        p.add(new JLabel("Role:")); p.add(role);
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
        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Leagues", Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setContentPane(new LeaguesPanel());
        d.pack();
        d.setSize(400, 300);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void showTeamsDialog() {
    JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
                                 "Teams", 
                                 Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    TeamsPanel teamsPanel = new TeamsPanel(); // or new TeamsPanel(currentLeague, true);
    dialog.setContentPane(teamsPanel);

    dialog.pack();
    dialog.setMinimumSize(new Dimension(500, 400));
    dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
    dialog.setVisible(true);
}

}

