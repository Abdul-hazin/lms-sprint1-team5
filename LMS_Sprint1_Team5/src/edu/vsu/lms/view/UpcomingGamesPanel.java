package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.Game;

public class UpcomingGamesPanel extends JPanel {
    private final String leagueName;
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);

    public UpcomingGamesPanel(String leagueName) {
        this.leagueName = leagueName;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Upcoming Games — " + leagueName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        list.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> refresh());
        add(btnRefresh, BorderLayout.SOUTH);

        refresh();
    }

    private void refresh() {
        model.clear();
        var league = AppState.getInstance().getLeagues().get(leagueName);
        if (league == null) {
            model.addElement("League not found.");
            return;
        }

        // Filter and sort by date ascending
        List<Game> upcoming = league.getGames().stream()
                .filter(g -> !g.hasResult())
                .sorted(Comparator.comparing(Game::getDate))
                .toList();

        if (upcoming.isEmpty()) {
            model.addElement("No upcoming games (all played or none scheduled).");
            return;
        }

        for (Game g : upcoming) {
            String display = String.format("%s — %s vs %s", 
                    g.getDate().toString(), g.getHomeTeam(), g.getAwayTeam());
            model.addElement(display);
        }
    }
}
