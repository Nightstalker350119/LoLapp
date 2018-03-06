package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class ChampionSpellDTO {
    @SerializedName("cooldownBurn")
    private String cooldownBurn;

    @SerializedName("resource")
    private String resource;

    @SerializedName("leveltip")
    private LevelTipDTO leveltip;

    @SerializedName("vars")
    private List<SpellVarsDTO> vars;

    @SerializedName("costType")
    private String costType;

    @SerializedName("image")
    private ImageDTO image;

    @SerializedName("sanitizedDescription")
    private String sanitizedDescription;

    @SerializedName("sanitizedTooltip")
    private String sanitizedTooltip;

    @SerializedName("effect")
    private List<List<Double>> effect;

    @SerializedName("tooltip")
    private String tooltip;

    @SerializedName("maxrank")
    private int maxrank;

    @SerializedName("costBurn")
    private String costBurn;

    @SerializedName("rangeBurn")
    private String rangeBurn;

    @SerializedName("range")
    private List<Integer> range;

    @SerializedName("cooldown")
    private List<Double> cooldown;

    @SerializedName("cost")
    private List<Integer> cost;

    @SerializedName("key")
    private String key;

    @SerializedName("description")
    private String description;

    @SerializedName("effectBurn")
    private List<String> effectBurn;

    @SerializedName("altimages")
    private List<ImageDTO> altimages;

    @SerializedName("name")
    private String name;

    public ChampionSpellDTO(String cooldownBurn, String resource, LevelTipDTO leveltip, List<SpellVarsDTO> vars,
                            String costType, ImageDTO image, String sanitizedDescription, String sanitizedTooltip,
                            List<List<Double>> effect, String tooltip, int maxrank, String costBurn, String rangeBurn,
                            List<Integer> range, List<Double> cooldown, List<Integer> cost, String key,
                            String description, List<String> effectBurn, List<ImageDTO> altimages, String name) {
        this.cooldownBurn = cooldownBurn;
        this.resource = resource;
        this.leveltip = leveltip;
        this.vars = vars;
        this.costType = costType;
        this.image = image;
        this.sanitizedDescription = sanitizedDescription;
        this.sanitizedTooltip = sanitizedTooltip;
        this.effect = effect;
        this.tooltip = tooltip;
        this.maxrank = maxrank;
        this.costBurn = costBurn;
        this.rangeBurn = rangeBurn;
        this.range = range;
        this.cooldown = cooldown;
        this.cost = cost;
        this.key = key;
        this.description = description;
        this.effectBurn = effectBurn;
        this.altimages = altimages;
        this.name = name;
    }
}
