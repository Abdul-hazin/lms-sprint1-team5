package edu.vsu.lms.view;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.vsu.lms.controller.TeamController;
import edu.vsu.lms.model.Team;
import edu.vsu.lms.persistence.AppState;

public class TeamsPanel extends JPanel {

    private static final String NO_TEAMS_PLACEHOLDER = "(No teams found)";
    private static final String NO_LEAGUE_PLACEHOLDER = "(No league selected)";

    private final TeamController ctrl = new TeamController();
    private final DefaultListModel<String> model = new DefaultListModel<>();
    private final JList<String> list = new JList<>(model);

    private final boolean readOnly;

    private String currentLeague;
    private String filterText = "";

    // UI bits
    private JLabel headerLabel;
    private JButton addBtn, deleteBtn, refreshBtn, reloadLeaguesBtn, playersBtn;
    private JComboBox<String> leagueBox;
    private JPanel headerRight;

    // ---------- CONSTRUCTORS ----------

    public TeamsPanel() {
        this(false);
    }

    public TeamsPanel(boolean readOnly) {
        this.readOnly = readOnly;
        initUI();
        populateLeagueBox(null);

        // If there is at least one league, select first and load teams
        if (leagueBox.getItemCount() > 0) {
            String firstLeague = (String) leagueBox.getItemAt(0);
            loadTeamsForLeague(firstLeague);
        } else {
            currentLeague = null;
            refresh(false);
        }
    }

    // 👇 compatibility constructor for TeamOfficialPanel
    public TeamsPanel(boolean readOnly, boolean embeddedMode) {
        this(readOnly);     // we currently ignore embeddedMode
    }

