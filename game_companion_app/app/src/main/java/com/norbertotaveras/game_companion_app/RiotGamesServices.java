package com.norbertotaveras.game_companion_app;

import com.norbertotaveras.game_companion_app.StaticData.ChampionDTO;
import com.norbertotaveras.game_companion_app.StaticData.ChampionListDTO;
import com.norbertotaveras.game_companion_app.Summoner.SummonerDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public interface RiotGamesServices {
    @GET("/lol/summoner/v3/summoners/by-name/{name}")
    Call<SummonerDTO> getSummonersByName(@Path("name") String name);

    // Retrieve all Champions
    @GET("/lol/platform/v3/champions")
    Call<List<ChampionDTO>> getChampions();

    // Retrieve a champion by ID
    @GET("/lol/platform/v3/champions/{id}")
    Call<List<ChampionDTO>> getChampionById();

    // Retrieves champion list
    @GET("/lol/static-data/v3/champions")
    Call<List<ChampionListDTO>> getChampionList();
}
