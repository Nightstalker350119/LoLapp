package com.norbertotaveras.game_companion_app;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.norbertotaveras.game_companion_app.DTO.League.LeaguePositionDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantIdentityDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class SummonerSearchResultsActivity
        extends AppCompatActivity
{
    private RiotGamesService apiService;

    private String searchName;
    private long searchAccountId;

    private ImageView profileIcon;
    private ImageView tierIcon;
    private long profileIconId;

    private Handler uiThreadHandler;

    private Point displaySize;
    //private ScrollView scrollParent;

    private TabLayout tabLayout;
    private ViewPager tabPager;
    private TabPagerAdapter tabPagerAdapter;
    private MatchesFragment matchesFragment;
    private ChampsFragment champsFragment;

    private TextView summonerName;
    private TextView rank;
    private TextView leaguePoints;
    private TextView winLoss;
    private TextView queueText;
    private TextView summonerSummary;

    //private LeagueCollectionFragmentAdapter leaguePagerAdapter;
    //private ViewPager leaguePager;

    private RiotAPI.DeferredRequest<SummonerDTO> deferredSummoner;

    private final LeagueInfo leagueInfo;

    private final MatchesFragment.MatchFilterMenuItem[] matchFilterMenuItems =
            MatchesFragment.getFilterMenuItems();

    private MaterialSheetFab<SummonerSearchFilterFab> matchFilterSheet;

    private boolean initializing;

    public SummonerSearchResultsActivity() {
        leagueInfo = new LeagueInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summoner_search_results);

        Intent intent = getIntent();
        searchName = intent.getStringExtra("searchName");
        searchAccountId = intent.getLongExtra("searchAccountId", 0);

        displaySize = getDisplaySize();
//        scrollParent = findViewById(R.id.nested_scroll_parent);
//        scrollParent.setNestedScrollingEnabled(true);

        tabLayout = findViewById(R.id.tabs);
        tabPager = findViewById(R.id.tab_pager);

        tabPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());

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
        queueText = findViewById(R.id.queue_name);

        initializing = true;

        apiService = RiotAPI.getInstance(getApplicationContext());

        uiThreadHandler = UIHelper.createRunnableLooper();

        initMatchFilterMenu();

        search();
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

        matchesFragment.setDeferredSummoner(deferredSummoner);

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

        matchesFragment.getMatchList(summoner);
    }

    private void initMatchFilterMenu() {
        SummonerSearchFilterFab fab = findViewById(R.id.fab);
        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.dim_overlay);
        final int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
        final int sheetColor = colorPrimary;
        final int fabColor = colorPrimary;

        // Initialize material sheet FAB
        matchFilterSheet = new MaterialSheetFab<>(fab, sheetView, overlay,
                sheetColor, fabColor);

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

    @Override
    public void onBackPressed() {
        // Close the match filter menu on back press if it is visible
        // otherwise, do default back behavior
        if (matchFilterSheet.isSheetVisible()) {
            matchFilterSheet.hideSheet();
        } else {
            super.onBackPressed();
        }
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
