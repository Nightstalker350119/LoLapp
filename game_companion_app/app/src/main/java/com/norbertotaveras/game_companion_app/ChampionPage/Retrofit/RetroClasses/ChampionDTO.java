package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;


import java.util.List;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class ChampionDTO {
    @SerializedName("info")
    private InfoDTO info;

    @SerializedName("enemytips")
    private List<String> enemytips;

    @SerializedName("stats")
    private StatsDTO stats;

    @SerializedName("name")
    private String name;

    @SerializedName("title")
    private String title;

    @SerializedName("image")
    private ImageDTO image;

    @SerializedName("tags")
    private List<String> tags;

    @SerializedName("partype")
    private String partype;

    @SerializedName("skins")
    private List<SkinDTO> skins;

    @SerializedName("passive")
    private PassiveDTO passive;

    @SerializedName("recommended")
    private List<RecommendedDTO> recommended;

    @SerializedName("allytips")
    private List<String> allytips;

    @SerializedName("key")
    private String key;

    @SerializedName("lore")
    private String lore;

    @SerializedName("id")
    private int id;

    @SerializedName("blurb")
    private String blurb;

    @SerializedName("spells")
    private List<ChampionSpellDTO> spells;

    public ChampionDTO(InfoDTO info, List<String> enemytips, StatsDTO stats, String name, String title,
                       ImageDTO image, List<String> tags, String partype, List<SkinDTO> skins, PassiveDTO passive,
                       List<RecommendedDTO> recommended, List<String> allytips, String key, String lore, int id, String blurb, List<ChampionSpellDTO> spells) {
        this.info = info;
        this.enemytips = enemytips;
        this.stats = stats;
        this.name = name;
        this.title = title;
        this.image = image;
        this.tags = tags;
        this.partype = partype;
        this.skins = skins;
        this.passive = passive;
        this.recommended = recommended;
        this.allytips = allytips;
        this.key = key;
        this.lore = lore;
        this.id = id;
        this.blurb = blurb;
        this.spells = spells;
    }

    public InfoDTO getInfo() {
        return info;
    }

    public void setInfo(InfoDTO info) {
        this.info = info;
    }

    public List<String> getEnemytips() {
        return enemytips;
    }

    public void setEnemytips(List<String> enemytips) {
        this.enemytips = enemytips;
    }

    public StatsDTO getStats() {
        return stats;
    }

    public void setStats(StatsDTO stats) {
        this.stats = stats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ImageDTO getImage() {
        return image;
    }

    public void setImage(ImageDTO image) {
        this.image = image;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getPartype() {
        return partype;
    }

    public void setPartype(String partype) {
        this.partype = partype;
    }

    public List<SkinDTO> getSkins() {
        return skins;
    }

    public void setSkins(List<SkinDTO> skins) {
        this.skins = skins;
    }

    public PassiveDTO getPassive() {
        return passive;
    }

    public void setPassive(PassiveDTO passive) {
        this.passive = passive;
    }

    public List<RecommendedDTO> getRecommended() {
        return recommended;
    }

    public void setRecommended(List<RecommendedDTO> recommended) {
        this.recommended = recommended;
    }

    public List<String> getAllytips() {
        return allytips;
    }

    public void setAllytips(List<String> allytips) {
        this.allytips = allytips;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLore() {
        return lore;
    }

    public void setLore(String lore) {
        this.lore = lore;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public List<ChampionSpellDTO> getSpells() {
        return spells;
    }

    public void setSpells(List<ChampionSpellDTO> spells) {
        this.spells = spells;
    }
}
