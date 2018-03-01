package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Emanuel on 2/28/2018.
 */

public interface ChampionRiotAPI {
    @GET("/lol/platform/v3/champions/{id}?api_key=<RGAPI-a413eed4-d564-4f08-87e0-737c0ac8fc52>")
    Call<String> getChampionById(@Path("id") int id);
}
