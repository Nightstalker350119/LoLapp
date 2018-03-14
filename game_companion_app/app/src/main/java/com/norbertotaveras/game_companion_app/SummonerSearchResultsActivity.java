package com.norbertotaveras.game_companion_app;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.norbertotaveras.game_companion_app.DTO.ChampionMastery.ChampionMasteryDTO;
import com.norbertotaveras.game_companion_app.DTO.League.LeaguePositionDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchReferenceDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchlistDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantIdentityDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class SummonerSearchResultsActivity
        extends AppCompatActivity
        implements View.OnClickListener {
    private final SummonerSearchResultsActivity activity = this;
    private RiotGamesService apiService;

    private String searchName;
    private long searchAccountId;

    private ImageView profileIcon;
    private ImageView tierIcon;
    private long profileIconId;

    private Handler uiThreadHandler;

    private CoordinatorLayout mainContent;
    private AppBarLayout appBarLayout;

    private TabLayout tabLayout;
    private ViewPager tabPager;
    private TabPagerAdapter tabPagerAdapter;
    private MatchesFragment matchesFragment;
    private ChampsFragment champsFragment;

    private TextView summonerName;
    private TextView rank;
    private TextView leaguePoints;
    private TextView winLoss;
    private TextView summonerSummary;

    private View[] seasonCards;
    private TextView[] seasonNums;
    private TextView[] seasonAchieved;

    private FabMenu filterMenu;
    private FabMenu sortMenu;
    private FabMenu.TabSwitcher menuSwitcher;

    private RiotAPI.DeferredRequest<SummonerDTO> deferredSummoner;

    private final LeagueInfo leagueInfo;

    public SummonerSearchResultsActivity() {
        leagueInfo = new LeagueInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summoner_search_results);

        View view = findViewById(android.R.id.content);

        mainContent = view.findViewById(R.id.main_content);
        appBarLayout = view.findViewById(R.id.appbar);

        filterMenu = new FabMenu(view, R.id.fab_filter_container);

        filterMenu.setActivateButton(new FabMenu.FabButtonShowMenu(filterMenu, R.id.fab_filter));

        filterMenu.setButtons(new FabMenu.FabButton[] {
                new FabButtonFilter(filterMenu, R.id.fab_filter_all, RiotAPI.QueueId.all),
                new FabButtonFilter(filterMenu, R.id.fab_filter_normal, RiotAPI.QueueId.normal),
                new FabButtonFilter(filterMenu, R.id.fab_filter_solo, RiotAPI.QueueId.rankedSolo),
                new FabButtonFilter(filterMenu, R.id.fab_filter_flex, RiotAPI.QueueId.rankedFlex),
                new FabButtonFilter(filterMenu, R.id.fab_filter_aram, RiotAPI.QueueId.aram),
                new FabButtonFilter(filterMenu, R.id.fab_filter_snow, RiotAPI.QueueId.snowUrf)
        });

        filterMenu.setSelectedIndex(0);

        sortMenu = new FabMenu(view, R.id.fab_sort_container);

        sortMenu.setActivateButton(new FabMenu.FabButtonShowMenu(sortMenu, R.id.fab_sort));

        sortMenu.setButtons(new FabMenu.FabButton[]{
                new FabButtonSort(sortMenu, R.id.fab_sort_name,
                        RiotAPI.ChampionMasteryComparators.byChampion),
                new FabButtonSort(sortMenu, R.id.fab_sort_points,
                        RiotAPI.ChampionMasteryComparators.byPoints),
                new FabButtonSort(sortMenu, R.id.fab_sort_level,
                        RiotAPI.ChampionMasteryComparators.byLevel)
        });

        sortMenu.setSelectedButton(null);

        Intent intent = getIntent();
        searchName = intent.getStringExtra("searchName");
        searchAccountId = intent.getLongExtra("searchAccountId", 0);

        tabLayout = findViewById(R.id.tabs);
        tabPager = findViewById(R.id.tab_pager);
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());

        // Set up automatic switching between FabMenu instances based on tab
        menuSwitcher = new FabMenu.TabSwitcher();

        matchesFragment = MatchesFragment.newInstance();
        champsFragment = ChampsFragment.newInstance();
        tabPagerAdapter.addFragment("Matches", matchesFragment);
        tabPagerAdapter.addFragment("Champs", champsFragment);
        menuSwitcher.addMenuToTab(filterMenu);
        menuSwitcher.addMenuToTab(sortMenu);
        menuSwitcher.setViewPager(tabPager);

        tabPager.setAdapter(tabPagerAdapter);
        tabLayout.setupWithViewPager(tabPager);

        rank = findViewById(R.id.rank_0);
        leaguePoints = findViewById(R.id.league_points_0);
        winLoss = findViewById(R.id.win_loss);

        summonerName = findViewById(R.id.summoner_name);
        summonerSummary = findViewById(R.id.summoner_summary);
        profileIcon = findViewById(R.id.profile_icon);
        tierIcon = findViewById(R.id.tier_icon_0);

        seasonCards = new View[] {
                findViewById(R.id.season_card_0),
                findViewById(R.id.season_card_1),
                findViewById(R.id.season_card_2)
        };

        seasonNums = new TextView[] {
                findViewById(R.id.season_num_0),
                findViewById(R.id.season_num_1),
                findViewById(R.id.season_num_2)
        };

        seasonAchieved = new TextView[] {
                findViewById(R.id.season_achieved_0),
                findViewById(R.id.season_achieved_1),
                findViewById(R.id.season_achieved_2)
        };

        for (TextView clr : seasonNums)
            clr.setText("");

        for (TextView clr : seasonAchieved)
            clr.setText("");

        for (View clr : seasonCards)
            clr.setVisibility(View.GONE);

        apiService = RiotAPI.getInstance(getApplicationContext());

        uiThreadHandler = UIHelper.createRunnableLooper();

        search();
    }

    private void search() {
        if (searchName != null) {
            deferredSummoner = new RiotAPI.DeferredRequest<>(
                    apiService.getSummonerByName(searchName));
        } else {
            deferredSummoner = new RiotAPI.DeferredRequest<>(
                    apiService.getSummonerByAccountId(searchAccountId));
        }

        matchesFragment.setDeferredSummoner(deferredSummoner);

        deferredSummoner.getData(new RiotAPI.AsyncCallback<SummonerDTO>() {
            @Override
            public void invoke(SummonerDTO summoner) {
                handleGetSummonerResponse(summoner);
                getRecentSeasonAchievements(summoner);
            }
        });
    }

    private void getRecentSeasonAchievements(final SummonerDTO summoner) {
        final int year = Calendar.getInstance().get(Calendar.YEAR);
        final int baseSeasonId = (year - 2014) * 2 + 3;

        // This could be increased to 3 loops, but the API service only goes back one season
        for (int i2 = 0; i2 < 1; ++i2) {
            final int i = i2;

            final int seasonId = baseSeasonId - i * 2;
            final int seasonNum = ((seasonId - 9) / 2) + 6;

            Log.v("PastTier", "Fetching seasonId=" + seasonId);

            final Call<MatchlistDTO> matchListRequest = apiService.getMatchListBySeasonId(
                    summoner.accountId, 0, 1, seasonId);

            RiotAPI.cachedRequest(matchListRequest, new RiotAPI.AsyncCallback<MatchlistDTO>() {
                @Override
                public void invoke(MatchlistDTO matchList) {
                    if (matchList == null || matchList.matches == null ||
                            matchList.matches.isEmpty())
                        return;

                    final MatchReferenceDTO firstMatch = matchList.matches.get(0);

                    final long firstMatchId = firstMatch.gameId;

                    RiotAPI.getCachedMatch(firstMatchId, new RiotAPI.AsyncCallback<MatchDTO>() {
                        @Override
                        public void invoke(MatchDTO match) {
                            Log.v("PastTier", "firstMatchId=" + firstMatchId);

                            // Find the participant identity for the summoner in this match
                            final ParticipantIdentityDTO identity =
                                    RiotAPI.participantIdentityFromSummoner(
                                            match.participantIdentities, summoner);
                            if (identity == null)
                                return;

                            final ParticipantDTO participant =
                                    RiotAPI.participantFromParticipantId(
                                            match.participants, identity.participantId);
                            if (participant == null)
                                return;

                            uiThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    seasonAchieved[i].setText(
                                            participant.highestAchievedSeasonTier);

                                    seasonNums[i].setText(getResources().getString(
                                            R.string.season_num, seasonNum));

                                    seasonCards[i].setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                }
            });
        }
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

        champsFragment.setSummoner(summoner);

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

        matchesFragment.getMatchList(summoner);
    }

    @Override
    public void onBackPressed() {
        if (!menuSwitcher.onBackPressed())
            super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

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

    private class TabPagerAdapter extends FragmentPagerAdapter {
        final ArrayList<TabInfo> tabs;

        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
            tabs = new ArrayList<>();
        }

        public void addFragment(String title, Fragment fragment) {
            tabs.add(new TabInfo(title, fragment));
        }

        @Override
        public Fragment getItem(int position) {
            return tabs.get(position).fragment;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabs.get(position).title;
        }

        @Override
        public int getCount() {
            return tabs.size();
        }

        private class TabInfo {
            String title;
            Fragment fragment;

            TabInfo(String title, Fragment fragment) {
                this.title = title;
                this.fragment = fragment;
            }
        }
    }

    private class FabButtonSort extends FabMenu.FabButton {
        public final Comparator<ChampionMasteryDTO> comparator;

        public FabButtonSort(FabMenu owner, int id, Comparator<ChampionMasteryDTO> comparator) {
            super(owner, id);
            this.comparator = comparator;
        }

        @Override
        public boolean onClick() {
            Log.v("FabMenu", "Clicked champs sort option");
            champsFragment.setSortOrder(comparator);
            return false;
        }
    }

    private class FabButtonFilter extends FabMenu.FabButton {
        public final RiotAPI.QueueId queueId;

        public FabButtonFilter(FabMenu owner, int id, RiotAPI.QueueId queueId) {
            super(owner, id);
            this.queueId = queueId;
        }

        @Override
        public boolean onClick() {
            matchesFragment.setMatchFilter(queueId);
            Log.v("FabMenu", "Clicked queueId filter option");
            return false;
        }
    }
}

