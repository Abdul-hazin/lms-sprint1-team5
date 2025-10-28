package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import edu.vsu.lms.controller.AuthController;
import edu.vsu.lms.persistence.AppState;

public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final AuthController auth = new AuthController();

    // Load persisted singleton so we can save on exit
    private final AppState state = AppState.getInstance();

    public MainFrame() {
        super("LMS — Sprint 1");

        // We'll handle saving on close ourselves
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Panels
        LoginPanel login = new LoginPanel(auth, this::onLoginSuccess);
        // ✅ matches your AdminDashboardPanel(AuthController, Runnable)
        AdminDashboardPanel admin = new AdminDashboardPanel(auth, this::onLogout);

        root.setLayout(cards);
        root.add(login, "login");
        root.add(admin, "admin");

        setContentPane(root);
        cards.show(root, "login");

        // Persist state on window close
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                try {
                    state.save();
                } finally {
                    dispose();
                    System.exit(0);
                }
            }
        });
    }

    private void onLoginSuccess() {
        // Optional: ensure defaults exist (safe to call)
        // state.seedDefaults();
        cards.show(root, "admin");
    }

    // Called by AdminDashboardPanel via the Runnable we passed in
    private void onLogout() {
        state.save();
        cards.show(root, "login");
    }
}
