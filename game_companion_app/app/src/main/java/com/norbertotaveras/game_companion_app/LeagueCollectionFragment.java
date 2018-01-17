package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.norbertotaveras.game_companion_app.DTO.League.LeagueListDTO;
import com.norbertotaveras.game_companion_app.DTO.League.LeaguePositionDTO;

/**
 * Created by Norberto Taveras on 12/2/2017.
 */


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * Use the {@link LeagueCollectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LeagueCollectionFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_LEAGUE_INFO = "leagueInfo";
    public static final String ARG_POSITION = "position";

    private SummonerSearchResultsActivity.LeagueInfo leagueInfo;
    private int position;

    public LeagueCollectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param leagueInfo Collection of leagues.
     * @param position Position in the league list
     * @return A new instance of fragment LeagueCollectionFragment.
     */
    public static LeagueCollectionFragment newInstance(
            SummonerSearchResultsActivity.LeagueInfo leagueInfo,
            int position) {
        LeagueCollectionFragment fragment = new LeagueCollectionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_LEAGUE_INFO, leagueInfo);
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            leagueInfo = (SummonerSearchResultsActivity.LeagueInfo)
                    arguments.getSerializable(ARG_LEAGUE_INFO);
            position = arguments.getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_league_collection,
                container, false);

        TextView queueName;
        ImageView tierIcon;
        TextView tier;
        TextView winLoss;

        queueName = view.findViewById(R.id.queue_name);
        tierIcon = view.findViewById(R.id.tier_icon);
        tier = view.findViewById(R.id.tier);

        LeaguePositionDTO currentLeague = leagueInfo.leaguePositions.get(
                leagueInfo.leaguePositions.keySet().toArray()[position]);
        queueName.setText(RiotAPI.beautifyQueueName(currentLeague.queueType));
        tierIcon.setImageResource(RiotAPI.tierNameToResourceId(currentLeague.tier));

        LeaguePositionDTO leaguePosition = leagueInfo.leaguePositions.get(currentLeague.queueType);
        tier.setText(RiotAPI.beautifyTierName(currentLeague.tier) +
                " (" + String.valueOf(leaguePosition.leaguePoints) +"LP)");

        winLoss = view.findViewById(R.id.win_loss);
        int games = leaguePosition.wins + leaguePosition.losses;
        int winPercent = leaguePosition.losses != 0
            ? 100 * leaguePosition.wins / games
            : 0;
        winLoss.setText(String.format("%dW %dL %d%%",
                leaguePosition.wins, leaguePosition.losses, winPercent));

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
