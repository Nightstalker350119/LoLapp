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

import com.norbertotaveras.game_companion_app.DTO.League.LeagueItemDTO;
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
    int position;

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

        LeagueListDTO currentLeague = leagueInfo.leagueList.get(position);
        queueName.setText(beautifyQueueName(currentLeague.queue));
        tierIcon.setImageResource(tierNameToResourceId(currentLeague.tier));

        LeaguePositionDTO leaguePosition = leagueInfo.leaguePositions.get(currentLeague.queue);
        tier.setText(beautifyTierName(currentLeague.tier) +
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

    private int tierNameToResourceId(String tierName) {
        switch (tierName) {
            case "SILVER": return R.drawable.silver;
            case "CHALLENGER": return R.drawable.challenger;
            case "DIAMOND": return R.drawable.diamond;
            case "GOLD": return R.drawable.gold;
            case "MASTER": return R.drawable.master;
            case "PLATINUM": return R.drawable.platinum;
            case "PROVISIONAL": return R.drawable.provisional;
            case "BRONZE": return R.drawable.bronze;
            default: return android.R.color.transparent;
        }
    }

    private String beautifyQueueName(String queueName) {
        queueName = transformQueueName(queueName);
        queueName = titleCaseFromUnderscores(queueName);
        queueName = queueName.replaceFirst("(\\d+)x(\\d+)$", "$1:$2");
        return queueName;
    }

    private String transformQueueName(String queueName) {
        queueName = queueName.replaceFirst("_SR$", "_5x5");
        queueName = queueName.replaceFirst("_TT$", "_3x3");
        return queueName;
    }

    private String beautifyTierName(String tierName) {
        return titleCaseFromUnderscores(tierName);
    }

    private String titleCaseFromUnderscores(String input) {
        String[] parts = input.split("_");
        StringBuilder sb = new StringBuilder(input.length() * 2);

        for (int i = 0; i < parts.length; ++i) {
            sb.append(parts[i].substring(0, 1).toUpperCase());
            sb.append(parts[i].substring(1).toLowerCase());
            if (i + 1 < parts.length)
                sb.append(' ');
        }

        return sb.toString();
    }
}
