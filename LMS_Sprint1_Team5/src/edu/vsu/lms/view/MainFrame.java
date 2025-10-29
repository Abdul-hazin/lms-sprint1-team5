package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import edu.vsu.lms.controller.AuthController;
import edu.vsu.lms.model.Role;
import edu.vsu.lms.model.User;

public class MainFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final AuthController auth = new AuthController();

    public MainFrame() {
        super("LMS — Sprint 1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // temporary content while loading
        setContentPane(new JLabel("Loading UI...", SwingConstants.CENTER));

        try {
            // Panels
            JComponent login = new LoginPanel(auth, this::onLoginSuccess);
            root.add(login, "login");

            // container
            setContentPane(root);
            cards.show(root, "login");

        } catch (Throwable t) {
            t.printStackTrace();
            setContentPane(errorPanel("MainFrame init failed", t));
        }
    }

    // Error display
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

    // ✅ Decides what panel to show after login
    private void onLoginSuccess() {
        User current = auth.getCurrentUser();
        if (current == null) {
            JOptionPane.showMessageDialog(this, "Login failed: no user found.");
            cards.show(root, "login");
            return;
        }

        JComponent nextPanel;

        try {
            if (current.getRole() == Role.LA) {
                nextPanel = new AdminDashboardPanel(auth, this::onLogout);
                root.add(nextPanel, "admin");
                cards.show(root, "admin");
            }

            else if (current.getRole() == Role.LO) {
                nextPanel = new LeagueOfficialPanel(this::onLogout); // pass logout callback
                root.add(nextPanel, "leagueOfficial");
                cards.show(root, "leagueOfficial");
            }
             else if (current.getRole() == Role.TO) { // assuming TO = Team Official
                nextPanel = new TeamOfficialPanel(this::onLogout); // pass logout callback
                 root.add(nextPanel, "teamOfficial");
                cards.show(root, "teamOfficial");
            }
            
            else {
                JOptionPane.showMessageDialog(this, "Unknown role: " + current.getRole());
                cards.show(root, "login");
                return;
            }

            revalidate();
            repaint();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading dashboard: " + e.getMessage());
            cards.show(root, "login");
        }
    }

    private void onLogout() {
        cards.show(root, "login");
    }
}
