package com.norbertotaveras.game_companion_app;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.norbertotaveras.game_companion_app.DTO.Match.MatchDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchReferenceDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchlistDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantIdentityDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MatchesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MatchesFragment
        extends Fragment
        implements View.OnScrollChangeListener
{
    private LinearLayoutManager matchListLayoutManager;
    private RecyclerView matchList;
    private View matchListNoResults;
    private MatchListAdapter matchListAdapter;
    private Handler uiThreadHandler;

    private ArrayList<Long> matchIds;
    private ConcurrentHashMap<Long, MatchDTO> matchResults;
    private boolean matchListAtEnd;
    private boolean gettingMatches;

    private final int matchBatchSize = 10;
    private int currentMatchIndex = 0;
    private RiotGamesService apiService;
    private RiotAPI.DeferredRequest<SummonerDTO> deferredSummoner;

    public MatchesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MatchesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MatchesFragment newInstance() {
        MatchesFragment fragment = new MatchesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        uiThreadHandler = UIHelper.createRunnableLooper();

        apiService = RiotAPI.getInstance(getActivity());

        matchIds = new ArrayList<>(matchBatchSize);
        matchResults = new ConcurrentHashMap<>(matchBatchSize);

        matchListAtEnd = false;

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_matches, container, false);
        matchListLayoutManager = new LinearLayoutManager(getActivity());

        matchList = view.findViewById(R.id.match_list);
        matchList.setLayoutManager(matchListLayoutManager);
        matchListAdapter = new MatchListAdapter();
        matchList.setAdapter(matchListAdapter);
        matchList.setHasFixedSize(true);
        matchList.setOnScrollChangeListener(this);

        matchListNoResults = view.findViewById(R.id.no_results);
        return view;
    }

    public void setDeferredSummoner(RiotAPI.DeferredRequest<SummonerDTO> deferredSummoner) {
        this.deferredSummoner = deferredSummoner;
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

    public void getMoreMatches() {
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

    public void getMatchList(SummonerDTO summoner) {
        if (matchListAtEnd) {
            gettingMatches = false;
            return;
        }

        final int beginIndex = currentMatchIndex;
        currentMatchIndex += matchBatchSize;

        Call<MatchlistDTO> getMatchlistRequest;

        if (currentFilter.queueId < 0) {
            getMatchlistRequest = apiService.getMatchList(
                    summoner.accountId, beginIndex, beginIndex + matchBatchSize);
        } else {
            getMatchlistRequest = apiService.getMatchList_FilterQueue(
                    summoner.accountId, beginIndex, beginIndex + matchBatchSize,
                    String.valueOf(currentFilter.queueId));
        }

        RiotAPI.rateLimitRequest(getMatchlistRequest, new Callback<MatchlistDTO>() {
            @Override
            public void onResponse(Call<MatchlistDTO> call, Response<MatchlistDTO> response) {
                final MatchlistDTO matchList = response.body();

                if (matchList == null || matchList.matches.isEmpty()) {
                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Call appendResults to invoke the logic that shows the "no results" element
                            matchListAdapter.appendResults(beginIndex);
                            matchListAtEnd = true;
                            gettingMatches = false;
                        }
                    });

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
                                uiThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        matchListAdapter.appendResults(beginIndex);
                                        gettingMatches = false;
                                    }
                                });
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

    private static String formatMinSec(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / 3600);
        if (h == 0)
            return String.valueOf(m) + "m " + String.valueOf(s) + "s";
        return String.valueOf(h) + "h " + String.valueOf(m) + "m " + String.valueOf(s) + "s";
    }

    private static String formatKdaRatio(long killsPlusAssists, long deaths) {
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

    private static String simpleDouble(double n) {
        if (n == Math.floor(n))
            return String.valueOf((int)n);
        return String.format(Locale.US, "%.2f", n);
    }

    // Find greatest common divisor using simple Euclid's algorithm
    private static long gcd(long a, long b)
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
    private MatchFilterMenuItem currentFilter;

    public void setMatchFilter(final MatchFilterMenuItem filter) {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                currentFilter = filter;
                matchResults.clear();
                matchIds.clear();
                currentMatchIndex = 0;
                matchListAtEnd = false;
                matchListAdapter.reset();
                matchListAdapter.notifyDataSetChanged();
            }
        });
    }

    public static MatchFilterMenuItem[] getFilterMenuItems() {
        return new MatchFilterMenuItem[] {
                new MatchFilterMenuItem(R.id.match_filter_all, -1),
                new MatchFilterMenuItem(R.id.match_filter_ranked_solo, 420),
                new MatchFilterMenuItem(R.id.match_filter_ranked_flex, 440),
                new MatchFilterMenuItem(R.id.match_filter_normal, 400),
                new MatchFilterMenuItem(R.id.match_filter_aram, 450),
                new MatchFilterMenuItem(R.id.match_filter_event, 1010),
        };
    }

    public void initMatchFilter(MatchFilterMenuItem matchFilterMenuItem) {
        currentFilter = matchFilterMenuItem;
    }

    public MatchFilterMenuItem getCurrentFilter() {
        return currentFilter;
    }

    private class MatchListAdapter 
            extends RecyclerView.Adapter<MatchListItem> {
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
            notifyItemRangeRemoved(0, allMatches.size());
            allMatches.clear();
            matches = new MatchDTO[0];
        }

        public void appendResults(int beginIndex) {
            for (int i = beginIndex, e = matchIds.size(); i < e; ++i) {
                long id = matchIds.get(i);
                MatchDTO item = matchResults.get(id);

                if (item == null)
                    continue;

                // Find insertion point (binary search)
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

            spellIcons = new ImageView[]{
                    view.findViewById(R.id.spell0),
                    view.findViewById(R.id.spell1)
            };

            runeIcons = new ImageView[]{
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

            deferredSummoner.getData(new RiotAPI.AsyncCallback<SummonerDTO>() {
                @Override
                public void invoke(SummonerDTO summoner) {
                    // See if we're populating a recycled view too late
                    if ((int) view.getTag() != rowId)
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
                            new RiotAPI.AsyncCallback<Drawable>() {
                                @Override
                                public void invoke(final Drawable item) {
                                    // See if we're populating a recycled view too late
                                    if ((int) view.getTag() != rowId)
                                        return;

                                    uiThreadHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            // See if we're populating a recycled view too late
                                            if ((int) view.getTag() != rowId)
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
                                if ((int) view.getTag() != rowId)
                                    return;

                                uiThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // See if we're populating a recycled view too late
                                        if ((int) view.getTag() != rowId)
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
                                if ((int) view.getTag() != rowId)
                                    return;

                                uiThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // See if we're populating a recycled view too late
                                        if ((int) view.getTag() != rowId)
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
                            if ((int) view.getTag() != rowId)
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
    }

    public static class MatchFilterMenuItem {
        public final int id;
        public final long queueId;
        public TextView item;

        public MatchFilterMenuItem(int id, long queueId) {
            this.id = id;
            this.queueId = queueId;
        }
    }

}
