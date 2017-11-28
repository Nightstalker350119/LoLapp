package com.norbertotaveras.game_companion_app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.norbertotaveras.game_companion_app.Summoner.SummonerDTO;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    Retrofit retroFit;
    RiotGamesServices apiService;
    static final String riotApiKey = "RGAPI-0d7654a4-d8b6-4be4-ac30-dda50f460c40";

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRiotAPI(riotApiKey);

        try {
            Call<SummonerDTO> test = apiService.getSummonersByName("NTaveras");

            test.enqueue(new Callback<SummonerDTO>() {
                @Override
                public void onResponse(Call<SummonerDTO> call, retrofit2.Response<SummonerDTO> response) {
                    SummonerDTO testBody = response.body();
                    Log.d("riottest", String.format("accountid = %d", testBody.accountId));
                }

                @Override
                public void onFailure(Call<SummonerDTO> call, Throwable t) {
                    Log.e("riottest", String.format("async request failed = %s", t));
                }
            });
        }
        catch (Exception ex) {
            Log.e("riottest", String.format("request completely failed = %s", ex));
        }

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void initRiotAPI(final String riotApiKey) {
        OkHttpClient.Builder httpClient =
                new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();

                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("api_key", riotApiKey)
                        .build();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .url(url);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        retroFit = new Retrofit.Builder()
                .client(httpClient.build())
                .baseUrl("https://na1.api.riotgames.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retroFit.create(RiotGamesServices.class);
    }
}
