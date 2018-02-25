package com.norbertotaveras.game_companion_app.DTO.Masteries;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MasteryPagesDTO implements Serializable {
    public Set<MasteryPageDTO> pages; // Collection of mastery pages associated with the summoner.
    public long summonerId; // Summoner ID.
}
