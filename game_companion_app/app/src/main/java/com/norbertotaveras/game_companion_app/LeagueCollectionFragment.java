package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.norbertotaveras.game_companion_app.DTO.League.LeagueItemDTO;
import com.norbertotaveras.game_companion_app.DTO.League.LeagueListDTO;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link LeagueCollectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LeagueCollectionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_LEAGUELIST = "leagueList";

    // TODO: Rename and change types of parameters
    private LeagueListDTO leagueList;

    private TextView queueName;
    private ImageView tierIcon;
    private TextView tier;

    public LeagueCollectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param leagueList Collection of leagues
     * @return A new instance of fragment LeagueCollectionFragment.
     */
    public static LeagueCollectionFragment newInstance(LeagueListDTO leagueList) {
        LeagueCollectionFragment fragment = new LeagueCollectionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LEAGUELIST, leagueList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            leagueList = (LeagueListDTO)getArguments().getSerializable(ARG_LEAGUELIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_league_collection,
                container, false);

        queueName = view.findViewById(R.id.queue_name);
        tierIcon = view.findViewById(R.id.tier_icon);
        tier = view.findViewById(R.id.tier);

        queueName.setText(leagueList.queue);
        tier.setText(leagueList.tier);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
