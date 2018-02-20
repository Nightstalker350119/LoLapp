package com.norbertotaveras.game_companion_app.ChampionPage;

/**
 * Created by Emanuel on 12/7/2017.
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;

import com.norbertotaveras.game_companion_app.R;


public class ChampionsBanRate extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_championstbanrate, container, false);
        return rootView;
    }
}
