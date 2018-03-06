package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Emanuel on 3/5/2018.
 */

public class StatsDTO {
    @SerializedName("armorperlevel")
    private double armorperlevel;

    @SerializedName("hpperlevel")
    private double hpperlevel;

    @SerializedName("attackdamage")
    private double attackdamage;

    @SerializedName("mpperlevel")
    private double mpperlevel;

    @SerializedName("attackspeedoffset")
    private double attackspeedoffset;

    @SerializedName("armor")
    private double armor;

    @SerializedName("hp")
    private double hp;

    @SerializedName("hpregenperlevel")
    private double hpregenperlevel;

    @SerializedName("spellblock")
    private double spellblock;

    @SerializedName("attackrange")
    private double attackrange;

    @SerializedName("movespeed")
    private double movespeed;

    @SerializedName("attackdamageperlevel")
    private double attackdamageperlevel;

    @SerializedName("mpregenperlevel")
    private double mpregenperlevel;

    @SerializedName("mp")
    private double mp;

    @SerializedName("spellblockperlevel")
    private double spellblockperlevel;

    @SerializedName("crit")
    private double crit;

    @SerializedName("mpregen")
    private double mpregen;

    @SerializedName("attackspeedperlevel")
    private double attackspeedperlevel;

    @SerializedName("hpregen")
    private double hpregen;

    @SerializedName("critperlevel")
    private double critperlevel;

    public StatsDTO(double armorperlevel, double hpperlevel, double attackdamage, double mpperlevel,
                    double attackspeedoffset, double armor, double hp, double hpregenperlevel,
                    double spellblock, double attackrange, double movespeed, double attackdamageperlevel,
                    double mpregenperlevel, double mp, double spellblockperlevel, double crit, double mpregen,
                    double attackspeedperlevel, double hpregen, double critperlevel) {
        this.armorperlevel = armorperlevel;
        this.hpperlevel = hpperlevel;
        this.attackdamage = attackdamage;
        this.mpperlevel = mpperlevel;
        this.attackspeedoffset = attackspeedoffset;
        this.armor = armor;
        this.hp = hp;
        this.hpregenperlevel = hpregenperlevel;
        this.spellblock = spellblock;
        this.attackrange = attackrange;
        this.movespeed = movespeed;
        this.attackdamageperlevel = attackdamageperlevel;
        this.mpregenperlevel = mpregenperlevel;
        this.mp = mp;
        this.spellblockperlevel = spellblockperlevel;
        this.crit = crit;
        this.mpregen = mpregen;
        this.attackspeedperlevel = attackspeedperlevel;
        this.hpregen = hpregen;
        this.critperlevel = critperlevel;
    }

    public double getArmorperlevel() {
        return armorperlevel;
    }

    public void setArmorperlevel(double armorperlevel) {
        this.armorperlevel = armorperlevel;
    }

    public double getHpperlevel() {
        return hpperlevel;
    }

    public void setHpperlevel(double hpperlevel) {
        this.hpperlevel = hpperlevel;
    }

    public double getAttackdamage() {
        return attackdamage;
    }

    public void setAttackdamage(double attackdamage) {
        this.attackdamage = attackdamage;
    }

    public double getMpperlevel() {
        return mpperlevel;
    }

    public void setMpperlevel(double mpperlevel) {
        this.mpperlevel = mpperlevel;
    }

    public double getAttackspeedoffset() {
        return attackspeedoffset;
    }

    public void setAttackspeedoffset(double attackspeedoffset) {
        this.attackspeedoffset = attackspeedoffset;
    }

    public double getArmor() {
        return armor;
    }

    public void setArmor(double armor) {
        this.armor = armor;
    }

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public double getHpregenperlevel() {
        return hpregenperlevel;
    }

    public void setHpregenperlevel(double hpregenperlevel) {
        this.hpregenperlevel = hpregenperlevel;
    }

    public double getSpellblock() {
        return spellblock;
    }

    public void setSpellblock(double spellblock) {
        this.spellblock = spellblock;
    }

    public double getAttackrange() {
        return attackrange;
    }

    public void setAttackrange(double attackrange) {
        this.attackrange = attackrange;
    }

    public double getMovespeed() {
        return movespeed;
    }

    public void setMovespeed(double movespeed) {
        this.movespeed = movespeed;
    }

    public double getAttackdamageperlevel() {
        return attackdamageperlevel;
    }

    public void setAttackdamageperlevel(double attackdamageperlevel) {
        this.attackdamageperlevel = attackdamageperlevel;
    }

    public double getMpregenperlevel() {
        return mpregenperlevel;
    }

    public void setMpregenperlevel(double mpregenperlevel) {
        this.mpregenperlevel = mpregenperlevel;
    }

    public double getMp() {
        return mp;
    }

    public void setMp(double mp) {
        this.mp = mp;
    }

    public double getSpellblockperlevel() {
        return spellblockperlevel;
    }

    public void setSpellblockperlevel(double spellblockperlevel) {
        this.spellblockperlevel = spellblockperlevel;
    }

    public double getCrit() {
        return crit;
    }

    public void setCrit(double crit) {
        this.crit = crit;
    }

    public double getMpregen() {
        return mpregen;
    }

    public void setMpregen(double mpregen) {
        this.mpregen = mpregen;
    }

    public double getAttackspeedperlevel() {
        return attackspeedperlevel;
    }

    public void setAttackspeedperlevel(double attackspeedperlevel) {
        this.attackspeedperlevel = attackspeedperlevel;
    }

    public double getHpregen() {
        return hpregen;
    }

    public void setHpregen(double hpregen) {
        this.hpregen = hpregen;
    }

    public double getCritperlevel() {
        return critperlevel;
    }

    public void setCritperlevel(double critperlevel) {
        this.critperlevel = critperlevel;
    }
}
