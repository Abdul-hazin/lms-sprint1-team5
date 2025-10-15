package edu.vsu.lms.model;
public class Team {
    private final String name;
    private int wins=0, losses=0;
    public Team(String name){ this.name = name; }
    public String getName(){ return name; }
    public int getWins(){ return wins; }
    public int getLosses(){ return losses; }
    public void addWin(){ wins++; }
    public void addLoss(){ losses++; }
}
