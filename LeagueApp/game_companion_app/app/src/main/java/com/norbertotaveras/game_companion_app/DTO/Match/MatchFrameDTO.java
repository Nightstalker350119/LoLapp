package com.norbertotaveras.game_companion_app.DTO.Match;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MatchFrameDTO implements Serializable {
    public long timestamp;
    public Map<Integer, MatchParticipantFrameDTO> participantFrames;
    public List<MatchEventDTO> events;

}
