package edu.vsu.lms.persistence;

import java.io.*;
import java.util.*;
import edu.vsu.lms.model.*;
import edu.vsu.lms.util.Passwords;

public class AppState implements Serializable {

    private static final long serialVersionUID = 1L;

     private static final String SAVE_FILE = System.getProperty("user.dir") + File.separator + "appstate.ser";

    private static final AppState INSTANCE = load();
    public static AppState getInstance() { return INSTANCE; }

    private final Map<String, User> users = new HashMap<>();
    public Map<String, User> getUsers() { return users; }

    private final Map<String, League> leagues = new HashMap<>();
    public Map<String, League> getLeagues() { return leagues; }

    private AppState() {
        
    }

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
    public void save() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(this);
            System.out.println("✅ Saved " + users.size() + " users and " + leagues.size() + " leagues to " + SAVE_FILE);
        } catch (IOException e) {
            System.err.println("❌ Error saving AppState:");
            e.printStackTrace();
        }
    }


    private static AppState load() {
    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
        AppState loaded = (AppState) in.readObject();
        System.out.println("✅ AppState loaded from file.");
        return loaded;
    } catch (IOException | ClassNotFoundException e) {
        System.out.println("⚠️ No saved AppState found. Starting fresh...");
        AppState fresh = new AppState();
        fresh.seedDefaults(); // only seed on first run
        return fresh;
    }
}
}

