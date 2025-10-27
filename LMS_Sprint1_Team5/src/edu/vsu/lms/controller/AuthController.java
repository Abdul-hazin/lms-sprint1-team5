package edu.vsu.lms.controller;

import edu.vsu.lms.persistence.AppState;
import edu.vsu.lms.model.User;
import edu.vsu.lms.util.Passwords;
import edu.vsu.lms.model.Role;

public class AuthController {

    private final AppState state = AppState.getInstance();
    private User currentUser;

    public User getCurrentUser() { return currentUser; }

    public boolean login(String id, String passwordPlain) {
        User u = state.getUsers().get(id);
        if (u == null) return false;
        if (u.isSuspended()) return false;
        String hash = Passwords.hash(passwordPlain);
        if (!hash.equals(u.getPasswordHash())) return false;
        currentUser = u;
        return true;
    }

    public boolean changeOwnPassword(String oldPlain, String newPlain) {
        if (currentUser == null) return false;
        if (!Passwords.hash(oldPlain).equals(currentUser.getPasswordHash())) return false;
        if (!Passwords.isStrong(newPlain)) return false;
        currentUser.setPasswordHash(Passwords.hash(newPlain));
        return true;
    }

    public boolean adminResetPassword(String userId, String newPlain) {
        if (currentUser == null || currentUser.getRole() != Role.LA) return false;
        User u = state.getUsers().get(userId);
        if (u == null) return false;
        if (!Passwords.isStrong(newPlain)) return false;
        u.setPasswordHash(Passwords.hash(newPlain));
        return true;
    }

    public void logout() {
        currentUser = null;
    }
}
