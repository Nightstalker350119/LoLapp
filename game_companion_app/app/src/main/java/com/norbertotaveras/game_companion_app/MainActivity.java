package com.norbertotaveras.game_companion_app;

import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.norbertotaveras.game_companion_app.ChampionPage.MainChampions;

/**
 * Created by Norberto Taveras on 1/30/2018.
 */

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        activateFragmentByNavigationId(R.id.navigation_home);
    }

    private void activateFragment(Fragment fragment) {
        getFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return activateFragmentByNavigationId(item.getItemId());
    }

    private boolean activateFragmentByNavigationId(int itemId) {
        Fragment fragment = null;

        switch (itemId) {
            case R.id.navigation_home:
                setTitle("Matches");
                fragment = MainFragment.newInstance();
                break;

            case R.id.navigation_dashboard:
                setTitle("Champions");
                fragment = MainChampions.newInstance();
                break;

            case R.id.navigation_notifications:
                break;

        }

        if (fragment != null)
            activateFragment(fragment);

        return fragment != null;
    }
}
