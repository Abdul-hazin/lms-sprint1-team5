package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;

import edu.vsu.lms.controller.AuthController;
import edu.vsu.lms.controller.UserAdminController;
import edu.vsu.lms.model.Role;
import edu.vsu.lms.model.User;

public class AdminDashboardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final AuthController auth;
    private final Runnable onLogout;                      // <-- logout callback
    private final UserAdminController userAdmin = new UserAdminController();
    private final DefaultListModel<String> usersModel = new DefaultListModel<>();
    private final JList<String> usersList = new JList<>(usersModel);

    // Keep actual users for selection mapping
    private List<User> currentUsers = new ArrayList<>();

    /** Old signature kept for compatibility */
    public AdminDashboardPanel(AuthController auth) { this(auth, null); }

    /** New signature with logout callback */
    public AdminDashboardPanel(AuthController auth, Runnable onLogout) {
        this.auth = auth;
        this.onLogout = onLogout;

        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Header + controls
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JLabel hello = new JLabel("Logged in as: " +
            (auth.getCurrentUser() != null ? auth.getCurrentUser().toString() : "?"));

        JButton btnRefresh = new JButton("Refresh Users");
        JButton btnAdd     = new JButton("Add User");
        JButton btnDelete  = new JButton("Delete User");   // NEW
        JButton btnLeagues = new JButton("Leagues…");      // you already wired LeaguesPanel
        JButton btnTeams   = new JButton("Teams…");        // requires TeamsPanel class
        JButton btnLogout  = new JButton("Log out");       // NEW

        top.add(hello);
        top.add(btnRefresh);
        top.add(btnAdd);
        top.add(btnDelete);
        top.add(btnLeagues);
        top.add(btnTeams);
        top.add(btnLogout);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(usersList), BorderLayout.CENTER);

        // Listeners
        btnRefresh.addActionListener(e -> refreshUsers());
        btnAdd.addActionListener(e -> showAddUserDialog());
        btnDelete.addActionListener(e -> onDeleteUser());                            // NEW

        btnLeagues.addActionListener(e -> {
            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Leagues", Dialog.ModalityType.APPLICATION_MODAL);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.setContentPane(new LeaguesPanel());
            d.pack();
            d.setSize(420, 320);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        });

        // If you don’t have TeamsPanel yet, comment this whole listener.
        btnTeams.addActionListener(e -> {
            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Teams", Dialog.ModalityType.APPLICATION_MODAL);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.setContentPane(new TeamsPanel());
            d.pack();
            d.setSize(520, 380);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        });

        btnLogout.addActionListener(e -> {                                           // NEW
            if (auth != null) auth.logout();
            if (onLogout != null) onLogout.run();
        });

        refreshUsers();
    }

    private void refreshUsers() {
        usersModel.clear();
        currentUsers = userAdmin.listUsersSorted();
        for (User u : currentUsers) {
            usersModel.addElement(u.toString());
        }
    }

    private void onDeleteUser() {
        int idx = usersList.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        User target = currentUsers.get(idx);

        // Optional safety: prevent deleting yourself
        if (auth != null && auth.getCurrentUser() != null &&
            target.getId().equals(auth.getCurrentUser().getId())) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account while logged in.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Delete user: " + target.getId() + " ?",
                "Confirm Delete",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        boolean deleted = userAdmin.deleteUser(target.getId());
        if (!deleted) {
            JOptionPane.showMessageDialog(this, "Delete failed (user may not exist, or last admin).");
        } else {
            refreshUsers();
        }
    }

    private void showAddUserDialog() {
        JTextField id = new JTextField();
        JTextField first = new JTextField();
        JTextField last = new JTextField();
        JComboBox<Role> role = new JComboBox<>(Role.values());
        JPasswordField pw = new JPasswordField();

        JPanel p = new JPanel(new GridLayout(0,1,6,6));
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
                    "Failed to add user. Check duplicate ID or password policy (>=6, upper, lower, digit, special !@#$%^&*).");
            } else {
                refreshUsers();
            }
        }
    }
}