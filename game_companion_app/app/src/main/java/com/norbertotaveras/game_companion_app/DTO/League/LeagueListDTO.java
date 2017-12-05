package com.norbertotaveras.game_companion_app.DTO.League;

import com.norbertotaveras.game_companion_app.DTO.League.LeagueItemDTO;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class LeagueListDTO implements Serializable {
    public String leagueid;
    public String tier;
    public String queue;
    public String name;
    public List<LeagueItemDTO> entries;
}
