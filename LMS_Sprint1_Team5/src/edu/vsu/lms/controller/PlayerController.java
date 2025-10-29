package edu.vsu.lms.controller;

import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.*;

import java.util.*;

public class PlayerController {
    private final AppState state = AppState.getInstance();

    public boolean addPlayer(String leagueName, String teamName,
                             String firstName, String lastName,
                             String position, int number) {
        League league = state.getLeagues().get(leagueName);
        if (league == null) return false;

        Team team = league.getTeams().get(teamName);
        if (team == null) return false;

        // check for duplicate jersey number within team
        for (Player p : team.getPlayers()) {
            if (p.getNumber() == number) {
                return false; // jersey number must be unique in team
            }
        }

        boolean ok = team.addPlayer(new Player(firstName, lastName, number, position));
        if (ok) state.save();
        return ok;
    }

    public List<Player> listPlayers(String leagueName, String teamName) {
        League league = state.getLeagues().get(leagueName);
        if (league == null) return List.of();

        Team team = league.getTeams().get(teamName);
        if (team == null) return List.of();

        // Sort players: last name → first name → jersey number
        List<Player> players = new ArrayList<>(team.getPlayers());
        players.sort(Comparator
                .comparing(Player::getLastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(Player::getFirstName, String.CASE_INSENSITIVE_ORDER)
                .thenComparingInt(Player::getNumber));
        return players;
    }

    public Optional<Player> findPlayer(String leagueName, String teamName, int number) {
        League league = state.getLeagues().get(leagueName);
        if (league == null) return Optional.empty();

        Team team = league.getTeams().get(teamName);
        if (team == null) return Optional.empty();

        return team.getPlayers().stream()
                .filter(p -> p.getNumber() == number)
                .findFirst();
    }

    public boolean removePlayer(String leagueName, String teamName, int number) {
        League league = state.getLeagues().get(leagueName);
        if (league == null) return false;

        Team team = league.getTeams().get(teamName);
        if (team == null) return false;

        boolean removed = team.getPlayers().removeIf(p -> p.getNumber() == number);
        if (removed) state.save();
        return removed;
    }
}