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

        // Show something immediately so a window appears even if a panel fails
        setContentPane(new JLabel("Loading UI...", SwingConstants.CENTER));

        try {
            // Use JComponent so we can assign either a real panel or an error panel
            JComponent login;
            try {
                login = new LoginPanel(auth, this::onLoginSuccess);
            } catch (Exception e) {
                e.printStackTrace();
                login = errorPanel("LoginPanel failed", e);
            }

            JComponent admin;
            try {
                admin = new AdminDashboardPanel(auth, this::onLogout);
            } catch (Exception e) {
                e.printStackTrace();
                admin = errorPanel("AdminDashboardPanel failed", e);
            }

            root.add(login, "login");
            root.add(admin, "admin");

            setContentPane(root);
            cards.show(root, "login");
        } catch (Throwable t) {
            t.printStackTrace();
            setContentPane(errorPanel("MainFrame init failed", t));
        }
    }

    // Helper panel to display UI errors
    private JPanel errorPanel(String title, Throwable t) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("⚠️ " + title, SwingConstants.CENTER), BorderLayout.NORTH);

        JTextArea area = new JTextArea();
        area.append(t.toString());
        for (StackTraceElement el : t.getStackTrace()) {
            area.append("\n    at " + el);
        }
        area.setEditable(false);
        area.setLineWrap(false);

        p.add(new JScrollPane(area), BorderLayout.CENTER);
        return p;
    }

    private void onLoginSuccess() {
        cards.show(root, "admin");
    }

    private void onLogout() {
        cards.show(root, "login");
    }
}