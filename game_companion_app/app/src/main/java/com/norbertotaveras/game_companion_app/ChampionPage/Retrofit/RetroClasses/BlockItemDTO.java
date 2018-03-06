package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class BlockItemDTO {
    @SerializedName("count")
    private int count;

    @SerializedName("id")
    private int id;

    public BlockItemDTO(int count, int id) {
        this.count = count;
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
