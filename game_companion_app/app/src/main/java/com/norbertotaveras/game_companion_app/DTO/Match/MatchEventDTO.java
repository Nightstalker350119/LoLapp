package com.norbertotaveras.game_companion_app.DTO.Match;

import java.util.List;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MatchEventDTO {
    public String eventType;
    public String towerType;
    public int teamId;
    public String ascendedType;
    public int killerId;
    public String levelUpType;
    public String pointCaptured;
    public List<Integer> assistingParticipantIds;
    public String wardType;
    public String monsterType;
    public String type; // (Legal values: CHAMPION_KILL, WARD_PLACED, WARD_KILL, BUILDING_KILL, ELITE_MONSTER_KILL, ITEM_PURCHASED, ITEM_SOLD, ITEM_DESTROYED, ITEM_UNDO, SKILL_LEVEL_UP, ASCENDED_EVENT, CAPTURE_POINT, PORO_KING_SUMMON)
    public int skillSlot;
    public int victimId;
    public long timestamp;
    public int afterId;
    public String monsterSubType;
    public String laneType;
    public int itemId;
    public int participantId;
    public String buildingType;
    public int creatorId;
    public MatchPositionDTO position;
    public int beforeId;
}
