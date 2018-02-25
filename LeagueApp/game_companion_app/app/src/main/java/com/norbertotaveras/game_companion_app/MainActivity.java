package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.norbertotaveras.game_companion_app.ChampionPage.ChampionsActivity;
import com.norbertotaveras.game_companion_app.ChampionPage.MainChampions;
import com.norbertotaveras.game_companion_app.DTO.League.LeaguePositionDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class MainActivity
        extends AppCompatActivity
        implements TextView.OnEditorActionListener, OnRecentSearchClickListener
{
    FirebaseAuth auth;
    Menu optionsMenu;

    private EditText search;

    private RecyclerView recentSearches;
    private ItemTouchHelper recentSearchesItemTouchHelper;
    private LinearLayoutManager recentSearchesLayoutManager;
    private RecentSearchesAdapter recentSearchesAdapter;

    RiotGamesService apiService;
    Handler uiThreadHandler;

    private final BottomNavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    showHomeResult();
                    return true;
                case R.id.navigation_dashboard:
                    showChampionResult();
                    return true;
                case R.id.navigation_notifications:
                    return true;
            }
            return false;
        }
    };
    private View noResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Codex");

        auth = FirebaseAuth.getInstance();

        search = findViewById(R.id.search);
        recentSearches = findViewById(R.id.recent_searches);

        recentSearchesItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView,
                                        RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(0, ItemTouchHelper.LEFT);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                RecentSearchItem holder = (RecentSearchItem)viewHolder;
                if (direction == ItemTouchHelper.LEFT)
                    holder.triggerDelete();
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }
        });

        recentSearchesAdapter = new RecentSearchesAdapter(this);
        recentSearchesAdapter.setOnRecentSearchClickListener(this);
        recentSearchesLayoutManager = new LinearLayoutManager(this);
        recentSearchesItemTouchHelper.attachToRecyclerView(recentSearches);

        recentSearches.setLayoutManager(recentSearchesLayoutManager);
        recentSearches.setHasFixedSize(true);
        recentSearches.setAdapter(recentSearchesAdapter);

        search.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
        search.setOnEditorActionListener(this);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        uiThreadHandler = UIHelper.createRunnableLooper();

        // Start fetching icon data and stuff while the user types their search
        apiService = RiotAPI.getInstance(getApplicationContext());

        if (auth.getCurrentUser() == null)
            showSignIn();

        updateNoResults();
    }

    private void showSignIn() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateOptionsMenu();

        recentSearchesAdapter.refresh(this);

        updateNoResults();
    }

    void updateNoResults() {
        noResults = findViewById(R.id.no_results);
        boolean anyResults = recentSearchesAdapter.getItemCount() > 0;
        noResults.setVisibility(anyResults ? View.GONE : View.VISIBLE);
        recentSearches.setVisibility(anyResults ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auth_menu, menu);
        optionsMenu = menu;

        updateOptionsMenu();

        return true;
    }

    private void updateOptionsMenu() {
        if (auth == null || optionsMenu == null)
            return;

        boolean signedIn = (auth.getCurrentUser() != null);

        MenuItem signInItem = optionsMenu.findItem(R.id.sign_in);
        signInItem.setEnabled(!signedIn);

        MenuItem signOutItem = optionsMenu.findItem(R.id.sign_out);
        signOutItem.setEnabled(signedIn);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_in:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                return true;

            case R.id.sign_out:
                auth.signOut();
                updateOptionsMenu();
                return true;
        }

        return false;
    }

    private String handleEditorAction(TextView view, int actionId, KeyEvent event) {
        String value;
        switch (actionId) {
            case IME_ACTION_SEARCH:
            case IME_ACTION_DONE:
                value = view.getText().toString();
                view.setText("");
                return value;

            default:
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                        event.getAction() == KeyEvent.ACTION_DOWN) {
                    value = view.getText().toString();
                    view.setText("");
                    return value;
                }

                break;
        }

        return null;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.search:
                String summonerName = handleEditorAction(v, actionId, event);
                if (summonerName != null)
                    showSearchResult(summonerName);
        }
        return false;
    }

    private void showSearchResult(String summonerName) {
        // Remove leading and trailing spaces and refuse to search for empty name
        summonerName = summonerName.trim();
        if (summonerName.length() == 0)
            return;

        Intent resultsIntent;
        resultsIntent = new Intent(this, SummonerSearchResultsActivity.class);
        resultsIntent.putExtra("searchName", summonerName);
        startActivity(resultsIntent);
    }

    private void showSearchResult(Long accountId) {
        // Remove leading and trailing spaces and refuse to search for empty name
        Intent resultsIntent;
        resultsIntent = new Intent(this, SummonerSearchResultsActivity.class);
        resultsIntent.putExtra("searchAccountId", accountId);
        startActivity(resultsIntent);
    }

    private void showChampionResult()
    {
        Intent championIntent;
        championIntent = new Intent(this, MainChampions.class);
        startActivity(championIntent);
    }

    private void showHomeResult()
    {
        Intent homeIntent;
        homeIntent = new Intent(this, MainActivity.class);
        startActivity(homeIntent);
    }

    @Override
    public void onRemoveClick(Long accountId) {
        RecentSearchStorage.remove(this, accountId);
        updateNoResults();
    }

    @Override
    public void onOpenClick(Long accountId) {
        showSearchResult(accountId);
    }

    private class RecentSearchItem extends RecyclerView.ViewHolder {
        final RecentSearchesAdapter owner;
        final View view;
        final ImageView profileIcon;
        final TextView summonerName;
        final TextView summonerLevel;
        final ImageView tierIcon;
        final TextView rank;
        final TextView leaguePoints;
        final TextView winLoss;

        private AtomicInteger uniqueId;
        private Long accountId;

        public RecentSearchItem(RecentSearchesAdapter owner, View view) {
            super(view);
            this.owner = owner;
            this.view = view;

            uniqueId = new AtomicInteger(0);

            profileIcon = view.findViewById(R.id.profile_icon);
            summonerName = view.findViewById(R.id.summoner_name);
            summonerLevel = view.findViewById(R.id.level);
            tierIcon = view.findViewById(R.id.tier_icon_0);
            rank = view.findViewById(R.id.rank_0);
            leaguePoints = view.findViewById(R.id.league_points_0);
            winLoss = view.findViewById(R.id.win_loss);
        }

        void bind(final Long accountId) {
            final int rowId = uniqueId.getAndIncrement();
            view.setTag(rowId);

            this.accountId = accountId;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    owner.onOpenClick(accountId);
                }
            });

            final Call<SummonerDTO> summonerRequest = apiService.getSummonerByAccountId(accountId);

            // Avoid flash of old content
            summonerName.setText("");
            summonerLevel.setText("");
            profileIcon.setImageDrawable(null);
            tierIcon.setImageResource(android.R.color.transparent);
            rank.setText("");
            leaguePoints.setText("");
            winLoss.setText("");

            RiotAPI.cachedRequest(summonerRequest, new RiotAPI.AsyncCallback<SummonerDTO>() {
                @Override
                public void invoke(final SummonerDTO summoner) {
                    // See if we're populating a recycled view too late
                    if ((int)view.getTag() != rowId)
                        return;

                    if (summoner == null) {
                        Log.e("RecentSearch",
                                "Summoner " + String.valueOf(accountId) + " not found");
                        return;
                    }

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // See if we're populating a recycled view too late
                            if ((int)view.getTag() != rowId)
                                return;

                            summonerName.setText(summoner.name);
                            summonerLevel.setText("Level " + String.valueOf(summoner.summonerLevel));
                        }
                    });

                    RiotAPI.fetchProfileIcon(summoner.profileIconId,
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

                                    profileIcon.setImageDrawable(item);
                                }
                            });
                        }
                    });

                    Call<List<LeaguePositionDTO>> leaguePositionRequest =
                            apiService.getLeaguePositionsBySummonerId(summoner.id);

                    RiotAPI.cachedRequest(leaguePositionRequest,
                            new RiotAPI.AsyncCallback<List<LeaguePositionDTO>>()
                    {
                        @Override
                        public void invoke(List<LeaguePositionDTO> positions) {
                            // See if we're populating a recycled view too late
                            if ((int)view.getTag() != rowId)
                                return;

                            LeaguePositionDTO rs5v5 = null;

                            // Java array doesn't have indexOf? Come on!
                            for (LeaguePositionDTO lp : positions) {
                                if (lp.queueType.equals("RANKED_SOLO_5x5")) {
                                    rs5v5 = lp;
                                    break;
                                }
                            }

                            if (rs5v5 == null)
                                return;

                            tierIcon.setImageResource(RiotAPI.tierNameToResourceId(
                                    rs5v5 != null ? rs5v5.tier : "PROVISIONAL", rs5v5.rank));

                            rank.setText(rs5v5 != null
                                    ? RiotAPI.beautifyTierName(rs5v5.tier) +
                                    " " + rs5v5.rank
                                    : "Unranked");

                            leaguePoints.setText(rs5v5 != null
                                    ? String.valueOf(rs5v5.leaguePoints) +
                                    " LP" : "- LP");

                            int winPercent = 100 * rs5v5.wins / (rs5v5.wins + rs5v5.losses);
                            winLoss.setText(getResources().getString(R.string.win_loss,
                                    rs5v5.wins, rs5v5.losses, winPercent));
                        }
                    });
                }
            });
        }

        public void triggerDelete() {
            owner.onRemoveClick(accountId);
        }
    }

    class RecentSearchesAdapter
            extends RecyclerView.Adapter<RecentSearchItem>
        implements OnRecentSearchClickListener
    {
        private OnRecentSearchClickListener listener;
        private List<Long> searchHistory;
        private final LayoutInflater inflater;
        AtomicInteger uniqueId;

        public RecentSearchesAdapter(Context context) {
            searchHistory = RecentSearchStorage.load(context);

            reverseList(searchHistory);

            inflater = getLayoutInflater();
            uniqueId = new AtomicInteger(0);
        }

        private <T> void reverseList(List<T> list) {
            int st = 0, en = list.size() - 1;
            while (st < en) {
                T a = list.get(st);
                T b = list.get(en);
                list.set(en, a);
                list.set(st, b);
                ++st;
                --en;
            }
        }

        public void setOnRecentSearchClickListener(OnRecentSearchClickListener listener) {
            this.listener = listener;
        }

        @Override
        public RecentSearchItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View holder;
            holder = inflater.inflate(R.layout.fragment_recent_search, parent, false);
            return new RecentSearchItem(this, holder);
        }

        @Override
        public void onBindViewHolder(RecentSearchItem holder, int position) {
            holder.bind(searchHistory.get(position));
        }

        @Override
        public int getItemCount() {
            return searchHistory.size();
        }

        public void refresh(Context context) {
            searchHistory = RecentSearchStorage.load(context);
            reverseList(searchHistory);
            notifyDataSetChanged();
        }

        @Override
        public void onRemoveClick(Long accountId) {
            int position = searchHistory.indexOf(accountId);
            searchHistory.remove(position);
            listener.onRemoveClick(accountId);
            notifyItemRemoved(position);
        }

        @Override
        public void onOpenClick(Long accountId) {
            listener.onOpenClick(accountId);
        }
    }
}

interface OnRecentSearchClickListener {
    void onRemoveClick(Long accountId);
    void onOpenClick(Long accountId);
}
