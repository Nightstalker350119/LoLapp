package com.norbertotaveras.game_companion_app;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
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
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.norbertotaveras.game_companion_app.DTO.League.LeaguePositionDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchReferenceDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchlistDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantIdentityDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class SummonerSearchResultsActivity extends AppCompatActivity implements View.OnClickListener, AbsListView.OnScrollListener {
    private RiotGamesService apiService;

    private String searchName;
    private long searchAccountId;

    private ImageView profileIcon;

    private long profileIconId;

    private Handler uiThreadHandler;

    private TextView summonerName;
    private TextView queueText;
    private TextView summonerSummary;
    LeagueCollectionFragmentAdapter leaguePagerAdapter;
    private ViewPager leaguePager;
    private ListView matchList;
    private ConstraintLayout matchListNoResults;
    private MatchListAdapter matchListAdapter;

    final int matchBatchSize = 10;
    int currentMatchIndex = 0;

    RiotAPI.DeferredRequest<SummonerDTO> deferredSummoner;

    ArrayList<Long> matchIds;
    ConcurrentHashMap<Long, MatchDTO> matchResults;
    boolean matchListAtEnd;

    private final LeagueInfo leagueInfo;

    // The order of this must match filterButtons!
    private final long[] filterQueueIds = new long[] {
            -1,     // all
            420,    // ranked solo
            440,    // ranked flex
            400,    // normal (draft blind)
            450,    // ARAM
            1010    // Event
    };

    // The order of this must match filterQueueIds!
    private Button[] filterButtons;
    private long currentFilter;

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

        leaguePager = findViewById(R.id.league_pager);

        leaguePagerAdapter = new LeagueCollectionFragmentAdapter(getSupportFragmentManager());
        leaguePager.setAdapter(leaguePagerAdapter);

        filterButtons = new Button[] {
                findViewById(R.id.game_filter_total),
                findViewById(R.id.game_filter_ranked_solo),
                findViewById(R.id.game_filter_ranked_flex),
                findViewById(R.id.game_filter_draft_blind),
                findViewById(R.id.game_filter_aram),
                findViewById(R.id.game_filter_event)
        };
        currentFilter = -1;

        matchIds = new ArrayList<>(matchBatchSize);
        matchResults = new ConcurrentHashMap<>(matchBatchSize);

        matchList = findViewById(R.id.match_list);
        matchListNoResults = findViewById(R.id.match_list_no_results);
        matchListAdapter = new MatchListAdapter();
        matchList.setAdapter(matchListAdapter);
        matchListAtEnd = false;

        for (int i = 0; i < filterButtons.length; ++i) {
            filterButtons[i].setOnClickListener(this);
        }

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

        apiService = RiotAPI.getInstance(getApplicationContext());

        Intent intent = getIntent();
        searchName = intent.getStringExtra("searchName");
        searchAccountId = intent.getLongExtra("searchAccountId", 0);

        uiThreadHandler = UIHelper.createRunnableLooper();

        search();

        matchList.setOnScrollListener(this);
    }

    private void search() {
        if (searchName != null) {
            deferredSummoner = new RiotAPI.DeferredRequest<>(
                    apiService.getSummonerByName(searchName));
        } else {
            deferredSummoner = new RiotAPI.DeferredRequest<>(
                    apiService.getSummonerByAccountId(searchAccountId));
        }

        deferredSummoner.getData(new RiotAPI.AsyncCallback<SummonerDTO>() {
            @Override
            public void invoke(SummonerDTO item) {
                handleGetSummonerResponse(item);
            }
        });
    }

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
                    String.format(Locale.US, "Summoner \"%s\" not found", searchName),
                    Toast.LENGTH_SHORT);
            finish();
            return;
        }

        RecentSearchStorage.add(this, summoner.accountId, false);

        summonerName.setText(summoner.name);

        String summary = String.valueOf("Level " + String.valueOf(summoner.summonerLevel) +
            " | ");
        summonerSummary.setText(summary);

        profileIconId = summoner.profileIconId;
        updateProfileIcon();

        leagueInfo.setSummoner(summoner);

        final Call<List<LeaguePositionDTO>> getLeaguePositionRequest =
                apiService.getLeaguePositionsBySummonerId(summoner.id);

        RiotAPI.rateLimitRequest(getLeaguePositionRequest, new Callback<List<LeaguePositionDTO>>() {
            @Override
            public void onResponse(Call<List<LeaguePositionDTO>> call,
                                   Response<List<LeaguePositionDTO>> response) {
                leagueInfo.setLeaguePositionDTO(response.body());
            }

            @Override
            public void onFailure(Call<List<LeaguePositionDTO>> call, Throwable t) {
                Log.e("RiotAPI", "getLeaguePositionsBySummonerId failed");
            }
        });

        getMatchList(summoner);
    }

    private void getMatchList(SummonerDTO summoner) {
        if (matchListAtEnd)
            return;

        final int beginIndex = currentMatchIndex;
        currentMatchIndex += matchBatchSize;

        Call<MatchlistDTO> getMatchlistRequest;

        if (currentFilter < 0) {
            getMatchlistRequest = apiService.getMatchList(
                    summoner.accountId, beginIndex, beginIndex + matchBatchSize);
        } else {
            getMatchlistRequest = apiService.getMatchList_FilterQueue(
                    summoner.accountId, beginIndex, beginIndex + matchBatchSize,
                    String.valueOf(currentFilter));
        }

        RiotAPI.rateLimitRequest(getMatchlistRequest, new Callback<MatchlistDTO>() {
            @Override
            public void onResponse(Call<MatchlistDTO> call, Response<MatchlistDTO> response) {
                MatchlistDTO matchList = response.body();

                if (matchList == null || matchList.matches.isEmpty()) {
                    // Call appendResults to invoke the logic that shows the "no results" element
                    matchListAdapter.appendResults(beginIndex);
                    matchListAtEnd = true;
                    return;
                }

                Log.v("MatchList", "Requesting " +
                        String.valueOf(matchList.matches.size()));

                for (MatchReferenceDTO match : matchList.matches)
                    matchIds.add(match.gameId);

                for (MatchReferenceDTO match : matchList.matches) {
                    Log.v("MatchList", "Requesting match id=" +
                            String.valueOf(match.gameId));

                    Call<MatchDTO> matchRequest = apiService.getMatch(match.gameId);

                    RiotAPI.rateLimitRequest(matchRequest, new Callback<MatchDTO>() {
                        @Override
                        public void onResponse(Call<MatchDTO> call, Response<MatchDTO> response) {
                            MatchDTO match = response.body();

                            assert(match != null);
                            matchResults.put(match.gameId, match);

                            if (matchResults.size() == matchIds.size())
                                matchListAdapter.appendResults(beginIndex);

                            Log.v("MatchList", "Got matches " +
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
                Log.e("RiotAPI", "getMatchList call failed");
            }
        });
    }

    void setMatchFilter(long filter) {
        currentFilter = filter;
        matchResults.clear();
        matchIds.clear();
        currentMatchIndex = 0;
        matchListAdapter.reset();
        getMoreMatches();
    }

    @Override
    public void onClick(View view) {
        boolean isFilterClick = false;
        for (int i = 0; i < filterButtons.length; ++i) {
            if (view == filterButtons[i]) {
                setMatchFilter(filterQueueIds[i]);
                isFilterClick = true;
            }
        }
        if (isFilterClick) {
            for (int i = 0; i < filterButtons.length; ++i) {
                if (view == filterButtons[i]) {
                    filterButtons[i].setTextColor(0xffff0000);
                } else {
                    filterButtons[i].setTextColor(0xff000000);
                }
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleIndex,
                         int visibleItemCount, int totalItemCount) {
        if (absListView != matchList)
            return;

        int lastVisible = firstVisibleIndex + visibleItemCount;

        if (lastVisible > 0 && lastVisible == currentMatchIndex) {
            getMoreMatches();
        }
    }

    private void getMoreMatches() {
        deferredSummoner.getData(new RiotAPI.AsyncCallback<SummonerDTO>() {
            @Override
            public void invoke(SummonerDTO summoner) {
                getMatchList(summoner);
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
            return leagueInfo != null ? leagueInfo.leaguePositions.size() : 0;
        }
    }

    private void updateLeagueList(LeagueInfo info) {
        leaguePagerAdapter.setLeagueInfo(info);
        leaguePagerAdapter.notifyDataSetChanged();
    }

    public class LeagueInfo implements Serializable {
        SummonerDTO summoner;

        // Hash table keyed on queueType
        Map<String, LeaguePositionDTO> leaguePositions;

        public void setSummoner(SummonerDTO summoner) {
            this.summoner = summoner;
            checkDone();
        }

        public void setLeaguePositionDTO(List<LeaguePositionDTO> leaguePositions) {
            this.leaguePositions = new HashMap<>();
            for (LeaguePositionDTO item : leaguePositions)
                this.leaguePositions.put(item.queueType, item);
            checkDone();
        }

        public void checkDone() {
            if (summoner != null && leaguePositions != null)
                updateLeagueList(this);
        }
    }

    private class MatchListAdapter extends BaseAdapter {
        private ArrayList<Long> allMatches;
        private MatchDTO[] matches;
        final LayoutInflater inflater;

        AtomicInteger uniqueId;

        public MatchListAdapter() {
            inflater = getLayoutInflater();
            allMatches = new ArrayList<>(matchBatchSize);
            uniqueId = new AtomicInteger(0);
        }

        public void reset() {
            allMatches.clear();
        }

        public void appendResults(int beginIndex) {
            for (int i = beginIndex, e = matchIds.size(); i < e; ++i) {
                long id = matchIds.get(i);
                MatchDTO item = matchResults.get(id);

                if (item == null)
                    continue;

                // Find insertion point
                int st = 0, en = allMatches.size(), mid = 0;
                while (st < en) {
                    mid = st + ((en - st) >> 1);

                    long cmpId = allMatches.get(mid);
                    MatchDTO cmpItem = matchResults.get(cmpId);

                    if (cmpItem.gameCreation <= item.gameCreation)
                        en = mid;
                    else
                        st = mid + 1;
                }

                allMatches.add(st, id);
            }

            matches = new MatchDTO[allMatches.size()];
            for (int i = 0, e = allMatches.size(); i < e; ++i) {
                long id = allMatches.get(i);
                MatchDTO item = matchResults.get(id);
                matches[i] = item;
            }

            notifyDataSetChanged();

            matchListNoResults.setVisibility(matches.length != 0 ? View.GONE : View.VISIBLE);
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
            if (a == 0 || b == 0)
                return 0;

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
            return String.format(Locale.US, "%.2f", n);
        }

        private String formatTimeDiff(long diff) {
            long n;
            if (diff < 60 * 1000) {
                n = diff / (1000);
                return String.valueOf(n) + " second" + (n != 1 ? "s" : "");
            } else if (diff < 60 * 60 * 1000) {
                n = diff / (60 * 1000);
                return String.valueOf(n) + " minute" + (n != 1 ? "s" : "");
            } else if (diff < 24 * 60 * 60 * 1000) {
                n = diff / (60 * 60 * 1000);
                return String.valueOf(n) + " hour" + (n != 1 ? "s" : "");
            } else {
                n = diff / (24 * 60 * 60 * 1000);
                return String.valueOf(n) + " day" + (n != 1 ? "s" : "");
            }
        }

        private String formatMinSec(long seconds) {
            long s = seconds % 60;
            long m = (seconds / 60) % 60;
            long h = (seconds / 3600);
            if (h == 0)
                return String.valueOf(m) + "m " + String.valueOf(s) + "s";
            return String.valueOf(h) + "h " + String.valueOf(m) + "m " + String.valueOf(s) + "s";
        }

        @Override
        public View getView(final int position, View recycledView, ViewGroup viewGroup) {
            if (recycledView == null) {
                recycledView = inflater.inflate(R.layout.fragment_match_list,
                        viewGroup, false);
            }

            final View view = recycledView;

            // Assign a unique identifier to this row to handle racing responses on recycled views
            final int rowId = uniqueId.getAndIncrement();
            view.setTag(rowId);

            Log.v("LeagueInfoList", "Formatting list item " + String.valueOf(position));

            final ImageView championIcon = view.findViewById(R.id.champion_icon);
            final ImageView[] spellIcons = new ImageView[] {
                    view.findViewById(R.id.spell0),
                    view.findViewById(R.id.spell1)
            };
            final ImageView[] runeIcons = new ImageView[] {
                    view.findViewById(R.id.rune0),
                    view.findViewById(R.id.rune1)
            };
            final ImageView[] itemIcons = new ImageView[] {
                    view.findViewById(R.id.items0),
                    view.findViewById(R.id.items1),
                    view.findViewById(R.id.items2),
                    view.findViewById(R.id.items3),
                    view.findViewById(R.id.items4),
                    view.findViewById(R.id.items5),
                    view.findViewById(R.id.items6)
            };

            final TextView kda = view.findViewById(R.id.kda);
            final TextView kdaRatio = view.findViewById(R.id.kda_ratio);
            final TextView specialKills = view.findViewById(R.id.special_kills);
            final TextView gameType = view.findViewById(R.id.game_mode);
            final TextView gameDuration = view.findViewById(R.id.game_duration);
            final TextView gameAgo = view.findViewById(R.id.game_ago);

            final TextView level = view.findViewById(R.id.level);
            final TextView minionKills = view.findViewById(R.id.minion_kills);
            final TextView participation = view.findViewById(R.id.participation);

            final TextView gameOutcome = view.findViewById(R.id.game_outcome);

            final MatchDTO match = matches[position];

            long timeSince = new Date().getTime() - match.gameCreation;

            gameAgo.setText(formatTimeDiff(timeSince) + " ago");

            String gameModeText;

//            switch (match.gameMode) {
//                case "CLASSIC":
//                    gameModeText = "Ranked Solo";
//                    break;
//
//                default:
//                    gameModeText = match.gameMode;
//                    break;
//            }

            switch (match.queueId) {
                case 400:
                    gameModeText = "Normal";
                    break;

                case 420:
                    gameModeText = "Ranked Solo";
                    break;

                case 440:
                    gameModeText = "Ranked Flex";
                    break;

                case 450:
                    gameModeText = "ARAM";
                    break;

                case 1010:
                    gameModeText = "Snow Urf";
                    break;

                default:
                    gameModeText = "queueId=" + match.queueId;
                    break;
            }

            gameType.setText(gameModeText);
//            +
//                    "\nm=" + match.gameMode +
//                    "\nt=" + match.gameType +
//                    "\np=" + match.platformId +
//                    "\nq=" + match.queueId);


            gameDuration.setText(formatMinSec(match.gameDuration));

            // Avoid flash of old content
            kda.setText("");
            gameOutcome.setText("");
            championIcon.setImageDrawable(null);
            for (int i = 0; i < spellIcons.length; ++i)
                spellIcons[i].setImageDrawable(null);
            for (int i = 0; i < runeIcons.length; ++i)
                runeIcons[i].setImageDrawable(null);
            for (int i = 0; i < itemIcons.length; ++i)
                itemIcons[i].setImageDrawable(null);
            participation.setText("");
            minionKills.setText("");
            level.setText("");
            kdaRatio.setText("");
            specialKills.setText("");

            deferredSummoner.getData(new RiotAPI.AsyncCallback<SummonerDTO>()
            {
                @Override
                public void invoke(SummonerDTO summoner) {
                    // See if we're populating a recycled view too late
                    if ((int)view.getTag() != rowId)
                        return;

                    ParticipantIdentityDTO summonerIdentity = null;

                    for (ParticipantIdentityDTO participantIdentity : match.participantIdentities) {
                        if (participantIdentity.player.accountId == summoner.accountId) {
                            summonerIdentity = participantIdentity;
                            break;
                        }
                    }

                    ParticipantDTO participantFind = null;

                    for (ParticipantDTO participantSearch : match.participants) {
                        if (participantSearch.participantId == summonerIdentity.participantId) {
                            participantFind = participantSearch;
                            break;
                        }
                    }

                    final ParticipantDTO participant = participantFind;

                    if (participant != null) {
                        final String kdaText = String.format(Locale.US, "%d / %d / %d",
                                participant.stats.kills, participant.stats.deaths,
                                participant.stats.assists);
                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // See if we're populating a recycled view too late
                                if ((int)view.getTag() != rowId)
                                    return;

                                kda.setText(kdaText);
                            }
                        });
                    }

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // See if we're populating a recycled view too late
                            if ((int)view.getTag() != rowId)
                                return;

                            view.setBackgroundColor(participant.stats.win
                                    ? 0xffa3cfec : 0xffe2b6b3);
                            view.setBackgroundTintMode(PorterDuff.Mode.ADD);

                            gameOutcome.setText(participant.stats.win
                                    ? "Victory" : "Defeat");
                            gameOutcome.setTextColor(participant.stats.win
                                    ? 0xff1a78ae : 0xffc6443e);
                        }
                    });

                    RiotAPI.fetchChampionIcon(participant.championId,
                            new RiotAPI.AsyncCallback<Drawable>()
                    {
                        @Override
                        public void invoke(final Drawable item) {
                            // See if we're populating a recycled view too late
                            if ((int)view.getTag() != rowId)
                                return;

                            uiThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // See if we're populating a recycled view too late
                                    if ((int)view.getTag() != rowId)
                                        return;

                                    championIcon.setImageDrawable(item);
                                }
                            });
                        }
                    });

                    for (int i = 0; i < 2; ++i) {
                        final int tempI = i;
                        long id = i > 0 ? participant.spell2Id : participant.spell1Id;
                        RiotAPI.fetchSpellIcon(id, new RiotAPI.AsyncCallback<Drawable>() {
                            @Override
                            public void invoke(final Drawable item) {
                                // See if we're populating a recycled view too late
                                if ((int)view.getTag() != rowId)
                                    return;

                                uiThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // See if we're populating a recycled view too late
                                        if ((int)view.getTag() != rowId)
                                            return;

                                        spellIcons[tempI].setImageDrawable(item);
                                    }
                                });
                            }
                        });
                    }

                    for (int i = 0; i < 2; ++i) {
                        final int tempI = i;

                        if (participant.runes == null || participant.runes.size() <= i)
                            continue;

                        long id = participant.runes.get(i).runeId;
                        RiotAPI.fetchRuneIcon(id, new RiotAPI.AsyncCallback<Drawable>() {
                            @Override
                            public void invoke(final Drawable item) {
                                // See if we're populating a recycled view too late
                                if ((int)view.getTag() != rowId)
                                    return;

                                uiThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // See if we're populating a recycled view too late
                                        if ((int)view.getTag() != rowId)
                                            return;

                                        runeIcons[tempI].setImageDrawable(item);
                                    }
                                });
                            }
                        });
                    }

                    long[] itemIds = {
                            participant.stats.item0,
                            participant.stats.item1,
                            participant.stats.item2,
                            participant.stats.item3,
                            participant.stats.item4,
                            participant.stats.item5,
                            participant.stats.item6
                    };

                    for (int i = 0; i < 7; ++i) {
                        final int tempI = i;
                        long id = itemIds[i];

                        if (id == 0)
                            continue;

                        RiotAPI.fetchItemIcon(id, new RiotAPI.AsyncCallback<Drawable>() {
                            @Override
                            public void invoke(final Drawable item) {
                                // See if we're populating a recycled view too late
                                if ((int)view.getTag() != rowId)
                                    return;

                                uiThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // See if we're populating a recycled view too late
                                        if ((int)view.getTag() != rowId)
                                            return;

                                        itemIcons[tempI].setImageDrawable(item);
                                    }
                                });
                            }
                        });
                    }

                    long totalDamageAll = 0;
                    long minionKillsAll = 0;
                    for (ParticipantDTO participantScan : match.participants) {
                        totalDamageAll += participantScan.stats.totalDamageDealtToChampions;
                        minionKillsAll += participantScan.stats.totalMinionsKilled +
                                participantScan.stats.neutralMinionsKilled;
                    }

                    final long participantMinionKills = participant.stats.totalMinionsKilled +
                            participant.stats.neutralMinionsKilled;

                    final long participantPercent = 100 *
                            participant.stats.totalDamageDealtToChampions / totalDamageAll;
                    final long killsPlusAssists = participant.stats.kills + participant.stats.assists;

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // See if we're populating a recycled view too late
                            if ((int)view.getTag() != rowId)
                                return;

                            participation.setText(killsPlusAssists > 0
                                    ? String.valueOf(100 * participant.stats.kills / killsPlusAssists)
                                    : "-");
                            String percentJungle = participant.stats.totalMinionsKilled != 0
                                    ? String.valueOf(100 *
                                    (participant.stats.neutralMinionsKilledEnemyJungle +
                                            participant.stats.neutralMinionsKilledTeamJungle) /
                                    participant.stats.totalMinionsKilled)
                                    : "-";
                            minionKills.setText(String.valueOf(participantMinionKills) +
                                    " (" + percentJungle + ") CS");
                            level.setText("Lvl " + String.valueOf(participant.stats.champLevel));
                        }
                    });

                    int deaths = participant.stats.deaths;

                    final String kdaText;
                    if (deaths > 0) {
                        int kdaRatioGcd = gcd((int)killsPlusAssists, deaths);
                        final double numer = kdaRatioGcd != 0
                                ? (double)killsPlusAssists / kdaRatioGcd : killsPlusAssists;
                        final double denom = kdaRatioGcd != 0
                                ? (double)deaths / kdaRatioGcd : deaths;

                        kdaText = simpleDouble(numer) + ":" + simpleDouble(denom);
                    } else {
                        kdaText = "Perfect";
                    }

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // See if we're populating a recycled view too late
                            if ((int)view.getTag() != rowId)
                                return;

                            kdaRatio.setText(kdaText);
                        }
                    });

                    String specialKillsText = null;

                    if (participant.stats.pentaKills > 0) {
                        specialKillsText = "Penta-kill!";
                    } else if (participant.stats.tripleKils > 0) {
                        specialKillsText = "Triple-kill";
                    } else if (participant.stats.doubleKills > 0) {
                        specialKillsText = "Double-kill";
                    }

                    final String specialKillsTextTemp = specialKillsText;
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // See if we're populating a recycled view too late
                            if ((int)view.getTag() != rowId)
                                return;

                            if (specialKillsTextTemp != null) {
                                specialKills.setText(specialKillsTextTemp);
                                specialKills.setVisibility(View.VISIBLE);
                            } else {
                                specialKills.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            });

            Log.v("LeagueInfoList",
                    "Formatting list item " + String.valueOf(position) + " done");

            return view;
        }
    }
}
