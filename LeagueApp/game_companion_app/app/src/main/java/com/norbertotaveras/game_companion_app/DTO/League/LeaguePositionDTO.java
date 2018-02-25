package com.norbertotaveras.game_companion_app.DTO.League;

import java.io.Serializable;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class LeaguePositionDTO implements Serializable {
    public String rank;
    public String queueType;
    public boolean hotStreak;
    public MiniSeriesDTO miniSeries;
    public int wins;
    public boolean veteran;
    public int losses;
    public boolean freshBlood;
    public String leagueId;
    public String playerOrTeamName;
    public boolean inactive;
    public String playerOrTeamId;
    public String leagueName;
    public String tier;
    public int leaguePoints;
}
