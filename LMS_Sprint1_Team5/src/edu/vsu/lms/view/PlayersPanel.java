package edu.vsu.lms.view;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import edu.vsu.lms.controller.PlayerController;
import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.Player;
import java.util.List;

public class PlayersPanel extends JPanel {
    private final String leagueName;
    private final String teamName;
    private final boolean readOnly;               // <<< NEW
    private final PlayerController ctrl = new PlayerController();

    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);
    private JComboBox<String> sortBox;

    // Keep your original 2-arg ctor for existing callers
    public PlayersPanel(String leagueName, String teamName) {
        this(leagueName, teamName, false);
    }

    // NEW: read-only toggle
    public PlayersPanel(String leagueName, String teamName, boolean readOnly) {
        this.leagueName = leagueName;
        this.teamName = teamName;
        this.readOnly = readOnly;

        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Header
        JPanel top = new JPanel(new BorderLayout());
        JLabel header = new JLabel("Players in " + teamName + " (" + leagueName + ")");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
        top.add(header, BorderLayout.WEST);

        // Sort box
        JPanel sortPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        sortPanel.add(new JLabel("Sort by:"));
        sortBox = new JComboBox<>(new String[]{"Last Name", "Position", "Number"});
        sortPanel.add(sortBox);
        JButton btnSort = new JButton("Sort");
        sortPanel.add(btnSort);
        top.add(sortPanel, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(list), BorderLayout.CENTER);

        // Footer buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton add = new JButton("Add Player");
        JButton edit = new JButton("Edit Player");
        JButton delete = new JButton("Delete Player");
        JButton move = new JButton("Move Player");

        // If read-only, hide the whole button row
        if (!readOnly) {
            bottom.add(add);
            bottom.add(edit);
            bottom.add(delete);
            bottom.add(move);
        }
        add(bottom, BorderLayout.SOUTH);

        // Actions
        if (!readOnly) {
            add.addActionListener(e -> onAddPlayer());
            edit.addActionListener(e -> onEditPlayer());
            delete.addActionListener(e -> onDeletePlayer());
            move.addActionListener(e -> onMovePlayer());
        }
        btnSort.addActionListener(e -> refresh());

        refresh();
    }

    private void refresh() {
        model.clear();

        // Copy to a modifiable list
        List<Player> players = new ArrayList<>(ctrl.listPlayers(leagueName, teamName));

        String sortBy = (String) sortBox.getSelectedItem();
        Comparator<Player> comparator;

        switch (Objects.requireNonNull(sortBy)) {
            case "Position":
                comparator = Comparator
                        .comparing((Player x) -> {
                            String pos = x.getPosition();
                            return pos == null ? "" : pos;
                        }, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Player::getLastName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "Number":
                comparator = Comparator.comparingInt(Player::getNumber);
                break;
            default: // Last Name
                comparator = Comparator
                        .comparing(Player::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Player::getFirstName, String.CASE_INSENSITIVE_ORDER);
        }

        players.sort(comparator);

        for (Player p : players) {
            model.addElement(String.format("%s %s — %s (#%d)",
                    p.getFirstName(), p.getLastName(),
                    p.getPosition() == null ? "" : p.getPosition(),
                    p.getNumber()));
        }
    }

    // --- The following handlers are unchanged functionally; they simply won't be wired in readOnly mode ---

    private void onAddPlayer() {
        JTextField first = new JTextField();
        JTextField last = new JTextField();
        JTextField position = new JTextField();
        JTextField numberField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.add(new JLabel("First Name:")); panel.add(first);
        panel.add(new JLabel("Last Name:")); panel.add(last);
        panel.add(new JLabel("Position:")); panel.add(position);
        panel.add(new JLabel("Jersey Number:")); panel.add(numberField);

        int ok = JOptionPane.showConfirmDialog(this, panel, "Add Player", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            int number = Integer.parseInt(numberField.getText().trim());
            boolean added = ctrl.addPlayer(leagueName, teamName,
                    first.getText().trim(), last.getText().trim(), position.getText().trim(), number);
            if (!added) {
                JOptionPane.showMessageDialog(this, "Duplicate number or invalid data.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid jersey number.");
        }
        refresh();
    }

    private void onEditPlayer() {
        int idx = list.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select a player first.");
            return;
        }

        String selected = list.getSelectedValue();
        var state = AppState.getInstance();
        var team = state.getLeagues().get(leagueName).getTeams().get(teamName);
        Player p = findByDisplayString(selected, team.getPlayers());
        if (p == null) return;

        JTextField first = new JTextField(p.getFirstName());
        JTextField last = new JTextField(p.getLastName());
        JTextField position = new JTextField(p.getPosition());
        JTextField numberField = new JTextField(String.valueOf(p.getNumber()));

        JPanel panel = new JPanel(new GridLayout(0,1));
        panel.add(new JLabel("First Name:")); panel.add(first);
        panel.add(new JLabel("Last Name:")); panel.add(last);
        panel.add(new JLabel("Position:")); panel.add(position);
        panel.add(new JLabel("Jersey Number:")); panel.add(numberField);

        int ok = JOptionPane.showConfirmDialog(this, panel, "Edit Player", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            int number = Integer.parseInt(numberField.getText().trim());
            // use controller update (safer) if you adopted it; otherwise this direct approach:
            p.setFirstName(first.getText().trim());
            p.setLastName(last.getText().trim());
            p.setPosition(position.getText().trim());
            p.setNumber(number);
            state.save();
            refresh();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid jersey number.");
        }
    }

    private void onDeletePlayer() {
        int idx = list.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select a player first.");
            return;
        }

        String selected = list.getSelectedValue();
        var state = AppState.getInstance();
        var team = state.getLeagues().get(leagueName).getTeams().get(teamName);
        Player p = findByDisplayString(selected, team.getPlayers());
        if (p == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete player '" + selected + "'?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        team.getPlayers().remove(p);
        state.save();
        refresh();
    }

    private void onMovePlayer() {
        int idx = list.getSelectedIndex();
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Select a player first.");
            return;
        }

        String selected = list.getSelectedValue();
        var state = AppState.getInstance();
        var league = state.getLeagues().get(leagueName);

        var sourceTeam = league.getTeams().get(teamName);
        Player p = findByDisplayString(selected, sourceTeam.getPlayers());
        if (p == null) return;

        String[] teams = league.getTeams().keySet().stream()
                .filter(t -> !t.equals(teamName))
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toArray(String[]::new);
        if (teams.length == 0) {
            JOptionPane.showMessageDialog(this, "No other teams to move this player to.");
            return;
        }

        String dest = (String) JOptionPane.showInputDialog(this,
                "Select destination team:", "Move Player",
                JOptionPane.PLAIN_MESSAGE, null, teams, teams[0]);
        if (dest == null) return;

        var destTeam = league.getTeams().get(dest);
        boolean numberExists = destTeam.getPlayers().stream()
                .anyMatch(x -> x.getNumber() == p.getNumber());
        if (numberExists) {
            JOptionPane.showMessageDialog(this, "That number already exists on " + dest);
            return;
        }

        sourceTeam.getPlayers().remove(p);
        destTeam.addPlayer(p);
        state.save();
        JOptionPane.showMessageDialog(this, "Player moved to " + dest);
        refresh();
    }

    private Player findByDisplayString(String display, Collection<Player> players) {
        for (Player p : players) {
            String fmt = String.format("%s %s — %s (#%d)",
                    p.getFirstName(), p.getLastName(), p.getPosition(), p.getNumber());
            if (fmt.equals(display)) return p;
        }
        return null;
    }
}
