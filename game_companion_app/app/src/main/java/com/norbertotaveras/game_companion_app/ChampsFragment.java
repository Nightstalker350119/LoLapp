package com.norbertotaveras.game_companion_app;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.norbertotaveras.game_companion_app.DTO.ChampionMastery.ChampionMasteryDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionListDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;

/**
 * Created by Norberto on 2/7/2018.
 */

public class ChampsFragment extends Fragment {
    private RecyclerView.LayoutManager champListLayoutManager;
    private RecyclerView champList;
    private ChampionListAdapter champListAdapter;
    private Handler uiHandler;
    private SummonerDTO summoner;
    private RiotAPI.DeferredRequest<List<ChampionMasteryDTO>> deferredChampions;
    private RiotGamesService apiService;

    public ChampsFragment() {
    }

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

        champList = view.findViewById(R.id.champ_list);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        uiHandler = UIHelper.createRunnableLooper();
        apiService = RiotAPI.getInstance(getActivity());
        champListAdapter = new ChampionListAdapter();
        champListLayoutManager = new LinearLayoutManager(getActivity());
        champList.setAdapter(champListAdapter);
        champList.setLayoutManager(champListLayoutManager);
    }

    public void setSummoner(SummonerDTO summoner) {
        this.summoner = summoner;

        if (deferredChampions == null) {
            Call<List<ChampionMasteryDTO>> championRequest =
                    apiService.getChampionMasteriesByAccountId(summoner.id);
            deferredChampions = new RiotAPI.DeferredRequest<>(championRequest);
        }

        deferredChampions.getData(new RiotAPI.AsyncCallback<List<ChampionMasteryDTO>>() {
            @Override
            public void invoke(List<ChampionMasteryDTO> item) {
                champListAdapter.setList(item);
            }
        });
    }

    public void setSortOrder(Comparator<ChampionMasteryDTO> comparator) {
        champListAdapter.setSortOrder(comparator);
    }

    class ChampionListItem extends RecyclerView.ViewHolder {
        final View view;

        final ImageView icon;
        final TextView name;
        final TextView points;
        final ImageView level;

        AtomicInteger uniqueId;

        public ChampionListItem(View itemView) {
            super(itemView);
            view = itemView;

            icon = view.findViewById(R.id.champion_icon);
            name = view.findViewById(R.id.champion_name);
            points = view.findViewById(R.id.champion_points);
            level = view.findViewById(R.id.level);

            uniqueId = new AtomicInteger(0);
        }

        public void bind(final ChampionMasteryDTO championMastery) {
            final int rowId = uniqueId.getAndIncrement();

            view.setTag(rowId);

            icon.setImageDrawable(null);
            name.setText("");
            points.setText("");
            level.setImageDrawable(null);

            RiotAPI.fetchChampionIcon(championMastery.championId,
                    new RiotAPI.AsyncCallback<Drawable>() {
                @Override
                public void invoke(final Drawable item) {
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            icon.setImageDrawable(item);
                        }
                    });
                }
            });

            RiotAPI.getChampionList(new RiotAPI.AsyncCallback<ChampionListDTO>() {
                @Override
                public void invoke(final ChampionListDTO championList) {
                    if ((int)view.getTag() != rowId)
                        return;

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if ((int)view.getTag() != rowId)
                                return;

                            ChampionDTO match = null;
                            for (Map.Entry<String, ChampionDTO> champion :
                                    championList.data.entrySet()) {
                                if (champion.getValue().id == championMastery.championId) {
                                    match = champion.getValue();
                                    break;
                                }
                            }

                            if (match == null)
                                return;

                            name.setText(match.name);
                        }
                    });
                }
            });

            points.setText(String.valueOf(championMastery.championPoints));
            level.setImageResource(RiotAPI.championLevelToResourceId(
                    championMastery.championLevel));
        }
    }

    class ChampionListAdapter extends RecyclerView.Adapter<ChampionListItem> {
        LayoutInflater inflater;

        private final List<ChampionMasteryDTO> champions;

        public ChampionListAdapter() {
            inflater = LayoutInflater.from(getActivity());
            champions = new ArrayList<>();
        }

        public void setList(final Collection<ChampionMasteryDTO> items) {
            //notifyItemRangeRemoved(0, champions.size());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyItemRangeRemoved(0, champions.size());
                    champions.clear();
                    champions.addAll(items);
                    notifyItemRangeInserted(0, champions.size());
                    //notifyDataSetChanged();
                }
            });
        }

        @Override
        public ChampionListItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View holder;
            holder = inflater.inflate(R.layout.fragment_champ_list, parent, false);
            return new ChampionListItem(holder);
        }

        @Override
        public void onBindViewHolder(ChampionListItem holder, int position) {
            holder.bind(champions.get(position));
        }

        @Override
        public int getItemCount() {
            return champions.size();
        }

        public void setSortOrder(Comparator<ChampionMasteryDTO> comparator) {
            ChampionMasteryDTO[] temp = new ChampionMasteryDTO[champions.size()];
            champions.toArray(temp);
            Arrays.sort(temp, comparator);
            champions.clear();
            champions.addAll(Arrays.asList(temp));
            notifyItemRangeChanged(0, champions.size());
        }
    }
}
