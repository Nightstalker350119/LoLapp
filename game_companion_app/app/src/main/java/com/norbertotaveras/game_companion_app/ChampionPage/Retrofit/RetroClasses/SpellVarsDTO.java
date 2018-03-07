package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class SpellVarsDTO {
    @SerializedName("ranksWith")
    private String ranksWith;

    @SerializedName("dyn")
    private String dyn;

    @SerializedName("link")
    private String link;

    @SerializedName("coeff")
    private List<Double> coeff;

    @SerializedName("key")
    private String key;

}
