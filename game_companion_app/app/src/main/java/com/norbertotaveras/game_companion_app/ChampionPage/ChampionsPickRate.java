package com.norbertotaveras.game_companion_app.ChampionPage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.norbertotaveras.game_companion_app.R;

/**
 * Created by Emanuel on 12/7/2017.
 */

public class ChampionsPickRate extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_championstpickrate, container, false);
        return rootView;
    }
}
