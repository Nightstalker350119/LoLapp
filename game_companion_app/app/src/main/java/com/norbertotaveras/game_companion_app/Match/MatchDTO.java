package com.norbertotaveras.game_companion_app.Match;

import java.util.List;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MatchDTO {
    public int seasonId;
    public int queueId;
    public long gameId;
    public List<ParticipantIdentityDTO> participantIdentities;
    public String gameVersion;
    public String platformId;
    public String gameMode;
    public int mapId;
    public String gameType;
    public List<TeamStatsDTO> teams;
    public List<ParticipantDTO> participants;
    public long gameDuration;
    public Long gameCreation;
}
