package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.norbertotaveras.game_companion_app.DTO.Match.MatchDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantIdentityDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Norberto Taveras on 1/30/2018.
 */

public class MatchDetailsActivity extends AppCompatActivity {
    private SummonerDTO summoner;
    private MatchDTO match;
    private ParticipantDTO participant;
    private ParticipantIdentityDTO participantIdentity;

    private TextView gameMode;
    private TextView gameDate;
    private TextView gameDuration;
    private TextView winLoss;
    private RecyclerView summonerList;
    private SummonerListAdapter summonerListAdapter;
    private LinearLayoutManager summonerListLayoutManager;

    private Handler uiHandler;
    private AppBarLayout appBarLayout;

    private boolean backgroundSet = false;

    public static void start(Context context, SummonerDTO summoner, MatchDTO match) {
        Intent intent = new Intent(context, MatchDetailsActivity.class);
        intent.putExtra("summoner", summoner);
        intent.putExtra("match", match);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match_details);

        Intent intent = getIntent();
        summoner = (SummonerDTO)intent.getSerializableExtra("summoner");
        match = (MatchDTO)intent.getSerializableExtra("match");

        uiHandler = UIHelper.createRunnableLooper();

        participantIdentity = RiotAPI.participantIdentityFromSummoner(
                match.participantIdentities, summoner);

        participant = RiotAPI.participantFromParticipantId(
                match.participants, participantIdentity.participantId);

        appBarLayout = findViewById(R.id.appbar);

        gameMode = findViewById(R.id.game_mode);
        gameDate = findViewById(R.id.game_date);
        gameDuration = findViewById(R.id.game_duration);
        winLoss = findViewById(R.id.win_loss);

        summonerListLayoutManager = new LinearLayoutManager(this);
        summonerListAdapter = new SummonerListAdapter(
                match.participantIdentities, match.participants);
        summonerList = findViewById(R.id.summoner_list);
        summonerList.setAdapter(summonerListAdapter);
        summonerList.setLayoutManager(summonerListLayoutManager);
        summonerList.setHasFixedSize(false);

        gameMode.setText(RiotAPI.queueIdToQueueName(match.queueId));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        gameDate.setText(dateFormat.format(match.gameCreation));

        gameDuration.setText(RiotAPI.formatMinSec(match.gameDuration));

