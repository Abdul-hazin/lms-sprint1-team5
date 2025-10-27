package edu.vsu.lms.model;
import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.io.Serializable;

public class League implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Map<String, Team> teams = new HashMap<>();
    public League(String name){ this.name = name; }
    public String getName(){ return name; }
    public Map<String, Team> getTeams(){ return teams; }
}
