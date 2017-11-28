package com.norbertotaveras.game_companion_app;

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

    @GET("/lol/platform/v3/champions")
    Call<List<ChampionDTO>> getChampions();
}
