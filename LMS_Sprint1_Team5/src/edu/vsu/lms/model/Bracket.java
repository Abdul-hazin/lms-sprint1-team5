package edu.vsu.lms.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

/**
 * Bracket
 * -------
 * Represents a single-elimination playoff bracket for a League.
 *
 * - Round 1 uses actual team names based on standings.
 * - Later rounds are scheduled as "TBD vs TBD" games and will be
 *   filled in as results become known.
 *
 * User Story coverage:
 * - US 25: schedule the bracket from league standings
 * - US 26: view the bracket (all rounds, games, dates)
 * - US 27: view bracket results (champion and runner-up)
 */
public class Bracket implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<BracketRound> rounds = new ArrayList<>();

    public Bracket() {}

    /** All rounds in this bracket (read-only view). */
    public List<BracketRound> getRounds() {
        return Collections.unmodifiableList(rounds);
    }

    /** Internal helper to add a round. */
     public void addRound(BracketRound round) {
        if (round != null) {
            rounds.add(round);
        }
    }

    // ----------------------------------------------------------------------
    // US 25: Create a single-elimination bracket from the league standings
    // ----------------------------------------------------------------------

    /**
     * Creates a single-elimination bracket from the current league standings.
     *
     * Assumptions:
     *  - Regular-season games have been played so standings are meaningful.
     *
     * Seeding logic:
     *  - Teams are sorted by wins DESC, losses ASC, then name.
     *  - We find the smallest power-of-two >= number of teams.
     *  - Any "extra" slots become byes for the top seeds.
     *
     * Example:
     *  - 6 teams -> 8-slot bracket -> 2 byes
     *    Seeds 1 and 2 receive byes, Round 1 games are:
     *       (3 vs 6), (4 vs 5)   plus auto-advanced byes for seeds 1 and 2.
     *    Round 2 and later are created as "TBD vs TBD" placeholders.
     */
    public static Bracket createSingleEliminationBracket(League league, LocalDate firstRoundDate) {
        if (league == null) {
            throw new IllegalArgumentException("League cannot be null");
        }
        if (firstRoundDate == null) {
            throw new IllegalArgumentException("firstRoundDate cannot be null");
        }

        // 1. Collect all teams and sort by standings (wins DESC, losses ASC, name ASC)
        List<Team> seeds = new ArrayList<>(league.getTeams().values());
        if (seeds.size() < 2) {
            throw new IllegalStateException("Need at least 2 teams to create a bracket.");
        }

        seeds.sort(Comparator
                .comparingInt(Team::getWins).reversed()
                .thenComparingInt(Team::getLosses)
                .thenComparing(Team::getName, String.CASE_INSENSITIVE_ORDER));

        int n = seeds.size();
        int totalSlots = nextPowerOfTwo(n); // ex: 6 teams -> 8 slots
        int numByes = totalSlots - n;       // ex: 8 - 6 = 2 byes

        // Put null entries so top seeds effectively get byes
        List<Team> slotList = new ArrayList<>(seeds);
        for (int i = 0; i < numByes; i++) {
            slotList.add(null); // acts as a "BYE"
        }

        Bracket bracket = new Bracket();

        // ---------- Round 1: actual games (plus BYE placeholders) ----------
        BracketRound round1 = new BracketRound(1, firstRoundDate);
        List<Integer> nextRoundSlots = new ArrayList<>();
        List<Game> byeGames = new ArrayList<>(); // games where a team gets a BYE

        int left = 0;
        int right = slotList.size() - 1;
        while (left < right) {
            Team t1 = slotList.get(left);
            Team t2 = slotList.get(right);

            if (t1 != null && t2 != null) {
                // Real game: t1 vs t2
                Game g = new Game(firstRoundDate, t1.getName(), t2.getName());
                round1.addGame(g);
                nextRoundSlots.add(1);
            } else if (t1 != null || t2 != null) {
                // One team + one null = bye; schedule a "virtual" BYE game
                Team adv = (t1 != null) ? t1 : t2;
                Game g = new Game(firstRoundDate, adv.getName(), "BYE");
                round1.addGame(g);
                byeGames.add(g);   // we will auto-advance this team
                nextRoundSlots.add(1);
            }
            // if both null, ignore (should not happen in normal bracket math)

            left++;
            right--;
        }

        bracket.addRound(round1);

        // Calculate how many teams go into the next round
        int teamsNextRound = nextRoundSlots.size();

        // ---------- Subsequent rounds: skeleton "TBD vs TBD" games ----------
        int roundNumber = 2;
        LocalDate roundDate = firstRoundDate.plusDays(7); // simple spacing; can tune

        while (teamsNextRound > 1) {
            int gamesThisRound = teamsNextRound / 2;

            BracketRound r = new BracketRound(roundNumber, roundDate);
            for (int i = 0; i < gamesThisRound; i++) {
                Game g = new Game(roundDate, "TBD", "TBD");
                r.addGame(g);      // placeholder
            }
            bracket.addRound(r);

            teamsNextRound = gamesThisRound;
            roundNumber++;
            roundDate = roundDate.plusDays(7);
        }

        // ---------- Auto-advance BYE teams into Round 2 ----------
        for (Game byeGame : byeGames) {
            String home = byeGame.getHomeTeam();
            String away = byeGame.getAwayTeam();

            String winner;
            if (!"BYE".equals(home)) {
                winner = home;
            } else if (!"BYE".equals(away)) {
                winner = away;
            } else {
                continue; // weird edge, both BYE
            }

            // Mark the BYE game as completed (score doesn't really matter)
            byeGame.setResult(winner, 0, 0);

            // Feed the winner into the next round's TBD slot
            bracket.advanceWinner(byeGame);
        }

        return bracket;
    }

    private static int nextPowerOfTwo(int n) {
        int p = 1;
        while (p < n) {
            p *= 2;
        }
        return p;
    }

    // ----------------------------------------------------------------------
    // AUTO-ADVANCE: winner progression into later rounds
    // ----------------------------------------------------------------------

    /**
     * After a game in an earlier round has a result, this will automatically
     * place the winner into the appropriate "TBD" slot in the next round.
     */
    public void advanceWinner(Game completedGame) {
        if (completedGame == null || !completedGame.hasResult()) return;

        // Iterate over all rounds except the last
        for (int i = 0; i < rounds.size() - 1; i++) {
            BracketRound current = rounds.get(i);

            int idx = current.getGames().indexOf(completedGame);
            if (idx == -1) {
                // This round doesn't contain the game
                continue;
            }

            // This game's winner feeds into the next round
            BracketRound next = rounds.get(i + 1);
            int nextGameIndex = idx / 2; // every 2 games feed into 1 next-round game

            if (nextGameIndex >= next.getGames().size()) {
                return; // nothing to feed into
            }

            Game nextGame = next.getGames().get(nextGameIndex);

            String winner = completedGame.getWinner();
            String home   = nextGame.getHomeTeam();
            String away   = nextGame.getAwayTeam();

            // We don't have setters, so we replace the Game instance entirely
            if ("TBD".equals(home)) {
                Game replacement = new Game(
                        nextGame.getDate(),
                        winner,
                        away
                );
                next.getGames().set(nextGameIndex, replacement);

            } else if ("TBD".equals(away)) {
                Game replacement = new Game(
                        nextGame.getDate(),
                        home,
                        winner
                );
                next.getGames().set(nextGameIndex, replacement);
            }

            return; // once advanced, we’re done
        }
    }

    // ----------------------------------------------------------------------
    // Round Labels (for nicer display: Quarterfinals, Semifinals, Final)
    // ----------------------------------------------------------------------

    /**
     * Returns a human-friendly label for a given round number.
     * e.g., "Quarterfinals", "Semifinals", "Final", or "Round X".
     */
    public String getRoundLabel(int roundNumber) {
        int totalRounds = rounds.size();
        int roundsLeft = totalRounds - roundNumber;

        return switch (roundsLeft) {
            case 2 -> "Quarterfinals";
            case 1 -> "Semifinals";
            case 0 -> "Final";
            default -> "Round " + roundNumber;
        };
    }

    // ----------------------------------------------------------------------
    // US 26: Viewing the bracket
    // ----------------------------------------------------------------------

    /**
     * Returns a human-readable description of the bracket:
     * all rounds, dates, and games.
     */
    public String formatBracket() {
        if (rounds.isEmpty()) {
            return "No bracket has been scheduled.";
        }
        StringBuilder sb = new StringBuilder();
        for (BracketRound r : rounds) {
            sb.append(getRoundLabel(r.getRoundNumber()))
              .append(" (").append(r.getDate()).append(")\n");
            for (Game g : r.getGames()) {
                sb.append("  ")
                  .append(g.getHomeTeam())
                  .append(" vs ")
                  .append(g.getAwayTeam())
                  .append(" — ");
                if (g.hasResult()) {
                    sb.append(g.getWinner())
                      .append(" def ")
                      .append(g.getLoser())
                      .append(" (")
                      .append(g.getWinningScore())
                      .append("-")
                      .append(g.getLosingScore())
                      .append(")");
                } else {
                    sb.append("Not played yet");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ----------------------------------------------------------------------
    // US 27: Bracket results (champion & runner-up)
    // ----------------------------------------------------------------------

    /**
     * @return true if the bracket has a completed final round
     * (all games in the last round have results).
     */
    public boolean isComplete() {
        if (rounds.isEmpty()) return false;
        BracketRound last = rounds.get(rounds.size() - 1);
        for (Game g : last.getGames()) {
            if (!g.hasResult()) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return the name of the champion team, or null if bracket not complete.
     */
    public String getChampionTeamName() {
        if (!isComplete()) return null;
        BracketRound last = rounds.get(rounds.size() - 1);
        if (last.getGames().isEmpty()) return null;
        Game finalGame = last.getGames().get(0);
        return finalGame.getWinner();
    }

    /**
     * @return the name of the runner-up team, or null if bracket not complete.
     */
    public String getRunnerUpTeamName() {
        if (!isComplete()) return null;
        BracketRound last = rounds.get(rounds.size() - 1);
        if (last.getGames().isEmpty()) return null;
        Game finalGame = last.getGames().get(0);
        return finalGame.getLoser();
    }

    /**
     * Formats a short results summary once the bracket has completed.
     */
    public String formatResults(League league) {
        if (!isComplete()) {
            return "Bracket results are not yet available. Some games are still unplayed.";
        }

        String champName = getChampionTeamName();
        String runnerName = getRunnerUpTeamName();

        Team champ = champName != null ? league.getTeams().get(champName) : null;
        Team runner = runnerName != null ? league.getTeams().get(runnerName) : null;

        StringBuilder sb = new StringBuilder();
        sb.append("Bracket Results\n");
        sb.append("====================\n");

        sb.append("Champion: ").append(champName != null ? champName : "Unknown").append("\n");
        if (champ != null) {
            sb.append(" Players:\n");
            for (Player p : champ.getPlayers()) {
                sb.append("  ").append(p.toString()).append("\n");
            }
        }

        sb.append("\nRunner-up: ").append(runnerName != null ? runnerName : "Unknown").append("\n");
        if (runner != null) {
            sb.append(" Players:\n");
            for (Player p : runner.getPlayers()) {
                sb.append("  ").append(p.toString()).append("\n");
            }
        }

        return sb.toString();
    }
}

