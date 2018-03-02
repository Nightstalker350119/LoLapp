package com.norbertotaveras.game_companion_app;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gordonwong.materialsheetfab.AnimatedFab;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
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
        implements TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {
    private RiotGamesService apiService;

    private String searchName;
    private long searchAccountId;

    private ImageView profileIcon;
    private ImageView tierIcon;
    private long profileIconId;

    private Handler uiThreadHandler;

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

    private AnimatedFab summonerFab;
    private AnimatedFab champsFab;

    //FAB Button stuff

//    FloatingActionButton fabPlus, fabSolo, fabName, fabFlex, fabPoints, fabNormal, fabLevel, fabARAM, fabEvent, fabFilter;
//    Animation FabOpen, FabClose, FabRotateClockWise, FabRotateCounterClockWise;
//    int wantedPosition = 0; //0=all | 1=Solo | 2=Flex | 3=Normals | 4=ARAM | 5=event | 6=Name | 7=Points | 8=Level
//    boolean isOpen = false;

    //FAB Button stuff

    private RiotAPI.DeferredRequest<SummonerDTO> deferredSummoner;

    private final LeagueInfo leagueInfo;

    private final MatchesFragment.MatchFilterMenuItem[] matchFilterMenuItems =
            MatchesFragment.getFilterMenuItems();
    private final ChampsFragment.ChampSortMenuItem[] champSortMenuItems =
            ChampsFragment.getSortMenuItems();

    private MaterialSheetFab<AnimatedMenuFab> matchFilterSheet;
    private MaterialSheetFab<AnimatedMenuFab> champSortSheet;

    public SummonerSearchResultsActivity() {
        leagueInfo = new LeagueInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summoner_search_results);


        //Button Functionality below
//        //Button functionality() {
//        fabPlus = findViewById(R.id.summoner_fab); //MainFAB
//        fabSolo = findViewById(R.id.match_filter_ranked_solo); //Sort by SoloQ
//        fabFlex = findViewById(R.id.match_filter_ranked_flex); //Sort by FlexQ
//        fabNormal = findViewById(R.id.match_filter_normal); //Sort by Normals
//        fabARAM = findViewById(R.id.match_filter_aram); //Sort by ARAM
//        fabEvent = findViewById(R.id.match_filter_event); //Sort by event
//        fabFilter = findViewById(R.id.match_filter_all); //Sort, filter back to all
//        fabName = findViewById(R.id.champ_sort_by_champion); //Sort by champion name
//        FabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open); //OpenAnimationOnFAB
//        FabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close); //CloseAnimationOnFAB
//        FabRotateClockWise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise); //FABRotate
//        FabRotateCounterClockWise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_counterclockwise); //FABRotateCCWise
//
//
//        //Button Events start here
//        fabPlus.setOnClickListener(new View.OnClickListener() { //Sorting buttons
//            @Override
//            public void onClick(View v) {
//                fabOpenClose();
//            }
//        });
//
//        fabFilter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fabOpenClose();
//
//                wantedPosition = 0;
//                Toast.makeText(SummonerSearchResultsActivity.this, "All roles", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        fabSolo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                fabOpenClose();
//
//                wantedPosition = 1;
//                Toast.makeText(SummonerSearchResultsActivity.this, "All roles", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        fabFlex.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                fabOpenClose();
//
//                wantedPosition = 2;
//                Toast.makeText(SummonerSearchResultsActivity.this, "All roles", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        fabNormal.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                fabOpenClose();
//
//                wantedPosition = 3;
//                Toast.makeText(SummonerSearchResultsActivity.this, "All roles", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        fabARAM.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                fabOpenClose();
//
//                wantedPosition = 4;
//                Toast.makeText(SummonerSearchResultsActivity.this, "All roles", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        fabEvent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                fabOpenClose();
//
//                wantedPosition = 5;
//                Toast.makeText(SummonerSearchResultsActivity.this, "All roles", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        fabName.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                fabOpenClose();
//
//                wantedPosition = 6;
//                Toast.makeText(SummonerSearchResultsActivity.this, "All roles", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        fabPoints.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                fabOpenClose();
//
//                wantedPosition = 7;
//                Toast.makeText(SummonerSearchResultsActivity.this, "All roles", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        fabLevel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v)
//            {
//                fabOpenClose();
//
//                wantedPosition = 8;
//                Toast.makeText(SummonerSearchResultsActivity.this, "All roles", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//
//

        Intent intent = getIntent();
        searchName = intent.getStringExtra("searchName");
        searchAccountId = intent.getLongExtra("searchAccountId", 0);

        tabLayout = findViewById(R.id.tabs);
        tabPager = findViewById(R.id.tab_pager);
        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
        tabPager.addOnPageChangeListener(this);

        matchesFragment = MatchesFragment.newInstance();
        champsFragment = ChampsFragment.newInstance();
        tabPagerAdapter.addFragment("Matches", matchesFragment);
        tabPagerAdapter.addFragment("Champs", champsFragment);

        tabPager.setAdapter(tabPagerAdapter);
        tabLayout.setupWithViewPager(tabPager);

        rank = findViewById(R.id.rank_0);
        leaguePoints = findViewById(R.id.league_points_0);
        winLoss = findViewById(R.id.win_loss);

        summonerName = findViewById(R.id.summoner_name);
        summonerSummary = findViewById(R.id.summoner_summary);
        profileIcon = findViewById(R.id.profile_icon);
        tierIcon = findViewById(R.id.tier_icon_0);

        summonerFab = findViewById(R.id.summoner_fab);
        champsFab = findViewById(R.id.champs_fab);

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

        tabLayout.addOnTabSelectedListener(this);

        apiService = RiotAPI.getInstance(getApplicationContext());

        uiThreadHandler = UIHelper.createRunnableLooper();

        initMatchFilterMenu();
        initChampSortMenu();

        enableFab(R.id.summoner_fab);

        search();
    }

    //Button Stuff Below
    // BUTTON STUFF! LOGAN
