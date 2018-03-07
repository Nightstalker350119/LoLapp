package com.norbertotaveras.game_companion_app.ChampionPage.Retrofit;

import com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses.ChampionDTO;
import com.norbertotaveras.game_companion_app.ChampionPage.Retrofit.RetroClasses.ChampionName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Emanuel on 2/28/2018.
 */

public interface ChampionRiotAPI {
    @GET("lol/static-data/v3/champions/{id}?api_key=RGAPI-737af915-de6b-4538-b7a1-da43b856bbea")
    Call<ChampionDTO> getChampionById(@Path("id") int id);

    @GET("lol/static-data/v3/champions?api_key=RGAPI-737af915-de6b-4538-b7a1-da43b856bbea")
    Call<List<ChampionDTO>> getChampions();

    @GET("lol/static-data/v3/champions?api_key=RGAPI-737af915-de6b-4538-b7a1-da43b856bbea")
    Call<List<ChampionName>> getChampionNames();
}
