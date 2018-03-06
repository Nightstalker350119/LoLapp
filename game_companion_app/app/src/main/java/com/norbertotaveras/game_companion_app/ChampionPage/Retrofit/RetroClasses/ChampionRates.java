package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Logan on 2/17/2018.
 */

public class ChampionRates {
    @SerializedName("championId")
    private String championId;

    @SerializedName("winRate")
    private String winRate;

    @SerializedName("playRate") //pickRate
    private String playRate;

    @SerializedName("banRate")
    private String banRate;

    @SerializedName("role")
    private String championRole;

    public ChampionRates(String championId, String winRate, String playRate, String banRate, String championRole) {
        this.championId = championId;
        this.winRate = winRate;
        this.playRate = playRate;
        this.banRate = banRate;
        this.championRole = championRole;
    }

    public String getChampionId() {
        return championId;
    }

    public void setChampionId(String championId) {
        this.championId = championId;
    }

    public String getWinRate() {
        return winRate;
    }

    public void setWinRate(String winRate) {
        this.winRate = winRate;
    }

    public String getPlayRate() {
        return playRate;
    }

    public void setPlayRate(String playRate) {
        this.playRate = playRate;
    }

    public String getBanRate() {
        return banRate;
    }

    public void setBanRate(String banRate) {
        this.banRate = banRate;
    }

    public String getChampionRole() {
        return championRole;
    }

    public void setChampionRole(String championRole) {
        this.championRole = championRole;
    }
}
