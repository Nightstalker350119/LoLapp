package com.norbertotaveras.game_companion_app.DTO.StaticData;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class BlockDTO implements Serializable {
    /* This class contains champion recommended block data */
    public List<BlockItemDTO> items;
    public boolean recMath;
    public String type;
}
