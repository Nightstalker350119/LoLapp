package com.norbertotaveras.game_companion_app.DTO.ChampionMastery;

import java.io.Serializable;

/**
 * Created by Norberto on 2/12/2018.
 */

public class ChampionMasteryDTO implements Serializable {
    public boolean chestGranted;
    public int championLevel;
    public int championPoints;
    public long championId;
    public long playerId;
    public long championPointsUntilNextLevel;
    public int tokensEarned;
    public long championPointsSinceLastLevel;
    public long lastPlayTime;
}
