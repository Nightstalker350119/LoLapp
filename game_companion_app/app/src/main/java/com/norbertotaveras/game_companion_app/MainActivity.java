package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.norbertotaveras.game_companion_app.DTO.League.LeaguePositionDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class MainActivity
        extends AppCompatActivity
        implements TextView.OnEditorActionListener, OnRecentSearchClickListener,
        AdapterView.OnItemClickListener {
    SummonerDTO summoner;

    FirebaseAuth auth;
    Menu optionsMenu;

    private TextView title1;
    private TextView title2;
    private EditText search;

    private ListView recentSearches;
    private RecentSearchesAdapter recentSearchesAdapter;

    RiotGamesService apiService;
    Handler uiThreadHandler;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Home");

        auth = FirebaseAuth.getInstance();

        title1 = findViewById(R.id.title1);
        title2 = findViewById(R.id.title2);
        search = findViewById(R.id.search);
        recentSearches = findViewById(R.id.recent_searches);

        recentSearchesAdapter = new RecentSearchesAdapter(this);
        recentSearchesAdapter.setOnRecentSearchClickListener(this);
        recentSearches.setAdapter(recentSearchesAdapter);
        recentSearches.setOnItemClickListener(this);

        search.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
        search.setOnEditorActionListener(this);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        uiThreadHandler = UIHelper.createRunnableLooper();

        // Start fetching icon data and stuff while the user types their search
        apiService = RiotAPI.getInstance(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        updateOptionsMenu();

        recentSearchesAdapter.refresh(this);
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
        switch (actionId) {
            case IME_ACTION_SEARCH:
            case IME_ACTION_DONE:
                return view.getText().toString();

            default:
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                        event.getAction() == KeyEvent.ACTION_DOWN) {
                    return view.getText().toString();
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

    private void showSearchResult(Long accountId) {
        // Refuse to search for obviously invalid account ID
        if (accountId <= 0)
            return;

        Intent resultsIntent;
        resultsIntent = new Intent(this, SummonerSearchResultsActivity.class);
        resultsIntent.putExtra("searchAccountId", accountId);
        startActivity(resultsIntent);
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

    String getSummonerName() {
        return search.getText().toString().trim();
    }

    public void OnRemoveClick(Long summonerId) {
        RecentSearchStorage.remove(this, summonerId);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long summonerId) {
        if (adapterView == recentSearches)
            showSearchResult(summonerId);
    }

    class RecentSearchesAdapter
            extends BaseAdapter {
        private OnRecentSearchClickListener listener;
        private List<Long> searchHistory;
        private final LayoutInflater inflater;
        AtomicInteger uniqueId;
        View addSummonerView;

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
        public int getCount() {
            return 1 + (searchHistory != null ? searchHistory.size() : 0);
        }

        @Override
        public Object getItem(int i) {
            return searchHistory != null && i < searchHistory.size() ? searchHistory.get(i) : null;
        }

        @Override
        public long getItemId(int i) {
            return searchHistory != null && i < searchHistory.size() ? searchHistory.get(i) : -1;
        }

        @Override
        public View getView(final int position, View recycledView, ViewGroup viewGroup) {
            if (position == searchHistory.size())
                return getAddSummonerView(recycledView, viewGroup);

            if (recycledView == null || (int)recycledView.getTag() == -1) {
                recycledView = inflater.inflate(R.layout.fragment_recent_search,
                        viewGroup, false);
            }

            final View view = recycledView;

            final int rowId = uniqueId.getAndIncrement();
            view.setTag(rowId);

            final ImageView profileIcon = view.findViewById(R.id.profile_icon);
            final TextView summonerName = view.findViewById(R.id.summoner_name);
            final TextView summonerLevel = view.findViewById(R.id.level);

            final LinearLayout[] queueContainers = new LinearLayout[]{
                    view.findViewById(R.id.queue_summary_0),
                    view.findViewById(R.id.queue_summary_1),
                    view.findViewById(R.id.queue_summary_2)
            };

            final TextView[] queueNames = new TextView[]{
                    view.findViewById(R.id.queue_name_0),
                    view.findViewById(R.id.queue_name_1),
                    view.findViewById(R.id.queue_name_2)
            };

            final ImageView[] tierIcons = new ImageView[]{
                    view.findViewById(R.id.tier_icon_0),
                    view.findViewById(R.id.tier_icon_1),
                    view.findViewById(R.id.tier_icon_2)
            };

            final TextView[] ranks = new TextView[]{
                    view.findViewById(R.id.rank_0),
                    view.findViewById(R.id.rank_1),
                    view.findViewById(R.id.rank_2)
            };

            final TextView[] leaguePoints = new TextView[]{
                    view.findViewById(R.id.league_points_0),
                    view.findViewById(R.id.league_points_1),
                    view.findViewById(R.id.league_points_2)
            };

            final String[] queueTypes = new String[]{
                    "RANKED_SOLO_5x5",
                    "RANKED_FLEX_5x5",
                    "RANKED_FLEX_3x3"
            };

            final TextView remove = view.findViewById(R.id.remove);

            // Display the more recent item closer to the top.
            // The most recent items are at the end of the history list, so reverse the order.
            final Long accountId = searchHistory.get(position);

            final Call<SummonerDTO> summonerRequest = apiService.getSummonerByAccountId(accountId);

            // Avoid flash of old content
            summonerName.setText("");
            summonerLevel.setText("");
            profileIcon.setImageDrawable(null);
            for (int i = 0; i < 3; ++i) {
                queueNames[i].setText("");
                tierIcons[i].setImageResource(android.R.color.transparent);
                ranks[i].setText("");
                leaguePoints[i].setText("");
            }

            RiotAPI.cachedRequest(summonerRequest, new RiotAPI.AsyncCallback<SummonerDTO>() {
                @Override
                public void invoke(final SummonerDTO summoner) {
                    // See if we're populating a recycled view too late
                    if ((int)view.getTag() != rowId)
                        return;

                    remove.setVisibility(View.INVISIBLE);

                    if (summoner == null) {
                        Log.e("RecentSearch",
                                "Summoner " + String.valueOf(accountId) + " not found");
                        return;
                    }

                    remove.setVisibility(View.VISIBLE);

                    remove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                        UIHelper.confirm(view.getContext(),
                                "Remove " + summoner.name + "?",
                                "Remove", "Cancel",
                                new UIHelper.ConfirmCallback() {
                            @Override
                            public void onChoice(boolean choice) {
                                if (choice) {
                                    listener.OnRemoveClick(accountId);
                                    searchHistory.remove(accountId);
                                    notifyDataSetChanged();
                                }
                            }
                        });
                        }
                    });

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // See if we're populating a recycled view too late
                            if ((int)view.getTag() != rowId)
                                return;

                            summonerName.setText(summoner.name);
                            summonerLevel.setText("Lvl " + String.valueOf(summoner.summonerLevel));
                        }
                    });

                    RiotAPI.fetchProfileIcon(summoner.profileIconId,
                            new RiotAPI.AsyncCallback<Drawable>() {
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
                            new RiotAPI.AsyncCallback<List<LeaguePositionDTO>>() {
                        @Override
                        public void invoke(List<LeaguePositionDTO> positions) {
                            // See if we're populating a recycled view too late
                            if ((int)view.getTag() != rowId)
                                return;

                            LeaguePositionDTO[] lookup = new LeaguePositionDTO[3];

                            // Java array doesn't have indexOf? Come on!
                            for (LeaguePositionDTO lp : positions) {
                                for (int i = 0; i < queueTypes.length; ++i) {
                                    if (queueTypes[i].equals(lp.queueType)) {
                                        lookup[i] = lp;
                                        break;
                                    }
                                }
                            }

                            for (int i = 0; i < 3; ++i) {
                                LeaguePositionDTO item = lookup[i];

                                String queueText = RiotAPI.beautifyQueueName(queueTypes[i]);
                                queueNames[i].setText(queueText);

                                tierIcons[i].setImageResource(RiotAPI.tierNameToResourceId(
                                        lookup[i] != null ? item.tier : "PROVISIONAL"));

                                ranks[i].setText(lookup[i] != null
                                        ? RiotAPI.beautifyTierName(item.tier) + " " + item.rank
                                        : "Unranked");

                                leaguePoints[i].setText(lookup[i] != null
                                        ? String.valueOf(item.leaguePoints) +
                                        " LP" : "- LP");
                            }
                        }
                    });
                }
            });

            return view;
        }

        private View getAddSummonerView(View recycledView, ViewGroup viewGroup) {
            addSummonerView = inflater.inflate(R.layout.fragment_add_search,
                    viewGroup, false);
            addSummonerView.setTag(-1);

            final EditText summonerName = addSummonerView.findViewById(R.id.add_name);
            final Button summonerAdd = addSummonerView.findViewById(R.id.add_button);

            View.OnClickListener addListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = summonerName.getText().toString().trim();

                    addSummoner(summonerName, name);
                }
            };

            summonerAdd.setOnClickListener(addListener);

            summonerName.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
            summonerName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView view, int i, KeyEvent keyEvent) {
                    String summonerName = handleEditorAction(view, i, keyEvent);
                    if (summonerName != null) {
                        addSummoner(view, summonerName);
                        return true;
                    }

                    return false;
                }
            });

            return addSummonerView;
        }

        private void addSummoner(final TextView view, String name) {
            Call<SummonerDTO> summonerRequest = apiService.getSummonerByName(name);

            RiotAPI.rateLimitRequest(summonerRequest, new Callback<SummonerDTO>() {
                @Override
                public void onResponse(Call<SummonerDTO> call,
                                       Response<SummonerDTO> response) {
                    Context context = addSummonerView.getContext();

                    SummonerDTO summoner = response.body();

                    if (summoner == null) {
                        UIHelper.showToast(context, "Summoner not found",
                                Toast.LENGTH_SHORT);
                        return;
                    }

                    searchHistory = RecentSearchStorage.add(context,
                            summoner.accountId, true);
                    reverseList(searchHistory);
                    notifyDataSetChanged();

                    view.setText("");
                }

                @Override
                public void onFailure(Call<SummonerDTO> call, Throwable t) {

                }
            });
        }

        public void refresh(Context context) {
            searchHistory = RecentSearchStorage.load(context);
            reverseList(searchHistory);
            notifyDataSetChanged();
        }
    }
}

interface OnRecentSearchClickListener {
    void OnRemoveClick(Long summonerId);
}
