package edu.vsu.lms.controller;

import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.*;

import java.util.*;

public class PlayerController {
    private final AppState state = AppState.getInstance();

    // ---------- Helpers ----------

    private League getLeague(String leagueName) {
        if (leagueName == null || leagueName.isBlank()) {
            leagueName = state.getOrInitDefaultLeague();
        }
        return state.getLeagues().get(leagueName);
    }

    private Team getTeam(String leagueName, String teamName) {
        League league = getLeague(leagueName);
        return (league == null) ? null : league.getTeams().get(teamName);
    }

    // ---------- Create / Read ----------

    public boolean addPlayer(String leagueName, String teamName,
                             String firstName, String lastName,
                             String position, int number) {
        Team team = getTeam(leagueName, teamName);
        if (team == null) return false;
        if (firstName == null || firstName.isBlank() ||
            lastName == null || lastName.isBlank() || number < 0) {
            return false;
        }

        // duplicate jersey number check (team-scoped)
        for (Player p : team.getPlayers()) {
            if (p.getNumber() == number) return false;
        }

        boolean ok = team.addPlayer(new Player(firstName.trim(),
                                               lastName.trim(),
                                               number,
                                               position == null ? "" : position.trim()));
        if (ok) state.save();
        return ok;
    }

    public List<Player> listPlayers(String leagueName, String teamName) {
        Team team = getTeam(leagueName, teamName);
        if (team == null) return List.of();

        // defensive copy + stable sort
        List<Player> players = new ArrayList<>(team.getPlayers());
        players.sort(Comparator
                .comparing(Player::getLastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Player::getFirstName, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(Player::getNumber));
        return players;
    }

    public Optional<Player> findPlayer(String leagueName, String teamName, int number) {
        Team team = getTeam(leagueName, teamName);
        if (team == null) return Optional.empty();

        return team.getPlayers().stream()
                .filter(p -> p.getNumber() == number)
                .findFirst();
    }

    // ---------- Update / Delete ----------

    /**
     * Update an existing player identified by originalNumber.
     * Enforces jersey-number uniqueness when the number changes.
     */
    public boolean updatePlayer(String leagueName, String teamName,
                                int originalNumber,
                                String newFirstName, String newLastName,
                                String newPosition, int newNumber) {
        Team team = getTeam(leagueName, teamName);
        if (team == null) return false;

        Player existing = null;
        for (Player p : team.getPlayers()) {
            if (p.getNumber() == originalNumber) {
                existing = p; break;
            }
        }
        if (existing == null) return false;

        // If changing jersey number, ensure it isn't taken
        if (newNumber != originalNumber) {
            for (Player p : team.getPlayers()) {
                if (p.getNumber() == newNumber) return false;
            }
            // Re-key correctly: remove old, set new number, add back
            team.removePlayer(existing);
            existing.setNumber(newNumber);
            // If add fails (shouldn't), roll back and return false
            if (!team.addPlayer(existing)) {
                // rollback to original
                existing.setNumber(originalNumber);
                team.addPlayer(existing);
                return false;
            }
        }

        existing.setFirstName(newFirstName == null ? "" : newFirstName.trim());
        existing.setLastName(newLastName == null ? "" : newLastName.trim());
        existing.setPosition(newPosition == null ? "" : newPosition.trim());

        state.save();
        return true;
    }

    public boolean removePlayer(String leagueName, String teamName, int number) {
        Team team = getTeam(leagueName, teamName);
        if (team == null) return false;

        Player target = null;
        for (Player p : team.getPlayers()) {
            if (p.getNumber() == number) { target = p; break; }
        }
        if (target == null) return false;

        boolean removed = team.removePlayer(target);
        if (removed) state.save();
        return removed;
    }

    // ---------- Move ----------

    /**
     * Move a player (by jersey number) from one team to another within the same league.
     * Prevents jersey-number collisions on the destination.
     */
    public boolean movePlayer(String leagueName, String sourceTeam, String destTeam, int number) {
        Team src = getTeam(leagueName, sourceTeam);
        Team dst = getTeam(leagueName, destTeam);
        if (src == null || dst == null) return false;

        Player p = null;
        for (Player x : src.getPlayers()) {
            if (x.getNumber() == number) { p = x; break; }
        }
        if (p == null) return false;

        for (Player x : dst.getPlayers()) {
            if (x.getNumber() == p.getNumber()) return false; // collision
        }

        // move atomically via Team API
        src.removePlayer(p);
        boolean ok = dst.addPlayer(p);
        if (!ok) { // rollback if unexpected
            src.addPlayer(p);
            return false;
        }

        state.save();
        return true;
    }

    // ---------- Optional UI helpers ----------

    public String toDisplay(Player p) {
        String pos = (p.getPosition() == null || p.getPosition().isBlank()) ? "" : " — " + p.getPosition();
        return String.format("%s %s%s (#%d)", p.getFirstName(), p.getLastName(), pos, p.getNumber());
    }

    public Optional<Player> fromDisplay(String leagueName, String teamName, String display) {
        Team team = getTeam(leagueName, teamName);
        if (team == null) return Optional.empty();
        for (Player p : team.getPlayers()) {
            if (toDisplay(p).equals(display)) return Optional.of(p);
        }
        return Optional.empty();
    }
}
