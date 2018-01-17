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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.norbertotaveras.game_companion_app.DTO.League.LeaguePositionDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import org.w3c.dom.Text;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class MainActivity
        extends AppCompatActivity
        implements TextView.OnEditorActionListener, View.OnKeyListener,
        OnRecentSearchClickListener {
    SummonerDTO summoner;

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

        title1 = findViewById(R.id.title1);
        title2 = findViewById(R.id.title2);
        search = findViewById(R.id.search);
        recentSearches = findViewById(R.id.recent_searches);

        recentSearchesAdapter = new RecentSearchesAdapter(this);
        recentSearchesAdapter.setOnRecentSearchClickListener(this);
        recentSearches.setAdapter(recentSearchesAdapter);

        search.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
        //search.setOnKeyListener(this);
        search.setOnEditorActionListener(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        uiThreadHandler = UIHelper.createRunnableLooper();

        // Start fetching icon data and stuff while the user types their search
        apiService = RiotAPI.getInstance(getApplicationContext());
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.search:
                switch (actionId) {
                    case IME_ACTION_SEARCH:
                        showSearchResult();
                        break;
                    default:
                        Log.v("wtf", "onEditorAction key " + String.valueOf(event));
                        return onKey(v, event.getKeyCode(), event);
                }
        }
        return false;
    }

    private void showSearchResult() {
        String searchText = search.getText().toString();
        Intent resultsIntent;
        resultsIntent = new Intent(this, SummonerSearchResultsActivity.class);
        resultsIntent.putExtra("searchText", searchText);
        startActivity(resultsIntent);
    }

    private void showChampionResult()
    {
        Intent championIntent;
        championIntent = new Intent(this, ChampionsActivity.class);
        startActivity(championIntent);
    }

    private void showHomeResult()
    {
        Intent homeIntent;
        homeIntent = new Intent(this, MainActivity.class);
        startActivity(homeIntent);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (v.getId()) {
            case R.id.search:
                if (keyCode == KeyEvent.KEYCODE_ENTER &&
                        event.getAction() == KeyEvent.ACTION_DOWN) {
                    showSearchResult();
                    return true;
                }
        }
        return false;
    }

    @Override
    public void OnRecentSearchClick(Long summonerId) {

    }

    public void OnRemoveClick(Long summonerId) {
        RecentSearchStorage.remove(this, summonerId);
    }

    class RecentSearchesAdapter
            extends BaseAdapter
            implements AdapterView.OnItemClickListener {
        private OnRecentSearchClickListener listener;
        private final List<Long> searchHistory;
        private final LayoutInflater inflater;

        public RecentSearchesAdapter(Context context) {
            searchHistory = RecentSearchStorage.load(context);
            inflater = getLayoutInflater();
        }

        public void setOnRecentSearchClickListener(OnRecentSearchClickListener listener) {
            this.listener = listener;
        }

        @Override
        public int getCount() {
            return searchHistory != null ? searchHistory.size() : 0;
        }

        @Override
        public Object getItem(int i) {
            return searchHistory != null ? searchHistory.get(i) : null;
        }

        @Override
        public long getItemId(int i) {
            return searchHistory != null ? searchHistory.get(i) : null;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = inflater.inflate(R.layout.fragment_recent_search,
                        viewGroup, false);
            }

            final ImageView profileIcon = view.findViewById(R.id.profile_icon);
            final TextView summonerName = view.findViewById(R.id.summoner_name);
            final TextView summonerLevel = view.findViewById(R.id.level);

            final LinearLayout[] queueContainers = new LinearLayout[] {
                    view.findViewById(R.id.queue_summary_0),
                    view.findViewById(R.id.queue_summary_1),
                    view.findViewById(R.id.queue_summary_2)
            };

            final TextView[] queueNames = new TextView[] {
                    view.findViewById(R.id.queue_name_0),
                    view.findViewById(R.id.queue_name_1),
                    view.findViewById(R.id.queue_name_2)
            };

            final ImageView[] tierIcons = new ImageView[] {
                    view.findViewById(R.id.tier_icon_0),
                    view.findViewById(R.id.tier_icon_1),
                    view.findViewById(R.id.tier_icon_2)
            };

            final TextView[] ranks = new TextView[] {
                    view.findViewById(R.id.rank_0),
                    view.findViewById(R.id.rank_1),
                    view.findViewById(R.id.rank_2)
            };

            final TextView[] leaguePoints = new TextView[] {
                    view.findViewById(R.id.league_points_0),
                    view.findViewById(R.id.league_points_1),
                    view.findViewById(R.id.league_points_2)
            };

            final TextView remove = view.findViewById(R.id.remove);

            final Long summonerId = searchHistory.get(position);

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.OnRemoveClick(summonerId);
                    searchHistory.remove(summonerId);
                    notifyDataSetChanged();
                }
            });

            final Call<SummonerDTO> summonerRequest = apiService.getSummonerById(summonerId);

            RiotAPI.cachedRequest(summonerRequest, new RiotAPI.AsyncCallback<SummonerDTO>() {
                @Override
                public void invoke(final SummonerDTO summoner) {
                    if (summoner == null) {
                        Log.e("RecentSearch",
                                "Summoner " + String.valueOf(summonerId) + " not found");
                        return;
                    }

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            summonerName.setText(summoner.name);
                            summonerLevel.setText("Lvl " + String.valueOf(summoner.summonerLevel));
                        }
                    });

                    RiotAPI.fetchProfileIcon(summoner.profileIconId,
                            new RiotAPI.AsyncCallback<Drawable>() {
                        @Override
                        public void invoke(final Drawable item) {
                            uiThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    profileIcon.setImageDrawable(item);
                                }
                            });
                        }
                    });
                }
            });

            Call<List<LeaguePositionDTO>> leaguePositionRequest =
                    apiService.getLeaguePositions(summonerId);

            RiotAPI.cachedRequest(leaguePositionRequest,
                    new RiotAPI.AsyncCallback<List<LeaguePositionDTO>>() {
                @Override
                public void invoke(List<LeaguePositionDTO> positions) {
                    LeaguePositionDTO[] lookup = new LeaguePositionDTO[3];

                    for (LeaguePositionDTO lp : positions) {
                        switch (lp.queueType) {
                            case "RANK_SOLO_5v5":
                                lookup[0] = lp;
                                break;

                            case "RANK_FLEX_3v3":
                                lookup[1] = lp;
                                break;

                            case "RANK_FLEX_5v5":
                                lookup[2] = lp;
                                break;

                        }
                    }

                    for (int i = 0; i < 3; ++i) {
                        LeaguePositionDTO item = lookup[i];

                        if (item != null) {
                            queueContainers[i].setVisibility(View.VISIBLE);
                        } else {
                            queueContainers[i].setVisibility(View.GONE);
                            continue;
                        }

                        queueNames[i].setText(RiotAPI.beautifyQueueName(item.queueType));

                        tierIcons[i].setImageResource(RiotAPI.tierNameToResourceId(item.tier));
                        ranks[i].setText(item.rank);
                        leaguePoints[i].setText(String.valueOf(item.leaguePoints));
                    }
                }
            });

            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            if (listener != null)
                listener.OnRecentSearchClick(id);
        }
    }
}

interface OnRecentSearchClickListener {
    void OnRecentSearchClick(Long summonerId);
    void OnRemoveClick(Long summonerId);
}
