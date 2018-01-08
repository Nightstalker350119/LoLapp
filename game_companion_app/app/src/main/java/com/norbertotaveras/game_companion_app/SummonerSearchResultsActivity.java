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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.norbertotaveras.game_companion_app.DTO.League.LeagueListDTO;
import com.norbertotaveras.game_companion_app.DTO.League.LeaguePositionDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchEventDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchReferenceDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchlistDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantIdentityDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionListDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private ListView matchList;
    private MatchListAdapter matchListAdapter;

    RiotAPI.DeferredRequest<SummonerDTO> deferredSummoner;

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

        matchList = findViewById(R.id.match_list);
        matchListAdapter = new MatchListAdapter();
        matchList.setAdapter(matchListAdapter);

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
        deferredSummoner = new RiotAPI.DeferredRequest<>(apiService.getSummonersByName(searchText));

        deferredSummoner.getData(new RiotAPI.AsyncCallback<SummonerDTO>() {
            @Override
            public void invoke(SummonerDTO item) {
                handleGetSummonerResponse(item);
            }
        });
    }

//    private void handleGetVersionsRequest(List<String> versions) {
//
//    }

    // Needs profileIconData, versionData
    private void updateProfileIcon() {
        RiotAPI.fetchProfileIcon(profileIconId, new RiotAPI.AsyncCallback<Drawable>() {
            @Override
            public void invoke(final Drawable icon) {
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

        leagueInfo.setSummoner(summoner);

        final Call<List<LeagueListDTO>> getLeagueListRequest =
                apiService.getLeagueList(summoner.id);

        RiotAPI.rateLimitRequest(getLeagueListRequest, new Callback<List<LeagueListDTO>>() {
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

        RiotAPI.rateLimitRequest(getLeaguePositionRequest, new Callback<List<LeaguePositionDTO>>() {
            @Override
            public void onResponse(Call<List<LeaguePositionDTO>> call,
                                   Response<List<LeaguePositionDTO>> response) {
                leagueInfo.setLeaguePositionDTO(response.body());
            }

            @Override
            public void onFailure(Call<List<LeaguePositionDTO>> call, Throwable t) {

            }
        });

        getMatchList(summoner);
    }

    private void getMatchList(SummonerDTO summoner) {
        final ConcurrentHashMap<Long, MatchDTO> matchResults =
                new ConcurrentHashMap<>(20);
        final ArrayList<Long> matchIds = new ArrayList<>(20);

        final Call<MatchlistDTO> getMatchlistRequest = apiService.getMatchList(
                summoner.accountId, 0, 30);

        RiotAPI.rateLimitRequest(getMatchlistRequest, new Callback<MatchlistDTO>() {
            @Override
            public void onResponse(Call<MatchlistDTO> call, Response<MatchlistDTO> response) {
                MatchlistDTO matchList = response.body();

                Log.v("MatchList", "Requesting " +
                        String.valueOf(matchList.matches.size()));

                for (MatchReferenceDTO match : matchList.matches) {
                    matchIds.add(match.gameId);
                }

                for (MatchReferenceDTO match : matchList.matches) {
                    Log.v("MatchList", "Requesting match id=" +
                            String.valueOf(match.gameId));

                    Call<MatchDTO> matchRequest = apiService.getMatch(match.gameId);

                    RiotAPI.rateLimitRequest(matchRequest, new Callback<MatchDTO>() {
                        @Override
                        public void onResponse(Call<MatchDTO> call, Response<MatchDTO> response) {
                            MatchDTO match = response.body();

                            matchResults.put(match.gameId, match);

                            if (matchResults.size() == matchIds.size())
                                handleMatchList(matchIds, matchResults);

                            Log.v("MatchList", "Got match " +
                                    String.valueOf(matchResults.size()) + " id=" +
                                    String.valueOf(match.gameId));
                        }

                        @Override
                        public void onFailure(Call<MatchDTO> call, Throwable t) {
                            Log.e("MatchList", "Failed to get match: " + t.toString());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<MatchlistDTO> call, Throwable t) {

            }
        });
    }

    private void handleMatchList(ArrayList<Long> matchIds,
                                 ConcurrentHashMap<Long, MatchDTO> matchResults) {
        matchListAdapter.setMatchList(matchResults.values());
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

    private class MatchListAdapter extends BaseAdapter {
        private MatchDTO[] matches;
        LayoutInflater inflater;

        public MatchListAdapter() {
            inflater = getLayoutInflater();

        }

        public void setMatchList(Collection<MatchDTO> matches) {
            this.matches = matches.toArray(new MatchDTO[matches.size()]);

            Arrays.sort(this.matches, new Comparator<MatchDTO>() {
                @Override
                public int compare(MatchDTO matchDTO, MatchDTO t1) {
                    return matchDTO.gameCreation > t1.gameCreation ? -1 :
                            matchDTO.gameCreation < t1.gameCreation ? 1 :
                                    0;
                }
            });

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return matches != null ? matches.length : 0;
        }

        @Override
        public Object getItem(int i) {
            return matches != null ? matches[i] : null;
        }

        @Override
        public long getItemId(int i) {
            return matches != null ? matches[i].gameId : null;
        }

        // Find greatest common divisor using simple Euclid's algorithm
        int gcd(int a, int b)
        {
            while (a != b) {
                if (a > b)
                    a -= b;
                else
                    b -= a;
            }

            return a;
        }

        String simpleDouble(double n) {
            if (n == Math.floor(n))
                return String.valueOf((int)n);
            return String.format("%.2f", n);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null)
                view = inflater.inflate(R.layout.fragment_match_list, viewGroup, false);

            final ImageView championIcon = view.findViewById(R.id.champion_icon);
            final ImageView spell0 = view.findViewById(R.id.spell0);
            final ImageView spell1 = view.findViewById(R.id.spell1);
            final ImageView spell2 = view.findViewById(R.id.spell2);
            final ImageView spell3 = view.findViewById(R.id.spell3);
            final TextView kda = view.findViewById(R.id.kda);
            final TextView kdaRatio = view.findViewById(R.id.kda_ratio);
            final TextView specialKills = view.findViewById(R.id.special_kills);
            final TextView gameType = view.findViewById(R.id.game_mode);
            final TextView gameDuration = view.findViewById(R.id.game_duration);

            final MatchDTO match = matches[i];

            String gameModeText;

            switch (match.gameMode) {
                case "CLASSIC":
                    gameModeText = "Ranked Solo";
                    break;

                default:
                    gameModeText = match.gameMode;
                    break;
            }

            gameType.setText(gameModeText);
            gameDuration.setText("?");

            deferredSummoner.getData(new RiotAPI.AsyncCallback<SummonerDTO>()
            {
                @Override
                public void invoke(SummonerDTO summoner) {
                ParticipantIdentityDTO summonerIdentity = null;

                for (ParticipantIdentityDTO participantIdentity : match.participantIdentities) {
                    if (participantIdentity.player.accountId == summoner.accountId) {
                        summonerIdentity = participantIdentity;
                        break;
                    }
                }

                ParticipantDTO participant = null;

                for (ParticipantDTO participantSearch : match.participants) {
                    if (participantSearch.participantId == summonerIdentity.participantId) {
                        participant = participantSearch;
                        break;
                    }
                }

                if (participant != null) {
                    String kdaText = String.format("%d / %d / %d", participant.stats.kills,
                            participant.stats.deaths, participant.stats.assists);
                    kda.setText(kdaText);
                }

                RiotAPI.fetchChampionIcon(participant.championId,
                        new RiotAPI.AsyncCallback<Drawable>()
                {
                    @Override
                    public void invoke(final Drawable item) {
                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                championIcon.setImageDrawable(item);
                            }
                        });
                    }
                });

                int killsPlusAssists = participant.stats.kills + participant.stats.assists;
                int deaths = participant.stats.deaths;

                if (deaths > 0) {
                    int kdaRatioGcd = gcd(killsPlusAssists, deaths);
                    double numer = (double)killsPlusAssists / kdaRatioGcd;
                    double denom = (double)deaths / kdaRatioGcd;

                    kdaRatio.setText(simpleDouble(numer) + ":" + simpleDouble(denom));
                } else {
                    kdaRatio.setText("Perfect");
                }

                String specialKillsText = null;

                if (participant.stats.pentaKills > 0) {
                    specialKillsText = "Penta-kill!";
                } else if (participant.stats.tripleKils > 0) {
                    specialKillsText = "Triple-kill";
                } else if (participant.stats.doubleKills > 0) {
                    specialKillsText = "Double-kill";
                }

                if (specialKillsText != null) {
                    specialKills.setText(specialKillsText);
                    specialKills.setVisibility(View.VISIBLE);
                } else {
                    specialKills.setVisibility(View.GONE);
                }
                }
            });

            return view;
        }
    }
}
