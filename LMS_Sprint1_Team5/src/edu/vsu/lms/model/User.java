package edu.vsu.lms.model;
import java.io.*;
import java.io.Serializable;


public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String firstName;
    private String lastName;
    private Role role;
    private String passwordHash;
    private boolean suspended;

    public User(String id, String firstName, String lastName, Role role, String passwordHash, boolean suspended) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.passwordHash = passwordHash;
        this.suspended = suspended;
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public Role getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }
    public boolean isSuspended() { return suspended; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setRole(Role role) { this.role = role; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setSuspended(boolean suspended) { this.suspended = suspended; }

    @Override
    public String toString() {
        return String.format("%s, %s (%s) - %s%s", lastName, firstName, id, role, suspended ? " [SUSPENDED]" : "");
    }
}
