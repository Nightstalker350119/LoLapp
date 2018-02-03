package com.norbertotaveras.game_companion_app;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
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
import java.text.SimpleDateFormat;
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

public class SummonerSearchResultsActivity
        extends AppCompatActivity implements View.OnScrollChangeListener {
    private RiotGamesService apiService;

    private String searchName;
    private long searchAccountId;

    private ImageView profileIcon;
    private ImageView tierIcon;
    private long profileIconId;

    private Handler uiThreadHandler;

    Point displaySize;
    private ScrollView scrollParent;

    private TextView summonerName;
    private TextView rank;
    private TextView leaguePoints;
    private TextView winLoss;
    private TextView queueText;
    private TextView summonerSummary;

    //private LeagueCollectionFragmentAdapter leaguePagerAdapter;
    //private ViewPager leaguePager;
    private LinearLayoutManager matchListLayoutManager;
    private RecyclerView matchList;
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
    private long currentFilter;
    private boolean initializing;
    private boolean gettingMatches;

    public SummonerSearchResultsActivity() {
        leagueInfo = new LeagueInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summoner_search_results);

        displaySize = getDisplaySize();
        scrollParent = findViewById(R.id.nested_scroll_parent);
        scrollParent.setNestedScrollingEnabled(true);

        rank = findViewById(R.id.rank_0);
        leaguePoints = findViewById(R.id.league_points_0);
        winLoss = findViewById(R.id.win_loss);

        summonerName = findViewById(R.id.summoner_name);
        summonerSummary = findViewById(R.id.summoner_summary);
        profileIcon = findViewById(R.id.profile_icon);
        tierIcon = findViewById(R.id.tier_icon_0);
        queueText = findViewById(R.id.queue_name);

        currentFilter = -1;

        matchIds = new ArrayList<>(matchBatchSize);
        matchResults = new ConcurrentHashMap<>(matchBatchSize);

        matchListLayoutManager = new LinearLayoutManager(this);

        matchList = findViewById(R.id.match_list);
        matchList.setLayoutManager(matchListLayoutManager);
        matchListAdapter = new MatchListAdapter();
        matchList.setAdapter(matchListAdapter);
        matchList.setHasFixedSize(true);
        matchList.setOnScrollChangeListener(this);

        matchListNoResults = findViewById(R.id.no_results);
        matchListAtEnd = false;

        initializing = true;

        matchList.setNestedScrollingEnabled(true);
        setMatchListHeight(displaySize.y - 375);

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

        apiService = RiotAPI.getInstance(getApplicationContext());

        Intent intent = getIntent();
        searchName = intent.getStringExtra("searchName");
        searchAccountId = intent.getLongExtra("searchAccountId", 0);

        uiThreadHandler = UIHelper.createRunnableLooper();

        search();
    }

    private void setMatchListHeight(int height) {
        ViewGroup.LayoutParams layoutParams = matchList.getLayoutParams();
        layoutParams.height = height;
        matchList.setLayoutParams(layoutParams);
    }

    private Point getDisplaySize() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
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

        String summary = String.valueOf("Level " + String.valueOf(summoner.summonerLevel));
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

                    initializing = false;
                    scrollParent.scrollTo(0, 0);

                    return;
                }

                Log.v("MatchList", "Requesting " +
                        String.valueOf(matchList.matches.size()));

                for (MatchReferenceDTO match : matchList.matches)
                    matchIds.add(match.gameId);

                for (MatchReferenceDTO match : matchList.matches) {
                    Log.v("MatchList", "Requesting match id=" +
                            String.valueOf(match.gameId));

                    RiotAPI.getCachedMatch(match.gameId, new RiotAPI.AsyncCallback<MatchDTO>() {
                        @Override
                        public void invoke(MatchDTO match) {
                            assert(match != null);
                            matchResults.put(match.gameId, match);

                            if (matchResults.size() == matchIds.size()) {
                                matchListAdapter.appendResults(beginIndex);
                                gettingMatches = false;

                                if (initializing) {
                                    initializing = false;
                                    scrollParent.scrollTo(0, 0);
                                }
                            }

                            Log.v("MatchList", "Got matches " +
                                    String.valueOf(matchResults.size()) + " id=" +
                                    String.valueOf(match.gameId));
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

    private void getMoreMatches() {
        if (!gettingMatches) {
            gettingMatches = true;
            deferredSummoner.getData(new RiotAPI.AsyncCallback<SummonerDTO>() {
                @Override
                public void invoke(SummonerDTO summoner) {
                    getMatchList(summoner);
                }
            });
        }
    }

    @Override
    public void onScrollChange(View view, int scrollX, int scrollY,
                               int oldScrollX, int oldScrollY)
    {
        switch (view.getId()) {
            case R.id.match_list:
                if (!matchListAtEnd && !matchList.canScrollVertically(1)) {
                    getMoreMatches();
                }
                break;
        }
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

    public class LeagueInfo implements Serializable {
        SummonerDTO summoner;

        // Hash table keyed on queueType
        Map<String, LeaguePositionDTO> leaguePositions;

        public void setSummoner(SummonerDTO summoner) {
            this.summoner = summoner;
        }

        public void setLeaguePositionDTO(List<LeaguePositionDTO> leaguePositions) {
            this.leaguePositions = new HashMap<>();
            for (LeaguePositionDTO item : leaguePositions)
                this.leaguePositions.put(item.queueType, item);

            LeaguePositionDTO rs5v5 = this.leaguePositions.get("RANKED_SOLO_5x5");

            int tierResource;
            String tierText;
            String lp;
            int winPercent;
            if (rs5v5 != null) {
                tierResource = RiotAPI.tierNameToResourceId(rs5v5.tier, rs5v5.rank);

                tierText = RiotAPI.beautifyTierName(rs5v5.tier);

                lp = String.valueOf(rs5v5.leaguePoints) + " LP";

                if (rs5v5.wins + rs5v5.losses > 0)
                    winPercent = 100 * rs5v5.wins / (rs5v5.wins + rs5v5.losses);
                else
                    winPercent = -1;
            } else {
                tierResource = RiotAPI.tierNameToResourceId("PROVISIONAL", "I");
                tierText = "Unranked";
                lp = "0 LP";
                winPercent = -1;
            }

            rank.setText(tierText);
            tierIcon.setImageResource(tierResource);
            leaguePoints.setText(lp);
            if (winPercent >= 0) {
                winLoss.setText(getResources().getString(R.string.win_loss,
                        rs5v5.wins, rs5v5.losses, winPercent));
            } else {
                winLoss.setText(getResources().getString(R.string.win_loss,
                        0, 0, 0));
            }
        }
    }

    private class MatchListItem extends RecyclerView.ViewHolder {
        final View view;

        final ConstraintLayout summaryContainer;

        final ImageView championIcon;
        final ImageView[] spellIcons;
        final ImageView[] runeIcons;

        final TextView kda;
        final TextView kdaRatio;
        final TextView specialKills;
        final TextView gameType;
        final TextView gameDuration;
        final TextView gameDate;
        final TextView gameId;

        final AtomicInteger uniqueId;

        public MatchListItem(View view) {
            super(view);

            this.view = view;
            uniqueId = new AtomicInteger(0);

            summaryContainer = view.findViewById(R.id.summary_container);

            championIcon = view.findViewById(R.id.champion_icon);

            spellIcons = new ImageView[] {
                    view.findViewById(R.id.spell0),
                    view.findViewById(R.id.spell1)
            };

            runeIcons = new ImageView[] {
                    view.findViewById(R.id.rune0),
                    view.findViewById(R.id.rune1)
            };

            kda = view.findViewById(R.id.kda);
            kdaRatio = view.findViewById(R.id.kda_ratio);
            specialKills = view.findViewById(R.id.special_kills);
            gameType = view.findViewById(R.id.game_mode);
            gameDuration = view.findViewById(R.id.game_duration);
            gameDate = view.findViewById(R.id.game_date);
            gameId = view.findViewById(R.id.game_id);
        }

        public void bind(final MatchDTO match) {
            // Assign a unique identifier to this row to handle racing responses on recycled views
            final int rowId = uniqueId.getAndIncrement();
            view.setTag(rowId);

            String gameModeText;
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

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            Date date = new Date(match.gameCreation);
            gameDate.setText(dateFormat.format(date));
            gameId.setText("#" + String.valueOf(match.gameId));

            final boolean isRemake = match.gameDuration < 300;

            gameDuration.setText(formatMinSec(match.gameDuration));

            // Avoid flash of old content
            kda.setText("");
            championIcon.setImageDrawable(null);
            for (int i = 0; i < spellIcons.length; ++i)
                spellIcons[i].setImageDrawable(null);
            for (int i = 0; i < runeIcons.length; ++i)
                runeIcons[i].setImageDrawable(null);
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

                    final String kdaText = String.format(Locale.US, "%d / %d / %d",
                            participant.stats.kills, participant.stats.deaths,
                            participant.stats.assists);

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

//                    long totalDamageAll = 0;
//                    long minionKillsAll = 0;
//                    for (ParticipantDTO participantScan : match.participants) {
//                        totalDamageAll += participantScan.stats.totalDamageDealtToChampions;
//                        minionKillsAll += participantScan.stats.totalMinionsKilled +
//                                participantScan.stats.neutralMinionsKilled;
//                    }

                    final long participantMinionKills = participant.stats.totalMinionsKilled +
                            participant.stats.neutralMinionsKilled;

                    int deaths = participant.stats.deaths;

                    final long killsPlusAssists = participant.stats.kills +
                            participant.stats.assists;

                    final String kdaRatioText = formatKdaRatio(killsPlusAssists, deaths);

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

                            view.setBackgroundColor(isRemake
                                    ? 0xffb6b6b6
                                    : participant.stats.win
                                    ? 0xffa3cfec : 0xffe2b6b3);
                            view.setBackgroundTintMode(PorterDuff.Mode.ADD);

                            runeIcons[0].setImageResource(
                                    RiotAPI.perkIdToResourceId(participant.stats.perk0));

                            runeIcons[1].setImageResource(
                                    RiotAPI.perkStyleIdToResourceId(
                                            participant.stats.perkSubStyle));

                            kda.setText(kdaText);
                            kdaRatio.setText(kdaRatioText);

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
    }

    private class MatchListAdapter extends RecyclerView.Adapter<MatchListItem> {
        private ArrayList<Long> allMatches;
        private MatchDTO[] matches;
        final LayoutInflater inflater;

        AtomicInteger uniqueId;

        public MatchListAdapter() {
            inflater = getLayoutInflater();
            allMatches = new ArrayList<>(matchBatchSize);
            uniqueId = new AtomicInteger(0);
        }

        @Override
        public MatchListItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View holder;
            holder = inflater.inflate(R.layout.fragment_match_list, parent, false);
            return new MatchListItem(holder);
        }

        @Override
        public void onBindViewHolder(MatchListItem holder, int position) {
            holder.bind(matches[position]);
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

            int oldLength = matches != null ? matches.length : 0;
            matches = new MatchDTO[allMatches.size()];
            for (int i = 0, e = allMatches.size(); i < e; ++i) {
                long id = allMatches.get(i);
                MatchDTO item = matchResults.get(id);
                matches[i] = item;
            }

            notifyItemRangeInserted(oldLength, matches.length - oldLength);

            matchListNoResults.setVisibility(matches.length != 0 ? View.GONE : View.VISIBLE);
        }

        @Override
        public long getItemId(int i) {
            return matches != null ? matches[i].gameId : null;
        }

        @Override
        public int getItemCount() {
            return matches != null ? matches.length : 0;
        }
    }

    private String formatKdaRatio(long killsPlusAssists, long deaths) {
        String kdaText;

        if (deaths > 0) {
            long kdaRatioGcd = gcd(killsPlusAssists, deaths);
            final double numer = kdaRatioGcd != 0
                    ? (double)killsPlusAssists / kdaRatioGcd : killsPlusAssists;
            final double denom = kdaRatioGcd != 0
                    ? (double)deaths / kdaRatioGcd : deaths;

            kdaText = simpleDouble(numer) + ":" + simpleDouble(denom);
        } else {
            kdaText = "Perfect";
        }

        return kdaText;
    }

    static String simpleDouble(double n) {
        if (n == Math.floor(n))
            return String.valueOf((int)n);
        return String.format(Locale.US, "%.2f", n);
    }

    // Find greatest common divisor using simple Euclid's algorithm
    long gcd(long a, long b)
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

    private class PlayerListAdapter extends BaseAdapter {
        List<ParticipantIdentityDTO> identities;
        List<ParticipantDTO> players;
        AtomicInteger uniqueId;

        public PlayerListAdapter(List<ParticipantIdentityDTO> identities,
                                 List<ParticipantDTO> players)
        {
            this.identities = identities;
            this.players = players;
            uniqueId = new AtomicInteger(0);
        }

        @Override
        public int getCount() {
            return players.size();
        }

        @Override
        public Object getItem(int i) {
            return players.get(i);
        }

        @Override
        public long getItemId(int i) {
            return players.get(i).participantId;
        }

        @Override
        public View getView(int position, View recycledView, ViewGroup viewGroup) {
            if (recycledView == null) {
                recycledView = getLayoutInflater().inflate(
                        R.layout.fragment_match_player_list, viewGroup, false);
            }

            final View view = recycledView;

            final int rowId = uniqueId.getAndIncrement();
            view.setTag(rowId);

            final ParticipantDTO participant = players.get(position);
            final ParticipantIdentityDTO identity = identities.get(position);

            final ImageView championIcon = view.findViewById(R.id.champion_icon);
            final ImageView[] spellIcons = new ImageView[] {
                    view.findViewById(R.id.spell0),
                    view.findViewById(R.id.spell1)
            };
            final ImageView[] runeIcons = new ImageView[] {
                    view.findViewById(R.id.rune0),
                    view.findViewById(R.id.rune1)
            };
            final TextView summonerName = view.findViewById(R.id.summoner_name);

            final TextView kda = view.findViewById(R.id.kda);
            final TextView kdaRatio = view.findViewById(R.id.kda_ratio);
            final ImageView[] items = new ImageView[] {
                    view.findViewById(R.id.items0),
                    view.findViewById(R.id.items1),
                    view.findViewById(R.id.items2),
                    view.findViewById(R.id.items3),
                    view.findViewById(R.id.items4),
                    view.findViewById(R.id.items5),
                    view.findViewById(R.id.items6)
            };
            final TextView goldEarned = view.findViewById(R.id.gold_earned);

            summonerName.setText(identity.player.summonerName);
            final String kdaText = String.format(Locale.US, "%d / %d / %d",
                    participant.stats.kills, participant.stats.deaths,
                    participant.stats.assists);
            kda.setText(kdaText);

            championIcon.setImageDrawable(null);

            RiotAPI.fetchChampionIcon(participant.championId,
                    new RiotAPI.AsyncCallback<Drawable>()
            {
                @Override
                public void invoke(final Drawable drawable) {
                    if ((int)view.getTag() != rowId)
                        return;

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if ((int)view.getTag() != rowId)
                                return;

                            championIcon.setImageDrawable(drawable);
                        }
                    });
                }
            });

            long[] spellIds = new long[] {
                    participant.spell1Id,
                    participant.spell2Id
            };

            for (int i = 0; i < spellIcons.length; ++i) {
                spellIcons[i].setImageDrawable(null);

                final int tempI = i;

                RiotAPI.fetchSpellIcon(spellIds[i], new RiotAPI.AsyncCallback<Drawable>() {
                    @Override
                    public void invoke(final Drawable drawable) {
                        if ((int)view.getTag() != rowId)
                            return;

                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if ((int)view.getTag() != rowId)
                                    return;

                                spellIcons[tempI].setImageDrawable(drawable);
                            }
                        });
                    }
                });
            }

            long[] runeIds = new long[] {
                    participant.runes != null ? participant.runes.get(0).runeId : 0,
                    participant.runes != null ? participant.runes.get(1).runeId : 0
            };

            for (int i = 0; i < runeIds.length; ++i) {
                runeIcons[i].setImageDrawable(null);

                final int tempI = i;

                RiotAPI.fetchRuneIcon(runeIds[i], new RiotAPI.AsyncCallback<Drawable>() {
                    @Override
                    public void invoke(final Drawable drawable) {
                        if ((int)view.getTag() != rowId)
                            return;

                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if ((int)view.getTag() != rowId)
                                    return;

                                runeIcons[tempI].setImageDrawable(drawable);
                            }
                        });
                    }
                });
            }

            long[] itemIds = new long[] {
                    participant.stats.item0,
                    participant.stats.item1,
                    participant.stats.item2,
                    participant.stats.item3,
                    participant.stats.item4,
                    participant.stats.item5,
                    participant.stats.item6
            };

            for (int i = 0; i < itemIds.length; ++i) {
                items[i].setImageDrawable(null);

                final int tempI = i;

                RiotAPI.fetchItemIcon(itemIds[i], new RiotAPI.AsyncCallback<Drawable>() {
                    @Override
                    public void invoke(final Drawable drawable) {
                        if ((int)view.getTag() != rowId)
                            return;

                        uiThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if ((int)view.getTag() != rowId)
                                    return;

                                items[tempI].setImageDrawable(drawable);
                            }
                        });
                    }
                });
            }

            return view;
        }
    }
}
