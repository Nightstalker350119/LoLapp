package com.norbertotaveras.game_companion_app;

import com.norbertotaveras.game_companion_app.DTO.League.LeagueListDTO;
import com.norbertotaveras.game_companion_app.DTO.League.LeaguePositionDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchlistDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.RealmDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionListDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ProfileIconDataDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.SummonerSpellListDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public interface RiotGamesService {
    //
    // Static data

    // Retrieves champion list.
    @GET("/lol/static-data/v3/champions")
    Call<ChampionListDTO> getChampionList();

    // Retrieve current patch version.
    @GET("/lol/static-data/v3/versions")
    Call<List<String>> getVersions();

    // Retrieve account profile icons.
    @GET("/lol/static-data/v3/profile-icons")
    Call<ProfileIconDataDTO> getProfileIcons();

    // Retrieve realms.
    @GET("/lol/static-data/v3/realms")
    Call<RealmDTO> getRealms();

    // Retrieve Summoner Spell List.
    @GET("/lol/static-data/v3/summoner-spells")
    Call<SummonerSpellListDTO> getSummonerSpellList(@Query("tags") String tags);

    //
    // Summoners

    @GET("/lol/summoner/v3/summoners/by-name/{name}")
    Call<SummonerDTO> getSummonerByName(@Path("name") String name);

    @GET("/lol/summoner/v3/summoners/by-account/{id}")
    Call<SummonerDTO> getSummonerById(@Path("id") Long id);

    //
    // Champions

    // Retrieve all Champions.
    @GET("/lol/platform/v3/champions")
    Call<List<ChampionDTO>> getChampions();

    // Retrieve a champion by ID.
    @GET("/lol/platform/v3/champions/{id}")
    Call<List<ChampionDTO>> getChampionById(@Path("id") long id);

    //
    // Leagues

    // Retrieve League Positions by Summoner ID.
    @GET("/lol/league/v3/positions/by-summoner/{id}")
    Call<List<LeaguePositionDTO>> getLeaguePositions(@Path("id") long id);

    //
    // Matches

    // Get matchlist for game played on given account ID and filtered using given filter parameters.
    @GET("/lol/match/v3/matchlists/by-account/{id}")
    Call<MatchlistDTO> getMatchList(@Path("id") long accountId,
                                    @Query("beginIndex") long beginIndex,
                                    @Query("endIndex") long endIndex);

    @GET("/lol/match/v3/matchlists/by-account/{id}")
    Call<MatchlistDTO> getMatchList_FilterQueue(@Path("id") long accountId,
                                    @Query("beginIndex") long beginIndex,
                                    @Query("endIndex") long endIndex,
                                    @Query("queue") String queueFilter);

    // Get match by match ID.
    @GET("/lol/match/v3/matches/{matchId}")
    Call<MatchDTO> getMatch(@Path("matchId") long id);
}
