package com.norbertotaveras.game_companion_app;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.norbertotaveras.game_companion_app.DTO.League.LeagueItemDTO;
import com.norbertotaveras.game_companion_app.DTO.League.LeagueListDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.RealmDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ProfileIconDataDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SummonerSearchResultsActivity extends AppCompatActivity {
    private RiotGamesService apiService;
    private String searchText;
    private TextView summonerLevel;
    private ImageView profileIcon;

    private ProfileIconDataDTO profileIconDataDTO;
    private long profileIconId;
    private String version;
    private boolean profileIconDone;

    private Handler uiThreadHandler;

    private TextView levelText;
    private TextView tierText;
    private TextView leagueText;
    private TextView queueText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summoner_search_results);

        summonerLevel = findViewById(R.id.level);
        profileIcon = findViewById(R.id.profile_icon);

        levelText = findViewById(R.id.level);
        tierText = findViewById(R.id.tier);
        leagueText = findViewById(R.id.league);
        queueText = findViewById(R.id.queue);

        TabHost host;
        host = findViewById(R.id.tab_scr);
        host.setup();

        TabHost.TabSpec spec;
        spec = host.newTabSpec("Summary");
        spec.setContent(R.id.tab_summary);
        spec.setIndicator("Summary");
        host.addTab(spec);

        spec = host.newTabSpec("Champs");
        spec.setContent(R.id.tab_champs);
        spec.setIndicator("Champs");
        host.addTab(spec);

        spec = host.newTabSpec("Runes");
        spec.setContent(R.id.tab_runes);
        spec.setIndicator("Runes");
        host.addTab(spec);

        apiService = RiotAPI.getInstance();

        Intent intent = getIntent();
        searchText = intent.getStringExtra("searchText");

        uiThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Runnable work = (Runnable)msg.obj;
                work.run();
            }
        };

        search();
    }

    private void search() {
        try {
            Call<SummonerDTO> getSummonerRequest = apiService.getSummonersByName(searchText);

            getSummonerRequest.enqueue(new Callback<SummonerDTO>() {
                @Override
                public void onResponse(Call<SummonerDTO> call,
                                       retrofit2.Response<SummonerDTO> response) {
                    handleGetSummonerResponse(response.body());
                }

                @Override
                public void onFailure(Call<SummonerDTO> call, Throwable t) {
                    Log.e("riottest", String.format("async request failed = %s", t));
                }
            });

            Call<ProfileIconDataDTO> getProfileIconsRequest = apiService.getProfileIcons();

            getProfileIconsRequest.enqueue(new Callback<ProfileIconDataDTO>() {
                @Override
                public void onResponse(Call<ProfileIconDataDTO> call,
                                       Response<ProfileIconDataDTO> response) {
                    handleGetProfileIconData(response.body());
                }

                @Override
                public void onFailure(Call<ProfileIconDataDTO> call, Throwable t) {

                }
            });

//            Call<List<String>> getVersionsRequest = apiService.getVersions();
//
//            getVersionsRequest.enqueue(new Callback<List<String>>() {
//                @Override
//                public void onResponse(Call<List<String>> call, Response<List<String>> response) {
//                    handleGetVersionsRequest(response.body());
//                }
//
//                @Override
//                public void onFailure(Call<List<String>> call, Throwable t) {
//
//                }
//            });

            Call<RealmDTO> getRealmsRequest = apiService.getRealms();

            getRealmsRequest.enqueue(new Callback<RealmDTO>() {
                @Override
                public void onResponse(Call<RealmDTO> call, Response<RealmDTO> response) {
                    handleGetRealmsRequest(response.body());
                }

                @Override
                public void onFailure(Call<RealmDTO> call, Throwable t) {

                }
            });
        }
        catch (Exception ex) {
            Log.e("riottest", String.format("request completely failed = %s", ex));
        }
    }

    private void handleGetRealmsRequest(RealmDTO realmDTO) {
        version = realmDTO.v;
        updateProfileIcon();
    }

//    private void handleGetVersionsRequest(List<String> versions) {
//
//    }

    private void handleGetProfileIconData(ProfileIconDataDTO iconDataDto) {
        profileIconDataDTO = iconDataDto;
        updateProfileIcon();
    }

    // Needs profileIconData, versionData
    private void updateProfileIcon() {
        if (!profileIconDone && profileIconId != 0 && version != null) {
            RiotAPI.fetchProfileIcon(version, profileIconId, new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call,
                                       okhttp3.Response response) throws IOException {
                    final Drawable icon = Drawable.createFromStream(
                            response.body().byteStream(), null);
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            profileIcon.setMinimumWidth(icon.getMinimumWidth());
                            profileIcon.setMinimumHeight(icon.getMinimumHeight());
                            profileIcon.setMaxWidth(icon.getIntrinsicWidth());
                            profileIcon.setMaxHeight(icon.getIntrinsicHeight());
                            profileIcon.setImageDrawable(icon);
                        }
                    });
                }

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {

                }
            });
            profileIconDone = true;
        }
    }

    private void handleGetSummonerResponse(final SummonerDTO summoner) {
        summonerLevel.setText(String.valueOf(summoner.summonerLevel));
        profileIconId = summoner.profileIconId;
        updateProfileIcon();

        Call<List<LeagueListDTO>> getLeagueListRequest = apiService.getLeagueList(summoner.id);

        getLeagueListRequest.enqueue(new Callback<List<LeagueListDTO>>() {
            @Override
            public void onResponse(Call<List<LeagueListDTO>> call,
                                   Response<List<LeagueListDTO>> response) {
                handleLeagueListResponse(summoner, response.body());
            }

            @Override
            public void onFailure(Call<List<LeagueListDTO>> call, Throwable t) {

            }
        });
    }

    private void handleLeagueListResponse(SummonerDTO summoner, List<LeagueListDTO> leagueList) {
        for (LeagueListDTO league : leagueList) {
            queueText.setText(league.queue);
            tierText.setText(league.tier);
            leagueText.setText(league.name);
            levelText.setText(String.valueOf(summoner.summonerLevel));


//            for (LeagueItemDTO item : league.entries) {
//                if (item.playerOrTeamId == summoner.name) {
//                    Log.v("gcadebug", "found them");
//                }
//            }
        }
    }
}
