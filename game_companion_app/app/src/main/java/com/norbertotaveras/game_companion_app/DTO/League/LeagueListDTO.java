package com.norbertotaveras.game_companion_app.DTO.League;

import com.norbertotaveras.game_companion_app.DTO.League.LeagueItemDTO;

import java.util.List;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class LeagueListDTO {
    public String leagueid;
    public String tier;
    public String queue;
    public String name;
    public List<LeagueItemDTO> entries;
}
