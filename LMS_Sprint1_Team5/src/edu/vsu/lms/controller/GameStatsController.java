package edu.vsu.lms.controller;

import edu.vsu.lms.model.*;

import java.io.Serializable;
import java.util.*;

/**
 * GameStatsController
 * -------------------
 * Facade for recording and querying game stats.
 *
 * Typical usage from the UI:
 *  - getOrCreateGameStats(game)
 *  - recordFreeThrows(game, teamName, player, att, made)
 *  - recordTwoPointers(...)
 *  - recordThreePointers(...)
 *  - recordAssists(...)
 *  - recordFouls(...)
 *  - getSortedStatsForTeam(game, "Boston Celtics")
 *
 * This controller keeps stats in memory; you can later hook it into AppState
 * or persistence (save/load to file) as needed.
 */
public class GameStatsController implements Serializable {
    
    private static final long serialVersionUID = 1L;
    // You can also move this into AppState if you want stats to be global.
    private final Map<Game, GameStats> statsByGame = new HashMap<>();

    /** Get existing GameStats or create a new one for this game. */
    public GameStats getOrCreateGameStats(Game game) {
        return statsByGame.computeIfAbsent(game, GameStats::new);
    }

    public GameStats getGameStats(Game game) {
        return statsByGame.get(game);
    }

    // ---------- Record events ----------

    public void recordFreeThrows(Game game, String teamName, Player player,
                                 int attempted, int made) {
        GameStats gs = getOrCreateGameStats(game);
        PlayerGameStats pgs = gs.getOrCreatePlayerStats(player, teamName);
        pgs.addFreeThrows(attempted, made);
    }

    public void recordTwoPointers(Game game, String teamName, Player player,
                                  int attempted, int made) {
        GameStats gs = getOrCreateGameStats(game);
        PlayerGameStats pgs = gs.getOrCreatePlayerStats(player, teamName);
        pgs.addTwoPointers(attempted, made);
    }

    public void recordThreePointers(Game game, String teamName, Player player,
                                    int attempted, int made) {
        GameStats gs = getOrCreateGameStats(game);
        PlayerGameStats pgs = gs.getOrCreatePlayerStats(player, teamName);
        pgs.addThreePointers(attempted, made);
    }

    public void recordAssists(Game game, String teamName, Player player,
                              int assistsToAdd) {
        GameStats gs = getOrCreateGameStats(game);
        PlayerGameStats pgs = gs.getOrCreatePlayerStats(player, teamName);
        pgs.addAssists(assistsToAdd);
    }

    public void recordFouls(Game game, String teamName, Player player,
                            int foulsToAdd) {
        GameStats gs = getOrCreateGameStats(game);
        PlayerGameStats pgs = gs.getOrCreatePlayerStats(player, teamName);
        pgs.addFouls(foulsToAdd);
        // if (pgs.isFouledOut()) -> UI can handle "fouled out" logic
    }

    // ---------- Queries for View Game Stats ----------

    /**
     * Returns sorted stats for a given team in this game:
     * ascending by last name, then first, then jersey number.
     */
    public List<PlayerGameStats> getSortedStatsForTeam(Game game, String teamName) {
        GameStats gs = statsByGame.get(game);
        if (gs == null) return Collections.emptyList();
        return gs.getTeamStatsSorted(teamName);
    }

    /**
     * Returns all stats in this game, sorted by last/first/jersey.
     */
    public List<PlayerGameStats> getAllSortedStats(Game game) {
        GameStats gs = statsByGame.get(game);
        if (gs == null) return Collections.emptyList();
        return gs.getAllStatsSorted();
    }

    /**
     * Helper to compute a team's total points in this game.
     */
    public int getTeamTotalPoints(Game game, String teamName) {
        GameStats gs = statsByGame.get(game);
        if (gs == null) return 0;

        int sum = 0;
        for (PlayerGameStats pgs : gs.getStatsForTeamName(teamName)) {
            sum += pgs.getTotalPoints();
        }
        return sum;
    }
}
