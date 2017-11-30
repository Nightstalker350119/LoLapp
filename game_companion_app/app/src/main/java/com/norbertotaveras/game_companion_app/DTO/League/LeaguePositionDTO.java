package com.norbertotaveras.game_companion_app.DTO.League;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class LeaguePositionDTO {
    public String rank;
    public String playerOrTeamName;
    public String playerOrTeamId;
    public boolean hotStreak;
    public boolean veteran;
    public boolean freshBlood;
    public boolean inactive;
    public int wins;
    public int leaguePoints;
    public int losses;
    public MiniSeriesDTO miniSeries;
}
