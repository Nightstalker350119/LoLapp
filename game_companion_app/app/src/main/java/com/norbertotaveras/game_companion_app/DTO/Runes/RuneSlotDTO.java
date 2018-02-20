package com.norbertotaveras.game_companion_app.DTO.Runes;

import java.io.Serializable;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class RuneSlotDTO implements Serializable {
    public int runeSlotID; // Rune slot ID.
    public int runeID; // Rune ID associated with the rune slot. For static information correlating to rune IDs, please refer to the LoL Static Data API.
}
