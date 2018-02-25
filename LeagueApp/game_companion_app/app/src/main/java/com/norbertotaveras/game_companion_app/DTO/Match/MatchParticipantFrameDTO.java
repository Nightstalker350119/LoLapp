package com.norbertotaveras.game_companion_app.DTO.Match;

import java.io.Serializable;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MatchParticipantFrameDTO implements Serializable {
    public int totalGold;
    public int teamScore;
    public int participantId;
    public int level;
    public int currentGold;
    public int minionsKilled;
    public int dominionScore;
    public MatchPositionDTO position;
    public int xp;
    public int jungleMinionsKilled;
}
