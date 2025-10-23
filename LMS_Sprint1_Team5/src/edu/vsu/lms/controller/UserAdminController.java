package edu.vsu.lms.controller;

import java.util.*;
import java.util.stream.Collectors;
import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.Role;
import edu.vsu.lms.model.User;
import edu.vsu.lms.util.Passwords;

public class UserAdminController {

    private final AppState state = AppState.getInstance();

    public boolean addUser(String id, String first, String last, Role role, String passwordPlain) {
        if (state.getUsers().containsKey(id)) return false;
        if (!Passwords.isStrong(passwordPlain)) return false;
        String hash = Passwords.hash(passwordPlain);
        User u = new User(id, first, last, role, hash, false);
        state.getUsers().put(id, u);
        state.save();
        return true;
    }

    public List<User> listUsersSorted() {
        return state.getUsers().values().stream()
            .sorted(Comparator.comparing(User::getLastName)
                .thenComparing(User::getFirstName)
                .thenComparing(User::getId))
            .collect(Collectors.toList());
    }

    public boolean suspendUser(String id) {
        User u = state.getUsers().get(id);
        if (u == null) return false;
        u.setSuspended(true);
        state.save();
        return true;
    }

    public boolean reinstateUser(String id) {
        User u = state.getUsers().get(id);
        if (u == null) return false;
        u.setSuspended(false);
        state.save();
        return true;
    }

}
