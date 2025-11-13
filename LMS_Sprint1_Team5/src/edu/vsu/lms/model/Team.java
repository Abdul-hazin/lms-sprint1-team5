package edu.vsu.lms.model;

import java.io.Serializable;
import java.util.*;

public class Team implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private int wins = 0;
    private int losses = 0;
    private final Map<Integer, Player> players = new HashMap<>();

    public Team(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public int getWins() { return wins; }
    public int getLosses() { return losses; }

    public void addWin() { wins++; }
    public void addLoss() { losses++; }

    // ✅ Add player safely (prevents duplicate numbers)
    public boolean addPlayer(Player p) {
        if (p == null || players.containsKey(p.getNumber())) {
            return false;
        }
        players.put(p.getNumber(), p);
        return true;
    }

    // ✅ Remove a player
    public boolean removePlayer(Player p) {
        if (p == null) return false;
        return players.remove(p.getNumber()) != null;
    }

    // ✅ Get all players (sorted)
    public Collection<Player> getPlayers() {
        return players.values();
    }

    // ✅ Find a player by jersey number
    public Player findPlayerByNumber(int number) {
        return players.get(number);
    }

    // ✅ Optional: display string
    @Override
    public String toString() {
        return String.format("%s (W:%d L:%d, %d players)", name, wins, losses, players.size());
    }
}
