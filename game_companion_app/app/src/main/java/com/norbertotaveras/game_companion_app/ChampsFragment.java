package com.norbertotaveras.game_companion_app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Norberto on 2/7/2018.
 */

public class ChampsFragment extends Fragment {
    public static ChampsFragment newInstance() {
        ChampsFragment fragment = new ChampsFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View view = getActivity().getLayoutInflater().inflate(
                R.layout.fragment_champs, container, false);

        return view;
    }
}
