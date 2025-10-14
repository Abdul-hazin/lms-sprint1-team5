package edu.vsu.lms;

import javax.swing.SwingUtilities;
import edu.vsu.lms.view.MainFrame;
import edu.vsu.lms.persistence.AppState;

public class Main {
    public static void main(String[] args) {
        // Seed initial state (includes default admin account)
        AppState.getInstance().seedDefaults();
        SwingUtilities.invokeLater(() -> {
            new MainFrame().setVisible(true);
        });
    }
}
