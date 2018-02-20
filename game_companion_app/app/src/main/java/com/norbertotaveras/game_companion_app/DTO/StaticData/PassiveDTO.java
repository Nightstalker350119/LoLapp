package com.norbertotaveras.game_companion_app.DTO.StaticData;

import com.norbertotaveras.game_companion_app.DTO.StaticData.ImageDTO;

import java.io.Serializable;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class PassiveDTO implements Serializable {
    /* This class contains champion passive data */
    public String sanitizedDescription;
    public String name;
    public String description;
    public ImageDTO image;
}
