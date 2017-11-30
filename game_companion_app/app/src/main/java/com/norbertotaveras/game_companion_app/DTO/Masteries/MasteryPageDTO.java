package com.norbertotaveras.game_companion_app.DTO.Masteries;

import java.util.List;

/**
 * Created by Emanuel on 11/28/2017.
 */

public class MasteryPageDTO {
    public long id; // Mastery page ID.
    public String name; // Mastery page name.
    public boolean current; // Indicates if the mastery page is the current mastery page.
    public List<MasteryDTO> masteries; // Collection of masteries associated with the mastery page.



}
