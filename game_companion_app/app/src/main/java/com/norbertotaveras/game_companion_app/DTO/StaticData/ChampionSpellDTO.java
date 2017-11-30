package com.norbertotaveras.game_companion_app.DTO.StaticData;

import java.util.List;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class ChampionSpellDTO {
    /* This class contains champion spell data */
    public String cooldownBurn;
    public String resource;
    public String costType;
    public String sanitizedDescription;
    public String tooltip;
    public String sanitizedTooltip;
    public String name;
    public String key;
    public String description;
    public ImageDTO image;
    public List<Integer> range; // This field is either a List of Integer or the String 'self' for spells that target one's own champion
    public List<Integer> cost;
    public List<Double> cooldown;
    public List<List<Double>> effect; // This field is a LIst of List of Double
    public List<SpellVarsDTO> vars;
    public List<String> effectBurn;
    public List<ImageDTO> altimages;
    public LevelTipDTO leveltip;
}
