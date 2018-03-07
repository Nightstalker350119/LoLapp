package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class LevelTipDTO {
    @SerializedName("effect")
    private List<String> effect;

    @SerializedName("label")
    private List<String> label;

    public LevelTipDTO(List<String> effect, List<String> label) {
        this.effect = effect;
        this.label = label;
    }

    public List<String> getEffect() {
        return effect;
    }

    public void setEffect(List<String> effect) {
        this.effect = effect;
    }

    public List<String> getLabel() {
        return label;
    }

    public void setLabel(List<String> label) {
        this.label = label;
    }
}
