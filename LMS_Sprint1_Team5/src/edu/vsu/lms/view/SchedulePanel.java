package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.util.*;
import java.util.List;
import edu.vsu.lms.controller.ScheduleController;

public class SchedulePanel extends JPanel {
    private final ScheduleController ctrl = new ScheduleController();
    private final String leagueName;
    private final JTextArea scheduleDisplay = new JTextArea();

    public SchedulePanel(String leagueName) {
        this.leagueName = leagueName;
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JLabel title = new JLabel("Generate Schedule for " + leagueName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        scheduleDisplay.setEditable(false);
        add(new JScrollPane(scheduleDisplay), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton generate = new JButton("Generate Schedule");
        JButton view = new JButton("View Schedule");
        bottom.add(generate);
        bottom.add(view);
        add(bottom, BorderLayout.SOUTH);

        generate.addActionListener(e -> onGenerate());
        view.addActionListener(e -> onView());
    }

    private void onGenerate() {
        JCheckBox tue = new JCheckBox("Tuesday", true);
        JCheckBox sat = new JCheckBox("Saturday", true);
        JPanel p = new JPanel(new GridLayout(0,1));
        p.add(new JLabel("Select play days:"));
        p.add(tue);
        p.add(sat);
        int ok = JOptionPane.showConfirmDialog(this, p, "Play Days", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        List<DayOfWeek> days = new ArrayList<>();
        if (tue.isSelected()) days.add(DayOfWeek.TUESDAY);
        if (sat.isSelected()) days.add(DayOfWeek.SATURDAY);

        boolean created = ctrl.generateSchedule(leagueName, days, LocalDate.now());
        if (created) JOptionPane.showMessageDialog(this, "Schedule created successfully!");
        else JOptionPane.showMessageDialog(this, "Schedule already exists or not enough teams.");
    }

    private void onView() {
        var lg = edu.vsu.lms.persistence.AppState.getInstance().getLeagues().get(leagueName);
        StringBuilder sb = new StringBuilder();
        for (var g : lg.getGames()) sb.append(g).append("\n");
        scheduleDisplay.setText(sb.toString());
    }
}