        boolean isRemake = RiotAPI.durationIsRemake(match.gameDuration);
        winLoss.setText(isRemake ? "Remake" : participant.stats.win ? "Victory" : "Defeat");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!backgroundSet) {
            setBackground(participant);
            backgroundSet = true;
        }
    }

    private void setBackground(final ParticipantDTO participant) {
        final Context context = this;
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                RiotAPI.setChampionSplashBackgroundByChampionId(context, uiHandler,
                        appBarLayout, participant.championId);
            }
        });
    }

    private class SummonerListItem
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private final View view;

        private ParticipantDTO participant;

        private final ImageView championIcon;
        private final TextView championLevel;
        private final TextView summonerName;
        private final TextView kda;
        private final TextView minionKills;
        private final TextView participation;

        private final ImageView[] spells;
        private final ImageView[] runes;
        private final ImageView[] items;

        private final TextView damageDone;
        private final TextView goldEarned;

        private final View playerDetails;

        private final HashSet<ParticipantDTO> expandedViews;

        public SummonerListItem(final View view, HashSet<ParticipantDTO> expandedViews) {
            super(view);
            this.view = view;
            this.expandedViews = expandedViews;

            championIcon = view.findViewById(R.id.champion_icon);
            championLevel = view.findViewById(R.id.champion_level);
            summonerName = view.findViewById(R.id.summoner_name);
            kda = view.findViewById(R.id.kda);
            minionKills = view.findViewById(R.id.minion_kills);
            participation = view.findViewById(R.id.participation);

            spells = new ImageView[] {
                    view.findViewById(R.id.spell0),
                    view.findViewById(R.id.spell1)
            };

            runes = new ImageView[] {
                    view.findViewById(R.id.rune0),
                    view.findViewById(R.id.rune1)
            };

            items = new ImageView[] {
                    view.findViewById(R.id.items0),
                    view.findViewById(R.id.items1),
                    view.findViewById(R.id.items2),
                    view.findViewById(R.id.items3),
                    view.findViewById(R.id.items4),
                    view.findViewById(R.id.items5),
                    view.findViewById(R.id.items6)
            };

            damageDone = view.findViewById(R.id.damage_done);
            goldEarned = view.findViewById(R.id.gold_earned);

            playerDetails = view.findViewById(R.id.player_detail);

            view.setOnClickListener(this);
        }

        public void bind(final ParticipantIdentityDTO participantIdentity,
                         final ParticipantDTO participant, final int rowId) {
            view.setTag(rowId);

            this.participant = participant;

            summonerName.setText(participantIdentity.player.summonerName);
            kda.setText(RiotAPI.formatKda(participant));
            minionKills.setText(getResources().getString(R.string.minion_kills,
                    participant.stats.totalMinionsKilled +
                participant.stats.neutralMinionsKilled));

            long totalKills = 0;
            for (ParticipantDTO scan : match.participants) {
                if (scan.teamId == participant.teamId)
                    totalKills += scan.stats.kills;
            }
            int pk = (int)Math.round(100.0 * (participant.stats.assists +
                    participant.stats.kills) / totalKills);

            participation.setText(getResources().getString(R.string.pkill, pk));

            RiotAPI.populateChampionIcon(view, rowId, uiHandler, championIcon, participant);
            RiotAPI.populateSpellIcons(view, rowId, uiHandler, spells, participant);
            RiotAPI.populateItemIcons(view, rowId, uiHandler, items, participant);
            RiotAPI.populateRuneIcons(view, rowId, uiHandler, runes, participant);

            championLevel.setText(String.valueOf(participant.stats.champLevel));

            damageDone.setText(getResources().getString(R.string.damage_done,
                    participant.stats.totalDamageDealt));
            goldEarned.setText(getResources().getString(R.string.gold_earned,
                    participant.stats.goldEarned));

            final boolean isRemake = RiotAPI.durationIsRemake(match.gameDuration);

            view.setBackgroundColor(isRemake
                    ? ContextCompat.getColor(view.getContext(), R.color.remakeColor)
                    : participant.stats.win
                    ? ContextCompat.getColor(view.getContext(), R.color.victoryColor)
                    : ContextCompat.getColor(view.getContext(), R.color.defeatColor));
            view.setBackgroundTintMode(PorterDuff.Mode.ADD);

            if (expandedViews.contains(participant))
                playerDetails.setVisibility(View.VISIBLE);
            else
                playerDetails.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View view) {
            boolean expanded = expandedViews.contains(participant);

            if (expanded) {
                playerDetails.setVisibility(View.GONE);
                expandedViews.remove(participant);
            } else {
                playerDetails.setVisibility(View.VISIBLE);
                expandedViews.add(participant);
            }
        }
    }

    private class SummonerListAdapter extends RecyclerView.Adapter<SummonerListItem> {
        private final LayoutInflater inflater;
        private final List<ParticipantIdentityDTO> identities;
        private final List<ParticipantDTO> players;
        private final AtomicInteger uniqueId;
        private final HashSet<ParticipantDTO> expandedViews;

        SummonerListAdapter(List<ParticipantIdentityDTO> identities,
                            List<ParticipantDTO> players)
        {
            this.identities = identities;
            this.players = players;
            inflater = getLayoutInflater();
            uniqueId = new AtomicInteger(0);
            expandedViews = new HashSet<>();
        }

        @Override
        public SummonerListItem onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.fragment_match_details_list,
                    parent, false);
            return new SummonerListItem(view, expandedViews);
        }

        @Override
        public void onBindViewHolder(SummonerListItem holder, int position) {
            holder.bind(identities.get(position), players.get(position),
                    uniqueId.getAndIncrement());
        }

        @Override
        public int getItemCount() {
            return players.size();
        }
    }
}
