package edu.vsu.lms.model;
import java.io.*;
import java.io.Serializable;

public class Team implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private final String name;
    private int wins=0, losses=0;
    public Team(String name){ this.name = name; }
    public String getName(){ return name; }
    public int getWins(){ return wins; }
    public int getLosses(){ return losses; }
    public void addWin(){ wins++; }
    public void addLoss(){ losses++; }
}
