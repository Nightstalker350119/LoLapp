package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class InfoDTO {
    @SerializedName("difficulty")
    private int difficulty;

    @SerializedName("attack")
    private int attack;

    @SerializedName("defense")
    private int defense;

    @SerializedName("magic")
    private int magic;

    public InfoDTO(int difficulty, int attack, int defense, int magic) {
        this.difficulty = difficulty;
        this.attack = attack;
        this.defense = defense;
        this.magic = magic;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }
}
