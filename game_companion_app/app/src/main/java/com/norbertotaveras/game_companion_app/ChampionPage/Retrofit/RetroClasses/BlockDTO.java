package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class BlockDTO {
    @SerializedName("items")
    private List<BlockItemDTO> items;

    @SerializedName("recMath")
    private boolean recMath;

    @SerializedName("type")
    private String type;

    public BlockDTO(List<BlockItemDTO> items, boolean recMath, String type) {
        this.items = items;
        this.recMath = recMath;
        this.type = type;
    }

    public List<BlockItemDTO> getItems() {
        return items;
    }

    public void setItems(List<BlockItemDTO> items) {
        this.items = items;
    }

    public boolean isRecMath() {
        return recMath;
    }

    public void setRecMath(boolean recMath) {
        this.recMath = recMath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
