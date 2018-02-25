package com.norbertotaveras.game_companion_app.DTO.Match;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MatchTimelineDTO implements Serializable {
    public List<MatchFrameDTO> frames;
    public Long frameInterval;
}
