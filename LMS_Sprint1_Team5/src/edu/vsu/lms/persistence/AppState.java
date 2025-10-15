package edu.vsu.lms.persistence;

import java.util.*;
import edu.vsu.lms.model.*;
import edu.vsu.lms.util.Passwords;

public class AppState {
    private static final AppState INSTANCE = new AppState();
    public static AppState getInstance() { return INSTANCE; }

    private final Map<String, User> users = new HashMap<>();
    public Map<String, User> getUsers() { return users; }

    private final Map<String, League> leagues = new HashMap<>();
    public Map<String, League> getLeagues() { return leagues; }

    private AppState() {}

    public void seedDefaults() {
        if (users.isEmpty()) {
            // Default admin: id=admin, password=Admin!1 (meets policy)
            String hash = Passwords.hash("Admin!1");
            User admin = new User("admin", "Lee", "Admin", Role.LA, hash, false);
            users.put(admin.getId(), admin);
        }

        // Default league so we always have at least one
        if (leagues.isEmpty()) {
            leagues.put("Default League", new League("Default League"));
        }
    }

    public String getOrInitDefaultLeague() {
        leagues.putIfAbsent("Default League", new League("Default League"));
        return "Default League";
    }
}