    // ---------- UI SETUP ----------

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ---- Header ----
        JPanel header = new JPanel(new BorderLayout(8, 0));
        headerLabel = new JLabel("Teams");
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 16f));

        headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerRight.add(new JLabel("League:"));
        leagueBox = new JComboBox<>();
        leagueBox.setPrototypeDisplayValue("Select a league with a long name");
        headerRight.add(leagueBox);

        reloadLeaguesBtn = new JButton("Reload Leagues");
        headerRight.add(reloadLeaguesBtn);

        refreshBtn = new JButton("Refresh");
        headerRight.add(refreshBtn);

        header.add(headerLabel, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ---- Center (search + list) ----
        JPanel center = new JPanel(new BorderLayout(6, 6));
        JTextField search = new JTextField();
        search.putClientProperty("JTextField.placeholderText", "Filter teams...");
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { onFilterChanged(search.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { onFilterChanged(search.getText()); }
            @Override public void changedUpdate(DocumentEvent e) { onFilterChanged(search.getText()); }
        });
        center.add(search, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String text = String.valueOf(value);
                if (NO_TEAMS_PLACEHOLDER.equals(text) || NO_LEAGUE_PLACEHOLDER.equals(text)) {
                    lbl.setForeground(lbl.getForeground().darker());
                    lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
                }
                return lbl;
            }
        });

        // Double-click to open Players
        list.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    onViewPlayers();
                }
            }
        });

        // Enable/disable Players button based on selection
        list.addListSelectionListener(e -> updateButtonsForSelection());

        center.add(new JScrollPane(list), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        // ---- Bottom actions ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        addBtn = new JButton("Add Team");
        deleteBtn = new JButton("Delete Selected");
        playersBtn = new JButton("Players");
        bottom.add(playersBtn);
        bottom.add(addBtn);
        bottom.add(deleteBtn);
        add(bottom, BorderLayout.SOUTH);

        if (readOnly) {
            addBtn.setVisible(false);
            deleteBtn.setVisible(false);
        }

        // Actions / shortcuts
        addBtn.addActionListener(e -> onAddTeam());
        deleteBtn.addActionListener(e -> onDeleteTeam());
        refreshBtn.addActionListener(e -> refresh(true));
        playersBtn.addActionListener(e -> onViewPlayers());

        leagueBox.addActionListener(e -> {
            String selected = (String) leagueBox.getSelectedItem();
            if (selected != null && !selected.isBlank() && !selected.equals(currentLeague)) {
                loadTeamsForLeague(selected);
            }
        });

        reloadLeaguesBtn.addActionListener(e -> {
            populateLeagueBox(currentLeague);
            if (currentLeague != null) {
                loadTeamsForLeague(currentLeague);
            }
        });

        // Delete key
        list.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                "deleteTeam");
        list.getActionMap().put("deleteTeam", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!readOnly) onDeleteTeam();
            }
        });

        // Ctrl+N add
        list.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_N,
                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
                "addTeam");
        list.getActionMap().put("addTeam", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!readOnly) onAddTeam();
            }
        });

        updateButtonsForSelection();
    }

    // ---------- HELPERS ----------

    private void updateButtonsForSelection() {
        boolean hasTeam = getSelectedTeam() != null;
        playersBtn.setEnabled(hasTeam);
        if (!readOnly) {
            deleteBtn.setEnabled(hasTeam && list.isEnabled());
            addBtn.setEnabled(currentLeague != null && !currentLeague.isBlank());
        }
        if (refreshBtn != null) refreshBtn.setEnabled(true);
        if (reloadLeaguesBtn != null) reloadLeaguesBtn.setEnabled(true);
    }

    /** Populate leagueBox from AppState; attempts to keep current selection. */
    private void populateLeagueBox(String preferSelect) {
        var leagues = AppState.getInstance().getLeagues().keySet().stream()
                .sorted(String::compareToIgnoreCase)
                .toArray(String[]::new);

        leagueBox.removeAllItems();
        for (String l : leagues) leagueBox.addItem(l);

        if (preferSelect != null && !preferSelect.isBlank()) {
            leagueBox.setSelectedItem(preferSelect);
        } else if (leagueBox.getItemCount() > 0) {
            leagueBox.setSelectedIndex(0);
        }
    }

    // ---------- MAIN FLOW ----------

    /** Load teams for a given league name. */
    public void loadTeamsForLeague(String leagueName) {
        currentLeague = (leagueName == null || leagueName.isBlank()) ? null : leagueName.trim();
        updateHeader();

        if (currentLeague != null &&
                (leagueBox.getSelectedItem() == null || !currentLeague.equals(leagueBox.getSelectedItem()))) {
            leagueBox.setSelectedItem(currentLeague);
        }

        refresh(false);
    }

    private void updateHeader() {
        String title = "Teams";
        title += (currentLeague == null) ? " — " + NO_LEAGUE_PLACEHOLDER : " — " + currentLeague;
        headerLabel.setText(title);
    }

    private void onFilterChanged(String text) {
        filterText = (text == null) ? "" : text.trim();
        refresh(true);
    }

    private void onAddTeam() {
        if (currentLeague == null || currentLeague.isBlank()) {
            JOptionPane.showMessageDialog(this, "Select a league first.");
            return;
        }

        String name = promptForTeamName();
        if (name == null) return;
        if (!isValidTeamName(name)) {
            JOptionPane.showMessageDialog(this,
                    "Team name must be 3–30 characters (letters, numbers, spaces, - ' &).",
                    "Invalid Name", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = ctrl.createTeam(currentLeague, name);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Duplicate or invalid team name.");
        }
        refresh(true);
        selectByName(name);
    }

    private void onDeleteTeam() {
        String selected = list.getSelectedValue();
        if (selected == null || isPlaceholder(selected)) {
            JOptionPane.showMessageDialog(this, "Select a team to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete team '" + selected + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = ctrl.deleteTeam(currentLeague, selected);
        if (!ok) JOptionPane.showMessageDialog(this, "Failed to delete team.");
        refresh(true);
    }

    /** Open PlayersPanel for the selected team */
    private void onViewPlayers() {
        String team = getSelectedTeam();
        if (currentLeague == null || currentLeague.isBlank()) {
            JOptionPane.showMessageDialog(this, "Select a league first.");
            return;
        }
        if (team == null) {
            JOptionPane.showMessageDialog(this, "Select a team first.");
            return;
        }

        JDialog d = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Players — " + team,
                Dialog.ModalityType.APPLICATION_MODAL);
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        d.setContentPane(new PlayersPanel(currentLeague, team, this.readOnly));
        d.pack();
        d.setSize(560, 460);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    /** Refresh the list of teams for the current league */
    private void refresh(boolean preserveSelection) {
        String previouslySelected = preserveSelection ? list.getSelectedValue() : null;

        model.clear();

        if (currentLeague == null || currentLeague.isBlank()) {
            model.addElement(NO_LEAGUE_PLACEHOLDER);
            list.setEnabled(false);
            updateButtonsForSelection();
            return;
        }

        List<Team> teams = ctrl.listTeams(currentLeague);
        if (teams == null || teams.isEmpty()) {
            model.addElement(NO_TEAMS_PLACEHOLDER);
            list.setEnabled(false);
            updateButtonsForSelection();
            return;
        }

        List<String> names = teams.stream()
                .map(Team::getName)
                .filter(Objects::nonNull)
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());

        if (!filterText.isBlank()) {
            String needle = filterText.toLowerCase();
            names = names.stream()
                    .filter(n -> n.toLowerCase().contains(needle))
                    .collect(Collectors.toList());
        }

        if (names.isEmpty()) {
            model.addElement(NO_TEAMS_PLACEHOLDER);
            list.setEnabled(false);
        } else {
            names.forEach(model::addElement);
            list.setEnabled(true);
        }

        if (preserveSelection && previouslySelected != null) {
            selectByName(previouslySelected);
        } else if (model.size() > 0 && !isPlaceholder(model.get(0))) {
            list.setSelectedIndex(0);
        }

        updateButtonsForSelection();
    }

    private boolean isValidTeamName(String name) {
        return name != null && name.matches("[A-Za-z0-9 '\\-&]{3,30}");
    }

    private boolean isPlaceholder(String s) {
        return NO_TEAMS_PLACEHOLDER.equals(s) || NO_LEAGUE_PLACEHOLDER.equals(s);
    }

    private String promptForTeamName() {
        String input = JOptionPane.showInputDialog(this, "Team name:");
        if (input == null) return null;
        input = input.trim().replaceAll("\\s{2,}", " ");
        return input.isEmpty() ? null : input;
    }

    private void selectByName(String name) {
        for (int i = 0; i < model.size(); i++) {
            if (name.equalsIgnoreCase(model.get(i))) {
                list.setSelectedIndex(i);
                list.ensureIndexIsVisible(i);
                break;
            }
        }
    }

    public String getSelectedTeam() {
        String v = list.getSelectedValue();
        return (v == null || isPlaceholder(v)) ? null : v;
    }

    protected String getCurrentLeague() {
        return currentLeague;
    }

    public void forceRefresh() {
        refresh(true);
    }

    public void forceReloadLeagues() {
        populateLeagueBox(currentLeague);
    }
}


