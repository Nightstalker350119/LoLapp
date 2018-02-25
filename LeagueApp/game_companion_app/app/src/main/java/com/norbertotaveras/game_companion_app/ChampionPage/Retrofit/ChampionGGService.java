package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit;

import com.norbertotaveras.game_companion_app.ChampionPage.ChampionRates;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Logan on 2/17/2018.
 */

public interface ChampionGGService {
    @GET("champions?api_key=75aeb3ae675651c7cbe4fc72651c846f") //&?sort=winRate-desc,championId,playRate,banRate
    Call<List<ChampionRates>> getChampInfo();
}
//HTTP/1.1 200 OK
//        {
//        data: [
//        {
//        _id: {
//        championId: 202,
//        role: "DUO_CARRY"
//        },
//        elo: "PLATINUM,DIAMOND,MASTER,CHALLENGER",
//        patch: "7.4",
//        championId: 202,
//        winRate: 0.5076744140965748,
//        playRate: 0.3929342370051475,
//        gamesPlayed: 257935,
//        percentRolePlayed: 0.9547596397650255,
//        banRate: 0.009379987498089421,
//        role: "DUO_CARRY"
//        }]
//        }