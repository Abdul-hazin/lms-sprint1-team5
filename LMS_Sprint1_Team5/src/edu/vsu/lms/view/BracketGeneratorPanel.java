package edu.vsu.lms.view;

import edu.vsu.lms.model.League;
import edu.vsu.lms.model.Team;
import edu.vsu.lms.model.Bracket;
import edu.vsu.lms.persistence.AppState;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * BracketGeneratorPanel
 * ---------------------
 * Lets an admin:
 *  - See how many teams are in a league
 *  - Choose how many teams should make the playoff bracket (top N)
 *  - Choose a first-round date
 *  - Preview the seeding order
 *  - Generate a Bracket and attach it to the League
 *
 * Bracket seeding:
 *  - Sorts teams by wins DESC, losses ASC, then name.
 *  - Takes the top N teams for the bracket.
 *  - Uses a single-elimination bracket (power-of-two slots with byes).
 */
public class BracketGeneratorPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final League league;

    private final JLabel lblLeagueName;
    private final JLabel lblTotalTeams;

    private final JSpinner teamCountSpinner;
    private final JTextField firstRoundDateField;
    private final JTextArea previewArea;

    public BracketGeneratorPanel(League league) {
        if (league == null) {
            throw new IllegalArgumentException("League cannot be null for bracket generator.");
        }
        this.league = league;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        /* ===== TOP: league info + controls ===== */
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;

        int totalTeams = league.getTeams().size();

        lblLeagueName = new JLabel("League: " + league.getName());
        lblLeagueName.setFont(lblLeagueName.getFont().deriveFont(Font.BOLD, 16f));

        lblTotalTeams = new JLabel("Total teams in league: " + totalTeams);

        // Spinner: how many teams in the bracket
        int minTeams = Math.min(2, Math.max(2, totalTeams)); // at least 2
        int maxTeams = Math.max(2, totalTeams);
        teamCountSpinner = new JSpinner(new SpinnerNumberModel(
                totalTeams,   // default: all teams
                2,            // minimum
                maxTeams,     // maximum
                1             // step
        ));

        // First-round date input (default: 7 days from today)
        LocalDate defaultDate = LocalDate.now().plusDays(7);
        firstRoundDateField = new JTextField(defaultDate.toString(), 10);

        int row = 0;

        // Row 0: league name
        gc.gridx = 0;
        gc.gridy = row++;
        gc.gridwidth = 2;
        top.add(lblLeagueName, gc);

        // Row 1: total teams
        gc.gridy = row++;
        top.add(lblTotalTeams, gc);

        gc.gridwidth = 1;

        // Row 2: number of teams in bracket
        gc.gridx = 0;
        gc.gridy = row;
        top.add(new JLabel("Teams in bracket (top N):"), gc);

        gc.gridx = 1;
        top.add(teamCountSpinner, gc);
        row++;

        // Row 3: first round date
        gc.gridx = 0;
        gc.gridy = row;
        top.add(new JLabel("First round date (YYYY-MM-DD):"), gc);

        gc.gridx = 1;
        top.add(firstRoundDateField, gc);

        add(top, BorderLayout.NORTH);

        /* ===== CENTER: seeding preview ===== */
        previewArea = new JTextArea(12, 40);
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane(previewArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Bracket Seeding Preview"));

        add(scroll, BorderLayout.CENTER);

        /* ===== BOTTOM: buttons ===== */
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnPreview  = new JButton("Preview Seeding");
        JButton btnGenerate = new JButton("Generate Bracket");
        JButton btnClose    = new JButton("Close");

        bottom.add(btnPreview);
        bottom.add(btnGenerate);
        bottom.add(btnClose);

        add(bottom, BorderLayout.SOUTH);

        /* ===== LISTENERS ===== */

        btnPreview.addActionListener(e -> previewSeeding());
        btnGenerate.addActionListener(e -> generateBracket());
        btnClose.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JDialog d) {
                d.dispose();
            }
        });

        // Initial preview
        previewSeeding();
    }

    // -------------------------------------------------------
    // Seeding preview
    // -------------------------------------------------------

    private void previewSeeding() {
        int n = (Integer) teamCountSpinner.getValue();
        List<Team> seeds = computeTopSeeds(n);

        StringBuilder sb = new StringBuilder();
        sb.append("Bracket will use top ").append(seeds.size()).append(" team(s):\n\n");

        int seedNumber = 1;
        for (Team t : seeds) {
            sb.append(String.format("Seed %2d: %-20s  (W: %d  L: %d)\n",
                    seedNumber++,
                    t.getName(),
                    t.getWins(),
                    t.getLosses()));
        }

        sb.append("\nNote:\n");
        sb.append("- Teams are sorted by wins DESC, losses ASC, then name.\n");
        sb.append("- If the number of teams is not a power of two,\n");
        sb.append("  top seeds will effectively receive BYEs in Round 1.\n");

        previewArea.setText(sb.toString());
    }

    /**
     * Sorts league teams and returns the top N as the seeds list.
     */
    private List<Team> computeTopSeeds(int count) {
        List<Team> all = new ArrayList<>(league.getTeams().values());

        all.sort(Comparator
                .comparingInt(Team::getWins).reversed()
                .thenComparingInt(Team::getLosses)
                .thenComparing(Team::getName, String.CASE_INSENSITIVE_ORDER));

        if (count > all.size()) {
            count = all.size();
        }
        return new ArrayList<>(all.subList(0, count));
    }

    // -------------------------------------------------------
    // Bracket generation
    // -------------------------------------------------------

    private void generateBracket() {
        int n = (Integer) teamCountSpinner.getValue();
        if (n < 2) {
            JOptionPane.showMessageDialog(this,
                    "Bracket must have at least 2 teams.",
                    "Invalid Size",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (n > league.getTeams().size()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot have more teams in the bracket than exist in the league.",
                    "Invalid Size",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate firstRoundDate;
        try {
            firstRoundDate = LocalDate.parse(firstRoundDateField.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Use YYYY-MM-DD.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Team> seeds = computeTopSeeds(n);

        // Build a bracket from just these seeds
        Bracket bracket = createBracketFromSeeds(seeds, firstRoundDate);

        // 🔥 Attach to league (adjust if your setter has a different name)
        league.setBracket(bracket);

        // Optional: persist via AppState if you like
        AppState.getInstance().save();

        JOptionPane.showMessageDialog(this,
                "Bracket generated for league \"" + league.getName() + "\"\n" +
                        "Teams in bracket: " + seeds.size() + "\n" +
                        "First round date: " + firstRoundDate,
                "Bracket Created",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Creates a single-elimination bracket from an explicitly provided
     * seed list (already sorted best to worst).
     *
     * This mirrors the logic in Bracket.createSingleEliminationBracket,
     * but works from a List<Team> instead of reading from the full league.
     */
    private Bracket createBracketFromSeeds(List<Team> seeds, LocalDate firstRoundDate) {
        if (seeds == null || seeds.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 teams to create a bracket.");
        }
        if (firstRoundDate == null) {
            throw new IllegalArgumentException("firstRoundDate cannot be null");
        }

        // We assume seeds are already sorted correctly.
        int n = seeds.size();
        int totalSlots = nextPowerOfTwo(n);
        int numByes = totalSlots - n;

        // Copy into slot list and add nulls as BYEs
        List<Team> slotList = new ArrayList<>(seeds);
        for (int i = 0; i < numByes; i++) {
            slotList.add(null); // BYE
        }

        Bracket bracket = new Bracket();

        // Round 1
        var round1 = new edu.vsu.lms.model.BracketRound(1, firstRoundDate);
        List<Integer> nextRoundSlots = new ArrayList<>();
        List<edu.vsu.lms.model.Game> byeGames = new ArrayList<>();

        int left = 0;
        int right = slotList.size() - 1;
        while (left < right) {
            Team t1 = slotList.get(left);
            Team t2 = slotList.get(right);

            if (t1 != null && t2 != null) {
                // real game
                edu.vsu.lms.model.Game g =
                        new edu.vsu.lms.model.Game(firstRoundDate, t1.getName(), t2.getName());
                round1.addGame(g);
                nextRoundSlots.add(1);
            } else if (t1 != null || t2 != null) {
                // bye case – auto-advance this team
                Team adv = (t1 != null) ? t1 : t2;
                edu.vsu.lms.model.Game g =
                        new edu.vsu.lms.model.Game(firstRoundDate, adv.getName(), "BYE");
                round1.addGame(g);
                byeGames.add(g);
                nextRoundSlots.add(1);
            }
            left++;
            right--;
        }

        bracket.addRound(round1);

        // Subsequent rounds: TBD vs TBD skeleton
        int teamsNextRound = nextRoundSlots.size();
        int roundNumber = 2;
        LocalDate roundDate = firstRoundDate.plusDays(7);

        while (teamsNextRound > 1) {
            int gamesThisRound = teamsNextRound / 2;

            edu.vsu.lms.model.BracketRound r =
                    new edu.vsu.lms.model.BracketRound(roundNumber, roundDate);

            for (int i = 0; i < gamesThisRound; i++) {
                edu.vsu.lms.model.Game g =
                        new edu.vsu.lms.model.Game(roundDate, "TBD", "TBD");
                r.addGame(g);
            }

            bracket.addRound(r);

            teamsNextRound = gamesThisRound;
            roundNumber++;
            roundDate = roundDate.plusDays(7);
        }

        // Auto-advance BYE winners into next round
        for (edu.vsu.lms.model.Game byeGame : byeGames) {
            String home = byeGame.getHomeTeam();
            String away = byeGame.getAwayTeam();

            String winner;
            if (!"BYE".equals(home)) {
                winner = home;
            } else if (!"BYE".equals(away)) {
                winner = away;
            } else {
                continue;
            }

            // Mark the game as completed (0–0, doesn't matter)
            byeGame.setResult(winner, 0, 0);

            // Let Bracket's auto-advance fill in next round slot
            bracket.advanceWinner(byeGame);
        }

        return bracket;
    }

    /** Helper for bracket math: next power of two >= n. */
    private static int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) {
            p *= 2;
        }
        return p;
    }
}
