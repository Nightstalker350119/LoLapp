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

public class MainActivity
        extends AppCompatActivity
        implements TextView.OnEditorActionListener, View.OnKeyListener {
    SummonerDTO summoner;

    TextView title1, title2;
    EditText search;

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
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
        search.setOnKeyListener(this);
        search.setOnEditorActionListener(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.search:
                switch (actionId) {
                    case IME_ACTION_DONE:
                        showSearchResult();
                        break;
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

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (v.getId()) {
            case R.id.search:
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    showSearchResult();
                    return true;
                }
        }
        return false;
    }
}