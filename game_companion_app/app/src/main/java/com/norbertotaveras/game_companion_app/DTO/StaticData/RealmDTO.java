package com.norbertotaveras.game_companion_app.DTO.StaticData;

import java.util.HashMap;

/**
 * Created by Norberto on 11/28/2017.
 */

public class RealmDTO {
    // Legacy script mode for IE6 or older.
    public String dd;

    // Latest changed version of Dragon Magic.
    public String l;

    // Default language for this realm.
    public HashMap<String, String> n;

    // Latest changed version for each data type listed.
    public int profileiconmax;

    // Special behavior number identifying the largest profile icon ID that can be used under 500.
    // Any profile icon that is requested between this number and 500 should be mapped to 0.
    public String store;

    // Additional API data drawn from other sources that may be related to Data Dragon
    // functionality.
    public String v;

    // Current version of this file for this realm.
    public String cdn;

    // The base CDN URL.
    public String css;

    // Latest changed version of Dragon Magic's CSS file.
    public String lg;
}
