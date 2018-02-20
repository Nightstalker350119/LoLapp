package com.norbertotaveras.game_companion_app.DTO.Match;

import com.norbertotaveras.game_companion_app.DTO.Masteries.MasteryDTO;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class ParticipantDTO implements Serializable {
    public ParticipantStatsDTO stats;
    public int participantId;
    public List<RuneDTO> runes;
    public ParticipantTimelineDTO timeline;
    public int teamId;
    public int spell2Id;
    public List<MasteryDTO> masteries;
    public String highestAchievedSeasonTier;
    public int spell1Id;
    public int championId;
}
