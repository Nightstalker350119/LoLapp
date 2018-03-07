package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class RecommendedDTO {
    @SerializedName("map")
    private String map;

    @SerializedName("blocks")
    private List<BlockDTO> blocks;

    @SerializedName("champion")
    private String champion;

    @SerializedName("title")
    private String title;

    @SerializedName("priority")
    private boolean priority;

    @SerializedName("mode")
    private String mode;

    @SerializedName("type")
    private String type;

    public RecommendedDTO(String map, List<BlockDTO> blocks, String champion,
                          String title, boolean priority, String mode, String type) {
        this.map = map;
        this.blocks = blocks;
        this.champion = champion;
        this.title = title;
        this.priority = priority;
        this.mode = mode;
        this.type = type;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public List<BlockDTO> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockDTO> blocks) {
        this.blocks = blocks;
    }

    public String getChampion() {
        return champion;
    }

    public void setChampion(String champion) {
        this.champion = champion;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
