package com.norbertotaveras.game_companion_app.DTO.StaticData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class ChampionListDTO {
    /* This class contains champion list data */
    public String version;
    public String type;
    public String format;
    public HashMap<String, String> keys;
    public HashMap<String, ChampionDTO> data;
    public List<ChampionDTO> champions; // The collection of champion information.
}
