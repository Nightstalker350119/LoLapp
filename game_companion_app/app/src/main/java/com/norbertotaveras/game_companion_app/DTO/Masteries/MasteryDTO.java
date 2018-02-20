package com.norbertotaveras.game_companion_app.DTO.Masteries;

import java.io.Serializable;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MasteryDTO implements Serializable {
    public int id; // Mastery ID. For static information correlating to masteries, please refer to the LoL Static Data API.
    public int rank; // Mastery rank (i.e., the number of points put into this mastery).
}
