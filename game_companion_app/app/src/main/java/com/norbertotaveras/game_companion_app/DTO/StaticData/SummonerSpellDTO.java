package com.norbertotaveras.game_companion_app.DTO.StaticData;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Norberto on 12/13/2017.
 */

public class SummonerSpellDTO implements Serializable {
    public List<SpellVarsDTO> vars;
    public ImageDTO image;
    public String costBurn;
    public List<Double> cooldown;
    public List<String> effectBurn;
    public int id;
    public String cooldownBurn;
    public String tooltip;
    public int maxrank;
    public String rangeBurn;
    public String description;
    public List<Object> effect;
    public String key;
    public LevelTipDTO leveltip;
    public List<String> modes;
    public String resource;
    public String name;
    public String costType;
    public String sanitizedDescription;
    public String sanitizedTooltip;
    public Object range;
    public List<Integer> cost;
    public int summonerLevel;
}
