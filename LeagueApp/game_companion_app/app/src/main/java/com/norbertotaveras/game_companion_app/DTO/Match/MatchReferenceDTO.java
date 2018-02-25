package com.norbertotaveras.game_companion_app.DTO.Match;

import java.io.Serializable;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MatchReferenceDTO implements Serializable {
    public String lane;
    public Long gameId;
    public int champion;
    public String platformId;
    public int season;
    public int queue;
    public String role;
    public Long timestamp;
}
