package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class SkinDTO {
    @SerializedName("num")
    private int num;

    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private int id;

    public SkinDTO(int num, String name, int id) {
        this.num = num;
        this.name = name;
        this.id = id;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
