package com.norbertotaveras.game_companion_app.DTO.Match;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MatchlistDTO implements Serializable {
    public List<MatchReferenceDTO> matches;
    public int totalGames;
    public int startIndex;
    public int endIndex;
}
