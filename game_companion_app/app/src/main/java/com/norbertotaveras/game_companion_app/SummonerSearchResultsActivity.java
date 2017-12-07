package com.norbertotaveras.game_companion_app;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.norbertotaveras.game_companion_app.DTO.League.LeagueListDTO;
import com.norbertotaveras.game_companion_app.DTO.League.LeaguePositionDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class SummonerSearchResultsActivity extends AppCompatActivity {
    private RiotGamesService apiService;
    private String searchText;
    private ImageView profileIcon;

    private long profileIconId;

    private Handler uiThreadHandler;

    private TextView summonerName;
    private TextView tierText;
    private TextView queueText;
    private TextView summonerSummary;
    LeagueCollectionFragmentAdapter leaguePagerAdapter;
    private ViewPager leaguePager;
    private final LeagueInfo leagueInfo;

    public SummonerSearchResultsActivity() {
        leagueInfo = new LeagueInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summoner_search_results);

        summonerName = findViewById(R.id.summoner_name);
        summonerSummary = findViewById(R.id.summoner_summary);
        profileIcon = findViewById(R.id.profile_icon);
        queueText = findViewById(R.id.queue_name);

        tierText = findViewById(R.id.tier);
        leaguePager = findViewById(R.id.league_pager);

        leaguePagerAdapter = new LeagueCollectionFragmentAdapter(getSupportFragmentManager());
        leaguePager.setAdapter(leaguePagerAdapter);

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
        }
        catch (Exception ex) {
            Log.e("riottest", String.format("request completely failed = %s", ex));
        }
    }

//    private void handleGetVersionsRequest(List<String> versions) {
//
//    }

    // Needs profileIconData, versionData
    private void updateProfileIcon() {
        RiotAPI.fetchProfileIcon(profileIconId, new okhttp3.Callback() {
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
    }

    private void handleGetSummonerResponse(final SummonerDTO summoner) {
        if (summoner == null) {
            UIHelper.showToast(this,
                    String.format("Summoner \"%s\" not found", searchText), Toast.LENGTH_SHORT);
            finish();
            return;
        }

        summonerName.setText(summoner.name);

        String summary = String.valueOf("Level " + String.valueOf(summoner.summonerLevel) +
            " | ");
        summonerSummary.setText(summary);

        profileIconId = summoner.profileIconId;
        updateProfileIcon();

        final Call<List<LeagueListDTO>> getLeagueListRequest =
                apiService.getLeagueList(summoner.id);

        leagueInfo.setSummoner(summoner);

        getLeagueListRequest.enqueue(new Callback<List<LeagueListDTO>>() {
            @Override
            public void onResponse(Call<List<LeagueListDTO>> call,
                                   Response<List<LeagueListDTO>> response) {
                leagueInfo.setLeagueList(response.body());
            }

            @Override
            public void onFailure(Call<List<LeagueListDTO>> call, Throwable t) {

            }
        });

        final Call<List<LeaguePositionDTO>> getLeaguePositionRequest =
                apiService.getLeaguePositions(summoner.id);

        getLeaguePositionRequest.enqueue(new Callback<List<LeaguePositionDTO>>() {
            @Override
            public void onResponse(Call<List<LeaguePositionDTO>> call,
                                   Response<List<LeaguePositionDTO>> response) {
                leagueInfo.setLeaguePositionDTO(response.body());
            }

            @Override
            public void onFailure(Call<List<LeaguePositionDTO>> call, Throwable t) {

            }
        });
    }

    private static class LeagueCollectionFragmentAdapter extends FragmentStatePagerAdapter {
        LeagueInfo leagueInfo;

        LeagueCollectionFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setLeagueInfo(LeagueInfo info) {
            leagueInfo = info;
        }

        @Override
        public Fragment getItem(int position) {
            if (leagueInfo == null)
                return null;

            Fragment fragment = new LeagueCollectionFragment();
            Bundle args = new Bundle();
            args.putSerializable(LeagueCollectionFragment.ARG_LEAGUE_INFO, leagueInfo);
            args.putInt(LeagueCollectionFragment.ARG_POSITION, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return leagueInfo != null ? leagueInfo.leagueList.size() : 0;
        }
    }

    private void updateLeagueList(LeagueInfo info) {
        leaguePagerAdapter.setLeagueInfo(info);
        leaguePagerAdapter.notifyDataSetChanged();
    }

    public class LeagueInfo implements Serializable {
        SummonerDTO summoner;
        List<LeagueListDTO> leagueList;

        // Hash table keyed on queueType
        Map<String, LeaguePositionDTO> leaguePositions;

        public void setSummoner(SummonerDTO summoner) {
            this.summoner = summoner;
            checkDone();
        }

        public void setLeagueList(List<LeagueListDTO> leagueList) {
            this.leagueList = leagueList;
            checkDone();
        }

        public void setLeaguePositionDTO(List<LeaguePositionDTO> leaguePositions) {
            this.leaguePositions = new HashMap<>();
            for (LeaguePositionDTO item : leaguePositions)
                this.leaguePositions.put(item.queueType, item);
            checkDone();
        }

        public void checkDone() {
            if (summoner != null && leagueList != null && leaguePositions != null)
                updateLeagueList(this);
        }
    }
}
