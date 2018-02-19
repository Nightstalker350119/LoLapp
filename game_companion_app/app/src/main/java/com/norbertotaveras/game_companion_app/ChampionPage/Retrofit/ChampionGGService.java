package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit;

import com.norbertotaveras.game_companion_app.ChampionPage.ChampionRates;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Logan on 2/17/2018.
 */

public interface ChampionGGService {
    @GET("/champions?champData=winRate&api_key=75aeb3ae675651c7cbe4fc72651c846f")
    Call<ChampionRates> getChampInfo();
}
