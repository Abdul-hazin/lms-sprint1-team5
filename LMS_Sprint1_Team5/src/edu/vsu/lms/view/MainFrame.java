package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;

import edu.vsu.lms.controller.AuthController;

public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final AuthController auth = new AuthController();

    public MainFrame() {
        super("LMS — Sprint 1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        LoginPanel login = new LoginPanel(auth, this::onLoginSuccess);
        AdminDashboardPanel admin = new AdminDashboardPanel(auth);

        root.add(login, "login");
        root.add(admin, "admin");

        setContentPane(root);
        cards.show(root, "login");
    }

    private void onLoginSuccess() {
        cards.show(root, "admin");
    }

    private void onLogout() {
        cards.show(root, "login");
    }
}
