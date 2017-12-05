package com.norbertotaveras.game_companion_app.DTO.League;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class LeagueItemDTO {
    public MiniSeriesDTO miniSeries;
    public String rank;
    public String playerOrTeamId;
    public String playerOrTeamName;
    public int wins;
    public int losses;
    public int leaguePoints;
    public boolean hotStreak;
    public boolean veteran;
    public boolean freshBlood;
    public boolean inactive;
}
