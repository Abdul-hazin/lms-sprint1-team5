package edu.vsu.lms.persistence;

import java.util.*;
import edu.vsu.lms.model.User;
import edu.vsu.lms.model.Role;
import edu.vsu.lms.util.Passwords;

public class AppState {
    private static final AppState INSTANCE = new AppState();
    public static AppState getInstance() { return INSTANCE; }

    private final Map<String, User> users = new HashMap<>();
    public Map<String, User> getUsers() { return users; }

    private AppState() {}

    public void seedDefaults() {
        if (users.isEmpty()) {
            // Default admin: id=admin, password=Admin!1 (meets policy)
            String hash = Passwords.hash("Admin!1");
            User admin = new User("admin", "Lee", "Admin", Role.LA, hash, false);
            users.put(admin.getId(), admin);
        }
    }
}
