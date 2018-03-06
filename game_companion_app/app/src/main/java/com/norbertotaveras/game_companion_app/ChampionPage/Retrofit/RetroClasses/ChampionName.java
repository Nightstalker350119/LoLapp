package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Emanuel on 3/4/2018.
 */

public class ChampionName {
    @SerializedName("name")
    private String championName;

    @SerializedName("id")
    private String championId;

    @SerializedName("title")
    private String title;

    @SerializedName("key")
    private String key;

    public ChampionName(String championName, String championId, String title, String key) {
        this.championName = championName;
        this.championId = championId;
        this.title = title;
        this.key = key;
    }

    public String getChampionName() {
        return championName;
    }

    public void setChampionName(String championName) {
        this.championName = championName;
    }

    public String getChampionId() {
        return championId;
    }

    public void setChampionId(String championId) {
        this.championId = championId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
