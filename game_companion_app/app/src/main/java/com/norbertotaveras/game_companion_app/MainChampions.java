package com.norbertotaveras.game_companion_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainChampions extends AppCompatActivity {

    private static final String TAG = "ChampionsWinRate";


    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mWinRates = new ArrayList<>(); //Needs api calls, use placeholders atm
    private ArrayList<String> mChampionPosition = new ArrayList<>(); //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_champions);
        Log.d(TAG, "onCreate: starting.");

        final Button btnTop = (Button) findViewById(R.id.topButton);
        btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float alpha = 0.45f;
                RecyclerView recyclerView = findViewById(R.id.winrecyclerview);
                recyclerView.getLayoutManager().scrollToPosition(0);
            }
        });

        initImageBitmaps();
    }



    private void initImageBitmaps(){
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");

        mImageUrls.add("https://www.mobafire.com/images/avatars/aurelion-sol-classic.png");
        mNames.add("Aurelion Sol");
        mWinRates.add("54.76%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.eposts.co/wp-content/uploads/2016/09/Evelynn_0.jpg");
        mNames.add("Evelynn");
        mWinRates.add("53.45%");
        mChampionPosition.add("Jungle");

        mImageUrls.add("https://vignette.wikia.nocookie.net/vsbattles/images/a/a8/Gangplank_2.png/revision/latest?cb=20161112021156");
        mNames.add("Gangplank");
        mWinRates.add("53.13%");
        mChampionPosition.add("Top");

        mImageUrls.add("https://www.mobafire.com/images/avatars/vladimir-classic.png");
        mNames.add("Vladimir");
        mWinRates.add("52.94%");
        mChampionPosition.add("Top | Mid");

        mImageUrls.add("http://www.behindthevoiceactors.com/_img/chars/jax-league-of-legends-4.27.jpg");
        mNames.add("Jax");
        mWinRates.add("50.89%");
        mChampionPosition.add("Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/avatars/talon-classic.png");
        mNames.add("Talon");
        mWinRates.add("50.44%");
        mChampionPosition.add("Middle");

        mImageUrls.add("http://ddragon.leagueoflegends.com/cdn/7.24.2/img/champion/Rammus.png");
        mNames.add("Rammus");
        mWinRates.add("50.34%");
        mChampionPosition.add("Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/avatars/kayn-classic.png");
        mNames.add("Kayn");
        mWinRates.add("49.73%");
        mChampionPosition.add("Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/avatars/zed-classic.png");
        mNames.add("Zed");
        mWinRates.add("48.98%");
        mChampionPosition.add("Middle");

        mImageUrls.add("https://www.mobafire.com/images/avatars/orianna-classic.png");
        mNames.add("Orianna");
        mWinRates.add("48.71%");
        mChampionPosition.add("Middle");

        mImageUrls.add("http://apollo-na-uploads.s3.amazonaws.com/1420537412050/ashe-league-of-legends-30090-1920x1080.jpg");
        mNames.add("Ashe");
        mWinRates.add("48.66%");
        mChampionPosition.add("ADC");

        mImageUrls.add("http://opgg-static.akamaized.net/images/lol/champion/Tryndamere.png?image=w_140&v=1");
        mNames.add("Tryndamere");
        mWinRates.add("47.74%");
        mChampionPosition.add("Top | Jungle");

        mImageUrls.add("https://www.mobafire.com/images/avatars/jhin-classic.png");
        mNames.add("Jhin");
        mWinRates.add("45.00%");
        mChampionPosition.add("ADC");

        mImageUrls.add("https://d181w3hxxigzvh.cloudfront.net/wp-content/uploads/2017/09/MissFortune_Splash_Tile_0.jpg");
        mNames.add("Miss Fortune");
        mWinRates.add("40.72%");
        mChampionPosition.add("ADC | Sup");

        mImageUrls.add("http://media.comicbook.com/2017/11/league-of-legends-zoe--1050934.jpg");
        mNames.add("Zoe");
        mWinRates.add("39.28%");
        mChampionPosition.add("Mid | Sup");

        initRecyclerView();
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: initialized RecyclerView");
        RecyclerView recyclerView = findViewById(R.id.winrecyclerview);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mNames, mImageUrls, mWinRates, mChampionPosition);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ;
    }
}
