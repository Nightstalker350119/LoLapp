package com.norbertotaveras.game_companion_app.DTO.StaticData;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by Norberto on 12/13/2017.
 */

public class SummonerSpellListDTO implements Serializable {
    public Map<String, SummonerSpellDTO> data;
    public String version;
    public String type;
}
