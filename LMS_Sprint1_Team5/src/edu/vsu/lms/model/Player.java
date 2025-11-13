package edu.vsu.lms.model;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private String firstName;
    private String lastName;
    private String position;
    private int number;
    

    public Player(String firstName, String lastName, int number, String position) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.number = number;
        this.position = position;
    }

    // Getters
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public int getNumber() { return number; }
    public String getPosition() { return position; }

   // Setters
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setNumber(int number) { this.number = number; }
    public void setPosition(String position) { this.position = position; }

    @Override
    public String toString() {
        return "#" + number + " - " + firstName + " " + lastName +
               (position != null && !position.isBlank() ? " (" + position + ")" : "");
    }
}