package com.norbertotaveras.game_companion_app.DTO.StaticData;

import java.util.List;

/**
 * Created by Norberto on 12/13/2017.
 */

public class SummonerSpellDTO {
    List<SpellVarsDTO> vars;
    ImageDTO image;
    String costBurn;
    List<Double> cooldown;
    List<String> effectBurn;
    int id;
    String cooldownBurn;
    String tooltip;
    int maxrank;
    String rangeBurn;
    String description;
    List<Object> effect;
    String key;
    LevelTipDTO leveltip;
    List<String> modes;
    String resource;
    String name;
    String costType;
    String sanitizedDescription;
    String sanitizedTooltip;
    Object range;
    List<Integer> cost;
    int summonerLevel;
}
