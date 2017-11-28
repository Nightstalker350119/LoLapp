package com.norbertotaveras.game_companion_app.Summoner;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class SummonerDTO {
    /* This class represents a summoner */
    public int profileIconId; // ID of the summoner icon associated with the summoner.
    public String name; // Summoner name.
    public long summonerLevel; // Summoner level associated with the summoner.
    // Date summoner was last modified specified as epoch milliseconds.
    // The following events will update this timestamp:
    // Profile icon change, Playing the tutorial or Advanced Tutorial, Finishing a game, Summoner name change.
    public long revisionDate;
    public long id; // Summoner ID.
    public long accountId; // Account ID.
}
