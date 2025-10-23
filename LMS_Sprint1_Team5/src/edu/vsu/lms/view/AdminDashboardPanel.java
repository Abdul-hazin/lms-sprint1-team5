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

    public AdminDashboardPanel(AuthController auth) {
        this.auth = auth;
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel hello = new JLabel("Logged in as: " + (auth.getCurrentUser() != null ? auth.getCurrentUser().toString() : "?"));
        JButton btnRefresh = new JButton("Refresh Users");
        JButton btnAdd = new JButton("Add User");
        JButton btnLeagues = new JButton("Leagues…");  // <-- create here

        top.add(hello);
        top.add(btnRefresh);
        top.add(btnAdd);
        top.add(btnLeagues);  // <-- and add to top

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(usersList), BorderLayout.CENTER);

        // listeners
        btnRefresh.addActionListener(e -> refreshUsers());
        btnAdd.addActionListener(e -> showAddUserDialog());
        btnLeagues.addActionListener(e -> {
            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this), "Leagues", Dialog.ModalityType.APPLICATION_MODAL);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.setContentPane(new LeaguesPanel());
            d.pack();
            d.setSize(400, 300);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
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

        JPanel p = new JPanel(new GridLayout(0,1));
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

