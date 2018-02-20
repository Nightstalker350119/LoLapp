package com.norbertotaveras.game_companion_app.DTO.StaticData;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by Norberto on 11/28/2017.
 */

public class ProfileIconDataDTO implements Serializable {
    public HashMap<String, ProfileIconDetailsDTO> data;
    public String version;
    public String type;
}
