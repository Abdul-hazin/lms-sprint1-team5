package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import edu.vsu.lms.controller.ScheduleController;
import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.Game;

public class RecordResultPanel extends JPanel {
    private final ScheduleController ctrl = new ScheduleController();
    private final String leagueName;
    private final JComboBox<Game> gameBox = new JComboBox<>();

    public RecordResultPanel(String leagueName) {
        this.leagueName = leagueName;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JLabel title = new JLabel("Record Game Result for " + leagueName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        // Fill dropdown with scheduled games
        var lg = AppState.getInstance().getLeagues().get(leagueName);
        for (Game g : lg.getGames()) {
            if (!g.hasResult()) gameBox.addItem(g);
        }

        JPanel center = new JPanel(new GridLayout(0,2,8,8));
        center.add(new JLabel("Select Game:"));
        center.add(gameBox);
        center.add(new JLabel("Home Score:"));
        JTextField homeScore = new JTextField();
        center.add(homeScore);
        center.add(new JLabel("Away Score:"));
        JTextField awayScore = new JTextField();
        center.add(awayScore);
        add(center, BorderLayout.CENTER);

        JButton record = new JButton("Save Result");
        record.addActionListener(e -> {
            Game g = (Game) gameBox.getSelectedItem();
            if (g == null) return;
            try {
                int h = Integer.parseInt(homeScore.getText().trim());
                int a = Integer.parseInt(awayScore.getText().trim());
                boolean ok = ctrl.recordResult(leagueName, g.getDate(), g.getHomeTeam(), g.getAwayTeam(), h, a);
                JOptionPane.showMessageDialog(this, ok ? "Result recorded!" : "Failed to record result.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid score values.");
            }
        });
        add(record, BorderLayout.SOUTH);
    }
}
