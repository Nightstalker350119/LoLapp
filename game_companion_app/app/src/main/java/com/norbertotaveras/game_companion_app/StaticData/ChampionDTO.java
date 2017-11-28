package com.norbertotaveras.game_companion_app.StaticData;

import java.util.List;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class ChampionDTO {
    /* This class contains champion information */
    public String name; //
    public String title;
    public String partype;
    public String key;
    public String lore;
    public String blurb;
    public List<String> enemyTips;
    public List<String> tags;
    public List<String> allytips;
    public boolean rankedPlayEnabled; // Ranked play enabled flag.
    public boolean botEnabled; // Bot enabled flag for custom games.
    public boolean botmmenabled; // Got Match Made enabled flag for co-op vs. AI games.
    public boolean active; // Indicates if the champion is active.
    public boolean freeToPlay; // Indicates if the champion is free to play. Free to play champions are rotated periodically.
    public long id;
    public int championid;
    public List<SkinsDTO> skins;
    public List<RecommendedDTO> recommended;
    public List<ChampionSpellDTO> spells;
    public InfoDTO info;
    public StatsDTO stats;
    public ImageDTO image;
    public PassiveDTO passive;

}
