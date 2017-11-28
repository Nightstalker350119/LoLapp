package com.norbertotaveras.game_companion_app.Match;

import java.util.Map;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class ParticipantTimelineDTO {
    public String lane;
    public int participantId;
    public Map<String, Double> csDiffPerMinDeltas;
    public Map<String, Double> goldPerMinDeltas;
    public Map<String, Double> xpDiffPerMinDeltas;
    public Map<String, Double> creepsPerMinDeltas;
    public Map<String, Double> xpPerMinDeltas;
    public Map<String, Double> damageTakenDiffPerMinDeltas;
    public Map<String, Double> damageTakenPerMinDeltas;
    public String role;

}
