package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Emanuel on 2/28/2018.
 */

public interface ChampionRiotAPI {
    @GET("lol/platform/v3/champions/{id}?api_key=RGAPI-1a17152c-8ee8-4f58-a953-9fc5156a70e7")
    Call<String> getChampionById(@Path("id") int id);
}