//    public void fabOpenClose()
//    {
//        if(isOpen)
//        {
//            fabSolo.startAnimation(FabClose);
//            fabName.startAnimation(FabClose);
//            fabFlex.startAnimation(FabClose);
//            fabPoints.startAnimation(FabClose);
//            fabNormal.startAnimation(FabClose);
//            fabLevel.startAnimation(FabClose);
//            fabARAM.startAnimation(FabClose);
//            fabEvent.startAnimation(FabClose);
//            fabFilter.startAnimation(FabClose);
//            fabPlus.startAnimation(FabRotateCounterClockWise);
//            fabSolo.setClickable(false);
//            fabName.setClickable(false);
//            fabFlex.setClickable(false);
//            fabPoints.setClickable(false);
//            fabNormal.setClickable(false);
//            fabLevel.setClickable(false);
//            fabARAM.setClickable(false);
//            fabEvent.setClickable(false);
//            fabFilter.setClickable(false);
//            isOpen = false;
//        }
//
//        else
//        {
//            //Animate buttons coming out of our FAB and get them working
//            fabSolo.startAnimation(FabOpen);
//            fabName.startAnimation(FabOpen);
//            fabFlex.startAnimation(FabOpen);
//            fabPoints.startAnimation(FabOpen);
//            fabNormal.startAnimation(FabOpen);
//            fabLevel.startAnimation(FabOpen);
//            fabARAM.startAnimation(FabOpen);
//            fabEvent.startAnimation(FabOpen);
//            fabFilter.startAnimation(FabOpen);
//            fabPlus.startAnimation(FabRotateClockWise);
//            fabSolo.setClickable(true);
//            fabName.setClickable(true);
//            fabFlex.setClickable(true);
//            fabPoints.setClickable(true);
//            fabNormal.setClickable(true);
//            fabLevel.setClickable(true);
//            fabARAM.setClickable(true);
//            fabEvent.setClickable(true);
//            fabFilter.setClickable(true);
//            isOpen = true;
//        }
//    }

    public void buttonAnimation(final Button button) { // Timing and animation effects
        Animation btn = new AlphaAnimation(1.00f, 0.00f);
        btn.setDuration(3000);
        btn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                button.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                button.setVisibility(View.INVISIBLE);
                button.setEnabled(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        button.startAnimation(btn);
    }

    public void buttonEventHandler(int wantedPosition)
    {
        switch (wantedPosition) {
            case 0: //Filter
                break;
            case 1: //Solo Results
                break;
            case 2: //Flex Results
                break;
            case 3: //Normal Results
                break;
            case 4: //ARAM Results
                break;
            case 5: //Event Results
                break;
            case 6: //Name Results
                break;
            case 7: //Points Results
                break;
            case 8: //Level Results
                break;
        }
    }

    // End of Button Stuff

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

    private MaterialSheetFab<AnimatedMenuFab> initFabMenu(int fabId, int sheetId) {
        AnimatedMenuFab fab = findViewById(fabId);
        View sheetView = findViewById(sheetId);
        View overlay = findViewById(R.id.dim_overlay);
        final int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
        final int sheetColor = colorPrimary;
        final int fabColor = colorPrimary;

        // Initialize material sheet FAB
        MaterialSheetFab<AnimatedMenuFab> sheet = new MaterialSheetFab<>(
                fab, sheetView, overlay, sheetColor, fabColor);

        return sheet;
    }

    private void enableFab(int id) {
        summonerFab.hide();
        champsFab.hide();

        switch (id) {
            case R.id.summoner_fab:
                summonerFab.show();
                break;

            case R.id.champs_fab:
                champsFab.show();
                break;
        }
    }

    private void initMatchFilterMenu() {
        matchFilterSheet = initFabMenu(R.id.summoner_fab, R.id.summoner_filter_sheet);

        for (MatchesFragment.MatchFilterMenuItem item : matchFilterMenuItems) {
            item.item = findViewById(item.id);
            item.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (MatchesFragment.MatchFilterMenuItem menuItem : matchFilterMenuItems) {
                        if (menuItem.item == view) {
                            setMatchFilter(menuItem);
                            break;
                        }
                    }
                }
            });
        }
        matchesFragment.initMatchFilter(matchFilterMenuItems[0]);
        updateMatchFilterMenu();
    }

    private void initChampSortMenu() {
        champSortSheet = initFabMenu(R.id.champs_fab, R.id.champ_sort_sheet);

        for (ChampsFragment.ChampSortMenuItem item : champSortMenuItems) {
            item.item = findViewById(item.id);
            item.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (ChampsFragment.ChampSortMenuItem menuItem : champSortMenuItems) {
                        if (menuItem.item == view) {
                            setChampSort(menuItem);
                            break;
                        }
                    }
                }
            });
        }
    }

    void setMatchFilter(final MatchesFragment.MatchFilterMenuItem filter) {
        matchesFragment.setMatchFilter(filter);
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                updateMatchFilterMenu();
                matchFilterSheet.hideSheet();
                matchesFragment.getMoreMatches();
            }
        });
    }

    private void setChampSort(ChampsFragment.ChampSortMenuItem menuItem) {
        champsFragment.setSortOrder(menuItem);
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                updateChampSortMenu();
                champSortSheet.hideSheet();
            }
        });
    }

    private void updateMatchFilterMenu() {
        MatchesFragment.MatchFilterMenuItem currentFilter = matchesFragment.getCurrentFilter();
        for (MatchesFragment.MatchFilterMenuItem item : matchFilterMenuItems) {
            if (item != currentFilter) {
                item.item.setBackgroundColor(ContextCompat.getColor(
                        this, R.color.colorPrimary));
            } else {
                item.item.setBackgroundColor(ContextCompat.getColor(
                        this, R.color.highlight));
            }
        }
    }

    private void updateChampSortMenu() {
        ChampsFragment.ChampSortMenuItem currentSort = champsFragment.getCurrentSort();
        for (ChampsFragment.ChampSortMenuItem item : champSortMenuItems) {
            if (item != currentSort) {
                item.item.setBackgroundColor(ContextCompat.getColor(
                        this, R.color.colorPrimary));
            } else {
                item.item.setBackgroundColor(ContextCompat.getColor(
                        this, R.color.highlight));
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Close the match filter menu on back press if it is visible
        // otherwise, do default back behavior
        if (matchFilterSheet.isSheetVisible())
            matchFilterSheet.hideSheet();
        else if (champSortSheet.isSheetVisible())
            champSortSheet.hideSheet();
        else
            super.onBackPressed();
    }

    // Enables the FloatingActionButton between tabs (Matches and Fragment)
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int pos = tab.getPosition();
        Fragment fragment = tabPagerAdapter.getItem(pos);

        if (fragment == matchesFragment) {
            enableFab(R.id.summoner_fab);
        } else if (fragment == champsFragment) {
            enableFab(R.id.champs_fab);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (champSortSheet.isSheetVisible())
            champSortSheet.hideSheet();
        if (matchFilterSheet.isSheetVisible())
            matchFilterSheet.hideSheet();
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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
}
