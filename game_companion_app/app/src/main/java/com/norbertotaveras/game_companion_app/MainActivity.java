package com.norbertotaveras.game_companion_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import retrofit2.Call;
import retrofit2.Callback;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH;

/**
 * Created by Norberto Taveras on 11/28/2017.
 */

public class MainActivity
        extends AppCompatActivity
        implements TextView.OnEditorActionListener, View.OnKeyListener {
    SummonerDTO summoner;

    TextView title1, title2;
    EditText search;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
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

        search.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
        //search.setOnKeyListener(this);
        search.setOnEditorActionListener(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Start fetching icon data and stuff while the user types their search
        RiotAPI.getInstance();
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
}
