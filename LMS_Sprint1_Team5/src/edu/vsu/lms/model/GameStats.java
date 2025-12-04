package edu.vsu.lms.model;

import java.io.Serializable;
import java.util.*;

/**
 * GameStats
 * ---------
 * Holds stats for all players who played in a single Game.
 *
 * Does NOT know about quarters, only full-game totals.
 */
public class GameStats implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Game game;

    // Map: Player -> stats for this game
    private final Map<Player, PlayerGameStats> statsByPlayer = new HashMap<>();

    // Optional: track which team each player was on (by Team name)
    private final Map<Player, String> teamNameByPlayer = new HashMap<>();

    public GameStats(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    /**
     * Get or create stats for a player.
     * Optionally provide the team name (e.g., "Boston Celtics").
     */
    public PlayerGameStats getOrCreatePlayerStats(Player player, String teamName) {
        PlayerGameStats stats = statsByPlayer.get(player);
        if (stats == null) {
            stats = new PlayerGameStats(player);
            statsByPlayer.put(player, stats);
            if (teamName != null) {
                teamNameByPlayer.put(player, teamName);
            }
        } else if (teamName != null) {
            teamNameByPlayer.put(player, teamName);
        }
        return stats;
    }

    public PlayerGameStats getStatsForPlayer(Player player) {
        return statsByPlayer.get(player);
    }

    public Collection<PlayerGameStats> getAllPlayerStats() {
        return statsByPlayer.values();
    }

    /**
     * Returns all stats for players belonging to the given team name.
     */
    public List<PlayerGameStats> getStatsForTeamName(String teamName) {
        List<PlayerGameStats> result = new ArrayList<>();
        for (Map.Entry<Player, PlayerGameStats> e : statsByPlayer.entrySet()) {
            Player p = e.getKey();
            String tn = teamNameByPlayer.get(p);
            if (tn != null && tn.equalsIgnoreCase(teamName)) {
                result.add(e.getValue());
            }
        }
        return result;
    }

    /**
     * Returns all stats sorted ascending by last name, then first, then jersey number.
     */
    public List<PlayerGameStats> getAllStatsSorted() {
        List<PlayerGameStats> list = new ArrayList<>(statsByPlayer.values());
        list.sort(Comparator
                .comparing((PlayerGameStats s) -> s.getPlayer().getLastName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(s -> s.getPlayer().getFirstName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(s -> s.getPlayer().getNumber()));
        return list;
    }

    /**
     * Returns stats for a team, sorted by last, first, jersey num.
     */
    public List<PlayerGameStats> getTeamStatsSorted(String teamName) {
        List<PlayerGameStats> list = getStatsForTeamName(teamName);
        list.sort(Comparator
                .comparing((PlayerGameStats s) -> s.getPlayer().getLastName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(s -> s.getPlayer().getFirstName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(s -> s.getPlayer().getNumber()));
        return list;
    }
}
