package com.norbertotaveras.game_companion_app.DTO.Match;

import java.util.List;
import java.util.Map;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MatchFrameDTO {
    public long timestamp;
    public Map<Integer, MatchParticipantFrameDTO> participantFrames;
    public List<MatchEventDTO> events;

}
