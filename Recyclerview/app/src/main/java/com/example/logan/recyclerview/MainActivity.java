package com.example.logan.recyclerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ChampionsWinRate";
    
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: starting.");

        initImageBitmaps();
    }


    private void initImageBitmaps(){
        Log.d(TAG, "initImageBitmaps: preparing bitmaps.");
        mImageUrls.add("http://opgg-static.akamaized.net/images/lol/champion/Tryndamere.png?image=w_140&v=1");
        mNames.add("Tryndamere");

        mImageUrls.add("http://apollo-na-uploads.s3.amazonaws.com/1420537412050/ashe-league-of-legends-30090-1920x1080.jpg");
        mNames.add("Ashe");

        mImageUrls.add("http://www.behindthevoiceactors.com/_img/chars/jax-league-of-legends-4.27.jpg");
        mNames.add("Jax");

        mImageUrls.add("http://ddragon.leagueoflegends.com/cdn/7.24.2/img/champion/Rammus.png");
        mNames.add("Rammus");

        mImageUrls.add("https://www.mobafire.com/images/avatars/kayn-classic.png");
        mNames.add("Kayn");

        mImageUrls.add("https://www.mobafire.com/images/avatars/jhin-classic.png");
        mNames.add("Jhin");

        initRecyclerView();
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: initialized RecyclerView");
        RecyclerView recyclerView = findViewById(R.id.winrecyclerview);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, mNames, mImageUrls);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
